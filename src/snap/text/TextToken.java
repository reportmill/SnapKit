/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a 'word' in a TextLine.
 */
public class TextToken implements Cloneable {

    // The token name
    private String  _name;

    // The TextLine
    protected TextLine  _textLine;

    // The start char index in line
    private int  _startCharIndex;

    // The end char index in line
    private int  _endCharIndex;

    // The index of token in line
    protected int  _index;

    // The TextRun
    private TextRun  _textRun;

    // An override TextStyle (optional)
    private TextStyle _textStyle;

    // An override Text color (optional)
    private Color _color;

    // The X location of token in line
    protected double  _x = -1;

    // The token width
    private double  _width = -1;

    // The token height
    private double  _height = -1;

    // Whether this token was split
    protected boolean  _split;

    // Whether token is hyphenated
    private boolean _hyphenated;

    // The shift x if text line is justified
    protected double _justifyShiftX = 0;

    // The string for this token
    private String _string;

    /**
     * Constructor.
     */
    public TextToken(TextLine aTextLine, int startCharIndex, int endCharIndex, TextRun aTextRun)
    {
        _textLine = aTextLine;
        _startCharIndex = startCharIndex;
        _endCharIndex = endCharIndex;
        _textRun = aTextRun;
        _textStyle = aTextRun.getStyle();
        _color = aTextRun.getColor();
    }

    /**
     * Returns the token name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the token name.
     */
    public void setName(String aName)  { _name = aName; }

    /**
     * Returns the TextLine.
     */
    public TextLine getTextLine()  { return _textLine; }

    /**
     * Returns the start char index of token in line.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index of token in line.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the length of token.
     */
    public int getLength()  { return _endCharIndex - _startCharIndex; }

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
    public TextStyle getTextStyle()  { return _textStyle; }

    /**
     * Returns the token font.
     */
    public Font getFont()  { return _textStyle.getFont(); }

    /**
     * Returns the token color.
     */
    public Color getTextColor()  { return _color; }

    /**
     * Sets token color.
     */
    public void setTextColor(Color aColor)
    {
        _color = aColor;
    }

    /**
     * Returns whether this run has a hyphen at the end.
     */
    public boolean isHyphenated()  { return _hyphenated; }

    /**
     * Sets whether this run has a hyphen at the end.
     */
    public void setHyphenated(boolean aFlag)  { _hyphenated = aFlag; _string = null; }

    /**
     * Returns the horizontal location of token in line.
     */
    public double getX()
    {
        // If set, just return
        if (_x >= 0) return _x;

        // Set X
        setTokensX(_textLine);

        // Return
        return _x;
    }

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
     * Returns the height.
     */
    public double getHeight()
    {
        if (_height > 0) return _height;
        double height = _textStyle.getLineHeight();
        return _height = height;
    }

    /**
     * Returns the token X coord in text block.
     */
    public double getTextX()
    {
        return _textLine.getTextX() + getX() + _justifyShiftX;
    }

    /**
     * Returns token Y coord in text block.
     */
    public double getTextY()  { return _textLine.getTextY(); }

    /**
     * Returns the y position for this token in text block.
     */
    public double getTextStringY()
    {
        // Get offset from y
        double offsetY = _textStyle.getAscent();
        int scripting = _textStyle.getScripting();
        if (scripting != 0)
            offsetY += getFont().getSize() * (scripting < 0? .4f : -.6f);

        // Return TextBoxY plus offset
        return getTextY() + offsetY;
    }

    /**
     * Returns the max X in text block.
     */
    public double getTextMaxX()
    {
        return getTextX() + getWidth();
    }

    /**
     * Returns the max Y in text block.
     */
    public double getTextMaxY()
    {
        return getTextY() + getHeight();
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

        // Return
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
        // If already set, just return
        if (_string != null) return _string;

        // Get string and append hyphen if set
        String string = _textLine.subSequence(_startCharIndex, _endCharIndex).toString();
        if(isHyphenated())
            string += '-';

        // Set, return
        return _string = string;
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
        TextToken splitCopy = copyToCharIndex(splitCharIndex);
        splitCopy._split = true;
        return splitCopy;
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

        // Get Run info
        TextRun run = aTextLine.getRun(0);
        int runEnd = run.getEndCharIndex();

        // Iterate over line chars
        while (tokenStart < lineLength) {

            // Find token start: Skip past whitespace
            while (tokenStart < runEnd && Character.isWhitespace(aTextLine.charAt(tokenStart)))
                tokenStart++;

            // Find token end: Skip to first non-whitespace char
            int tokenEnd = tokenStart;
            while (tokenEnd < runEnd && !Character.isWhitespace(aTextLine.charAt(tokenEnd)))
                tokenEnd++;

            // If chars found, create/add token
            if (tokenStart < tokenEnd) {
                TextToken token = new TextToken(aTextLine, tokenStart, tokenEnd, run);
                tokens.add(token);
                tokenStart = tokenEnd;
            }

            // If at RunEnd but not LineEnd, update Run info with next run
            if (tokenStart == runEnd && tokenStart < lineLength) {
                run = run.getNext();
                runEnd = run.getEndCharIndex();
            }
        }

        // Return
        return tokens.toArray(new TextToken[0]);
    }

    /**
     * Sets the X values for tokens in line.
     */
    public static void setTokensX(TextLine aTextLine)
    {
        TextToken[] tokens = aTextLine.getTokens();
        TextRun textRun = aTextLine.getRun(0);
        TextStyle runStyle = textRun.getStyle();
        double charSpacing = runStyle.getCharSpacing();
        int charIndex = 0;
        double tokenX = 0;

        for (TextToken token : tokens) {

            // Update runStyle
            if (token.getTextRun() != textRun) {
                textRun = token.getTextRun();
                runStyle = textRun.getStyle();
                charSpacing = runStyle.getCharSpacing();
            }

            // Find token start: Skip past whitespace
            int tokenStart = token.getStartCharIndex();
            while (charIndex < tokenStart) {
                char loopChar = aTextLine.charAt(charIndex);
                if (loopChar == '\t')
                    tokenX = aTextLine.getXForTabAtIndexAndX(charIndex, tokenX);
                else tokenX += runStyle.getCharAdvance(loopChar) + charSpacing;
                charIndex++;
            }

            // Set token X
            token._x = tokenX;

            // Update token Width
            double tokenW = token.getWidth();
            tokenX += tokenW + charSpacing;
            charIndex = token.getEndCharIndex();
        }
    }
}
