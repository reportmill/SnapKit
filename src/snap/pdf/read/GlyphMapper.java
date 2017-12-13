/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.*;
import java.io.*;
import snap.pdf.*;

/**
 * A GlyphMapper object knows how to take an array of bytes and convert them into propper format for Font
 * 
 * The pdf engine will get the glyphs from the font by calling the createGlyphVector routines, so this object needs
 * to return its data such that createGlyphVector will do the right thing.
 * 
 * By default, java.awt.Font reads the unicode cmap from the font when createGlyphVector is called.  So, if font
 * has a valid unicode cmap table, this object can simply map the input string into unicode.  
 * 
 * The font in the pdf file includes an Encoding dictionary which specifies not only some standard font encoding names
 * (WinAnsiEncoding, MacRomanEncoding, etc) but also a mapping for specific glyphs.  These dictionaries specify glyphs
 * by name, so the mapper needs a way to take the glyph name and turn it into a value that ultimately will get mapped
 * to the right thing via createGlyphVector.
 * 
 * Here's a typical Encoding dictionary:
 *   << /Type /Encoding
 *      /BaseEncoding /MacRomanEncoding
 *      /Differences [ 219 /Euro ] >> 
 * In this case, the mapper needs to know not only how to convert bytes in MacRomanEncoding into unicode, but also it
 * needs to be able to find the glyph named "Euro" (an adobe glyph name) in the unicode set (EURO SIGN : U+20AC)
 * and use that whenever the input string contains the value 219. This class has an adobeGlyphNameToUnicode() method
 * to help with that problem.
 * 
 * Some fonts have CMaps instead of encoding dictionaries.  These are structures for specifying the encodings of large
 * numbers of characters.  Their source data may be multi-byte, unlike fonts encoded with an encoding dictionary,
 * whose strings are specified as single bytes.
 * 
 * CMaps can come into play for "CID" fonts.  In a CID font, the raw bytes passed to a text showing operation are first
 * passed through a CMap (which a subclass of this object can implement) to return a CID. There are built-in CMaps (the
 * most important being /Identity-H & /Identity-V) along with potentially embedded CMaps.
 * In a CID font, a particular character id may be specified by one or more bytes from the input data.  Different CIDs
 * in the same CMap can be selected by different numbers of bytes, allowing for encodings like shift-JIS, where roman
 * chars are identified by one byte but japanese chars are identified by two bytes.
 * 
 * So the process for rendering text becomes:
 *   1.  Map input bytes to CIDs (unicode for simple fonts, through a CMap for CIDFont)
 *   2.  a.  If the CIDs are Unicode, render them with the font.  (AWT fonts should
 *           understand unicode).
 *       b.  Otherwise, map the CID to a glyph ID (GID) and render by specifying actual
 *       glyph codes.  This only works for embedded fonts, since that's really the
 *       only way to guarantee that a particular GID will select the right glyph.
 *       This second mapping from cid->gid is done through the use of a special
 *       stream embedded along with the font with the key "CIDToGIDMap"
 *       
 * A GlyphMapper object, therefore, has 2 responsibilities - one required and one optional. First, it must know how to
 * convert an input stream of bytes into an output stream of CIDs.  Second, if it knows how, it can convert an input
 * stream of CIDs into GIDs.
 */
public abstract class GlyphMapper {

/**
 * Creates a new mapper for the font.  For Latin fonts, the encodingDict is the dict pulled out of the pdf file
 * (with /BaseFont & /Differences entries) more complex encoding dicts could contain cmaps or other info.
 */
public GlyphMapper(Map fontDict, PDFFile srcfile)  { }

/**
 * Returns true if this subclass knows how to handle the given encoding.
 */
public static boolean canHandleEncoding(String pdfEncodingName)  { return false; }

/**
 * Returns true if the encoding consumes more than single byte for a character. Note that encodings that sometimes
 * maps single bytes, but other times maps more than one, should always return true for this routine.
 */
public abstract boolean isMultiByte();

/**
 * Quickly returns maximum number of chars that could result from conversion of inbytes bytes according to encoding.
 * A single byte encoding, like the latin ones, will take a single byte and convert it into a single char, so this
 * routine would just return len. An encoding that takes 2 bytes and converts them to a char would return len/2, etc.
 */
public abstract int maximumOutputBufferSize(byte inbytes[], int offset, int len);

/**
 * The workhorse.  Take an input buffer and convert everything. The output buffer should be allocated using the size
 * from the above routine.  The actual number of chars in the final output is returned.
 */
public abstract int mapBytesToChars(byte inbytes[], int offset, int len, char outchars[]);

/**
 * Returns true if encoding knows how to convert to glyph indices.
 */
public boolean supportsCIDToGIDMapping() { return false; }

/**
 * Sets cid to glyph index map info. This object will will be interpreted as whatever format is appropriate for
 * particular glyphmapper.
 */
public void setCIDToGIDMap(Object mapobj)  { }

/**
 * Does the cid->gid mapping, if it knows how. Returns total number of glyphs created.
 */
public int mapCharsToGIDs(char cidbuffer[], int numcids, int gidbuffer[])  { return 0; }

/**
 * Search routine to find a GlyphMapper subclass.
 */
public static GlyphMapper createGlyphMapper(Map fontDict, PDFFile srcfile)
{  
    Object encode = srcfile.getXRefObj(fontDict.get("Encoding"));
    String ename=null;
    
    if (encode instanceof String)
        ename = (String)encode;
    else if (encode instanceof Map)
        ename = (String)((Map)encode).get("BaseEncoding");
    
    // Check Latin GlyphMapper
    if(Latin.canHandleEncoding(ename))
        return new Latin(fontDict, srcfile);
        
    // Check Identity GlyphMapper
    if(Indentity.canHandleEncoding(ename))
        return new Indentity(fontDict, srcfile);
        
    // TODO: take this out eventually and replace with a default (probably winansi)
    throw new PDFException("unimplemented encoding name:"+ename);
}

//---- utility routines used by concrete subclasses -----

/**
 * Maps a single glyph name string to a unicode value. This is an implementation of the algorithm described in
 * http://partners.adobe.com/public/developer/opentype/index_glyph.html
 */
public static int adobeGlyphNameToUnicode(String name, Map fontDict)
{
    // Step 1: drop the first period and anything after, if present
    int index = name.indexOf('.');
    if(index >= 0) {
        name = name.substring(0, index); }

    // Step 2: split along underscores
    String components[] = name.split("_");

    // Step 3: map
    // General algorithm maps multiple components to a unicode string. We only map the first one, for now. This
    // implies a one-to-one mapping of all character codes to unicode characters.
    if(components.length < 1)
        return -1;
    name = components[0];

    Object val=null;
    if(fontDict!=null && "/ZapfDingbats".equals(fontDict.get("BaseFont")))
        val=adobeZapfDingbatsGlyphList().get(name);
    if(val==null)
        val = adobeGlyphList().get(name);

    int uval = -1;
    if(val!=null) {
        if(val instanceof List) 
            val = ((List)val).get(0); // "You take what you need, & you leave the rest..."
        uval = ((Number)val).intValue();
    }
    
    else {
        try {
            int len=name.length();
            if (name.startsWith("uni") && (len >= 7))
                // again, could be multiple values, like uni20a0403c but we're punting
                uval = Integer.parseInt(name.substring(3, 7), 16);
            else if (name.startsWith("u") && (len >= 5) && (len <= 7))
                uval = Integer.parseInt(name.substring(1), 16);
        }
        catch (NumberFormatException nfe) { } // chars after u or uni didn't parse as hex Fall to unknown glyph case
    }
    
    // Return uval
    return uval;
}

// Cached maps
static private Map _zapfDingbatsMap, _glyphMap;

/**
 * Returns the unicode map for glyph names in the ZapfDingbats set, lazily reading them in.
 */
static Map adobeZapfDingbatsGlyphList()
{
    if(_zapfDingbatsMap!=null) return _zapfDingbatsMap;
    return _zapfDingbatsMap = readGlyphList("ZapfDingbatsGlyphList.txt");
}

/**
 * Returns the unicode map for glyph names in the standard set.
 */
static Map adobeGlyphList()  { return _glyphMap!=null? _glyphMap : (_glyphMap=readGlyphList("AdobeGlyphList.txt")); }

/**
 * Parse adobe glyph files and turn them into a map of glyphnames -> unicode values.
 */
static Map readGlyphList(String name)
{
    try {
        InputStream s = GlyphMapper.class.getResourceAsStream(name);
        if(s==null) throw new IOException("Internal error: couldn't locate resouce " + name);

        // Have to reset the syntax, because it parses numbers by default, but it does so as decimal.
        // Since all number in the file are hex, we need to turn that off
        Reader r = new BufferedReader(new InputStreamReader(s));
        StreamTokenizer parser = new StreamTokenizer(r);
        parser.resetSyntax();
        parser.wordChars('0', '9'); parser.wordChars('a', 'z'); parser.wordChars('A', 'Z');
        parser.wordChars(128 + 32, 255);
        parser.whitespaceChars(0, ' ');
        parser.commentChar('#'); // lines starting with a # are comments.
        parser.eolIsSignificant(true); // let us know about lineends, so we get glyphs that map to more than one code
        parser.whitespaceChars(';', ';'); // The name of the glyph and its code are separated by semicolons
       
        Hashtable table = new Hashtable(256); int token;
        while((token=parser.nextToken())!=StreamTokenizer.TT_EOF) {
            
            // Turn a single char glyph name into a string (ascii glyph names only)
            String glyphname = null;
            if(token>65 && token<128) glyphname = String.valueOf((char)token);
            else if(token==StreamTokenizer.TT_WORD) glyphname = parser.sval;
                
            // Got a glyph name. Parse the rest of the line
            if(glyphname!=null) {
                Integer glyphcode = null;
                ArrayList codes = null;
                do {
                    token = parser.nextToken();
                    if(token==StreamTokenizer.TT_WORD) {
                        // codes are in hex
                        int i = Integer.parseInt(parser.sval, 16);
                        if(glyphcode==null)
                            glyphcode = i;
                        else {
                            // multiple codes get turned into an array
                            if(codes==null) {
                                codes = new ArrayList(2);
                                codes.add(glyphcode);
                            }
                            codes.add(i);
                        }
                    }
                } while((token!=StreamTokenizer.TT_EOL) && (token!=StreamTokenizer.TT_EOF));

                // Done with the line, add the created Integer or List to the hashtable
                if(glyphcode==null) System.err.println("GlyphMapper.readGlyphList: No codes?");
                else table.put(glyphname, codes!=null? (Object)codes : (Object)glyphcode);
            }
        }
        return table;
    }
        
    // bad news: should only get here if a file is missing from jar or if a parse error with file. It's unrecoverable.
    catch (Exception e) { throw new RuntimeException(e); }
}

/* For testing purposes */
public static void main(String args[])
{
    String gname = "zeroinferior"; if(args.length>0) gname = args[0];
    int lookup = adobeGlyphNameToUnicode(gname, null);
    System.out.println(gname + " = " + Integer.toHexString(lookup));
}

/**
 * A concrete subclass of GlyphMapper for the Identity-H & Identity-V maps input bytes -> cids is done with no mapping,
 * just interpreting the bytes as big-endian shorts. Also supports mapping to GIDs via an embedded CIDToGIDMap
 *
 * Note that for fonts with CIDToGIDMaps, it might be tempting to provide a way to skip the CID step and just go from
 * input bytes to GIDs. However, all the font metric info is based on CIDs, so we're always going to have to convert to
 * CIDs no matter what.
 */
public static class Indentity extends GlyphMapper {
    
    // Ivars
    int cidsToGids[]=null;
    boolean identityCidToGid=false;
    
    /** Create new Identity GlyphMapper. */
    public Indentity(Map fontDict, PDFFile srcfile)  { super(fontDict, srcfile); }
    
    public boolean isMultiByte() { return true; }
    
    /** For CID fonts that know how to map their cids directly into glyph indices. */
    public void setCIDToGIDMap(Object mapobj)
    {
        // cid->gid stream is just an array of big-endian shorts
        if (mapobj instanceof PDFStream) {
            byte map[] = ((PDFStream)mapobj).decodeStream();
            int ncids = map.length/2;
            cidsToGids = new int[ncids];
            for(int i=0; i<ncids; ++i)
                cidsToGids[i] = ((map[2*i]&255)<<8) | (map[2*i+1]&255);
        }
        // A special cid->gid map is the "Identity" map
        else if ("Identity".equals(mapobj)) 
            identityCidToGid = true;
    }
    
    public int maximumOutputBufferSize(byte[] inbytes, int offset, int length)  { return length/2; }
    
    public int mapBytesToChars(byte[] inbytes, int offset, int length, char[] outchars)
    {
        // odd byte at end left out
        for(int i=0; i<length-1; i+=2) { // big endian
            outchars[i/2] = (char)((inbytes[i+offset]<<8) | (inbytes[i+offset+1]&255)); }
        return length/2;
    }
    
    // For fonts that have a CIDToGIDMap
    public boolean supportsCIDToGIDMapping()  { return (cidsToGids != null) || identityCidToGid; }
    
    public int mapCharsToGIDs(char cidbuffer[], int ncids, int gidbuffer[])
    {
        // CIDType0 fonts use cids, CIDType2 (truetype) use glyphids
        if(identityCidToGid || cidsToGids==null) {
            for(int i=0; i<ncids; ++i) gidbuffer[i] = cidbuffer[i] & 0xffff; }
        else for(int i=0; i<ncids; ++i) gidbuffer[i] = cidsToGids[cidbuffer[i] & 0xffff];
        return ncids;
    }
    
    public static boolean canHandleEncoding(String pdfEncodingName)
    {
        return pdfEncodingName.equals("/Identity-H") || pdfEncodingName.equals("/Identity-V");
    }
}

/**
 * A LatinGlyphMapper is a concrete subclass of GlyphMapper that can handle the standard pdf encodings that map single
 * byte character codes from a named encoding, with possible modifications by a Differences array. This class uses the
 * java.nio.charset package to handle macRoman and winAnsi base encodings.
 * 
 * It can also handle a null encoding, which it does by using the adobestandard mapping.  Note that this is the right
 * thing for the standard fonts, but not exactly correct all the time.  In PDF, a null encoding actually specifies
 * the font's internal (1 byte) encoding.
 */
public static class Latin extends GlyphMapper {

    // Latin char map
    char latinMap[];
    
    /** Create new Latin GlyphMapper. */
    public Latin(Map fontDict, PDFFile srcfile)
    {
        super(fontDict, srcfile);
        
        // allocate map
        latinMap=new char[256];
        
        // Get the base encoding name
        Object obj = srcfile.getXRefObj(fontDict.get("Encoding"));
        String encodingName=null;
        if (obj instanceof String)
            encodingName = (String)obj;
        else if (obj instanceof Map)
            encodingName = (String)((Map)obj).get("BaseEncoding");
        
        initializeMapForEncoding(encodingName);
        
        // Modify the map according to the differences array
        if (obj instanceof Map) {
            Object diffs = srcfile.getXRefObj(((Map)obj).get("Differences"));
            if ((diffs != null) && (diffs instanceof List))
                applyDifferences((List)diffs, fontDict);
        }
     }
    
    public boolean isMultiByte() { return false; }
    
    // Note: This code used to use java.nio.charset to create base encoding map, until I discovered java.nio.charset
    // doesn't necessarily return the same thing on every platform. In particular, MacRoman encoding was not available
    // on some windows machines. So, all the simple encodings are hardcoded in this file.
    void initializeMapForEncoding(String pdfName)
    {
        // Special case for adobeStandardEncoding 
        char baseMap[];
        if(pdfName==null) baseMap = adobeStandard;
        else if(pdfName.equals("/MacRomanEncoding")) baseMap = macRomanMap;
        else if(pdfName.equals("/WinAnsiEncoding")) baseMap = winAnsiMap;
        else throw new PDFException("Unknown encoding name : " + pdfName);
        
        System.arraycopy(baseMap,0,latinMap,0,256);
    }
    
    /* List looks like : [ num1 /glyphname1 /glyphname2 num2 /glyphname3 ...] */
    void applyDifferences(List diffs, Map f)
    {
        int first = 0;
        for(int i=0, n=diffs.size(); i<n; ++i) {
            Object item = diffs.get(i);
            if (item instanceof Number) 
                first = ((Number)item).intValue();
            else if (item instanceof String) {
                String glyphname = (String)item;
                // It really ought to be an error if the name doesn't start with a /
                if (glyphname.startsWith("/"))
                    glyphname = glyphname.substring(1);
                latinMap[first] = (char)GlyphMapper.adobeGlyphNameToUnicode(glyphname, f);
                ++first;
            }
        }
    }
    
    public int maximumOutputBufferSize(byte[] inbytes, int offset, int len)  { return len; } // 1 byte -> 1 glyph
    
    public int mapBytesToChars(byte[] inbytes, int offset, int len, char[] outchars)
    {
        for(int i=0; i<len; ++i) outchars[i] = latinMap[(inbytes[i+offset] & 0xff)];
        return len;
    }

    public static boolean canHandleEncoding(String pdfEncName)
    {
        return pdfEncName==null || pdfEncName.equals("/MacRomanEncoding") || pdfEncName.equals("/WinAnsiEncoding");
    }
}
    
/** The AdobeStandard encoding map */
private static final char adobeStandard[] = {
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 
    0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x2019, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f, 
    0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f, 
    0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f, 
    0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f, 
    0x2018, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f, 
    0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e, 0xffff, 
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 
    0xffff, 0x00a1, 0x00a2, 0x00a3, 0x2044, 0x00a5, 0x0192, 0x00a7, 0x00a4, 0x0027, 0x201c, 0x00ab, 0x2039, 0x203a, 0xfb01, 0xfb02, 
    0xffff, 0x2013, 0x2020, 0x2021, 0x00b7, 0xffff, 0x00b6, 0xffff, 0x201a, 0x201e, 0x201d, 0x00bb, 0x2026, 0x2030, 0xffff, 0x00bf, 
    0xffff, 0x0060, 0x00b4, 0x02c6, 0x02dc, 0x00af, 0x02d8, 0x02d9, 0x00a8, 0xffff, 0x02da, 0x00b8, 0xffff, 0x02dd, 0x02db, 0x02c7, 
    0x2014, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 
    0xffff, 0x00c6, 0xffff, 0x00aa, 0xffff, 0xffff, 0xffff, 0xffff, 0x0141, 0x00d8, 0x0152, 0x00ba, 0xffff, 0xffff, 0xffff, 0xffff, 
    0xffff, 0x00e6, 0xffff, 0xffff, 0xffff, 0x0131, 0xffff, 0xffff, 0x0142, 0x00f8, 0x0153, 0x00df, 0xffff, 0xffff, 0xffff, 0xffff
};

/** The /MacRoman encoding map */
private static final char macRomanMap[] = {
    0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007, 0x0008, 0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x000e, 0x000f,
    0x0010, 0x0011, 0x0012, 0x0013, 0x0014, 0x0015, 0x0016, 0x0017, 0x0018, 0x0019, 0x001a, 0x001b, 0x001c, 0x001d, 0x001e, 0x001f,
    0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
    0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
    0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f,
    0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f,
    0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f,
    0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e, 0x007f,
    0x00c4, 0x00c5, 0x00c7, 0x00c9, 0x00d1, 0x00d6, 0x00dc, 0x00e1, 0x00e0, 0x00e2, 0x00e4, 0x00e3, 0x00e5, 0x00e7, 0x00e9, 0x00e8,
    0x00ea, 0x00eb, 0x00ed, 0x00ec, 0x00ee, 0x00ef, 0x00f1, 0x00f3, 0x00f2, 0x00f4, 0x00f6, 0x00f5, 0x00fa, 0x00f9, 0x00fb, 0x00fc,
    0x2020, 0x00b0, 0x00a2, 0x00a3, 0x00a7, 0x2022, 0x00b6, 0x00df, 0x00ae, 0x00a9, 0x2122, 0x00b4, 0x00a8, 0x2260, 0x00c6, 0x00d8,
    0x221e, 0x00b1, 0x2264, 0x2265, 0x00a5, 0x00b5, 0x2202, 0x2211, 0x220f, 0x03c0, 0x222b, 0x00aa, 0x00ba, 0x03a9, 0x00e6, 0x00f8,
    0x00bf, 0x00a1, 0x00ac, 0x221a, 0x0192, 0x2248, 0x2206, 0x00ab, 0x00bb, 0x2026, 0x00a0, 0x00c0, 0x00c3, 0x00d5, 0x0152, 0x0153,
    0x2013, 0x2014, 0x201c, 0x201d, 0x2018, 0x2019, 0x00f7, 0x25ca, 0x00ff, 0x0178, 0x2044, 0x20ac, 0x2039, 0x203a, 0xfb01, 0xfb02,
    0x2021, 0x00b7, 0x201a, 0x201e, 0x2030, 0x00c2, 0x00ca, 0x00c1, 0x00cb, 0x00c8, 0x00cd, 0x00ce, 0x00cf, 0x00cc, 0x00d3, 0x00d4,
    0xf8ff, 0x00d2, 0x00da, 0x00db, 0x00d9, 0x0131, 0x02c6, 0x02dc, 0x00af, 0x02d8, 0x02d9, 0x02da, 0x00b8, 0x02dd, 0x02db, 0x02c7
};

/** The /WinAnsi encoding map (windows code page 1252) */
private static final char winAnsiMap[] = {
    0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007, 0x0008, 0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x000e, 0x000f,
    0x0010, 0x0011, 0x0012, 0x0013, 0x0014, 0x0015, 0x0016, 0x0017, 0x0018, 0x0019, 0x001a, 0x001b, 0x001c, 0x001d, 0x001e, 0x001f,
    0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
    0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
    0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f,
    0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f,
    0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f,
    0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e, 0x007f,
    0x20ac, 0xfffd, 0x201a, 0x0192, 0x201e, 0x2026, 0x2020, 0x2021, 0x02c6, 0x2030, 0x0160, 0x2039, 0x0152, 0xfffd, 0x017d, 0xfffd,
    0xfffd, 0x2018, 0x2019, 0x201c, 0x201d, 0x2022, 0x2013, 0x2014, 0x02dc, 0x2122, 0x0161, 0x203a, 0x0153, 0xfffd, 0x017e, 0x0178,
    0x00a0, 0x00a1, 0x00a2, 0x00a3, 0x00a4, 0x00a5, 0x00a6, 0x00a7, 0x00a8, 0x00a9, 0x00aa, 0x00ab, 0x00ac, 0x00ad, 0x00ae, 0x00af,
    0x00b0, 0x00b1, 0x00b2, 0x00b3, 0x00b4, 0x00b5, 0x00b6, 0x00b7, 0x00b8, 0x00b9, 0x00ba, 0x00bb, 0x00bc, 0x00bd, 0x00be, 0x00bf,
    0x00c0, 0x00c1, 0x00c2, 0x00c3, 0x00c4, 0x00c5, 0x00c6, 0x00c7, 0x00c8, 0x00c9, 0x00ca, 0x00cb, 0x00cc, 0x00cd, 0x00ce, 0x00cf,
    0x00d0, 0x00d1, 0x00d2, 0x00d3, 0x00d4, 0x00d5, 0x00d6, 0x00d7, 0x00d8, 0x00d9, 0x00da, 0x00db, 0x00dc, 0x00dd, 0x00de, 0x00df,
    0x00e0, 0x00e1, 0x00e2, 0x00e3, 0x00e4, 0x00e5, 0x00e6, 0x00e7, 0x00e8, 0x00e9, 0x00ea, 0x00eb, 0x00ec, 0x00ed, 0x00ee, 0x00ef,
    0x00f0, 0x00f1, 0x00f2, 0x00f3, 0x00f4, 0x00f5, 0x00f6, 0x00f7, 0x00f8, 0x00f9, 0x00fa, 0x00fb, 0x00fc, 0x00fd, 0x00fe, 0x00ff
};

}