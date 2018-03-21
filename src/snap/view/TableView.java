/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.Color;
import snap.gfx.Insets;
import snap.gfx.Paint;
import snap.util.*;

/**
 * A View subclass to show a table of items.
 */
public class TableView <T> extends ParentView implements View.Selectable <T> {

    // The items
    List <T>                _items = new ArrayList();
    
    // The selected index
    int                     _selIndex = -1;
    
    // The selected column
    int                     _selCol;
    
    // Whether to show table header
    boolean                 _showHeader;
    
    // Whether to show horziontal/vertical lines
    boolean                 _showLinesH, _showLinesV;
    
    // Grid color
    Color                   _gridColor;
    
    // Row height
    int                     _rowHeight = 24;

    // The Cell Configure method
    Consumer <ListCell<T>>  _cellConf;
    
    // The SplitView to hold columns
    SplitView               _split = new SplitView();
    
    // The ScrollView to hold SplitView+Columns
    ScrollView              _scroll = new ScrollView(_split);
    
    // The view to hold header
    ParentView              _header = createHeaderView();
    
    // Constants
    static final Paint DIVIDER_FILL = new Color("#EEEEEE");
    static final Paint DIVIDER_FILLH = new Color("#E0E0E0");
    
/**
 * Creates a new TableView.
 */
public TableView()
{
    // Enable Action event for selection change
    enableEvents(Action);
    setFocusable(true); setFocusWhenPressed(true);
    
    // Configure Columns SplitView and ScrollView and add
    _split.setBorder(null); _split.setGrowWidth(true);
    setBorder(_scroll.getBorder()); _scroll.setBorder(null);
    addChild(_scroll);
    
    // Set main scroller to sync HeaderScroller
    Scroller scroller = _scroll.getScroller();
    scroller.addPropChangeListener(pce -> getHeaderScroller().setScrollH(scroller.getScrollH()), Scroller.ScrollH_Prop);
    
    // Whenever one split needs layout, propogate to other
    SplitView hsplit = getHeaderSplitView();
    _split.addPropChangeListener(pc -> hsplit.relayout(), NeedsLayout_Prop);
    hsplit.addPropChangeListener(pc -> _split.relayout(), NeedsLayout_Prop);
}

/**
 * Returns the ScrollView.
 */
public ScrollView getScrollView()  { return _scroll; }

/**
 * Returns the items.
 */
public List <T> getItems()  { return _items; }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)
{
    if(ListUtils.equalsId(theItems, _items) || SnapUtils.equals(theItems,_items)) return;
    _items.clear();
    if(theItems!=null) _items.addAll(theItems);
    for(TableCol tcol : getCols()) tcol.setItems(theItems);
}

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(theItems!=null? Arrays.asList(theItems) : null); }

/**
 * Tell table to update given items (none means all).
 */
public void updateItems(T ... theItems)
{
    for(TableCol tcol : getCols()) tcol.updateItems(theItems);
}

/**
 * Returns the number of columns.
 */
public int getColCount()  { return _split.getItemCount(); }

/**
 * Returns the column at given index.
 */
public TableCol getCol(int anIndex)  { return (TableCol)_split.getItem(anIndex); }

/**
 * Returns the column at given index.
 */
public TableCol[] getCols()  { return _split.getItems().toArray(new TableCol[0]); }

/**
 * Adds a TableCol.
 */
public void addCol(TableCol aCol)
{
    // Add column to column SplitView
    _split.addItem(aCol);
    
    // Create Header Box for Column Header label
    View hdr = aCol.getHeader();
    BoxView hdrBox = new BoxView(hdr) {
        protected double getPrefWidthImpl(double aH)  { return aCol.getPrefWidth(); }
        public void setPrefWidth(double aValue)  { aCol.setPrefWidth(aValue); }
        public boolean isGrowWidth()  { return aCol.isGrowWidth(); }
    };
    hdrBox.setFillWidth(true);
    
    // Add Header Box to Header SplitView
    SplitView hsplit = getHeaderSplitView();
    hsplit.addItem(hdrBox);
    
    // Configure split dividers
    for(Divider div : _split.getDividers()) { div.setDividerSize(2); div.setFill(DIVIDER_FILL); div.setBorder(null); }
    for(Divider div : hsplit.getDividers()) { div.setDividerSize(2); div.setFill(DIVIDER_FILLH); div.setBorder(null); }
    
    // Synchronize TableCol selection with this TableView
    aCol.addPropChangeListener(pc -> setSelIndex(aCol.getSelIndex()), SelIndex_Prop);
}

/**
 * Remove's the TableCol at the given index from this Table's children list.
 */
public TableCol removeCol(int anIndex)  { return (TableCol)_split.removeChild(anIndex); }

/**
 * Removes the given TableCol from this table's children list.
 */
public int removeCol(TableCol aCol)  { return _split.removeItem(aCol); }

/**
 * Returns the number of rows.
 */
public int getRowCount()  { return getItems().size(); }

/**
 * Returns whether to show header.
 */
public boolean isShowHeader()  { return _showHeader; }

/**
 * Sets whether to show header.
 */
public void setShowHeader(boolean aValue)
{
    if(aValue==isShowHeader()) return;
    firePropChange("ShowHeader", _showHeader, _showHeader = aValue);
    
    // Add/remove header
    if(aValue) addChild(_header,0);
    else removeChild(_header);
}

/**
 * Returns whether to show horizontal lines.
 */
public boolean isShowLinesX()  { return _showLinesH; }

/**
 * Sets whether to show horizontal lines.
 */
public void setShowLinesX(boolean aValue)
{
    firePropChange("ShowHorizontalLines", _showLinesH, _showLinesH = aValue);
}

/**
 * Returns whether to show vertical lines.
 */
public boolean isShowLinesY()  { return _showLinesV; }

/**
 * Sets whether to show vertical lines.
 */
public void setShowLinesY(boolean aValue)
{
    firePropChange("ShowVerticalLines", _showLinesV, _showLinesV = aValue);
}

/**
 * Returns grid color.
 */
public Color getGridColor()  { return _gridColor; }

/**
 * Sets grid color.
 */
public void setGridColor(Color aValue)
{
    firePropChange("GridColor", _gridColor, _gridColor = aValue);
}

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
 * Returns the HeaderView.
 */
public ParentView getHeaderView()  { return _header!=null? _header : (_header=createHeaderView()); }

/**
 * Returns the HeaderView.
 */
protected ParentView createHeaderView()
{
    SplitView split = new SplitView(); split.setGrowWidth(true); split.setBorder(null);
    
    Scroller scroll = new Scroller(); scroll.setContent(split);
    LineView line = new LineView(0,.5,10,.5); line.setPrefHeight(1); line.setBorder(Color.LIGHTGRAY,1);
    ColView vbox = new ColView(); vbox.setFillWidth(true);
    vbox.setChildren(scroll,line);
    return vbox;
}

/**
 * Returns the Header ScrollView.
 */
protected Scroller getHeaderScroller()
{
    ColView vbox = (ColView)getHeaderView();
    return (Scroller)vbox.getChild(0);
}

/**
 * Returns the HeaderView.
 */
protected SplitView getHeaderSplitView()  { return (SplitView)getHeaderScroller().getContent(); }

/**
 * Returns the selected index.
 */
public int getSelIndex()  { return _selIndex; }

/**
 * Sets the selected index.
 */
public void setSelIndex(int anIndex)
{
    if(anIndex==_selIndex) return;
    firePropChange(SelIndex_Prop, _selIndex, _selIndex = anIndex);
    for(TableCol tcol : getCols()) tcol.setSelIndex(anIndex);
    fireActionEvent();
}

/**
 * Returns the selected item.
 */
public T getSelItem()  { return _selIndex>=0? _items.get(_selIndex) : null; }

/**
 * Sets the selected index.
 */
public void setSelItem(T anItem)
{
    int index = _items.indexOf(anItem);
    setSelIndex(index);
}
/**
 * Returns the selected row.
 */
public int getSelectedRow()  { return getSelIndex(); }

/**
 * Returns the selected column.
 */
public int getSelectedCol()  { return _selCol; }

/**
 * Sets the selected column.
 */
public void setSelectedCol(int anIndex)  { _selCol = anIndex; }

/**
 * Returns the row index at given point.
 */
public int getRowAt(double aX, double aY)  { return (int)(aY/getRowHeight()); }

/**
 * Override to reset cells.
 */
public void setY(double aValue)
{
    if(aValue==getY()) return; super.setY(aValue);
    for(TableCol tcol : getCols()) tcol.relayout();
}

/**
 * Override to reset cells.
 */
public void setHeight(double aValue)
{
    if(aValue==getHeight()) return; super.setHeight(aValue);
    for(TableCol tcol : getCols()) tcol.relayout();
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    //double pw = 0; for(TableCol tcol : getCols()) pw += tcol.getPrefWidth(); return pw;
    return _scroll.getPrefWidth(aH);
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    double ph = isShowHeader()? _header.getPrefHeight(aW) : 0;
    ph += getRowHeight()*getItems().size();
    return ph;
}

/**
 * Override to layout children with VBox layout.
 */
protected void layoutImpl()
{
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    double hh = 0;
    
    // If Header, update bounds
    if(isShowHeader()) { hh = _header.getPrefHeight();
        _header.setBounds(x,y,_header.getWidth(),hh); }
        
    // Layout out scrollView
    _scroll.setBounds(x,y+hh,w,h-hh);
}

/**
 * Override to sync header width with TableView.ScrollView.Scroller.
 */
protected void layoutDeepImpl()
{
    _scroll.layoutDeep();
    _header.setWidth(_scroll.getScroller().getWidth());
    _header.layoutDeep();
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return SelItem_Prop; }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive ShowHeader
    if(!isShowHeader()) e.add("ShowHeader", false);
    
    // Archive GridColor, ShowLinesX, ShowLinesY
    if(getGridColor()!=null) e.add("GridColor", '#' + getGridColor().toHexString());
    if(isShowLinesX()) e.add("ShowLinesX", true);
    if(isShowLinesY()) e.add("ShowLinesY", true);
    
    // Archive RowHeight
    if(getRowHeight()!=24) e.add("RowHeight", getRowHeight());
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Unarchive TableHeader
    if(anElement.hasAttribute("ShowHeader"))
        setShowHeader(anElement.getAttributeBooleanValue("ShowHeader"));
    
    // Unarchive GridColor, ShowLinesX, ShowLinesY
    if(anElement.hasAttribute("GridColor")) setGridColor(new Color(anElement.getAttributeValue("GridColor")));
    setShowLinesX(anElement.getAttributeBoolValue("ShowLinesX", false));
    setShowLinesY(anElement.getAttributeBoolValue("ShowLinesY", false));
    
    // Unarchive RowHeight
    setRowHeight(anElement.getAttributeIntValue("RowHeight", 24));
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getColCount(); i<iMax; i++) { TableCol child = getCol(i);
        anElement.add(anArchiver.toXML(child, this)); }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive as child views
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && TableCol.class.isAssignableFrom(cls)) {
            TableCol col = (TableCol)anArchiver.fromXML(childXML, this);
            addCol(col);
        }
    }
}

}