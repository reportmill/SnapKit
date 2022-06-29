/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;
import java.util.List;

/**
 * A PropArchiver subclass specifically to convert to/from XML.
 */
public class PropArchiverXML extends PropArchiver {

    /**
     * Converts a PropObject to XML.
     */
    public XMLElement convertPropObjectToXML(PropObject aPropObject)
    {
        // Convert native to node
        PropNode propNode = convertNativeToNode(aPropObject);

        // Convert node to XML
        String propName = propNode.getClassName();
        XMLElement xml = convertNodeToXML(propName, propNode);

        // Return
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
    protected XMLElement convertNodeToXML(String aPropName, PropNode aPropNode)
    {
        // Create XML element for PropNode
        XMLElement xml = new XMLElement(aPropName);

        // Get list of configured PropNames
        List<String> propNames = aPropNode.getPropNames();

        // Iterate over PropNames and add XML for each
        for (String propName : propNames) {

            // Get Node value and whether it is node and/or array
            Object nodeValue = aPropNode.getNodeValueForPropName(propName);
            boolean isArrayProp = aPropNode.isArrayProp(propName);
            boolean isRelationProp = aPropNode.isRelationProp(propName);

            // Handle null
            if (nodeValue == null) {
                xml.add(propName, "null");
            }

            // Handle Relation prop
            else if (isRelationProp)
                addNodeToXML(xml, propName, nodeValue, isArrayProp);

            // Handle String (non-Node) prop
            else {
                String stringValue = (String) nodeValue;
                addNodeStringToXML(xml, propName, stringValue, isArrayProp);
            }
        }

        // Return xml
        return xml;
    }

    /**
     * Adds a node prop to XML.
     */
    protected void addNodeToXML(XMLElement xml, String propName, Object nodeValue, boolean isArrayProp)
    {
        // Handle Node array
        if (isArrayProp) {

            // Get array
            PropNode[] nodeArray = (PropNode[]) nodeValue;

            // Create child XML
            XMLElement propXML = new XMLElement(propName);
            xml.addElement(propXML);

            // Handle PropNode array
            for (PropNode childNode : nodeArray) {
                String childName = childNode.getClassName();
                XMLElement childXML = convertNodeToXML(childName, childNode);
                propXML.addElement(childXML);
            }
        }

        // Handle Node object
        else {
            PropNode propNode = (PropNode) nodeValue;
            XMLElement propXML = convertNodeToXML(propName, propNode);
            xml.addElement(propXML);
        }
    }

    /**
     * Adds a String (non-node) prop to XML.
     */
    protected void addNodeStringToXML(XMLElement xml, String propName, String stringValue, boolean isArrayProp)
    {
        // Handle primitive array
        if (isArrayProp) {
            XMLElement propXML = new XMLElement(propName);
            xml.addElement(propXML);
            propXML.setValue(stringValue);
        }

        // Handle primitive value
        else xml.add(propName, stringValue);
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
}
