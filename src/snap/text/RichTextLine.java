/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;

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
    }

    /**
     * Adds characters with attributes to this line at given index.
     */
    @Override
    public void addChars(CharSequence theChars, TextStyle theStyle, int anIndex)
    {
        // Get run at index for style and add length
        BaseTextRun run = getRunForCharIndexAndStyle(anIndex, theStyle);
        run.addLength(theChars.length());

        // Add chars
        _sb.insert(anIndex, theChars);
        updateRuns(run.getIndex());
        updateText();
    }

    /**
     * Returns the run to add chars to for given style and char index.
     * Will try to use any adjacent run with conforming style, otherwise, will create/add new.
     */
    private BaseTextRun getRunForCharIndexAndStyle(int charIndex, TextStyle aStyle)
    {
        // Get run at index (just return if style is null or equal)
        BaseTextRun run = getRunForCharIndex(charIndex);
        if (aStyle == null || aStyle.equals(run.getStyle()))
            return run;

        // If empty, just set style and return
        if (run.length() == 0) {
            run.setStyle(aStyle);
            return run;
        }

        // If charIndex at run end and next run has same style, return it instead
        if (charIndex == run.getEnd() && run.getIndex() + 1 < getRunCount()) {
            BaseTextRun nextRun = getRun(run.getIndex() + 1);
            if (aStyle.equals(nextRun.getStyle()))
                return nextRun;
        }

        // Get index to insert new run (need to split run if charIndex in middle)
        int newRunIndex = run.getIndex();
        if (charIndex > run.getStart()) {
            newRunIndex++;
            if (charIndex < run.getEnd())
                splitRunForCharIndex(run, charIndex - run.getStart());
        }

        // Create new run for new chars, add and return
        BaseTextRun newRun = createRun();
        newRun.setStyle(aStyle);
        addRun(newRun, newRunIndex);
        return newRun;
    }

    /**
     * Removes characters in given range.
     */
    @Override
    public void removeChars(int aStart, int anEnd)
    {
        // If empty range, just return
        if (anEnd == aStart) return;

        // Iterate over effected runs and remove chars
        int end = anEnd;
        while (aStart < end) {

            // Get run at end
            BaseTextRun run = getRunForCharIndex(end);
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
                run.addLength(start - end);
                _sb.delete(start, end);
                updateRuns(run.getIndex());
            }

            // Reset end to runStart
            end = runStart;
        }

        updateText();
    }

    /**
     * Sets the style for the line (propagates to runs).
     */
    protected void setStyle(TextStyle aStyle)
    {
        for (BaseTextRun run : getRuns())
            run.setStyle(aStyle);
        _width = -1;
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
    protected void appendLine(RichTextLine aLine)
    {
        // Add chars
        _sb.append(aLine._sb);

        // Add runs
        for (int i = 0, iMax = aLine.getRunCount(); i < iMax; i++) {
            BaseTextRun run = aLine.getRun(i);
            BaseTextRun run2 = run.clone();
            run2._textLine = this;
            addRun(run2, getRunCount());
        }
    }

    /**
     * Splits given run at given char index and returns the run containing the remaining chars (and identical attributes).
     */
    protected BaseTextRun splitRunForCharIndex(BaseTextRun aRun, int anIndex)
    {
        // Clone to get tail and delete chars from each
        BaseTextRun remainder = aRun.clone();
        aRun.addLength(anIndex - aRun.length());
        remainder.addLength(-anIndex);

        // Add remainder and return
        addRun(remainder, aRun.getIndex() + 1);
        return remainder;
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
        for (BaseTextRun run : _runs)
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
        for (BaseTextRun run : _runs)
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
        for (BaseTextRun run : _runs)
            if (run.isUnderlined() && run.length() > 0)
                return true;
        return false;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public RichTextLine clone()
    {
        return (RichTextLine) super.clone();
    }
}