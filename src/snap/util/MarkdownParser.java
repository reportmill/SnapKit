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
 *     ~~~ My Code ~~~                    |  RunBlock code
 *     ---                                |  Separator (maybe soon)
 *     > My block quote                   |  BlockQuote (maybe soon)
 */
public class MarkdownParser {

    // The parse text
    private ParseText _parseText;

    // The current indent level
    private int _indentLevel;

    // The current line indent
    private int _lineIndent;

    // The running list of document nodes
    private List<MarkdownNode> _documentNodes = new ArrayList<>();

    // Constants
    public static final String HEADER_MARKER = "#";
    public static final String LIST_ITEM_MARKER = "- ";
    public static final String LIST_ITEM_MARKER2 = "* ";
    public static final String LIST_ITEM_MARKER3 = "+ ";
    public static final String LINK_MARKER = "[";
    private static final String LINK_END_MARKER = "]";
    public static final String IMAGE_MARKER = "![";
    public static final String CODE_BLOCK_MARKER = "```";
    private static final String RUN_BLOCK_MARKER = "~~~";
    private static final String SEPARATOR_MARKER = "---";
    public static final String DIRECTIVE_MARKER = "@[";

    // Constant for Nodes that are stand-alone
    private static final String[] BLOCK_NODE_MARKERS = { HEADER_MARKER, LIST_ITEM_MARKER, LIST_ITEM_MARKER2, LIST_ITEM_MARKER3,
            CODE_BLOCK_MARKER, RUN_BLOCK_MARKER, DIRECTIVE_MARKER };
    private static final String[] INLINE_NODE_MARKERS = { LINK_MARKER, IMAGE_MARKER };

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

        // Update indent vars
        int lastIndentLevel = _indentLevel;
        int lastLineIndent = _lineIndent;
        _lineIndent = _parseText.getLineIndent();
        if (_lineIndent > lastLineIndent + 2)
            _indentLevel++;

        // Parse next node, set indent level and reset indent vars
        MarkdownNode nextNode = parseNextNodeImpl();
        nextNode.setIndentLevel(_indentLevel);
        _indentLevel = lastIndentLevel;
        _lineIndent = lastLineIndent;

        // Return
        return nextNode;
    }

    /**
     * Parses the next node.
     */
    private MarkdownNode parseNextNodeImpl()
    {
        // Handle headers
        if (nextCharsStartWith(HEADER_MARKER))
            return parseHeaderNode();

        // Handle list item
        if (nextCharsStartWithListItemMarker())
            return parseListNode();

        // Handle separator block
        if (nextCharsStartWith(SEPARATOR_MARKER))
            return parseSeparatorNode();

        // Handle code block
        if (nextCharsStartWith(CODE_BLOCK_MARKER))
            return parseCodeBlockNode();

        // Handle run block
        if (nextCharsStartWith(RUN_BLOCK_MARKER))
            return parseRunBlockNode();

        // Handle directive
        if (nextCharsStartWith(DIRECTIVE_MARKER))
            return parseDirectiveNode();

        // Parse paragraph
        return parseParagraphNode();
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
        String charsTillLineEnd = getCharsTillLineEnd().toString().trim();

        // Return header node
        MarkdownNode headerNode = new MarkdownNode(MarkdownNode.NodeType.Header, charsTillLineEnd);
        headerNode.setAttributeValue(MarkdownNode.HEADER_LEVEL, headerLevel);
        return headerNode;
    }

    /**
     * Parses a list node.
     */
    private MarkdownNode parseListNode()
    {
        // Create list node and listItemNodes
        MarkdownNode listNode = new MarkdownNode(MarkdownNode.NodeType.List, null);
        List<MarkdownNode> listItemNodes = new ArrayList<>();
        int lineIndent = _parseText.getLineIndent();

        // Parse available list items and add to listItemNodes
        while (nextCharsStartWithListItemMarker() && (listItemNodes.isEmpty() || _parseText.getLineIndent() >= lineIndent)) {
            lineIndent = _parseText.getLineIndent();
            MarkdownNode listItemNode = parseListItemNode();
            listItemNodes.add(listItemNode);
        }

        // Add list items nodes to list node and return
        listNode.setChildNodes(listItemNodes);
        return listNode;
    }

    /**
     * Parses a list item node.
     */
    private MarkdownNode parseListItemNode()
    {
        // Eat identifier chars and get current indent for this list item
        eatChars(LIST_ITEM_MARKER.length());
        int lineIndent = _parseText.getLineIndent();

        // Create and return ListItem node with paragraph node
        MarkdownNode listItemNode = new MarkdownNode(MarkdownNode.NodeType.ListItem, null);
        MarkdownNode paragraphNode = parseParagraphNode();
        listItemNode.addChildNode(paragraphNode);

        // If followed by blank line, eat it (should also mark list as 'loose')
        if (isAtBlankLine())
            _parseText.eatTillLineEnd();

        // If next line is indented, parse node and add to list
        while (_parseText.getLineIndent() > lineIndent + 2) {
            MarkdownNode nextBlockNode = parseNextNode();
            listItemNode.addChildNode(nextBlockNode);
            if (isAtBlankLine())
                _parseText.eatTillLineEnd();
        }

        // Return
        return listItemNode;
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
     * Parses a code block node.
     */
    private MarkdownNode parseCodeBlockNode()
    {
        eatChars(CODE_BLOCK_MARKER.length());
        String charsTillBlockEnd = getCharsTillMatchingTerminator(CODE_BLOCK_MARKER).toString();
        return new MarkdownNode(MarkdownNode.NodeType.CodeBlock, charsTillBlockEnd);
    }

    /**
     * Parses a RunBlock node.
     */
    private MarkdownNode parseRunBlockNode()
    {
        // Get string
        eatChars(RUN_BLOCK_MARKER.length());
        String metaData = getCharsTillLineEnd().toString().replace("}", "");
        String charsTillBlockEnd = getCharsTillMatchingTerminator(RUN_BLOCK_MARKER).toString();

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
                System.out.println("MarkdownParser.parseRunBlockNode: invalid codeBlockIndex: " + codeBlockIndexStr);
                dollarIndex = endIndex;
            }
        }

        // Return node
        MarkdownNode runBlockNode = new MarkdownNode(MarkdownNode.NodeType.RunBlock, charsTillBlockEnd);
        if (!metaData.isEmpty())
            runBlockNode.setOtherText(metaData);
        return runBlockNode;
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

        // Create and return directive node
        MarkdownNode directiveNode = new MarkdownNode(MarkdownNode.NodeType.Directive, directiveText);
        return directiveNode;
    }

    /**
     * Parses paragraph node.
     */
    private MarkdownNode parseParagraphNode()
    {
        MarkdownNode paragraphNode = new MarkdownNode(MarkdownNode.NodeType.Paragraph, null);
        List<MarkdownNode> inlineNodes = parseInlineNodes();
        paragraphNode.setChildNodes(inlineNodes);
        return paragraphNode;
    }

    /**
     * Parses inline nodes.
     */
    private List<MarkdownNode> parseInlineNodes()
    {
        // Parse inline node: Text, Link, Image, CodeBlock
        MarkdownNode inlineNode = parseInlineNode();
        List<MarkdownNode> inlineNodes = new ArrayList<>();
        inlineNodes.add(inlineNode);

        // While next chars start with inline node, parse and add child node
        while (nextCharsStartWithInlineNode()) {
            inlineNode = parseInlineNode();
            inlineNodes.add(inlineNode);
        }

        // Return
        return inlineNodes;
    }

    /**
     * Parses inline node: link, image, code span, bold, italic, text.
     */
    private MarkdownNode parseInlineNode()
    {
        // Handle link
        if (nextCharsStartWith(LINK_MARKER))
            return parseLinkNode();

        // Handle image
        if (nextCharsStartWith(IMAGE_MARKER))
            return parseImageNode();

        // Return text node
        return parseTextNode();
    }

    /**
     * Parses a text node.
     */
    private MarkdownNode parseTextNode()
    {
        String textChars = getCharsTillTextEnd().toString().trim();
        return new MarkdownNode(MarkdownNode.NodeType.Text, textChars);
    }

    /**
     * Parses a link node.
     */
    private MarkdownNode parseLinkNode()
    {
        // Eat marker chars
        eatChars(LINK_MARKER.length());

        // Parse paragraph node
        List<MarkdownNode> inlineNodes = parseInlineNodes();

        // If missing link close char, complain
        if (!nextCharsStartWith(LINK_END_MARKER))
            System.err.println("MarkdownParser.parseLinkNode: Missing link close char");
        else eatChar();

        // Create link node with paragraph node as child
        MarkdownNode linkNode = new MarkdownNode(MarkdownNode.NodeType.Link, null);
        linkNode.setChildNodes(inlineNodes);

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
     * Returns chars till text end (which is either at next inline node start or line end).
     */
    private CharSequence getCharsTillTextEnd()
    {
        StringBuilder sb = new StringBuilder();

        // Iterate over chars until next inline node start or line end to get chars
        while (hasChars()) {

            // If next chars start with well-defined inline node marker, break
            if (ArrayUtils.hasMatch(INLINE_NODE_MARKERS, this::nextCharsStartWith))
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
     * Returns whether next chars associated with current node line start with inline node.
     * Eats any chars before next node.
     */
    private boolean nextCharsStartWithInlineNode()
    {
        // If not at empty line, return true (any next chars will be paragraph node). Except for link end marker
        if (!isAtBlankLine()) {

            // If next char is link end, return false
            skipWhiteSpace();
            if (nextCharsStartWith(LINK_END_MARKER))
                return false;

            // Return true
            return true;
        }

        // Eat reset of line
        getCharsTillLineEnd();

        // If at empty line, return false (next node will be block node)
        if (isAtBlankLine())
            return false;

        // If next chars are block node, return false
        skipWhiteSpace();
        if (ArrayUtils.hasMatch(BLOCK_NODE_MARKERS, this::nextCharsStartWith))
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
     * Returns whether current parse location is at a blank line.
     */
    private boolean isAtBlankLine()  { return _parseText.isAtBlankLine(); }
}
