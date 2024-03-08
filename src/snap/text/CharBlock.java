/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.props.PropObject;
import snap.util.CharSequenceX;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages a list of char lines.
 */
public class CharBlock extends PropObject implements CharSequenceX {

    // The lines
    private List<CharLine> _lines = new ArrayList<>();

    // The length of all chars
    private int _length;

    /**
     * Constructor.
     */
    public CharBlock()
    {
        super();
    }

    /**
     * Return number of lines.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Return individual line at given index.
     */
    public CharLine getLine(int anIndex)  { return _lines.get(anIndex); }

    /**
     * Creates a new CharLine for use in this text.
     */
    protected CharLine createLine()
    {
        return new CharLine(this, 0);
    }

    /**
     * Adds a block at given index.
     */
    protected void addLine(CharLine charLine, int anIndex)
    {
        _lines.add(anIndex, charLine);
        charLine._index = anIndex;
        updateLines(anIndex - 1);
    }

    /**
     * Removes the block at given index.
     */
    protected void removeLine(int anIndex)
    {
        CharLine charLine = _lines.remove(anIndex);
        charLine._charBlock = null;
        updateLines(anIndex - 1);
    }

    /**
     * Updates Lines from index line to text end to account for changed line.
     */
    protected void updateLines(int anIndex)
    {
        // Get BaseLine and length at end of BaseLine
        CharLine baseLine = anIndex >= 0 ? getLine(anIndex) : null;
        _length = baseLine != null ? baseLine.getEndCharIndex() : 0;

        // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
        for (int i = anIndex + 1, iMax = _lines.size(); i < iMax; i++) {
            CharLine line = getLine(i);
            updateLine(line, i, _length);
            _length += line.length();
        }
    }

    /**
     * Updates an individual line for new index and start char index.
     */
    protected void updateLine(CharLine textLine, int newIndex, int newStartCharIndex)
    {
        textLine._index = newIndex;
        textLine._startCharIndex = newStartCharIndex;
    }

    /**
     * Returns the last line.
     */
    public CharLine getLastLine()
    {
        int lineCount = getLineCount();
        return lineCount > 0 ? getLine(lineCount - 1) : null;
    }

    /**
     * Returns the block at the given char index.
     */
    public CharLine getLineForCharIndex(int charIndex)
    {
        // Check for index outside bounds or index at end
        int length = length();
        if (charIndex < 0 || charIndex >= length) {
            if (charIndex == length)
                return getLastLine();
            throw new IndexOutOfBoundsException("Index " + charIndex + " outside bounds " + length);
        }

        // Get Low/high indexes
        int lowIndex = 0;
        int highIndex = getLineCount() - 1;

        // Iterate over lines until found
        while (lowIndex <= highIndex) {
            int midIndex = (lowIndex + highIndex) / 2;
            CharLine textLine = getLine(midIndex);
            if (charIndex < textLine.getStartCharIndex())
                highIndex = midIndex - 1;
            else if (charIndex >= textLine.getEndCharIndex())
                lowIndex = midIndex + 1;
            else return textLine;
        }

        // Should be impossible - lines would have to be misconfigured
        throw new IndexOutOfBoundsException("Index not found " + charIndex + " beyond " + length());
    }

    /**
     * Override to return length.
     */
    @Override
    public int length()  { return _length; }

    /**
     * Override to get char from line at char index.
     */
    @Override
    public char charAt(int charIndex)
    {
        CharLine charLine = getLineForCharIndex(charIndex);
        int charIndexInLine = charIndex - charLine.getStartCharIndex();
        return charLine.charAt(charIndexInLine);
    }

    /**
     * Override to get subsequence from lines.
     */
    @Override
    public CharSequence subSequence(int startCharIndex, int endCharIndex)
    {
        int length = endCharIndex - startCharIndex;
        StringBuilder sb = new StringBuilder(length);
        CharLine charLine = getLineForCharIndex(startCharIndex);
        int charIndex = startCharIndex;

        // Iterate over lines until done
        while (charIndex < endCharIndex) {

            // Copy chars from charLine to string builder
            int startCharIndexInLine = charIndex - charLine.getStartCharIndex();
            int endCharIndexForLine = Math.min(endCharIndex, charLine.getEndCharIndex());
            int endCharIndexInLine = endCharIndexForLine - charLine.getStartCharIndex();
            for (int i = startCharIndexInLine; i < endCharIndexInLine; i++)
                sb.append(charLine.charAt(i));

            // Update charIndex and get next line
            charIndex = endCharIndexForLine;
            if (charIndex < endCharIndex)
                charLine = charLine.getNext();
        }

        // Return string
        return sb.toString();
    }
}
