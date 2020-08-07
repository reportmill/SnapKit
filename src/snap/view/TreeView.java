/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;

import snap.geom.Polygon;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show a list of hierarchical items.
 */
public class TreeView <T> extends ParentView implements Selectable<T> {
    
    // The items
    private PickList <T>  _items = new PickList<>();
    
    // The selected column
    private int  _selCol;
    
    // The resolver
    private TreeResolver <T>  _resolver = new TreeResolver.Adapter<T>() { };
    
    // Row height
    private int  _rowHeight = 20;

    // The Preferred number of rows
    private int  _prefRowCount = -1;
    
    // The maximum number of rows
    private int  _maxRowCount = -1;

    // The Cell Configure method
    private Consumer <ListCell<T>>  _cellConf;
    
    // The set of expanded items
    private Set <T>  _expanded = new HashSet<>();
    
    // Images for collapsed/expanded
    private Image  _clpImg, _expImg;
    
    // The SplitView to hold columns
    private SplitView  _split = new SplitView();
    
    // The ScrollView to hold SplitView+Columns
    private ScrollView  _scroll = new ScrollView(_split);
    
    // Constants
    private static final Paint DIVIDER_FILL = new Color("#EEEEEE");
    //private static final Paint DIVIDER_FILLH = new Color("#E0E0E0");

    /**
     * Creates a new TreeView.
     */
    public TreeView()
    {
        // Enable Action event for selection change
        enableEvents(Action);
        setFocusable(true); setFocusWhenPressed(true); setFocusPainted(false);

        // Configure Columns SplitView and ScrollView and add
        _split.setBorder(null);
        _split.setGrowWidth(true);
        _split.setDividerSpan(2);
        Divider div = _split.getDivider();
        div.setFill(DIVIDER_FILL);
        div.setBorder(null);
        div.setReach(3);
        _scroll.setBorder(null);
        addChild(_scroll);

        // Set main scroller to sync HeaderScroller
        //Scroller scroller = _scroll.getScroller();
        //scroller.addPropChangeListener(pce -> getHeaderScroller().setScrollH(scroller.getScrollH()), Scroller.ScrollH_Prop);

        // Whenever one split needs layout, propogate to other
        //SplitView hsplit = getHeaderSplitView();
        //_split.addPropChangeListener(pc -> hsplit.relayout(), NeedsLayout_Prop);
        //hsplit.addPropChangeListener(pc -> _split.relayout(), NeedsLayout_Prop);

        // Register PickList to notify when selection changes
        _items.addPropChangeListener(pc -> pickListSelChange(pc));

        // Create/add first column
        TreeCol treeCol = new TreeCol();
        addCol(treeCol);
    }

    /**
     * Returns the ScrollView.
     */
    public ScrollView getScrollView()  { return _scroll; }

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
    public void setPrefRowCount(int aValue)  { _prefRowCount = aValue; relayoutParent(); }

    /**
     * Returns the maximum number of rows.
     */
    public int getMaxRowCount()  { return _maxRowCount; }

    /**
     * Sets the maximum number of rows.
     */
    public void setMaxRowCount(int aValue)  { _maxRowCount = aValue; relayoutParent(); }

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
    public int getColCount()  { return _split.getItemCount(); }

    /**
     * Returns the column at given index.
     */
    public TreeCol <T> getCol(int anIndex)  { return (TreeCol)_split.getItem(anIndex); }

    /**
     * Returns the column at given index.
     */
    public TreeCol<T>[] getCols()  { return _split.getItems().toArray(new TreeCol[getColCount()]); }

    /**
     * Adds a column.
     */
    public void addCol(TreeCol aCol)  { addCol(aCol,getColCount()); }

    /**
     * Adds a column at index.
     */
    public void addCol(TreeCol aCol, int anIndex)
    {
        // Add TreeCol to SplitView
        aCol.setTree(this);
        _split.addItem(aCol, anIndex);

        // Replace column picklist with tableView picklist
        aCol.setPickList(_items);
    }

    /**
     * Adds columns.
     */
    public void addCols(TreeCol ... theCols)  { for (TreeCol c : theCols) addCol(c); }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return getItems().size(); }

    /**
     * Returns the items.
     */
    public List <T> getItems()  { return _items; }

    /**
     * Sets the items.
     */
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
        T sitem = getSelItem();
        _items.clear();
        _items.addAll(theItems);
        for (TreeCol tcol : getCols()) tcol.setItems(theItems);
        setSelItem(sitem);

        // Prune removed items from expanded set
        Object expanded[] = _expanded.toArray();
        for (Object item : expanded)
            if (!_items.contains(item))
                _expanded.remove(item);
    }

    /**
     * Sets the items.
     */
    public void setItems(T ... theItems)  { setItems(Arrays.asList(theItems)); }

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
     * Called when PickList changes selection.
     */
    protected void pickListSelChange(PropChange aPC)
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

        // Scroll selection to visible
        //if (isShowing()) scrollSelToVisible();
    }

    /**
     * Returns the list of expanded items for given items.
     */
    public List <T> getExpandedItems(List <T> theItems)
    {
        List <T> items = theItems;
        for (int i=0; i<items.size(); i++) { T item = items.get(i);

            // If item not expanded just continue
            if (!isExpanded(item)) continue;

            // If we haven't yet created new list, do it now
            if (items==theItems) items = new ArrayList(items);

            // Remove successive items decended from current item
            for (int j=i+1,jMax=items.size();j<jMax;j++) { T next = items.get(i+1);
                if (isAncestor(next,item)) items.remove(i+1);
                else break;
            }

            // If item no long parent (could have changed), clear state and continue
            if (!isParent(item)) { setExpanded(item,false); continue; }

            // Get item children and add after item
            T citems[] = getChildren(item);
            for (int j=0;j<citems.length;j++) items.add(i+j+1, citems[j]);
        }

        // Return items
        return items;
    }

    /**
     * Called to update items that have changed.
     */
    public void updateItems(T ... theItems)
    {
        for (TreeCol tcol : getCols())
            tcol.updateItems(theItems);
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
    public int getRowAt(double aX, double aY)  { return (int)(aY/getRowHeight()); }

    /**
     * Expands all tree nodes.
     */
    public void expandAll()  { for (int i=0;i<getItems().size();i++) expandItem(getItems().get(i)); }

    /**
     * Expands the given item.
     */
    public void expandItem(T anItem)
    {
        // If not expandable, just return
        if (!isParent(anItem) || isExpanded(anItem) || !getItems().contains(anItem)) return;

        // Set item expanded state, reset items and update given item
        setExpanded(anItem, true);
        setItems(getItems());
        updateItems(anItem);
    }

    /**
     * Expands the given item.
     */
    public void collapseItem(T anItem)
    {
        // If not collapsable, just return
        if (!isParent(anItem) || !isExpanded(anItem) || !getItems().contains(anItem)) return;

        // Get items copy and remove successive items decended from given item
        List <T> items = new ArrayList(getItems());
        int index = items.indexOf(anItem);
        for (int i=index+1,iMax=items.size();i<iMax;i++) { T next = items.get(index+1);
            if (isAncestor(next,anItem)) items.remove(index+1);
            else break;
        }

        // Set item expanded state, reset items and update given item
        setExpanded(anItem, false);
        setItemsImpl(items);
        updateItems(anItem);
    }

    /**
     * Expands the given item.
     */
    public void toggleItem(T anItem)
    {
        if (isExpanded(anItem)) collapseItem(anItem);
        else expandItem(anItem);
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
    public int getParentCount(T anItem)
    {
        int pc = 0; for (T p=getParent(anItem); p!=null; p=getParent(p)) pc++; return pc;
    }

    /**
     * Returns whether given item has given object as any of it's ancestors.
     */
    public boolean isAncestor(T anItem, T aPar)
    {
        for (T par=getParent(anItem); par!=null; par=getParent(par))
            if (par==aPar)
                return true;
        return false;
    }

    /**
     * Whether given object is a parent (has children).
     */
    public boolean isParent(T anItem)  { return _resolver.isParent(anItem); }

    /**
     * Returns the children.
     */
    public T[] getChildren(T aParent)  { return _resolver.getChildren(aParent); }

    /**
     * Returns the text to be used for given item.
     */
    public String getText(T anItem, int aCol)  { return _resolver.getText(anItem, aCol); }

    /**
     * Return the image to be used for given item.
     */
    public Image getImage(T anItem)  { return _resolver.getImage(anItem); }

    /**
     * Return the graphic to be used for given item.
     */
    public View getGraphic(T anItem)  { return _resolver.getGraphic(anItem); }

    /**
     * Returns whether an item is expanded.
     */
    public boolean isExpanded(T anItem)  { return _expanded.contains(anItem); }

    /**
     * Sets whether an item is expaned.
     */
    public void setExpanded(T anItem, boolean aValue)
    {
        if (aValue) _expanded.add(anItem);
        else _expanded.remove(anItem);
    }

    /**
     * Searches for parent of given item (only works if given item is visible).
     */
    public T findParent(T anItem)
    {
        List <T> items = getItems();
        int index = items.indexOf(anItem); if (index<0) return null;
        for (int i=index-1;i>=0;i--) { T item = items.get(i);
            if (isParent(item) && isExpanded(item) && ArrayUtils.contains(getChildren(item), anItem))
                return item; }
        return null;
    }

    /**
     * Override to reset cells.
     */
    public void setY(double aValue)
    {
        if (aValue==getY()) return; super.setY(aValue);
        for (TreeCol tcol : getCols()) tcol.relayout();
    }

    /**
     * Override to reset cells.
     */
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return; super.setHeight(aValue);
        for (TreeCol tcol : getCols()) tcol.relayout();
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, _scroll, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        // If PrefRowCount set, return PrefRowCount*RowHeight
        if (getPrefRowCount()>0)
            return getPrefRowCount()*getRowHeight() + getInsetsAll().getHeight();

        // Return pref height of Scroll
        return BoxView.getPrefHeight(this, _scroll, aW);
    }

    /**
     * Returns the maximum height.
     */
    public double getMaxHeight()
    {
        // If MaxRowCount set, return MaxRowCount*RowHeight
        if (getMaxRowCount()>0)
            return getMaxRowCount()*getRowHeight() + getInsetsAll().getHeight();

        // Return normal version
        return super.getMaxHeight();
    }

    /**
     * Override to layout ScrollView.
     */
    protected void layoutImpl()  { BoxView.layout(this, _scroll, null, true, true); }

    /**
     * Override to ensure that DragGesture events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // If DragGesture outside ScrollView.Scroller, just return
        if (anEvent.isDragGesture())
            if (!_scroll.getScroller().contains(anEvent.getX(), anEvent.getY())) {
                anEvent.consume(); return; }
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
        if (_expImg!=null) return _expImg;
        Image img = Image.get(9,9,true); Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
        pntr.setColor(Color.BLACK); pntr.draw(poly); pntr.fill(poly); pntr.flush();
        return _expImg = img;
    }

    /**
     * Returns an image of a down arrow.
     */
    public Image getCollapsedImage()
    {
        // If down arrow icon hasn't been created, create it
        if (_clpImg!=null) return _clpImg;
        Image img = Image.get(9,9,true); Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 1.5, 7.5, 5.5, 4.5);
        pntr.setColor(Color.BLACK); pntr.draw(poly); pntr.fill(poly); pntr.flush();
        return _clpImg = img;
    }

    /**
     * Returns the default border.
     */
    public Border getDefaultBorder()  { return ScrollView.SCROLL_VIEW_BORDER; }

    /**
     * Returns whether given items are equal to set items.
     */
    protected boolean equalsItems(List theItems)
    {
        return ListUtils.equalsId(theItems, getItems()) || theItems.equals(getItems());
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXMLView(anArchiver, anElement);
    }
}