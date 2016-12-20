/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.geom.*;
import java.util.Map;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;

/**
 * Function-based (type 1) shadings
 * x,y values in shading space are just passed to the function and the returned color value is plotted.
 */
public class PDFPatternShadingFunction extends PDFPatternShading {
    AffineTransform shading_xform;
    float domain[];
    PDFFunction func;
    
public PDFPatternShadingFunction(Map patternDict, Map shadingDict, PDFFile srcFile)
{
    super(patternDict, shadingDict, srcFile);
}

/** Read the shading parameters */
public void initializeShadingParameters(Map shadingDict, PDFFile srcFile)
{
  super.initializeShadingParameters(shadingDict, srcFile);
  // Not sure about this - there are 2 matrices here. One inherited from Pattern, and this one from shading dictionary
  shading_xform = PDFDictUtils.getTransform(shadingDict, srcFile, "Matrix");
  if (shading_xform==null)
      shading_xform = new AffineTransform();
  domain = PDFDictUtils.getFloatArray(shadingDict, srcFile, "Domain");
  if (domain==null)
      domain = new float[]{0,1,0,1};
  func = PDFFunction.getInstance(srcFile.getXRefObj(shadingDict.get("Function")), srcFile);
  if (func==null)
      throw new PDFException("Error reading shading function");
  //TODO: Should check that the number of outputs of the function match the colorspace
}

public void doShading(int samples[], int x, int y, int w, int h)
{
    int sindex;
    float coord[]=new float[2];
    float domainxScale, domainyScale;
    
    // Map the x,y of the pixel into the domain of the function
    // and set the pixel to the result of the function.
    sindex=0;
    domainxScale = (domain[1]-domain[0])/w;
    domainyScale = (domain[3]-domain[2])/h;
    coord[1]=domain[2];
    for(int j=0; j<h; ++j) {
        coord[0] = domain[0];
        for(int i=0; i<w; ++i) {
            samples[sindex] = getRGBAPixel(func.evaluate(coord));
            ++sindex;
            coord[0] += domainxScale;
         }
        coord[1] += domainyScale;
    }
}

}