/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * This class manages an array of object values.
 */
public class ObjectArray<T> implements Cloneable {

    // The String array
    protected T[]  _array;

    // The array length
    protected int  _length;

    // The class
    private Class<T>  _compClass;

    /**
     * Constructor.
     */
    public ObjectArray(Class<T> aClass)
    {
        super();
        _compClass = aClass;
        _array = (T[]) Array.newInstance(aClass, 8);
    }

    /**
     * Constructor.
     */
    public ObjectArray(T[] anArray)
    {
        super();
        _array = anArray;
        _compClass = (Class<T>) anArray.getClass().getComponentType();
        _length = anArray.length;
    }

    /**
     * Returns the length.
     */
    public final int length()  { return _length; }

    /**
     * Sets the length.
     */
    public void setLength(int aValue)
    {
        // Expand components array if needed
        if (aValue >= _length)
            _array = Arrays.copyOf(_array, aValue);

        // Set length
        _length = aValue;
    }

    /**
     * Returns the Object value at index.
     */
    public T get(int anIndex)  { return _array[anIndex]; }

    /**
     * Sets the String value at index.
     */
    public void set(T aValue, int anIndex)
    {
        _array[anIndex] = aValue;
    }

    /**
     * Adds the value at index.
     */
    public void add(T aValue)
    {
        add(aValue, _length);
    }

    /**
     * Adds the value at index.
     */
    public void add(T aValue, int anIndex)
    {
        // Expand components array if needed
        if (_length == _array.length)
            _array = Arrays.copyOf(_array, Math.max(_array.length * 2, 20));

        // If index is inside current length, shift existing elements over
        if (anIndex < _length)
            System.arraycopy(_array, anIndex, _array, anIndex + 1, _length - anIndex);

        // Set value and increment length
        _array[anIndex] = aValue;
        _length++;
    }

    /**
     * Removes the float value at index.
     */
    public void removeIndex(int anIndex)
    {
        // Shift remaining elements in
        System.arraycopy(_array, anIndex + 1, _array, anIndex, _length - anIndex - 1);
        _length--;
    }

    /**
     * Returns the simple array (trimmed to length).
     */
    public T[] getArray()
    {
        if (_length != _array.length)
            _array = Arrays.copyOf(_array, _length);
        return _array;
    }

    /**
     * Returns the last item.
     */
    public T getLast()  { return _length > 0 ? _array[_length - 1] : null; }

    /**
     * Returns the component class.
     */
    public Class<T> getComponentClass()  { return _compClass; }

    /**
     * Standard clone implementation.
     */
    @Override
    protected ObjectArray<T> clone()
    {
        // Do normal version
        ObjectArray<T> clone;
        try { clone = (ObjectArray<T>) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Copy array
        clone._array = getArray();

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
        sb.append("Length=").append(length());

        // Return
        return sb.toString();
    }
}
