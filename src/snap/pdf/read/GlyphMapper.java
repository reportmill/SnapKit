/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.*;
import java.io.*;
import snap.pdf.PDFException;
import snap.pdf.PDFFile;

/**
 * A GlyphMapper object knows how to take an array of bytes and convert them into
 * the propper format for an awt Font (or whatever font subclass was returned by the
 * FontFactory) to turn into glyphs.
 * 
 * The pdf engine will get the glyphs from the font by calling the createGlyphVector
 * routines, so this object needs to return its data such that createGlyphVector
 * will do the right thing.
 * 
 * By default, java.awt.Font reads the unicode cmap from the font when createGlyphVector
 * is called.  So, if the font has a valid unicode cmap table, this object can
 * simply map the input string into unicode.  
 * 
 * The font in the pdf file includes an Encoding dictionary which specifies not
 * only some standard font encoding names (WinAnsiEncoding, MacRomanEncoding, etc)
 * but also a mapping for specific glyphs.  These dictionaries specify glyphs by
 * name, so the mapper needs a way to take the glyph name and turn it into a value
 * that ultimately will get mapped to the right thing via createGlyphVector.
 * 
 * Here's a typical Encoding dictionary:
 *   << /Type /Encoding
 *      /BaseEncoding /MacRomanEncoding
 *      /Differences [ 219 /Euro ] >> 
 * In this case, the mapper needs to know not only how to convert bytes in 
 * MacRomanEncoding into unicode, but also it needs to be able to find the 
 * glyph named "Euro" (an adobe glyph name) in the unicode set (EURO SIGN : U+20AC)
 * and use that whenever the input string contains the value 219.
 * This class has an adobeGlyphNameToUnicode() method to help with that problem.
 * 
 * Some fonts have CMaps instead of encoding dictionaries.  These are structures
 * for specifying the encodings of large numbers of characters.  Their source
 * data may be multi-byte, unlike fonts encoded with an encoding dictionary,
 * whose strings are specified as single bytes.
 * 
 * CMaps can come into play for "CID" fonts.  In a CID font, the raw bytes passed 
 * to a text showing operation are first passed through a CMap (which a subclass 
 * of this object can implement) to return a CID.  There are built-in CMaps (the
 * most important being /Identity-H & /Identity-V) along with potentially embedded
 * CMaps.
 * In a CID font, a particular character id may be specified by one or more bytes
 * from the input data.  Different CIDs in the same CMap can be selected by different
 * numbers of bytes, allowing for encodings like shift-JIS, where roman chars
 * are identified by one byte but japanese chars are identified by two bytes.
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
 * A GlyphMapper object, therefore, has 2 responsibilities - one required and one
 * optional. First, it must know how to convert an input stream of bytes into an
 * output stream of CIDs.  Second, if it knows how, it can convert an input stream
 * of CIDs into GIDs.
 */
public abstract class GlyphMapper {

/** Creates a new mapper for the font.  For Latin fonts, the encodingDict
 * is the dict pulled out of the pdf file (with /BaseFont & /Differences entries)
 * more complex encoding dicts could contain cmaps or other info.
 */
public GlyphMapper(Map fontDict, PDFFile srcfile) {
    super();
}

/** Returns true if this subclass knows how to handle the given encoding */
public static boolean canHandleEncoding(String pdfEncodingName) { return false; }

/** Returns true if the encoding consumes more than single byte for a character.
 * Note that encodings that sometimes maps single bytes, but other times
 * maps more than one, should always return true for this routine.
 */
public abstract boolean isMultiByte();

/** Quickly returns the maximum number of chars that could result from the
 * conversion of inbytes bytes according to the encoding.
 * A single byte encoding, like the latin ones, will take a single byte
 * and convert it into a single char, so this routine would just return len.
 * An encoding that takes 2 bytes and converts them to a char would return len/2, etc.
 */
public abstract int maximumOutputBufferSize(byte inbytes[], int offset, int len);

/** The workhorse.  Take an input buffer and convert everything. 
 * The output buffer should be allocated using the size from the
 * above routine.  The actual number of chars in the final output
 * is returned.
 */
public abstract int mapBytesToChars(byte inbytes[], int offset, int len, char outchars[]);



/** Returns true if encoding knows how to convert to glyph indices.  */
public boolean supportsCIDToGIDMapping() { return false; }

/** Sets the cid to glyph index map info.  This object will will be interpreted
 * as whatever format is appropriate for the particular glyphmapper.
 */
public void setCIDToGIDMap(Object mapobj) {}

/** Does the cid->gid mapping, if it knows how.
 * Returns the total number of glyphs created.
 **/
public int mapCharsToGIDs(char cidbuffer[], int numcids, int gidbuffer[]) {return 0;}



/** Search routine to find a GlyphMapper subclass */
public static GlyphMapper createGlyphMapper(Map fontDict, PDFFile srcfile)
{  
    Object encode = srcfile.getXRefObj(fontDict.get("Encoding"));
    String ename=null;
    
    if (encode instanceof String)
        ename = (String)encode;
    else if (encode instanceof Map)
        ename = (String)((Map)encode).get("BaseEncoding");
    
    if (LatinGlyphMapper.canHandleEncoding(ename))
        return new LatinGlyphMapper(fontDict, srcfile);
    else if (IndentityGlyphMapper.canHandleEncoding(ename))
        return new IndentityGlyphMapper(fontDict, srcfile);
    // TODO: take this out eventually and replace with a default (probably winansi)
    throw new PDFException("unimplemented encoding name:"+ename);
}

//---- utility routines used by concrete subclasses -----

/** Maps a single glyph name string to a unicode value.
 * This is an implementation of the algorithm described in
 * http://partners.adobe.com/public/developer/opentype/index_glyph.html
 */
public static int adobeGlyphNameToUnicode(String name, Map fontDict)
{
    int index, uval=-1;
    Object val=null;
    String components[];

    // Step 1: drop the first period and anything after, if present
    index=name.indexOf('.');
    if (index >= 0) {
        name=name.substring(0, index);
    }

    // Step 2: split along underscores
    components=name.split("_");

    // Step 3: map
    // General algorithm maps multiple components to a unicode string.
    // We only map the first one, for now. This implies a one-to-one
    // mapping of all character codes to unicode characters.
    if (components.length < 1)
        return -1;
    name=components[0];

    if ((fontDict!=null) && ("/ZapfDingbats".equals(fontDict.get("BaseFont"))))
        val=adobeZapfDingbatsGlyphList().get(name);

    if (val == null)
        val=adobeGlyphList().get(name);

    if (val != null) {
        if (val instanceof List) 
            val=((List)val).get(0); // "You take what you need, & you leave the rest..."
        uval = ((Number)val).intValue();
    }
    else {
        try {
            int len=name.length();
            if (name.startsWith("uni") && (len >= 7))
                // again, could be multiple values, like uni20a0403c but we're
                // punting
                uval=Integer.parseInt(name.substring(3, 7), 16);
            else if (name.startsWith("u") && (len >= 5) && (len <= 7))
                uval=Integer.parseInt(name.substring(1), 16);
        } catch (NumberFormatException nfe) {
            // chars after u or uni didn't parse as hex
            // Fall through to unknown glyph case.
        }
    }
    return uval;
}

static private Map _zapfDingbatsMap=null;
static private Map _glyphMap=null;

/** Returns the unicode map for glyph names in the ZapfDingbats set, lazily reading them in. */
static Map adobeZapfDingbatsGlyphList()
{
    if (_zapfDingbatsMap==null) {
       _zapfDingbatsMap = readGlyphList("ZapfDingbatsGlyphList.txt");
    }
    return _zapfDingbatsMap;
}

/** Returns the unicode map for glyph names in the standard set. */
static Map adobeGlyphList()
{
    if (_glyphMap==null) {
        _glyphMap = readGlyphList("AdobeGlyphList.txt");
    }
    return _glyphMap;
}

/** parse the adobe glyph files and turn them into a map of glyphnames -> unicode values */
static Map readGlyphList(String name)
{
    Hashtable table=null;
    InputStream s=GlyphMapper.class.getResourceAsStream(name);

    try {
        if (s == null)
            throw new IOException("Internal error: couldn't locate resouce "
                    + name);

        Reader r=new BufferedReader(new InputStreamReader(s));
        StreamTokenizer parser=new StreamTokenizer(r);
        String glyphname;
        char oneChar[] = new char[1];
        Integer glyphcode;
        ArrayList codes;
        int token, i;

        // Have to reset the syntax, because it parses numbers by default, but it does so as decimal.
        // Since all number in the file are hex, we need to turn that off
        parser.resetSyntax();
        parser.wordChars('0', '9');
        parser.wordChars('a', 'z');
        parser.wordChars('A', 'Z');
        parser.wordChars(128 + 32, 255);
        parser.whitespaceChars(0, ' ');
        // lines starting with a # are comments.
        parser.commentChar('#');
        // let us know about lineends, so we can get glyphs that map to more
        // than one code
        parser.eolIsSignificant(true);
        // The name of the glyph and its code are separated by semicolons
        parser.whitespaceChars(';', ';');
       
        table=new Hashtable(256);
        while ((token=parser.nextToken()) != StreamTokenizer.TT_EOF) {
            // Turn a single char glyph name into a string (ascii glyph names only)
            if ((token>65)&&(token<128)) {
                oneChar[0]=(char)token;
                glyphname = new String(oneChar);
            }
            else if (token == StreamTokenizer.TT_WORD) {
                glyphname=parser.sval;
            }
            else glyphname=null;
                
            // Got a glyph name. Parse the rest of the line
            if (glyphname != null) {
                glyphcode=null;
                codes=null;
                do {
                    token=parser.nextToken();
                    if (token == StreamTokenizer.TT_WORD) {
                        // codes are in hex
                        i=Integer.parseInt(parser.sval, 16);
                        if (glyphcode == null)
                            glyphcode=new Integer(i);
                        else {
                            // multiple codes get turned into an array
                            if (codes == null) {
                                codes=new ArrayList(2);
                                codes.add(glyphcode);
                            }
                            codes.add(new Integer(i));
                        }
                    }
                } while ((token != StreamTokenizer.TT_EOL)
                        && (token != StreamTokenizer.TT_EOF));

                // Done with the line, add the created Integer or List to the
                // hashtable
                if (glyphcode==null) {
                    System.err.println("no codes?  set a breakpoint here");
                }
                else table.put(glyphname, (codes != null) ? (Object) codes
                        : (Object) glyphcode);
            }
        }
    } catch (Exception e) {
        // bad news : should only get here if a file is missing from the jar
        // or if there was a parse error with the file.
        // It's unrecoverable.
        e.printStackTrace();
        System.exit(-1);
    }

    return table;
}

/* For testing purposes */
public static void main(String args[])
{
    String gname = "zeroinferior";
    int lookup;
    
    if (args.length > 0)
        gname = args[0];
    
    lookup = adobeGlyphNameToUnicode(gname, null);
    System.out.println(gname + " = " + Integer.toHexString(lookup));
    
}

}
