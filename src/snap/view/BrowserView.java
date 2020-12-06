/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;

import snap.geom.Insets;
import snap.geom.Polygon;
import snap.gfx.*;
import snap.util.*;

/**
 * A browser class.
 */
public class BrowserView <T> extends ParentView implements Selectable<T> {
    
    // The first column
    private BrowserCol <T>  _col0;

    // The resolver
    private TreeResolver <T>  _resolver = new TreeResolver.Adapter() { };
    
    // Row height
    private int  _rowHeight = 20;

    // The Cell Configure method
    private Consumer <ListCell<T>>  _cellConf;
    
    // The preferred number of columns
    private int  _prefColCount = 2;
    
    // The preferred column width
    private int  _prefColWidth = 150;
    
    // The selected column index
    private int  _selCol = -1;
    
    // The view that holds the columns
    private RowView  _colView = new RowView();
    
    // The ScrollView to hold SplitView+Columns
    private ScrollView  _scroll = new ScrollView(_colView);
    
    // The image to be used for branch
    private Image  _branchImg;
    
    // Constants for properties
    public static final String PrefColCount_Prop = "PrefColCount";
    public static final String PrefColWidth_Prop = "PrefColWidth";
    
    /**
     * Creates a new BrowserView.
     */
    public BrowserView()
    {
        setFocusable(true);
        setFocusWhenPressed(true);

        _scroll.setFillHeight(true);
        addChild(_scroll);
        _colView.setFillHeight(true);
        _col0 = addCol();
        enableEvents(Action);
    }

    /**
     * Returns the ScrollView.
     */
    public ScrollView getScrollView()  { return _scroll; }

    /**
     * Returns the preferred number of visible columns in the browser.
     */
    public int getPrefColCount()  { return _prefColCount; }

    /**
     * Sets the preferred number of visible columns in the browser.
     */
    public void setPrefColCount(int aValue)  { _prefColCount = aValue; }

    /**
     * Returns the preferred column width.
     */
    public int getPrefColWidth()  { return _prefColWidth; }

    /**
     * Sets the preferred column width.
     */
    public void setPrefColWidth(int aWidth)  { _prefColWidth = aWidth; }

    /**
     * Returns the row height.
     */
    public int getRowHeight()  { return _rowHeight; }

    /**
     * Sets the row height.
     */
    public void setRowHeight(int aValue)
    {
        // Set value
        firePropChange("RowHeight", _rowHeight, _rowHeight = aValue);

        // Update columns
        for (BrowserCol col : getCols()) col.setRowHeight(aValue);
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
     * Returns the items.
     */
    public List <T> getItems()  { return _col0.getItems(); }

    /**
     * Sets the items.
     */
    public void setItems(List <T> theItems)
    {
        // If already set, just return
        if (ListUtils.equalsId(theItems, getItems()) || theItems.equals(getItems())) return;

        // Get current selected item and col0 selected item
        T selItem = getSelItem(), selItem0 = _col0.getSelItem();
        _col0.setItems(theItems);
        if (selItem0==_col0.getSelItem())
            setSelItem(selItem);
        else setSelItem(null);
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
     * Returns the resolver.
     */
    public TreeResolver <T> getResolver()  { return _resolver; }

    /**
     * Sets the resolver.
     */
    public void setResolver(TreeResolver <T> aResolver)  { _resolver = aResolver; }

    // Convenience resolver methods
    private boolean isParent(T anItem)  { return _resolver.isParent(anItem); }
    private T getParent(T anItem)  { return _resolver.getParent(anItem); }
    private T[] getChildren(T aParent)  { return _resolver.getChildren(aParent); }
    private String getText(T anItem)  { return _resolver.getText(anItem); }
    private Image getImage(T anItem)  { return _resolver.getImage(anItem); }
    private Image getBranchImage(T anItem)  { return _resolver.getBranchImage(anItem); }

    /**
     * Returns the column count.
     */
    public int getColCount()  { return _colView.getChildCount(); }

    /**
     * Returns the browser column list at given index.
     */
    public BrowserCol <T> getCol(int anIndex)  { return (BrowserCol)_colView.getChild(anIndex); }

    /**
     * Returns the last column.
     */
    public BrowserCol <T> getColLast()  { return getCol(getColCount()-1); }

    /**
     * Returns the browser columns.
     */
    public BrowserCol[] getCols()
    {
        int cc = getColCount(); BrowserCol cols[] = new BrowserCol[cc];
        for (int i=0;i<cc;i++) cols[i] = getCol(i);
        return cols;
    }

    /**
     * Adds a column.
     */
    protected BrowserCol addCol()
    {
        // Create new browser column and set index
        BrowserCol bcol = new BrowserCol(this);
        int index = bcol._index = getColCount();

        // Add to ColBox
        _colView.addChild(bcol);

        // If not root column, set items from last
        if (index>0) {
            BrowserCol <T> lastCol = getCol(index-1);
            T item = lastCol.getSelItem();
            T items[] = getChildren(item);
            bcol.setItems(items);
        }

        // Reset all cached widths
        for (BrowserCol col : getCols())
            col.relayoutParent();

        // Return column
        return bcol;
    }

    /**
     * Removes a column.
     */
    protected void removeCol(int anIndex)
    {
        _colView.removeChild(anIndex);
    }

    /**
     * Returns the currently selected column.
     */
    public BrowserCol <T> getSelCol()  { int sci = getSelColIndex(); return sci>=0? getCol(sci) : null; }

    /**
     * Returns the currently selected column.
     */
    public int getSelColIndex()  { return _selCol; }

    /**
     * Sets the selected column index.
     */
    protected void setSelColIndex(int anIndex)
    {
        // Set value
        _selCol = anIndex;

        // Iterate back from end to index and remove unused columns
        for (int i=getColCount()-1; i>anIndex; i--)
            removeCol(i);

        // See if we need to add column
        T item = getSelItem();
        if (item!=null && isParent(item))
            addCol();
    }

    /**
     * Returns the selected index.
     */
    public int getSelIndex()
    {
        BrowserCol <T> bcol = getSelCol();
        return bcol!=null? bcol.getSelIndex() : -1;
    }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int anIndex)
    {
        BrowserCol <T> bcol = getSelCol();
        if (bcol!=null)
            bcol.setSelIndex(anIndex);
    }

    /**
     * Returns the selected item.
     */
    public T getSelItem()
    {
        BrowserCol <T> bcol = getSelCol();
        return bcol!=null? bcol.getSelItem() : null;
    }

    /**
     * Sets the selected item.
     */
    public void setSelItem(T anItem)  { setSelItem(anItem, true); }

    /**
     * Sets the selected item.
     */
    public void setSelItem(T anItem, boolean scrollToVisible)
    {
        // If null item, reset to first column
        if (anItem==null) {
            getCol(0).setSelIndex(-1);
            setSelColIndex(0);
            return;
        }

        // If already set, just return
        if (anItem.equals(getSelItem())) return;

        // If item in last column, select it
        if (getColLast().getItems().contains(anItem)) {
            getColLast().setSelItem(anItem);
            setSelColIndex(getColLast().getIndex());
            if (scrollToVisible) scrollSelToVisible();
            return;
        }

        // Otherwise if item is selected, select column
        BrowserCol col = getColWithSelItem(anItem);
        if (col!=null) {
            setSelColIndex(col.getIndex());
            if (scrollToVisible) scrollSelToVisible();
            return;
        }

        // Otherwise, select parent
        T par = getParent(anItem);
        setSelItem(par, false);

        // Select item
        getColLast().setSelItem(anItem);
        setSelColIndex(getColLast().getIndex());
        if (scrollToVisible) scrollSelToVisible();
    }

    /**
     * Scrolls current selection to visible.
     */
    public void scrollSelToVisible()
    {
        // If ColView NeedsLayout, come back later
        if (_colView.isNeedsLayout() || ViewUtils.isMouseDown()) {
            getEnv().runLater(() -> scrollSelToVisible()); return; }

        // Scroll ColLast to visible
        _colView.scrollToVisible(getColLast().getBounds());
    }

    /**
     * Returns the column that has selected item.
     */
    protected BrowserCol getColWithSelItem(T anItem)
    {
        for (int i=getColCount()-1;i>=0;i--) { BrowserCol col = getCol(i);
            if (col.getSelItem()==anItem)
                return col; }
        return null;
    }

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
        for (int i=0, iMax=getColCount(); i<iMax; i++) {
            BrowserCol <T> col = getCol(i);
            T item = col.getSelItem(); if (item==null) break;
            if (i>0) buf.append(aSeparator);
            buf.append(getText(item));
        }

        // Return path string
        return buf.toString();
    }

    /**
     * PreferredSize.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double pw = getPrefColCount()*getPrefColWidth();
        return ins.left + pw + ins.right;
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double ph = 0; for (BrowserCol col : getCols()) ph = Math.max(ph, col.getBestHeight(-1));
        return ins.top + ph + ins.bottom;
    }

    /**
     * Override to layout ScrollView.
     */
    protected void layoutImpl()
    {
        Insets ins = getInsetsAll();
        double x = ins.left, w = getWidth() - x - ins.right;
        double y = ins.top, h = getHeight() - y - ins.bottom;
        _scroll.setBounds(x,y,w,h);
    }

    /**
     * Override to automatically set parent Scroller to FillHeight.
     */
    protected void setParent(ParentView aPar)
    {
        super.setParent(aPar);
        if (aPar instanceof Scroller)
            ((Scroller)aPar).setFillHeight(true);
    }

    /**
     * Called to configure browser cell.
     */
    protected void configureBrowserCell(BrowserCol aCol, ListCell <T> aCell)
    {
        // Set real text for item, image for item, and make text grow width
        T item = aCell.getItem(); if (item==null) return;
        String name = getText(item);
        aCell.setText(name);
        Image img = getImage(item);
        if (img!=null) aCell.setImage(img);
        aCell.getStringView().setGrowWidth(true);

        // If parent, add branch icon
        if (isParent(item)) {
            Image bimg = getBranchImage(item);
            if (bimg==null) bimg = getBranchImage();
            if (bimg!=null) aCell.setImageAfter(bimg);
        }

        // If cell configure, call that
        Consumer cconf = getCellConfigure();
        if (cconf!=null)
            cconf.accept(aCell);
    }

    /**
     * Returns the icon to indicate branch nodes in a browser (right arrow by default).
     */
    public Image getBranchImage()
    {
        // If branch icon hasn't been created, create it
        if (_branchImg!=null) return _branchImg;
        Image img = Image.get(9,11,true);
        Polygon poly = new Polygon(1.5,1.5,7.5,5.5,1.5,9.5);
        Painter pntr = img.getPainter(); pntr.setColor(Color.BLACK); pntr.fill(poly); pntr.flush();
        return _branchImg = img;
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive PrefColCount, PrefColWidth
        if (getPrefColCount()!=2) e.add(PrefColCount_Prop, getPrefColCount());
        if (getPrefColWidth()!=150) e.add(PrefColWidth_Prop, getPrefColWidth());

        // Archive
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Archive PrefColCount, PrefColWidth
        if (anElement.hasAttribute(PrefColCount_Prop)) setPrefColCount(anElement.getAttributeIntValue(PrefColCount_Prop));
        if (anElement.hasAttribute(PrefColWidth_Prop)) setPrefColWidth(anElement.getAttributeIntValue(PrefColWidth_Prop));
    }
}