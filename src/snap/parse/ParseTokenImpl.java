package snap.parse;
import snap.util.CharRange;

/**
 * A basic implementation of a Token.
 */
public class ParseTokenImpl implements ParseToken {

    // The text that provided this token
    protected CharSequence _text;

    // The name this token matches
    protected String _name;

    // The pattern this token matches
    protected String _pattern;

    // The start char index
    protected int _startCharIndex;

    // The end char index
    protected int _endCharIndex;

    // The CharRange line to get LineIndex and ColumnIndex
    private CharRange _charRangeLine;

    // The string
    protected String _string;

    /**
     * Constructor.
     */
    public ParseTokenImpl()
    {
        super();
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the pattern.
     */
    public String getPattern()  { return _pattern; }

    /**
     * Returns the char start.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the char end.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the line index.
     */
    public int getLineIndex()
    {
        CharRange charRangeLine = getCharRangeLine();
        return charRangeLine.getLineIndex();
    }

    /**
     * Returns the column index.
     */
    public int getColumnIndex()
    {
        CharRange charRangeLine = getCharRangeLine();
        return charRangeLine.getColumnIndex();
    }

    /**
     * Returns the CharRangeLine.
     */
    private CharRange getCharRangeLine()
    {
        if (_charRangeLine != null) return _charRangeLine;
        return _charRangeLine = CharRange.getCharRangeLineForCharIndex(_text, _startCharIndex);
    }

    /**
     * Returns the string.
     */
    public String getString()
    {
        if (_string != null) return _string;

        if (_startCharIndex == _endCharIndex)
            return "";

        CharSequence chars = _text.subSequence(_startCharIndex, _endCharIndex);
        return _string = chars.toString();
    }

    /**
     * Returns the string.
     */
    public String toString()
    {
        return "Token { start:" + _startCharIndex + ", end:" + _endCharIndex + " }: " + getString();
    }
}
