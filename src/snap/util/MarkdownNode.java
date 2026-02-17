package snap.util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a Markdown node.
 */
public class MarkdownNode {

    // The node type
    private NodeType _nodeType;

    // The text
    private String _text;

    // Other text
    private String _otherText;

    // Child nodes
    private List<MarkdownNode> _childNodes = EMPTY_NODE_LIST;

    // Constants for node type
    public enum NodeType { Root, Header1, Header2, Text, Link, Image, CodeBlock, List, ListItem, Mixed, Directive, Runnable, Separator }

    // Empty list
    private static final List<MarkdownNode> EMPTY_NODE_LIST = Collections.emptyList();

    /**
     * Constructor.
     */
    public MarkdownNode(NodeType aType, String theText)
    {
        _nodeType = aType;
        _text = theText;
    }

    /**
     * Returns the node type.
     */
    public NodeType getNodeType()  { return _nodeType; }

    /**
     * Returns the text.
     */
    public String getText()  { return _text; }

    /**
     * Returns the other text.
     */
    public String getOtherText()  { return _otherText; }

    /**
     * Sets the other text.
     */
    public void setOtherText(String otherText)  { _otherText = otherText; }

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
    }

    /**
     * Adds a child node.
     */
    public void addChildNode(MarkdownNode aNode)
    {
        if (_childNodes == EMPTY_NODE_LIST) _childNodes = new ArrayList<>();
        _childNodes.add(aNode);
    }

    /**
     * Removes a child node.
     */
    public void removeChildNode(int childIndex)
    {
        _childNodes.remove(childIndex);
    }

    /**
     * Returns a mixable node for given node.
     */
    protected static MarkdownNode getMixedNodeForNode(MarkdownNode aNode)
    {
        if (aNode.getNodeType() == NodeType.Mixed)
            return aNode;
        MarkdownNode mixedNode = new MarkdownNode(NodeType.Mixed, null);
        mixedNode.addChildNode(aNode);
        return mixedNode;
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
