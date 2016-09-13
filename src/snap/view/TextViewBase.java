package snap.view;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A View subclass for viewing/editing a TextBox.
 */
public class TextViewBase extends ParentView implements PropChangeListener {

    // The text being edited
    TextBox               _tbox;
    
    // Whether text is editable
    boolean               _editable;
    
    // Whether text should wrap
    boolean               _wrapText;
    
    // The text selection
    TextSel               _sel;
    
    // Whether the editor is word selecting (double click) or paragraph selecting (triple click)
    boolean               _wordSel, _pgraphSel;
    
    // The Selection color
    Color                 _selColor = new Color(181, 214, 254, 255);
    
    // The current TextStyle for the cursor or selection
    TextStyle             _inputStyle;
    
    // The text pane undoer
    Undoer                _undoer = new Undoer();
    
    // The index of the last replace so we can commit undo changes if not adjacent
    int                   _lastReplaceIndex;
    
    // The mouse down point
    double                _downX, _downY;
    
    // The animator for caret blinking
    ViewTimer             _caretTimer;
    
    // Whether to hide carent
    boolean               _hideCaret;
    
    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String WrapText_Prop = "WrapText";

/**
 * Creates a new TextBoxBase.
 */
public TextViewBase()
{
    // Set default font
    getTextBox();
    setRich(false);
    setFont(getDefaultFont());
    
    // Enable mouse, key and focus events
    enableEvents(MouseEvents); enableEvents(KeyEvents);
    setFocusable(true); setFocusWhenPressed(true);
    
    // Disable Tab stealing FocusTraversalKeys
    //setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
    //setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
}

/**
 * Returns the number of characters in the text string.
 */
public int length()  { return getTextBox().length(); }

/**
 * Returns the individual character at given index.
 */
public char charAt(int anIndex)  { return getTextBox().charAt(anIndex); }

/**
 * Returns the plain string of the text being edited.
 */
public String getText()  { return getTextBox().getString(); }

/**
 * Set text string of text editor.
 */
public void setText(String aString)
{
    // If string already set, just return
    String str = aString!=null? aString : "";
    if(str.length()==length() && (str.length()==0 || str.equals(getText()))) return;
    
    // Set string and notify
    getTextBox().setString(aString);
    textDidChange();
}

/**
 * Set the source for TextComponent text.
 */
public void setSource(Object aSource)
{
    getTextBox().setSource(aSource);
    textDidChange();
    setSel(0);
}

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return getTextBox().getSourceURL(); }

/**
 * Returns the source file.
 */
public WebFile getSourceFile()  { return getTextBox().getSourceFile(); }

/**
 * Returns the rich text.
 */
public RichText getRichText()  { return getTextBox().getText(); }

/**
 * Returns the text that is being edited.
 */
public TextBox getTextBox()  { if(_tbox==null) setTextBox(createText()); return _tbox; }

/**
 * Sets the text that is to be edited.
 */
public void setTextBox(TextBox aText)
{
    // If value already set, just return
    if(aText==_tbox) return;
    
    // Set Text and add/remove PropChangeListener
    if(_tbox!=null) _tbox.getText().removePropChangeListener(this);
    _tbox = aText;
    if(_tbox!=null) _tbox.getText().addPropChangeListener(this);
    
    // Reset selection
    _sel = new TextSel(_tbox, 0, 0, 0); //_text.setPropChangeEnabled(true);
    repaint();
}

/**
 * Creates a new Text.
 */
protected TextBox createText()  { return new TextBox(); }

/**
 * Returns the padding default.
 */
public Insets getDefaultPadding()  { return _def; } static Insets _def = new Insets(2);

/**
 * Returns whether Text shape is editable.
 */
public boolean isEditable()  { return _editable; }

/**
 * Sets whether Text shape is editable.
 */
public void setEditable(boolean aValue)
{
    if(aValue==isEditable()) return;
    firePropChange(Editable_Prop, _editable, _editable=aValue);
}

/**
 * Returns whether text wraps.
 */
public boolean isWrapText()  { return _wrapText; }

/**
 * Sets whether text wraps.
 */
public void setWrapText(boolean aValue)
{
    if(aValue==_wrapText) return;
    firePropChange(WrapText_Prop, _wrapText, _wrapText=aValue);
    getTextBox().setWrapText(aValue);
}

/**
 * Returns whether text supports multiple styles.
 */
public boolean isRich()  { return !getTextBox().isSingleStyle(); }

/**
 * Sets whether text supports multiple styles.
 */
public void setRich(boolean aValue)  { getTextBox().setSingleStyle(!aValue); }

/**
 * Returns the text selection.
 */
public TextSel getSel()  { return _sel; }

/**
 * Returns the character index of the start of the text selection.
 */
public int getSelStart()  { return _sel.getStart(); }

/**
 * Sets the selection start.
 */
public void setSelStart(int aValue)  { setSel(aValue,getSelEnd()); }

/**
 * Returns the character index of the end of the text selection.
 */
public int getSelEnd()  { return _sel.getEnd(); }

/**
 * Sets the selection end.
 */
public void setSelEnd(int aValue)  { setSel(getSelStart(),aValue); }

/**
 * Returns the character index of the last explicitly selected char (confined to the bounds of the selection).
 */
public int getSelAnchor()  { return _sel.getAnchor(); }

/**
 * Returns whether the selection is empty.
 */
public boolean isSelEmpty()  { return _sel.isEmpty(); }

/**
 * Sets the character index of the text cursor.
 */
public void setSel(int newStartEnd)  { setSel(newStartEnd, newStartEnd); }

/**
 * Sets the character index of the start and end of the text selection.
 */
public void setSel(int aStart, int anEnd)  { setSel(aStart, anEnd, anEnd); }

/**
 * Sets the character index of the start and end of the text selection.
 */
public void setSel(int aStart, int anEnd, int anAnchor)
{
    // Make sure start is before end
    if(anEnd<aStart) { int temp = anEnd; anEnd = aStart; aStart = temp; }
    
    // Get old selection shape
    if(isShowing()) repaintSel();
    
    // Set new start/end
    _sel = new TextSel(_tbox, aStart, anEnd, anAnchor);
    
    // Reset InputStyle
    _inputStyle = null;
    
    // Fire property change
    firePropChange("Selection", aStart + ((long)anEnd)<<32, 0);
    
    // Repaint selection and scroll to visible (after delay)
    if(isShowing()) {
        repaintSel();
        getEnv().runLater(() -> scrollSelToVisible());
    }
}

/**
 * Selects all the characters in the text editor.
 */
public void selectAll()  { setSel(0, length()); }

/**
 * Repaint the selection.
 */
protected void repaintSel()
{
    if(_sel==null) return;
    Shape shape = _sel.getPath();
    Rect rect = shape.getBounds(); rect.inset(-1);
    repaint(rect);
}

/**
 * Scrolls Selection to visible.
 */
protected void scrollSelToVisible()
{
    // Get selection rect. If empty, outset by 1. If abuts left border, make sure left border is visible.
    Rect srect = getSel().getPath().getBounds();
    if(srect.isEmpty()) srect.inset(-1,-2);
    if(srect.getX()-1<=getTextBox().getX()) { srect.setX(-getX()); srect.setWidth(10); }
    
    // If selection rect not fully contained in visible rect, scrollRectToVisible
    Rect vrect = getClipBoundsAll(); if(vrect==null || vrect.isEmpty()) return;
    if(!vrect.contains(srect)) {
        if(!srect.intersects(vrect)) srect.inset(0,-100);
        scrollToVisible(srect);
    }
}

/**
 * Returns the color of the current selection or cursor.
 */
public Color getColor()  { return getInputStyle().getColor(); }

/**
 * Sets the color of the current selection or cursor.
 */
public void setColor(Color aColor)  { setInputStyleValue(TextStyle.COLOR_KEY, aColor); }

/**
 * Returns the font of the current selection or cursor.
 */
public Font getFont()
{
    if(!isRich()) return _font!=null? _font : getDefaultFont();
    return getInputStyle().getFont();
}

/**
 * Sets the font of the current selection or cursor.
 */
public void setFont(Font aFont)
{
    setInputStyleValue(TextStyle.FONT_KEY, aFont);
    if(!isRich()) _font = aFont.equals(getDefaultFont())? null : aFont;
}

// Bogus font to work with fonts normally when not rich
Font _font;
protected void checkFont()
{
    if(isRich()) return;
    if(!getFont().equals(getInputStyle().getFont())) setInputStyleValue(TextStyle.FONT_KEY, getFont());
}

/**
 * Returns the style at given char index.
 */
public TextStyle getStyleAt(int anIndex)  { return getTextBox().getStyleAt(anIndex); }

/**
 * Returns the TextStyle applied to any input characters.
 */
public TextStyle getInputStyle()
{
    // If InputStyle has been cleared, reset from selection start
    if(_inputStyle==null)
        _inputStyle = getStyleAt(getSelStart());
    return _inputStyle;
}

/**
 * Sets the attributes that are applied to current selection or newly typed chars.
 */
public void setInputStyleValue(String aKey, Object aValue)
{
    // If selection is zero length, just modify input style
    if(isSelEmpty() && isRich())
        _inputStyle = getInputStyle().copyFor(aKey, aValue);
    
    // If selection is multiple chars, apply attribute to text and reset InputStyle
    else {
        getTextBox().setStyleValue(aKey, aValue, getSelStart(), getSelEnd()); _inputStyle = null;
        repaint();
    }
}

/**
 * Adds the given string to end of text.
 */
public void addChars(String aStr, Object ... theAttrs)
{
    int len = length();
    TextStyle style = getStyleAt(len).copyFor(theAttrs);
    replaceChars(aStr, style, len, len, true);
}

/**
 * Adds the given string with given style to text at given index.
 */
public void addChars(String aStr, TextStyle aStyle) { replaceChars(aStr, aStyle, length(), length(), true); }

/**
 * Adds the given string with given style to text at given index.
 */
public void addChars(String aStr, TextStyle aStyle, int anIndex) { replaceChars(aStr, aStyle, anIndex, anIndex, true); }

/**
 * Replaces the current selection with the given string.
 */
public void replaceChars(String aString)  { replaceChars(aString, null, getSelStart(), getSelEnd(), true);}

/**
 * Replaces the current selection with the given string.
 */
public void replaceChars(String aString, TextStyle aStyle, int aStart, int anEnd, boolean doUpdateSel)
{
    // Get string length (if no string length and no char range, just return)
    int strLen = aString!=null? aString.length() : 0; if(strLen==0 && aStart==anEnd) return;
    
    // If change is not adjacent to last change, call UndoerSaveChanges
    if((strLen>0 && aStart!=_lastReplaceIndex) || (strLen==0 && anEnd!=_lastReplaceIndex))
        undoerSaveChanges();

    // Do actual replace chars    
    TextStyle style = aStyle!=null? aStyle : aStart==getSelStart()? getInputStyle() : getStyleAt(aStart);
    getTextBox().replaceChars(aString, style, aStart, anEnd);
    
    // Update selection to be at end of new string
    if(doUpdateSel)
        setSel(aStart + strLen);

    // Otherwise, if replace was before current selection, adjust current selection
    else if(aStart<=getSelEnd()) {
        int delta = strLen - (anEnd - aStart);
        int start = getSelStart(); if(aStart<=start) start += delta;
        setSel(start, getSelEnd() + delta);
    }
    
    // Update LastReplaceIndex
    _lastReplaceIndex = aStart + strLen;
}

/**
 * Deletes the current selection.
 */
public void delete()  { delete(getSelStart(), getSelEnd(), true); }

/**
 * Deletes the given range of chars.
 */
public void delete(int aStart, int anEnd, boolean doUpdateSel) { replaceChars(null, null, aStart, anEnd, doUpdateSel); }

/**
 * Moves the selection index forward a character (or if a range is selected, moves to end of range).
 */
public void selectForward(boolean isShiftDown)
{
    // If shift is down, extend selection forward
    if(isShiftDown) {
        if(getSelAnchor()==getSelStart() && !isSelEmpty()) setSel(getSelStart()+1, getSelEnd());
        else { setSel(getSelStart(), getSelEnd()+1, getSelEnd()+1); }
    }
    
    // Set new selection
    else setSel(_sel.getCharRight());
}

/**
 * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
 */
public void selectBackward(boolean isShiftDown)
{
    // If shift is down, extend selection back
    if(isShiftDown) {
        if(getSelAnchor()==getSelEnd() && !isSelEmpty()) setSel(getSelStart(), getSelEnd()-1);
        else { setSel(getSelStart()-1, getSelEnd(), getSelStart()-1); }
    }
    
    // Set new selection
    else setSel(_sel.getCharLeft());
}

/**
 * Moves the selection index up a line, trying to preserve distance from beginning of line.
 */
public void selectUp()  { setSel(_sel.getCharUp()); }

/**
 * Moves the selection index down a line, trying preserve distance from beginning of line.
 */
public void selectDown()  { setSel(_sel.getCharDown()); }

/**
 * Moves the insertion point to the beginning of line.
 */
public void selectLineStart()  { setSel(_sel.getLineStart()); }

/**
 * Moves the insertion point to next newline or text end.
 */
public void selectLineEnd()  { setSel(_sel.getLineEnd()); }

/**
 * Deletes the character before of the insertion point.
 */
public void deleteBackward()
{
    if(!isSelEmpty()) { delete(); return; }
    RichText text = getRichText(); int end = getSelStart(), start = end - 1; if(end==0) return;
    if(text.isAfterLineEnd(end)) start = text.lastIndexOfNewline(end);
    delete(start, end, true);
}

/**
 * Deletes the character after of the insertion point.
 */
public void deleteForward()
{
    if(!isSelEmpty()) { delete(); return; }
    RichText text = getRichText(); int start = getSelStart(), end = start + 1; if(start>=length()) return;
    if(text.isLineEnd(end - 1)) end = text.indexAfterNewline(end - 1);
    delete(start, end, true);
}

/**
 * Deletes the characters from the insertion point to the end of the line.
 */
public void deleteToLineEnd()
{
    // If there is a current selection, just delete it
    RichText text = getRichText();
    if(!isSelEmpty())
        delete();
    
    // Otherwise, if at line end, delete line end
    else if(text.isLineEnd(getSelEnd()))
        delete(getSelStart(), text.indexAfterNewline(getSelStart()), true);

    // Otherwise delete up to next newline or line end
    else {
        int index = text.indexOfNewline(getSelStart());
        delete(getSelStart(), index>=0? index : length(), true);
    }
}

/**
 * Clears the text.
 */
public void clear()
{
    Undoer undoer = getUndoer(); if(undoer!=null && undoer.isEnabled()) undoer.disable(); else undoer = null;
    getRichText().clear();
    if(undoer!=null) undoer.enable();
}

/**
 * Returns the number of lines.
 */
public int getLineCount()  { return getTextBox().getLineCount(); }

/**
 * Returns the individual line at given index.
 */
public TextBoxLine getLine(int anIndex)  { return getTextBox().getLine(anIndex); }

/**
 * Returns the last line.
 */
public TextBoxLine getLineLast()  { return getTextBox().getLineLast(); }

/**
 * Returns the line for the given character index.
 */
public TextBoxLine getLineAt(int anIndex)  { return getTextBox().getLineAt(anIndex); }

/**
 * Returns the token for given character index.
 */
public TextBoxToken getTokenAt(int anIndex)  { return getTextBox().getTokenAt(anIndex); }

/**
 * Returns the char index for given point in text coordinate space.
 */
public int getCharIndex(double anX, double aY)  { return getTextBox().getCharIndex(anX, aY); }

/**
 * Returns the selection color.
 */
public Color getSelColor()  { return _selColor; }

/**
 * Paint background.
 */
protected void paintBack(Painter aPntr)
{
    // Paint background
    if(getFill()!=null) {
        aPntr.setPaint(getFill()); aPntr.fillRect(0,0,getWidth(),getHeight()); }
}

/**
 * Paint component.
 */
protected void paintFront(Painter aPntr)
{
    // Get bounds in parent
    Rect clip = aPntr.getClipBounds(); if(clip==null) clip = getBoundsInside();
    
    // If alignment not TOP_LEFT, shift text block
    double dx = ViewUtils.getAlignX(getAlign());
    double dy = ViewUtils.getAlignY(getAlign());
    if(dx!=0 || dy!=0) {
        Rect tbnds = getTextBoxBounds(); TextBox tbox = getTextBox();
        dx = tbnds.getX() + Math.round(dx*(tbnds.getWidth() - tbox.getPrefWidth()));
        dy = tbnds.getY() + Math.round(dy*(tbnds.getHeight() - tbox.getPrefHeight()));
        tbox.setX(dx); tbox.setY(dy);
    }
    
    // Paint selection
    paintSel(aPntr);
    
    // Add text
    int s = -1, e = -1;
    for(int i=0, iMax=getLineCount(); i<iMax; i++) { TextBoxLine line = getLine(i);
        if(line.getMaxY()<clip.getMinY()) continue;
        if(line.getY()>clip.getMaxY()) break;
        if(s==-1) s = i; e = i;
        for(int j=0,jMax=line.getTokenCount(); j<jMax;j++) { TextBoxToken token = line.getToken(j);
            double tx = token.getX(), tb = token.getBaseline();
            aPntr.setFont(token.getFont()); aPntr.setPaint(token.getColor());
            aPntr.drawString(token.getString(), tx, tb, token.getStyle().getCharSpacing());
            if(token.isUnderlined()) {
                aPntr.setStroke(Stroke.Stroke1); aPntr.drawLine(tx, tb+3, tx + token.getWidth(), tb+3); }
        }
    }
}

/**
 * Paints the selection.
 */
protected void paintSel(Painter aPntr)
{
    if(!isEditable()) return;
    TextSel sel = getSel();
    Shape spath = sel.getPath();
    if(sel.isEmpty()) { if((isFocused() || _downX>0) && !_hideCaret) {
        aPntr.setPaint(Color.BLACK); aPntr.setStroke(Stroke.Stroke1); aPntr.draw(spath); }}
    else { aPntr.setPaint(getSelColor()); aPntr.fill(spath); }
}

/**
 * Process event.
 */
protected void processEvent(ViewEvent anEvent)
{
    switch(anEvent.getType()) {
        case MousePressed: mousePressed(anEvent); break;
        case MouseDragged: mouseDragged(anEvent); break;
        case MouseReleased: mouseReleased(anEvent); break;
        case MouseClicked: mouseClicked(anEvent); break;
        case MouseMoved: mouseMoved(anEvent); break;
        case KeyPressed: keyPressed(anEvent); break;
        case KeyTyped: keyTyped(anEvent); break;
        case KeyReleased: keyReleased(anEvent); break;
    }
    
    // Consume all mouse events
    if(anEvent.isMouseEvent()) anEvent.consume();
}

/**
 * Handles mouse pressed.
 */
protected void mousePressed(ViewEvent anEvent)
{
    // Stop caret animation
    setCaretAnim(false);
    
    // Store the mouse down point
    _downX = anEvent.getX(); _downY = anEvent.getY();
    
    // Determine if word or paragraph selecting
    if(!anEvent.isShiftDown()) _wordSel = _pgraphSel = false;
    if(anEvent.getClickCount()==2) _wordSel = true;
    else if(anEvent.getClickCount()==3) _pgraphSel = true;
    
    // Get selected range for down point and drag point
    TextSel selection = new TextSel(_tbox, _downX, _downY, _downX, _downY, _wordSel, _pgraphSel);
    int start = selection.getStart(), end = selection.getEnd();
    
    // If shift is down, xor selection
    if(anEvent.isShiftDown()) {
        if(selection.getStart()<=getSelStart()) end = getSelEnd();
        else start = getSelStart();
    }
    
    // Set selection
    setSel(start, end);
}

/**
 * Handles mouse dragged.
 */
protected void mouseDragged(ViewEvent anEvent)
{
    // Get selected range for down point and drag point
    TextSel sel = new TextSel(_tbox, _downX, _downY, anEvent.getX(), anEvent.getY(), _wordSel, _pgraphSel);
    int start = sel.getStart(), end = sel.getEnd();
    
    // If shift is down, xor selection
    if(anEvent.isShiftDown()) {
        if(start<=getSelStart()) end = getSelEnd();
        else start = getSelStart();
    }
    
    // Set selection
    setSel(start, end);
}

/**
 * Handles mouse released.
 */
protected void mouseReleased(ViewEvent anEvent) { setCaretAnim(); _downX = _downY = 0; }

/**
 * Handles mouse clicked.
 */
protected void mouseClicked(ViewEvent anEvent)
{
    int cindex = getCharIndex(anEvent.getX(), anEvent.getY());
    TextStyle style = getStyleAt(cindex);
    if(style.getLink()!=null) openLink(style.getLink().getString());
}

/**
 * Handle MouseMoved.
 */
protected void mouseMoved(ViewEvent anEvent)
{
    int cindex = getCharIndex(anEvent.getX(), anEvent.getY());
    TextStyle style = getStyleAt(cindex);
    if(style.getLink()!=null) setCursor(Cursor.HAND);
    else showCursor();
}

/**
 * Called when a key is pressed.
 */
protected void keyPressed(ViewEvent anEvent)
{
    // Get event info
    int keyCode = anEvent.getKeyCode();
    boolean commandDown = anEvent.isShortcutDown(), controlDown = anEvent.isControlDown();
    boolean emacsDown = SnapUtils.isWindows? anEvent.isAltDown() : controlDown;
    boolean shiftDown = anEvent.isShiftDown();
    setCaretAnim(false);

    // Handle command keys
    if(commandDown) {
    
        // If shift-down, just return
        if(shiftDown && keyCode!=KeyCode.Z) return;
        
        // Handle common command keys
        switch(keyCode) {
            case KeyCode.X: cut(); anEvent.consume(); break; // Handle command-x cut
            case KeyCode.C: copy(); anEvent.consume(); break; // Handle command-c copy
            case KeyCode.V: paste(); anEvent.consume(); break; // Handle command-v paste
            case KeyCode.A: selectAll(); anEvent.consume(); break; // Handle command-a select all
            case KeyCode.Z: if(shiftDown) redo(); else undo(); anEvent.consume(); break; // Handle command-z undo
            default: return; // Any other command keys just return
        }
    }
    
    // Handle control keys (not applicable on Windows, since they are handled by command key code above)
    else if(emacsDown) {
        
        // If shift down, just return
        if(shiftDown) return;
        
        // Handle common emacs key bindings
        switch(keyCode) {
            case KeyCode.F: selectForward(false); break; // Handle control-f key forward
            case KeyCode.B: selectBackward(false); break; // Handle control-b key backward
            case KeyCode.P: selectUp(); break; // Handle control-p key up
            case KeyCode.N: selectDown(); break; // Handle control-n key down
            case KeyCode.A: selectLineStart(); break; // Handle control-a line start
            case KeyCode.E: selectLineEnd(); break; // Handle control-e line end
            case KeyCode.D: deleteForward(); break; // Handle control-d delete forward
            case KeyCode.K: deleteToLineEnd(); break; // Handle control-k delete line to end
            default: return; // Any other control keys, just return
        }
    }
    
    // Handle supported non-character keys
    else switch(keyCode) {
        case KeyCode.TAB: if(!getEventAdapter().isEnabled(Action)) { replaceChars("\t"); anEvent.consume(); } break;
        case KeyCode.ENTER:
            if(getEventAdapter().isEnabled(Action)) { selectAll(); fireActionEvent(); }
            else { replaceChars("\n"); anEvent.consume(); } break; // Handle enter
        case KeyCode.LEFT: selectBackward(shiftDown); anEvent.consume(); break; // Handle left arrow
        case KeyCode.RIGHT: selectForward(shiftDown); anEvent.consume(); break; // Handle right arrow
        case KeyCode.UP: selectUp(); anEvent.consume(); break; // Handle up arrow
        case KeyCode.DOWN: selectDown(); anEvent.consume(); break; // Handle down arrow
        case KeyCode.HOME: selectLineStart(); break; // Handle home key
        case KeyCode.END: selectLineEnd(); break; // Handle end key
        case KeyCode.BACK_SPACE: deleteBackward(); anEvent.consume(); break; // Handle Backspace key
        case KeyCode.DELETE: deleteForward(); anEvent.consume(); break; // Handle Delete key
        case KeyCode.SPACE: anEvent.consume(); break; // I have no idea why ScrollPane scrolls if this isn't called
        default: return; // Any other non-character key, just return
    }
    
    // Consume the event
    //anEvent.consume();
}

/**
 * Called when a key is typed.
 */
protected void keyTyped(ViewEvent anEvent)
{
    // Get event info
    String keyChars = anEvent.getKeyString();
    char keyChar = keyChars.length()>0? keyChars.charAt(0) : 0;
    boolean charDefined = keyChar!=KeyCode.CHAR_UNDEFINED && !Character.isISOControl(keyChar);
    boolean commandDown = anEvent.isShortcutDown(), controlDown = anEvent.isControlDown();
    boolean emacsDown = SnapUtils.isWindows? anEvent.isAltDown() : controlDown;
    
    // If actual text entered, replace
    if(charDefined && !commandDown && !controlDown && !emacsDown) {
        replaceChars(keyChars);
        hideCursor(); //anEvent.consume();
    }
}

/**
 * Called when a key is released.
 */
protected void keyReleased(ViewEvent anEvent)  { setCaretAnim(); }

/**
 * Shows the cursor.
 */
public void showCursor()  { setCursor(Cursor.TEXT); }

/**
 * Hides the cursor.
 */
public void hideCursor()  { setCursor(Cursor.NONE); }

/**
 * Returns whether anim is needed.
 */
private boolean isCaretAnimNeeded()  { return isFocused() && getSel().isEmpty() && isShowing(); }

/**
 * Sets the caret animation to whether it's needed.
 */
private void setCaretAnim()  { setCaretAnim(isCaretAnimNeeded()); }

/**
 * Returns whether ProgressBar is animating.
 */
private boolean isCaretAnim()  { return _caretTimer!=null; }

/**
 * Sets anim.
 */
private void setCaretAnim(boolean aValue)
{
    if(aValue==isCaretAnim()) return;
    if(aValue) {
        _caretTimer = new ViewTimer(500, t -> { _hideCaret = !_hideCaret; repaintSel(); });
        _caretTimer.start();
    }
    else { _caretTimer.stop(); _caretTimer = null; _hideCaret = false; repaintSel(); }
}

/**
 * Copies the current selection onto the clip board, then deletes the current selection.
 */
public void cut()  { copy(); delete(); }

/**
 * Copies the current selection onto the clipboard.
 */
public void copy()
{
    // If valid selection, get text for selection and add to clipboard
    if(!isSelEmpty()) {
        String string = getSel().getString();
        Clipboard cb = Clipboard.get(); //Map content = new HashMap(); content.put(DataFormat.PLAIN_TEXT, string);
        cb.setContent(string);
    }
}

/**
 * Pasts the current clipboard data over the current selection.
 */
public void paste()
{
    // Clear last undo set so paste doesn't get lumped in to coalescing
    undoerSaveChanges();
    
    // Get system clipboard and its contents (return if null)
    Clipboard cb = Clipboard.get();
    
    // If contents contains Text, get content bytes, unarchive Text s and replace current selection
    if(cb.hasString()) { String string = cb.getString();
        replaceChars(string); }
}

/**
 * Opens a given link.
 */
protected void openLink(String aLink)  { System.out.println("Open Link: " + aLink); }

/**
 * Returns the undoer.
 */
public Undoer getUndoer()  { return _undoer; }

/**
 * Called to undo the last edit operation in the editor.
 */
public boolean undo()
{
    undoerSaveChanges(); boolean b = _undoer.undo()!=null; if(!b) ViewUtils.beep(); return b;
}

/**
 * Called to redo the last undo operation in the editor.
 */
public boolean redo()
{
    undoerSaveChanges(); boolean b = _undoer.redo()!=null; if(!b) ViewUtils.beep(); return b;
}

/**
 * Adds a property change to undoer.
 */
protected void undoerAddPropertyChange(PropChange anEvent)
{
    // Get undoer (just return if null or disabled)
    Undoer undoer = getUndoer(); if(undoer==null || !undoer.isEnabled()) return;
    
    // Get ActiveUndoSet - if no previous changes, set UndoSelection
    UndoSet activeUndoSet = undoer.getActiveUndoSet();
    if(activeUndoSet.getChangeCount()==0)
        activeUndoSet.setUndoSelection(getUndoSelection());
    
    // Add property
    undoer.addPropertyChange(anEvent);
}

/**
 * Saves changes to undoer.
 */
public void undoerSaveChanges()
{
    Undoer undoer = getUndoer(); if(undoer==null) return;
    UndoSet activeUndoSet = undoer.getActiveUndoSet(); activeUndoSet.setRedoSelection(getUndoSelection());
    undoer.saveChanges();
}

/**
 * Returns a selection object for undoer.
 */
protected Object getUndoSelection()  { return new UndoTextSel(); }

/**
 * A class to act as text selection.
 */
public class UndoTextSel implements Undoer.Selection {
    public int start = getSelStart(), end = getSelEnd();  // Use ivars to avoid min()
    public void setSelection()  { TextViewBase.this.setSel(start, end); }
    public boolean equals(Object anObj)  { UndoTextSel other = (UndoTextSel)anObj;
        return start==other.start && end==other.end; }
    public int hashCode()  { return start + end; }
}

/**
 * Called when characters where added, updated or deleted.
 */
public void propertyChange(PropChange anEvent)
{
    // Add property change
    undoerAddPropertyChange(anEvent);

    // Forward on to listeners
    firePropChange(anEvent);
    
    // Notify text did change
    textDidChange();
}

/**
 * Called when text changes in some way.
 */
protected void textDidChange()
{
    relayoutParent(); repaint();
}

/**
 * Returns the width needed to display all characters.
 */
protected double getPrefWidthImpl(double aH)
{
    checkFont();
    Insets ins = getInsetsAll();
    return ins.left + getTextBox().getPrefWidth() + ins.right;
}

/**
 * Returns the height needed to display all characters.
 */
protected double getPrefHeightImpl(double aW)
{
    checkFont();
    Insets ins = getInsetsAll();
    return ins.top + getTextBox().getPrefHeight() + ins.bottom;
}

/**
 * Layout children.
 */
protected void layoutChildren()  { checkFont(); }

/**
 * Override to update getTextBlock.Rect.
 */
public void setWidth(double aWidth)  { super.setWidth(aWidth); setTextBoxBounds(); }

/**
 * Override to update getTextBlock.Rect.
 */
public void setHeight(double aHeight)  { super.setHeight(aHeight); setTextBoxBounds(); }

/**
 * Sets the Text.Rect from text area.
 */
protected Rect getTextBoxBounds()
{
    Insets ins = getInsetsAll(); boolean wrap = isWrapText();
    double x = ins.left, w = getWidth() - x - ins.right;
    double y = ins.top, h = getHeight() - y - ins.bottom;
    return new Rect(x,y,w,h);
}

/**
 * Sets the Text.Rect from text area.
 */
protected void setTextBoxBounds()  { getTextBox().setBounds(getTextBoxBounds()); }

/**
 * Override to check caret animation and scrollSelToVisible when showing.
 */
protected void setShowing(boolean aValue)
{
    if(aValue==isShowing()) return; super.setShowing(aValue);
    if(isFocused()) setCaretAnim();
    if(aValue && getSelStart()!=0)
        getEnv().runDelayed(() -> scrollSelToVisible(), 200, true);
}

/**
 * Override to check caret animation and repaint.
 */
protected void setFocused(boolean aValue)
{
    // Do normal version
    if(aValue==isFocused()) return; super.setFocused(aValue);
    
    // Toggle caret animation and repaint
    if(!aValue || _downX==0) setCaretAnim();
    repaint();
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Rich, Editable, WrapText
    if(isRich()) e.add("Rich", true);
    if(!isEditable()) e.add("Editable", false);
    if(isWrapText()) e.add("WrapText", true);

    // If RichText, archive rich text
    if(isRich()) {
        e.removeElement("font");
        XMLElement rtxml = anArchiver.toXML(getRichText()); rtxml.setName("RichText");
        if(rtxml.size()>0) e.add(rtxml); //for(int i=0, iMax=rtxml.size(); i<iMax; i++) e.add(rtxml.get(i));
    }

    // Otherwise, archive text string
    else if(getText()!=null && getText().length()>0) e.add("text", getText());
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Hack for archived rich stuff
    XMLElement rtxml = anElement.get("RichText");
    if(rtxml==null && anElement.get("string")!=null) rtxml = anElement;
    boolean rich = rtxml!=null;
    
    // Unarchive Rich, Editable, WrapText
    if(anElement.getAttribute("Rich")!=null || rich) setRich(anElement.getAttributeBoolValue("Rich") || rich);
    if(anElement.getAttribute("Editable")!=null) setEditable(anElement.getAttributeBoolValue("editable"));
    if(anElement.getAttribute("WrapText")!=null) setWrapText(anElement.getAttributeBoolValue("WrapText"));

    // Unarchive margin, valign (should go soon)
    if(anElement.hasAttribute("margin")) setPadding(Insets.get(anElement.getAttributeValue("margin")));
    if(anElement.hasAttribute("valign")) {
        String align = anElement.getAttributeValue("valign");
        if(align.equals("top")) setAlign(Pos.get(HPos.LEFT,VPos.TOP));
        else if(align.equals("middle")) setAlign(Pos.get(HPos.LEFT,VPos.CENTER));
        else if(align.equals("bottom")) setAlign(Pos.get(HPos.LEFT,VPos.BOTTOM));
    }
    
    // If Rich, unarchive rich text
    if(isRich()) {
        getUndoer().disable();
        if(rtxml!=null) getRichText().fromXML(anArchiver, rtxml);
        getUndoer().enable();
    }

    // Otherwise unarchive text. Text can be "text" or "value" attribute, or as content (CDATA or otherwise)
    else {
        String str = anElement.getAttributeValue("text",  anElement.getAttributeValue("value", anElement.getValue()));
        if(str!=null && str.length()>0)
            setText(str);
    }
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getText(); if(str.length()>40) str = str.substring(0,40) + "...";
    return getClass().getSimpleName() + ": " + str;
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "Text"; }

}