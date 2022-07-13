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
            aSB.append('"').append(aKey).append('"').append(':');
            if (aNode instanceof JSObject || aNode instanceof JSArray)
                aSB.append(' ');
        }

        // Handle Object
        if (aNode instanceof JSObject) {

            // Get whether map is deep (not leaf)
            JSObject objectJS = (JSObject) aNode;
            boolean deep = _indentLevel == 0 || isDeep(objectJS);

            // Append map opening
            aSB.append('{');
            if (deep)
                appendNewlineIndent(aSB, ++_indentLevel);
            else aSB.append(' ');

            // Append keys, values and separators
            Map<String, JSValue> keyValues = objectJS.getKeyValues();
            String[] keys = keyValues.keySet().toArray(new String[0]);
            for (int i = 0, iMax = keys.length; i < iMax; i++) {

                // Append child
                String key = keys[i];
                JSValue child = keyValues.get(key);
                append(aSB, key, child);

                // If has next, append separator and whitespace
                if (i+1 < iMax) {
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

        // Handle Array
        if (aNode instanceof JSArray) {

            // Get whether list is deep (not leaf)
            JSArray arrayJS = (JSArray) aNode;
            boolean deep = isDeep(aNode);

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

        Object value = aNode.getValue();

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

        // Return string buffer
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
        if (aNode instanceof JSObject) {
            JSObject objectJS = (JSObject) aNode;
            Collection<JSValue> valueSet = objectJS.getKeyValues().values();
            for (JSValue node : valueSet)
                if (node instanceof JSObject || node instanceof JSArray)
                    return true;
        }
        if (aNode instanceof JSArray) {
            JSArray arrayJS = (JSArray) aNode;
            for (JSValue node : arrayJS.getValues())
                if (node instanceof JSObject || node instanceof JSArray)
                    return true;
        }
        return false;
    }
}