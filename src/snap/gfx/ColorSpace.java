package snap.gfx;

/**
 * This abstract class is used to serve as a color space tag to identify the
 * specific color space of a Color object or, via a ColorModel object,
 * of an Image, a BufferedImage, or a GraphicsDevice.  It contains
 * methods that transform colors in a specific color space to/from sRGB
 * and to/from a well-defined CIEXYZ color space.
 */
public abstract class ColorSpace {

    private int type;
    private int numComponents;
    private transient String [] compName = null;

    // Cache of singletons for the predefined color spaces.
    private static ColorSpace sRGBspace, XYZspace, PYCCspace, GRAYspace, LINEAR_RGBspace;

    /** Any of the family of XYZ color spaces. */
    //@Native public static final int TYPE_XYZ = 0;

    /** Any of the family of Lab color spaces. */
    public static final int TYPE_Lab = 1;

    /** Any of the family of Luv color spaces. */
    //@Native public static final int TYPE_Luv = 2;

    /** Any of the family of YCbCr color spaces. */
    //@Native public static final int TYPE_YCbCr = 3;

    /** Any of the family of Yxy color spaces. */
    //@Native public static final int TYPE_Yxy = 4;

    /** Any of the family of RGB color spaces. */
    public static final int TYPE_RGB = 5;

    /** Any of the family of GRAY color spaces. */
    public static final int TYPE_GRAY = 6;

    /** Any of the family of HSV color spaces. */
    //@Native public static final int TYPE_HSV = 7;

    /** Any of the family of HLS color spaces. */
    //@Native public static final int TYPE_HLS = 8;

    /** Any of the family of CMYK color spaces. */
    public static final int TYPE_CMYK = 9;

    /** Any of the family of CMY color spaces. */
    public static final int TYPE_CMY = 11;

    /** Generic 2 component color spaces. */
    public static final int TYPE_2CLR = 12;

    /** Generic 3 component color spaces. */
    public static final int TYPE_3CLR = 13;

    /** Generic 4 component color spaces. */
    public static final int TYPE_4CLR = 14;

    /** Generic 5 component color spaces. */
    public static final int TYPE_5CLR = 15;

    /**
     * The sRGB color space defined at http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html
     */
    public static final int CS_sRGB = 1000;

    /**
     * A built-in linear RGB color space.  This space is based on the
     * same RGB primaries as CS_sRGB, but has a linear tone reproduction curve.
     */
    public static final int CS_LINEAR_RGB = 1004;

    /**
     * The CIEXYZ conversion color space defined above.
     */
    public static final int CS_CIEXYZ = 1001;

    /** The Photo YCC conversion color space. */
    //@Native public static final int CS_PYCC = 1002;

    /** The built-in linear gray scale color space. */
    public static final int CS_GRAY = 1003;
    
    // The object that creates new ColorSpaces for current graphics environment. */
    public static ColorSpaceFactory  _factory;

    /**
     * Constructs a ColorSpace object given a color space type and the number of components.
     */
    protected ColorSpace (int type, int numcomponents)  { this.type = type; this.numComponents = numcomponents; }

    /**
     * Returns true if the ColorSpace is CS_sRGB.
     */
    public boolean isCS_sRGB () { return (this == sRGBspace); }

    /**
     * Transforms a color value assumed to be in this ColorSpace
     * into a value in the default CS_sRGB color space.
     */
    public abstract float[] toRGB(float[] colorvalue);

    /**
     * Transforms a color value assumed to be in the default CS_sRGB
     * color space into this ColorSpace.
     */
    public abstract float[] fromRGB(float[] rgbvalue);

    /**
     * Transforms a color value assumed to be in this ColorSpace
     * into the CS_CIEXYZ conversion color space.
     */
    public abstract float[] toCIEXYZ(float[] colorvalue);

    /**
     * Transforms a color value assumed to be in the CS_CIEXYZ conversion
     * color space into this ColorSpace.
     */
    public abstract float[] fromCIEXYZ(float[] colorvalue);

    /**
     * Returns the color space type of this ColorSpace (for example TYPE_RGB, TYPE_XYZ, ...).  The type defines the
     * number of components of the color space and the interpretation, e.g. TYPE_RGB identifies a color space with
     * three components - red, green, and blue.  It does not define the particular color
     * characteristics of the space, e.g. the chromaticities of the primaries.
     */
    public int getType()  { return type; }

    /**
     * Returns the number of components of this ColorSpace.
     */
    public int getNumComponents()  { return numComponents; }

    /**
     * Returns the name of the component given the component index.
     */
    public String getName(int idx)
    {
        // Handle common cases here
        if (idx < 0 || idx > numComponents-1)
            throw new IllegalArgumentException("Component index out of range: " + idx);

        if (compName == null) {
            switch (type) {
                //case ColorSpace.TYPE_XYZ: compName = new String[] { "X", "Y", "Z" }; break;
                case ColorSpace.TYPE_Lab: compName = new String[] { "L", "a", "b" }; break;
                //case ColorSpace.TYPE_Luv: compName = new String[] { "L", "u", "v" }; break;
                //case ColorSpace.TYPE_YCbCr: compName = new String[] { "Y", "Cb", "Cr" }; break;
                //case ColorSpace.TYPE_Yxy: compName = new String[] { "Y", "x", "y" }; break;
                case ColorSpace.TYPE_RGB: compName = new String[] { "Red", "Green", "Blue" }; break;
                case ColorSpace.TYPE_GRAY: compName = new String[] { "Gray" }; break;
                //case ColorSpace.TYPE_HSV: compName = new String[] { "Hue", "Saturation", "Value" }; break;
                //case ColorSpace.TYPE_HLS: compName = new String[] { "Hue", "Lightness", "Saturation" }; break;
                case ColorSpace.TYPE_CMYK: compName = new String[] { "Cyan", "Magenta", "Yellow", "Black" }; break;
                case ColorSpace.TYPE_CMY: compName = new String[] { "Cyan", "Magenta", "Yellow" }; break;
                default: String[] tmp = new String[numComponents];
                    for (int i=0; i<tmp.length; i++) tmp[i] = "Unnamed color component(" + i + ")";
                    compName = tmp;
            }
        }
        return compName[idx];
    }

    /**
     * Returns the minimum normalized color component value for the specified component.
     */
    public float getMinValue(int comp)
    {
        if (comp < 0 || comp > numComponents-1)
            throw new IllegalArgumentException("Component index out of range: " + comp);
        return 0f;
    }

    /**
     * Returns the maximum normalized color component value for the specified component.
     */
    public float getMaxValue(int comp)
    {
        if (comp < 0 || comp > numComponents-1)
            throw new IllegalArgumentException("Component index out of range: " + comp);
        return 1f;
    }

    /**
     * Returns a ColorSpace representing one of the specific predefined color spaces.
     * @param colorspace a specific color space identified by one of the predefined class constants (e.g.
     * CS_sRGB, CS_LINEAR_RGB, CS_CIEXYZ, CS_GRAY, or CS_PYCC)
     */
    public static ColorSpace getInstance(int aCS)
    {
        switch (aCS) {
            case CS_sRGB: return sRGBspace != null ? sRGBspace : (sRGBspace = _factory.getInstance(aCS));
            case CS_CIEXYZ: return XYZspace != null ? XYZspace : (XYZspace = _factory.getInstance(aCS));
            case CS_GRAY: return GRAYspace != null ? GRAYspace : (GRAYspace = _factory.getInstance(aCS));
            case CS_LINEAR_RGB: return LINEAR_RGBspace!=null? LINEAR_RGBspace : (LINEAR_RGBspace=_factory.getInstance(aCS));
            default: throw new IllegalArgumentException ("Unknown color space");
        }
    }

    /**
     * Create ICC ColorSpace from source (stream or bytes)
     */
    public static ColorSpace createColorSpaceICC(Object aSource)
    {
        return _factory.createColorSpaceICC(aSource);
    }

    /**
     * The ColorSpaceFactory.
     */
    public interface ColorSpaceFactory {

        /** Returns a ColorSpace for given type. */
        public ColorSpace getInstance(int aCS);

        /** Create ICC ColorSpace from source. */
        public ColorSpace createColorSpaceICC(Object aSource);
    }
}