/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
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
    public PropNode convertNativeToNode(PropObject aPropObj)
    {
        // Create new PropNode
        PropNode propNode = new PropNode(aPropObj, this);

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

            // Handle Relation
            if (prop.isRelation())
                convertNativeToNodeForPropRelation(propNode, prop, nativeValue);

                // Handle Simple property (not relation)
            else convertNativeToNodeForPropSimple(propNode, prop, nativeValue);
        }
    }

    /**
     * Converts and adds given native relation value (PropObject/PropObject[]) to given PropNode for given Prop.
     */
    protected void convertNativeToNodeForPropRelation(PropNode propNode, Prop prop, Object nativeValue)
    {
        // Convert native relation value to PropNode
        Object nodeValue = convertNativeToNodeForPropRelationImpl(propNode, prop, nativeValue);

        // If nodeValue is empty PropNode or array and Prop.DefaultValue is EMPTY_OBJECT, skip
        if (PropUtils.isEmptyNodeOrArray(nodeValue) && prop.getDefaultValue() == PropObject.EMPTY_OBJECT)
            return;

        // Add prop/value
        propNode.addNativeAndNodeValueForPropName(prop, nativeValue, nodeValue);
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
            PropNode propNode = convertNativeToNode(propObject);
            return propNode;
        }

        // Handle List
        if (nativeValue instanceof List) {
            List<?> list = (List<?>) nativeValue;
            Object[] array = list.toArray();
            return convertNativeToNodeForPropRelationArray(array);
        }

        // Handle array
        if (nativeValue.getClass().isArray())
            return convertNativeToNodeForPropRelationArray(nativeValue);

        // Return original object (assumed to be primitive)
        return nativeValue;
    }

    /**
     * Returns an array of nodes or primitives for given array.
     */
    private Object convertNativeToNodeForPropRelationArray(Object nativeArray)
    {
        // Get array and create PropNode array
        Object[] array = (Object[]) nativeArray;
        PropNode[] propNodes = new PropNode[array.length];

        // Iterate over native array objects and try to create/set PropNode for each
        for (int i = 0; i < array.length; i++) {
            PropObject propObject = (PropObject) array[i];
            propNodes[i] = convertNativeToNode(propObject);
        }

        // Return
        return propNodes;
    }

    /**
     * Adds a given native simple value (String, Number, etc.) to given PropNode for given Prop.
     */
    protected void convertNativeToNodeForPropSimple(PropNode propNode, Prop prop, Object nativeValue)
    {
        // If String-codeable, get coded String and return
        if (StringCodec.SHARED.isCodeable(nativeValue)) {

            // Get coded string
            String stringValue = StringCodec.SHARED.codeString(nativeValue);

            // If empty array and Prop.DefaultValue is EMPTY_OBJECT, skip
            if (prop.isArray() && stringValue.equals("[]") && prop.getDefaultValue() == PropObject.EMPTY_OBJECT)
                return;

            // Add prop/value
            propNode.addNativeAndNodeValueForPropName(prop, nativeValue, stringValue);
        }

        // Otherwise complain
        else System.err.println("PropArchiver.convertNativeToNode: Value not codeable: " + nativeValue.getClass());
    }

    /**
     * Returns an unarchived PropObject for given PropNode.
     */
    public PropObject convertNodeToNative(PropNode aPropNode)
    {
        return null;
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
}
