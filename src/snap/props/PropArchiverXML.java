/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;

/**
 * A PropArchiver subclass specifically to convert to/from XML.
 */
public class PropArchiverXML extends PropArchiverX {

    /**
     * Constructor.
     */
    public PropArchiverXML()
    {
        super();
        _formatConverter = new XMLFormatConverter();
    }

    /**
     * Converts a PropObject to XML.
     */
    public XMLElement writePropObjectToXML(PropObject aPropObject)
    {
        // Convert native to node
        PropNode propNode = convertNativeToNode(null, aPropObject);

        // Convert node to XML
        String propName = propNode.getClassName();
        XMLElement xml = (XMLElement) convertNodeToFormatNode(propName, propNode);

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
        PropNode propNode = convertFormatNodeToNode(null, null, anElement);

        // Convert PropNode (graph) to PropObject
        PropObject propObject = convertNodeToNative(propNode);

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

    /**
     * Returns a copy of the given object using archival.
     */
    public <T extends PropObject> T copy(T aPropObject)
    {
        XMLElement xml = writePropObjectToXML(aPropObject);
        return (T) readPropObjectFromXML(xml);
    }

    /**
     * This FormatNode implementation allows PropArchiver to read/write to XML.
     */
    public static class XMLFormatConverter implements PropArchiverX.FormatConverter<Object> {

        /**
         * Creates a format node for given prop name.
         */
        public Object createFormatNode(String aPropName)
        {
            return new XMLElement(aPropName);
        }

        /**
         * Creates a format array node for given prop name and array.
         */
        public Object createFormatArrayNode(String aPropName, Object arrayObj)
        {
            String arrayStr = StringCodec.SHARED.codeString(arrayObj);
            XMLElement arrayXML = new XMLElement(aPropName);
            arrayXML.setValue(arrayStr);
            return arrayXML;
        }

        /**
         * Return child property keys.
         */
        @Override
        public String[] getChildKeys(Object aNode)
        {
            // Handle XMLElement
            if (aNode instanceof XMLElement) {
                XMLElement xml = (XMLElement) aNode;
                int attrCount = xml.getAttributeCount();
                int elemCount = xml.getElementCount();
                String[] keys = new String[attrCount + elemCount];
                for (int i = 0; i < attrCount; i++)
                    keys[i] = xml.getAttribute(i).getName();
                for (int i = 0; i < elemCount; i++)
                    keys[i + attrCount] = xml.getElement(i).getName();
                return keys;
            }

            // Return not found
            System.err.println("XMLFormatConverter.getChildKeys: Unexpected node: " + aNode);
            return new String[0];
        }

        /**
         * Return child property value for given key.
         */
        @Override
        public Object getChildNodeForKey(Object aNode, String aName)
        {
            // Handle XMLElement
            if (aNode instanceof XMLElement) {

                // Look for attribute
                XMLElement xml = (XMLElement) aNode;
                Object value = xml.getAttribute(aName);
                if (value != null)
                    return value;

                // Look for element
                value = xml.getElement(aName);
                if (value != null)
                    return value;

                // Special Class_Key support: XML can exclude explicit Class key if matches name
                if (aName.equals(CLASS_KEY))
                    return xml.getName();
            }

            // Return not found
            System.err.println("XMLFormatConverter.getChildNodeForKey: Unexpected node: " + aNode);
            return null;
        }

        /**
         * Returns the node value as string.
         */
        @Override
        public String getNodeValueAsString(Object aNode)
        {
            // Handle Attribute
            if (aNode instanceof XMLAttribute)
                return ((XMLAttribute) aNode).getValue();

            // Handle Element
            if (aNode instanceof XMLElement)
                return ((XMLElement) aNode).getValue();

            // Handle String (Probably Class_Key)
            if (aNode instanceof String)
                return (String) aNode;

            // Return not found
            System.err.println("XMLFormatConverter.getNodeValueAsString: Unexpected node: " + aNode);
            return null;
        }

        /**
         * Returns array of nodes for a format node.
         */
        public Object[] getNodeValueAsArray(Object anArrayNode)
        {
            // Handle Element
            if (anArrayNode instanceof XMLElement)
                return ((XMLElement) anArrayNode).getElements().toArray();

            // Handle unexpected
            System.err.println("XMLFormatConverter.getNodeValueAsArray: Unexpected array node: " + anArrayNode);
            return null;
        }

        /**
         * Sets a node value for given key.
         */
        public void setNodeValueForKey(Object aNode, String aKey, Object aValue)
        {
            // Handle Element
            if (aNode instanceof XMLElement) {
                XMLElement xml = (XMLElement) aNode;

                // Add node value
                if (aValue instanceof XMLElement)
                    xml.addElement((XMLElement) aValue);

                // Add simple value
                else {

                    // Special support for Class_Key: Can skip if matches element name
                    if (aKey == CLASS_KEY && aValue.equals(xml.getName()))
                        return;

                    // Add value
                    xml.add(aKey, aValue);
                }
            }

            // Handle unexpected
            else System.err.println("XMLFormatConverter.setNodeValueForKey: Unexpected node: " + aNode);
        }

        /**
         * Adds a node array item for given array key.
         */
        public void addNodeArrayItemForKey(Object aNode, String aKey, Object aValue)
        {
            // Handle Element
            if (aNode instanceof XMLElement) {
                XMLElement xml = (XMLElement) aNode;

                // Get array element (create/add if missing)
                XMLElement arrayXML = xml.getElement(aKey);
                if (arrayXML == null) {
                    arrayXML = new XMLElement(aKey);
                    xml.addElement(arrayXML);
                }

                // Add item
                if (aValue instanceof XMLElement)
                    arrayXML.addElement((XMLElement) aValue);
                else System.err.println("XMLFormatConverter.addNodeArrayItemForKey: Unexpected array item node: " + aNode);
            }

            // Handle unexpected
            else System.err.println("XMLFormatConverter.addNodeArrayItemForKey: Unexpected array node: " + aNode);
        }
    }
}
