/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

import snap.util.FormatUtils;
import snap.util.Loadable;
import snap.util.SnapUtils;
import snap.util.StringUtils;
import snap.web.WebFile;
import snap.web.WebURL;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Image, such as JPEG, PNG, GIF, TIFF, BMP.
 */
public abstract class Image implements Loadable {

    // The image source
    private Object  _source;

    // The image source URL
    private WebURL  _url;

    // The image type
    private String  _type;

    // The cached width/height
    private double  _width = -1, _height = -1;

    // The pixel width/height
    private int  _pw = -1, _ph = -1;

    // The X/Y DPI
    private double  _dpiX = -1, _dpiY = -1;

    // Whether image has alpha
    private Boolean  _hasAlpha;

    // The image source bytes
    private byte[]  _bytes;

    // Whether the image is loaded
    private boolean  _loaded = true;

    // The decoded bytes
    private byte[]  _bytesRGB;

    // The decoded bytes with alpha
    private byte[]  _bytesRGBA;

    // The image set, if animated image
    private ImageSet  _imgSet;

    // Loadable Support
    private Loadable.Support  _loadLsnrs = new Loadable.Support(this);

    // Supported image type strings
    private static String[]  _types = {"gif", "jpg", "jpeg", "png", "tif", "tiff", "bmp"};

    /**
     * Constructor.
     */
    protected Image()
    {
    }

    /**
     * Returns the name of image (if from URL/file).
     */
    public String getName()
    {
        WebURL url = getSourceURL();
        return url != null ? url.getFilename() : null;
    }

    /**
     * Returns the source.
     */
    public Object getSource()  { return _source; }

    /**
     * Sets the source.
     */
    protected void setSource(Object aSource)
    {
        // Probably shouldn't allow this here
        if (aSource instanceof byte[])
            _bytes = (byte[]) aSource;

        // Set source
        else _source = aSource;
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()
    {
        // If already set, just return
        if (_url != null) return _url;
        if (_source == null) return null;

        // Get, set, return
        WebURL url = WebURL.getURL(_source);
        return _url = url;
    }

    /**
     * Returns the width of given image.
     */
    public double getWidth()
    {
        if (_width >= 0) return _width;
        return _width = getWidthImpl();
    }

    /**
     * Returns the height of given image.
     */
    public double getHeight()
    {
        if (_height >= 0) return _height;
        return _height = getHeightImpl();
    }

    /**
     * Returns the width of given image in pixels.
     */
    public int getPixWidth()
    {
        if (_pw >= 0) return _pw;
        return _pw = getPixWidthImpl();
    }

    /**
     * Returns the height of given image in pixels.
     */
    public int getPixHeight()
    {
        if (_ph >= 0) return _ph;
        return _ph = getPixHeightImpl();
    }

    /**
     * Returns the horizontal image DPI.
     */
    public double getDPIX()
    {
        if (_dpiX >= 0) return _dpiX;
        return _dpiX = getDPIXImpl();
    }

    /**
     * Returns the vertical image DPI.
     */
    public double getDPIY()
    {
        if (_dpiY >= 0) return _dpiY;
        return _dpiY = getDPIYImpl();
    }

    /**
     * Returns whether image has alpha.
     */
    public boolean hasAlpha()
    {
        if (_hasAlpha != null) return _hasAlpha;
        return _hasAlpha = hasAlphaImpl();
    }

    /**
     * Returns the native object.
     */
    public abstract Object getNative();

    /**
     * Returns the width of given image.
     */
    protected double getWidthImpl()
    {
        return getPixWidth() * 72 / getDPIX();
    }

    /**
     * Returns the height of given image.
     */
    protected double getHeightImpl()
    {
        return getPixHeight() * 72 / getDPIY();
    }

    /**
     * Returns the width of given image in pixels.
     */
    protected abstract int getPixWidthImpl();

    /**
     * Returns the height of given image in pixels.
     */
    protected abstract int getPixHeightImpl();

    /**
     * Returns the width of given image.
     */
    protected double getDPIXImpl()  { return 72; }

    /**
     * Returns the height of given image.
     */
    protected double getDPIYImpl()  { return 72; }

    /**
     * Returns whether image has alpha.
     */
    protected abstract boolean hasAlphaImpl();

    /**
     * Returns the source bytes.
     */
    public byte[] getBytes()
    {
        // If already set, just return
        if (_bytes != null) return _bytes;

        // Get, set, return
        byte[] bytes = getBytesImpl();
        return _bytes = bytes;
    }

    /**
     * Returns the source bytes.
     */
    protected byte[] getBytesImpl()
    {
        // Get bytes for URL and return
        WebURL url = getSourceURL();
        if (url != null)
            return url.getBytes();

        // Get bytes for source and return
        byte[] bytes = SnapUtils.getBytes(_source);
        if (bytes != null)
            return bytes;

        // If image created from scratch or native, generate bytes
        if (getNative() != null)
            return hasAlpha() ? getBytesPNG() : getBytesJPEG();

        // Complain and return
        System.err.println("Image.getBytes: Image bytes not found for source " + _source);
        return null;
    }

    /**
     * Returns the type of the image bytes provided.
     */
    public String getType()
    {
        // If already set, just return
        if (_type != null) return _type;

        // Get, set, return
        String type = ImageUtils.getImageType(getBytes());
        return _type = type;
    }

    /**
     * Returns whether image is loaded (might be delayed is source is URL).
     */
    public boolean isLoaded()  { return _loaded; }

    /**
     * Sets whether image is loaded.
     */
    public void setLoaded(boolean aValue)
    {
        // If already set, just return
        if (aValue == _loaded) return;
        _loaded = aValue;

        // If setting, reset size, fire prop change, fire load listeners
        if (aValue) {
            _width = _height = -1;
            fireLoadListeners();
        }
    }

    /**
     * Returns an RGB integer for given x, y.
     */
    public abstract int getRGB(int aX, int aY);

    /**
     * Returns the decoded RGB bytes of this image.
     */
    public byte[] getBytesRGB()
    {
        if (_bytesRGB != null) return _bytesRGB;
        return _bytesRGB = getBytesRGBImpl();
    }

    /**
     * Returns the decoded RGBA bytes of this image.
     */
    public byte[] getBytesRGBA()
    {
        if (_bytesRGBA != null) return _bytesRGBA;
        return _bytesRGBA = getBytesRGBAImpl();
    }

    /**
     * Returns the decoded RGB bytes of this image.
     */
    protected abstract byte[] getBytesRGBImpl();

    /**
     * Returns the decoded RGBA bytes of this image.
     */
    protected abstract byte[] getBytesRGBAImpl();

    /**
     * Returns the JPEG bytes for image.
     */
    public abstract byte[] getBytesJPEG();

    /**
     * Returns the PNG bytes for image.
     */
    public abstract byte[] getBytesPNG();

    /**
     * Returns a painter to mark up image.
     */
    public abstract Painter getPainter();

    /**
     * Returns the image set.
     */
    public ImageSet getImageSet()
    {
        getNative();
        return _imgSet;
    }

    /**
     * Sets the image set.
     */
    protected void setImageSet(ImageSet anIS)  { _imgSet = anIS; }

    /**
     * Returns the image scale.
     */
    public double getScale()
    {
        return getDPIX() != 72 ? getDPIX() / 72 : 1;
    }

    /**
     * Returns a copy of this image at new size and scale.
     */
    public Image cloneForSizeAndScale(double aW, double aH, double aScale)
    {
        Image img2 = Image.getImageForSizeAndScale(aW, aH, hasAlpha(), aScale);
        Painter pntr = img2.getPainter();
        pntr.setImageQuality(1);
        pntr.drawImage(this, 0, 0, aW, aH);
        return img2;
    }

    /**
     * Returns a new image scaled by given percent.
     */
    public Image getImageScaled(double aRatio)
    {
        int newW = (int) Math.round(getPixWidth() * aRatio);
        int newH = (int) Math.round(getPixHeight() * aRatio);
        return getImageForSize(newW, newH);
    }

    /**
     * Returns a new image at given size.
     */
    public Image getImageForSize(double aW, double aH)
    {
        int newW = (int) Math.round(aW);
        int newH = (int) Math.round(aH);
        Image img2 = Image.get(newW, newH, hasAlpha());
        Painter pntr = img2.getPainter();
        pntr.setImageQuality(1);
        pntr.drawImage(this, 0, 0, getWidth(), getHeight(), 0, 0, newW, newH);
        return img2;
    }

    /**
     * Returns a subimage from rectangle.
     */
    public Image getSubimage(double aX, double aY, double aW, double aH)
    {
        Image img2 = Image.get((int) Math.round(aW), (int) Math.round(aH), hasAlpha());
        img2.getPainter().drawImage(this, aX, aY, aW, aH, 0, 0, aW, aH);
        return img2;
    }

    /**
     * Returns an image inside a larget image.
     */
    public Image getFramedImage(int aW, int aH, double aX, double aY)
    {
        Image img2 = Image.get(aW, aH, hasAlpha());
        Painter pntr = img2.getPainter();
        pntr.drawImage(this, aX, aY);
        return img2;
    }

    /**
     * Returns an image with ImageSet for given number of frames (assumes this is horizontal sprite sheet).
     */
    public Image getSpriteSheetFrames(int aCount)
    {
        List<Image> images = new ArrayList<>(aCount);
        int w = getPixWidth() / aCount;
        for (int i = 0; i < aCount; i++) {
            Image img = getSubimage(i * w, 0, w, getPixHeight());
            images.add(img);
        }
        ImageSet iset = new ImageSet(images);
        return iset.getImage(0);
    }

    /**
     * Blurs the image by mixing pixels with those around it to given radius.
     */
    public void blur(int aRad, Color aColor)
    {
        System.err.println("Image.blur: Not impl");
    }

    /**
     * Embosses the image by mixing pixels with those around it to given radius.
     */
    public void emboss(double aRadius, double anAzi, double anAlt)
    {
        System.err.println("Image.emboss: Not impl");
    }

    /**
     * Adds a load listener (cleared automatically when loaded).
     */
    public void addLoadListener(Runnable aLoadLsnr)
    {
        _loadLsnrs.addLoadListener(aLoadLsnr);
    }

    /**
     * Triggers calls to load listeners.
     */
    public void fireLoadListeners()
    {
        _loadLsnrs.fireListeners();
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        // Append Width, Height
        StringBuffer sb = new StringBuffer();
        sb.append(" Width:").append(FormatUtils.formatNum("#.##", getWidth()));
        sb.append(", Height:").append(FormatUtils.formatNum("#.##", getHeight()));

        // Append PixWidth, PixHeight
        if (getPixWidth() != getWidth())
            sb.append(", PixWidth:").append(getPixWidth());
        if (getPixHeight() != getHeight())
            sb.append(", PixHeight:").append(getPixHeight());

        // Apend Name, SourceURL
        if (getName() != null)
            sb.append(", Name:\"").append(getName()).append("\"");
        if (getSourceURL() != null)
            sb.append(", URL:").append(getSourceURL());

        // Return
        return sb.toString();
    }

    /**
     * Creates image from source.
     */
    public static Image get(Object aSource)
    {
        return GFXEnv.getEnv().getImage(aSource);
    }

    /**
     * Creates image from bytes.
     */
    public static Image getImageForBytes(byte[] theBytes)
    {
        return GFXEnv.getEnv().getImage(theBytes);
    }

    /**
     * Creates image from class and resource path.
     */
    public static Image get(Class aClass, String aPath)
    {
        WebURL url = WebURL.getURL(aClass, aPath);
        if (url == null)
            url = WebURL.getURL(aClass, "pkg.images/" + aPath);
        return url != null ? Image.get(url) : null;
    }

    /**
     * Creates image from URL and resource path.
     */
    public static Image get(WebURL aBaseURL, String aName)
    {
        // If either param is null, just return
        if (aBaseURL == null || aName == null) return null;

        // Get BaseURL directory
        WebFile file = aBaseURL.getFile();
        if (file == null) return null;
        WebFile dir = file.isDir() ? file : file.getParent();
        if (dir == null) return null;

        // Get directory file for name
        WebFile ifile = dir.getFileForName(aName);
        if (ifile == null)
            ifile = dir.getFileForName("pkg.images/" + aName);
        if (ifile == null) return null;

        // Return image for file
        return get(ifile);
    }

    /**
     * Creates image for width, height and alpha at 72 dpi.
     */
    public static Image get(int aWidth, int aHeight, boolean hasAlpha)
    {
        return getImageForSizeAndScale(aWidth, aHeight, hasAlpha, 1);
    }

    /**
     * Creates image for width, height and alpha at screen dpi scale (72 dpi normal, 144 dpi for retina/hidpi).
     */
    public static Image getImageForSize(double aWidth, double aHeight, boolean hasAlpha)
    {
        return getImageForSizeAndScale(aWidth, aHeight, hasAlpha, 0);
    }

    /**
     * Creates image for width, height, alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public static Image getImageForSizeAndScale(double aWidth, double aHeight, boolean hasAlpha, double aScale)
    {
        return GFXEnv.getEnv().getImageForSizeAndScale(aWidth, aHeight, hasAlpha, aScale);
    }

    /**
     * Returns whether given extension is supported.
     */
    public static boolean canRead(String anExt)
    {
        return StringUtils.containsIC(_types, anExt.toLowerCase());
    }

    /**
     * Returns whether image reader can read the file provided in the byte array.
     */
    public static boolean canRead(byte[] bytes)
    {
        return ImageUtils.getImageType(bytes) != null;
    }
}