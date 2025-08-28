/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.props.PropChange;
import snap.props.Undoer;
import snap.text.TextModel;
import snap.text.TextLine;
import snap.text.TextSel;
import snap.util.Convert;
import snap.util.ListUtils;
import snap.view.*;
import java.util.List;

/**
 * A panel for editing text files.
 */
public class TextPane extends ViewOwner {

    // The TextArea
    private TextArea  _textArea;

    // The ToolBarPane
    private ChildView  _toolBarPane;

    // The find panel
    private DrawerView _findPanel;

    // The find text field
    private TextField _findTextField;

    /**
     * Constructor.
     */
    public TextPane()
    {
        super();
        _textArea = createTextArea();
    }

    /**
     * Returns the TextArea.
     */
    public TextArea getTextArea()  { return _textArea; }

    /**
     * Creates the TextArea.
     */
    protected TextArea createTextArea()  { return new TextArea(true); }

    /**
     * Load text.
     */
    protected void loadTextAreaText()  { }

    /**
     * Shows the find panel.
     */
    public void showFindPanel()
    {
        getFindPanel().showDrawer(getTextArea().getParent());

        TextArea textArea = getTextArea();
        if (!textArea.getSel().isEmpty())
            _findTextField.setText(textArea.getSel().getString());
        _findTextField.selectAll();
        requestFocus(_findTextField);
    }

    /**
     * Hides the find panel.
     */
    public void hideFindPanel()
    {
        getFindPanel().hide();

        TextArea textArea = getTextArea();
        requestFocus(textArea);
    }

    /**
     * Returns the find panel.
     */
    protected DrawerView getFindPanel()
    {
        if (_findPanel != null) return _findPanel;

        // Load UI and set
        ParentView findPanelUI = (ParentView) UILoader.loadViewForString(FIND_PANEL_UI);
        _findTextField = (TextField) findPanelUI.getChildForName("FindText");
        _findTextField.getLabel().setImage(Image.getImageForClassResource(TextPane.class, "Find.png"));

        findPanelUI.addPropChangeListener(pc -> handleFindPanelShowingChange(), Showing_Prop);

        // Create find panel
        _findPanel = new DrawerView();
        _findPanel.setDecorated(false);
        _findPanel.setLean(Pos.TOP_CENTER);
        _findPanel.setGrowWidth(true);
        _findPanel.setAnimTime(200);
        _findPanel.setContent(findPanelUI);

        // Return
        return _findPanel;
    }

    private void handleFindPanelShowingChange()
    {
        if (_findPanel == null) return;

        if (_findPanel.isShowing()) {
            requestFocus(_findTextField);
        }
        else {
            TextArea textArea = getTextArea();
            requestFocus(textArea);
        }
    }

    /**
     * Saves text to file.
     */
    public void saveTextToFile()
    {
        TextArea textArea = getTextArea();
        TextModel textModel = textArea.getTextModel();

        // Do real version
        if (textModel.getSourceUrl() != null) {
            try { textModel.writeTextToSourceFile(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }
    }

    /**
     * Sets font size.
     */
    public void setFontSize(double fontSize)
    {
        if (fontSize < 1) return;
        TextArea textArea = getTextArea();
        Font font = textArea.getTextFont();
        Font font2 = new Font(font.getName(), fontSize);
        textArea.setTextFont(font2);
        requestFocus(textArea);
    }

    /**
     * Increase font size.
     */
    public void increaseFontSize()
    {
        TextArea textArea = getTextArea();
        Font font = textArea.getTextFont();
        Font font2 = new Font(font.getName(), font.getSize() + 1);
        textArea.setTextFont(font2);
    }

    /**
     * Decrease font size.
     */
    public void decreaseFontSize()
    {
        TextArea textArea = getTextArea();
        Font font = textArea.getTextFont();
        Font font2 = new Font(font.getName(), font.getSize() - 1);
        textArea.setTextFont(font2);
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        // Create ToolBar
        _toolBarPane = (ChildView) super.createUI();

        // Create/config TextArea
        _textArea.setPadding(new Insets(5));
        _textArea.setFill(Color.WHITE);
        _textArea.setGrowWidth(true);
        _textArea.setEditable(true);
        _textArea.setSyncTextFont(false);
        _textArea.setUndoActivated(true);

        // Wrap TextArea in ScrollPane
        ScrollView scrollView = new ScrollView(_textArea);
        scrollView.setName("ScrollView");

        // Create SelectionText Label in BottomBox
        Label selectionLabel = new Label();
        selectionLabel.setName("SelectionText");

        // Create BottomRowView and add SelectionText
        RowView bottomRowView = new RowView();
        bottomRowView.setName("BottomBox");
        bottomRowView.setPadding(2, 2, 2, 5);
        bottomRowView.addChild(selectionLabel);

        // Create BorderView and add ToolBar, text and bottom box
        BorderView borderView = new BorderView();
        borderView.setTop(_toolBarPane);
        borderView.setCenter(scrollView);
        borderView.setBottom(bottomRowView);

        // Return
        return borderView;
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Disable all toolbar button focus
        List<ButtonBase> toolBarButtons = ListUtils.filterByClass(_toolBarPane.getChildren(), ButtonBase.class);
        toolBarButtons.forEach(button -> button.setFocusable(false));

        // Get text area and start listening for events (KeyEvents, MouseReleased, DragOver/Exit/Drop)
        _textArea.addPropChangeListener(this::handleTextAreaPropChange);
        _textArea.getTextAdapter().addTextModelPropChangeListener(this::handleSourceTextPropChange);
        setFirstFocus(_textArea);

        // Load text area text
        loadTextAreaText();

        // Register command-s for save, command-f for find, command-l for line number and escape
        addKeyActionHandler("SaveButton", "Shortcut+S");
        addKeyActionHandler("FindButton", "Shortcut+F");
        addKeyActionHandler("FindText", "Shortcut+G");
        addKeyActionHandler("FindTextPrevious", "Shortcut+Shift+G");
        addKeyActionHandler("LineNumberPanelAction", "Shortcut+L");
        addKeyActionHandler("EscapeAction", "ESC");
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Reset FontSizeText
        setViewValue("FontSizeText", _textArea.getTextFont().getSize());

        // Update UndoButton, RedoButton
        Undoer undoer = _textArea.getUndoer();
        setViewEnabled("UndoButton", undoer.hasUndos());
        setViewEnabled("RedoButton", undoer.hasRedos());

        // Update SelectionText
        setViewText("SelectionText", getSelectionInfo());
    }

    /**
     * Respond to UI controls.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        TextArea textArea = getTextArea();

        switch (anEvent.getName()) {

            // Handle SaveButton
            case "SaveButton" -> saveTextToFile();

            // Handle CutButton, CopyButton, PasteButton, DeleteButton
            case "CutButton" -> textArea.cut();
            case "CopyButton" -> textArea.copy();
            case "PasteButton" -> textArea.paste();

            // Handle UndoButton, RedoButton
            case "UndoButton" -> textArea.undo();
            case "RedoButton" -> textArea.redo();

            // Handle FontSizeText,
            case "FontSizeText" -> setFontSize(anEvent.getFloatValue());
            case "IncreaseFontButton" -> increaseFontSize();
            case "DecreaseFontButton" -> decreaseFontSize();

            // Handle FindButton
            case "FindButton" -> showFindPanel();

            // Handle FindText, FindTextPrevious
            case "FindText", "FindTextPrevious" -> {
                String findString = getViewStringValue("FindText");
                boolean ignoreCase = getViewBoolValue("IgnoreCaseCheckBox");
                boolean findNext = anEvent.equals("FindText");
                find(findString, ignoreCase, findNext);
            }

            // Handle LineNumberPanelAction (Without RunLater, modal DialogBox seems to cause event resend)
            case "LineNumberPanelAction" -> runLater(() -> showLineNumberPanel());

            // Handle EscapeAction
            case "EscapeAction" -> hideFindPanel();
        }
    }

    /**
     * Get compile info.
     */
    public String getSelectionInfo()
    {
        StringBuilder sb = new StringBuilder();
        TextLine textLine = getTextArea().getLineForCharIndex(getTextArea().getSelStart());
        sb.append("Line ").append(textLine.getLineIndex() + 1);
        sb.append(", Col ").append(getTextArea().getSelStart() - textLine.getStartCharIndex());
        return sb.toString();
    }

    /**
     * Finds the given string.
     */
    public void find(String aString, boolean ignoreCase, boolean isNext)
    {
        // Set String Value in FindText (if needed)
        if (_findTextField != null)
            _findTextField.setText(aString);

        // Get search string and find in text
        TextArea tarea = getTextArea();
        String string = aString;
        if (ignoreCase) string = string.toLowerCase();
        String text = tarea.getText();
        if (ignoreCase) text = text.toLowerCase();

        // Get index of search
        int sstart = tarea.getSelStart(), send = tarea.getSelEnd();
        int index = isNext ? text.indexOf(string, send) : text.lastIndexOf(string, Math.max(sstart - 1, 0));

        // If index not found, beep and try again from start
        if (index < 0) {
            beep();
            index = isNext ? text.indexOf(string) : text.lastIndexOf(string);
        }

        // If index found, select text and focus
        if (index >= 0) tarea.setSel(index, index + string.length());
    }

    /**
     * Shows the LineNumberPanel.
     */
    public void showLineNumberPanel()
    {
        TextArea textArea = getTextArea();
        TextSel sel = textArea.getSel();
        TextLine selStartLine = sel.getStartLine();
        int selStartLineIndex = selStartLine.getLineIndex() + 1;
        int selStart = sel.getStart();
        int col = selStart - selStartLine.getStartCharIndex();

        // Run ShowLineNumber panel
        String msg = String.format("Enter Line Number:\n(Line %d, Col %d, Char %d)", selStartLineIndex, col, selStart);
        DialogBox dialogBox = new DialogBox("Go to Line");
        dialogBox.setQuestionMessage(msg);
        String lineNumStr = dialogBox.showInputDialog(getUI(), Integer.toString(selStartLineIndex));
        if (lineNumStr == null)
            return;

        // Get LineIndex from response and select line
        int lineIndex = Convert.intValue(lineNumStr) - 1;
        textArea.getTextAdapter().selectLine(lineIndex);

        // Focus TextArea
        requestFocus(textArea);
    }

    /**
     * Called when TextArea does prop change.
     */
    protected void handleTextAreaPropChange(PropChange aPC)  { }

    /**
     * Called when TextModel does prop change.
     */
    protected void handleSourceTextPropChange(PropChange aPC)
    {
        resetLater();
    }

    /**
     * Silly test.
     */
    public static void main(String[] args)
    {
        TextPane textPane = new TextPane();
        TextArea textArea = textPane.getTextArea();
        textArea.setPrefSize(800, 600);
        //String text = WebURL.getURL(TextPane.class, "TextPane.snp").getText(); textArea.setText(text);
        textPane.setWindowVisible(true);
    }

    /**
     * THe Find panel UI.
     */
    private static final String FIND_PANEL_UI = """
        <ColView>
          <RowView Margin="3" GrowWidth="true">
            <TextField Name="FindText" PrefWidth="180" PrefHeight="22" Margin="3" BorderRadius="4" GrowWidth="true" Font="Arial 12" PromptText="Find" />
            <CheckBox Name="IgnoreCaseCheckBox" PrefWidth="47" PrefHeight="22" Text="IC" Selected="true" />
          </RowView>
          <RectView PrefHeight="1" Fill="#C0" GrowWidth="true" />
        </ColView>
        """;
}