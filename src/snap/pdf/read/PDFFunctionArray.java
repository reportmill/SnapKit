/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.*;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;

/**
 * An array function is not one of the built-in PDF function types.  Rather, it is a cover function for the case
 * where an array of n Functions is used as a single input, n output function.
 * Can be used, for example, to declare completely different functions for each channel in a colorspace.
 */
public class PDFFunctionArray extends PDFFunction {

    //
    PDFFunction subFuncs[];
    
public PDFFunctionArray(List funcs, PDFFile srcFile)
{
    int n=funcs.size();
    
    subFuncs = new PDFFunction[n];
    returnValues = new float[n];
    for(int i=0; i<n; ++i) {
        subFuncs[i] = PDFFunction.getInstance(srcFile.getXRefObj(funcs.get(i)), srcFile);
        if(subFuncs[i].numInputValues()!=1 || subFuncs[i].numOutputValues() != 1)
            throw new PDFException("Only functions with one input and one output dimension are valid in array");
    }
}

// The n output values are obtained independantly from each subfunction
public float[] evaluate(float inputs[])
{
    float out[];
    
    for(int i=0, n=subFuncs.length; i<n; ++i) {
        out = subFuncs[i].evaluate(inputs);
        returnValues[i]=out[0];
    }
    return returnValues;
}

public int numInputValues() { return 1; }
public int numOutputValues() { return subFuncs.length; }

public void function_implementation(float[] in, float[] out) {}

}