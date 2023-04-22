package snap.util;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.PropChangeSupport;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A list implementation that includes support for a selection and a synchronized sorted list.
 */
public class PickList<E> extends AbstractList<E> implements Cloneable {
    
    // The real list
    private List<E>  _list;

    // Whether list supports multiple selection
    private boolean  _multiSel;

    // The selection
    private ListSel  _sel;

    // The PropChangeSupport
    protected PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String Item_Prop = "Item";
    public static final String Sel_Prop = "Sel";
    public static final String MultiSel_Prop = "MultiSel";

    /**
     * Constructor.
     */
    public PickList()
    {
        super();
        _list = new ArrayList<>();
        _sel = new ListSel(-1, -1);
    }

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
    public void setAll(Collection<? extends E> aCollection)
    {
        E[] selItems = (E[]) getSelItems();
        clear();
        if (aCollection != null)
            addAll(aCollection);
        setSelItems(selItems);
    }

    /**
     * Clears the list.
     */
    public void clear()
    {
        while (_list.size() > 0)
            remove(0);
    }

    /**
     * Returns whether list allows multiple selections.
     */
    public boolean isMultiSel()  { return _multiSel; }

    /**
     * Sets whether list allows multiple selections.
     */
    public void setMultiSel(boolean aValue)
    {
        if (aValue == isMultiSel()) return;
        firePropChange(MultiSel_Prop, _multiSel, _multiSel = aValue, -1);
    }

    /**
     * Returns the ListSel.
     */
    public ListSel getSel()  { return _sel; }

    /**
     * Sets the ListSel.
     */
    public void setSel(ListSel aSel)
    {
        // Trim to size
        ListSel sel = aSel!=null ? aSel.copyForMaxSize(size()) : ListSel.EMPTY;
        if (!isMultiSel())
            sel = aSel.copyForSingleSel();

        // If already set, just return
        if (Objects.equals(sel, _sel)) return;

        // Set new Sel and fire prop change
        firePropChange(Sel_Prop, _sel, _sel = sel, -1);
    }

    /**
     * Returns whether selection is empty.
     */
    public boolean isSelEmpty()  { return _sel.isEmpty(); }

    /**
     * Returns whether given index is selected index.
     */
    public boolean isSelIndex(int anIndex)  { return _sel.isSel(anIndex); }

    /**
     * Returns the selected index.
     */
    public int getSelIndex()
    {
        return _sel.getLead();
    }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int anIndex)
    {
        setSel(new ListSel(anIndex, anIndex));
    }

    /**
     * Returns the selected indices.
     */
    public int[] getSelIndexes()  { return _sel.getIndexes(); }

    /**
     * Sets the selected index.
     */
    public void setSelIndexes(int ... theIndexes)
    {
        ListSel sel = ListSel.getSelForIndexArray(theIndexes);
        setSel(sel);
    }

    /**
     * Clears the selection.
     */
    public void clearSel()
    {
        setSel(ListSel.EMPTY);
    }

    /**
     * Returns the selected item.
     */
    public E getSelItem()
    {
        int selIndex = getSelIndex();
        return selIndex >= 0 && selIndex < size() ? get(selIndex) : null;
    }

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
        int[] selIndexes = getSelIndexes();
        T[] selItems = (T[]) Array.newInstance(aClass, selIndexes.length);
        for (int i = 0; i < selIndexes.length; i++)
            selItems[i] = (T) get(selIndexes[i]);
        return selItems;
    }

    /**
     * Sets the selected index.
     */
    public void setSelItems(E ... theItems)
    {
        int[] selIndexes = getIndexesForItems(theItems);
        setSelIndexes(selIndexes);
    }

    /**
     * Returns indexes for items.
     */
    public int[] getIndexesForItems(E ... theItems)
    {
        // Iterate over items and return array of indexes
        int len = theItems.length, len2 = 0;
        int[] indexes = new int[len];
        for (E item : theItems) {
            int itemIndex = indexOf(item);
            if (itemIndex >= 0)
                indexes[len2++] = itemIndex;
        }

        // Trim list and return
        if (len2 != len)
            indexes = Arrays.copyOf(indexes, len2);
        return indexes;
    }

    /**
     * Selects up in the list.
     */
    public void selectUp()
    {
        int selIndex = getSelIndex();
        if (selIndex > 0)
            setSelIndex(selIndex - 1);
    }

    /**
     * Selects up in the list.
     */
    public void selectDown()
    {
        int selIndex = getSelIndex();
        if (selIndex < size()-1)
            setSelIndex(selIndex + 1);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcs == PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
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
        if (!_pcs.hasListener(aProp)) return;
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
        PickList<E> clone;
        try { clone = (PickList<E>) super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
        clone._list = new ArrayList<>(_list);
        clone._pcs = PropChangeSupport.EMPTY;  // Clear listeners and return clone
        return clone;
    }
}