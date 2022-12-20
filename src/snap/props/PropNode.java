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

    // The ClassName, if available
    private String  _className;

    // The XML name of node if from XML
    private String  _xmlName;

    // Whether this PropNode needs to declare actual class name
    private boolean  _needsClassDeclaration;

    // A map of prop names to PropObject values as strings
    private Map<String,Object>  _propValues = new LinkedHashMap<>();

    /**
     * Constructor.
     */
    public PropNode()
    {
        super();
    }

    /**
     * Returns the native object class name.
     */
    public String getClassName()  { return _className; }

    /**
     * Sets the native object class name.
     */
    public void setClassName(String aName)  { _className = aName; }

    /**
     * Returns the XML name of node if from XML.
     */
    public String getXmlName()  { return _xmlName; }

    /**
     * Sets the XML name of node if from XML.
     */
    public void setXmlName(String aValue)  { _xmlName = aValue; }

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
    public void setPropValue(String aPropName, Object nodeValue)
    {
        _propValues.put(aPropName, nodeValue);
    }

    public void setPropValue(Prop aProp, Object nodeValue)
    {
        throw new RuntimeException("PropNode.setPropValue(prop) Not implemented");
    }

    /**
     * Returns the prop value as string.
     */
    public String getPropValueAsString(String aPropName)
    {
        Object value = getPropValue(aPropName);
        String valueStr = StringCodec.SHARED.codeString(value);
        return valueStr;
    }

    /**
     * Returns all values as PropNode array.
     */
    public PropNode[] getPropValuesAsArray()
    {
        // Return all values as PropNode array
        Collection<Object> values = _propValues.values();
        boolean allPropNodes = values.stream().allMatch(obj -> obj instanceof PropNode);
        if (allPropNodes)
            return values.toArray(new PropNode[0]);

        // Should never happen
        System.err.println("PropNode.getPropValuesAsArray: Not all children are PropNodes");
        return new PropNode[0];
    }

    /**
     * Returns the list of configured props.
     */
    public List<Prop> getProps()  { return null; }

    /**
     * Returns the list of configured prop names.
     */
    public String[] getPropNames()
    {
        Set<String> propNamesSet = _propValues.keySet();
        return propNamesSet.toArray(new String[0]);
    }

    /**
     * Whether node is empty.
     */
    public boolean isEmpty()
    {
        return _propValues.size() == 0;
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
        if (_className != null)
            StringUtils.appendProp(sb, "Class", _className);

        // Add XmlName
        if (_xmlName != null)
            StringUtils.appendProp(sb, "XmlName", _xmlName);

        // Add leaf props
        String[] propNames = getPropNames();
        StringUtils.appendProp(sb, "Props", Arrays.toString(propNames));

        // Return string
        return sb.toString();
    }
}
