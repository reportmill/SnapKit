/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show a table of items.
 */
public class TableView <T> extends ParentView implements View.Selectable <T> {

    // The items
    PickList <T>            _items = new PickList();
    
    // The selected column
    int                     _selCol;
    
    // Whether to show table header
    boolean                 _showHeader;
    
    // Whether to show horziontal/vertical grid lines
    boolean                 _showGridX, _showGridY;
    
    // Grid color
    Color                   _gridColor;
    
    // Row height
    int                     _rowHeight = 24;

    // An optional method hook to configure cell
    Consumer <ListCell<T>>  _cellConf;
    
    // An optional method hook to configure cell for editing
    Consumer <ListCell<T>>  _cellConfEdit;
    
    // Whether table cells are editable
    boolean                 _editable;
    
    // The SplitView to hold columns
    SplitView               _split = new SplitView();
    
    // The ScrollView to hold SplitView+Columns
    ScrollView              _scroll = new ScrollView(_split);
    
    // The view to hold header
    ParentView              _header;
    
    // Constants
    public static final String Editable_Prop = "Editable";
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
    _split.setBorder(null); _split.setGrowWidth(true); _split.setSpacing(2);
    Divider div = _split.getDivider(); div.setFill(DIVIDER_FILL); div.setBorder(null); div.setReach(3);
    setBorder(_scroll.getBorder()); _scroll.setBorder(null);
    addChild(_scroll);
    
    // Bind main Scroll.ScrollH to HeaderScroller (both ways)
    Scroller scroller = _scroll.getScroller();
    ViewUtils.bind(scroller, Scroller.ScrollH_Prop, getHeaderScroller(), true);
    
    // Register PickList to notify when selection changes
    _items.addPropChangeListener(pc -> pickListSelChange(pc));
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
    // If items already set, just return
    if(ListUtils.equalsId(theItems, _items) || SnapUtils.equals(theItems,_items)) return;
    
    // Set items
    _items.setAll(theItems);
    relayout(); relayoutParent(); repaint();
    for(TableCol tc : getCols()) tc.setItems(theItems);
}

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(theItems!=null? Arrays.asList(theItems) : null); }

/**
 * Returns the selected index.
 */
public int getSelIndex()  { return _items.getSelIndex(); }

/**
 * Sets the selected index.
 */
public void setSelIndex(int anIndex)  { _items.setSelIndex(anIndex); }

/**
 * Returns the selected item.
 */
public T getSelItem()  { return _items.getSelItem(); }

/**
 * Sets the selected index.
 */
public void setSelItem(T anItem)  { _items.setSelItem(anItem); }

/**
 * Selects up in the list.
 */
public void selectUp()  { _items.selectUp(); }

/**
 * Selects up in the list.
 */
public void selectDown()  { _items.selectDown(); }

/**
 * Called when PickList changes selection.
 */
protected void pickListSelChange(PropChange aPC)
{
    // If not SelIndex, just return
    if(aPC.getPropertyName()!=PickList.SelIndex_Prop) return;
    
    // FirePropChange
    int oldInd = (Integer)aPC.getOldValue(), newInd = (Integer)aPC.getNewValue();
    firePropChange(SelIndex_Prop, oldInd, newInd);
    
    // Scroll selection to visible
    //if(isShowing()) scrollSelToVisible();
}

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
    aCol._table = this;
    
    // Create Header Box for Column Header label
    View hdr = aCol.getHeader();
    BoxView hdrBox = new BoxView(hdr) {
        protected double getPrefWidthImpl(double aH)  { return aCol.getPrefWidth(); }
        public boolean isGrowWidth()  { return aCol.isGrowWidth(); }
    };
    hdrBox.setFillWidth(true);
    
    // Bind hdrBox.PrefWidth to aCol.PrefWidth, and visa-versa
    ViewUtils.bind(aCol, PrefWidth_Prop, hdrBox, true);
    
    // Add Header Box to Header SplitView
    SplitView hsplit = getHeaderSplitView();
    hsplit.addItem(hdrBox);
    
    // Configure split dividers
    for(Divider div : _split.getDividers()) { div.setFill(DIVIDER_FILL); div.setBorder(null); }
    for(Divider div : hsplit.getDividers()) { div.setFill(DIVIDER_FILLH); div.setBorder(null); }
    
    // Replace column picklist with tableView picklist
    aCol.setPickList(_items);
}

/**
 * Remove's the TableCol at the given index from this Table's children list.
 */
public TableCol removeCol(int anIndex)
{
    TableCol col = getCol(anIndex);
    removeCol(col);
    return col;
}

/**
 * Removes the given TableCol from this table's children list.
 */
public int removeCol(TableCol aCol)
{
    int ind = _split.removeItem(aCol);
    if(ind>=0) getHeaderSplitView().removeItem(ind);
    return ind;
}

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
 * Returns whether to show horizontal grid lines.
 */
public boolean isShowGridX()  { return _showGridX; }

/**
 * Sets whether to show horizontal grid lines.
 */
public void setShowGridX(boolean aValue)
{
    firePropChange("ShowGridX", _showGridX, _showGridX = aValue);
}

/**
 * Returns whether to show vertical grid lines.
 */
public boolean isShowGridY()  { return _showGridY; }

/**
 * Sets whether to show vertical grid lines.
 */
public void setShowGridY(boolean aValue)
{
    firePropChange("ShowGridY", _showGridY, _showGridY = aValue);
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
 * Called to set method to configure cell for rendering.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return _cellConf; }

/**
 * Called to set method to configure cell for rendering.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _cellConf = aCC; }

/**
 * Called to set method to configure cell for editing.
 */
public Consumer<ListCell<T>> getCellConfigureEdit()  { return _cellConfEdit; }

/**
 * Called to set method to configure cell for editing.
 */
public void setCellConfigureEdit(Consumer<ListCell<T>> aCC)  { _cellConfEdit = aCC; }

/**
 * Returns whether table cells are editable.
 */
public boolean isEditable()  { return _editable; }

/**
 * Sets whether table cells are editable.
 */
public void setEditable(boolean aValue)
{
    // If already set, just return
    if(aValue==isEditable()) return;
    
    // Set value, fire prop change and enable MouseRelease events
    firePropChange(Editable_Prop, _editable, _editable = aValue);
    if(aValue) enableEvents(MouseRelease);
    else disableEvents(MouseRelease);
}

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
    split.setSpacing(_split.getSpacing());
    Divider div = split.getDivider(); div.setFill(DIVIDER_FILLH); div.setBorder(null); div.setReach(3);
    
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
 * Returns the selected row.
 */
public int getSelRow()  { return getSelIndex(); }

/**
 * Returns the selected column.
 */
public int getSelCol()  { return _selCol; }

/**
 * Sets the selected column.
 */
public void setSelCol(int anIndex)  { _selCol = anIndex; }

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
 * Handle events.
 */
public void processEvent(ViewEvent anEvent)
{
    // Handle Mouse double-click
    if(anEvent.isMouseClick() && anEvent.getClickCount()==2 && isEditable()) {
        ListCell cell = getCellAtXY(anEvent.getX(), anEvent.getY());
        if(cell!=null)
            configureCellEdit(cell);
    }
}

/**
 * Returns the column at given X coord.
 */
public TableCol <T> getColAtX(double aX)
{
    for(TableCol col : getCols())
        if(aX>=col.getX() && aX<=col.getMaxX())
            return col;
    return null;
}

/**
 * Returns the cell at given Y coord.
 */
public ListCell <T> getCellAtXY(double aX, double aY)
{
    TableCol <T> col = getColAtX(aX); if(col==null) return null;
    Point pnt = col.parentToLocal(aX, aY, this);
    return col.getCellAtY(pnt.y);
}

/**
 * Called to configure a cell for edit.
 */
protected void configureCellEdit(ListCell <T> aCell)
{
    if(getCellConfigureEdit()!=null)
        getCellConfigureEdit().accept(aCell);
    else {
        aCell.setEditing(true);
    }
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
    if(isShowGridX()) e.add("ShowGridX", true);
    if(isShowGridY()) e.add("ShowGridY", true);
    
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
    setShowGridX(anElement.getAttributeBoolValue("ShowGridX", false));
    setShowGridY(anElement.getAttributeBoolValue("ShowGridY", false));
    
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