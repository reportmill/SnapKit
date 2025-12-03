package snap.webenv;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.FontFile;
import snap.util.MathUtils;
import snap.webapi.*;
import java.util.Arrays;

/**
 * A FontFile subclass for CheerpJ.
 */
public class CJFontFile extends FontFile {

    // The name
    private String  _name = "Arial";

    // The Family name
    private String  _familyName = "Arial";

    // The JavaScript name
    private String  _jsName;

    // The PostScript/PDF name
    private String  _psName;

    // The char advance cache array
    private double[]  _adv;

    // The canvas
    private static HTMLCanvasElement _canvas;

    // The RenderContext2D
    private static CanvasRenderingContext2D _cntx;

    /**
     * Creates a new TVFontFile for given name.
     */
    public CJFontFile(String aName)
    {
        // Set name and family
        _name = aName;
        _familyName = _name.replace("Bold","").replace("Italic","").trim();

        // Create/init advance cache array
        _adv = new double[255];
        Arrays.fill(_adv, -1);

        // Initialize Canvas/Context
        if (_canvas == null) {
            _canvas = (HTMLCanvasElement) HTMLDocument.getDocument().createElement("canvas");
            _cntx = (CanvasRenderingContext2D) _canvas.getContext("2d");
        }

        // If font not available, reset family to Arial
        if (!isAvailable())
            _familyName = "Arial";
    }

    /**
     * Returns the name of this font.
     */
    public String getName()  { return _name; }

    /**
     * Returns the name of this font in English.
     */
    public String getNameEnglish()  { return _name; }

    /**
     * Returns the family name of this font.
     */
    public String getFamily()  { return _familyName; }

    /**
     * Returns the PostScript name of this font.
     */
    public String getPSName()
    {
        if (_psName != null) return _psName;
        String psName = _name.replace(" ", "-");
        return _psName = psName;
    }

    /**
     * Returns the family name of this font in English.
     */
    public String getFamilyEnglish()  { return _familyName; }

    /**
     * Returns the font declaration string in JavaScript format.
     */
    public String getJSName()
    {
        // If already set, just return
        if (_jsName != null) return _jsName;

        // Create name
        String jsName = "";
        if (isBold())
            jsName += "Bold ";
        if (isItalic())
            jsName += "Italic ";
        jsName += "1000px ";
        jsName += getFamily();

        // Set/return
        return _jsName = jsName;
    }

    /**
     * Returns the char advance for the given char.
     */
    protected double charAdvanceImpl(char aChar)
    {
        // Handle basic range
        if (aChar <= 255) {
            double adv = _adv[aChar];
            if (adv >= 0)
                return adv;
            return _adv[aChar] = charAdvanceImplImpl(aChar);
        }

        // Extended chars
        return charAdvanceImplImpl(aChar);
    }

    /**
     * Returns the char advance for the given char.
     */
    private double charAdvanceImplImpl(char aChar)
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText(String.valueOf(aChar));
        return metrics.getWidth() / 1000d;
    }

    /**
     * Returns the bounds rect for glyphs in given string.
     */
    public Rect getGlyphBounds(String aString)
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText(aString);
        double glyphW = metrics.getWidth();
        double glyphAsc = metrics.getActualBoundingBoxAscent();
        double glyphDesc = metrics.getActualBoundingBoxDescent();
        double glyphH = glyphAsc + glyphDesc;
        return new Rect(0, -glyphAsc, glyphW, glyphH);
    }

    /**
     * Returns the path for a given char (does the real work, but doesn't cache).
     */
    protected Shape getCharPathImpl(char c)
    {
        return new Rect(0,0,1000,1000);
    }

    /**
     * Returns the path for given string with character spacing.
     */
    public Shape getOutline(CharSequence aStr, double aSize, double aX, double aY, double aCharSpacing)
    {
        return new Rect(0,0,1000,1000);
    }

    /**
     * Returns the max distance above the baseline that this font goes.
     */
    public double getAscent()
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText("H");
        double ascent = metrics.getFontBoundingBoxAscent() / 1000;
        return ascent>0 ? ascent : .906;
    }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()
    {
        _cntx.setFont(getJSName());
        TextMetrics metrics = _cntx.measureText("H");
        double desc = metrics.getFontBoundingBoxDescent() / 1000;
        return desc>0 ? desc : .212;
    }

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()
    {
        return .033;
    }

    /**
     * Returns if this font can display the given char.
     */
    protected boolean canDisplayImpl(char aChar)
    {
        return true;
    }

    /**
     * Returns whether font is available.
     */
    private boolean isAvailable()
    {
        _cntx.setFont("72px Arial");
        double defaultSize = _cntx.measureText("ABC123").getWidth();
        _cntx.setFont("72px " + _familyName + ", Arial");
        double testSize = _cntx.measureText("ABC123").getWidth();
        return !MathUtils.equals(testSize, defaultSize);
    }

    /** Override to return TVM font. */
    public Object getNative()  { return getName() + ' ' + 1000; }

    /** Override to return TVM font. */
    public Object getNative(double aSize)  { return getName() + ' ' + aSize; }

    /** Override to return TVM font. */
    public String getNativeName()  { return getName(); }
}