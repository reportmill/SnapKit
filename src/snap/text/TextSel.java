/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Path;

/**
 * A class to represent a selection of text.
 */
public class TextSel {

    // The TextBox
    private TextBox  _textBox;

    // The TextDoc
    private TextDoc  _textDoc;

    // The selection anchor
    private int  _anchor;

    // The selection index
    private int  _index;

    // The start/end
    private int  _start;

    // The start/end
    private int  _end;

    /**
     * Creates a new selection.
     */
    public TextSel(TextBox aTextBox, int aStart, int aEnd)
    {
        _textBox = aTextBox;
        _textDoc = _textBox.getTextDoc();
        _anchor = aStart;
        _index = aEnd;
        _start = Math.min(aStart, aEnd);
        _end = Math.max(aStart, aEnd);
    }

    /**
     * Returns the selected range that would result from the given two points.
     */
    public TextSel(TextBox aTextBox, double x1, double y1, double x2, double y2, boolean isWordSel, boolean isParaSel)
    {
        // Get text
        _textBox = aTextBox;
        _textDoc = _textBox.getTextDoc();

        // Get character index for point 1 & point 2
        int p1CharIndex = _textBox.getCharIndexForXY(x1, y1);
        int p2CharIndex = _textBox.getCharIndexForXY(x2, y2);

        // Set selection start and end for selected chars
        int selStart = Math.min(p1CharIndex, p2CharIndex);
        int selEnd = Math.max(p1CharIndex, p2CharIndex);

        // If word selecting, expand selection to word boundary
        if (isWordSel) {
            while (selStart > 0 && isWordChar(_textBox.charAt(selStart - 1))) selStart--;
            while (selEnd < _textBox.length() && isWordChar(_textBox.charAt(selEnd))) selEnd++;
        }

        // If paragraph selecting, expand selection to paragraph boundary
        else if (isParaSel) {
            while (selStart > 0 && !_textDoc.isLineEndChar(selStart - 1)) selStart--;
            while (selEnd < _textBox.length() && !_textDoc.isLineEndChar(selEnd)) selEnd++;
            if (selEnd < _textBox.length()) selEnd++;
        }

        // Set selection char indexes
        _anchor = p1CharIndex < p2CharIndex ? selStart : selEnd;
        _index = p1CharIndex < p2CharIndex ? selEnd : selStart;
        _start = selStart;
        _end = selEnd;
    }

    /**
     * Returns the text.
     */
    public TextBox getTextBox()  { return _textBox; }

    /**
     * Returns the selection anchor (initial char of multi-char selection - usually start).
     */
    public int getAnchor()
    {
        return Math.min(_anchor, _textDoc.length());
    }

    /**
     * Returns the cursor position (final char of multi-char selection - usually end).
     */
    public int getIndex()
    {
        return Math.min(_index, _textDoc.length());
    }

    /**
     * Returns the selection start.
     */
    public int getStart()
    {
        return Math.min(_start, _textDoc.length());
    }

    /**
     * Returns the selection end.
     */
    public int getEnd()
    {
        return Math.min(_end, _textDoc.length());
    }

    /**
     * The length.
     */
    public int getSize()
    {
        return getEnd() - getStart();
    }

    /**
     * Returns whether selection is empty.
     */
    public boolean isEmpty()
    {
        return getStart() == getEnd();
    }

    /**
     * Returns the selected text string.
     */
    public String getString()
    {
        return _textDoc.subSequence(getStart(), getEnd()).toString();
    }

    /**
     * Moves the selection index forward a character (or if a range is selected, moves to end of range).
     */
    public int getCharRight()
    {
        // If selection empty but not at end, get next char (or after newline, if at newline)
        int charIndex = getEnd();
        if (isEmpty() && charIndex < _textBox.length())
            charIndex = _textDoc.isLineEnd(charIndex) ? _textDoc.indexAfterNewline(charIndex) : (charIndex + 1);
        return charIndex;
    }

    /**
     * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
     */
    public int getCharLeft()
    {
        // If selection empty but not at start, get previous char (or before newline if after newline)
        int charIndex = getStart();
        if (isEmpty() && charIndex > 0)
            charIndex = _textDoc.isAfterLineEnd(charIndex) ? _textDoc.lastIndexOfNewline(charIndex) : (charIndex - 1);
        return charIndex;
    }

    /**
     * Moves the selection index up a line, trying to preserve distance from beginning of line.
     */
    public int getCharUp()
    {
        int selIndex = getIndex();
        TextBoxLine lastColumnLine = _textBox.getLineForCharIndex(selIndex);
        int lastColumn = selIndex - lastColumnLine.getStartCharIndex();
        TextBoxLine thisLine = getStartLine();
        TextBoxLine nextLine = thisLine.getPrevious();
        if (nextLine == null)
            return getStart();

        int charIndexInLine = Math.max(Math.min(nextLine.length() - 1, lastColumn), 0);
        return charIndexInLine + nextLine.getStartCharIndex();
    }

    /**
     * Moves the selection index down a line, trying preserve distance from beginning of line.
     */
    public int getCharDown()
    {
        int selIndex = getIndex();
        TextBoxLine lastColumnLine = _textBox.getLineForCharIndex(selIndex);
        int lastColumn = selIndex - lastColumnLine.getStartCharIndex();
        TextBoxLine thisLine = getEndLine();
        TextBoxLine nextLine = thisLine.getNext();
        if (nextLine == null)
            return getEnd();

        int charIndexInLine = Math.max(Math.min(nextLine.length() - 1, lastColumn), 0);
        return charIndexInLine + nextLine.getStartCharIndex();
    }

    /**
     * Moves the insertion point to the beginning of line.
     */
    public int getLineStart()
    {
        // Get index at beginning of current line and index of first non-whitespace char and set selection
        int index1 = _textDoc.lastIndexAfterNewline(getEnd());
        if (index1 < 0)
            index1 = 0;
        int index2 = index1;
        while (index2 < _textBox.length() && _textBox.charAt(index2) == ' ')
            index2++;

        return !isEmpty() || index2 != getStart() ? index2 : index1;
    }

    /**
     * Moves the insertion point to next newline or text end.
     */
    public int getLineEnd()
    {
        // Get index of newline and set selection
        int index = _textDoc.indexOfNewline(getEnd());
        return index >= 0 ? index : _textBox.length();
    }

    /**
     * Returns the line at selection start.
     */
    public TextBoxLine getStartLine()
    {
        return _textBox.getLineForCharIndex(getStart());
    }

    /**
     * Returns the line at selection end.
     */
    public TextBoxLine getEndLine()
    {
        return _textBox.getLineForCharIndex(getEnd());
    }

    /**
     * Returns a path for two char indexes - it will be a a simple box with extensions for first/last lines.
     */
    public Path getPath()
    {
        return _textBox.getPathForCharRange(getStart(), getEnd());
    }

    /**
     * Returns whether a character should be considered is part of a word when WordSelecting.
     */
    protected boolean isWordChar(char c)
    {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + " { " + getStart() + ", " + getEnd() + " }";
    }
}