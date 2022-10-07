/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a 'word' in a TextLine.
 */
public class TextToken implements Cloneable {

    // The TextLine
    private TextLine  _textLine;

    // The start char index in line
    private int  _startCharIndex;

    // The end char index in line
    private int  _endCharIndex;

    // The end char index in line including space
    private int  _endAllCharIndex;

    // The index of token in line
    protected int  _index;

    // The TextRun
    private TextRun  _textRun;

    // The X location of token in line
    protected double  _x;

    // The width
    private double  _width = -1;

    // Whether this token was split
    protected boolean  _split;

    /**
     * Constructor.
     */
    public TextToken(TextLine aTextLine, int startCharIndex, int endCharIndex, TextRun aTextRun)
    {
        _textLine = aTextLine;
        _startCharIndex = startCharIndex;
        _endCharIndex = endCharIndex;
        _textRun = aTextRun;

        // Get end char index including white space
        _endAllCharIndex = _endCharIndex;
        while (_endAllCharIndex < _textRun.getEnd() && Character.isWhitespace(_textLine.charAt(_endAllCharIndex)))
            _endAllCharIndex++;
    }

    /**
     * Returns the TextLine.
     */
    public TextLine getTextLine()  { return _textLine; }

    /**
     * Returns the start char index.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the length of token.
     */
    public int getLength()  { return _endCharIndex - _startCharIndex; }

    /**
     * Returns the end char index in line including white space.
     */
    public int getEndAllCharIndex()  { return _endAllCharIndex; }

    /**
     * Returns the index of token in line.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the TextRun.
     */
    public TextRun getTextRun()  { return _textRun; }

    /**
     * Returns the TextStyle for token.
     */
    public TextStyle getTextStyle()  { return _textRun.getStyle(); }

    /**
     * Returns the horizontal location of token in line.
     */
    public double getX()  { return _x; }

    /**
     * Returns the width.
     */
    public double getWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Get, set, return
        double tokenW = getWidthForLineRange(_startCharIndex, _endCharIndex, true);
        return _width = tokenW;
    }

    /**
     * Returns the width for given char range.
     */
    protected double getWidthForLineRange(int startCharIndex, int endCharIndex, boolean trimCharSpacing)
    {
        // Get run info
        TextStyle runStyle = _textRun.getStyle();
        double charSpacing = runStyle.getCharSpacing();

        // Iterate over chars
        double tokenW = 0;
        for (int i = startCharIndex; i < endCharIndex; i++) {
            char loopChar = _textLine.charAt(i);
            tokenW += runStyle.getCharAdvance(loopChar) + charSpacing;
        }

        // If TrimCharSpacing, remove extra spacing
        if (trimCharSpacing && charSpacing != 0)
            tokenW -= charSpacing;

        // Set, return
        return tokenW;
    }

    /**
     * Returns the X for given char index.
     */
    public double getXForCharIndex(int charIndex)
    {
        // Weirdo
        if (charIndex < 0) {
            double charX = getX();
            double charsW = getWidthForLineRange(_startCharIndex + charIndex, _startCharIndex, false);
            return charX - charsW;
        }

        // Normal version
        double charX = getX();
        double charsW = getWidthForLineRange(_startCharIndex, _startCharIndex + charIndex, false);
        return charX + charsW;
    }

    /**
     * Returns the next token, if available.
     */
    public TextToken getNext()
    {
        int nextIndex = _index + 1;
        TextToken[] tokens = _textLine.getTokens();
        return nextIndex < tokens.length ? tokens[nextIndex] : null;
    }

    /**
     * Returns the previous token, if available.
     */
    public TextToken getPrevious()
    {
        int prevIndex = _index - 1;
        TextToken[] tokens = _textLine.getTokens();
        return prevIndex >= 0 ? tokens[prevIndex] : null;
    }

    /**
     * Returns the string.
     */
    public String getString()
    {
        return _textLine.subSequence(_startCharIndex, _endCharIndex).toString();
    }

    /**
     * Returns a copy from given char index to end.
     */
    public TextToken copyFromCharIndex(int charIndex)
    {
        TextToken copy = clone();
        copy._startCharIndex = _startCharIndex + charIndex;
        copy._width = -1;
        copy._x = getXForCharIndex(charIndex);
        return copy;
    }

    /**
     * Returns a copy of leading chars to given char index.
     */
    public TextToken copyToCharIndex(int charIndex)
    {
        TextToken copy = clone();
        copy._endCharIndex = _startCharIndex + charIndex;
        copy._width = -1;
        copy._split = true;
        return copy;
    }

    /**
     * Returns whether token can be split.
     */
    public boolean isSplittable()
    {
        int splittable = getSplittableCharIndex();
        return splittable > 0;
    }

    /**
     * Returns the last split index.
     */
    public int getSplittableCharIndex()
    {
        int hyph = TextHyphenDict.getShared().getHyphen(_textLine, _startCharIndex, _endCharIndex);
        if (hyph >= getLength())
            return 0;
        return hyph;
    }

    /**
     * Returns a copy from given char index on.
     */
    public TextToken copyForSplittable()
    {
        int splitCharIndex = getSplittableCharIndex();
        return copyToCharIndex(splitCharIndex);
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public TextToken clone()
    {
        TextToken clone;
        try { clone = (TextToken) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        StringBuffer sb = new StringBuffer();
        StringUtils.appendProp(sb, "StartCharIndex", _startCharIndex);
        StringUtils.appendProp(sb, "EndCharIndex", _endCharIndex);
        StringUtils.appendProp(sb, "Index", _index);
        StringUtils.appendProp(sb, "X", _x);
        StringUtils.appendProp(sb, "String", getString());
        return sb.toString();
    }

    /**
     * Returns the tokens.
     */
    public static TextToken[] createTokensForTextLine(TextLine aTextLine)
    {
        // Loop vars
        List<TextToken> tokens = new ArrayList<>();
        int tokenStart = 0;
        int lineLength = aTextLine.length();
        double tokenX = 0;

        // Get Run info
        TextRun run = aTextLine.getRun(0);
        int runEnd = run.getEnd();
        TextStyle runStyle = run.getStyle();
        double charSpacing = runStyle.getCharSpacing();

        // Iterate over line chars
        while (tokenStart < lineLength) {

            // Find token start: Skip past whitespace
            char loopChar;
            while (tokenStart < runEnd && Character.isWhitespace(loopChar = aTextLine.charAt(tokenStart))) {
                if (loopChar == '\t')
                    tokenX = aTextLine.getXForTabAtIndexAndX(tokenStart, tokenX);
                else tokenX += runStyle.getCharAdvance(loopChar) + charSpacing;
                tokenStart++;
            }

            // Find token end: Skip to first non-whitespace char
            int tokenEnd = tokenStart;
            while (tokenEnd < runEnd && !Character.isWhitespace(aTextLine.charAt(tokenEnd)))
                tokenEnd++;

            // If chars found, create/add token
            if (tokenStart < tokenEnd) {
                TextToken token = new TextToken(aTextLine, tokenStart, tokenEnd, run);
                token._index = tokens.size();
                token._x = tokenX;
                tokens.add(token);
                tokenStart = tokenEnd;
                double tokenW = token.getWidth();
                tokenX += tokenW;
            }

            // If at RunEnd but not LineEnd, update Run info with next run
            if (tokenStart == runEnd && tokenStart < lineLength) {
                run = run.getNext();
                runEnd = run.getEnd();
                runStyle = run.getStyle();
                charSpacing = runStyle.getCharSpacing();
            }
        }

        // Return
        return tokens.toArray(new TextToken[0]);
    }
}
