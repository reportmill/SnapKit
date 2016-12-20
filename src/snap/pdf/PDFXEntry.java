/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;

/**
 * This class represents a PDF XRef entry.
 */
public class PDFXEntry {
    
    // The state
    public int      state = EntryUnknown;
    
    // The object number
    public int      objectNumber;
    
    // The file offset
    public int      fileOffset = -1;
    
    // The generation number
    public int      generation;
    
    // The object value
    public Object   value;
    
    // Constants for Entry types
    public static final int EntryUnknown = 0;
    public static final int EntryDeleted = 1;
    public static final int EntryRead = 2;
    public static final int EntryNotYetRead = 3;
    public static final int EntryCompressed = 4;
    
/**
 * Creates a new PDFXEntry.
 */
public PDFXEntry()  { }

/**
 * Creates a new PPDFXEntryBase.
 */
public PDFXEntry(int anIndex)  { objectNumber = anIndex; }

/**
 * Releases the reference to the object.
 */
public void reset()
{
    if(state==EntryRead) {
        state = EntryNotYetRead;
        value = null;
    }
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    PDFXEntry other = anObj instanceof PDFXEntry? (PDFXEntry)anObj : null; if(other==null) return false;
    return other.fileOffset==fileOffset && other.generation==generation;
}

/**
 * Returns a string representation.
 */
public String toString()  { return objectNumber + " 0 R"; }

}