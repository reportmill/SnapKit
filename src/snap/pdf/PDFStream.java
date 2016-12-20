/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.*;

/**
 * This class represents a PDF stream object.
 */
public class PDFStream implements Cloneable {
    
    // Stream bytes
    public byte     _bytes[];
    
    // Stream dictionary
    public Map      _dict;

/**
 * Creates a new PDFStream from the byte array and map.
 */
public PDFStream(byte bytes[], Map aMap)
{
    _bytes = bytes; // no copy
    _dict = aMap==null? new Hashtable() : new Hashtable(aMap);
}

/**
 * Creates a new PDFStream by copying the byte array and map.
 */
public PDFStream(byte bytes[], int offset, int len, Map aMap)
{
    _bytes = new byte[len]; // Make local copy of data
    System.arraycopy(bytes, offset, _bytes, 0, len);
    _dict = aMap==null? new Hashtable() : new Hashtable(aMap); // and hashtable
}

/**
 * Returns the stream bytes.
 */
public byte[] getBytes()  { return _bytes; }

/**
 * Returns the stream dictionary.
 */
public Map getDict()  { return _dict; }

/**
 * The dict for a stream may specify multiple filters. The "Filter" property is either a single filter name or an
 * array of filter names. If it's an array, the PDF reader will apply the filters in the order they appear.
 * Call this method with the name of the filter after applying the encoding to the data.
 */
public void addFilter(String aName)
{
    Object filters = _dict.get("Filter");
    
    // If no filters yet, just add aName
    if(filters==null)
        _dict.put("Filter", aName);
    
    // If one filter, convert to list
    else if(filters instanceof String)
        _dict.put("Filter", Arrays.asList(aName, filters));
    
    // If list of filters, add new filter
    else ((List)filters).add(0, aName);
}

/**
 * Tests whether a specifc filter will be needed to decode the stream.
 */
public boolean usesFilter(String fName)  { return (indexOfFilter(fName)>=0); }

/**
 * Returns the index of a particular filter in the filter chain, or -1 if not found.
 */
public int indexOfFilter(String fName)
{
    Object filters = _dict.get("Filter"); if(filters==null) return -1;
    if (filters instanceof String)
        return filters.equals(fName)? 0 : -1;
    List flist = (List)filters;
    for(int i=0, n=flist.size(); i<n; ++i) 
        if (flist.get(i).equals(fName))
            return i;
    return -1;
}

/**
 * Returns the total number of filters with which this stream is currently encoded.
 */
public int numFilters()
{
    Object filters = _dict.get("Filter");
    if(filters==null) return 0;
    if(filters instanceof String) return 1;
    return ((List)filters).size();
}

/**
 * Returns the filter parameters for a particular filter
 */
public Map getFilterParameters(String name)
{
    Object parameters = _dict.get("DecodeParms"); // Get the filter parameters
    int nfilters = numFilters();
    int index = indexOfFilter(name);
    
    // If there's only a single filter, the parameters should be a dictionary
    if((nfilters==1) && (index==0) && (parameters instanceof Map))
        return (Map)parameters;
    
    // otherwise it should be a list, with one dict per filter
    if((parameters instanceof List) && (index>=0))
        return (Map)((List)parameters).get(index);

    // otherwise return null
    return null;
}
    
/**
 * Returns the result of running the data through all the filters.
 */
public byte[] decodeStream()  { return decodeStream(numFilters()); }

/**
 * Returns the result of running the data through the first n filters.
 */
public byte[] decodeStream(int nfilters)
{
    // Get filters for the stream (just return _data if none)
    Object filter = _dict.get("Filter");
    if(filter==null || nfilters==0)
        return _bytes;

    // Get the filter parameters
    Object parameters = _dict.get("DecodeParms"); // parms?  what's a parm?
    
    // If list, run through all filters
    if(filter instanceof List) { List filters = (List)filter;
        List paramList = null;
        int len = _bytes.length, iMax = filters.size();
        
        if(iMax>nfilters)
            iMax = nfilters;
        if(parameters instanceof List)
            paramList = (List)parameters;
        
        byte decoded[] = _bytes;
        for(int i=0; i<iMax; i++) { String fname = (String)filters.get(i);
            decoded = PDFUtils.getBytesDecoded(decoded, 0, len, fname, paramList!=null? (Map)paramList.get(i) : null);
            len = decoded.length;
        }
        return decoded;
    }
    
    // If not list, just decode bytes and return
    return PDFUtils.getBytesDecoded(_bytes, 0, _bytes.length, (String)filter, (Map)parameters);
}

/**
 * Standard clone implementation.
 */
public PDFStream clone()
{
    PDFStream copy = null; try { copy = (PDFStream)super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
    copy._dict = new Hashtable(_dict);
    return copy;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    StringBuffer sb = new StringBuffer();
    for(Map.Entry entry : (Set<Map.Entry>)getDict().entrySet()) {
        sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n'); }
    byte bytes[] = decodeStream();
    try { if(bytes!=null) sb.append(new String(bytes)); }
    catch(Exception e) { System.err.println(e); }
    return sb.toString();
}

}