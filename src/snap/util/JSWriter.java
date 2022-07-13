/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.Collection;
import java.util.Map;

/**
 * Writes a JSON to string.
 */
public class JSWriter {

    // The current indent level
    private int  _indentLevel = 0;
    
    // The indent string
    private String  _indent = "\t";

    // Whether to quote keys
    private boolean  _quoteKeys = true;
    
    // Whether writer compacts JSON (no indent or newline)
    private boolean  _compacted = false;
    
    /**
     * Returns the current indent.
     */
    public String getIndent()  { return _indent; }

    /**
     * Sets the current indent string.
     */
    public JSWriter setIndent(String anIndent)
    {
        _indent = anIndent;
        return this;
    }

    /**
     * Returns whether to quote keys.
     */
    public boolean isQuoteKeys()  { return _quoteKeys; }

    /**
     * Sets whether to quote keys.
     */
    public void setQuoteKeys(boolean aValue)
    {
        _quoteKeys = aValue;
    }

    /**
     * Returns whether writer compacts JSON (no indent or newline).
     */
    public boolean isCompacted()  { return _compacted; }

    /**
     * Sets whether writer compacts JSON (no indent or newline).
     */
    public JSWriter setCompacted(boolean aValue)
    {
        _compacted = aValue;
        return this;
    }

    /**
     * Returns a string for given JSON node.
     */
    public String getString(JSValue aNode)
    {
        StringBuffer sb = getStringBuffer(aNode);
        return sb.toString();
    }

    /**
     * Returns a string buffer for given JSON node.
     */
    public StringBuffer getStringBuffer(JSValue aNode)
    {
        StringBuffer sb = new StringBuffer(1024);
        return append(sb, null, aNode);
    }

    /**
     * Returns a string buffer for given JSON node.
     */
    protected StringBuffer append(StringBuffer aSB, String aKey, JSValue aNode)
    {
        // Append key
        if (aKey != null) {

            // Append key + colon + space
            if (_quoteKeys)
                aSB.append('"').append(aKey).append('"').append(':').append(' ');
            else aSB.append(aKey).append(':').append(' ');
        }

        // Handle Object
        if (aNode instanceof JSObject)
            return appendJSObject(aSB, (JSObject) aNode);

        // Handle Array
        if (aNode instanceof JSArray)
            return appendJSArray(aSB, (JSArray) aNode);

        // Handle JSValue
        return appendJSValue(aSB, aNode);
    }

    /**
     * Appends the given JSObject to StringBuffer.
     */
    protected StringBuffer appendJSObject(StringBuffer aSB, JSObject objectJS)
    {
        // Get whether map is deep (not leaf)
        boolean deep = _indentLevel == 0 || isDeep(objectJS);

        // Append map opening
        aSB.append('{');
        if (deep)
            appendNewlineIndent(aSB, ++_indentLevel);
        else aSB.append(' ');

        // Get JSObject KeyValues and Keys
        Map<String, JSValue> keyValues = objectJS.getKeyValues();
        String[] keys = keyValues.keySet().toArray(new String[0]);

        // Iterate over keys and append Key/Value for each
        for (int i = 0, iMax = keys.length; i < iMax; i++) {

            // Append child
            String key = keys[i];
            JSValue child = keyValues.get(key);
            append(aSB, key, child);

            // If has next, append separator and whitespace
            if (i + 1 < iMax) {
                if (deep)
                    appendNewlineIndent(aSB.append(','));
                else aSB.append(", ");
            }
        }

        // Append trailing whitespace and close
        if (deep)
            appendNewlineIndent(aSB, --_indentLevel).append('}');
        else aSB.append(" }");
        return aSB;
    }

    /**
     * Appends the given JSArray to StringBuffer.
     */
    protected StringBuffer appendJSArray(StringBuffer aSB, JSArray arrayJS)
    {
        // Get whether list is deep (not leaf)
        boolean deep = isDeep(arrayJS);

        // Append list opening
        aSB.append('[');
        if (deep)
            appendNewlineIndent(aSB, ++_indentLevel);
        else aSB.append(' ');

        // Iterate over items to append items and separators
        int count = arrayJS.getValueCount();
        for (int i = 0; i < count; i++) {

            // Append item
            JSValue item = arrayJS.getValue(i);
            append(aSB, null, item);

            // If has next, append separator
            boolean hasNext = i+1 < count;
            if (hasNext) {
                if (deep)
                    appendNewlineIndent(aSB.append(','));
                else aSB.append(", ");
            }
        }

        // Append trailing whitespace and close
        if (deep)
            appendNewlineIndent(aSB, --_indentLevel).append(']');
        else aSB.append(" ]");
        return aSB;
    }

    /**
     * Appends the given JSValue to StringBuffer.
     */
    protected StringBuffer appendJSValue(StringBuffer aSB, JSValue valueJS)
    {
        Object value = valueJS.getValue();

        // Handle String
        if (value instanceof String) {
            String string = (String) value;
            aSB.append('"');
            for (int i = 0, iMax = string.length(); i < iMax; i++) {
                char c = string.charAt(i);
                if (c=='"' || c=='\\' || c=='/') aSB.append('\\').append(c);
                else if (c=='\b') aSB.append("\\b");
                else if (c=='\f') aSB.append("\\f");
                else if (c=='\n') aSB.append("\\n");
                else if (c=='\r') aSB.append("\\r");
                else if (c=='\t') aSB.append("\\t");
                else if (Character.isISOControl(c))
                    System.err.println("JSONWriter.append: Tried to print control char in string: " + string);
                else aSB.append(c);
            }
            aSB.append('"');
        }

        // Handle Number
        else if (value instanceof Number) {
            Number num = (Number) value;
            String str = FormatUtils.formatNum("#.##", num);
            aSB.append(str);
        }

        // Handle Boolean
        else if (value instanceof Boolean)
            aSB.append(((Boolean) value) ? "true" : "false");

            // Handle Null
        else if (value == null)
            aSB.append("null");

        else System.err.println(("JSONWriter.append: Unknown type: " + value.getClass()));
        return aSB;
    }

    /**
     * Appends newline and indent.
     */
    protected StringBuffer appendNewlineIndent(StringBuffer aSB)
    {
        return appendNewlineIndent(aSB, _indentLevel);
    }

    /**
     * Appends newline and indent.
     */
    protected StringBuffer appendNewlineIndent(StringBuffer aSB, int aLevel)
    {
        // If tiny mode enabled, just append space and return
        if (isCompacted())
            return aSB.append(' ');

        // Otherwise, append newline, indent and return
        aSB.append('\n');
        for (int i=0; i<aLevel; i++) aSB.append(_indent);
        return aSB;
    }

    /**
     * Writes the given JSON object to given file path.
     */
    public void writeJSON(JSValue aNode, String aPath)
    {
        String json = getString(aNode);
        SnapUtils.writeBytes(StringUtils.getBytes(json), aPath);
    }

    /**
     * Returns whether given node has child Map or List of Map/List.
     */
    protected boolean isDeep(JSValue aNode)
    {
        // Handle JSObject
        if (aNode instanceof JSObject) {
            JSObject objectJS = (JSObject) aNode;
            Collection<JSValue> valueSet = objectJS.getKeyValues().values();
            for (JSValue node : valueSet)
                if (node instanceof JSObject || node instanceof JSArray)
                    return true;
        }

        // Handle JSArray
        if (aNode instanceof JSArray) {
            JSArray arrayJS = (JSArray) aNode;
            for (JSValue node : arrayJS.getValues())
                if (node instanceof JSObject || node instanceof JSArray)
                    return true;
        }

        // Return false for anything else
        return false;
    }
}