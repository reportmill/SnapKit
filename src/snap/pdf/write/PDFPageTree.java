/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import java.util.*;
import snap.pdf.PDFFile;
import snap.pdf.PDFWriter;
import snap.pdf.PDFXTable;

/**
 * This class represents a pages tree in a PDF file.
 */
public class PDFPageTree {
    
    // Pages
    List <PDFPageWriter>  _pages = new Vector(2);
    
    // Dict
    Map                   _dict = new Hashtable();

/**
 * Creates a new pdf pages tree.
 */
public PDFPageTree(PDFFile file)  { _dict.put("Type", "/Pages"); }

/**
 * Returns the number of pages.
 */
public int getPageCount()  { return _pages.size(); }

/**
 * Returns the PDF page at the given index.
 */
public PDFPageWriter getPage(int anIndex)  { return _pages.get(anIndex); }

/**
 * Adds a given page to this pages tree.
 */
public void addPage(PDFPageWriter aPage)  { _pages.add(aPage); }

/**
 * Returns the index of the given page in this pages tree.
 */
public int indexOf(PDFPageWriter aPage)  { return _pages.indexOf(aPage); }

/**
 * Tells all pages in this pages tree to resolve page references.
 */
public void resolvePageReferences()
{
    int count = _pages.size();
    while(count-- > 0)
        getPage(count).resolvePageReferences(this);
}

/**
 * Writes this pages tree to the given PDF file.
 */
public void writePDF(PDFWriter aWriter)
{
    List refs = new ArrayList(getPageCount());
    PDFXTable xref = aWriter.getXRefTable();
    for(int i=0, iMax=getPageCount(); i<iMax; i++)
        refs.add(xref.getRefString(getPage(i)));
    
    // 
    _dict.put("Kids", refs);
    _dict.put("Count", getPageCount());
    
    // 
    aWriter.writeXRefEntry(_dict);
}

}