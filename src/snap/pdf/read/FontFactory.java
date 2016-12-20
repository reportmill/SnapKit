/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.Font;
import java.util.*;
import snap.pdf.PDFFile;

/**
 * A FontFactory object knows how to create fonts from raw data pulled out of a pdf file.
 * The raw data may be just the name of the font, in which case the factory should get the font however it can.
 * Alternately, the data may be actual bytes from a font embedded in the pdf file.
 * 
 * In either case, the FontFactory should create a java.awt.Font object (or a subclass of Font) for use by the engine.
 * 
 * A default FontFactory is supplied, but you can create your own and set it for use with pdfEngine.setFontFactory()
 */
public interface FontFactory {

/** Constants used to identify embedded font types */
public final static int AdobeType0Font = 0;
public final static int AdobeType1Font = 1;
public final static int AdobeMultipleMasterFont = 2;
public final static int AdobeType3Font = 3;
public final static int TrueTypeFont = 4;
public final static int AdobeCIDType0Font = 5;
public final static int AdobeCIDType2Font = 6;
public final static int UnknownFontType = 100;

/** Given a Font dictionary with keys and values as described in the pdf spec,
 * return a java.awt.Font to use for it.
 */
public Font getFont(Map fontDict, PDFFile srcfile);

/** Look on the system for a font with the given name.  */
public Font getFont(String name, String type);

/** Try some font substitutions.  */
public Font getSubstituteFont(Map fontDict);

/** When all else fails, use this font.  Damn well better return something. */
public Font getDefaultFont();

/** Returns an instance of a GlyphMapper object which knows how to translate
 * strings as they would appear in the pdf into a suitable encoding for the font.
 * See GlyphMapper.java for more info.
 */
public GlyphMapper getGlyphMapper(Map fontDict, PDFFile srcfile);

/** Returns the array of widths for all glyphs in the font.
 * PDF Requires this array to be in the font dictionary, except for the standard 14
 * fonts.
 * Note that the array is indexed by character code, not glyph code as mapped 
 * through the encoding.
 * Size of the width array in the dict is last-first+1 in the dictionary,
 * but this routine should always return either an array of 256 chars
 * or a PDFGlyphWidthTable for cid fonts.
 * 
 * It should also do something reasonable if there is no width array (like
 * for the standard 14)
 * Also, width should be for 1 pt font (value in pdf width array/1000).
 */
public Object getGlyphWidths(Map fontDict, PDFFile srcfile);


}
