package snap.pdf.read;
import java.awt.color.ColorSpace;
import java.util.Map;
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
    
    public DeviceRGB()  { this(ColorSpace.TYPE_RGB, 3); }
    public DeviceRGB(int type, int numcomponents)  { super(type, numcomponents); }
    
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
 * java.awt.Paint interface can be created for all the different shading types, as well as for tiling patterns.
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

}