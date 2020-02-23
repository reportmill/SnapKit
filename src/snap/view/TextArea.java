/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.text.*;
import snap.util.*;
import snap.web.*;

/**
 * A view subclass for displaying and editing large blocks of text and rich text using a TextBox with RichText.
 */
public class TextArea extends View {

    // The text being edited
    TextBox _tbox;
    
    // Whether text is editable
    boolean               _editable;
    
    // Whether text should wrap lines that overrun bounds
    boolean               _wrapLines;
    
    // The selection char indexes
    int                   _selIndex, _selAnchor, _selStart, _selEnd;
    
    // The text selection
    TextSel               _sel;
    
    // Whether the editor is word selecting (double click) or paragraph selecting (triple click)
    boolean               _wordSel, _pgraphSel;
    
    // The current TextStyle for the cursor or selection
    TextStyle             _selStyle;
    
    // The Selection color
    Color                 _selColor = new Color(181, 214, 254, 255);
    
    // The text pane undoer
    Undoer                _undoer = new Undoer();
    
    // The index of the last replace so we can commit undo changes if not adjacent
    int                   _lastReplaceIndex;
    
    // The mouse down point
    double                _downX, _downY;
    
    // The animator for caret blinking
    ViewTimer             _caretTimer;
    
    // Whether to show text insertion point caret
    boolean               _showCaret;
    
    // Whether to send action on enter key press
    boolean               _fireActionOnEnterKey;

    // Whether to send action on focus lost (if content changed)
    boolean               _fireActionOnFocusLost;
    
    // The content on focus gained
    String                _focusGainedText;
    
    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String WrapLines_Prop = "WrapLines";
    public static final String FireActionOnEnterKey_Prop = "FireActionOnEnterKey";
    public static final String FireActionOnFocusLost_Prop = "FireActionOnFocusLost";
    public static final String Selection_Prop = "Selection";

/**
 * Creates a new TextArea.
 */
public TextArea()
{
    // Create/set default TextBox
    _tbox = createTextBox();
    _tbox.getRichText().addPropChangeListener(_richTextPropLsnr);
    
    // Configure
    setPlainText(true);
    setFont(getDefaultFont());
    setFocusPainted(false);
}

/**
 * Returns the text that is being edited.
 */
public TextBox getTextBox()  { return _tbox; }

/**
 * Creates a new TextBox.
 */
protected TextBox createTextBox()  { return new TextBox(); }

/**
 * Returns the rich text.
 */
public RichText getRichText()  { return getTextBox().getRichText(); }

/**
 * Sets the RichText.
 */
public void setRichText(RichText aRichText)
{
    // If already set, just return
    RichText old = getRichText(); if(aRichText==old) return;
    
    // Add/remove PropChangeListener
    if(old!=null) old.removePropChangeListener(_richTextPropLsnr);
    getTextBox().setText(aRichText);
    if(aRichText!=null) aRichText.addPropChangeListener(_richTextPropLsnr);
    
    // Reset selection
    if(getSelStart()!=0 || !isSelEmpty()) setSel(0);
    repaint();
}

/**
 * Returns the source of current content (URL, File, String path, etc.)
 */
public Object getSource()  { return getTextBox().getSource(); }

/**
 * Set the source for TextComponent text.
 */
public void setSource(Object aSource)
{
    // Set source and notify textDidChange
    getTextBox().setSource(aSource);
    textDidChange();
    
    // Reset selection (to line end if single-line, otherwise text start)
    int sindex = getTextBox().getLineCount()==1 && length()<40? length() : 0;
    setSel(sindex);
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
    
    // Set string and notify textDidChange
    getTextBox().setString(aString);
    textDidChange();
    
    // Reset selection (to line end if single-line, otherwise text start)
    int sindex = getTextBox().getLineCount()==1 && length()<40? length() : 0;
    setSel(sindex);
}

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
    
    // If editable, set some related attributes
    if(aValue) {
        enableEvents(MouseEvents); enableEvents(KeyEvents);
        setFocusable(true); setFocusWhenPressed(true); setFocusKeysEnabled(false);
    }
    
    else {
        disableEvents(MouseEvents); disableEvents(KeyEvents);
        setFocusable(false); setFocusWhenPressed(false); setFocusKeysEnabled(false);
    }
}

/**
 * Returns whether to wrap lines that overrun bounds.
 */
public boolean isWrapLines()  { return _wrapLines; }

/**
 * Sets whether to wrap lines that overrun bounds.
 */
public void setWrapLines(boolean aValue)
{
    if(aValue==_wrapLines) return;
    firePropChange(WrapLines_Prop, _wrapLines, _wrapLines=aValue);
    getTextBox().setWrapLines(aValue);
}

/**
 * Returns whether text supports multiple styles.
 */
public boolean isRich()  { return !isPlainText(); }

/**
 * Returns whether text is plain text (has only one font, color. etc.).
 */
public boolean isPlainText()  { return getRichText().isPlainText(); }

/**
 * Sets whether text is plain text (has only one font, color. etc.).
 */
public void setPlainText(boolean aValue)  { getRichText().setPlainText(aValue); }

/**
 * Returns the default style for text.
 */
public TextStyle getDefaultStyle()  { return getRichText().getDefaultStyle(); }

/**
 * Sets the default style.
 */
public void setDefaultStyle(TextStyle aStyle)  { getRichText().setDefaultStyle(aStyle); }

/**
 * Returns the default line style for text.
 */
public TextLineStyle getDefaultLineStyle()  { return getRichText().getDefaultLineStyle(); }

/**
 * Sets the default line style.
 */
public void setDefaultLineStyle(TextLineStyle aLineStyle)  { getRichText().setDefaultLineStyle(aLineStyle); }

/**
 * Returns whether text view fires action on enter key press.
 */
public boolean isFireActionOnEnterKey()  { return _fireActionOnEnterKey; }

/**
 * Sets whether text area sends action on enter key press.
 */
public void setFireActionOnEnterKey(boolean aValue)
{
    if(aValue==_fireActionOnEnterKey) return;
    if(aValue) enableEvents(Action);
    else getEventAdapter().disableEvents(this, Action);
    firePropChange(FireActionOnEnterKey_Prop, _fireActionOnEnterKey, _fireActionOnEnterKey = aValue);
}

/**
 * Returns whether text view fires action on focus lost (if text changed).
 */
public boolean isFireActionOnFocusLost()  { return _fireActionOnFocusLost; }

/**
 * Sets whether text area sends action on focus lost (if text changed).
 */
public void setFireActionOnFocusLost(boolean aValue)
{
    if(aValue==_fireActionOnFocusLost) return;
    firePropChange(FireActionOnFocusLost_Prop, _fireActionOnFocusLost, _fireActionOnFocusLost = aValue);
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
 * Returns whether the selection is empty.
 */
public boolean isSelEmpty()  { return getSelStart()==getSelEnd(); }

/**
 * Returns the initial character index of the selection (usually SelStart).
 */
public int getSelAnchor()  { return Math.min(_selAnchor, length()); }

/**
 * Returns the final character index of the selection (usually SelEnd).
 */
public int getSelIndex()  { return Math.min(_selIndex, length()); }

/**
 * Returns the character index of the start of the text selection.
 */
public int getSelStart()  { return Math.min(_selStart, length()); }

/**
 * Returns the character index of the end of the text selection.
 */
public int getSelEnd()  { return Math.min(_selEnd, length()); }

/**
 * Returns the text selection.
 */
public TextSel getSel()  { return _sel!=null? _sel : (_sel=new TextSel(_tbox, _selAnchor, _selIndex)); }

/**
 * Sets the character index of the text cursor.
 */
public void setSel(int newStartEnd)  { setSel(newStartEnd, newStartEnd); }

/**
 * Sets the character index of the start and end of the text selection.
 */
public void setSel(int aStart, int aEnd)
{
    // If already set, just return
    int len = length(), anchor = Math.min(aStart,len), index = Math.min(aEnd,len);
    if(anchor==_selAnchor && index==_selIndex) return;
    
    // Repaint old selection
    if(isShowing()) repaintSel();
    
    // Set new values
    _selAnchor = aStart; _selIndex = aEnd;
    _selStart = Math.min(aStart, aEnd); _selEnd = Math.max(aStart, aEnd);
    
    // Fire selection property change and clear selection
    firePropChange(Selection_Prop, _sel, _sel=null);

    // Reset SelStyle
    _selStyle = null;
    
    // Repaint selection and scroll to visible (after delay)
    if(isShowing()) {
        repaintSel();
        setCaretAnim();
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
    Shape shape = getSel().getPath();
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
    Rect vrect = getClipAllBounds(); if(vrect==null || vrect.isEmpty()) return;
    if(!vrect.contains(srect)) {
        if(!srect.intersects(vrect)) srect.inset(0,-100);
        scrollToVisible(srect);
    }
}

/**
 * Returns the font of the current selection or cursor.
 */
public Font getFont()
{
    if(isRich()) return getSelStyle().getFont();
    return super.getFont();
}

/**
 * Sets the font of the current selection or cursor.
 */
public void setFont(Font aFont)
{
    super.setFont(aFont);
    if(aFont!=null) setSelStyleValue(TextStyle.FONT_KEY, aFont);
}

/**
 * Returns the color of the current selection or cursor.
 */
public Paint getTextFill()  { return getSelStyle().getColor(); }

/**
 * Sets the color of the current selection or cursor.
 */
public void setTextFill(Paint aColor)
{
    setSelStyleValue(TextStyle.COLOR_KEY, aColor instanceof Color? aColor : null);
}

/**
 * Returns whether TextView is underlined.
 */
public boolean isUnderlined()  { return getSelStyle().isUnderlined(); }

/**
 * Sets whether TextView is underlined.
 */
public void setUnderlined(boolean aValue)  { setSelStyleValue(TextStyle.UNDERLINE_KEY, aValue? 1 : 0); }

/**
 * Returns the text line alignment.
 */
public HPos getLineAlign()  { return getLineStyle().getAlign(); }

/**
 * Sets the text line alignment.
 */
public void setLineAlign(HPos anAlign)  { setLineStyleValue(TextLineStyle.ALIGN_KEY, anAlign); }

/**
 * Returns whether the text line justifies text.
 */
public boolean isLineJustify()  { return getLineStyle().isJustify(); }

/**
 * Sets whether the text line justifies text.
 */
public void setLineJustify(boolean aValue)  { setLineStyleValue(TextLineStyle.JUSTIFY_KEY, aValue); }

/**
 * Returns the style at given char index.
 */
public TextStyle getStyleAt(int anIndex)  { return getRichText().getStyleAt(anIndex); }

/**
 * Returns the TextStyle for the current selection and/or input characters.
 */
public TextStyle getSelStyle()  { return _selStyle!=null? _selStyle : (_selStyle=getStyleAt(getSelStart())); }

/**
 * Sets the attributes that are applied to current selection or newly typed chars.
 */
public void setSelStyleValue(String aKey, Object aValue)
{
    // If selection is zero length, just modify input style
    if(isSelEmpty() && isRich())
        _selStyle = getSelStyle().copyFor(aKey, aValue);
    
    // If selection is multiple chars, apply attribute to text and reset SelStyle
    else {
        getRichText().setStyleValue(aKey, aValue, getSelStart(), getSelEnd()); _selStyle = null;
        repaint();
    }
}

/**
 * Returns the TextLineStyle for currently selection.
 */
public TextLineStyle getLineStyle()  { return getRichText().getLineStyleAt(getSelStart()); }

/**
 * Sets the line attributes that are applied to current selection or newly typed chars.
 */
public void setLineStyleValue(String aKey, Object aValue)
{
    getRichText().setLineStyleValue(aKey, aValue, getSelStart(), getSelEnd());
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
 * Deletes the current selection.
 */
public void delete()  { delete(getSelStart(), getSelEnd(), true); }

/**
 * Deletes the given range of chars.
 */
public void delete(int aStart, int anEnd, boolean doUpdateSel) { replaceChars(null, null, aStart, anEnd, doUpdateSel); }

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
    TextStyle style = aStyle!=null? aStyle : aStart==getSelStart()? getSelStyle() : getStyleAt(aStart);
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
 * Moves the selection index forward a character (or if a range is selected, moves to end of range).
 */
public void selectForward(boolean isShiftDown)
{
    // If shift is down, extend selection forward
    if(isShiftDown) {
        if(getSelAnchor()==getSelStart() && !isSelEmpty()) setSel(getSelStart()+1, getSelEnd());
        else { setSel(getSelStart(), getSelEnd()+1); }
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
        else { setSel(getSelEnd(), getSelStart()-1); }
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
 * Paint text.
 */
protected void paintFront(Painter aPntr)
{
    // Get bounds in parent
    Rect clip = aPntr.getClipBounds(); if(clip==null) clip = getBoundsLocal();
    
    // If alignment not TOP_LEFT, shift text block
    /*double dx = ViewUtils.getAlignX(getAlign()), dy = ViewUtils.getAlignY(getAlign());
    if(dx!=0 || dy!=0) {
        Rect tbnds = getTextBoxBounds(); TextBox tbox = getTextBox();
        dx = tbnds.getX() + Math.round(dx*(tbnds.getWidth() - tbox.getPrefWidth(-1)));
        dy = tbnds.getY() + Math.round(dy*(tbnds.getHeight() - tbox.getPrefHeight(tbox.getWidth())));
        tbox.setX(dx); tbox.setY(dy); }*/
    
    // Paint selection
    paintSel(aPntr);
    
    // Paint TextBox
    getTextBox().paint(aPntr);
}

/**
 * Paints the selection.
 */
protected void paintSel(Painter aPntr)
{
    if(!isEditable()) return;
    TextSel sel = getSel();
    Shape spath = sel.getPath();
    if(sel.isEmpty()) { if(_showCaret) {
        aPntr.setPaint(Color.BLACK); aPntr.setStroke(Stroke.Stroke1); aPntr.draw(spath); }}
    else { aPntr.setPaint(getSelColor()); aPntr.fill(spath); }
}

/**
 * Process event.
 */
protected void processEvent(ViewEvent anEvent)
{
    switch(anEvent.getType()) {
        case MousePress: mousePressed(anEvent); break;
        case MouseDrag: mouseDragged(anEvent); break;
        case MouseRelease: mouseReleased(anEvent); break;
        case MouseMove: mouseMoved(anEvent); break;
        case KeyPress: keyPressed(anEvent); break;
        case KeyType: keyTyped(anEvent); break;
        case KeyRelease: keyReleased(anEvent); break;
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
    TextSel sel = new TextSel(_tbox, _downX, _downY, _downX, _downY, _wordSel, _pgraphSel);
    int anchor = sel.getAnchor(), index = sel.getIndex();
    
    // If shift is down, xor selection
    if(anEvent.isShiftDown()) {
        anchor = Math.min(getSelStart(), sel.getStart());
        index = Math.max(getSelEnd(), sel.getEnd());
    }
    
    // Set selection
    setSel(anchor, index);
}

/**
 * Handles mouse dragged.
 */
protected void mouseDragged(ViewEvent anEvent)
{
    // Get selected range for down point and drag point
    TextSel sel = new TextSel(_tbox, _downX, _downY, anEvent.getX(), anEvent.getY(), _wordSel, _pgraphSel);
    int anchor = sel.getAnchor(), index = sel.getIndex();
    
    // If shift is down, xor selection
    if(anEvent.isShiftDown()) {
        anchor = Math.min(getSelStart(), sel.getStart());
        index = Math.max(getSelEnd(), sel.getEnd());
    }
    
    // Set selection
    setSel(anchor, index);
}

/**
 * Handles mouse released.
 */
protected void mouseReleased(ViewEvent anEvent)
{
    setCaretAnim(); _downX = _downY = 0;

    if(anEvent.isMouseClick()) {
        int cindex = getCharIndex(anEvent.getX(), anEvent.getY());
        TextStyle style = getStyleAt(cindex);
        if(style.getLink()!=null) openLink(style.getLink().getString());
    }
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
    setCaretAnim(false); _showCaret = true;

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
        case KeyCode.TAB: replaceChars("\t"); anEvent.consume(); break;
        case KeyCode.ENTER:
            if(isFireActionOnEnterKey()) { selectAll(); fireActionEvent(anEvent); }
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
public boolean isCaretAnim()  { return _caretTimer!=null; }

/**
 * Sets anim.
 */
public void setCaretAnim(boolean aValue)
{
    if(aValue==isCaretAnim()) return;
    if(aValue) {
        _caretTimer = new ViewTimer(500, t -> { _showCaret = !_showCaret; repaintSel(); });
        _caretTimer.start(); _showCaret = true; repaintSel();
    }
    else { _caretTimer.stop(); _caretTimer = null; _showCaret = false; repaintSel(); }
}

/**
 * Returns the font scale of the text box.
 */
public double getFontScale()  { return getTextBox().getFontScale(); }

/**
 * Sets the font scale of the text box.
 */
public void setFontScale(double aValue)
{
    getTextBox().setFontScale(aValue);
    relayoutParent();
}

/**
 * Scales font sizes of all text in TextBox to fit in bounds by finding/setting FontScale.
 */
public void scaleTextToFit()  { getTextBox().scaleTextToFit(); relayoutParent(); }

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
        String str = getSel().getString();
        Clipboard cboard = Clipboard.getCleared();
        cboard.addData(str);
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
    String pname = anEvent.getPropertyName();
    
    // If PlainText Style_Prop or LineStyle_Prop, just return
    if(isPlainText() && (pname==RichText.Style_Prop || pname==RichText.LineStyle_Prop))
        return;
    
    // Get ActiveUndoSet - if no previous changes, set UndoSelection
    UndoSet activeUndoSet = undoer.getActiveUndoSet();
    if(activeUndoSet.getChangeCount()==0)
        activeUndoSet.setUndoSelection(getUndoSelection());
    
    // Add property
    undoer.addPropChange(anEvent);
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
    public void setSelection()  { TextArea.this.setSel(start, end); }
    public boolean equals(Object anObj)  { UndoTextSel other = (UndoTextSel)anObj;
        return start==other.start && end==other.end; }
    public int hashCode()  { return start + end; }
}

// The PropChangeListener to catch RichText PropChanges.
private PropChangeListener _richTextPropLsnr = pce -> richTextPropChange(pce);

/**
 * Called when RichText changes (chars added, updated or deleted).
 */
protected void richTextPropChange(PropChange aPC)
{
    // Add property change
    undoerAddPropertyChange(aPC);

    // Forward on to listeners
    firePropChange(aPC);
    
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
 * Overrride to return Arial 11.
 */
public Font getDefaultFont()  { return Font.Arial11; }

/**
 * Overrride to return 2,2,2,2.
 */
public Insets getDefaultPadding()  { return _def; } static Insets _def = new Insets(2);

/**
 * Override to return white.
 */
public Paint getDefaultFill()  { return isEditable()? Color.WHITE : null; }

/**
 * Returns the width needed to display all characters.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    double h = aH>=0? (aH - ins.top - ins.bottom) : aH;
    double pw = getTextBox().getPrefWidth(h);
    return ins.left + pw + ins.right;
}

/**
 * Returns the height needed to display all characters.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    double w = aW>=0? (aW - ins.left - ins.right) : aW;
    double ph = getTextBox().getPrefHeight(w);
    return ins.top + ph + ins.bottom;
}

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
    Insets ins = getInsetsAll();
    double x = ins.left, w = getWidth() - x - ins.right;
    double y = ins.top, h = getHeight() - y - ins.bottom;
    return new Rect(x,y,w,h);
}

/**
 * Sets the Text.Rect from text area.
 */
protected void setTextBoxBounds()  { getTextBox().setBounds(getTextBoxBounds()); }

/**
 * Override to update font.
 */
protected void setParent(ParentView aPar)
{
    // Do normal version
    super.setParent(aPar);
    
    // If PlainText, update to to parent (should probably watch parent Font_Prop change as well)
    if(isPlainText() && !isFontSet() && !getFont().equals(getSelStyle().getFont()))
        setSelStyleValue(TextStyle.FONT_KEY, getFont());
}

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
 * Override to forward to text box.
 */
public void setAlign(Pos aPos)
{
    // Do normal version
    super.setAlign(aPos);
    
    // Push align to TextBox via DefaultLineStyle.Aign (X) and TextBox align Y 
    TextLineStyle lstyle = getDefaultLineStyle().copyFor(TextLineStyle.ALIGN_KEY, aPos.getHPos());
    setDefaultLineStyle(lstyle);
    getTextBox().setAlignY(aPos.getVPos());
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

    // Handle FireActionOnFocusLost
    if(_fireActionOnFocusLost) {
        
        // If focus gained, set FocusedGainedValue and select all (if not from mouse press)
        if(aValue) {
            _focusGainedText = getText();
            if(!ViewUtils.isMouseDown()) selectAll();
        }
        
        // If focus lost and FocusGainedVal changed, fire action
        else if(!SnapUtils.equals(_focusGainedText, getText()))
            fireActionEvent(null);
    }
}

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return "Text"; }

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getText(); if(str.length()>40) str = str.substring(0,40) + "...";
    return getClass().getSimpleName() + ": " + str;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXML(anArchiver);
    
    // Archive Rich, Editable, WrapLines
    if(isRich()) e.add("Rich", true);
    if(!isEditable()) e.add("Editable", false);
    if(isWrapLines()) e.add(WrapLines_Prop, true);

    // If RichText, archive rich text
    if(isRich()) {
        e.removeElement("font");
        XMLElement rtxml = anArchiver.toXML(getRichText()); rtxml.setName("RichText");
        if(rtxml.size()>0) e.add(rtxml); //for(int i=0, iMax=rtxml.size(); i<iMax; i++) e.add(rtxml.get(i));
    }

    // Otherwise, archive text string
    else if(getText()!=null && getText().length()>0) e.add("text", getText());
    
    // Archive FireActionOnEnterKey, FireActionOnFocusLost
    if(isFireActionOnEnterKey()) e.add(FireActionOnEnterKey_Prop, true);
    if(isFireActionOnFocusLost()) e.add(FireActionOnFocusLost_Prop, true);
    return e;
}

/**
 * XML unarchival.
 */
public TextArea fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXML(anArchiver, anElement);
    
    // Hack for archived rich stuff
    XMLElement rtxml = anElement.get("RichText");
    if(rtxml==null && anElement.get("string")!=null) rtxml = anElement;
    if(rtxml!=null) setPlainText(false);
    
    // Unarchive Rich, Editable, WrapLines
    if(anElement.hasAttribute("Rich")) setPlainText(!anElement.getAttributeBoolValue("Rich"));
    if(anElement.hasAttribute("Editable")) setEditable(anElement.getAttributeBoolValue("Editable"));
    if(anElement.hasAttribute(WrapLines_Prop)) setWrapLines(anElement.getAttributeBoolValue(WrapLines_Prop));
    if(anElement.hasAttribute("WrapText")) setWrapLines(anElement.getAttributeBoolValue("WrapText"));

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
    
    // Unarchive FireActionOnEnterKey, FireActionOnFocusLost
    if(anElement.hasAttribute(FireActionOnEnterKey_Prop))
        setFireActionOnEnterKey(anElement.getAttributeBoolValue(FireActionOnEnterKey_Prop, true));
    if(anElement.hasAttribute(FireActionOnFocusLost_Prop))
        setFireActionOnFocusLost(anElement.getAttributeBoolValue(FireActionOnFocusLost_Prop, true));
    return this;
}

}