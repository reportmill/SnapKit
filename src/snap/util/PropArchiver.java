/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

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
        PropNode propNode = new PropNode(null);

        // Iterate over properties and add to prop node
        for (String propName : propNames) {

            // If prop hasn't changed, just skip
            if (propSheet.isPropDefault(propName))
                continue;

            // Get PropValue
            Object value = aPropObj.getPropValue(propName);

            // If value is PropObject, convert to recurse to get as PropNode
            if (value instanceof PropObject)
                value = propObjectToPropNode((PropObject) value);

            // If value is List, convert
            else if (value instanceof List) {
                List list = (List) value;

                // Handle empty list
                if (list.isEmpty())
                    value = new PropNode[0];

                // Handle list of PropObject
                else if (list.get(0) instanceof PropObject) {
                    PropNode[] propNodes = new PropNode[list.size()];
                    for (int i = 0; i < list.size(); i++)
                        propNodes[i] = propObjectToPropNode((PropObject) list.get(i));
                    value = propNodes;
                }

                // Handle list of primitives
                else value = list.toArray();
            }

            // Add prop/value
            propNode.addPropValue(propName, value);
        }

        // Return PropNode
        return propNode;
    }

    /**
     * Returns a PropObject for given PropNode.
     */
    public PropObject propNodeToPropObject(PropNode aPropNode)
    {
        return null;
    }

}
