/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;
import snap.web.WebURL;

/**
 * Represents a node in a JSON tree.
 */
public class JsonNode {
    
    // The value
    private Object  _value;
    
    /**
     * Constructor.
     */
    public JsonNode()  { }

    /**
     * Constructor.
     */
    public JsonNode(Object anObj)
    {
        setValue(anObj);
    }

    /**
     * Returns the value.
     */
    public Object getValue()  { return _value; }

    /**
     * Sets the value.
     */
    protected void setValue(Object anObj)
    {
        // Handle enum special
        if (anObj instanceof Enum)
            _value = anObj.toString();

        // Handle String, Number, Boolean
        else if (anObj instanceof String || anObj instanceof Number || anObj instanceof Boolean || anObj == null)
            _value = anObj;

        // Complain
        else throw new RuntimeException("JSONNode: Unsupported core type (" + anObj.getClass() + ")");
    }

    /**
     * Returns the value as String if type is String.
     */
    public String getValueAsString()
    {
        return Convert.stringValue(_value);
    }

    /**
     * Returns the node as a native object.
     */
    public Object getNative()  { return _value; }

    /**
     * Standard equals implementation.
     */
    @Override
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        JsonNode other = anObj instanceof JsonNode ? (JsonNode) anObj : null; if (other == null) return false;
        if (!Objects.equals(other._value, _value)) return false;
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    @Override
    public int hashCode()
    {
        int hc = _value != null ? _value.hashCode() : 0;
        return hc;
    }

    /**
     * Returns a string representation of node (as JSON, of course).
     */
    @Override
    public String toString()
    {
        JsonWriter writer = new JsonWriter();
        return writer.getString(this);
    }

    /**
     * Returns a string representation of node (as JSON, of course).
     */
    public String toStringCompacted()
    {
        JsonWriter writer = new JsonWriter();
        writer.setCompacted(true);
        return writer.getString(this);
    }

    /**
     * Reads JSON from a source.
     */
    public static JsonNode readSource(Object aSource)
    {
        WebURL url = WebURL.createUrl(aSource);
        JsonParser parser = new JsonParser();
        String text = url.getText();
        return parser.readString(text);
    }
}