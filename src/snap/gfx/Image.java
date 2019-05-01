/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.*;
import snap.util.*;
import snap.web.*;

/**
 * Represents an Image, such as JPEG, PNG, GIF, TIFF, BMP.
 */
public abstract class Image {

     // The image source
     Object           _source;
     
     // The image source URL
     WebURL           _url;
     
     // The image type
     String           _type;
     
     // The cached width/height
     double           _width = -1, _height = -1;
     
     // The pixel width/height
     int              _pw = -1, _ph = -1;
     
     // The X/Y DPI
     double           _dpiX = -1, _dpiY = -1;
     
     // Whether image has alpha
     Boolean          _hasAlpha;
     
     // The image source bytes
     byte             _bytes[];
     
     // Whether the image is loaded
     boolean          _loaded = true;;
     
     // The decoded bytes
     byte             _bytesRGB[], _bytesRGBA[];

     // The image set, if animated image
     ImageSet         _imgSet;

    // PropertyChangeSupport
    PropChangeSupport _pcs = PropChangeSupport.EMPTY, _loadLsnr;

    // Supported image type strings
    static String     _types[] = { "gif", "jpg", "jpeg", "png", "tif", "tiff", "bmp" };

    // Constants for properties
    public static final String Loaded_Prop = "Name";

/**
 * Returns the name of image (if from URL/file).
 */
public String getName()  { WebURL url = getSourceURL(); return url!=null? url.getPathName() : null; }

/**
 * Returns the source.
 */
public Object getSource()  { return _source; }

/**
 * Sets the source.
 */
protected void setSource(Object aSource)  { _source = aSource; }

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()
{
    if(_url!=null) return _url;
    return _url = WebURL.getURL(_source);
}

/**
 * Returns the width of given image.
 */
public double getWidth()  { return _width>=0? _width : (_width=getWidthImpl()); }

/**
 * Returns the height of given image.
 */
public double getHeight()  { return _height>=0? _height : (_height=getHeightImpl()); }

/**
 * Returns the width of given image in pixels.
 */
public int getPixWidth()  { return _pw>=0? _pw : (_pw = getPixWidthImpl()); }

/**
 * Returns the height of given image in pixels.
 */
public int getPixHeight()  { return _ph>=0? _ph : (_ph = getPixHeightImpl()); }

/**
 * Returns the horizontal image DPI.
 */
public double getDPIX()  { return _dpiX>=0? _dpiX : (_dpiX=getDPIXImpl()); }

/**
 * Returns the vertical image DPI.
 */
public double getDPIY()  { return _dpiY>=0? _dpiY : (_dpiY=getDPIYImpl()); }

/**
 * Returns whether image has alpha.
 */
public boolean hasAlpha()  { return _hasAlpha!=null? _hasAlpha : (_hasAlpha=hasAlphaImpl()); }

/**
 * Returns the native object.
 */
public abstract Object getNative();

/**
 * Returns the width of given image.
 */
protected double getWidthImpl()  { return getPixWidth()*72/getDPIX(); }

/**
 * Returns the height of given image.
 */
protected double getHeightImpl()  { return getPixHeight()*72/getDPIY(); }

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
public byte[] getBytes()  { return _bytes!=null? _bytes : (_bytes=getBytesImpl()); }

/**
 * Returns the source bytes.
 */
protected byte[] getBytesImpl()
{
    // Get bytes for URL and return
    WebURL url = getSourceURL();
    if(url!=null)
        return url.getBytes();
        
    // Get bytes for source and return
    byte bytes[] = SnapUtils.getBytes(_source);
    if(bytes!=null)
        return bytes;
        
    // If image created from scratch or native, generate bytes
    if(getNative()!=null)
        return hasAlpha()? getBytesPNG() : getBytesJPEG();
    
    // Complain and return
    System.err.println("Image.getBytes: Image bytes not found for source " + _source);
    return null;
}

/**
 * Returns the type of the image bytes provided.
 */
public String getType()  { return _type!=null? _type : (_type=ImageUtils.getImageType(getBytes())); }

/**
 * Returns whether image is loaded (might be delayed is source is URL).
 */
public boolean isLoaded()  { return _loaded; }

/**
 * Sets whether image is loaded.
 */
protected void setLoaded(boolean aValue)
{
    if(aValue==_loaded) return;
    _width = _height = -1;
    firePropChange(Loaded_Prop, _loaded, _loaded=aValue);
    if(aValue && _loadLsnr!=null) {
        _loadLsnr.firePropChange(new PropChange(this, Loaded_Prop, false, true)); _loadLsnr = null; }
}

/**
 * Returns an RGB integer for given x, y.
 */
public abstract int getRGB(int aX, int aY);

/**
 * Returns the decoded RGB bytes of this image.
 */
public byte[] getBytesRGB()  { return _bytesRGB!=null? _bytesRGB : (_bytesRGB=getBytesRGBImpl()); }

/**
 * Returns the decoded RGBA bytes of this image.
 */
public byte[] getBytesRGBA()  { return _bytesRGBA!=null? _bytesRGBA : (_bytesRGBA=getBytesRGBAImpl()); }

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
 * Returns a new image scaled by given percent.
 */
public Image getImageScaled(double aRatio)
{
    int w = (int)Math.round(getPixWidth()*aRatio), h = (int)Math.round(getPixHeight()*aRatio);
    Image img2 = Image.get(w, h, hasAlpha());
    img2.getPainter().drawImage(this, 0, 0, getWidth(), getHeight(), 0, 0, w, h);
    return img2;
}

/**
 * Returns a subimage from rectangle.
 */
public Image getSubimage(double aX, double aY, double aW, double aH)
{
    Image img2 = Image.get((int)Math.round(aW), (int)Math.round(aH), hasAlpha());
    img2.getPainter().drawImage(this, aX, aY, aW, aH, 0, 0, aW, aH);
    return img2;
}

/**
 * Returns an image inside a larget image.
 */
public Image getFramedImage(int aW, int aH, double aX, double aY)
{
    Image img2 = Image.get(aW, aH, hasAlpha());
    Painter pntr = img2.getPainter(); pntr.drawImage(this, aX, aY);
    return img2;
}

/**
 * Returns an image with ImageSet for given number of frames (assumes this is horizontal sprite sheet).
 */
public Image getSpriteSheetFrames(int aCount)
{
    List <Image> images = new ArrayList(aCount); int w = getPixWidth()/aCount;
    for(int i=0;i<aCount;i++) {
        Image img = getSubimage(i*w,0,w,getPixHeight());
        images.add(img);
    }
    ImageSet iset = new ImageSet(images);
    return iset.getImage(0);
}

/**
 * Blurs the image by mixing pixels with those around it to given radius.
 */
public void blur(int aRad, Color aColor)  { System.err.println("Image.blur: Not impl"); }

/**
 * Embosses the image by mixing pixels with those around it to given radius.
 */
public void emboss(double aRadius, double anAzi, double anAlt)  { System.err.println("Image.emboss: Not impl"); }

/**
 * Adds a load listener. This is cleared automatically when image is loaded.
 */
public void addLoadListener(PropChangeListener aLoadLsnr)
{
    if(isLoaded()) { aLoadLsnr.propertyChange(new PropChange(this, Loaded_Prop, false, true)); return; }
    if(_loadLsnr==null) _loadLsnr = new PropChangeSupport(this);
    _loadLsnr.addPropChangeListener(aLoadLsnr);
}

/**
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aPCL)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aPCL);
}

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal)
{
    if(!_pcs.hasListener(aProp)) return;
    _pcs.firePropChange(new PropChange(this, aProp, oldVal, newVal));
}

/**
 * Creates a new image from source.
 */
public static Image get(Object aSource)  { return GFXEnv.getEnv().getImage(aSource); }

/**
 * Creates a new image from class and resource path.
 */
public static Image get(Class aClass, String aPath)
{
    WebURL url = WebURL.getURL(aClass, aPath);
    if(url==null)
        url = WebURL.getURL(aClass, "pkg.images/" + aPath);
    return url!=null? Image.get(url) : null;
}

/**
 * Returns the image from URL and resource path.
 */
public static Image get(WebURL aBaseURL, String aName)
{
    if(aBaseURL==null || aName==null) return null;
    WebFile file = aBaseURL.getFile(); if(file==null) return null;
    WebFile dir = file.isDir()? file : file.getParent(); if(dir==null) return null;
    WebFile ifile = dir.getFile(aName);
    if(ifile==null) ifile = dir.getFile("pkg.images/" + aName); if(ifile==null) return null;
    return get(ifile);
}

/**
 * Creates a new image for width, height and alpha.
 */
public static Image get(int aWidth, int aHeight, boolean hasAlpha)
{
    return GFXEnv.getEnv().getImage(aWidth,aHeight,hasAlpha);
}

/**
 * Returns whether given extension is supported.
 */
public static boolean canRead(String anExt)  { return StringUtils.containsIC(_types, anExt.toLowerCase()); }

/**
 * Returns whether image reader can read the file provided in the byte array.
 */
public static boolean canRead(byte bytes[])  { return ImageUtils.getImageType(bytes)!=null; }

}