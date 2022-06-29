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

    // A list of props configured for node
    private List<Prop>  _props = new ArrayList<>();

    // A list of prop names configured for node
    private List<String>  _propNames = new ArrayList<>();

    // A map of prop names to PropObject values in native form
    private Map<String,Object>  _nativeValues = new HashMap<>();

    // A map of prop names to PropObject values as strings
    private Map<String,Object>  _nodeValues = new HashMap<>();

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
     * Returns the PropSet.
     */
    public PropSet getPropSet()
    {
        if (_native instanceof PropObject)
            return ((PropObject) _native).getPropSet();
        System.err.println("PropNode.getPropSet: Not found for class: " + _native.getClass());
        return null;
    }

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
    public void addNativeAndNodeValueForPropName(Prop aProp, Object nativeValue, Object nodeValue)
    {
        // Add PropName to PropNames
        _props.add(aProp);
        String propName = aProp.getName();
        _propNames.add(propName);

        // Add value to NativeValues
        _nativeValues.put(propName, nativeValue);

        // Add to NodeValues
        _nodeValues.put(propName, nodeValue);
    }

    /**
     * Returns a PropObject value a String for given prop name.
     */
    public Object getNodeValueForPropName(String aPropName)
    {
        Object nodeValue = _nodeValues.get(aPropName);
        return nodeValue;
    }

    /**
     * Adds a String or Node value for given prop name.
     */
    public void addNodeValueForPropName(String aPropName, Object aValue)
    {
        _propNames.add(aPropName);
        _nodeValues.put(aPropName, aValue);
    }

    /**
     * Returns a PropObject value a String for given prop name.
     */
    public String getStringValueForPropName(String aPropName)
    {
        // Get value from StringValues (just return if found)
        Object nodeValue = getNodeValueForPropName(aPropName);
        if (nodeValue == null || nodeValue instanceof String)
            return (String) nodeValue;

        // Complain and return
        System.err.println("PropNode.getStringValueForPropName: No node value is not string: " + nodeValue.getClass());
        return null;
    }

    /**
     * Returns a Prop for given PropName.
     */
    public Prop getPropForName(String aName)
    {
        for (Prop prop : _props)
            if (prop.getName().equals(aName))
                return prop;

        System.err.println("PropNode.getPropForName: Prop not found in props list: " + aName);
        PropSet propSet = getPropSet();
        return propSet.getPropForName(aName);
    }

    /**
     * Returns whether prop is array prop.
     */
    public boolean isArrayProp(String aPropName)
    {
        Prop prop = getPropForName(aPropName);
        return prop.isArray();
    }

    /**
     * Returns whether prop is node prop object or array.
     */
    public boolean isRelationProp(String aPropName)
    {
        Prop prop = getPropForName(aPropName);
        return prop.isRelation();
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
            StringUtils.appendProp(sb, "Class", className);

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
