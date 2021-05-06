/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.gfx.*;
import snap.util.*;

/**
 * An view subclass for editing a single line of text.
 */
public class TextField extends ParentView {
    
    // The StringBuffer to hold text
    private StringBuffer  _sb = new StringBuffer();
    
    // The column count to be used for preferred width (if set)
    private int  _colCount = 12;
    
    // The paint for the text
    private Paint  _textFill = Color.BLACK;
    
    // A label in the background for promt text and/or in text controls
    private Label  _label;
    
    // The string to show when textfield is empty
    private String  _promptText;
    
    // The radius of border
    private double  _rad = 4;
    
    // The selection start/end
    private int  _selStart, _selEnd;
    
    // Whether the editor is word selecting (double click) or paragraph selecting (triple click)
    private boolean  _wordSel, _pgraphSel;
    
    // Whether text field is showing a completion selection
    private boolean  _compSel;
    
    // The mouse down point
    private double  _downX, _downY;
    
    // The animator for caret blinking
    private ViewTimer  _caretTimer;
    
    // Whether to hide caret
    private boolean  _hideCaret;
    
    // The value of text on focus gained
    protected String  _focusGainedText;
    
    // Whether text has been edited since last focus
    private boolean  _edited;

    // Constants for properties
    public static final String ColCount_Prop = "ColCount";
    public static final String Edited_Prop = "Edited";
    public static final String PromptText_Prop = "PromptText";
    public static final String Radius_Prop = "Radius";
    public static final String Sel_Prop = "Selection";
    public static final String TextFill_Prop = "TextFill";
    
    // The color of the border when focused
    static Color    SELECTION_COLOR = new Color(181, 214, 254, 255);
    
    /**
     * Creates a new TextField.
     */
    public TextField()
    {
        setFill(Color.WHITE);
        setBorder(getDefaultBorder());
        enableEvents(Action);
        enableEvents(MouseEvents);
        enableEvents(KeyEvents);
        setFocusable(true);
        setFocusWhenPressed(true);

        // Configure label and set
        _label = new Label();
        _label.setPadding(0,0,0,0);
        _label.setPickable(false);
        addChild(_label);
    }

    /**
     * Returns the text fill.
     */
    public Paint getTextFill()  { return _textFill; }

    /**
     * Sets the text fill.
     */
    public void setTextFill(Paint aPaint)
    {
        if (SnapUtils.equals(aPaint, _textFill)) return;
        firePropChange(TextFill_Prop, _textFill, _textFill = aPaint);
        repaint();
    }

    /**
     * Returns the column count.
     */
    public int getColCount()  { return _colCount; }

    /**
     * Sets the column count.
     */
    public void setColCount(int aValue)
    {
        firePropChange(ColCount_Prop, _colCount, _colCount=aValue);
        relayoutParent();
    }

    /**
     * Returns the total column width.
     */
    double getTotalColWidth()  { return Math.ceil(_colCount*getFont().charAdvance('X')); }

    /**
     * Returns the prompt text.
     */
    public String getPromptText()  { return _promptText; }

    /**
     * Sets the prompt text.
     */
    public void setPromptText(String aStr)
    {
        if (SnapUtils.equals(aStr, _promptText)) return;
        _label.setText(aStr);
        _label.setTextFill(Color.LIGHTGRAY);
        firePropChange(PromptText_Prop, _promptText, _promptText = aStr);
    }

    /**
     * Returns the label in the background.
     */
    public Label getLabel()  { return _label; }

    /**
     * Returns the rounding radius.
     */
    public double getRadius()  { return _rad; }

    /**
     * Sets the rounding radius.
     */
    public void setRadius(double aValue)
    {
        if (aValue==getRadius()) return;
        firePropChange(Radius_Prop, _rad, _rad = aValue);
    }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

    /**
     * Returns the default border.
     */
    public Border getDefaultBorder()
    {
        if (_defBorder!=null) return _defBorder;
        Border bdr = Border.createLineBorder(Color.LIGHTGRAY, 1);
        bdr = bdr.copyForInsets(Insets.EMPTY);
        return _defBorder = bdr;
    }
    private static Border _defBorder;

    /**
     * Returns the padding default.
     */
    public Insets getDefaultPadding()  { return _def; }
    static Insets _def = new Insets(2,2,2,5);

    /**
     * Override to return white.
     */
    public Paint getDefaultFill()  { return Color.WHITE; }

    /**
     * Returns the text width.
     */
    public double getTextWidth()
    {
        return length()>0? Math.ceil(getFont().getStringAdvance(getText())) : 0;
    }

    /**
     * Returns the text height.
     */
    public double getTextHeight()
    {
        return Math.ceil(getFont().getLineHeight());
    }

    /**
     * Returns the text bounds.
     */
    public Rect getTextBounds(boolean inBounds)
    {
        // Get basic bounds for TextField size/insets and font/string width/height
        Insets ins = getInsetsAll();
        double width = getWidth(), height = getHeight();
        double tx = ins.left, ty = ins.top, tw = getTextWidth(), th = getTextHeight();

        // If requested to return text bounds in view bounds, constrain
        if (inBounds) {
            if (tx+tw>width-ins.right) tw = width - tx - ins.right;
            if (ty+th>height-ins.bottom) th = height - ty - ins.bottom;
        }

        // Adjust for PromptText if set
        if (_label.isStringViewSet())
            tx += _label.getStringView().getX() + _label.getStringView().getTransX();

        // Adjust rect by alignment
        double ax = ViewUtils.getAlignX(this);
        double ay = ViewUtils.getAlignY(this);
        if (ax>0) { double extra = width - tx - ins.right - tw;
            tx = Math.max(tx+extra*ax,tx); }
        if (ay>0) { double extra = height - ty - ins.bottom - th;
            ty = Math.max(ty+Math.round(extra*ay),ty); }

        // Create/return rect
        return new Rect(tx,ty,tw,th);
    }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public int getCharIndexAt(double anX)
    {
        Rect bnds = getTextBounds(false);
        if (anX<bnds.getX()) return 0;
        if (anX>=bnds.getMaxX()) return length();
        double cx = bnds.getX(); Font font = getFont();
        for (int i=0,iMax=length();i<iMax;i++) { char c = charAt(i);
            double cw = font.getCharAdvance(c,true);
            if (anX<=cx+cw/2)
                return i;
            cx += cw;
        }
        return length();
    }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public double getXForChar(int anIndex)
    {
        Rect bnds = getTextBounds(false);
        if (anIndex==0) return bnds.getX();
        if (anIndex==length()) return bnds.getMaxX();
        double cx = bnds.getX(); Font font = getFont();
        for (int i=0,iMax=anIndex;i<iMax;i++) { char c = charAt(i);
            cx += font.getCharAdvance(c,true); }
        return cx;
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double pw1 = getColCount()>0 ? getTotalColWidth() : getTextWidth() + 10;
        double pw2 = _label.getPrefWidth();
        double pw3 = Math.max(pw1, pw2);
        return pw3 + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double ph1 = getTextHeight();
        double ph2 = _label.getPrefHeight();
        double ph3 = Math.max(ph1, ph2);
        return ph3 + ins.getHeight() + 4;
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        Insets ins = getInsetsAll();
        double x = ins.left, w = getWidth() - ins.getWidth();
        double y = ins.top, h = getHeight() - ins.getHeight();
        _label.setBounds(x, y, w, h);
    }

    /**
     * Override to track FocusGainedValue.
     */
    protected void setFocused(boolean aValue)
    {
        if (aValue==isFocused()) return; super.setFocused(aValue);

        // Toggle caret animation and repaint
        if (!aValue || _downX==0) setCaretAnim();
        repaint();

        // If focus gained, set FocusedGainedValue and select all (if not from mouse press)
        if (aValue) {
            _focusGainedText = getText(); _edited = false;
        }

        // If focus lost and FocusGainedVal changed, fire action
        else if (isEdited())
            fireActionEvent(null);
    }

    /**
     * Override to update Prompt label.
     */
    protected void textDidChange()
    {
        // Ensure that selection is still within bounds
        setSel(getSelStart(), getSelEnd());

        // If PromptText present, update Label.StringView.Visible
        if (_promptText!=null) _label.getStringView().setPaintable(length()==0);

        // If focused and text has changed, updated Edited
        if (isFocused() && !isEdited() && !SnapUtils.equals(getText(), _focusGainedText))
            setEdited(true);

        // Relayout parent and repaint
        relayoutParent(); repaint();
    }

    /**
     * Override to reset FocusedGainedVal.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        _focusGainedText = getText();
        _edited = false;
        super.fireActionEvent(anEvent);
    }

    /**
     * Returns whether text has been edited since last focus (while focused).
     */
    public boolean isEdited()  { return _edited; }

    /**
     * Sets whether text has been edited since last focus (while focused).
     */
    protected void setEdited(boolean aValue)
    {
        if (aValue==isEdited()) return;
        firePropChange(Edited_Prop, _edited, _edited = aValue);
    }

    /**
     * Returns the number of characters in the text string.
     */
    public int length()  { return _sb.length(); }

    /**
     * Returns the individual character at given index.
     */
    public char charAt(int anIndex)  { return _sb.charAt(anIndex); }

    /**
     * Returns the plain string of the text being edited.
     */
    public String getText()  { return _sb.toString(); }

    /**
     * Set text string of text editor.
     */
    public void setText(String aString)  { replaceChars(aString, 0, length(), false); }

    /**
     * Returns the character index of the start of the text selection.
     */
    public int getSelStart()  { return _selStart; }

    /**
     * Sets the selection start.
     */
    public void setSelStart(int aValue)  { setSel(aValue,getSelEnd()); }

    /**
     * Returns the character index of the end of the text selection.
     */
    public int getSelEnd()  { return _selEnd; }

    /**
     * Sets the selection end.
     */
    public void setSelEnd(int aValue)  { setSel(getSelStart(),aValue); }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isSelEmpty()  { return _selStart==_selEnd; }

    /**
     * Sets the character index of the text cursor.
     */
    public void setSel(int newStartEnd)  { setSel(newStartEnd, newStartEnd); }

    /**
     * Sets the character index of the start and end of the text selection.
     */
    public void setSel(int aStart, int anEnd)
    {
        // Make sure start is before end and both are within bounds
        if (anEnd<aStart) { int temp = anEnd; anEnd = aStart; aStart = temp; }
        aStart = MathUtils.clamp(aStart, 0, length());
        anEnd = MathUtils.clamp(anEnd, 0, length());
        if (aStart==_selStart && anEnd==_selEnd) return;

        // Set new start/end
        _selStart = aStart; _selEnd = anEnd;

        // Fire property change
        firePropChange(Sel_Prop, aStart + ((long)anEnd)<<32, 0);
        repaint();
        _compSel = false;
    }

    /**
     * Returns the selection string.
     */
    public String getSelString()  { return getText(); }

    /**
     * Returns the selection bounds.
     */
    public Rect getSelBounds()
    {
        Rect bnds = getTextBounds(false);
        double x1 = getXForChar(getSelStart());
        double x2 = isSelEmpty() ? x1 : getXForChar(getSelEnd());
        bnds.x = x1; bnds.width = x2 - x1;
        return bnds;
    }

    /**
     * Selects all the characters in the text editor.
     */
    public void selectAll()
    {
        // If mouse down, come back later
        if (ViewUtils.isMouseDown()) { getEnv().runLater(() -> selectAll()); return; }

        // Set selection
        setSel(0, length());
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(String aString)
    {
        replaceChars(aString, getSelStart(), getSelEnd(), true);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(String aString, int aStart, int anEnd, boolean doUpdateSel)
    {
        // Get string length (if no string length and no char range, just return)
        int strLen = aString!=null ? aString.length() : 0; if (strLen==0 && aStart==anEnd) return;

        // Do actual replace chars
        if (aStart!=anEnd) _sb.delete(aStart, anEnd);
        if (aString!=null) _sb.insert(aStart, aString);

        // Update selection to be at end of new string
        if (doUpdateSel)
            setSel(aStart + strLen);

        // Otherwise, if replace was before current selection, adjust current selection
        //else if (aStart<=getSelEnd()) {
        //    int delta = strLen - (anEnd - aStart), start = getSelStart(); if (aStart<=start) start += delta;
        //    setSel(start, getSelEnd() + delta); }

        // Notify textDidChange
        textDidChange();
    }

    /**
     * Deletes the current selection.
     */
    public void delete()  { delete(getSelStart(), getSelEnd(), true); }

    /**
     * Deletes the given range of chars.
     */
    public void delete(int aStart, int anEnd, boolean doUpdateSel)
    {
        replaceChars(null, aStart, anEnd, doUpdateSel);
    }

    /**
     * Moves the selection index forward a character (or if a range is selected, moves to end of range).
     */
    public void selectForward(boolean isShiftDown)
    {
        // If shift is down, extend selection forward, otherwise set new selection
        if (isShiftDown) setSel(getSelStart(), getSelEnd()+1);
        else setSel(getSelEnd()+1);
    }

    /**
     * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
     */
    public void selectBackward(boolean isShiftDown)
    {
        // If shift is down, extend selection back, otherwise set new selection
        if (isShiftDown) setSel(getSelStart()-1, getSelEnd());
        else setSel(getSelStart()-1);
    }

    /**
     * Moves the insertion point to the beginning of line.
     */
    public void selectLineStart()  { setSel(0); }

    /**
     * Moves the insertion point to next newline or text end.
     */
    public void selectLineEnd()  { setSel(length()); }

    /**
     * Deletes the character before of the insertion point.
     */
    public void deleteBackward()
    {
        // If CompSel (completion selection), run extra time
        if (_compSel) { _compSel = false; deleteBackward(); }

        // If selected range, delete selected range
        if (!isSelEmpty() || getSelStart()==0) delete();

        // Otherwise delete previous char
        else delete(getSelStart()-1, getSelStart(), true);
    }

    /**
     * Deletes the character after of the insertion point.
     */
    public void deleteForward()
    {
        if (!isSelEmpty()) { delete(); return; }
        int start = getSelStart(), end = start + 1; if (start>=length()) return;
        delete(start, end, true);
    }

    /**
     * Deletes the characters from the insertion point to the end of the line.
     */
    public void deleteToLineEnd()
    {
        // If there is a current selection, just delete it
        if (!isSelEmpty())
            delete();

        // Otherwise delete up to next newline or line end
        delete(getSelStart(), length(), true);
    }

    /**
     * Clears the text.
     */
    public void clear()  { delete(0, length(), true); }

    /**
     * Sets text that represents a completion of current text. This preserves the capitalization of chars in the current
     * text and sets the selection to the remainder. If next key press is delete, removes the added remainder.
     */
    public void setCompletionText(String aString)
    {
        String text = getText(); if (!StringUtils.startsWithIC(aString,text)) return;
        String text2 = text + aString.substring(text.length());
        setText(text2);
        setSel(text.length(), text2.length());
        _compSel = true;
    }

    /**
     * Paint component.
     */
    protected void paintBack(Painter aPntr)
    {
        double w = getWidth(), h = getHeight(); //aPntr.clearRect(0,0,w,h);
        RoundRect rrect = new RoundRect(.5, .5, w-1, h-1, _rad);

        // Fill round rect
        Paint fill = getFill();
        if (fill!=null) {
            aPntr.setPaint(getFill()); aPntr.fill(rrect);
        }

        // Stroke round rect
        Border bdr = getBorder();
        if (bdr!=null) {
            aPntr.setColor(bdr.getColor()); aPntr.setStroke(Stroke.Stroke1);
            aPntr.draw(rrect);
        }
    }

    /**
     * Paints TextField.
     */
    protected void paintFront(Painter aPntr)
    {
        // If empty, just paint selection and return
        if (length()==0) {
            paintSel(aPntr); return; }

        // Get text bounds
        Rect bnds = getTextBounds(true);
        double tx = bnds.x, ty = bnds.y;

        // Clip to text bounds
        aPntr.save(); aPntr.clip(bnds);

        // Paint selection
        paintSel(aPntr);

        // Paint text
        String str = getText();
        Font font = getFont();
        aPntr.setFont(font); aPntr.setPaint(_textFill);
        aPntr.drawString(str, tx, ty + Math.ceil(font.getAscent()));
        aPntr.restore();
    }

    /**
     * Paints TextField Selection.
     */
    protected void paintSel(Painter aPntr)
    {
        if (isFocused()) {
            Rect sbnds = getSelBounds(); double sx = sbnds.x, sy = sbnds.y, sh = sbnds.height;
            if (isSelEmpty()) { if (!_hideCaret) {
                aPntr.setPaint(_textFill); aPntr.setStroke(Stroke.Stroke1); aPntr.drawLine(sx,sy,sx,sy+sh);
            }}
            else {
                aPntr.setPaint(SELECTION_COLOR); aPntr.fill(sbnds);
            }
        }
    }

    /**
     * Process event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        switch (anEvent.getType()) {
            case MousePress: mousePressed(anEvent); break;
            case MouseDrag: mouseDragged(anEvent); break;
            case MouseRelease: mouseReleased(anEvent); break;
            case MouseMove: mouseMoved(anEvent); break;
            case KeyPress: keyPressed(anEvent); break;
            case KeyType: keyTyped(anEvent); break;
            case KeyRelease: keyReleased(anEvent); break;
        }

        // Consume all mouse events
        if (anEvent.isMouseEvent())
            anEvent.consume();
    }

    /**
     * Handles mouse pressed.
     */
    protected void mousePressed(ViewEvent anEvent)
    {
        // Stop caret animation
        setCaretAnim(false);

        // Store the mouse down point
        _downX = anEvent.getX();
        _downY = anEvent.getY();

        // Determine if word or paragraph selecting
        if (!anEvent.isShiftDown()) _wordSel = _pgraphSel = false;
        if (anEvent.getClickCount()==2) _wordSel = true;
        else if (anEvent.getClickCount()==3) _pgraphSel = true;

        // Get selected range for down point
        int start = getCharIndexAt(_downX), end = start;

        // If word selecting extend to word bounds
        if (_wordSel) {
            while (start>0 && Character.isLetterOrDigit(charAt(start-1))) start--;
            while (end<length() && Character.isLetterOrDigit(charAt(end))) end++;
        }

        // If paragraph selecting extend to text bounds
        else if (_pgraphSel) { start = 0; end = length(); }

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            if (start<=getSelStart()) end = getSelEnd();
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
        int start = getCharIndexAt(_downX);
        int end = getCharIndexAt(anEvent.getX());
        if (end<start) { int t = start; start = end; end = t; }

        // If word selecting, extend to word bounds
        if (_wordSel) {
            while(start>0 && Character.isLetterOrDigit(charAt(start-1))) start--;
            while(end<length() && Character.isLetterOrDigit(charAt(end))) end++;
        }

        // If paragraph selecting, extend to text bounds
        else if (_pgraphSel) { start = 0; end = length(); }

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            if (start<=getSelStart()) end = getSelEnd();
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
     * Handle MouseMoved.
     */
    protected void mouseMoved(ViewEvent anEvent)  { showCursor(); }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed(ViewEvent anEvent)
    {
        // Get event info
        int keyCode = anEvent.getKeyCode();
        boolean commandDown = anEvent.isShortcutDown();
        boolean controlDown = anEvent.isControlDown();
        boolean shiftDown = anEvent.isShiftDown();
        boolean altDown = anEvent.isAltDown();
        boolean emacsDown = SnapUtils.isWindows ? altDown : controlDown;
        setCaretAnim(false);

        // Handle command keys
        if (commandDown) {

            // If shift-down, just return
            if (shiftDown && keyCode!=KeyCode.Z) return;

            // Handle common command keys
            switch (keyCode) {
                case KeyCode.X: cut(); anEvent.consume(); break; // Handle command-x cut
                case KeyCode.C: copy(); anEvent.consume(); break; // Handle command-c copy
                case KeyCode.V: paste(); anEvent.consume(); break; // Handle command-v paste
                case KeyCode.A: selectAll(); anEvent.consume(); break; // Handle command-a select all
                default: return; // Any other command keys just return
            }
        }

        // Handle control keys (not applicable on Windows, since they are handled by command key code above)
        else if (emacsDown) {

            // If shift down, just return
            if (shiftDown) return;

            // Handle common emacs key bindings
            switch (keyCode) {
                case KeyCode.F: selectForward(false); break; // Handle control-f key forward
                case KeyCode.B: selectBackward(false); break; // Handle control-b key backward
                case KeyCode.A: selectLineStart(); break; // Handle control-a line start
                case KeyCode.E: selectLineEnd(); break; // Handle control-e line end
                case KeyCode.D: deleteForward(); break; // Handle control-d delete forward
                case KeyCode.K: deleteToLineEnd(); break; // Handle control-k delete line to end
                default: return; // Any other control keys, just return
            }
        }

        // Handle supported non-character keys
        else switch (keyCode) {
            case KeyCode.TAB: anEvent.consume(); break;
            case KeyCode.ENTER: selectAll(); fireActionEvent(anEvent); anEvent.consume(); break; // Handle enter
            case KeyCode.LEFT: selectBackward(shiftDown); anEvent.consume(); break; // Handle left arrow
            case KeyCode.RIGHT: selectForward(shiftDown); anEvent.consume(); break; // Handle right arrow
            case KeyCode.HOME: selectLineStart(); break; // Handle home key
            case KeyCode.END: selectLineEnd(); break; // Handle end key
            case KeyCode.BACK_SPACE: deleteBackward(); anEvent.consume(); break; // Handle Backspace key
            case KeyCode.DELETE: deleteForward(); anEvent.consume(); break; // Handle Delete key
            case KeyCode.ESCAPE: escape(anEvent); break;
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
        if (charDefined && !commandDown && !controlDown && !emacsDown) {
            replaceChars(keyChars);
            hideCursor();
            anEvent.consume();
        }

        // If alt-TAB or alt-ENTER
        //if (altDown && anEvent.isEnterKey() || anEvent.isTabKey()) {
        //    replaceChars(keyChars); hideCursor(); anEvent.consume(); }
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
    private boolean isCaretAnimNeeded()  { return isFocused() && isSelEmpty() && isShowing(); }

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
        if (aValue==isCaretAnim()) return;
        if (aValue) {
            _caretTimer = new ViewTimer(500, t -> { _hideCaret = !_hideCaret; repaint(); });
            _caretTimer.start();
        }
        else { _caretTimer.stop(); _caretTimer = null; _hideCaret = false; repaint(); }
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
        if (!isSelEmpty()) {
            String str = getSelString();
            Clipboard cboard = Clipboard.getCleared();
            cboard.addData(str);
        }
    }

    /**
     * Pasts the current clipboard data over the current selection.
     */
    public void paste()
    {
        Clipboard cb = Clipboard.get();
        if (cb.hasString()) {
            String string = cb.getString();
            replaceChars(string);
        }
    }

    /**
     * Called when escape key is pressed to cancels editing in TextField.
     * First cancel resets focus gained value. Second hands focus to previous view.
     */
    public void escape(ViewEvent anEvent)
    {
        // If not focused, just return
        if (!isFocused()) return;

        // If value has changed since focus gained, reset to original value
        if (isEdited()) {
            setText(_focusGainedText);
            setEdited(false);
            selectAll();
            if (anEvent != null)
                anEvent.consume();
        }

        // Otherwise hand focus to previous view
        else {
            getWindow().requestFocus(null);
            anEvent.consume();
        }
    }

    /**
     * Override to check caret animation and scrollSelToVisible when showing.
     */
    protected void setShowing(boolean aValue)
    {
        if (aValue==isShowing()) return; super.setShowing(aValue);
        if (isFocused())
            setCaretAnim();
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
        String str = getText();
        if (str.length()>40) str = str.substring(0,40) + "...";
        return getClass().getSimpleName() + ": " + str;
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Do normal version
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ColCount, Text, PromptText, Radius
        if (!isPrefWidthSet() && getColCount()!=12) e.add(ColCount_Prop, getColCount());
        if (getText()!=null && getText().length()>0) e.add("text", getText());
        if (getPromptText()!=null && getPromptText().length()>0) e.add(PromptText_Prop, getPromptText());
        if (getRadius()!=4) e.add(Radius_Prop, getRadius());
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal version
        super.fromXMLView(anArchiver, anElement);

        // Unarchive ColCount, Text, PromptText, Radius
        if (anElement.hasAttribute(ColCount_Prop))
            setColCount(anElement.getAttributeIntValue(ColCount_Prop));
        String str = anElement.getAttributeValue("text");
        if (str == null)
            str = anElement.getAttributeValue("value", anElement.getValue());
        if (str != null && str.length() > 0)
            setText(str);
        if (anElement.hasAttribute(PromptText_Prop))
            setPromptText(anElement.getAttributeValue(PromptText_Prop));
        if (anElement.hasAttribute(Radius_Prop))
            setRadius(anElement.getAttributeDoubleValue(Radius_Prop));
    }

    /**
     * Sets the given TextField to animate background label alignment from center to left when focused.
     */
    public static void setBackLabelAlignAnimatedOnFocused(TextField aTextField, boolean aValue)
    {
        aTextField.getLabel().setAlign(Pos.CENTER);
        aTextField.addPropChangeListener(pce -> {
            if (aTextField.isFocused())
                ViewAnimUtils.setAlign(aTextField.getLabel(), Pos.CENTER_LEFT, 200);
            else ViewAnimUtils.setAlign(aTextField.getLabel(), Pos.CENTER, 600);
        }, View.Focused_Prop);
    }
}