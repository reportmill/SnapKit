/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Writes a JSON to string.
 */
public class JsonWriter {

    // The current indent level
    private int _indentLevel = 0;
    
    // The indent string
    private String _indent = "\t";

    // Whether to quote keys
    private boolean _quoteKeys = true;
    
    // Whether writer compacts JSON (no indent or newline)
    private boolean _compacted = false;

    // Constant for escape chars
    private static final char[] ESCAPE_CHARS = { '"', '\\', '\b', '\f', '\n', '\r', '\t' };

    /**
     * Constructor.
     */
    public JsonWriter()
    {
        super();
    }

    /**
     * Returns the current indent.
     */
    public String getIndent()  { return _indent; }

    /**
     * Sets the current indent string.
     */
    public JsonWriter setIndent(String anIndent)
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
    public void setCompacted(boolean aValue)
    {
        _compacted = aValue;
    }

    /**
     * Returns a string for given JSON node.
     */
    public String getString(JsonNode jsonNode)
    {
        StringBuffer sb = getStringBuffer(jsonNode);
        return sb.toString();
    }

    /**
     * Returns a string buffer for given JSON node.
     */
    public StringBuffer getStringBuffer(JsonNode jsonNode)
    {
        StringBuffer sb = new StringBuffer(1024);
        return append(sb, null, jsonNode);
    }

    /**
     * Returns a string buffer for given JSON node.
     */
    protected StringBuffer append(StringBuffer aSB, String aKey, JsonNode jsonNode)
    {
        // Append key
        if (aKey != null) {

            // Append key + colon + space
            if (_quoteKeys)
                aSB.append('"').append(aKey).append('"').append(':').append(' ');
            else aSB.append(aKey).append(':').append(' ');
        }

        // Handle Object
        if (jsonNode instanceof JsonObject)
            return appendJsonObject(aSB, (JsonObject) jsonNode);

        // Handle Array
        if (jsonNode instanceof JsonArray)
            return appendJsonArray(aSB, (JsonArray) jsonNode);

        // Handle value
        return appendJsonValue(aSB, jsonNode);
    }

    /**
     * Appends the given JSON object to StringBuffer.
     */
    protected StringBuffer appendJsonObject(StringBuffer aSB, JsonObject jsonObject)
    {
        // Get whether map is deep (not leaf)
        boolean deep = _indentLevel == 0 || isDeep(jsonObject);

        // Append map opening
        aSB.append('{');
        if (deep)
            appendNewlineIndent(aSB, ++_indentLevel);
        else aSB.append(' ');

        // Get JSON object KeyValues and Keys
        Map<String, JsonNode> keyValues = jsonObject.getKeyValues();
        String[] keys = keyValues.keySet().toArray(new String[0]);

        // Iterate over keys and append Key/Value for each
        for (int i = 0, iMax = keys.length; i < iMax; i++) {

            // Append child
            String key = keys[i];
            JsonNode child = keyValues.get(key);
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
     * Appends the given JSON array to StringBuffer.
     */
    protected StringBuffer appendJsonArray(StringBuffer aSB, JsonArray jsonArray)
    {
        // Get whether list is deep (not leaf)
        boolean deep = isDeep(jsonArray);

        // Append list opening
        aSB.append('[');
        if (deep)
            appendNewlineIndent(aSB, ++_indentLevel);
        else aSB.append(' ');

        // Iterate over items to append items and separators
        int count = jsonArray.getValueCount();
        for (int i = 0; i < count; i++) {

            // Append item
            JsonNode item = jsonArray.getValue(i);
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
     * Appends the given JSON value to StringBuffer.
     */
    protected StringBuffer appendJsonValue(StringBuffer aSB, JsonNode jsonNode)
    {
        Object value = jsonNode.getValue();

        // Handle String
        if (value instanceof String string) {
            aSB.append('"');
            for (int i = 0, iMax = string.length(); i < iMax; i++) {
                char c = string.charAt(i);
                if (isEscapeChar(c))
                    aSB.append(getEscapeCharString(c));
                else aSB.append(c);
            }
            aSB.append('"');
        }

        // Handle Number
        else if (value instanceof Number num) {
            String str = FormatUtils.formatNum("#.##", num);
            aSB.append(str);
        }

        // Handle Boolean
        else if (value instanceof Boolean boolValue)
            aSB.append(boolValue);

            // Handle Null
        else if (value == null)
            aSB.append("null");

        else System.err.println(("JSONWriter.append: Unknown type: " + value.getClass()));
        return aSB;
    }

    /**
     * Appends newline and indent.
     */
    protected void appendNewlineIndent(StringBuffer aSB)
    {
        appendNewlineIndent(aSB, _indentLevel);
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
        aSB.append(String.valueOf(_indent).repeat(Math.max(0, aLevel)));
        return aSB;
    }

    /**
     * Writes the given JSON object to given file path.
     */
    public void writeJSON(JsonNode aNode, String aPath)
    {
        String json = getString(aNode);
        SnapUtils.writeBytes(StringUtils.getBytes(json), aPath);
    }

    /**
     * Returns whether given node has child Map or List of Map/List.
     */
    protected boolean isDeep(JsonNode aNode)
    {
        // Handle object
        if (aNode instanceof JsonObject jsonObject) {
            Collection<JsonNode> valueSet = jsonObject.getKeyValues().values();
            for (JsonNode node : valueSet)
                if (node instanceof JsonObject || node instanceof JsonArray)
                    return true;
        }

        // Handle array
        if (aNode instanceof JsonArray jsonArray) {
            for (JsonNode node : jsonArray.getValues())
                if (node instanceof JsonObject || node instanceof JsonArray)
                    return true;
        }

        // Return false for anything else
        return false;
    }

    /**
     * Returns whether a given character is an escape character.
     */
    private static boolean isEscapeChar(char aChar)
    {
        if (Arrays.binarySearch(ESCAPE_CHARS, aChar) >= 0)
            return true;
        return Character.isISOControl(aChar);
    }

    /**
     * Returns the escape string for given escape character.
     */
    private static String getEscapeCharString(char aChar)
    {
        return switch (aChar) {
            case '"' -> "\\\"";
            case '\\' -> "\\\\";
            case '\b' -> "\\b";
            case '\f' -> "\\f";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            default -> {
                if (Character.isISOControl(aChar)) {
                    System.err.println("JSONWriter.append: Tried to print control char in string: " + aChar);
                    yield "";
                }
                yield String.valueOf(aChar);
            }
        };
    }
}