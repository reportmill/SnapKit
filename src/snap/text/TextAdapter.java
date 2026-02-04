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
import java.util.function.BiConsumer;

/**
 * This class acts as an intermediary between a 'text view' and a text model, handling selection, editing, cursor
 * input events, etc.
 */
public class TextAdapter extends PropObject {

    // The view
    protected View _view;

    // The text being edited
    protected TextModel _textModel;

    // The text being displayed
    protected TextLayout _textLayout;

    // Whether text is editable
    private boolean  _editable;

    // The text undoer
    private Undoer _undoer = Undoer.DISABLED_UNDOER;

    // A PropChangeListener to send TextModel PropChanges to adapter client.
    private PropChangeListener[] _textModelPropChangeLsnrs = new PropChangeListener[0];

    // A consumer to handle link clicks
    private BiConsumer<ViewEvent,String> _linkHandler;

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

    // The PropChangeListener to catch TextModel PropChanges.
    private PropChangeListener _textModelPropChangeLsnr = this::handleTextModelPropChange;

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
    public static final String Editable_Prop = "Editable";
    public static final String RichText_Prop = "RichText";
    public static final String Selection_Prop = "Selection";
    public static final String TextModel_Prop = "TextModel";
    public static final String TextLayout_Prop = "TextLayout";
    public static final String WrapLines_Prop = "WrapLines";

    /**
     * Constructor for given text.
     */
    public TextAdapter(TextLayout textLayout)
    {
        super();
        setTextLayout(textLayout);
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
        _view.addPropChangeListener(this::handleViewPropChange);
    }

    /**
     * Returns the text model.
     */
    public TextModel getTextModel()  { return _textModel; }

    /**
     * Sets the text model.
     */
    public void setTextModel(TextModel textModel)
    {
        // If already set, just return
        if (textModel == _textModel) return;

        // Remove PropChangeListener
        if (_textModel != null)
            _textModel.removePropChangeListener(_textModelPropChangeLsnr);

        // Set new text model
        batchPropChange(TextModel_Prop, _textModel, _textModel = textModel);

        // Add PropChangeListener
        _textModel.addPropChangeListener(_textModelPropChangeLsnr);

        // Relayout parent, repaint
        if (_view != null) {
            _view.relayoutParent();
            _view.relayout();
            _view.repaint();
        }

        // Make sure text layout matches text model
        if (_textLayout instanceof TextModelX textModelX)
            textModelX.setSourceText(_textModel);
        else if (_textLayout == null || _textLayout.getTextModel() != _textModel)
            setTextLayout(_textModel);

        // FirePropChange
        fireBatchPropChanges();
    }

    /**
     * Returns the text layout.
     */
    public TextLayout getTextLayout()  { return _textLayout; }

    /**
     * Sets the text layout.
     */
    public void setTextLayout(TextLayout textLayout)
    {
        if (textLayout == _textLayout) return;

        // Set new text layout
        batchPropChange(TextLayout_Prop, _textLayout, _textLayout = textLayout);

        // Relayout parent, repaint
        if (_view != null) {
            _view.relayoutParent();
            _view.relayout();
            _view.repaint();
        }

        // Make sure text model matches layout text model
        setTextModel(_textLayout.getTextModel());

        // FirePropChange
        fireBatchPropChanges();
    }

    /**
     * Returns the plain string of the text being edited.
     */
    public String getString()  { return _textModel.getString(); }

    /**
     * Set text string of text editor.
     */
    public void setString(String aString)
    {
        if (aString.contentEquals(_textModel.getChars())) return;
        _textModel.setString(aString);

        // Reset selection (to line end if single-line, otherwise text start)
        int defaultSelIndex = _textModel.getLineCount() == 1 && length() < 40 ? length() : 0;
        setSel(defaultSelIndex);
        getUndoer().reset();
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
    public boolean isWrapLines()  { return _textLayout.isWrapLines(); }

    /**
     * Sets whether to wrap lines that overrun bounds.
     */
    public void setWrapLines(boolean aValue)
    {
        // If already set, just return
        if (aValue == isWrapLines()) return;

        // If already TextModelX, just forward to TextModelX
        if (_textModel instanceof TextModelX)
            ((TextModelX) _textModel).setWrapLines(aValue);

        // Otherwise, wrap text in TextModelX
        else if (aValue) {
            TextModelX wrappedTextLayout = new TextModelX(_textModel);
            wrappedTextLayout.setWrapLines(true);
            setTextLayout(wrappedTextLayout);
        }

        // Fire prop change
        firePropChange(WrapLines_Prop, !aValue, aValue);
    }

    /**
     * Returns the text undoer.
     */
    public Undoer getUndoer()  { return _undoer; }

    /**
     * Sets the text undoer.
     */
    public void setUndoer(Undoer anUndoer)  { _undoer = anUndoer; }

    /**
     * Returns whether undo is activated.
     */
    public boolean isUndoActivated()  { return _undoer != Undoer.DISABLED_UNDOER; }

    /**
     * Called to activate undo.
     */
    public void setUndoActivated(boolean aValue)
    {
        if (aValue == isUndoActivated()) return;

        // If activating, create new undoer
        if (aValue) {
            _undoer = new Undoer();
            _undoer.setAutoSave(true);
        }

        // Otherwise, reset to disabled
        else _undoer = Undoer.DISABLED_UNDOER;
    }

    /**
     * Adds a property change to undoer.
     */
    private void addTextModelPropChangeToUndoer(PropChange propChange)
    {
        // Get undoer (just return if null or disabled)
        Undoer undoer = getUndoer();
        if (!undoer.isEnabled())
            return;

        // If PlainText Style_Prop or LineStyle_Prop, just return
        if (!isRichText()) {
            String propName = propChange.getPropName();
            if (propName == TextModel.Style_Prop || propName == TextModel.LineStyle_Prop)
                return;
        }

        // Add property
        undoer.addPropChange(propChange);
    }

    /**
     * Adds a prop change listener for TextModel prop changes.
     */
    public void addTextModelPropChangeListener(PropChangeListener propChangeListener)
    {
        _textModelPropChangeLsnrs = ArrayUtils.add(_textModelPropChangeLsnrs, propChangeListener);
    }

    /**
     * Removes a prop change listener for TextModel prop changes.
     */
    public void removeTextModelPropChangeListener(PropChangeListener propChangeListener)
    {
        _textModelPropChangeLsnrs = ArrayUtils.remove(_textModelPropChangeLsnrs, propChangeListener);
    }

    /**
     * Returns the consumer that is called when a link is activated.
     */
    public BiConsumer<ViewEvent,String> getLinkHandler()  { return _linkHandler; }

    /**
     * Sets the consumer that is called when a link is activated.
     */
    public void setLinkHandler(BiConsumer<ViewEvent,String> linkHandler)
    {
        if (linkHandler == getLinkHandler()) return;
        _linkHandler = linkHandler;
    }

    /**
     * Returns whether editor is doing check-as-you-type spelling.
     */
    public boolean isSpellChecking()  { return isSpellChecking; }

    /**
     * Returns whether text supports multiple styles.
     */
    public boolean isRichText()  { return _textModel.isRichText(); }

    /**
     * Sets whether text supports multiple styles.
     */
    public void setRichText(boolean aValue)  { _textModel.setRichText(aValue); }

    /**
     * Returns the default text style for text.
     */
    public TextStyle getDefaultTextStyle()  { return _textModel.getDefaultTextStyle(); }

    /**
     * Sets the default text style for text.
     */
    public void setDefaultTextStyle(TextStyle textStyle)  { _textModel.setDefaultTextStyle(textStyle); }

    /**
     * Sets default text style for given style string.
     */
    public void setDefaultTextStyleString(String styleString)  { _textModel.setDefaultTextStyleString(styleString); }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _textModel.getDefaultLineStyle(); }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)  { _textModel.setDefaultLineStyle(aLineStyle); }

    /**
     * Returns the number of characters in the text string.
     */
    public int length()  { return _textModel.length(); }

    /**
     * Returns the individual character at given index.
     */
    public char charAt(int anIndex)  { return _textModel.charAt(anIndex); }

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
        return _sel = new TextSel(_textLayout, _selAnchor, _selIndex);
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
        if (anchor == _selAnchor && index == _selIndex)
            return;

        // Repaint old selection
        if (_view != null && _view.isShowing())
            repaintSel();

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
        TextModelUtils.setMouseY(_textModel, 0);

        // Repaint selection and scroll to visible (after delay)
        if (_view != null && _view.isShowing()) {
            repaintSel();
            updateCaretAnim();
            _view.runLater(this::scrollSelToVisible);
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
     * Selects a given line number.
     */
    public void selectLine(int lineIndex)
    {
        lineIndex = MathUtils.clamp(lineIndex, 0, getLineCount() - 1);
        TextLine textLine = getLine(lineIndex);
        setSel(textLine.getStartCharIndex(), textLine.getEndCharIndex());
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
        _view.repaint(rect);
    }

    /**
     * Scrolls Selection to visible.
     */
    protected void scrollSelToVisible()
    {
        // Get visible bounds - if no reason to scroll, just return
        Rect visibleBounds = _view.getVisibleBounds();
        double viewW = _view.getWidth();
        double viewH = _view.getHeight();
        if (visibleBounds.isEmpty() || visibleBounds.width == viewW && visibleBounds.height == viewH)
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
            selRect.x = visibleBounds.x;
            selRect.width = visibleBounds.width;
        }

        // If selection rect not fully contained in visible bounds, scrollRectToVisible
        if (!visibleBounds.contains(selRect))
            _view.scrollToVisible(selRect);
    }

    /**
     * Returns the font of current selection.
     */
    public Font getTextFont()
    {
        if (isRichText()) {
            TextStyle selStyle = getSelTextStyle();
            return selStyle.getFont();
        }
        return _textModel.getDefaultFont();
    }

    /**
     * Sets the font of current selection.
     */
    public void setTextFont(Font aFont)
    {
        if (isRichText())
            setSelTextStyleValue(TextStyle.Font_Prop, aFont);
        else _textModel.setDefaultFont(aFont);
    }

    /**
     * Returns the color of the current selection or cursor.
     */
    public Color getTextColor()
    {
        if (isRichText())
            return getSelTextStyle().getColor();
        return _textModel.getDefaultTextColor();
    }

    /**
     * Sets the color of the current selection or cursor.
     */
    public void setTextColor(Color aColor)
    {
        if (isRichText())
            setSelTextStyleValue(TextStyle.Color_Prop, aColor != null ? aColor : Color.BLACK);
        else _textModel.setDefaultTextColor(aColor);
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
        TextSel sel = TextModelUtils.smartFindFormatRange(_textModel, selStart, selEnd);
        if (sel != null)
            setSel(sel.getStart(), sel.getEnd());

        // Return if we are at end of string (this should never happen)
        if (getSelStart() >= length())
            return;

        // If there is a format, add it to current attributes and set for selected text
        setSelTextStyleValue(TextStyle.Font_Prop, aFormat);
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
        setSelTextStyleValue(TextStyle.Underline_Prop, aValue ? 1 : 0);
    }

    /**
     * Sets current selection to superscript.
     */
    public void setSuperscript()
    {
        TextStyle selStyle = getSelTextStyle();
        int state = selStyle.getScripting();
        setSelTextStyleValue(TextStyle.Scripting_Prop, state == 0 ? 1 : 0);
    }

    /**
     * Sets current selection to subscript.
     */
    public void setSubscript()
    {
        TextStyle selStyle = getSelTextStyle();
        int state = selStyle.getScripting();
        setSelTextStyleValue(TextStyle.Scripting_Prop, state == 0 ? -1 : 0);
    }

    /**
     * Set the alignment of text.
     */
    public void setAlign(Pos aPos)
    {
        TextModel textLayout = (TextModel) getTextLayout();
        if (aPos.getHPos() != textLayout.getDefaultLineStyle().getAlign())
            textLayout.setDefaultLineStyle(textLayout.getDefaultLineStyle().copyForAlign(aPos.getHPos()));
        textLayout.setAlignY(aPos.getVPos());
    }

    /**
     * Returns the TextStyle for the current selection and/or input characters.
     */
    public TextStyle getSelTextStyle()
    {
        // If already set, just return
        if (_selStyle != null) return _selStyle;

        // If not rich text, just return default style
        if (!isRichText())
            return _selStyle = getDefaultTextStyle();

        // Get style for sel range
        int selStart = getSelStart();
        int selEnd = getSelEnd();
        TextStyle selStyle = _textModel.getTextStyleForCharRange(selStart, selEnd);

        // Set/return
        return _selStyle = selStyle;
    }

    /**
     * Sets the attributes that are applied to current selection or newly typed chars.
     */
    public void setSelTextStyleValue(String aKey, Object aValue)
    {
        // If selection is zero length, just modify input style
        if (isSelEmpty() && isRichText()) {
            TextStyle selStyle = getSelTextStyle();
            _selStyle = selStyle.copyForStyleKeyValue(aKey, aValue);
        }

        // If selection is multiple chars, apply attribute to text and reset SelStyle
        else {
            _textModel.setTextStyleValue(aKey, aValue, getSelStart(), getSelEnd());
            _selStyle = null;
            if (_view != null)
                _view.repaint();
        }
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
        _textModel.addCharsWithStyle(theChars, textStyle, charIndex);
        setSel(charIndex + theChars.length());
    }

    /**
     * Adds the given chars to text with given style string.
     */
    public void addCharsWithStyleString(CharSequence theChars, String styleString)
    {
        TextStyle textStyle = getDefaultTextStyle();
        TextStyle textStyle2 = textStyle.copyForStyleString(styleString);
        _textModel.addCharsWithStyle(theChars, textStyle2);
    }

    /**
     * Deletes the given range of chars.
     */
    public void removeChars(int aStart, int anEnd)
    {
        _textModel.removeChars(aStart, anEnd);
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
            else textStyle = _textModel.getTextStyleForCharRange(aStart, anEnd);
        }

        // Forward to TextModel replaceChars() and update selection to end of new string
        _textModel.replaceCharsWithStyle(theChars, textStyle, aStart, anEnd);
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
     * Replaces the current selection with the given contents (TextModel or String).
     */
    public void replaceCharsWithContent(Object theContent)
    {
        // If Clipboard has TextModel, paste it
        if (theContent instanceof TextModel textModel) {
            int selStart = getSelStart();
            int selEnd = getSelEnd();
            _textModel.removeChars(selStart, selEnd);
            _textModel.addCharsForTextModel(textModel, selStart);
            setSel(selStart + textModel.length());
        }

        // If Clipboard has String, paste it
        else if (theContent instanceof String str)
            replaceChars(str);

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
        if (_textModel.getCharsX().isAfterLineEnd(deleteEnd))
            deleteStart = _textModel.getCharsX().lastIndexOfNewline(deleteEnd);

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
        if (_textModel.getCharsX().isLineEnd(deleteEnd - 1))
            deleteEnd = _textModel.getCharsX().indexAfterNewline(deleteEnd - 1);

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
        else if (_textModel.getCharsX().isLineEnd(getSelEnd()))
            removeChars(getSelStart(), _textModel.getCharsX().indexAfterNewline(getSelStart()));

        // Otherwise delete up to next newline or line end
        else {
            int index = _textModel.getCharsX().indexOfNewline(getSelStart());
            removeChars(getSelStart(), index >= 0 ? index : length());
        }
    }

    /**
     * Clears the text.
     */
    public void clear()
    {
        // Disable undo
        Undoer undoer = getUndoer();
        undoer.disable();

        _textModel.clear();

        // Reset undo
        undoer.reset();
    }

    /**
     * Returns the number of lines.
     */
    public int getLineCount()  { return _textLayout.getLineCount(); }

    /**
     * Returns the individual line at given index.
     */
    public TextLine getLine(int anIndex)  { return _textLayout.getLine(anIndex); }

    /**
     * Returns the line for the given character index.
     */
    public TextLine getLineForCharIndex(int anIndex)  { return _textLayout.getLineForCharIndex(anIndex); }

    /**
     * Returns the token for given character index.
     */
    public TextToken getTokenForCharIndex(int anIndex)  { return _textLayout.getTokenForCharIndex(anIndex); }

    /**
     * Returns the char index for given point in text coordinate space.
     */
    public int getCharIndexForXY(double anX, double aY)  { return _textLayout.getCharIndexForXY(anX, aY); }

    /**
     * Returns the link at given XY.
     */
    public TextLink getTextLinkForXY(double aX, double aY)
    {
        // If not RichText, just return
        if (!isRichText()) return null;

        // Get TextStyle at XY and return link
        int charIndex = getCharIndexForXY(aX, aY);
        TextStyle textStyle = _textLayout.getTextStyleForCharIndex(charIndex);
        return textStyle.getLink();
    }

    /**
     * Paint selection and text.
     */
    public void paintAll(Painter aPntr)
    {
        // Paint selection
        paintSel(aPntr);

        // Paint TextModel
        _textLayout.paint(aPntr);
    }

    /**
     * Paints the selection.
     */
    public void paintSel(Painter aPntr)
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
     * Paint text.
     */
    public void paintText(Painter aPntr)
    {
        _textLayout.paint(aPntr);
    }

    /**
     * Process event.
     */
    public void processEvent(ViewEvent anEvent)
    {
        switch (anEvent.getType()) {
            case MousePress -> mousePressed(anEvent);
            case MouseDrag -> mouseDragged(anEvent);
            case MouseRelease -> mouseReleased(anEvent);
            case MouseMove -> mouseMoved(anEvent);
            case KeyPress -> handleKeyPressEvent(anEvent);
            case KeyType -> handleKeyTypeEvent(anEvent);
            case KeyRelease -> handleKeyReleaseEvent(anEvent);
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
        TextSel sel = new TextSel(_textLayout, _downX, _downY, _downX, _downY, _wordSel, _pgraphSel);
        int anchor = sel.getAnchor();
        int index = sel.getIndex();

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            anchor = Math.min(getSelStart(), sel.getStart());
            index = Math.max(getSelEnd(), sel.getEnd());
        }

        // Set selection
        setSel(anchor, index);
        TextModelUtils.setMouseY(_textLayout, _downY);
    }

    /**
     * Handles mouse dragged.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        // Get selected range for down point and drag point
        TextSel sel = new TextSel(_textLayout, _downX, _downY, anEvent.getX(), anEvent.getY(), _wordSel, _pgraphSel);
        int anchor = sel.getAnchor();
        int index = sel.getIndex();

        // If shift is down, xor selection
        if (anEvent.isShiftDown()) {
            anchor = Math.min(getSelStart(), sel.getStart());
            index = Math.max(getSelEnd(), sel.getEnd());
        }

        // Set selection
        setSel(anchor, index);
        TextModelUtils.setMouseY(_textLayout, anEvent.getY());
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
                handleLinkEvent(anEvent, textLink.getString());
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
    public void handleKeyPressEvent(ViewEvent anEvent)
    {
        // Get event info
        boolean emacsDown = SnapEnv.isWindows ? anEvent.isAltDown() : anEvent.isControlDown();

        // Reset caret
        setCaretAnim(false);
        setShowCaret(isCaretNeeded());

        // Handle shortcut keys
        if (anEvent.isShortcutDown())
            handleShortcutKeyPressEvent(anEvent);

        // Handle emacs keys
        else if (emacsDown)
            handleEmacsKeyPressEvent(anEvent);

        // Handle anything else
        else handlePlainKeyPressEvent(anEvent);
    }

    /**
     * Called when a shortcut key is pressed.
     */
    protected void handleShortcutKeyPressEvent(ViewEvent anEvent)
    {
        // If shift-down, just return
        int keyCode = anEvent.getKeyCode();
        boolean shiftDown = anEvent.isShiftDown();
        if (shiftDown && keyCode != KeyCode.Z)
            return;

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
            case KeyCode.Y: redo(); break;
        }
    }

    /**
     * Called when an emacs key is pressed.
     */
    protected void handleEmacsKeyPressEvent(ViewEvent anEvent)
    {
        // If shift down, just return
        if (anEvent.isShiftDown()) return;

        // Handle common emacs key bindings
        switch (anEvent.getKeyCode()) {
            case KeyCode.F -> selectForward(); // Handle control-f key forward
            case KeyCode.B -> selectBackward(); // Handle control-b key backward
            case KeyCode.P -> selectUp(); // Handle control-p key up
            case KeyCode.N -> selectDown(); // Handle control-n key down
            case KeyCode.A -> selectLineStart(); // Handle control-a line start
            case KeyCode.E -> selectLineEnd(); // Handle control-e line end
            case KeyCode.D -> deleteForward(); // Handle control-d delete forward
            case KeyCode.K -> deleteToLineEnd(); // Handle control-k delete line to end
        }
    }

    /**
     * Called when a plain key is pressed (not shortcut or emacs).
     */
    protected void handlePlainKeyPressEvent(ViewEvent anEvent)
    {
        switch (anEvent.getKeyCode()) {

            // Handle Tab, Enter
            case KeyCode.TAB -> handleTabKeyPressEvent(anEvent);
            case KeyCode.ENTER -> handleEnterKeyPressEvent(anEvent);

            // Handle Left, Right, Up, Down arrows
            case KeyCode.LEFT -> { selectBackward(); anEvent.consume(); }
            case KeyCode.RIGHT -> { selectForward(); anEvent.consume(); }
            case KeyCode.UP -> { selectUp(); anEvent.consume(); }
            case KeyCode.DOWN -> { selectDown(); anEvent.consume(); }

            // Handle Home, End
            case KeyCode.HOME -> selectLineStart();
            case KeyCode.END -> selectLineEnd();


            // Handle Backspace, Delete
            case KeyCode.BACK_SPACE -> handleBackSpaceKeyPressEvent(anEvent);
            case KeyCode.DELETE -> { deleteForward(); anEvent.consume(); }

            // Handle Space key
            case KeyCode.SPACE -> anEvent.consume();
        }
    }

    /**
     * Called when a key is typed.
     */
    public void handleKeyTypeEvent(ViewEvent anEvent)
    {
        // Get event info
        String keyChars = anEvent.getKeyString();
        char keyChar = !keyChars.isEmpty() ? keyChars.charAt(0) : 0;
        boolean charDefined = keyChar != KeyCode.CHAR_UNDEFINED && !Character.isISOControl(keyChar);
        boolean commandDown = anEvent.isShortcutDown();
        boolean controlDown = anEvent.isControlDown();
        boolean emacsDown = SnapEnv.isWindows ? anEvent.isAltDown() : controlDown;

        // If actual text entered, replace
        if (charDefined && !commandDown && !controlDown && !emacsDown) {
            replaceChars(keyChars);
            hideCursor(); //anEvent.consume();
        }
    }

    /**
     * Called when a key is released.
     */
    public void handleKeyReleaseEvent(ViewEvent anEvent)
    {
        updateCaretAnim();
    }

    /**
     * Called when adapter gets Enter key pressed event.
     */
    protected void handleEnterKeyPressEvent(ViewEvent anEvent)
    {
        replaceChars("\n");
        anEvent.consume();
    }

    /**
     * Called when adapter gets backspace key pressed event.
     */
    protected void handleBackSpaceKeyPressEvent(ViewEvent anEvent)
    {
        deleteBackward();
        anEvent.consume();
    }

    /**
     * Called when adapter gets tab key pressed event.
     */
    protected void handleTabKeyPressEvent(ViewEvent anEvent)
    {
        replaceChars("\t");
        anEvent.consume();
    }

    /**
     * Shows the cursor.
     */
    public void showCursor()
    {
        if (_view != null)
            _view.setCursor(Cursor.TEXT);
    }

    /**
     * Hides the cursor.
     */
    public void hideCursor()
    {
        if (_view != null)
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
        if (_view != null)
            repaintSel();
    }

    /**
     * Returns whether caret is needed (true when text is focused, showing and empty selection).
     */
    protected boolean isCaretNeeded()
    {
        if (_view == null || !_view.isShowing())
            return false;
        if (!_view.isFocused())
            return false;
        if (!getSel().isEmpty())
            return false;

        // If there is a window, it should be focused: It is possible to be set Showing, but not in Window
        WindowView window = _view.getWindow();
        if (window != null && !window.isFocused()) {
            PopupWindow popupWindow = window.getPopup();
            if (popupWindow == null || popupWindow.isFocused())
                return false;
        }

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
     * Returns the font scale of the text.
     */
    public double getFontScale()
    {
        if (_textLayout instanceof TextModelX)
            return ((TextModelX) _textLayout).getFontScale();
        return 1;
    }

    /**
     * Sets the font scale of the text.
     */
    public void setFontScale(double aValue)
    {
        if (_textLayout instanceof TextModelX)
            ((TextModelX) _textLayout).setFontScale(aValue);
        else System.out.println("TextAdapter.setFontScale not supported on this text");
        if (_view != null)
            _view.relayoutParent();
    }

    /**
     * Scales font sizes of all text to fit in bounds by finding/setting FontScale.
     */
    public void scaleTextToFit()
    {
        if (_textLayout instanceof TextModelX)
            ((TextModelX) _textLayout).scaleTextToFit();
        else System.out.println("TextAdapter.scaleTextToFit not supported on this text");
        if (_view != null)
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
        if (_textModel.isRichText()) {
            TextModel textForRange = _textModel.copyForRange(selStart, selEnd);
            XMLElement xml = new XMLArchiver().toXML(textForRange);
            String xmlStr = xml.getString();
            clipboard.addData(SNAP_RICHTEXT_TYPE, xmlStr);
        }

        // Add plain text (text/plain)
        String textString = _textModel.subSequence(selStart, selEnd).toString();
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

        // Get clipboard content and replace chars
        Object content = getClipboardContent(clipboard);
        replaceCharsWithContent(content);
    }

    /**
     * Returns the clipboard content.
     */
    private Object getClipboardContent(Clipboard clipboard)
    {
        // If Clipboard has RICHTEXT_TYPE, paste it
        if (clipboard.hasData(SNAP_RICHTEXT_TYPE)) {
            byte[] bytes = clipboard.getDataBytes(SNAP_RICHTEXT_TYPE);
            if (bytes != null && bytes.length > 0) {  // Shouldn't need this - Happens when pasting content from prior instance
                TextModel richText = TextModel.createDefaultTextModel(true);
                XMLArchiver archiver = new XMLArchiver();
                archiver.setRootObject(richText);
                archiver.readXmlFromBytes(bytes);
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
     * Called to undo the last text change.
     */
    public void undo()
    {
        Undoer undoer = getUndoer();
        UndoSet undoSet = undoer.undo();
        if (undoSet != null)
            setTextSelForUndoSet(undoSet, false);
        else ViewUtils.beep();
    }

    /**
     * Called to redo the last text change.
     */
    public void redo()
    {
        Undoer undoer = getUndoer();
        UndoSet undoSet = undoer.redo();
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
            if (propName == TextModel.Chars_Prop) {
                CharSequence addString = (CharSequence) (isRedo ? propChange.getNewValue() : propChange.getOldValue());
                int startIndex = propChange.getIndex();
                int endIndex = startIndex + (addString != null ? addString.length() : 0);
                setSel(startIndex, endIndex);
                break;
            }

            // Handle Style prop: Set sel to char range
            else if (propName == TextModel.Style_Prop) {
                TextModelUtils.StyleChange styleChange = (TextModelUtils.StyleChange) propChange;
                int startIndex = styleChange.getStart();
                int endIndex = styleChange.getEnd();
                setSel(startIndex, endIndex);
            }
        }
    }

    /**
     * Returns the area bounds for given view.
     */
    public Rect getTextBounds()  { return _textLayout.getBounds(); }

    /**
     * Sets the text bounds.
     */
    public void setTextBounds(Rect boundsRect)
    {
        _textLayout.setBounds(boundsRect);
    }

    /**
     * Returns the width needed to display all characters.
     */
    public double getPrefWidth()  { return _textLayout.getPrefWidth(); }

    /**
     * Returns the height needed to display all characters.
     */
    public double getPrefHeight(double aW)  { return _textLayout.getPrefHeight(aW); }

    /**
     * Called when link is triggered.
     */
    protected void handleLinkEvent(ViewEvent anEvent, String aLink)
    {
        BiConsumer<ViewEvent,String> linkHandler = getLinkHandler();
        if (linkHandler != null)
            linkHandler.accept(anEvent, aLink);
    }

    /**
     * Called when TextModel changes (chars added, updated or deleted).
     */
    protected void handleTextModelPropChange(PropChange propChange)
    {
        // Add prop change to undoer
        addTextModelPropChangeToUndoer(propChange);

        // Forward on to listeners
        for (PropChangeListener propChangeLsnr : _textModelPropChangeLsnrs)
            propChangeLsnr.handlePropChange(propChange);

        // Relayout and repaint
        if (_view != null) {
            _view.relayoutParent();
            _view.repaint();
        }
    }

    /**
     * Called when view has prop change.
     */
    private void handleViewPropChange(PropChange propChange)
    {
        switch (propChange.getPropName()) {
            case View.Showing_Prop -> handleViewShowingChange();
            case View.Focused_Prop -> handleViewFocusedChange();
        }
    }

    /**
     * Called when View.Showing changes.
     */
    private void handleViewShowingChange()
    {
        // If focused, update CaretAnim
        if (_view.isFocused()) {
            updateCaretAnim();

            // If Showing, make sure selection is visible
            if (_view.isShowing() && getSelStart() != 0)
                _view.runDelayed(this::scrollSelToVisible, 200);
        }

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
                _windowFocusedChangedLsnr = pc -> handleViewWindowFocusedChange();
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
     * Called when view focus property changes to update caret anim.
     */
    private void handleViewFocusedChange()
    {
        setShowCaret(false);
        updateCaretAnim();
    }

    /**
     * Called when view window focus property changes to update caret anim.
     */
    private void handleViewWindowFocusedChange()
    {
        ViewUtils.runLater(this::updateCaretAnim);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getString();
        if (str.length() > 40)
            str = str.substring(0, 40) + "...";
        return getClass().getSimpleName() + ": " + str;
    }
}