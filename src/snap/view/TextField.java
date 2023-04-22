/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

import java.util.Objects;

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
    
    // The selection start/end
    private int  _selStart, _selEnd;
    
    // Whether the editor is word selecting (double click) or paragraph selecting (triple click)
    private boolean  _wordSel, _pgraphSel;
    
    // Whether text field is showing a completion selection
    private boolean  _compSel;
    
    // The mouse down point
    private double  _downX;
    
    // The animator for caret blinking
    private ViewTimer  _caretTimer;
    
    // Whether to hide caret
    private boolean  _hideCaret;

    // Whether to send action on focus lost (if content changed)
    private boolean  _fireActionOnFocusLost = true;

    // The value of text on focus gained
    protected String  _focusGainedText;
    
    // Whether text has been edited since last focus
    private boolean  _edited;

    // Constants for properties
    public static final String ColCount_Prop = "ColCount";
    public static final String Edited_Prop = "Edited";
    public static final String PromptText_Prop = "PromptText";
    public static final String Sel_Prop = "Selection";
    public static final String TextFill_Prop = "TextFill";
    public static final String FireActionOnFocusLost_Prop = "FireActionOnFocusLost";

    // Constants for property defaults
    private static Border DEFAULT_TEXT_FIELD_BORDER = Border.createLineBorder(Color.LIGHTGRAY, 1).copyForInsets(Insets.EMPTY);
    private static double DEFAULT_TEXT_FIELD_BORDER_RADIUS = 3;
    private static final Insets DEFAULT_TEXT_FIELD_PADDING = new Insets(2, 2, 2, 5);

    // The color of the border when focused
    static Color SELECTION_COLOR = new Color(181, 214, 254, 255);

    /**
     * Creates a new TextField.
     */
    public TextField()
    {
        setFill(Color.WHITE);
        setBorder(DEFAULT_TEXT_FIELD_BORDER);
        setBorderRadius(DEFAULT_TEXT_FIELD_BORDER_RADIUS);
        setFocusable(true);
        setFocusWhenPressed(true);
        setActionable(true);
        enableEvents(MouseEvents);
        enableEvents(KeyEvents);

        // Configure label and set
        _label = new Label();
        _label.setPadding(0, 0, 0, 0);
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
        if (Objects.equals(aPaint, _textFill)) return;
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
        firePropChange(ColCount_Prop, _colCount, _colCount = aValue);
        relayoutParent();
    }

    /**
     * Returns the total column width.
     */
    double getTotalColWidth()
    {
        return Math.ceil(_colCount * getFont().charAdvance('X'));
    }

    /**
     * Returns the prompt text.
     */
    public String getPromptText()  { return _promptText; }

    /**
     * Sets the prompt text.
     */
    public void setPromptText(String aStr)
    {
        if (Objects.equals(aStr, _promptText)) return;
        _label.setText(aStr);
        _label.setTextFill(Color.LIGHTGRAY);
        firePropChange(PromptText_Prop, _promptText, _promptText = aStr);
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
        if (aValue == _fireActionOnFocusLost) return;
        firePropChange(FireActionOnFocusLost_Prop, _fireActionOnFocusLost, _fireActionOnFocusLost = aValue);
    }

    /**
     * Returns the label in the background.
     */
    public Label getLabel()  { return _label; }

    /**
     * Returns the default alignment.
     */
    @Override
    public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

    /**
     * Returns the default border.
     */
    @Override
    public Border getDefaultBorder()  { return DEFAULT_TEXT_FIELD_BORDER; }

    /**
     * Override to return white.
     */
    @Override
    public Paint getDefaultFill()  { return Color.WHITE; }

    /**
     * Returns the text width.
     */
    public double getTextWidth()
    {
        return length() > 0 ? Math.ceil(getFont().getStringAdvance(getText())) : 0;
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
        double viewW = getWidth();
        double viewH = getHeight();
        double textX = ins.left;
        double textY = ins.top;
        double textW = getTextWidth();
        double textH = getTextHeight();

        // If requested to return text bounds in view bounds, constrain
        if (inBounds) {
            if (textX + textW > viewW - ins.right)
                textW = viewW - textX - ins.right;
            if (textY + textH > viewH - ins.bottom)
                textH = viewH - textY - ins.bottom;
        }

        // Adjust for PromptText if set
        if (_label.isStringViewSet()) {
            StringView stringView = _label.getStringView();
            textX += stringView.getX() + stringView.getTransX();
        }

        // Adjust rect by alignment
        double alignX = ViewUtils.getAlignX(this);
        double alignY = ViewUtils.getAlignY(this);
        if (alignX > 0) {
            double extra = viewW - textX - ins.right - textW;
            textX = Math.max(textX + extra * alignX, textX);
        }
        if (alignY > 0) {
            double extra = viewH - textY - ins.bottom - textH;
            textY = Math.max(textY + Math.round(extra * alignY), textY);
        }

        // Create/return rect
        return new Rect(textX, textY, textW, textH);
    }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public int getCharIndexAt(double anX)
    {
        Rect textBounds = getTextBounds(false);
        if (anX < textBounds.getX())
            return 0;
        if (anX >= textBounds.getMaxX())
            return length();

        double charX = textBounds.getX();
        Font font = getFont();
        for (int i = 0, iMax = length(); i < iMax; i++) {
            char loopChar = charAt(i);
            double charW = font.getCharAdvance(loopChar, true);
            if (anX <= charX + charW / 2)
                return i;
            charX += charW;
        }

        // Return length
        return length();
    }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public double getXForChar(int anIndex)
    {
        Rect textBounds = getTextBounds(false);
        if (anIndex == 0)
            return textBounds.getX();
        if (anIndex == length())
            return textBounds.getMaxX();

        //
        double charX = textBounds.getX();
        Font font = getFont();
        for (int i = 0, iMax = anIndex; i < iMax; i++) {
            char loopChar = charAt(i);
            charX += font.getCharAdvance(loopChar, true);
        }

        // Return char X
        return charX;
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double prefW1 = getColCount() > 0 ? getTotalColWidth() : getTextWidth() + 10;
        double prefW2 = _label.getPrefWidth();
        double prefW3 = Math.max(prefW1, prefW2);
        return prefW3 + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double prefH1 = getTextHeight();
        double prefH2 = _label.getPrefHeight();
        double prefH3 = Math.max(prefH1, prefH2);
        return prefH3 + ins.getHeight() + 4;
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        _label.setBounds(areaX, areaY, areaW, areaH);
    }

    /**
     * Override to track FocusGainedValue.
     */
    protected void setFocused(boolean aValue)
    {
        // Do normal version
        if (aValue == isFocused()) return;
        super.setFocused(aValue);

        // Toggle caret animation and repaint
        if (!aValue || _downX == 0)
            setCaretAnim();
        repaint();

        // Handle focus gained: set FocusedGainedValue and select all (if not from mouse press)
        if (aValue) {
            _focusGainedText = getText();
            _edited = false;
        }

        // Handle focus lost: If FocusGainedVal changed, fire action
        else {
            if (isEdited() && isFireActionOnFocusLost())
                fireActionEvent(null);
        }
    }

    /**
     * Override to update Prompt label.
     */
    protected void textDidChange()
    {
        // Ensure that selection is still within bounds
        setSel(getSelStart(), getSelEnd());

        // If PromptText present, update Label.StringView.Visible
        if (_promptText != null)
            _label.getStringView().setPaintable(length() == 0);

        // If focused and text has changed, updated Edited
        if (isFocused() && !isEdited() && !Objects.equals(getText(), _focusGainedText))
            setEdited(true);

        // Relayout parent and repaint
        relayoutParent();
        repaint();
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
        if (aValue == isEdited()) return;
        firePropChange(Edited_Prop, _edited, _edited = aValue);
    }

    /**
     * Returns the number of characters in the text string.
     */
    public int length()  { return _sb.length(); }

    /**
     * Returns the individual character at given index.
     */
    public char charAt(int anIndex)
    {
        return _sb.charAt(anIndex);
    }

    /**
     * Returns the plain string of the text being edited.
     */
    public String getText()
    {
        return _sb.toString();
    }

    /**
     * Set text string of text editor.
     */
    public void setText(String aString)
    {
        replaceChars(aString, 0, length(), false);
    }

    /**
     * Returns the character index of the start of the text selection.
     */
    public int getSelStart()  { return _selStart; }

    /**
     * Sets the selection start.
     */
    public void setSelStart(int aValue)
    {
        setSel(aValue, getSelEnd());
    }

    /**
     * Returns the character index of the end of the text selection.
     */
    public int getSelEnd()  { return _selEnd; }

    /**
     * Sets the selection end.
     */
    public void setSelEnd(int aValue)
    {
        setSel(getSelStart(), aValue);
    }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isSelEmpty()  { return _selStart==_selEnd; }

    /**
     * Sets the character index of the text cursor.
     */
    public void setSel(int newStartEnd)
    {
        setSel(newStartEnd, newStartEnd);
    }

    /**
     * Sets the character index of the start and end of the text selection.
     */
    public void setSel(int aStart, int anEnd)
    {
        // Make sure start is before end and both are within bounds
        if (anEnd < aStart) {
            int temp = anEnd;
            anEnd = aStart;
            aStart = temp;
        }
        aStart = MathUtils.clamp(aStart, 0, length());
        anEnd = MathUtils.clamp(anEnd, 0, length());
        if (aStart == _selStart && anEnd == _selEnd)
            return;

        // Set new start/end
        _selStart = aStart;
        _selEnd = anEnd;

        // Fire property change
        firePropChange(Sel_Prop, aStart + ((long) anEnd) << 32, 0);
        repaint();
        _compSel = false;
    }

    /**
     * Returns the selection string.
     */
    public String getSelString()
    {
        return getText();
    }

    /**
     * Returns the selection bounds.
     */
    public Rect getSelBounds()
    {
        Rect bounds = getTextBounds(false);
        double x1 = getXForChar(getSelStart());
        double x2 = isSelEmpty() ? x1 : getXForChar(getSelEnd());
        bounds.x = x1;
        bounds.width = x2 - x1;
        return bounds;
    }

    /**
     * Selects all the characters in the text editor.
     */
    public void selectAll()
    {
        // If mouse down, come back later
        if (ViewUtils.isMouseDown()) {
            getEnv().runLater(() -> selectAll());
            return;
        }

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
        int strLen = aString != null ? aString.length() : 0;
        if (strLen == 0 && aStart == anEnd)
            return;

        // Do actual replace chars
        if (aStart != anEnd)
            _sb.delete(aStart, anEnd);
        if (aString != null)
            _sb.insert(aStart, aString);

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
    public void delete()
    {
        delete(getSelStart(), getSelEnd(), true);
    }

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
        if (isShiftDown) setSel(getSelStart(), getSelEnd() + 1);
        else setSel(getSelEnd() + 1);
    }

    /**
     * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
     */
    public void selectBackward(boolean isShiftDown)
    {
        // If shift is down, extend selection back, otherwise set new selection
        if (isShiftDown) setSel(getSelStart() - 1, getSelEnd());
        else setSel(getSelStart() - 1);
    }

    /**
     * Moves the insertion point to the beginning of line.
     */
    public void selectLineStart()
    {
        setSel(0);
    }

    /**
     * Moves the insertion point to next newline or text end.
     */
    public void selectLineEnd()
    {
        setSel(length());
    }

    /**
     * Deletes the character before of the insertion point.
     */
    public void deleteBackward()
    {
        // If CompSel (completion selection), run extra time
        if (_compSel) {
            _compSel = false;
            deleteBackward();
        }

        // If selected range, delete selected range
        if (!isSelEmpty() || getSelStart() == 0) delete();

            // Otherwise delete previous char
        else delete(getSelStart() - 1, getSelStart(), true);
    }

    /**
     * Deletes the character after of the insertion point.
     */
    public void deleteForward()
    {
        if (!isSelEmpty()) {
            delete();
            return;
        }
        int start = getSelStart(), end = start + 1;
        if (start >= length()) return;
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
    public void clear()
    {
        delete(0, length(), true);
    }

    /**
     * Sets text that represents a completion of current text. This preserves the capitalization of chars in the current
     * text and sets the selection to the remainder. If next key press is delete, removes the added remainder.
     */
    public void setCompletionText(String aString)
    {
        // Get text - just return if completion string is just text
        String text = getText();
        if (!StringUtils.startsWithIC(aString, text))
            return;

        // Get completion string by appending chars to original text and set
        String text2 = text + aString.substring(text.length());
        setText(text2);

        // Set sel and mark as CompletionSel
        setSel(text.length(), text2.length());
        _compSel = true;
    }

    /**
     * Paints TextField.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        // If empty, just paint selection and return
        if (length() == 0) {
            paintSel(aPntr);
            return;
        }

        // Get text bounds
        Rect textBounds = getTextBounds(true);

        // Clip to text bounds
        aPntr.save();
        aPntr.clip(textBounds);

        // Paint selection
        paintSel(aPntr);

        // Get/set font/paint
        Font font = getFont();
        aPntr.setFont(font);
        Paint textFill = isEnabled() ? getTextFill() : Color.GRAY;
        aPntr.setPaint(textFill);

        // Paint text
        String str = getText();
        double baseY = textBounds.y + Math.ceil(font.getAscent());
        aPntr.drawString(str, textBounds.x, baseY);

        // Restore clip
        aPntr.restore();
    }

    /**
     * Paints TextField Selection.
     */
    protected void paintSel(Painter aPntr)
    {
        // If not focused, just return
        if (!isFocused())
            return;

        // Paint caret (empty selection)
        if (isSelEmpty()) {

            // Paint caret if flash on
            if (!_hideCaret) {
                aPntr.setPaint(Color.BLACK);
                aPntr.setStroke(Stroke.Stroke1);
                Rect selBounds = getSelBounds();
                aPntr.drawLine(selBounds.x, selBounds.y, selBounds.x, selBounds.getMaxY());
            }
        }

        // Paint full selection
        else {
            aPntr.setPaint(SELECTION_COLOR);
            Rect selBounds = getSelBounds();
            aPntr.fill(selBounds);
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

        // Determine if word or paragraph selecting
        if (!anEvent.isShiftDown())
            _wordSel = _pgraphSel = false;
        if (anEvent.getClickCount() == 2)
            _wordSel = true;
        else if (anEvent.getClickCount() == 3)
            _pgraphSel = true;

        // Get selected range for down point
        int start = getCharIndexAt(_downX), end = start;

        // If word selecting extend to word bounds
        if (_wordSel) {
            while (start > 0 && Character.isLetterOrDigit(charAt(start - 1))) start--;
            while (end < length() && Character.isLetterOrDigit(charAt(end))) end++;
        }

        // If paragraph selecting extend to text bounds
        else if (_pgraphSel) {
            start = 0;
            end = length();
        }

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            if (start <= getSelStart())
                end = getSelEnd();
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
        if (end < start) {
            int swap = start;
            start = end;
            end = swap;
        }

        // If word selecting, extend to word bounds
        if (_wordSel) {
            while (start > 0 && Character.isLetterOrDigit(charAt(start - 1)))
                start--;
            while (end < length() && Character.isLetterOrDigit(charAt(end)))
                end++;
        }

        // If paragraph selecting, extend to text bounds
        else if (_pgraphSel) {
            start = 0;
            end = length();
        }

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            if (start <= getSelStart())
                end = getSelEnd();
            else start = getSelStart();
        }

        // Set selection
        setSel(start, end);
    }

    /**
     * Handles mouse released.
     */
    protected void mouseReleased(ViewEvent anEvent)
    {
        setCaretAnim();
        _downX = 0;
    }

    /**
     * Handle MouseMoved.
     */
    protected void mouseMoved(ViewEvent anEvent)
    {
        showCursor();
    }

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
            if (shiftDown && keyCode != KeyCode.Z) return;

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
        char keyChar = keyChars.length() > 0 ? keyChars.charAt(0) : 0;
        boolean charDefined = keyChar != KeyCode.CHAR_UNDEFINED && !Character.isISOControl(keyChar);
        boolean commandDown = anEvent.isShortcutDown(), controlDown = anEvent.isControlDown();
        boolean emacsDown = SnapUtils.isWindows ? anEvent.isAltDown() : controlDown;

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
    protected void keyReleased(ViewEvent anEvent)
    {
        setCaretAnim();
    }

    /**
     * Shows the cursor.
     */
    public void showCursor()
    {
        setCursor(Cursor.TEXT);
    }

    /**
     * Hides the cursor.
     */
    public void hideCursor()
    {
        setCursor(Cursor.NONE);
    }

    /**
     * Returns whether anim is needed.
     */
    private boolean isCaretAnimNeeded()
    {
        return isFocused() && isSelEmpty() && isShowing();
    }

    /**
     * Sets the caret animation to whether it's needed.
     */
    private void setCaretAnim()
    {
        setCaretAnim(isCaretAnimNeeded());
    }

    /**
     * Returns whether ProgressBar is animating.
     */
    private boolean isCaretAnim()
    {
        return _caretTimer != null;
    }

    /**
     * Sets anim.
     */
    private void setCaretAnim(boolean aValue)
    {
        // If already set, just return
        if (aValue == isCaretAnim()) return;

        // Handle on
        if (aValue) {
            _caretTimer = new ViewTimer(500, t -> {
                _hideCaret = !_hideCaret;
                repaint();
            });
            _caretTimer.start();
        }

        // Handle off
        else {
            _caretTimer.stop();
            _caretTimer = null;
            _hideCaret = false;
            repaint();
        }
    }

    /**
     * Copies the current selection onto the clip board, then deletes the current selection.
     */
    public void cut()
    {
        copy();
        delete();
    }

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
        // Get clipboard - if not loaded, come back loaded
        Clipboard clipboard = Clipboard.get();
        if (!clipboard.isLoaded()) {
            clipboard.addLoadListener(() -> paste());
            return;
        }

        // Handle clipboard String
        if (clipboard.hasString()) {
            String string = clipboard.getString();
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
        if (aValue == isShowing()) return;
        super.setShowing(aValue);
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
        if (str.length() > 40)
            str = str.substring(0, 40) + "...";
        return getClass().getSimpleName() + ": " + str;
    }

    /**
     * Override for custom defaults.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Padding
        if (aPropName == Padding_Prop)
            return DEFAULT_TEXT_FIELD_PADDING;

        // Do normal version
        return super.getPropDefault(aPropName);
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Do normal version
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ColCount, Text, PromptText
        if (!isPrefWidthSet() && getColCount() != 12) e.add(ColCount_Prop, getColCount());
        if (getText() != null && getText().length() > 0) e.add("text", getText());
        if (getPromptText() != null && getPromptText().length() > 0) e.add(PromptText_Prop, getPromptText());
        return e;
    }

    /**
     * XML unarchival.
     */
    public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal version
        super.fromXMLView(anArchiver, anElement);

        // Unarchive ColCount, Text, PromptText
        if (anElement.hasAttribute(ColCount_Prop))
            setColCount(anElement.getAttributeIntValue(ColCount_Prop));
        String str = anElement.getAttributeValue("text");
        if (str == null)
            str = anElement.getAttributeValue("value", anElement.getValue());
        if (str != null && str.length() > 0)
            setText(str);
        if (anElement.hasAttribute(PromptText_Prop))
            setPromptText(anElement.getAttributeValue(PromptText_Prop));
    }

    /**
     * Sets the given TextField to animate background label alignment from center to left when focused.
     */
    public static void setBackLabelAlignAnimatedOnFocused(TextField aTextField, boolean aValue)
    {
        Label textFieldLabel = aTextField.getLabel();
        textFieldLabel.setAlign(Pos.CENTER);
        aTextField.addPropChangeListener(pce -> {
            if (aTextField.isFocused())
                ViewAnimUtils.setAlign(textFieldLabel, Pos.CENTER_LEFT, 200);
            else ViewAnimUtils.setAlign(textFieldLabel, Pos.CENTER, 600);
        }, View.Focused_Prop);
    }
}