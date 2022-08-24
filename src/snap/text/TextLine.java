/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.util.ArrayUtils;

/**
 * This class represents a line of text in a Text.
 */
public class TextLine implements CharSequence, Cloneable {

    // The TextDoc that contains this line
    protected TextDoc  _text;

    // The StringBuffer that holds line chars
    protected StringBuffer  _sb = new StringBuffer();

    // The char index of the start of this line in text
    protected int  _start;

    // The run for this line
    protected TextRun[]  _runs = EMPTY_RUNS;

    // The line style
    protected TextLineStyle  _lineStyle;

    // The index of this line in text
    protected int  _index;

    // The width of this line
    protected double _width = -1;

    // Constants
    private static final TextRun[] EMPTY_RUNS = new TextRun[0];

    /**
     * Constructor.
     */
    public TextLine(TextDoc aTextDoc)
    {
        _text = aTextDoc;
        _lineStyle = _text.getDefaultLineStyle();
        addRun(createRun(), 0);
    }

    /**
     * Returns the RichText.
     */
    public TextDoc getText()  { return _text; }

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
     * Returns the start char index of this line in text.
     */
    public int getStart()  { return _start; }

    /**
     * Returns the end char index of this line in text.
     */
    public int getEnd()  { return _start + length(); }

    /**
     * Returns the index of this line in text.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the number of runs for this line.
     */
    public int getRunCount()  { return _runs.length; }

    /**
     * Returns the individual run at given index.
     */
    public TextRun getRun(int anIndex)  { return _runs[anIndex]; }

    /**
     * Returns the line runs.
     */
    public TextRun[] getRuns()  { return _runs; }

    /**
     * Returns the line style.
     */
    public TextLineStyle getLineStyle()  { return _lineStyle; }

    /**
     * Sets the line style.
     */
    public void setLineStyle(TextLineStyle aLineStyle)
    {
        _lineStyle = aLineStyle;
    }

    /**
     * Adds characters with attributes to this line at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // Add length to run
        TextRun run = getRun(0);
        run.addLength(theChars.length());

        // Add chars
        _sb.insert(anIndex, theChars);
        updateText();
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        // Remove length from run
        TextRun run = getRun(0);
        run.addLength(aStart - anEnd);

        // Remove chars
        _sb.delete(aStart, anEnd);
        updateText();
    }

    /**
     * Adds a run to line.
     */
    protected void addRun(TextRun aRun, int anIndex)
    {
        _runs = ArrayUtils.add(_runs, aRun, anIndex);
        updateRuns(anIndex - 1);
    }

    /**
     * Removes the run at given index.
     */
    protected void removeRun(int anIndex)
    {
        _runs = ArrayUtils.remove(_runs, anIndex);
    }

    /**
     * Creates a new run.
     */
    protected TextRun createRun()
    {
        return new TextRun(this);
    }

    /**
     * Sets the style for the line (propagates to runs).
     */
    protected void setStyle(TextStyle aStyle)
    {
        for (TextRun run : getRuns())
            run.setStyle(aStyle);
        _width = -1;
    }

    /**
     * Returns the width of line.
     */
    public double getWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Get from runs
        double width = 0;
        for (TextRun run : _runs)
            width += run.getWidth();

        // Set, return
        return _width = width;
    }

    /**
     * Returns the width of line from given index.
     */
    public double getWidth(int anIndex)
    {
        // If index 0, use cached
        if (anIndex <= 0) return getWidth();

        // Calculate
        double width = 0;
        for (TextRun run : _runs)
            if (anIndex < run.getEnd())
                width += run.getWidth(anIndex - run.getStart());

        // Return
        return width;
    }

    /**
     * Returns the head run for the line.
     */
    public TextRun getRunForCharIndex(int anIndex)
    {
        // Iterate over runs and return run containing char index
        for (TextRun run : _runs)
            if (anIndex <= run.getEnd())
                return run;

        // Complain
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
    }

    /**
     * Returns the last run.
     */
    public TextRun getRunLast()
    {
        int runCount = getRunCount();
        return runCount > 0 ? getRun(runCount - 1) : null;
    }

    /**
     * Splits the line at given character index.
     */
    protected TextLine splitLineAtIndex(int anIndex)
    {
        TextLine remainder = clone();
        remainder.removeChars(0, anIndex);
        removeChars(anIndex, length());
        return remainder;
    }

    /**
     * Appends the given line to the end of this line.
     */
    protected void appendLine(TextLine aLine)
    {
        // Add chars
        _sb.append(aLine._sb);

        // Add runs
        for (int i = 0, iMax = aLine.getRunCount(); i < iMax; i++) {
            TextRun run = aLine.getRun(i);
            TextRun run2 = run.clone();
            run2._textLine = this;
            addRun(run2, getRunCount());
        }
    }

    /**
     * Returns the next line, if available.
     */
    public TextLine getNext()
    {
        int nextIndex = _index + 1;
        return _text != null && nextIndex < _text.getLineCount() ? _text.getLine(nextIndex) : null;
    }

    /**
     * Returns the alignment associated with this line.
     */
    public HPos getAlignX()  { return _lineStyle.getAlign(); }

    /**
     * Sets the alignment associated with this line.
     */
    public void setAlignX(HPos anAlign)
    {
        TextLineStyle lineStyle = getLineStyle().copyFor(anAlign);
        setLineStyle(lineStyle);
    }

    /**
     * Returns whether line contains an underlined run.
     */
    public boolean isUnderlined()
    {
        for (TextRun run : _runs)
            if (run.isUnderlined() && run.length() > 0)
                return true;
        return false;
    }

    /**
     * Returns the last char.
     */
    public char getLastChar()
    {
        int len = length();
        return len > 0 ? charAt(len - 1) : 0;
    }

    /**
     * Returns whether line ends with space.
     */
    public boolean isLastCharWhiteSpace()
    {
        char c = getLastChar();
        return c == ' ' || c == '\t';
    }

    /**
     * Returns whether run ends with newline.
     */
    public boolean isLastCharNewline()
    {
        char c = getLastChar();
        return c == '\r' || c == '\n';
    }

    /**
     * Updates length due to change in given run.
     */
    protected void updateRuns(int aRunIndex)
    {
        // Get BaseRun and Length at end of BaseRun
        TextRun baseRun = aRunIndex >= 0 ? getRun(aRunIndex) : null;
        int length = baseRun != null ? baseRun.getEnd() : 0;

        // Iterate over runs beyond BaseRun and update Index, Start and Length
        for (int i = aRunIndex + 1, iMax = getRunCount(); i < iMax; i++) {
            TextRun run = getRun(i);
            run._index = i;
            run._start = length;
            length += run.length();
        }
    }

    /**
     * Updates text.
     */
    protected void updateText()
    {
        if (_text != null)
            _text.updateLines(getIndex());
        _width = -1;
    }

    /**
     * Returns a copy of this line for given char range.
     */
    public TextLine copyForRange(int aStart, int aEnd)
    {
        // Do normal clone
        TextLine clone = clone();

        // Remove leading/trailing chars
        if (aEnd < length())
            clone.removeChars(aEnd, length());
        if (aStart > 0)
            clone.removeChars(0, aStart);

        // Return
        return clone;
    }

    /**
     * Standard clone implementation.
     */
    public TextLine clone()
    {
        // Do normal version
        TextLine clone;
        try { clone = (TextLine) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Clone StringBuffer, Runs
        clone._sb = new StringBuffer(_sb);
        clone._runs = _runs.clone();
        for (int i = 0; i < _runs.length; i++) {
            TextRun runClone = clone._runs[i] = _runs[i].clone();
            runClone._textLine = clone;
        }

        // Return
        return clone;
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
        StringBuilder sb = new StringBuilder();

        // Add Start, End, Length, Index, String
        sb.append("Start=").append(getStart());
        sb.append(", End=").append(getEnd());
        sb.append(", Length=").append(length());
        sb.append(", Index=").append(getIndex());

        // Append String
        String string = getString().replace("\n", "\\n");
        sb.append(", String=").append(string);

        // Return
        return sb.toString();
    }
}