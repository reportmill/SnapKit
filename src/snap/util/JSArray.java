/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * This JSValue subclass represents an array.
 */
public class JSArray extends JSValue {

    // The map to hold key/values
    private List<JSValue>  _values = new ArrayList<>();

    /**
     * Constructor.
     */
    public JSArray()
    {
        super();
    }

    /**
     * Constructor.
     */
    public JSArray(Object anArray)
    {
        int length = Array.getLength(anArray);
        for (int i = 0; i < length; i++) {
            Object item = Array.get(anArray, i);
            addValue(new JSValue(item));
        }
    }

    /**
     * Returns the values.
     */
    public List<JSValue> getValues()  { return _values; }

    /**
     * Returns the number of values.
     */
    public int getValueCount()  { return _values.size(); }

    /**
     * Returns a value at given index.
     */
    public JSValue getValue(int anIndex)
    {
        return _values.get(anIndex);
    }

    /**
     * Add a value for key.
     */
    public void addValue(JSValue aValue)
    {
        addValue(aValue, _values.size());
    }

    /**
     * Add a value for key.
     */
    public void addValue(JSValue aValue, int anIndex)
    {
        _values.add(anIndex, aValue);
    }

    /**
     * Add a native value.
     */
    public void addNativeValue(Object aValue)
    {
        JSValue valueJS = new JSValue(aValue);
        addValue(valueJS);
    }

    /**
     * Returns this JS object as a native object.
     */
    public List<?> getNative()
    {
        // Create native list
        List<Object> list = new ArrayList<>(_values.size());

        // Iterate over items and replace with native
        for (int i = 0, iMax = _values.size(); i < iMax; i++) {
            JSValue item = _values.get(i);
            Object nativeItem = item.getNative();
            list.add(nativeItem);
        }

        // Return
        return list;
    }

    /**
     * Returns the value as String if type is String.
     */
    public String getValueAsString()
    {
        return toString();
    }
}
