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
    boolean                 _showHeader, _showHeaderCol;
    
    // Whether to show horziontal/vertical grid lines
    boolean                 _showGridX, _showGridY;
    
    // Grid color
    Color                   _gridColor;
    
    // Row height
    double                  _rowHeight, _rowHeightCached = -1;

    // The cell padding
    Insets                  _cellPad = ListArea.CELL_PAD_DEFAULT;
    
    // An optional method hook to configure cell
    Consumer <ListCell<T>>  _cellConf;
    
    // An optional method hook to configure cell for editing
    Consumer <ListCell<T>>  _cellEditStart, _cellEditEnd;
    
    // Whether table cells are editable
    boolean                 _editable;
    
    // The SplitView to hold columns
    SplitView               _split;
    
    // The ScrollView to hold SplitView+Columns
    ScrollGroup             _scrollGroup;
    
    // The view to hold header
    SplitView               _header;
    
    // The header column
    TableCol                _headerCol;
    
    // A listener to watch for Cell.Editing set to false
    PropChangeListener      _editEndLsnr;

    // Constants
    public static final String CellPadding_Prop = "CellPadding";
    public static final String Editable_Prop = "Editable";
    public static final String RowHeight_Prop = "RowHeight";
    static final Paint DIVIDER_FILL = new Color("#EEEEEE");
    static final Paint DIVIDER_FILLH = new Color("#E0E0E0");
    
/**
 * Creates a new TableView.
 */
public TableView()
{
    // Enable Action event for selection change
    enableEvents(MousePress, KeyPress, Action);
    setFocusable(true); setFocusWhenPressed(true);
    
    // Create/configure Columns SplitView and ScrollView and add
    _split = new SplitView(); _split.setBorder(null); _split.setGrowWidth(true); _split.setSpacing(2);
    Divider div = _split.getDivider(); div.setFill(DIVIDER_FILL); div.setBorder(null); div.setReach(3);
    
    // Create/configure/add ScrollGroup
    _scrollGroup = new ScrollGroup(_split);
    setBorder(_scrollGroup.getBorder()); _scrollGroup.setBorder(null);
    addChild(_scrollGroup);
    
    // Register PickList to notify when selection changes
    _items.addPropChangeListener(pc -> pickListSelChange(pc));
}

/**
 * Returns the ScrollGroup.
 */
public ScrollGroup getScrollGroup()  { return _scrollGroup; }

/**
 * Returns the ScrollView.
 */
public ScrollView getScrollView()  { return _scrollGroup.getScrollView(); }

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
    if(_headerCol!=null) _headerCol.setItems(theItems);
    itemsChanged();
}

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(theItems!=null? Arrays.asList(theItems) : null); }

/**
 * Called when PickList items changed.
 */
protected void itemsChanged()
{
    _rowHeightCached = -1;
}

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
 * Selects up in the table.
 */
public void selectUp()
{
    int row = getSelRow();
    if(row>0) setSelIndex(row-1);
}

/**
 * Selects down in the table.
 */
public void selectDown()
{
    int row = getSelRow();
    if(row<getItems().size()-1)
        setSelIndex(row+1);
}

/**
 * Selects right in the table.
 */
public void selectRight()
{
    int row = getSelRow(); if(row<0) row = 0;
    int col = getSelCol()+1; if(col>=getColCount()) { col = 0; row = (row+1)%getRowCount(); }
    setSelCell(row, col);
}

/**
 * Selects right in the table.
 */
public void selectLeft()
{
    int row = getSelRow(); if(row<0) row = 0;
    int col = getSelCol()-1; if(col<0) { col = getColCount()-1; row = (row+getRowCount()-1)%getRowCount(); }
    setSelCell(row, col);
}

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
    repaint();
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
public TableCol getCol(int anIndex)
{
    if(anIndex==-1 && isShowHeaderCol()) return getHeaderCol();
    return (TableCol)_split.getItem(anIndex);
}

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
    SplitView hsplit = getHeaderView();
    hsplit.addItem(hdrBox);
    
    // Configure split dividers
    for(Divider div : _split.getDividers()) { div.setFill(DIVIDER_FILL); div.setBorder(null); }
    for(Divider div : hsplit.getDividers()) { div.setFill(DIVIDER_FILLH); div.setBorder(null); }
    
    // Replace column picklist with tableView picklist
    aCol.setPickList(_items);
    aCol.setCellPadding(getCellPadding());
    _rowHeightCached = -1;
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
    if(ind>=0) getHeaderView().removeItem(ind);
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
    _scrollGroup.setTopView(aValue? getHeaderView() : null);
}

/**
 * Returns whether to show header.
 */
public boolean isShowHeaderCol()  { return _showHeaderCol; }

/**
 * Sets whether to show header.
 */
public void setShowHeaderCol(boolean aValue)
{
    if(aValue==isShowHeaderCol()) return;
    firePropChange("ShowHeaderCol", _showHeaderCol, _showHeaderCol = aValue);
    
    // Add/remove header
    _scrollGroup.setLeftView(aValue? getHeaderCol() : null);
    _scrollGroup.getCornerNW().setContent(aValue? getHeaderCol().getHeader() : null);
}

/**
 * Returns the header col.
 */
public TableCol getHeaderCol()
{
    if(_headerCol!=null) return _headerCol;
    _headerCol = new TableCol(); _headerCol._table = this;
    _headerCol.setPickList(_items);
    _headerCol.setCellPadding(getCellPadding());
    _headerCol.setItems(getItems());
    return _headerCol;
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
 * Returns whether row height has been explicitly set.
 */
public boolean isRowHeightSet()  { return _rowHeight>0; }

/**
 * Returns the row height.
 */
public double getRowHeight()
{
    if(_rowHeight>0) return _rowHeight;
    if(_rowHeightCached>0) return _rowHeightCached;
    
    _rowHeightCached = 1;
    for(TableCol col : getCols()) _rowHeightCached = Math.max(_rowHeightCached, col.getRowHeightSuper());
    return _rowHeightCached;
}

/**
 * Sets the row height.
 */
public void setRowHeight(double aValue)
{
    firePropChange(RowHeight_Prop, _rowHeight, _rowHeight = aValue);
}

/**
 * Returns the cell padding.
 */
public Insets getCellPadding()  { return _cellPad; }

/**
 * Sets the cell padding.
 */
public void setCellPadding(Insets aPad)
{
    if(aPad==null) aPad = ListArea.CELL_PAD_DEFAULT; if(aPad.equals(_cellPad)) return;
    firePropChange(CellPadding_Prop, _cellPad, _cellPad=aPad);
    for(TableCol col : getCols()) col.setCellPadding(_cellPad);
    relayout(); relayoutParent();
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
 * Called to set method to start cell editing.
 */
public Consumer<ListCell<T>> getCellEditStart()  { return _cellEditStart; }

/**
 * Called to set method to start cell editing.
 */
public void setCellEditStart(Consumer<ListCell<T>> aCC)  { _cellEditStart = aCC; }

/**
 * Called to set method to stop cell for editing.
 */
public Consumer<ListCell<T>> getCellEditEnd()  { return _cellEditEnd; }

/**
 * Called to set method to stop cell for editing.
 */
public void setCellEditEnd(Consumer<ListCell<T>> aCC)  { _cellEditEnd = aCC; }

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
    if(aValue) enableEvents(MouseRelease, KeyPress);
    else disableEvents(MouseRelease, KeyPress);
}

/**
 * Returns the HeaderView.
 */
public SplitView getHeaderView()  { return _header!=null? _header : (_header=createHeaderView()); }

/**
 * Returns the HeaderView.
 */
protected SplitView createHeaderView()
{
    SplitView split = new SplitView(); split.setGrowWidth(true); split.setBorder(null);
    split.setSpacing(_split.getSpacing());
    Divider div = split.getDivider(); div.setFill(DIVIDER_FILLH); div.setBorder(null); div.setReach(3);
    return split;
}

/**
 * Returns the cell at given row and col.
 */
public ListCell <T> getCell(int aRow, int aCol)
{
    if(aRow<0) return null; // || aCol<0
    TableCol col = getCol(aCol);
    ListCell <T> cell = col.getCellForRow(aRow);
    return cell;
}

/**
 * Returns the row index at given point.
 */
public int getRowAt(double aX, double aY)  { return (int)(aY/getRowHeight()); }

/**
 * Returns the column at given X coord.
 */
public TableCol <T> getColAtX(double aX)
{
    // Check normal columns
    Point pnt = _split.parentToLocal(aX, 0, this);
    for(TableCol col : getCols())
        if(pnt.x>=col.getX() && pnt.x<=col.getMaxX())
            return col;

    // If header column is showing, check it
    if(isShowHeaderCol()) { TableCol hdrCol = getHeaderCol();
        pnt = hdrCol.parentToLocal(aX,0);
        if(hdrCol.contains(aX,1))
            return hdrCol;
    }

    // Return null, since column not found    
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
 * Override to give control to table.
 */
protected void configureCell(TableCol <T> aCol, ListCell <T> aCell)
{
    aCol.configureCellText(aCell);
    aCol.configureCellFills(aCell);
    Consumer cconf = getCellConfigure();
    if(cconf!=null) cconf.accept(aCell);
}

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
public void setSelCol(int anIndex)
{
    // If already set, just return
    if(anIndex==getSelCol()) return;
    
    // Set value
    _selCol = anIndex;
    repaint();
}

/**
 * Returns the selected cell.
 */
public ListCell <T> getSelCell()  { return getCell(getSelRow(), getSelCol()); }

/**
 * Returns the selected cell.
 */
public void setSelCell(int aRow, int aCol)
{
    // If already selected, just return
    if(aRow==getSelRow() && aCol==getSelCol()) return;
    
    // Stop cell editing
    editCellStop();
    
    // Set selected column and row
    if(aCol<getColCount())
        setSelCol(aCol);
    if(aRow<getRowCount())
        setSelIndex(aRow);
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, _scrollGroup, aH); }

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
protected void layoutImpl()  { BoxView.layout(this, _scrollGroup, null, true, true); }

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MousePress: If hit column isn't selected, select + fire action + consume event
    if(anEvent.isMousePress()) {
        TableCol col = getColAtX(anEvent.getX());
        int index = col!=null? col.getColIndex() : -1;
        if(index>=0 && index!=getSelCol()) {
            setSelCol(index);
            fireActionEvent();
            anEvent.consume();
        }
    }
    
    // Handle Mouse double-click
    if(anEvent.isMouseClick() && anEvent.getClickCount()==2 && isEditable()) {
        ListCell cell = getCellAtXY(anEvent.getX(), anEvent.getY());
        editCell(cell);
    }
    
    // Handle KeyPress
    else if(anEvent.isKeyPress())
        processKeyEvent(anEvent);
}

/**
 * Handle events.
 */
protected void processKeyEvent(ViewEvent anEvent)
{
    int kcode = anEvent.getKeyCode();
    switch(kcode) {
        case KeyCode.UP: selectUp(); fireActionEvent(); anEvent.consume(); break;
        case KeyCode.DOWN: selectDown(); fireActionEvent(); anEvent.consume(); break;
        case KeyCode.LEFT: selectLeft(); fireActionEvent(); anEvent.consume(); break;
        case KeyCode.RIGHT: selectRight(); fireActionEvent(); anEvent.consume(); break;
        case KeyCode.TAB:
            if(anEvent.isShiftDown()) selectLeft(); else selectRight();
            fireActionEvent(); anEvent.consume(); break;
        default: {
            char c = anEvent.getKeyChar();
            boolean printable = Character.isJavaIdentifierPart(c); // Lame
            if(isEditable() && printable) {
                ListCell cell = getSelCell();
                editCell(cell);
            }
            else if(kcode==KeyCode.ENTER) { selectDown(); anEvent.consume(); break; }
        }
    }
}

/**
 * Called when TableCol gets mouse press.
 */
protected void colDidMousePress(TableCol aCol, ViewEvent anEvent)
{
    int row = aCol.getSelIndex(), col = aCol.getColIndex();
    if(row!=getSelRow() || col!=getSelCol()) {
        setSelCell(row, col);
        fireActionEvent();
        anEvent.consume();
    }
}

/**
 * Override to paint highlight for selected cell.
 */
protected void paintAbove(Painter aPntr)
{
    ListCell cell = getSelCell(); if(cell==null) return;
    Rect bnds = cell.localToParent(cell.getBoundsLocal(), this).getBounds();
    Image img = getSelectedRectImage(bnds);
    aPntr.drawImage(img, bnds.x-1, bnds.y-1);
}

/**
 * A fuzzy cell border image to highlight cell.
 */
protected Image getSelectedRectImage(Rect aRect)
{
    if(_selImg!=null && _selImg.getWidth()==aRect.width+2 && _selImg.getHeight()==aRect.height+2) return _selImg;
    Rect rect = aRect.getInsetRect(0,0); rect.x = rect.y = 1;
    ShapeView shpView = new ShapeView(rect);
    shpView.setSize(aRect.width+2, aRect.height+2);
    shpView.setBorder(ViewEffect.FOCUSED_COLOR.brighter(),1);
    shpView.setEffect(ViewEffect.getFocusEffect());
    return _selImg = ViewUtils.getImage(shpView);
} Image _selImg;

/**
 * Called to edit given cell.
 */
public void editCell(ListCell aCell)
{
    if(aCell==null || !isEditable() || aCell.isEditing()) return;
    cellEditStart(aCell);
}

/**
 * Called to stop editing a cell.
 */
public void editCellStop()
{
    if(!isEditable()) return;
    ListCell cell = getSelCell();
    if(cell!=null && cell.isEditing())
        cellEditEnd(cell);
}

/**
 * Called to configure a cell for edit.
 */
protected void cellEditStart(ListCell <T> aCell)
{
    // Call CellEditStart (or just set Cell.Editing true)
    if(getCellEditStart()!=null)
        getCellEditStart().accept(aCell);
    else aCell.setEditing(true);
    
    // Add listener to catch Cell.Editing false
    aCell.addPropChangeListener(_editEndLsnr = pc -> cellEditEnd(aCell), Label.Editing_Prop);
}

/**
 * Called when Cell.Editing set false to stop cell editing.
 */
protected void cellEditEnd(ListCell <T> aCell)
{
    aCell.removePropChangeListener(_editEndLsnr, Label.Editing_Prop); _editEndLsnr = null;
    if(getCellEditEnd()!=null)
        getCellEditEnd().accept(aCell);
    aCell.setEditing(false);
}

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
 * Override to forward to table.
 */
public View getFocusNext()
{
    selectRight();
    ListCell cell = getSelCell();
    if(cell!=null && isEditable()) getEnv().runLater(() -> editCell(cell));
    return cell;
}

/**
 * Override to forward to table.
 */
public View getFocusPrev()
{
    selectLeft();
    ListCell cell = getSelCell();
    if(cell!=null && isEditable()) getEnv().runLater(() -> editCell(cell));
    return cell;
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
    if(isRowHeightSet()) e.add(RowHeight_Prop, getRowHeight());
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
    if(anElement.hasAttribute(RowHeight_Prop)) setRowHeight(anElement.getAttributeIntValue(RowHeight_Prop));
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