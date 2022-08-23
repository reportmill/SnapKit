/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.geom.HPos;
import snap.util.ArrayUtils;

/**
 * A class to represent a line of text (for each newline) in RichText.
 */
public class RichTextLine extends BaseTextLine {

    /**
     * Constructor.
     */
    public RichTextLine(RichText aRichText)
    {
        super(aRichText);
        _lineStyle = _text.getDefaultLineStyle();
        addRun(createRun(), 0);
    }

    /**
     * Returns the RichText.
     */
    public RichText getText()  { return (RichText) _text; }

    /**
     * Adds characters with attributes to this line at given index.
     */
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // Get run at index - if empty, go ahead and set style
        RichTextRun run = getRunAt(anIndex);
        if (run.length() == 0 && theStyle != null)
            run.setStyle(theStyle);

        // If style provided and different from current style, get dedicated run
        if (theStyle != null && !theStyle.equals(run.getStyle())) {
            if (anIndex == run.getStart())
                run = addRun(theStyle, run.getIndex());
            else if (anIndex == run.getEnd())
                run = addRun(theStyle, run.getIndex() + 1);
            else {
                run = splitRun(run, anIndex - run.getStart());
                run = addRun(theStyle, run.getIndex());
            }
        }

        // Add chars
        run.insert(theChars);
        _sb.insert(anIndex, theChars);
        updateRuns(run.getIndex());
        updateText();
    }

    /**
     * Removes characters in given range.
     */
    public void removeChars(int aStart, int anEnd)
    {
        // If empty range, just return
        if (anEnd == aStart) return;

        // Iterate over effected runs and remove chars
        int end = anEnd;
        while (aStart < end) {

            // Get run at end
            RichTextRun run = getRunAt(end);
            if (end == run.getStart())
                run = getRun(run.getIndex() - 1);
            int runStart = run.getStart();
            int start = Math.max(aStart, runStart);

            // If range matches run range, just remove it
            if (start == runStart && end == run.getEnd() && getRunCount() > 1) {
                int runIndex = run.getIndex();
                removeRun(runIndex);
                _sb.delete(start, end);
                updateRuns(runIndex - 1);
            }

            // Otherwise delete chars from run
            else {
                run.delete(start - runStart, end - runStart);
                _sb.delete(start, end);
                updateRuns(run.getIndex());
            }

            // Reset end to runStart
            end = runStart;
        }

        updateText();
    }

    /**
     * Returns the number of runs for this line.
     */
    public int getRunCount()  { return _runs.length; }

    /**
     * Returns the individual run at given index.
     */
    public RichTextRun getRun(int anIndex)  { return _runs[anIndex]; }

    /**
     * Returns the line runs.
     */
    public RichTextRun[] getRuns()  { return _runs; }

    /**
     * Creates a new run.
     */
    protected RichTextRun createRun()
    {
        return new RichTextRun(this);
    }

    /**
     * Adds a new run at given index.
     */
    private RichTextRun addRun(TextStyle theStyle, int anIndex)
    {
        RichTextRun run = createRun();
        if (theStyle != null)
            run.setStyle(theStyle);
        addRun(run, anIndex);
        return run;
    }

    /**
     * Adds a run to line.
     */
    private void addRun(RichTextRun aRun, int anIndex)
    {
        _runs = ArrayUtils.add(_runs, aRun, anIndex);
        updateRuns(anIndex - 1);
    }

    /**
     * Removes the run at given index.
     */
    private void removeRun(int anIndex)
    {
        _runs = ArrayUtils.remove(_runs, anIndex);
    }

    /**
     * Returns the last run.
     */
    public RichTextRun getRunLast()
    {
        int runCount = getRunCount();
        return runCount > 0 ? getRun(runCount - 1) : null;
    }

    /**
     * Returns the head run for the line.
     */
    public RichTextRun getRunAt(int anIndex)
    {
        for (RichTextRun run : _runs)
            if (anIndex < run.getEnd())
                return run;
        if (anIndex == length())
            return getRunLast();

        // Complain
        throw new IndexOutOfBoundsException("Index " + anIndex + " beyond " + length());
    }

    /**
     * Splits given run at given index and returns a run containing the remaining characters (and identical attributes).
     */
    protected RichTextRun splitRun(RichTextRun aRun, int anIndex)
    {
        // Clone to get tail and delete chars from each
        RichTextRun remainder = aRun.clone();
        aRun.delete(anIndex, aRun.length());
        remainder.delete(0, anIndex);

        // Add remainder and return
        addRun(remainder, aRun.getIndex() + 1);
        return remainder;
    }

    /**
     * Updates length due to change in given run.
     */
    protected void updateRuns(int aRunIndex)
    {
        // Get BaseRun and Length at end of BaseRun
        RichTextRun baseRun = aRunIndex >= 0 ? getRun(aRunIndex) : null;
        int length = baseRun != null ? baseRun.getEnd() : 0;

        // Iterate over runs beyond BaseRun and update Index, Start and Length
        for (int i = aRunIndex + 1, iMax = getRunCount(); i < iMax; i++) {
            RichTextRun run = getRun(i);
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
     * Sets the style for the line (propogates to runs).
     */
    protected void setStyle(TextStyle aStyle)
    {
        for (RichTextRun run : getRuns())
            run.setStyle(aStyle);
        _width = -1;
    }

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
     * Returns the alignment associated with this paragraph.
     */
    public HPos getAlignX()  { return _lineStyle.getAlign(); }

    /**
     * Sets the alignment associated with this paragraph.
     */
    public void setAlignX(HPos anAlign)
    {
        setLineStyle(getLineStyle().copyFor(anAlign));
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
     * Splits the line at given character index.
     */
    protected RichTextLine splitLineAtIndex(int anIndex)
    {
        RichTextLine remainder = clone();
        remainder.removeChars(0, anIndex);
        removeChars(anIndex, length());
        return remainder;
    }

    /**
     * Appends the given line to the end of this line.
     */
    protected void joinLine(RichTextLine aLine)
    {
        // Add chars
        _sb.append(aLine._sb);

        // Add runs
        for (int i = 0, iMax = aLine.getRunCount(); i < iMax; i++) {
            RichTextRun run = aLine.getRun(i);
            RichTextRun run2 = run.clone();
            run2._textLine = this;
            addRun(run2, getRunCount());
        }
    }

    /**
     * Returns a RichTextLine for given char range.
     */
    public RichTextLine subline(int aStart, int aEnd)
    {
        RichTextLine clone = clone();
        if (aEnd < length()) clone.removeChars(aEnd, length());
        if (aStart > 0) clone.removeChars(0, aStart);
        return clone;
    }

    /**
     * Returns the width of line.
     */
    protected double getWidthImpl()
    {
        double width = 0;
        for (RichTextRun run : _runs)
            width += run.getWidth();
        return width;
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
        for (RichTextRun run : _runs)
            if (anIndex < run.getEnd())
                width += run.getWidth(anIndex - run.getStart());

        // Return
        return width;
    }

    /**
     * Returns whether line contains an underlined run.
     */
    public boolean isUnderlined()
    {
        for (RichTextRun run : _runs)
            if (run.isUnderlined() && run.length() > 0)
                return true;
        return false;
    }

    /**
     * Standard clone implementation.
     */
    public RichTextLine clone()
    {
        // Do normal version
        RichTextLine clone;
        try { clone = (RichTextLine) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Clone StringBuffer, Runs
        clone._sb = new StringBuffer(_sb);
        clone._runs = _runs.clone();
        for (int i = 0; i < _runs.length; i++) {
            RichTextRun runClone = clone._runs[i] = _runs[i].clone();
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
        String str = getString();
        str = str.replace("\n", "\\n");
        return getClass().getSimpleName() + "[" + getIndex() + "](" + getStart() + "," + getEnd() + "): str=\"" + str + "\"";
    }
}