/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A panel for editing text files.
 */
public class TextPane extends ViewOwner implements PropChangeListener {

    // The TextView
    TextView            _textView;
    
    // The ToolBarPane
    ChildView                _toolBarPane;
    
    // Whether text pane text is modified
    boolean             _textModified;
    
/**
 * Returns the TextView.
 */
public TextView getTextView()  { getUI(); return _textView; }

/**
 * Creates the TextView.
 */
protected TextView createTextView()  { return new TextView(); }

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
 * Implement to set modified.
 */
public void propertyChange(PropChange anEvent)  { resetLater(); }

/**
 * Create UI.
 */
public View createUI()
{
    // Create ToolBar
    _toolBarPane = (ChildView)super.createUI();
    
    // Disable all button focus
    for(View node : _toolBarPane.getChildren()) if(node instanceof ButtonBase) node.setFocusable(false);
    
    // Create TextView and add to ScrollView (make sure it is always as big as ScrollView ViewportBounds
    TextViewBase text = createTextView();
    text.setName("TextView"); text.setGrowWidth(true); text.setGrowHeight(true);
    ScrollView spane = new ScrollView(text); spane.setName("ScrollView");
    setFirstFocus(text);
    
    // Create SelectionText Label in BottomBox
    Label slabel = new Label(); slabel.setName("SelectionText"); slabel.setFont(new Font("Arial", 11));
    HBox bbox = new HBox(); bbox.setPadding(2,2,2,5); bbox.setName("BottomBox");
    bbox.addChild(slabel);
    
    // Create BorderView and add ToolBar, text and bottom box
    BorderView pane = new BorderView();
    pane.setTop(_toolBarPane); pane.setCenter(spane); pane.setBottom(bbox);
    return pane;
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get text area and start listening for events (KeyEvents, MouseClicked, DragOver/Exit/Drop)
    _textView = getView("TextView", TextView.class);
    _textView.getTextBox().getText().addPropChangeListener(this);
    
    // Configure FindText
    getView("FindText", TextField.class).setPromptText("Find");
    getView("FindText", TextField.class).getLabel().setImage(Image.get(TextPane.class, "Find.png"));
    
    // Add binding for SelectionText
    addViewBinding("SelectionText", "Text", "SelectionInfo");
    
    // Register command-s for save, command-f for find, command-l for line number and escape
    addKeyActionEvent("SaveButton", "Shortcut+S");
    addKeyActionEvent("FindButton", "Shortcut+F");
    addKeyActionEvent("FindText", "Shortcut+G");
    addKeyActionEvent("FindTextPrevious", "Shortcut+Shift+G");
    addKeyActionEvent("LineNumberPanelAction", "Shortcut+L");
    addKeyActionEvent("EscapeAction", "ESC");
}

/**
 * Reset UI.
 */
public void resetUI()
{    
    // Reset FontSizeText
    setViewValue("FontSizeText", getTextView().getFont().getSize());
    
    // Reset TextModified
    setTextModified(getTextView().getUndoer().hasUndos());
}

/**
 * Respond to UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle SaveButton
    if(anEvent.equals("SaveButton")) saveChanges();
    
    // Handle FontSizeText
    if(anEvent.equals("FontSizeText")) { float size = anEvent.getFloatValue(); if(size<1) return;
        Font font = getTextView().getFont(), font2 = new Font(font.getName(), size);
        getTextView().setFont(font2);
        requestFocus(getTextView());
    }
    
    // Handle IncreaseFontButton
    if(anEvent.equals("IncreaseFontButton")) {
        Font font = getTextView().getFont(), font2 = new Font(font.getName(), font.getSize()+1);
        getTextView().setFont(font2);
    }
    
    // Handle DecreaseFontButton
    if(anEvent.equals("DecreaseFontButton")) {
        Font font = getTextView().getFont(), font2 = new Font(font.getName(), font.getSize()-1);
        getTextView().setFont(font2);
    }
    
    // Handle UndoButton
    if(anEvent.equals("UndoButton")) {
        if(getTextView().getUndoer().hasUndos())
            getTextView().undo();
        else beep();
    }
    
    // Handle RedoButton
    if(anEvent.equals("RedoButton")) {
        if(getTextView().getUndoer().hasRedos())
            getTextView().redo();
        else beep();
    }
    
    // Handle FindButton
    if(anEvent.equals("FindButton")) {
        if(!getTextView().getSel().isEmpty()) setViewValue("FindText", getTextView().getSel().getString());
        getView("FindText", TextViewBase.class).selectAll();
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
        if(t1.isFocused() || t2.isFocused()) requestFocus(getTextView());
    }
}

/**
 * Save file.
 */
public void saveChanges()
{
    saveChangesImpl(); // Do real version
    getTextView().getUndoer().reset(); // Reset Undo
    setTextModified(false); // Reset TextModified
}

/**
 * Save file.
 */
protected void saveChangesImpl()
{
    WebURL surl = getTextView().getTextBox().getSourceURL();
    WebFile file = surl.getFile();
    if(file==null) file = surl.createFile(false);
    file.setText(getTextView().getTextBox().getString());
    try { file.save(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Get compile info.
 */
public String getSelectionInfo()
{
    StringBuffer sb = new StringBuffer();
    TextBoxLine textLine = getTextView().getLineAt(getTextView().getSelStart());
    sb.append("Line ").append(textLine.getIndex()+1);
    sb.append(", Col ").append(getTextView().getSelStart() - textLine.getStart());
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
    TextViewBase tview = getTextView();
    String string = aString; if(ignoreCase) string = string.toLowerCase();
    String text = tview.getText(); if(ignoreCase) text = text.toLowerCase();
    
    // Get index of search
    int sstart = tview.getSelStart(), send = tview.getSelEnd();
    int index = isNext? text.indexOf(string, send) : text.lastIndexOf(string, Math.max(sstart-1,0));
    
    // If index not found, beep and try again from start
    if(index<0) {
        beep();
        index = isNext? text.indexOf(string) : text.lastIndexOf(string, text.length());
    }
    
    // If index found, select text and focus
    if(index>=0) tview.setSel(index, index + string.length());
}

/**
 * Shows the LineNumberPanel.
 */
public void showLineNumberPanel()
{
    TextSel sel = getTextView().getSel();
    int lnum = sel.getStartLine().getIndex()+1, start = sel.getStart(), col = start - sel.getStartLine().getStart();
    String msg = String.format("Enter Line Number:\n(Line %d, Col %d, Char %d)", lnum, col, start);
    DialogBox dbox = new DialogBox("Go to Line"); dbox.setQuestionMessage(msg);
    String lstring = dbox.showInputDialog(getUI(), Integer.toString(lnum));
    int lindex = lstring!=null? SnapUtils.intValue(lstring) -1 : -1;
    if(lindex<0) lindex = 0; else if(lindex>=getTextView().getLineCount()) lindex = getTextView().getLineCount() -1;
    TextBoxLine line = lindex>=0 && lindex<getTextView().getLineCount()? getTextView().getLine(lindex) : null;
    getTextView().setSel(line.getStart(), line.getEnd());
    requestFocus(getTextView());
}

}