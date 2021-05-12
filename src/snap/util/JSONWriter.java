/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.util.List;

/**
 * Writes a JSON to string.
 */
public class JSONWriter {

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
    public JSONWriter setIndent(String anIndent)
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
    public JSONWriter setCompacted(boolean aValue)
    {
        _compacted = aValue;
        return this;
    }

    /**
     * Returns a string for given JSON node.
     */
    public String getString(JSONNode aNode)
    {
        return getStringBuffer(aNode).toString();
    }

    /**
     * Returns a string buffer for given JSON node.
     */
    public StringBuffer getStringBuffer(JSONNode aNode)
    {
        StringBuffer sb = new StringBuffer(1024);
        return append(sb, aNode);
    }

    /**
     * Returns a string buffer for given JSON node.
     */
    protected StringBuffer append(StringBuffer aSB, JSONNode aNode)
    {
        // Append key
        String key = aNode.getKey();
        if (key != null) {
            aSB.append('"').append(key).append('"').append(':');
            if (aNode.isArray() || aNode.isObject())
                aSB.append(' ');
        }

        // Handle node types
        switch(aNode.getType()) {

            // Handle Object
            case Object: {

                // Get whether map is deep (not leaf)
                boolean deep = _indentLevel==0 || isDeep(aNode);

                // Append map opening
                aSB.append('{');
                if (deep)
                    appendNewlineIndent(aSB, ++_indentLevel);
                else aSB.append(' ');

                // Append keys, values and separators
                List <JSONNode> nodes = aNode.getNodes();
                for (int i=0, iMax=nodes.size(); i<iMax; i++) {

                    // Append child
                    JSONNode child = nodes.get(i);
                    append(aSB, child);

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

            } break;

            // Handle Array
            case Array: {

                // Get whether list is deep (not leaf)
                boolean deep = isDeep(aNode);

                // Append list opening
                aSB.append('[');
                if (deep)
                    appendNewlineIndent(aSB, ++_indentLevel);
                else aSB.append(' ');

                // Iterate over items to append items and separators
                int count = aNode.getNodeCount();
                for (int i=0; i<count; i++) {

                    // Append item
                    JSONNode item = aNode.getNode(i);
                    append(aSB, item);

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

            } break;

            // Handle String
            case String: {
                aSB.append('"');
                for (int i=0, iMax=aNode.getString().length(); i<iMax; i++) {
                    char c = aNode.getString().charAt(i);
                    if (c=='"' || c=='\\' || c=='/') aSB.append('\\').append(c);
                    else if (c=='\b') aSB.append("\\b");
                    else if (c=='\f') aSB.append("\\f");
                    else if (c=='\n') aSB.append("\\n");
                    else if (c=='\r') aSB.append("\\r");
                    else if (c=='\t') aSB.append("\\t");
                    else if (Character.isISOControl(c))
                        System.err.println("JSONWriter.append: Tried to print control char in string: "+aNode.getString());
                    else aSB.append(c);
                }
                aSB.append('"');
            } break;

            // Handle Number
            case Number: {
                Number num = aNode.getNumber();
                String str = FormatUtils.formatNum("#.##", num);
                aSB.append(str);
                break;
            }

            // Handle Boolean
            case Boolean: aSB.append(aNode.getBoolean() ? "true" : "false"); break;

            // Handle Null
            case Null: aSB.append("null"); break;
        }

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
    public void writeJSON(JSONNode aNode, String aPath)
    {
        String json = getString(aNode);
        SnapUtils.writeBytes(StringUtils.getBytes(json), aPath);
    }

    /**
     * Returns whether given node has child Map or List of Map/List.
     */
    protected boolean isDeep(JSONNode aNode)
    {
        if (aNode.isArray() || aNode.isObject())
            for (JSONNode child : aNode.getNodes())
                if (child.isArray() || child.isObject())
                    return true;
        return false;
    }
}