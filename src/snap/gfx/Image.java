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
    private Object _source;

    // The image source URL
    private WebURL _url;

    // The image type
    private String _type;

    // The cached width/height
    protected double _width, _height;

    // The pixel width/height
    protected int _pixW, _pixH;

    // Whether image has alpha
    protected boolean _hasAlpha;

    // The X/Y DPI
    protected int _dpiX = 72, _dpiY = 72;

    // The dpi scale (1 = normal, 2 = retina/hidpi)
    protected int _dpiScale = 1;

    // The image source bytes
    private byte[]  _bytes;

    // Whether the image is loaded
    private boolean _loaded = true;

    // The decoded bytes
    private byte[]  _bytesRGB;

    // The decoded bytes with alpha
    private byte[]  _bytesRGBA;

    // The image set, if animated image
    private ImageSet  _imgSet;

    // Loadable Support
    private Loadable.Support  _loadLsnrs = new Loadable.Support(this);

    // Whether waiting for image load
    private boolean _waitingForImageLoad;

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
        WebURL url = WebURL.getUrl(_source);
        return _url = url;
    }

    /**
     * Returns the width of given image.
     */
    public double getWidth()  { return _width; }

    /**
     * Returns the height of given image.
     */
    public double getHeight()  { return _height; }

    /**
     * Returns the width of given image in pixels.
     */
    public int getPixWidth()  { return _pixW; }

    /**
     * Returns the height of given image in pixels.
     */
    public int getPixHeight()  { return _pixH; }

    /**
     * Returns whether image has alpha.
     */
    public boolean hasAlpha()  { return _hasAlpha; }

    /**
     * Returns the horizontal image DPI.
     */
    public double getDpiX()  { return _dpiX; }

    /**
     * Returns the vertical image DPI.
     */
    public double getDpiY()  { return _dpiY; }

    /**
     * Returns the image dpi scale (1 = 72 dpi, 2 = Retina/HiDPI).
     */
    public double getDpiScale()  { return _dpiScale; }

    /**
     * Returns the native object.
     */
    public abstract Object getNative();

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

        // If setting, fire prop change, fire load listeners
        if (aValue)
            fireLoadListeners();

        // If another thread is waiting for image load, wake thread
        if (_waitingForImageLoad)
            wakeForImageLoad();
    }

    /**
     * Override to wait.
     */
    public synchronized void waitForImageLoad()
    {
        if (!isLoaded()) {
            try {
                _waitingForImageLoad = true;
                wait();
                _waitingForImageLoad = false;
            }
            catch (Exception e) { System.out.println("CJImage.waitForImageLoad: Failure: " + e.getMessage()); }
        }
    }

    /**
     * Stop wait.
     */
    private synchronized void wakeForImageLoad()
    {
        notify();
    }

    /**
     * Returns an RGB integer for given x, y.
     */
    public abstract int getRGB(int aX, int aY);

    /**
     * Sets an RGB integer for given x, y.
     */
    public void setRGB(int aX, int aY, int rgb)
    {
        Painter pntr = getPainter();
        pntr.setComposite(Painter.Composite.SRC_IN);
        pntr.setColor(new Color(rgb));
        pntr.fillRect(aX, aY, 1, 1);
        pntr.setComposite(Painter.Composite.SRC_OVER);
    }

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
     * Returns a copy of this image scaled by given percent.
     */
    public Image copyForScale(double aRatio)
    {
        int newW = (int) Math.round(getPixWidth() * aRatio);
        int newH = (int) Math.round(getPixHeight() * aRatio);
        return copyForSize(newW, newH);
    }

    /**
     * Returns a copy of this image at new size.
     */
    public Image copyForSize(double newW, double newH)
    {
        double dpiScale = getDpiScale();
        return copyForSizeAndDpiScale(newW, newH, dpiScale);
    }

    /**
     * Returns a copy of this image at new dpi scale.
     */
    public Image copyForDpiScale(double dpiScale)
    {
        double imageW = getWidth();
        double imageH = getHeight();
        return copyForSizeAndDpiScale(imageW, imageH, dpiScale);
    }

    /**
     * Returns a copy of this image at new size and dpi scale.
     */
    public Image copyForSizeAndDpiScale(double newW, double newH, double dpiScale)
    {
        Image cloneImage = Image.getImageForSizeAndDpiScale(newW, newH, hasAlpha(), dpiScale);
        Painter pntr = cloneImage.getPainter();
        pntr.setImageQuality(1);
        pntr.drawImage(this, 0, 0, newW, newH);
        return cloneImage;
    }

    /**
     * Returns a subimage from rectangle.
     */
    public Image copyForCropRect(double aX, double aY, double newW, double newH)
    {
        Image cloneImage = Image.getImageForSize(Math.round(newW), Math.round(newH), hasAlpha());
        Painter pntr = cloneImage.getPainter();
        pntr.drawImage(this, aX, aY, newW, newH, 0, 0, newW, newH);
        return cloneImage;
    }

    /**
     * Returns an image inside a larger image.
     */
    public Image getFramedImage(int newW, int newH, double imageX, double imageY)
    {
        Image cloneImage = Image.getImageForSize(newW, newH, hasAlpha());
        Painter pntr = cloneImage.getPainter();
        pntr.drawImage(this, imageX, imageY);
        return cloneImage;
    }

    /**
     * Returns an image with ImageSet for given number of frames (assumes this is horizontal sprite sheet).
     */
    public Image getSpriteSheetFrames(int aCount)
    {
        List<Image> images = new ArrayList<>(aCount);
        int w = getPixWidth() / aCount;
        for (int i = 0; i < aCount; i++) {
            Image img = copyForCropRect(i * w, 0, w, getPixHeight());
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
        StringBuilder sb = new StringBuilder();
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
    public static Image getImageForSource(Object aSource)
    {
        return GFXEnv.getEnv().getImageForSource(aSource);
    }

    /**
     * Creates image from bytes.
     */
    public static Image getImageForBytes(byte[] theBytes)
    {
        return GFXEnv.getEnv().getImageForSource(theBytes);
    }

    /**
     * Creates image from class and resource path.
     */
    public static Image getImageForClassResource(Class<?> aClass, String aPath)
    {
        WebURL url = WebURL.getResourceUrl(aClass, aPath);
        if (url == null)
            url = WebURL.getResourceUrl(aClass, "pkg.images/" + aPath);
        return url != null ? Image.getImageForSource(url) : null;
    }

    /**
     * Creates image from URL and resource path.
     */
    public static Image getImageForUrl(WebURL imageUrl)  { return GFXEnv.getEnv().getImageForSource(imageUrl); }

    /**
     * Creates image from URL and resource path.
     */
    public static Image getImageForUrlResource(WebURL aBaseURL, String aName)
    {
        // If either param is null, just return
        if (aBaseURL == null || aName == null) return null;

        // Get BaseURL directory
        WebFile file = aBaseURL.getFile();
        if (file == null)
            return null;
        WebFile dir = file.isDir() ? file : file.getParent();
        if (dir == null)
            return null;

        // Get directory file for name
        WebFile imageFile = dir.getFileForName(aName);
        if (imageFile == null)
            imageFile = dir.getFileForName("pkg.images/" + aName);
        if (imageFile == null)
            return null;

        // Return image for file
        return getImageForSource(imageFile);
    }

    /**
     * Creates image for width, height and alpha at standard 72 dpi.
     */
    public static Image getImageForSize(double aWidth, double aHeight, boolean hasAlpha)
    {
        return getImageForSizeAndDpiScale(aWidth, aHeight, hasAlpha, 1);
    }

    /**
     * Creates image for width, height, alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public static Image getImageForSizeAndDpiScale(double aWidth, double aHeight, boolean hasAlpha, double aScale)
    {
        return GFXEnv.getEnv().getImageForSizeAndDpiScale(aWidth, aHeight, hasAlpha, aScale);
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

    @Deprecated
    public Image cloneForSize(double newW, double newH) { return copyForSize(newW, newH); }
    @Deprecated
    public Image cloneForCropRect(double aX, double aY, double newW, double newH) { return copyForCropRect(aX, aY, newW, newH); }
}