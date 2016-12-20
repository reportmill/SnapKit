/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf;
import java.util.*;
import snap.gfx.Color;
import snap.view.*;
import snap.web.WebURL;

/**
 * Views a PDF file.
 */
public class XRefView extends ViewOwner {
    
    // The PDFFile
    PDFFile    _pfile;
    
    // The page index
    int        _pindex;
    
    // The TreeView
    TreeView   _treeView;
    
    // The TextView
    TextView   _textView;
    
    // The ImageView
    ImageView  _imageView;
    
    // The PDF TextView
    TextView   _ptextView;
    
/**
 * Creates a new XRefView.
 */
public XRefView(Object aSource)
{
    _pfile = new PDFFile(WebURL.getURL(aSource).getBytes());
}

/**
 * Returns the UI.
 */
protected void initUI()
{
    _treeView = getView("TreeView", TreeView.class);
    _treeView.setResolver(new PDFResolver());
    _treeView.setItems(_pfile);
    _treeView.expandAll();
    _treeView.getCol(0).setAltPaint(new Color("#F8F8F8"));
    _textView = getView("TextView", TextView.class);
    
    //
    _imageView = getView("ImageView", ImageView.class);
    _imageView.setImage(_pfile.getPage(0).getImage());
    _imageView.setFill(Color.WHITE); _imageView.setBorder(Color.BLACK, 1);
    enableEvents(_imageView, MouseRelease);
    
    // PDFTextView
    _ptextView = getView("PDFTextView", TextView.class);
    String str = new String(_pfile.getBytes());
    _ptextView.setText(str);
    
    enableEvents(getUI(), DragDrop);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    Object item = _treeView.getSelectedItem();
    _textView.setText(getTextViewText(item));
}

/**
 * RespoindUI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ImageView
    if(anEvent.equals("ImageView")) {
        _pindex = (_pindex+1)%_pfile.getPageCount();
        _imageView.setImage(_pfile.getPage(_pindex).getImage());
    }
    
    // Handle DragDrop
    if(anEvent.isDragDrop()) {
        anEvent.acceptDrag();
        WebURL url = WebURL.getURL(anEvent.getDragFiles().get(0));
        _pfile = new PDFFile(url.getBytes());
        _treeView.setItems(_pfile);
        _imageView.setImage(_pfile.getPage(0).getImage());
        String str = new String(_pfile.getBytes());
        _ptextView.setText(str);
    }
}

/**
 * Returns text view text.
 */
public String getTextViewText(Object anItem)
{
    Object item = _pfile.getXRefObj(anItem);
    if(item instanceof Map) { Map map = (Map)item;
        StringBuffer sb = new StringBuffer("<<\n");
        for(Map.Entry entry : (Set<Map.Entry>)map.entrySet())
            sb.append("    /").append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
        sb.append(">>");
        return sb.toString();
    }
    if(item instanceof List) { List list = (List)item;
        StringBuffer sb = new StringBuffer("[\n");
        for(Object itm : list) sb.append("    ").append(itm).append('\n');
        sb.append("]");
        return sb.toString();
    }
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
    Object src = args.length>0? args[0] : "/tmp/test.pdf";
    WebURL url = WebURL.getURL(src);
    XRefView xrv = new XRefView(url); xrv.getWindow().setTitle(url.getPathName() + " - " + url.getPathNameSimple());
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