/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.UndoSet;
import snap.props.Undoer;
import snap.text.*;
import snap.util.*;
import java.util.Objects;

/**
 * A view subclass for displaying and editing a TextDoc (using TextBox).
 */
public class TextArea extends View {

    // The text being edited
    private TextBox  _textBox;

    // Whether text is editable
    private boolean  _editable;

    // Whether text should wrap lines that overrun bounds
    private boolean  _wrapLines;

    // The char index of carat
    private int  _selIndex;

    // The char index of last char selection
    private int  _selAnchor;

    // The char index of current selection start/end
    private int  _selStart, _selEnd;

    // The text selection
    private TextSel  _sel;

    // Whether the editor is word selecting (double click) or paragraph selecting (triple click)
    private boolean  _wordSel, _pgraphSel;

    // The current TextStyle for the cursor or selection
    private TextStyle  _selStyle;

    // The Selection color
    private Color  _selColor = new Color(181, 214, 254, 255);

    // The text pane undoer
    private Undoer  _undoer = new Undoer();

    // The index of the last replace so we can commit undo changes if not adjacent
    private int  _lastReplaceIndex;

    // The mouse down point
    private double  _downX, _downY;

    // The animator for caret blinking
    private ViewTimer  _caretTimer;

    // Whether to show text insertion point caret
    private boolean  _showCaret;

    // Whether to send action on enter key press
    private boolean  _fireActionOnEnterKey;

    // Whether to send action on focus lost (if content changed)
    private boolean  _fireActionOnFocusLost;

    // The content on focus gained
    private String  _focusGainedText;

    // Whether RM should be spell checking
    public static boolean  isSpellChecking = Prefs.get().getBoolean("SpellChecking", false);

    // Whether hyphenating is activated
    static boolean  _hyphenating = Prefs.get().getBoolean("Hyphenating", false);

    // The MIME type for SnapKit RichText
    public static final String  SNAP_RICHTEXT_TYPE = "reportmill/xstring";

    // The PropChangeListener to catch TextDoc PropChanges.
    private PropChangeListener  _textDocPropLsnr = pce -> textDocDidPropChange(pce);

    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String WrapLines_Prop = "WrapLines";
    public static final String FireActionOnEnterKey_Prop = "FireActionOnEnterKey";
    public static final String FireActionOnFocusLost_Prop = "FireActionOnFocusLost";
    public static final String Selection_Prop = "Selection";

    // Constants for property defaults
    private static final Insets DEFAULT_TEXT_AREA_PADDING = new Insets(2);

    /**
     * Creates a new TextArea.
     */
    public TextArea()
    {
        // Create/set default TextBox
        TextBox tbox = createTextBox();
        setTextBox(tbox);

        // Configure
        setFont(getDefaultFont());
        setFocusPainted(false);
    }

    /**
     * Returns the text that is being edited.
     */
    public TextBox getTextBox()  { return _textBox; }

    /**
     * Returns the text box used to layout text.
     */
    public void setTextBox(TextBox aTextBox)
    {
        // If already set, just return
        if (aTextBox == _textBox) return;

        // Set TextBox
        _textBox = aTextBox;

        // Remove PropChangeListener from old TextDoc
        TextDoc oldTextDoc = getTextDoc();
        if (oldTextDoc != null)
            oldTextDoc.removePropChangeListener(_textDocPropLsnr);

        // Add PropChangeListener to new RichText
        _textBox.getTextDoc().addPropChangeListener(_textDocPropLsnr);
    }

    /**
     * Creates a new TextBox.
     */
    protected TextBox createTextBox()
    {
        return new TextBox();
    }

    /**
     * Returns the TextDoc.
     */
    public TextDoc getTextDoc()
    {
        TextBox textBox = getTextBox();
        return textBox.getTextDoc();
    }

    /**
     * Sets the TextDoc.
     */
    public void setTextDoc(TextDoc aTextDoc)
    {
        // If already set, just return
        TextDoc oldTextDoc = getTextDoc(); if (aTextDoc == oldTextDoc) return;

        // Add/remove PropChangeListener
        if (oldTextDoc != null)
            oldTextDoc.removePropChangeListener(_textDocPropLsnr);
        getTextBox().setTextDoc(aTextDoc);
        if (aTextDoc != null)
            aTextDoc.addPropChangeListener(_textDocPropLsnr);

        // Reset selection
        if (getSelStart() != 0 || !isSelEmpty())
            setSel(0);

        // Relayout parent, repaint
        relayoutParent();
        repaint();
    }

    /**
     * Returns the source of current content (URL, File, String path, etc.)
     */
    public Object getSource()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.getSource();
    }

    /**
     * Set the source for TextComponent text.
     */
    public void setSource(Object aSource)
    {
        // Get TextDoc for Source
        TextDoc textDoc = TextDoc.newFromSource(aSource);

        // Set source and notify textDidChange
        TextBox textBox = getTextBox();
        textBox.setTextDoc(textDoc);
        textDidChange();

        // Reset selection (to line end if single-line, otherwise text start)
        int selIndex = textBox.getLineCount() == 1 && length() < 40 ? length() : 0;
        setSel(selIndex);
    }

    /**
     * Returns the plain string of the text being edited.
     */
    public String getText()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.getString();
    }

    /**
     * Set text string of text editor.
     */
    public void setText(String aString)
    {
        // If string already set, just return
        String str = aString != null ? aString : "";
        if (str.length() == length() && (str.length() == 0 || str.equals(getText()))) return;

        // Set string and notify textDidChange
        TextBox textBox = getTextBox();
        textBox.setString(aString);
        textDidChange();

        // Reset selection (to line end if single-line, otherwise text start)
        int selIndex = textBox.getLineCount() == 1 && length() < 40 ? length() : 0;
        setSel(selIndex);
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
        if (aValue == isEditable()) return;

        firePropChange(Editable_Prop, _editable, _editable = aValue);

        // If editable, set some related attributes
        if (aValue) {
            enableEvents(MouseEvents);
            enableEvents(KeyEvents);
            setFocusable(true);
            setFocusWhenPressed(true);
            setFocusKeysEnabled(false);
        }

        else {
            disableEvents(MouseEvents);
            disableEvents(KeyEvents);
            setFocusable(false);
            setFocusWhenPressed(false);
            setFocusKeysEnabled(true);
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
        if (aValue == _wrapLines) return;
        firePropChange(WrapLines_Prop, _wrapLines, _wrapLines = aValue);
        TextBox textBox = getTextBox();
        textBox.setWrapLines(aValue);
    }

    /**
     * Returns whether editor is doing check-as-you-type spelling.
     */
    public boolean isSpellChecking()  { return isSpellChecking; }

    /**
     * Returns whether text supports multiple styles.
     */
    public boolean isRichText()
    {
        TextBox textBox = getTextBox();
        return textBox.isRichText();
    }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)
    {
        TextBox textBox = getTextBox();
        textBox.setRichText(aValue);
    }

    /**
     * Returns the default style for text.
     */
    public TextStyle getDefaultStyle()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.getDefaultStyle();
    }

    /**
     * Sets the default style.
     */
    public void setDefaultStyle(TextStyle aStyle)
    {
        TextDoc textDoc = getTextDoc();
        textDoc.setDefaultStyle(aStyle);
    }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.getDefaultLineStyle();
    }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)
    {
        TextDoc textDoc = getTextDoc();
        textDoc.setDefaultLineStyle(aLineStyle);
    }

    /**
     * Returns whether text view fires action on enter key press.
     */
    public boolean isFireActionOnEnterKey()  { return _fireActionOnEnterKey; }

    /**
     * Sets whether text area sends action on enter key press.
     */
    public void setFireActionOnEnterKey(boolean aValue)
    {
        if (aValue == _fireActionOnEnterKey) return;
        if (aValue)
            enableEvents(Action);
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
        if (aValue == _fireActionOnFocusLost) return;
        firePropChange(FireActionOnFocusLost_Prop, _fireActionOnFocusLost, _fireActionOnFocusLost = aValue);
    }

    /**
     * Returns the number of characters in the text string.
     */
    public int length()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.length();
    }

    /**
     * Returns the individual character at given index.
     */
    public char charAt(int anIndex)
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.charAt(anIndex);
    }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isSelEmpty()
    {
        return getSelStart() == getSelEnd();
    }

    /**
     * Returns the initial character index of the selection (usually SelStart).
     */
    public int getSelAnchor()
    {
        return Math.min(_selAnchor, length());
    }

    /**
     * Returns the final character index of the selection (usually SelEnd).
     */
    public int getSelIndex()
    {
        return Math.min(_selIndex, length());
    }

    /**
     * Returns the character index of the start of the text selection.
     */
    public int getSelStart()
    {
        return Math.min(_selStart, length());
    }

    /**
     * Returns the character index of the end of the text selection.
     */
    public int getSelEnd()
    {
        return Math.min(_selEnd, length());
    }

    /**
     * Returns the text selection.
     */
    public TextSel getSel()
    {
        if (_sel != null) return _sel;
        TextSel sel = new TextSel(_textBox, _selAnchor, _selIndex);
        return _sel = sel;
    }

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
    public void setSel(int aStart, int aEnd)
    {
        // If already set, just return
        int len = length();
        int anchor = Math.min(aStart, len);
        int index = Math.min(aEnd, len);
        if (anchor == _selAnchor && index == _selIndex) return;

        // Repaint old selection
        if (isShowing()) repaintSel();

        // Set new values
        _selAnchor = aStart;
        _selIndex = aEnd;
        _selStart = Math.min(aStart, aEnd);
        _selEnd = Math.max(aStart, aEnd);

        // Fire selection property change and clear selection
        firePropChange(Selection_Prop, _sel, _sel = null);

        // Reset SelStyle
        _selStyle = null;

        // Repaint selection and scroll to visible (after delay)
        if (isShowing()) {
            repaintSel();
            setCaretAnim();
            getEnv().runLater(() -> scrollSelToVisible());
        }
    }

    /**
     * Selects all the characters in the text editor.
     */
    public void selectAll()
    {
        setSel(0, length());
    }

    /**
     * Repaint the selection.
     */
    protected void repaintSel()
    {
        TextSel sel = getSel();
        Shape selPath = sel.getPath();
        Rect rect = selPath.getBounds();
        rect.inset(-1);
        repaint(rect);
    }

    /**
     * Scrolls Selection to visible.
     */
    protected void scrollSelToVisible()
    {
        // Get selection rect. If empty, outset by 1. If abuts left border, make sure left border is visible.
        TextSel sel = getSel();
        Rect selRect = sel.getPath().getBounds();
        if (selRect.isEmpty())
            selRect.inset(-1, -2);
        if (selRect.x - 1 <= getTextBox().getX()) {
            selRect.setX(-getX());
            selRect.setWidth(10);
        }

        // If selection rect not fully contained in visible rect, scrollRectToVisible
        Rect vrect = getClipAllBounds();
        if (vrect == null || vrect.isEmpty()) return;
        if (!vrect.contains(selRect)) {
            if (!selRect.intersects(vrect)) selRect.inset(0, -100);
            scrollToVisible(selRect);
        }
    }

    /**
     * Returns the font of the current selection or cursor.
     */
    public Font getFont()
    {
        // If RichText, return SelStyle.Font
        if (isRichText()) {
            TextStyle selStyle = getSelStyle();
            return selStyle.getFont();
        }

        // Do normal version
        return super.getFont();
    }

    /**
     * Sets the font of the current selection or cursor.
     */
    public void setFont(Font aFont)
    {
        // If RichText, just update SelStyle.Font and return
        if (isRichText()) {
            if (aFont != null)
                setSelStyleValue(TextStyle.FONT_KEY, aFont);
            return;
        }

        // Do normal version
        if (Objects.equals(aFont, getFont())) return;
        super.setFont(aFont);

        // Update SelStyle.Font
        setSelStyleValue(TextStyle.FONT_KEY, aFont);
    }

    /**
     * Override to update font.
     */
    @Override
    protected void parentFontChanged()
    {
        // Do normal version
        super.parentFontChanged();

        // Update SelStyle.Font
        if (!isRichText()) {
            Font font = getFont();
            setSelStyleValue(TextStyle.FONT_KEY, font);
        }
    }

    /**
     * Returns the color of the current selection or cursor.
     */
    public Color getTextColor()
    {
        TextStyle selStyle = getSelStyle();
        return selStyle.getColor();
    }

    /**
     * Sets the color of the current selection or cursor.
     */
    public void setTextColor(Paint aColor)
    {
        setSelStyleValue(TextStyle.COLOR_KEY, aColor instanceof Color ? aColor : null);
    }

    /**
     * Returns the color of the current selection or cursor.
     */
    public Paint getTextFill()
    {
        TextStyle selStyle = getSelStyle();
        return selStyle.getColor();
    }

    /**
     * Sets the color of the current selection or cursor.
     */
    public void setTextFill(Paint aColor)
    {
        setSelStyleValue(TextStyle.COLOR_KEY, aColor instanceof Color ? aColor : null);
    }

    /**
     * Returns whether current selection is outlined.
     */
    public Border getTextBorder()
    {
        TextStyle selStyle = getSelStyle();
        return selStyle.getBorder();
    }

    /**
     * Sets whether current selection is outlined.
     */
    public void setTextBorder(Border aBorder)
    {
        setSelStyleValue(TextStyle.BORDER_KEY, aBorder);
    }

    /**
     * Returns the format of the current selection or cursor.
     */
    public TextFormat getFormat()
    {
        TextStyle selStyle = getSelStyle();
        return selStyle.getFormat();
    }

    /**
     * Sets the format of the current selection or cursor, after trying to expand the selection to encompass currently
     * selected, @-sign delineated key.
     */
    public void setFormat(TextFormat aFormat)
    {
        // Get format selection range and select it (if non-null)
        TextSel sel = smartFindFormatRange();
        if (sel != null)
            setSel(sel.getStart(), sel.getEnd());

        // Return if we are at end of string (this should never happen)
        if (getSelStart() >= length())
            return;

        // If there is a format, add it to current attributes and set for selected text
        setSelStyleValue(TextStyle.FORMAT_KEY, aFormat);
    }

    /**
     * Returns whether TextView is underlined.
     */
    public boolean isUnderlined()
    {
        TextStyle selStyle = getSelStyle();
        return selStyle.isUnderlined();
    }

    /**
     * Sets whether TextView is underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        setSelStyleValue(TextStyle.UNDERLINE_KEY, aValue ? 1 : 0);
    }

    /**
     * Sets current selection to superscript.
     */
    public void setSuperscript()
    {
        TextStyle selStyle = getSelStyle();
        int state = selStyle.getScripting();
        setSelStyleValue(TextStyle.SCRIPTING_KEY, state == 0 ? 1 : 0);
    }

    /**
     * Sets current selection to subscript.
     */
    public void setSubscript()
    {
        TextStyle selStyle = getSelStyle();
        int state = selStyle.getScripting();
        setSelStyleValue(TextStyle.SCRIPTING_KEY, state == 0 ? -1 : 0);
    }

    /**
     * Returns the text line alignment.
     */
    public HPos getLineAlign()
    {
        TextLineStyle textLineStyle = getSelLineStyle();
        return textLineStyle.getAlign();
    }

    /**
     * Sets the text line alignment.
     */
    public void setLineAlign(HPos anAlign)
    {
        setSelLineStyleValue(TextLineStyle.ALIGN_KEY, anAlign);
    }

    /**
     * Returns whether the text line justifies text.
     */
    public boolean isLineJustify()
    {
        TextLineStyle textLineStyle = getSelLineStyle();
        return textLineStyle.isJustify();
    }

    /**
     * Sets whether the text line justifies text.
     */
    public void setLineJustify(boolean aValue)
    {
        setSelLineStyleValue(TextLineStyle.JUSTIFY_KEY, aValue);
    }

    /**
     * Returns the style at given char index.
     */
    public TextStyle getStyleForCharIndex(int anIndex)
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.getStyleForCharIndex(anIndex);
    }

    /**
     * Returns the TextStyle for the current selection and/or input characters.
     */
    public TextStyle getSelStyle()
    {
        // If already set, just return
        if (_selStyle != null) return _selStyle;

        // Get, set, return
        TextStyle textStyle = getSelStyleImpl();
        return _selStyle = textStyle;
    }

    /**
     * Returns the TextStyle for the current selection and/or input characters.
     */
    protected TextStyle getSelStyleImpl()
    {
        // If empty Sel, get style at SelStart
        TextDoc textDoc = getTextDoc();
        int selStart = getSelStart();
        if (isSelEmpty())
            return getStyleForCharIndex(selStart);

        // Get Run at SelStart
        TextRun selRun = textDoc.getRunForCharIndex(selStart);

        // If SelStart at end of run but not end of line, get next run
        TextLine selLine = selRun.getLine();
        if (selStart == selLine.getStartCharIndex() + selRun.getEnd() && selStart < selLine.getEndCharIndex())
            selRun = selRun.getNext();

        // Return
        return selRun.getStyle();
    }

    /**
     * Sets the attributes that are applied to current selection or newly typed chars.
     */
    public void setSelStyleValue(String aKey, Object aValue)
    {
        // If selection is zero length, just modify input style
        if (isSelEmpty() && isRichText()) {
            TextStyle selStyle = getSelStyleImpl();
            _selStyle = selStyle.copyFor(aKey, aValue);
        }

            // If selection is multiple chars, apply attribute to text and reset SelStyle
        else {
            TextDoc textDoc = getTextDoc();
            textDoc.setStyleValue(aKey, aValue, getSelStart(), getSelEnd());
            _selStyle = null;
            repaint();
        }
    }

    /**
     * Returns the TextLineStyle for currently selection.
     */
    public TextLineStyle getSelLineStyle()
    {
        TextDoc textDoc = getTextDoc();
        return textDoc.getLineStyleForCharIndex(getSelStart());
    }

    /**
     * Sets the line attributes that are applied to current selection or newly typed chars.
     */
    public void setSelLineStyleValue(String aKey, Object aValue)
    {
        TextDoc textDoc = getTextDoc();
        textDoc.setLineStyleValue(aKey, aValue, getSelStart(), getSelEnd());
    }

    /**
     * Adds the given string to end of text.
     */
    public void addChars(String aStr, Object... theAttrs)
    {
        int len = length();
        TextStyle style = getStyleForCharIndex(len).copyFor(theAttrs);
        replaceChars(aStr, style, len, len, true);
    }

    /**
     * Adds the given string with given style to text at given index.
     */
    public void addChars(String aStr, TextStyle aStyle)
    {
        int length = length();
        replaceChars(aStr, aStyle, length, length, true);
    }

    /**
     * Adds the given string with given style to text at given index.
     */
    public void addChars(String aStr, TextStyle aStyle, int anIndex)
    {
        replaceChars(aStr, aStyle, anIndex, anIndex, true);
    }

    /**
     * Deletes the current selection.
     */
    public void delete()
    {
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        delete(selStart, selEnd, true);
    }

    /**
     * Deletes the given range of chars.
     */
    public void delete(int aStart, int anEnd, boolean doUpdateSel)
    {
        replaceChars(null, null, aStart, anEnd, doUpdateSel);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(String aString)
    {
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        replaceChars(aString, null, selStart, selEnd, true);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(String aString, TextStyle aStyle, int aStart, int anEnd, boolean doUpdateSel)
    {
        // Get string length (if no string length and no char range, just return)
        int strLen = aString != null ? aString.length() : 0;
        if (strLen == 0 && aStart == anEnd)
            return;

        // If change is not adjacent to last change, call UndoerSaveChanges
        if ((strLen > 0 && aStart != _lastReplaceIndex) || (strLen == 0 && anEnd != _lastReplaceIndex))
            undoerSaveChanges();

        // Do actual replace chars
        TextStyle style = aStyle != null ? aStyle : aStart == getSelStart() ? getSelStyle() : getStyleForCharIndex(aStart);
        TextDoc textDoc = getTextDoc();
        textDoc.replaceChars(aString, style, aStart, anEnd);

        // Update selection to be at end of new string
        if (doUpdateSel)
            setSel(aStart + strLen);

            // Otherwise, if replace was before current selection, adjust current selection
        else if (aStart <= getSelEnd()) {
            int delta = strLen - (anEnd - aStart);
            int start = getSelStart();
            if (aStart <= start)
                start += delta;
            setSel(start, getSelEnd() + delta);
        }

        // Update LastReplaceIndex
        _lastReplaceIndex = aStart + strLen;
    }

    /**
     * Replaces the current selection with the given TextDoc.
     */
    public void replaceCharsWithTextDoc(TextDoc aTextDoc, int aStart, int anEnd, boolean doUpdateSel)
    {
        int start = aStart;
        int end = anEnd;

        // Iterate over runs and do replace for each one individually
        for (TextLine line : aTextDoc.getLines()) {
            TextRun[] lineRuns = line.getRuns();
            for (TextRun run : lineRuns) {
                String runStr = run.getString();
                TextStyle runStyle = run.getStyle();
                replaceChars(runStr, runStyle, start, end, false);
                start = end = start + run.length();
            }
        }

        // Update selection to be at end of new string
        if (doUpdateSel) {
            int selIndex = aStart + aTextDoc.length();
            setSel(selIndex);
        }
    }

    /**
     * Replaces the current selection with the given contents (TextDoc or String).
     */
    public void replaceCharsWithContent(Object theContent)
    {
        // If Clipboard has TextDoc, paste it
        if (theContent instanceof TextDoc) {
            TextDoc textDoc = (TextDoc) theContent;
            int selStart = getSelStart();
            int selEnd = getSelEnd();
            replaceCharsWithTextDoc(textDoc, selStart, selEnd, true);
        }

        // If Clipboard has String, paste it
        else if (theContent instanceof String) {
            String str = (String) theContent;
            replaceChars(str);
        }

        // Complain about the unknown
        else System.out.println("TextArea.replaceCharsWithContent: Unknown content: " + theContent);
    }

    /**
     * Moves the selection index forward a character (or if a range is selected, moves to end of range).
     */
    public void selectForward(boolean isShiftDown)
    {
        // If shift is down, extend selection forward
        if (isShiftDown) {
            if (getSelAnchor() == getSelStart() && !isSelEmpty())
                setSel(getSelStart() + 1, getSelEnd());
            else {
                setSel(getSelStart(), getSelEnd() + 1);
            }
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
        if (isShiftDown) {
            if (getSelAnchor() == getSelEnd() && !isSelEmpty())
                setSel(getSelStart(), getSelEnd() - 1);
            else {
                setSel(getSelEnd(), getSelStart() - 1);
            }
        }

        // Set new selection
        else setSel(_sel.getCharLeft());
    }

    /**
     * Moves the selection index up a line, trying to preserve distance from beginning of line.
     */
    public void selectUp()
    {
        setSel(_sel.getCharUp());
    }

    /**
     * Moves the selection index down a line, trying preserve distance from beginning of line.
     */
    public void selectDown()
    {
        setSel(_sel.getCharDown());
    }

    /**
     * Moves the insertion point to the beginning of line.
     */
    public void selectLineStart()
    {
        setSel(_sel.getLineStart());
    }

    /**
     * Moves the insertion point to next newline or text end.
     */
    public void selectLineEnd()
    {
        setSel(_sel.getLineEnd());
    }

    /**
     * Deletes the character before of the insertion point.
     */
    public void deleteBackward()
    {
        if (!isSelEmpty()) {
            delete();
            return;
        }

        TextDoc text = getTextDoc();
        int end = getSelStart(); if (end == 0) return;
        int start = end - 1;
        if (text.isAfterLineEnd(end))
            start = text.lastIndexOfNewline(end);

        delete(start, end, true);
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

        TextDoc text = getTextDoc();
        int start = getSelStart(); if (start >= length()) return;
        int end = start + 1;
        if (text.isLineEnd(end - 1))
            end = text.indexAfterNewline(end - 1);

        delete(start, end, true);
    }

    /**
     * Deletes the characters from the insertion point to the end of the line.
     */
    public void deleteToLineEnd()
    {
        // If there is a current selection, just delete it
        TextDoc text = getTextDoc();
        if (!isSelEmpty())
            delete();

            // Otherwise, if at line end, delete line end
        else if (text.isLineEnd(getSelEnd()))
            delete(getSelStart(), text.indexAfterNewline(getSelStart()), true);

            // Otherwise delete up to next newline or line end
        else {
            int index = text.indexOfNewline(getSelStart());
            delete(getSelStart(), index >= 0 ? index : length(), true);
        }
    }

    /**
     * Clears the text.
     */
    public void clear()
    {
        Undoer undoer = getUndoer();
        if (undoer != null && undoer.isEnabled())
            undoer.disable();
        else undoer = null;
        getTextDoc().clear();
        if (undoer != null)
            undoer.enable();
    }

    /**
     * Returns the number of lines.
     */
    public int getLineCount()
    {
        TextBox textBox = getTextBox();
        return textBox.getLineCount();
    }

    /**
     * Returns the individual line at given index.
     */
    public TextBoxLine getLine(int anIndex)
    {
        TextBox textBox = getTextBox();
        return textBox.getLine(anIndex);
    }

    /**
     * Returns the last line.
     */
    public TextBoxLine getLineLast()
    {
        TextBox textBox = getTextBox();
        return textBox.getLineLast();
    }

    /**
     * Returns the line for the given character index.
     */
    public TextBoxLine getLineAt(int anIndex)
    {
        TextBox textBox = getTextBox();
        return textBox.getLineForCharIndex(anIndex);
    }

    /**
     * Returns the token for given character index.
     */
    public TextBoxToken getTokenAt(int anIndex)
    {
        TextBox textBox = getTextBox();
        return textBox.getTokenAt(anIndex);
    }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public int getCharIndex(double anX, double aY)
    {
        TextBox textBox = getTextBox();
        return textBox.getCharIndex(anX, aY);
    }

    /**
     * Returns the link at given XY.
     */
    public TextLink getLinkAtXY(double aX, double aY)
    {
        // If not RichText, just return
        if (!isRichText()) return null;

        // Get TextStyle at XY and return link
        int charIndex = getCharIndex(aX, aY);
        TextStyle textStyle = getStyleForCharIndex(charIndex);
        return textStyle.getLink();
    }

    /**
     * Returns the selection color.
     */
    public Color getSelColor()  { return _selColor; }

    /**
     * Paint text.
     */
    protected void paintFront(Painter aPntr)
    {
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
        // If not editable, just return
        if (!isEditable()) return;

        // Get Selection and path
        TextSel sel = getSel();
        Shape spath = sel.getPath();

        // If empty selection, paint carat
        if (sel.isEmpty()) {
            if (isShowCaret()) {
                aPntr.setPaint(Color.BLACK);
                aPntr.setStroke(Stroke.Stroke1);
                aPntr.draw(spath);
            }
        }

        // Otherwise
        else {
            aPntr.setPaint(getSelColor());
            aPntr.fill(spath);
        }
    }

    /**
     * Process event. Make this public so TextArea can be used to edit text outside of normal Views.
     */
    public void processEvent(ViewEvent anEvent)
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
        if (anEvent.isMouseEvent()) anEvent.consume();
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
        if (!anEvent.isShiftDown())
            _wordSel = _pgraphSel = false;
        if (anEvent.getClickCount() == 2)
            _wordSel = true;
        else if (anEvent.getClickCount() == 3)
            _pgraphSel = true;

        // Get selected range for down point and drag point
        TextSel sel = new TextSel(_textBox, _downX, _downY, _downX, _downY, _wordSel, _pgraphSel);
        int anchor = sel.getAnchor();
        int index = sel.getIndex();

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
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
        TextSel sel = new TextSel(_textBox, _downX, _downY, anEvent.getX(), anEvent.getY(), _wordSel, _pgraphSel);
        int anchor = sel.getAnchor();
        int index = sel.getIndex();

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
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
        setCaretAnim();
        _downX = _downY = 0;

        if (anEvent.isMouseClick()) {
            TextLink textLink = getLinkAtXY(anEvent.getX(), anEvent.getY());
            if (textLink != null)
                openLink(textLink.getString());
        }
    }

    /**
     * Handle MouseMoved.
     */
    protected void mouseMoved(ViewEvent anEvent)
    {
        TextLink textLink = getLinkAtXY(anEvent.getX(), anEvent.getY());
        if (textLink != null)
            setCursor(Cursor.HAND);
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
        boolean emacsDown = SnapUtils.isWindows ? anEvent.isAltDown() : controlDown;
        boolean shiftDown = anEvent.isShiftDown();
        setCaretAnim(false);
        setShowCaret(isCaretNeeded());

        // Handle command keys
        if (commandDown) {

            // If shift-down, just return
            if (shiftDown && keyCode != KeyCode.Z) return;

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
        else if (emacsDown) {

            // If shift down, just return
            if (shiftDown) return;

            // Handle common emacs key bindings
            switch (keyCode) {
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
        else switch (keyCode) {
                case KeyCode.TAB:
                    replaceChars("\t");
                    anEvent.consume();
                    break;
                case KeyCode.ENTER:
                    if (isFireActionOnEnterKey()) {
                        selectAll();
                        fireActionEvent(anEvent);
                    } else {
                        replaceChars("\n");
                        anEvent.consume();
                    }
                    break; // Handle enter
                case KeyCode.LEFT:
                    selectBackward(shiftDown);
                    anEvent.consume();
                    break; // Handle left arrow
                case KeyCode.RIGHT:
                    selectForward(shiftDown);
                    anEvent.consume();
                    break; // Handle right arrow
                case KeyCode.UP:
                    selectUp();
                    anEvent.consume();
                    break; // Handle up arrow
                case KeyCode.DOWN:
                    selectDown();
                    anEvent.consume();
                    break; // Handle down arrow
                case KeyCode.HOME:
                    selectLineStart();
                    break; // Handle home key
                case KeyCode.END:
                    selectLineEnd();
                    break; // Handle end key
                case KeyCode.BACK_SPACE:
                    deleteBackward();
                    anEvent.consume();
                    break; // Handle Backspace key
                case KeyCode.DELETE:
                    deleteForward();
                    anEvent.consume();
                    break; // Handle Delete key
                case KeyCode.SPACE:
                    anEvent.consume();
                    break; // I have no idea why ScrollPane scrolls if this isn't called
                default:
                    return; // Any other non-character key, just return
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
        boolean commandDown = anEvent.isShortcutDown();
        boolean controlDown = anEvent.isControlDown();
        boolean emacsDown = SnapUtils.isWindows ? anEvent.isAltDown() : controlDown;

        // If actual text entered, replace
        if (charDefined && !commandDown && !controlDown && !emacsDown) {
            replaceChars(keyChars);
            hideCursor(); //anEvent.consume();
        }
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
     * Returns whether to show carat.
     */
    public boolean isShowCaret()  { return _showCaret; }

    /**
     * Sets whether to show carat.
     */
    protected void setShowCaret(boolean aValue)
    {
        if (aValue == _showCaret) return;
        _showCaret = aValue;
        repaintSel();
    }

    /**
     * Returns whether caret is needed (true when text is focused, showing and empty selection).
     */
    private boolean isCaretNeeded()
    {
        return isFocused() && getSel().isEmpty() && isShowing();
    }

    /**
     * Sets the caret animation to whether it's needed.
     */
    private void setCaretAnim()
    {
        boolean show = isCaretNeeded();
        setCaretAnim(show);
    }

    /**
     * Returns whether caret is flashing.
     */
    public boolean isCaretAnim()  { return _caretTimer!=null; }

    /**
     * Sets whether caret is flashing.
     */
    public void setCaretAnim(boolean aValue)
    {
        // If already set, just return
        if (aValue == isCaretAnim()) return;

        // If setting
        if (aValue) {
            _caretTimer = new ViewTimer(500, t -> setShowCaret(!isShowCaret()));
            _caretTimer.start();
            setShowCaret(true);
        }

        // If stopping
        else {
            _caretTimer.stop();
            _caretTimer = null;
            setShowCaret(false);
        }

        // Repaint selection
        repaintSel();
    }

    /**
     * Returns the font scale of the text box.
     */
    public double getFontScale()
    {
        return getTextBox().getFontScale();
    }

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
    public void scaleTextToFit()
    {
        getTextBox().scaleTextToFit();
        relayoutParent();
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
        // If no selection, just return
        if (isSelEmpty()) return;

        // Get TextDoc for selected characters and get as XML string and plain string
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        TextDoc textDoc = getTextDoc();

        // Add to clipboard as rich text and String (text/plain)
        Clipboard clipboard = Clipboard.getCleared();

        // Add rich text
        if (textDoc instanceof RichText) {
            RichText richText = (RichText) textDoc;
            TextDoc richTextForRange = richText.copyForRange(selStart, selEnd);
            XMLElement xml = new XMLArchiver().toXML(richTextForRange);
            String xmlStr = xml.getString();
            clipboard.addData(SNAP_RICHTEXT_TYPE, xmlStr);
        }

        // Add plain text
        String textString = textDoc.subSequence(selStart, selEnd).toString();
        clipboard.addData(textString);
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

        // Clear last undo set so paste doesn't get lumped in to coalescing
        undoerSaveChanges();

        // Get clipboard content
        Object content = getClipboardContent(clipboard);

        // Paste clipboard content
        replaceCharsWithContent(content);
    }

    /**
     * Returns the clipboard content.
     */
    protected Object getClipboardContent(Clipboard clipboard)
    {
        // If Clipboard has TextDoc, paste it
        if (clipboard.hasData(SNAP_RICHTEXT_TYPE)) {
            byte[] bytes = clipboard.getDataBytes(SNAP_RICHTEXT_TYPE);
            if (bytes != null && bytes.length > 0) {  // Shouldn't need this - Happens when pasting content from prior instance
                RichText richText = new RichText();
                XMLArchiver archiver = new XMLArchiver();
                archiver.setRootObject(richText);
                archiver.readFromXMLBytes(bytes);
                return  richText;
            }
        }

        // If Clipboard has String, paste it
        if (clipboard.hasString()) {
            String str = clipboard.getString();
            if (str != null && str.length() > 0)
                return  str;
        }

        // Return not found
        return null;
    }

    /**
     * Opens a given link.
     */
    protected void openLink(String aLink)
    {
        System.out.println("Open Link: " + aLink);
    }

    /**
     * Returns the undoer.
     */
    public Undoer getUndoer()  { return _undoer; }

    /**
     * Called to undo the last edit operation in the editor.
     */
    public boolean undo()
    {
        undoerSaveChanges();
        boolean b = _undoer.undo() != null;
        if (!b) ViewUtils.beep();
        return b;
    }

    /**
     * Called to redo the last undo operation in the editor.
     */
    public boolean redo()
    {
        undoerSaveChanges();
        boolean b = _undoer.redo() != null;
        if (!b) ViewUtils.beep();
        return b;
    }

    /**
     * Adds a property change to undoer.
     */
    protected void undoerAddPropertyChange(PropChange anEvent)
    {
        // Get undoer (just return if null or disabled)
        Undoer undoer = getUndoer();
        if (undoer == null || !undoer.isEnabled()) return;

        // If PlainText Style_Prop or LineStyle_Prop, just return
        String propName = anEvent.getPropName();
        if (!isRichText() && (propName == TextDoc.Style_Prop || propName == TextDoc.LineStyle_Prop))
            return;

        // Get ActiveUndoSet - if no previous changes, set UndoSelection
        UndoSet activeUndoSet = undoer.getActiveUndoSet();
        if (activeUndoSet.getChangeCount() == 0)
            activeUndoSet.setUndoSelection(getUndoSelection());

        // Add property
        undoer.addPropChange(anEvent);
    }

    /**
     * Saves changes to undoer.
     */
    public void undoerSaveChanges()
    {
        Undoer undoer = getUndoer();
        if (undoer == null) return;
        UndoSet activeUndoSet = undoer.getActiveUndoSet();
        activeUndoSet.setRedoSelection(getUndoSelection());
        undoer.saveChanges();
    }

    /**
     * Returns a selection object for undoer.
     */
    protected Object getUndoSelection()
    {
        return new UndoTextSel();
    }

    /**
     * A class to act as text selection.
     */
    public class UndoTextSel implements Undoer.Selection {

        // Ivars
        public int start = getSelStart(), end = getSelEnd();  // Use ivars to avoid min()

        // SetSel
        public void setSelection()
        {
            TextArea.this.setSel(start, end);
        }

        // Equals
        public boolean equals(Object anObj)
        {
            UndoTextSel other = anObj instanceof UndoTextSel ? (UndoTextSel) anObj : null;
            return other != null && start == other.start && end == other.end;
        }

        // HashCode
        public int hashCode()  { return start + end; }
    }

    /**
     * Called when TextDoc changes (chars added, updated or deleted).
     */
    protected void textDocDidPropChange(PropChange aPC)
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
        relayoutParent();
        repaint();
    }

    /**
     * Overrride to return Arial 11.
     */
    public Font getDefaultFont()  { return Font.Arial11; }

    /**
     * Override to return white.
     */
    public Paint getDefaultFill()
    {
        return isEditable() ? Color.WHITE : null;
    }

    /**
     * Returns the width needed to display all characters.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double h = aH >= 0 ? (aH - ins.top - ins.bottom) : aH;
        double pw = getTextBox().getPrefWidth(h);
        return ins.left + pw + ins.right;
    }

    /**
     * Returns the height needed to display all characters.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double w = aW >= 0 ? (aW - ins.left - ins.right) : aW;
        double ph = getTextBox().getPrefHeight(w);
        return ins.top + ph + ins.bottom;
    }

    /**
     * Override to update getTextBlock.Rect.
     */
    public void setWidth(double aWidth)
    {
        super.setWidth(aWidth);
        setTextBoxBounds();
    }

    /**
     * Override to update getTextBlock.Rect.
     */
    public void setHeight(double aHeight)
    {
        super.setHeight(aHeight);
        setTextBoxBounds();
    }

    /**
     * Sets the Text.Rect from text area.
     */
    protected Rect getTextBoxBounds()
    {
        Insets ins = getInsetsAll();
        double x = ins.left, w = getWidth() - x - ins.right;
        double y = ins.top, h = getHeight() - y - ins.bottom;
        return new Rect(x, y, w, h);
    }

    /**
     * Sets the Text.Rect from text area.
     */
    protected void setTextBoxBounds()
    {
        TextBox tbox = getTextBox();
        Rect tbnds = getTextBoxBounds();
        tbox.setBounds(tbnds);
    }

    /**
     * Override to update font.
     */
    protected void setParent(ParentView aPar)
    {
        // Do normal version
        super.setParent(aPar);

        // If PlainText, update to parent (should probably watch parent Font_Prop change as well)
        if (!isRichText() && !isFontSet() && !getFont().equals(getSelStyle().getFont()))
            setSelStyleValue(TextStyle.FONT_KEY, getFont());
    }

    /**
     * Override to check caret animation and scrollSelToVisible when showing.
     */
    protected void setShowing(boolean aValue)
    {
        if (aValue == isShowing()) return;
        super.setShowing(aValue);
        if (isFocused()) setCaretAnim();
        if (aValue && getSelStart() != 0)
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
        if (aValue == isFocused()) return;
        super.setFocused(aValue);

        // Update caret
        setShowCaret(false);
        setCaretAnim();

        // Repaint ?
        repaint();

        // Handle FireActionOnFocusLost
        if (_fireActionOnFocusLost) {

            // If focus gained, set FocusedGainedValue and select all (if not from mouse press)
            if (aValue) {
                _focusGainedText = getText();
                if (!ViewUtils.isMouseDown()) selectAll();
            }

            // If focus lost and FocusGainedVal changed, fire action
            else if (!SnapUtils.equals(_focusGainedText, getText()))
                fireActionEvent(null);
        }
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return "Text"; }

    /**
     * Returns the path for the current selection.
     */
    public Shape getSelPath()
    {
        return getSel().getPath();
    }

    /**
     * Returns a path for misspelled word underlining.
     */
    public Shape getSpellingPath()
    {
        // Get text box and text string and path object
        TextBox tbox = getTextBox();
        String string = tbox.getString();
        Path path = new Path();

        // Iterate over text
        for (SpellCheck.Word word = SpellCheck.getMisspelledWord(string, 0); word != null;
             word = SpellCheck.getMisspelledWord(string, word.getEnd())) {

            // Get word bounds
            int start = word.getStart();
            if (start >= tbox.getEndCharIndex()) break;
            int end = word.getEnd();
            if (end > tbox.getEndCharIndex()) end = tbox.getEndCharIndex();

            // If text editor selection starts in word bounds, just continue - they are still working on this word
            if (start <= getSelStart() && getSelStart() <= end)
                continue;

            // Get the selection's start line index and end line index
            int startLineIndex = getLineAt(start).getIndex();
            int endLineIndex = getLineAt(end).getIndex();

            // Iterate over selected lines
            for (int i = startLineIndex; i <= endLineIndex; i++) {
                TextBoxLine line = getLine(i);

                // Get the bounds of line
                double x1 = line.getX();
                double x2 = line.getMaxX();
                double y = line.getBaseline() + 3;

                // If starting line, adjust x1 for starting character
                if (i == startLineIndex)
                    x1 = line.getXForChar(start - line.getStartCharIndex() - tbox.getStartCharIndex());

                // If ending line, adjust x2 for ending character
                if (i == endLineIndex)
                    x2 = line.getXForChar(end - line.getStartCharIndex() - tbox.getStartCharIndex());

                // Append rect for line to path
                path.moveTo(x1, y);
                path.lineTo(x2, y);
            }
        }

        // Return path
        return path;
    }

    /**
     * This method returns the range of the @-sign delinated key closest to the current selection (or null if not found).
     */
    private TextSel smartFindFormatRange()
    {
        int selStart = getSelStart(), selEnd = getSelEnd();
        int prevAtSignIndex = -1, nextAtSignIndex = -1;
        String string = getText();

        // See if selection contains an '@'
        if (selEnd > selStart)
            prevAtSignIndex = string.indexOf("@", selStart);
        if (prevAtSignIndex >= selEnd)
            prevAtSignIndex = -1;

        // If there wasn't an '@' in selection, see if there is one before the selected range
        if (prevAtSignIndex < 0)
            prevAtSignIndex = string.lastIndexOf("@", selStart - 1);

        // If there wasn't an '@' in or before selection, see if there is one after the selected range
        if (prevAtSignIndex < 0)
            prevAtSignIndex = string.indexOf("@", selEnd);

        // If there is a '@' in, before or after selection, see if there is another after it
        if (prevAtSignIndex >= 0)
            nextAtSignIndex = string.indexOf("@", prevAtSignIndex + 1);

        // If there is a '@' in, before or after selection, but not one after it, see if there is one before that
        if (prevAtSignIndex >= 0 && nextAtSignIndex < 0)
            nextAtSignIndex = string.lastIndexOf("@", prevAtSignIndex - 1);

        // If both a previous and next '@', select the chars inbetween
        if (prevAtSignIndex >= 0 && nextAtSignIndex >= 0 && prevAtSignIndex != nextAtSignIndex) {
            int start = Math.min(prevAtSignIndex, nextAtSignIndex);
            int end = Math.max(prevAtSignIndex, nextAtSignIndex);
            return new TextSel(_textBox, start, end + 1);
        }

        // Return null since range not found
        return null;
    }

    /**
     * Returns whether layout tries to hyphenate wrapped words.
     */
    public static boolean isHyphenating()  { return _hyphenating; }

    /**
     * Sets whether layout tries to hyphenate wrapped words.
     */
    public static void setHyphenating(boolean aValue)
    {
        Prefs.get().setValue("Hyphenating", _hyphenating = aValue);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getText();
        if (str.length() > 40) str = str.substring(0, 40) + "...";
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
            return DEFAULT_TEXT_AREA_PADDING;

        // Do normal version
        return super.getPropDefault(aPropName);
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive Rich, Editable, WrapLines
        if (isRichText()) e.add("Rich", true);
        if (!isEditable()) e.add("Editable", false);
        if (isWrapLines()) e.add(WrapLines_Prop, true);

        // If RichText, archive rich text
        if (isRichText()) {
            e.removeElement("font");
            XMLElement richTextXML = anArchiver.toXML(getTextDoc());
            richTextXML.setName("RichText");
            if (richTextXML.size() > 0)
                e.add(richTextXML); //for (int i=0, iMax=rtxml.size(); i<iMax; i++) e.add(rtxml.get(i));
        }

        // Otherwise, archive text string
        else if (getText() != null && getText().length() > 0) e.add("text", getText());

        // Archive FireActionOnEnterKey, FireActionOnFocusLost
        if (isFireActionOnEnterKey()) e.add(FireActionOnEnterKey_Prop, true);
        if (isFireActionOnFocusLost()) e.add(FireActionOnFocusLost_Prop, true);
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
        XMLElement richTextXML = anElement.get("RichText");
        if (richTextXML == null && anElement.get("string") != null)
            richTextXML = anElement;
        if (richTextXML != null)
            setRichText(true);

        // Unarchive Rich, Editable, WrapLines
        if (anElement.hasAttribute("Rich")) setRichText(anElement.getAttributeBoolValue("Rich"));
        if (anElement.hasAttribute("Editable")) setEditable(anElement.getAttributeBoolValue("Editable"));
        if (anElement.hasAttribute(WrapLines_Prop)) setWrapLines(anElement.getAttributeBoolValue(WrapLines_Prop));
        if (anElement.hasAttribute("WrapText")) setWrapLines(anElement.getAttributeBoolValue("WrapText"));

        // If Rich, unarchive rich text
        if (isRichText() && richTextXML != null) {
            RichText richText = (RichText) getTextDoc();
            getUndoer().disable();
            richText.fromXML(anArchiver, richTextXML);
            getUndoer().enable();
        }

        // Otherwise unarchive text. Text can be "text" or "value" attribute, or as content (CDATA or otherwise)
        else {
            String str = anElement.getAttributeValue("text", anElement.getAttributeValue("value", anElement.getValue()));
            if (str != null && str.length() > 0)
                setText(str);
        }

        // Unarchive FireActionOnEnterKey, FireActionOnFocusLost
        if (anElement.hasAttribute(FireActionOnEnterKey_Prop))
            setFireActionOnEnterKey(anElement.getAttributeBoolValue(FireActionOnEnterKey_Prop, true));
        if (anElement.hasAttribute(FireActionOnFocusLost_Prop))
            setFireActionOnFocusLost(anElement.getAttributeBoolValue(FireActionOnFocusLost_Prop, true));
        return this;
    }
}