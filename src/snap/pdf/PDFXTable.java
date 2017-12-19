/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.*;
import snap.pdf.read.PDFDictUtils;
import snap.util.ListUtils;

/**
 * A class to manage XRefs in PDFFile.
 */
public class PDFXTable {
    
    // The file
    public PDFFile        _pfile;
    
    // The XRefs
    public List <PDFXEntry>   _xrefs = new ArrayList();

    // List of entries (this should use XRefs)
    public List               _entries = new Vector(16);
    
    // The reader
    public PDFReader          _reader;
    
/**
 * Creates an XTable for file.
 */
public PDFXTable(PDFFile aPF)  { _pfile = aPF; }

/**
 * Creates an XTable for file.
 */
public PDFXTable(PDFFile aPF, PDFReader aReader)  { _pfile = aPF; _reader = aReader; }

/**
 * Returns the list of PDFEntry objects from XRef table.
 */
public List <PDFXEntry> getXRefs()  { return _xrefs; }

/**
 * Returns the number of PDFEntry objects from XRef table.
 */
public int getXRefCount()  { return _xrefs.size(); }

/**
 * Sets the list to contain at least this many refs.
 */
public void setXRefMax(int aCount)  { while(_xrefs.size()<aCount) _xrefs.add(new PDFXEntry(_xrefs.size())); }

/**
 * Returns the individual XRef at given index.
 */
public PDFXEntry getXRef(int anIndex)
{
    setXRefMax(anIndex+1);
    return _xrefs.get(anIndex);
}

/**
 * Given an object, check to see if its an indirect reference - if so, resolve the reference.
 */
public Object getXRefObj(Object anObj)
{
    if(anObj instanceof PDFXEntry) return getXRefObj((PDFXEntry)anObj);
    return anObj;
}

/**
 * Returns the object from the xref table, reading it if necessary.
 */
public Object getXRefObj(PDFXEntry anEntry)
{
    try { return getXRefObjImpl(anEntry); }
    catch(Exception e) { e.printStackTrace(); return null; }
}

/**
 * Returns the object from the xref table, reading it if necessary.
 */
protected Object getXRefObjImpl(PDFXEntry anEntry) throws Exception
{
     // Handle entry by entry state
    switch(anEntry.state) {
    
        // Handle read object
        case PDFXEntry.EntryRead: return anEntry.value;
            
        // Handle unread object: read, decrypt (maybe), update entry and return
        case PDFXEntry.EntryNotYetRead: {
            Object obj = _reader.readObjectDefAt(anEntry.fileOffset);
            if(_pfile._securityHandler!=null)
                obj = _pfile._securityHandler.decryptObject(obj, anEntry.objectNumber, anEntry.generation);
            anEntry.state = PDFXEntry.EntryRead;
            return anEntry.value = obj;
        }
            
        // Handle compressed object
        case PDFXEntry.EntryCompressed:
            int ostreamObjNum = anEntry.fileOffset;
            int objIndex = anEntry.generation;
            Object obj = readCompressedEntry(ostreamObjNum, objIndex);
            anEntry.state = PDFXEntry.EntryRead;
            return anEntry.value = obj;
        
        // Handle deleted object
        case PDFXEntry.EntryDeleted: return null;
        
        // Handle unknown object
        default: throw new PDFException("Reference to unknown object");
    }
}

/**
 * Read compressed entry.
 */
public Object readCompressedEntry(int ostmNum, int objIndex)
{
    // Get the xref object for the object stream
    PDFXEntry entry = getXRef(ostmNum);
    Object obj = getXRefObj(entry);
    
    // The first time through, the object will point to the stream representation of the object stream.
    // Get it and create a PDFObjectStream, then change the reference to point to the object stream
    PDFObjectStream oStm;
    if(obj instanceof PDFStream) {
        oStm = new PDFObjectStream((PDFStream)obj, _pfile);
        entry.value = oStm;
    }
    else oStm = (PDFObjectStream)obj;
    
    return oStm.get(objIndex);
}

/**
 * Call this to clear the state of all xref table entries.  This will allow
 * objects created while examining the file to get garbage collected.
 */
public void resetXRefs()  { for(PDFXEntry xref : _xrefs) xref.reset(); }

/**
 * Returns the number of entries in xref table.
 */
public int getEntryCount()  { return _entries.size(); }

/**
 * Returns the specific entry at the given index.
 */
public Object getEntry(int anIndex)  { return _entries.get(anIndex); }

/**
 * Adds an object and returns the string by which the object can be referenced inside the pdf file.
 */
public String addObject(Object anObj)  { return addObject(anObj, false); }

/**
 * Adds an object and returns the string by which the object can be referenced inside the pdf file.
 */
public String addObject(Object anObj, boolean definitelyIsNew)
{
    // Check to see if it's there already
    int index = definitelyIsNew? -1 : ListUtils.indexOfId(_entries, anObj);

    if(index == -1) {
        _entries.add(anObj);
        index = _entries.size();
    }
    else index++;
    
    // Return
    return getRefString(index);
}

/**
 * Returns the index of a given entry object.
 */
public int indexOfEntry(Object anObj)
{
    int index = ListUtils.indexOfId(_entries, anObj);
    if (index != -1) ++index;
    return index;
}

/**
 * Returns a reference string for the entry object at the given index.
 */
public String getRefString(int anIndex)
{
    if(anIndex<1 || anIndex>_entries.size())
        throw new RuntimeException("Entry #" + anIndex + " not in xref table");
    return anIndex + " 0 R";
}

/**
 * Returns a reference string for the given entry object.
 */
public String getRefString(Object anObj)
{
    // If there are a million objects, we may want to change entries from array to dictionary for performance
    int index = indexOfEntry(anObj);
    if(index==-1)
        throw new RuntimeException("object not present in xref table");
    return getRefString(index);
}

/**
 * A class to read pdf 1.5 compressed objects. Compressed objects are stored in a stream with this structure:
 *   << 
 *     /Type /ObjStm
 *     /N     <number of compressed objects>
 *     /First <byte offset of first object>
 *     /Extends <ptr to another objstm>
 *   >>
 *   stream
 *   <objnum1> <byteoffset1> <objnum2> <byteoffset2> ...
 *   object1 object2 object3
 *   endstream
 *   
 * PDFObjectStream reads the offset table at the beginning of stream and stores the objects & byte numbers at
 * initialization. To read a specific object, the PDFReader's stream data gets reset to object stream's data and
 * position is set to position in table.
 */
private static class PDFObjectStream {
    
    // Ivars
    int _firstOffset;
    Offsets _offTable[];
    PDFReader _reader;

    /** Creates new PDFObjectStream. */     
    public PDFObjectStream(PDFStream aStream, PDFFile srcFile)
    {
        Map sdict = aStream.getDict();
        int count = PDFDictUtils.getInt(sdict, srcFile, "N");
        _firstOffset = PDFDictUtils.getInt(sdict,srcFile,"First");
        
        // allocate space for the offsets
        _offTable = new Offsets[count];
        
        // save away the decompressed stream data
        byte sbytes[] = aStream.decodeStream();
        _reader = new PDFReader(srcFile, sbytes);
        readOffsets();
    }
    
    /** Read offsets. */
    public void readOffsets()
    {
        // Read 2 ints from the stream (object number, relative offset)
        for(int i=0, iMax=_offTable.length; i<iMax; i++) {
            int onum = _reader.readInt();
            int off = _reader.readInt();
            _offTable[i] = new Offsets(onum, off);
        }
    }
    
    /** Return object with num. */
    public Object get(int objnum)
    {
        //int relativeOffset = Offsets.findOffset(offTable, objnum);
        int relativeOffset = _offTable[objnum].offset; if(relativeOffset<0) return null;
        _reader.setCharIndex(relativeOffset + _firstOffset);
        return _reader.readObject();
    }
}

/**
 * A private class that represents a single element in the object stream.
 */
private static class Offsets {
    
    // Create new offsets
    public Offsets(int num, int off)  { offset = off; }  int offset; //int num;
    
    /** Binary search array for a particular object number and return its relative file offset, or -1 if not found. */
    //public static int findOffset(Offsets offArray[], int objNum)
    //{ Offsets key = new Offsets(objNum,0); int where = Arrays.binarySearch(offArray, key);
    //    return where>=0? offArray[where].offset : -1; }
}

}