package snap.pdf.read;
import java.util.*;
import snap.pdf.*;

/**
 * PDFFunction subclasses.
 */
public class PDFFunctions {

/**
 * Type 2 exponential interpolation functions.  Simple interpolation between 2 sets of m values.
 * N=1 -> linear interpolation.
 */
public static class Interpolation extends PDFFunction {

    // start and end points for interpolation
    float  C0[], C1[];
    // exponent
    float N;
    
    public Interpolation(Map functionDict, PDFFile srcFile)  { super(functionDict, srcFile); }
    
    public void initializeParameters(Map functionDict, PDFFile srcFile)
    {
        super.initializeParameters(functionDict, srcFile);
        C0 = PDFDictUtils.getFloatArray(functionDict, srcFile, "C0");
        if (C0==null) { C0 = new float[1]; C0[0] = 0; }  // Default for f(0)=0
        C1 = PDFDictUtils.getFloatArray(functionDict, srcFile, "C1");
        if (C1==null) { C1 = new float[1]; C1[0] = 1; }  // Default for f(1) = 1
        
        Object val = functionDict.get("N");
        
        if((!(val instanceof Number)) || (C0.length != C1.length) || 
          ((outputRange != null) && (C0.length*2 != outputRange.length)) || (inputDomain.length != 2))
          throw new PDFException("Illegal definition of exponential interpolation function");
        
        N=((Number)val).floatValue();
        
        // might as well do the subtract once here instead of over and over a million times
        for(int i=0; i<C1.length; ++i)
            C1[i] -= C0[i];
    }
    
    // Single value input, arbitrary length output
    public int numInputValues() { return 1; }
    public int numOutputValues() { return C0.length; }
    
    // formula is slightly different because c1-c0 is cached in c1 (see above)
    public void function_implementation(float in[], float out[])
    {
        double xn = N==1 ? in[0] : Math.pow(in[0], N);
        for(int i=0, n=numOutputValues(); i<n; ++i)
            out[i] = (float)(C0[i]+xn*C1[i]);
    }
}

/**
 * A Stitching function is a function that contains an array of subfunctions. The stitching function takes it's
 * one input value, finds the particular subfunction by using the Domain and Bounds arrays, and then maps the
 * function into the subfunction's domain using the Encode array.  It then returns the results of the subfunction
 * on the mapped input value.
 */
public static class Stitching extends PDFFunction {
    PDFFunction subFunctions[];
    float bounds[];
    float encode[];
    
    public Stitching(Map functionDict, PDFFile srcFile)  { super(functionDict, srcFile); }
    
    public Stitching(PDFStream functionStream, PDFFile srcFile)  { super(functionStream, srcFile); }
    
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
        
        // Find the function within whose domain the input value lies. The superclass will already have clamped the
        // input value to this function's domain, so there's no need to check Domain[0,1]
        int i, n = subFunctions.length;
        for(i=0; i<n-1; ++i)
            if (v<bounds[i])
                break;
        PDFFunction sub = subFunctions[i];
        // Map the input value into the subfunction's domain
        float blower = i==0 ? inputDomain[0] : bounds[i-1];
        float bupper = i==n-1 ? inputDomain[1] : bounds[i];
        in[0]=encode[2*i] + (v-blower)*(encode[2*i+1]-encode[2*i])/(bupper-blower);
        sub.function_implementation(in,out);  // Forward to subfunction
    }
}

/**
 * Type 0 sampled functions. The original function is sampled at various points and these values are passed as a pdf
 * stream.  When a sampledFunction is evaluated, it looks in the sample table for the values corresponding to the 
 * sampled points surrounding the input value and interpolates between them. Functions of Order 0 use multilinear
 * interpolation, while Order 3 functions use a cubic spline.
 */
public static class Sampled extends PDFFunction {

    // How many samples are in the sample table for each dimension
    int samplesPerDimension[];
    
    // The min,max ranges for each input dimension (values are scaled to these, not clamped)
    float inputValueEncoding[];
    
    // The ranges for the output dimensions
    float outputSampleDecoding[];
    
    // Describes the format of the values in the sample stream
    int bitsPerSample;
    
    // The order of interpolation.  1=linear, 3=cubic spline
    int order;
    
    // The actual samples
    int sampleTable[];
    
    public Sampled(PDFStream functionStream, PDFFile srcFile)
    {
        super(functionStream, srcFile);
        readSampleTable(functionStream);
    }
    
    public void initializeParameters(Map functionDict, PDFFile srcFile)
    {
        super.initializeParameters(functionDict, srcFile);
        int inputDimension = numInputValues();
        
        samplesPerDimension = PDFDictUtils.getIntArray(functionDict, srcFile, "Size");
        Object v = functionDict.get("BitsPerSample");
        if ((samplesPerDimension==null) || (samplesPerDimension.length != inputDimension) || (!(v instanceof Number)))
            throw new PDFException("Illegal type 0 function definition");
        bitsPerSample = ((Number)v).intValue();
        if((bitsPerSample!=1) && (bitsPerSample!=2) && (bitsPerSample != 4) && (bitsPerSample != 8) &&
            (bitsPerSample != 12) && (bitsPerSample != 16) && (bitsPerSample != 24) && (bitsPerSample != 32))
            throw new PDFException("Illegal value of BitsPerSample in function definition");
        
        // the rest of the parameters are optional and have defaults
        v = functionDict.get("Order");
        if (v==null)
            order = 1;
        else if (v instanceof Number)
            order = ((Number)v).intValue();
        else throw new PDFException("Illegal type 0 function definition");
        
        // TODO: someday put in splines (order=3)
        if (order != 1)
            throw new PDFException("Type 0 function only supports linear interpolation");
        
        inputValueEncoding = PDFDictUtils.getFloatArray(functionDict, srcFile, "Encode");
        // defaults to [0 (Size[0]-1) 0 (Size[1]-1) ...]
        if (inputValueEncoding==null) {
            inputValueEncoding = new float[2*inputDimension];
            for(int i=0; i<inputDimension; ++i) {
                inputValueEncoding[2*i]=0;
                inputValueEncoding[2*i+1]=samplesPerDimension[i]-1;
            }
        }
        
        outputSampleDecoding = PDFDictUtils.getFloatArray(functionDict, srcFile, "Decode");
        if (outputSampleDecoding==null)
            outputSampleDecoding = outputRange;
    }
    
    // read the sample table, which is comprised of a packed stream of bitsPerSample bits.
    // bitsPerSample can range from 1->32, so we'll cache the samples as an array of ints.
    void readSampleTable(PDFStream s)
    {
         byte rawBytes[] = s.decodeStream();
         int inputDims = numInputValues();
         int outputDims = numOutputValues();
         int totalSamples=outputDims;
         int byteSize;
         int i,j;
         
         for(i=0; i<inputDims; ++i)
             totalSamples *= samplesPerDimension[i];
         byteSize = (totalSamples*bitsPerSample+7)/8;
         // In the case of the indexed colorspace lookup table, it turned 
         // out that Acrobat allowed you to have extra bytes in the sample
         // stream.  That may also be the case here, but until I see
         // an example of that, I'm considering it an error.
         if (byteSize != rawBytes.length)
             throw new PDFException("Wrong number of bytes in type 0 functon sample table");
         
         // Allocate the sample table as an array of ints, and expand out values in the stream
         sampleTable = new int[totalSamples];
         
         // simple ones first
         if (bitsPerSample==8) 
             for(i=0; i<totalSamples; ++i)
                 sampleTable[i]=rawBytes[i]&255;
         else if (bitsPerSample==16)
             for(i=0,j=0; i<totalSamples; ++i) {
                 sampleTable[i]=(rawBytes[j]&255)<<8 | (rawBytes[j+1]&255); j+=2; }
         else if (bitsPerSample==24)
             for(i=0,j=0; i<totalSamples; ++i) {
                 sampleTable[i]=(rawBytes[j]&255)<<16 | (rawBytes[j+1]&255)<<8 | (rawBytes[j+2]&255); j+=3; }
         else if (bitsPerSample==32)
             for(i=0,j=0; i<totalSamples; ++i) {
                 sampleTable[i] = (rawBytes[j]&255)<<24 | (rawBytes[j+1]&255)<<16 |
                     (rawBytes[j+2]&255)<<8 | (rawBytes[j+3]&255); j+=4; }
         else if(bitsPerSample<8) {
             int samplesPerByte = 8/bitsPerSample;
             int mask = (1<<bitsPerSample)-1;
             for(i=0,j=0; j<byteSize; ++j) {
                 int samples = rawBytes[j];
                 for(int n=0;(n<samplesPerByte) && (i<totalSamples);++n)
                     sampleTable[i++]=(samples>>(8-n*bitsPerSample))&mask;
             }
         }
         else if (bitsPerSample==12) {
             i=j=0;
             while(j<byteSize) {
               sampleTable[i]=(rawBytes[j]&255)<<4; ++j;
               sampleTable[i]|=(rawBytes[j]>>4)&15; ++i;
               if (j<byteSize-1) {
                   sampleTable[i]=(rawBytes[j]&15)<<8; ++j;
                   sampleTable[i]|=(rawBytes[j]&255); ++i; ++j;
               }
           }
         }
    }
    
    public void function_implementation(float[] in, float[] out)
    {
        float index;
        // modifying in[] values is safe since super makes a local copy
        
        // Step 1. Clip to Domain - this is done by the superclass 
        for(int i=0; i<in.length; ++i) {
            // Step 2. Map from Domain to Encode. Note that the 2 subtracts and the divide could be cached
            index = (in[i]-inputDomain[2*i])*((inputValueEncoding[2*i+1]-inputValueEncoding[2*i])/
               (inputDomain[2*i+1]-inputDomain[2*i])) + inputValueEncoding[2*i];
            //Step 3.  Clip to the dimension's bounds
            if (index<0)
                index=0;
            if (index>samplesPerDimension[i]-1)
                index=samplesPerDimension[i]-1;
            in[i] = index;
        }
        
        // in[] now contains the point mapped into sample table space. Get the sample values by interpolation
        multilinear_interpolate(in, out);
        
        // Now map the output sample values to the Decode range
        //TODO: This step could be done once for the sample table on init. Ultimately, I guess it makes sense to do it
        // here only if the number of samples is greater than the number of actual invocations of this function.
        // If you have a table with 1000 samples in it, but you only invoke the function once, you're better off doing
        // it here.  Conversely, if your table has 10 samples and you invoke the function 1000 times, you'd be better
        // off doing it in init. The same logic applies to expanding out sample bit stream, which we do in init.
        // (actually, the condition would be #samples<#outputDimensions*#invocations)
        int sampleMax = (1<<bitsPerSample)-1;
        for(int i=0; i<out.length; ++i)
            out[i] = outputSampleDecoding[2*i] + 
                     out[i]*(outputSampleDecoding[2*i+1]-outputSampleDecoding[2*i]) / sampleMax;
        // done.  Clipping to range is handled by superclass.
    }
    
    // Get the hypercube surrounding in[] and interpolate from the
    // output samples at each of the 2^n vertices of the hypercube.
    public void multilinear_interpolate(float in[], float out[])
    {
        int indim = in.length;
        int outdim = out.length;
        
        // No point playing around with a hypercube if it's just a 1 dimensional interpolation
        if(indim==1) {
            int left = (int)in[0];
            float fraction = in[0]-left;
            int sample_index = outdim*left;
            if (fraction==0) {//landed on a sample
                for(int i=0; i<outdim; ++i)
                    out[i]=sampleTable[sample_index+i];
            }
            else { // 1d linear interpolate
                for(int i=0; i<outdim; ++i)
                    out[i]=sampleTable[sample_index+i]*(1-fraction)+sampleTable[sample_index+outdim+i]*fraction;
            }      
        }
        
        else {
          
          // Initialize output values to 0
          for(int dim=0; dim<outdim; dim++) out[dim] = 0;
          
            // for each vertex of hypercube, caluclate offset to individual samples in table and weight for each sample
            int num_vertices=1<<indim;  //# hypercube vertices = 2^dimension
            for(int combination=0; combination<num_vertices; ++combination) {
                int sample_offset = 0; // offset into sample table corresponding to a vertex
                int mask = 1;
                float weight = 1;
                int samples_dimension_offset=outdim;
                for(int dimension=0; dimension<indim; ++dimension) {
                    int left = (int)in[dimension];
                    float fraction = in[dimension]-left;
                    int sample_index;
                    if((combination & mask) == 0) {
                        weight *= (1-fraction); sample_index = left; }
                    else {
                        weight *= fraction; sample_index = left+1;
                        // left+1 is outside the table if you land exactly on the last sample in the dimension.
                        // In this case, weight is 0, so set sample_index to 0 to avoid ArrayIndexOutOfBounds.
                        if(sample_index>=samplesPerDimension[dimension])
                            sample_index = 0;
                    }
                  
                    // Add in this dimension's value to the sample table offset
                    sample_offset += sample_index*samples_dimension_offset;
                    samples_dimension_offset *= samplesPerDimension[dimension];
                    mask<<=1; // get mask for next bit in this vertex number
                }
                // add weighted contribution of the vertex for each output dimension
                for(int dimension=0; dimension<outdim; ++dimension)
                    out[dimension] += sampleTable[sample_offset+dimension]*weight;
            }
        }
    }
}

/**
 * An array function is not one of the built-in PDF function types.  Rather, it is a cover function for the case
 * where an array of n Functions is used as a single input, n output function.
 * Can be used, for example, to declare completely different functions for each channel in a colorspace.
 */
public static class Array extends PDFFunction {

    //
    PDFFunction subFuncs[];
    
    public Array(List funcs, PDFFile srcFile)
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
        for(int i=0, n=subFuncs.length; i<n; ++i) {
            float out[] = subFuncs[i].evaluate(inputs);
            returnValues[i] = out[0];
        }
        return returnValues;
    }
    
    public int numInputValues() { return 1; }
    public int numOutputValues() { return subFuncs.length; }
    
    public void function_implementation(float[] in, float[] out) {}
}

}