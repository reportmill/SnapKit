/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.ArrayUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class primarily converts a PropObject (graph) to/from a PropMap (graph).
 *
 * The graph of PropMaps can easily be converted to/from XML, JSON, etc.
 */
public class PropArchiver {

    // A map of names to Class names, for unarchival
    private Map<String,Class<?>> _classMap;

    // The root object (for unarchival, optional)
    private PropObject _rootObject;

    // Resources
    private Resource[] _resources = new Resource[0];

    // A helper class to archive common SnapKit classes (Font, Color, etc.)
    protected PropArchiverHpr _helper;

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
     * Returns the object to read properties into.
     */
    public PropObject getRootObject()  { return _rootObject; }

    /**
     * Sets the object to read properties into.
     */
    public void setRootObject(PropObject rootObject)  { _rootObject = rootObject; }

    /**
     * Returns a PropMap for given PropObject.
     */
    protected PropMap convertPropObjectToPropMap(PropObject aPropObj)
    {
        // Create new PropMap
        PropMap propMap = new PropMap();
        String className = aPropObj.getClass().getSimpleName();
        propMap.setClassName(className);

        // Get props for archival and add values for each to PropMap
        List<Prop> childProps = aPropObj.getPropSet().getArchivalProps();

        // Iterate over props and add node value for each to PropMap
        for (Prop childProp : childProps) {

            // If prop hasn't changed, just skip
            String propName = childProp.getName();
            if (aPropObj.isPropDefault(propName))
                continue;

            // Get object value from PropObject.PropName
            Object nativeValue = aPropObj.getPropValue(propName);
            if (nativeValue == null)
                continue;

            // Handle relation
            if (childProp.isRelation()) {

                // Handle relation array: Convert to PropMap[] and add to PropMap
                if (childProp.isArray()) {
                    Object[] array = nativeValue instanceof List<?> list ? list.toArray() : (Object[]) nativeValue;
                    PropMap[] relPropMaps = ArrayUtils.map(array, relObj -> convertPropObjectRelationObjectToPropMap(relObj, childProp), PropMap.class);
                    propMap.setPropValue(childProp.getName(), relPropMaps);
                }

                // Handle relation object: Convert to prop map and add to PropMap if not null or EMPTY_OBJECT
                else {
                    PropMap relationPropMap = convertPropObjectRelationObjectToPropMap(nativeValue, childProp);
                    if (childProp.getDefaultValue() != PropObject.EMPTY_OBJECT || !relationPropMap.isEmpty())
                        propMap.setPropValue(childProp.getName(), relationPropMap);
                }
            }

            // Handle simple value: Just add to PropMap
            else propMap.setPropValue(childProp.getName(), nativeValue);
        }

        // Return
        return propMap;
    }

    /**
     * Converts given prop object relation object to PropMap.
     */
    private PropMap convertPropObjectRelationObjectToPropMap(Object relationObj, Prop relationProp)
    {
        // Swap in PropObjectProxy if needed
        PropObject proxy = _helper.getProxyForObject(relationObj);
        if (proxy != null)
            relationObj = proxy;

        // Handle PropObject
        PropObject propObject = (PropObject) relationObj;
        PropMap propMap = convertPropObjectToPropMap(propObject);

        // Configure PropMap.NeedsClassDeclaration
        boolean needsClassDeclaration = PropUtils.isClassDeclarationNeededForObjectAndProp(propObject, relationProp);
        if (needsClassDeclaration)
            propMap.setNeedsClassDeclaration(true);

        // Return
        return propMap;
    }

    /**
     * Converts a PropMap (graph) to PropObject.
     */
    protected PropObject convertPropMapToPropObject(PropMap propMap, Prop aProp, PropObject aPropObject)
    {
        // Get PropObject
        PropObject propObject = aPropObject;
        if (propObject == null)
            propObject = createPropObjectForPropMap(propMap, aProp);

        // Call set prop map and return
        propObject.setPropMapForArchiver(this, propMap);
        return propObject;
    }

    /**
     * Converts a PropMap (graph) to PropObject.
     */
    protected void setPropMapForPropObject(PropMap propMap, PropObject propObject)
    {
        // Get PropMap props
        String[] propNames = propMap.getPropNames();

        // Iterate over props and convert each to native
        for (String propName : propNames) {

            // Skip special Class key
            if (propName.equals(CLASS_KEY)) continue;

            // Get node value
            Prop prop = propObject.getPropForName(propName);
            if (prop == null) { System.err.println("PropArchiver.convertNodeToNative: Unknown prop: " + propName); continue; }
            Object nodeValue = propMap.getPropValue(propName);

            // Get native value
            Object nativeValue = null;

            // Handle Relation
            if (prop.isRelation()) {

                // Handle Relation array
                if (prop.isArray()) {

                    // If nodeValue is PropMap, get values as array (since XML can't differentiate lists)
                    if (nodeValue instanceof PropMap)
                        nodeValue = ((PropMap) nodeValue).getPropValuesAsArray();

                    // If node is empty array string, replace with empty PropMap array (since JSON can't differentiate empty lists)
                    else if (nodeValue instanceof String && ((String) nodeValue).replace(" ", "").equals("[]"))
                        nodeValue = new PropMap[0];

                    // Get relation node array
                    assert (nodeValue instanceof PropMap[]);
                    PropMap[] relationNodeArray = (PropMap[]) nodeValue;

                    // Create native list or array for prop
                    Class<?> nativeArrayClass = prop.getPropClass();
                    if (List.class.isAssignableFrom(nativeArrayClass))
                        nativeValue = new ArrayList<>(relationNodeArray.length);
                    else {
                        Class<?> nativeCompClass = nativeArrayClass.getComponentType();
                        nativeValue = Array.newInstance(nativeCompClass, relationNodeArray.length);
                    }

                    // Fill native array
                    for (int i = 0; i < relationNodeArray.length; i++) {

                        // Get array node and array object
                        PropMap relationNode = relationNodeArray[i];
                        Object relationNative = convertPropMapToPropObject(relationNode, prop, null);

                        // If proxy, swap in real
                        if (relationNative instanceof PropObjectProxy)
                            relationNative = ((PropObjectProxy<?>) relationNative).getReal();

                        // Set in nativeValue array
                        if (nativeValue instanceof List)
                            ((List<Object>) nativeValue).add(relationNative);
                        else Array.set(nativeValue, i, relationNative);
                    }
                }

                // Handle Relation
                else if (nodeValue instanceof PropMap relationNode) {

                    // If Prop.Preexisting, use preexisting object
                    PropObject relationObjPreexisting = null;
                    if (prop.isPreexisting())
                        relationObjPreexisting = (PropObject) propObject.getPropValue(propName);

                    // Convert to native (if PropObjectProxy, swap for real)
                    nativeValue = convertPropMapToPropObject(relationNode, prop, relationObjPreexisting);
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
    protected Map<String,Class<?>> createClassMap()  { return new HashMap<>(); }

    /**
     * Adds a Class to class map.
     */
    public void addClassMapClass(Class<? extends PropObject> aClass)
    {
        Map<String,Class<?>> classMap = getClassMap();
        classMap.put(aClass.getSimpleName(), aClass);
    }

    /**
     * Returns a class for name.
     */
    public Class<?> getClassForName(String aName)
    {
        Map<String,Class<?>> classMap = getClassMap();
        return classMap.get(aName);
    }

    /**
     * Creates a PropObject for PropMap.
     */
    protected PropObject createPropObjectForPropMap(PropMap propMap, Prop aProp)
    {
        // Get PropObject class
        Class<?> propObjClass = getPropObjectClassForPropMap(propMap, aProp);
        if (propObjClass != null)
            return createPropObjectForClass(propObjClass);

        // Complain and return
        System.err.println("PropArchiver.createPropObjectForPropMap: Undetermined class for node: " + propMap);
        return null;
    }

    /**
     * Returns a PropObject class for PropMap.
     */
    protected Class<?> getPropObjectClassForPropMap(PropMap propMap, Prop aProp)
    {
        // If Class prop set, try that
        String className = propMap.getPropValueAsString(CLASS_KEY);
        if (className != null) {
            Class<?> cls = getClassForName(className);
            if (cls != null)
                return cls;
        }

        // Try XML name
        String xmlName = propMap.getXmlName();
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
        // Convert PropObject to PropMap
        PropMap propMap = convertPropObjectToPropMap(aPropObject);

        // Convert back - not sure I need to create prop
        Class<?> propObjClass = aPropObject.getClass();
        Prop prop = new Prop(propObjClass.getSimpleName(), propObjClass, null);
        T copy = (T) convertPropMapToPropObject(propMap, prop, null);

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
