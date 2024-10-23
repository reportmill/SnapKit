package snap.viewx;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Image;
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

    // The root markdown node
    private MDNode _rootMarkdownNode;

    // The selected code block node
    private MDNode _selCodeBlockNode;

    // Map of directive values
    private Map<String,String> _directives = new HashMap<>();

    /**
     * Constructor.
     */
    public MarkDownView()
    {
        super();
        setPadding(10, 20, 20, 20);
        setFill(Color.WHITE);
    }

    /**
     * Sets MarkDown.
     */
    public void setMarkDown(String markDown)
    {
        _rootMarkdownNode = new MDParser().parseMarkdownChars(markDown);
        MDNode[] rootNodes = _rootMarkdownNode.getChildNodes();

        for (MDNode mdnode : rootNodes) {
            View nodeView = createViewForNode(mdnode);
            if (nodeView != null)
                addChild(nodeView);
        }
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
        textArea.setMargin(16, 8, 16, 8);
        if (headerNode.getNodeType() == MDNode.NodeType.Header2)
            textArea.setMargin(8, 8, 8, 8);

        // Reset style
        TextStyle textStyle = headerNode.getNodeType() == MDNode.NodeType.Header1 ? MDUtils.getHeader1Style() : MDUtils.getHeader2Style();
        TextBlock textBlock = textArea.getTextBlock();
        textBlock.setDefaultTextStyle(textStyle);

        // Set text
        textBlock.addChars(headerNode.getText());

        // Return
        return textArea;
    }

    /**
     * Creates a view for text node.
     */
    protected View createViewForTextNode(MDNode contentNode)
    {
        TextArea textArea = new TextArea();
        textArea.setWrapLines(true);
        textArea.setMargin(8, 8, 8, 8);

        // Reset style
        TextStyle textStyle = MDUtils.getContentStyle();
        TextBlock textBlock = textArea.getTextBlock();
        textBlock.setDefaultTextStyle(textStyle);

        // Set text
        textBlock.addChars(contentNode.getText());

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
    protected void handleLinkClick(String urlAddr)  { }

    /**
     * Creates a view for image node.
     */
    protected View createViewForImageNode(MDNode imageNode)
    {
        String urlAddr = imageNode.getOtherText();
        WebURL url = WebURL.getURL(urlAddr);
        Image image = url != null ? Image.getImageForSource(url) : null;
        ImageView imageView = new ImageView(image);

        // Wrap in box
        BoxView boxView = new BoxView(imageView);
        boxView.setAlign(Pos.CENTER_LEFT);
        boxView.setMargin(8, 8, 8, 8);

        // Return
        return boxView;
    }

    /**
     * Creates a view for list node.
     */
    protected ChildView createViewForListNode(MDNode listNode)
    {
        // Create list view
        ColView listNodeView = new ColView();
        listNodeView.setMargin(8, 8, 8, 8);

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
            bulletTextArea.setMargin(0, 0, 0, 0);
            mixedNodeView.addChild(bulletTextArea, 0);
        }

        // Return
        return mixedNodeView;
    }

    /**
     * Creates a view for code block.
     */
    protected View createViewForCodeBlockNode(MDNode codeNode)
    {
        TextArea textArea = createTextAreaViewForCodeBlockNode(codeNode);

        // Wrap in box and return
        BoxView codeBlockBox = new BoxView(textArea);
        codeBlockBox.setAlign(Pos.CENTER_LEFT);
        return codeBlockBox;
    }

    /**
     * Creates a view for code block.
     */
    protected TextArea createTextAreaViewForCodeBlockNode(MDNode codeNode)
    {
        TextArea textArea = new TextView();
        textArea.setMargin(8, 8, 8, 8);
        textArea.setPadding(16, 16, 16, 16);
        textArea.setBorderRadius(8);
        textArea.setFill(new Color(.96, .97, .98));
        textArea.setEditable(true);
        textArea.setFocusPainted(true);

        // Reset style
        TextStyle textStyle = MDUtils.getCodeStyle();
        TextBlock textBlock = textArea.getTextBlock();
        textBlock.setDefaultTextStyle(textStyle);

        // Set text
        textBlock.addChars(codeNode.getText());

        // Add listener to select code block
        textArea.addEventFilter(e -> _selCodeBlockNode = codeNode, MousePress);

        // Return
        return textArea;
    }

    /**
     * Creates a view for Runnable block.
     */
    protected View createViewForRunnableNode(MDNode codeNode)
    {
        // Wrap in box and return
        BoxView codeBlockBox = new BoxView();
        codeBlockBox.setName("Runnable");
        codeBlockBox.setMargin(16, 16, 16, 16);
        codeBlockBox.setPadding(16, 16, 16, 16);
        codeBlockBox.setBorderRadius(8);
        codeBlockBox.setFill(new Color(.96, .97, .98));
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
        mixedNodeView.setMargin(8, 8, 8, 8);
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
                childNodeView.setMargin(0, 0, 0, 0);
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
            View childNodeView = createTextAreaViewForCodeBlockNode(childNode);
            childNodeView.setMargin(0, 0, 0, 0);
            childNodeView.setPadding(8, 8, 8, 8);
            return childNodeView;
        }

        // Handle anything else
        View childNodeView = createViewForNode(childNode); assert (childNodeView != null);
        childNodeView.setMargin(0, 0, 0, 0);
        return childNodeView;
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return ColView.getPrefWidth(this, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return ColView.getPrefHeight(this, aW);
    }

    /**
     * Override to layout children with ColView layout.
     */
    protected void layoutImpl()
    {
        ColView.layout(this, true);
    }

    /**
     * Adds a text or link node content to a given text area.
     */
    private void addTextOrLinkNodeToTextArea(TextArea textArea, MDNode aNode)
    {
        // Handle link node
        if (aNode.getNodeType() == MDNode.NodeType.Link) {

            // Create link style
            String urlAddr = aNode.getOtherText();
            TextLink textLink = new TextLink(urlAddr);
            TextBlock textBlock = textArea.getTextBlock();
            TextStyle textStyle = textBlock.getDefaultTextStyle();
            TextStyle linkTextStyle = textStyle.copyForStyleValue(textLink);

            // If text already present, add space
            if (textBlock.length() > 0)
                textBlock.addChars(" ");

            // Set text
            textBlock.addCharsWithStyle(aNode.getText(), linkTextStyle);
        }

        // Otherwise, add chars
        else {
            String nodeText = aNode.getText();
            if (nodeText != null)
                textArea.getTextBlock().addChars(nodeText);
        }
    }
}
