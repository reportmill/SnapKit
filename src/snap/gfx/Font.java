/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.util.*;

/**
 * This class represents a font for use in rich text. Currently this is necessary because Java fonts are missing
 * so much basic typographic information.
 */
public class Font implements XMLArchiver.Archivable {
    
    // This font's base font file
    private FontFile  _fontFile;
    
    // This fonts point size
    private double  _size;
    
    // Whether this font had to use substitute
    private boolean  _substitute;
    
    // The system native version of this font
    private Object  _native;

    // Whether font not found error has already been printed
    private static boolean  _fontNotFoundErrorPrinted = false;
    
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
            Arial10 = new Font("Arial",10d);
            Arial11 = Arial10.deriveFont(11d);
            Arial12 = Arial10.deriveFont(12d);
            Arial13 = Arial10.deriveFont(13d);
            Arial14 = Arial10.deriveFont(14d);
            Arial16 = Arial10.deriveFont(16d);
        }
        catch(Throwable t) { t.printStackTrace(); throw new RuntimeException(t); }
    }

    /**
     * Creates an empty font (really only used for unarchival).
     */
    public Font()
    {
        this(Arial12._fontFile, 12);
    }

    /**
     * Creates a font for the given font file and point size.
     */
    protected Font(FontFile aFontFile, double aPointSize)
    {
        _fontFile = aFontFile;
        _size = aPointSize;
    }

    /**
     * Returns the font for the given name and size.
     */
    public Font(String aName, double aSize)
    {
        // Get fontFile for aName
        _fontFile = FontFile.getFontFile(aName);
        _size = aSize;

        // If fontFile not found, substitute Helvetica (try to get the style right)
        if (_fontFile==null) {
            _substitute = true; _fontFile = Arial10.getFontFile();

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
    public double charAdvance(char aChar)  { return _fontFile.charAdvance(aChar)*_size; }

    /**
     * Returns the char advance for a given character.
     */
    public double getCharAdvance(char aChar, boolean isFractional)
    {
        double adv = _fontFile.charAdvance(aChar)*_size;
        return isFractional ? adv : Math.round(adv);
    }

    /**
     * Returns the kerning for the given pair of characters (no way to do this in Java!).
     */
    public double getCharKern(char aChar1, char aChar2)  { return _fontFile.getCharKern(aChar1, aChar2)*_size; }

    /**
     * Returns the path for a given character.
     */
    public Shape getCharPath(char aChar)  { return _fontFile.getCharPath(aChar); }

    /**
     * Returns the bounds for a given character.
     */
    public Rect getCharBounds(char aChar)
    {
        Shape path = getCharPath(aChar);
        Rect cbounds = path.getBounds();
        cbounds.scale(_size);
        return cbounds;
    }

    /**
     * Returns the horizontal distance spanned by the given string when rendered in this font.
     */
    public double getStringAdvance(String aString)
    {
        double w = 0;
        for (int i=0, iMax=aString.length(); i<iMax; i++)
            w += charAdvance(aString.charAt(i));
        return w;
    }

    /**
     * Returns the bounds rect for given string.
     */
    public Rect getStringBounds(String aString)
    {
        double w = getStringAdvance(aString);
        return new Rect(0, getDescent(), w, getDescent() + getAscent());
    }

    /**
     * Returns the bounds rect for glyphs in given string.
     */
    public Rect getGlyphBounds(String aString)
    {
        Rect rect = _fontFile.getGlyphBounds(aString);
        double size = getSize();
        rect.y = rect.y/1000d*size;
        rect.width = rect.width/1000d*size;
        rect.height = rect.height/1000d*size;
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
    public double getAscent()  { return _fontFile.getAscent()*_size; }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()  { return _fontFile.getDescent()*_size; }

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()  { return _fontFile.getLeading()*_size; }

    /**
     * Returns the height for a line of text in this font.
     */
    public double getLineHeight()  { return _fontFile.getLineHeight()*_size; }

    /**
     * Returns the distance from the top of a line of text to the to top of a successive line of text.
     */
    public double getLineAdvance()  { return _fontFile.getLineAdvance()*_size; }

    /**
     * Returns the distance below the baseline that an underline should be drawn.
     */
    public double getUnderlineOffset()  { return _fontFile.getUnderlineOffset()*_size; }

    /**
     * Returns the default thickness that an underline should be drawn.
     */
    public double getUnderlineThickness()  { return _fontFile.getUnderlineThickness()*_size; }

    /**
     * Returns the distance above the baseline that a strikethrough should be drawn.
     */
    public double getStrikethroughOffset()  { return _fontFile.getStrikethroughOffset()*_size; }

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
        FontFile ff = _fontFile.getBold();
        return ff!=null ? new Font(ff, _size) : null;
    }

    /**
     * Returns the italic version of this font.
     */
    public Font getItalic()
    {
        FontFile ff =_fontFile.getItalic();
        return ff!=null ? new Font(ff, _size) : null;
    }

    /**
     * Returns a font with the same family as the receiver but with the given size.
     */
    public Font deriveFont(double aPointSize)
    {
        return aPointSize==_size ? this : new Font(_fontFile, aPointSize);
    }

    /**
     * Returns a font with the same family as the receiver but with size adjusted by given scale factor.
     */
    public Font scaleFont(double aScale)
    {
        return aScale==1 ? this : new Font(_fontFile, _size*aScale);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity, class and get other
        if (anObj==this) return true;
        if (!(anObj instanceof Font)) return false;
        Font font = (Font)anObj;

        // Check FontFile, Size
        if (font._fontFile!=_fontFile) return false;
        if (font._size!=_size) return false;
        return true; // Return true since all checks passed
    }

    /**
     * Standard hashcode implementation.
     */
    public int hashCode()  { return getName().hashCode() + (int)(_size*10); }

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
        if (_native!=null) return _native;
        _native = _fontFile.getNative(_size);
        return _native;
    }

    /**
     * Returns the font for the given name and size.
     */
    public static Font get(String aName, double aSize)
    {
        return getFont(aName, aSize);
    }

    /**
     * Returns the font for the given name and size.
     */
    public static Font getFont(String aName, double aSize)
    {
        FontFile ffile = FontFile.getFontFile(aName);
        return ffile!=null ? new Font(ffile, aSize) : null;
    }

    /**
     * Returns the user's default font.
     */
    public static Font getDefaultFont()  { return Arial12; }

    /**
     * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
     */
    public static String[] getFontNames()  { return GFXEnv.getEnv().getFontNames(); }

    /**
     * Returns a list of all system family names.
     */
    public static String[] getFamilyNames()  { return GFXEnv.getEnv().getFamilyNames(); }

    /**
     * Returns a list of all font names for a given family name.
     */
    public static String[] getFontNames(String aFamilyName)
    {
        return GFXEnv.getEnv().getFontNames(aFamilyName);
    }
}