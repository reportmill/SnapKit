/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.geom.AffineTransform;
import java.util.Map;
import snap.pdf.PDFFile;

/**
 * Implementation of Type 2 shadings, which vary color along a linear axis.
 * 
 * The shading takes a pixel in the area to be to filled and drops a perpendicular to the axis defined by coords[].
 * It then calculates the parametric point t of the intersection, with t running from domain[0] to domain[1] along
 * the axis.  t is then turned into a color value using the supplied pdf function.
 * TODO: currently ignores transform
 */
public class PDFPatternShadingAxial extends PDFPatternShading {
  PDFFunction func;
  boolean extend[];
  double Ax,Ay,Bx,By;
  double BAx,BAy,denom;
  double tMin,tMax,tScale;

public PDFPatternShadingAxial(Map patternDict, Map shadingDict, PDFFile srcFile)
{
    super(patternDict, shadingDict, srcFile);
}

/** Read the shading parameters */
public void initializeShadingParameters(Map shadingDict, PDFFile srcFile)
{
  float coords[];
  float domain[];
  
  super.initializeShadingParameters(shadingDict, srcFile);
  coords = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Coords");
  domain = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Domain");
  if (domain==null)
      domain = new float[]{0,1};
  func = PDFFunction.getInstance(srcFile.getXRefObj(shadingDict.get("Function")), srcFile);
  extend = PDFDictUtils.getBoolArray(shadingDict, srcFile, "Extend");
  if (extend==null)
      extend = new boolean[]{false, false};

  /* A & B are the starting & ending points of the line segment */
  Ax=coords[0];
  Ay=coords[1];
  Bx=coords[2];
  By=coords[3];

  // Mapping the distance along the line to the domain
  tMin=domain[0];
  tMax=domain[1];
}

public void doShading(int samples[], int x, int y, int w, int h)
{
    double PAx, PAy;
    float t[] = new float[1];

    // For every point P in the raster, find point t along AB where dotProduct(A-B, P-AB(t)) = 0
    int sindex = 0;
    for(int j=0; j<h; ++j) {
        PAy=(y+j-Ay)*BAy;
        PAx=x-Ax;
        for(int i=0; i<w; ++i) {
            t[0]=(float)(tMin+(tScale*(BAx*PAx+PAy)));
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
    double pts[] = {Ax, Ay, Bx, By};
    x.transform(pts, 0, pts, 0, 2);
    Ax=pts[0];
    Ay=pts[1];
    BAx=pts[2]-pts[0];
    BAy=pts[3]-pts[1];
    denom = BAx*BAx+BAy*BAy;
    tScale=(tMax-tMin)/denom;
}

}