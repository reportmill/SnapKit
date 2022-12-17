/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import java.util.ArrayList;
import java.util.List;
import snap.util.XMLAttribute;
import snap.util.XMLElement;

/**
 * A PropArchiver subclass specifically to convert to/from XML.
 */
public class PropArchiverX extends PropArchiver {

    // The FormatConverter
    private FormatConverter _formatConverter;

    /**
     * Returns XML for PropNode.
     */
    protected XMLElement convertNodeToXML(String aPropName, PropNode aPropNode)
    {
        // Create XML element for PropNode
        XMLElement xml = new XMLElement(aPropName);

        // If PropNode.NeedsClassDeclaration, add Class attribute to XML
        if (aPropNode.isNeedsClassDeclaration()) {
            String className = aPropNode.getClassName();
            if (!className.equals(aPropName))
                xml.add("Class", className);
        }

        // Get configured Props
        List<Prop> props = aPropNode.getProps();

        // Iterate over PropNames and add XML for each
        for (Prop prop : props) {

            // Get Node value and whether it is node and/or array
            String propName = prop.getName();
            Object nodeValue = aPropNode.getNodeValueForPropName(propName);
            boolean isRelation = prop.isRelation();

            // Handle null
            if (nodeValue == null)
                xml.add(propName, "null");

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
     * Reads a PropNode from abstract format node.
     */
    protected PropNode convertFormatNodeToNode(PropNode aParent, Prop aProp, Object aFormatNode)
    {
        // Create PropObject for element
        PropObject propObject = createPropObjectForFormatNode(aParent, aProp, aFormatNode);

        // Create PropNode for propObject
        PropNode propNode = new PropNode(propObject, this);

        // Get list of configured format node child property keys attributes
        String[] childNodeKeys = _formatConverter.getChildKeys(aFormatNode);

        // Iterate over child prop keys and add node/native value for each
        for (String propName : childNodeKeys) {

            // Skip special Class key
            if (propName.equals("Class")) continue;

            // Get prop
            Prop prop = propObject.getPropForName(propName);
            if (prop == null) continue; // Should never happen

            // Get value
            Object childNode = _formatConverter.getChildNodeForKey(aFormatNode, propName);

            // Handle array
            if (prop.isArray()) {

                // Handle Relation array: Get node value for JSON and add to PropNode
                if (prop.isRelation()) {
                    PropNode[] nodeValue = convertFormatToNodeForRelationArray(propNode, prop, childNode);
                    propNode.addNodeValueForProp(prop, nodeValue);
                }

                // Handle simple array: Read array string and add to PropNode
                else {
                    String nodeValue = _formatConverter.getNodeValueAsString(childNode);
                    propNode.addNodeValueForProp(prop, nodeValue);
                }
            }

            // Handle relation: Get node value for JSON and add to PropNode
            else {

                // Handle relation
                if (prop.isRelation()) {
                    PropNode nodeValue = convertFormatNodeToNode(propNode, prop, childNode);
                    propNode.addNodeValueForProp(prop, nodeValue);
                }

                // Handle simple node
                else {
                    String nodeValue = _formatConverter.getNodeValueAsString(childNode);
                    addNodeValueForProp(propNode, prop, nodeValue);
                }
            }
        }

        // Return
        return propNode;
    }

    /**
     * Reads a PropNode from Format.
     */
    protected PropNode[] convertFormatToNodeForRelationArray(PropNode aParent, Prop aProp, Object anArrayNode)
    {
        // Get array of format nodes from format array node
        Object[] arrayItems = _formatConverter.getNodeValueAsArray(anArrayNode);

        // Create PropNodes list
        List<PropNode> propNodes = new ArrayList<>(arrayItems.length);

        // Iterate over ArrayItems and add node/native value for each
        for (Object item : arrayItems) {

            PropNode propNode = convertFormatNodeToNode(aParent, aProp, item);
            if (propNode != null)
                propNodes.add(propNode);
        }

        // Return
        return propNodes.toArray(new PropNode[0]);
    }

    /**
     * Creates a PropObject for format node.
     */
    protected PropObject createPropObjectForFormatNode(PropNode aParent, Prop aProp, Object aFormatNode)
    {
        // If Prop.Preexisting, just instance from PropObject instead
        if (aProp != null && aProp.isPreexisting() && aParent != null) {
            PropObject propObject = aParent.getPropObject();
            Object existingInstance = propObject.getPropValue(aProp.getName());
            if (existingInstance instanceof PropObject)
                return (PropObject) existingInstance;
        }

        // Get PropObject class
        Class<?> propObjClass = getPropObjectClassForFormatNode(aProp, aFormatNode);
        if (propObjClass != null)
            return createPropObjectForClass(propObjClass);

        // Complain and return
        System.err.println("PropArchiverX.createPropObjectForFormatNode: Undetermined class for format node: " + aFormatNode);
        return null;
    }

    /**
     * Returns a PropObject class for format node.
     */
    protected Class<?> getPropObjectClassForFormatNode(Prop aProp, Object aFormatNode)
    {
        // If Class prop set, try that
        Object classNode = _formatConverter.getChildNodeForKey(aFormatNode, "Class");
        if (classNode != null) {
            String className = _formatConverter.getNodeValueAsString(classNode);
            Class<?> cls = getClassForName(className);
            if (cls != null)
                return cls;
        }

        // Try Prop.DefaultPropClass (If array, swap for component class)
        Class<?> propClass = aProp != null ? aProp.getDefaultPropClass() : null;
        if (propClass != null) {
            if (propClass.isArray())
                propClass = propClass.getComponentType();
            return propClass;
        }

        // Try PropName - shouldn't need this since Prop.DefaultPropClass should be set
        String propName = aProp != null ? aProp.getName() : null;
        propClass = getClassForName(propName);
        if (propClass != null)
            return propClass;

        // Return not found
        return null;
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
     * An interface to read/write to an abstract format node.
     */
    public interface FormatConverter<T> {

        /**
         * Return child property keys.
         */
        String[] getChildKeys(T aNode);

        /**
         * Return child property value for given key.
         */
        Object getChildNodeForKey(Object aNode, String aName);

        /**
         * Returns the node value as string.
         */
        String getNodeValueAsString(Object aNode);

        /**
         * Returns array of nodes for a format node.
         */
        Object[] getNodeValueAsArray(Object anArrayNode);
    }

    /**
     * This FormatNode implementation allows PropArchiver to read/write to XML.
     */
    public static class XMLFormatConverter implements FormatConverter<Object> {

        /**
         * Return child property keys.
         */
        @Override
        public String[] getChildKeys(Object aNode)
        {
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
                if (aName.equals("Class"))
                    return xml.getName();
            }

            // Return not found
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
            return null;
        }

        /**
         * Returns array of nodes for a format node.
         */
        public Object[] getNodeValueAsArray(Object anArrayNode)
        {
            if (anArrayNode instanceof XMLElement)
                return ((XMLElement) anArrayNode).getElements().toArray();
            return null;
        }
    }
}
