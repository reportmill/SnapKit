/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;

import snap.geom.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show a table of items.
 */
public class TableView <T> extends ParentView implements Selectable<T> {

    // The items
    private PickList <T>  _items = new PickList<>();

    // The Table selection
    private ListSel2D _sel = ListSel2D.EMPTY;
    
    // Whether to show table header
    private boolean  _showHeader;
    
    // Whether to show table header column
    private boolean  _showHeaderCol;

    // Whether to show horizontal/vertical grid lines
    private boolean  _showGridX, _showGridY;
    
    // Grid color
    private Color  _gridColor;
    
    // Row height
    private double  _rowHeight, _rowHeightCached = -1;

    // The cell padding
    private Insets  _cellPad = ListArea.CELL_PAD_DEFAULT;
    
    // An optional method hook to configure cell
    private Consumer <ListCell<T>>  _cellConf;
    
    // Whether table cells are editable
    private boolean  _editable;

    // The list cell that is currently being edited
    private ListCell<T>  _editingCell;

    // The SplitView to hold columns
    private SplitView  _split;
    
    // The ScrollView to hold SplitView+Columns
    private ScrollGroup  _scrollGroup;
    
    // The view to hold header
    private SplitView  _header;
    
    // The header column
    private TableCol<T>  _headerCol;

    // A helper to handle selection
    private TableViewSelector  _selector = new TableViewSelector(this);

    // Constants for properties
    public static final String CellPadding_Prop = "CellPadding";
    public static final String Editable_Prop = "Editable";
    public static final String EditingCell_Prop = "EditingCell";
    public static final String RowHeight_Prop = "RowHeight";
    public static final String ShowHeader_Prop = "ShowHeader";

    // Internal constants
    public static final int DIVIDER_SPAN = 2;
    private static final Paint DIVIDER_FILL = new Color("#EEEEEE");
    private static final Paint DIVIDER_FILLH = new Color("#E0E0E0");
    
    /**
     * Creates a new TableView.
     */
    public TableView()
    {
        // Set border, enable Action event for selection change
        setBorder(getDefaultBorder());
        enableEvents(MousePress, MouseDrag, MouseRelease, KeyPress, Action);
        setFocusable(true);
        setFocusWhenPressed(true);

        // Create/configure Columns SplitView and ScrollView and add
        _split = new SplitView();
        _split.setBorder(null);
        _split.setGrowWidth(true);
        _split.setDividerSpan(DIVIDER_SPAN);
        Divider div = _split.getDivider();
        div.setFill(DIVIDER_FILL);
        div.setBorder(null);
        div.setReach(3);

        // Create/configure/add ScrollGroup
        _scrollGroup = new ScrollGroup(_split);
        _scrollGroup.setBorder(null);
        addChild(_scrollGroup);

        // Register PickList to notify when prop changes
        _items.addPropChangeListener(pc -> pickListPropChange(pc));
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
        if (ListUtils.equalsId(theItems, _items) || SnapUtils.equals(theItems,_items)) return;

        // Set items
        _items.setAll(theItems);
        for (TableCol tc : getCols())
            tc.setItems(theItems);
        if (_headerCol!=null)
            _headerCol.setItems(theItems);

        // Register for relayout/repaint
        relayout(); relayoutParent(); repaint();
    }

    /**
     * Sets the items.
     */
    public void setItems(T ... theItems)
    {
        setItems(theItems!=null ? Arrays.asList(theItems) : null);
    }

    /**
     * Returns whether list allows multiple selections.
     */
    public boolean isMultiSel()  { return _items.isMultiSel(); }

    /**
     * Sets whether list allows multiple selections.
     */
    public void setMultiSel(boolean aValue)  { _items.setMultiSel(aValue); }

    /**
     * Returns the selection.
     */
    public ListSel getSel()
    {
        return _items.getSel();
    }

    /**
     * Sets the table selection.
     */
    public void setSel(ListSel aSel)
    {
        // If already set, just return
        if (aSel.equals(getSel())) return;

        // Stop cell editing
        editCellStop();

        // Set selection
        _items.setSel(aSel);
    }

    /**
     * Returns the selection.
     */
    public ListSel2D getSel2D()
    {
        ListSel sel = getSel();
        if (sel.isEmpty())
            return ListSel2D.EMPTY;
        return new ListSel2D(0, sel.getAnchor(), getColCount()-1, sel.getLead());
    }

    /**
     * Sets the table selection.
     */
    public void setSel2D(ListSel2D aSel)
    {
        // If already set, just return
        if (aSel.equals(getSel2D())) return;

        // Stop cell editing
        editCellStop();

        // If Empty, set ListSel.Empty
        if (aSel.isEmpty())
            setSel(ListSel.EMPTY);

        // Otherwise, create ListSel and set
        else {
            ListSel sel = new ListSel(aSel.getAnchorY(), aSel.getLeadY());
            setSel(sel);
        }

        // Update Sel
        _sel = aSel;
        repaint();
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
     * Returns the selected row.
     */
    public int getSelRowIndex()  { return getSelIndex(); }

    /**
     * Returns the selected column.
     */
    public int getSelColIndex()  { return _sel.getLeadX(); }

    /**
     * Returns the selected cell.
     */
    public void setSelRowColIndex(int aRow, int aCol)
    {
        setSel2D(new ListSel2D(aCol, aRow, aCol, aRow));
    }

    /**
     * Returns the selected cell.
     */
    public ListCell <T> getSelCell()
    {
        int row = getSelRowIndex();
        int col = getSelColIndex();
        if (row<0 || col<0)
            return null;
        return getCell(row, col);
    }

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
        int row = getSelRowIndex();
        if (row>0)
            setSelIndex(row-1);
        else ViewUtils.beep();
    }

    /**
     * Selects down in the table.
     */
    public void selectDown()
    {
        int row = getSelRowIndex();
        if (row<getItems().size()-1)
            setSelIndex(row+1);
        else ViewUtils.beep();
    }

    /**
     * Selects right in the table.
     */
    public void selectRight()
    {
        int row = getSelRowIndex(); if (row<0) row = 0;
        int col = getSelColIndex()+1; if (col>=getColCount()) { col = 0; row = (row+1)%getRowCount(); }
        setSelRowColIndex(row, col);
    }

    /**
     * Selects right in the table.
     */
    public void selectLeft()
    {
        int row = getSelRowIndex(); if (row<0) row = 0;
        int col = getSelColIndex()-1; if (col<0) { col = getColCount()-1; row = (row+getRowCount()-1)%getRowCount(); }
        setSelRowColIndex(row, col);
    }

    /**
     * Called when PickList changes selection.
     */
    protected void pickListPropChange(PropChange aPC)
    {
        // Handle Sel_Prop: Get array of changed indexes and update
        String propName = aPC.getPropName();
        if (propName==PickList.Sel_Prop) {
            ListSel sel1 = (ListSel)aPC.getOldValue();
            ListSel sel2 = (ListSel)aPC.getNewValue();
            int changed[] = ListSel.getChangedIndexes(sel1, sel2);
            //for (int i : changed)
            //    updateIndex(i);

            int oldInd = changed.length>1 ? changed[0] : -1;
            int newInd = changed.length>1 ? changed[changed.length-1] : -1;
            firePropChange(SelIndex_Prop, oldInd, newInd);
        }

        // Handle Items_Prop: Reset RowHeightCached
        else if (propName==PickList.Item_Prop) {
            _rowHeightCached = -1;
        }

        // Scroll selection to visible
        //if (isShowing()) scrollSelToVisible();
        repaint();
    }

    /**
     * Tell table to update given items (none means all).
     */
    public void updateItems(T ... theItems)
    {
        for (TableCol tcol : getCols())
            tcol.updateItems(theItems);
    }

    /**
     * Returns the number of columns.
     */
    public int getColCount()  { return _split.getItemCount(); }

    /**
     * Returns the column at given index.
     */
    public TableCol <T> getCol(int anIndex)
    {
        if (anIndex==-1 && isShowHeaderCol()) return getHeaderCol();
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
        for (Divider div : _split.getDividers()) {
            div.setFill(DIVIDER_FILL); div.setBorder(null); }
        for (Divider div : hsplit.getDividers()) {
            div.setFill(DIVIDER_FILLH); div.setBorder(null); }

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
        if (ind>=0) getHeaderView().removeItem(ind);
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
        // If already set, just return
        if (aValue==isShowHeader()) return;

        // Set value, fire prop change
        firePropChange(ShowHeader_Prop, _showHeader, _showHeader = aValue);

        // Add/remove header
        _scrollGroup.setTopView(aValue ? getHeaderView() : null);
    }

    /**
     * Returns the HeaderView.
     */
    public SplitView getHeaderView()
    {
        // If already set, just return
        if (_header!=null) return _header;

        // Create and return
        SplitView split = new SplitView();
        split.setGrowWidth(true);
        split.setBorder(null);
        split.setDividerSpan(DIVIDER_SPAN);
        Divider div = split.getDivider();
        div.setFill(DIVIDER_FILLH);
        div.setBorder(null);
        div.setReach(3);
        return _header = split;
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
        // If already set, just return
        if (aValue==isShowHeaderCol()) return;

        // Set value and firePropChange
        firePropChange("ShowHeaderCol", _showHeaderCol, _showHeaderCol = aValue);

        // Add/remove header
        _scrollGroup.setLeftView(aValue ? getHeaderCol() : null);
        _scrollGroup.getCornerNW().setContent(aValue ? getHeaderCol().getHeader() : null);
    }

    /**
     * Returns the header col.
     */
    public TableCol getHeaderCol()
    {
        // If already set, just return
        if (_headerCol!=null) return _headerCol;

        // Create and return
        _headerCol = new TableCol();
        _headerCol._table = this;
        _headerCol.setFill(null);
        _headerCol.setPickList(_items);
        _headerCol.setCellPadding(getCellPadding());
        _headerCol.setItems(getItems());
        _headerCol.setCellConfigure(cell -> configureHeaderColCell(cell));
        return _headerCol;
    }

    /**
     * Default configure for HeaderCol cells (use index).
     */
    private void configureHeaderColCell(ListCell <T> aCell)
    {
        aCell.setText(String.valueOf(aCell.getRow()+1));
        aCell.setAlign(HPos.CENTER);
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
        if (_rowHeight>0)
            return _rowHeight;
        if (_rowHeightCached>0)
            return _rowHeightCached;

        _rowHeightCached = 1;
        for (TableCol col : getCols())
            _rowHeightCached = Math.max(_rowHeightCached, col.getRowHeightSuper());
        if (isShowHeaderCol())
            _rowHeightCached = Math.max(_rowHeightCached, getHeaderCol().getRowHeightSuper());
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
        if (aPad==null) aPad = ListArea.CELL_PAD_DEFAULT; if (aPad.equals(_cellPad)) return;
        firePropChange(CellPadding_Prop, _cellPad, _cellPad=aPad);
        for (TableCol col : getCols())
            col.setCellPadding(_cellPad);
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
     * Returns the cell at given row and col.
     */
    public ListCell <T> getCell(int aRow, int aCol)
    {
        if (aRow<0) return null; // || aCol<0
        TableCol col = getCol(aCol);
        ListCell <T> cell = col!=null ? col.getCellForRow(aRow) : null;
        return cell;
    }

    /**
     * Get rid of this.
     */
    public int getRowAt(double aX, double aY)  { return getRowIndexForY(aY); }

    /**
     * Returns the col index for given X.
     */
    public int getColIndexForX(double aX)
    {
        Point pnt = _split.parentToLocal(aX, 0, this);
        TableCol cols[] = getCols();
        for (int i=0; i<cols.length; i++) {
            TableCol col = cols[i];
            if (pnt.x >= col.getX() && pnt.x <= col.getMaxX())
                return i;
        }
        return -1;
    }

    /**
     * Returns the row index for given Y.
     */
    public int getRowIndexForY(double aY)
    {
        Point pnt = _split.parentToLocal(0, aY, this);
        return (int) (pnt.y/getRowHeight());
    }

    /**
     * Returns the column at given X coord.
     */
    public TableCol <T> getColForX(double aX)
    {
        // Check normal columns
        Point pnt = _split.parentToLocal(aX, 0, this);
        for (TableCol col : getCols())
            if (pnt.x>=col.getX() && pnt.x<=col.getMaxX())
                return col;

        // If header column is showing, check it
        if (isShowHeaderCol()) {
            TableCol hdrCol = getHeaderCol();
            pnt = hdrCol.parentToLocal(aX,0);
            if (hdrCol.contains(aX,1))
                return hdrCol;
        }

        // Return null, since column not found
        return null;
    }

    /**
     * Returns the cell at given Y coord.
     */
    public ListCell <T> getCellForXY(double aX, double aY)
    {
        TableCol <T> col = getColForX(aX); if (col==null) return null;
        Point pnt = col.parentToLocal(aX, aY, this);
        return col.getCellForY(pnt.y);
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
        Insets ins = getInsetsAll();
        double hdrPH = isShowHeader() ? _header.getPrefHeight(aW) : 0;
        double rowsPH = getRowHeight()*getItems().size();
        double ph = hdrPH + rowsPH + ins.getHeight();
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
        _selector.processEvent(anEvent);
    }

    /**
     * Override to catch KeyPress (tab or enter) for TableView or cells (when editing).
     */
    protected void processEventFilters(ViewEvent anEvent)
    {
        // Do normal version
        super.processEventFilters(anEvent);

        // Handle KeyPress Tab
        if (anEvent.isKeyPress()) {
            int kcode = anEvent.getKeyCode();
            switch (kcode) {
                case KeyCode.TAB:
                    if (anEvent.isShiftDown()) selectLeft(); else selectRight();
                    fireActionEvent(anEvent); anEvent.consume(); requestFocus(); break;
                case KeyCode.ENTER:
                    if (anEvent.isShiftDown()) selectUp(); else selectDown();
                    fireActionEvent(anEvent); anEvent.consume(); requestFocus(); break;
            }
        }
    }

    /**
     * Override to paint highlight for selected cell.
     */
    protected void paintAbove(Painter aPntr)
    {
        // Get Selected Cell (just return if null)
        ListCell cell = getSelCell(); if (cell == null) return;

        // Clip to Scroller bounds
        Scroller scroller = getScrollView().getScroller();
        Rect scrollerBnds = scroller.getBoundsLocal();
        Rect scrollerBndsInTable = scroller.localToParent(scrollerBnds, this).getBounds();
        aPntr.clip(scrollerBndsInTable);

        // Get fuzzy border image for selected cell bounds
        Rect cellBounds = cell.getBoundsLocal();
        Rect cellBoundsInTable = cell.localToParent(cellBounds, this).getBounds();
        ImageBox imgBox = getSelRectImage(cellBoundsInTable);
        imgBox.paintImageBox(aPntr, cellBoundsInTable.x, cellBoundsInTable.y);
    }

    /**
     * A fuzzy cell border image to highlight cell.
     */
    protected ImageBox getSelRectImage(Rect aRect)
    {
        // If already set and at right size, just return
        if (_selImgBox!=null && _selImgBox.width==aRect.width && _selImgBox.height==aRect.height)
            return _selImgBox;

        // Create, set and return
        Shape shape = aRect.copyForBounds(0,0, aRect.width, aRect.height);
        ShapeView shpView = new ShapeView(shape);
        boolean focused = isFoc();
        shpView.setBorder(focused ? ViewEffect.FOCUSED_COLOR.brighter() : Color.GRAY,1);
        shpView.setEffect(focused ? ViewEffect.getFocusEffect() : new ShadowEffect(5, Color.GRAY, 0, 0));
        return _selImgBox = ViewUtils.getImageBoxForScale(shpView, -1);
    }

    // A fuzzy cell border image to highlight cell
    private ImageBox _selImgBox;

    /** Returns whether this view or child view has focus. */
    boolean isFoc()
    {
        WindowView win = getWindow(); if (win==null) return false;
        View fv = win.getFocusedView();
        for (View v = fv; v!=null; v = v.getParent())
            if (v==this)
                return true;
        return false;
    }

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
        if (aValue==isEditable()) return;
        _editable = aValue;

        // Set value, fire prop change and enable MouseRelease events
        if (aValue)
            enableEvents(MouseRelease, KeyPress);
        else disableEvents(MouseRelease, KeyPress);

        firePropChange(Editable_Prop, !_editable, _editable);
    }

    /**
     * Returns the cell currently editing.
     */
    public ListCell<T> getEditingCell()  { return _editingCell; }

    /**
     * Sets the cell currently editing.
     */
    protected void setEditingCell(ListCell<T> aCell)
    {
        if (aCell==getEditingCell()) return;
        firePropChange(EditingCell_Prop, _editingCell, _editingCell = aCell);
    }

    /**
     * Called to edit given cell.
     */
    public void editCell(ListCell aCell)
    {
        // If not appropriate, just return
        if (aCell==null || !isEditable() || aCell.isEditing()) return;

        // Call CellEditStart (or just set Cell.Editing true)
        aCell.setEditing(true);
    }

    /**
     * Called to stop editing a cell.
     */
    public void editCellStop()
    {
        if (!isEditable()) return;
        ListCell cell = getSelCell();
        if (cell!=null && cell.isEditing())
            cell.setEditing(false);
    }

    /**
     * Called when cell editing starts.
     */
    protected void cellEditingChanged(ListCell<T> aCell)
    {
        // Update EditingCell
        ListCell<T> cell = aCell.isEditing() ? aCell : null;
        setEditingCell(cell);
    }

    /**
     * Override to reset cells.
     */
    public void setY(double aValue)
    {
        if (aValue==getY()) return; super.setY(aValue);
        for (TableCol tcol : getCols()) tcol.relayout();
    }

    /**
     * Override to reset cells.
     */
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return; super.setHeight(aValue);
        for (TableCol tcol : getCols()) tcol.relayout();
    }

    /**
     * Override to forward to table.
     */
    public View getFocusNext()
    {
        selectRight();
        ListCell cell = getSelCell();
        if (cell!=null && isEditable()) getEnv().runLater(() -> editCell(cell));
        return cell;
    }

    /**
     * Override to forward to table.
     */
    public View getFocusPrev()
    {
        selectLeft();
        ListCell cell = getSelCell();
        if (cell!=null && isEditable()) getEnv().runLater(() -> editCell(cell));
        return cell;
    }

    /**
     * Override to clear focus image.
     */
    protected void setFocused(boolean aValue)
    {
        if (aValue==isFocused()) return;
        super.setFocused(aValue);
        _selImgBox = null;
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return SelItem_Prop; }

    /**
     * Returns the default border.
     */
    public Border getDefaultBorder()  { return ScrollView.SCROLL_VIEW_BORDER; }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ShowHeader
        if (isShowHeader()) e.add(ShowHeader_Prop, false);

        // Archive GridColor, ShowLinesX, ShowLinesY
        if (getGridColor()!=null) e.add("GridColor", '#' + getGridColor().toHexString());
        if (isShowGridX()) e.add("ShowGridX", true);
        if (isShowGridY()) e.add("ShowGridY", true);

        // Archive RowHeight
        if (isRowHeightSet()) e.add(RowHeight_Prop, getRowHeight());
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
        if (anElement.hasAttribute(ShowHeader_Prop))
            setShowHeader(anElement.getAttributeBooleanValue(ShowHeader_Prop));

        // Unarchive GridColor, ShowLinesX, ShowLinesY
        if (anElement.hasAttribute("GridColor"))
            setGridColor(new Color(anElement.getAttributeValue("GridColor")));
        setShowGridX(anElement.getAttributeBoolValue("ShowGridX", false));
        setShowGridY(anElement.getAttributeBoolValue("ShowGridY", false));

        // Unarchive RowHeight
        if (anElement.hasAttribute(RowHeight_Prop))
            setRowHeight(anElement.getAttributeIntValue(RowHeight_Prop));
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (int i=0, iMax=getColCount(); i<iMax; i++) { TableCol child = getCol(i);
            anElement.add(anArchiver.toXML(child, this)); }
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive as child views
        for (int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
            Class cls = anArchiver.getClass(childXML.getName());
            if (cls!=null && TableCol.class.isAssignableFrom(cls)) {
                TableCol col = (TableCol)anArchiver.fromXML(childXML, this);
                addCol(col);
            }
        }
    }
}