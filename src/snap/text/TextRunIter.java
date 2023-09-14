/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;

/**
 * This class iterates over TextBlock TextRuns.
 */
public class TextRunIter {

    // The TextBlock
    private TextBlock _textBlock;

    // The start char index
    private int _startCharIndex;

    // The end char index
    private int _endCharIndex;

    // Whether to trim end runs
    private boolean _trimEndRuns;

    // The current text line
    private TextLine _textLine;

    // The current text run
    private TextRun _textRun;

    // The next text run
    private TextRun _nextRun;

    /**
     * Constructor.
     */
    public TextRunIter(TextBlock textBlock, int startCharIndex, int endCharIndex, boolean trimEndRuns)
    {
        _textBlock = textBlock;
        _startCharIndex = startCharIndex;
        _endCharIndex = endCharIndex;
        _trimEndRuns = trimEndRuns;

        // Get starting line
        _textLine = textBlock.getLineForCharIndex(startCharIndex);

        // Get/set starting next run
        int startCharIndexInLine = startCharIndex - _textLine.getStartCharIndex();
        _nextRun = _textLine.getRunForCharRange(startCharIndexInLine, _textLine.length());
    }

    /**
     * Returns whether there is another run.
     */
    public boolean hasNextRun()
    {
        // Get next run
        if (_nextRun == null)
            _nextRun = getNextRunImpl();

        // If next run starts after end char index, clear it
        if (_nextRun != null) {
            int runStartCharIndexInText = _nextRun.getStartCharIndex() + _nextRun.getLine().getStartCharIndex();
            if (_endCharIndex <= runStartCharIndexInText)
                return false;
        }

        // Return whether next run is set
        return _nextRun != null;
    }

    /**
     * Returns the next run.
     */
    public TextRun getNextRun()
    {
        // Set next run
        _textRun = _nextRun;
        _nextRun = null;

        // If TrimEndRuns is set, trim run if needed
        if (_textRun != null && _trimEndRuns)
            _textRun = trimRunIfNeeded(_textRun);

        // Return
        return _textRun;
    }

    /**
     * Returns the next run.
     */
    private TextRun getNextRunImpl()
    {
        // If current text run has next, return it
        if (_textRun != null) {
            TextRun nextRun = _textRun.getNext();
            if (nextRun != null)
                return nextRun;
        }

        // If still have lines, get/set next line and return first run
        if (_textLine != null) {
            _textLine = _textLine.getNext();
            if (_textLine != null)
                return _textLine.getRun(0);
        }

        // Return no next run
        return null;
    }

    /**
     * Returns the current line.
     */
    public TextLine getLine()  { return _textLine; }

    /**
     * Splits the end runs.
     */
    public void splitEndRuns()
    {
        // If start run starts before start char index, split at start char index
        TextRun startRun = _nextRun;
        int startIndexInLine = _startCharIndex - _textLine.getStartCharIndex();
        if (startIndexInLine > startRun.getStartCharIndex()) {
            int newRunStartInLine = startIndexInLine - startRun.getStartCharIndex();
            _nextRun = _textLine.splitRunForCharIndex(startRun, newRunStartInLine);
        }

        // If end run ends after end char index, split at end char index
        TextRun endRun = _textBlock.getRunForCharIndex(_endCharIndex);
        TextLine endLine = endRun.getLine();
        int endIndexInLine = _endCharIndex - endLine.getStartCharIndex();
        if (endIndexInLine < endRun.getEndCharIndex()) {
            int newRunEndInLine = endIndexInLine - endRun.getStartCharIndex();
            endLine.splitRunForCharIndex(endRun, newRunEndInLine);
        }
    }

    /**
     * Trims a run if starts before StartCharIndex or ends before EndCharIndex.
     */
    private TextRun trimRunIfNeeded(TextRun textRun)
    {
        // If run starts before StartCharIndex or ends before EndCharIndex, return copy for sub range
        TextLine textLine = textRun.getLine();
        int lineStartCharIndex = textLine.getStartCharIndex();
        int startCharIndexInLine = _startCharIndex - lineStartCharIndex;
        int endCharIndexInLine = _endCharIndex - lineStartCharIndex;

        // If run starts before StartCharIndex or ends before EndCharIndex, return copy for sub range
        if (startCharIndexInLine > textRun.getStartCharIndex() || endCharIndexInLine < textRun.getEndCharIndex()) {
            int startCharOffset = Math.max(startCharIndexInLine - textRun.getStartCharIndex(), 0);
            int endCharOffset = textRun.length() - Math.max(textRun.getEndCharIndex() - endCharIndexInLine, 0);
            return textRun.copyForRange(startCharOffset, endCharOffset);
        }

        // Return
        return textRun;
    }
}
