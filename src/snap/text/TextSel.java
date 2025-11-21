/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.Shape;
import snap.util.CharSequenceX;
import snap.view.ViewUtils;

/**
 * A class to represent a selection of text.
 */
public class TextSel {

    // The TextLayout
    private TextLayout _textLayout;

    // The TextModel
    private TextModel _textModel;

    // The selection anchor
    private int  _anchor;

    // The selection index
    private int  _index;

    // The start/end
    private int  _start;

    // The start/end
    private int  _end;

    /**
     * Constructor.
     */
    public TextSel(TextLayout textLayout, int aStart, int aEnd)
    {
        _textLayout = textLayout;
        _textModel = textLayout.getTextModel();
        _anchor = aStart;
        _index = aEnd;
        _start = Math.min(aStart, aEnd);
        _end = Math.max(aStart, aEnd);
    }

    /**
     * Constructor for selected range resulting from the given two points.
     */
    public TextSel(TextLayout textLayout, double x1, double y1, double x2, double y2, boolean isWordSel, boolean isParaSel)
    {
        // Get text
        _textLayout = textLayout;
        _textModel = textLayout.getTextModel();

        // Get character index for point 1 & point 2
        int p1CharIndex = _textLayout.getCharIndexForXY(x1, y1);
        int p2CharIndex = _textLayout.getCharIndexForXY(x2, y2);

        // Set selection start and end for selected chars
        int selStart = Math.min(p1CharIndex, p2CharIndex);
        int selEnd = Math.max(p1CharIndex, p2CharIndex);
        int textLength = _textLayout.length();

        // If word selecting, expand selection to word boundary
        if (isWordSel) {
            while (selStart > 0 && isWordChar(_textModel.charAt(selStart - 1)))
                selStart--;
            while (selEnd < textLength && isWordChar(_textModel.charAt(selEnd)))
                selEnd++;
        }

        // If paragraph selecting, expand selection to paragraph boundary
        else if (isParaSel) {
            while (selStart > 0 && !_textModel.isLineEndChar(selStart - 1))
                selStart--;
            while (selEnd < textLength && !_textModel.isLineEndChar(selEnd))
                selEnd++;
            if (selEnd < textLength)
                selEnd++;
        }

        // Set selection char indexes
        _anchor = p1CharIndex < p2CharIndex ? selStart : selEnd;
        _index = p1CharIndex < p2CharIndex ? selEnd : selStart;
        _start = selStart;
        _end = selEnd;
    }

    /**
     * Returns the selection anchor (initial char of multi-char selection - usually start).
     */
    public int getAnchor()  { return Math.min(_anchor, _textModel.length()); }

    /**
     * Returns the cursor position (final char of multi-char selection - usually end).
     */
    public int getIndex()  { return Math.min(_index, _textModel.length()); }

    /**
     * Returns the selection start.
     */
    public int getStart()  { return Math.min(_start, _textModel.length()); }

    /**
     * Returns the selection end.
     */
    public int getEnd()  { return Math.min(_end, _textModel.length()); }

    /**
     * The length.
     */
    public int getSize()  { return getEnd() - getStart(); }

    /**
     * Returns whether selection is empty.
     */
    public boolean isEmpty()  { return getStart() == getEnd(); }

    /**
     * Returns the selected text string.
     */
    public String getString()
    {
        int startCharIndex = getStart();
        int endCharIndex = getEnd();
        return _textModel.subSequence(startCharIndex, endCharIndex).toString();
    }

    /**
     * Moves the selection index forward a character (or if a range is selected, moves to end of range).
     */
    public int getCharRight()
    {
        // If selection empty but not at end, get next char (or after newline, if at newline)
        int charIndex = getEnd();
        if ((isEmpty() || ViewUtils.isShiftDown()) && charIndex < _textLayout.length())
            charIndex = _textLayout.isLineEnd(charIndex) ? _textLayout.indexAfterNewline(charIndex) : (charIndex + 1);
        return charIndex;
    }

    /**
     * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
     */
    public int getCharLeft()
    {
        // If selection empty but not at start, get previous char (or before newline if after newline)
        int charIndex = getStart();
        if ((isEmpty() || ViewUtils.isShiftDown()) && charIndex > 0)
            charIndex = _textLayout.isAfterLineEnd(charIndex) ? _textLayout.lastIndexOfNewline(charIndex) : (charIndex - 1);
        return charIndex;
    }

    /**
     * Moves the selection index up a line, trying to preserve distance from beginning of line.
     */
    public int getCharUp()
    {
        int selIndex = getIndex();
        TextLine lastColumnLine = _textLayout.getLineForCharIndex(selIndex);
        int lastColumn = selIndex - lastColumnLine.getStartCharIndex();
        TextLine thisLine = getStartLine();
        TextLine nextLine = thisLine.getPrevious();
        if (nextLine == null)
            return getStart();

        int charIndexInLine = Math.min(nextLine.length() - 1, lastColumn);
        return charIndexInLine + nextLine.getStartCharIndex();
    }

    /**
     * Moves the selection index down a line, trying preserve distance from beginning of line.
     */
    public int getCharDown()
    {
        int selIndex = getIndex();
        TextLine lastColumnLine = _textLayout.getLineForCharIndex(selIndex);
        int lastColumn = selIndex - lastColumnLine.getStartCharIndex();
        TextLine thisLine = getEndLine();
        TextLine nextLine = thisLine.getNext();
        if (nextLine == null)
            return getEnd();

        int nextLineMax = nextLine.length(); if (nextLine.getString().endsWith("\n")) nextLineMax--;
        int charIndexInLine = Math.min(nextLineMax, lastColumn);
        return charIndexInLine + nextLine.getStartCharIndex();
    }

    /**
     * Moves the insertion point to the beginning of line.
     */
    public int getLineStart()
    {
        // Get index at beginning of current line
        int index1 = _textLayout.lastIndexAfterNewline(getEnd());
        if (index1 < 0)
            index1 = 0;

        // Get index of first non-whitespace char and set selection
        int index2 = index1;
        int textLength = _textLayout.length();
        while (index2 < textLength && _textLayout.charAt(index2) == ' ')
            index2++;

        return !isEmpty() || index2 != getStart() ? index2 : index1;
    }

    /**
     * Moves the insertion point to next newline or text end.
     */
    public int getLineEnd()
    {
        // Get index of newline and set selection
        int index = _textLayout.indexOfNewline(getEnd());
        return index >= 0 ? index : _textLayout.length();
    }

    /**
     * Returns the line at selection start.
     */
    public TextLine getStartLine()
    {
        return _textLayout.getLineForCharIndex(getStart());
    }

    /**
     * Returns the line at selection end.
     */
    public TextLine getEndLine()
    {
        // Get line at end char index
        int endCharIndex = getEnd();
        TextLine endLine = _textLayout.getLineForCharIndex(endCharIndex);

        // If end char index is at start of line and sel not empty, back up to previous line
        if (endCharIndex == endLine.getStartCharIndex() && !isEmpty())
            endLine = endLine.getPrevious();

        // Return
        return endLine;
    }

    /**
     * Returns whether selection is at line end.
     */
    public boolean isAtLineEnd()
    {
        int selCharIndex = getStart();
        TextLine selLine = getStartLine();
        int selLineEnd = selLine.getEndCharIndex();
        if (selLine.isLastCharNewline())
            selLineEnd--;
        return selCharIndex == selLineEnd;
    }

    /**
     * Returns a path for two char indexes - it will be a a simple box with extensions for first/last lines.
     */
    public Shape getPath()  { return _textLayout.getPathForCharRange(getStart(), getEnd()); }

    /**
     * Returns whether a character should be considered is part of a word when WordSelecting.
     */
    protected boolean isWordChar(char c)  { return Character.isLetterOrDigit(c) || c == '_'; }

    /**
     * Standard toString implementation.
     */
    public String toString()  { return getClass().getSimpleName() + " { " + getStart() + ", " + getEnd() + " }"; }
}