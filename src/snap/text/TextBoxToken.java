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

    // The box line that contains this token
    private TextBoxLine     _line;
    
    // The bounds of this token in TextLine
    private double _x, _y, _width, _height;

    // Shift
    protected double  _shiftX;
    
    // The start/end position of this token in line
    private int  _start, _end;
    
    // The string for this token
    private String  _str;
    
    // The attributes run for this token from line
    private TextStyle  _style;
    
    // The color for this token
    private Color _color;
    
    // Whether token is hyphenated
    private boolean  _hyphenated;
    
    /**
     * Creates a new Token for given box line, TextStyle and character start/end.
     */
    public TextBoxToken(TextBoxLine aLine, TextStyle aStyle, int aStart, int anEnd)
    {
        _line = aLine;
        _start = aStart;
        _end = anEnd;
        _style = aStyle;
        _color = aStyle.getColor();

        if(aStyle.getLink()!=null) {
            setColor(Color.BLUE); setUnderlined(true);
        }
    }

    /**
     * Returns the TextBoxLine.
     */
    public TextBoxLine getLine()  { return _line; }

    /**
     * Returns the token string.
     */
    public String getString()
    {
        if(_str!=null) return _str;
        _str = getLine().subSequence(_start, _end).toString(); if(isHyphenated()) _str += '-';
        return _str;
    }

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
    public double getHeight()  { return _height>0? _height : (_height=_style.getLineHeight()); }

    /**
     * Returns the X location in text global coords.
     */
    public double getTextBoxX()  { return _line.getX() + _x + _shiftX; }

    /**
     * Returns the Y location.
     */
    public double getTextBoxY()  { return _line.getY() + _y; }

    /**
     * Returns the y position for this run text global coords.
     */
    public double getTextBoxStringY()
    {
        // Get offset from y
        double offsetY = _style.getAscent();
        if (getScripting()!=0)
            offsetY += getFont().getSize()*(getScripting()<0? .4f : -.6f);

        // Return TextBoxY plus offset
        return getTextBoxY() + offsetY;
    }

    /**
     * Returns the max X.
     */
    public double getTextBoxMaxX()  { return getTextBoxX() + getWidth(); }

    /**
     * Returns the max Y.
     */
    public double getTextBoxMaxY()  { return getTextBoxY() + getHeight(); }

    /**
     * Returns the start character position of this token in line.
     */
    public int getStart()  { return _start; }

    /**
     * Returns the end character position of this token in line.
     */
    public int getEnd()  { return _end; }

    /**
     * Returns the run associated with this token.
     */
    public TextStyle getStyle()  { return _style; }

    /**
     * Sets the TextStyle.
     */
    public void setStyle(TextStyle aStyle)
    {
        _style = aStyle;
        _height = 0;
    }

    /**
     * Returns the font for this token.
     */
    public Font getFont()  { return _style.getFont(); }

    /**
     * Returns the color for this token.
     */
    public Color getColor()  { return _color; }

    /**
     * Sets the color for this token.
     */
    public void setColor(Color aColor)  { _color = aColor; }

    /**
     * Returns whether this run is underlined.
     */
    public boolean isUnderlined()  { return _style.isUnderlined(); }

    /**
     * Sets whether this run is underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        setStyle(_style.copyFor(TextStyle.UNDERLINE_KEY, aValue ? 1 : 0));
    }

    /**
     * Returns the run's scripting.
     */
    public int getScripting()  { return _style.getScripting(); }

    /**
     * Returns whether this run has a hyphen at the end.
     */
    public boolean isHyphenated()  { return _hyphenated; }

    /**
     * Sets whether this run has a hyphen at the end.
     */
    public void setHyphenated(boolean aFlag)  { _hyphenated = aFlag; _str = null; }

    /**
     * Standard toString implementation.
     */
    public String toString()  { return getClass().getSimpleName() + ": " + getString(); }
}