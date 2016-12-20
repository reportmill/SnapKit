/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.Map;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/**
 * Type 0 sampled functions. The original function is sampled at various points and these values are passed as a pdf
 * stream.  When a sampledFunction is evaluated, it looks in the sample table for the values corresponding to the 
 * sampled points surrounding the input value and interpolates between them. Functions of Order 0 use multilinear
 * interpolation, while Order 3 functions use a cubic spline.
 */
public class PDFFunctionSampled extends PDFFunction {

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
    
public PDFFunctionSampled(PDFStream functionStream, PDFFile srcFile)
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
    if ((bitsPerSample != 1) &&
        (bitsPerSample != 2) &&
        (bitsPerSample != 4) &&
        (bitsPerSample != 8) &&
        (bitsPerSample != 12) &&
        (bitsPerSample != 16) &&
        (bitsPerSample != 24) &&
        (bitsPerSample != 32))
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
// bitsPerSample can range from 1->32, so we'll cache the samples as an array 
// of ints.
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
             sampleTable[i]=(rawBytes[j]&255)<<8 | (rawBytes[j+1]&255);
             j+=2;
         }
     else if (bitsPerSample==24)
         for(i=0,j=0; i<totalSamples; ++i) {
             sampleTable[i]=(rawBytes[j]&255)<<16 | (rawBytes[j+1]&255)<<8 | (rawBytes[j+2]&255);
             j+=3;
         }
     else if (bitsPerSample==32)
         for(i=0,j=0; i<totalSamples; ++i) {
             sampleTable[i]=(rawBytes[j]&255)<<24 | (rawBytes[j+1]&255)<<16 | (rawBytes[j+2]&255)<<8 | (rawBytes[j+3]&255);
             j+=4;
         }
     else if (bitsPerSample<8) {
         int samples,mask;
         int samplesPerByte = 8/bitsPerSample;
         mask = (1<<bitsPerSample)-1;
         for(i=0,j=0; j<byteSize; ++j) {
             samples = rawBytes[j];
             for(int n=0;(n<samplesPerByte) && (i<totalSamples);++n)
                 sampleTable[i++]=(samples>>(8-n*bitsPerSample))&mask;
         }
     }
     else if (bitsPerSample==12) {
         i=j=0;
         while(j<byteSize) {
           sampleTable[i]=(rawBytes[j]&255)<<4;
           ++j;
           sampleTable[i]|=(rawBytes[j]>>4)&15;
           ++i;
           if (j<byteSize-1) {
               sampleTable[i]=(rawBytes[j]&15)<<8;
               ++j;
               sampleTable[i]|=(rawBytes[j]&255);
               ++i;
               ++j;
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
        // Step 2. Map from Domain to Encode
        // note that the 2 subtracts and the divide could be cached
        index=(in[i]-inputDomain[2*i])*((inputValueEncoding[2*i+1]-inputValueEncoding[2*i])/
                                       (inputDomain[2*i+1]-inputDomain[2*i]))
                                       +inputValueEncoding[2*i];
        //Step 3.  Clip to the dimension's bounds
        if (index<0)
            index=0;
        if (index>samplesPerDimension[i]-1)
            index=samplesPerDimension[i]-1;
        in[i] = index;
    }
    
    // in[] now contains the point mapped into sample table space.
    // Get the sample values by interpolation
    multilinear_interpolate(in, out);
    
    // Now map the output sample values to the Decode range
    //TODO: This step could be done once for the sample table on init.
    // Ultimately, I guess it makes sense to do it here only if the number
    // of samples is greater than the number of actual invocations of
    // this function.
    // If you have a table with 1000 samples in it, but you only invoke
    // the function once, you're better off doing it here.  Conversely,
    // if your table has 10 samples and you invoke the function 1000 times,
    // you'd be better off doing it in init.
    // The same logic applies to expanding out the sample bit stream, which
    // we currently do in init.
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
    int i;
    int indim = in.length;
    int outdim = out.length;
    int sample_index;
    int left;
    float fraction;
    
    // No point playing around with a hypercube if it's just a 1 dimensional interpolation
    if (indim==1) {
        left = (int)in[0];
        fraction = in[0]-left;
        sample_index = outdim*left;
        if (fraction==0) {//landed on a sample
            for(i=0; i<outdim; ++i)
                out[i]=sampleTable[sample_index+i];
        }
        else { // 1d linear interpolate
            for(i=0; i<outdim; ++i)
                out[i]=sampleTable[sample_index+i]*(1-fraction)+sampleTable[sample_index+outdim+i]*fraction;
        }      
    }
    else {
      float weight;
      int num_vertices=1<<indim;  //# hypercube vertices = 2^dimension
      int sample_offset; // offset into the sample table corresponding to a vertex
      int samples_dimension_offset;
      int dimension,combination, mask;
      
      // Initialize output values to 0
      for(dimension=0; dimension<outdim; ++dimension)
          out[dimension]=0;
      
      // for each vertex of the hypercube, caluclate the offset to the 
      // individual samples in the table and the weight for each sample.
      for(combination=0; combination<num_vertices; ++combination) {
          sample_offset=0;
          mask=1;
          weight=1;
          samples_dimension_offset=outdim;
          for(dimension=0; dimension<indim; ++dimension) {
              left = (int)in[dimension];
              fraction = in[dimension]-left;
              if ((combination & mask) == 0) {
                  weight *= (1-fraction);
                  sample_index=left;
              }
              else {
                  weight *= fraction;
                  sample_index=left+1;
                  //left+1 is outside the table if you land
                  //exactly on the last sample in the dimension.
                  //In this case, weight will be zero, so just
                  //set sample_index to 0 so we don't get an
                  //arrayIndexOutOfBounds error.
                  if (sample_index>=samplesPerDimension[dimension])
                      sample_index=0;
              }
              // Add in this dimension's value to the sample table offset
              sample_offset += sample_index*samples_dimension_offset;
              samples_dimension_offset *= samplesPerDimension[dimension];
              // get mask for next bit in this vertex number.
              mask<<=1;
          }
          // add weighted contribution of the vertex for each output dimension
          for(dimension=0; dimension<outdim; ++dimension)
              out[dimension] += sampleTable[sample_offset+dimension]*weight;
      }
    }
}

}