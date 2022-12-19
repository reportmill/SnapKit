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

    // The PropObject represented by this node
    private PropObject  _propObject;

    // The ClassName, if available
    private String  _className;

    // Whether this PropNode needs to declare actual class name
    private boolean  _needsClassDeclaration;

    // A map of prop names to PropObject values as strings
    private Map<String,Object>  _propValues = new HashMap<>();

    // A list of props configured for node
    private List<Prop>  _props = new ArrayList<>();

    /**
     * Constructor.
     */
    public PropNode(PropObject aPropObj, PropArchiver anArchiver)
    {
        _archiver = anArchiver;
        _propObject = aPropObj;
        if (aPropObj != null)
            _className = aPropObj.getClass().getSimpleName();
    }

    /**
     * Returns the PropObject.
     */
    public PropObject getPropObject()  { return _propObject; }

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
     * Returns a Prop for given PropName.
     */
    public Prop getPropForName(String aName)
    {
        // Iterate over props and return if name found
        for (Prop prop : _props)
            if (prop.getName().equals(aName))
                return prop;

        // Return not found
        return null;
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
