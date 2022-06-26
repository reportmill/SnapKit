/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.StringUtils;
import java.util.*;

/**
 * A class to represent a PropObject and its property values as both native and String values. This middle ground
 * greatly facilitates conversion of PropObjects to/from XML, JSON, etc.
 */
public class PropNode {

    // The native object represented by this node
    private Object  _native;

    // The PropArchiver associated with this node
    private PropArchiver  _archiver;

    // The ClassName, if available
    private String  _className;

    // A list of prop names configured for node
    private List<String>  _propNames = new ArrayList<>();

    // A map of prop names to PropObject values in native form
    private Map<String,Object>  _nativeValues = new HashMap<>();

    // A map of prop names to PropObject values as strings
    private Map<String,String>  _stringValues = new HashMap<>();

    // A constant to represent null value
    public static final Object NULL_VALUE = new Object();

    /**
     * Constructor.
     */
    public PropNode(Object aValue, PropArchiver anArchiver)
    {
        _native = aValue;
        _archiver = anArchiver;
        if (aValue != null)
            _className = aValue.getClass().getSimpleName();
    }

    /**
     * Returns the native object.
     */
    public Object getNative()  { return _native; }

    /**
     * Returns the native object class name.
     */
    public String getClassName()  { return _className; }

    /**
     * Returns the list of configured prop names.
     */
    public List<String> getPropNames()  { return _propNames; }

    /**
     * Returns a PropObject value in native form for given prop name.
     */
    public Object getNativeValueForPropName(String aPropName)
    {
        Object propValue = _nativeValues.get(aPropName);
        return propValue;
    }

    /**
     * Adds a PropObject value in native form for given prop name.
     */
    public void addNativeValueForPropName(String aPropName, Object aValue)
    {
        Object value = aValue != null ? aValue : NULL_VALUE;
        _propNames.add(aPropName);
        _nativeValues.put(aPropName, value);
    }

    /**
     * Returns a PropObject value a String for given prop name.
     */
    public String getStringValueForPropName(String aPropName)
    {
        // Get value from StringValues (just return if found)
        String propValue = _stringValues.get(aPropName);
        if (propValue != null)
            return propValue;

        // Get native value from NativeValues
        Object nativeValue = _nativeValues.get(aPropName);
        if (nativeValue != null) {

        }

        // Return
        return propValue;
    }

    /**
     * Adds a PropObject value a String for given prop name.
     */
    public void addStringValueForPropName(String aPropName, String aValue)
    {
        _propNames.add(aPropName);
        _stringValues.put(aPropName, aValue);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + "{ " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        // Add ClassName
        StringBuffer sb = new StringBuffer();
        String className = getClassName();
        if (className != null)
            StringUtils.appendProp(sb, "ClassName", className);

        // Add leaf props
        List<String> propNames = getPropNames();
        for (String propName : propNames) {
            Object propValue = getStringValueForPropName(propName);
            if (propValue == null) continue;
            StringUtils.appendProp(sb, propName, propValue);
        }

        // Return string
        return sb.toString();
    }
}
