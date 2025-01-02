package snap.viewx;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.text.TextBlock;
import snap.text.TextLink;
import snap.text.TextStyle;
import snap.util.ArrayUtils;
import snap.util.MDNode;
import snap.util.MDParser;
import snap.util.MDUtils;
import snap.view.*;
import snap.web.WebURL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This view class renders mark down.
 */
public class MarkDownView extends ChildView {

    // The source url (and it's parent)
    private WebURL _sourceUrl, _sourceDirUrl;

    // The root markdown node
    private MDNode _rootMarkdownNode;

    // The selected code block node
    private MDNode _selCodeBlockNode;

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
    private static final Color BLOCK_COLOR = new Color(.96, .97, .98);
    private static final Color BLOCK_BORDER_COLOR = BLOCK_COLOR.blend(Color.BLACK, .15);
    private static final Border BLOCK_BORDER = Border.createLineBorder(BLOCK_BORDER_COLOR, 1);
    private static final Color SEPARATOR_COLOR = BLOCK_COLOR.blend(Color.BLACK, .1);

    /**
     * Constructor.
     */
    public MarkDownView()
    {
        super();
        setPadding(DOC_PADDING);
        setFill(Color.WHITE);
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
     * Sets MarkDown.
     */
    public void setMarkDown(String markDown)
    {
        _rootMarkdownNode = new MDParser().parseMarkdownChars(markDown);
        MDNode[] rootNodes = _rootMarkdownNode.getChildNodes();

        for (MDNode node : rootNodes)
            addViewForNode(node);
    }

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
    public MDNode getSelCodeBlockNode()
    {
        if (_selCodeBlockNode != null)
            return _selCodeBlockNode;

        MDNode[] rootNodes = _rootMarkdownNode.getChildNodes();
        return ArrayUtils.findMatch(rootNodes, node -> node.getNodeType() == MDNode.NodeType.CodeBlock);
    }

    /**
     * Adds a view for given node.
     */
    protected void addViewForNode(MDNode markNode)
    {
        View nodeView = createViewForNode(markNode);
        if (nodeView != null)
            addChild(nodeView);

        if (markNode.getNodeType() == MDNode.NodeType.Header1 || markNode.getNodeType() == MDNode.NodeType.Header2)
            addViewForSeparatorNode();
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
    protected View createViewForNode(MDNode markNode)
    {
        switch (markNode.getNodeType()) {
            case Header1: case Header2: return createViewForHeaderNode(markNode);
            case Text: return createViewForTextNode(markNode);
            case Link: return createViewForLinkNode(markNode);
            case Image: return createViewForImageNode(markNode);
            case CodeBlock: return createViewForCodeBlockNode(markNode);
            case Runnable: return createViewForRunnableNode(markNode);
            case List: return createViewForListNode(markNode);
            case Mixed: return createViewForMixedNode(markNode);
            case Directive: return createViewForDirectiveNode(markNode);
            default:
                System.err.println("MarkDownView.createViewForNode: No support for type: " + markNode.getNodeType());
                return null;
        }
    }

    /**
     * Creates a view for header node.
     */
    protected View createViewForHeaderNode(MDNode headerNode)
    {
        TextArea textArea = new TextArea();
        textArea.setMargin(HEADER1_MARGIN);
        if (headerNode.getNodeType() == MDNode.NodeType.Header2)
            textArea.setMargin(HEADER2_MARGIN);

        // Reset style
        TextStyle textStyle = headerNode.getNodeType() == MDNode.NodeType.Header1 ? MDUtils.getHeader1Style() : MDUtils.getHeader2Style();
        TextBlock textBlock = textArea.getTextBlock();
        textBlock.setDefaultTextStyle(textStyle);

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
    protected View createViewForTextNode(MDNode contentNode)
    {
        TextArea textArea = new TextArea(true);
        textArea.setWrapLines(true);
        textArea.setMargin(GENERAL_MARGIN);

        // Reset style
        TextStyle textStyle = MDUtils.getContentStyle();
        TextBlock textBlock = textArea.getTextBlock();
        textBlock.setDefaultTextStyle(textStyle);

        // Set text
        addNodeTextToTextArea(textArea, contentNode.getText());

        // Return
        return textArea;
    }

    /**
     * Creates a view for link node.
     */
    protected View createViewForLinkNode(MDNode linkNode)
    {
        // Create view for mixed node
        RowView linkedNodeView = createViewForMixedNode(linkNode);
        View[] linkNodeViewChildren = linkedNodeView.getChildren();

        // Add link to children
        String urlAddr = linkNode.getOtherText();
        if (urlAddr != null)
            Stream.of(linkNodeViewChildren).forEach(childView -> addLinkToLinkView(childView, urlAddr));

        // Return
        return linkedNodeView;
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
        if (linkNodeView instanceof TextArea) {

            // Create link style
            TextLink textLink = new TextLink(urlAddr);
            TextStyle textStyle = MDUtils.getContentStyle();
            TextStyle linkTextStyle = textStyle.copyForStyleValue(textLink);

            // Add link
            TextArea textArea = (TextArea) linkNodeView;
            TextBlock textBlock = textArea.getTextBlock();
            textBlock.setTextStyle(linkTextStyle, 0, textBlock.length());
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
    protected View createViewForImageNode(MDNode imageNode)
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
            return _sourceDirUrl.getChild(urlAddr);
        return WebURL.getURL(urlAddr);
    }

    /**
     * Creates a view for list node.
     */
    protected ChildView createViewForListNode(MDNode listNode)
    {
        // Create list view
        ColView listNodeView = new ColView();
        listNodeView.setMargin(GENERAL_MARGIN);

        // Get list item views and add to listNodeView
        MDNode[] listItemNodes = listNode.getChildNodes();
        View[] listItemViews = ArrayUtils.map(listItemNodes, node -> createViewForListItemNode(node), View.class);
        Stream.of(listItemViews).forEach(listNodeView::addChild);

        // Return
        return listNodeView;
    }

    /**
     * Creates a view for list item node.
     */
    protected ChildView createViewForListItemNode(MDNode listItemNode)
    {
        // Create view for mixed node
        RowView mixedNodeView = createViewForMixedNode(listItemNode);

        // If first child is TextArea, add bullet
        if (mixedNodeView.getChild(0) instanceof TextArea) {
            TextArea textArea = (TextArea) mixedNodeView.getChild(0);
            textArea.getTextBlock().addChars("• ", 0);
        }

        // Otherwise create text area and insert
        else {
            View bulletTextArea = createViewForTextNode(new MDNode(MDNode.NodeType.Text, "• "));
            bulletTextArea.setMargin(NO_MARGIN);
            mixedNodeView.addChild(bulletTextArea, 0);
        }

        // Return
        return mixedNodeView;
    }

    /**
     * Creates a view for code block.
     */
    protected TextArea createViewForCodeBlockNode(MDNode codeNode)
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
        TextStyle textStyle = MDUtils.getCodeStyle();
        TextBlock textBlock = textView.getTextBlock();
        textBlock.setDefaultTextStyle(textStyle);

        // Set text
        textBlock.addChars(codeNode.getText());

        // Add listener to select code block
        textView.addEventFilter(e -> _selCodeBlockNode = codeNode, MousePress);

        // Return
        return textView;
    }

    /**
     * Creates a view for Runnable block.
     */
    protected View createViewForRunnableNode(MDNode codeNode)
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
    protected View createViewForDirectiveNode(MDNode directiveNode)
    {
        String directiveString = directiveNode.getText();
        String[] dirParts = directiveString.split("=");
        if (dirParts.length == 2)
            setDirectiveValue(dirParts[0], dirParts[1]);
        return null;
    }

    /**
     * Creates a view for mixed node (children are Text, Link, Image or CodeBlock).
     */
    protected RowView createViewForMixedNode(MDNode mixedNode)
    {
        // Create row view for mixed node
        RowView mixedNodeView = new RowView();
        mixedNodeView.setMargin(GENERAL_MARGIN);
        mixedNodeView.setSpacing(4);

        // Get children
        MDNode[] childNodes = mixedNode.getChildNodes();
        TextArea lastTextArea = null;

        // Iterate over children
        for (MDNode childNode : childNodes) {

            // If last node is Text or Link and last view is TextArea, just add chars
            MDNode.NodeType nodeType = childNode.getNodeType();
            if (lastTextArea != null && (nodeType == MDNode.NodeType.Text || nodeType == MDNode.NodeType.Link))
                addTextOrLinkNodeToTextArea(lastTextArea, childNode);

            // Otherwise create view and add
            else {
                View childNodeView = createViewForMixedNodeChildNode(childNode);
                childNodeView.setMargin(NO_MARGIN);
                mixedNodeView.addChild(childNodeView);
                if (childNodeView instanceof TextArea && nodeType != MDNode.NodeType.CodeBlock)
                    lastTextArea = (TextArea) childNodeView;
                else lastTextArea = null;
            }
        }

        // Return
        return mixedNodeView;
    }

    /**
     * Creates a view for mixed node (children are Text, Link, Image or CodeBlock).
     */
    private View createViewForMixedNodeChildNode(MDNode childNode)
    {
        MDNode.NodeType nodeType = childNode.getNodeType();

        // Handle CodeBlock special
        if (nodeType == MDNode.NodeType.CodeBlock) {
            View childNodeView = createViewForCodeBlockNode(childNode);
            childNodeView.setMargin(NO_MARGIN);
            childNodeView.setPadding(INLINE_PADDING);
            return childNodeView;
        }

        // Handle anything else
        View childNodeView = createViewForNode(childNode); assert (childNodeView != null);
        childNodeView.setMargin(NO_MARGIN);
        return childNodeView;
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return ColView.getPrefWidth(this, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return ColView.getPrefHeight(this, aW, false); }

    /**
     * Override to layout children with ColView layout.
     */
    protected void layoutImpl()  { ColView.layout(this, false); }

    /**
     * Adds a text or link node content to a given text area.
     */
    private void addTextOrLinkNodeToTextArea(TextArea textArea, MDNode aNode)
    {
        // If text already present, add space
        if (textArea.length() > 0)
            textArea.addChars(" ");

        // Handle link node
        if (aNode.getNodeType() == MDNode.NodeType.Link) {

            // Create link style
            String urlAddr = aNode.getOtherText();
            TextLink textLink = new TextLink(urlAddr);
            TextStyle textStyle = textArea.getDefaultTextStyle();
            TextStyle linkTextStyle = textStyle.copyForStyleValue(textLink);

            // Iterate over child nodes and add text to text area
            MDNode[] childNodes = aNode.getChildNodes();
            for (MDNode childNode : childNodes) {
                if (childNode.getNodeType() == MDNode.NodeType.Text)
                    textArea.addCharsWithStyle(childNode.getText(), linkTextStyle);
                else System.out.println("MarkDownView: Unsupported link content type: " + childNode.getNodeType());
            }

            // Enable events
            textArea.setEditable(true);
            textArea.setFocusable(false);
            textArea.getTextAdapter().setLinkHandler((e,url) -> handleLinkClick(url));
        }

        // Otherwise, add chars
        else {
            String nodeText = aNode.getText();
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
            TextBlock textBlock = textArea.getTextBlock();
            TextStyle textStyle = textArea.getDefaultTextStyle();
            textBlock.addCharsWithStyle(nodeText, textStyle);
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
        TextBlock textBlock = textArea.getTextBlock();
        Font defaultFont = textBlock.getDefaultFont();
        switch (fontStyle) {
            case "Bold": defaultFont = defaultFont.getBold(); break;
            case "Italic": defaultFont = defaultFont.getItalic(); break;
            case "BoldItalic": defaultFont = defaultFont.getBold().getItalic(); break;
        }

        // Get text style for font and add chars
        TextStyle textStyle = textArea.getDefaultTextStyle().copyForStyleValue(defaultFont);
        textBlock.addCharsWithStyle(theChars, textStyle);
    }

    // Return regex for FontStyle
    private static String getRegexForFontStyle(String fontStyle)
    {
        switch (fontStyle) {
            case "BoldItalic": return "(?<!\\\\)\\*\\*\\*";
            case "Bold": return "(?<!\\\\)\\*\\*";
            case "Italic": return "(?<!\\\\)\\*";
            default: throw new RuntimeException("Unknown font style: " + fontStyle);
        }
    }

    // Return next FontStyle
    private static String getNextFontStyleForFontStyle(String fontStyle)
    {
        switch (fontStyle) {
            case "Bold": return "Italic";
            case "BoldItalic": return "Bold";
            default: return null;
        }
    }
}
