package snap.text;
import snap.util.CharSequenceUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a TextModel implementation using an array of text lines.
 */
public class TextBlock extends TextModel {

    // The TextLines in this text
    protected List<TextLine> _lines = new ArrayList<>();

    /**
     * Constructor.
     */
    public TextBlock()
    {
        this(false);
    }

    /**
     * Constructor with option to make rich text.
     */
    public TextBlock(boolean isRich)
    {
        super(isRich);
        TextLine defaultLine = new TextLine(this);
        addLine(defaultLine, 0);
    }

    /**
     * Returns the string for the text.
     */
    public String getString()
    {
        StringBuilder sb = new StringBuilder(length());
        _lines.forEach(line -> sb.append(line._sb));
        return sb.toString();
    }

    /**
     * Returns the number of block in this doc.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Returns the individual block in this doc.
     */
    public TextLine getLine(int anIndex)  { return _lines.get(anIndex); }

    /**
     * Returns the list of blocks.
     */
    public List<TextLine> getLines()  { return _lines; }

    /**
     * Adds a block at given index.
     */
    protected void addLine(TextLine aLine, int anIndex)
    {
        _lines.add(anIndex, aLine);
        aLine._textModel = this;
        updateLines(anIndex - 1);
    }

    /**
     * Removes the block at given index.
     */
    protected void removeLine(int anIndex)
    {
        TextLine line = _lines.remove(anIndex);
        line._textModel = line;
        updateLines(anIndex - 1);
    }

    /**
     * Adds characters with given style to this text at given index.
     */
    public void addCharsWithStyle(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // If no chars, just return
        if (theChars == null) return;

        // Get line for char index
        TextLine textLine = getLineForCharIndex(anIndex);

        // If adding at text end and last line and ends with newline, create/add new line (should never happen)
        if (anIndex == textLine.getEndCharIndex() && textLine.isLastCharNewline()) {
            TextLine remainderLine = textLine.splitLineAtIndex(textLine.length());
            addLine(remainderLine, textLine.getLineIndex() + 1);
            textLine = remainderLine;
        }

        // Loop vars
        int charsLength = theChars.length();
        int charIndexInChars = 0;

        // Iterate over chars until all added
        while (charIndexInChars < charsLength) {

            // Get chars up to first newline or end
            CharSequence chars = theChars;
            int newlineIndex = CharSequenceUtils.indexAfterNewline(theChars, charIndexInChars);
            int endCharIndexInChars = newlineIndex > 0 ? newlineIndex : charsLength;
            if (charIndexInChars > 0 || endCharIndexInChars < charsLength)
                chars = theChars.subSequence(charIndexInChars, endCharIndexInChars);

            // Get char index and update line
            int charIndex = anIndex + charIndexInChars;
            if (charIndexInChars > 0) {
                while (charIndex > textLine.getEndCharIndex() || charIndex == textLine.getEndCharIndex() && textLine.isLastCharNewline())
                    textLine = textLine.getNext();
            }

            // Add chars to line
            addCharsToLine(chars, theStyle, charIndex, textLine, newlineIndex > 0);

            // Set start to last end
            charIndexInChars += chars.length();
        }

        // Send PropertyChange
        if (isPropChangeEnabled())
            firePropChange(new TextModelUtils.CharsChange(this, null, theChars, anIndex));
        _prefW = -1;
    }

    /**
     * Adds a block of chars to line - each block is guaranteed to either have no newlines or end with newline.
     */
    protected void addCharsToLine(CharSequence theChars, TextStyle theStyle, int charIndex, TextLine textLine, boolean charsHaveNewline)
    {
        // Add chars to line
        int charIndexInLine = charIndex - textLine.getStartCharIndex();
        textLine.addCharsWithStyle(theChars, theStyle, charIndexInLine);

        // If chars have newline, move chars after newline to next line
        if (charsHaveNewline) {
            int moveCharsIndexInLine = charIndexInLine + theChars.length();
            moveLineCharsToNextLine(textLine, moveCharsIndexInLine);
        }

        // Reset line alignment
        textLine._x = 0;

        // Perform post processing
        addCharsToLineFinished(textLine);
    }

    /**
     * Called after chars added to line to do further processing, like horizontal alignment or wrapping.
     */
    protected void addCharsToLineFinished(TextLine textLine)
    {
        textLine.updateAlignmentAndJustify();
    }

    /**
     * Move line chars from given start char index to line end to next line.
     */
    protected void moveLineCharsToNextLine(TextLine textLine, int startCharIndex)
    {
        // Get next line to move chars to
        TextLine nextLine = textLine.getNext();

        // If no next line or moving chars + newline, create/add new line
        if (nextLine == null || startCharIndex < textLine.length() && textLine.isLastCharNewline()) {
            nextLine = textLine.clone();
            nextLine.removeChars(0, nextLine.length());
            addLine(nextLine, textLine.getLineIndex() + 1);
        }

        // Get last run
        TextRun lastRun = textLine.getLastRun();

        // Iterate over runs from end of line, moving chars from each to next line
        while (textLine.length() > startCharIndex) {

            // Remove run chars from text line (chars after startCharIndex)
            int runStartCharIndex = Math.max(startCharIndex, lastRun.getStartCharIndex());
            CharSequence moveChars = textLine.subSequence(runStartCharIndex, textLine.length());
            textLine.removeChars(runStartCharIndex, textLine.length());

            // Add run chars to next line
            TextStyle textStyle = lastRun.getTextStyle();
            int nextLineStartCharIndex = nextLine.getStartCharIndex();
            boolean charsHaveNewline = CharSequenceUtils.indexAfterNewline(moveChars, 0) > 0;
            addCharsToLine(moveChars, textStyle, nextLineStartCharIndex, nextLine, charsHaveNewline);

            // Get previous run
            lastRun = lastRun.getPrevious();
        }
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStartCharIndex, int anEndCharIndex)
    {
        // If empty range, just return
        if (anEndCharIndex == aStartCharIndex) return;

        // If PropChangeEnabled, get chars to be deleted
        CharSequence removedChars = isPropChangeEnabled() ? subSequence(aStartCharIndex, anEndCharIndex) : null;

        // Delete lines/chars for range from end to start
        int removeEndCharIndex = anEndCharIndex;
        while (removeEndCharIndex > aStartCharIndex) {

            // Get line at end index
            TextLine textLine = getLineForCharIndex(removeEndCharIndex);
            if (removeEndCharIndex == textLine.getStartCharIndex())
                textLine = textLine.getPrevious();

            // Get Line.Start
            int lineStartCharIndex = textLine.getStartCharIndex();
            int removeStartCharIndex = Math.max(aStartCharIndex, lineStartCharIndex);
            removeCharsFromLine(removeStartCharIndex, removeEndCharIndex, textLine);

            // Reset end
            removeEndCharIndex = lineStartCharIndex;
        }

        // If deleted chars is set, send property change
        if (removedChars != null && isPropChangeEnabled())
            firePropChange(new TextModelUtils.CharsChange(this, removedChars, null, aStartCharIndex));
        _prefW = -1;
    }

    /**
     * Remove chars from line.
     */
    protected void removeCharsFromLine(int startCharIndex, int endCharIndex, TextLine textLine)
    {
        // Simple case: If range is whole line, can just remove line if more than one line ...
        int lineStartCharIndex = textLine.getStartCharIndex();
        if (startCharIndex == lineStartCharIndex && endCharIndex == textLine.getEndCharIndex() && getLineCount() > 1) {

            // ... and either (1) line is first line or (2) previous line wrapped or (3) not last line
            TextLine previousLine = textLine.getPrevious();
            if (previousLine == null || !previousLine.isLastCharNewline() || textLine != getLastLine()) {
                removeLine(textLine.getLineIndex());
                return;
            }
        }

        // Remove chars from line
        int startCharIndexInLine = startCharIndex - lineStartCharIndex;
        int endCharIndexInLine = endCharIndex - lineStartCharIndex;
        textLine.removeChars(startCharIndexInLine, endCharIndexInLine);

        // If no newline remaining in line, join with next line
        if (!textLine.isLastCharNewline())
            joinLineWithNextLine(textLine);
    }

    /**
     * Joins given line with next line.
     */
    protected void joinLineWithNextLine(TextLine textLine)
    {
        TextLine nextLine = textLine.getNext();
        if (nextLine == null)
            return;

        // Iterate over NextLine runs and add chars for each
        TextRun[] textRuns = nextLine.getRuns();
        for (TextRun textRun : textRuns) {
            CharSequence chars = textRun.getString();
            TextStyle textStyle = textRun.getTextStyle();
            int endCharIndex = textLine.getEndCharIndex();
            addCharsToLine(chars, textStyle, endCharIndex, textLine, false);
        }

        // Remove NextLine
        removeLine(nextLine.getLineIndex());
    }

    /**
     * Resets Line Y positions for lines after given line index.
     */
    protected void resetLineYForLinesAfterIndex(int lineIndex)
    {
        // Iterate over lines beyond given lineIndex and reset Y (stop if line is already reset)
        for (int i = lineIndex + 1, iMax = getLineCount(); i < iMax; i++) {
            TextLine line = getLine(i);
            if (line._y == -1)
                return;
            line._y = -1;
        }
    }

    /**
     * Updates Lines (Index, Start) from index line to text end.
     */
    protected void updateLines(int anIndex)
    {
        // Get BaseLine and length at end of BaseLine
        TextLine baseLine = anIndex >= 0 ? getLine(anIndex) : null;
        _length = baseLine != null ? baseLine.getEndCharIndex() : 0;

        // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
        for (int i = anIndex + 1, iMax = getLineCount(); i < iMax; i++) {
            TextLine line = getLine(i);
            updateLine(line, i, _length);
            _length += line.length();
        }

        // Reset AlignY offset
        _alignedY = -1;
    }

    /**
     * Updates an individual line for new index and start char index.
     */
    protected void updateLine(TextLine textLine, int newIndex, int newStartCharIndex)
    {
        textLine._lineIndex = newIndex;
        textLine._startCharIndex = newStartCharIndex;
        textLine._y = -1;
    }

    /**
     * Sets the width.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;
        super.setWidth(aValue);
        _lines.forEach(line -> line.updateAlignmentAndJustify());
    }
}
