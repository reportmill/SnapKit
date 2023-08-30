/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.util.StringUtils;

/**
 * This class represents a range of characters in a TextBoxLine that have the same style.
 */
public class TextBoxRun {

    // The line this run is from
    protected TextBoxLine _line;

    // The start char index of this run in line
    protected int _startCharIndex;

    // The end char index of this run in line
    protected int _endCharIndex;

    // The TextStyle for characters in this run
    protected TextStyle _style;

    // The run x
    protected double _x = -1;

    // The run width
    protected double _width = -1;

    /**
     * Creates a new TextBoxRun.
     */
    public TextBoxRun(TextBoxLine aLine, TextStyle aStyle, int aStart, int aEnd)
    {
        _line = aLine;
        _style = aStyle;
        _startCharIndex = aStart;
        _endCharIndex = aEnd;
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
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end index of this run in line.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the length of run.
     */
    public int length()  { return _endCharIndex - _startCharIndex; }

    /**
     * Returns an individual char in run.
     */
    public char charAt(int anIndex)
    {
        return _line.charAt(_startCharIndex + anIndex);
    }

    /**
     * Returns the x location of run.
     */
    public double getX()
    {
        if (_x >= 0) return _x;
        return _x = _line.getXForCharIndex(_startCharIndex);
    }

    /**
     * Returns the width of run.
     */
    public double getWidth()
    {
        if (_width >= 0) return _width;
        return _width = _line.getXForCharIndex(_endCharIndex) - getX();
    }

    /**
     * Returns the max x location of run.
     */
    public double getMaxX()
    {
        return getX() + getWidth();
    }

    /**
     * Returns the baseline y value of run.
     */
    public double getBaseline()
    {
        return _line.getBaseline();
    }

    /**
     * Returns the string.
     */
    public String getString()
    {
        return _line.subSequence(_startCharIndex, _endCharIndex).toString();
    }

    /**
     * Standard toString implementation.
     */
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
        // Add Name
        StringBuffer sb = new StringBuffer();
        StringUtils.appendProp(sb, "Start", _startCharIndex);
        StringUtils.appendProp(sb, "End", _endCharIndex);
        StringUtils.appendProp(sb, "String", getString());
        return sb.toString();
    }
}