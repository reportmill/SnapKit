/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.read;
import java.util.List;
import snap.pdf.PDFException;

/**
 * This class is used to map cids to widths. Widths are always indexed by character code.
 * For single byte fonts, we allocate a single 256 entry array for quick character code -> width lookup.
 * Instead of using a 64K entry array for 2 byte fonts, widths are represented by a sorted array of WidthTableEntries.
 *   
 * The pdf format for widths looks like:
 *    [cid [w1 w2 w3] cidfirst cidlast w4 cid2 [w5 w6 w7 w8]]
 * 
 * so the widths for a range of cids can be specified as an array of widths, or by a single width that applies to every
 * cid in the range.
 * 
 * All WidthTableEntries have a startcid & endcid range and can then hold either
 *  a) an array of endcid-startcid+1 floats
 *  b) a single float 
 *  
 * The widthtable can then do a binary search through the table to find the specific widthtable entry.
 * The worst possible case would be a table of 64k entries, each describing a single glyph, and a search for a glyph
 * through this would take log2(65536) = 16 hops through a huge table.  Any fontlist this large would actually
 * just revert to a single entry with 64k widths, so there would be no searching.
 * Average case would be a much smaller, especially since this is often used for font subsets.
 */
public class PDFGlyphWidthTable {
    
    // Ivars
    WidthTableEntry  widthtable[];
    float            defaultwidth;
  
/**
 * Main constructor. pdfwidths is the array from the CIDfont's /W entry, and defwidth is the /DW entry.
 * Both entries are optional, in which case all the widths are 1000.
 */
public PDFGlyphWidthTable(List pdfwidths, Object defwidth)
{
    initTable(pdfwidths);
    defaultwidth = defwidth instanceof Number? ((Number)defwidth).intValue()/1000f : 1; // default for default is 1000
}

/** Creates the table of WidthTableEntry subclasses */
void initTable(List pdfwidths)
{
    // no widths provided.  Will always use the default width
    if(pdfwidths==null) {
        widthtable = null; return; }
    
    int imax = pdfwidths.size();
    if(imax==0) {
        widthtable = null; return; }
    
    // Iterate array twice, first to validate array & figure out # entries to allocate, and then to fill in table
    //   /W  [1 [500 300 1000 990] 5 21 800 22 [100] ]
    int nentries = 0, previous_entry = -1;
    for(int i=0; i<imax; ++i) {
        Object obj = pdfwidths.get(i);
        if(!(obj instanceof Number))
            throw new PDFException("Illegal font widths array");
        int cidfirst = ((Number)obj).intValue();
        if(cidfirst<=previous_entry) // make sure they're sorted
            throw new PDFException("Font widths array is not sorted");
        
        obj = pdfwidths.get(++i);
        if (obj instanceof List) { int nwidths = ((List)obj).size();
            ++nentries; previous_entry = cidfirst + nwidths - 1;
        }
        else if (obj instanceof Number) {
            int cidlast = ((Number)obj).intValue();
            if (cidlast<cidfirst)
                throw new PDFException("Bad cid range in font widths array");
            ++i; //skip the width
            ++nentries;
            previous_entry = cidlast;
        }
    }
    
    // allocate the table & fill it in
    widthtable = new WidthTableEntry[nentries];
    int windex = 0;
    for(int i=0; i<imax; ++i) {
        int cidfirst = ((Number)pdfwidths.get(i)).intValue();
        Object obj = pdfwidths.get(++i);
        if(obj instanceof List) { List l = (List)obj;
            if (l.size()==1)  // if only one width in array, don't allocate an array
                widthtable[windex] = new WidthRangeEntry(cidfirst, cidfirst, ((Number)l.get(0)).intValue());
            else widthtable[windex] = new WidthArrayEntry(cidfirst, (List)obj);
        }
        else {
            int end = ((Number)obj).intValue(), wid = ((Number)pdfwidths.get(++i)).intValue();
            widthtable[windex] = new WidthRangeEntry(cidfirst, end, wid);
        }
        ++windex;
   }
}

/** Binary search the list and return the width */
public float getWidth(int cid)
{
    if(widthtable!=null) {
        int left = 0, right = widthtable.length;
        while(left<right) {
            int middle = (left+right)/2;
            WidthTableEntry entry = widthtable[middle];
            if(cid<entry.startcid) {
                if (right==middle) break;
                right = middle;
            }
            else if(cid>entry.endcid) {
                if (left==middle) break;
                left = middle;
            }
            else return entry.getWidth(cid);
        }
    }
    // not found, use the missing midth
    return defaultwidth;
}

/**
 * WidthTableEntry is the base class for all entries in the table.
 */
abstract class WidthTableEntry {
    public int startcid, endcid;
    
    abstract float getWidth(int cid);
}

/**
 * WidthArrayEntry represents a start cid and a list of widths: cid [w1 w2 w3]
 */
class WidthArrayEntry extends WidthTableEntry {
    public float widths[];
  
    public WidthArrayEntry(int cid, List intwidths)
    {
        int nwidths = intwidths.size();
        startcid = cid; endcid = cid+nwidths-1;
        widths = new float[nwidths];
        for(int i=0; i<nwidths; ++i) widths[i] = ((Number)intwidths.get(i)).intValue()/1000f;
    }
  
    public float getWidth(int cid) { return widths[cid-startcid]; }
}

/**
 * WidthRangeEntry represents a cid range and a single width: cid1 cid2 w   or   cid [w1]
 */
class WidthRangeEntry extends WidthTableEntry {
    public float width;
  
    public WidthRangeEntry(int start, int end, int iwidth)
    {
        startcid = start; endcid = end; width = iwidth/1000f;
    }
  
    public float getWidth(int cid) { return width; }
}

}