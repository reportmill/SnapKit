/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.color.ColorSpace;

/*
 * Hard to believe this is even necessary. For consistency sake, colors are always created with a ColorSpace.
 * The java docs claim that the standard sRGB space can be gotten with ColorSpace.getInstance(ColorSpace.CS_sRGB).
 * However, this seems to return a colorspace whose colors don't really match the device defaults which are usually
 * also referred to in the docs as sRGB.
 * 
 * In other words the following does not evaluate to true:
 * 
 *   boolean sRGBisTheSameAsDefault() {
 *     float vals[] = {1,1,1};
 *     Color scolor = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), vals);
 *     Color default = new Color(1,1,1);
 *     return scolor.equals(default);
 *     }
 *  
 *  This class, therefore, is just an identity colorspace that passes input values straight through.
 */
public class PDFDeviceRGB extends ColorSpace {

    private static PDFDeviceRGB _oneSpaceForAll = new PDFDeviceRGB();
    
public static PDFDeviceRGB sharedInstance()  { return _oneSpaceForAll; }

public PDFDeviceRGB()  { this(ColorSpace.TYPE_RGB, 3); }

public PDFDeviceRGB(int type, int numcomponents)  { super(type, numcomponents); }

public float[] toRGB(float[] colorvalue)  { return colorvalue; }

public float[] fromRGB(float[] rgbvalue)  { return rgbvalue; }

public float[] toCIEXYZ(float[] colorvalue)  { return null; }

public float[] fromCIEXYZ(float[] colorvalue)  { return null; }

}