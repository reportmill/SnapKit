/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;

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
    public XMLElement writePropObjectToXML(PropObject aPropObject)
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
    public byte[] writePropObjectToXMLBytes(PropObject aPropObject)
    {
        XMLElement xml = writePropObjectToXML(aPropObject);
        byte[] xmlBytes = xml.getBytes();
        return xmlBytes;
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
        // Probably not helpful
        anElement.setIgnoreCase(true);

        // Read resources
        readResources(anElement);

        // Read PropNode from XML
        PropNode propNode = PropNodeXML.convertXMLToPropNode(anElement);
        propNode.setXmlName(anElement.getName());

        // Convert PropNode (graph) to PropObject
        Prop prop = new Prop(anElement.getName(), Object.class, null);
        PropObject propObject = convertNodeToNative(propNode, prop);

        // Return
        return propObject;
    }

    /**
     * Reads resources from <Resource> elements in given xml (top-level) element, converts from ASCII encoding and
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
