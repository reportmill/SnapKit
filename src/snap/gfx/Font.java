/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.props.PropObject;
import snap.props.PropSet;
import snap.util.*;
import java.util.Arrays;

/**
 * This class represents a font for use in rich text. Currently this is necessary because Java fonts are missing
 * so much basic typographic information.
 */
public class Font extends PropObject implements XMLArchiver.Archivable {
    
    // This font's base font file
    private FontFile  _fontFile;
    
    // This fonts point size
    private double  _size;
    
    // Whether this font had to use substitute
    private boolean  _substitute;
    
    // The system native version of this font
    private Object  _native;

    // A good code font
    private static Font _codeFont;

    // Whether font not found error has already been printed
    private static boolean  _fontNotFoundErrorPrinted = false;

    // Constants for properties
    public static final String Name_Prop = "Name";
    public static final String Size_Prop = "Size";

    // Some common fonts (using Arial since it seems more reliable on Windows & Mac)
    public static Font Arial10;
    public static Font Arial11;
    public static Font Arial12;
    public static Font Arial13;
    public static Font Arial14;
    public static Font Arial16;
    
    // Moved class initialization here because we were getting some odd init errors with bogus stacktraces
    static {
        try {
            FontFile arialFontFile = FontFile.getArialFontFile();
            Arial10 = new Font(arialFontFile,10d);
            Arial11 = Arial10.copyForSize(11d);
            Arial12 = Arial10.copyForSize(12d);
            Arial13 = Arial10.copyForSize(13d);
            Arial14 = Arial10.copyForSize(14d);
            Arial16 = Arial10.copyForSize(16d);
        }
        catch(Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    /**
     * Constructor (really only used for unarchival).
     */
    public Font()
    {
        super();
        _fontFile = FontFile.getArialFontFile();
        _size = 12;
    }

    /**
     * Constructor for given font file and point size.
     */
    protected Font(FontFile aFontFile, double aPointSize)
    {
        super();
        _fontFile = aFontFile;
        _size = aPointSize;
    }

    /**
     * Constructor for given name and size.
     */
    public Font(String aName, double aSize)
    {
        super();

        // Get fontFile for aName
        _fontFile = FontFile.getFontFile(aName);
        _size = aSize;

        // If fontFile not found, substitute Arial (try to get the style right)
        if (_fontFile == null) {
            _substitute = true;
            _fontFile = FontFile.getArialFontFile();

            if (!_fontNotFoundErrorPrinted) {
                System.err.println("Font Alert! See http://www.reportmill.com/support/docs/fonts.html");
                _fontNotFoundErrorPrinted = true;
            }

            // Complain about lost font (should put aName in HashSet and complain only once per font name)
            System.err.println("Font: No font found for " + aName + " (using " + _fontFile + ")");
        }
    }

    /**
     * Returns the name of this font.
     */
    public String getName()  { return _fontFile.getName(); }

    /**
     * Returns the name of this font in English.
     */
    public String getNameEnglish()  { return _fontFile.getNameEnglish(); }

    /**
     * Returns the font size of this font.
     */
    public double getSize()  { return _size; }

    /**
     * Returns the family name of this font.
     */
    public String getFamily()  { return _fontFile.getFamily(); }

    /**
     * Returns the family name of this font in English.
     */
    public String getFamilyEnglish()  { return _fontFile.getFamilyEnglish(); }

    /**
     * Returns the PostScript name of this font.
     */
    public String getPSName()  { return _fontFile.getPSName(); }

    /**
     * Returns the font file for this font.
     */
    public FontFile getFontFile()  { return _fontFile; }

    /**
     * Returns the char advance for the given char.
     */
    public double charAdvance(char aChar)
    {
        double charAdv = _fontFile.charAdvance(aChar);
        return charAdv * _size;
    }

    /**
     * Returns the char advance for a given character.
     */
    public double getCharAdvance(char aChar, boolean isFractional)
    {
        double adv = _fontFile.charAdvance(aChar) * _size;
        return isFractional ? adv : Math.round(adv);
    }

    /**
     * Returns the kerning for the given pair of characters (no way to do this in Java!).
     */
    public double getCharKern(char aChar1, char aChar2)
    {
        double charKern = _fontFile.getCharKern(aChar1, aChar2);
        return charKern * _size;
    }

    /**
     * Returns the path for a given character.
     */
    public Shape getCharPath(char aChar)  { return _fontFile.getCharPath(aChar); }

    /**
     * Returns the bounds for a given character.
     */
    public Rect getCharBounds(char aChar)
    {
        Shape charPath = getCharPath(aChar);
        Rect charBounds = charPath.getBounds();
        charBounds.scale(_size);
        return charBounds;
    }

    /**
     * Returns the horizontal distance spanned by the given string when rendered in this font.
     */
    public double getStringAdvance(String aString)
    {
        double strW = 0;
        for (int i = 0, iMax = aString.length(); i < iMax; i++)
            strW += charAdvance(aString.charAt(i));
        return strW;
    }

    /**
     * Returns the bounds rect for given string.
     */
    public Rect getStringBounds(String aString)
    {
        double ascent = getAscent();
        double descent = getDescent();
        double strW = getStringAdvance(aString);
        double strH = ascent + descent;
        return new Rect(0, descent, strW, strH);
    }

    /**
     * Returns the bounds rect for glyphs in given string.
     */
    public Rect getGlyphBounds(String aString)
    {
        Rect rect = _fontFile.getGlyphBounds(aString);
        double size = getSize();
        rect.y = rect.y / 1000 * size;
        rect.width = rect.width / 1000 * size;
        rect.height = rect.height / 1000 * size;
        return rect;
    }

    /**
     * Returns the path for given string with character spacing.
     */
    public Shape getOutline(CharSequence aStr, double aX, double aY, double aCharSpace)
    {
        return _fontFile.getOutline(aStr, getSize(), aX, aY, aCharSpace);
    }

    /**
     * Returns the max distance above the baseline that this font goes.
     */
    public double getAscent()  { return _fontFile.getAscent() * _size; }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()  { return _fontFile.getDescent() * _size; }

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()  { return _fontFile.getLeading() * _size; }

    /**
     * Returns the height for a line of text in this font.
     */
    public double getLineHeight()  { return _fontFile.getLineHeight() * _size; }

    /**
     * Returns the distance from the top of a line of text to the to top of a successive line of text.
     */
    public double getLineAdvance()  { return _fontFile.getLineAdvance() * _size; }

    /**
     * Returns the distance below the baseline that an underline should be drawn.
     */
    public double getUnderlineOffset()  { return _fontFile.getUnderlineOffset() * _size; }

    /**
     * Returns the default thickness that an underline should be drawn.
     */
    public double getUnderlineThickness()  { return _fontFile.getUnderlineThickness() * _size; }

    /**
     * Returns the distance above the baseline that a strikethrough should be drawn.
     */
    public double getStrikethroughOffset()  { return _fontFile.getStrikethroughOffset() * _size; }

    /**
     * Returns whether this font is considered bold.
     */
    public boolean isBold()  { return _fontFile.isBold(); }

    /**
     * Returns whether this font is considered italic.
     */
    public boolean isItalic()  { return _fontFile.isItalic(); }

    /**
     * Returns whether font had to substitute because name wasn't found.
     */
    public boolean isSubstitute()  { return _substitute; }

    /**
     * Returns if this font can display the given char.
     */
    public boolean canDisplay(char aChar)  { return _fontFile.canDisplay(aChar); }

    /**
     * Returns the bold version of this font.
     */
    public Font getBold()
    {
        FontFile boldFontFile = _fontFile.getBold();
        return boldFontFile != null ? new Font(boldFontFile, _size) : null;
    }

    /**
     * Returns the italic version of this font.
     */
    public Font getItalic()
    {
        FontFile italicFontFile =_fontFile.getItalic();
        return italicFontFile != null ? new Font(italicFontFile, _size) : null;
    }

    /**
     * Returns a font with the same family as the receiver but with the given size.
     */
    public Font copyForSize(double aPointSize)
    {
        if (aPointSize == _size)
            return this;
        return new Font(_fontFile, aPointSize);
    }

    /**
     * Returns a font with the same family as the receiver but with size adjusted by given scale factor.
     */
    public Font copyForScale(double aScale)
    {
        double newSize = _size * aScale;
        return copyForSize(newSize);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity, class and get other
        if (anObj == this) return true;
        if (!(anObj instanceof Font)) return false;
        Font other = (Font) anObj;

        // Check FontFile, Size
        if (other._fontFile != _fontFile) return false;
        if (other._size != _size) return false;

        // Return equal
        return true;
    }

    /**
     * Standard hashcode implementation.
     */
    public int hashCode()  { return getName().hashCode() + (int) (_size * 10); }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Name, Size
        aPropSet.addPropNamed(Name_Prop, String.class, null);
        aPropSet.addPropNamed(Size_Prop, double.class, 0d);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Name, Size
            case Name_Prop: return getNameEnglish();
            case Size_Prop: return getSize();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("font");
        e.add("name", getNameEnglish());
        e.add("size", _size);
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        String name = anElement.getAttributeValue("name");
        _fontFile = FontFile.getFontFile(name);
        _size = anElement.getAttributeFloatValue("size");
        return this;
    }

    /**
     * Returns the font name, size and family for this font.
     */
    public String toString()  { return getName() + " " + _size + " (" + getFamily() + ")"; }

    /**
     * Returns the native version of this font.
     */
    public Object getNative()
    {
        if (_native != null) return _native;
        _native = _fontFile.getNative(_size);
        return _native;
    }

    /**
     * Returns the font for the given name and size.
     */
    public static Font getFont(String aName, double aSize)
    {
        FontFile fontFile = FontFile.getFontFile(aName);
        return fontFile != null ? new Font(fontFile, aSize) : null;
    }

    /**
     * Returns the user's default font.
     */
    public static Font getDefaultFont()  { return Arial12; }

    /**
     * Returns a good code font (at 12 pt).
     */
    public static Font getCodeFont()
    {
        if (_codeFont != null) return _codeFont;

        // Get font names and size
        String[] names = { "Monaco", "Consolas", "Lucida Console", "Courier" };

        // Look for font
        for (String name : names) {
            _codeFont = new Font(name, 12);
            if (_codeFont.getFamily().startsWith(name))
                break;
        }

        // Return
        return _codeFont;
    }

    /**
     * Returns a good code font at given point size.
     */
    public static Font getCodeFontForSize(double pointSize)
    {
        return getCodeFont().copyForSize(pointSize);
    }

    /**
     * Returns a list of all system font names.
     */
    public static String[] getFontNames()
    {
        GFXEnv gfxEnv = GFXEnv.getEnv();
        return gfxEnv.getFontNames();
    }

    /**
     * Returns a list of all system family names.
     */
    public static String[] getFamilyNames()
    {
        GFXEnv gfxEnv = GFXEnv.getEnv();
        return gfxEnv.getFamilyNames();
    }

    /**
     * Returns a list of all font names for a given family name.
     */
    public static String[] getFontNames(String aFamilyName)
    {
        GFXEnv gfxEnv = GFXEnv.getEnv();
        return gfxEnv.getFontNames(aFamilyName);
    }

    /**
     * Tries to return a font from given object.
     */
    public static Font of(Object anObj)
    {
        // Handle font or null
        if (anObj instanceof Font || anObj == null)
            return (Font) anObj;

        // Handle string: try to pick off size
        if (anObj instanceof String) {
            String str = (String) anObj;
            String[] parts = str.split("\\s");
            if (parts.length > 1) {
                 double size = Convert.doubleValue(parts[parts.length - 1]);
                 if (size > 0) {
                     parts = Arrays.copyOf(parts, parts.length - 1);
                     String name = String.join("", parts);
                     return getFont(name, size);
                 }
            }
            return getFont(str, 12);
        }

        // Complain and return null
        System.out.println("Font.of: Can't determine front from: " + anObj);
        return null;
    }
}