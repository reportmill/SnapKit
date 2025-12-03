package snap.webapi;

/**
 * This class is a wrapper for Web API Node (https://developer.mozilla.org/en-US/docs/Web/API/Node).
 */
public class Node extends JSProxy {

    /**
     * Constructor.
     */
    public Node()
    {
        super();
    }

    /**
     * Constructor.
     */
    public Node(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Returns the node name: e.g.: div, img, canvas.
     */
    public String getNodeName()  { return getMemberString("nodeName"); }

    /**
     * Returns the parent node.
     */
    public Node getParentNode()
    {
        Object parentNodeJS = getMember("parentNode");
        return parentNodeJS != null ? new HTMLElement(parentNodeJS) : null;
    }

    /**
     * Add given child node.
     */
    public void appendChild(Node childNode)  { call("appendChild", childNode._jsObj); }

    /**
     * Remove given child node.
     */
    public void removeChild(Node childNode)  { call("removeChild", childNode._jsObj); }

    /**
     * Returns a copy of this node.
     */
    public Node cloneNode(boolean deep)
    {
        Object jsObj = call("cloneNode", deep);
        String nodeName = getNodeName();
        return HTMLElement.getElementForName(nodeName, jsObj);
    }

    /**
     * Returns a new node for given JS node.
     */
    public static Node getNodeForNodeJS(Object jsObj)
    {
        String nodeName = WebEnv.get().getMemberString(jsObj, "nodeName");

        if (nodeName.equals("#text"))
            return new Text(jsObj);

        return HTMLElement.getElementForName(nodeName, jsObj);
    }

    /**
     * Returns the text content property of the node.
     */
    public String getTextContent()  { return getMemberString("textContent"); }

    /**
     * Sets the text content property of the node.
     */
    public void setTextContent(String var1)  { setMemberString("textContent", var1); }

    //String getNodeValue();
    //void setNodeValue(String var1);
    //short getNodeType();
    //NodeList<Node> getChildNodes();
    //Node getFirstChild();
    //Node getLastChild();
    //Node getPreviousSibling();
    //Node getNextSibling();
    //NamedNodeMap<Attr> getAttributes();
    //Node insertBefore(Node var1, Node var2);
    //Node replaceChild(Node var1, Node var2);
    //boolean hasChildNodes();
    //boolean hasChildNodesJS();
    //void normalize();
    //boolean isSupported(String var1, String var2);
    //String getNamespaceURI();
    //String getPrefix();
    //void setPrefix(String var1);
    //String getLocalName();
    //boolean hasAttributes();
    //Document getOwnerDocument();
}
