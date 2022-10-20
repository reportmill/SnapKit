/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.util.CharSequenceX;

/**
 * This is class represents a range of characters in a TextLine that share the same style.
 */
public class TextRun implements CharSequenceX, Cloneable {

    // The line that contains this run
    protected TextLine  _textLine;

    // The start char index of this run in line
    protected int  _startCharIndex;

    // The char length of this run
    protected int  _length;

    // The attributes of the Run
    protected TextStyle  _style = TextStyle.DEFAULT;

    // The index of this run in line
    protected int  _index;

    // The width of run
    protected double  _width = -1;

    /**
     * Constructor.
     */
    public TextRun(TextLine aTextLine)
    {
        super();
        _textLine = aTextLine;
    }

    /**
     * Returns the TextLine that contains this TextRun.
     */
    public TextLine getLine()  { return _textLine; }

    /**
     * Returns the start char index for this run.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index for this run.
     */
    public int getEndCharIndex()  { return _startCharIndex + length(); }

    /**
     * Returns the run style.
     */
    public TextStyle getStyle()  { return _style; }

    /**
     * Sets the run style.
     */
    protected void setStyle(TextStyle aStyle)
    {
        _style = aStyle;
        _width = -1;
        _textLine._width = -1;
        _textLine._tokens = null;
    }

    /**
     * Returns the run index.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the length in characters for this run.
     */
    public int length()  { return _length; }

    /**
     * CharSequence method returning character at given index.
     */
    public char charAt(int anIndex)
    {
        return _textLine.charAt(_startCharIndex + anIndex);
    }

    /**
     * CharSequence method return character sequence for range.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        return _textLine.subSequence(_startCharIndex + aStart, _startCharIndex + anEnd);
    }

    /**
     * Returns the string for this run.
     */
    public String getString()
    {
        CharSequence chars = _textLine.subSequence(_startCharIndex, _startCharIndex + _length);
        return chars.toString();
    }

    /**
     * Returns the width of run.
     */
    public double getWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Calculate, set, return
        double width = getWidth(0);
        return _width = width;
    }

    /**
     * Returns the width of run from given index.
     */
    public double getWidth(int anIndex)
    {
        // If zero, return cached
        if (anIndex <= 0 && _width >= 0) return getWidth();

        // Calculate
        double width = 0;
        int len = length();
        while (len - 1 > 0 && Character.isWhitespace(charAt(len - 1))) len--;
        for (int i = anIndex; i < len; i++)
            width += _style.getCharAdvance(charAt(i));
        if (len - anIndex > 1)
            width += (len - anIndex - 1) * getCharSpacing();

        // Return
        return width;
    }

    /**
     * Adds length to grow this run (negative value reduces it).
     */
    protected void addLength(int aLength)
    {
        _length += aLength;
        _width = -1;
        assert (_length >= 0);
    }

    /**
     * Returns the font for this run.
     */
    public Font getFont()
    {
        return getStyle().getFont();
    }

    /**
     * Returns the color for this run.
     */
    public Color getColor()
    {
        return getStyle().getColor();
    }

    /**
     * Returns the format for this run.
     */
    public TextFormat getFormat()
    {
        return getStyle().getFormat();
    }

    /**
     * Returns the border for this run.
     */
    public Border getBorder()
    {
        return getStyle().getBorder();
    }

    /**
     * Returns whether this run is underlined.
     */
    public boolean isUnderlined()
    {
        return getStyle().isUnderlined();
    }

    /**
     * Returns the underline style of this run.
     */
    public int getUnderlineStyle()
    {
        return getStyle().getUnderlineStyle();
    }

    /**
     * Returns the scripting for this run (1=SuperScripting, -1=Subscripting, 0=none).
     */
    public int getScripting()
    {
        return getStyle().getScripting();
    }

    /**
     * Returns the char spacing.
     */
    public float getCharSpacing()
    {
        return (float) getStyle().getCharSpacing();
    }

    /**
     * Returns the char advance for a given character.
     */
    public double getCharAdvance(char aChar)
    {
        return getFont().charAdvance(aChar);
    }

    /**
     * Returns the max distance above the baseline for this run font.
     */
    public double getAscent()
    {
        return getFont().getAscent();
    }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()
    {
        return getFont().getDescent();
    }

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()
    {
        return getFont().getLeading();
    }

    /**
     * Returns the line advance.
     */
    public double getLineAdvance()
    {
        return getAscent() + getDescent() + getLeading();
    }

    /**
     * Returns the next run, if available.
     */
    public TextRun getNext()
    {
        int nextIndex = _index + 1;
        return _textLine != null && nextIndex < _textLine.getRunCount() ? _textLine.getRun(nextIndex) : null;
    }

    /**
     * Returns whether this run is equal to the given object.
     */
    @Override
    public boolean equals(Object anObj)
    {
        // Check identity and get other
        if (anObj == this) return true;
        TextRun other = anObj instanceof TextRun ? (TextRun) anObj : null;
        if (other == null) return false;

        // Check Start, Length, Style
        if (other.getStartCharIndex() != getStartCharIndex()) return false;
        if (other.length() != length()) return false;
        if (!other.getStyle().equals(getStyle())) return false;

        // Return equal
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    @Override
    public int hashCode()
    {
        return length();
    }

    /**
     * Returns a basic clone of this object.
     */
    @Override
    public TextRun clone()
    {
        // Do normal version
        TextRun clone;
        try { clone = (TextRun) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Return
        return clone;
    }

    /**
     * Returns a string representation of this run.
     */
    @Override
    public String toString()
    {
        String string = getString();
        string = string.replace("\n", "\\n");
        return getClass().getSimpleName() + "(" + getStartCharIndex() + "," + getEndCharIndex() + "): " + string;
    }
}
