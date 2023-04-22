/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This JSValue subclass represents an object.
 */
public class JSObject extends JSValue {

    // The map to hold key/values
    private Map<String, JSValue>  _keyValues = new LinkedHashMap<>();

    /**
     * Constructor.
     */
    public JSObject()
    {
        super();
    }

    /**
     * Returns the KeyValues.
     */
    public Map<String,JSValue> getKeyValues()  { return _keyValues; }

    /**
     * Returns the number of key values.
     */
    public int getValueCount()  { return _keyValues.size(); }

    /**
     * Returns a value for a key.
     */
    public JSValue getValue(String aKey)
    {
        return _keyValues.get(aKey);
    }

    /**
     * Sets a value for key.
     */
    public void setValue(String aKey, JSValue aValue)
    {
        if (aValue == null)
            _keyValues.remove(aKey);
        else _keyValues.put(aKey, aValue);
    }

    /**
     * Returns the native value for given key.
     */
    public Object getNativeValue(String aKey)
    {
        JSValue value = getValue(aKey);
        Object nativeValue = value != null ? value.getNative() : null;
        return nativeValue;
    }

    /**
     * Sets a native value.
     */
    public void setNativeValue(String aKey, Object aValue)
    {
        JSValue valueJS = new JSValue(aValue);
        setValue(aKey, valueJS);
    }

    /**
     * Returns the native value for given key.
     */
    public String getStringValue(String aKey)
    {
        Object nativeValue = getNativeValue(aKey);
        return Convert.stringValue(nativeValue);
    }

    /**
     * Returns this JS object as a native object.
     */
    @Override
    public Map<String,Object> getNative()
    {
        // Create native map
        Map<String,Object> map = new LinkedHashMap<>(_keyValues.size());

        // Iterate over KeyValues map and replace JS with native
        for (Map.Entry<String, JSValue> entry : _keyValues.entrySet()) {
            String key = entry.getKey();
            JSValue value = entry.getValue();
            Object nativeValue = value.getNative();
            map.put(key, nativeValue);
        }

        // Return
        return map;
    }

    /**
     * Returns the node as a map.
     */
    public Map<String,Object> getAsMap()
    {
        return getNative();
    }
}
