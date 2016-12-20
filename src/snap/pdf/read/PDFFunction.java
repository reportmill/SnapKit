/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.*;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/**
 * This is an abstract superclass for all objects implementing PDF functions. PDF functions come in several flavors,
 * but their basic job is to take a set of n input values and return m output values.
 */
public abstract class PDFFunction {

    // possible values for functionType
    public static final int Sampled = 0;
    public static final int ExponentialInterpolation = 2;
    public static final int Stitching = 3;
    public static final int PostScriptCalculator = 4;
    
    // The kind of function
    public int functionType;
    // input numbers are clipped to this range
    public float inputDomain[];
    // output numbers are clipped to this range
    public float outputRange[];
    
    // buffers for return values and inputs, assuming that
    // this function will get called over and over
    public float returnValues[];
    public float inputValues[];
    
/** Reads the function dictionary and creates an instance of the appropriate PDFFunction subclass */
public static PDFFunction getInstance(Object function, PDFFile srcFile)
{
    PDFStream functionStream;
    Map functionDict;
    
    if (function instanceof List) {
        return new PDFFunctionArray((List)function, srcFile);
    }
    else if (function instanceof PDFStream) {
        functionStream = (PDFStream)function;
        functionDict = functionStream.getDict();
    }
    else if (function instanceof Map) {
        functionStream = null;
        functionDict = (Map)function;
    }
    else throw new PDFException("Illegal function definition");
    
    int type = ((Number)functionDict.get("FunctionType")).intValue();
    
    switch(type) {
        case Sampled: return new PDFFunctionSampled(functionStream, srcFile);
        case ExponentialInterpolation: return new PDFFunctionInterpolation(functionDict,srcFile);
        case Stitching: return new PDFFunctionStitching(functionDict, srcFile);
        default: throw new PDFException("Type "+type+" functions not supported");
    }
}

public PDFFunction() { }

/** Constructor - takes the pdf function dictionary and the file it came from. */
public PDFFunction(Map functionDict, PDFFile srcFile)
{
    // Get all the parameters
    initializeParameters(functionDict,srcFile);
    
    // initialize input and output cache arrays
    inputValues = new float[numInputValues()];
    returnValues = new float[numOutputValues()];
}

/** Constructor for functions which take a stream */
public PDFFunction(PDFStream functionStream, PDFFile srcFile)  { this(functionStream.getDict(), srcFile); }

/** Read the function parameters */
public void initializeParameters(Map functionDict, PDFFile srcFile)
{
    // Get parameters common to all functions
    inputDomain = PDFDictUtils.getFloatArray(functionDict, srcFile, "Domain");
    outputRange = PDFDictUtils.getFloatArray(functionDict, srcFile, "Range");
    // subclasses should override this and call super.initializeParameters()
}

/** Returns the number of parameters this function takes as input */
public int numInputValues() { return inputDomain.length/2; }

/** Returns the number of values this function returns.
 *  Should be overridden by subclasses that don't require outputRange.
 */
public int numOutputValues() { return outputRange!=null ? outputRange.length/2 : 0; }

/** Evaluate the function.  Note that the return value array is reused, so 
 * if you need to save the results around for any length of time, you should
 * copy them out.
 */
public float[] evaluate(float inputs[])
{
    // clip to a local copy so we don't clobber the originals
    clipToRange(inputs, inputDomain, inputValues);
    function_implementation(inputValues, returnValues);
    if (outputRange != null)
        clipToRange(returnValues, outputRange, returnValues);
    return returnValues;
}

/** Used internally by all function types to clip both the input and the output
 * values to the Range and Domain values.
 */
public void clipToRange(float vals[], float range[], float clipped[])
{
    for(int i=0, n=vals.length; i<n; ++i) {
        clipped[i]=vals[i];
        if (clipped[i]<range[2*i])
            clipped[i]=range[2*i];
        if (clipped[i]>range[2*i+1])
            clipped[i]=range[2*i+1];
    }
}

// Stupid name, but whatever.  Overide this to make a function.
// Clipping of input and values to their respective ranges is taken care of
// by evaluate(), so subclasses don't have to worry about that.
public abstract void function_implementation(float in[], float out[]);

}