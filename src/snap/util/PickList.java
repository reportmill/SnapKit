package snap.util;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A list implementation that includes support for a selection and a synchronized sorted list.
 */
public class PickList <E> extends AbstractList <E> implements Cloneable {
    
    // The real list
    List <E>                     _list = new ArrayList();

    // The selected index
    int                          _selIndex = -1;
    
    // The PropChangeSupport
    protected PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String Item_Prop = "Item";
    public static final String SelIndex_Prop = "SelIndex";

/**
 * Returns whether list allows multiple selections.
 */
public boolean isMultipleChoice()  { return false; }

/**
 * Return list size.
 */
public int size()  { return _list.size(); }

/**
 * Return list item at index.
 */
public E get(int anIndex)  { return _list.get(anIndex); }

/**
 * Add list item.
 */
public void add(int anIndex, E anItem)
{
    _list.add(anIndex, anItem);
    firePropChange(Item_Prop, null, anItem, anIndex);
}

/**
 * Remove list item.
 */
public E remove(int anIndex)
{
    E item = _list.remove(anIndex);
    firePropChange(Item_Prop, item, null, anIndex);
    return item;
}

/**
 * Sets all items.
 */
public void setAll(Collection <? extends E> aCol)
{
    Object sitems[] = getSelItems();
    clear();
    if(aCol!=null) addAll(aCol);
    setSelItems(sitems);
}

/**
 * Returns the selected index.
 */
public int getSelIndex()  { return _selIndex; }

/**
 * Adds a selected index.
 */
public void addSelIndex(int anIndex)
{
    setSelIndex(anIndex);
}

/**
 * Sets the selected index.
 */
public void setSelIndex(int anIndex)
{
    if(anIndex==_selIndex) return;
    firePropChange(SelIndex_Prop, _selIndex, _selIndex = anIndex, -1);
}

/**
 * Returns the minimum selected index.
 */
public int getSelIndexMin()
{
    int indexes[] = getSelIndices();
    int min = Integer.MAX_VALUE; for(int i : indexes) min = Math.min(min, i);
    return min!=Integer.MAX_VALUE? min : -1;
}

/**
 * Returns the maximum selected index.
 */
public int getSelIndexMax()
{
    int indexes[] = getSelIndices();
    int max = -1; for(int i : indexes) max = Math.max(max, i);
    return max;
}

/**
 * Returns the selected indices.
 */
public int[] getSelIndices()  { return _selIndex>=0? new int[] { _selIndex } : new int[0]; }

/**
 * Sets the selection interval.
 */
public void setSelInterval(int aStart, int anEnd)
{
    int min = Math.min(aStart,anEnd), max = Math.max(aStart,anEnd), len = max-min+1;
    int indexes[] = new int[len]; for(int i=0;i<len;i++) indexes[i] = i + min;
    setSelIndex(min);
}

/**
 * Returns the selected item.
 */
public E getSelItem()  { return _selIndex>=0 && _selIndex<size()? get(_selIndex) : null; }

/**
 * Sets the selected index.
 */
public void setSelItem(E anItem)
{
    int index = indexOf(anItem);
    setSelIndex(index);
}

/**
 * Returns the selected item.
 */
public Object[] getSelItems()  { return getSelItems(Object.class); }

/**
 * Returns the selected item.
 */
public <T> T[] getSelItems(Class <T> aClass)
{
    int selInds[] = getSelIndices();
    T[] items = (T[])Array.newInstance(aClass, selInds.length);
    for(int i=0;i<selInds.length;i++) items[i] = (T)get(selInds[i]);
    return items;
}

/**
 * Adds a selected item.
 */
public void addSelItem(E anItem)
{
    int ind = indexOf(anItem);
    addSelIndex(ind);
}

/**
 * Sets the selected index.
 */
public void setSelItems(Object theItems[])  { for(Object itm : theItems) addSelItem((E)itm); }

/**
 * Selects up in the list.
 */
public void selectUp()  { if(getSelIndex()>0)setSelIndex(getSelIndex()-1); }

/**
 * Selects up in the list.
 */
public void selectDown()  { if(getSelIndex()<size()-1) setSelIndex(getSelIndex()+1); }

/**
 * Returns the list items as a single string with items separated by newlines.
 */
public String getItemsString()  { return ListUtils.joinStrings(this, "\n"); }

/**
 * Sets the list items as a single string with items separated by newlines.
 */
public void setItemsString(String aString)
{
    String items[] = aString!=null? aString.split("\n") : new String[0];
    for(int i=0; i<items.length; i++) items[i] = items[i].trim();
    clear();
    Collections.addAll(this, (E)items);
}

/**
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aLsnr)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aLsnr);
}

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aLsnr)  { _pcs.removePropChangeListener(aLsnr); }

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
{
    if(!_pcs.hasListener(aProp)) return;
    firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
}

/**
 * Fires a given property change.
 */
protected void firePropChange(PropChange aPCE)  {  _pcs.firePropChange(aPCE); }

/**
 * Standard clone implementation.
 */
public Object clone()
{
    PickList clone = null; try { clone = (PickList)super.clone(); }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    clone._list = new ArrayList(_list);
    clone._pcs = PropChangeSupport.EMPTY;  // Clear listeners and return clone
    return clone;
}
  
}