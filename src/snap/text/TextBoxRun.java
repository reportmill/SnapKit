/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;

import snap.gfx.Color;
import snap.gfx.Font;

/**
 * This class represents a range of characters in a TextBoxLine that have the same style.
 */
public class TextBoxRun {
    
    // The line this run is from
    TextBoxLine    _line;
    
    // The start/end char index of this run in line
    int            _start, _end;
    
    // The TextStyle for characters in this run
    TextStyle      _style;

    // The run x and width
    double         _x = -1, _width = -1;
    
/**
 * Creates a new TextBoxRun.
 */
public TextBoxRun(TextBoxLine aLine, TextStyle aStyle, int aStart, int aEnd)
{
    _line = aLine; _style = aStyle; _start = aStart; _end = aEnd;
}

/**
 * Returns the line.
 */
public TextBoxLine getLine()  { return _line; }

/**
 * Returns the run style.
 */
public TextStyle getStyle()  { return _style; }

/**
 * Returns the font.
 */
public Font getFont()  { return _style.getFont(); }

/**
 * Returns the color.
 */
public Color getColor()  { return _style.getColor(); }

/**
 * Returns the start index of this run in line.
 */
public int getStart()  { return _start; }

/**
 * Returns the end index of this run in line.
 */
public int getEnd()  { return _end; }

/**
 * Returns the length of run.
 */
public int length()  { return _end - _start; }

/**
 * Returns an individual char in run.
 */
public char charAt(int anIndex)  { return _line.charAt(_start+anIndex); }

/**
 * Returns the x location of run.
 */
public double getX()
{
    if(_x>=0) return _x;
    return _x = _line.getXForChar(_start);
}

/**
 * Returns the width of run.
 */
public double getWidth()
{
    if(_width>=0) return _width;
    return _width = _line.getXForChar(_end) - getX();
}

/**
 * Returns the max x location of run.
 */
public double getMaxX()  { return getX() + getWidth(); }

/**
 * Returns the baseline y value of run.
 */
public double getBaseline()  { return _line.getBaseline(); }

/**
 * Returns the string.
 */
public String getString()  { return _line.subSequence(_start,_end).toString(); }

/**
 * Standard toString implementation.
 */
public String toString()
{
    return getClass().getSimpleName() + "{ start=" + _start + ", end=" + _end + ",string=" + getString() + " }";
}

}