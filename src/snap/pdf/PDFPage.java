/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.*;
import snap.gfx.*;
import snap.pdf.read.*;

/**
 * This class is used by PDFFile to represent a single page.
 */
public class PDFPage {
    
    // The PDF file that this page is a part of
    PDFFile         _pfile;
    
    // The PDF page dictionary
    Map             _page;
    
    // The index of this page
    int             _index;

    // Resource dictionaries
    List            _resources;
    
    // cache so page is only rendered once
    Image           _image;
    
/**
 * Creates a new PDF page for the given PDF file and page index.
 */
public PDFPage(PDFFile aPdfFile, int anIndex)
{
    // Set PDF file
    _pfile = aPdfFile;
    
    // Set page index
    _index = anIndex;

    // Get page dict
    _page = (Map)getPageDict(_pfile._pagesDict, anIndex, new int[] {0});
    
    // Get the page's resources (which may be inherited) and initialize the resource stack
    Object r = getXRefObj(getPageResources());
    _resources = new ArrayList(1);
    if (r != null)
        _resources.add(r);
}

/**
 * Returns the PDFFile.
 */
public PDFFile getFile()  { return _pfile; }

/**
 * Returns the media box of this page.
 */
public Rect getMediaBox()
{
    // Get the bounds of the page and the cliprect to figure out the bbox and matrix of the form
    Object obj = getXRefObj(inheritedAttributeForKeyInPage("MediaBox", _page));
  
    // List elements are xmin, ymin, xmax, ymax
    List bbox = (List)obj;
    float bx = ((Number)bbox.get(0)).floatValue(), by = ((Number)bbox.get(1)).floatValue();
    float bw = ((Number)bbox.get(2)).floatValue() - bx, bh = ((Number)bbox.get(3)).floatValue() - by;    
    return new Rect(bx, by, bw, bh);
}

/**
 * Returns the crop box of this page.
 */
public Rect getCropBox()
{
    // Get the bounds of the page and the cliprect to figure out the bbox and matrix of the form
    Object obj = getXRefObj(inheritedAttributeForKeyInPage("CropBox", _page));
    if(obj==null)
        obj = getXRefObj(inheritedAttributeForKeyInPage("MediaBox", _page));
  
    // List elements are xmin, ymin, xmax, ymax
    List bbox = (List)obj;
    float bx = ((Number)bbox.get(0)).floatValue(), by = ((Number)bbox.get(1)).floatValue();
    float bw = ((Number)bbox.get(2)).floatValue() - bx, bh = ((Number)bbox.get(3)).floatValue() - by;    
    return new Rect(bx, by, bw, bh);
}

/**
 * Returns the resources stack for this page.
 */
public List getResources()  { return _resources; }

/**
 * Most pages will have only a single resource dictionary, which it may have inherited from the parent file.
 * However, if the page has Forms or Patterns in it, there may be additional, temporary resource dictionaries.
 */
public void pushResources(Map r)  { _resources.add(r); }

public void popResources()  { _resources.remove(_resources.size()-1); }

/** Returns the main resource directory for the page, which may be a reference. */
public Object getPageResources()  { return inheritedAttributeForKeyInPage("Resources", _page); }

/**
 * Returns the named object from the page's resources.  
 * Used in more specific methods below for Fonts, ExtGStates, ColorSpaces, etc.
 * This method is specific to entries in the resource dictionary that are themseleves dictionaries, but the only
 * Resource which currently isn't a dictionaries is the ProcSet.
 * If you need the ProcSet or something other than a dictionary, use getResources() and pull it out from there.
 */
public Object findResource(String resourceName, String name)
{
    List rezstack = getResources();
    int n = rezstack.size();
  
    while(n-->0) {
        Map resources = (Map)getXRefObj(rezstack.get(n));
        Object adict = getXRefObj(resources.get(resourceName));
        if(adict instanceof Map) {
            Object obj = ((Map)adict).get(name);
            if(obj!=null)
              return getXRefObj(obj);
        }
    }
    return null;
}

/**
 * Returns the XRef object for given object.
 */
public Object getXRefObj(Object anObj)  { return _pfile.getXRefObj(anObj); }

/**
 * Returns x.
 */
protected Object inheritedAttributeForKeyInPage(String aKey, Map aPage)
{
    // Get value for key from page dict
    Object value = aPage.get(aKey);

    // If key not declared in this object, check parent
    if(value==null) {
        Object parent = aPage.get("Parent");
        if(parent!=null)
            value = inheritedAttributeForKeyInPage(aKey, (Map)getXRefObj(parent));
    }
    
    return value;
}

/** Returns the page contents for this page. */
public Object getPageContents()
{
    // Get the contents of the page
    Object contents = getXRefObj(_page.get("Contents"));
  
    // contents is always an indirect reference, and could be a stream or an array of streams.
    // A Form XObject is a stream, not a reference to one, so we have to take the old contents
    // of the page in whatever form and create a stream for the form.
    if((contents instanceof List) && (((List)contents).size() == 1))
        contents = getXRefObj(((List)contents).get(0));
    
    return contents;
}

/** Returns the page contents as a PDF stream. */
public PDFStream getPageContentsStream()
{
    // Catch exceptions
    try {
    
    // Get the contents of the page
    Object contents = getPageContents();

    // If page contents is array of streams, concat into one (decode & decompress first)
    if(contents instanceof List) { List carray = (List)contents;
        byte subdatas[][] = new byte[carray.size()][];
        int sdatalen = 0;
        
        for(int i=0, iMax=carray.size(); i<iMax; i++) {
        
            // Get object and complain if not PDFStream
            Object obj = getXRefObj(carray.get(i));
            if(!(obj instanceof PDFStream))
                throw new PDFException("Element of page's contents array is not a stream");
            PDFStream onestream = (PDFStream)obj;
            
            // Resolve the filter parameters 
            Map onestreamdict = onestream.getDict();
            obj = getXRefObj(onestreamdict.get("Filter"));
            if(obj!=null) 
                onestreamdict.put("Filter", obj);
            obj = getXRefObj(onestreamdict.get("DecodeParms"));  // need to resolve anything inside?
            if(obj!=null) 
                onestreamdict.put("DecodeParms", obj);
                
            // decode the data
            subdatas[i] = onestream.decodeStream();
            sdatalen += subdatas[i].length;
        }
            
        // Append all the subdatas into a single array
        byte sdata[] = new byte[sdatalen];
        int destpos=0;
        for(int i=0, iMax=carray.size(); i<iMax; i++) {
            System.arraycopy(subdatas[i],0,sdata,destpos,subdatas[i].length);
            destpos += subdatas[i].length;
        }

        contents = new PDFStream(sdata, null);
    }
    
    // If page contents is single stream,
    else if(!(contents instanceof PDFStream))
        contents = null;

    // Return page contents stream
    return (PDFStream)contents;
        
    // Catch exceptions
    } catch(Exception e) { System.err.println("Error decoding page contents : "+e); return null; }
}

/**
 * Set everything to the default implementations and return an Image for this page.
 */
public Image getImage()
{
    // If already set, just return
    if(_image!=null) return _image;
    
    // Get page bounds
    Rect media = getMediaBox(), crop = getCropBox(), bounds = media.getIntersectRect(crop);
    int width = (int)Math.round(bounds.width), height = (int)Math.round(bounds.height);
    
    // Create PDF painter that renders into an image
    Image img = Image.get(width,height,false);
    Painter ipntr = img.getPainter();
    ipntr.setColor(Color.WHITE); ipntr.fillRect(0,0,width,height);
    
    // Create PDF painter that renders into an image
    PDFPagePainter ppntr = new PDFPagePainter(this);
    ppntr.paint(ipntr, null, null, null);
    
    // Return image
    return _image = img;
}

/**
 * Draw the page to g,  scaled to fit the rectangle r.
 */
public void paint(Painter aPntr, Rect aRect)
{
    PDFPagePainter pntr = new PDFPagePainter(this);
    pntr.paint(aPntr, null, aRect, null);
}
    
/**
 * Returns the page dict for this page.
 */
public Object getPageDict(Map pages, int pnum, int start[])
{
    Object type = pages.get("Type");
    
    // hit a leaf node
    if(type.equals("/Page")) {
        if(start[0] == pnum)
            return pages;
        ++start[0];
        return null;
    }
    
    if(!type.equals("/Pages"))
        throw new PDFException("PDFPage.getPageDict: Type not Pages"); // Was { _pfile._valid = false; return null;
    
    int count = (Integer)getXRefObj(pages.get("Count"));
    if(pnum>=start[0] && (start[0]+count>pnum)) {
        List kids = (List)getXRefObj(pages.get("Kids"));
        count = kids.size();
        for(int i=0; i<count; i++) { Object obj = getXRefObj(kids.get(i));
            obj = getPageDict((Map)obj, pnum, start);
            if(obj!=null)
                return obj;
        }
        throw new PDFException("PDFPage.getPageDict: Page dict not found"); // Was _pfile._valid = false;
    }
    
    // Increment start and return null
    start[0] += count;
    return null;
}

}