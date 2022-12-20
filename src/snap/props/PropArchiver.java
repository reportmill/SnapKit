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

    // A map of names to Class names, for unarchival
    private Map<String,Class<?>>  _classMap;

    // Resources
    private Resource[]  _resources = new Resource[0];

    // A helper class to archive common SnapKit classes (Font, Color, etc.)
    protected PropArchiverHpr  _helper;

    // Constant for special Class key
    public static final String CLASS_KEY = "Class";

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
    protected PropNode convertNativeToNode(PropObject aPropObj, Prop aProp)
    {
        // Create new PropNode
        PropNode propNode = new PropNode();
        String className = aPropObj.getClass().getSimpleName();
        propNode.setClassName(className);

        // Configure PropNode.NeedsClassDeclaration
        boolean needsClassDeclaration = PropUtils.isClassDeclarationNeededForObjectAndProp(aPropObj, aProp);
        if (needsClassDeclaration)
            propNode.setNeedsClassDeclaration(true);

        // Get props for archival and iterate
        Prop[] props = aPropObj.getPropsForArchival();

        // Get PropChanger Props
        //Prop[] propChangerProps = Stream.of(props).filter(prop -> prop.isPropChanger()).toArray(size -> new Prop[size]);
        //if (propChangerProps.length > 0)
        //    convertNativeToNodeForProps(aPropObj, propNode, propChangerProps);

        // Iterate over props and add native/node values for each to PropNode
        convertNativeToNodeForProps(aPropObj, propNode, props);

        // Handle optional extra props for archival - hook to allow additional props based on main props
        Prop[] propsExtra = aPropObj.getPropsForArchivalExtra();
        if (propsExtra != null)
            convertNativeToNodeForProps(aPropObj, propNode, propsExtra);

        // Return
        return propNode;
    }

    /**
     * Returns a PropNode for given PropObject.
     */
    protected void convertNativeToNodeForProps(PropObject aPropObj, PropNode aPropNode, Prop[] theProps)
    {
        // Iterate over props and add node value for each to PropNode
        for (Prop prop : theProps) {

            // If prop hasn't changed, just skip
            String propName = prop.getName();
            if (aPropObj.isPropDefault(propName))
                continue;

            // Get object value from PropObject.PropName
            Object nativeValue = aPropObj.getPropValue(propName);
            Object nodeValue = nativeValue;

            // Handle relation
            if (prop.isRelation()) {

                // Convert relation
                nodeValue = convertNativeToNodeForPropRelation(prop, nativeValue);

                // Handle Prop.Default EMPTY_OBJECT: If nodeValue is empty PropNode, clear value
                if (prop.getDefaultValue() == PropObject.EMPTY_OBJECT && nodeValue instanceof PropNode) {
                    PropNode propNode = (PropNode) nodeValue;
                    if (propNode.isEmpty())
                        nodeValue = null;
                }
            }

            // If nodeValue, add to PropNode
            if (nodeValue != null)
                aPropNode.setPropValue(prop.getName(), nodeValue);
        }
    }

    /**
     * Converts given native relation object to PropNode/PropNode[].
     */
    protected Object convertNativeToNodeForPropRelation(Prop aProp, Object nativeValue)
    {
        // Handle null
        if (nativeValue == null)
            return null;

        // Handle Array
        if (aProp.isArray()) {

            // Get array
            Object[] array;
            if (nativeValue instanceof List) {
                List<?> list = (List<?>) nativeValue;
                array = list.toArray();
            }
            else array = (Object[]) nativeValue;

            // Iterate over native array objects and try to create/set PropNode for each
            PropNode[] propNodes = new PropNode[array.length];
            for (int i = 0; i < array.length; i++) {
                PropObject propObject = (PropObject) array[i];
                propNodes[i] = convertNativeToNode(propObject, aProp);
            }

            // Return
            return propNodes;
        }

        // Swap in PropObjectProxy if needed
        PropObject proxy = _helper.getProxyForObject(nativeValue);
        if (proxy != null)
            nativeValue = proxy;

        // Handle PropObject
        PropObject propObject = (PropObject) nativeValue;
        PropNode propNode = convertNativeToNode(propObject, aProp);

        // Return
        return propNode;
    }

    /**
     * Converts a PropNode (graph) to PropObject.
     */
    protected PropObject convertNodeToNative(PropNode propNode, Prop aProp)
    {
        return convertNodeToNative(propNode, aProp, null);
    }

    /**
     * Converts a PropNode (graph) to PropObject.
     */
    protected PropObject convertNodeToNative(PropNode propNode, Prop aProp, PropObject aPropObject)
    {
        // Get PropObject
        PropObject propObject = aPropObject;
        if (propObject == null)
            propObject = createPropObjectForPropNode(propNode, aProp);

        // Get PropNode props
        String[] propNames = propNode.getPropNames();

        // Iterate over props and convert each to native
        for (String propName : propNames) {

            // Skip special Class key
            if (propName.equals(CLASS_KEY)) continue;

            // Get node value
            Prop prop = propObject.getPropForName(propName);
            if (prop == null) { System.err.println("PropArchiver.convertNodeToNative: Unknown prop: " + propName); continue; }
            Object nodeValue = propNode.getPropValue(propName);

            // Get native value
            Object nativeValue = null;

            // Handle Relation
            if (prop.isRelation()) {

                // Handle Relation array
                if (prop.isArray()) {

                    // If node is PropNode, get PropNode values as array (since XML can't differentiate lists)
                    if (nodeValue instanceof PropNode)
                        nodeValue = ((PropNode) nodeValue).getPropValuesAsArray();

                    // Get relation node array
                    PropNode[] relationNodeArray = (PropNode[]) nodeValue;

                    // Create native array for prop
                    Class<?> nativeArrayClass = prop.getPropClass();
                    Class<?> nativeCompClass = nativeArrayClass.getComponentType();
                    nativeValue = Array.newInstance(nativeCompClass, relationNodeArray.length);

                    // Fill native array
                    for (int i = 0; i < relationNodeArray.length; i++) {

                        // Get array node and array object
                        PropNode relationNode = relationNodeArray[i];
                        Object relationNative = convertNodeToNative(relationNode, prop, null);

                        // If proxy, swap in real
                        if (relationNative instanceof PropObjectProxy)
                            relationNative = ((PropObjectProxy<?>) relationNative).getReal();

                        // Set in nativeValue array
                        Array.set(nativeValue, i, relationNative);
                    }
                }

                // Handle Relation
                else if (nodeValue instanceof PropNode) {

                    // Get relation node
                    PropNode relationNode = (PropNode) nodeValue;

                    // If Prop.Preexisting, use preexisting object
                    PropObject relationObjPreexisting = null;
                    if (prop.isPreexisting())
                        relationObjPreexisting = (PropObject) propObject.getPropValue(propName);

                    // Convert to native (if PropObjectProxy, swap for real)
                    nativeValue = convertNodeToNative(relationNode, prop, relationObjPreexisting);
                    if (nativeValue instanceof PropObjectProxy)
                        nativeValue = ((PropObjectProxy<?>) nativeValue).getReal();
                }
            }

            // Handle primitive property
            else {

                // Set native value
                nativeValue = nodeValue;

                // If string, see if it needs to be decoded
                if (nodeValue instanceof String) {
                    Class<?> propClass = prop.getPropClass();
                    nativeValue = StringCodec.SHARED.decodeString((String) nodeValue, propClass);
                }
            }

            // Set value in prop object
            if (!prop.isPreexisting())
                propObject.setPropValue(prop.getName(), nativeValue);
        }

        // Return
        return propObject;
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
     * Creates a PropObject for PropNode.
     */
    protected PropObject createPropObjectForPropNode(PropNode aPropNode, Prop aProp)
    {
        // Get PropObject class
        Class<?> propObjClass = getPropObjectClassForPropNode(aPropNode, aProp);
        if (propObjClass != null)
            return createPropObjectForClass(propObjClass);

        // Complain and return
        System.err.println("PropArchiver.createPropObjectForPropNode: Undetermined class for node: " + aPropNode);
        return null;
    }

    /**
     * Returns a PropObject class for PropNode.
     */
    protected Class<?> getPropObjectClassForPropNode(PropNode aPropNode, Prop aProp)
    {
        // If Class prop set, try that
        String className = aPropNode.getPropValueAsString(CLASS_KEY);
        if (className != null) {
            Class<?> cls = getClassForName(className);
            if (cls != null)
                return cls;
        }

        // Try XML name
        String xmlName = aPropNode.getXmlName();
        if (xmlName != null) {
            Class<?> propClass = getClassForName(xmlName);
            if (propClass != null)
                return propClass;
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
     * Convenience newInstance.
     */
    protected PropObject createPropObjectForClass(Class<?> aClass)
    {
        // See if we have proxy
        PropObject proxyObject = _helper.getProxyForClass(aClass);
        if (proxyObject != null)
            return proxyObject;

        Object propObject;
        try { propObject = aClass.newInstance(); }
        catch (InstantiationException | IllegalAccessException e) { throw new RuntimeException(e + " for: " + aClass); }

        // Return
        return (PropObject) propObject;
    }

    /**
     * Returns a copy of the given PropObject using archival.
     */
    public <T extends PropObject> T copyPropObject(T aPropObject)
    {
        // Convert PropObject to PropNode
        PropNode propNode = convertNativeToNode(aPropObject, null);

        // Convert back - not sure I need to create prop
        Class<?> propObjClass = aPropObject.getClass();
        Prop prop = new Prop(propObjClass.getSimpleName(), propObjClass, null);
        T copy = (T) convertNodeToNative(propNode, prop);

        // Return
        return copy;
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
        // Iterate over resources and return resource matching name
        for (Resource resource : _resources)
            if (resource.getName().equals(aName))
                return resource;

        // Return not found
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
        public String getName()  { return _name; }

        // Returns resource bytes
        public byte[] getBytes()  { return _bytes; }

        // Standard equals implementation
        public boolean equalsBytes(byte[] bytes)
        {
            // Check length
            if (bytes.length != _bytes.length) return false;

            // Check bytes
            for (int i = 0, iMax = bytes.length; i < iMax; i++)
                if (bytes[i] != _bytes[i])
                    return false;

            // Return true
            return true;
        }
    }
}
