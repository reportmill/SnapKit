/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class to represent an archived PlotObject.
 */
public class PropNode {

    // The prop value
    private Object  _value;

    // A map of property names to child nodes
    private Map<String,Object>  _children = new TreeMap<>();

    // A constant to represent null value
    public static final Object NULL_VALUE = new Object();

    /**
     * Constructor.
     */
    public PropNode(Object aValue)
    {
        _value = aValue;
    }

    /**
     * Returns the value.
     */
    public Object getValue()  { return _value; }

    /**
     * Adds a key/value child.
     */
    public void addPropValue(String aPropName, Object aValue)
    {
        Object value = aValue != null ? aValue : NULL_VALUE;
        _children.put(aPropName, value);
    }

    /**
     * Returns XML for PropNode.
     */
    public XMLElement getXML(String aName)
    {
        XMLElement xml = new XMLElement(aName);

        // Iterate over child entries and add XML attribute or child element
        for (Map.Entry<String,Object> entry : _children.entrySet()) {

            // Get prop name/value
            String propName = entry.getKey();
            Object propValue = entry.getValue();

            // Handle PropNode
            if (propValue instanceof PropNode) {
                PropNode propNode = (PropNode) propValue;
                XMLElement propXML = propNode.getXML(propName);
                xml.addElement(propXML);
            }

            // Handle array
            if (propValue instanceof Object[]) {
                Object[] array = (Object[]) propValue;
                XMLElement propXML = new XMLElement(propName);
                xml.addElement(propXML);

                // Handle array of PropNode
                if (array.length > 0 && array[0] instanceof PropNode) {
                    for (Object obj : array) {
                        PropNode childNode = (PropNode) obj;
                        XMLElement childXML = childNode.getXML(propName);
                        propXML.addElement(childXML);
                    }
                }

                // Handle array of primitive
                else {
                    String valStr = Arrays.toString(array);
                    propXML.setValue(valStr);
                }
            }

            // Handle primitive
            else xml.add(propName, propValue);
        }

        // Return xml
        return xml;
    }

}
