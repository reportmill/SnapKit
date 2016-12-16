/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.*;
import snap.web.*;

/**
 * Represents an Image, such as JPEG, PNG, GIF, TIFF, BMP.
 */
public abstract class Image extends Object {

     // The image source
     Object          _source;
     
     // The image source URL
     WebURL          _url;
     
     // The image source bytes
     byte            _bytes[];
     
     // The image type
     String          _type;
     
     // The native image
     Object          _native;
     
     // The cached width/height
     double          _width = -1, _height = -1;

    // Supported image type strings
    static String    _types[] = { "gif", "jpg", "jpeg", "png", "tif", "tiff", "bmp" };

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
    try { _url = WebURL.getURL(_source); } catch(Exception e) { }
    return _url;
}

/**
 * Returns the width of given image.
 */
public double getWidth()  { return _width>=0? _width : (_width=getPixWidth()*72/getWidthDPI()); }

/**
 * Returns the height of given image.
 */
public double getHeight()  { return _height>=0? _height : (_height=getPixHeight()*72/getHeightDPI()); }

/**
 * Returns the width of given image.
 */
public double getWidthDPI()  { return 72; }

/**
 * Returns the height of given image.
 */
public double getHeightDPI()  { return 72; }

/**
 * Returns the width of given image in pixels.
 */
public abstract int getPixWidth();

/**
 * Returns the height of given image in pixels.
 */
public abstract int getPixHeight();

/**
 * Returns whether image has alpha.
 */
public abstract boolean hasAlpha();

/**
 * Returns whether the image is non-grayscale.
 */
public boolean isColor()  { return isIndexedColor() || getSamplesPerPixel()>2; }

/**
 * Returns number of components.
 */
public abstract int getSamplesPerPixel();

/**
 * Returns the number of bits per sample.
 */
public abstract int getBitsPerSample();

/**
 * Returns the number of bits per pixel (derived from bits per sample and samples per pixel).
 */
public int getBitsPerPixel()  { return getBitsPerSample()*getSamplesPerPixel(); }

/**
 * Returns the number of bytes per row (derived from width and bits per pixel).
 */
public int getBytesPerRow()  { return (getPixWidth()*getBitsPerPixel()+7)/8; }

/**
 * Returns whether index color model.
 */
public abstract boolean isIndexedColor();

/**
 * Color map support: returns the bytes of color map from a color map image.
 */
public abstract byte[] getColorMap();

/**
 * Color map support: returns the index of the transparent color in a color map image.
 */
public abstract int getAlphaColorIndex();

/**
 * Returns the source bytes.
 */
public byte[] getBytes()  { return _bytes!=null? _bytes : (_bytes=getBytesImpl()); }

/**
 * Returns the source bytes.
 */
public byte[] getBytesImpl()
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
 * Returns an RGB integer for given x, y.
 */
public abstract int getRGB(int aX, int aY);

/**
 * Returns the ARGB array of this image.
 */
public int[] getArrayARGB()  { System.err.println("Image.getArrayARGB: Not implemented"); return null; }

/**
 * Returns the decoded RGBA bytes of this image.
 */
public abstract byte[] getBytesRGBA();

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
 * Returns a subimage from rectangle.
 */
public Image getSubimage(double aX, double aY, double aW, double aH)
{
    Image simg = Image.get((int)Math.round(aW), (int)Math.round(aH), hasAlpha());
    simg.getPainter().drawImage(this,-aX,-aY,aW,aH);
    return simg;
}

/**
 * Returns whether image data is premultiplied.
 */
public abstract boolean isPremultiplied();

/**
 * Sets whether image data is premultiplied.
 */
public abstract void setPremultiplied(boolean aValue);

/**
 * Blurs the image by mixing pixels with those around it to given radius.
 */
public void blur(int aRad)
{
    // Make image premultiplied
    setPremultiplied(true);
    
    // Get image data (and temp data)
    int w = getPixWidth(), h = getPixHeight();
    int spix[] = getArrayARGB(), tpix[] = new int[w*h];

    // Apply 1D gausian kernal for speed, as horizontal, then vertical (order = 2*rad instead of rad^2)
    float kern1[] = GFXUtils.getGaussianKernel(aRad,0); // size = aRad*2+1 x 1
    GFXUtils.convolve(spix, tpix, w, h, kern1, aRad*2+1);  // Horizontal 1D, kern size = aRad*2+1 x 1
    GFXUtils.convolve(tpix, spix, w, h, kern1, 1);         // Vertical 1D, kern size = 1 x aRad*2+1
    
    // Convert blur image to non-premultiplied and return
    setPremultiplied(false);
}

/**
 * Embosses the image by mixing pixels with those around it to given radius.
 */
public void emboss(double aRadius, double anAzi, double anAlt)
{
    // Get basic info
    int w = getPixWidth(), h = getPixHeight();
    int radius = (int)Math.round(aRadius), rad = Math.abs(radius);
    
    // Create bump map: original graphics offset by radius, blurred. Color doesn't matter - only alpha channel used.
    Image bumpImg = Image.get(w+rad*2, h+rad*2, true);
    Painter ipntr = bumpImg.getPainter(); ipntr.setImageQuality(1); //ipntr.clipRect(0, 0, width, height);
    ipntr.drawImage(this, rad, rad, w, h);
    ipntr.flush();
    bumpImg.blur(rad);

    // Get source and bump pixels as int arrays and call general emboss method
    int spix[] = getArrayARGB(); if(spix==null) return;
    int bpix[] = bumpImg.getArrayARGB();
    GFXUtils.emboss(spix, bpix, w, h, radius, anAzi*Math.PI/180, anAlt*Math.PI/180);
}

/**
 * Returns the native object.
 */
public abstract Object getNative();

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