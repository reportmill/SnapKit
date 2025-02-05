package snap.parse;
import snap.util.CharSequenceX;

/**
 * This class represents a line
 */
public class TokenLine implements CharSequenceX {

    // The token doc
    private TokenDoc _tokenDoc;

    // The chars
    protected CharSequence _chars;

    // The start char index
    private int _startCharIndex;

    // The end char index
    private int _endCharIndex;

    // The line index
    protected int _lineIndex;

    /**
     * Constructor.
     */
    public TokenLine(TokenDoc tokenDoc, int startCharIndex, int endCharIndex, int lineIndex)
    {
        _tokenDoc = tokenDoc;
        _chars = tokenDoc._chars;
        _startCharIndex = startCharIndex;
        _endCharIndex = endCharIndex;
        _lineIndex = lineIndex;
    }

    /**
     * Returns the start char index in source CharLines.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index in source CharLines.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the line index for this line.
     */
    public int getLineIndex()  { return _lineIndex + _tokenDoc._startLineIndex; }

    /**
     * Returns the next line.
     */
    public TokenLine getNext()
    {
        int nextIndex = _lineIndex + 1;
        return nextIndex < _tokenDoc.getLineCount() ? _tokenDoc.getLine(nextIndex) : null;
    }

    /**
     * Returns the previous line.
     */
    public TokenLine getPrevious()
    {
        int prevIndex = _lineIndex - 1;
        return prevIndex >= 0 ? _tokenDoc.getLine(prevIndex) : null;
    }

    /**
     * CharSequence method.
     */
    @Override
    public int length()  { return _endCharIndex - _startCharIndex; }

    /**
     * CharSequence method.
     */
    @Override
    public char charAt(int charIndex)
    {
        int startCharIndex = getStartCharIndex();
        int charIndexInChars = startCharIndex + charIndex;
        return _chars.charAt(charIndexInChars);
    }

    /**
     * CharSequence method.
     */
    @Override
    public CharSequence subSequence(int startCharIndex, int endCharIndex)
    {
        int startCharIndexInRange = getStartCharIndex();
        int startCharIndexInChars = startCharIndexInRange + startCharIndex;
        int endCharIndexInChars = startCharIndexInRange + endCharIndex;
        return _chars.subSequence(startCharIndexInChars, endCharIndexInChars);
    }
}
