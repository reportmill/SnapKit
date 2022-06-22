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

    /**
     * Constructor.
     */
    public PropArchiver()
    {

    }

    /**
     * Returns a PropNode for given PropObject.
     */
    public PropNode convertPropObjectToPropNode(PropObject aPropObj)
    {
        // Get props
        PropSet propSet = aPropObj.getPropSet();
        Prop[] props = propSet.getProps();

        // Create new PropNode
        PropNode propNode = new PropNode(null);
        propNode.setClassName(aPropObj.getClass().getSimpleName());

        // Iterate over props and add node value for each to PropNode
        for (Prop prop : props) {

            // If prop hasn't changed, just skip
            String propName = prop.getName();
            if (aPropObj.isPropDefault(propName))
                continue;

            // Get object value from PropObject.PropName
            Object objValue = aPropObj.getPropValue(propName);

            // Get value for propNode (as PropNode or primitive)
            Object nodeValue = convertObjectToPropNodeOrPrimitive(objValue);

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
        if (anObj.getClass().isArray()) {
            Object[] array = (Object[]) anObj;
            return convertArrayToPropNodeOrPrimitive(array);
        }

        // Return original object (assumed to be primitive)
        return anObj;
    }

    /**
     * Returns an array of nodes or primitives for given array.
     */
    private Object[] convertArrayToPropNodeOrPrimitive(Object[] array)
    {
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

}
