package snap.text;

/**
 * This class iterates over TextBlock TextRuns.
 */
public class TextRunIter {

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
        _startCharIndex = startCharIndex;
        _endCharIndex = endCharIndex;
        _trimEndRuns = trimEndRuns;

        // Get starting line
        _textLine = textBlock.getLineForCharIndex(startCharIndex);

        // Get/set starting next run
        int startCharIndexInLine = startCharIndex - _textLine.getStartCharIndex();
        _nextRun = _textLine.getRunForCharIndex(startCharIndexInLine);
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
     * Trims a run if starts before StartCharIndex or ends before EndCharIndex.
     */
    private TextRun trimRunIfNeeded(TextRun textRun)
    {
        // If run starts before StartCharIndex or ends before EndCharIndex, return copy for sub range
        int startCharIndexInLine = _startCharIndex - textRun.getLine().getStartCharIndex();
        int endCharIndexInLine = _endCharIndex - textRun.getLine().getStartCharIndex();
        if (startCharIndexInLine > textRun.getStartCharIndex() || endCharIndexInLine < textRun.getEndCharIndex()) {
            int newRunStartCharIndex = Math.max(startCharIndexInLine, textRun.getStartCharIndex());
            int newRunEndStartCharIndex = Math.min(endCharIndexInLine, textRun.getEndCharIndex());
            return textRun.copyForRange(newRunStartCharIndex, newRunEndStartCharIndex);
        }

        // Return
        return textRun;
    }
}
