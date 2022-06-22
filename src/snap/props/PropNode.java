/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.StringUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class to represent a PropObject in a generic way for conversion to/from XML, JSON, etc.
 */
public class PropNode {

    // The ClassName, if available
    private String  _className;

    // A map of property names to child nodes/values
    private Map<String,Object>  _children = new LinkedHashMap<>();

    // A constant to represent null value
    public static final Object NULL_VALUE = new Object();

    /**
     * Constructor.
     */
    public PropNode(Object aValue)
    {
        if (aValue != null)
            _className = aValue.getClass().getSimpleName();
    }

    /**
     * Returns the class name.
     */
    public String getClassName()  { return _className; }

    /**
     * Sets the class name.
     */
    public void setClassName(String aName)
    {
        _className = aName;
    }

    /**
     * Returns the prop values.
     */
    public Set<Map.Entry<String,Object>> getPropValues()
    {
        return _children.entrySet();
    }

    /**
     * Returns the prop values.
     */
    public Object getPropValue(String aPropName)
    {
        Object propValue = _children.get(aPropName);
        return propValue;
    }

    /**
     * Adds a key/value child.
     */
    public void addPropValue(String aPropName, Object aValue)
    {
        Object value = aValue != null ? aValue : NULL_VALUE;
        _children.put(aPropName, value);
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
        for (Map.Entry<String,Object> entry : _children.entrySet()) {
            String propName = entry.getKey();
            Object propValue = entry.getValue();
            if (propValue instanceof PropNode) continue;
            StringUtils.appendProp(sb, propName, propValue);
        }

        // Return string
        return sb.toString();
    }
}
