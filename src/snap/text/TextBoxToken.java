/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Color;
import snap.gfx.Font;

/**
 * A class to represent a word in a line of text.
 */
public class TextBoxToken {

    // The token name
    private String  _name;

    // The box line that contains this token
    private TextBoxLine  _textLine;

    // The start char index in line
    private int  _startCharIndex;

    // The end char index in line
    private int  _endCharIndex;

    // The attributes run for this token from line
    private TextStyle  _textStyle;

    // The color for this token
    private Color  _color;

    // The bounds of this token in TextLine
    private double  _x, _width, _height;

    // Shift
    protected double  _shiftX;

    // The string for this token
    private String  _string;
    
    // Whether token is hyphenated
    private boolean  _hyphenated;
    
    /**
     * Creates a new Token for given box line, TextStyle and character start/end.
     */
    public TextBoxToken(TextBoxLine aLine, TextStyle aStyle, int aStart, int anEnd)
    {
        _textLine = aLine;
        _startCharIndex = aStart;
        _endCharIndex = anEnd;
        _textStyle = aStyle;
        _color = aStyle.getColor();

        if (aStyle.getLink() != null) {
            setTextColor(Color.BLUE);
            setUnderlined(true);
        }
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
     * Returns the TextBoxLine.
     */
    public TextBoxLine getTextLine()  { return _textLine; }

    /**
     * Returns the start char index of token in line.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index of token in line.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the TextStyle for token.
     */
    public TextStyle getTextStyle()  { return _textStyle; }

    /**
     * Sets the TextStyle for token.
     */
    public void setTextStyle(TextStyle aStyle)
    {
        _textStyle = aStyle;
        _height = 0;
    }

    /**
     * Returns the font for this token.
     */
    public Font getFont()  { return _textStyle.getFont(); }

    /**
     * Returns the color for this token.
     */
    public Color getTextColor()
    {
        if (_color != null)
            return _color;
        return _textStyle.getColor();
    }

    /**
     * Sets the color for this token.
     */
    public void setTextColor(Color aColor)  { _color = aColor; }

    /**
     * Returns whether this run is underlined.
     */
    public boolean isUnderlined()  { return _textStyle.isUnderlined(); }

    /**
     * Sets whether this run is underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        setTextStyle(_textStyle.copyFor(TextStyle.UNDERLINE_KEY, aValue ? 1 : 0));
    }

    /**
     * Returns the run's scripting.
     */
    public int getScripting()  { return _textStyle.getScripting(); }

    /**
     * Returns whether this run has a hyphen at the end.
     */
    public boolean isHyphenated()  { return _hyphenated; }

    /**
     * Sets whether this run has a hyphen at the end.
     */
    public void setHyphenated(boolean aFlag)  { _hyphenated = aFlag; _string = null; }

    /**
     * Returns the X location in line coords.
     */
    public double getX()  { return _x; }

    /**
     * Sets the X location of token in line.
     */
    public void setX(double aX)  { _x = aX; }

    /**
     * Returns the width.
     */
    public double getWidth()  { return _width; }

    /**
     * Sets the width.
     */
    public void setWidth(double aWidth)  { _width = aWidth; }

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
     * Returns the X location in text global coords.
     */
    public double getTextBoxX()
    {
        return _textLine.getX() + _x + _shiftX;
    }

    /**
     * Returns the Y location.
     */
    public double getTextBoxY()  { return _textLine.getY(); }

    /**
     * Returns the y position for this run text global coords.
     */
    public double getTextBoxStringY()
    {
        // Get offset from y
        double offsetY = _textStyle.getAscent();
        int scripting = getScripting();
        if (scripting != 0)
            offsetY += getFont().getSize() * (scripting < 0? .4f : -.6f);

        // Return TextBoxY plus offset
        return getTextBoxY() + offsetY;
    }

    /**
     * Returns the max X.
     */
    public double getTextBoxMaxX()
    {
        return getTextBoxX() + getWidth();
    }

    /**
     * Returns the max Y.
     */
    public double getTextBoxMaxY()
    {
        return getTextBoxY() + getHeight();
    }

    /**
     * Returns the token string.
     */
    public String getString()
    {
        // If already set, just return
        if (_string != null) return _string;

        // Get
        TextBoxLine textLine = getTextLine();
        String string = textLine.subSequence(_startCharIndex, _endCharIndex).toString();
        if(isHyphenated())
            string += '-';

        // Set, return
        return _string = string;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + ": " + getString();
    }
}