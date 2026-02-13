/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;
import snap.web.WebURL;

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
        PropNode propNode = convertNativeToNode(aPropObject, null);

        // Convert node to XML
        String className = aPropObject.getClass().getSimpleName();
        XMLElement xml = PropNodeXML.convertPropNodeToXML(propNode, className);

        // Archive resources
        for (Resource resource : getResources()) {
            XMLElement resourceXML = new XMLElement("Resource");
            resourceXML.add("Name", resource.getName());
            resourceXML.setValueBytes(resource.getBytes());
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
     * Reads a PropObject from XML source.
     */
    public Object readPropObjectFromXmlUrl(WebURL sourceUrl)
    {
        XMLElement xml = XMLElement.readXmlFromUrl(sourceUrl);
        return readPropObjectFromXml(xml);
    }

    /**
     * Reads a PropObject from XML.
     */
    public Object readPropObjectFromXmlBytes(byte[] theBytes)
    {
        XMLElement xml = XMLElement.readXmlFromBytes(theBytes);
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

        // Read PropNode from XML
        PropNode propNode = PropNodeXML.convertXMLToPropNode(anElement);
        propNode.setXmlName(anElement.getName());

        // Convert PropNode (graph) to PropObject
        Prop prop = new Prop(anElement.getName(), Object.class, null);
        PropObject rootObject = getRootObject();
        PropObject propObject = convertNodeToNative(propNode, prop, rootObject);

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
}
