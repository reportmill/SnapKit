package snap.util;
import java.util.*;

/**
 * This class represents a Markdown node.
 */
public class MarkdownNode {

    // The parent node
    private MarkdownNode _parent;

    // The node type
    private NodeType _nodeType;

    // The text
    private String _text;

    // Other text
    private String _otherText;

    // The indent level of this node
    private int _indentLevel;

    // Child nodes
    private List<MarkdownNode> _childNodes = EMPTY_NODE_LIST;

    // Attributes
    private Map<String,Object> _attributes = EMPTY_ATTRIBUTES_MAP;

    // Constants for node type
    public enum NodeType {

        // Root node
        Document,

        // Block nodes
        Header, Paragraph, List, ListItem, Directive, CodeBlock, RunBlock, Separator, // BlockQuote

        // Inline nodes
        Link, Image, Text //, Strong, Emphasis, CodeSpan
    }

    // Constants for attributes
    public static final String HEADER_LEVEL = "HeaderLevel";

    // Empty list
    private static final List<MarkdownNode> EMPTY_NODE_LIST = Collections.emptyList();
    private static final Map<String,Object> EMPTY_ATTRIBUTES_MAP = Collections.emptyMap();

    /**
     * Constructor.
     */
    public MarkdownNode(NodeType aType, String theText)
    {
        _nodeType = aType;
        _text = theText;
    }

    /**
     * Returns the parent node.
     */
    public MarkdownNode getParent()  { return _parent; }

    /**
     * Returns the node type.
     */
    public NodeType getNodeType()  { return _nodeType; }

    /**
     * Returns the text.
     */
    public String getText()
    {
        if (_text == null && !_childNodes.isEmpty())
            _text = ListUtils.mapToStringsAndJoin(_childNodes, MarkdownNode::getText, "");
        return _text;
    }

    /**
     * Returns the other text.
     */
    public String getOtherText()  { return _otherText; }

    /**
     * Sets the other text.
     */
    public void setOtherText(String otherText)  { _otherText = otherText; }

    /**
     * Returns the indent level.
     */
    public int getIndentLevel()  { return _indentLevel; }

    /**
     * Sets the indent level.
     */
    protected void setIndentLevel(int aValue)  { _indentLevel = aValue; }

    /**
     * Returns the child nodes.
     */
    public List<MarkdownNode> getChildNodes()  { return _childNodes; }

    /**
     * Sets the child nodes.
     */
    protected void setChildNodes(List<MarkdownNode> nodesArray)
    {
        _childNodes = new ArrayList<>(nodesArray);
        nodesArray.forEach(childNode -> childNode._parent = this);
    }

    /**
     * Adds a child node.
     */
    public void addChildNode(MarkdownNode aNode)
    {
        if (_childNodes == EMPTY_NODE_LIST) _childNodes = new ArrayList<>();
        _childNodes.add(aNode);
        aNode._parent = this;
    }

    /**
     * Removes a child node.
     */
    public void removeChildNode(int childIndex)
    {
        _childNodes.remove(childIndex);
    }

    /**
     * Returns the first child.
     */
    public MarkdownNode getFirstChild()  { return !_childNodes.isEmpty() ? _childNodes.get(0) : null; }

    /**
     * Returns attribute for given key.
     */
    public Object getAttributeValue(String attributeName)
    {
        return _attributes.get(attributeName);
    }

    /**
     * Sets attribute for given key.
     */
    protected void setAttributeValue(String attributeName, Object aValue)
    {
        if (_attributes == EMPTY_ATTRIBUTES_MAP) _attributes = new HashMap<>(1);
        _attributes.put(attributeName, aValue);
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "MarkdownNode { NodeType: " + _nodeType;
        if (_text != null) str += ", Text: " + _text;
        if (_otherText != null) str += ", Link: " + _otherText;
        if (_childNodes != null) str += ", ChildCount: " + _childNodes.size();
        return str + " }";
    }
}
