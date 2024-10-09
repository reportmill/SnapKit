/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.props.*;
import snap.util.*;
import snap.view.*;
import java.util.List;

/**
 * This class acts as an intermediary between a 'text view' and a text block, handling selection, editing, cursor
 * input events, etc.
 */
public class TextAdapter extends PropObject {

    // The view
    protected View _view;

    // The text being edited
    protected TextBlock _textBlock;

    // Whether text is editable
    private boolean  _editable;

    // Whether text should wrap lines that overrun bounds
    private boolean  _wrapLines;

    // Whether text undo is activated
    private boolean  _undoActivated;

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

    // The mouse down point
    private double  _downX, _downY;

    // The runnable for caret flashing
    private Runnable _caretRun;

    // Whether to show text insertion point caret
    private boolean  _showCaret;

    // The PropChangeListener to catch SourceText PropChanges.
    private PropChangeListener _sourceTextPropLsnr = this::handleSourceTextPropChange;

    // A PropChangeListener to enable/disable caret when window loses focus
    private PropChangeListener  _windowFocusedChangedLsnr;

    // A pointer to window that text is showing in so we can remove WindowFocusChangedLsnr
    private WindowView _showingWindow;

    // Whether as-you-type spell checking is enabled
    public static boolean isSpellChecking = Prefs.getDefaultPrefs().getBoolean("SpellChecking", false);

    // The MIME type for SnapKit RichText
    public static final String SNAP_RICHTEXT_TYPE = "reportmill/xstring";

    // The Selection color
    private static Color TEXT_SEL_COLOR = new Color(181, 214, 254, 255);

    // Constants for properties
    public static final String RichText_Prop = "RichText";
    public static final String Editable_Prop = "Editable";
    public static final String WrapLines_Prop = "WrapLines";
    public static final String SourceText_Prop = "SourceText";
    public static final String Selection_Prop = "Selection";

    /**
     * Constructor for source text block.
     */
    public TextAdapter(TextBlock sourceText)
    {
        super();

        // Set default TextBlock
        _textBlock = sourceText;
        _textBlock.getSourceText().addPropChangeListener(_sourceTextPropLsnr);
    }

    /**
     * Returns the view.
     */
    public View getView()  { return _view; }

    /**
     * Sets the view.
     */
    public void setView(View aView)
    {
        if (aView == getView()) return;
        _view = aView;
        _view.addPropChangeListener(this::handleViewPropChanged);
    }

    /**
     * Returns the text block that holds the text.
     */
    public TextBlock getTextBlock()  { return _textBlock; }

    /**
     * Sets the text block that holds the text.
     */
    public void setTextBlock(TextBlock aTextBlock)
    {
        // If already set, just return
        if (aTextBlock == _textBlock) return;

        // Remove PropChangeListener
        if (_textBlock != null)
            _textBlock.getSourceText().removePropChangeListener(_sourceTextPropLsnr);

        // Set new text block
        _textBlock = aTextBlock;

        // Add PropChangeListener
        _textBlock.getSourceText().addPropChangeListener(_sourceTextPropLsnr);
        if (isUndoActivated())
            _textBlock.setUndoActivated(true);

        // Relayout parent, repaint
        if (_view != null) {
            _view.relayoutParent();
            _view.relayout();
            _view.repaint();
        }
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
            TextBox textBox = new TextBox(aTextBlock);
            textBox.setWrapLines(true);
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
        if (str.length() == length() && (str.isEmpty() || str.equals(getText()))) return;

        // Set string
        _textBlock.setString(aString);

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
            TextBox textBox = new TextBox(_textBlock);
            textBox.setWrapLines(true);
            setTextBlock(textBox);
        }
    }

    /**
     * Returns whether undo is activated.
     */
    public boolean isUndoActivated()  { return _undoActivated; }

    /**
     * Called to activate undo.
     */
    public void setUndoActivated(boolean aValue)
    {
        if (aValue == isUndoActivated()) return;
        _undoActivated = aValue;
        _textBlock.setUndoActivated(aValue);
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
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)  { _textBlock.setRichText(aValue); }

    /**
     * Returns the default text style for text.
     */
    public TextStyle getDefaultTextStyle()  { return _textBlock.getDefaultTextStyle(); }

    /**
     * Sets the default text style for text.
     */
    public void setDefaultTextStyle(TextStyle textStyle)  { _textBlock.setDefaultTextStyle(textStyle); }

    /**
     * Sets default text style for given style string.
     */
    public void setDefaultTextStyleString(String styleString)  { _textBlock.setDefaultTextStyleString(styleString); }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _textBlock.getDefaultLineStyle(); }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)  { _textBlock.setDefaultLineStyle(aLineStyle); }

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
        if (_view != null && _view.isShowing()) repaintSel();

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
        if (_view != null && _view.isShowing()) {
            repaintSel();
            updateCaretAnim();
            _view.runLater(() -> scrollSelToVisible());
        }
    }

    /**
     * Sets the selection with shift key modification, if shift key is down.
     */
    private void setSelWithShiftKeyCheck(int charIndex)
    {
        if (ViewUtils.isShiftDown()) {
            int selStart = Math.min(charIndex, getSelStart());
            int selEnd = Math.max(charIndex, getSelEnd());
            setSel(selStart, selEnd);
        }
        else setSel(charIndex);
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
        _view.repaint(rect);
    }

    /**
     * Scrolls Selection to visible.
     */
    protected void scrollSelToVisible()
    {
        // Get visible rect - if no reason to scroll, just return
        Rect visRect = _view.getClipAllBounds();
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();
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
            _view.scrollToVisible(selRect);
    }

    /**
     * Returns the font of the text block.
     */
    public Font getTextFont()
    {
        if (isRichText()) {
            TextStyle selStyle = getSelTextStyle();
            return selStyle.getFont();
        }
        return _textBlock.getDefaultFont();
    }

    /**
     * Sets the font of the text block.
     */
    public void setTextFont(Font aFont)
    {
        if (isRichText())
            setSelStyleValue(TextStyle.Font_Prop, aFont);
        else _textBlock.setDefaultFont(aFont);
    }

    /**
     * Returns the color of the current selection or cursor.
     */
    public Color getTextColor()
    {
        TextStyle selStyle = getSelTextStyle();
        return selStyle.getColor();
    }

    /**
     * Sets the color of the current selection or cursor.
     */
    public void setTextColor(Color aColor)
    {
        if (isRichText())
            setSelStyleValue(TextStyle.Color_Prop, aColor != null ? aColor : Color.BLACK);
        else _textBlock.setDefaultTextColor(aColor);
    }

    /**
     * Returns the format of the current selection or cursor.
     */
    public TextFormat getFormat()
    {
        TextStyle selStyle = getSelTextStyle();
        return selStyle.getFormat();
    }

    /**
     * Sets the format of the current selection or cursor, after trying to expand the selection to encompass currently
     * selected, @-sign delineated key.
     */
    public void setFormat(TextFormat aFormat)
    {
        // Get format selection range and select it (if non-null)
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        TextSel sel = TextBlockUtils.smartFindFormatRange(_textBlock, selStart, selEnd);
        if (sel != null)
            setSel(sel.getStart(), sel.getEnd());

        // Return if we are at end of string (this should never happen)
        if (getSelStart() >= length())
            return;

        // If there is a format, add it to current attributes and set for selected text
        setSelStyleValue(TextStyle.Font_Prop, aFormat);
    }

    /**
     * Returns whether TextView is underlined.
     */
    public boolean isUnderlined()
    {
        TextStyle selStyle = getSelTextStyle();
        return selStyle.isUnderlined();
    }

    /**
     * Sets whether TextView is underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        setSelStyleValue(TextStyle.Underline_Prop, aValue ? 1 : 0);
    }

    /**
     * Sets current selection to superscript.
     */
    public void setSuperscript()
    {
        TextStyle selStyle = getSelTextStyle();
        int state = selStyle.getScripting();
        setSelStyleValue(TextStyle.Scripting_Prop, state == 0 ? 1 : 0);
    }

    /**
     * Sets current selection to subscript.
     */
    public void setSubscript()
    {
        TextStyle selStyle = getSelTextStyle();
        int state = selStyle.getScripting();
        setSelStyleValue(TextStyle.Scripting_Prop, state == 0 ? -1 : 0);
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
    public TextStyle getTextStyleForCharIndex(int charIndex)
    {
        return _textBlock.getTextStyleForCharIndex(charIndex);
    }

    /**
     * Returns the TextStyle for the current selection and/or input characters.
     */
    public TextStyle getSelTextStyle()
    {
        // If already set, just return
        if (_selStyle != null) return _selStyle;

        // Get style for sel range
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        TextStyle selStyle = _textBlock.getTextStyleForCharRange(selStart, selEnd);

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
            TextStyle selStyle = getSelTextStyle();
            _selStyle = selStyle.copyForStyleKeyValue(aKey, aValue);
        }

        // If selection is multiple chars, apply attribute to text and reset SelStyle
        else {
            _textBlock.setTextStyleValue(aKey, aValue, getSelStart(), getSelEnd());
            _selStyle = null;
            _view.repaint();
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
     * Adds the given chars to end of text.
     */
    public void addChars(CharSequence theChars)  { addCharsWithStyle(theChars, null, length()); }

    /**
     * Adds the given chars at given char index.
     */
    public void addChars(CharSequence theChars, int charIndex)  { addCharsWithStyle(theChars, null, charIndex); }

    /**
     * Adds the given chars with given style to text end.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle textStyle)
    {
        addCharsWithStyle(theChars, textStyle, length());
    }

    /**
     * Adds the given chars with given style to text at given index.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle textStyle, int charIndex)
    {
        if (theChars == null) return;
        _textBlock.addCharsWithStyle(theChars, textStyle, charIndex);
        setSel(charIndex + theChars.length());
    }

    /**
     * Adds the given chars to text with given style string.
     */
    public void addCharsWithStyleString(CharSequence theChars, String styleString)
    {
        TextStyle textStyle = getDefaultTextStyle();
        TextStyle textStyle2 = textStyle.copyForStyleString(styleString);
        _textBlock.addCharsWithStyle(theChars, textStyle2);
    }

    /**
     * Deletes the given range of chars.
     */
    public void removeChars(int aStart, int anEnd)
    {
        _textBlock.removeChars(aStart, anEnd);
        setSel(aStart, aStart);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(CharSequence theChars)
    {
        int startCharIndex = getSelStart();
        int endCharIndex = getSelEnd();
        replaceCharsWithStyle(theChars, null, startCharIndex, endCharIndex);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceChars(CharSequence theChars, int startCharIndex, int endCharIndex)
    {
        replaceCharsWithStyle(theChars, null, startCharIndex, endCharIndex);
    }

    /**
     * Replaces the current selection with the given string.
     */
    public void replaceCharsWithStyle(CharSequence theChars, TextStyle textStyle, int aStart, int anEnd)
    {
        // Get string length (if no string length and no char range, just return)
        int strLen = theChars != null ? theChars.length() : 0;
        if (strLen == 0 && aStart == anEnd)
            return;

        // Get style (might need SelStyle if replacing empty selection)
        if (textStyle == null) {
            if (aStart == getSelStart())
                textStyle = getSelTextStyle();
            else textStyle = _textBlock.getTextStyleForCharRange(aStart, anEnd);
        }

        // Forward to TextBlock replaceChars() and update selection to end of new string
        _textBlock.replaceCharsWithStyle(theChars, textStyle, aStart, anEnd);
        setSel(aStart + strLen);
    }

    /**
     * Deletes the current selection.
     */
    public void delete()
    {
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        removeChars(selStart, selEnd);
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
        else System.out.println("TextAdapter.replaceCharsWithContent: Unknown content: " + theContent);
    }

    /**
     * Moves the selection index forward a character (or if a range is selected, moves to end of range).
     */
    public void selectForward()
    {
        int charIndex = _sel.getCharRight();
        setSelWithShiftKeyCheck(charIndex);
    }

    /**
     * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
     */
    public void selectBackward()
    {
        int charIndex = _sel.getCharLeft();
        setSelWithShiftKeyCheck(charIndex);
    }

    /**
     * Moves the selection index up a line, trying to preserve distance from beginning of line.
     */
    public void selectUp()
    {
        int charIndex = _sel.getCharUp();
        setSelWithShiftKeyCheck(charIndex);
    }

    /**
     * Moves the selection index down a line, trying preserve distance from beginning of line.
     */
    public void selectDown()
    {
        int charIndex = _sel.getCharDown();
        setSelWithShiftKeyCheck(charIndex);
    }

    /**
     * Moves the insertion point to the beginning of line.
     */
    public void selectLineStart()
    {
        int charIndex = _sel.getLineStart();
        setSelWithShiftKeyCheck(charIndex);
    }

    /**
     * Moves the insertion point to next newline or text end.
     */
    public void selectLineEnd()
    {
        int charIndex = _sel.getLineEnd();
        setSelWithShiftKeyCheck(charIndex);
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

        removeChars(deleteStart, deleteEnd);
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

        removeChars(deleteStart, deleteEnd);
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
            removeChars(getSelStart(), _textBlock.indexAfterNewline(getSelStart()));

        // Otherwise delete up to next newline or line end
        else {
            int index = _textBlock.indexOfNewline(getSelStart());
            removeChars(getSelStart(), index >= 0 ? index : length());
        }
    }

    /**
     * Clears the text.
     */
    public void clear()  { _textBlock.clear(); }

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
        TextStyle textStyle = _textBlock.getTextStyleForCharIndex(charIndex);
        return textStyle.getLink();
    }

    /**
     * Paint text.
     */
    public void paintText(Painter aPntr)
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
            aPntr.setPaint(TEXT_SEL_COLOR);
            aPntr.fill(selPath);
        }
    }

    /**
     * Process event.
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
    public void mousePressed(ViewEvent anEvent)
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
    public void mouseDragged(ViewEvent anEvent)
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
    public void mouseReleased(ViewEvent anEvent)
    {
        updateCaretAnim();
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
    public void mouseMoved(ViewEvent anEvent)
    {
        TextLink textLink = getTextLinkForXY(anEvent.getX(), anEvent.getY());
        if (textLink != null)
            _view.setCursor(Cursor.HAND);
        else showCursor();
    }

    /**
     * Called when a key is pressed.
     */
    public void keyPressed(ViewEvent anEvent)
    {
        // Get event info
        int keyCode = anEvent.getKeyCode();
        boolean shortcutDown = anEvent.isShortcutDown();
        boolean controlDown = anEvent.isControlDown();
        boolean emacsDown = SnapUtils.isWindows ? anEvent.isAltDown() : controlDown;
        boolean shiftDown = anEvent.isShiftDown();

        // Reset caret
        setCaretAnim(false);
        setShowCaret(isCaretNeeded());

        // Handle shortcut keys
        if (shortcutDown) {

            // If shift-down, just return
            if (shiftDown && keyCode != KeyCode.Z) return;

            // Handle common command keys
            switch(keyCode) {
                case KeyCode.X: cut(); anEvent.consume(); break; // Handle command-x cut
                case KeyCode.C: copy(); anEvent.consume(); break; // Handle command-c copy
                case KeyCode.V: paste(); anEvent.consume(); break; // Handle command-v paste
                case KeyCode.A: selectAll(); anEvent.consume(); break; // Handle command-a select all
                case KeyCode.Z:
                    if (shiftDown)
                        redo();
                    else undo();
                    anEvent.consume(); break; // Handle command-z undo
            }
        }

        // Handle control keys (not applicable on Windows, since they are handled by command key code above)
        else if (emacsDown) {

            // If shift down, just return
            if (shiftDown) return;

            // Handle common emacs key bindings
            switch (keyCode) {
                case KeyCode.F: selectForward(); break; // Handle control-f key forward
                case KeyCode.B: selectBackward(); break; // Handle control-b key backward
                case KeyCode.P: selectUp(); break; // Handle control-p key up
                case KeyCode.N: selectDown(); break; // Handle control-n key down
                case KeyCode.A: selectLineStart(); break; // Handle control-a line start
                case KeyCode.E: selectLineEnd(); break; // Handle control-e line end
                case KeyCode.D: deleteForward(); break; // Handle control-d delete forward
                case KeyCode.K: deleteToLineEnd(); break; // Handle control-k delete line to end
            }
        }

        // Handle supported non-character keys
        else {
            switch (keyCode) {

                // Handle Tab, Enter
                case KeyCode.TAB: replaceChars("\t"); anEvent.consume(); break;
                case KeyCode.ENTER: replaceChars("\n"); anEvent.consume(); break;

                // Handle Left, Right, Up, Down arrows
                case KeyCode.LEFT: selectBackward(); anEvent.consume(); break;
                case KeyCode.RIGHT: selectForward(); anEvent.consume(); break;
                case KeyCode.UP: selectUp(); anEvent.consume(); break;
                case KeyCode.DOWN: selectDown(); anEvent.consume(); break;

                // Handle Home, End
                case KeyCode.HOME: selectLineStart(); break;
                case KeyCode.END: selectLineEnd(); break;

                // Handle Backspace, Delete
                case KeyCode.BACK_SPACE: deleteBackward(); anEvent.consume(); break;
                case KeyCode.DELETE: deleteForward(); anEvent.consume(); break;

                // Handle Space key
                case KeyCode.SPACE: anEvent.consume(); break;
            }
        }
    }

    /**
     * Called when a key is typed.
     */
    public void keyTyped(ViewEvent anEvent)
    {
        // Get event info
        String keyChars = anEvent.getKeyString();
        char keyChar = !keyChars.isEmpty() ? keyChars.charAt(0) : 0;
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
    public void keyReleased(ViewEvent anEvent)
    {
        updateCaretAnim();
    }

    /**
     * Shows the cursor.
     */
    public void showCursor()
    {
        _view.setCursor(Cursor.TEXT);
    }

    /**
     * Hides the cursor.
     */
    public void hideCursor()
    {
        _view.setCursor(Cursor.NONE);
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
        if (!_view.isShowing())
            return false;
        if (!_view.isFocused())
            return false;
        if (!getSel().isEmpty())
            return false;

        // If there is a window, it should be focused: It is possible to be set Showing, but not in Window
        WindowView window = _view.getWindow();
        if (window != null && !window.isFocused())
            return false;

        // Return true
        return true;
    }

    /**
     * Sets the caret animation to whether it's needed.
     */
    protected void updateCaretAnim()
    {
        boolean show = isCaretNeeded();
        setCaretAnim(show);
    }

    /**
     * Returns whether caret is flashing.
     */
    private boolean isCaretAnim()  { return _caretRun != null; }

    /**
     * Sets whether caret is flashing.
     */
    private void setCaretAnim(boolean aValue)
    {
        // If already set, just return
        if (aValue == isCaretAnim()) return;

        // If setting
        if (aValue) {
            _caretRun = () -> setShowCaret(!isShowCaret());
            _view.runIntervals(_caretRun, 500);
            setShowCaret(true);
        }

        // If stopping
        else {
            _view.stopIntervals(_caretRun);
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
        else System.out.println("TextAdapter.setFontScale not supported on this text");
        _view.relayoutParent();
    }

    /**
     * Scales font sizes of all text in TextBox to fit in bounds by finding/setting FontScale.
     */
    public void scaleTextToFit()
    {
        if (_textBlock instanceof TextBox)
            ((TextBox) _textBlock).scaleTextToFit();
        else System.out.println("TextAdapter.scaleTextToFit not supported on this text");
        _view.relayoutParent();
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
    public Object getClipboardContent(Clipboard clipboard)
    {
        // If Clipboard has RICHTEXT_TYPE, paste it
        if (clipboard.hasData(SNAP_RICHTEXT_TYPE)) {
            byte[] bytes = clipboard.getDataBytes(SNAP_RICHTEXT_TYPE);
            if (bytes != null && bytes.length > 0) {  // Shouldn't need this - Happens when pasting content from prior instance
                TextBlock richText = new TextBlock(true);
                XMLArchiver archiver = new XMLArchiver();
                archiver.setRootObject(richText);
                archiver.readFromXMLBytes(bytes);
                return  richText;
            }
        }

        // If Clipboard has String, paste it
        if (clipboard.hasString()) {
            String str = clipboard.getString();
            if (str != null && !str.isEmpty())
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
     * Returns the area bounds for given view.
     */
    public Rect getTextBounds()  { return _textBlock.getBounds(); }

    /**
     * Sets the text bounds.
     */
    public void setTextBounds(Rect boundsRect)  { _textBlock.setBounds(boundsRect); }

    /**
     * Returns the width needed to display all characters.
     */
    public double getPrefWidth()  { return _textBlock.getPrefWidth(); }

    /**
     * Returns the height needed to display all characters.
     */
    public double getPrefHeight(double aW)
    {
        if (_textBlock instanceof TextBox)
            return ((TextBox) _textBlock).getPrefHeight(aW);
        return _textBlock.getPrefHeight();
    }

    /**
     * Called when SourceText changes (chars added, updated or deleted).
     */
    protected void handleSourceTextPropChange(PropChange aPC)
    {
        // Forward on to listeners
        firePropChange(aPC);

        // Relayout and repaint
        _view.relayoutParent();
        _view.repaint();
    }

    /**
     * Called when view has prop change.
     */
    private void handleViewPropChanged(PropChange aPC)
    {
        switch (aPC.getPropName()) {
            case View.Width_Prop: case View.Height_Prop: handleViewSizeChanged(); break;
            case View.Showing_Prop: handleViewShowingChanged(); break;
            case View.Focused_Prop: handleViewFocusedChanged(); break;
            case View.Align_Prop: handleViewAlignChanged(); break;
        }
    }

    /**
     * Called when View width/height changes.
     */
    private void handleViewSizeChanged()
    {
        Rect textBounds = ViewUtils.getAreaBounds(_view);
        setTextBounds(textBounds);
    }

    /**
     * Called when View.Showing changes.
     */
    private void handleViewShowingChanged()
    {
        // If focused, update CaretAnim
        if (_view.isFocused())
            updateCaretAnim();

        // If Showing, make sure selection is visible
        if (_view.isShowing() && getSelStart() != 0)
            _view.runDelayed(() -> scrollSelToVisible(), 200);

        // Manage listener for Window.Focus changes
        updateWindowFocusChangedLsnr();
    }

    /**
     * Updates WindowFocusChangedLsnr when Showing prop changes to update caret showing.
     */
    private void updateWindowFocusChangedLsnr()
    {
        // Handle Showing: Set ShowingWindow, add WindowFocusChangedLsnr and reset caret
        if (_view.isShowing()) {
            _showingWindow = _view.getWindow();
            if (_showingWindow != null) {
                _windowFocusedChangedLsnr = e -> updateCaretAnim();
                _showingWindow.addPropChangeListener(_windowFocusedChangedLsnr, View.Focused_Prop);
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
    private void handleViewAlignChanged()
    {
        Pos viewAlign = _view.getAlign();

        // Push align to TextBlock via DefaultLineStyle.Align (X) and TextBlock align Y
        TextLineStyle lineStyle = getDefaultLineStyle().copyFor(TextLineStyle.ALIGN_KEY, viewAlign.getHPos());
        setDefaultLineStyle(lineStyle);

        // Forward to text block
        if (_textBlock instanceof TextBox)
            ((TextBox) _textBlock).setAlignY(viewAlign.getVPos());
        else System.err.println("TextAdapter.setAlign: Not support on this text block");
    }

    /**
     * Override to check caret animation and repaint.
     */
    private void handleViewFocusedChanged()
    {
        // Update caret
        setShowCaret(false);
        updateCaretAnim();
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
}