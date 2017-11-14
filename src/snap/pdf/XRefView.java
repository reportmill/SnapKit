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
    
    // The XRef TextArea
    TextArea   _xtextArea;
    
    // The PDF TextArea
    TextArea   _ptextArea;
    
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
    String str = getFileString();
    _ptextArea.setText(str);
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

    Image img = _pfile!=null? _pfile.getPage(_pindex).getImage() : null;
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
    
    // Create/add XRef TextArea
    TextPane xtextPane = new TextPane();
    View xtextPaneUI = xtextPane.getUI(); xtextPaneUI.setGrowWidth(true);
    _xtextArea = xtextPane.getTextArea(); _xtextArea.setFont(Font.Arial14);
    SplitView xsplit = getView("XRefSplit", SplitView.class);
    xsplit.addItem(xtextPaneUI);
    
    // Create/add PDFTextArea
    TextPane ptextPane = new TextPane();
    View ptextPaneUI = ptextPane.getUI(); ptextPaneUI.setGrowWidth(true);
    _ptextArea = ptextPane.getTextArea(); _ptextArea.setFont(Font.Arial14);
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
    _xtextArea.setText(getEntryText(item));
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
        WebURL url = anEvent.getClipboard().getFiles().get(0).getSourceURL();
        setSource(url);
        anEvent.dropComplete();
    }
}

/**
 * Returns XRef entry text.
 */
public String getEntryText(Object anItem)
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
 * Returns the file string.
 */
public String getFileString()
{
    // If no file, return empty string
    if(_pfile==null) return "";
    
    // Get string builder for file bytes
    byte bytes[] = _pfile!=null? _pfile.getBytes() : null;
    String str = bytes!=null? new String(bytes) : "";
    StringBuilder sb = new StringBuilder(str);
    
    // Strip out binary between "stream" and "endstream" strings
    for(int i=sb.indexOf("stream");i>0;i=sb.indexOf("stream",i)) {
        int end = sb.indexOf("endstream", i+6); if(end<0) break;
        boolean binary = false; for(int j=i+6;j<end;j++) if(isBinary(sb.charAt(j))) { binary = true; break; }
        if(binary) {
            sb.delete(i+7, end); i = i + 17; }
        else i = end + 10;
    }
    
    // Return string
    return sb.toString();
}

/** Returns whether given char is binary. */
static boolean isBinary(char c)  { return Character.isISOControl(c) || !Character.isDefined(c); }

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
            String str = getTypeString(obj);
            String xstr = xref.toString(); if(str!=null) xstr += " " + str;
            return xstr;
        }
        if(anItem==_pfile.getTrailer()) return "Trailer";
        return anItem.toString();
    }
}

}