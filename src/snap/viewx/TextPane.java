/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.*;
import snap.text.TextBoxLine;
import snap.text.TextSel;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A panel for editing text files.
 */
public class TextPane extends ViewOwner {

    // The TextArea
    TextArea            _textArea;
    
    // The ToolBarPane
    ChildView           _toolBarPane;
    
    // Whether text pane text is modified
    boolean             _textModified;
    
/**
 * Returns the TextArea.
 */
public TextArea getTextArea()  { getUI(); return _textArea; }

/**
 * Creates the TextArea.
 */
protected TextArea createTextArea()  { return new TextArea(); }

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
public void setTextModified(boolean aFlag)  { _textModified = aFlag; }

/**
 * Create UI.
 */
protected View createUI()
{
    // Create ToolBar
    _toolBarPane = (ChildView)super.createUI();
    
    // Disable all button focus
    for(View node : _toolBarPane.getChildren()) if(node instanceof ButtonBase) node.setFocusable(false);
    
    // Create/config TextArea
    TextArea text = createTextArea();
    text.setFill(Color.WHITE); text.setEditable(true); text.setName("TextArea");
    setFirstFocus(text);
    
    // Wrap TextArea in ScrollPane
    ScrollView scroll = new ScrollView(text); scroll.setName("ScrollView");
    
    // Create SelectionText Label in BottomBox
    Label slabel = new Label(); slabel.setName("SelectionText"); slabel.setFont(new Font("Arial", 11));
    RowView bbox = new RowView(); bbox.setPadding(2,2,2,5); bbox.setName("BottomBox");
    bbox.addChild(slabel);
    
    // Create BorderView and add ToolBar, text and bottom box
    BorderView pane = new BorderView();
    pane.setTop(_toolBarPane); pane.setCenter(scroll); pane.setBottom(bbox);
    return pane;
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get text area and start listening for events (KeyEvents, MouseReleased, DragOver/Exit/Drop)
    _textArea = getView("TextArea", TextArea.class);
    _textArea.getRichText().addPropChangeListener(pc -> resetLater());
    
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
    // Reset FontSizeText
    setViewValue("FontSizeText", getTextArea().getFont().getSize());
    
    // Reset TextModified
    setTextModified(getTextArea().getUndoer().hasUndos());
    
    // Update SelectionText, UndoButton, RedoButton
    setViewText("SelectionText", getSelectionInfo());
    setViewEnabled("UndoButton", getTextArea().getUndoer().hasUndos());
    setViewEnabled("RedoButton", getTextArea().getUndoer().hasRedos());
}

/**
 * Respond to UI controls.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle SaveButton
    if(anEvent.equals("SaveButton")) saveChanges();
    
    // Handle FontSizeText
    if(anEvent.equals("FontSizeText")) { float size = anEvent.getFloatValue(); if(size<1) return;
        Font font = getTextArea().getFont(), font2 = new Font(font.getName(), size);
        getTextArea().setFont(font2);
        requestFocus(getTextArea());
    }
    
    // Handle IncreaseFontButton
    if(anEvent.equals("IncreaseFontButton")) {
        Font font = getTextArea().getFont(), font2 = new Font(font.getName(), font.getSize()+1);
        getTextArea().setFont(font2);
    }
    
    // Handle DecreaseFontButton
    if(anEvent.equals("DecreaseFontButton")) {
        Font font = getTextArea().getFont(), font2 = new Font(font.getName(), font.getSize()-1);
        getTextArea().setFont(font2);
    }
    
    // Handle UndoButton
    if(anEvent.equals("UndoButton")) {
        if(getTextArea().getUndoer().hasUndos())
            getTextArea().undo();
        else beep();
    }
    
    // Handle RedoButton
    if(anEvent.equals("RedoButton")) {
        if(getTextArea().getUndoer().hasRedos())
            getTextArea().redo();
        else beep();
    }
    
    // Handle FindButton
    if(anEvent.equals("FindButton")) {
        if(!getTextArea().getSel().isEmpty()) setViewValue("FindText", getTextArea().getSel().getString());
        getView("FindText", TextField.class).selectAll();
        requestFocus("FindText");
    }
    
    // Handle FindText
    if(anEvent.equals("FindText") || anEvent.equals("FindTextPrevious")) {
        String string = getViewStringValue("FindText");
        boolean ignoreCase = getViewBoolValue("IgnoreCaseCheckBox");
        boolean findNext = anEvent.equals("FindText");
        find(string, ignoreCase, findNext);
    }
    
    // Handle LineNumberPanelAction (Without RunLater, modal DialogBox seems to cause event resend)
    if(anEvent.equals("LineNumberPanelAction"))
        runLater(() -> showLineNumberPanel());
            
    // Handle EscapeAction
    if(anEvent.equals("EscapeAction")) {
        View t1 = getView("FindText"), t2 = getView("FontSizeText");
        if(t1.isFocused() || t2.isFocused()) requestFocus(getTextArea());
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
    WebURL surl = getTextArea().getTextBox().getSourceURL();
    WebFile file = surl.getFile();
    if(file==null) file = surl.createFile(false);
    file.setText(getTextArea().getTextBox().getString());
    try { file.save(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Get compile info.
 */
public String getSelectionInfo()
{
    StringBuffer sb = new StringBuffer();
    TextBoxLine textLine = getTextArea().getLineAt(getTextArea().getSelStart());
    sb.append("Line ").append(textLine.getIndex()+1);
    sb.append(", Col ").append(getTextArea().getSelStart() - textLine.getStart());
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
    String string = aString; if(ignoreCase) string = string.toLowerCase();
    String text = tarea.getText(); if(ignoreCase) text = text.toLowerCase();
    
    // Get index of search
    int sstart = tarea.getSelStart(), send = tarea.getSelEnd();
    int index = isNext? text.indexOf(string, send) : text.lastIndexOf(string, Math.max(sstart-1,0));
    
    // If index not found, beep and try again from start
    if(index<0) {
        beep();
        index = isNext? text.indexOf(string) : text.lastIndexOf(string, text.length());
    }
    
    // If index found, select text and focus
    if(index>=0) tarea.setSel(index, index + string.length());
}

/**
 * Shows the LineNumberPanel.
 */
public void showLineNumberPanel()
{
    TextSel sel = getTextArea().getSel();
    int lnum = sel.getStartLine().getIndex()+1, start = sel.getStart(), col = start - sel.getStartLine().getStart();
    String msg = StringUtils.format("Enter Line Number:\n(Line %d, Col %d, Char %d)", lnum, col, start);
    DialogBox dbox = new DialogBox("Go to Line"); dbox.setQuestionMessage(msg);
    String lstring = dbox.showInputDialog(getUI(), Integer.toString(lnum));
    int lindex = lstring!=null? SnapUtils.intValue(lstring) -1 : -1;
    if(lindex<0) lindex = 0; else if(lindex>=getTextArea().getLineCount()) lindex = getTextArea().getLineCount() -1;
    TextBoxLine line = lindex>=0 && lindex<getTextArea().getLineCount()? getTextArea().getLine(lindex) : null;
    getTextArea().setSel(line.getStart(), line.getEnd());
    requestFocus(getTextArea());
}

/**
 * Silly test.
 */
public static void main(String args[])
{
    TextPane tp = new TextPane();
    tp.getTextArea().setPrefSize(800,600);
    //String text = WebURL.getURL(TextPane.class, "TextPane.snp").getText();
    //tp.getTextArea().setText(text);
    tp.setWindowVisible(true);
}

}