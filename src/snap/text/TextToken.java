package snap.text;

/**
 * This class represents a 'word' in a TextLine.
 */
public class TextToken {

    // The TextLine
    private TextLine  _textLine;

    // The start char index in line
    private int  _startCharIndex;

    // The end char index in line
    private int  _endCharIndex;

    // The TextRun
    private TextRun  _textRun;

    // The width
    private double  _width = -1;

    /**
     * Constructor.
     */
    public TextToken(TextLine aTextLine, int startCharIndex, int endCharIndex, TextRun aTextRun)
    {
        _textLine = aTextLine;
        _startCharIndex = startCharIndex;
        _endCharIndex = endCharIndex;
        _textRun = aTextRun;
    }

    /**
     * Returns the TextLine.
     */
    public TextLine getTextLine()  { return _textLine; }

    /**
     * Returns the start char index.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the TextRun.
     */
    public TextRun getTextRun()  { return _textRun; }

    /**
     * Returns the width.
     */
    public double getWidth()
    {
        // If already set, just return
        if (_width >= 0) return _width;

        // Get run info
        TextStyle runStyle = _textRun.getStyle();

        // Iterate over chars
        double tokenW = 0;
        for (int i = _startCharIndex; i < _endCharIndex; i++) {
            char loopChar = _textLine.charAt(i);
            tokenW += runStyle.getCharAdvance(loopChar);
        }

        // If CharSpacing set, add it in
        double charSpacing = runStyle.getCharSpacing();
        if (charSpacing != 0) {
            int length = _endCharIndex - _startCharIndex;
            tokenW += charSpacing * (length - 1);
        }

        // Set, return
        return _width = tokenW;
    }

}
