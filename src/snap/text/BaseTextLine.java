/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;

/**
 * This class represents a line of text in a Text.
 */
public abstract class BaseTextLine implements CharSequence, Cloneable {

    // The BaseText that contains this line
    protected BaseText  _text;

    // The StringBuffer that holds line chars
    protected StringBuffer  _sb = new StringBuffer();

    // The run for this line
    protected RichTextRun[]  _runs = EMPTY_RUNS;

    // The line style
    protected TextLineStyle  _lineStyle;

    // The index of this line in text
    protected int  _index;

    // The char index of the start of this line in text
    protected int  _start;

    // The width of this line
    protected double _width = -1;

    // Constants
    private static final RichTextRun[] EMPTY_RUNS = new RichTextRun[0];

    /**
     * Constructor.
     */
    public BaseTextLine(BaseText aBaseText)
    {
        _text = aBaseText;
        //_lineStyle = _text.getDefaultLineStyle();
        //addRun(createRun(), 0);
    }

    /**
     * Returns the RichText.
     */
    public BaseText getText()  { return _text; }

    /**
     * Returns the length of this text line.
     */
    public int length()  { return _sb.length(); }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)  { return _sb.charAt(anIndex); }

    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)  { return _sb.subSequence(aStart, anEnd); }

    /**
     * Returns the index of given string in line.
     */
    public int indexOf(String aStr, int aStart)  { return _sb.indexOf(aStr, aStart); }

    /**
     * Returns the string for the line.
     */
    public String getString()  { return _sb.toString(); }

    /**
     * Returns the index of this line in text.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the start char index of this line in text.
     */
    public int getStart()  { return _start; }

    /**
     * Returns the end char index of this line in text.
     */
    public int getEnd()  { return _start + length(); }

    /**
     * Returns the next line, if available.
     */
    public BaseTextLine getNext()
    {
        return _text != null && _index + 1 < _text.getLineCount() ? _text.getLine(_index + 1) : null;
    }


    /**
     * Returns the width of line.
     */
    public double getWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Get, set, return
        double width = getWidthImpl();
        return _width = width;
    }

    /**
     * Returns the width of line.
     */
    protected abstract double getWidthImpl();
}
