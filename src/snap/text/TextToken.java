/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.parse.ParseToken;
import snap.parse.Tokenizer;
import snap.util.StringUtils;

/**
 * This class represents a 'word' in a TextLine.
 */
public class TextToken implements ParseToken, Cloneable {

    // The token name
    protected String  _name;

    // The token pattern
    protected String  _pattern;

    // The TextLine
    protected TextLine  _textLine;

    // The start char index in line
    private int _startCharIndexInLine;

    // The end char index in line
    private int _endCharIndexInLine;

    // The index of token in line
    protected int  _index;

    // An override TextStyle (optional)
    private TextStyle _textStyle;

    // An override Text color (optional)
    private Color _color;

    // The X location of token in line
    protected double  _x = -1;

    // The token width
    private double  _width = -1;

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
    public TextToken(TextLine aTextLine, int startCharIndexInLine, int endCharIndexInLine, TextStyle aTextStyle)
    {
        _textLine = aTextLine;
        _startCharIndexInLine = startCharIndexInLine;
        _endCharIndexInLine = endCharIndexInLine;
        _textStyle = aTextStyle;
        _color = _textStyle.getColor();
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
     * Returns the regex pattern that defines this token.
     */
    @Override
    public String getPattern()  { return _pattern; }

    /**
     * Sets the regex pattern that defines this token.
     */
    public void setPattern(String aPattern)  { _pattern = aPattern; }

    /**
     * Returns the TextLine.
     */
    public TextLine getTextLine()  { return _textLine; }

    /**
     * Returns the line index.
     */
    public int getLineIndex()  { return _textLine.getLineIndex(); }

    /**
     * Returns the start char index of token in line.
     */
    public int getStartCharIndexInLine()  { return _startCharIndexInLine; }

    /**
     * Returns the end char index of token in line.
     */
    public int getEndCharIndexInLine()  { return _endCharIndexInLine; }

    /**
     * Returns the start char index of token in text.
     */
    public int getStartCharIndex()  { return _startCharIndexInLine + _textLine.getStartCharIndex(); }

    /**
     * Returns the end char index of token in text.
     */
    public int getEndCharIndex()  { return _endCharIndexInLine + _textLine.getStartCharIndex(); }

    /**
     * Returns the length of token.
     */
    public int getLength()  { return _endCharIndexInLine - _startCharIndexInLine; }

    /**
     * Returns the index of token in line.
     */
    public int getIndex()  { return _index; }

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
        double tokenW = getWidthForLineRange(_startCharIndexInLine, _endCharIndexInLine, true);
        return _width = tokenW;
    }

    /**
     * Returns the token max x.
     */
    public double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the height.
     */
    public double getHeight()  { return _textStyle.getLineHeight(); }

    /**
     * Returns the token X coord in text block.
     */
    public double getTextX()  { return _textLine.getTextX() + getX() + _justifyShiftX; }

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
        TextStyle textStyle = getTextStyle();
        double charSpacing = textStyle.getCharSpacing();

        // Iterate over chars
        double tokenW = 0;
        for (int i = startCharIndex; i < endCharIndex; i++) {
            char loopChar = _textLine.charAt(i);
            tokenW += textStyle.getCharAdvance(loopChar) + charSpacing;
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
            double charsW = getWidthForLineRange(_startCharIndexInLine + charIndex, _startCharIndexInLine, false);
            return charX - charsW;
        }

        // Normal version
        double charX = getX();
        double charsW = getWidthForLineRange(_startCharIndexInLine, _startCharIndexInLine + charIndex, false);
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
        String string = _textLine.subSequence(_startCharIndexInLine, _endCharIndexInLine).toString();
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
        copy._startCharIndexInLine = _startCharIndexInLine + charIndex;
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
        copy._endCharIndexInLine = _startCharIndexInLine + charIndex;
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
        int hyph = TextHyphenDict.getShared().getHyphen(_textLine, _startCharIndexInLine, _endCharIndexInLine);
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
        StringUtils.appendProp(sb, "StartCharIndex", _startCharIndexInLine);
        StringUtils.appendProp(sb, "EndCharIndex", _endCharIndexInLine);
        StringUtils.appendProp(sb, "Index", _index);
        StringUtils.appendProp(sb, "X", _x);
        StringUtils.appendProp(sb, "String", getString());
        return sb.toString();
    }

    /**
     * Returns whether token is special token.
     * This is needed because of CJ error !!!!!!!!
     */
    @Override
    public boolean isSpecial()
    {
        String name = getName();
        return name == Tokenizer.SINGLE_LINE_COMMENT || name == Tokenizer.MULTI_LINE_COMMENT;
    }

    /**
     * Sets the X values for tokens in line.
     */
    private static void setTokensX(TextLine aTextLine)
    {
        TextToken[] tokens = aTextLine.getTokens();
        TextRun textRun = aTextLine.getRun(0);
        TextStyle textStyle = textRun.getTextStyle();
        double charSpacing = textStyle.getCharSpacing();
        int charIndex = 0;
        double tokenX = 0;

        for (TextToken token : tokens) {

            // Update textStyle
            if (token.getTextStyle() != textStyle) {
                textStyle = token.getTextStyle();
                charSpacing = textStyle.getCharSpacing();
            }

            // Find token start: Skip past whitespace
            int tokenStart = token.getStartCharIndexInLine();
            while (charIndex < tokenStart) {
                char loopChar = aTextLine.charAt(charIndex);
                if (loopChar == '\t')
                    tokenX = aTextLine.getXForTabAtIndexAndX(charIndex, tokenX);
                else tokenX += textStyle.getCharAdvance(loopChar) + charSpacing;
                charIndex++;
            }

            // Set token X
            token._x = tokenX;

            // Update token Width
            double tokenW = token.getWidth();
            tokenX += tokenW + charSpacing;
            charIndex = token.getEndCharIndexInLine();
        }
    }
}
