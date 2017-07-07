/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.List;
import java.awt.color.ColorSpace;
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
protected Object getXRefObj(Object anObj)  { return _pfile.getXRefObj(anObj); }

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

/**
 * Accessors for the resource dictionaries.
 */
public Map getExtendedGStateNamed(String name) { return (Map)findResource("ExtGState", name); }

/**
 * Returns the pdf Font dictionary for a given name (like "/f1").  You
 * can use the FontFactory to get interesting objects from the dictionary.
 */  
public Map getFontDictForAlias(String alias) { return (Map)findResource("Font", alias); }

/**
 * Like above, but for XObjects. XObjects can be Forms or Images.
 * If the dictionary represents an Image, this routine calls the ImageFactory to create a java.awt.Image.
 * If it's a Form XObject, the object returned will be a PDFForm.
*/ 
public Object getXObject(String pdfName)
{
    PDFStream xobjStream = (PDFStream)findResource("XObject",pdfName);
    
    if (xobjStream != null) {
        Map xobjDict = xobjStream.getDict();
        
        // Check to see if we went through this already
        Object cached = xobjDict.get("_rbcached_xobject_");
        if(cached != null)
            return cached;
        
        String type = (String)xobjDict.get("Subtype");
        if (type==null)
            throw new PDFException("Unknown xobject type");
        
        // Image XObject - pass it to the ImageFactory
        if (type.equals("/Image")) {
            // First check for a colorspace entry for the image, and create an awt colorspace.
            Object space = getXRefObj(xobjDict.get("ColorSpace"));
            ColorSpace imageCSpace = space==null ? null : PDFColorSpace.getColorspace(space, _pfile, this);
            cached = PDFImage.getImage(xobjStream, imageCSpace, _pfile);
        }
        
        // A PDFForm just saves the stream away for later parsing
        else if (type.equals("/Form"))
            cached = new PDFForm(xobjStream);
        
        if (cached != null) {
            xobjDict.put("_rbcached_xobject_", cached);
            return cached;
        }
    }
    
    // Complain and return null
    System.err.println("Unable to get xobject named \""+pdfName+"\"");
    return null;
}

/** Creates a new pattern object for the resource name */
public PDFPattern getPattern(String pdfName)
{
    Object pat = findResource("Pattern", pdfName);
    PDFPattern patobj = PDFPattern.getInstance(pat, _pfile);
    
    // Resolve the colorspace.
    if (patobj instanceof PDFPatterns.Shading) {
        Map shmap = (Map)getXRefObj(((Map)pat).get("Shading"));
        Object csobj = getXRefObj(shmap.get("ColorSpace"));
        if (csobj != null) 
          ((PDFPatterns.Shading)patobj).setColorSpace(PDFColorSpace.getColorspace(csobj, _pfile, this));
    }
    
    return patobj;
}

/** Creates a new shadingPattern for the resource name.  Used by the shading operator */
public PDFPatterns.Shading getShading(String pdfName)
{
    Map pat = (Map)findResource("Shading", pdfName);
    PDFPatterns.Shading patobj = PDFPatterns.Shading.getInstance(pat, _pfile);
    
    // Resolve the colorspace.
    Object csobj = getXRefObj(pat.get("ColorSpace"));
    if (csobj != null) 
          patobj.setColorSpace(PDFColorSpace.getColorspace(csobj, _pfile, this));
    return patobj;
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
public Image getImage2()
{
    // If already set, just return
    if(_image!=null) return _image;
    
    // Create PDF painter that renders into an image
    PDFMarkupHandler pntr = PDFMarkupHandler.get();
    parse(pntr);
    return _image = pntr.getImage();
}

/**
 * Set everything to the default implementations and return an Image for this page.
 */
public Image getImage()
{
    // If already set, just return
    if(_image!=null) return _image;
    
    // Get page bounds
    Rect media = getMediaBox();
    Rect crop = getCropBox();
    Rect bounds = media.getIntersectRect(crop);
    int width = (int)Math.round(bounds.width), height = (int)Math.round(bounds.height);
    
    // Create PDF painter that renders into an image
    Image img = Image.get(width,height,false);
    Painter ipntr = img.getPainter();
    ipntr.setColor(Color.WHITE); ipntr.fillRect(0,0,width,height);
    ipntr.setColor(Color.BLACK); ipntr.drawRect(.5,.5,width-1,height-1);
    
    // Get transform from PDF coords to image (Snap) coords and appyt to image painter
    Rect r1 = new Rect(0,0,width,height);
    Rect r2 = new Rect(0,height,width,-height);
    Transform xfm = Transform.get(r1, r2);
    ipntr.transform(xfm);
    
    // Create PagePainter, paint page to image painter and return image
    PagePainter ppntr = new PagePainter();
    ppntr.paint(_pfile.getPage(_index), ipntr);
    return _image = img;
}

/**
 * Draw the page to g,  scaled to fit the rectangle r.
 */
public void paint(Painter aPntr, Rect aRect)
{
    // Set PDF painter
    PDFMarkupHandler pntr = PDFMarkupHandler.get(aPntr, aRect); if(pntr==null) return;
    parse(pntr);
    pntr.endPage();
}
    
/**
 * Main entry point for parsing the page marking operations
 */
public void parse(PDFMarkupHandler aPntr)
{
    try { new PDFPageParser(_pfile, _index, aPntr).parse(); }
    catch(Exception e) { e.printStackTrace(); }
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