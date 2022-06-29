/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;
import java.util.ArrayList;
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

        // Get configured Props
        List<Prop> props = aPropNode.getProps();

        // Iterate over PropNames and add XML for each
        for (Prop prop : props) {

            // Get Node value and whether it is node and/or array
            String propName = prop.getName();
            Object nodeValue = aPropNode.getNodeValueForPropName(propName);
            boolean isRelation = prop.isRelation();

            // Handle null
            if (nodeValue == null) {
                xml.add(propName, "null");
            }

            // Handle Relation prop
            else if (isRelation)
                convertNodeToXMLForPropRelation(xml, prop, nodeValue);

            // Handle String (non-Node) prop
            else {
                String stringValue = (String) nodeValue;
                convertNodeToXMLForPropSimple(xml, prop, stringValue);
            }
        }

        // Return xml
        return xml;
    }

    /**
     * Converts and adds a prop node relation value to XML.
     */
    protected void convertNodeToXMLForPropRelation(XMLElement xml, Prop prop, Object nodeValue)
    {
        // Get prop info
        String propName = prop.getName();
        boolean isArray = prop.isArray();

        // Handle Node array
        if (isArray) {

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
     * Converts and adds a prop node string value to XML.
     */
    protected void convertNodeToXMLForPropSimple(XMLElement xml, Prop prop, String stringValue)
    {
        // Get prop info
        String propName = prop.getName();
        boolean isArray = prop.isArray();

        // Handle primitive array
        if (isArray) {
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
        // Configure PropNode (graph) from XML
        anElement.setIgnoreCase(true);
        PropNode propNode = convertXMLToNode(null, null, anElement);

        // Convert PropNode (graph) to PropObject
        PropObject propObject = propNode.getPropObject();

        // Return
        return propObject;
    }

    /**
     * Reads a PropNode from XML.
     */
    protected PropNode convertXMLToNode(PropObject aParent, Prop aProp, XMLElement anElement)
    {
        // Create PropObject for element
        PropObject propObject = createPropObjectForXML(aProp, anElement);

        // Create PropNode for propObject
        PropNode propNode = new PropNode(propObject, this);

        // Get list of configured XML attributes
        List<XMLAttribute> attributes = anElement.getAttributes();

        // Iterate over XML attributes and add node/native value for each
        for (XMLAttribute attr : attributes) {

            // Get attribute name/value
            String attrName = attr.getName();
            String attrValue = attr.getValue();

            // Get prop
            Prop prop = propObject.getPropForName(attrName);
            if (prop == null) {
                System.out.println("PropArchiverXML.convertXMLToNode: No prop found for attr name: " + attrName);
                continue;
            }

            // Add to
            Object nativeValue = convertNodeToNativeForPropSimple(propNode, prop, attrValue);

            // Add prop native/node values
            propNode.addNativeAndNodeValueForPropName(prop, nativeValue, attrValue);
        }

        // Get list of configured XML elements
        List<XMLElement> elements = anElement.getElements();

        // Iterate over XML elements and add node/native value for each
        for (XMLElement xml : elements) {

            // XML name
            String xmlName = xml.getName();

            // Get prop
            Prop prop = propObject.getPropForName(xmlName);
            if (prop == null) {
                System.out.println("PropArchiverXML.convertXMLToNode: No prop found for xml name: " + xmlName);
                continue;
            }

            // Handle array
            if (prop.isArray()) {
                // Read nodeValue array
                PropNode[] nodeValue = convertXMLToNodeForXMLArray(aParent, prop, xml);

                // Get native value
                PropObject[] nativeValue = new PropObject[nodeValue.length];
                for (int i = 0; i < nodeValue.length; i++)
                    nativeValue[i] = nodeValue[i].getPropObject();

                // Add prop and native/node values
                propNode.addNativeAndNodeValueForPropName(prop, nativeValue, nodeValue);
            }

            // Handle simple
            else {
                // Read NodeValue for xml
                PropNode nodeValue = convertXMLToNode(aParent, prop, xml);

                // Get native value
                Object nativeValue = nodeValue.getNative();

                // Add prop and native/node values
                propNode.addNativeAndNodeValueForPropName(prop, nativeValue, nodeValue);
            }
        }

        // Return
        return propNode;
    }

    /**
     * Reads a PropNode from XML.
     */
    protected PropNode[] convertXMLToNodeForXMLArray(PropObject aParent, Prop aProp, XMLElement anElement)
    {
        // Get list of configured XML elements
        List<XMLElement> elements = anElement.getElements();

        // Create list
        List<PropNode> propNodes = new ArrayList<>(elements.size());

        // Iterate over XML elements and add node/native value for each
        for (XMLElement xml : elements) {

            PropNode propNode = convertXMLToNode(aParent, aProp, xml);
            if (propNode != null)
                propNodes.add(propNode);
        }

        // Return
        return propNodes.toArray(new PropNode[0]);
    }

    /**
     * Creates a PropObject for XML element.
     */
    protected PropObject createPropObjectForXML(Prop aProp, XMLElement anElement)
    {
        // If Class attribute set, try that
        String xmlClassName = anElement.getAttributeValue("Class");
        if (xmlClassName != null) {
            Class<?> cls = getClassForName(xmlClassName);
            if (cls != null)
                return createPropObjectForClass(cls);
        }

        // If Prop is Array, try XML name next
        if (aProp != null && aProp.isArray()) {
            String xmlName = anElement.getName();
            Class<?> xmlNameClass = getClassForName(xmlName);
            if (xmlNameClass != null)
                return createPropObjectForClass(xmlNameClass);
        }

        // Try Prop class attribute
        Class<?> propClass = aProp != null ? aProp.getPropClass() : null;
        if (propClass != null) {

            // If array, swap for component class
            if (propClass.isArray())
                propClass = propClass.getComponentType();

            return createPropObjectForClass(propClass);
        }

        // Try PropName as ClassMap name
        String propName = aProp != null ? aProp.getName() : null;
        propClass = getClassForName(propName);
        if (propClass != null) {
            return createPropObjectForClass(propClass);
        }

        // Try element name
        String xmlName = anElement.getName();
        Class<?> xmlNameClass = getClassForName(xmlName);
        if (xmlNameClass != null)
            return createPropObjectForClass(xmlNameClass);

        // Complain and return
        System.err.println("PropArchiverXML.createPropObjectForXML: Undetermined class for XML: " + anElement);
        return null;
    }

    /**
     * Convenience newInstance.
     */
    private PropObject createPropObjectForClass(Class<?> aClass)
    {
        Object propObject;
        try { propObject = aClass.newInstance(); }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // See if we need proxy
        PropObject propObject1 = _helper.getProxyForObject(propObject);
        if (propObject1 != null)
            propObject = propObject1;

        // Return
        return (PropObject) propObject;
    }
}
