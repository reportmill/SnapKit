/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.FormatUtils;
import snap.util.SnapUtils;
import snap.util.XMLElement;
import snap.util.XMLParser;

import java.util.Arrays;
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

        // Get PropNode entries
        Set<Map.Entry<String,Object>> propValues = aPropNode.getPropValues();

        // Iterate over child entries and add XML attribute or child element
        for (Map.Entry<String,Object> entry : propValues) {
            String propName = entry.getKey();
            Object propValue = entry.getValue();
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
        // Handle null
        if (propValue == null) {
            xml.add(propName, propValue);
        }

        // Handle PropNode
        else if (propValue instanceof PropNode) {
            PropNode propNode = (PropNode) propValue;
            XMLElement propXML = convertPropNodeToXML(propName, propNode);
            xml.addElement(propXML);
        }

        // Handle array
        else if (propValue instanceof Object[]) {

            // Get array
            Object[] array = (Object[]) propValue;

            // Create child XML
            XMLElement propXML = new XMLElement(propName);
            xml.addElement(propXML);

            // Handle PropNode array
            if (array.length > 0 && array[0] instanceof PropNode) {
                for (Object obj : array) {
                    PropNode childNode = (PropNode) obj;
                    String childName = childNode.getClassName();
                    XMLElement childXML = convertPropNodeToXML(childName, childNode);
                    propXML.addElement(childXML);
                }
            }

            // Handle primitive array
            else {
                String valStr = Arrays.toString(array);
                propXML.setValue(valStr);
            }
        }

        // Handle primitive value
        else {

            // If float/double, format to avoid many decimals
            if (propValue instanceof Double || propValue instanceof Float)
                propValue = FormatUtils.formatNum((Number) propValue);

            // Handle double array
            else if (propValue instanceof double[]) {
                String arrayStr = PropUtils.getStringForDoubleArray((double[]) propValue);
                xml.addElement(new XMLElement(propName, arrayStr));
                return;
            }

            // Add prop
            xml.add(propName, propValue);
        }
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
        return null;
    }
}
