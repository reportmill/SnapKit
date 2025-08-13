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
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropSet;
import snap.util.*;

/**
 * A View to show a list of items.
 */
public class ListView <T> extends ParentView implements Selectable<T> {

    // The items
    protected PickList <T> _items;

    // The row height
    private double _rowHeight;

    // The cell padding
    private Insets _cellPad = getCellPaddingDefault();

    // The function to format text
    private Function <T,String> _itemTextFunc;

    // The Cell Configure method
    private Consumer <ListCell<T>> _cellConf;

    // A simple alternate way to set item text using Key
    private String _itemKey;

    // The background color for alternating rows
    private Color _altRowColor;

    // Whether list distinguishes item under the mouse
    private boolean _targeting;

    // The index of the item currently being targeted
    private int _targetedIndex = -1;

    // Whether list is editable
    private boolean _editable;

    // The list cell that is currently being edited
    private ListCell<T> _editingCell;

    // The index of first visible cell
    private int _cellStart = -1;

    // The index of last visible cell
    private int _cellEnd;

    // Set of items that need to be updated
    private final Set <T> _updateItems = new HashSet<>();

    // Value of cell width/height
    private double _sampleWidth = -1, _sampleHeight = -1;

    // The PropChangeListener to handle PickList selection change
    private PropChangeListener _pickListPropChangeLsnr;

    // A helper object to handle list selection
    private ListViewSelector _selector;

    // Whether list needs to scroll selection to visible after next layout or show
    private boolean _needsScrollSelToVisible;

    // Shared CellPadding default
    public static final Insets CELL_PAD_DEFAULT = new Insets(2);

    // Constants for properties
    public static final String RowHeight_Prop = "RowHeight";
    public static final String CellPadding_Prop = "CellPadding";
    public static final String Editable_Prop = "Editable";
    public static final String ItemKey_Prop = "ItemKey";
    public static final String Sel_Prop = PickList.Sel_Prop;
    public static final String EditingCell_Prop = "EditingCell";

    /**
     * Constructor.
     */
    public ListView()
    {
        super();
        _altRowColor = ViewTheme.get().getContentAltColor();
        _pickListPropChangeLsnr = this::handlePickListPropChange;
        _selector = new ListViewSelector(this);

        // Events
        setActionable(true);
        setFocusable(true);
        setFocusWhenPressed(true);
        setOverflow(Overflow.Scroll);
        enableEvents(MousePress, MouseDrag, MouseRelease);

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
    @Override
    public List <T> getItems()  { return _items; }

    /**
     * Sets the items.
     */
    @Override
    public void setItems(List<T> theItems)
    {
        if (equalsItems(theItems)) return;
        _items.setAll(theItems);
    }

    /**
     * Sets the underlying picklist.
     */
    protected void setPickList(PickList<T> pickList)
    {
        // Remove old PickList
        if (_items != null)
            _items.removePropChangeListener(_pickListPropChangeLsnr);

        // Set New one
        _items = pickList;

        // Configure for new PickList
        _items.addPropChangeListener(_pickListPropChangeLsnr);
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
    @Override
    public int getSelIndex()  { return _items.getSelIndex(); }

    /**
     * Sets the selected index.
     */
    @Override
    public void setSelIndex(int anIndex)  { _items.setSelIndex(anIndex); }

    /**
     * Returns the selected indexes.
     */
    public int[] getSelIndexes()  { return _items.getSelIndexes(); }

    /**
     * Sets the selected index.
     */
    public void setSelIndexes(int[] theIndexes)  { _items.setSelIndexes(theIndexes); }

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
     * Returns the selected items.
     */
    public List<T> getSelItems()  { return _items.getSelItems(); }

    /**
     * Sets the selected items.
     */
    public void setSelItems(List<T> theItems)  { _items.setSelItems(theItems); }

    /**
     * Selects up in the list.
     */
    public void selectUp()  { _items.selectUp(); }

    /**
     * Selects up in the list.
     */
    public void selectDown()  { _items.selectDown(); }

    /**
     * Handle enter action.
     */
    public void processEnterAction(ViewEvent anEvent)
    {
        if (anEvent.isShiftDown())
            return;
        fireActionEvent(anEvent);
        anEvent.consume();
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
        if (_rowHeight > 0) return _rowHeight;
        if (_sampleHeight < 0)
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
        if (aPad == null)
            aPad = getCellPaddingDefault();
        if (aPad.equals(_cellPad)) return;
        firePropChange(CellPadding_Prop, _cellPad, _cellPad = aPad);
        relayout();
        relayoutParent();
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
        double rowHeight = getRowHeight();
        int index = (int) (aY / rowHeight);
        int lastIndex = getItemCount() - 1;
        return Math.min(index, lastIndex);
    }

    /**
     * Returns function for determining text for an item.
     */
    public Function <T,String> getItemTextFunction()  { return _itemTextFunc; }

    /**
     * Sets function for determining text for an item.
     */
    public void setItemTextFunction(Function <T,String> aFunc)
    {
        if (aFunc == getItemTextFunction()) return;
        _itemTextFunc = aFunc;
    }

    /**
     * Returns method to configure list cells.
     */
    public Consumer<ListCell<T>> getCellConfigure()  { return _cellConf; }

    /**
     * Sets method to configure list cells.
     */
    public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _cellConf = aCC; }

    /**
     * Returns the ItemKey (a simple alternate way to set item text using KeyChain).
     */
    public String getItemKey()  { return _itemKey; }

    /**
     * Sets the ItemKey (a simple alternate way to set item text using KeyChain).
     */
    public void setItemKey(String aKey)
    {
        String old = _itemKey; _itemKey = aKey;
        setItemTextFunction(itm -> Convert.stringValue(KeyChain.getValue(itm, _itemKey)));
        firePropChange(ItemKey_Prop, old, _itemKey);
    }

    /**
     * Returns the background color for alternating rows.
     */
    public Color getAltRowColor()  { return _altRowColor; }

    /**
     * Sets the background color for alternating rows.
     */
    public void setAltRowColor(Color aColor)
    {
        if (Objects.equals(aColor, getAltRowColor())) return;
        _altRowColor = aColor;
    }

    /**
     * Returns whether list shows visual cue for item under the mouse.
     */
    public boolean isTargeting()  { return _targeting; }

    /**
     * Sets whether list shows visual cue for item under the mouse.
     */
    public void setTargeting(boolean aValue)
    {
        if (aValue == _targeting) return;
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
        if (anIndex == _targetedIndex) return;
        updateIndex(_targetedIndex);
        _targetedIndex = anIndex;
        updateIndex(_targetedIndex);
    }

    /**
     * Called to update item in list that has changed.
     */
    public void updateItem(T anItem)
    {
        // Sync while adding to UpdateItems
        synchronized (_updateItems) {
            _updateItems.add(anItem);
        }

        // Relayout
        relayout();
    }

    /**
     * Called to update all visible items in list.
     */
    public void updateItems()
    {
        // Add items from all visible/existing cells
        ViewList children = getChildren();
        for (View child : children) {
            ListCell<T> cell = child instanceof ListCell ? (ListCell<T>) child : null;
            T item = cell != null ? cell.getItem() : null;
            if (item != null)
                updateItem(item);
        }
    }

    /**
     * Called to update items in list that have changed.
     */
    public void updateItems(T[] theItems)
    {
        for (T item : theItems)
            updateItem(item);
    }

    /**
     * Called to update items in the list that have changed, by index.
     */
    public void updateIndex(int anIndex)
    {
        T item = anIndex >= 0 && anIndex < getItemCount() ? getItem(anIndex) : null;
        if (item != null)
            updateItem(item);
    }

    /**
     * Updates item at index (required to be in visible range).
     */
    protected void updateCellAt(int anIndex)
    {
        int cellIndex = anIndex - _cellStart;
        ListCell<T> cell = createCell(anIndex);
        configureCell(cell);
        removeChild(cellIndex);
        addChild(cell, cellIndex);
    }

    /**
     * Returns the cell at given index.
     */
    public ListCell <T> getCell(int anIndex)
    {
        return anIndex < getChildCount() ? (ListCell<T>) getChild(anIndex) : null;
    }

    /**
     * Returns the cell for given Y.
     */
    public ListCell <T> getCellForY(double aY)
    {
        ViewList children = getChildren();

        // Iterate over children
        for (View child : children) {
            if (!(child instanceof ListCell)) continue;
            ListCell<T> cell = (ListCell<T>) child;
            if (aY >= cell.getY() && aY <= cell.getMaxY() && cell.getItem() != null)
                return cell;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the cell at given index.
     */
    public ListCell <T> getCellForRow(int anIndex)
    {
        int cellIndex = anIndex - _cellStart;
        return cellIndex >= 0 && cellIndex < getChildCount() ? (ListCell<T>) getChild(cellIndex) : null;
    }

    /**
     * Returns the cell for selected index/item.
     */
    public ListCell<T> getSelCell()
    {
        int selIndex = getSelIndex();
        return getCellForRow(selIndex);
    }

    /**
     * Returns the bounds for item at index.
     */
    public Rect getItemBounds(int anIndex)
    {
        double rowH = getRowHeight();
        double areaW = getWidth();
        double index = Math.max(anIndex, 0);
        return new Rect(0, index * rowH, areaW, rowH);
    }

    /**
     * Scrolls Selection to visible.
     */
    protected void scrollSelToVisible()
    {
        // If needs layout, register for later
        if (isNeedsLayout()) {
            _needsScrollSelToVisible = true;
            return;
        }

        // Reset NeedsScrollSelToVisible
        _needsScrollSelToVisible = false;

        // Get selection rect. If empty, outset by 1
        int selIndex = getSelIndex();
        Rect scrollBounds = getItemBounds(selIndex);
        if (scrollBounds.isEmpty())
            scrollBounds.inset(-1,-2);
        else scrollBounds.width = 30;

        // If visible bounds empty or fully contains selection rect, just return
        Rect visibleBounds = getVisibleBounds();
        if (visibleBounds.isEmpty() || visibleBounds.contains(scrollBounds))
            return;

        // If totally out of view, add buffer. Then scroll rect to visible
        if (!scrollBounds.intersectsShape(visibleBounds))
            scrollBounds.inset(0,-4 * getRowHeight());
        scrollToVisible(scrollBounds);
    }

    /**
     * Returns the preferred width.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        if (_sampleWidth < 0)
            calcSampleSize();
        return _sampleWidth;
    }

    /**
     * Returns the preferred height.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        double rowH = getRowHeight();
        int itemCount = getItemCount();
        return rowH * itemCount;
    }

    /**
     * Override to layout children with ColView layout.
     */
    protected void layoutImpl()
    {
        // Get size info
        double areaW = getWidth();
        double rowH = getRowHeight();

        // Update CellStart/CellEnd for visible bounds
        Rect visibleBounds = getVisibleBounds();
        _cellStart = (int) Math.max(visibleBounds.y / rowH, 0);
        _cellEnd = (int) (visibleBounds.getMaxY() / rowH);

        // Remove cells before new visible range
        while (getChildCount() > 0 && getCell(0).getRow() < _cellStart)
            removeChild(0);

        // Remove cells after new visible range
        for (int i = getChildCount()-1; i >= 0 && getCell(i).getRow() > _cellEnd; i--)
            removeChild(i);

        // Update cells in visible range
        for (int i = _cellStart; i <= _cellEnd; i++) {

            // Get cell index for item
            int cellIndex = i - _cellStart;

            // If row cell already set, update it
            if (cellIndex < getChildCount()) {

                // Get item and cell for row
                T item = i < getItemCount() ? getItem(i) : null;
                ListCell<T> cell = getCell(cellIndex);

                // If item index less than cell item index, create/add cell
                if (i < cell.getRow()) {
                    ListCell<T> cell2 = createCell(i);
                    addChild(cell2, cellIndex);
                    configureCell(cell2);
                    cell.setBounds(0,i * rowH, areaW, rowH);
                    cell.layout();
                }

                // Otherwise, if cell isn't point to item or registered for update, update cell
                else if (item != cell.getItem() || _updateItems.contains(item))
                    updateCellAt(i);
            }

            // Otherwise create, configure and add
            else {
                ListCell<T> cell = createCell(i);
                addChild(cell);
                configureCell(cell);
                cell.setBounds(0,i * rowH, areaW, rowH);
                cell.layout();
            }
        }

        // Clear UpdateItems
        synchronized (_updateItems) {
            _updateItems.clear();
        }

        // Do real layout
        ColViewProxy<?> viewProxy = getViewProxy();
        viewProxy.layoutView();

        // If NeedsScrollSelToVisible, send later
        if (_needsScrollSelToVisible)
            runLater(this::scrollSelToVisible);

        // Check wants ScrollView
        if (getOverflow() == Overflow.Scroll)
            ViewUtils.checkWantsScrollView(this);
    }

    /**
     * Returns the ViewProxy to do layout.
     */
    protected ColViewProxy<?> getViewProxy()
    {
        // Create proxy
        ColViewProxy<?> viewProxy = new ColViewProxy<>(this);
        viewProxy.setFillWidth(true);

        // Set padding.top to include space for hidden rows at top
        double insTop = _cellStart * getRowHeight();
        viewProxy.setPadding(Insets.add(viewProxy.getPadding(), insTop, 0, 0, 0));
        return viewProxy;
    }

    /**
     * Creates a cell for item at index.
     */
    protected ListCell<T> createCell(int anIndex)
    {
        T item = anIndex >= 0 && anIndex < getItemCount() ? getItem(anIndex) : null;
        ListCell<T> cell = new ListCell<>(this, item, anIndex, getColIndex(), isSelIndex(anIndex));
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
        Consumer<ListCell<T>> cconf = getCellConfigure();
        if (cconf != null)
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
    protected void configureCellText(ListCell<T> aCell)
    {
        // Get cell item
        T item = aCell.getItem();

        // Get String for cell item
        String text = null;
        if (_itemTextFunc != null)
            text = item != null ? _itemTextFunc.apply(item) : null;
        else if (item instanceof String)
            text = (String) item;
        else if (item instanceof Enum)
            text = item.toString();
        else if (item instanceof Number)
            text = item.toString();
        else if (getCellConfigure() == null && item != null)
            text = item.toString();

        // Set cell text
        aCell.setText(text);
    }

    /**
     * Called to configure a cell fill and text color.
     */
    protected void configureCellFills(ListCell <T> aCell)
    {
        // Handle Cell.Selected
        if (aCell.isSelected()) {
            aCell.setFill(ViewUtils.getSelectFill());
            aCell.setTextColor(ViewUtils.getTextSelectedColor());
        }

        // Handle Targeting given cell
        else if (isTargeting() && aCell.getRow() == getTargetedIndex())  {
            aCell.setFill(ViewUtils.getTargetFill());
            aCell.setTextColor(ViewUtils.getTextTargetedColor());
        }

        // Handle alternate rows
        else if (_altRowColor != null && aCell.getRow() % 2 == 0) {
            aCell.setFill(_altRowColor);
            aCell.setTextColor(ViewUtils.getTextColor());
        }

        // Handle normal case
        else {
            aCell.setFill(null);
            aCell.setTextColor(ViewUtils.getTextColor());
        }
    }

    /**
     * Returns text for item.
     */
    public String getText(T anItem)
    {
        // If ItemTextFunc, just apply
        String text;
        if (_itemTextFunc != null)
            text = anItem != null ? _itemTextFunc.apply(anItem) : null;

            // If CellConfigure, create cell and call
        else if (getCellConfigure() != null) {
            Consumer<ListCell<T>> cconf = getCellConfigure();
            ListCell<T> cell = new ListCell<>(this, anItem, 0, 0, false);
            cell.setText(anItem != null ? anItem.toString() : null);
            cconf.accept(cell);
            text = cell.getText();
        }

        // Otherwise just get string
        else text = anItem != null ? anItem.toString() : null;

        // Return text
        return text;
    }

    /**
     * Returns the column index.
     */
    protected int getColIndex()  { return 0; }

    /**
     * Calculates sample width and height from items.
     */
    protected void calcSampleSize()
    {
        // Create/configure sample cell
        ListCell<T> cell = new ListCell<>(this, null, 0, getColIndex(), false);
        cell.setFont(getFont());
        cell.setPadding(getCellPadding());

        // Iterate over cells (max 30) to get reasonable sample size
        int count = Math.min(getItemCount(), 30);
        for (int i = 0; i < count; i++) {

            // Reset cell
            cell._item = i < getItemCount() ? getItem(i) : null;
            cell._row = i;
            for (int j = cell.getChildCount() - 1; j >= 0; j--) {
                View child = cell.getChild(j);
                if (child != cell._stringView && child != cell._graphic)
                    cell.removeChild(j);
            }

            // Configure (make sure text is not empty)
            configureCell(cell);
            if (cell.getText() == null || cell.getText().isEmpty())
                cell.setText("X");

            // Get Sample Width/Height
            double cellPrefW = cell.getPrefWidth();
            double cellPrefH = cell.getPrefHeight();
            _sampleWidth = Math.max(_sampleWidth, cellPrefW);
            _sampleHeight = Math.max(_sampleHeight, cellPrefH);
        }

        // Make sure there's some minimum
        if (_sampleWidth < 0)
            _sampleWidth = 100;
        if (_sampleHeight < 0)
            _sampleHeight = Math.ceil(getFont().getLineHeight() + 4);
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
        if (aValue == isEditable()) return;
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
        if (aCell == getEditingCell()) return;

        // If clearing, request focus
        if (aCell == null && getWindow() != null)
            getWindow().requestFocus(null);

        // Fire PropChange
        firePropChange(EditingCell_Prop, _editingCell, _editingCell = aCell);
    }

    /**
     * Edit cell.
     */
    public void editCell(ListCell<T> aCell)
    {
        // If not possible, complain and return
        if (aCell == null) {
            ViewUtils.beep();
            return;
        }

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
     * Called when PickList fires prop change.
     */
    private void handlePickListPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        // Handle Sel_Prop: Get array of changed indexes and update
        if (propName == PickList.Sel_Prop) {
            ListSel sel1 = (ListSel) aPC.getOldValue();
            ListSel sel2 = (ListSel) aPC.getNewValue();
            int[] changed = ListSel.getChangedIndexes(sel1, sel2);
            for (int i : changed)
                updateIndex(i);

            // Repackage and forward
            firePropChange(Sel_Prop, aPC.getOldValue(), aPC.getNewValue());

            // Scroll selection to visible
            scrollSelToVisible();
        }

        // Handle Items_Prop
        else if (propName == PickList.Item_Prop) {
            relayout();
            relayoutParent();
            repaint();
            _sampleWidth = _sampleHeight = -1;
        }
    }

    /**
     * Override to reset cells.
     */
    @Override
    public void setY(double aValue)
    {
        if (aValue == getY()) return;
        super.setY(aValue);
        relayout();
    }

    /**
     * Override to reset cells.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        relayout();
    }

    /**
     * Override to see if paint exposes missing cells. If so, request layout.
     * Should only happen under rare circumstances, like when a parent Scroller grows.
     */
    @Override
    public void paintAll(Painter aPntr)
    {
        // Do normal version
        super.paintAll(aPntr);

        // If paint bounds larger visible cell bounds, register for layout (delayed)
        Rect clipBounds = aPntr.getClipBounds();
        double rowH = getRowHeight();
        int cellStart = (int) Math.max(clipBounds.y / rowH, 0);
        int cellEnd = (int) (clipBounds.getMaxY() / rowH);
        if (cellStart < _cellStart || cellEnd > _cellEnd)
            getEnv().runLater(() -> relayout());
    }

    /**
     * Override to return text for currently selected item.
     */
    @Override
    public String getText()
    {
        T item = getSelItem();
        return item != null ? getText(item) : null;
    }

    /**
     * Override to set the given text by matching it to existing item text.
     */
    @Override
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
        for (T item : getItems()) {
            String str = getText(item);
            if (Objects.equals(aString, str))
                return item;
        }

        // If items are primitive type, get primitive type for item string. Return matching item
        T item0 = getItemCount() > 0 ? getItem(0) : null;
        T itemX = null;
        if (item0 instanceof String) itemX = (T) aString;
        else if (item0 instanceof Integer) itemX = (T) Convert.getInteger(aString);
        else if (item0 instanceof Float) itemX = (T) Convert.getFloat(aString);
        else if (item0 instanceof Double) itemX = (T) Convert.getDouble(aString);
        int index = itemX != null ? getItems().indexOf(itemX) : -1;
        if (index >= 0)
            return getItem(index);

        // Return null
        return null;
    }

    /**
     * Override to maybe scroll sel to visible.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        if (aValue == isShowing()) return;
        if (aValue && _needsScrollSelToVisible)
            scrollSelToVisible();
        super.setShowing(aValue);
    }

    /**
     * Returns whether given items are equal to set items.
     */
    protected boolean equalsItems(List<T> theItems)
    {
        return ListUtils.equalsId(theItems, _items) || Objects.equals(theItems, _items);
    }

    /**
     * Override to reset AltRowColor
     */
    @Override
    protected void themeChanged(ViewTheme oldTheme, ViewTheme newTheme)
    {
        super.themeChanged(oldTheme, newTheme);
        if (Objects.equals(_altRowColor, oldTheme.getContentAltColor()))
            _altRowColor = newTheme.getContentAltColor();
        removeChildren();
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return SelItem_Prop; }

    /**
     * Override to support RowHeight property.
     */
    @Override
    public boolean isPropDefault(String propName)
    {
        if (propName == RowHeight_Prop)
            return !isRowHeightSet();
        return super.isPropDefault(propName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // RowHeight, ItemKey
        aPropSet.addPropNamed(RowHeight_Prop, double.class, 0d);
        aPropSet.addPropNamed(ItemKey_Prop, String.class, EMPTY_OBJECT);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // RowHeight, ItemKey
            case RowHeight_Prop -> getRowHeight();
            case ItemKey_Prop -> getItemKey();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // RowHeight, ItemKey
            case RowHeight_Prop -> setRowHeight(Convert.doubleValue(aValue));
            case ItemKey_Prop -> setItemKey(Convert.stringValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
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
        if (!isPropDefault(ItemKey_Prop)) e.add(ItemKey_Prop, getItemKey());

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