/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ArrayUtils;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * This class primarily converts a PropObject (graph) to/from a PropNode (graph).
 *
 * The graph of PropNodes can easily be converted to/from XML, JSON, etc.
 */
public class PropArchiver {

    // A helper class to archive common SnapKit classes (Font, Color, etc.)
    protected PropArchiverHpr  _helper;

    // A map of names to Class names, for unarchival
    private Map<String,Class<?>>  _classMap;

    // Resources
    private Resource[]  _resources = new Resource[0];

    /**
     * Constructor.
     */
    public PropArchiver()
    {
        _helper = new PropArchiverHpr(this);
    }

    /**
     * Returns a PropNode for given PropObject.
     */
    public PropNode convertNativeToNode(Prop prop, PropObject aPropObj)
    {
        // Create new PropNode
        PropNode propNode = new PropNode(aPropObj, this);

        // Configure PropNode.NeedsClassDeclaration
        boolean needsClassDeclaration = PropUtils.isNodeNeedsClassDeclarationForProp(propNode, prop);
        if (needsClassDeclaration)
            propNode.setNeedsClassDeclaration(true);

        // Get props for archival and iterate
        Prop[] props = aPropObj.getPropsForArchival();

        // Iterate over props and add native/node values for each to PropNode
        convertNativeToNodeForProps(aPropObj, propNode, props);

        // Handle optional extra props for archival - hook to allow additional props based on main props
        Prop[] propsExtra = aPropObj.getPropsForArchivalExtra();
        if (propsExtra != null)
            convertNativeToNodeForProps(aPropObj, propNode, propsExtra);

        // Return PropNode
        return propNode;
    }

    /**
     * Returns a PropNode for given PropObject.
     */
    protected void convertNativeToNodeForProps(PropObject aPropObj, PropNode propNode, Prop[] props)
    {
        // Iterate over props and add node value for each to PropNode
        for (Prop prop : props) {

            // If prop hasn't changed, just skip
            String propName = prop.getName();
            if (aPropObj.isPropDefault(propName))
                continue;

            // Get object value from PropObject.PropName
            Object nativeValue = aPropObj.getPropValue(propName);
            boolean isSimple = !prop.isRelation();

            // Get node value
            Object nodeValue = isSimple ?
                    convertNativeToNodeForPropSimple(prop, nativeValue) :
                    convertNativeToNodeForPropRelation(propNode, prop, nativeValue);

            // If nodeValue, add to PropNode
            if (nodeValue != null)
                propNode.addNodeValueForProp(prop, nodeValue);
        }
    }

    /**
     * Adds a given native simple value (String, Number, etc.) to given PropNode for given Prop.
     */
    protected String convertNativeToNodeForPropSimple(Prop prop, Object nativeValue)
    {
        // If String-codeable, get coded String and return
        if (StringCodec.SHARED.isCodeable(nativeValue)) {

            // Get coded string
            String stringValue = StringCodec.SHARED.codeString(nativeValue);

            // If empty array and Prop.DefaultValue is EMPTY_OBJECT, return null
            if (prop.isArray() && stringValue.equals("[]") && prop.getDefaultValue() == PropObject.EMPTY_OBJECT)
                return null;

            // Return
            return stringValue;
        }

        // Otherwise complain and return null
        System.err.println("PropArchiver.convertNativeToNodeForPropSimple: Value not codeable: " + nativeValue.getClass());
        return null;
    }

    /**
     * Converts and adds given native relation value (PropObject/PropObject[]) to given PropNode for given Prop.
     */
    protected Object convertNativeToNodeForPropRelation(PropNode propNode, Prop prop, Object nativeValue)
    {
        // Convert native relation value to PropNode
        Object nodeValue = convertNativeToNodeForPropRelationImpl(propNode, prop, nativeValue);

        // If nodeValue is empty PropNode or array and Prop.DefaultValue is EMPTY_OBJECT, skip
        if (PropUtils.isEmptyNodeOrArray(nodeValue) && prop.getDefaultValue() == PropObject.EMPTY_OBJECT)
            return null;

        // Return
        return nodeValue;
    }

    /**
     * Converts given native relation object to PropNode/PropNode[].
     */
    protected Object convertNativeToNodeForPropRelationImpl(PropNode aPropNode, Prop aProp, Object nativeValue)
    {
        // Handle null
        if (nativeValue == null)
            return null;

        // Give helper first shot
        PropObject proxy = _helper.getProxyForObject(nativeValue);
        if (proxy != null)
            return convertNativeToNodeForPropRelationImpl(aPropNode, aProp, proxy);

        // Handle PropObject
        if (nativeValue instanceof PropObject) {
            PropObject propObject = (PropObject) nativeValue;
            PropNode propNode = convertNativeToNode(aProp, propObject);
            return propNode;
        }

        // Handle List
        if (nativeValue instanceof List) {
            List<?> list = (List<?>) nativeValue;
            Object[] array = list.toArray();
            return convertNativeToNodeForPropRelationArray(aProp, array);
        }

        // Handle array
        if (nativeValue.getClass().isArray())
            return convertNativeToNodeForPropRelationArray(aProp, nativeValue);

        // Return original object (assumed to be primitive)
        return nativeValue;
    }

    /**
     * Returns an array of nodes or primitives for given array.
     */
    private Object convertNativeToNodeForPropRelationArray(Prop prop, Object nativeArray)
    {
        // Get array and create PropNode array
        Object[] array = (Object[]) nativeArray;
        PropNode[] propNodes = new PropNode[array.length];

        // Iterate over native array objects and try to create/set PropNode for each
        for (int i = 0; i < array.length; i++) {
            PropObject propObject = (PropObject) array[i];
            propNodes[i] = convertNativeToNode(prop, propObject);
        }

        // Return
        return propNodes;
    }

    /**
     * Converts a PropNode (graph) to PropObject.
     */
    protected PropObject convertNodeToNative(PropNode propNode)
    {
        // Get PropObject
        PropObject propObject = propNode.getPropObject();

        // Get PropNode props
        List<Prop> props = propNode.getProps();

        // Iterate over props and convert each to native
        for (Prop prop : props) {

            // Get node value
            Object nodeValue = propNode.getNodeValueForPropName(prop.getName());

            // Get native value
            Object nativeValue = null;

            // Handle simple
            if (nodeValue instanceof String)
                nativeValue = convertNodeToNativeForPropSimple(propNode, prop, (String) nodeValue);

            // Handle Relation
            else if (nodeValue instanceof PropNode) {

                // Get relation node
                PropNode relationNode = (PropNode) nodeValue;

                // Convert to native (if PropObjectProxy, swap for real)
                nativeValue = convertNodeToNative(relationNode);
                if (nativeValue instanceof PropObjectProxy)
                    nativeValue = ((PropObjectProxy) nativeValue).getReal();
            }

            // Handle Relation array
            else if (nodeValue instanceof PropNode[]) {

                // Get relation node array
                PropNode[] relationNodeArray = (PropNode[]) nodeValue;

                // Create native array for prop
                Class<?> nativeArrayClass = prop.getDefaultPropClass();
                Class<?> nativeCompClass = nativeArrayClass.getComponentType();
                nativeValue = Array.newInstance(nativeCompClass, relationNodeArray.length);

                // Fill native array
                for (int i = 0; i < relationNodeArray.length; i++) {
                    PropNode relationNode = relationNodeArray[i];
                    Object relationNative = convertNodeToNative(relationNode);
                    if (relationNative instanceof PropObjectProxy)
                        relationNative = ((PropObjectProxy) relationNative).getReal();
                    Array.set(nativeValue, i, relationNative);
                }
            }

            // Complain
            else System.err.println("PropArchiver: convertNodeToNative: Illegal node value: " + nodeValue.getClass());

            // Set value in prop object
            if (!prop.isPreexisting())
                propObject.setPropValue(prop.getName(), nativeValue);
        }

        // Return
        return propObject;
    }

    /**
     * Returns a native simple value (String, Number, etc.) to given PropNode for given Node string value.
     */
    protected Object convertNodeToNativeForPropSimple(PropNode propNode, Prop prop, String nodeValue)
    {
        // Get coded string
        Class<?> propClass = prop.getPropClass();
        Object nativeValue = StringCodec.SHARED.decodeString(nodeValue, propClass);

        // Return
        return nativeValue;
    }

    /**
     * Adds a node value to a prop node.
     */
    protected void addNodeValueForProp(PropNode propNode, Prop prop, Object nodeValue)
    {
        // Add value
        propNode.addNodeValueForProp(prop, nodeValue);

        // If Prop.PropChanger, push to propObject
        if (prop.isPropChanger() && (nodeValue instanceof String || nodeValue == null)) {

            // Convert node value to native
            Object nativeValue = convertNodeToNativeForPropSimple(propNode, prop, (String) nodeValue);

            // Set native value in PropObject
            PropObject propObject = propNode.getPropObject();
            propObject.setPropValue(prop.getName(), nativeValue);
        }
    }

    /**
     * Returns the map of names to class names.
     */
    public Map<String,Class<?>> getClassMap()
    {
        // If already set, just return
        if (_classMap != null) return _classMap;

        // Create, set, return
        Map<String,Class<?>> classMap = createClassMap();
        return _classMap = classMap;
    }

    /**
     * Creates the map of names to class names.
     */
    protected Map<String,Class<?>> createClassMap()  { return null; }

    /**
     * Returns a class for name.
     */
    public Class<?> getClassForName(String aName)
    {
        Map<String,Class<?>> classMap = getClassMap();
        return classMap.get(aName);
    }

    /**
     * Returns the list of optional resources associated with this archiver.
     */
    public Resource[] getResources()  { return _resources; }

    /**
     * Returns an individual resource associated with this archiver, by index.
     */
    public Resource getResource(int anIndex)  { return _resources[anIndex]; }

    /**
     * Returns an individual resource associated with this archiver, by name.
     */
    public Resource getResourceForName(String aName)
    {
        for (Resource resource : _resources)
            if (resource.getName().equals(aName))
                return resource;
        return null;
    }

    /**
     * Adds a byte array resource to this archiver (only if absent).
     */
    public String addResource(String aName, byte[] theBytes)
    {
        // If resource has already been added, just return its name
        for (Resource resource : _resources)
            if (resource.equalsBytes(theBytes))
                return resource.getName();

        // Add new resource
        _resources = ArrayUtils.add(_resources, new Resource(aName, theBytes));

        // Return name
        return aName;
    }

    /**
     * This inner class represents a named resource associated with an archiver.
     */
    public static class Resource {

        // The resource name
        private String  _name;

        // The resource bytes
        private byte[]  _bytes;

        // Creates new resource for given bytes and name
        public Resource(String aName, byte[] theBytes)
        {
            _name = aName;
            _bytes = theBytes;
        }

        // Returns resource name
        public String getName()
        {
            return _name;
        }

        // Returns resource bytes
        public byte[] getBytes()
        {
            return _bytes;
        }

        // Standard equals implementation
        public boolean equalsBytes(byte[] bytes)
        {
            if (bytes.length != _bytes.length) return false;
            for (int i = 0, iMax = bytes.length; i < iMax; i++)
                if (bytes[i] != _bytes[i])
                    return false;
            return true;
        }
    }
}
