package snap.util;

/**
 * This class provides fundamental functionality for parsing text characters.
 */
public class ParseText {

    // The input
    private CharSequence _input;

    // The char index
    private int _charIndex;

    /**
     * Constructor.
     */
    public ParseText(CharSequence theChars)
    {
        _input = theChars;
    }

    /**
     * Returns the char at given char index.
     */
    public char charAt(int charIndex)  { return _input.charAt(charIndex); }

    /**
     * Returns whether there are more chars.
     */
    public boolean hasChars()  { return _charIndex < _input.length(); }

    /**
     * Returns the next char.
     */
    public char nextChar()  { return _input.charAt(_charIndex); }

    /**
     * Advances charIndex by one.
     */
    public void eatChar()  { _charIndex++; }

    /**
     * Advances charIndex by given char count.
     */
    public void eatChars(int charCount)  { _charIndex += charCount; }

    /**
     * Eats the line end char.
     */
    public void eatLineEnd()
    {
        if (nextChar() == '\r')
            eatChar();
        if (hasChars() && nextChar() == '\n')
            eatChar();
    }

    /**
     * Returns whether next chars start with given string.
     */
    public boolean nextCharsStartWith(CharSequence startChars)
    {
        // If not enough chars, return false
        int charsLeft = _input.length() - _charIndex;
        if (charsLeft < startChars.length())
            return false;

        // Iterate over startChars and return false if any don't match nextChars
        for (int charIndex = 0; charIndex < startChars.length(); charIndex++) {
            if (startChars.charAt(charIndex) != _input.charAt(_charIndex + charIndex))
                return false;
        }

        // Return true
        return true;
    }

    /**
     * Skips whitespace.
     */
    public void skipWhiteSpace()
    {
        while (_charIndex < _input.length() && Character.isWhitespace(nextChar()))
            _charIndex++;
    }

    /**
     * Returns the chars till line end.
     */
    public CharSequence getCharsTillLineEnd()
    {
        // Get startCharIndex and eatChars till line end or text end
        int startCharIndex = _charIndex;
        while (hasChars() && !CharSequenceUtils.isLineEndChar(nextChar()))
            eatChar();

        // Get endCharIndex and eatLineEnd
        int endCharIndex = _charIndex;
        if (hasChars())
            eatLineEnd();

        // Return chars
        return getCharsForCharRange(startCharIndex, endCharIndex);
    }

    /**
     * Returns the chars till matching terminator.
     */
    public CharSequence getCharsTillMatchingTerminator(CharSequence endChars)
    {
        // If leading newline, just skip it
        if (hasChars() && CharSequenceUtils.isLineEndChar(nextChar()))
            eatLineEnd();

        // Get startCharIndex and eatChars till matching chars or text end
        int startCharIndex = _charIndex;
        while (hasChars() && !nextCharsStartWith(endChars))
            eatChar();

        // Get endCharIndex and eatChars for matching chars
        int endCharIndex = _charIndex;
        if (CharSequenceUtils.isLineEndChar(charAt(endCharIndex - 1)))
            endCharIndex--;

        // Get endCharIndex and eatChars for matching chars
        if (hasChars())
            eatChars(endChars.length());

        // Return chars
        return getCharsForCharRange(startCharIndex, endCharIndex);
    }

    /**
     * Returns chars for char range.
     */
    public CharSequence getCharsForCharRange(int startCharIndex, int endCharIndex)
    {
        return _input.subSequence(startCharIndex, endCharIndex);
    }

    /**
     * Returns the length of leading whitespace chars for given char sequence.
     */
    public boolean isAtEmptyLine()
    {
        // Get leading space chars
        for (int i = _charIndex; i < _input.length(); i++) {
            char loopChar = _input.charAt(i);
            if (!Character.isWhitespace(loopChar))
                return false;
            if (loopChar == '\n')
                break;
        }

        // Return
        return true;
    }
}
