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
        PropMap propMap = convertPropObjectToPropMap(aPropObject);

        // Convert node to JSON
        JsonObject objectJS = convertPropMapToJson(propMap);

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

        // Read PropMap from JSON
        PropMap propMap = convertJsonToPropMap(objectJS);

        // Convert PropMap (graph) to PropObject
        PropObject rootObject = getRootObject();
        PropObject propObject = convertPropMapToPropObject(propMap, null, rootObject);

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
     * Converts a given PropMap to JSON object.
     */
    private static JsonObject convertPropMapToJson(PropMap propMap)
    {
        // Create JSONObject for PropMap
        JsonObject jsonObj = new JsonObject();

        // If PropMap.NeedsClassDeclaration, add Class property to JSON object
        if (propMap.isNeedsClassDeclaration()) {
            String className = propMap.getClassName();
            jsonObj.setNativeValue(CLASS_KEY, className);
        }

        // Get configured Props
        String[] propNames = propMap.getPropNames();

        // Iterate over PropNames and add JSON for each
        for (String propName : propNames) {

            // Get Node value and whether it is node and/or array
            Object nodeValue = propMap.getPropValue(propName);
            boolean isRelation = nodeValue instanceof PropMap || nodeValue instanceof PropMap[];
            boolean isArray = nodeValue != null && nodeValue.getClass().isArray();

            // Handle null
            if (nodeValue == null)
                jsonObj.setNativeValue(propName, "null");

                // Handle Relation prop
            else if (isRelation) {

                // Handle array
                if (isArray && nodeValue instanceof PropMap[] arrayPropMaps) {
                    JsonArray jsonArray = new JsonArray();
                    for (PropMap arrayNode : arrayPropMaps) {
                        JsonObject jsonNode = convertPropMapToJson(arrayNode);
                        jsonArray.addValue(jsonNode);
                    }
                    jsonObj.setValue(propName, jsonArray);
                }

                // Handle simple relation
                else if (nodeValue instanceof PropMap relNode) {
                    JsonObject jsonNode = convertPropMapToJson(relNode);
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
     * Converts a given JSON object to PropMap.
     */
    private static PropMap convertJsonToPropMap(JsonObject jsonObject)
    {
        // Create PropMap for JSON object
        PropMap propMap = new PropMap();

        // Get attributes
        Set<String> propNames = jsonObject.getKeyValues().keySet();

        // Iterate over properties and add to PropMap
        for (String propName : propNames) {

            // Get property value
            JsonNode propValue = jsonObject.getValue(propName);

            // Handle relation array
            if (propValue instanceof JsonArray jsonArray) {
                int count = jsonArray.getValueCount();

                // Handle array of JSON object: Convert objects to PropMap array and set
                if (count > 0 && jsonArray.getValue(0) instanceof JsonObject) {

                    // Convert array
                    PropMap[] nodeArray = new PropMap[count];
                    for (int i = 0; i < count; i++) {
                        JsonObject jsonObj = (JsonObject) jsonArray.getValue(i);
                        PropMap node = convertJsonToPropMap(jsonObj);
                        nodeArray[i] = node;
                    }

                    // Set node array
                    propMap.setPropValue(propName, nodeArray);
                }

                // Handle simple array
                else {
                    String arrayStr = jsonArray.getValueAsString();
                    propMap.setPropValue(propName, arrayStr);
                }
            }

            // Handle Object
            else if (propValue instanceof JsonObject jsonObj) {
                PropMap childNode = convertJsonToPropMap(jsonObj);
                propMap.setPropValue(propName, childNode);
            }

            // Handle value
            else {
                Object nativeValue = propValue.getNative();
                propMap.setPropValue(propName, nativeValue);
            }
        }

        // Return
        return propMap;
    }
}
