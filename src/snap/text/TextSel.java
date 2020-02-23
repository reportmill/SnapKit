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
    TextBox     _tbox;

    // The RichText
    RichText    _text;
    
    // The selection anchor and index
    int         _anchor, _index;

    // The start/end
    int         _start, _end;
    
/**
 * Creates a new selection.
 */
public TextSel(TextBox aTextBox, int aStart, int aEnd)
{
    _tbox = aTextBox; _text = _tbox.getRichText();
    _anchor = aStart; _index = aEnd;
    _start = Math.min(aStart,aEnd); _end = Math.max(aStart,aEnd);
}

/**
 * Returns the selected range that would result from the given two points.
 */
public TextSel(TextBox aTextBox, double x1, double y1, double x2, double y2, boolean isWordSel, boolean isParaSel)
{
    // Get text
    _tbox = aTextBox; _text = _tbox.getRichText();
    
    // Get character index for point 1 & point 2
    int p1Char = _tbox.getCharIndex(x1, y1);
    int p2Char = _tbox.getCharIndex(x2, y2);
    
    // Set selection start and end for selected chars
    int selStart = Math.min(p1Char, p2Char);
    int selEnd = Math.max(p1Char, p2Char);
    
    // If word selecting, expand selection to word boundary
    if(isWordSel) {
        while(selStart>0 && isWordChar(_tbox.charAt(selStart-1))) selStart--;
        while(selEnd<_tbox.length() && isWordChar(_tbox.charAt(selEnd))) selEnd++;
    }
    
    // If paragraph selecting, expand selection to paragraph boundary
    else if(isParaSel) {
        while(selStart>0 && !_text.isLineEndChar(selStart-1)) selStart--;
        while(selEnd<_tbox.length() && !_text.isLineEndChar(selEnd)) selEnd++;
        if(selEnd<_tbox.length()) selEnd++;
    }

    // Set selection char indexes
    _anchor = p1Char<p2Char? selStart : selEnd;
    _index = p1Char<p2Char? selEnd : selStart;
    _start = selStart; _end = selEnd;
}

/**
 * Returns the text.
 */
public TextBox getTextBox()  { return _tbox; }

/**
 * Returns the selection anchor (initial char of multi-char selection - usually start).
 */
public int getAnchor()  { return Math.min(_anchor, _text.length()); }

/**
 * Returns the cursor position (final char of multi-char selection - usually end).
 */
public int getIndex()  { return Math.min(_index, _text.length()); }

/**
 * Returns the selection start.
 */
public int getStart()  { return Math.min(_start, _text.length()); }
    
/**
 * Returns the selection end.
 */
public int getEnd()  { return Math.min(_end, _text.length()); }

/**
 * The length.
 */
public int getSize()  { return getEnd() - getStart(); }

/**
 * Returns whether selection is empty.
 */
public boolean isEmpty()  { return getStart()==getEnd(); }
    
/**
 * Returns the selected text string.
 */
public String getString()  { return _text.subSequence(getStart(), getEnd()).toString(); }

/**
 * Moves the selection index forward a character (or if a range is selected, moves to end of range).
 */
public int getCharRight()
{
    // If selection empty but not at end, get next char (or after newline, if at newline) 
    int index = getEnd();
    if(isEmpty() && index<_tbox.length())
        index = _text.isLineEnd(index)? _text.indexAfterNewline(index) : (index + 1);
    return index;
}

/**
 * Moves the selection index backward a character (or if a range is selected, moves to beginning of range).
 */
public int getCharLeft()
{
    // If selection empty but not at start, get previous char (or before newline if after newline)
    int index = getStart();
    if(isEmpty() && index>0)
        index = _text.isAfterLineEnd(index)? _text.lastIndexOfNewline(index) : (index - 1);
    return index;
}

/**
 * Moves the selection index up a line, trying to preserve distance from beginning of line.
 */
public int getCharUp()
{
    int selIndex = getIndex();
    TextBoxLine lastColumnLine = _tbox.getLineAt(selIndex);
    int lastColumn = selIndex - lastColumnLine.getStart();
    TextBoxLine thisLine = getStartLine(), nextLine = thisLine.getPrevLine();
    int index = nextLine!=null? nextLine.getStart() + Math.min(nextLine.length()-1, lastColumn) : getStart();
    return index;
}

/**
 * Moves the selection index down a line, trying preserve distance from beginning of line.
 */
public int getCharDown()
{
    int selIndex = getIndex();
    TextBoxLine lastColumnLine = _tbox.getLineAt(selIndex);
    int lastColumn = selIndex - lastColumnLine.getStart();
    TextBoxLine thisLine = getEndLine(), nextLine = thisLine.getNextLine();
    int index = nextLine!=null? nextLine.getStart() + Math.min(nextLine.length()-1, lastColumn) : getEnd();
    return index;
}

/**
 * Moves the insertion point to the beginning of line.
 */
public int getLineStart()
{
    // Get index at beginning of current line and index of first non-whitespace char and set selection
    int index1 = _text.lastIndexAfterNewline(getEnd()); if(index1<0) index1 = 0;
    int index2 = index1; while(index2<_tbox.length() && _tbox.charAt(index2)==' ') index2++;
    return !isEmpty() || index2!=getStart()? index2 : index1;
}

/**
 * Moves the insertion point to next newline or text end.
 */
public int getLineEnd()
{
    // Get index of newline and set selection
    int index = _text.indexOfNewline(getEnd());
    return index>=0? index : _tbox.length();
}

/**
 * Returns the line at selection start.
 */
public TextBoxLine getStartLine()  { return _tbox.getLineAt(getStart()); }

/**
 * Returns the line at selection end.
 */
public TextBoxLine getEndLine()  { return _tbox.getLineAt(getEnd()); }

/**
 * Returns a path for two char indexes - it will be a a simple box with extensions for first/last lines.
 */
public Path getPath()  { return _tbox.getPathForChars(getStart(),getEnd()); }

/**
 * Returns whether a character should be considered is part of a word when WordSelecting.
 */
protected boolean isWordChar(char c)  { return Character.isLetterOrDigit(c) || c=='_'; }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + " { " + getStart() + ", " + getEnd() + " }"; }

}