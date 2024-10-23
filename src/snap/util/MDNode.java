package snap.util;

/**
 * This class represents a Markdown node.
 */
public class MDNode {

    // The node type
    protected NodeType _nodeType;

    // The text
    protected String _text;

    // Other text
    private String _otherText;

    // Child nodes
    protected MDNode[] _childNodes;

    // Constants for node type
    public enum NodeType { Root, Header1, Header2, Text, Link, Image, CodeBlock, List, ListItem, Mixed, Directive, Runnable }

    /**
     * Constructor.
     */
    public MDNode(NodeType aType, String theText)
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
    public MDNode[] getChildNodes()  { return _childNodes; }

    /**
     * Sets the child nodes.
     */
    protected void setChildNodes(MDNode[] nodesArray)
    {
        _childNodes = nodesArray;
    }

    /**
     * Adds a child node.
     */
    protected void addChildNode(MDNode aNode)
    {
        if (_childNodes == null) _childNodes = new MDNode[0];
        _childNodes = ArrayUtils.add(_childNodes, aNode);
    }

    /**
     * Returns a mixable node for given node.
     */
    protected static MDNode getMixedNodeForNode(MDNode aNode)
    {
        if (aNode.getNodeType() == NodeType.Mixed)
            return aNode;
        MDNode mixedNode = new MDNode(NodeType.Mixed, null);
        mixedNode.addChildNode(aNode);
        return mixedNode;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "MDNode { NodeType: " + _nodeType;
        if (_text != null) str += ", Text: " + _text;
        if (_otherText != null) str += ", Link: " + _otherText;
        if (_childNodes != null) str += ", ChildCount: " + _childNodes.length;
        return str + " }";
    }
}
