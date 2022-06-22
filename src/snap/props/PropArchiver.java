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

    // The current prop name
    protected String  _propName;

    // A helper class to archive common SnapKit classes (Font, Color, etc.)
    private PropArchiverHpr  _helper;

    /**
     * Constructor.
     */
    public PropArchiver()
    {
        _helper = new PropArchiverHpr();
    }

    /**
     * Returns a PropNode for given PropObject.
     */
    public PropNode convertPropObjectToPropNode(PropObject aPropObj)
    {
        // Get props
        PropSet propSet = aPropObj.getPropSet();
        Prop[] props = propSet.getArchivalProps();

        // Create new PropNode
        PropNode propNode = new PropNode(aPropObj);

        // Iterate over props and add node value for each to PropNode
        for (Prop prop : props) {

            // If prop hasn't changed, just skip
            String propName = prop.getName();
            if (aPropObj.isPropDefault(propName))
                continue;

            // Get object value from PropObject.PropName
            Object objValue = aPropObj.getPropValue(propName);

            // Get value for propNode (as PropNode or primitive)
            PropObject oldPropObj = _propObject;
            Prop oldProp = _prop;
            String oldPropName = _propName;
            _propObject = aPropObj;
            _prop = prop;
            _propName = propName;
            Object nodeValue = convertObjectToPropNodeOrPrimitive(objValue);
            _propObject = oldPropObj;
            _prop = oldProp;
            _propName = oldPropName;

            // If nodeValue is empty PropNode or array and Prop.DefaultValue is EMPTY_OBJECT, skip
            if (isEmptyObject(nodeValue) && prop.getDefaultValue() == PropObject.EMPTY_OBJECT)
                continue;

            // Add prop/value
            propNode.addPropValue(propName, nodeValue);
        }

        // Return PropNode
        return propNode;
    }

    /**
     * Converts given object to PropNode or primitive.
     */
    protected Object convertObjectToPropNodeOrPrimitive(Object anObj)
    {
        // Handle null
        if (anObj == null)
            return null;

        // Give helper first shot
        Object hprValue = _helper.convertObjectToPropNodeOrPrimitive(anObj);
        if (hprValue != null)
            return hprValue;

        // Handle PropObject
        if (anObj instanceof PropObject) {
            PropObject propObject = (PropObject) anObj;
            PropNode propNode = convertPropObjectToPropNode(propObject);
            return propNode;
        }

        // Handle List
        if (anObj instanceof List) {
            List<?> list = (List<?>) anObj;
            Object[] array = list.toArray();
            return convertArrayToPropNodeOrPrimitive(array);
        }

        // Handle array
        if (anObj.getClass().isArray())
            return convertArrayToPropNodeOrPrimitive(anObj);

        // Return original object (assumed to be primitive)
        return anObj;
    }

    /**
     * Returns an array of nodes or primitives for given array.
     */
    private Object convertArrayToPropNodeOrPrimitive(Object arrayObj)
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
            propNodes[i] = convertPropObjectToPropNode(propObject);
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
        if (anObj instanceof PropNode && ((PropNode) anObj).getPropValues().size() == 0)
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
