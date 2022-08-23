/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;

/**
 * This is class represents a range of characters in a TextLine that share the same style.
 */
public class BaseTextRun implements CharSequence, Cloneable {

    // The line that holds this run
    protected BaseTextLine  _textLine;

    // The start char index of this run in line
    protected int  _start;

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
    public BaseTextRun(BaseTextLine aTextLine)
    {
        super();
        _textLine = aTextLine;
    }

    /**
     * Returns the string for this run.
     */
    public String getString()
    {
        CharSequence chars = _textLine.subSequence(_start, _start + _length);
        return chars.toString();
    }

    /**
     * Returns the length in characters for this run.
     */
    public int length()  { return _length; }

    /**
     * CharSequence method returning character at given index.
     */
    public char charAt(int anIndex)
    {
        return _textLine.charAt(_start + anIndex);
    }

    /**
     * CharSequence method return character sequence for range.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        return _textLine.subSequence(_start + aStart, _start + anEnd);
    }

    /**
     * Returns the start character index for this run.
     */
    public int getStart()  { return _start; }

    /**
     * Returns the end character index for this run.
     */
    public int getEnd()  { return _start + length(); }

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
    }

    /**
     * Returns the run index.
     */
    public int getIndex()  { return _index; }

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
     * Insets chars at index.
     */
    protected void insert(CharSequence theChars)
    {
        _length += theChars.length();
        _width = -1;
    }

    /**
     * Deletes chars from index.
     */
    protected void delete(int aStart, int anEnd)
    {
        int charCount = anEnd - aStart;
        _length -= charCount;
        _width = -1;
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
     * Returns whether this run is equal to the given object.
     */
    public boolean equals(Object anObj)
    {
        // Check identity and get other
        if (anObj == this) return true;
        BaseTextRun other = anObj instanceof BaseTextRun ? (BaseTextRun) anObj : null;
        if (other == null) return false;

        // Check Start, Length, Style
        if (other.getStart() != getStart()) return false;
        if (other.length() != length()) return false;
        if (!other.getStyle().equals(getStyle())) return false;

        // Return equal
        return true;
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        return length();
    }

    /**
     * Returns a basic clone of this object.
     */
    public BaseTextRun clone()
    {
        // Do normal version
        BaseTextRun clone;
        try { clone = (BaseTextRun) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Return
        return clone;
    }

    /**
     * Returns a string representation of this run.
     */
    public String toString()
    {
        String string = getString();
        string = string.replace("\n", "\\n");
        return getClass().getSimpleName() + "(" + getStart() + "," + getEnd() + "): " + string;
    }
}
