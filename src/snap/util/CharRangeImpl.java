package snap.util;

/**
 * This class is a simple CharRange implementation.
 */
class CharRangeImpl implements CharRange {

    // The chars
    private CharSequence _chars;

    // The start char index
    private int _startCharIndex;

    // The end char index
    private int _endCharIndex;

    // The line
    protected CharRange _line;

    // The line
    protected int _lineIndex;

    /**
     * Constructor.
     */
    public CharRangeImpl(CharSequence theChars, int startCharIndex, int endCharIndex)
    {
        _chars = theChars;
        _startCharIndex = startCharIndex;
        _endCharIndex = endCharIndex;
    }

    /**
     * Returns the start char index in source CharLines.
     */
    @Override
    public int getStartCharIndex()  { return _startCharIndex; }

    /**
     * Returns the end char index in source CharLines.
     */
    @Override
    public int getEndCharIndex()  { return _endCharIndex; }

    /**
     * Returns the line (as a CharRange) for this range.
     */
    @Override
    public CharRange getLine()
    {
        if (_line != null) return _line;
        return _line = CharRange.newCharRangeLineForCharsAndCharIndex(_chars, _startCharIndex);
    }

    /**
     * Returns the line index for this range.
     */
    @Override
    public int getLineIndex()
    {
        CharRange line = getLine();
        return line == this ? _lineIndex : line.getLineIndex();
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
