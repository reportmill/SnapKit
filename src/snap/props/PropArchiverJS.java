/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.util.*;

/**
 * A PropArchiver subclass specifically to convert to/from JSON.
 */
public class PropArchiverJS extends PropArchiver {

    /**
     * Constructor.
     */
    public PropArchiverJS()
    {
        super();
    }

    /**
     * Converts a PropObject to JSON.
     */
    public JSObject writePropObjectToJSON(PropObject aPropObject)
    {
        // Convert native to node
        PropNode propNode = convertNativeToNode(aPropObject, null);

        // Convert node to JSON
        JSObject objectJS = PropNodeJSON.convertPropNodeToJSON(propNode);

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
        PropNode propNode = PropNodeJSON.convertJSONToPropNode(objectJS);

        // Convert PropNode (graph) to PropObject
        PropObject propObject = convertNodeToNative(propNode, null);

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
}
