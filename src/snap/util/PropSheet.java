/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;

/**
 * This class holds a set of properties for a PropObject graph. The primary purpose is to facilitate archival to file
 * (JSON/XML) such that the generated file is minimal (it only writes out properties that have changed from the default).
 */
public class PropSheet {

    // The PropObject this sheet holds properties for
    private PropObject  _propObj;

    // A map of cached key/values
    private Map<String,Object>  _propVals = new HashMap<>();

    // Properties that have been explicitly set from default
    private Set<String>  _setProps = new HashSet<>();

    // The PropDefaults this sheet uses to resolve values for unset properties
    private PropDefaults  _propDefaults;

    // Constant to represent null values
    private static final Object NULL_VALUE = new Object();

    /**
     * Constructor.
     */
    public PropSheet(PropObject aPropObj)
    {
        _propObj = aPropObj;
        _propDefaults = PropDefaults.getPropDefaultsForPropObject(aPropObj);
    }

    /**
     * Returns the PropDefaults.
     */
    public PropDefaults getPropDefaults()  { return _propDefaults; }

    /**
     * Returns the value for a given property.
     */
    public Object getPropValue(String aPropName)
    {
        // Get cached value
        Object value = _propVals.get(aPropName);

        // If null, get from defaults
        if (value == null) {

            // Get default value
            value = getPropDefault(aPropName);

            // If null, replace with NULL_VALUE
            if (value == null)
                value = NULL_VALUE;

            // Add to map
            _propVals.put(aPropName, value);
        }

        // Return value (return actual null for NULL_VALUE)
        return value != NULL_VALUE ? value : null;
    }

    /**
     * Sets the value for a given property.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        // If value already set, just return
        Object value = aValue != null ? aValue : NULL_VALUE;
        if (Objects.equals(value, getPropValue(aPropName))) return;

        // Add prop to PropVals cache and SetProps
        _propVals.put(aPropName, value);
        _setProps.add(aPropName);
    }

    /**
     * Returns whether a prop has been explicitly set.
     */
    public boolean isPropDefault(String aPropName)
    {
        return _setProps.contains(aPropName);
    }

    /**
     * Returns the prop default value.
     */
    public Object getPropDefault(String aPropName)
    {
        Object value = _propDefaults.getPropDefault(aPropName);

        // If value is INHERIT_VALUE, forward to parent
        if (value == PropDefaults.INHERIT_VALUE && _propObj != null) {
            PropObject propParent = _propObj.getPropParent();
            if (propParent != null)
                value = propParent.getPropDefault(aPropName);
        }

        // Return value
        return value;
    }

    /**
     * Returns prop value as int.
     */
    public final boolean getPropValueBool(String aPropName)
    {
        Object val = getPropValue(aPropName);
        return SnapUtils.boolValue(val);
    }

    /**
     * Returns prop value as int.
     */
    public final int getPropValueInt(String aPropName)
    {
        Object val = getPropValue(aPropName);
        return SnapUtils.intValue(val);
    }

    /**
     * Returns prop value as double.
     */
    public final double getPropValueDouble(String aPropName)
    {
        Object val = getPropDefault(aPropName);
        return SnapUtils.doubleValue(val);
    }

    /**
     * Returns prop value as String.
     */
    public final String getPropValueString(String aPropName)
    {
        Object val = getPropValue(aPropName);
        return SnapUtils.stringValue(val);
    }
}
