/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.props.PropChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public abstract class BaseText implements CharSequence {

    // The TextDocLine in this text
    protected List<BaseTextLine> _lines = new ArrayList<>();

    // The length of this text
    protected int  _length;

    // The default text style for this text
    protected TextStyle  _defStyle = TextStyle.DEFAULT;

    // The default line style for this text
    protected TextLineStyle  _defLineStyle = TextLineStyle.DEFAULT;

    // Whether text only allows a single font, color, etc.
    protected boolean  _plainText;

    // Whether property change is enabled
    protected boolean  _propChangeEnabled = true;

    // The width of the rich text
    protected double  _width = -1;

    // The PropChangeSupport
    protected PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String Chars_Prop = "Chars";
    public static final String Style_Prop = "Style";
    public static final String LineStyle_Prop = "LineStyle";

    /**
     * Constructor.
     */
    public BaseText()
    {
        super();
    }

    /**
     * Returns the number of characters in the text.
     */
    public int length()  { return _length; }

    /**
     * Returns the char value at the specified index.
     */
    public char charAt(int anIndex)
    {
        BaseTextLine line = getLineForCharIndex(anIndex);
        return line.charAt(anIndex - line.getStart());
    }
    /**
     * Returns a new char sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int aStart, int anEnd)
    {
        StringBuffer sb = new StringBuffer(anEnd - aStart);
        BaseTextLine line = getLineForCharIndex(aStart);
        while (aStart < anEnd) {
            int end = Math.min(line.getEnd(), anEnd);
            sb.append(line.subSequence(aStart - line.getStart(), end - line.getStart()));
            aStart = end;
            line = line.getNext();
        }
        return sb;
    }

    /**
     * Returns the string for the text.
     */
    public String getString()
    {
        StringBuilder sb = new StringBuilder(length());
        for (BaseTextLine line : _lines)
            sb.append(line._sb);
        return sb.toString();
    }

    /**
     * Returns the default style for text.
     */
    public TextStyle getDefaultStyle()  { return _defStyle; }

    /**
     * Sets the default style.
     */
    public void setDefaultStyle(TextStyle aStyle)
    {
        _defStyle = aStyle;
    }

    /**
     * Returns the default line style for text.
     */
    public TextLineStyle getDefaultLineStyle()  { return _defLineStyle; }

    /**
     * Sets the default line style.
     */
    public void setDefaultLineStyle(TextLineStyle aLineStyle)
    {
        _defLineStyle = aLineStyle;
    }

    /**
     * Returns the number of block in this doc.
     */
    public int getLineCount()  { return _lines.size(); }

    /**
     * Returns the individual block in this doc.
     */
    public BaseTextLine getLine(int anIndex)
    {
        return _lines.get(anIndex);
    }

    /**
     * Returns the list of blocks.
     */
    public List<BaseTextLine> getLines()  { return _lines; }

    /**
     * Returns the block at the given char index.
     */
    public BaseTextLine getLineForCharIndex(int anIndex)
    {
        // Iterate over lines and return line containing char index
        for (BaseTextLine line : _lines)
            if (anIndex < line.getEnd())
                return line;

        // If index of text end, return last
        if (anIndex == length())
            return getLineLast();

        // Complain
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
    }

    /**
     * Returns the last block.
     */
    public BaseTextLine getLineLast()
    {
        int lineCount = getLineCount();
        return lineCount > 0 ? getLine(lineCount - 1) : null;
    }

    /**
     * Updates Lines (Index, Start) from index line to text end.
     */
    protected void updateLines(int anIndex)
    {
        // Get BaseLine and length at end of BaseLine
        BaseTextLine baseLine = anIndex >= 0 ? getLine(anIndex) : null;
        _length = baseLine != null ? baseLine.getEnd() : 0;

        // Iterate over lines beyond BaseLine and update Index, Start, Length and Y
        for (int i = anIndex + 1, iMax = _lines.size(); i < iMax; i++) {
            BaseTextLine line = getLine(i);
            line._index = i;
            line._start = _length;
            _length += line.length();
        }
    }
}
