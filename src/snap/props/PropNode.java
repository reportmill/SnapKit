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

    // The PropArchiver associated with this node
    private PropArchiver  _archiver;

    // The native object represented by this node
    private Object  _native;

    // The ClassName, if available
    private String  _className;

    // Whether this PropNode needs to declare actual class name
    private boolean  _needsClassDeclaration;

    // A list of props configured for node
    private List<Prop>  _props = new ArrayList<>();

    // A map of prop names to PropObject values as strings
    private Map<String,Object>  _propValues = new HashMap<>();

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
     * Returns the PropObject.
     */
    public PropObject getPropObject()
    {
        return _native instanceof PropObject ? (PropObject) _native : null;
    }

    /**
     * Returns the native object.
     */
    public Object getNative()
    {
        if (_native instanceof PropObjectProxy)
            return ((PropObjectProxy) _native).getReal();
        return _native;
    }

    /**
     * Returns the native object class name.
     */
    public String getClassName()  { return _className; }

    /**
     * Returns whether this PropNode needs to declare actual class name
     */
    public boolean isNeedsClassDeclaration()  { return _needsClassDeclaration; }

    /**
     * Sets whether this PropNode needs to declare actual class name
     */
    public void setNeedsClassDeclaration(boolean aValue)
    {
        _needsClassDeclaration = aValue;
    }

    /**
     * Returns the PropSet.
     */
    public PropSet getPropSet()
    {
        // Handle PropObject
        if (_native instanceof PropObject)
            return ((PropObject) _native).getPropSet();

        // Complain and return not found
        System.err.println("PropNode.getPropSet: Not found for class: " + _native.getClass());
        return null;
    }

    /**
     * Returns the list of configured props.
     */
    public List<Prop> getProps()  { return _props; }

    /**
     * Returns the list of configured prop names.
     */
    public String[] getPropNames()
    {
        return _props.stream().map(prop -> prop.getName()).toArray(size -> new String[size]);
    }

    /**
     * Returns a node value (String, PropNode, PropNode[]) for given prop name.
     */
    public Object getPropValue(String aPropName)
    {
        return _propValues.get(aPropName);
    }

    /**
     * Sets a node value (String, PropNode, PropNode[]) for given prop name.
     */
    public void setPropValue(Prop aProp, Object nodeValue)
    {
        // Add Prop and value
        _props.add(aProp);
        String propName = aProp.getName();

        // Add to NodeValues
        _propValues.put(propName, nodeValue);
    }

    /**
     * Returns a Prop for given PropName.
     */
    public Prop getPropForName(String aName)
    {
        // Iterate over props and return if name found
        for (Prop prop : _props)
            if (prop.getName().equals(aName))
                return prop;

        // Complain since PropNode should really only know about configured props
        System.err.println("PropNode.getPropForName: Prop not found in props list: " + aName);
        PropSet propSet = getPropSet();
        return propSet.getPropForName(aName);
    }

    /**
     * Standard toString implementation.
     */
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
        // Add ClassName
        StringBuffer sb = new StringBuffer();
        String className = getClassName();
        if (className != null)
            StringUtils.appendProp(sb, "Class", className);

        // Add leaf props
        String[] propNames = getPropNames();
        StringUtils.appendProp(sb, "Props", Arrays.toString(propNames));

        // Return string
        return sb.toString();
    }
}
