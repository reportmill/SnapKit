/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.Map;
import java.awt.geom.*;
import snap.pdf.PDFFile;

/**
 * A concrete subclass of PDFShadingPattern which implements pdf radial (type 3) shadings.
 * 
 * PDF radial shadings are defined by two circles and a function. The circles need not be centered at the same location.
 * The shading works by interpolating the centers and radii of the two circles.  The distance along the line defined by
 * the two centers or radii is then mapped into the domain of the function, which returns the final color values.
 * TODO: currently ignores transform and background color
 */
public class PDFPatternShadingRadial extends PDFPatternShading {

    float domain[];
    PDFFunction func;
    boolean extend[];
    float dx,dy,dr;
    float x0,y0,r0;
    float pA;
    
public PDFPatternShadingRadial(Map patternDict, Map shadingDict, PDFFile srcFile)
{
    super(patternDict, shadingDict, srcFile);
}

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


public void doShading(int samples[], int x, int y, int w, int h) {
    int sindex;
    double Xp,Yp,pB0,pC0;
    double twoA,pB,pC;
    double rad,t0,t1;
    float t[]=new float[1];
    boolean validPt;
    int backsample;
    
    // call out to separate version of this routine if there was a transform other than identity
    if (xform != null) {
        doShadingWithTransform(samples,x,y,w,h);
    }

    // For every point P in the raster, find the circle on which that point lies.
    // Basically, the circles are described by three parametrics:
    //   x(t)=x0+t(x1-x0)
    //   y(t)=y0+t(y1-y0)
    //   r(t)=r0+t(r1-r0)
    // So, for a given point P, we need to solve for t the following
    //   r(t) = |P{x(t),y(t)}|
    else {
    sindex=0;
    twoA=2*pA;
    //cache the sample value for the background
    if (background != null)
        backsample=getRGBAPixel(background);
    else
        backsample=0;
    for(int j=0; j<h; ++j) {
        Yp=y0-(y+j);
        Xp=x0-x;
        pC0=Yp*Yp-r0*r0;
        pB0=Yp*dy-r0*dr;
        for(int i=0; i<w; ++i) {
            pB=2*(Xp*dx+pB0);
            pC=Xp*Xp+pC0;
            // Got A,B,C of quadratic At^2+Bt+C
            //  [-B+-sqrt(B^2-4AC)]/2A
            rad = pB*pB-4*pA*pC;
            validPt=false;
            if (rad>=0) {
                rad=Math.sqrt(rad);
                if (twoA>0) {
                    t1=(rad-pB)/twoA;
                    t0=-(pB+rad)/twoA;
                }
                else {
                    t0=(rad-pB)/twoA;
                    t1=-(pB+rad)/twoA;
                }
                if (t1>=0) {
                    if (t1<=1) {
                        t[0]=(float)t1;
                        validPt=true;
                    }
                    else if (extend[1]) {
                        t[0]=1;
                        validPt=true;
                    }
                    else {
                        t0=-(pB+rad)/twoA;
                        if ((t0>=0) && (t0<=1)) {
                            t[0]=(float)t0;
                            validPt=true;
                        }
                    }
                }
                else if (extend[0]) {
                    t[0]=0;
                    validPt=true;
                }
            }
            if (validPt) {
                if (domain != null)
                    t[0]=domain[0]+t[0]*(domain[1]-domain[0]);
                samples[sindex] = getRGBAPixel(func.evaluate(t));
            }
            else {
                //sample = background color
                samples[sindex]=backsample;
            }
            ++sindex;
            --Xp;
        }
    }
    }
}

public void doShadingWithTransform(int samples[], int x, int y, int w, int h) {
    int sindex;
    double /*Xp,Yp,*/pB0,pC0;
    double twoA,pB,pC;
    double rad,t0,t1;
    float t[]=new float[1];
    boolean validPt;
    Point2D.Double srcPt = new Point2D.Double();
    Point2D.Double dstPt = new Point2D.Double();
    int backsample;
    
    // It's probably the case that this routine is only needed if the
    // matrix includes a skew.  Otherwise, transforming the input
    // coords and the radii would probably be enough.
    // however...
    sindex=0;
    twoA=2*pA;
    
    if (background != null)
        backsample=getRGBAPixel(background);
    else
        backsample=0;
    
    for(int j=0; j<h; ++j) {
        srcPt.y=y0-(y+j);    //Yp
        srcPt.x=x0-x;       //Xp
        for(int i=0; i<w; ++i) {
//            inv.transform(srcPt,dstPt);
            xform.transform(srcPt,dstPt);
            pC0=dstPt.y*dstPt.y-r0*r0;
            pB0=dstPt.y*dy-r0*dr;
            pB=2*(dstPt.x*dx+pB0);
            pC=dstPt.x*dstPt.x+pC0;
            rad = pB*pB-4*pA*pC;
            validPt=false;
            if (rad>=0) {
                rad=Math.sqrt(rad);
                if (twoA>0) {
                    t1=(rad-pB)/twoA;
                    t0=-(pB+rad)/twoA;
                }
                else {
                    t0=(rad-pB)/twoA;
                    t1=-(pB+rad)/twoA;
                }
                if (t1>=0) {
                    if (t1<=1) {
                        t[0]=(float)t1;
                        validPt=true;
                    }
                    else if (extend[1]) {
                        t[0]=1;
                        validPt=true;
                    }
                    else {
                        t0=-(pB+rad)/twoA;
                        if ((t0>=0) && (t0<=1)) {
                            t[0]=(float)t0;
                            validPt=true;
                        }
                    }
                }
                else if (extend[0]) {
                    t[0]=0;
                    validPt=true;
                }
            }
            if (validPt) {
                if (domain != null)
                    t[0]=domain[0]+t[0]*(domain[1]-domain[0]);
                samples[sindex] = getRGBAPixel(func.evaluate(t));
            }
            else {
                //sample = background color
                samples[sindex]=backsample;
            }
            ++sindex;
            --srcPt.x;
        }
    }
}

}