package snap.gfx;

/**
 * A class to represent a selection of text.
 */
public class TextSel {

    // The TextBox
    TextBox     _tbox;

    // The RichText
    RichText    _text;

    // The start/end
    int         _start, _end;
    
    // The anchor (usually End)
    int         _anchor;

/**
 * Creates a new selection.
 */
public TextSel(TextBox aTextBox, int aStart, int anEnd) { this(aTextBox, aStart, anEnd, anEnd); }

/**
 * Creates a new selection.
 */
public TextSel(TextBox aTextBox, int aStart, int anEnd, int anAnchor)
{
    _tbox = aTextBox; _text = _tbox.getText(); _start = aStart; _end = anEnd; _anchor = anAnchor;
}

/**
 * Returns the selected range that would result from the given two points.
 */
public TextSel(TextBox aTextBox, double x1, double y1, double x2, double y2, boolean isWordSel, boolean isParaSel)
{
    // Get text
    _tbox = aTextBox; _text = _tbox.getText();
    
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
        while(selStart>0 && !_text.isNewlineChar(selStart-1)) selStart--;
        while(selEnd<_tbox.length() && !_text.isNewlineChar(selEnd)) selEnd++;
        if(selEnd<_tbox.length()) selEnd++;
    }

    // Set values
    _start = selStart; _end = selEnd; _anchor = selEnd;
}

/**
 * Returns the text.
 */
public TextBox getTextBox()  { return _tbox; }

/**
 * Returns the start.
 */
public int getStart()  { return Math.min(_start,_text.length()); }
    
/**
 * Returns the end.
 */
public int getEnd()  { return Math.min(_end,_text.length()); }

/**
 * Returns the anchor.
 */
public int getAnchor()  { return Math.min(_anchor<=_start? _start : _end,_text.length()); }

/**
 * The length.
 */
public int getSize()  { return _end - _start; }

/**
 * Returns whether selection is empty.
 */
public boolean isEmpty()  { return _start==_end; }
    
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
    TextBoxLine lastColumnLine = _tbox.getLineAt(_anchor);
    int lastColumn = _anchor - lastColumnLine.getStart();
    TextBoxLine thisLine = getStartLine(), nextLine = thisLine.getPrevLine();
    int index = nextLine!=null? nextLine.getStart() + Math.min(nextLine.length()-1, lastColumn) : getStart();
    return index;
}

/**
 * Moves the selection index down a line, trying preserve distance from beginning of line.
 */
public int getCharDown()
{
    TextBoxLine lastColumnLine = _tbox.getLineAt(_anchor);
    int lastColumn = _anchor - lastColumnLine.getStart();
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