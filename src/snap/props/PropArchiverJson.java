/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;
import java.util.Set;

/**
 * A PropArchiver subclass specifically to convert to/from JSON.
 */
public class PropArchiverJson extends PropArchiver {

    /**
     * Constructor.
     */
    public PropArchiverJson()
    {
        super();
    }

    /**
     * Converts a PropObject to JSON.
     */
    public JsonObject writePropObjectToJSON(PropObject aPropObject)
    {
        // Convert native to node
        PropNode propNode = convertNativeToNode(aPropObject, null);

        // Convert node to JSON
        JsonObject objectJS = convertPropNodeToJson(propNode);

        // Archive resources
        /*for (Resource resource : getResources()) {
            JsonObject resourceXML = new XMLElement("Resource");
            resourceXML.add("Name", resource.getName());
            resourceXML.setValueBytes(resource.getBytes());
            objectJS.add(resourceXML);
        }*/

        // Return
        return objectJS;
    }

    /**
     * Reads a PropObject from JSON source.
     */
    public Object readPropObjectFromJSONSource(Object aSource)
    {
        // Get bytes from source - if not found or empty, complain
        byte[] jsonBytes = SnapUtils.getBytes(aSource);
        if (jsonBytes == null || jsonBytes.length == 0)
            throw new RuntimeException("PropArchiverJS.readPropObjectFromJSONSource: Cannot read source: " + aSource);

        // Try to get SourceURL from source
        //if (getSourceURL() == null) { WebURL surl = WebURL.getURL(aSource); setSourceURL(surl); }

        // Read from bytes and return
        return readPropObjectFromJSONBytes(jsonBytes);
    }

    /**
     * Reads a PropObject from JSON String.
     */
    public Object readPropObjectFromJSONString(String jsonString)
    {
        try {
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.readString(jsonString);
            return readPropObjectFromJSON(json);
        }

        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Reads a PropObject from JSON.
     */
    public Object readPropObjectFromJSONBytes(byte[] theBytes)
    {
        String string = new String(theBytes);
        return readPropObjectFromJSONString(string);
    }

    /**
     * Reads a PropObject from JSON.
     */
    public PropObject readPropObjectFromJSON(JsonObject objectJS)
    {
        // Read resources
        readResources(objectJS);

        // Read PropNode from JSON
        PropNode propNode = convertJsonToPropNode(objectJS);

        // Convert PropNode (graph) to PropObject
        PropObject rootObject = getRootObject();
        PropObject propObject = convertNodeToNative(propNode, null, rootObject);

        // Return
        return propObject;
    }

    /**
     * Reads resources from {@literal <Resource>} objects in given JS (top-level) object, converts from ASCII encoding and
     * adds to archiver.
     */
    protected void readResources(JsonObject anElement)
    {
        // Get resources from top level <resource> tags
        /*for (int i = anElement.indexOf("Resource"); i >= 0; i = anElement.indexOf("Resource", i)) {

            // Get/remove current resource element
            XMLElement e = anElement.removeElement(i);

            // Get resource name and bytes
            String name = e.getAttributeValue("name");
            byte[] bytes = e.getValueBytes();

            // Add resource bytes for name
            addResource(name, bytes);
        }*/
    }

    /**
     * Converts a given PropNode to JSON object.
     */
    public static JsonObject convertPropNodeToJson(PropNode aPropNode)
    {
        // Create JSONObject for PropNode
        JsonObject jsonObj = new JsonObject();

        // If PropNode.NeedsClassDeclaration, add Class property to JSON object
        if (aPropNode.isNeedsClassDeclaration()) {
            String className = aPropNode.getClassName();
            jsonObj.setNativeValue(CLASS_KEY, className);
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
                    JsonArray jsonArray = new JsonArray();
                    for (PropNode arrayNode : arrayNodes) {
                        JsonObject jsonNode = convertPropNodeToJson(arrayNode);
                        jsonArray.addValue(jsonNode);
                    }
                    jsonObj.setValue(propName, jsonArray);
                }

                // Handle simple relation
                else {
                    PropNode relNode = (PropNode) nodeValue;
                    JsonObject jsonNode = convertPropNodeToJson(relNode);
                    jsonObj.setValue(propName, jsonNode);
                }
            }

            // Handle primitive array
            else if (isArray) {
                JsonArray jsonArray = new JsonArray(nodeValue);
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
     * Converts a given JSON object to PropNode.
     */
    public static PropNode convertJsonToPropNode(JsonObject jsonObject)
    {
        // Create PropNode for JSON object
        PropNode propNode = new PropNode();

        // Get attributes
        Set<String> propNames = jsonObject.getKeyValues().keySet();

        // Iterate over properties and add to PropNode
        for (String propName : propNames) {

            // Get property value
            JsonNode propValue = jsonObject.getValue(propName);

            // Handle relation array
            if (propValue instanceof JsonArray jsonArray) {
                int count = jsonArray.getValueCount();

                // Handle array of JSON object: Convert objects to PropNode array and set
                if (count > 0 && jsonArray.getValue(0) instanceof JsonObject) {

                    // Convert array
                    PropNode[] nodeArray = new PropNode[count];
                    for (int i = 0; i < count; i++) {
                        JsonObject jsonObj = (JsonObject) jsonArray.getValue(i);
                        PropNode node = convertJsonToPropNode(jsonObj);
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
            else if (propValue instanceof JsonObject jsonObj) {
                PropNode childNode = convertJsonToPropNode(jsonObj);
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
