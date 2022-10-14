/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;

/**
 * A class to represent a line of text (for each newline) in RichText.
 */
public class RichTextLine extends TextLine {

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
        TextRun run = getRunForCharIndexAndStyle(anIndex, theStyle);
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
    private TextRun getRunForCharIndexAndStyle(int charIndex, TextStyle aStyle)
    {
        // Get run at index (just return if style is null or equal)
        TextRun run = getRunForCharIndex(charIndex);
        if (aStyle == null || aStyle.equals(run.getStyle()))
            return run;

        // If empty, just set style and return
        if (run.length() == 0) {
            run.setStyle(aStyle);
            return run;
        }

        // If charIndex at run end and next run has same style, return it instead
        if (charIndex == run.getEndCharIndex() && run.getIndex() + 1 < getRunCount()) {
            TextRun nextRun = getRun(run.getIndex() + 1);
            if (aStyle.equals(nextRun.getStyle()))
                return nextRun;
        }

        // Get index to insert new run (need to split run if charIndex in middle)
        int newRunIndex = run.getIndex();
        if (charIndex > run.getStartCharIndex()) {
            newRunIndex++;
            if (charIndex < run.getEndCharIndex())
                splitRunForCharIndex(run, charIndex - run.getStartCharIndex());
        }

        // Create new run for new chars, add and return
        TextRun newRun = createRun();
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
            TextRun run = getRunForCharIndex(end);
            int runStart = run.getStartCharIndex();
            int start = Math.max(aStart, runStart);

            // If range matches run range, just remove it
            if (start == runStart && end == run.getEndCharIndex() && getRunCount() > 1) {
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
     * Splits given run at given char index and returns the run containing the remaining chars (and identical attributes).
     */
    protected TextRun splitRunForCharIndex(TextRun aRun, int anIndex)
    {
        // Clone to get tail and delete chars from each
        TextRun remainder = aRun.clone();
        aRun.addLength(anIndex - aRun.length());
        remainder.addLength(-anIndex);

        // Add remainder and return
        addRun(remainder, aRun.getIndex() + 1);
        return remainder;
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