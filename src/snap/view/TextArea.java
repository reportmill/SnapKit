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
import java.util.List;
import java.util.Objects;

/**
 * A view subclass for displaying and editing a TextBlock.
 */
public class TextArea extends View {

    // The text being edited
    private TextBlock _textBlock;

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

    // The mouse down point
    private double  _downX, _downY;

    // The runnable for caret flashing
    private Runnable _caretRun;

    // Whether to show text insertion point caret
    private boolean  _showCaret;

    // Whether to send action on enter key press
    private boolean  _fireActionOnEnterKey;

    // Whether to send action on focus lost (if content changed)
    private boolean  _fireActionOnFocusLost;

    // The content on focus gained
    private String  _focusGainedText;

    // A helper class for key processing
    private TextAreaKeys  _keys = createTextAreaKeys();

    // Whether as-you-type spell checking is enabled
    public static boolean  isSpellChecking = Prefs.getDefaultPrefs().getBoolean("SpellChecking", false);

    // Whether hyphenating is activated
    static boolean  _hyphenating = Prefs.getDefaultPrefs().getBoolean("Hyphenating", false);

    // The MIME type for SnapKit RichText
    public static final String  SNAP_RICHTEXT_TYPE = "reportmill/xstring";

    // The PropChangeListener to catch SourceText PropChanges.
    private PropChangeListener _sourceTextPropLsnr = pc -> sourceTextDidPropChange(pc);

    // A PropChangeListener to enable/disable caret when window loses focus
    private PropChangeListener  _windowFocusedChangedLsnr;

    // A pointer to window this TextArea is showing in so we can remove WindowFocusChangedLsnr
    private WindowView  _showingWindow;

    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String WrapLines_Prop = "WrapLines";
    public static final String FireActionOnEnterKey_Prop = "FireActionOnEnterKey";
    public static final String FireActionOnFocusLost_Prop = "FireActionOnFocusLost";
    public static final String SourceText_Prop = "SourceText";
    public static final String Selection_Prop = "Selection";

    // Constants for property defaults
    private static final Insets DEFAULT_TEXT_AREA_PADDING = new Insets(2);

    /**
     * Creates a new TextArea.
     */
    public TextArea()
    {
        super();
        _padding = DEFAULT_TEXT_AREA_PADDING;

        // Configure
        setFocusPainted(false);

        // Create/set default TextBlock
        _textBlock = new TextBox();
        _textBlock.getSourceText().addPropChangeListener(_sourceTextPropLsnr);
        _textBlock.activateUndo();
    }

    /**
     * Returns the text block that holds the text.
     */
    public TextBlock getTextBlock()  { return _textBlock; }

    /**
     * Sets the text block that holds the text.
     */
    protected void setTextBlock(TextBlock aTextBlock)
    {
        // If already set, just return
        if (aTextBlock == _textBlock) return;

        // Update new TextBlock.ParentTextStyle
        if (aTextBlock != null) {
            View parent = getParent();
            if (isFontSet() || parent != null) {
                Font font = isFontSet() ? getFont() : parent.getFont();
                TextStyle parentTextStyle = aTextBlock.getParentTextStyle().copyFor(font);
                aTextBlock.setParentTextStyle(parentTextStyle);
            }
        }

        // Remove PropChangeListener
        if (_textBlock != null)
            _textBlock.getSourceText().removePropChangeListener(_sourceTextPropLsnr);

        // Set new text block
        _textBlock = aTextBlock;

        // Add PropChangeListener
        _textBlock.getSourceText().addPropChangeListener(_sourceTextPropLsnr);

        // Reset selection (to line end if single-line, otherwise text start)
        int selIndex = getLineCount() == 1 && length() < 40 ? length() : 0;
        setSel(selIndex);

        // Activate undo
        _textBlock.getSourceText().activateUndo();

        // Relayout parent, repaint
        relayoutParent();
        repaint();
    }

    /**
     * Returns the root text block.
     */
    public TextBlock getSourceText()  { return _textBlock.getSourceText(); }

    /**
     * Sets the source TextBlock.
     */
    public void setSourceText(TextBlock aTextBlock)
    {
        // If already set, just return
        TextBlock oldSourceText = getSourceText();
        if (aTextBlock == oldSourceText) return;

        // Get appropriate text block for source text
        TextBlock textBlock = aTextBlock;
        if (isWrapLines() && !(aTextBlock instanceof TextBox)) {
            TextBox textBox = new TextBox();
            textBox.setWrapLines(true);
            textBox.setSourceText(aTextBlock);
            textBlock = textBox;
        }

        // Set text block
        setTextBlock(textBlock);

        // FirePropChange
        firePropChange(SourceText_Prop, oldSourceText, aTextBlock);
    }

    /**
     * Returns the plain string of the text being edited.
     */
    public String getText()  { return _textBlock.getString(); }

    /**
     * Set text string of text editor.
     */
    public void setText(String aString)
    {
        // If string already set, just return
        String str = aString != null ? aString : "";
        if (str.length() == length() && (str.length() == 0 || str.equals(getText()))) return;

        // Set string and notify textDidChange
        _textBlock.setString(aString);
        textDidChange();

        // Reset selection (to line end if single-line, otherwise text start)
        int selIndex = _textBlock.getLineCount() == 1 && length() < 40 ? length() : 0;
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
        // If already set, just return
        if (aValue == _wrapLines) return;

        // Set and fire prop change
        firePropChange(WrapLines_Prop, _wrapLines, _wrapLines = aValue);

        // If already TextBox, just forward to TextBox
        if (_textBlock instanceof TextBox)
            ((TextBox) _textBlock).setWrapLines(aValue);

        // Otherwise, wrap text in text box and set new text box
        else if (aValue) {
            TextBox textBox = new TextBox();
            textBox.setSourceText(_textBlock);
            textBox.setWrapLines(true);
            setTextBlock(textBox);
        }
    }

    /**
     * Returns whether editor is doing check-as-you-type spelling.
     */
    public boolean isSpellChecking()  { return isSpellChecking; }

    /**
     * Returns whether text supports multiple styles.
     */
    public boolean isRichText()  { return _textBlock.isRichText(); }

    /**
     * Returns the default style for text.
     */
    public TextStyle getDefaultStyle()  { return _textBlock.getDefaultStyle(); }

    /**
     * Sets the default style.
     */
    public void setDefaultStyle(TextStyle aStyle)  { _textBlock.setDefaultStyle(aStyle); }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _textBlock.getDefaultLineStyle(); }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)  { _textBlock.setDefaultLineStyle(aLineStyle); }

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
        firePropChange(FireActionOnEnterKey_Prop, _fireActionOnEnterKey, _fireActionOnEnterKey = aValue);

        // Update Actionable
        setActionable(isFireActionOnEnterKey() || isFireActionOnFocusLost());
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
    public int length()  { return _textBlock.length(); }

    /**
     * Returns the individual character at given index.
     */
    public char charAt(int anIndex)  { return _textBlock.charAt(anIndex); }

    /**
     * Returns whether the selection is empty.
     */
    public boolean isSelEmpty()  { return getSelStart() == getSelEnd(); }

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
    public TextSel getSel()
    {
        if (_sel != null) return _sel;
        TextSel sel = new TextSel(_textBlock, _selAnchor, _selIndex);
        return _sel = sel;
    }

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

        // Reset mouse point
        TextBlockUtils.setMouseY(_textBlock, 0);

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
    public void selectAll()  { setSel(0, length()); }

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
        // Get visible rect - if no reason to scroll, just return
        Rect visRect = getClipAllBounds();
        double viewW = getWidth();
        double viewH = getHeight();
        if (visRect == null || visRect.isEmpty() || visRect.width == viewW && visRect.height == viewH)
            return;

        // Get selection rect with healthy margin, constrained to bounds
        TextSel textSel = getSel();
        Rect selRect = textSel.getPath().getBounds();
        selRect.inset(-72);
        selRect.x = Math.max(selRect.x, 0);
        selRect.y = Math.max(selRect.y, 0);
        if (selRect.getMaxX() > viewW) selRect.width = viewW - selRect.x;
        if (selRect.getMaxY() > viewH) selRect.height = viewH - selRect.y;

        // If sel rect covers whole width, cancel horizontal scroll
        if (selRect.x == 0 && selRect.width == viewW) {
            selRect.x = visRect.x;
            selRect.width = visRect.width;
        }

        // If selection rect not fully contained in visible rect, scrollRectToVisible
        if (!visRect.contains(selRect))
            scrollToVisible(selRect);
    }

    /**
     * Returns the font of the current selection or cursor.
     */
    public Font getFont()
    {
        // Handle RichText: Return SelStyle.Font
        if (isRichText()) {
            TextStyle selStyle = getSelStyle();
            return selStyle.getFont();
        }

        // Handle plain text with DefaultStyleSet: Return DefaultStyle.Font
        if (_textBlock.isDefaultTextStyleSet()) {
            TextStyle textStyle = _textBlock.getDefaultStyle();
            return textStyle.getFont();
        }

        // Do normal version
        return super.getFont();
    }

    /**
     * Sets the font of the current selection or cursor.
     */
    public void setFont(Font aFont)
    {
        // Handle RichText: just update SelStyle.Font and return
        if (isRichText()) {
            if (aFont != null)
                setSelStyleValue(TextStyle.FONT_KEY, aFont);
            return;
        }

        // Update TextBlock.DefaultTextStyle, ParentTextStyle
        _textBlock.setPropChangeEnabled(false);
        TextStyle defaultTextStyle = aFont != null ? _textBlock.getDefaultStyle().copyFor(aFont) : null;
        _textBlock.setDefaultStyle(defaultTextStyle);

        // Do normal version
        super.setFont(aFont);

        // Update TextBlock.ParentTextStyle
        TextStyle parentTextStyle = defaultTextStyle != null ? defaultTextStyle : _textBlock.getParentTextStyle().copyFor(aFont);
        _textBlock.setParentTextStyle(parentTextStyle);
        _textBlock.setPropChangeEnabled(true);
    }

    /**
     * Override to update font.
     */
    @Override
    protected void parentFontChanged()
    {
        // Handle RichText: Just return
        if (isRichText()) return;

        // Do normal version
        super.parentFontChanged();

        // Update TextBlock.ParentTextStyle
        Font font = getFont();
        TextStyle parentTextStyle = _textBlock.getParentTextStyle().copyFor(font);
        _textBlock.setParentTextStyle(parentTextStyle);
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
        TextSel sel = TextAreaUtils.smartFindFormatRange(this);
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
        return _textBlock.getStyleForCharIndex(anIndex);
    }

    /**
     * Returns the TextStyle for the current selection and/or input characters.
     */
    public TextStyle getSelStyle()
    {
        // If already set, just return
        if (_selStyle != null) return _selStyle;

        // Get style for sel range
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        TextStyle selStyle = _textBlock.getStyleForCharRange(selStart, selEnd);

        // Set/return
        return _selStyle = selStyle;
    }

    /**
     * Sets the attributes that are applied to current selection or newly typed chars.
     */
    public void setSelStyleValue(String aKey, Object aValue)
    {
        // If selection is zero length, just modify input style
        if (isSelEmpty() && isRichText()) {
            TextStyle selStyle = getSelStyle();
            _selStyle = selStyle.copyFor(aKey, aValue);
        }

        // If selection is multiple chars, apply attribute to text and reset SelStyle
        else {
            _textBlock.setStyleValue(aKey, aValue, getSelStart(), getSelEnd());
            _selStyle = null;
            repaint();
        }
    }

    /**
     * Returns the TextLineStyle for currently selection.
     */
    public TextLineStyle getSelLineStyle()
    {
        return _textBlock.getLineStyleForCharIndex(getSelStart());
    }

    /**
     * Sets the line attributes that are applied to current selection or newly typed chars.
     */
    public void setSelLineStyleValue(String aKey, Object aValue)
    {
        _textBlock.setLineStyleValue(aKey, aValue, getSelStart(), getSelEnd());
    }

    /**
     * Adds the given string to end of text.
     */
    public void addChars(CharSequence theChars, Object... theAttrs)
    {
        int len = length();
        TextStyle style = getStyleForCharIndex(len).copyFor(theAttrs);
        replaceChars(theChars, style, len, len, true);
    }

    /**
     * Adds the given string with given style to text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle aStyle)
    {
        int length = length();
        replaceChars(theChars, aStyle, length, length, true);
    }

    /**
     * Adds the given string with given style to text at given index.
     */
    public void addChars(CharSequence theChars, TextStyle aStyle, int anIndex)
    {
        replaceChars(theChars, aStyle, anIndex, anIndex, true);
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
    public void replaceChars(CharSequence theChars)
    {
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        replaceChars(theChars, null, selStart, selEnd, true);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(CharSequence theChars, TextStyle aStyle, int aStart, int anEnd, boolean doUpdateSel)
    {
        // Get string length (if no string length and no char range, just return)
        int strLen = theChars != null ? theChars.length() : 0;
        if (strLen == 0 && aStart == anEnd)
            return;

        // Get style (might need SelStyle if replacing empty selection)
        TextStyle style = aStyle;
        if (style == null) {
            if (aStart == getSelStart())
                style = getSelStyle();
            else style = _textBlock.getStyleForCharRange(aStart, anEnd);
        }

        // Forward to TextBlock replaceChars()
        _textBlock.replaceChars(theChars, style, aStart, anEnd);

        // Update selection to be at end of new string
        if (doUpdateSel)
            setSel(aStart + strLen);

        // Otherwise, if replace was before current selection, adjust current selection
        else if (aStart < getSelEnd()) {
            int delta = strLen - (anEnd - aStart);
            int start = getSelStart();
            if (aStart < start)
                start += delta;
            setSel(start, getSelEnd() + delta);
        }
    }

    /**
     * Replaces the current selection with the given contents (TextBlock or String).
     */
    public void replaceCharsWithContent(Object theContent)
    {
        // If Clipboard has TextBlock, paste it
        if (theContent instanceof TextBlock) {
            TextBlock textBlock = (TextBlock) theContent;
            int selStart = getSelStart();
            int selEnd = getSelEnd();
            _textBlock.removeChars(selStart, selEnd);
            _textBlock.addTextBlock(textBlock, selStart);
            setSel(selStart, selStart + textBlock.length());
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
        else {
            int charIndex = _sel.getCharRight();
            setSel(charIndex);
        }
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
        else {
            int charIndex = _sel.getCharLeft();
            setSel(charIndex);
        }
    }

    /**
     * Moves the selection index up a line, trying to preserve distance from beginning of line.
     */
    public void selectUp()
    {
        int charIndex = _sel.getCharUp();
        setSel(charIndex);
    }

    /**
     * Moves the selection index down a line, trying preserve distance from beginning of line.
     */
    public void selectDown()
    {
        int charIndex = _sel.getCharDown();
        setSel(charIndex);
    }

    /**
     * Moves the insertion point to the beginning of line.
     */
    public void selectLineStart()
    {
        int charIndex = _sel.getLineStart();
        setSel(charIndex);
    }

    /**
     * Moves the insertion point to next newline or text end.
     */
    public void selectLineEnd()
    {
        int charIndex = _sel.getLineEnd();
        setSel(charIndex);
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

        int deleteEnd = getSelStart(); if (deleteEnd == 0) return;
        int deleteStart = deleteEnd - 1;
        if (_textBlock.isAfterLineEnd(deleteEnd))
            deleteStart = _textBlock.lastIndexOfNewline(deleteEnd);

        delete(deleteStart, deleteEnd, true);
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

        int deleteStart = getSelStart(); if (deleteStart >= length()) return;
        int deleteEnd = deleteStart + 1;
        if (_textBlock.isLineEnd(deleteEnd - 1))
            deleteEnd = _textBlock.indexAfterNewline(deleteEnd - 1);

        delete(deleteStart, deleteEnd, true);
    }

    /**
     * Deletes the characters from the insertion point to the end of the line.
     */
    public void deleteToLineEnd()
    {
        // If there is a current selection, just delete it
        if (!isSelEmpty())
            delete();

        // Otherwise, if at line end, delete line end
        else if (_textBlock.isLineEnd(getSelEnd()))
            delete(getSelStart(), _textBlock.indexAfterNewline(getSelStart()), true);

        // Otherwise delete up to next newline or line end
        else {
            int index = _textBlock.indexOfNewline(getSelStart());
            delete(getSelStart(), index >= 0 ? index : length(), true);
        }
    }

    /**
     * Clears the text.
     */
    public void clear()
    {
        _textBlock.clear();
    }

    /**
     * Returns the number of lines.
     */
    public int getLineCount()  { return _textBlock.getLineCount(); }

    /**
     * Returns the individual line at given index.
     */
    public TextLine getLine(int anIndex)  { return _textBlock.getLine(anIndex); }

    /**
     * Returns the line for the given character index.
     */
    public TextLine getLineForCharIndex(int anIndex)  { return _textBlock.getLineForCharIndex(anIndex); }

    /**
     * Returns the token for given character index.
     */
    public TextToken getTokenForCharIndex(int anIndex)  { return _textBlock.getTokenForCharIndex(anIndex); }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public int getCharIndexForXY(double anX, double aY)  { return _textBlock.getCharIndexForXY(anX, aY); }

    /**
     * Returns the link at given XY.
     */
    public TextLink getTextLinkForXY(double aX, double aY)
    {
        // If not RichText, just return
        if (!isRichText()) return null;

        // Get TextStyle at XY and return link
        int charIndex = getCharIndexForXY(aX, aY);
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

        // Paint TextBlock
        _textBlock.paint(aPntr);
    }

    /**
     * Paints the selection.
     */
    protected void paintSel(Painter aPntr)
    {
        // If not editable, just return
        if (!isEditable()) return;

        // Get Selection and path
        TextSel textSel = getSel();
        Shape selPath = textSel.getPath();

        // If empty selection, paint carat
        if (textSel.isEmpty()) {
            if (isShowCaret()) {
                aPntr.setPaint(Color.BLACK);
                aPntr.setStroke(Stroke.Stroke1);
                aPntr.draw(selPath);
            }
        }

        // Otherwise
        else {
            aPntr.setPaint(getSelColor());
            aPntr.fill(selPath);
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
        TextSel sel = new TextSel(_textBlock, _downX, _downY, _downX, _downY, _wordSel, _pgraphSel);
        int anchor = sel.getAnchor();
        int index = sel.getIndex();

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            anchor = Math.min(getSelStart(), sel.getStart());
            index = Math.max(getSelEnd(), sel.getEnd());
        }

        // Set selection
        setSel(anchor, index);
        TextBlockUtils.setMouseY(_textBlock, _downY);
    }

    /**
     * Handles mouse dragged.
     */
    protected void mouseDragged(ViewEvent anEvent)
    {
        // Get selected range for down point and drag point
        TextSel sel = new TextSel(_textBlock, _downX, _downY, anEvent.getX(), anEvent.getY(), _wordSel, _pgraphSel);
        int anchor = sel.getAnchor();
        int index = sel.getIndex();

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            anchor = Math.min(getSelStart(), sel.getStart());
            index = Math.max(getSelEnd(), sel.getEnd());
        }

        // Set selection
        setSel(anchor, index);
        TextBlockUtils.setMouseY(_textBlock, anEvent.getY());
    }

    /**
     * Handles mouse released.
     */
    protected void mouseReleased(ViewEvent anEvent)
    {
        setCaretAnim();
        _downX = _downY = 0;

        if (anEvent.isMouseClick()) {
            TextLink textLink = getTextLinkForXY(anEvent.getX(), anEvent.getY());
            if (textLink != null)
                openLink(textLink.getString());
        }
    }

    /**
     * Handle MouseMoved.
     */
    protected void mouseMoved(ViewEvent anEvent)
    {
        TextLink textLink = getTextLinkForXY(anEvent.getX(), anEvent.getY());
        if (textLink != null)
            setCursor(Cursor.HAND);
        else showCursor();
    }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed(ViewEvent anEvent)
    {
        _keys.keyPressed(anEvent);
    }

    /**
     * Called when a key is typed.
     */
    protected void keyTyped(ViewEvent anEvent)
    {
        _keys.keyTyped(anEvent);
    }

    /**
     * Called when a key is released.
     */
    protected void keyReleased(ViewEvent anEvent)
    {
        _keys.keyReleased(anEvent);
    }

    /**
     * Override to create TextAreaKeys.
     */
    protected TextAreaKeys createTextAreaKeys()  { return new TextAreaKeys(this); }

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
    protected boolean isCaretNeeded()
    {
        if (!isShowing())
            return false;
        if (!isFocused())
            return false;
        if (!getSel().isEmpty())
            return false;

        // If there is a window, it should be focused: It is possible to be set Showing, but not in Window
        WindowView window = getWindow();
        if (window != null && !window.isFocused())
            return false;

        // Return true
        return true;
    }

    /**
     * Sets the caret animation to whether it's needed.
     */
    protected void setCaretAnim()
    {
        boolean show = isCaretNeeded();
        setCaretAnim(show);
    }

    /**
     * Returns whether caret is flashing.
     */
    public boolean isCaretAnim()  { return _caretRun != null; }

    /**
     * Sets whether caret is flashing.
     */
    public void setCaretAnim(boolean aValue)
    {
        // If already set, just return
        if (aValue == isCaretAnim()) return;

        // If setting
        if (aValue) {
            _caretRun = () -> setShowCaret(!isShowCaret());
            runIntervals(_caretRun, 500);
            setShowCaret(true);
        }

        // If stopping
        else {
            stopIntervals(_caretRun);
            _caretRun = null;
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
        if (_textBlock instanceof TextBox)
            return ((TextBox) _textBlock).getFontScale();
        return 1;
    }

    /**
     * Sets the font scale of the text box.
     */
    public void setFontScale(double aValue)
    {
        if (_textBlock instanceof TextBox)
            ((TextBox) _textBlock).setFontScale(aValue);
        else System.out.println("TextArea.setFontScale not supported on this text");
        relayoutParent();
    }

    /**
     * Scales font sizes of all text in TextBox to fit in bounds by finding/setting FontScale.
     */
    public void scaleTextToFit()
    {
        if (_textBlock instanceof TextBox)
            ((TextBox) _textBlock).scaleTextToFit();
        else System.out.println("TextArea.scaleTextToFit not supported on this text");
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

        // Get clipboard
        Clipboard clipboard = Clipboard.getCleared();

        // Get selection start/end
        int selStart = getSelStart();
        int selEnd = getSelEnd();

        // Add rich text
        if (_textBlock.isRichText()) {
            TextBlock textForRange = _textBlock.copyForRange(selStart, selEnd);
            XMLElement xml = new XMLArchiver().toXML(textForRange);
            String xmlStr = xml.getString();
            clipboard.addData(SNAP_RICHTEXT_TYPE, xmlStr);
        }

        // Add plain text (text/plain)
        String textString = _textBlock.subSequence(selStart, selEnd).toString();
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
        // If Clipboard has RICHTEXT_TYPE, paste it
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
    public Undoer getUndoer()  { return _textBlock.getSourceText().getUndoer(); }

    /**
     * Called to undo the last text change.
     */
    public void undo()
    {
        UndoSet undoSet = _textBlock.getSourceText().undo();
        if (undoSet != null)
            setTextSelForUndoSet(undoSet, false);
        else ViewUtils.beep();
    }

    /**
     * Called to redo the last text change.
     */
    public void redo()
    {
        UndoSet undoSet = _textBlock.getSourceText().redo();
        if (undoSet != null)
            setTextSelForUndoSet(undoSet, true);
        else ViewUtils.beep();
    }

    /**
     * Sets the selection from given UndoSet.
     */
    private void setTextSelForUndoSet(UndoSet undoSet, boolean isRedo)
    {
        // Get prop changes (reverse if redo)
        List<PropChange> propChanges = undoSet.getChanges();
        if (isRedo)
            propChanges = ListUtils.getReverse(propChanges);

        // Iterate over prop changes
        for (PropChange propChange : propChanges) {
            String propName = propChange.getPropName();

            // Handle Chars prop: Set sel to new add chars
            if (propName == TextBlock.Chars_Prop) {
                CharSequence addString = (CharSequence) (isRedo ? propChange.getNewValue() : propChange.getOldValue());
                int startIndex = propChange.getIndex();
                int endIndex = startIndex + (addString != null ? addString.length() : 0);
                setSel(startIndex, endIndex);
                break;
            }

            // Handle Style prop: Set sel to char range
            else if (propName == TextBlock.Style_Prop) {
                TextBlockUtils.StyleChange styleChange = (TextBlockUtils.StyleChange) propChange;
                int startIndex = styleChange.getStart();
                int endIndex = styleChange.getEnd();
                setSel(startIndex, endIndex);
            }
        }
    }

    /**
     * Called when SourceText changes (chars added, updated or deleted).
     */
    protected void sourceTextDidPropChange(PropChange aPC)
    {
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
        double prefW = _textBlock instanceof TextBox ? ((TextBox) _textBlock).getPrefWidth(h) : _textBlock.getPrefWidth();
        return ins.left + prefW + ins.right;
    }

    /**
     * Returns the height needed to display all characters.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double w = aW >= 0 ? (aW - ins.left - ins.right) : aW;
        double prefH = _textBlock instanceof TextBox ? ((TextBox) _textBlock).getPrefHeight(w) : _textBlock.getPrefHeight();
        return ins.top + prefH + ins.bottom;
    }

    /**
     * Override to update getTextBlock.Rect.
     */
    public void setWidth(double aWidth)
    {
        super.setWidth(aWidth);
        updateTextBlockBounds();
    }

    /**
     * Override to update getTextBlock.Rect.
     */
    public void setHeight(double aHeight)
    {
        super.setHeight(aHeight);
        updateTextBlockBounds();
    }

    /**
     * Sets the Text.Rect from text area.
     */
    protected Rect getTextBounds()
    {
        Insets ins = getInsetsAll();
        double textX = ins.left;
        double textY = ins.top;
        double textW = Math.max(getWidth() - ins.getWidth(), 0);
        double textH = Math.max(getHeight() - ins.getHeight(), 0);
        return new Rect(textX, textY, textW, textH);
    }

    /**
     * Sets the TextBlock.Bounds from text bounds.
     */
    protected void updateTextBlockBounds()
    {
        Rect textBounds = getTextBounds();
        _textBlock.setBounds(textBounds);
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
    @Override
    protected void setShowing(boolean aValue)
    {
        // Do normal version
        if (aValue == isShowing()) return;
        super.setShowing(aValue);

        // If focused, update CaretAnim
        if (isFocused())
            setCaretAnim();

        // If Showing, make sure selection is visible
        if (aValue && getSelStart() != 0)
            getEnv().runDelayed(() -> scrollSelToVisible(), 200);

        // Manage listener for Window.Focus changes
        updateWindowFocusChangedLsnr();
    }

    /**
     * Updates WindowFocusChangedLsnr when Showing prop changes to update caret showing.
     */
    private void updateWindowFocusChangedLsnr()
    {
        // Handle Showing: Set ShowingWindow, add WindowFocusChangedLsnr and reset caret
        if (isShowing()) {
            _showingWindow = getWindow();
            if (_showingWindow != null) {
                _windowFocusedChangedLsnr = e -> setCaretAnim();
                _showingWindow.addPropChangeListener(_windowFocusedChangedLsnr, Focused_Prop);
            }
        }

        // Handle not Showing: Remove WindowFocusChangedLsnr and clear
        else {
            if (_showingWindow != null)
                _showingWindow.removePropChangeListener(_windowFocusedChangedLsnr);
            _showingWindow = null;
            _windowFocusedChangedLsnr = null;
        }
    }

    /**
     * Override to forward to text box.
     */
    public void setAlign(Pos aPos)
    {
        // Do normal version
        super.setAlign(aPos);

        // Push align to TextBlock via DefaultLineStyle.Align (X) and TextBlock align Y
        TextLineStyle lstyle = getDefaultLineStyle().copyFor(TextLineStyle.ALIGN_KEY, aPos.getHPos());
        setDefaultLineStyle(lstyle);
        if (_textBlock instanceof TextBox)
            ((TextBox) _textBlock).setAlignY(aPos.getVPos());
        else System.err.println("TextArea.setAlign: Not support on this text block");
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
            else if (!Objects.equals(_focusGainedText, getText()))
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
     * Returns whether layout tries to hyphenate wrapped words.
     */
    public static boolean isHyphenating()  { return _hyphenating; }

    /**
     * Sets whether layout tries to hyphenate wrapped words.
     */
    public static void setHyphenating(boolean aValue)
    {
        Prefs.getDefaultPrefs().setValue("Hyphenating", _hyphenating = aValue);
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
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement xml = super.toXML(anArchiver);
        toXMLTextArea(anArchiver, xml);
        return xml;
    }

    /**
     * XML archival.
     */
    protected void toXMLTextArea(XMLArchiver anArchiver, XMLElement xml)
    {
        // Archive Rich, Editable, WrapLines
        if (isRichText()) xml.add("Rich", true);
        if (!isEditable()) xml.add("Editable", false);
        if (isWrapLines()) xml.add(WrapLines_Prop, true);

        // If RichText, archive rich text
        if (isRichText()) {
            xml.removeElement("font");
            XMLElement richTextXML = anArchiver.toXML(_textBlock);
            richTextXML.setName("RichText");
            if (richTextXML.size() > 0)
                xml.add(richTextXML);
        }

        // Otherwise, archive text string
        else if (getText() != null && getText().length() > 0) xml.add("text", getText());

        // Archive FireActionOnEnterKey, FireActionOnFocusLost
        if (isFireActionOnEnterKey()) xml.add(FireActionOnEnterKey_Prop, true);
        if (isFireActionOnFocusLost()) xml.add(FireActionOnFocusLost_Prop, true);
    }

    /**
     * XML unarchival.
     */
    public TextArea fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        fromXMLTextArea(anArchiver, anElement);
        return this;
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLTextArea(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive Rich, Editable, WrapLines
        if (anElement.hasAttribute("Rich"))
            _textBlock.setRichText(anElement.getAttributeBoolValue("Rich"));
        if (anElement.hasAttribute("Editable"))
            setEditable(anElement.getAttributeBoolValue("Editable"));
        if (anElement.hasAttribute(WrapLines_Prop))
            setWrapLines(anElement.getAttributeBoolValue(WrapLines_Prop));
        if (anElement.hasAttribute("WrapText"))
            setWrapLines(anElement.getAttributeBoolValue("WrapText"));

        // If RichText, unarchive rich text
        XMLElement richTextXML = anElement.get("RichText");
        if (richTextXML != null) {
            _textBlock.setRichText(true);
            _textBlock.fromXML(anArchiver, richTextXML);
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
    }
}