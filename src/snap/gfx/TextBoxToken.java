/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * A class to represent a word in a line of text.
 */
public class TextBoxToken {

    // The box line that contains this token
    TextBoxLine     _line;
    
    // The bounds of this token in TextLine
    double          _xloc, _yloc, _width, _height, _shiftX;
    
    // The start/end position of this token in line
    int             _start, _end;
    
    // The string for this token
    String          _str;
    
    // The attributes run for this token from line
    TextStyle       _style;
    
    // The color for this token
    Color           _color;
    
    // The font for this token
    Font            _font;
    
    // The scripting of this run
    int             _scripting;
    
    // Whether token is underlined
    boolean         _underlined;
    
    // Whether token is hyphenated
    boolean         _hyphenated;
    
    // The link for token
    TextLink        _link;
    
/**
 * Creates a new Token for given box line, TextStyle and character start/end.
 */
public TextBoxToken(TextBoxLine aLine, TextStyle aStyle, int aStart, int anEnd)
{
    _line = aLine; _start = aStart; _end = anEnd;
    _style = aStyle;
    _font = aStyle.getFont();
    _color = aStyle.getColor();
    _scripting = aStyle.getScripting();
    _underlined = aStyle.isUnderlined();
    _link = aStyle.getLink(); if(_link!=null) { _color = Color.BLUE; _underlined = true; }
}

/**
 * Returns the TextBoxLine.
 */
public TextBoxLine getLine()  { return _line; }

/**
 * Returns the X location in text global coords.
 */
public double getX()  { return _line.getX() + _xloc + _shiftX; }

/**
 * Returns the X location in line coords.
 */
public double getXLocal()  { return _xloc; }

/**
 * Sets the X location of token in line.
 */
public void setXLocal(double aX)  { _xloc = aX; }

/**
 * Returns the Y location.
 */
public double getY()  { return _line.getY() + _yloc; }

/**
 * Sets the Y location of token in line.
 */
public void setYLocal(double aY)  { _yloc = aY; }

/**
 * Returns the y position for this run text global coords.
 */
public double getBaseline()  { return getY() + getBaselineLocal(); }

/**
 * Returns the baseline in line coords.
 */
public double getBaselineLocal()
{
    double offset = getScripting()==0? 0 : getFont().getSize()*(getScripting()<0? .4f : -.6f);
    return _style.getAscent() + offset;
}

/**
 * Sets the token baseline in line coords.
 */
public void setBaselineLocal(double aValue)  { setYLocal(aValue - _style.getAscent()); }

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
 * Returns the max X.
 */
public double getMaxX()  { return getX() + getWidth(); }

/**
 * Returns the max Y.
 */
public double getMaxY()  { return getY() + getHeight(); }

/**
 * Returns the start character position of this token in line.
 */
public int getStart()  { return _start; }

/**
 * Returns the end character position of this token in line.
 */
public int getEnd()  { return _end; }

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
 * Returns the run associated with this token.
 */
public TextStyle getStyle()  { return _style; }

/**
 * Returns the font for this token.
 */
public Font getFont()  { return _font; }

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
public boolean isUnderlined()  { return _underlined; }

/**
 * Sets whether this run is underlined.
 */
public void setUnderlined(boolean aValue)  { _underlined = aValue; }

/**
 * Returns the run's scripting.
 */
public int getScripting()  { return _scripting; }

/**
 * Returns the token's link.
 */
public TextLink getLink()  { return _link; }

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