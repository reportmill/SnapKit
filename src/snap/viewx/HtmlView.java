package snap.viewx;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.*;
import snap.text.TextModel;
import snap.text.TextLink;
import snap.text.TextStyle;
import snap.util.*;
import snap.view.*;
import snap.web.WebFile;
import snap.web.WebURL;

/**
 * This view class renders HTML by taking an XMLElement graph and creating views similar to how
 * MarkdownView renders MarkdownNode objects.
 */
public class HtmlView extends ChildView {

    // The source url (and its parent dir url)
    private WebURL _sourceUrl, _sourceDirUrl;

    // The document element
    private XMLElement _documentElement;

    // Constants
    private static final Insets DOC_PADDING = new Insets(10, 20, 20, 20);
    private static final Insets HEADER1_MARGIN = new Insets(24, 8, 8, 8);
    private static final Insets HEADER2_MARGIN = new Insets(16, 8, 8, 8);
    private static final Insets SEPARATOR_MARGIN = new Insets(8, 8, 8, 8);
    private static final Insets GENERAL_MARGIN = new Insets(8, 8, 8, 8);
    private static final Insets GENERAL_PADDING = new Insets(16, 16, 16, 16);
    private static final Insets NO_MARGIN = Insets.EMPTY;
    private static Color BLOCK_COLOR = new Color(.96, .97, .98);
    private static Color BLOCK_BORDER_COLOR = BLOCK_COLOR.blend(Color.BLACK, .15);
    private static Border BLOCK_BORDER = Border.createLineBorder(BLOCK_BORDER_COLOR, 1);
    private static Color SEPARATOR_COLOR = BLOCK_COLOR.blend(Color.BLACK, .1);

    /**
     * Constructor.
     */
    public HtmlView()
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
     * Sets HTML from a string (parses it with mixed-content support).
     */
    public void setHtml(String htmlStr)
    {
        XMLParser parser = new XMLParser();
        parser.allowMixedContent();
        _documentElement = parser.parseXMLFromString(htmlStr);
        buildViewsFromDocument();
    }

    /**
     * Sets the document XMLElement directly (already parsed).
     */
    public void setDocumentElement(XMLElement documentElement)
    {
        _documentElement = documentElement;
        buildViewsFromDocument();
    }

    /**
     * Returns the document element.
     */
    public XMLElement getDocumentElement()  { return _documentElement; }

    /**
     * Builds child views from the document element.
     */
    protected void buildViewsFromDocument()
    {
        if (_documentElement == null) return;

        // Navigate to body if this is a full html document
        XMLElement contentElement = _documentElement;
        String docName = _documentElement.getName().toLowerCase();
        if (docName.equals("html")) {
            XMLElement body = _documentElement.getElement("body");
            if (body != null)
                contentElement = body;
        }

        // Add views for each child element
        for (int i = 0; i < contentElement.getElementCount(); i++)
            addViewForElement(contentElement.getElement(i));
    }

    /**
     * Creates and adds a view for the given element.
     */
    protected void addViewForElement(XMLElement element)
    {
        View view = createViewForElement(element);
        if (view != null) {
            setHtmlElementForView(view, element);
            addChild(view);
        }
    }

    /**
     * Creates a view for the given element by dispatching on tag name.
     */
    protected View createViewForElement(XMLElement element)
    {
        String tag = element.getName().toLowerCase();
        return switch (tag) {
            case "h1" -> createViewForHeaderElement(element, 1);
            case "h2" -> createViewForHeaderElement(element, 2);
            case "h3" -> createViewForHeaderElement(element, 3);
            case "h4" -> createViewForHeaderElement(element, 4);
            case "h5" -> createViewForHeaderElement(element, 5);
            case "h6" -> createViewForHeaderElement(element, 6);
            case "p" -> createViewForParagraphElement(element);
            case "ul" -> createViewForListElement(element, false);
            case "ol" -> createViewForListElement(element, true);
            case "pre" -> createViewForPreElement(element);
            case "hr" -> createViewForSeparatorElement();
            case "blockquote" -> createViewForBlockquoteElement(element);
            case "img" -> createViewForImageElement(element);
            case "div", "section", "article", "main", "header", "footer", "nav" ->
                createViewForContainerElement(element);
            default -> {
                System.err.println("HtmlView.createViewForElement: Unsupported block tag: " + tag);
                yield null;
            }
        };
    }

    /**
     * Creates a view for a heading element (h1-h6).
     */
    protected View createViewForHeaderElement(XMLElement headerElement, int level)
    {
        TextArea textArea = new TextArea();
        textArea.setMargin(level <= 2 ? HEADER1_MARGIN : HEADER2_MARGIN);

        // Map level to MarkdownUtils header styles (1=big, 2+=smaller)
        int styleLevel = level <= 1 ? 1 : 2;
        TextStyle textStyle = MarkdownUtils.getHeaderStyleForLevel(styleLevel);
        TextModel textModel = textArea.getTextModel();
        textModel.setDefaultTextStyle(textStyle);

        // Add inline content
        addInlineContentToTextArea(headerElement, textArea);
        return textArea;
    }

    /**
     * Creates a view for a paragraph element. If the paragraph contains an img child, returns a RowView
     * that interleaves TextArea runs and ImageViews; otherwise returns a plain TextArea.
     */
    protected View createViewForParagraphElement(XMLElement paragraphElement)
    {
        // Simple case: no img children - use a plain TextArea
        if (paragraphElement.getElement("img") == null) {
            TextArea textArea = createBlockTextArea();
            addInlineContentToTextArea(paragraphElement, textArea);
            return textArea;
        }

        // Mixed case: use a RowView to interleave text runs and images
        RowView rowView = new RowView();
        rowView.setMargin(GENERAL_MARGIN);
        rowView.setSpacing(4);
        rowView.setAlign(Pos.CENTER_LEFT);

        // Add leading text (before first child element) to initial TextArea
        TextArea textArea = null;
        String leadText = paragraphElement.getValue();
        if (leadText != null && !leadText.isEmpty()) {
            textArea = createBlockTextArea();
            textArea.setMargin(NO_MARGIN);
            textArea.getTextModel().addChars(leadText);
            rowView.addChild(textArea);
        }

        // Walk child elements, splitting on img
        for (int i = 0; i < paragraphElement.getElementCount(); i++) {
            XMLElement child = paragraphElement.getElement(i);

            if (child.getName().equalsIgnoreCase("img")) {
                rowView.addChild(createViewForImageElement(child));
                textArea = null;
            }
            else {
                if (textArea == null) {
                    textArea = createBlockTextArea();
                    textArea.setMargin(NO_MARGIN);
                    rowView.addChild(textArea);
                }
                addInlineElementToTextArea(child, textArea);
            }

            // Tail text (text after this child, before the next sibling)
            String tailText = child.getTailContent();
            if (tailText != null && !tailText.isEmpty()) {
                if (textArea == null) {
                    textArea = createBlockTextArea();
                    textArea.setMargin(NO_MARGIN);
                    rowView.addChild(textArea);
                }
                textArea.getTextModel().addChars(tailText);
            }
        }

        return rowView;
    }

    /**
     * Creates a view for a list element (ul/ol).
     */
    protected View createViewForListElement(XMLElement listElement, boolean isOrdered)
    {
        ColView listView = new ColView();
        listView.setMargin(GENERAL_MARGIN);
        int itemIndex = 1;

        for (int i = 0; i < listElement.getElementCount(); i++) {
            XMLElement child = listElement.getElement(i);
            if (child.getName().equalsIgnoreCase("li")) {
                View itemView = createViewForListItemElement(child, isOrdered ? itemIndex++ : 0);
                if (itemView != null) {
                    setHtmlElementForView(itemView, child);
                    listView.addChild(itemView);
                }
            }
        }

        return listView;
    }

    /**
     * Creates a view for a list item element. Pass itemIndex=0 for unordered (bullet), >0 for ordered.
     */
    protected View createViewForListItemElement(XMLElement listItemElement, int itemIndex)
    {
        RowView rowView = new RowView();
        rowView.setMargin(new Insets(2, 8, 2, 16));
        rowView.setSpacing(6);
        rowView.setAlign(Pos.TOP_LEFT);

        // Add bullet or number marker
        String markerText = itemIndex > 0 ? itemIndex + "." : "•";
        TextArea markerArea = new TextArea();
        TextStyle contentStyle = MarkdownUtils.getContentStyle();
        markerArea.getTextModel().setDefaultTextStyle(contentStyle);
        markerArea.getTextModel().addChars(markerText);
        markerArea.setPrefWidth(20);
        rowView.addChild(markerArea);

        // Add item content text area
        TextArea contentArea = createBlockTextArea();
        contentArea.setMargin(NO_MARGIN);
        contentArea.setGrowWidth(true);
        addInlineContentToTextArea(listItemElement, contentArea);
        rowView.addChild(contentArea);

        return rowView;
    }

    /**
     * Creates a view for a pre element (preformatted/code block).
     */
    protected View createViewForPreElement(XMLElement preElement)
    {
        // Get code text - may be directly in pre or inside a nested <code> child
        String codeText = preElement.getValue();
        if (codeText == null) {
            XMLElement codeElement = preElement.getElement("code");
            if (codeElement != null)
                codeText = codeElement.getValue();
        }
        if (codeText == null) codeText = "";

        TextArea textView = new TextView();
        textView.setMargin(GENERAL_MARGIN);
        textView.setPadding(GENERAL_PADDING);
        textView.setBorder(BLOCK_BORDER);
        textView.setBorderRadius(8);
        textView.setFill(BLOCK_COLOR);

        TextStyle codeStyle = MarkdownUtils.getCodeStyle();
        TextModel textModel = textView.getTextModel();
        textModel.setDefaultTextStyle(codeStyle);
        textModel.addChars(codeText);

        return textView;
    }

    /**
     * Creates a view for a horizontal rule element.
     */
    protected View createViewForSeparatorElement()
    {
        RectView rectView = new RectView();
        rectView.setPrefHeight(1);
        rectView.setGrowWidth(true);
        rectView.setFill(SEPARATOR_COLOR);
        rectView.setMargin(SEPARATOR_MARGIN);
        return rectView;
    }

    /**
     * Creates a view for a blockquote element.
     */
    protected View createViewForBlockquoteElement(XMLElement blockquoteElement)
    {
        TextArea textArea = createBlockTextArea();
        textArea.setPadding(new Insets(8, 16, 8, 16));
        textArea.setBorder(BLOCK_BORDER);
        textArea.setBorderRadius(4);
        textArea.setFill(BLOCK_COLOR);
        addInlineContentToTextArea(blockquoteElement, textArea);
        return textArea;
    }

    /**
     * Creates a view for an image element.
     */
    protected View createViewForImageElement(XMLElement imgElement)
    {
        String urlAddr = imgElement.getAttributeValue("src", "");
        WebURL url = getUrlForAddress(urlAddr);
        Image image = url != null ? Image.getImageForSource(url) : null;
        ImageView imageView = new ImageView(image);
        imageView.setKeepAspect(true);

        BoxView boxView = new BoxView(imageView);
        boxView.setAlign(Pos.CENTER_LEFT);
        boxView.setMargin(GENERAL_MARGIN);
        boxView.setFillWidth(true);
        return boxView;
    }

    /**
     * Creates a view for a generic container element (div, section, article, etc).
     * Iterates children and adds sub-views into a ColView.
     */
    protected View createViewForContainerElement(XMLElement containerElement)
    {
        ColView colView = new ColView();
        colView.setMargin(GENERAL_MARGIN);

        for (int i = 0; i < containerElement.getElementCount(); i++) {
            View childView = createViewForElement(containerElement.getElement(i));
            if (childView != null)
                colView.addChild(childView);
        }

        return colView;
    }

    /**
     * Returns a URL for the given address string, resolving relative paths against the source URL.
     */
    protected WebURL getUrlForAddress(String urlAddr)
    {
        if (_sourceUrl != null && !urlAddr.contains(":"))
            return _sourceDirUrl.getChildUrlForPath(urlAddr);
        return WebURL.getUrl(urlAddr);
    }

    /**
     * Creates a standard block-level TextArea with default content style and word wrap.
     */
    protected TextArea createBlockTextArea()
    {
        TextArea textArea = new TextArea(true);
        textArea.setWrapLines(true);
        textArea.setMargin(GENERAL_MARGIN);
        TextStyle textStyle = MarkdownUtils.getContentStyle();
        textArea.getTextModel().setDefaultTextStyle(textStyle);
        return textArea;
    }

    /**
     * Adds all inline content from an element (leading text + inline children + tail texts) to a TextArea.
     * Requires the XMLElement to have been parsed with mixed-content support to populate tail content.
     */
    protected void addInlineContentToTextArea(XMLElement element, TextArea textArea)
    {
        // Add leading text content (text before the first child element)
        String leadText = element.getValue();
        if (leadText != null && !leadText.isEmpty())
            textArea.getTextModel().addChars(leadText);

        // Iterate child inline elements
        for (int i = 0; i < element.getElementCount(); i++) {
            XMLElement child = element.getElement(i);
            addInlineElementToTextArea(child, textArea);

            // Add tail content — text that follows this child element inside the parent
            String tailText = child.getTailContent();
            if (tailText != null && !tailText.isEmpty())
                textArea.getTextModel().addChars(tailText);
        }
    }

    /**
     * Adds a single inline element's content to a TextArea with appropriate styling.
     */
    protected void addInlineElementToTextArea(XMLElement element, TextArea textArea)
    {
        String tag = element.getName().toLowerCase();
        TextModel textModel = textArea.getTextModel();
        TextStyle baseStyle = textArea.getDefaultTextStyle();
        Font baseFont = baseStyle.getFont();

        switch (tag) {
            case "strong", "b" -> {
                String text = element.getValue();
                if (text != null)
                    textModel.addCharsWithStyle(text, baseStyle.copyForStyleValue(baseFont.getBold()));
                // Recurse for any nested inline children
                addNestedInlineContent(element, textArea, baseStyle.copyForStyleValue(baseFont.getBold()));
            }
            case "em", "i" -> {
                String text = element.getValue();
                if (text != null)
                    textModel.addCharsWithStyle(text, baseStyle.copyForStyleValue(baseFont.getItalic()));
                addNestedInlineContent(element, textArea, baseStyle.copyForStyleValue(baseFont.getItalic()));
            }
            case "code" -> {
                String text = element.getValue();
                if (text != null)
                    textModel.addCharsWithStyle(text, MarkdownUtils.getCodeStyle());
            }
            case "a" -> {
                String href = element.getAttributeValue("href");
                if (href != null) {
                    TextLink textLink = new TextLink(href);
                    TextStyle linkStyle = baseStyle.copyForStyleValue(textLink);
                    String text = element.getValue();
                    if (text == null || text.isEmpty()) text = href;
                    textModel.addCharsWithStyle(text, linkStyle);
                    textArea.getTextAdapter().setLinkHandler((e, url) -> handleLinkClick(url));
                }
            }
            case "br" -> textModel.addChars("\n");
            case "span" -> addInlineContentToTextArea(element, textArea);
            default -> {
                // For unknown inline elements, just add their text
                String text = element.getValue();
                if (text != null)
                    textModel.addChars(text);
                addNestedInlineContent(element, textArea, baseStyle);
            }
        }
    }

    /**
     * Adds nested inline child elements and their tail text using a specific style for lead text.
     */
    private void addNestedInlineContent(XMLElement element, TextArea textArea, TextStyle style)
    {
        TextModel textModel = textArea.getTextModel();
        for (int i = 0; i < element.getElementCount(); i++) {
            XMLElement child = element.getElement(i);
            addInlineElementToTextArea(child, textArea);
            String tailText = child.getTailContent();
            if (tailText != null && !tailText.isEmpty())
                textModel.addCharsWithStyle(tailText, style);
        }
    }

    /**
     * Called when a link is clicked.
     */
    protected void handleLinkClick(String urlAddr)
    {
        if (urlAddr.startsWith("http"))
            GFXEnv.getEnv().openURL(urlAddr);
    }

    /**
     * Override to use column layout (stacks block-level views vertically).
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new ColViewLayout(this); }

    /**
     * Returns the XMLElement associated with a given view.
     */
    public static XMLElement getHtmlElementForView(View aView)
    {
        return (XMLElement) aView.getMetadataForKey("HtmlElement");
    }

    /**
     * Associates an XMLElement with a given view via metadata.
     */
    private static void setHtmlElementForView(View aView, XMLElement element)
    {
        aView.setMetadataForKey("HtmlElement", element);
    }

    /**
     * Standard main method for testing.
     */
    public static void main(String[] args)
    {
        WebFile markdownFile = WebFile.getFileForPath("https://reportmill.com/SlideShow/samples/Tutorial.md");
        //WebFile markdownFile = WebFile.getFileForPath("/Users/jeff/SnapDev/SnapCode/src/snapcode/util/HelpFile.md");
        //WebFile markdownFile = WebFile.getFileForPath("/Users/jeff/SnapDev/Snaptris/src/snaptris/ReadMe.md");
        //WebFile markdownFile = WebFile.getFileForPath("/Users/jeff/SnapDev/SnapCode/src/snapcode/app/HomePage.md");
        MarkdownNode markdownNode = new MarkdownParser().parseMarkdownChars(markdownFile.getText());
        XMLElement htmlElement = MarkdownToHtml.markdownNodesToHtml(markdownNode.getChildNodes());
        System.out.println(htmlElement.getString());

        HtmlView htmlView = new HtmlView();
        htmlView.setHtml(htmlElement.getString());
        WindowView window = new WindowView();
        window.setContent(new ScrollView(htmlView));
        window.setVisible(true);
    }
}
