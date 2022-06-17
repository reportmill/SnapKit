/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

import java.util.List;

/**
 * This class archives/unarchives PropObjects to/from a tree of generic PropNodes, which can then easily be
 * written/read from file as XML or JSON.
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
    public PropNode propObjectToPropNode(PropObject aPropObj)
    {
        // Get properties
        PropSheet propSheet = aPropObj.getPropSheet();
        PropDefaults propDefaults = propSheet.getPropDefaults();
        String[] propNames = propDefaults.getPropNames();

        // Create new PropNode
        PropNode propNode = new PropNode(null);
        propNode.setClassName(aPropObj.getClass().getSimpleName());

        // Iterate over properties and add to prop node
        for (String propName : propNames) {

            // If prop hasn't changed, just skip
            if (propSheet.isPropDefault(propName))
                continue;

            // Get PropValue
            Object value = aPropObj.getPropValue(propName);
            if (value instanceof List)
                value = ((List) value).toArray();

            // If value is PropObject, convert to recurse to get as PropNode
            if (value instanceof PropObject)
                value = propObjectToPropNode((PropObject) value);

            // If value is list/array, convert
            else if (value instanceof List || value instanceof Object[])
                value = vectorConversion(value);

            // Add prop/value
            propNode.addPropValue(propName, value);
        }

        // Iterate over relations
        String[] relationNames = propDefaults.getRelationNames();
        for (String relationName : relationNames) {

            // Handle single relation
            Object relObj = aPropObj.getPropValue(relationName);
            if (relObj instanceof PropObject) {
                PropObject relPropObj = relObj instanceof PropObject ? (PropObject) relObj : null;
                PropNode relPropNode = propObjectToPropNode(relPropObj);
                propNode.addPropValue(relationName, relPropNode);
            }

            // Handle relation list
            else if (relObj instanceof List || relObj instanceof Object[]) {
                Object[] relNodes = vectorConversion(relObj);
                propNode.addPropValue(relationName, relNodes);
            }
        }

        // Return PropNode
        return propNode;
    }

    /**
     * Returns an array of nodes or primatives for given array.
     */
    private Object[] vectorConversion(Object aListOrArray)
    {
        Object[] array = aListOrArray instanceof List ? ((List) aListOrArray).toArray() : (Object[]) aListOrArray;
        if (array.length == 0 || !(array[0] instanceof PropObject))
            return array;

        PropNode[] propNodes = new PropNode[array.length];
        for (int i = 0; i < array.length; i++) {
            PropObject propObject = (PropObject) array[i];
            propNodes[i] = propObjectToPropNode(propObject);
        }

        return propNodes;
    }

    /**
     * Returns a PropObject for given PropNode.
     */
    public PropObject propNodeToPropObject(PropNode aPropNode)
    {
        return null;
    }

}
