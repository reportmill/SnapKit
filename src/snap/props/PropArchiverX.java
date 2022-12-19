/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import java.util.ArrayList;
import java.util.List;

/**
 * A PropArchiver subclass specifically to convert to/from XML.
 */
public abstract class PropArchiverX extends PropArchiver {

    // The FormatConverter
    protected FormatConverter<Object>  _formatConverter;

    // Constant for special Class key
    public static final String CLASS_KEY = "Class";

    /**
     * Returns an abstract format node (as defined by FormatConverter) for given PropNode.
     */
    protected Object convertNodeToFormatNode(String aPropName, PropNode aPropNode)
    {
        // Create format node for PropNode
        Object formatNode = _formatConverter.createFormatNode(aPropName);

        // If PropNode.NeedsClassDeclaration, add Class attribute to XML
        if (aPropNode.isNeedsClassDeclaration()) {
            String className = aPropNode.getClassName();
            _formatConverter.setNodeValueForKey(formatNode, CLASS_KEY, className);
        }

        // Get configured Props
        List<Prop> props = aPropNode.getProps();

        // Iterate over PropNames and add XML for each
        for (Prop prop : props) {

            // Get Node value and whether it is node and/or array
            String propName = prop.getName();
            Object nodeValue = aPropNode.getPropValue(propName);
            boolean isRelation = prop.isRelation();

            // Handle null
            if (nodeValue == null)
                _formatConverter.setNodeValueForKey(formatNode, propName, "null");

            // Handle Relation prop
            else if (isRelation)
                convertNodeToFormatNodeForRelation(formatNode, prop, nodeValue);

            // Handle primitive array
            else if (prop.isArray()) {
                PropObject propObj = aPropNode.getPropObject();
                Object projObjValue = propObj.getPropValue(propName);
                Object arrayNode = _formatConverter.createFormatArrayNode(propName, projObjValue);
                _formatConverter.setNodeValueForKey(formatNode, propName, arrayNode);
            }

            // Handle String (non-Node) prop
            else {
                String stringValue = (String) nodeValue;
                _formatConverter.setNodeValueForKey(formatNode, propName, stringValue);
            }
        }

        // Return xml
        return formatNode;
    }

    /**
     * Converts and adds a prop node relation value to abstract format node.
     */
    protected void convertNodeToFormatNodeForRelation(Object aFormatNode, Prop prop, Object nodeValue)
    {
        // Get prop info
        String propName = prop.getName();
        boolean isArray = prop.isArray();

        // Handle Node array
        if (isArray) {

            // Get array
            PropNode[] nodeArray = (PropNode[]) nodeValue;

            // Create child format array node
            //Object arrayNode = _formatConverter.createFormatArrayNode(propName);
            //_formatConverter.setNodeValueForKey(aFormatNode, propName, arrayNode);

            // Handle PropNode array
            for (PropNode childNode : nodeArray) {
                String childName = childNode.getClassName();
                Object childFormatNode = convertNodeToFormatNode(childName, childNode);
                _formatConverter.addNodeArrayItemForKey(aFormatNode, propName, childFormatNode);
            }
        }

        // Handle Node object
        else {
            PropNode propNode = (PropNode) nodeValue;
            Object childFormatNode = convertNodeToFormatNode(propName, propNode);
            _formatConverter.setNodeValueForKey(aFormatNode, propName, childFormatNode);
        }
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
            if (propName.equals(CLASS_KEY)) continue;

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
                    propNode.setPropValue(prop, nodeValue);
                }

                // Handle simple array: Read array string and add to PropNode
                else {
                    String nodeValue = _formatConverter.getNodeValueAsString(childNode);
                    propNode.setPropValue(prop, nodeValue);
                }
            }

            // Handle relation: Get node value for JSON and add to PropNode
            else {

                // Handle relation
                if (prop.isRelation()) {
                    PropNode nodeValue = convertFormatNodeToNode(propNode, prop, childNode);
                    propNode.setPropValue(prop, nodeValue);
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
        Object classNode = _formatConverter.getChildNodeForKey(aFormatNode, CLASS_KEY);
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
     * An interface to read/write to an abstract format node.
     */
    public interface FormatConverter<T> {

        /**
         * Creates a format node for given prop name.
         */
        T createFormatNode(String aPropName);

        /**
         * Creates a format array node for given prop name and array.
         */
        T createFormatArrayNode(String aPropName, Object arrayObj);

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

        /**
         * Sets a node value for given key.
         */
        void setNodeValueForKey(Object aNode, String aKey, Object aValue);

        /**
         * Adds a node array item for given array key.
         */
        void addNodeArrayItemForKey(Object aNode, String aKey, Object aValue);
    }
}
