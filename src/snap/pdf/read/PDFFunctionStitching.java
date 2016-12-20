/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.*;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/**
 * A Stitching function is a function that contains an array of subfunctions. The stitching function takes it's
 * one input value, finds the particular subfunction by using the Domain and Bounds arrays, and then maps the
 * function into the subfunction's domain using the Encode array.  It then returns the results of the subfunction
 * on the mapped input value.
 */
public class PDFFunctionStitching extends PDFFunction {
    PDFFunction subFunctions[];
    float bounds[];
    float encode[];
    
public PDFFunctionStitching(Map functionDict, PDFFile srcFile)  { super(functionDict, srcFile); }

public PDFFunctionStitching(PDFStream functionStream, PDFFile srcFile)  { super(functionStream, srcFile); }

public void initializeParameters(Map functionDict, PDFFile srcFile)
{
    super.initializeParameters(functionDict, srcFile);
    
    Object funcs = srcFile.getXRefObj(functionDict.get("Functions"));
    bounds = PDFDictUtils.getFloatArray(functionDict,srcFile,"Bounds");
    encode = PDFDictUtils.getFloatArray(functionDict,srcFile,"Encode");
    
    if ((!(funcs instanceof List)) || (encode==null))
        throw new PDFException("Illegal declaration of type 3 function");
    
    List fdicts = (List)funcs;
    int nfuncs = fdicts.size();
    int nbounds = bounds==null ? 0 : bounds.length;
    
    if((nbounds != nfuncs-1) || (encode.length != 2*nfuncs))
        throw new PDFException("Illegal declaration of type 3 function");

    subFunctions = new PDFFunction[nfuncs];
    for(int i=0; i<nfuncs; ++i) 
        subFunctions[i]=PDFFunction.getInstance(srcFile.getXRefObj(fdicts.get(i)), srcFile);
    
    if(bounds==null) {
        if(nfuncs == 1)
            bounds = subFunctions[0].inputDomain;
        else throw new PDFException("Illegal declaration of type 3 function");
    }
}

// Always 1 input
public int numInputValues()  { return 1; }

// All the subfuncs have the same number of outputs
public int numOutputValues()  { return subFunctions[0].numOutputValues(); }

public void function_implementation(float[] in, float[] out)
{
    float v = in[0];
    int i, n=subFunctions.length;
    PDFFunction sub;
    float blower,bupper;
    
    // Find the function within whose domain the input value lies. The superclass will already have clamped the
    // input value to this function's domain, so there's no need to check Domain[0,1]
    for(i=0; i<n-1; ++i)
        if (v<bounds[i])
            break;
    sub = subFunctions[i];
    // Map the input value into the subfunction's domain
    blower = i==0 ? inputDomain[0] : bounds[i-1];
    bupper = i==n-1 ? inputDomain[1] : bounds[i];
    in[0]=encode[2*i] + (v-blower)*(encode[2*i+1]-encode[2*i])/(bupper-blower);
    // Forward to the subfunction
    sub.function_implementation(in,out);
}

}