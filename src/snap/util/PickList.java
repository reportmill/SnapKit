package snap.util;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A list implementation that includes support for a selection and a synchronized sorted list.
 */
public class PickList <E> extends AbstractList <E> implements Cloneable {
    
    // The real list
    private List <E>  _list = new ArrayList<>();

    // Whether list supports multiple selection
    private boolean  _multiSel;

    // The selected index
    private int  _selIndexes[] = EMPTY_SEL;
    
    // The PropChangeSupport
    protected PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String Item_Prop = "Item";
    public static final String SelIndex_Prop = "SelIndex";
    public static final String SelIndexes_Prop = "SelIndexes";
    public static final String MultiSel_Prop = "MultiSel";

    // Constants
    private static final int EMPTY_SEL[] = new int[0];

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
        E sitems[] = (E[]) getSelItems();
        clear();
        if (aCol!=null) addAll(aCol);
        setSelItems(sitems);
    }

    /**
     * Clears the list.
     */
    public void clear()  { _list.clear(); }

    /**
     * Returns whether list allows multiple selections.
     */
    public boolean isMultiSel()  { return _multiSel; }

    /**
     * Sets whether list allows multiple selections.
     */
    public void setMultiSel(boolean aValue)
    {
        if (aValue==isMultiSel()) return;
        firePropChange(MultiSel_Prop, _multiSel, _multiSel = aValue, -1);
    }

    /**
     * Returns the selected index.
     */
    public int getSelIndex()
    {
        return _selIndexes.length>0 ? _selIndexes[0] : -1;
    }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int anIndex)
    {
        // If MultiSel, clear and add
        if (isMultiSel()) {
            setSelIndexes(anIndex);
            return;
        }

        // If already set, just return
        if (anIndex == getSelIndex()) return;

        // Set new value and fire prop
        int old = getSelIndex();
        _selIndexes = anIndex>=0 ? new int[] { anIndex } : EMPTY_SEL;
        firePropChange(SelIndex_Prop, old, anIndex, -1);
    }

    /**
     * Returns the selected indices.
     */
    public int[] getSelIndexes()  { return _selIndexes; }

    /**
     * Sets the selected index.
     */
    public void setSelIndexes(int ... theIndexes)
    {
        // If already set, just return
        if (Arrays.equals(theIndexes, getSelIndexes())) return;

        // Clear and set
        int old[] = _selIndexes;
        _selIndexes = theIndexes;
        firePropChange(SelIndexes_Prop, old, _selIndexes, -1);
    }

    /**
     * Adds a selected index.
     */
    public void addSelIndex(int anIndex)
    {
        // If SingleSel, just set and return
        if (!isMultiSel()) {
            setSelIndex(anIndex);
            return;
        }

        // If already selected, just return
        if (isSelIndex(anIndex)) return;

        // Add index to SelIndexes (sorted)
        int len = _selIndexes.length;
        _selIndexes = Arrays.copyOf(_selIndexes, len + 1);
        _selIndexes[len] = anIndex;
        Arrays.sort(_selIndexes);

        // Fire prop change (probably need to do this right one day)
        firePropChange(SelIndexes_Prop, false, true, anIndex);
    }

    /**
     * Removes a selected index.
     */
    public void removeSelIndex(int anIndex)
    {
        // If SingleSel, just set and return
        if (!isMultiSel()) {
            if (anIndex==getSelIndex())
                clearSel();
            return;
        }

        // Get index of list item index in SelIndexes array (just return if not there)
        int ind = Arrays.binarySearch(_selIndexes, anIndex);
        if (ind<0)
            return;

        // Remove index
        _selIndexes = ArrayUtils.remove(_selIndexes, ind);

        // Fire prop change (probably need to do this right one day)
        firePropChange(SelIndexes_Prop, true, false, anIndex);
    }

    /**
     * Clears the selection.
     */
    public void clearSel()
    {
        // If already clear, just return
        if (getSelIndex()==-1) return;

        // If MultiSel, clear
        if (isMultiSel())
            setSelIndexes(EMPTY_SEL);
        else setSelIndex(-1);
    }

    /**
     * Returns whether given index is selected index.
     */
    public boolean isSelIndex(int anIndex)
    {
        // Handle SingleSel
        if (!isMultiSel())
            return anIndex==getSelIndex();

        // Handle MultiSel
        int ind = Arrays.binarySearch(_selIndexes, anIndex);
        return ind>=0;
    }

    /**
     * Returns the minimum selected index.
     */
    public int getSelIndexMin()
    {
        return getSelIndex();
    }

    /**
     * Returns the maximum selected index.
     */
    public int getSelIndexMax()
    {
        int len = _selIndexes.length;
        return len>0 ? _selIndexes[len-1] : -1;
    }

    /**
     * Sets the selection interval.
     */
    public void setSelInterval(int aStart, int anEnd)
    {
        int min = Math.min(aStart, anEnd);
        int max = Math.max(aStart, anEnd);
        int len = max - min + 1;
        int indexes[] = new int[len];
        for (int i=0;i<len;i++) indexes[i] = i + min;
        setSelIndex(min);
    }

    /**
     * Adds the interval to this index.
     */
    public void addSelIntervalToIndex(int anIndex)
    {
        // If no selection or not MultiSel, just set index
        if (getSelIndex()<0 || !isMultiSel())
            setSelIndex(anIndex);

        // If index above max, add from max to index
        else if (anIndex>getSelIndexMax()) {
            for (int i=getSelIndexMax()+1; i<=anIndex; i++)
                addSelIndex(i);
        }

        // If index below min, add from min to index
        else if (anIndex<getSelIndexMin()) { int selInd = getSelIndexMin();
            for (int i=anIndex; i<selInd; i++)
                addSelIndex(i);
        }

        // If above min, add from min to index
        else {
            for (int i=getSelIndexMin(); i<=anIndex; i++)
                addSelIndex(i);
        }
    }

    /**
     * Returns the selected item.
     */
    public E getSelItem()
    {
        int ind = getSelIndex();
        return ind>=0 && ind<size() ? get(ind) : null;
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
        int selInds[] = getSelIndexes();
        T[] items = (T[]) Array.newInstance(aClass, selInds.length);
        for (int i=0; i<selInds.length; i++)
            items[i] = (T) get(selInds[i]);
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
    public void setSelItems(E ... theItems)
    {
        for (E item : theItems)
            addSelItem(item);
    }

    /**
     * Selects up in the list.
     */
    public void selectUp()
    {
        if (getSelIndex()>0)
            setSelIndex(getSelIndex()-1);
    }

    /**
     * Selects up in the list.
     */
    public void selectDown()
    {
        if (getSelIndex()<size()-1)
            setSelIndex(getSelIndex()+1);
    }

    /**
     * Returns the list items as a single string with items separated by newlines.
     */
    public String getItemsString()
    {
        return ListUtils.joinStrings(this, "\n");
    }

    /**
     * Sets the list items as a single string with items separated by newlines.
     */
    public void setItemsString(String aString)
    {
        String items[] = aString!=null ? aString.split("\n") : new String[0];
        for (int i=0; i<items.length; i++)
            items[i] = items[i].trim();
        clear();
        Collections.addAll(this, (E)items);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aLsnr)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
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
        PickList clone = null; try { clone = (PickList)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
        clone._list = new ArrayList(_list);
        clone._pcs = PropChangeSupport.EMPTY;  // Clear listeners and return clone
        return clone;
    }
}