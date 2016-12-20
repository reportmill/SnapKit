/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.color.ColorSpace;
import snap.pdf.PDFException;

/**
 * PDFIndexedColorSpace
 * 
 * The PDFIndexedColorSpace is an awt ColorSpace subclass which represents a pdf indexed colorspace.
 * Colors are chosen by specifying an index into a color table.  The table consists of colors within an arbitrary
 * base ColorSpace.  The actual data in the table are bytes specifying components in the base colorspace.
 * 
 * For example:  [/Indexed /DeviceRGB 2 <000000FFFFFF0000FF>]
 */
public class PDFIndexedColorSpace extends ColorSpace {

  // The colorspace in which our lookup table components are expressed
  ColorSpace baseSpace;
  
  // Total number of colors in the lookup table
  int nColors;
  
  // the lookup table - an aray of nColors x 3 sRGB components
  float rgbs[][];
  
  // indicates color table also holds an alpha component
  boolean hasAlpha;

/**
 * Create PDFIndexedColorSpace.
 */
public PDFIndexedColorSpace(ColorSpace base, int hival, byte comps[])  { this(base, hival, false, comps); }

/**
 * Create PDFIndexedColorSpace.
 */
public PDFIndexedColorSpace(ColorSpace base, int hival, boolean alpha, byte comps[])
{
    super(base.getType(), 1);
    
    int ccomps = base.getNumComponents();
    int ncomps = ccomps+(alpha?1:0);
    int carraylen = ncomps*(hival+1);
    float max;
    float color_buffer[], converted_rgb[];

    // Acrobat seems to consider it legal to have more bytes in the stream
    // than necessary, so only check to see if there isn't enough data.
    // Excess data just gets ignored.    
    if (comps.length < carraylen)
        throw new PDFException("Too few color components provided for indexed colorspace");
    baseSpace = base;
    nColors = hival+1;
    color_buffer = new float[ncomps];
    rgbs = new float[nColors][3];
    
    // cache component min/max for normalizing loop
    float comp_min[] = new float[ncomps];
    float comp_scale[] = new float[ncomps];
    for(int i=0; i<ncomps; ++i) {
        comp_min[i] = i<ccomps ? baseSpace.getMinValue(i) : 0;
        max = i<ccomps ? baseSpace.getMaxValue(i) : 1;
        comp_scale[i] = (max-comp_min[i])/255;
    }
    
    // normalize components for each color in the clut and convert to rgb
    for(int i=0; i<nColors; ++i) {
        for(int j=0; j<ncomps; ++j)
            color_buffer[j]=comp_min[j]+comp_scale[j]*(comps[i*ncomps+j]&255);
        converted_rgb=baseSpace.toRGB(color_buffer);
        for(int j=0; j<3; ++j)
            rgbs[i][j]=converted_rgb[j];
    }
}

/** colors in an indexed colorspace are ints in the range 0->numColors-1 */
public float getMinValue(int i)  { return 0; }

public float getMaxValue(int i)  { return nColors-1; }

public float[] toRGB(float[] colorvalue)
{
    int cindex = (int)colorvalue[0];
    return rgbs[cindex];
}

public float[] fromRGB(float[] rgbvalue)
{
    throw new IllegalArgumentException("Indexed colorspaces cannot map color values to indices");
}

public float[] toCIEXYZ(float[] colorvalue)
{
   float rgb[] = toRGB(colorvalue);
   float comps[] = baseSpace.fromRGB(rgb);
   return baseSpace.toCIEXYZ(comps);
}

public float[] fromCIEXYZ(float[] colorvalue)
{
    throw new IllegalArgumentException("Indexed colorspaces cannot map color values to indices");
}

}