package snap.parse;

/**
 * A basic implementation of a Token.
 */
public class ParseTokenImpl implements ParseToken {

    // The token line that holds this token
    protected TokenLine _tokenLine;

    // The name this token matches
    protected String _name;

    // The pattern this token matches
    protected String _pattern;

    // The start char index
    protected int _startCharIndex;

    // The end char index
    protected int _endCharIndex;

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
     * Returns the token line that holds this token.
     */
    public TokenLine getTokenLine()  { return _tokenLine; }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the pattern.
     */
    public String getPattern()  { return _pattern; }

    /**
     * Returns the start char index of this token in text.
     */
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index of this token in text.
     */
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the line index.
     */
    public int getLineIndex()  { return _tokenLine.getLineIndex(); }

    /**
     * Returns the start char index of this token in line.
     */
    public int getStartCharIndexInLine()
    {
        int tokenLineStartCharIndex = _tokenLine.getStartCharIndex();
        return _startCharIndex - tokenLineStartCharIndex;
    }

    /**
     * Returns the string.
     */
    public String getString()
    {
        if (_string != null) return _string;

        if (_startCharIndex == _endCharIndex)
            return "";

        CharSequence inputChars = _tokenLine._chars;
        CharSequence tokenChars = inputChars.subSequence(_startCharIndex, _endCharIndex);
        return _string = tokenChars.toString();
    }

    /**
     * Returns the string.
     */
    public String toString()
    {
        return "Token { start:" + _startCharIndex + ", end:" + _endCharIndex + " }: " + getString();
    }
}
