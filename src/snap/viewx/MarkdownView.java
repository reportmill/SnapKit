package snap.viewx;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.text.TextModel;
import snap.text.TextLink;
import snap.text.TextStyle;
import snap.util.*;
import snap.view.*;
import snap.web.WebURL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This view class renders mark down.
 */
public class MarkdownView extends ChildView {

    // The source url (and it's parent)
    private WebURL _sourceUrl, _sourceDirUrl;

    // The document markdown node
    private MarkdownNode _documentNode;

    // The selected code block node
    private MarkdownNode _selCodeBlockNode;

    // Map of directive values
    private Map<String,String> _directives = new HashMap<>();

    // Constants
    private static final Insets DOC_PADDING = new Insets(10, 20, 20, 20);
    private static final Insets HEADER1_MARGIN = new Insets(24, 8, 8, 8);
    private static final Insets HEADER2_MARGIN = new Insets(24, 8, 8, 8);
    private static final Insets SEPARATOR_MARGIN = new Insets(8, 8, 8, 8);
    private static final Insets GENERAL_MARGIN = new Insets(18, 8, 18, 8);
    private static final Insets GENERAL_PADDING = new Insets(16, 16, 16, 16);
    private static final Insets NO_MARGIN = Insets.EMPTY;
    private static final Insets INLINE_PADDING = new Insets(8, 8, 8, 8);
    private static Color BLOCK_COLOR = new Color(.96, .97, .98);
    private static Color BLOCK_BORDER_COLOR = BLOCK_COLOR.blend(Color.BLACK, .15);
    private static Border BLOCK_BORDER = Border.createLineBorder(BLOCK_BORDER_COLOR, 1);
    private static Color SEPARATOR_COLOR = BLOCK_COLOR.blend(Color.BLACK, .1);

    /**
     * Constructor.
     */
    public MarkdownView()
    {
        super();
        setPadding(DOC_PADDING);
        setFill(ViewTheme.get().getContentColor());
    }

    /**
     * Returns the Source URL.
     */
    public WebURL getSourceUrl()  { return _sourceUrl; }

    /**
     * Sets the Source URL.
     */
    public void setSourceUrl(WebURL sourceUrl)
    {
        _sourceUrl = sourceUrl;
        if (_sourceUrl != null)
            _sourceDirUrl = sourceUrl.getParent();
    }

    /**
     * Sets Markdown string.
     */
    public void setMarkdown(String markdownStr)
    {
        _documentNode = new MarkdownParser().parseMarkdownChars(markdownStr);
        List<MarkdownNode> documentNodes = _documentNode.getChildNodes();

        for (MarkdownNode node : documentNodes)
            addViewForNode(node);
    }

    /**
     * Returns the document markdown node.
     */
    public MarkdownNode getDocumentNode()  { return _documentNode; }

    /**
     * Returns the markdown nodes.
     */
    public List<MarkdownNode> getMarkdownNodes()  { return _documentNode.getChildNodes(); }

    /**
     * Returns the directive value.
     */
    public boolean isDirectiveSet(String aKey)
    {
        return _directives.get(aKey) != null;
    }

    /**
     * Returns the directive value.
     */
    public String getDirectiveValue(String aKey)
    {
        return _directives.get(aKey);
    }

    /**
     * Sets the directive value.
     */
    public void setDirectiveValue(String aKey, String aValue)
    {
        _directives.put(aKey, aValue);
    }

    /**
     * Returns the selected code block node.
     */
    public MarkdownNode getSelCodeBlockNode()
    {
        if (_selCodeBlockNode != null)
            return _selCodeBlockNode;

        List<MarkdownNode> docNodes = _documentNode.getChildNodes();
        return ListUtils.findMatch(docNodes, node -> node.getNodeType() == MarkdownNode.NodeType.CodeBlock);
    }

    /**
     * Adds a view for given node.
     */
    protected void addViewForNode(MarkdownNode markdownNode)
    {
        // If view provided for node, add it
        View nodeView = createViewForNode(markdownNode);
        if (nodeView != null)
            addViewForNode(nodeView, markdownNode);

        // Otherwise
        else if (markdownNode.getNodeType() == MarkdownNode.NodeType.List)
            addViewForListNode(markdownNode);

        // If Header, add separator
        if (markdownNode.getNodeType() == MarkdownNode.NodeType.Header)
            addViewForSeparatorNode();
    }

    /**
     * Adds a view for given node.
     */
    protected void addViewForListNode(MarkdownNode listNode)
    {
        List<MarkdownNode> listItemNodes = listNode.getChildNodes();
        listItemNodes.forEach(this::addViewForListItemNode);
    }

    /**
     * Adds a view for given node.
     */
    protected void addViewForListItemNode(MarkdownNode listItemNode)
    {
        // Create view for paragraph node
        MarkdownNode paragraphNode = listItemNode.getChildNodes().get(0);
        RowView paragraphNodeView = createViewForParagraphNode(paragraphNode);
        addViewForNode(paragraphNodeView, listItemNode);

        // If first child is TextArea, add bullet
        if (paragraphNodeView.getChild(0) instanceof TextArea textArea)
            textArea.getTextModel().addChars("• ", 0);

        // Otherwise create text area and insert
        else {
            View bulletTextArea = createViewForTextNode(new MarkdownNode(MarkdownNode.NodeType.Text, "• "));
            bulletTextArea.setMargin(NO_MARGIN);
            paragraphNodeView.addChild(bulletTextArea, 0);
        }

        // If more child nodes, wrap in ColView and return
        if (listItemNode.getChildNodes().size() > 1) {
            List<MarkdownNode> childNodes = listItemNode.getChildNodes().subList(1, listItemNode.getChildNodes().size());
            childNodes.forEach(this::addViewForNode);
        }
    }

    /**
     * Adds a view for given node.
     */
    protected void addViewForNode(View nodeView, MarkdownNode markdownNode)
    {
        int STANDARD_INDENT = 40;
        int indentLevel = markdownNode.getIndentLevel();
        if (indentLevel > 0)
            nodeView.setMargin(Insets.add(nodeView.getMargin(), 0, 0, 0, indentLevel * STANDARD_INDENT));
        addChild(nodeView);
    }

    /**
     * Adds a separator view.
     */
    protected void addViewForSeparatorNode()
    {
        View separatorView = createViewForSeparatorNode();
        addChild(separatorView);
    }

    /**
     * Creates view for node.
     */
    protected View createViewForNode(MarkdownNode markdownNode)
    {
        return switch (markdownNode.getNodeType()) {
            case Header -> createViewForHeaderNode(markdownNode);
            case List -> createViewForListNode(markdownNode);
            case CodeBlock -> createViewForCodeBlockNode(markdownNode);
            case RunBlock -> createViewForRunnableNode(markdownNode);
            case Directive -> createViewForDirectiveNode(markdownNode);
            case Paragraph -> createViewForParagraphNode(markdownNode);
            case Link -> createViewForLinkNode(markdownNode);
            case Image -> createViewForImageNode(markdownNode);
            case Text -> createViewForTextNode(markdownNode);
            default -> {
                System.err.println("MarkdownView.createViewForNode: No support for type: " + markdownNode.getNodeType());
                yield null;
            }
        };
    }

    /**
     * Creates a view for header node.
     */
    protected View createViewForHeaderNode(MarkdownNode headerNode)
    {
        TextArea textArea = new TextArea();
        textArea.setMargin(MarkdownUtils.getHeaderLevel(headerNode) == 1 ? HEADER1_MARGIN : HEADER2_MARGIN);

        // Reset style
        int headerLevel = MarkdownUtils.getHeaderLevel(headerNode);
        TextStyle textStyle = MarkdownUtils.getHeaderStyleForLevel(headerLevel);
        TextModel textModel = textArea.getTextModel();
        textModel.setDefaultTextStyle(textStyle);

        // Set text
        addNodeTextToTextArea(textArea, headerNode.getText());

        // Return
        return textArea;
    }

    /**
     * Creates a view for separator node.
     */
    protected View createViewForSeparatorNode()
    {
        RectView rectView = new RectView();
        rectView.setPrefHeight(1);
        rectView.setGrowWidth(true);
        rectView.setFill(SEPARATOR_COLOR);
        rectView.setMargin(SEPARATOR_MARGIN);
        return rectView;
    }

    /**
     * Creates a view for text node.
     */
    protected View createViewForTextNode(MarkdownNode contentNode)
    {
        TextArea textArea = new TextArea(true);
        textArea.setWrapLines(true);
        textArea.setMargin(GENERAL_MARGIN);

        // Reset style
        TextStyle textStyle = MarkdownUtils.getContentStyle();
        TextModel textModel = textArea.getTextModel();
        textModel.setDefaultTextStyle(textStyle);

        // Set text
        addNodeTextToTextArea(textArea, contentNode.getText());

        // Return
        return textArea;
    }

    /**
     * Creates a view for link node.
     */
    protected View createViewForLinkNode(MarkdownNode linkNode)
    {
        // Create view for link paragraph node node
        RowView linkNodeView = createViewForParagraphNode(linkNode);
        ViewList linkNodeViewChildren = linkNodeView.getChildren();

        // Add link to children
        String urlAddr = linkNode.getOtherText();
        if (urlAddr != null)
            linkNodeViewChildren.forEach(childView -> addLinkToLinkView(childView, urlAddr));

        // Return
        return linkNodeView;
    }

    /**
     * Adds a link to link view.
     */
    protected void addLinkToLinkView(View linkNodeView, String urlAddr)
    {
        // Add link handler
        linkNodeView.addEventHandler(e -> handleLinkClick(urlAddr), MouseRelease);
        linkNodeView.setCursor(Cursor.HAND);

        // Handle TextArea: Add link style
        if (linkNodeView instanceof TextArea textArea) {

            // Create link style
            TextLink textLink = new TextLink(urlAddr);
            TextStyle textStyle = MarkdownUtils.getContentStyle();
            TextStyle linkTextStyle = textStyle.copyForStyleValue(textLink);

            // Add link
            TextModel textModel = textArea.getTextModel();
            textModel.setTextStyle(linkTextStyle, 0, textModel.length());
        }
    }

    /**
     * Called when link is clicked.
     */
    protected void handleLinkClick(String urlAddr)
    {
        if (urlAddr.startsWith("http"))
            GFXEnv.getEnv().openURL(urlAddr);
    }

    /**
     * Creates a view for image node.
     */
    protected View createViewForImageNode(MarkdownNode imageNode)
    {
        String urlAddr = imageNode.getOtherText();
        WebURL url = getUrlForAddress(urlAddr);
        Image image = url != null ? Image.getImageForSource(url) : null;
        ImageView imageView = new ImageView(image);
        imageView.setKeepAspect(true);

        // Wrap in box
        BoxView boxView = new BoxView(imageView);
        boxView.setAlign(Pos.CENTER_LEFT);
        boxView.setMargin(GENERAL_MARGIN);
        boxView.setFillWidth(true);

        // Return
        return boxView;
    }

    /**
     * Returns a URL for given url address.
     */
    protected WebURL getUrlForAddress(String urlAddr)
    {
        if (getSourceUrl() != null && !urlAddr.contains(":"))
            return _sourceDirUrl.getChildUrlForPath(urlAddr);
        return WebURL.getUrl(urlAddr);
    }

    /**
     * Creates a view for list node.
     */
    protected ChildView createViewForListNode(MarkdownNode listNode)  { return null; }

    /**
     * Creates a view for code block.
     */
    protected TextArea createViewForCodeBlockNode(MarkdownNode codeNode)
    {
        TextArea textView = new TextView();
        textView.setMargin(GENERAL_MARGIN);
        textView.setPadding(GENERAL_PADDING);
        textView.setBorder(BLOCK_BORDER);
        textView.setBorderRadius(8);
        textView.setFill(BLOCK_COLOR);
        textView.setEditable(true);
        textView.setFocusPainted(true);

        // Reset style
        TextStyle textStyle = MarkdownUtils.getCodeStyle();
        TextModel textModel = textView.getTextModel();
        textModel.setDefaultTextStyle(textStyle);

        // Set text
        textModel.addChars(codeNode.getText());

        // Add listener to select code block
        textView.addEventFilter(e -> _selCodeBlockNode = codeNode, MousePress);

        // Return
        return textView;
    }

    /**
     * Creates a view for Runnable block.
     */
    protected View createViewForRunnableNode(MarkdownNode codeNode)
    {
        // Wrap in box and return
        BoxView codeBlockBox = new BoxView();
        codeBlockBox.setName("Runnable");
        codeBlockBox.setMargin(GENERAL_MARGIN);
        codeBlockBox.setPadding(GENERAL_PADDING);
        codeBlockBox.setBorder(BLOCK_BORDER);
        codeBlockBox.setBorderRadius(8);
        codeBlockBox.setFill(BLOCK_COLOR);
        codeBlockBox.setAlign(Pos.CENTER_LEFT);

        // Apply style string
        String styleString = codeNode.getOtherText();
        if (styleString != null)
            codeBlockBox.setPropsString(styleString);

        // Return
        return codeBlockBox;
    }

    /**
     * Creates a view for directive.
     */
    protected View createViewForDirectiveNode(MarkdownNode directiveNode)
    {
        String directiveString = directiveNode.getText();
        String[] dirParts = directiveString.split("=");
        if (dirParts.length == 2)
            setDirectiveValue(dirParts[0], dirParts[1]);
        return null;
    }

    /**
     * Creates a view for paragraph node (children are inline nodes: Text, Link, Image or CodeSpan).
     */
    protected RowView createViewForParagraphNode(MarkdownNode paragraphNode)
    {
        // Create row view for paragraph node
        RowView paragraphNodeView = new RowView();
        paragraphNodeView.setMargin(GENERAL_MARGIN);
        paragraphNodeView.setSpacing(4);

        // Get child inline nodes
        List<MarkdownNode> inlineNodes = paragraphNode.getChildNodes();
        TextArea lastTextArea = null;

        // Iterate over children
        for (MarkdownNode childNode : inlineNodes) {

            // If last node is Text or Link and last view is TextArea, just add chars
            MarkdownNode.NodeType nodeType = childNode.getNodeType();
            if (lastTextArea != null && (nodeType == MarkdownNode.NodeType.Text || nodeType == MarkdownNode.NodeType.Link))
                addInlineNodeToTextArea(childNode, lastTextArea);

            // Otherwise create view and add
            else {
                View childNodeView = createViewForParagraphChildBlockNode(childNode);
                childNodeView.setMargin(NO_MARGIN);
                paragraphNodeView.addChild(childNodeView);
                if (childNodeView instanceof TextArea && nodeType != MarkdownNode.NodeType.CodeBlock)
                    lastTextArea = (TextArea) childNodeView;
                else lastTextArea = null;
            }
        }

        // Return
        return paragraphNodeView;
    }

    /**
     * Creates a view for paragraph child that is really block node.
     */
    private View createViewForParagraphChildBlockNode(MarkdownNode childNode)
    {
        MarkdownNode.NodeType nodeType = childNode.getNodeType();

        // Handle CodeBlock special
        if (nodeType == MarkdownNode.NodeType.CodeBlock) {
            View childNodeView = createViewForCodeBlockNode(childNode);
            childNodeView.setMargin(NO_MARGIN);
            childNodeView.setPadding(INLINE_PADDING);
            return childNodeView;
        }

        // Handle anything else
        View childNodeView = createViewForNode(childNode); //assert (childNodeView != null);
        childNodeView.setMargin(NO_MARGIN);
        return childNodeView;
    }

    /**
     * Override to return Column layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new ColViewLayout(this); }

    /**
     * Adds an inline node (Text, Link) to a given text area.
     */
    private void addInlineNodeToTextArea(MarkdownNode inlineNode, TextArea textArea)
    {
        // If text already present, add space
        if (textArea.length() > 0)
            textArea.addChars(" ");

        // Handle link node
        if (inlineNode.getNodeType() == MarkdownNode.NodeType.Link) {

            // Create link style
            String urlAddr = inlineNode.getOtherText();
            TextLink textLink = new TextLink(urlAddr);
            TextStyle textStyle = textArea.getDefaultTextStyle();
            TextStyle linkTextStyle = textStyle.copyForStyleValue(textLink);

            // Iterate over child nodes and add text to text area
            List<MarkdownNode> childNodes = inlineNode.getChildNodes();
            for (MarkdownNode childNode : childNodes) {
                if (childNode.getNodeType() == MarkdownNode.NodeType.Text)
                    textArea.addCharsWithStyle(childNode.getText(), linkTextStyle);
                else System.out.println("MarkdownView: Unsupported link content type: " + childNode.getNodeType());
            }

            // Enable events
            textArea.setEditable(true);
            textArea.setFocusable(false);
            textArea.getTextAdapter().setLinkHandler((e,url) -> handleLinkClick(url));
        }

        // Otherwise, add chars
        else {
            String nodeText = inlineNode.getText();
            if (nodeText != null)
                addNodeTextToTextArea(textArea, nodeText);
        }
    }

    /**
     * Adds node text (markdown formatted string) to a text area.
     */
    private static void addNodeTextToTextArea(TextArea textArea, String nodeText)
    {
        String fontStyle = nodeText.contains("*") ? "BoldItalic" : null;
        addNodeTextToTextAreaImpl(textArea, nodeText, fontStyle);
    }

    /**
     * Adds text to a text area.
     */
    private static void addNodeTextToTextAreaImpl(TextArea textArea, String nodeText, String fontStyle)
    {
        // If no fontStyle, just add chars with default font
        if (fontStyle == null) {
            TextModel textModel = textArea.getTextModel();
            TextStyle textStyle = textArea.getDefaultTextStyle();
            textModel.addCharsWithStyle(nodeText, textStyle);
            return;
        }

        // Get strings separated by '***'
        String regex = getRegexForFontStyle(fontStyle);
        String[] splitStrings = nodeText.split(regex);
        String nextFontStyle = getNextFontStyleForFontStyle(fontStyle);

        // Iterate over separated strings
        for (int i = 0; i < splitStrings.length; i += 2) {
            addNodeTextToTextAreaImpl(textArea, splitStrings[i], nextFontStyle);
            if (i + 1 < splitStrings.length)
                addTextAreaTextForFontStyle(textArea, splitStrings[i + 1], fontStyle);
        }
    }

    // Adds given string to given text area with "Bold", "Italic" or "BoldItalic" default font.
    private static void addTextAreaTextForFontStyle(TextArea textArea, String theChars, String fontStyle)
    {
        // Get font for given fontStyle
        TextModel textModel = textArea.getTextModel();
        Font defaultFont = textModel.getDefaultFont();
        defaultFont = switch (fontStyle) {
            case "Bold" -> defaultFont.getBold();
            case "Italic" -> defaultFont.getItalic();
            case "BoldItalic" -> defaultFont.getBold().getItalic();
            default -> defaultFont;
        };

        // Get text style for font and add chars
        TextStyle textStyle = textArea.getDefaultTextStyle().copyForStyleValue(defaultFont);
        textModel.addCharsWithStyle(theChars, textStyle);
    }

    // Return regex for FontStyle
    private static String getRegexForFontStyle(String fontStyle)
    {
        return switch (fontStyle) {
            case "BoldItalic" -> "(?<!\\\\)\\*\\*\\*";
            case "Bold" -> "(?<!\\\\)\\*\\*";
            case "Italic" -> "(?<!\\\\)\\*";
            default -> throw new RuntimeException("Unknown font style: " + fontStyle);
        };
    }

    // Return next FontStyle
    private static String getNextFontStyleForFontStyle(String fontStyle)
    {
        return switch (fontStyle) {
            case "Bold" -> "Italic";
            case "BoldItalic" -> "Bold";
            default -> null;
        };
    }
}
