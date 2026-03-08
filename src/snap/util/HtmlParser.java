package snap.util;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for HTML.
 */
public class HtmlParser {

    // The HTML node
    private XMLElement _htmlNode;

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
        return switch (htmlNode.getName()) {
            case "h1" -> createMarkdownHeaderNodeForHtml(htmlNode);
            case "ul" -> createMarkdownListNodeForHtml(htmlNode);
            case "li" -> createMarkdownListItemNodeForHtml(htmlNode);
            case "code" -> createMarkdownCodeBlockNodeForHtml(htmlNode);
            case "p" -> createMarkdownParagraphNodeForHtml(htmlNode);
            case "a" -> createMarkdownLinkNodeForHtml(htmlNode);
            case "img" -> createMarkdownImageNodeForHtml(htmlNode);
            case "pre" -> createMarkdwonTextNodeForHtml(htmlNode);
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
    private MarkdownNode createMarkdownListItemNodeForHtml(XMLElement htmlNode)
    {
        String listText = htmlNode.getValue();
        if (listText == null)
            return null;
        MarkdownNode textMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Text, listText);
        MarkdownNode paragraphMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Paragraph, listText);
        paragraphMarkdownNode.addChildNode(textMarkdownNode);
        MarkdownNode listItemMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.ListItem, listText);
        listItemMarkdownNode.addChildNode(paragraphMarkdownNode);
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
        String codeText = htmlNode.getValue();
        MarkdownNode codeBlockMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Paragraph, codeText);
        return codeBlockMarkdownNode;
    }

    /**
     * Returns a markdown link node for given html link node.
     */
    private MarkdownNode createMarkdownLinkNodeForHtml(XMLElement htmlNode)
    {
        String linkText = htmlNode.getValue();
        String linkUrl = htmlNode.getAttributeValue("href");
        MarkdownNode linkMarkdownNode = new MarkdownNode(MarkdownNode.NodeType.Link, linkText);
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
}
