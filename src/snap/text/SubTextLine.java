/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import java.util.Arrays;

/**
 * This class represents a line of text in a Text.
 */
public class SubTextLine extends TextLine {

    // The real TextLine that this object maps to
    protected TextLine  _textLine;

    // The start char index in TextLine
    protected int  _startCharIndexInLine;

    // The length of line
    protected int  _length;

    /**
     * Constructor.
     */
    public SubTextLine(SubText aSubText, TextLine aTextLine, int lineLength)
    {
        super(aSubText);

        // Set ivars
        _textLine = aTextLine;
        _startCharIndexInLine = 0;
        _length = lineLength;

        // Create Runs
        TextRun[] textLineRuns = _textLine.getRuns();
        TextRun[] subLineRuns = _runs = new TextRun[textLineRuns.length];
        for (int i = 0; i < textLineRuns.length; i++) {
            TextRun textLineRun = textLineRuns[i];
            TextRun subLineRun = subLineRuns[i] = new TextRun(this);
            subLineRun._start = textLineRun._start;
            subLineRun._length = textLineRun._length;
            subLineRun._style = textLineRun._style;
            subLineRun._index = i;
        }
    }

    /**
     * Returns the length of this text line.
     */
    public int length()  { return _length; }

    /**
     * Sets the length.
     */
    public void setLength(int aLength)
    {
        _length = aLength;
        _runs[0]._length = aLength;
        updateText();
    }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)
    {
        return _textLine.charAt(_startCharIndexInLine + anIndex);
    }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        return _textLine.subSequence(_startCharIndexInLine + aStart, _startCharIndexInLine + anEnd);
    }

    /**
     * Returns the index of given string in line.
     */
    public int indexOf(String aStr, int aStart)
    {
        return _textLine.indexOf(aStr, _startCharIndexInLine + aStart);
    }

    /**
     * Returns the string for the line.
     */
    public String getString()
    {
        return subSequence(0, length()).toString();
    }

    /**
     * Returns the line style.
     */
    public TextLineStyle getLineStyle()  { return _textLine.getLineStyle(); }

    /**
     * Sets the line style.
     */
    public void setLineStyle(TextLineStyle aLineStyle)  { _textLine.setLineStyle(aLineStyle); }

    /**
     * Override to get tokens from TextLine.
     */
    @Override
    protected TextToken[] createTokens()
    {
        // Get normal line tokens - just return if empty
        TextToken[] tokens = _textLine.getTokens();
        if (tokens.length == 0)
            return tokens;

        // Get copy vars
        TextToken[] subTokens = new TextToken[tokens.length];
        int lineLength = length();
        int subTokenCount = 0;

        // Iterate over tokens and copy those within Subline
        for (TextToken token : tokens) {

            // If loop token start beyond line, stop
            if (token.getStartCharIndex() > lineLength)
                break;

            // Copy token
            token = token.clone();
            token._textLine = this;

            // If loop token stradles line, do split copy
            if (token.getEndCharIndex() > lineLength) {
                int tokenLength = lineLength - token.getStartCharIndex();
                token = token.copyToCharIndex(tokenLength);
            }

            // Add to array
            subTokens[subTokenCount++] = token;
        }

        // If some tokens didn't make it, trim array
        if (subTokenCount < tokens.length)
            subTokens = Arrays.copyOf(subTokens, subTokenCount);

        // Return
        return subTokens;
    }

    /**
     * Returns a copy of this line for given char range.
     */
    public TextLine copyForRange(int aStart, int aEnd)
    {
        return _textLine.copyForRange(_startCharIndexInLine + aStart, _startCharIndexInLine + aEnd);
    }
}