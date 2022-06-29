/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import java.util.List;

/**
 * This class primarily converts a PropObject (graph) to/from a PropNode (graph).
 *
 * The graph of PropNodes can easily be converted to/from XML, JSON, etc.
 */
public class PropArchiver {

    // The current prop object
    protected PropObject  _propObject;

    // The current prop
    protected Prop  _prop;

    // A helper class to archive common SnapKit classes (Font, Color, etc.)
    private PropArchiverHpr  _helper;

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

        // Get props for archival
        Prop[] props = aPropObj.getPropsForArchival();

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
                addNativeRelationValueForProp(aPropObj, propNode, prop, nativeValue);

            // Handle Simple property (not relation)
            else addNativeSimpleValueForProp(propNode, prop, nativeValue);
        }

        // Get props for archival
        Prop[] propsExtra = aPropObj.getPropsForArchivalExtra();
        if (propsExtra != null) {

            // Iterate over props and add node value for each to PropNode
            for (Prop prop : propsExtra) {

                // If prop hasn't changed, just skip
                String propName = prop.getName();
                if (aPropObj.isPropDefault(propName))
                    continue;

                // Get object value from PropObject.PropName
                Object nativeValue = aPropObj.getPropValue(propName);

                // Handle Relation
                if (prop.isRelation())
                    addNativeRelationValueForProp(aPropObj, propNode, prop, nativeValue);

                    // Handle Simple property (not relation)
                else addNativeSimpleValueForProp(propNode, prop, nativeValue);
            }
        }

        // Return PropNode
        return propNode;
    }

    /**
     * Adds a given native relation value (PropObject/PropObject[]) to given PropNode for given Prop.
     */
    protected void addNativeRelationValueForProp(PropObject aParent, PropNode propNode, Prop prop, Object nativeValue)
    {
        // Cache weird state vars, set new values
        PropObject oldPropObj = _propObject;
        Prop oldProp = _prop;
        _propObject = aParent; _prop = prop;

        // Convert native relation value to PropNode
        Object nodeValue = convertNativeRelationToNode(nativeValue);

        // Restore weird state vars
        _propObject = oldPropObj; _prop = oldProp;

        // If nodeValue is empty PropNode or array and Prop.DefaultValue is EMPTY_OBJECT, skip
        if (isEmptyObject(nodeValue) && prop.getDefaultValue() == PropObject.EMPTY_OBJECT)
            return;

        // Add prop/value
        propNode.addNativeAndNodeValueForPropName(prop, nativeValue, nodeValue);
    }

    /**
     * Adds a given native simple value (String, Number, etc.) to given PropNode for given Prop.
     */
    protected void addNativeSimpleValueForProp(PropNode propNode, Prop prop, Object nativeValue)
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
     * Converts given native relation object to PropNode or primitive.
     */
    protected Object convertNativeRelationToNode(Object anObj)
    {
        // Handle null
        if (anObj == null)
            return null;

        // Give helper first shot
        PropObject proxy = _helper.getProxyForObject(anObj);
        if (proxy != null)
            return convertNativeRelationToNode(proxy);

        // Handle PropObject
        if (anObj instanceof PropObject) {
            PropObject propObject = (PropObject) anObj;
            PropNode propNode = convertNativeToNode(propObject);
            return propNode;
        }

        // Handle List
        if (anObj instanceof List) {
            List<?> list = (List<?>) anObj;
            Object[] array = list.toArray();
            return convertNativeArrayToNode(array);
        }

        // Handle array
        if (anObj.getClass().isArray())
            return convertNativeArrayToNode(anObj);

        // Return original object (assumed to be primitive)
        return anObj;
    }

    /**
     * Returns an array of nodes or primitives for given array.
     */
    private Object convertNativeArrayToNode(Object arrayObj)
    {
        // If primitive array, just return
        if (arrayObj.getClass().getComponentType().isPrimitive())
            return arrayObj;

        // Get array
        Object[] array = (Object[]) arrayObj;

        // If empty array or array components are primitive, just return
        if (array.length == 0 || !(array[0] instanceof PropObject))
            return array;

        // Create array of converted array contents
        PropNode[] propNodes = new PropNode[array.length];
        for (int i = 0; i < array.length; i++) {
            PropObject propObject = (PropObject) array[i];
            propNodes[i] = convertNativeToNode(propObject);
        }

        // Return
        return propNodes;
    }

    /**
     * Returns a PropObject for given PropNode.
     */
    public PropObject convertPropNodeToPropObject(PropNode aPropNode)
    {
        return null;
    }

    /**
     * Returns whether given object is empty PropNode or array.
     */
    private static boolean isEmptyObject(Object anObj)
    {
        // Handle null?
        if (anObj == null)
            return false;

        // Handle PropNode with PropValues size 0
        if (anObj instanceof PropNode && ((PropNode) anObj).getPropNames().size() == 0)
            return true;

        // Handle array with length 0
        if (anObj.getClass().isArray()) {
            Class compClass = anObj.getClass().getComponentType();
            if (Object.class.isAssignableFrom(compClass))
                return ((Object[]) anObj).length == 0;
            else if (float.class == compClass)
                return ((float[]) anObj).length == 0;
            else if (double.class == compClass)
                return ((double[]) anObj).length == 0;
            else if (int.class == compClass)
                return ((int[]) anObj).length == 0;
            else System.err.println("PropArchiver.isEmptyObject: Unknown comp class: " + compClass);
        }

        // Return false since no PropNode or array with no values
        return false;
    }
}
