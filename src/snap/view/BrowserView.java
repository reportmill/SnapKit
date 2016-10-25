/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.ListUtils;

/**
 * A browser class.
 */
public class BrowserView <T> extends ParentView {
    
    // The first column
    BrowserCol <T>          _col0;

    // The resolver
    TreeResolver <T>        _resolver = new TreeResolver.Adapter() { };
    
    // Row height
    int                     _rowHeight = 20;

    // The Cell Configure method
    Consumer <ListCell<T>>  _cellConf;
    
    // The minimum column width
    int                     _minColWidth = 200;

    // The minimum number of columns
    int                     _visColCount = 2;
    
    // The HBox layout
    ViewLayout.HBoxLayout   _layout = new ViewLayout.HBoxLayout(this);
    
    // Selection back paint
    Paint                   _selBackPaint = new Color("#022eff");
    
/**
 * Creates a new BrowserView.
 */
public BrowserView()
{
    _col0 = addCol();
    _layout.setFillHeight(true);
    enableEvents(Action);
}

/**
 * Returns the items.
 */
public List <T> getItems()  { return _col0.getItems(); }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)
{
    // If already set, just return
    if(ListUtils.equalsId(theItems, getItems()) || theItems.equals(getItems())) return;
    _col0.setItems(theItems);
}

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(Arrays.asList(theItems)); }

/**
 * Called to update items that have changed.
 */
public void updateItems(T ... theItems)  { _col0.updateItems(theItems); }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _col0.getSelectedIndex(); }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)
{
    if(anIndex==getSelectedIndex()) return;
    //firePropertyChange("SelectedIndex", _selIndex, _selIndex = anIndex);
    _col0.setSelectedIndex(anIndex);
    fireActionEvent();
}

/**
 * Returns the resolver.
 */
public TreeResolver getResolver()  { return _resolver; }

/**
 * Sets the resolver.
 */
public void setResolver(TreeResolver aResolver)  { _resolver = aResolver; }

/**
 * Returns the parent of given item.
 */
public T getParent(T anItem)  { return _resolver.getParent(anItem); }

/**
 * Returns the parent of given item.
 */
protected int getParentCount(T anItem)  { int pc = 0; for(T p=anItem; p!=null; p=getParent(p)) pc++; return pc; }

/**
 * Whether given object is a parent (has children).
 */
protected boolean isParent(T anItem)  { return _resolver.isParent(anItem); }

/**
 * The number of children in given parent.
 */
protected int getChildCount(T aParent)  { return _resolver.getChildren(aParent).length; }

/**
 * The child at given index in given parent.
 */
protected T getChild(T aParent, int anIndex)  { return _resolver.getChildren(aParent)[anIndex]; }

/**
 * Returns the children.
 */
public T[] getChildren(T aParent)  { return _resolver.getChildren(aParent); }

/**
 * Returns the text to be used for given item.
 */
protected String getText(T anItem)  { return _resolver.getText(anItem); }

/**
 * Return the image to be used for given item.
 */
protected Image getImage(T anItem)  { return _resolver.getImage(anItem); }

/**
 * Return the graphic to be used for given item.
 */
protected View getGraphic(T anItem)  { return _resolver.getGraphic(anItem); }

/**
 * Return the image to be used for given item after text.
 */
protected Image getImageAfter(T anItem)  { return _resolver.getImage(anItem); }

/**
 * Return the graphic to be used for given item after text.
 */
protected View getGraphicAfter(T anItem)  { return _resolver.getGraphic(anItem); }

/**
 * Return the branch image to be used for given item.
 */
protected Image getBranchImage(T anItem)  { return _resolver.getBranchImage(anItem); }

/**
 * Returns the column count.
 */
public int getColCount()  { return getChildCount(); }

/**
 * Returns the browser column list at given index.
 */
public BrowserCol <T> getCol(int anIndex)
{
    ScrollView spane = (ScrollView)getChild(anIndex);
    return (BrowserCol)spane.getContent();
}

/**
 * Adds a column.
 */
protected BrowserCol addCol()
{
    BrowserCol bcol = new BrowserCol(this);
    int index = bcol._index = getChildCount();
    ColScrollView spane = new ColScrollView(bcol);
    addChild(spane);
    if(index>0) {
        BrowserCol <T> lastCol = getCol(index-1);
        T item = lastCol.getSelectedItem();
        bcol.setItems(getChildren(item));
    }
    return bcol;
}

/**
 * Returns the currently selected column.
 */
public BrowserCol <T> getSelectedCol()
{
    int columnIndex = getChildCount() - 1;
    while(columnIndex>=0) {
        BrowserCol <T> col = getCol(columnIndex--);
        if(col.getSelectedItem()!=null)
            return col;
    }
    return null;
}

/**
 * Returns the currently selected column.
 */
public int getSelectedColIndex()
{
    int colIndex = getChildCount() - 1;
    while(colIndex>=0) {
        BrowserCol col = getCol(colIndex--);
        if(col.getSelectedItem()!=null)
            return colIndex;
    }
    return -1;
}

/**
 * Sets the selected column index.
 */
protected void setSelectedColumnIndex(int anIndex)
{
    int selIndex = getSelectedColIndex(); if(anIndex==selIndex) return;  // If value already set, just return
    for(int i=selIndex+1, iMax=getColCount(); i<iMax; i++)      // Clear selection in columns after this one
        getCol(i).setSelectedIndex(-1);
}

/**
 * Returns the selected item.
 */
public T getSelectedItem()
{
    BrowserCol <T> bcol = getSelectedCol();
    return bcol!=null? bcol.getSelectedItem() : null;
}

/**
 * Returns the number of visible columns in the browser.
 */
public int getVisColCount()  { return _visColCount; }

/**
 * Sets the number of visible columns in the browser.
 */
public void setVisColCount(int aValue)  { _visColCount = aValue; }

/**
 * Returns the row height.
 */
public int getRowHeight()  { return _rowHeight; }

/**
 * Sets the row height.
 */
public void setRowHeight(int aValue)
{
    firePropChange("RowHeight", _rowHeight, _rowHeight = aValue);
}

/**
 * Called to set method for rendering.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return _cellConf; }

/**
 * Called to set method for rendering.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _cellConf = aCC; }

/**
 * Returns the path constructed by appending the selected row in each column by a dot.
 */
public String getPath()  { return getPath("."); }

/**
 * Returns the path constructed by appending the selected row in each column by a dot.
 */ 
public String getPath(String aSeparator)
{
    // Create string buffer for path
    StringBuffer buf = new StringBuffer();
    
    // Iterate over browser columns to add selected row items
    for(int i=0, iMax=getColCount(); i<iMax; i++) {
        BrowserCol col = getCol(i);
        Object item = col.getSelectedItem(); if(item==null) break;
        if(i>0) buf.append(aSeparator);
        buf.append(item.toString());
    }
    
    // Return path string
    return buf.toString();
}

/**
 * Returns the minimum column width.
 */
public int getMinColumnWidth()  { return _minColWidth; }

/**
 * Sets the minimum column width.
 */
public void setMinColumnWidth(int aWidth)  { _minColWidth = aWidth; }

/**
 * PreferredSize.
 */
public double getPrefWidthImpl(double aH)
{
    View vport = getParent(); double width = vport!=null? vport.getWidth() : 150;
    return width/getVisColCount()*getColCount();
}

/**
 * Returns the preferred height.
 */
public double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(-1); }

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

/**
 * Scrollable methods.
 */
public boolean isScrollFitWidth() { return getColCount()<=getVisColCount(); }
public boolean isScrollFitHeight() { return true; }

/**
 * Called to configure browser cell.
 */
protected void configureBrowserCell(BrowserCol aCol, ListCell <T> aCell)
{
    // Set real text for item, image for item, and make text grow width
    T item = aCell.getItem(); if(item==null) return;
    String name = getText(item); aCell.setText(name);
    Image img = getImage(item); if(img!=null) aCell.setImage(img);
    aCell.getStringView().setGrowWidth(true);
    
    // If parent, add branch icon
    if(isParent(item)) {
        Image bimg = getBranchImage(item); if(bimg==null) bimg = getBranchImage();
        if(bimg!=null) aCell.setImageAfter(bimg);
    }
    
    // If cell configure, call that
    Consumer cconf = getCellConfigure();
    if(cconf!=null) cconf.accept(aCell);
}

/**
 * Returns the icon to indicate branch nodes in a browser (right arrow by default).
 */
public Image getBranchImage()
{
    // If branch icon hasn't been created, create it
    if(_branchImg!=null) return _branchImg;
    Image img = Image.get(9,11,true);
    Polygon poly = new Polygon(1.5,1.5,7.5,5.5,1.5,9.5);
    Painter pntr = img.getPainter(); pntr.setColor(Color.BLACK); pntr.fill(poly); pntr.flush();
    return _branchImg = img;
} Image _branchImg;

/**
 * A browser column ScrollView.
 */
private class ColScrollView extends ScrollView {

    /** Creates a new ColScrollView. */
    public ColScrollView(BrowserCol aCol)
    {
        setContent(aCol); setShowHBar(false); setShowVBar(true); getScroller().setFill(Color.WHITE);
    }
    
    /** PreferredSize. */
    public double getPrefWidthImpl(double aH)
    {
        View brsr = getParent(), vport = brsr!=null? brsr.getParent() : null;
        double width = vport!=null? vport.getWidth() : brsr!=null? brsr.getWidth() : 200;
        return width/getVisColCount();
    }
}

}