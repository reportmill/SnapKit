package snap.props;
import snap.util.*;
import java.util.Set;

/**
 * This utility class converts PropNode to/from JSObject.
 */
public class PropNodeJSON {

    /**
     * Converts a given PropNode to JSONObject.
     */
    public static JSObject convertPropNodeToJSON(PropNode aPropNode)
    {
        // Create JSONObject for PropNode
        JSObject jsonObj = new JSObject();

        // If PropNode.NeedsClassDeclaration, add Class property to JSON object
        if (aPropNode.isNeedsClassDeclaration()) {
            String className = aPropNode.getClassName();
            jsonObj.setNativeValue(PropArchiver.CLASS_KEY, className);
        }

        // Get configured Props
        String[] propNames = aPropNode.getPropNames();

        // Iterate over PropNames and add JSON for each
        for (String propName : propNames) {

            // Get Node value and whether it is node and/or array
            Object nodeValue = aPropNode.getPropValue(propName);
            boolean isRelation = nodeValue instanceof PropNode || nodeValue instanceof PropNode[];
            boolean isArray = nodeValue != null && nodeValue.getClass().isArray();

            // Handle null
            if (nodeValue == null)
                jsonObj.setNativeValue(propName, "null");

            // Handle Relation prop
            else if (isRelation) {

                // Handle array
                if (isArray) {
                    PropNode[] arrayNodes = (PropNode[]) nodeValue;
                    JSArray jsonArray = new JSArray();
                    for (PropNode arrayNode : arrayNodes) {
                        JSObject jsonNode = convertPropNodeToJSON(arrayNode);
                        jsonArray.addValue(jsonNode);
                    }
                    jsonObj.setValue(propName, jsonArray);
                }

                // Handle simple relation
                else {
                    PropNode relNode = (PropNode) nodeValue;
                    JSObject jsonNode = convertPropNodeToJSON(relNode);
                    jsonObj.setValue(propName, jsonNode);
                }
            }

            // Handle primitive array
            else if (isArray) {
                JSArray jsonArray = new JSArray(nodeValue);
                jsonObj.setValue(propName, jsonArray);
            }

            // Handle simple prop
            else {
                jsonObj.setNativeValue(propName, nodeValue);
            }
        }

        // Return
        return jsonObj;
    }

    /**
     * Converts a given JSONObject to PropNode.
     */
    public static PropNode convertJSONToPropNode(JSObject aJSONObject)
    {
        // Create PropNode for JSONObject
        PropNode propNode = new PropNode();

        // Get attributes
        Set<String> propNames = aJSONObject.getKeyValues().keySet();

        // Iterate over properties and add to PropNode
        for (String propName : propNames) {

            // Get property value
            JSValue propValue = aJSONObject.getValue(propName);

            // Handle relation array
            if (propValue instanceof JSArray) {

                // Get JSON array
                JSArray jsonArray = (JSArray) propValue;
                int count = jsonArray.getValueCount();

                // Handle array of JSObject: Convert JSObjects to PropNode array and set
                if (count > 0 && jsonArray.getValue(0) instanceof JSObject) {

                    // Convert array
                    PropNode[] nodeArray = new PropNode[count];
                    for (int i = 0; i < count; i++) {
                        JSObject jsonObj = (JSObject) jsonArray.getValue(i);
                        PropNode node = convertJSONToPropNode(jsonObj);
                        nodeArray[i] = node;
                    }

                    // Set node array
                    propNode.setPropValue(propName, nodeArray);
                }

                // Handle simple array
                else {
                    String arrayStr = jsonArray.getValueAsString();
                    propNode.setPropValue(propName, arrayStr);
                }
            }

            // Handle Object
            else if (propValue instanceof JSObject) {
                JSObject jsonObject = (JSObject) propValue;
                PropNode childNode = convertJSONToPropNode(jsonObject);
                propNode.setPropValue(propName, childNode);
            }

            // Handle value
            else {
                Object nativeValue = propValue.getNative();
                propNode.setPropValue(propName, nativeValue);
            }
        }

        // Return
        return propNode;
    }
}
