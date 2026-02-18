package snap.util;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses a Markdown file and returns a tree of nodes. Supported syntax is:
 *     # My Main Header                   |  Header1
 *     ## My Subheader                    |  Header2
 *     * My list item 1                   |  ListItem (reads till next ListItem or blank line)
 *     [My Link Text](http:...)           |  Link
 *     ![My Image Text](http:...)         |  Image
 *     {@literal @} [ Header1:CustomName]            |  Directive - to redefine rendering
 *     ``` My Code ```                    |  CodeBlock
 *     ~~~ My Code ~~~                    |  Runnable code
 *     ---                                |  Separator (maybe soon)
 *     > My block quote                   |  BlockQuote (maybe soon)
 */
public class MarkdownParser {

    // The parse text
    private ParseText _parseText;

    // The running list of document nodes
    private List<MarkdownNode> _documentNodes = new ArrayList<>();

    // Constants
    public static final String HEADER_MARKER = "#";
    public static final String LIST_ITEM_MARKER = "- ";
    public static final String LIST_ITEM_MARKER2 = "* ";
    public static final String LIST_ITEM_MARKER3 = "+ ";
    public static final String LINK_MARKER = "[";
    public static final String IMAGE_MARKER = "![";
    public static final String DIRECTIVE_MARKER = "@[";
    public static final String CODE_BLOCK_MARKER = "```";
    private static final String LINK_END_MARKER = "]";
    private static final String RUNNABLE_MARKER = "~~~";
    private static final String SEPARATOR_MARKER = "---";

    // Constant for Nodes that are stand-alone
    private static final String[] MIXABLE_NODE_MARKERS = { LINK_MARKER, IMAGE_MARKER, CODE_BLOCK_MARKER };
    private static final String[] NON_MIXABLE_NODE_MARKERS = { HEADER_MARKER, LIST_ITEM_MARKER, DIRECTIVE_MARKER };

    /**
     * Constructor.
     */
    public MarkdownParser()
    {
        super();
    }

    /**
     * Parses given markdown chars and returns document node.
     */
    public MarkdownNode parseMarkdownChars(CharSequence markdownChars)
    {
        // Set input chars
        _parseText = new ParseText(markdownChars);

        // Create document node and child nodes
        MarkdownNode documentNode = new MarkdownNode(MarkdownNode.NodeType.Document, null);
        _documentNodes = new ArrayList<>();

        // Read nodes
        while (hasChars()) {
            MarkdownNode nextNode = parseNextNode();
            if (nextNode != null)
                _documentNodes.add(nextNode);
        }

        // Set child nodes
        documentNode.setChildNodes(_documentNodes);

        // Return
        return documentNode;
    }

    /**
     * Parses the next node.
     */
    private MarkdownNode parseNextNode()
    {
        // Skip white space - just return if no more chars
        skipWhiteSpace();
        if (!hasChars())
            return null;

        // Handle headers
        if (nextCharsStartWith(HEADER_MARKER))
            return parseHeaderNode();

        // Handle list item
        if (nextCharsStartWithListItemMarker())
            return parseListNode();

        // Handle directive
        if (nextCharsStartWith(DIRECTIVE_MARKER))
            return parseDirectiveNode();

        // Parse inline node: Text, Link, Image, CodeBlock
        return parseMixableNode();
    }

    /**
     * Parses mixable nodes: Text, Link, Image, CodeBlock or Mixed.
     */
    private MarkdownNode parseMixableNode()
    {
        // Parse mixable node: Text, Link, Image, CodeBlock
        MarkdownNode mixableNode = parseMixableNodeImpl();

        // While next chars start with mixable node, parse and add child node
        while (nextCharsStartWithMixableNode()) {
            mixableNode = MarkdownNode.getMixedNodeForNode(mixableNode);
            MarkdownNode nextMixedNode = parseMixableNodeImpl();
            mixableNode.addChildNode(nextMixedNode);
        }

        // Return
        return mixableNode;
    }

    /**
     * Parses mixable nodes: Text, Link, Image, CodeBlock
     */
    private MarkdownNode parseMixableNodeImpl()
    {
        // Handle link
        if (nextCharsStartWith(LINK_MARKER))
            return parseLinkNode();

        // Handle image
        if (nextCharsStartWith(IMAGE_MARKER))
            return parseImageNode();

        // Handle code block
        if (nextCharsStartWith(CODE_BLOCK_MARKER))
            return parseCodeBlockNode();

        // Handle runnable block
        if (nextCharsStartWith(RUNNABLE_MARKER))
            return parseRunnableNode();

        // Handle separator block
        if (nextCharsStartWith(SEPARATOR_MARKER))
            return parseSeparatorNode();

        // Return text node
        return parseTextNode();
    }

    /**
     * Parses a Header node.
     */
    private MarkdownNode parseHeaderNode()
    {
        // Get header level by counting hash chars
        int headerLevel = 0;
        while (hasChars() && nextChar() == '#') {
            headerLevel++;
            eatChar();
        }

        // Get Header level NodeType and chars till line end
        MarkdownNode.NodeType nodeType = headerLevel == 1 ? MarkdownNode.NodeType.Header1 : MarkdownNode.NodeType.Header2;
        String charsTillLineEnd = getCharsTillLineEnd().toString().trim();

        // Return header node
        return new MarkdownNode(nodeType, charsTillLineEnd);
    }

    /**
     * Parses a text node.
     */
    private MarkdownNode parseTextNode()
    {
        String textChars = getCharsTillParagraphEnd().toString().trim();
        return new MarkdownNode(MarkdownNode.NodeType.Paragraph, textChars);
    }

    /**
     * Parses a code block node.
     */
    private MarkdownNode parseCodeBlockNode()
    {
        eatChars(CODE_BLOCK_MARKER.length());
        String charsTillBlockEnd = getCharsTillMatchingTerminator(CODE_BLOCK_MARKER).toString();
        return new MarkdownNode(MarkdownNode.NodeType.CodeBlock, charsTillBlockEnd);
    }

    /**
     * Parses a Runnable node.
     */
    private MarkdownNode parseRunnableNode()
    {
        // Get string
        eatChars(RUNNABLE_MARKER.length());
        String metaData = getCharsTillLineEnd().toString().replace("}", "");
        String charsTillBlockEnd = getCharsTillMatchingTerminator(RUNNABLE_MARKER).toString();

        // Replace occurrences of '$<num>' with CodeBlock at that index
        for (int dollarIndex = charsTillBlockEnd.indexOf('$'); dollarIndex >= 0; dollarIndex = charsTillBlockEnd.indexOf('$', dollarIndex)) {
            int endIndex = dollarIndex + 1;
            while (endIndex < charsTillBlockEnd.length() && Character.isDigit(charsTillBlockEnd.charAt(endIndex)))
                endIndex++;
            String codeBlockIndexStr = charsTillBlockEnd.substring(dollarIndex, endIndex);
            int codeBlockIndex = Convert.intValue(codeBlockIndexStr) - 1;
            MarkdownNode[] codeBlockNodes = ListUtils.filterToArray(_documentNodes, node -> node.getNodeType() == MarkdownNode.NodeType.CodeBlock, MarkdownNode.class);
            if (codeBlockIndex >= 0 && codeBlockIndex < codeBlockNodes.length) {
                MarkdownNode codeBlockNode = codeBlockNodes[codeBlockIndex];
                String codeBlockText = codeBlockNode.getText();
                charsTillBlockEnd = charsTillBlockEnd.replace(codeBlockIndexStr, codeBlockText);
                dollarIndex += codeBlockText.length();
            }
            else {
                System.out.println("MarkdownParser.parseRunnableNode: invalid codeBlockIndex: " + codeBlockIndexStr);
                dollarIndex = endIndex;
            }
        }

        // Return node
        MarkdownNode runnableNode = new MarkdownNode(MarkdownNode.NodeType.Runnable, charsTillBlockEnd);
        if (!metaData.isEmpty())
            runnableNode.setOtherText(metaData);
        return runnableNode;
    }

    /**
     * Parses a Separator node.
     */
    private MarkdownNode parseSeparatorNode()
    {
        eatChars(SEPARATOR_MARKER.length());
        while (nextChar() != '\n' && nextChar() != '\r') eatChar();
        eatLineEnd();
        return new MarkdownNode(MarkdownNode.NodeType.Separator, null);
    }

    /**
     * Parses a list node.
     */
    private MarkdownNode parseListNode()
    {
        // Create list node and listItemNodes
        MarkdownNode listNode = new MarkdownNode(MarkdownNode.NodeType.List, null);
        List<MarkdownNode> listItemNodes = new ArrayList<>();

        // Parse available list items and add to listItemNodes
        while (nextCharsStartWithListItemMarker()) {
            MarkdownNode listItemNode = parseListItemNode();
            listItemNodes.add(listItemNode);
        }

        // Add listItemsNodes to list node and return
        listNode.setChildNodes(listItemNodes);
        return listNode;
    }

    /**
     * Parses a list item node. These can contain
     */
    private MarkdownNode parseListItemNode()
    {
        // Eat identifier chars
        eatChars(LIST_ITEM_MARKER.length());

        // Parse mixable child node
        MarkdownNode mixableNode = parseMixableNode();

        // Create ListItem node
        MarkdownNode listItemNode = new MarkdownNode(MarkdownNode.NodeType.ListItem, null);

        // If node is mixed, move its children to new node, otherwise just add child
        if (mixableNode.getNodeType() == MarkdownNode.NodeType.Mixed)
            listItemNode.setChildNodes(mixableNode.getChildNodes());
        else listItemNode.addChildNode(mixableNode);

        // Return
        return listItemNode;
    }

    /**
     * Parses a link node.
     */
    private MarkdownNode parseLinkNode()
    {
        // Eat marker chars
        eatChars(LINK_MARKER.length());

        // Parse nodes till link close
        MarkdownNode mixableNode = parseMixableNode();

        // If missing link close char, complain
        if (!nextCharsStartWith(LINK_END_MARKER))
            System.err.println("MarkdownParser.parseLinkNode: Missing link close char");
        else eatChar();

        // Create link node
        MarkdownNode linkNode = new MarkdownNode(MarkdownNode.NodeType.Link, null);

        // If node is mixed, move its children to new node, otherwise just add child
        if (mixableNode.getNodeType() == MarkdownNode.NodeType.Mixed)
            linkNode.setChildNodes(mixableNode.getChildNodes());
        else linkNode.addChildNode(mixableNode);

        // Parse and set url text
        String urlAddr = parseLinkUrlAddress();
        linkNode.setOtherText(urlAddr);

        // Return
        return linkNode;
    }

    /**
     * Parses a link URL address.
     */
    private String parseLinkUrlAddress()
    {
        // If no link Url marker, just return
        skipWhiteSpace();
        if (!nextCharsStartWith("("))
            return null;

        // Return link chars
        eatChar();
        return getCharsTillMatchingTerminator(")").toString().trim();
    }

    /**
     * Parses an image node.
     */
    private MarkdownNode parseImageNode()
    {
        // Eat marker chars
        eatChars(IMAGE_MARKER.length());

        // Get chars till image text close
        String imageText = getCharsTillMatchingTerminator("]").toString().trim();

        // Create link node
        MarkdownNode linkNode = new MarkdownNode(MarkdownNode.NodeType.Image, imageText);

        // Parse and set url text
        String urlAddr = parseLinkUrlAddress();
        linkNode.setOtherText(urlAddr);

        // Return
        return linkNode;
    }

    /**
     * Parses a directive node.
     */
    private MarkdownNode parseDirectiveNode()
    {
        // Eat marker chars
        eatChars(DIRECTIVE_MARKER.length());

        // Get chars till link close
        String directiveText = getCharsTillMatchingTerminator(LINK_END_MARKER).toString().trim();

        // Create directive node
        MarkdownNode directiveNode = new MarkdownNode(MarkdownNode.NodeType.Directive, directiveText);

        // Return
        return directiveNode;
    }

    /**
     * Returns chars till paragraph end (which is either at next non-inline node start or line end).
     */
    private CharSequence getCharsTillParagraphEnd()
    {
        StringBuilder sb = new StringBuilder();

        // Iterate over chars until next mixable node start or line end to get chars
        while (hasChars()) {

            // If next chars start with mixable node, break
            boolean nextCharsStartWithMixableNode = ArrayUtils.hasMatch(MIXABLE_NODE_MARKERS, str -> nextCharsStartWith(str));
            if (nextCharsStartWithMixableNode)
                break;

            // If next char is newline, break
            char nextChar = nextChar();
            if (nextChar == '\n')
                break;

            // If next chars is link end, break
            if (nextCharsStartWith(LINK_END_MARKER))
                break;

            // Append char to buffer and eat
            sb.append(nextChar);
            eatChar();
        }

        // Return
        return sb;
    }

    /**
     * Returns whether next chars associated with current node line start with mixable node.
     * Eats any chars before next node.
     */
    private boolean nextCharsStartWithMixableNode()
    {
        // If not at empty line, return true (any next chars will be paragraph node). Except for link end marker
        if (!isAtEmptyLine()) {

            // If next char is link end, return false
            skipWhiteSpace();
            if (nextCharsStartWith(LINK_END_MARKER))
                return false;

            // Return true
            return true;
        }

        // Eat reset of line
        getCharsTillLineEnd();

        // If at empty line, return false (next node will be stand-alone)
        if (isAtEmptyLine())
            return false;

        // If next char is non-mixable node
        skipWhiteSpace();
        boolean nextCharStartsWithNonMixableNode = ArrayUtils.hasMatch(NON_MIXABLE_NODE_MARKERS, this::nextCharsStartWith);
        if (nextCharStartsWithNonMixableNode)
            return false;

        // Return true
        return true;
    }

    /**
     * Returns whether next chars start with list marker: '-', '*' or '+'.
     */
    private boolean nextCharsStartWithListItemMarker()
    {
        return nextCharsStartWith(LIST_ITEM_MARKER) || nextCharsStartWith(LIST_ITEM_MARKER2) || nextCharsStartWith(LIST_ITEM_MARKER3);
    }

    /**
     * Returns whether there are more chars.
     */
    private boolean hasChars()  { return _parseText.hasChars(); }

    /**
     * Returns the next char.
     */
    private char nextChar()  { return _parseText.nextChar(); }

    /**
     * Advances charIndex by one.
     */
    private void eatChar()  { _parseText.eatChar(); }

    /**
     * Advances charIndex by given char count.
     */
    private void eatChars(int charCount)  { _parseText.eatChars(charCount); }

    /**
     * Eats the line end char.
     */
    private void eatLineEnd()  { _parseText.eatLineEnd(); }

    /**
     * Returns whether next chars start with given string.
     */
    private boolean nextCharsStartWith(CharSequence startChars)  { return _parseText.nextCharsStartWith(startChars); }

    /**
     * Skips whitespace.
     */
    private void skipWhiteSpace()  { _parseText.skipWhiteSpace(); }

    /**
     * Returns the chars till line end.
     */
    private CharSequence getCharsTillLineEnd()  { return _parseText.getCharsTillLineEnd(); }

    /**
     * Returns the chars till matching terminator.
     */
    private CharSequence getCharsTillMatchingTerminator(CharSequence endChars)  { return _parseText.getCharsTillMatchingTerminator(endChars); }

    /**
     * Returns the length of leading whitespace chars for given char sequence.
     */
    private boolean isAtEmptyLine()  { return _parseText.isAtEmptyLine(); }
}
