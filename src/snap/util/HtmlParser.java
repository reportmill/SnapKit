package snap.util;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parser for HTML.
 */
public class HtmlParser {

    // The HTML node
    private XMLElement _htmlNode;

    // The indent level for markdown creation
    private int _indentLevel = 0;

    // Constants for nodes
    private static final String HEADER_NODE = "h1";
    private static final String PARAGRAPH_NODE = "p";
    private static final String LINK_NODE = "a";
    private static final String IMAGE_NODE = "img";
    private static final String LIST_NODE = "ul";
    private static final String LIST_ITEM_NODE = "li";
    private static final String CODE_NODE = "code";
    private static final String PRE_NODE = "pre";
    private static final String EMPHASIS_NODE = "em";
    private static final String BOLD_NODE = "b";

    // Constant
    private static final List<String> INLINE_NODES = List.of(LINK_NODE, EMPHASIS_NODE, BOLD_NODE);
    private static final List<String> BLOCK_NODES = List.of(LIST_NODE, CODE_NODE);

    /**
     * Constructor.
     */
    public HtmlParser()
    {
        super();
    }

    /**
     * Parses given HTML string.
     */
    public XMLElement parseHtmlString(String htmlString)
    {
        int htmlIndex = htmlString.indexOf("<html");
        htmlString = htmlString.substring(htmlIndex);
        htmlString = htmlString.replace("'", "\"");
        htmlString = htmlString.replace("<em>", "");
        htmlString = htmlString.replace("</em>", "");
        htmlString = htmlString.replace("<b>", "");
        htmlString = htmlString.replace("</b>", "");
        XMLParser xmlParser = new XMLParser();
        xmlParser.allowMixedContent();
        return _htmlNode = xmlParser.parseXMLFromString(htmlString);
    }

    /**
     * Returns markdown nodes for last parse.
     */
    public List<MarkdownNode> getMarkdownNodesForHtml()
    {
        XMLElement body = _htmlNode.getElement("body");
        List<XMLElement> bodyNodes = body.getElements();
        List<MarkdownNode> markdownNodes = new ArrayList<>();

        for (XMLElement node : bodyNodes) {

            // If header, add separator
            if (node.getName().equals("h1") && !markdownNodes.isEmpty())
                markdownNodes.add(new MarkdownNode(MarkdownNode.NodeType.Separator, null));

            // Get node
            MarkdownNode markdownNode = createMarkdownNodeForHtml(node);
            markdownNodes.add(markdownNode);
        }

        // Return
        return markdownNodes;
    }

    /**
     * Returns a markdown node for given XML node.
     */
    private MarkdownNode createMarkdownNodeForHtml(XMLElement htmlNode)
    {
        MarkdownNode markdownNode = createMarkdownNodeForHtmlImpl(htmlNode);
        if (markdownNode != null)
            markdownNode.setIndentLevel(_indentLevel);
        return markdownNode;
    }

    /**
     * Returns a markdown node for given XML node.
     */
    private MarkdownNode createMarkdownNodeForHtmlImpl(XMLElement htmlNode)
    {
        return switch (htmlNode.getName()) {
            case HEADER_NODE -> createMarkdownHeaderNodeForHtml(htmlNode);
            case LIST_NODE -> createMarkdownListNodeForHtml(htmlNode);
            case LIST_ITEM_NODE -> createMarkdownListItemNodeForHtml(htmlNode);
            case CODE_NODE -> createMarkdownCodeBlockNodeForHtml(htmlNode);
            case PARAGRAPH_NODE -> createMarkdownParagraphNodeForHtml(htmlNode);
            case LINK_NODE -> createMarkdownLinkNodeForHtml(htmlNode);
            case IMAGE_NODE -> createMarkdownImageNodeForHtml(htmlNode);
            case PRE_NODE -> createMarkdwonTextNodeForHtml(htmlNode);
            default -> {
                System.out.println("Unknown HTML node: " + htmlNode.getName());
                yield null;
            }
        };
    }

    /**
     * Returns a markdown list node for given list node.
     */
    private MarkdownNode createMarkdownListNodeForHtml(XMLElement htmlNode)
    {
        MarkdownNode listMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.List, null);
        List<XMLElement> listItemHtmlNodes = htmlNode.getElements();

        for (XMLElement listItemHtmlNode : listItemHtmlNodes) {
            if (listItemHtmlNode.getName().equals("li")) {
                MarkdownNode listItemMarkdownNode = createMarkdownNodeForHtml(listItemHtmlNode);
                if (listItemMarkdownNode != null)
                    listMarkdownNode.addChildNode(listItemMarkdownNode);
            }
        }

        // Return
        return listMarkdownNode;
    }

    /**
     * Returns a markdown header node for given html header node.
     */
    private MarkdownNode createMarkdownHeaderNodeForHtml(XMLElement htmlNode)
    {
        String headerText = htmlNode.getValue();
        MarkdownNode headerMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Header, headerText);
        return headerMarkdownNode;
    }

    /**
     * Returns a markdown list item node for given html list item node.
     */
    private MarkdownNode createMarkdownListItemNodeForHtml(XMLElement listItemHtmlNode)
    {
        // Create list item markdown node and add paragraph node
        MarkdownNode listItemMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.ListItem, null);
        MarkdownNode paragraphMarkdownNode = createMarkdownParagraphNodeForHtml(listItemHtmlNode);
        listItemMarkdownNode.addChildNode(paragraphMarkdownNode);

        // Iterate over children
        List<XMLElement> childNodes = listItemHtmlNode.getElements();
        for (XMLElement childNode : childNodes) {

            // If block node, create/add markdown block node
            if (isBlockNode(childNode)) {
                _indentLevel++;
                MarkdownNode childMarkdownNode = createMarkdownNodeForHtml(childNode);
                if (childMarkdownNode != null) {
                    listItemMarkdownNode.addChildNode(childMarkdownNode);
                    childMarkdownNode.setIndentLevel(1);
                }
                _indentLevel--;
            }

            // Complain
            else System.out.println("HtmlParser.createMarkdownListItemNodeForHtml: Unexpected list item child: " + childNode.getName());
        }


        // Return
        return listItemMarkdownNode;
    }

    /**
     * Returns a markdown code block node for given html code node.
     */
    private MarkdownNode createMarkdownCodeBlockNodeForHtml(XMLElement htmlNode)
    {
        String codeText = htmlNode.getValue();
        MarkdownNode codeBlockMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.CodeBlock, codeText);
        return codeBlockMarkdownNode;
    }

    /**
     * Returns a markdown paragraph node for given html paragraph node.
     */
    private MarkdownNode createMarkdownParagraphNodeForHtml(XMLElement htmlNode)
    {
        MarkdownNode paragraphMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Paragraph, null);

        // If text, add text node
        String paragraphText = htmlNode.getValue();
        if (paragraphText != null) {
            MarkdownNode textMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Text, paragraphText);
            paragraphMarkdownNode.addChildNode(textMarkdownNode);
        }

        // If child nodes, add them
        List<XMLElement> childNodes = htmlNode.getElements();
        for (XMLElement childNode : childNodes) {
            if (isInlineNode(childNode)) {
                MarkdownNode childMarkdownNode = createMarkdownNodeForHtml(childNode);
                Objects.requireNonNull(childMarkdownNode);
                paragraphMarkdownNode.addChildNode(childMarkdownNode);
            }
        }

        // Complain about empty paragraph node
        if (paragraphText == null && paragraphMarkdownNode.getChildNodes().isEmpty()) {
            MarkdownNode textMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Text, " ");
            paragraphMarkdownNode.addChildNode(textMarkdownNode);
            System.out.println("HtmlParser.createMarkdownParagraphNodeForHtml: Empty paragraph/list node");
        }

        // Return
        return paragraphMarkdownNode;
    }

    /**
     * Returns a markdown link node for given html link node.
     */
    private MarkdownNode createMarkdownLinkNodeForHtml(XMLElement htmlNode)
    {
        MarkdownNode linkMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Link, null);
        String linkText = htmlNode.getValue();
        MarkdownNode textMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Text, linkText);
        linkMarkdownNode.addChildNode(textMarkdownNode);
        String linkUrl = htmlNode.getAttributeValue("href");
        linkMarkdownNode.setOtherText(linkUrl);
        return linkMarkdownNode;
    }

    /**
     * Returns a markdown image node for given html image node.
     */
    private MarkdownNode createMarkdownImageNodeForHtml(XMLElement htmlNode)
    {
        String srcUrl = htmlNode.getAttributeValue("src");
        MarkdownNode imageMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Image, srcUrl);
        return imageMarkdownNode;
    }

    /**
     * Returns a markdown text node for given html text node.
     */
    private MarkdownNode createMarkdwonTextNodeForHtml(XMLElement htmlNode)
    {
        String text = htmlNode.getValue();
        MarkdownNode textMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Text, text);
        return textMarkdownNode;
    }

    /**
     * Returns whether given HTML node is block node.
     */
    private static boolean isBlockNode(XMLElement htmlNode)  { return BLOCK_NODES.contains(htmlNode.getName()); }

    /**
     * Returns whether given HTML node is inline node.
     */
    private static boolean isInlineNode(XMLElement htmlNode)  { return INLINE_NODES.contains(htmlNode.getName()); }
}
