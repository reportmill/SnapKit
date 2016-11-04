/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show a list of hierarchical items.
 */
public class TreeView <T> extends ParentView implements View.Selectable <T> {
    
    // Whether tree root is visible
    boolean                 _rootVis;
    
    // Whether to show root handle
    boolean                 _rootHandle;
    
    // The items
    List <T>                _items = new ArrayList();
    
    // The selected index
    int                     _selIndex = -1;
    
    // The selected column
    int                     _selCol;
    
    // The resolver
    TreeResolver <T>        _resolver = new TreeResolver.Adapter() { };
    
    // Row height
    int                     _rowHeight = 20;

    // The Cell Configure method
    Consumer <ListCell<T>>  _cellConf;
    
    // The set of expanded items
    Set <T>                 _expanded = new HashSet();
    
    // Images for collapsed/expanded
    Image                   _clpImg, _expImg;
    
    // Constants for TreeView
    public static final String ShowRoot_Prop = "ShowRoot";
    public static final String ShowRootHandle_Prop = "ShowRootHandle";
    
/**
 * Creates a new TreeView.
 */
public TreeView()
{
    enableEvents(Action);
    TreeCol treeCol = new TreeCol();
    addChild(treeCol);
    setFocusable(true); setFocusWhenPressed(true);
}

/**
 * Returns whether root is visible.
 */
public boolean isShowRoot()  { return _rootVis; }

/**
 * Sets whether root is visible.
 */
public void setShowRoot(boolean aValue)
{
    if(aValue==_rootVis) return;
    firePropChange(ShowRoot_Prop, _rootVis, _rootVis = aValue);
}

/**
 * Returns whether root shows handle.
 */
public boolean isShowRootHandle()  { return _rootHandle; }

/**
 * Sets whether root shows handle.
 */
public void setShowRootHandle(boolean aValue)
{
    if(aValue==_rootHandle) return;
    firePropChange(ShowRootHandle_Prop, _rootHandle, _rootHandle = aValue);
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
 * Returns the number of columns.
 */
public int getColCount()  { return getChildCount(); }

/**
 * Returns the column at given index.
 */
public TreeCol <T> getCol(int anIndex)  { return (TreeCol)getChild(anIndex); }

/**
 * Returns the column at given index.
 */
public TreeCol<T>[] getCols()  { return Arrays.copyOf(getChildren(), getChildCount(), TreeCol[].class); }

/**
 * Adds a column.
 */
public void addCol(TreeCol aCol)  { addCol(aCol,getColCount()); }

/**
 * Adds a column at index.
 */
public void addCol(TreeCol aCol, int anIndex)  { addChild(aCol, anIndex); }

/**
 * Adds columns.
 */
public void addCols(TreeCol ... theCols)  { for(TreeCol c : theCols) addCol(c); }

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
    if(equalsItems(theItems)) return;
    T sitem = getSelectedItem();
    _items.clear();
    _items.addAll(theItems);
    for(TreeCol tcol : getCols()) tcol.setItems(theItems);
    setSelectedItem(sitem);
    
    // Prune removed items from expanded set
    Object expanded[] = _expanded.toArray();
    for(Object item : expanded) if(!_items.contains(item)) _expanded.remove(item);
}

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(Arrays.asList(theItems)); }

/**
 * Returns the list of expanded items for given items.
 */
public List <T> getExpandedItems(List <T> theItems)
{
    List <T> items = theItems;
    for(int i=0; i<items.size(); i++) { T item = items.get(i);
    
        // If item not expanded just continue
        if(!isExpanded(item)) continue;
        
        // If we haven't yet created new list, do it now
        if(items==theItems) items = new ArrayList(items);
        
        // Remove successive items decended from current item
        for(int j=i+1,jMax=items.size();j<jMax;j++) { T next = items.get(i+1);
            if(isAncestor(next,item)) items.remove(i+1);
            else break;
        }
        
        // If item no long parent (could have changed), clear state and continue
        if(!isParent(item)) { setExpanded(item,false); continue; }
        
        // Get item children and add after item
        T citems[] = getChildren(item);
        for(int j=0;j<citems.length;j++) items.add(i+j+1, citems[j]);
    }
    
    // Return items
    return items;
}

/**
 * Called to update items that have changed.
 */
public void updateItems(T ... theItems)
{
    for(TreeCol tcol : getCols()) tcol.updateItems(theItems);
}

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _selIndex; }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)
{
    if(anIndex==_selIndex) return;
    firePropChange("SelectedIndex", _selIndex, _selIndex = anIndex);
    for(TreeCol tcol : getCols()) tcol.setSelectedIndex(anIndex);
    fireActionEvent();
}

/**
 * Returns the minimum selected index.
 */
public int getSelectedIndexMin()
{
    int indexes[] = getSelectedIndices();
    int min = Integer.MAX_VALUE; for(int i : indexes) min = Math.min(min, i);
    return min!=Integer.MAX_VALUE? min : -1;
}

/**
 * Returns the maximum selected index.
 */
public int getSelectedIndexMax()
{
    int indexes[] = getSelectedIndices();
    int max = -1; for(int i : indexes) max = Math.max(max, i);
    return max;
}

/**
 * Returns the selected indices.
 */
public int[] getSelectedIndices()  { return _selIndex>=0? new int[] { _selIndex } : new int[0]; }

/**
 * Sets the selection interval.
 */
public void setSelectionInterval(int aStart, int anEnd)
{
    int min = Math.min(aStart,anEnd), max = Math.max(aStart,anEnd), len = max-min+1;
    int indexes[] = new int[len]; for(int i=0;i<len;i++) indexes[i] = i + min;
    setSelectedIndex(min);
}

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return _selIndex>=0? _items.get(_selIndex) : null; }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)
{
    int index = _items.indexOf(anItem);
    setSelectedIndex(index);
}

/**
 * Returns the selected row.
 */
public int getSelectedRow()  { return getSelectedIndex(); }

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
 * Expands all tree nodes.
 */
public void expandAll()  { for(int i=0;i<getItems().size();i++) expandItem(getItems().get(i)); }

/**
 * Expands the given item.
 */
public void expandItem(T anItem)
{
    // If not expandable, just return
    if(!isParent(anItem) || isExpanded(anItem) || !getItems().contains(anItem)) return;
    
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
    if(!isParent(anItem) || !isExpanded(anItem) || !getItems().contains(anItem)) return;
    
    // Get items copy and remove successive items decended from given item
    List <T> items = new ArrayList(getItems());
    int index = items.indexOf(anItem);
    for(int i=index+1,iMax=items.size();i<iMax;i++) { T next = items.get(index+1);
        if(isAncestor(next,anItem)) items.remove(index+1);
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
    if(isExpanded(anItem)) collapseItem(anItem);
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
    int pc = 0; for(T p=getParent(anItem); p!=null; p=getParent(p)) pc++; return pc;
}

/**
 * Returns whether given item has given object as any of it's ancestors.
 */
public boolean isAncestor(T anItem, T aPar)
{
    for(T par=getParent(anItem); par!=null; par=getParent(par))
        if(par==aPar)
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
    if(aValue) _expanded.add(anItem);
    else _expanded.remove(anItem);
}

/**
 * Searches for parent of given item (only works if given item is visible).
 */
public T findParent(T anItem)
{
    List <T> items = getItems();
    int index = items.indexOf(anItem); if(index<0) return null;
    for(int i=index-1;i>=0;i--) { T item = items.get(i);
        if(isParent(item) && isExpanded(item) && ArrayUtils.contains(getChildren(item), anItem))
            return item; }
    return null;
}

/**
 * Override to reset cells.
 */
public void setY(double aValue)
{
    if(aValue==getY()) return; super.setY(aValue);
    for(TreeCol tcol : getCols()) tcol.relayout();
}

/**
 * Override to reset cells.
 */
public void setHeight(double aValue)
{
    if(aValue==getHeight()) return; super.setHeight(aValue);
    for(TreeCol tcol : getCols()) tcol.relayout();
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    double pw = 0;
    for(TreeCol tcol : getCols()) pw += tcol.getPrefWidth();
    return pw;
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return getRowHeight()*getItems().size(); }

/**
 * Override to layout children with VBox layout.
 */
protected void layoutChildren()
{
    ViewLayout.HBoxLayout layout = new ViewLayout.HBoxLayout(this); layout.setFillHeight(true);
    layout.layoutChildren();
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "SelectedItem"; }

/**
 * Returns an Icon of a down arrow.
 */
public Image getExpandedImage()
{
    // If down arrow icon hasn't been created, create it
    if(_expImg!=null) return _expImg;
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
    if(_clpImg!=null) return _clpImg;
    Image img = Image.get(9,9,true); Painter pntr = img.getPainter();
    Polygon poly = new Polygon(1.5, 1.5, 1.5, 7.5, 5.5, 4.5);
    pntr.setColor(Color.BLACK); pntr.draw(poly); pntr.fill(poly); pntr.flush();
    return _clpImg = img;
}

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

    // Archive RootVisible, ShowRootHandles
    if(!isShowRoot()) e.add("RootVisible", false);
    if(!isShowRootHandle()) e.add("ShowRootHandles", false);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);

    // Unarchive RootVisible, ShowRootHandles
    if(anElement.hasAttribute("RootVisible")) setShowRoot(anElement.getAttributeBooleanValue("RootVisible"));
    if(anElement.hasAttribute("ShowRootHandles"))
        setShowRootHandle(anElement.getAttributeBooleanValue("ShowRootHandles"));
    if(anElement.hasAttribute("ShowsRootHandles"))
        setShowRootHandle(anElement.getAttributeBooleanValue("ShowsRootHandles"));
}

}