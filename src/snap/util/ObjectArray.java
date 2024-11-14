/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * This class manages an array of object values.
 */
public class ObjectArray<T> extends AbstractList<T> implements Cloneable {

    // The String array
    protected T[]  _array;

    // The array length
    protected int _size;

    // The class
    private Class<T> _compClass;

    /**
     * Constructor.
     */
    public ObjectArray(Class<T> aClass)
    {
        this(aClass, 8);
    }

    /**
     * Constructor.
     */
    public ObjectArray(Class<T> aClass, int length)
    {
        super();
        _compClass = aClass;
        _array = (T[]) Array.newInstance(aClass, length);
    }

    /**
     * Constructor.
     */
    public ObjectArray(T[] anArray)
    {
        super();
        _array = anArray;
        _compClass = (Class<T>) anArray.getClass().getComponentType();
        _size = anArray.length;
    }

    /**
     * Returns the length.
     */
    public final int size()  { return _size; }

    /**
     * Sets the length.
     */
    public void setLength(int aValue)
    {
        // Expand components array if needed
        if (aValue >= _size)
            _array = Arrays.copyOf(_array, aValue);

        // Set length
        _size = aValue;
    }

    /**
     * Returns the Object value at index.
     */
    @Override
    public T get(int anIndex)  { return _array[anIndex]; }

    /**
     * Sets the String value at index.
     */
    @Override
    public T set(int anIndex, T aValue)
    {
        T oldValue = _array[anIndex];
        _array[anIndex] = aValue;
        return oldValue;
    }

    /**
     * Adds the value at index.
     */
    @Override
    public void add(int anIndex, T aValue)
    {
        // Expand components array if needed
        if (_size == _array.length)
            _array = Arrays.copyOf(_array, Math.max(_array.length * 2, 8));

        // If index is inside current length, shift existing elements over
        if (anIndex < _size)
            System.arraycopy(_array, anIndex, _array, anIndex + 1, _size - anIndex);

        // Set value and increment length
        _array[anIndex] = aValue;
        _size++;
    }

    /**
     * Removes the item at index.
     */
    public T remove(int anIndex)
    {
        T oldValue = _array[anIndex];

        // Shift remaining elements in
        System.arraycopy(_array, anIndex + 1, _array, anIndex, _size - anIndex - 1);
        _size--;

        // Return
        return oldValue;
    }

    /**
     * Returns whether array is empty.
     */
    public boolean isEmpty()  { return _size == 0; }

    /**
     * Returns the simple array (trimmed to length).
     */
    public T[] getArray()
    {
        if (_size != _array.length)
            _array = Arrays.copyOf(_array, _size);
        return _array;
    }

    /**
     * Returns the last item.
     */
    public T getLast()  { return _size > 0 ? _array[_size - 1] : null; }

    /**
     * Returns the component class.
     */
    public Class<T> getComponentClass()  { return _compClass; }

    /**
     * Clears the list.
     */
    public void clear()  { setLength(0); }

    /**
     * Standard clone implementation.
     */
    @Override
    protected ObjectArray<T> clone()
    {
        // Do normal version
        ObjectArray<T> clone;
        try { clone = (ObjectArray<T>) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Copy array
        clone._array = getArray().clone();

        // Return
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        // Add Name
        StringBuilder sb = new StringBuilder();
        String className = getComponentClass().getName();
        sb.append("ComponentClass=").append(className).append(", ");

        // Add Length
        sb.append("Length=").append(size());

        // Return
        return sb.toString();
    }
}
