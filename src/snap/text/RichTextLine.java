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
    public RichTextLine(TextBlock aTextBlock)
    {
        super(aTextBlock);
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
}