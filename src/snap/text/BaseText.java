/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.gfx.Font;
import snap.props.PropObject;
import snap.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the basic text storage class, holding a list of TextLine.
 */
public abstract class BaseText extends PropObject implements CharSequence {

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
     * Adds a block at given index.
     */
    protected void addLine(BaseTextLine aLine, int anIndex)
    {
        _lines.add(anIndex, aLine);
        aLine._text = this;
        updateLines(anIndex - 1);
    }

    /**
     * Removes the block at given index.
     */
    protected BaseTextLine removeLine(int anIndex)
    {
        BaseTextLine line = _lines.remove(anIndex);
        line._text = null;
        updateLines(anIndex - 1);
        return line;
    }

    /**
     * Returns the longest line.
     */
    public BaseTextLine getLineLongest()
    {
        BaseTextLine longLine = null;
        double longW = 0;
        for (BaseTextLine line : _lines) {
            if (line.getWidth() > longW) {
                longLine = line;
                longW = line.getWidth();
            }
        }

        // Return
        return longLine;
    }

    /**
     * Returns the width of text.
     */
    public double getPrefWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Calc, set, return
        BaseTextLine line = getLineLongest();
        double prefW = Math.ceil(line != null ? line._width : 0);
        return _width = prefW;
    }

    /**
     * Returns the width of text from given index.
     */
    public double getPrefWidth(int anIndex)
    {
        // If given char index 0, do cached version
        if (anIndex <= 0) return getPrefWidth();

        // Iterate over lines and get max line width
        double prefW = 0;
        for (BaseTextLine line : _lines) {
            if (anIndex < line.getEnd()) {
                double lineW = line.getWidth(anIndex - line.getStart());
                prefW = Math.max(prefW, lineW);
            }
        }

        // Return
        return prefW;
    }

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
     * Returns the TextRun that contains the given char index.
     */
    public BaseTextRun getRunForCharIndex(int charIndex)
    {
        BaseTextLine line = getLineForCharIndex(charIndex);
        return line.getRunForCharIndex(charIndex - line.getStart());
    }

    /**
     * Returns the last run.
     */
    public BaseTextRun getRunLast()
    {
        BaseTextLine lastLine = getLineLast();
        BaseTextRun lastRun = lastLine != null ? lastLine.getRunLast() : null;
        return lastRun;
    }

    /**
     * Returns the Font for run at given character index.
     */
    public Font getFontForCharIndex(int charIndex)
    {
        BaseTextRun textRun = getRunForCharIndex(charIndex);
        return textRun.getFont();
    }

    /**
     * Returns the TextStyle for the run at the given character index.
     */
    public TextStyle getStyleForCharIndex(int charIndex)
    {
        BaseTextRun textRun = getRunForCharIndex(charIndex);
        return textRun.getStyle();
    }

    /**
     * Returns the TextLineStyle for the run at the given character index.
     */
    public TextLineStyle getLineStyleForCharIndex(int charIndex)
    {
        BaseTextLine textLine = getLineForCharIndex(charIndex);
        return textLine.getLineStyle();
    }

    /**
     * Returns the index of given string.
     */
    public int indexOf(String aStr, int aStart)
    {
        for (BaseTextLine line : getLines()) {
            if (aStart >= line.getEnd()) continue;
            int lineStart = line.getStart();
            int index = line.indexOf(aStr, aStart - lineStart);
            if (index >= 0)
                return index + lineStart;
        }

        // Return not found
        return -1;
    }

    /**
     * Returns index of next newline (or carriage-return/newline) starting at given char index.
     */
    public int indexOfNewline(int aStart)
    {
        return StringUtils.indexOfNewline(this, aStart);
    }

    /**
     * Returns index just beyond next newline (or carriage-return/newline) starting at given char index.
     */
    public int indexAfterNewline(int aStart)
    {
        return StringUtils.indexAfterNewline(this, aStart);
    }

    /**
     * Returns index of the previous newline (or carriage-return/newline) starting at given char index.
     */
    public int lastIndexOfNewline(int aStart)
    {
        return StringUtils.lastIndexOfNewline(this, aStart);
    }

    /**
     * Returns index just beyond previous newline (or carriage-return/newline) starting at given char index.
     */
    public int lastIndexAfterNewline(int aStart)
    {
        return StringUtils.lastIndexAfterNewline(this, aStart);
    }

    /**
     * Returns whether the index in the given char sequence is at a line end.
     */
    public boolean isLineEnd(int anIndex)
    {
        return StringUtils.isLineEnd(this, anIndex);
    }

    /**
     * Returns whether the index in the given char sequence is at just after a line end.
     */
    public boolean isAfterLineEnd(int anIndex)
    {
        return StringUtils.isAfterLineEnd(this, anIndex);
    }

    /**
     * Returns whether a char is a newline char.
     */
    public boolean isLineEndChar(int anIndex)
    {
        return StringUtils.isLineEndChar(this, anIndex);
    }

    /**
     * Returns whether property change is enabled.
     */
    public boolean isPropChangeEnabled()  { return _propChangeEnabled; }

    /**
     * Sets whether property change is enabled.
     */
    public void setPropChangeEnabled(boolean aValue)
    {
        _propChangeEnabled = aValue;
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

    @Override
    public String toStringProps()
    {
        // LineCount, Length
        StringBuilder sb = new StringBuilder();
        sb.append("Length=").append(length());
        sb.append(", LineCount=").append(getLineCount());

        // Add String
        String str = "";
        for (int i = 0, iMax = Math.min(getLineCount(), 5); i < iMax; i++)
            str += "\n" + getLine(i);
        sb.append(str);

        // Return
        return sb.toString();
    }
}
