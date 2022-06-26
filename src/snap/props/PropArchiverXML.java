/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A PropArchiver subclass specifically to convert to/from XML.
 */
public class PropArchiverXML extends PropArchiver {

    /**
     * Converts a PropObject to XML.
     */
    public XMLElement convertPropObjectToXML(PropObject aPropObject)
    {
        PropNode propNode = convertPropObjectToPropNode(aPropObject);
        String propName = propNode.getClassName();
        XMLElement xml = convertPropNodeToXML(propName, propNode);
        return xml;
    }

    /**
     * Converts a PropObject to XML.
     */
    public byte[] convertPropObjectToXMLBytes(PropObject aPropObject)
    {
        XMLElement xml = convertPropObjectToXML(aPropObject);
        byte[] xmlBytes = xml.getBytes();
        return xmlBytes;
    }

    /**
     * Returns XML for PropNode.
     */
    protected XMLElement convertPropNodeToXML(String aPropName, PropNode aPropNode)
    {
        // Create XML element for PropNode
        XMLElement xml = new XMLElement(aPropName);

        // Get list of configured PropNames
        List<String> propNames = aPropNode.getPropNames();

        // Iterate over PropNames and add XML for each
        for (String propName : propNames) {
            Object propValue = aPropNode.getNativeValueForPropName(propName);
            addNameAndValueToXML(xml, propName, propValue);
        }

        // Return xml
        return xml;
    }

    /**
     * Adds XML for given prop name/value pair.
     */
    protected void addNameAndValueToXML(XMLElement xml, String propName, Object propValue)
    {
        // If String-codeable, get coded String and set in XML
        if (StringCodec.SHARED.isCodeable(propValue)) {

            // Get Coded String
            String valueString = StringCodec.SHARED.codeString(propValue);

            // Handle primitive array
            if (propValue != null && propValue.getClass().isArray()) {
                XMLElement propXML = new XMLElement(propName);
                xml.addElement(propXML);
                propXML.setValue(valueString);
            }

            // Handle primitive value
            else xml.add(propName, valueString);
        }

        // Handle PropNode
        else if (propValue instanceof PropNode) {
            PropNode propNode = (PropNode) propValue;
            XMLElement propXML = convertPropNodeToXML(propName, propNode);
            xml.addElement(propXML);
        }

        // Handle array of PropNode
        else if (isPropNodeArray(propValue)) {

            // Get array
            Object[] array = (Object[]) propValue;

            // Create child XML
            XMLElement propXML = new XMLElement(propName);
            xml.addElement(propXML);

            // Handle PropNode array
            for (Object obj : array) {
                PropNode childNode = (PropNode) obj;
                String childName = childNode.getClassName();
                XMLElement childXML = convertPropNodeToXML(childName, childNode);
                propXML.addElement(childXML);
            }
        }

        // Otherwise complain
        else System.err.println("PropArchiver.addNameAndValueToXML: Value not codeable: " + propValue.getClass());
    }

    /**
     * Reads a PropObject from XML source.
     */
    public Object readPropObjectFromXMLSource(Object aSource)
    {
        // Get bytes from source - if not found or empty, complain
        byte[] xmlBytes = SnapUtils.getBytes(aSource);
        if (xmlBytes == null || xmlBytes.length == 0)
            throw new RuntimeException("XMLArchiver.readObject: Cannot read source: " + aSource);

        // Try to get SourceURL from source
        //if (getSourceURL() == null) { WebURL surl = WebURL.getURL(aSource); setSourceURL(surl); }

        // Read from bytes and return
        return readPropObjectFromXMLBytes(xmlBytes);
    }

    /**
     * Reads a PropObject from XML String.
     */
    public Object readPropObjectFromXMLString(String xmlString)
    {
        try {
            XMLParser xmlParser = new XMLParser();
            XMLElement xml = xmlParser.parseXMLFromString(xmlString);
            return readPropObjectFromXML(xml);
        }

        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Reads a PropObject from XML.
     */
    public Object readPropObjectFromXMLBytes(byte[] theBytes)
    {
        XMLElement xml = XMLElement.readFromXMLBytes(theBytes);
        return readPropObjectFromXML(xml);
    }

    /**
     * Reads a PropObject from XML.
     */
    public PropObject readPropObjectFromXML(XMLElement anElement)
    {
        PropNode propNode = readPropNodeFromXML(anElement);
        PropObject propObject = convertPropNodeToPropObject(propNode);
        return propObject;
    }

    /**
     * Reads a PropNode from XML.
     */
    protected PropNode readPropNodeFromXML(XMLElement anElement)
    {
        return null;
    }

    /**
     * Returns whether given object is PropNode array.
     */
    private static boolean isPropNodeArray(Object anObj)
    {
        Class<?> objClass = anObj.getClass();
        if (!objClass.isArray()) return false;
        Object[] array = (Object[]) anObj;
        Class<?> compClass = objClass.getComponentType();
        if (PropNode.class.isAssignableFrom(compClass))
            return true;
        Object comp0 = array.length > 0 ? array[0] : null;
        return comp0 instanceof PropNode;
    }
}
