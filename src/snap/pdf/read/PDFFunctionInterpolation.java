/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.Map;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;

/**
 * Type 2 exponential interpolation functions.  Simple interpolation between 2 sets of m values.
 * N=1 -> linear interpolation.
 */
public class PDFFunctionInterpolation extends PDFFunction {

    // start and end points for interpolation
    float  C0[], C1[];
    // exponent
    float N;
    
public PDFFunctionInterpolation(Map functionDict, PDFFile srcFile)  { super(functionDict, srcFile); }

public void initializeParameters(Map functionDict, PDFFile srcFile)
{
    super.initializeParameters(functionDict, srcFile);
    C0 = PDFDictUtils.getFloatArray(functionDict, srcFile, "C0");
    // Default for f(0)=0
    if (C0==null) {
        C0 = new float[1];
        C0[0] = 0;
    }
    C1 = PDFDictUtils.getFloatArray(functionDict, srcFile, "C1");
    // Default for f(1) = 1
    if (C1==null) {
        C1 = new float[1];
        C1[0] = 1;
    }
    
    Object val = functionDict.get("N");
    
    if ((!(val instanceof Number)) || 
          (C0.length != C1.length) || 
          ((outputRange != null) && (C0.length*2 != outputRange.length)) ||
          (inputDomain.length != 2))
      throw new PDFException("Illegal definition of exponential interpolation function");
    
    N=((Number)val).floatValue();
    
    // might as well do the subtract once here instead of over and over a million times
    for(int i=0; i<C1.length; ++i)
        C1[i] -= C0[i];
}

//Single value input, arbitrary length output
public int numInputValues() { return 1; }
public int numOutputValues() { return C0.length; }

public void function_implementation(float in[], float out[])
{
    double xn = N==1 ? in[0] : Math.pow(in[0], N);
    for(int i=0, n=numOutputValues(); i<n; ++i)
        // formula is slightly different because c1-c0 is cached in c1 (see above)
        out[i] = (float)(C0[i]+xn*C1[i]);
}

}