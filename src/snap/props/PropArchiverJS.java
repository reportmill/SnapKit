/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;
import java.util.*;

/**
 * A PropArchiver subclass specifically to convert to/from JSON.
 */
public class PropArchiverJS extends PropArchiver {

    /**
     * Converts a PropObject to JSON.
     */
    public JSObject convertPropObjectToJSON(PropObject aPropObject)
    {
        // Convert native to node
        PropNode propNode = convertNativeToNode(null, aPropObject);
        propNode.setNeedsClassDeclaration(true);

        // Convert node to JSON
        JSObject objectJS = convertNodeToJSON(null, propNode);

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
     * Converts a PropObject to JSON.
     */
    public byte[] convertPropObjectToJSONBytes(PropObject aPropObject)
    {
        JSObject json = convertPropObjectToJSON(aPropObject);
        String jsonStr = json.toString();
        byte[] jsonBytes = jsonStr.getBytes();
        return jsonBytes;
    }

    /**
     * Returns JSON for PropNode.
     */
    protected JSObject convertNodeToJSON(String aPropName, PropNode aPropNode)
    {
        // Create JSObject for PropNode
        JSObject propNodeJS = new JSObject();

        // If PropNode.NeedsClassDeclaration, add Class attribute to JSON
        if (aPropNode.isNeedsClassDeclaration()) {
            String className = aPropNode.getClassName();
            if (!className.equals(aPropName))
                propNodeJS.setNativeValue("Class", className);
        }

        // Get configured Props
        List<Prop> props = aPropNode.getProps();

        // Iterate over PropNames and add JSValue/JSObject/JSArray for each
        for (Prop prop : props) {

            // Get Node value and whether it is node and/or array
            String propName = prop.getName();
            Object nodeValue = aPropNode.getNodeValueForPropName(propName);
            boolean isRelation = prop.isRelation();

            // Handle null
            if (nodeValue == null)
                propNodeJS.setNativeValue(propName, null);

            // Handle Relation prop
            else if (isRelation)
                convertNodeToJSONForPropRelation(propNodeJS, prop, nodeValue);

            // Handle array of double or String
            else if (prop.isArray()) {

                // If double[] or String[], add JSArray
                Class propClass = prop.getPropClass();
                if (propClass == double[].class || propClass == String.class) {
                    Object arrayObj = aPropNode.getPropObject().getPropValue(propName);
                    JSArray arrayJS = new JSArray(arrayObj);
                    propNodeJS.setValue(propName, arrayJS);
                }

                // Otherwise add string
                else propNodeJS.setNativeValue(propName, nodeValue);
            }

            // Handle String (non-Node) prop
            else {

                // If PropClass is Number or Boolean, use original PropObject value
                Class propClass = prop.getPropClass();
                if (Number.class.isAssignableFrom(propClass) || propClass == Boolean.class || propClass.isPrimitive())
                    nodeValue = aPropNode.getPropObject().getPropValue(propName);

                // Set native value
                propNodeJS.setNativeValue(propName, nodeValue);
            }
        }

        // Return
        return propNodeJS;
    }

    /**
     * Converts and adds a prop node relation value to JSON.
     */
    protected void convertNodeToJSONForPropRelation(JSObject parentJS, Prop prop, Object nodeValue)
    {
        // Get prop info
        String propName = prop.getName();
        boolean isArray = prop.isArray();

        // Handle Node array
        if (isArray) {

            // Get array
            PropNode[] nodeArray = (PropNode[]) nodeValue;

            // Create JSArray
            JSArray arrayJS = new JSArray();
            parentJS.setValue(propName, arrayJS);

            // Handle PropNode array
            for (PropNode childNode : nodeArray) {
                JSObject childNodeJS = convertNodeToJSON(null, childNode);
                arrayJS.addValue(childNodeJS);
            }
        }

        // Handle Node object
        else {
            PropNode propNode = (PropNode) nodeValue;
            JSObject propNodeJS = convertNodeToJSON(propName, propNode);
            parentJS.setValue(propName, propNodeJS);
        }
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
        PropNode propNode = convertJSONToNode(null, null, objectJS);

        // Convert PropNode (graph) to PropObject
        PropObject propObject = convertNodeToNative(propNode);

        // Return
        return propObject;
    }

    /**
     * Reads a PropNode from JSON.
     */
    protected PropNode convertJSONToNode(PropNode aParent, Prop aProp, JSObject anObjectJS)
    {
        // Create PropObject for JSObject
        PropObject propObject = createPropObjectForJSON(aParent, aProp, anObjectJS);

        // Create PropNode for propObject
        PropNode propNode = new PropNode(propObject, this);

        // Get list of configured JSON attributes
        Map<String,JSValue> keyValues = anObjectJS.getKeyValues();
        Collection<String> keys = keyValues.keySet();

        // Iterate over JSON KeyValue keys and add node/native value for each
        for (String propName : keys) {

            // Skip special Class key
            if (propName.equals("Class")) continue;

            // Get prop
            Prop prop = propObject.getPropForName(propName);
            if (prop == null) continue; // Should never happen

            // Get value
            JSValue valueJS = anObjectJS.getValue(propName);

            // Handle array
            if (prop.isArray()) {

                // Handle Relation array: Get node value for JSON and add to PropNode
                if (prop.isRelation()) {
                    PropNode[] nodeValue = convertJSONToNodeForRelationArray(propNode, prop, (JSArray) valueJS);
                    propNode.addNodeValueForProp(prop, nodeValue);
                }

                // Handle simple array: Read array string and add to PropNode
                else {
                    String nodeValue = valueJS.getValueAsString();
                    propNode.addNodeValueForProp(prop, nodeValue);
                }
            }

            // Handle relation: Get node value for JSON and add to PropNode
            else {
                if (valueJS instanceof JSObject) {
                    JSObject objectJS = (JSObject) valueJS;
                    PropNode nodeValue = convertJSONToNode(propNode, prop, objectJS);
                    propNode.addNodeValueForProp(prop, nodeValue);
                }
            }
        }

        // Return
        return propNode;
    }

    /**
     * Reads a PropNode from JSON.
     */
    protected PropNode[] convertJSONToNodeForRelationArray(PropNode aParent, Prop aProp, JSArray anObjectJS)
    {
        // Get list of configured JSON objects
        List<JSValue> valuesJS = anObjectJS.getValues();

        // Create list
        List<PropNode> propNodes = new ArrayList<>(valuesJS.size());

        // Iterate over JSON objects and add node/native value for each
        for (JSValue valueJS : valuesJS) {

            PropNode propNode = convertJSONToNode(aParent, aProp, (JSObject) valueJS);
            if (propNode != null)
                propNodes.add(propNode);
        }

        // Return
        return propNodes.toArray(new PropNode[0]);
    }

    /**
     * Creates a PropObject for JSON object.
     */
    protected PropObject createPropObjectForJSON(PropNode aParent, Prop aProp, JSObject anObjectJS)
    {
        // If Prop.Preexisting, just instance from PropObject instead
        if (aProp != null && aProp.isPreexisting() && aParent != null) {
            PropObject propObject = aParent.getPropObject();
            Object existingInstance = propObject.getPropValue(aProp.getName());
            if (existingInstance instanceof PropObject)
                return (PropObject) existingInstance;
        }

        // If Class attribute set, try that
        String className = anObjectJS.getStringValue("Class");
        if (className != null) {
            Class<?> cls = getClassForName(className);
            if (cls != null)
                return createPropObjectForClass(cls);
        }

        // If Prop is Array, try name next
        /*if (aProp != null && aProp.isArray()) {
            String xmlName = anObjectJS.getName();
            Class<?> xmlNameClass = getClassForName(xmlName);
            if (xmlNameClass != null)
                return createPropObjectForClass(xmlNameClass);
        }*/

        // Try Prop class attribute
        Class<?> propClass = aProp != null ? aProp.getDefaultPropClass() : null;
        if (propClass != null) {

            // If array, swap for component class
            if (propClass.isArray())
                propClass = propClass.getComponentType();

            return createPropObjectForClass(propClass);
        }

        // Try PropName as ClassMap name
        String propName = aProp != null ? aProp.getName() : null;
        propClass = getClassForName(propName);
        if (propClass != null) {
            return createPropObjectForClass(propClass);
        }

        // Try object name
        /*String xmlName = anElement.getName();
        Class<?> xmlNameClass = getClassForName(xmlName);
        if (xmlNameClass != null)
            return createPropObjectForClass(xmlNameClass);*/

        // Complain and return
        System.err.println("PropArchiverJS.createPropObjectForJSON: Undetermined class for JS: " + anObjectJS);
        return null;
    }

    /**
     * Convenience newInstance.
     */
    private PropObject createPropObjectForClass(Class<?> aClass)
    {
        // See if we have proxy
        PropObject proxyObject = _helper.getProxyForClass(aClass);
        if (proxyObject != null)
            return proxyObject;

        Object propObject;
        try { propObject = aClass.newInstance(); }
        catch (InstantiationException | IllegalAccessException e) { throw new RuntimeException(e + " for: " + aClass); }

        // Return
        return (PropObject) propObject;
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
}
