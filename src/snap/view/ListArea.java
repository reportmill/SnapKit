/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

/**
 * A View to show a list of items and act as basis of ListView.
 * 
 * To display custom text in list, simply call list.setItemTextFunction(itm -> itm.getName());
 * 
 * To custom configure list cell, simply call list.setCellConfigure(cell -> cell.setImage(img));
 */
public class ListArea <T> extends ParentView implements Selectable<T> {

    // The items
    protected PickList <T>  _items;
    
    // The row height
    private double  _rowHeight;
    
    // The cell padding
    private Insets  _cellPad = getCellPaddingDefault();
    
    // The function to format text
    private Function <T,String>  _itemTextFunc;
    
    // The Cell Configure method
    private Consumer <ListCell<T>>  _cellConf;
    
    // A simple alternate way to set ListArea item text using Key
    private String  _itemKey;
    
    // The paint for alternating cells
    private Paint  _altPaint = ALT_GRAY;
    
    // Whether list distinguishes item under the mouse
    private boolean  _targeting;
    
    // The index of the item currently being targeted
    private int  _targetedIndex = -1;
    
    // Whether list is editable
    private boolean  _editable;

    // The list cell that is currently being edited
    private ListCell<T>  _editingCell;

    // The index of first visible cell
    private int  _cellStart = -1;

    // The index of last visible cell
    private int _cellEnd;

    // Set of items that need to be updated
    private Set <T>  _updateItems = new HashSet<>();
    
    // Value of cell width/height
    private double  _sampleWidth = -1, _sampleHeight = -1;
    
    // The PropChangeListener to handle PickList selection change
    private PropChangeListener  _itemsLsnr = pc -> pickListPropChange(pc);

    // A helper object to handle list selection
    private ListAreaSelector _selector = new ListAreaSelector(this);
    
    // Shared CellPadding default
    public static final Insets  CELL_PAD_DEFAULT = new Insets(2);
    
    // Shared constants for colors
    private static Paint ALT_GRAY = Color.get("#F8F8F8");
    private static Paint SEL_FILL = ViewUtils.getSelectFill();
    private static Paint SEL_TEXT_FILL = ViewUtils.getSelectTextFill();
    private static Paint TARG_FILL = ViewUtils.getTargetFill();
    private static Paint TARG_TEXT_FILL = ViewUtils.getTargetTextFill();
    
    // Constants for properties
    public static final String CellPadding_Prop = "CellPadding";
    public static final String Editable_Prop = "Editable";
    public static final String EditingCell_Prop = "EditingCell";
    public static final String ItemKey_Prop = "ItemKey";
    public static final String RowHeight_Prop = "RowHeight";
    public static final String Sel_Prop = PickList.Sel_Prop;

    /**
     * Creates a new ListArea.
     */
    public ListArea()
    {
        // Basic
        super();
        setFill(Color.WHITE);

        // Events
        setFocusable(true);
        setFocusWhenPressed(true);
        enableEvents(MousePress, MouseDrag, MouseRelease, KeyPress, Action);

        // Create/set PickList
        setPickList(new PickList<>());
    }

    /**
     * Returns the number of items.
     */
    public int getItemCount()  { return getItems().size(); }

    /**
     * Returns the individual item at index.
     */
    public T getItem(int anIndex)  { return getItems().get(anIndex); }

    /**
     * Returns the items.
     */
    public List <T> getItems()  { return _items; }

    /**
     * Sets the items.
     */
    public void setItems(List <T> theItems)
    {
        if (equalsItems(theItems)) return;
        _items.setAll(theItems);
    }

    /**
     * Sets the items.
     */
    public void setItems(T ... theItems)
    {
        List<T> items = theItems!=null ? Arrays.asList(theItems) : null;
        setItems(items);
    }

    /**
     * Sets the underlying picklist.
     */
    protected void setPickList(PickList <T> aPL)
    {
        // Remove old PickList
        if (_items!=null) _items.removePropChangeListener(_itemsLsnr);

        // Set New one
        _items = aPL;

        // Configure for new PickList
        _items.addPropChangeListener(_itemsLsnr);
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
     * Returns the List selection.
     */
    public ListSel getSel()  { return _items.getSel(); }

    /**
     * Sets the List selection.
     */
    public void setSel(ListSel aSel)
    {
        _items.setSel(aSel);
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
     * Returns the selected indexes.
     */
    public int[] getSelIndexes()  { return _items.getSelIndexes(); }

    /**
     * Sets the selected index.
     */
    public void setSelIndexes(int ... theIndexes)  { _items.setSelIndexes(theIndexes); }

    /**
     * Clears the selection.
     */
    public void clearSel()  { _items.clearSel(); }

    /**
     * Returns whether given index is selected index.
     */
    public boolean isSelIndex(int anIndex)  { return _items.isSelIndex(anIndex); }

    /**
     * Returns the selected item.
     */
    public T getSelItem()  { return _items.getSelItem(); }

    /**
     * Sets the selected index.
     */
    public void setSelItem(T anItem)  { _items.setSelItem(anItem); }

    /**
     * Returns the selected itemes.
     */
    public Object[] getSelItems()  { return _items.getSelItems(); }

    /**
     * Returns the selected itemes.
     */
    public <T> T[] getSelItems(Class <T> aClass)  { return _items.getSelItems(aClass); }

    /**
     * Sets the selected items.
     */
    public void setSelItems(T ... theItems)  { _items.setSelItems(theItems); }

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
    protected void pickListPropChange(PropChange aPC)
    {
        // If not SelIndex, just return
        String propName = aPC.getPropName();

        // Handle Sel_Prop: Get array of changed indexes and update
        if (propName==PickList.Sel_Prop) {
            ListSel sel1 = (ListSel)aPC.getOldValue();
            ListSel sel2 = (ListSel)aPC.getNewValue();
            int changed[] = ListSel.getChangedIndexes(sel1, sel2);
            for (int i : changed)
                updateIndex(i);

            // Repackage and forward
            firePropChange(Sel_Prop, aPC.getOldValue(), aPC.getNewValue());
        }

        // Handle Items_Prop
        else if (propName==PickList.Item_Prop) {
            relayout(); relayoutParent(); repaint();
            _sampleWidth = _sampleHeight = -1;
        }

        // Scroll selection to visible
        if (isShowing())
            scrollSelToVisible();
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
        if (_rowHeight>0) return _rowHeight;
        if (_sampleHeight<0)
            calcSampleSize();
        return _sampleHeight;
    }

    /**
     * Sets the row height.
     */
    public void setRowHeight(double aValue)
    {
        _rowHeight = aValue;
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
        if (aPad==null) aPad = getCellPaddingDefault();
        if (aPad.equals(_cellPad)) return;
        firePropChange(CellPadding_Prop, _cellPad, _cellPad=aPad);
        relayout(); relayoutParent();
    }

    /**
     * Returns the default cell padding.
     */
    public Insets getCellPaddingDefault()  { return CELL_PAD_DEFAULT; }

    /**
     * Returns the row index at given Y location.
     */
    public int getRowIndexForY(double aY)
    {
        int index = (int) (aY/getRowHeight());
        return Math.min(index, getItemCount()-1);
    }

    /**
     * Returns function for deteriming text for an item.
     */
    public Function <T,String> getItemTextFunction()  { return _itemTextFunc; }

    /**
     * Sets function for deteriming text for an item.
     */
    public void setItemTextFunction(Function <T,String> aFunc)  { _itemTextFunc = aFunc; }

    /**
     * Returns method to configure list cells.
     */
    public Consumer<ListCell<T>> getCellConfigure()  { return _cellConf; }

    /**
     * Sets method to configure list cells.
     */
    public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _cellConf = aCC; }

    /**
     * Returns the ItemKey (a simple alternate way to set ListArea item text using KeyChain).
     */
    public String getItemKey()  { return _itemKey; }

    /**
     * Sets the ItemKey (a simple alternate way to set ListArea item text using KeyChain).
     */
    public void setItemKey(String aKey)
    {
        String old = _itemKey; _itemKey = aKey;
        setItemTextFunction(itm -> SnapUtils.stringValue(KeyChain.getValue(itm, _itemKey)));
        firePropChange(ItemKey_Prop, old, _itemKey);
    }

    /**
     * Returns the paint for alternating cells.
     */
    public Paint getAltPaint()  { return _altPaint; }

    /**
     * Sets the paint for alternating cells.
     */
    public void setAltPaint(Paint aPaint)  { _altPaint = aPaint; }

    /**
     * Returns whether list shows visual cue for item under the mouse.
     */
    public boolean isTargeting()  { return _targeting; }

    /**
     * Sets whether list shows visual cue for item under the mouse.
     */
    public void setTargeting(boolean aValue)
    {
        if (aValue==_targeting) return;
        _targeting = aValue;
        if (_targeting)
            enableEvents(MouseMove, MouseExit);
        else disableEvents(MouseMove, MouseExit);
    }

    /**
     * Returns the index of the currently targeted cell.
     */
    public int getTargetedIndex()  { return _targetedIndex; }

    /**
     * Sets the index of the currently targeted cell.
     */
    protected void setTargetedIndex(int anIndex)
    {
        if (anIndex==_targetedIndex) return;
        updateIndex(_targetedIndex);
        _targetedIndex = anIndex;
        updateIndex(_targetedIndex);
    }

    /**
     * Called to update items in list that have changed.
     */
    public void updateItems(T ... theItems)
    {
        synchronized (_updateItems) {
            if (theItems!=null && theItems.length>0)
                Collections.addAll(_updateItems, theItems);
            else _updateItems.addAll(getItems());
        }
        relayout();
    }

    /**
     * Called to update items in the list that have changed, by index.
     */
    public void updateIndex(int anIndex)
    {
        T item = anIndex>=0 && anIndex<getItemCount() ? getItem(anIndex) : null;
        if (item!=null)
            updateItems(item);
    }

    /**
     * Updates item at index (required to be in visible range).
     */
    protected void updateCellAt(int anIndex)
    {
        int cindex = anIndex - _cellStart;
        ListCell cell = createCell(anIndex);
        ListCell oldCell = getCell(cindex);
        configureCell(cell);
        cell.setBounds(oldCell.getBounds());
        cell.layout();
        removeChild(cindex); addChild(cell, cindex);
    }

    /**
     * Returns the cell at given index.
     */
    public ListCell <T> getCell(int anIndex)
    {
        return anIndex<getChildCount() ? (ListCell)getChild(anIndex) : null;
    }

    /**
     * Returns the cell for given Y.
     */
    public ListCell <T> getCellForY(double aY)
    {
        for (View cell : getChildren()) {
            if (!(cell instanceof ListCell)) continue;
            if (aY>=cell.getY() && aY<=cell.getMaxY())
                return (ListCell)cell;
        }
        return null;
    }

    /**
     * Returns the cell at given index.
     */
    public ListCell <T> getCellForRow(int anIndex)
    {
        int cindex = anIndex - _cellStart;
        return cindex>=0 && cindex<getChildCount() ? (ListCell)getChild(cindex) : null;
    }

    /**
     * Returns the cell for selected index/item.
     */
    public ListCell<T> getSelCell()
    {
        int ind = getSelIndex();
        return getCellForRow(ind);
    }

    /**
     * Returns the bounds for item at index.
     */
    public Rect getItemBounds(int anIndex)
    {
        double rh = getRowHeight(), width = getWidth(), index = Math.max(anIndex,0);
        return new Rect(0, index*rh, width, rh);
    }

    /**
     * Scrolls Selection to visible.
     */
    protected void scrollSelToVisible()
    {
        // If needs layout, come back later
        if (isNeedsLayout() || ViewUtils.isMetaDown()) {
            getEnv().runLaterOnce("scrollSelToVisible",() -> scrollSelToVisible());
            return;
        }

        // Get selection rect. If empty, outset by 1
        Rect srect = getItemBounds(getSelIndex());
        if (srect.isEmpty()) srect.inset(-1,-2);
        else srect.width = 30;

        // If visible rect not set or empty or fully contains selection rect, just return
        Rect vrect = getClipAllBounds(); if (vrect==null || vrect.isEmpty()) return;
        if (vrect.contains(srect)) return;

        // If totally out of view, add buffer. Then scroll rect to visible
        if (!srect.intersects(vrect))
            srect.inset(0,-4*getRowHeight());
        scrollToVisible(srect);
        repaint();
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        if (_sampleWidth<0) calcSampleSize();
        return _sampleWidth;
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return getRowHeight()*getItemCount();
    }

    /**
     * Override to layout children with VBox layout.
     */
    protected void layoutImpl()
    {
        // Update CellStart/CellEnd for current ClipBounds
        Rect clip = getClipAllBounds();
        if (clip==null) clip = getBoundsLocal();
        double rh = getRowHeight();

        // Update CellStart/CellEnd for current ClipBounds
        _cellStart = (int) Math.max(clip.getY()/rh,0);
        _cellEnd = (int) (clip.getMaxY()/rh);

        // Remove cells before and/or after new visible range
        while (getChildCount()>0 && getCell(0).getRow()<_cellStart)
            removeChild(0);
        for (int i=getChildCount()-1; i>=0 && getCell(i).getRow()>_cellEnd; i--)
            removeChild(i);

        // Update cells in visible range
        for (int i=_cellStart,cindex=0;i<=_cellEnd;i++,cindex++) {

            // If row cell already set, update it
            if (cindex<getChildCount()) {
                T item = i<getItemCount() ? getItem(i) : null;
                ListCell cell = getCell(cindex);
                if (i<cell.getRow()) {
                    ListCell cell2 = createCell(i);
                    addChild(cell2,cindex);
                    configureCell(cell2);
                    cell.setBounds(0,i*rh, getWidth(), rh);
                    cell.layout();
                }
                else if (item!=cell.getItem())
                    updateCellAt(i);
            }

            // Otherwise create, configure and add
            else {
                ListCell cell = createCell(i);
                addChild(cell);
                configureCell(cell);
                cell.setBounds(0,i*rh, getWidth(), rh);
                cell.layout();
            }
        }

        // Get copy of items and clear
        T items[] = null; synchronized (_updateItems) {
            items = (T[])_updateItems.toArray();
            _updateItems.clear();
        }

        // Update items
        for (T item : items) {
            int index = getItems().indexOf(item);
            if (index>=_cellStart && index<=_cellEnd)
                updateCellAt(index);
        }

        // Do real layout
        ColView.layout(this, true);
    }

    /**
     * Creates a cell for item at index.
     */
    protected ListCell createCell(int anIndex)
    {
        T item = anIndex>=0 && anIndex<getItemCount() ? getItem(anIndex) : null;
        ListCell cell = new ListCell(this, item, anIndex, getColIndex(), isSelIndex(anIndex));
        cell.setPadding(getCellPadding());
        cell.setPrefHeight(getRowHeight());
        return cell;
    }

    /**
     * Called to configure a cell.
     */
    protected void configureCell(ListCell <T> aCell)
    {
        // Do basic cell configure
        cellConfigureBasic(aCell);

        // If cell configure set, call it
        Consumer cconf = getCellConfigure();
        if (cconf!=null)
            cconf.accept(aCell);
    }

    /**
     * Called to do standard cell configure.
     */
    public void cellConfigureBasic(ListCell<T> aCell)
    {
        configureCellText(aCell);
        configureCellFills(aCell);
    }

    /**
     * Called to configure a cell text.
     */
    protected void configureCellText(ListCell <T> aCell)
    {
        T item = aCell.getItem();
        String text = null;
        if (_itemTextFunc!=null)
            text = item!=null ? _itemTextFunc.apply(item) : null;
        else if (item instanceof String)
            text = (String)item;
        else if (item instanceof Enum)
            text = item.toString();
        else if (item instanceof Number)
            text = item.toString();
        else if (getCellConfigure()==null && item!=null)
            text = item.toString();
        aCell.setText(text);
    }

    /**
     * Called to configure a cell fill and text fill.
     */
    protected void configureCellFills(ListCell <T> aCell)
    {
        if (aCell.isSelected()) {
            aCell.setFill(SEL_FILL); aCell.setTextFill(SEL_TEXT_FILL);
        }
        else if (isTargeting() && aCell.getRow()==getTargetedIndex())  {
            aCell.setFill(TARG_FILL); aCell.setTextFill(TARG_TEXT_FILL);
        }
        else if (aCell.getRow()%2==0) {
            aCell.setFill(_altPaint); aCell.setTextFill(Color.BLACK);
        }
        else {
            aCell.setFill(null); aCell.setTextFill(Color.BLACK);
        }
    }

    /**
     * Returns text for item.
     */
    public String getText(T anItem)
    {
        // If ItemTextFunc, just apply
        String text;
        if (_itemTextFunc!=null)
            text = anItem!=null ? _itemTextFunc.apply(anItem) : null;

        // If CellConfigure, create cell and call
        else if (getCellConfigure()!=null) { Consumer cconf = getCellConfigure();
            ListCell cell = new ListCell(this, anItem, 0, 0, false);
            cell.setText(anItem!=null ? anItem.toString() : null);
            cconf.accept(cell);
            text = cell.getText();
        }

        // Otherwise just get string
        else text = anItem!=null ? anItem.toString() : null;

        // Return text
        return text;
    }

    /**
     * Returns the column index.
     */
    protected int getColIndex()  { return 0; }

    /**
     * Returns the insets.
     */
    public Insets getInsetsAll()
    {
        Insets ins = super.getInsetsAll();
        if (_cellStart>0)
            ins = Insets.add(ins, _cellStart*getRowHeight(), 0, 0, 0);
        return ins;
    }

    /**
     * Override to reset cells.
     */
    public void setY(double aValue)
    {
        if (aValue==getY()) return; super.setY(aValue);
        relayout();
    }

    /**
     * Override to reset cells.
     */
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return; super.setHeight(aValue);
        relayout();
    }

    /**
     * Override to see if paint exposes missing cells. If so, request layout.
     * Should only happen under rare circumstances, like when a Scroller containing ListArea grows.
     */
    public void paintAll(Painter aPntr)
    {
        super.paintAll(aPntr);
        Rect clip = aPntr.getClipBounds(); double rh = getRowHeight();
        int cellStart = (int)Math.max(clip.getY()/rh,0), cellEnd = (int)(clip.getMaxY()/rh);
        if (cellStart<_cellStart || cellEnd>_cellEnd)
            getEnv().runLater(() -> relayout());
    }

    /**
     * Calculates sample width and height from items.
     */
    protected void calcSampleSize()
    {
        // Create/configure sample cell
        ListCell cell = new ListCell(this, null, 0, getColIndex(), false);
        cell.setFont(getFont());
        cell.setPadding(getCellPadding());

        // Iterate over cells (max 30) to get reasonable sample size
        int count = Math.min(getItemCount(), 30);
        for (int i=0; i<count; i++) {

            // Reset cell
            cell._item = i<getItemCount() ? getItem(i) : null;
            cell._row = i;
            for (int j=cell.getChildCount()-1;j>=0;j--) { View child = cell.getChild(j);
                if (child!=cell._strView && child!=cell._graphic)
                    cell.removeChild(j);
            }

            // Configure (make sure text is not empty)
            configureCell(cell);
            if (cell.getText()==null || cell.getText().length()==0)
                cell.setText("X");

            // Get Sample Width/Height
            _sampleWidth = Math.max(_sampleWidth, cell.getPrefWidth());
            _sampleHeight = Math.max(_sampleHeight, cell.getPrefHeight());
        }

        // Make sure there's some minimum
        if (_sampleWidth<0)
            _sampleWidth = 100;
        if (_sampleHeight<0)
            _sampleHeight = Math.ceil(getFont().getLineHeight()+4);
    }

    /**
     * Returns whether list cells are editable.
     */
    public boolean isEditable()  { return _editable; }

    /**
     * Sets whether list cells are editable.
     */
    public void setEditable(boolean aValue)
    {
        if (aValue==isEditable()) return;
        firePropChange(Editable_Prop, _editable, _editable = aValue);
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
        // If already set, just return
        if (aCell==getEditingCell()) return;

        // If clearing, request focus
        if (aCell==null && getWindow()!=null)
            getWindow().requestFocus(null);

        // Fire PropChange
        firePropChange(EditingCell_Prop, _editingCell, _editingCell = aCell);
    }

    /**
     * Edit cell.
     */
    public void editCell(ListCell aCell)
    {
        // If not possible, complain and return
        if (aCell==null) { ViewUtils.beep(); return; }

        // Set editing
        aCell.setEditing(true);
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
     * Process events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        _selector.processEvent(anEvent);
    }

    /**
     * Override to return white.
     */
    public Paint getDefaultFill()  { return Color.WHITE; }

    /**
     * Override to return text for currently selected item.
     */
    public String getText()
    {
        T item = getSelItem();
        return item!=null ? getText(item) : null;
    }

    /**
     * Override to set the given text in this ListArea by matching it to existing item text.
     */
    public void setText(String aString)
    {
        T item = getItemForText(aString);
        setSelItem(item);
    }

    /**
     * Return list item that matches string.
     */
    public T getItemForText(String aString)
    {
        // Iterate over items and if item text is exact match for string, return it
        for (T item : getItems()) { String str = getText(item);
            if (SnapUtils.equals(aString, str))
                return item; }

        // If items are primitive type, get primitive type for item string. Return matching item
        T item0 = getItemCount()>0 ? getItem(0) : null, itemX = null;
        if (item0 instanceof String) itemX = (T)aString;
        else if (item0 instanceof Integer) itemX = (T)SnapUtils.getInteger(aString);
        else if (item0 instanceof Float) itemX = (T)SnapUtils.getFloat(aString);
        else if (item0 instanceof Double) itemX = (T)SnapUtils.getDouble(aString);
        int index = itemX!=null ? getItems().indexOf(itemX) : -1;
        if (index>=0)
            return getItem(index);

        // Return null
        return null;
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()
    {
        return getBinding(SelIndex_Prop)!=null ? SelIndex_Prop : SelItem_Prop;
    }

    /**
     * Returns whether given items are equal to set items.
     */
    protected boolean equalsItems(List theItems)
    {
        return ListUtils.equalsId(theItems, _items) || SnapUtils.equals(theItems,_items);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive RowHeight, ItemKey
        if (isRowHeightSet()) e.add(RowHeight_Prop, getRowHeight());
        if (getItemKey()!=null) e.add(ItemKey_Prop, getItemKey());

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive RowHeight, ItemKey
        if (anElement.hasAttribute(RowHeight_Prop))
            setRowHeight(anElement.getAttributeIntValue(RowHeight_Prop));
        if (anElement.hasAttribute(ItemKey_Prop))
            setItemKey(anElement.getAttributeValue(ItemKey_Prop));
    }

}