/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.*;
import snap.web.WebURL;

/**
 * Represents a node in a JSON tree.
 */
public class JSValue {
    
    // The value
    private Object  _value;
    
    /**
     * Creates a new node.
     */
    public JSValue()  { }

    /**
     * Creates a new node.
     */
    public JSValue(Object anObj)
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
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        JSValue other = anObj instanceof JSValue ? (JSValue) anObj : null; if (other == null) return false;
        if (!Objects.equals(other._value, _value)) return false;
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        int hc = _value != null ? _value.hashCode() : 0;
        return hc;
    }

    /**
     * Returns a string representation of node (as JSON, of course).
     */
    public String toString()
    {
        JSWriter writer = new JSWriter();
        return writer.getString(this);
    }

    /**
     * Returns a string representation of node (as JSON, of course).
     */
    public String toStringCompacted()
    {
        JSWriter writer = new JSWriter();
        writer.setCompacted(true);
        return writer.getString(this);
    }

    /**
     * Reads JSON from a source.
     */
    public static JSValue readSource(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        JSParser parser = new JSParser();
        String text = url.getText();
        return parser.readString(text);
    }
}