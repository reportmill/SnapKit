/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;
import snap.util.StringUtils;

/**
 * PDFFontFactory.java 
 */
public class PDFFontFactory implements FontFactory {

/**
 * Given a Font dictionary with keys and values as described in the pdf spec, return java.awt.Font to use for it.
 */
public Font getFont(Map fontDict, PDFFile srcfile)
{
    if (fontDict==null) 
        return getDefaultFont();
    
    // First check to see if we've created & cached the font already
    Font awtFont = (Font)fontDict.get("_rbcached_awtfont_");
    if (awtFont != null)
        return awtFont;

    // The (optional) PDF FontDescriptor dictionary
    Map descriptor;
    Object fobj;
    // The Subtype (ie. The font format.  awt likes TrueType fonts)
    String type = (String)fontDict.get("Subtype");
    
    // Check first to see if the font was embedded in the file
    if (type.equals("/TrueType") || type.equals("/CIDFontType2")) {
        // resolve any indirect reference to the FontDescriptor
        descriptor = (Map)srcfile.getXRefObj(fontDict.get("FontDescriptor"));
        if (descriptor != null) {
            // TrueType fonts are embedded as streams under the key FontFile2
            fobj = srcfile.getXRefObj(descriptor.get("FontFile2"));
            // Another possibility is to have FontFile3, with a FontFile2 subtype
            if (fobj==null) {
                fobj = srcfile.getXRefObj(descriptor.get("FontFile3"));
                if (fobj instanceof PDFStream)
                    if (!((PDFStream)fobj).getDict().get("Subtype").equals("/FontFile2"))
                        fobj=null;
            }
            if ((fobj != null) && (fobj instanceof PDFStream)) {
                // get the fontfile bytes from the stream
                try {
                    byte fontFile[] = ((PDFStream)fobj).decodeStream();
                
                    // make a stream for the bytes and let awt try to read the font
                    InputStream fstream = new ByteArrayInputStream(fontFile, 0, fontFile.length);
                    awtFont = Font.createFont(Font.TRUETYPE_FONT, fstream);
                    fstream.close();
                }
                catch(Exception e) {
                    System.err.println("Error loading font \""+fontDict.get("BaseFont")+"\" : "+e);
                    awtFont=null;
                }
            }
        }
    }
    else if (type.equals("/Type0")) { // composite font with a single single cid font descendant
        return getFont(getDescendantFont(fontDict, srcfile), srcfile);
    }
    // If Font wasn't embedded in the file, is an unsupported type, or it couldn't be read,
    // look on the system using the font's name.
    if (awtFont == null) {
        String fontName = (String)srcfile.getXRefObj(fontDict.get("BaseFont"));
        if (fontName != null) 
            awtFont = getFont(fontName.substring(1), type);
    }
    
    // Still couldn't get the font.  Try font substitution
    if (awtFont == null)
        awtFont = getSubstituteFont(fontDict);
    
    // Oy vey, still no font.  Get the default
    if (awtFont == null) {
        System.err.println("Couldn't get a font for \""+fontDict.get("BaseFont")+"\"");
        awtFont = getDefaultFont();
    }

    // cache it
    fontDict.put("_rbcached_awtfont_",awtFont);
    return awtFont;
}

/** Look on the system for a font with the given name.  */
public Font getFont(String name, String type)
{
    int fstyle = Font.PLAIN;
    
    if (type.equals("/TrueType")) {
        List fontPieces = StringUtils.separate(name, ",");
        int nPieces = fontPieces.size();
        
        if (nPieces==0)
            return null;
        
        name = (String)fontPieces.get(0);
        if (nPieces>1) {
            String styleString = (String)fontPieces.get(1);
            if (styleString.equals("Bold"))
                fstyle = Font.BOLD;
            else if (styleString.equals("Italic"))
                fstyle = Font.ITALIC;
            else if (styleString.equals("BoldItalic"))
                fstyle = Font.BOLD|Font.ITALIC;
        }
    }
    return new Font(name, fstyle, 1);
}

/** Try some font substitutions. TODO:  Might be able to do a half-assed job using java.awt.font.TextAttributes. */
public Font getSubstituteFont(Map fontDict)  { return null; }

/** When all else fails, use this font.  Damn well better return something. */
public Font getDefaultFont()  { return new Font("SansSerif", Font.PLAIN, 1); }

/** Create a glyphmapper for the font specified by the pdf font dictionary */
public GlyphMapper getGlyphMapper(Map fontDict, PDFFile srcfile)
{
    // Check if we did it already
    GlyphMapper mapper = (GlyphMapper)fontDict.get("_rbcached_glyphmapper_");
    if (mapper != null)
        return mapper;

    mapper = GlyphMapper.createGlyphMapper(fontDict,srcfile);
    // For composite fonts, if descendant CID font knows how to map CIDs to glyphs, add that information to glyphmapper.
    if ("/Type0".equals(fontDict.get("Subtype"))) {
        Map child = getDescendantFont(fontDict, srcfile);
        mapper.setCIDToGIDMap(srcfile.getXRefObj(child.get("CIDToGIDMap")));
    }

    fontDict.put("_rbcached_glyphmapper_", mapper);
    return mapper;
}

/**
 * Utility routine for composite fonts.  Composite fonts have a single descendant CID font.
 */
Map getDescendantFont(Map fontDict, PDFFile srcfile)
{
    if ("/Type0".equals(fontDict.get("Subtype"))) {
        List descendants = (List)srcfile.getXRefObj(fontDict.get("DescendantFonts"));
        Map child;
        String subtype;
        if (descendants == null)
            throw new PDFException("Can't find descendant for Type 0 composite font "+fontDict);
        child = (Map)srcfile.getXRefObj(descendants.get(0));
        // This is the anally-retentive check to guard against malformed files
        // which would lead to infinite recursion.
        subtype = (String)child.get("Subtype");
        if ((subtype == null) || (!subtype.startsWith("/CIDFont")))
            throw new PDFException("Descendant of Type 0 composite font must be a CID font");
        return child;
    }
    return null;
}

/**
 * Returns the widths for all glyphs in the fonts. Return value is either a float[] for simple single-byte fonts or an
 * instance of a PDFGlyphWidthTable for multi-byte or CID fonts.
 */
public Object getGlyphWidths(Map fontDict, PDFFile srcfile)
{
    Object obj = fontDict.get("_rbcached_glyphwidths_");
    int i;
    float missing=1;
    
    if (obj != null)
        return obj;
    
    String type = (String)fontDict.get("Subtype");
    
    //Composite fonts look up widths in their descendant
    if (type.equals("/Type0")) 
        obj = getGlyphWidths(getDescendantFont(fontDict, srcfile), srcfile);
    //CIDFonts look up their widths from a GlyphWidthTable
    else if (type.startsWith("/CIDFontType")) {
        obj = new PDFGlyphWidthTable((List)srcfile.getXRefObj(fontDict.get("W")), fontDict.get("DW"));        
    }
    //Single byte fonts use a simple array of 256 floats
    else { 
        float widths[] = new float[256];
   
        //Get the optional MissingWidth from the not-really-optional font descriptor
        Map descriptor=(Map)srcfile.getXRefObj(fontDict.get("FontDescriptor"));
        if (descriptor != null) {
            obj = descriptor.get("MissingWidth");
            if (obj != null)
                missing = ((Number)obj).floatValue()/1000f;
        }

        // If there's a width array, use it
        Object pdfw = srcfile.getXRefObj(fontDict.get("Widths"));
        if ((pdfw != null) && (pdfw instanceof List)) {
            List wlist = (List)pdfw;
            int first = ((Number)fontDict.get("FirstChar")).intValue();
            int last = ((Number)fontDict.get("LastChar")).intValue();
            
            //Fill in the float array.
            for(i=0; i<256; ++i) {
                if ((i<first) || (i>last))
                    widths[i] = missing;
                else
                    widths[i] = ((Number)wlist.get(i-first)).floatValue()/1000f;
            }
        }
        else {
            // No width array.  Should only happen for standard14 fonts
            // Use awt and cross your fingers
            Font aFont = getFont(fontDict, srcfile);
            Graphics g = srcfile.getMarkupHandler().getGraphics();
             
            if (g!=null) {
                FontMetrics metrics = g.getFontMetrics(aFont.deriveFont(1000f));
                int iwidths[] = metrics.getWidths();
                
                //using a 1000 pt font to get the metrics
                for(i=0; i<256; ++i)
                    widths[i]=iwidths[i]/1000f;
            }
            else { // no width info available.  Everybody gets missingwidth, which will be wrong
                for(i=0; i<256; ++i)
                    widths[i]=missing;
            }
        }
        obj = widths;
    }
    
    // cache it
    fontDict.put("_rbcached_glyphwidths_", obj);
    return obj;
}

}