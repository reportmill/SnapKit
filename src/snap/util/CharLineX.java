/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * This class represents a line of chars in a CharLines.
 */
public class CharLineX implements CharLine {

    // The source char lines
    protected CharBlockX _charBlock;

    // A string builder to hold line chars
    private StringBuilder _sb = new StringBuilder();

    // The index of this line
    protected int _index;

    // The start char index
    protected int _startCharIndex;

    /**
     * Constructor.
     */
    public CharLineX(CharBlockX charBlock, int startCharIndex)
    {
        super();
        _charBlock = charBlock;
        _startCharIndex = startCharIndex;
    }

    /**
     * Returns the source text.
     */
    @Override
    public CharBlock getCharBlock()  { return _charBlock; }

    /**
     * Adds characters to this line at given index.
     */
    protected void addChars(CharSequence theChars, int anIndex)
    {
        // Add chars and update text
        _sb.insert(anIndex, theChars);
        updateText();
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        // Remove chars and update text
        _sb.delete(aStart, anEnd);
        updateText();
    }

    /**
     * Updates text.
     */
    protected void updateText()
    {
        // Update Lines
        if (_charBlock != null)
            _charBlock.updateLines(getIndex());
    }

    /**
     * Returns the index of this line
     */
    @Override
    public int getIndex()  { return _index; }

    /**
     * Returns the start char index in source CharLines.
     */
    @Override
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index in source CharLines.
     */
    @Override
    public int getEndCharIndex()  { return _startCharIndex + _sb.length(); }

    /**
     * Override to return number of chars in line.
     */
    @Override
    public int length()  { return _sb.length(); }

    /**
     * Override to return char at given index in line.
     */
    @Override
    public char charAt(int charIndex)  { return _sb.charAt(charIndex); }

    /**
     * Override to return subSequence of char range in line.
     */
    @Override
    public CharSequence subSequence(int startCharIndex, int endCharIndex)  { return _sb.subSequence(startCharIndex, endCharIndex); }
}
