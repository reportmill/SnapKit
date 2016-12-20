/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.color.ColorSpace;

/**
 * This is an implementation of the DeviceCMYK colorspace as defined in the pdf spec.
 * Note that there is a CMYK colorspace profile available from Sun in the JAICMM package, but there are several
 * problems with it. First and foremost, it doesn't generate colors that are consistent with Acrobat.
 * Secondly, it is enormous, at 1/2 meg.
 * 
 * The cmyk->rgb conversion is trivial. rgb->cmyk, however, can be arbitrarily complex as custom black-generation and
 * undercolor-removal functions can be specified in the graphics state.
 * 
 * The spec defines the rgb->cmyk conversion like this :
 *    UCR(x) = undercolor-removal function
 *    BG(x)  = black-generation function
 *    k = min(1-red, 1-green, 1-blue)
 *     cyan = min(1, max(0, 1-red-UCR(k)))
 *     magenta = min(1, max(0, 1-green-UCR(k)))
 *     yellow = min(1, max(0, 1-blue-UCR(k)))
 *     black = min(1, max(0, BG(k)))
 */
public class PDFDeviceCMYK extends ColorSpace {

    // We take all values and run them through the pdf conversion algorithms to generate DeviceRGB values.  We then fit
    // into the awt colorspace world by passing the values to the standard sRGB space.
    ColorSpace deviceRGB = ColorSpace.getInstance(CS_sRGB);
  
public PDFDeviceCMYK()  { this(TYPE_CMYK, 4); }

public PDFDeviceCMYK(int type, int numcomponents)  { super(type, numcomponents); }

public float[] toRGB(float[] colorvalue)
{
    float rgb[] = new float[3];
    rgb[0] = 1 - Math.min(1, colorvalue[0]+colorvalue[3]);
    rgb[1] = 1 - Math.min(1, colorvalue[1]+colorvalue[3]);
    rgb[2] = 1 - Math.min(1, colorvalue[2]+colorvalue[3]);
    return rgb;
}

public float[] fromRGB(float[] rgbvalue)
{
    // TODO : BG, UCR, BG2, & UCR2 parameters of gstate have functions that describe the black-generation and
    // undercolor-removal functions. There is no standard default for these functions (the spec claims the
    // default is device dependent)
    return null;
}

public float[] toCIEXYZ(float[] colorvalue)  { return deviceRGB.toCIEXYZ(toRGB(colorvalue)); }

public float[] fromCIEXYZ(float[] colorvalue)  { return fromRGB(deviceRGB.fromCIEXYZ(colorvalue)); }

}