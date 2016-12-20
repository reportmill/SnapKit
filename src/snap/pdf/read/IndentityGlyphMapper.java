/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.Map;
import snap.pdf.PDFFile;
import snap.pdf.PDFStream;

/**
 * A concrete subclass of GlyphMapper for the Identity-H & Identity-V maps input bytes -> cids is done with no mapping,
 * just interpreting the bytes as big-endian shorts. Also supports mapping to GIDs via an embedded CIDToGIDMap
 *
 * Note that for fonts with CIDToGIDMaps, it might be tempting to provide a way to skip the CID step and just go from
 * input bytes to GIDs. However, all the font metric info is based on CIDs, so we're always going to have to convert to
 * CIDs no matter what.
 */
public class IndentityGlyphMapper extends GlyphMapper {
    int cidsToGids[]=null;
    boolean identityCidToGid=false;
    
public static boolean canHandleEncoding(String pdfEncodingName)
{
    return pdfEncodingName.equals("/Identity-H") || pdfEncodingName.equals("/Identity-V");
}

public IndentityGlyphMapper(Map fontDict, PDFFile srcfile) {
    super(fontDict, srcfile);
}

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

public boolean isMultiByte() { return true; }

public int maximumOutputBufferSize(byte[] inbytes, int offset, int length)
{
    return length/2;
}

public int mapBytesToChars(byte[] inbytes, int offset, int length, char[] outchars)
{
    // odd byte at end left out
    for(int i=0; i<length-1; i+=2) {
        // big endian
        outchars[i/2] = (char)((inbytes[i+offset]<<8) | (inbytes[i+offset+1]&255));
      }
    return length/2;
}

// For fonts that have a CIDToGIDMap
public boolean supportsCIDToGIDMapping()
{
    return (cidsToGids != null) || identityCidToGid;
}

public int mapCharsToGIDs(char cidbuffer[], int ncids, int gidbuffer[])
{
    int i;
    
    // CIDType0 fonts use cids, CIDType2 (truetype) use glyphids
    if (identityCidToGid || (cidsToGids==null)) {
        for(i=0; i<ncids; ++i)
            gidbuffer[i] = cidbuffer[i] & 0xffff;
    }
    else {
        for(i=0; i<ncids; ++i) 
            gidbuffer[i]=cidsToGids[cidbuffer[i] & 0xffff];
    }
    return ncids;
}

}
