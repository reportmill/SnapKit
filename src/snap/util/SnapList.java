/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A list implementation that supports property changes and a cached getArray() method.
 */
public class SnapList <E> extends AbstractList <E> {
    
    // The actual list
    private List <E>  _list = new Vector();
    
    // A cached array of list
    private E  _array[];
    
    // PropertyChangeSupport
    PropChangeSupport  _pcs = new PropChangeSupport(this);
    
    // Constant for Items
    public static final String ITEMS_PROP = "Items";
    
    /**
     * Returns an item at index.
     */
    public E get(int anIndex)
    {
        return _list.get(anIndex);
    }

    /**
     * Returns the size of list.
     */
    public int size()
    {
        return _list.size();
    }

    /**
     * Adds an element to list.
     */
    public void add(int anIndex, E anItem)
    {
        _list.add(anIndex, anItem);
        _array = null;
        firePropChange(ITEMS_PROP, null, anItem, anIndex);
    }

    /**
     * Removes an element from list at index.
     */
    public E remove(int anIndex)
    {
        E item = _list.remove(anIndex);
        _array = null;
        firePropChange(ITEMS_PROP, item, null, anIndex);
        return item;
    }

    public E set(int anIndex, E anItem)
    {
        E item = remove(anIndex);
        add(anIndex, anItem);
        return item;
    }

    /**
     * Returns a cached array of this list (because of erasure, requires that type is specified).
     */
    public E[] getArray(Class<E> aClass)
    {
        if (_array!=null)
            return _array;
        _array = (E[]) Array.newInstance(aClass, size());
        return _array = toArray(_array);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL)
    {
        _pcs.addPropChangeListener(aPCL);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aPCL)
    {
        _pcs.removePropChangeListener(aPCL);
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
    {
        if (!_pcs.hasListener(aProp))
            return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
    }

    /**
     * Sends the property change.
     */
    protected void firePropChange(PropChange anEvent)
    {
        _pcs.firePropChange(anEvent);
    }
}