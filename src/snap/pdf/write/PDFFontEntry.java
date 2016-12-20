/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import java.util.*;
import snap.gfx.*;

/**
 * An inner class to store font info.
 * There are individual entries when a font has chars beyond 255, in blocks of 256.
 */
public class PDFFontEntry {

    // Original Font
    Font              _font;
    
    // Name of font for PDF file
    String            _pdfName;
    
    // Chars present in base range (0-255), represented as boolean array
    boolean           _chars[];
    
    // Chars present in extended range (anything above 255), represented as char array
    List <Character>  _uchars;
    
    // The font entry
    int               _fontCharSet;
    
/**
 * Creates a new font entry for a given pdf file and font.
 */
public PDFFontEntry(Font aFont, int fontCharSet, PDFFontEntry rootEntry)
{
    // Set font
    _font = aFont;
    
    // Set name from font PostScript name (so it will be a legal PDF name too)
    _pdfName = aFont.getPSName(); // Used to be RMStringUtils.replace(aFont.getFontNameEnglish(), " ", "-");
    
    // Set font char set
    _fontCharSet = fontCharSet;
    
    // If root char set, do stuff
    if(fontCharSet==0) {
    
        // Initialize present chars for base font (this is bogus, shouldn't write base font if not used)
        _chars = new boolean[256];
        _chars[32] = true;
        
        // Initialize present chars for extended fonts
        _uchars = new ArrayList();
    }
    
    // Otherwise do other stuff
    else {
        _pdfName += "." + fontCharSet;
        _chars = rootEntry._chars;
        _uchars = rootEntry._uchars;
    }
}

/**
 * Returns the font for this font entry.
 */
public Font getFont()  { return _font; }

/**
 * Returns the char set for this font entry. Font entries represent blocks of 256 chars for a given font, and the
 * char set is the index of the current font entry's block of 256 chars.
 */
public int getCharSet()  { return _fontCharSet; }

/**
 * Returns the char count for this font entry.
 */
public int getCharCount()  { return Math.min(_uchars.size() - (_fontCharSet-1)*256, 256); }

/**
 * Returns the char at the given index for this font entry.
 */
public char getChar(int anIndex)  { return _uchars.get((_fontCharSet-1)*256 + anIndex); }

/**
 * Returns the pdf name of the font entry font.
 */
public String getPDFName()  { return _pdfName; }

}