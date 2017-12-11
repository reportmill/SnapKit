package snap.pdf.read;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Map;
import snap.gfx.ColorSpace;
import snap.pdf.*;

/**
 * PDFPattern subclasses.
 */
public class PDFPatterns {

/*
 * PDFTilingPattern.java. Created Dec 15, 2005. Copyright (c) 2005 by Joshua Doenias
 */
public static class Tiling extends PDFPattern {
    int paintType, tilingType;
    Rectangle2D bounds;
    AffineTransform xform;
    float xstep, ystep;
    Map resources;
    byte pdfData[];
    TexturePaint tile;
  
    /** Create new PDFPattern Tiling. */
    public Tiling(PDFStream pstream, PDFFile srcFile)
    {
        Map pmap = pstream.getDict();
        
        paintType = PDFDictUtils.getInt(pmap, srcFile, "PaintType");
        tilingType = PDFDictUtils.getInt(pmap, srcFile, "TilingType");
        bounds = PDFDictUtils.getRectangle(pmap, srcFile, "BBox");
        xstep = PDFDictUtils.getFloat(pmap, srcFile, "XStep");
        ystep = PDFDictUtils.getFloat(pmap, srcFile, "YStep");
        xform = PDFDictUtils.getTransform(pmap, srcFile, "Matrix");
        if(xform==null) xform = new AffineTransform();
        
        resources = (Map)srcFile.getXRefObj(pmap.get("Resources"));
        pdfData = pstream.decodeStream();
    }
    
    public AffineTransform getTransform()  { return xform; }
    public Rectangle2D getBounds()  { return bounds; }
    public Map getResources()  { return resources; }
    public byte[] getContents()  { return pdfData; }
    
    public void setTile(BufferedImage timage)
    {
        Rectangle2D b = getBounds();
        Rectangle2D anchor = new Rectangle2D.Float((float)b.getX(), (float)b.getY(), xstep, ystep);
        tile = new TexturePaint(timage, anchor); pdfData = null; resources = null;
    }
    
    public Paint getPaint()  { return tile; }
}

/**
 * PDFShadingPattern.
 */
public static abstract class Shading extends PDFPattern implements PaintContext, Paint {

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
    
    /** Create an instance from a shading dictionary or a pattern dictionary. */
    public static Shading getInstance(Map dict, PDFFile srcFile)
    {
        Object v = srcFile.getXRefObj(dict.get("Shading")); // TODO:BUG - what about shading resources that are streams?
        Map shadingDict, patternDict = null;
        
        // If dictionary is a pattern dictionary, it will have /Shading subdictionary.  If no /Shading key is found,
        // this routine assumes the input dictionary already is a Shading dictionary
        if(v instanceof Map) { shadingDict = (Map)v; patternDict = dict; }
        else if(v instanceof PDFStream) shadingDict = ((PDFStream)v).getDict();
        else shadingDict = dict;
    
        v = shadingDict.get("ShadingType");
        if(!(v instanceof Number))
            throw new PDFException("Invalid shading definition");
        
        int t = ((Number)v).intValue();
        switch (t) {
            case 1 : return new PDFPatterns.ShadingFunction(patternDict, shadingDict, srcFile);
            case 2 : return new PDFPatterns.ShadingAxial(patternDict, shadingDict, srcFile);
            case 3 : return new PDFPatterns.ShadingRadial(patternDict, shadingDict, srcFile);
            default : throw new PDFException("Type "+t+" shading patterns not implemented");
        }
    }
    
    /** Constructor - patternDict may be null if this object was created from the shading operator (sh). */
    public Shading(Map ptrnDict, Map shdgDict, PDFFile srcFile)
    {
        // Get pattern-specific parameters
        if (ptrnDict != null) {
            xform = PDFDictUtils.getTransform(ptrnDict, srcFile, "Matrix");
            if(xform==null) xform = new AffineTransform();
            extGState = (Map)srcFile.getXRefObj(ptrnDict.get("ExtGState"));
        }
        else xform = new AffineTransform();
        
        // Get all the shading parameters
        initializeShadingParameters(shdgDict, srcFile);
    }
    
    /** Read the shading parameters */
    public void initializeShadingParameters(Map shadingDict, PDFFile srcFile)
    {
        // Get parameters common to all shading types
        background = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Background");
        bounds = PDFDictUtils.getRectangle(shadingDict, srcFile, "BBox");
        Object a = shadingDict.get("AntiAlias");
        antialias = a instanceof Boolean && ((Boolean)a).booleanValue();
    
        // subclasses should override this and call super.initializeParameters()
    }
    
    /** Sets the transform from user space to device space */
    public void setDeviceTransform(AffineTransform x, Rectangle devRect) { }
    
    /** Returns the pattern space transform */
    public AffineTransform getTransform() { return xform; }
    public Rectangle2D getBounds() { return bounds; }
    
    public static final int DeviceRGBShading=0;
    public static final int DeviceGrayShading=1;
    public static final int ArbitraryColorSpaceShading=2;
    
    /**
     * Cache a flag for use inside shading loop indicating how to convert color values to samples. DeviceRGB &
     * DeviceGray can set the sample directly.  Anything else has to get converted by the ColorSpace object into RGB.
     * According to the spec, certain colorspaces aren't supported by Acrobat (like /Indexed), although in principle
     * there's no reason why they couldn't be (if you felt like writing dithering routines and all that)
     * If an unsupported colorspace is used in a shading, the colorspace will eventually throw an exception when
     * toRGB() gets called.
     */
    public void setColorSpace(ColorSpace c)
    { 
        if (c instanceof PDFColorSpaces.DeviceRGB) shadingColorMode = DeviceRGBShading;
        else if (c.getType()==ColorSpace.TYPE_GRAY) shadingColorMode = DeviceGrayShading;
        else { shadingColorMode = ArbitraryColorSpaceShading; space = c; }
    }
    
    /**
     * This routine will get called for every pixel in the inner loop of the shading.  If that gets too slow, you could
     * always try to inline it. sample_values are assumed to to have the right number of elements in the
     * right range for the colorspace.
     */
    public int getRGBAPixel(float sample_vals[])
    {
        int pixel;
        switch(shadingColorMode) {
            case DeviceGrayShading : pixel = ((int)(255*sample_vals[0])) * 0x010101; break;
            case ArbitraryColorSpaceShading : sample_vals = space.toRGB(sample_vals); // fall through
            case DeviceRGBShading :
                pixel = (((int)(255*sample_vals[0]))<<16) | (((int)(255*sample_vals[1]))<<8) | ((int)(255*sample_vals[2]));
                break;
            default: pixel = 0; // mostly to shut up the compiler
        }
        
        // all pixels are fully opaque
        return pixel | 0xff000000;
    }
    
    // Paint & PaintContext interface methods
    public Paint getPaint()  { return this; }
    
    /** Paint & PaintContext interfaces */
    public PaintContext createContext(ColorModel cm, Rectangle devBnds, Rectangle2D usrBnds,
        AffineTransform xform, RenderingHints hints)
    {
        setDeviceTransform(xform,devBnds); return this;
    }
    
    public Raster getRaster(int x, int y, int w, int h)
    {
        // Allocate an ARGB raster and pass the sample buffer to the shading implementation
        DataBufferInt dbuf = new DataBufferInt(w*h); int scanlineStride[] = {0xff0000, 0xff00, 0xff, 0xff000000 };
        WritableRaster r = Raster.createPackedRaster(dbuf, w, h, w, scanlineStride, new Point(0,0));
        int samples[] = dbuf.getData();
    
        doShading(samples,x,y,w,h);    
        return r;
    }
    
    /** Alpha & color definitions */
    public int getTransparency() { return TRANSLUCENT; }
    
    // ARGB
    public ColorModel getColorModel()  { return new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000); }
    
    /**
     * Subclasses should implement this method to draw the shading. Samples is defined to be an array of
     * 8 bit persample/4 samples per pixel ARGB pixels. There is no padding, so all scanlines are w integers wide.
     */
    public abstract void doShading(int samples[], int x, int y, int w, int h);
    public void dispose()  { }  // Really??
}

/**
 * Implementation of Type 2 shadings, which vary color along a linear axis.
 * 
 * The shading takes a pixel in the area to be to filled and drops a perpendicular to the axis defined by coords[].
 * It then calculates the parametric point t of the intersection, with t running from domain[0] to domain[1] along
 * the axis.  t is then turned into a color value using the supplied pdf function.
 * TODO: currently ignores transform
 */
public static class ShadingAxial extends PDFPatterns.Shading {
    PDFFunction func;
    boolean extend[];
    double Ax, Ay, Bx, By, BAx, BAy, denom, tMin,tMax,tScale;

    public ShadingAxial(Map ptrnDict, Map shdgDict, PDFFile srcFile)  { super(ptrnDict, shdgDict, srcFile); }
    
    /** Read the shading parameters */
    public void initializeShadingParameters(Map shadingDict, PDFFile srcFile)
    {
      super.initializeShadingParameters(shadingDict, srcFile);
      float coords[] = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Coords");
      float domain[] = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Domain");
      if(domain==null) domain = new float[]{0,1};
      func = PDFFunction.getInstance(srcFile.getXRefObj(shadingDict.get("Function")), srcFile);
      extend = PDFDictUtils.getBoolArray(shadingDict, srcFile, "Extend");
      if(extend==null) extend = new boolean[]{false, false};
    
      // A & B are the starting & ending points of the line segment.
      Ax = coords[0]; Ay = coords[1]; Bx = coords[2]; By = coords[3];
    
      // Mapping the distance along the line to the domain
      tMin = domain[0]; tMax = domain[1];
    }
    
    public void doShading(int samples[], int x, int y, int w, int h)
    {
        // For every point P in the raster, find point t along AB where dotProduct(A-B, P-AB(t)) = 0
        int sindex = 0; float t[] = new float[1];
        for(int j=0; j<h; ++j) {
            double PAy = (y+j-Ay)*BAy;
            double PAx = x-Ax;
            for(int i=0; i<w; ++i) {
                t[0] = (float)(tMin+(tScale*(BAx*PAx+PAy)));
                samples[sindex] = getRGBAPixel(func.evaluate(t));
                ++sindex;
                ++PAx;
             }
        }
    }
    
    /** Sets the transform from user space to device space */
    public void setDeviceTransform(AffineTransform x, java.awt.Rectangle devRect)
    {
        // transform original line into device coords and recalculate values
        double pts[] = { Ax, Ay, Bx, By };
        x.transform(pts, 0, pts, 0, 2);
        Ax = pts[0]; Ay = pts[1]; BAx = pts[2]-pts[0]; BAy = pts[3]-pts[1];
        denom = BAx*BAx+BAy*BAy; tScale = (tMax-tMin)/denom;
    }
}

/**
 * Function-based (type 1) shadings
 * x,y values in shading space are just passed to the function and the returned color value is plotted.
 */
public static class ShadingFunction extends PDFPatterns.Shading {
    AffineTransform shading_xform;
    float domain[];
    PDFFunction func;
    
    public ShadingFunction(Map ptrnDict, Map shdgDict, PDFFile srcFile)  { super(ptrnDict, shdgDict, srcFile); }
    
    /** Read the shading parameters */
    public void initializeShadingParameters(Map shadingDict, PDFFile srcFile)
    {
      super.initializeShadingParameters(shadingDict, srcFile);
      // Not sure about this - there are 2 matrices here. One inherited from Pattern, and this one from shading dict
      shading_xform = PDFDictUtils.getTransform(shadingDict, srcFile, "Matrix");
      if(shading_xform==null) shading_xform = new AffineTransform();
      domain = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Domain");
      if(domain==null) domain = new float[]{0,1,0,1};
      func = PDFFunction.getInstance(srcFile.getXRefObj(shadingDict.get("Function")), srcFile);
      if(func==null)
          throw new PDFException("Error reading shading function");
      //TODO: Should check that the number of outputs of the function match the colorspace
    }
    
    public void doShading(int samples[], int x, int y, int w, int h)
    {
        // Map x,y of pixel into domain of function and set pixel to result of function.
        int sindex = 0;
        float domainxScale = (domain[1]-domain[0])/w;
        float domainyScale = (domain[3]-domain[2])/h;
        float coord[] = new float[2]; coord[1] = domain[2];
        for(int j=0; j<h; ++j) {
            coord[0] = domain[0];
            for(int i=0; i<w; ++i) {
                samples[sindex] = getRGBAPixel(func.evaluate(coord)); sindex++;
                coord[0] += domainxScale;
             }
            coord[1] += domainyScale;
        }
    }
}

/**
 * A concrete subclass of PDFShadingPattern which implements pdf radial (type 3) shadings.
 * 
 * PDF radial shadings are defined by two circles and a function. The circles need not be centered at same location.
 * The shading works by interpolating the centers and radii of the two circles.  The distance along line defined by
 * the two centers or radii is then mapped into the domain of the function, which returns the final color values.
 * TODO: currently ignores transform and background color
 */
public static class ShadingRadial extends PDFPatterns.Shading {

    float domain[];
    PDFFunction func;
    boolean extend[];
    float dx, dy, dr;
    float x0, y0, r0;
    float pA;
    
    /** Creates new ShadingRadial. */
    public ShadingRadial(Map ptrnDict, Map shdgDict, PDFFile srcFile)  { super(ptrnDict, shdgDict, srcFile); }
    
    /** Read the shading parameters */
    public void initializeShadingParameters(Map shadingDict, PDFFile srcFile)
    {
        super.initializeShadingParameters(shadingDict, srcFile);
        float coords[] = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Coords");
        x0 = coords[0]; y0 = coords[1]; r0 = coords[2];
        dx = coords[3]-x0; dy = coords[4]-y0; dr = coords[5]-r0;
        pA = dx*dx+dy*dy-dr*dr;
      
        domain = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Domain");
        if(domain!=null && domain[0]==0 && domain[1]==1) domain = null;
        func = PDFFunction.getInstance(srcFile.getXRefObj(shadingDict.get("Function")), srcFile);
        extend = PDFDictUtils.getBoolArray(shadingDict, srcFile, "Extend");
        if(extend==null) extend = new boolean[] {false, false};
        if(xform.isIdentity()) xform = null;
    }
    
    /** Sets the transform from user space to device space */
    public void setDeviceTransform(AffineTransform x, java.awt.Rectangle devRect)
    {
        // transform original line into device coords and recalculate values
        float pts[] = {x0, y0, x0+dx, y0+dy};
        x.transform(pts, 0, pts, 0, 2);
        x0 = pts[0]; y0 = pts[1];
        dx = pts[2]-pts[0]; dy = pts[3]-pts[1];
        
        // skew or non-uniform scale will do something strange here
        float rpts[] = {0,0,r0,0,r0+dr,0};
        x.transform(rpts, 0, rpts, 0, 3);
        r0=(float)Math.sqrt((rpts[0]-rpts[2])*(rpts[0]-rpts[2])+(rpts[1]-rpts[3])*(rpts[1]-rpts[3]));
        dr=(float)Math.sqrt((rpts[0]-rpts[4])*(rpts[0]-rpts[4])+(rpts[1]-rpts[5])*(rpts[1]-rpts[5]))-r0;
        
        pA = dx*dx+dy*dy-dr*dr;
    }

    /** doShading method. */
    public void doShading(int samples[], int x, int y, int w, int h)
    {
        // call out to separate version of this routine if there was a transform other than identity
        if(xform!=null) {
            doShadingWithTransform(samples,x,y,w,h); return; }
    
        // For every point P in the raster, find circle on which that point lies.
        // Basically, the circles are described by three parametrics:
        //   x(t)=x0+t(x1-x0),  y(t)=y0+t(y1-y0),  r(t)=r0+t(r1-r0)
        // So, for a given point P, we need to solve for t in:  r(t) = |P{x(t),y(t)}|
        int sindex = 0;
        double twoA = 2*pA;
        int backsample = background!=null? getRGBAPixel(background) : 0;
        double t0,t1;
        float t[] = new float[1];
    
        for(int j=0; j<h; ++j) {
            double Yp = y0-(y+j);
            double Xp = x0-x;
            double pC0 = Yp*Yp-r0*r0;
            double pB0 = Yp*dy-r0*dr;
            for(int i=0; i<w; ++i) {
                double pB = 2*(Xp*dx+pB0);
                double pC = Xp*Xp+pC0; // Got A,B,C of quadratic At^2+Bt+C [-B+-sqrt(B^2-4AC)]/2A
                double rad = pB*pB-4*pA*pC;
                boolean validPt = false;
                if(rad>=0) {
                    rad = Math.sqrt(rad);
                    if(twoA>0) { t1 = (rad-pB)/twoA; t0 = -(pB+rad)/twoA; }
                    else { t0 = (rad-pB)/twoA; t1 = -(pB+rad)/twoA; }
                    if(t1>=0) {
                        if (t1<=1) { t[0] = (float)t1; validPt = true; }
                        else if(extend[1]) { t[0] = 1; validPt = true; }
                        else { t0 = -(pB+rad)/twoA; if(t0>=0 && t0<=1) { t[0] = (float)t0; validPt = true; } }
                    }
                    else if (extend[0]) { t[0] = 0; validPt = true; }
                }
                if(validPt) {
                    if(domain!=null)
                        t[0] = domain[0]+t[0]*(domain[1]-domain[0]);
                    samples[sindex] = getRGBAPixel(func.evaluate(t));
                }
                else samples[sindex] = backsample; //sample = background color
                sindex++; Xp--;
            }
        }
    }
    
    // It's probably the case that this routine is only needed if matrix includes a skew.  Otherwise, transforming
    // the input coords and the radii would probably be enough. however...
    public void doShadingWithTransform(int samples[], int x, int y, int w, int h)
    {
        Point2D.Double srcPt = new Point2D.Double(), dstPt = new Point2D.Double();
        int backsample = background!=null? getRGBAPixel(background) : 0;
        float t[] = new float[1];
        int sindex = 0;
        double twoA = 2*pA;
        
        for(int j=0; j<h; ++j) {
            srcPt.y = y0-(y+j);    //Yp
            srcPt.x = x0-x;       //Xp
            for(int i=0; i<w; ++i) {
                xform.transform(srcPt, dstPt);
                double pC0 = dstPt.y*dstPt.y-r0*r0; //Yp
                double pB0 = dstPt.y*dy-r0*dr; //Xp
                double pB = 2*(dstPt.x*dx+pB0);
                double pC = dstPt.x*dstPt.x+pC0;
                double rad = pB*pB-4*pA*pC;
                boolean validPt = false;
                if(rad>=0) {
                    rad = Math.sqrt(rad);
                    double t0, t1;
                    if(twoA>0) { t1 = (rad-pB)/twoA; t0 = -(pB+rad)/twoA; }
                    else { t0 = (rad-pB)/twoA; t1 = -(pB+rad)/twoA; }
                    if(t1>=0) {
                        if(t1<=1) { t[0] = (float)t1; validPt = true; }
                        else if(extend[1]) { t[0] = 1; validPt = true; }
                        else { t0 = -(pB+rad)/twoA; if(t0>=0 && t0<=1) { t[0] = (float)t0; validPt = true; } }
                    }
                    else if(extend[0]) { t[0] = 0; validPt = true; }
                }
                if(validPt) {
                    if(domain!=null) t[0] = domain[0]+t[0]*(domain[1]-domain[0]);
                    samples[sindex] = getRGBAPixel(func.evaluate(t));
                }
                else samples[sindex] = backsample; //sample = background color
                sindex++; srcPt.x--;
            }
        }
    }
}

}