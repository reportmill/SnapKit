/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;
import snap.web.WebURL;

import java.util.List;

/**
 * A PropArchiver subclass specifically to convert to/from XML.
 */
public class PropArchiverXML extends PropArchiver {

    /**
     * Constructor.
     */
    public PropArchiverXML()
    {
        super();
    }

    /**
     * Converts a PropObject to XML.
     */
    public XMLElement writePropObjectToXml(PropObject aPropObject)
    {
        // Convert native to node
        PropMap propMap = aPropObject.getPropMapForArchiver(this);

        // Convert node to XML
        String className = aPropObject.getClass().getSimpleName();
        XMLElement xml = convertPropMapToXML(propMap, className);

        // Archive resources
        for (Resource resource : getResources()) {
            XMLElement resourceXML = new XMLElement("Resource");
            resourceXML.add("Name", resource.name());
            resourceXML.setValueBytes(resource.bytes());
            xml.add(resourceXML);
        }

        // Return
        return xml;
    }

    /**
     * Converts a PropObject to XML.
     */
    public byte[] writePropObjectToXmlBytes(PropObject aPropObject)
    {
        XMLElement xml = writePropObjectToXml(aPropObject);
        byte[] xmlBytes = xml.getBytes();
        return xmlBytes;
    }

    /**
     * Reads a PropObject from XML source URL.
     */
    public Object readPropObjectFromXmlUrl(WebURL sourceUrl)
    {
        setSourceURL(sourceUrl);
        XMLElement xml = XMLElement.readXmlFromUrl(sourceUrl);
        return readPropObjectFromXml(xml);
    }

    /**
     * Reads a PropObject from XML.
     */
    public Object readPropObjectFromXmlString(String xmlString)
    {
        XMLElement xml = XMLElement.readXmlFromString(xmlString);
        return readPropObjectFromXml(xml);
    }

    /**
     * Reads a PropObject from XML.
     */
    public Object readPropObjectFromXmlBytes(byte[] xmlBytes)
    {
        XMLElement xml = XMLElement.readXmlFromBytes(xmlBytes);
        return readPropObjectFromXml(xml);
    }

    /**
     * Reads a PropObject from XML.
     */
    public PropObject readPropObjectFromXml(XMLElement anElement)
    {
        // Probably not helpful
        anElement.setIgnoreCase(true);

        // Read resources
        readResources(anElement);

        // Read PropMap from XML
        PropMap propMap = convertXMLToPropMap(anElement);
        propMap.setXmlName(anElement.getName());

        // Convert PropMap (graph) to PropObject
        Prop prop = new Prop(anElement.getName(), Object.class, null);
        PropObject rootObject = getRootObject();
        PropObject propObject = convertPropMapToPropObject(propMap, prop, rootObject);

        // Return
        return propObject;
    }

    /**
     * Reads resources from {@literal <Resource>} elements in given xml (top-level) element, converts from ASCII encoding and
     * adds to archiver.
     */
    protected void readResources(XMLElement anElement)
    {
        // Get resources from top level <resource> tags
        for (int i = anElement.indexOf("Resource"); i >= 0; i = anElement.indexOf("Resource", i)) {

            // Get/remove current resource element
            XMLElement e = anElement.removeElement(i);

            // Get resource name and bytes
            String name = e.getAttributeValue("name");
            byte[] bytes = e.getValueBytes();

            // Add resource bytes for name
            addResource(name, bytes);
        }
    }

    /**
     * Converts a given PropMap to XML element.
     */
    private static XMLElement convertPropMapToXML(PropMap propMap, String aNodeKey)
    {
        // Create XML for PropMap
        XMLElement xml = new XMLElement(aNodeKey);

        // If PropMap.NeedsClassDeclaration, add Class attribute to XML
        if (propMap.isNeedsClassDeclaration()) {
            String className = propMap.getClassName();
            if (!className.equals(aNodeKey))
                xml.add(CLASS_KEY, className);
        }

        // Get configured Props
        String[] propNames = propMap.getPropNames();

        // Iterate over PropNames and add XML for each
        for (String propName : propNames) {

            // Get map value and whether it is prop map and/or array
            Object mapValue = propMap.getPropValue(propName);
            boolean isRelation = mapValue instanceof PropMap || mapValue instanceof PropMap[];
            boolean isArray = mapValue != null && mapValue.getClass().isArray();

            // Handle null
            if (mapValue == null)
                xml.add(propName, "null");

                // Handle Relation prop
            else if (isRelation) {

                // Handle array
                if (isArray && mapValue instanceof PropMap[] arrayPropMaps) {
                    XMLElement arrayXML = new XMLElement(propName);
                    for (PropMap arrayNode : arrayPropMaps) {
                        String className = arrayNode.getClassName();
                        XMLElement arrayNodeXML = convertPropMapToXML(arrayNode, className);
                        arrayXML.addElement(arrayNodeXML);
                    }
                    xml.addElement(arrayXML);
                }

                // Handle simple relation
                else if (mapValue instanceof PropMap relationPropMap) {
                    XMLElement nodeXML = convertPropMapToXML(relationPropMap, propName);
                    xml.addElement(nodeXML);
                }
            }

            // Handle primitive array
            else if (isArray) {
                String arrayStr = propMap.getPropValueAsString(propName);
                XMLElement arrayXML = new XMLElement(propName);
                arrayXML.setValue(arrayStr);
                xml.addElement(arrayXML);
            }

            // Handle String (non-Node) prop
            else {
                String stringValue = propMap.getPropValueAsString(propName);
                xml.add(propName, stringValue);
            }
        }

        // Return
        return xml;
    }

    /**
     * Converts a given XML element to PropMap.
     */
    private static PropMap convertXMLToPropMap(XMLElement anElement)
    {
        // Create PropMap for XML element
        PropMap propMap = new PropMap();

        // Get attributes
        List<XMLAttribute> attributes = anElement.getAttributes();

        // Iterate over attributes and add to PropMap
        for (XMLAttribute attribute : attributes) {

            String propName = attribute.getName();
            propMap.setPropValue(propName, attribute.getValue());
        }

        // Get elements
        List<XMLElement> elements = anElement.getElements();

        // Iterate over elements and add to PropMap
        for (XMLElement element : elements) {

            // If child element is definitely an array element, convert to PropMap array and set that
            if (isDefinitelyArrayElement(element)) {
                PropMap[] arrayNodes = convertXMLToPropMapArray(element);
                propMap.setPropValue(element.getName(), arrayNodes);
                continue;
            }

            // If element has value, assume it is primitive array
            String propName = element.getName();
            String xmlValue = element.getValue();
            if (xmlValue != null) {
                propMap.setPropValue(propName, xmlValue);
                continue;
            }

            // Get child xml as PropMap and set in parent
            PropMap relation = convertXMLToPropMap(element);
            relation.setXmlName(propName);
            propMap.setPropValue(propName, relation);
        }

        // Return xml
        return propMap;
    }

    /**
     * Returns whether given XML element is definitely an array. Only true if any child element has redundant name.
     */
    private static boolean isDefinitelyArrayElement(XMLElement anElement)
    {
        // If element has attributes or value, it isn't array
        if (anElement.getAttributeCount() > 0 || anElement.getValue() != null)
            return false;

        // If child elements don't have distinct names, it's an array
        List<XMLElement> elements = anElement.getElements();
        int elementCount = elements.size();
        long distinctCount = elements.stream().map(xml -> xml.getName()).distinct().count();
        return distinctCount != elementCount;
    }

    /**
     * Returns an array of PropMaps
     */
    private static PropMap[] convertXMLToPropMapArray(XMLElement anElement)
    {
        // Get elements and create PropMaps array
        List<XMLElement> elements = anElement.getElements();
        int elementCount = elements.size();
        PropMap[] propMaps = new PropMap[elementCount];

        // Iterate over elements, convert each and add to PropMap array
        for (int i = 0; i < elementCount; i++) {

            // Get child xml as PropMap and set in parent
            XMLElement element = elements.get(i);
            PropMap relation = convertXMLToPropMap(element);
            String propName = element.getName();
            relation.setXmlName(propName);

            // Add relation to array
            propMaps[i] = relation;
        }

        // Return
        return propMaps;
    }
}
