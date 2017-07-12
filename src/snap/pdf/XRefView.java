/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.*;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.TextPane;
import snap.web.WebURL;

/**
 * Views a PDF file.
 */
public class XRefView extends ViewOwner {
    
    // The PDF file source
    Object     _src;
    
    // The PDFFile
    PDFFile    _pfile;
    
    // The page index
    int        _pindex;
    
    // The ImageView
    ImageView  _imageView;
    
    // The XRef TreeView
    TreeView   _treeView;
    
    // The XRef TextView
    TextView   _xtextView;
    
    // The PDF TextView
    TextView   _ptextView;
    
/**
 * Creates a new XRefView.
 */
public XRefView(Object aSource)  { setSource(aSource); }

/**
 * Returns the source.
 */
public Object getSource()  { return _src; }

/**
 * Sets the source.
 */
public void setSource(Object aSource)
{
    if(aSource==_src) return;
    _src = aSource;
    
    // Get source bytes and reset file
    byte bytes[] = SnapUtils.getBytes(_src);
    PDFFile pfile = bytes!=null? new PDFFile(bytes) : null;
    setPDFFile(pfile);
}

/**
 * Returns the PDF file.
 */
public PDFFile getPDFFile()  { return _pfile; }

/**
 * Sets the PDF file.
 */
public void setPDFFile(PDFFile aFile)
{
    if(aFile==_pfile) return;
    _pfile = aFile;
    getUI();
    
    // Set image
    Image img = _pfile!=null? _pfile.getPage(0).getImage() : null;
    _imageView.setImage(img);
    
    // Set XRefs
    _treeView.setItems(_pfile);
    _treeView.expandAll();
    
    // Set Text
    byte bytes[] = _pfile!=null? _pfile.getBytes() : null;
    String str = bytes!=null? new String(bytes) : "";
    _ptextView.setText(str);
}

/**
 * Returns the page.
 */
public int getPage()  { return _pindex; }

/**
 * Sets the page.
 */
public void setPage(int anIndex)
{
    if(anIndex==_pindex) return;
    _pindex = anIndex;

    Image img = _pfile!=null? _pfile.getPage(0).getImage() : null;
    _imageView.setImage(img);
}

/**
 * Returns the number of pages in file.
 */
public int getPageCount()  { return _pfile!=null? _pfile.getPageCount() : 0; }

/**
 * Returns the UI.
 */
protected void initUI()
{
    // Get TabView
    TabView tabView = getView("TabView", TabView.class);
    
    // Get/configure ImageView
    _imageView = getView("ImageView", ImageView.class); _imageView.getParent().setPrefSize(820,940);
    _imageView.setFill(Color.WHITE); _imageView.setBorder(Color.BLACK, 1);
    enableEvents(_imageView, MouseRelease);
    
    // Get/configure TreeView
    _treeView = getView("TreeView", TreeView.class);
    _treeView.setResolver(new PDFResolver());
    _treeView.getCol(0).setAltPaint(new Color("#F8F8F8"));
    
    // Create/add XRef TextView
    TextPane xtextPane = new TextPane();
    View xtextPaneUI = xtextPane.getUI(); xtextPaneUI.setGrowWidth(true);
    _xtextView = xtextPane.getTextView(); _xtextView.setFont(Font.Arial14);
    SplitView xsplit = getView("XRefSplit", SplitView.class);
    xsplit.addItem(xtextPaneUI);
    
    // Create/add PDFTextView
    TextPane ptextPane = new TextPane();
    View ptextPaneUI = ptextPane.getUI(); ptextPaneUI.setGrowWidth(true);
    _ptextView = ptextPane.getTextView(); _ptextView.setFont(Font.Arial14);
    tabView.addTab("Text", ptextPaneUI);
    
    // Set PFile
    enableEvents(getUI(), DragDrop);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    Object item = _treeView.getSelectedItem();
    _xtextView.setText(getTextViewText(item));
    System.out.println("PW: " + _xtextView.getPrefWidth());
}

/**
 * RespoindUI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // If no file, just bail
    if(_pfile==null) return;
    
    // Handle ImageView
    if(anEvent.equals("ImageView"))
        setPage((getPage()+1)%getPageCount());
    
    // Handle DragDrop
    if(anEvent.isDragDrop()) {
        anEvent.acceptDrag();
        WebURL url = WebURL.getURL(anEvent.getDragFiles().get(0));
        setSource(url);
        anEvent.dropComplete();
    }
}

/**
 * Returns text view text.
 */
public String getTextViewText(Object anItem)
{
    // If no file, return empty string
    if(_pfile==null) return "";
    
    // Get item string
    Object item = _pfile.getXRefObj(anItem);
    
    // Handle Map
    if(item instanceof Map) { Map map = (Map)item;
        StringBuffer sb = new StringBuffer("<<\n");
        for(Map.Entry entry : (Set<Map.Entry>)map.entrySet())
            sb.append("    /").append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
        sb.append(">>");
        return sb.toString();
    }
    
    // Handle List
    if(item instanceof List) { List list = (List)item;
        StringBuffer sb = new StringBuffer("[\n");
        for(Object itm : list) sb.append("    ").append(itm).append('\n');
        sb.append("]");
        return sb.toString();
    }
    
    // Handle anything else
    return item!=null? item.toString() : "(null)";
}

/**
 * Returns the type string for PDF object.
 */
public String getTypeString(Object anObj)
{
    if(anObj instanceof PDFStream) { PDFStream stream = (PDFStream)anObj;
        Map dict = stream.getDict();
        String type = (String)dict.get("Type");
        String stype = (String)dict.get("Subtype"); if(stype!=null) type += ' ' + stype;
        if(type!=null) return type;
        return "Stream";
    }
    if(anObj instanceof Map)
        return "Dict";
    if(anObj instanceof List)
        return "Array";
    return null;
}

/**
 * Main method.
 */
public static void main(String args[])
{
    // Get default doc source
    Object src = args.length>0? args[0] : "/tmp/test.pdf";
    WebURL url = WebURL.getURL(src); if(url.getFile()==null) url = null;
    
    // Create Viewer
    XRefView xrv = new XRefView(url);
    xrv.getUI().setPrefSize(1000,1000);
    xrv.getWindow().setTitle(url.getPathName() + " - " + url.getPathNameSimple());
    xrv.setWindowVisible(true);
}

/**
 * A TreeResolver.
 */
private class PDFResolver extends TreeResolver {

    /**
     * Returns the parent of given item.
     */
    public Object getParent(Object anItem)
    {
        if(anItem instanceof PDFFile) return null;
        if(anItem instanceof PDFXTable) return _pfile;
        if(anItem instanceof PDFXEntry) return _pfile.getXRefTable();
        if(anItem==_pfile.getTrailer()) return _pfile;
        return false;
    }
    
    /**
     * Whether given object is a parent (has children).
     */
    public boolean isParent(Object anItem)
    {
        if(anItem instanceof PDFFile) return true;
        if(anItem instanceof PDFXTable) return true;
        return false;
    }
    
    /**
     * Returns the children.
     */
    public Object[] getChildren(Object aParent)
    {
        if(aParent instanceof PDFFile) { PDFFile pfile = (PDFFile)aParent;
            return new Object[] { pfile.getXRefTable(), pfile.getTrailer() };
        }
        if(aParent instanceof PDFXTable) { PDFXTable xtable = (PDFXTable)aParent;
            return xtable.getXRefs().toArray();
        }
        return null;
    }
    
    /**
     * Returns the text to be used for given item.
     */
    public String getText(Object anItem)
    {
        if(anItem instanceof PDFFile) return "File";
        if(anItem instanceof PDFXTable) return "XTable";
        if(anItem instanceof PDFXEntry) { PDFXEntry xref = (PDFXEntry)anItem;
            Object obj = _pfile.getXRefObj(xref);
            return xref + " " + getTypeString(obj);
        }
        if(anItem==_pfile.getTrailer()) return "Trailer";
        return anItem.toString();
    }
}

}