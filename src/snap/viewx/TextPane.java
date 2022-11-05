/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.text.TextBoxLine;
import snap.text.TextDoc;
import snap.text.TextSel;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A panel for editing text files.
 */
public class TextPane extends ViewOwner {

    // The TextArea
    private TextArea  _textArea;

    // The ToolBarPane
    private ChildView  _toolBarPane;

    // Whether text pane text is modified
    private boolean  _textModified;

    /**
     * Returns the TextArea.
     */
    public TextArea getTextArea()
    {
        getUI();
        return _textArea;
    }

    /**
     * Creates the TextArea.
     */
    protected TextArea createTextArea()
    {
        return new TextArea();
    }

    /**
     * Returns the toolbar pane.
     */
    public ChildView getToolBarPane()  { return _toolBarPane; }

    /**
     * Returns whether text is modified.
     */
    public boolean isTextModified()  { return _textModified; }

    /**
     * Sets whether text is modified.
     */
    public void setTextModified(boolean aFlag)
    {
        _textModified = aFlag;
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Create ToolBar
        _toolBarPane = (ChildView) super.createUI();

        // Disable all button focus
        for (View node : _toolBarPane.getChildren()) if (node instanceof ButtonBase) node.setFocusable(false);

        // Create/config TextArea
        TextArea text = createTextArea();
        text.setFill(Color.WHITE);
        text.setEditable(true);
        text.setName("TextArea");
        setFirstFocus(text);

        // Wrap TextArea in ScrollPane
        ScrollView scroll = new ScrollView(text);
        scroll.setName("ScrollView");

        // Create SelectionText Label in BottomBox
        Label slabel = new Label();
        slabel.setName("SelectionText");
        slabel.setFont(new Font("Arial", 11));
        RowView bbox = new RowView();
        bbox.setPadding(2, 2, 2, 5);
        bbox.setName("BottomBox");
        bbox.addChild(slabel);

        // Create BorderView and add ToolBar, text and bottom box
        BorderView pane = new BorderView();
        pane.setTop(_toolBarPane);
        pane.setCenter(scroll);
        pane.setBottom(bbox);
        return pane;
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get text area and start listening for events (KeyEvents, MouseReleased, DragOver/Exit/Drop)
        _textArea = getView("TextArea", TextArea.class);
        _textArea.getTextDoc().addPropChangeListener(pc -> resetLater());

        // Configure FindText
        getView("FindText", TextField.class).setPromptText("Find");
        getView("FindText", TextField.class).getLabel().setImage(Image.get(TextPane.class, "Find.png"));

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
        // Get TextArea
        TextArea textArea = getTextArea();

        // Update UndoButton, RedoButton
        setViewEnabled("UndoButton", textArea.getUndoer().hasUndos());
        setViewEnabled("RedoButton", textArea.getUndoer().hasRedos());

        // Reset FontSizeText
        setViewValue("FontSizeText", textArea.getFont().getSize());

        // Reset TextModified
        setTextModified(textArea.getUndoer().hasUndos());

        // Update SelectionText
        setViewText("SelectionText", getSelectionInfo());
    }

    /**
     * Respond to UI controls.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get TextArea
        TextArea textArea = getTextArea();

        // Handle SaveButton
        if (anEvent.equals("SaveButton"))
            saveChanges();

        // Handle CutButton, CopyButton, PasteButton, DeleteButton
        if (anEvent.equals("CutButton"))
            textArea.cut();
        if (anEvent.equals("CopyButton"))
            textArea.copy();
        if (anEvent.equals("PasteButton"))
            textArea.paste();

        // Handle UndoButton
        if (anEvent.equals("UndoButton")) {
            if (textArea.getUndoer().hasUndos())
                textArea.undo();
            else beep();
        }

        // Handle RedoButton
        if (anEvent.equals("RedoButton")) {
            if (textArea.getUndoer().hasRedos())
                textArea.redo();
            else beep();
        }

        // Handle FontSizeText
        if (anEvent.equals("FontSizeText")) {
            float size = anEvent.getFloatValue();
            if (size < 1) return;
            Font font = textArea.getFont();
            Font font2 = new Font(font.getName(), size);
            textArea.setFont(font2);
            requestFocus(textArea);
        }

        // Handle IncreaseFontButton
        if (anEvent.equals("IncreaseFontButton")) {
            Font font = textArea.getFont();
            Font font2 = new Font(font.getName(), font.getSize() + 1);
            textArea.setFont(font2);
        }

        // Handle DecreaseFontButton
        if (anEvent.equals("DecreaseFontButton")) {
            Font font = textArea.getFont();
            Font font2 = new Font(font.getName(), font.getSize() - 1);
            textArea.setFont(font2);
        }

        // Handle FindButton
        if (anEvent.equals("FindButton")) {
            if (!textArea.getSel().isEmpty())
                setViewValue("FindText", textArea.getSel().getString());
            getView("FindText", TextField.class).selectAll();
            requestFocus("FindText");
        }

        // Handle FindText
        if (anEvent.equals("FindText") || anEvent.equals("FindTextPrevious")) {
            String string = getViewStringValue("FindText");
            boolean ignoreCase = getViewBoolValue("IgnoreCaseCheckBox");
            boolean findNext = anEvent.equals("FindText");
            find(string, ignoreCase, findNext);
        }

        // Handle LineNumberPanelAction (Without RunLater, modal DialogBox seems to cause event resend)
        if (anEvent.equals("LineNumberPanelAction"))
            runLater(() -> showLineNumberPanel());

        // Handle EscapeAction
        if (anEvent.equals("EscapeAction")) {
            View t1 = getView("FindText"), t2 = getView("FontSizeText");
            if (t1.isFocused() || t2.isFocused())
                requestFocus(textArea);
        }
    }

    /**
     * Save file.
     */
    public void saveChanges()
    {
        saveChangesImpl(); // Do real version
        getTextArea().getUndoer().reset(); // Reset Undo
        setTextModified(false); // Reset TextModified
    }

    /**
     * Save file.
     */
    protected void saveChangesImpl()
    {
        TextArea textArea = getTextArea();
        TextDoc textDoc = textArea.getTextDoc();
        textDoc.saveToSourceFile();
    }

    /**
     * Get compile info.
     */
    public String getSelectionInfo()
    {
        StringBuffer sb = new StringBuffer();
        TextBoxLine textLine = getTextArea().getLineForCharIndex(getTextArea().getSelStart());
        sb.append("Line ").append(textLine.getIndex() + 1);
        sb.append(", Col ").append(getTextArea().getSelStart() - textLine.getStartCharIndex());
        return sb.toString();
    }

    /**
     * Finds the given string.
     */
    public void find(String aString, boolean ignoreCase, boolean isNext)
    {
        // Set String Value in FindText (if needed)
        setViewValue("FindText", aString);

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
            index = isNext ? text.indexOf(string) : text.lastIndexOf(string, text.length());
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
        TextBoxLine selStartLine = sel.getStartLine();
        int selStartLineIndex = selStartLine.getIndex() + 1;
        int selStart = sel.getStart();
        int col = selStart - selStartLine.getStartCharIndex();

        // Run ShowLineNumber panel
        String msg = String.format("Enter Line Number:\n(Line %d, Col %d, Char %d)", selStartLineIndex, col, selStart);
        DialogBox dialogBox = new DialogBox("Go to Line");
        dialogBox.setQuestionMessage(msg);
        String lineNumStr = dialogBox.showInputDialog(getUI(), Integer.toString(selStartLineIndex));

        // Get LineIndex from response
        int lineIndex = lineNumStr != null ? SnapUtils.intValue(lineNumStr) - 1 : -1;
        if (lineIndex < 0)
            lineIndex = 0;
        else if (lineIndex >= textArea.getLineCount())
            lineIndex = textArea.getLineCount() - 1;

        // Select line and focus
        TextBoxLine line = lineIndex >= 0 && lineIndex < textArea.getLineCount() ? textArea.getLine(lineIndex) : null;
        textArea.setSel(line.getStartCharIndex(), line.getEndCharIndex());
        requestFocus(textArea);
    }

    /**
     * Silly test.
     */
    public static void main(String args[])
    {
        TextPane textPane = new TextPane();
        TextArea textArea = textPane.getTextArea();
        textArea.setPrefSize(800, 600);
        //String text = WebURL.getURL(TextPane.class, "TextPane.snp").getText();
        //textArea.setText(text);
        textPane.setWindowVisible(true);
    }

}