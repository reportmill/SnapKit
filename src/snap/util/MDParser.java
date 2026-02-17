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
public class MDParser {

    // The input
    private CharSequence _input;

    // The char index
    private int _charIndex;

    // The running list of root nodes
    private List<MDNode> _rootNodes = new ArrayList<>();

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
    public MDParser()
    {
        super();
    }

    /**
     * Parses given text string and returns node.
     */
    public MDNode parseMarkdownChars(CharSequence theChars)
    {
        // Set input chars
        _input = theChars;
        _charIndex = 0;

        // Create rootNode and child nodes
        MDNode rootNode = new MDNode(MDNode.NodeType.Root, null);
        _rootNodes = new ArrayList<>();

        // Read nodes
        while (hasChars()) {
            MDNode nextNode = parseNextNode();
            if (nextNode != null)
                _rootNodes.add(nextNode);
        }

        // Set child nodes
        rootNode.setChildNodes(_rootNodes);

        // Return
        return rootNode;
    }

    /**
     * Parses the next node.
     */
    private MDNode parseNextNode()
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

        // Parse mixable node: Text, Link, Image, CodeBlock
        return parseMixableNode();
    }

    /**
     * Parses mixable nodes: Text, Link, Image, CodeBlock or Mixed.
     */
    private MDNode parseMixableNode()
    {
        // Parse mixable node: Text, Link, Image, CodeBlock
        MDNode mixableNode = parseMixableNodeImpl();

        // While next chars start with mixable node, parse and add child node
        while (nextCharsStartWithMixableNode()) {
            mixableNode = MDNode.getMixedNodeForNode(mixableNode);
            MDNode nextMixedNode = parseMixableNodeImpl();
            mixableNode.addChildNode(nextMixedNode);
        }

        // Return
        return mixableNode;
    }

    /**
     * Parses mixable nodes: Text, Link, Image, CodeBlock
     */
    private MDNode parseMixableNodeImpl()
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
    private MDNode parseHeaderNode()
    {
        // Get header level by counting hash chars
        int headerLevel = 0;
        while (hasChars() && nextChar() == '#') {
            headerLevel++;
            eatChar();
        }

        // Get Header level NodeType and chars till line end
        MDNode.NodeType nodeType = headerLevel == 1 ? MDNode.NodeType.Header1 : MDNode.NodeType.Header2;
        String charsTillLineEnd = getCharsTillLineEnd().toString().trim();

        // Return header node
        return new MDNode(nodeType, charsTillLineEnd);
    }

    /**
     * Parses a text node.
     */
    private MDNode parseTextNode()
    {
        String textChars = getCharsTillTextEnd().toString().trim();
        return new MDNode(MDNode.NodeType.Text, textChars);
    }

    /**
     * Parses a code block node.
     */
    private MDNode parseCodeBlockNode()
    {
        eatChars(CODE_BLOCK_MARKER.length());
        String charsTillBlockEnd = getCharsTillMatchingTerminator(CODE_BLOCK_MARKER).toString();
        return new MDNode(MDNode.NodeType.CodeBlock, charsTillBlockEnd);
    }

    /**
     * Parses a Runnable node.
     */
    private MDNode parseRunnableNode()
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
            MDNode[] codeBlockNodes = ListUtils.filterToArray(_rootNodes, node -> node.getNodeType() == MDNode.NodeType.CodeBlock, MDNode.class);
            if (codeBlockIndex >= 0 && codeBlockIndex < codeBlockNodes.length) {
                MDNode codeBlockNode = codeBlockNodes[codeBlockIndex];
                String codeBlockText = codeBlockNode.getText();
                charsTillBlockEnd = charsTillBlockEnd.replace(codeBlockIndexStr, codeBlockText);
                dollarIndex += codeBlockText.length();
            }
            else {
                System.out.println("MDParser.parseRunnableNode: invalid codeBlockIndex: " + codeBlockIndexStr);
                dollarIndex = endIndex;
            }
        }

        // Return node
        MDNode runnableNode = new MDNode(MDNode.NodeType.Runnable, charsTillBlockEnd);
        if (!metaData.isEmpty())
            runnableNode.setOtherText(metaData);
        return runnableNode;
    }

    /**
     * Parses a Separator node.
     */
    private MDNode parseSeparatorNode()
    {
        eatChars(SEPARATOR_MARKER.length());
        while (nextChar() != '\n' && nextChar() != '\r') eatChar();
        eatLineEnd();
        return new MDNode(MDNode.NodeType.Separator, null);
    }

    /**
     * Parses a list node.
     */
    private MDNode parseListNode()
    {
        // Create list node and listItemNodes
        MDNode listNode = new MDNode(MDNode.NodeType.List, null);
        List<MDNode> listItemNodes = new ArrayList<>();

        // Parse available list items and add to listItemNodes
        while (nextCharsStartWithListItemMarker()) {
            MDNode listItemNode = parseListItemNode();
            listItemNodes.add(listItemNode);
        }

        // Add listItemsNodes to list node and return
        listNode.setChildNodes(listItemNodes);
        return listNode;
    }

    /**
     * Parses a list item node. These can contain
     */
    private MDNode parseListItemNode()
    {
        // Eat identifier chars
        eatChars(LIST_ITEM_MARKER.length());

        // Parse mixable child node
        MDNode mixableNode = parseMixableNode();

        // Create ListItem node
        MDNode listItemNode = new MDNode(MDNode.NodeType.ListItem, null);

        // If node is mixed, move its children to new node, otherwise just add child
        if (mixableNode.getNodeType() == MDNode.NodeType.Mixed)
            listItemNode.setChildNodes(mixableNode.getChildNodes());
        else listItemNode.addChildNode(mixableNode);

        // Return
        return listItemNode;
    }

    /**
     * Parses a link node.
     */
    private MDNode parseLinkNode()
    {
        // Eat marker chars
        eatChars(LINK_MARKER.length());

        // Parse nodes till link close
        MDNode mixableNode = parseMixableNode();

        // If missing link close char, complain
        if (!nextCharsStartWith(LINK_END_MARKER))
            System.err.println("MDParser.parseLinkNode: Missing link close char");
        else eatChar();

        // Create link node
        MDNode linkNode = new MDNode(MDNode.NodeType.Link, null);

        // If node is mixed, move its children to new node, otherwise just add child
        if (mixableNode.getNodeType() == MDNode.NodeType.Mixed)
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
    private MDNode parseImageNode()
    {
        // Eat marker chars
        eatChars(IMAGE_MARKER.length());

        // Get chars till image text close
        String imageText = getCharsTillMatchingTerminator("]").toString().trim();

        // Create link node
        MDNode linkNode = new MDNode(MDNode.NodeType.Image, imageText);

        // Parse and set url text
        String urlAddr = parseLinkUrlAddress();
        linkNode.setOtherText(urlAddr);

        // Return
        return linkNode;
    }

    /**
     * Parses a directive node.
     */
    private MDNode parseDirectiveNode()
    {
        // Eat marker chars
        eatChars(DIRECTIVE_MARKER.length());

        // Get chars till link close
        String directiveText = getCharsTillMatchingTerminator(LINK_END_MARKER).toString().trim();

        // Create directive node
        MDNode directiveNode = new MDNode(MDNode.NodeType.Directive, directiveText);

        // Return
        return directiveNode;
    }

    /**
     * Returns the char at given char index.
     */
    private char charAt(int charIndex)  { return _input.charAt(charIndex); }

    /**
     * Returns whether there are more chars.
     */
    private boolean hasChars()  { return _charIndex < _input.length(); }

    /**
     * Returns the next char.
     */
    private char nextChar()  { return _input.charAt(_charIndex); }

    /**
     * Advances charIndex by one.
     */
    private void eatChar()  { _charIndex++; }

    /**
     * Advances charIndex by given char count.
     */
    private void eatChars(int charCount)  { _charIndex += charCount; }

    /**
     * Eats the line end char.
     */
    private void eatLineEnd()
    {
        if (nextChar() == '\r')
            eatChar();
        if (hasChars() && nextChar() == '\n')
            eatChar();
    }

    /**
     * Returns whether next chars start with given string.
     */
    private boolean nextCharsStartWith(CharSequence startChars)
    {
        // If not enough chars, return false
        int charsLeft = _input.length() - _charIndex;
        if (charsLeft < startChars.length())
            return false;

        // Iterate over startChars and return false if any don't match nextChars
        for (int charIndex = 0; charIndex < startChars.length(); charIndex++) {
            if (startChars.charAt(charIndex) != _input.charAt(_charIndex + charIndex))
                return false;
        }

        // Return true
        return true;
    }

    /**
     * Skips whitespace.
     */
    private void skipWhiteSpace()
    {
        while (_charIndex < _input.length() && Character.isWhitespace(nextChar()))
            _charIndex++;
    }

    /**
     * Returns the chars till line end.
     */
    private CharSequence getCharsTillLineEnd()
    {
        // Get startCharIndex and eatChars till line end or text end
        int startCharIndex = _charIndex;
        while (hasChars() && !CharSequenceUtils.isLineEndChar(nextChar()))
            eatChar();

        // Get endCharIndex and eatLineEnd
        int endCharIndex = _charIndex;
        if (hasChars())
            eatLineEnd();

        // Return chars
        return getCharsForCharRange(startCharIndex, endCharIndex);
    }

    /**
     * Returns the chars till matching terminator.
     */
    private CharSequence getCharsTillMatchingTerminator(CharSequence endChars)
    {
        // If leading newline, just skip it
        if (hasChars() && CharSequenceUtils.isLineEndChar(nextChar()))
            eatLineEnd();

        // Get startCharIndex and eatChars till matching chars or text end
        int startCharIndex = _charIndex;
        while (hasChars() && !nextCharsStartWith(endChars))
            eatChar();

        // Get endCharIndex and eatChars for matching chars
        int endCharIndex = _charIndex;
        if (CharSequenceUtils.isLineEndChar(charAt(endCharIndex - 1)))
            endCharIndex--;

        // Get endCharIndex and eatChars for matching chars
        if (hasChars())
            eatChars(endChars.length());

        // Return chars
        return getCharsForCharRange(startCharIndex, endCharIndex);
    }

    /**
     * Returns chars till text end (which is either at next non-mixable node start or line end).
     */
    private CharSequence getCharsTillTextEnd()
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
     * Returns chars for char range.
     */
    private CharSequence getCharsForCharRange(int startCharIndex, int endCharIndex)
    {
        return _input.subSequence(startCharIndex, endCharIndex);
    }

    /**
     * Returns the length of leading whitespace chars for given char sequence.
     */
    private boolean isAtEmptyLine()
    {
        // Get leading space chars
        for (int i = _charIndex; i < _input.length(); i++) {
            char loopChar = _input.charAt(i);
            if (!Character.isWhitespace(loopChar))
                return false;
            if (loopChar == '\n')
                break;
        }

        // Return
        return true;
    }

    /**
     * Returns whether next chars associated with current node line start with mixable node.
     * Eats any chars before next node.
     */
    private boolean nextCharsStartWithMixableNode()
    {
        // If not at empty line, return true (any next chars will be text node). Except for link end marker
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
}
