/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;

import snap.geom.Polygon;
import snap.gfx.*;
import snap.props.PropChange;
import snap.util.*;

/**
 * A View subclass to show a list of hierarchical items.
 */
public class TreeView <T> extends ParentView implements Selectable<T> {
    
    // The items
    private PickList <T> _items = new PickList<>();
    
    // The selected column
    private int _selCol;
    
    // The resolver
    private TreeResolver <T> _resolver = new TreeResolver.Adapter<>();
    
    // Row height
    private int _rowHeight = 20;

    // The Preferred number of rows
    private int _prefRowCount = -1;
    
    // The maximum number of rows
    private int _maxRowCount = -1;

    // The Cell Configure method
    private Consumer <ListCell<T>> _cellConf;
    
    // The set of expanded items
    private Set<T> _expanded = new HashSet<>();
    
    // Image for collapsed parent item
    private Image _clpImg;
    
    // Image for expanded parent item
    private Image _expImg;

    // The SplitView to hold columns
    private SplitView _splitView;
    
    // Constants
    private static final Paint DIVIDER_FILL = new Color("#EEEEEE");

    /**
     * Constructor.
     */
    public TreeView()
    {
        super();
        setActionable(true);
        setFocusable(true);
        setFocusWhenPressed(true);
        setFocusPainted(false);
        setOverflow(Overflow.Scroll);

        // Create/configure Columns SplitView
        _splitView = new SplitView();
        _splitView.setBorder(null);
        _splitView.setGrowWidth(true);
        _splitView.setDividerSpan(2);
        addChild(_splitView);

        // Configure SplitView.Divider
        Divider divider = _splitView.getDivider();
        divider.setFill(DIVIDER_FILL);
        divider.setBorder(null);

        // Register PickList to notify when selection changes
        _items.addPropChangeListener(this::handlePickListSelChange);

        // Create/add first column
        TreeCol<T> treeCol = new TreeCol<>();
        addCol(treeCol);
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
     * Returns the preferred number of rows.
     */
    public int getPrefRowCount()  { return _prefRowCount; }

    /**
     * Sets the preferred number of rows.
     */
    public void setPrefRowCount(int aValue)
    {
        _prefRowCount = aValue;
        relayoutParent();
    }

    /**
     * Returns the maximum number of rows.
     */
    public int getMaxRowCount()  { return _maxRowCount; }

    /**
     * Sets the maximum number of rows.
     */
    public void setMaxRowCount(int aValue)
    {
        _maxRowCount = aValue;
        relayoutParent();
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
     * Returns the number of columns.
     */
    public int getColCount()  { return _splitView.getItemCount(); }

    /**
     * Returns the column at given index.
     */
    public TreeCol <T> getCol(int anIndex)  { return (TreeCol<T>) _splitView.getItem(anIndex); }

    /**
     * Returns the column at given index.
     */
    public TreeCol<T>[] getCols()
    {
        List<TreeCol<?>> treeCols = (List<TreeCol<?>>) (List<?>) _splitView.getItems();
        return treeCols.toArray(new TreeCol[0]);
    }

    /**
     * Adds a column.
     */
    public void addCol(TreeCol<T> aCol)  { addCol(aCol,getColCount()); }

    /**
     * Adds a column at index.
     */
    public void addCol(TreeCol<T> aCol, int anIndex)
    {
        // Add TreeCol to SplitView
        aCol.setTree(this);
        _splitView.addItem(aCol, anIndex);

        // Replace column picklist with tableView picklist
        aCol.setPickList(_items);
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return getItems().size(); }

    /**
     * Returns the items.
     */
    @Override
    public List <T> getItems()  { return _items; }

    /**
     * Sets the items.
     */
    @Override
    public void setItems(List <T> theItems)
    {
        List <T> items = getExpandedItems(theItems);
        setItemsImpl(items);
    }

    /**
     * Sets the items.
     */
    protected void setItemsImpl(List <T> theItems)
    {
        // If already set, just return
        if (equalsItems(theItems)) return;

        // Clear items
        T selItem = getSelItem();
        _items.clear();
        _items.addAll(theItems);

        // Iterate over columns and setItems
        for (TreeCol<T> treeCol : getCols())
            treeCol.setItems(theItems);
        setSelItem(selItem);

        // Prune removed items from expanded set
        T[] expanded = (T[]) _expanded.toArray();
        for (T item : expanded)
            if (!_items.contains(item))
                _expanded.remove(item);
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
     * Returns the selected item.
     */
    @Override
    public T getSelItem()  { return _items.getSelItem(); }

    /**
     * Sets the selected index.
     */
    @Override
    public void setSelItem(T anItem)  { _items.setSelItem(anItem); }

    /**
     * Returns the list of expanded items for given items.
     */
    public List <T> getExpandedItems(List <T> theItems)
    {
        List <T> items = theItems;
        for (int i = 0; i < items.size(); i++) {

            // If item not expanded just continue
            T item = items.get(i);
            if (!isItemExpanded(item))
                continue;

            // If we haven't yet created new list, do it now
            if (items == theItems)
                items = new ArrayList<>(items);

            // Remove successive items decended from current item
            for (int j = i + 1, jMax = items.size(); j < jMax; j++) {
                T next = items.get(i + 1);
                if (isItemAncestor(next,item))
                    items.remove(i + 1);
                else break;
            }

            // If item no long parent (could have changed), clear state and continue
            if (!isItemParent(item)) {
                setItemExpanded(item,false);
                continue;
            }

            // Get item children and add after item
            List<T> childItems = getItemChildren(item);
            for (int j = 0; j < childItems.size(); j++)
                items.add(i + j + 1, childItems.get(j));
        }

        // Return items
        return items;
    }

    /**
     * Called to update item that has changed.
     */
    public void updateItem(T anItem)
    {
        for (TreeCol<T> treeCol : getCols())
            treeCol.updateItem(anItem);
    }

    /**
     * Called to update all visible items.
     */
    public void updateItems()
    {
        for (TreeCol<T> treeCol : getCols())
            treeCol.updateItems();
    }

    /**
     * Called to update items that have changed.
     */
    public void updateItems(T[] theItems)
    {
        for (TreeCol<T> treeCol : getCols())
            treeCol.updateItems(theItems);
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
    public void setSelCol(int anIndex)  { _selCol = anIndex; }

    /**
     * Returns the row index at given point.
     */
    public int getRowIndexForY(double aY)
    {
        int rowH = getRowHeight();
        return (int) (aY / rowH);
    }

    /**
     * Expands all tree nodes.
     */
    public void expandAll()
    {
        for (int i = 0; i < getItems().size(); i++)
            expandItem(getItems().get(i));
    }

    /**
     * Expands the given item.
     */
    public void expandItem(T anItem)
    {
        // If not a parent or already expanded, just return
        if (!isItemParent(anItem) || isItemExpanded(anItem))
            return;

        // Make sure parent is expanded
        T parent = getItemParent(anItem);
        if (parent != null && !isItemExpanded(parent))
            expandItem(parent);

        // Set item expanded state, reset items and update given item
        setItemExpanded(anItem, true);
        setItems(getItems());
        updateItem(anItem);
    }

    /**
     * Collapses all tree nodes.
     */
    public void collapseAll()
    {
        for (int i = 0; i < getItems().size(); i++)
            collapseItem(getItems().get(i));
    }

    /**
     * Expands the given item.
     */
    public void collapseItem(T anItem)
    {
        // If not collapsable, just return
        if (!isItemParent(anItem) || !isItemExpanded(anItem) || !getItems().contains(anItem)) return;

        // Get items copy and remove successive items decended from given item
        List <T> items = new ArrayList<>(getItems());
        int index = items.indexOf(anItem);
        for (int i = index + 1, iMax = items.size(); i < iMax;i++) {
            T next = items.get(index + 1);
            if (isItemAncestor(next,anItem))
                items.remove(index + 1);
            else break;
        }

        // Set item expanded state, reset items and update given item
        setItemExpanded(anItem, false);
        setItemsImpl(items);
        updateItem(anItem);
    }

    /**
     * Expands the given item.
     */
    public void toggleItem(T anItem)
    {
        if (isItemExpanded(anItem))
            collapseItem(anItem);
        else expandItem(anItem);
    }

    /**
     * Returns the resolver.
     */
    public TreeResolver<T> getResolver()  { return _resolver; }

    /**
     * Sets the resolver.
     */
    public void setResolver(TreeResolver<T> aResolver)  { _resolver = aResolver; }

    /**
     * Returns the parent of given item.
     */
    public T getItemParent(T anItem)  { return _resolver.getParent(anItem); }

    /**
     * Returns the parent of given item.
     */
    public int getItemParentCount(T anItem)
    {
        int parentCount = 0;
        for (T parent = getItemParent(anItem); parent != null; parent = getItemParent(parent)) parentCount++;
        return parentCount;
    }

    /**
     * Returns whether given item has given object as any of it's ancestors.
     */
    public boolean isItemAncestor(T anItem, T aPar)
    {
        for (T parent = getItemParent(anItem); parent != null; parent = getItemParent(parent))
            if (parent == aPar)
                return true;
        return false;
    }

    /**
     * Whether given object is a parent (has children).
     */
    public boolean isItemParent(T anItem)  { return _resolver.isParent(anItem); }

    /**
     * Returns the children.
     */
    public List<T> getItemChildren(T aParent)  { return _resolver.getChildren(aParent); }

    /**
     * Returns whether an item is expanded.
     */
    public boolean isItemExpanded(T anItem)  { return _expanded.contains(anItem); }

    /**
     * Sets whether an item is expaned.
     */
    public void setItemExpanded(T anItem, boolean aValue)
    {
        if (aValue)
            _expanded.add(anItem);
        else _expanded.remove(anItem);
    }

    /**
     * Searches for parent of given item (only works if given item is visible).
     */
    public T findItemParent(T anItem)
    {
        List <T> items = getItems();
        int index = items.indexOf(anItem);
        if (index < 0)
            return null;

        for (int i = index - 1; i >= 0; i--) {
            T item = items.get(i);
            if (isItemParent(item) && isItemExpanded(item) && getItemChildren(item).contains(anItem))
                return item;
        }

        // Return not found
        return null;
    }

    /**
     * Called when PickList changes selection.
     */
    private void handlePickListSelChange(PropChange propChange)
    {
        // Handle Sel_Prop: Get array of changed indexes and update
        if (propChange.getPropName() == PickList.Sel_Prop) {
            ListSel sel1 = (ListSel) propChange.getOldValue();
            ListSel sel2 = (ListSel) propChange.getNewValue();
            int[] changed = ListSel.getChangedIndexes(sel1, sel2);

            int oldInd = changed.length > 1 ? changed[0] : -1;
            int newInd = changed.length > 1 ? changed[changed.length-1] : -1;
            firePropChange(SelIndex_Prop, oldInd, newInd);
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
        for (TreeCol<T> treeCol : getCols())
            treeCol.relayout();
    }

    /**
     * Override to reset cells.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        for (TreeCol<T> treeCol : getCols())
            treeCol.relayout();
    }

    /**
     * Override to propagate to inner views.
     */
    @Override
    public void setBorderRadius(double aValue)
    {
        super.setBorderRadius(aValue);
        _splitView.setBorderRadius(aValue);
    }

    /**
     * Override to return pref row count.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        // If PrefRowCount set, return PrefRowCount * RowHeight
        int prefRowCount = getPrefRowCount();
        if (prefRowCount > 0)
            return prefRowCount * getRowHeight() + getInsetsAll().getHeight();

        // Do normal version
        return super.getPrefHeightImpl(aW);
    }

    /**
     * Override to check for overflow.
     */
    @Override
    protected void layoutImpl()
    {
        super.layoutImpl();

        // Check wants ScrollView
        if (getOverflow() == Overflow.Scroll)
            ViewUtils.checkWantsScrollView(this);
    }

    /**
     * Override to return box layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()
    {
        return new BoxViewLayout(this, _splitView, true, true);
    }

    /**
     * Returns the maximum height.
     */
    public double getMaxHeight()
    {
        // If MaxRowCount set, return MaxRowCount*RowHeight
        if (getMaxRowCount() > 0)
            return getMaxRowCount() * getRowHeight() + getInsetsAll().getHeight();

        // Return normal version
        return super.getMaxHeight();
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return SelItem_Prop; }

    /**
     * Returns an Icon of a down arrow.
     */
    public Image getExpandedImage()
    {
        // If down arrow icon hasn't been created, create it
        if (_expImg != null) return _expImg;
        Image img = Image.getImageForSize(9,9,true);
        Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
        pntr.setColor(Color.BLACK);
        pntr.draw(poly);
        pntr.fill(poly); pntr.flush();
        return _expImg = img;
    }

    /**
     * Returns an image of a down arrow.
     */
    public Image getCollapsedImage()
    {
        // If down arrow icon hasn't been created, create it
        if (_clpImg != null) return _clpImg;
        Image img = Image.getImageForSize(9,9,true);
        Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 1.5, 7.5, 5.5, 4.5);
        pntr.setColor(Color.BLACK);
        pntr.draw(poly);
        pntr.fill(poly); pntr.flush();
        return _clpImg = img;
    }

    /**
     * Returns whether given items are equal to set items.
     */
    protected boolean equalsItems(List<T> theItems)
    {
        return ListUtils.equalsId(theItems, getItems()) || theItems.equals(getItems());
    }
}