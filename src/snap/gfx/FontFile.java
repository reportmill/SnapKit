/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Path;
import snap.geom.Rect;
import snap.geom.Shape;

import java.util.*;

/**
 * This class represents all the information about a font that is independent of size. This allows Font to be 
 * lighter weight (essentially just a font file at a given size).
 */
public abstract class FontFile {
    
    // Cached "canDisplay" bitset
    private BitSet  _canDisplay = new BitSet(128);
    
    // Cached reference to bold version of font
    private FontFile  _boldVersion = null;
    
    // Cached reference to italic version of font
    private FontFile  _italicVersion = null;
    
    // Cache of char widths
    private float  _charWidths[] = new float[0];
    
    // Cached glyph paths
    private Map<Character, Shape>  _glyphPaths = new Hashtable<>();
    
    // Cached map of all previously encountered font files
    private static Map<String,FontFile>  _allFontFiles = new Hashtable<>();
    
    /**
     * Returns a font file for a given font name.
     */
    public static synchronized FontFile getFontFile(String aName)
    {
        FontFile ffile = _allFontFiles.get(aName);
        if (ffile==null)
            _allFontFiles.put(aName, ffile = GFXEnv.getEnv().getFontFile(aName));
        return ffile;
    }

    /**
     * Returns the name of this font.
     */
    public abstract String getName();

    /**
     * Returns the name of this font in English.
     */
    public String getNameEnglish()  { return getName(); }

    /**
     * Returns the family name of this font.
     */
    public abstract String getFamily();

    /**
     * Returns the family name of this font in English.
     */
    public String getFamilyEnglish()  { return getFamily(); }

    /**
     * Returns the PostScript name of this font.
     */
    public String getPSName()  { return getName(); }

    /**
     * Returns the char advance for the given char.
     */
    public double charAdvance(char aChar)
    {
        // If char in cache range, load from cache (might have to load cache too)
        if (aChar<_charWidths.length) {
            double cwidth = _charWidths[aChar];
            if (cwidth<0)
                cwidth = _charWidths[aChar] = (float)charAdvanceImpl(aChar);
            return cwidth;
        }

        // Extend cache if less than CharWidthsLength (1200 + 256, 1456)
        if (aChar<1456) synchronized (this) {
            int oldLen = _charWidths.length;
            int newLen = oldLen==0 && aChar<256 ? 256 : 1456;
            _charWidths = Arrays.copyOf(_charWidths, newLen);
            Arrays.fill(_charWidths, oldLen, newLen, -1);
            return charAdvance(aChar);
        }

        // Get value straight from FontMetrics
        return charAdvanceImpl(aChar);
    }

    /**
     * Returns the char advance for the given char.
     */
    protected abstract double charAdvanceImpl(char aChar);

    /**
     * Returns the bounds rect for glyphs in given string.
     */
    public abstract Rect getGlyphBounds(String aString);

    /**
     * Returns the kerning for the given pair of characters (no way to do this in Java!).
     */
    public double getCharKern(char aChar1, char aChar2)  { return 0; }

    /**
     * Returns the path for a given character.
     */
    public Shape getCharPath(char aChar)
    {
        // See if aChar's path has been cached in _glyphPaths (if so, return it)
        Shape path = _glyphPaths.get(aChar); if (path!=null) return path;

        // Get path for char (try glyph at index 0 if that fails)
        path = getCharPathImpl(aChar);
        if (path==null)
            path = getCharPathImpl((char)0);

        // Add path to glyph paths map
        if (path!=null)
            _glyphPaths.put(aChar, path);

        // Return path
        return path;
    }

    /**
     * Returns the path for a given char (does the real work, but doesn't cache).
     */
    protected abstract Shape getCharPathImpl(char c);

    /**
     * Returns the path for given string with character spacing.
     */
    public Shape getOutline(CharSequence aStr, double aSize, double aX, double aY, double aCharSpacing)
    {
        Path path = new Path();
        double x = aX;
        double descent = getDescent()*aSize;
        for (int i=0, iMax=aStr.length();i<iMax; i++) { char c = aStr.charAt(i);
           Shape cpath = getCharPath(c);
           Rect charBnds = cpath.getBounds();
           double charW = charBnds.getWidth();
           double charH = charBnds.getHeight();
           cpath = cpath.copyFor(new Rect(x, aY - descent, charW*aSize/1000, charH*aSize/1000));
           path.append(cpath);
           x += charAdvance(c)*aSize + aCharSpacing;
        }
        return path;
    }

    /**
     * Returns the max distance above the baseline that this font goes.
     */
    public abstract double getAscent();

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public abstract double getDescent();

    /**
     * Returns the default distance between lines for this font.
     */
    public abstract double getLeading();

    /**
     * Returns the height of a line of text in this font.
     */
    public double getLineHeight()  { return getAscent() + getDescent(); }

    /**
     * Returns the distance from the top of a line of text to the to top of a successive line of text.
     */
    public double getLineAdvance()  { return getAscent() + getDescent() + getLeading(); }

    /**
     * Returns the max advance of characters in this font.
     */
    public double getMaxAdvance()  { return charAdvance('W'); }

    /**
     * Returns the distance below the baseline that an underline should be drawn.
     */
    public double getUnderlineOffset()  { return -getDescent()/2; }

    /**
     * Returns the default thickness that an underline should be drawn.
     */
    public double getUnderlineThickness()  { return 1/16f; }

    /**
     * Returns the distance above the baseline that a strikethrough should be drawn.
     */
    public double getStrikethroughOffset()  { return getAscent()/2; }

    /**
     * Returns whether this font is considered bold.
     */
    public boolean isBold()  { return getNameEnglish().indexOf("Bold")>0; }

    /**
     * Returns whether this font is considered italic.
     */
    public boolean isItalic()  { return getNameEnglish().indexOf("Italic")>0 || getNameEnglish().indexOf("Oblique")>0; }

    /**
     * Returns if this font can display the given char.
     */
    public boolean canDisplay(char aChar)
    {
        // If aChar is set in _canDisplay bitset, return true
        if (_canDisplay.get(aChar))
            return true;

        // Get AWT Font canDisplay (not sure I need all the extra checks)
        boolean canDisplay = canDisplayImpl(aChar) ||
            aChar=='\n' || aChar=='\r' || aChar=='\t' || aChar==' ' ||
            (aChar<256 && (getNameEnglish().startsWith("Wingdings") || getNameEnglish().startsWith("Webdings")));

        // If true and less than 128, set in bitset
        if (canDisplay && aChar<128)
            _canDisplay.set(aChar);

        // Return can display
        return canDisplay;
    }

    /**
     * Returns if this font can display the given char.
     */
    protected boolean canDisplayImpl(char aChar)  { return true; }

    /**
     * Returns the bold version of this font.
     */
    public FontFile getBold()
    {
        // If bold version set, just return
        if (_boldVersion!=null) return _boldVersion!=this ? _boldVersion : null;

        // Get list of font names in this font's family
        String familyNames[] = GFXEnv.getEnv().getFontNames(getFamily());

        // Iterate over font names and find font file with highest "MatchFactor"
        for (int i=0, iMax=familyNames.length, matchFactor=0; i<iMax; i++) { String fname = familyNames[i];

            // Get font file for font name
            FontFile fontFile = getFontFile(fname);

            // If this font differs in boldness...
            if (isBold()!=fontFile.isBold()) {

                // Really weight matchFactor for versions that match italic condition
                int newMF = isItalic()==fontFile.isItalic() ? 1000 : 0;

                // Weight matchFactor for matching words (+10 for matching words, -1 for missing words)
                newMF += matchingWords(getName(), fontFile.getName());

                if (newMF>matchFactor) {
                    matchFactor = newMF;
                    _boldVersion = fontFile;
                }
            }
        }

        // If bold version wasn't found, set bold version to this font (so we'll know we looked)
        if (_boldVersion==null)
            _boldVersion = this;

        // Return bold version (or null if version is this font)
        return _boldVersion==this ? null : _boldVersion;
    }

    /**
     * Returns the italic version of this font.
     */
    public FontFile getItalic()
    {
        // If italic version set, just return
        if (_italicVersion!=null) return _italicVersion!=this ? _italicVersion : null;

        // Get list of font names in this font's family
        String list[] = GFXEnv.getEnv().getFontNames(getFamily());

        // Iterate over font names and find font file with highest "MatchFactor"
        for (int i=0, iMax=list.length, matchFactor=0; i<iMax; i++) { String fname = list[i];

            // Get font file for font name
            FontFile fontFile = getFontFile(fname);

            // If this font differs in italicness...
            if (isItalic()!=fontFile.isItalic()) {

                // Definitely weight matchFactor for versions that match bold condition
                int newMF = isBold()==fontFile.isBold() ? 1000 : 0;

                // Weight matchFactor for matching words (+10 for matching words, -1 for missing words)
                newMF += matchingWords(getName(), fontFile.getName());

                if (newMF>matchFactor) {
                    matchFactor = newMF;
                    _italicVersion = fontFile;
                }
            }
        }

        // If italic version wasn't found, set italic version to this font (so we'll know we looked)
        if (_italicVersion==null)
            _italicVersion = this;

        // Return italic version (or null if version is this font)
        return _italicVersion==this ? null : _italicVersion;
    }

    /**
     * Returns the system native version of this font file.
     */
    public abstract Object getNative();

    /**
     * Returns the system native name of this font file.
     */
    public String getNativeName()  { return getName(); }

    /**
     * Returns the system native version of font for given size.
     */
    public abstract Object getNative(double aSize);

    /**
     * Returns the font name of this font file.
     */
    public String toString()  { return getName(); }

    /**
     * Utility method to determine the number of matching words in two phrases.
     */
    private int matchingWords(String s1, String s2)
    {
        String ls = s1.length()>s2.length() ? s1 : s2;
        String ss = ls==s1 ? s2 : s1;
        int sc = 0, ec = 1, mwc = 0;

        while (ec<=ls.length()) {
            char ecc = ec<ls.length() ? ls.charAt(ec) : ' ';
            if (Character.isUpperCase(ecc) || (ecc==' ') || (ecc=='-')) {
                String word = ls.substring(sc, ec);
                if (ss.indexOf(word)>=0)
                    mwc += 10;
                else mwc--;
                sc = Character.isUpperCase(ecc) ? ec : ec + 1;
                ec = sc;
            }
            ec++;
        }

        return mwc;
    }
}