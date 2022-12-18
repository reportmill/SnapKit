/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;
import java.util.*;

/**
 * A PropArchiver subclass specifically to convert to/from JSON.
 */
public class PropArchiverJS extends PropArchiverX {

    /**
     * Constructor.
     */
    public PropArchiverJS()
    {
        super();
        _formatConverter = new JSONFormatConverter();
    }

    /**
     * Converts a PropObject to JSON.
     */
    public JSObject writePropObjectToJSON(PropObject aPropObject)
    {
        // Convert native to node
        PropNode propNode = convertNativeToNode(null, aPropObject);
        propNode.setNeedsClassDeclaration(true);

        // Convert node to JSON
        JSObject objectJS = (JSObject) convertNodeToFormatNode(null, propNode);

        // Archive resources
        /*for (Resource resource : getResources()) {
            JSObject resourceXML = new XMLElement("Resource");
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
            JSParser parser = new JSParser();
            JSObject json = (JSObject) parser.readString(jsonString);
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
    public PropObject readPropObjectFromJSON(JSObject objectJS)
    {
        // Read resources
        readResources(objectJS);

        // Read PropNode from JSON
        PropNode propNode = convertFormatNodeToNode(null, null, objectJS);

        // Convert PropNode (graph) to PropObject
        PropObject propObject = convertNodeToNative(propNode);

        // Return
        return propObject;
    }

    /**
     * Reads resources from <Resource> objects in given JS (top-level) object, converts from ASCII encoding and
     * adds to archiver.
     */
    protected void readResources(JSObject anElement)
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
     * This FormatNode implementation allows PropArchiver to read/write to JSON.
     */
    public static class JSONFormatConverter implements PropArchiverX.FormatConverter<Object> {

        /**
         * Creates a format node for given prop name.
         */
        @Override
        public Object createFormatNode(String aPropName)
        {
            return new JSObject();
        }

        /**
         * Creates a format array node for given prop name and array.
         */
        @Override
        public Object createFormatArrayNode(String aPropName, Object arrayObj)
        {
            return new JSArray(arrayObj);
        }

        /**
         * Return child property keys.
         */
        @Override
        public String[] getChildKeys(Object aNode)
        {
            // Handle JSObject
            if (aNode instanceof JSObject) {
                JSObject jsonObj = (JSObject) aNode;
                Map<String,JSValue> keyValues = jsonObj.getKeyValues();
                Set<String> keys = keyValues.keySet();
                return keys.toArray(new String[0]);
            }

            return new String[0];
        }

        /**
         * Return child property value for given key.
         */
        @Override
        public Object getChildNodeForKey(Object aNode, String aName)
        {
            // Handle JSObject
            if (aNode instanceof JSObject) {
                JSObject jsonObj = (JSObject) aNode;
                return jsonObj.getValue(aName);
            }

            // Return not found
            return null;
        }

        /**
         * Returns the node value as string.
         */
        @Override
        public String getNodeValueAsString(Object aNode)
        {
            // Handle JSValue
            if (aNode instanceof JSValue) {
                JSValue jsonValue = (JSValue) aNode;
                return jsonValue.getValueAsString();
            }

            // Return not found
            System.err.println("JSONFormatConverter.getNodeValueAsString: Unexpected node: " + aNode);
            return null;
        }

        /**
         * Returns array of nodes for a format node.
         */
        @Override
        public Object[] getNodeValueAsArray(Object anArrayNode)
        {
            // Handle JSArray
            if (anArrayNode instanceof JSArray) {
                JSArray jsonArray = (JSArray) anArrayNode;
                List<JSValue> valuesList = jsonArray.getValues();
                return valuesList.toArray();
            }

            System.err.println("JSONFormatConverter.getNodeValueAsArray: Unexpected array node: " + anArrayNode);
            return null;
        }

        /**
         * Sets a node value for given key.
         */
        @Override
        public void setNodeValueForKey(Object aNode, String aKey, Object aValue)
        {
            // Handle JSObject
            if (aNode instanceof JSObject) {
                JSObject jsonObj = (JSObject) aNode;
                if (aValue instanceof JSValue)
                    jsonObj.setValue(aKey, (JSValue) aValue);
                else jsonObj.setNativeValue(aKey, aValue);
            }

            // Handle unexpected
            else System.err.println("JSONFormatConverter.setNodeValueForKey: Unexpected node: " + aNode);
        }

        /**
         * Adds a node array item for given array key.
         */
        @Override
        public void addNodeArrayItemForKey(Object aNode, String aKey, Object aValue)
        {
            // Handle JSObject
            if (aNode instanceof JSObject) {
                JSObject jsonObj = (JSObject) aNode;

                // Get array element (create/add if missing)
                JSArray arrayJS = (JSArray) jsonObj.getValue(aKey);
                if (arrayJS == null) {
                    arrayJS = new JSArray();
                    jsonObj.setValue(aKey, arrayJS);
                }

                // Add item
                if (aValue instanceof JSValue)
                    arrayJS.addValue((JSValue) aValue);
                else System.err.println("JSONFormatConverter.addNodeArrayItemForKey: Unexpected array item node: " + aNode);
            }

            // Handle unexpected
            else System.err.println("JSONFormatConverter.addNodeArrayItemForKey: Unexpected array node: " + aNode);
        }
    }
}
