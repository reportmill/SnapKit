/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.Map;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/**
 * PDFShadingPattern.
 */
public abstract class PDFPatternShading extends PDFPattern implements PaintContext, Paint {

    // Transform
    AffineTransform      xform;
    
    // Extended GState
    Map                  extGState;
    
    // Colorspace
    ColorSpace           space;
    
    // Background
    float                background[];
    
    // Bounds
    Rectangle2D          bounds;
    
    // Whether to antialias
    boolean              antialias;
    
    // Shading color mode
    int                  shadingColorMode;
    
/**
 * Create an instance from a shading dictionary or a pattern dictionary.
 */
public static PDFPatternShading getInstance(Map dict, PDFFile srcFile)
{
    Object v = srcFile.getXRefObj(dict.get("Shading")); // TODO:BUG - what about shading resources that are pdfstreams?
    Map shadingDict, patternDict;
    PDFPatternShading pat;
    
    // If the dictionary is a pattern dictionary, it will have
    // a /Shading subdictionary.  If no /Shading key is found, this
    // routine assumes the input dictionary already is a Shading dictionary
    if (v instanceof Map) {
        shadingDict = (Map)v;
        patternDict = dict;
    }
    else if (v instanceof PDFStream) {
        shadingDict = ((PDFStream)v).getDict();
        patternDict = null;
    }
    else {
        shadingDict = dict;
        patternDict = null;
    }

    v=shadingDict.get("ShadingType");
    if (!(v instanceof Number))
        throw new PDFException("Invalid shading definition");
    
    int t = ((Number)v).intValue();
    switch (t) {
        case 1 : pat = new PDFPatternShadingFunction(patternDict, shadingDict, srcFile); break;
        case 2 : pat = new PDFPatternShadingAxial(patternDict, shadingDict, srcFile); break;
        case 3 : pat = new PDFPatternShadingRadial(patternDict, shadingDict, srcFile); break;
        default : throw new PDFException("Type "+t+" shading patterns not implemented");
    }
    
    return pat;
}


/** Constructor - patternDict may be null if this object was created from the shading operator (sh). */
public PDFPatternShading(Map patternDict, Map shadingDict, PDFFile srcFile)
{
    super();
    // Get pattern-specific parameters
    if (patternDict != null) {
        xform = PDFDictUtils.getTransform(patternDict, srcFile, "Matrix");
        if (xform==null)
            xform = new AffineTransform();
        extGState = (Map)srcFile.getXRefObj(patternDict.get("ExtGState"));
    }
    else
        xform = new AffineTransform();
    
    // Get all the shading parameters
    initializeShadingParameters(shadingDict, srcFile);
}

/** Read the shading parameters */
public void initializeShadingParameters(Map shadingDict, PDFFile srcFile)
{
    // Get parameters common to all shading types
    
    background = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Background");
    bounds = PDFDictUtils.getRectangle(shadingDict, srcFile, "BBox");
    Object a = shadingDict.get("AntiAlias");
    if (a instanceof Boolean) 
        antialias = ((Boolean)a).booleanValue();
    else
        antialias=false;

    // subclasses should override this and call super.initializeParameters()
}

/** Sets the transform from user space to device space */
public void setDeviceTransform(AffineTransform x, Rectangle devRect) {}

/** Returns the pattern space transform */
public AffineTransform getTransform() { return xform; }
public Rectangle2D getBounds() { return bounds; }

public static final int DeviceRGBShading=0;
public static final int DeviceGrayShading=1;
public static final int ArbitraryColorSpaceShading=2;

public void setColorSpace(ColorSpace c) { 
    // Cache a flag for use inside shading loop indicating how to
    // convert color values to samples.  DeviceRGB & DeviceGray
    // can set the sample directly.  Anything else has to 
    // get converted by the ColorSpace object into RGB.
    // According to the spec, certain colorspaces aren't supported
    // by Acrobat (like /Indexed), although in principle there's
    // no reason why they couldn't be (if you felt like writing dithering
    // routines and all that)
    // If an unsupported colorspace is used in a shading, the 
    // colorspace will eventually throw an exception when toRGB() gets called.
    if (c instanceof PDFDeviceRGB)
        shadingColorMode=DeviceRGBShading;
    else if (c.getType()==ColorSpace.TYPE_GRAY) 
        shadingColorMode=DeviceGrayShading;
    else {
        shadingColorMode=ArbitraryColorSpaceShading;
        space=c;
    }
}

/**
 * This routine will get called for every pixel in the inner loop of the shading.  If that gets too slow, you could
 * always try to inline it. sample_values are assumed to to have the right number of elements in the
 * right range for the colorspace.
 */
public int getRGBAPixel(float sample_values[])
{
    int pixel;
    
    switch(shadingColorMode) {
    case DeviceGrayShading : pixel = ((int)(255*sample_values[0])) * 0x010101; break;
    case ArbitraryColorSpaceShading : sample_values = space.toRGB(sample_values); // fall through
    case DeviceRGBShading :
        pixel = (((int)(255*sample_values[0]))<<16) |
                (((int)(255*sample_values[1]))<<8) |
                 ((int)(255*sample_values[2]));
        break;
    default: pixel = 0; // mostly to shut up the compiler
    }
    
    // all pixels are fully opaque
    return pixel | 0xff000000;
}

// Paint & PaintContext interface methods
public Paint getPaint()  { return this; }

String PR(Rectangle2D r) { return "["+r.getX()+" "+r.getY()+" "+r.getWidth()+" "+r.getHeight()+"]";}
String PT(Point2D p) { return "{"+p.getX()+", "+p.getY()+"}"; }

/** Paint & PaintContext interfaces */
public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
    AffineTransform xform, RenderingHints hints)
{
    setDeviceTransform(xform,deviceBounds);
    return this;
}

public void dispose() {}

public Raster getRaster(int x, int y, int w, int h)
{
// Allocate an ARGB raster and pass the sample buffer to the shading implementation
    DataBufferInt dbuf = new DataBufferInt(w*h);
    WritableRaster r = Raster.createPackedRaster(dbuf, w, h, w, new int[]{0xff0000,0xff00,0xff,0xff000000}, new Point(0,0));
    int samples[] = dbuf.getData();

    doShading(samples,x,y,w,h);    
    return r;
}

/** Alpha & color definitions */
public int getTransparency() { return TRANSLUCENT; }

public ColorModel getColorModel()
{   // ARGB
    return new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);
}

/**
 * Subclasses should implement this method to draw the shading. Samples is defined to be an array of
 * 8 bit persample/4 samples per pixel ARGB pixels. There is no padding, so all scanlines are w integers wide.
 */
public abstract void doShading(int samples[], int x, int y, int w, int h);

}