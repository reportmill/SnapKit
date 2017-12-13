package snap.pdf.read;
import java.util.*;
import snap.gfx.ColorSpace;
import snap.pdf.*;

/**
 * ColorSpace subclasses.
 */
public class PDFColorSpaces {

/**
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
public static class DeviceRGB extends ColorSpace {

    public static DeviceRGB get()  { return _shared; } static DeviceRGB _shared = new DeviceRGB();
    
    public DeviceRGB()  { super(ColorSpace.TYPE_RGB, 3); }
    
    public float[] toRGB(float[] colorvalue)  { return colorvalue; }
    public float[] fromRGB(float[] rgbvalue)  { return rgbvalue; }
    public float[] toCIEXYZ(float[] colorvalue)  { return null; }
    public float[] fromCIEXYZ(float[] colorvalue)  { return null; }
}

/**
 * The Pattern colorspace is a special colorspace where shadings and tiling patterns can be declared. PDF treats this
 * like other colorspaces, but it is not a full colorspace as far as awt is concerned.  Awt colorspaces are expected
 * to be able to convert colors between each other, but this doesn't make sense for patterns or shadings. The
 * conversion methods just generate errors, so if the colorspace is ever used in a strange place (like an image) it
 * will generate an exception, and we don't need to always be checking to see if a particular colorspace
 * is appropriate for the given operation.
 * 
 * To draw in a pattern colorspace, you can ask the colorspace for a Paint object. Classes which implement the
 * awt Paint interface can be created for all the different shading types, as well as for tiling patterns.
 */
public static class PatternSpace extends ColorSpace {
    public Map patternDict;
    public ColorSpace tileSpace;
    
    public PatternSpace()  { super(TYPE_RGB, 0); }
    public PatternSpace(ColorSpace tspace)  { this(); tileSpace = tspace; }
    
    public float[] toRGB(float[] colorval) { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }
    public float[] fromRGB(float[] rgbval) { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }
    public float[] toCIEXYZ(float[] clrval) { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }
    public float[] fromCIEXYZ(float[] clrval) { throw new IllegalArgumentException("Illegal use of pattern colorspace"); }
}

/**
 * PDFIndexedColorSpace
 * 
 * The PDFIndexedColorSpace is an awt ColorSpace subclass which represents a pdf indexed colorspace.
 * Colors are chosen by specifying an index into a color table.  The table consists of colors within an arbitrary
 * base ColorSpace.  The actual data in the table are bytes specifying components in the base colorspace.
 * 
 * For example:  [/Indexed /DeviceRGB 2 <000000FFFFFF0000FF>]
 */
public static class IndexedColorSpace extends ColorSpace {

    // The colorspace in which our lookup table components are expressed
    ColorSpace baseSpace;
  
    // Total number of colors in the lookup table
    int nColors;
  
    // the lookup table - an aray of nColors x 3 sRGB components
    float rgbs[][];
  
    // indicates color table also holds an alpha component
    boolean hasAlpha;

    /** Create IndexedColorSpace. */
    public IndexedColorSpace(ColorSpace base, int hival, byte comps[])  { this(base, hival, false, comps); }
    
    /** Create PDFIndexedColorSpace. */
    public IndexedColorSpace(ColorSpace base, int hival, boolean alpha, byte comps[])
    {
        super(base.getType(), 1);
        
        int ccomps = base.getNumComponents();
        int ncomps = ccomps+(alpha?1:0);
    
        // Acrobat seems to consider it legal to have more bytes in the stream than necessary, so only check to see if
        // there isn't enough data. Excess data just gets ignored.    
        int carraylen = ncomps*(hival+1);
        if (comps.length < carraylen)
            throw new PDFException("Too few color components provided for indexed colorspace");
        baseSpace = base;
        nColors = hival+1;
        rgbs = new float[nColors][3];
        
        // Cache component min/max for normalizing loop
        float comp_min[] = new float[ncomps];
        float comp_scale[] = new float[ncomps];
        for(int i=0; i<ncomps; ++i) {
            comp_min[i] = i<ccomps? baseSpace.getMinValue(i) : 0;
            float max = i<ccomps? baseSpace.getMaxValue(i) : 1;
            comp_scale[i] = (max-comp_min[i])/255;
        }
        
        // Normalize components for each color in the clut and convert to rgb
        float color_buffer[] = new float[ncomps];
        for(int i=0; i<nColors; ++i) {
            for(int j=0; j<ncomps; ++j)
                color_buffer[j] = comp_min[j]+comp_scale[j]*(comps[i*ncomps+j]&255);
            float converted_rgb[] = baseSpace.toRGB(color_buffer);
            for(int j=0; j<3; ++j) rgbs[i][j] = converted_rgb[j];
        }
    }

    /** colors in an indexed colorspace are ints in the range 0->numColors-1 */
    public float getMinValue(int i)  { return 0; }
    
    public float getMaxValue(int i)  { return nColors-1; }
    
    public float[] toRGB(float[] cval)  { int cindex = (int)cval[0]; return rgbs[cindex]; }
    
    public float[] fromRGB(float[] rgbvalue)
    { throw new IllegalArgumentException("Indexed colorspaces cannot map color values to indices"); }
    
    public float[] toCIEXYZ(float[] colorvalue)
    {
       float rgb[] = toRGB(colorvalue);
       float comps[] = baseSpace.fromRGB(rgb);
       return baseSpace.toCIEXYZ(comps);
    }
    
    public float[] fromCIEXYZ(float[] cval)
    { throw new IllegalArgumentException("Indexed colorspaces cannot map color values to indices"); }
}

/**
 * DeviceNColorSpace represents a pdf /DeviceN colorspace.  This is a more general form of a separation colorspace,
 * so it is also the superclass of a PDFSeparationColorSpace.
 * 
 * In PDF, separation & deviceN spaces are used for subtractive color. Drawing to an additive device (like the screen),
 * however, requires using an alternative additive colorspace. This object, therefore is similar to
 * PDFIndexedColorspace, in that it is merely a mapping from a input values to a color in a different colorspace.
 * 
 * Whereas an indexed colorspace takes an input value and maps it into a color, a separation takes an input value and
 * uses a special tintTransform function to get the component values to use in the alternative color space.
 * 
 * Two special separation names are /All & /None. /All is supposed to use all the colorants, which is usefull for 
 * putting down registration marks, but seems stupid on the screen. /None is supposed to be a null colorspace. There's
 * no real support in awt for null colorspaces, so parser should check for this case specifically and toss any drawing.
 */
public static class DeviceNColorSpace extends ColorSpace {

    // the additive colorspace
    ColorSpace alternative;
    
    // The function to map a tint value to color components.
    PDFFunction tintTransform;
    
    // The names of the colorants (like /Cyan, /PukeYellow, /All, etc.)
    List colorantNames;
    
    // Additional attributes
    Map attributes;
    
    // buffers to do conversions in
    float alt_colors[], rgb_colors[];

    public DeviceNColorSpace(List names, ColorSpace altspace, PDFFunction tinttrans, Map attrs) 
    {
        super(altspace.getType(), names.size());
        colorantNames = names; alternative = altspace; tintTransform = tinttrans; attributes = attrs;
        
        // colorant names.  Check for case of all channels being /None
        colorantNames = null;
        for(int i=0; i<names.size(); ++i)
            if (!"/None".equals(names.get(i))) {
                colorantNames = names; break; }
        
        // set up conversion buffers
        alt_colors = null;
        
        // no drawing should happen if all colorants are specified as None, but in case it does, this always returns white.
        if (colorantNames==null)
            rgb_colors = new float[]{1,1,1};
            
        // all other attempts to draw to the screen go through the alternate space.
        else { rgb_colors = new float[3]; alt_colors = new float[3]; }
    }
    
    /**
     * If all colorants in a deviceN or Separation are specified as /None,no drawing is done.  You can call this method
     * to determine if this is is the case, indicating that you can toss all drawing operations done in this colorspace.
     */
    public boolean doesDraw()  { return (colorantNames != null); }
    
    public float[] toRGB(float[] cval)
    {
        if (alt_colors == null) return rgb_colors; // special separations have their values initialized once
        return alternative.toRGB(tintTransform.evaluate(cval)); // run color through the tintTransform and convert
    }
    
    // No reverse mapping - tintTransform functions arent necessarily invertable. Not that we'd need to do it anyway.
    public float[] fromRGB(float[] rgbvalue)  { return null; }
    
    public float[] toCIEXYZ(float[] cval)
    {
        if (alt_colors == null) return rgb_colors; // special separations have their values initialized once
        return alternative.toCIEXYZ(tintTransform.evaluate(cval)); // run color through the tintTransform and convert
    }
    
    public float[] fromCIEXYZ(float[] colorvalue)  { return null; } // no reverse mapping 
}

/** 
 * A ColorSpace subclass to represent a pdf /Separation colorspace.
 * This is just a subclass of PDFDeviceNColorSpace with a single colorant.
 */
public static class SeparationColorSpace extends DeviceNColorSpace {

    public SeparationColorSpace(String name, ColorSpace altspace, PDFFunction tinttrans) 
    {
        super(Collections.singletonList(name), altspace, tinttrans, null);
    }
}

}