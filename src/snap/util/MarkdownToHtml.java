package snap.util;
import snap.web.WebFile;
import java.util.List;

/**
 * Converts a list of MarkdownNode objects to an HTML XMLElement graph.
 *
 * Text content uses the mixed-content model: leading text is stored as the element's value,
 * and text following an inline element is stored as that element's tailContent.
 */
public class MarkdownToHtml {

    /**
     * Returns an HTML XMLElement graph for the given list of MarkdownNode.
     * The returned element is an {@code <html>} element containing a {@code <body>}.
     */
    public static XMLElement markdownNodesToHtml(List<MarkdownNode> markdownNodes)
    {
        XMLElement htmlElement = new XMLElement("html");
        XMLElement bodyElement = new XMLElement("body");
        htmlElement.addElement(bodyElement);
        addBlockNodesToElement(markdownNodes, bodyElement);
        return htmlElement;
    }

    /**
     * Adds block-level MarkdownNodes to the given parent XMLElement.
     */
    private static void addBlockNodesToElement(List<MarkdownNode> markdownNodes, XMLElement parentElement)
    {
        for (MarkdownNode markdownNode : markdownNodes)
            addBlockNodeToElement(markdownNode, parentElement);
    }

    /**
     * Adds a single MarkdownNode to the given parent XMLElement as a block-level element.
     */
    private static void addBlockNodeToElement(MarkdownNode markdownNode, XMLElement parentElement)
    {
        switch (markdownNode.getNodeType()) {
            case Document -> addBlockNodesToElement(markdownNode.getChildNodes(), parentElement);
            case Header -> addHeaderNodeToElement(markdownNode, parentElement);
            case Paragraph -> addParagraphNodeToElement(markdownNode, parentElement);
            case List -> addListNodeToElement(markdownNode, parentElement);
            case ListItem -> addListItemNodeToElement(markdownNode, parentElement);
            case CodeBlock, RunBlock -> addCodeBlockNodeToElement(markdownNode, parentElement);
            case Separator -> parentElement.addElement(new XMLElement("hr"));
            case Directive -> { /* skip */ }
            default -> addInlineChildrenToElement(List.of(markdownNode), parentElement);
        }
    }

    /**
     * Adds a Header node as {@code <h1>} through {@code <h6>}.
     */
    private static void addHeaderNodeToElement(MarkdownNode headerNode, XMLElement parentElement)
    {
        int level = MarkdownUtils.getHeaderLevel(headerNode);
        if (level <= 0) level = 1;
        level = Math.min(level, 6);
        XMLElement headerElement = new XMLElement("h" + level);
        addBlockContentToElement(headerNode, headerElement);
        parentElement.addElement(headerElement);
    }

    /**
     * Adds a Paragraph node as a {@code <p>} element.
     */
    private static void addParagraphNodeToElement(MarkdownNode paragraphNode, XMLElement parentElement)
    {
        XMLElement pElement = new XMLElement("p");
        addBlockContentToElement(paragraphNode, pElement);
        parentElement.addElement(pElement);
    }

    /**
     * Adds a List node as a {@code <ul>} element.
     */
    private static void addListNodeToElement(MarkdownNode listNode, XMLElement parentElement)
    {
        XMLElement ulElement = new XMLElement("ul");
        for (MarkdownNode childNode : listNode.getChildNodes())
            addBlockNodeToElement(childNode, ulElement);
        parentElement.addElement(ulElement);
    }

    /**
     * Adds a ListItem node as an {@code <li>} element.
     * Inline children are added as inline content; nested List children are added as blocks.
     */
    private static void addListItemNodeToElement(MarkdownNode listItemNode, XMLElement parentElement)
    {
        XMLElement liElement = new XMLElement("li");
        List<MarkdownNode> childNodes = listItemNode.getChildNodes();

        if (childNodes.isEmpty()) {
            String text = listItemNode.getText();
            if (text != null) liElement.setValue(text);
        }
        else {
            // Gather inline children and pass them together so mixed-content text positioning works
            java.util.List<MarkdownNode> inlineBuffer = new java.util.ArrayList<>();
            for (MarkdownNode childNode : childNodes) {
                if (childNode.getNodeType() == MarkdownNode.NodeType.List) {
                    if (!inlineBuffer.isEmpty()) {
                        addInlineChildrenToElement(inlineBuffer, liElement);
                        inlineBuffer.clear();
                    }
                    addListNodeToElement(childNode, liElement);
                }
                else inlineBuffer.add(childNode);
            }
            if (!inlineBuffer.isEmpty())
                addInlineChildrenToElement(inlineBuffer, liElement);
        }

        parentElement.addElement(liElement);
    }

    /**
     * Adds a CodeBlock or RunBlock node as a {@code <pre><code>} element.
     */
    private static void addCodeBlockNodeToElement(MarkdownNode codeNode, XMLElement parentElement)
    {
        XMLElement preElement = new XMLElement("pre");
        XMLElement codeElement = new XMLElement("code");
        codeElement.setValue(codeNode.getText());
        preElement.addElement(codeElement);
        parentElement.addElement(preElement);
    }

    /**
     * Adds the content of a block node (header, paragraph, etc.) to the given element.
     * Uses inline children when present; falls back to the node's text value.
     */
    private static void addBlockContentToElement(MarkdownNode markdownNode, XMLElement element)
    {
        List<MarkdownNode> childNodes = markdownNode.getChildNodes();
        if (!childNodes.isEmpty())
            addInlineChildrenToElement(childNodes, element);
        else if (markdownNode.getText() != null)
            element.setValue(markdownNode.getText());
    }

    /**
     * Adds inline MarkdownNode children to the given parent XMLElement using the mixed-content model:
     * leading text becomes the parent's value, and text following an inline element becomes that
     * element's tailContent.
     */
    private static void addInlineChildrenToElement(List<MarkdownNode> childNodes, XMLElement parentElement)
    {
        if (childNodes.isEmpty())
            return;

        XMLElement lastInlineElement = null;

        for (MarkdownNode childNode : childNodes) {
            if (childNode.getNodeType() == MarkdownNode.NodeType.Text) {
                String text = childNode.getText();
                if (text == null) text = "";
                if (lastInlineElement == null)
                    parentElement.setValue(text);
                else
                    lastInlineElement.setTailContent(text);
            }
            else {
                XMLElement inlineElement = createInlineElement(childNode);
                parentElement.addElement(inlineElement);
                lastInlineElement = inlineElement;
            }
        }
    }

    /**
     * Creates an inline XMLElement for the given MarkdownNode.
     */
    private static XMLElement createInlineElement(MarkdownNode markdownNode)
    {
        return switch (markdownNode.getNodeType()) {
            case BoldText -> {
                XMLElement strong = new XMLElement("strong");
                addBlockContentToElement(markdownNode, strong);
                yield strong;
            }
            case ItalicText -> {
                XMLElement em = new XMLElement("em");
                addBlockContentToElement(markdownNode, em);
                yield em;
            }
            case BoldItalicText -> {
                XMLElement strong = new XMLElement("strong");
                XMLElement em = new XMLElement("em");
                em.setValue(markdownNode.getText());
                strong.addElement(em);
                yield strong;
            }
            case QuoteText -> {
                XMLElement code = new XMLElement("code");
                code.setValue(markdownNode.getText());
                yield code;
            }
            case Link -> {
                XMLElement a = new XMLElement("a");
                String href = markdownNode.getOtherText();
                if (href != null) a.add("href", href);
                addBlockContentToElement(markdownNode, a);
                yield a;
            }
            case Image -> {
                XMLElement img = new XMLElement("img");
                String src = markdownNode.getOtherText();
                if (src != null) img.add("src", src);
                String alt = markdownNode.getText();
                if (alt != null) img.add("alt", alt);
                yield img;
            }
            default -> {
                XMLElement span = new XMLElement("span");
                addBlockContentToElement(markdownNode, span);
                yield span;
            }
        };
    }

    /**
     * Standard main method for testing.
     */
    public static void main(String[] args)
    {
        //WebFile markdownFile = WebFile.getFileForPath("/Users/jeff/SnapDev/Snaptris/src/snaptris/ReadMe.md");
        WebFile markdownFile = WebFile.getFileForPath("/Users/jeff/SnapDev/SnapCode/src/snapcode/app/HomePage.md");
        MarkdownNode markdownNode = new MarkdownParser().parseMarkdownChars(markdownFile.getText());
        XMLElement htmlElement = markdownNodesToHtml(markdownNode.getChildNodes());
        System.out.println(htmlElement);
        SnapUtils.writeBytes(htmlElement.getString().getBytes(), "/tmp/Markdown.html");
    }
}
