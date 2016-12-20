/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import java.util.*;
import snap.gfx.*;
import snap.pdf.*;

/**
 * A class to write embedded PDF.
 */
public class PDFWriterPDF {

/**
 * The idea here is to extract a page from a pdf file and import it into another by turning it into a pdf form xobject.
 * This allows us to independently control its ctm and use it over & over. We have to extract any resources that it
 * uses and add them to the new pdf file.
 */
public static void writePDF(PDFWriter aWriter, PDFFile readerFile, int aPageIndex)
{
    // Trap exceptions for PDF parsing (reader.getPDFFile())
    try {

    // Get reader PDF file and page
    PDFPage readerPage = readerFile.getPage(aPageIndex);
    String imageName = System.identityHashCode(readerPage) + ""; //anImageData.getName()
    
    // Bump the pdf version number, if necessary
    aWriter.getPDFFile().setVersion(readerFile.getVersion());
    
    // Get the crop box of page for use in BBox and Matrix strings
    Rect crop = readerPage.getCropBox();
    
    // Now create the form dictionary
    Map dict = new Hashtable(8);
    dict.put("Type", "/XObject");
    dict.put("Subtype", "/Form");
    dict.put("FormType", "1");
    dict.put("BBox", "[" + crop.getX() + " " + crop.getY() + " " + crop.getMaxX() + " " + crop.getMaxY() + "]");
    dict.put("Matrix", "[1 0 0 1 " + (-crop.getX()) + " " + (-crop.getY()) + "]");
    dict.put("Name", "/" + imageName);
    
    // Add page resources from reader page to writer and form dictionary
    Object readerPageResources = readerPage.getPageResources();
    Object writerPageResources = addObjectToWriter(aWriter, readerFile, readerPageResources);
    dict.put("Resources", writerPageResources);
  
    // Get reader page contents stream and add its dictionary to writer
    PDFStream readerPageContentsStream = readerPage.getPageContentsStream();
    Map readerPageContentsDict = readerPageContentsStream.getDict();
    Map writerPageContentsDict = (Map)addObjectToWriter(aWriter, readerFile, readerPageContentsDict);
    dict.putAll(writerPageContentsDict);
    
    // Now create the form stream object and dump it to writer
    PDFStream formStream = new PDFStream(readerPageContentsStream.getBytes(), dict);
    aWriter.writeStream(formStream);
    
    // Everything is wrapped in parse exception handler, complain if thrown
    } catch(Exception e) { System.err.println("Error parsing pdf file : " + e); e.printStackTrace(); }    
}

/**
 * This function recurses through a tree of PDF objects and make sure
 * that any objects that are referenced within the tree get added to the xref table.
 * Starting at the root node, we traverse every object in the tree.  If any of the nodes
 * are indirect references (ie. the pdf "n 0 R") we add the object that is referenced to the xref table.
 * Returns a fully resolved object suitable for inclusion in the file.
 */
public static Object addObjectToWriter(PDFWriter aWriter, PDFFile pFile, Object anObj)
{
    // Check for local version, and if found, return
    Object local = aWriter._readerWriterXRefMap.get(anObj);
    if(local!=null)
        return local;
        
    // Handle reader entry: add entry object and return object xref entry
    if(anObj instanceof PDFXEntry) {
        Object resolved = pFile.getXRefObj(anObj);
        Object resolveLocal = addObjectToWriter(aWriter, pFile, resolved);
        local = aWriter.getXRefTable().addObject(resolveLocal);
    }
    
    // Handle reader stream: add stream dictionary and return new stream with new dictionary
    else if(anObj instanceof PDFStream) {
        PDFStream readerStream = (PDFStream)anObj;
        Map writerDictionary = (Map)addObjectToWriter(aWriter, pFile, readerStream.getDict());
        local = new PDFStream(readerStream.getBytes(), writerDictionary);
    }

    // Handle map: add map values and return new map with new values
    else if(anObj instanceof Map) { Map map = (Map)anObj, map2 = new HashMap(); local = map2;
        for(Map.Entry entry : (Set <Map.Entry>)map.entrySet()) {
            Object value = addObjectToWriter(aWriter, pFile, entry.getValue());
            map2.put(entry.getKey(), value);
        }
    }
    
    // Handle list: add list values and return new list with new values
    else if(anObj instanceof List) { List list = (List)anObj, list2 = new ArrayList(list.size()); local = list2;
        for(int i=0, iMax=list.size(); i<iMax; i++) {
            Object value = addObjectToWriter(aWriter, pFile, list.get(i));
            list2.add(value);
        }
    }
    
    // Handle everything else
    else local = anObj;

    // Add local object if it has changed
    if(local!=anObj)
        aWriter._readerWriterXRefMap.put(anObj, local);

    // Return object reference
    return local;
}

}