/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.color.*;
import java.util.*;

/**
 * PDFDeviceNColorSpace
 * 
 * Represents a pdf /DeviceN colorspace.  This is a more general form of a
 * separation colorspace, so it is also the superclass of a PDFSeparationColorSpace.
 * 
 * In PDF, separation & deviceN spaces are used for subtractive color.
 * Drawing to an additive device (like the screen), however, requires
 * using an alternative additive colorspace.
 * 
 * This object, therefore is similar to PDFIndexedColorspace,
 * in that it is merely a mapping from a input values to 
 * a color in a different colorspace.
 * 
 * Whereas an indexed colorspace takes an input value and maps 
 * it into a color, a separation takes an input value and uses
 * a special tintTransform function to get the component values
 * to use in the alternative color space.
 * 
 * Two special separation names are /All & /None
 * /All is supposed to use all the colorants, which is usefull for 
 * putting down registration marks, but seems stupid on the screen.
 * /None is supposed to be a null colorspace.  There's no real
 * support in awt for null colorspaces, so the parser should
 * check for this case specifically and toss any drawing.
 */
public class PDFDeviceNColorSpace extends ColorSpace {

    // the additive colorspace
    ColorSpace alternative;
    
    // The function to map a tint value to color components.
    PDFFunction tintTransform;
    
    // The names of the colorants (like /Cyan, /PukeYellow, /All, etc.)
    List colorantNames;
    
    // Additional attributes
    Map attributes;
    
    // buffers to do conversions in
    float alt_colors[];
    float rgb_colors[];

public PDFDeviceNColorSpace(List names, ColorSpace altspace, PDFFunction tinttrans, Map attrs) 
{
    super(altspace.getType(), names.size());
    colorantNames = names;
    alternative = altspace;
    tintTransform = tinttrans;
    attributes = attrs;
    
    // colorant names.  Check for case of all channels being /None
    colorantNames=null;
    for(int i=0; i<names.size(); ++i)
        if (!"/None".equals(names.get(i))) {
            colorantNames = names;
            break;
        }
    
    // set up conversion buffers
    alt_colors=null;
    
    // no drawing should happen if all colorants are specified as None,
    // but just in case it does, this colorspace always returns white.
    if (colorantNames==null)
        rgb_colors = new float[]{1,1,1};
    // all other attempts to draw to the screen go through the alternate 
    // space.
    else {
        rgb_colors = new float[3];
        alt_colors = new float[3];
    }
}

//If all the colorants in a deviceN or Separation are specified as /None,
//no drawing is done.  You can call this method to determine if this is
//is the case, indicating that you can toss all drawing operations done in this colorspace.
public boolean doesDraw() 
{
    return (colorantNames != null);
}

public float[] toRGB(float[] colorvalue)
{
    // special separations have their values initialized once
    if (alt_colors == null)
        return rgb_colors;
    
    // run the color through the tintTransform and convert
    return alternative.toRGB(tintTransform.evaluate(colorvalue));
}

public float[] fromRGB(float[] rgbvalue)
{
    // no reverse mapping - tintTransform functions arent
    // necessarily invertable.  Not that we'd ever need to
    // do it anyway.
    return null;
}

public float[] toCIEXYZ(float[] colorvalue)
{
    // special separations have their values initialized once
    if (alt_colors == null)
        return rgb_colors;
    
    // run the color through the tintTransform and convert
    return alternative.toCIEXYZ(tintTransform.evaluate(colorvalue));
}

public float[] fromCIEXYZ(float[] colorvalue)
{
    // no reverse mapping 
    return null;
}

}
