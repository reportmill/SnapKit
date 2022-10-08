package snap.parse;
import snap.util.StringUtils;

/**
 * This Tokenizer subclass supports Java/C single-line and multi-line comments.
 */
public class CodeTokenizer extends Tokenizer {

    // Whether to support standard Java style single line comments and multiple line comments
    private boolean  _slc, _mlc;

    /**
     * Constructor.
     */
    public CodeTokenizer()
    {
        super();
    }

    /**
     * Returns whether tokenizer reads standard Java single line comments.
     */
    public boolean isReadSingleLineComments()
    {
        return _slc;
    }

    /**
     * Sets whether tokenizer reads standard Java single line comments.
     */
    public void setReadSingleLineComments(boolean aValue)
    {
        _slc = aValue;
    }

    /**
     * Returns whether tokenizer reads standard Java multiple line comments.
     */
    public boolean isReadMultiLineComments()
    {
        return _mlc;
    }

    /**
     * Sets whether tokenizer reads standard Java multiple line comments.
     */
    public void setReadMultiLineComments(boolean aValue)
    {
        _mlc = aValue;
    }

    /**
     * Processes and returns next special token.
     */
    protected ParseToken getNextSpecialToken(ParseToken aSpecialToken)
    {
        // Skip whitespace
        skipWhiteSpace();

        // Look for standard Java single/multi line comments tokens
        if (!(_slc || _mlc))
            return null;
        if (!hasChar() || getChar() != '/')
            return null;

        // Look for SingleLine or DoubleLine comments
        ParseToken specialToken = _slc ? getSingleLineCommentToken(aSpecialToken) : null;
        if (specialToken == null && _mlc)
            specialToken = getMultiLineCommentToken(aSpecialToken);

        // Return special token
        return specialToken;
    }

    /**
     * Processes and returns a single line comment token if next up in input.
     */
    protected ParseToken getSingleLineCommentToken(ParseToken aSpecialToken)
    {
        // If next two chars are single line comment (//), return token
        if (hasChars(2) && getChar() == '/' && getChar(1) == '/') {

            // Update CharIndex to line end
            int tokenStart = _charIndex;
            _charIndex += 2;
            _charIndex = StringUtils.indexAfterNewline(getInput(), _charIndex);

            // If no newline in input, set to end
            if (_charIndex < 0)
                _charIndex = length();

                // Otherwise, update LineIndex, LineStart for next line
            else {
                _lineIndex++;
                _lineStart = _charIndex;
            }

            // Create/return new special token
            return createToken(SINGLE_LINE_COMMENT, null, tokenStart, _charIndex, aSpecialToken);
        }

        // Return not found
        return null;
    }

    /**
     * Process and return a multi-line comment if next up in input.
     */
    public ParseToken getMultiLineCommentToken(ParseToken aSpecialToken)
    {
        // If next two chars are multi line comment (/*) prefix, return token
        if (hasChars(2) && getChar() == '/' && getChar(1) == '*')
            return getMultiLineCommentTokenMore(aSpecialToken);
        return null;
    }

    /**
     * Returns a token from the current char to multi-line comment termination or input end.
     */
    public ParseToken getMultiLineCommentTokenMore(ParseToken aSpecialToken)
    {
        // Mark start of MultiLineComment token (just return null if at input end)
        int start = _charIndex;
        if (start == length())
            return null;

        // Gobble chars until multi-line comment termination or input end
        while (hasChar()) {
            char loopChar = eatChar();
            if (loopChar == '*' && hasChar() && getChar() == '/') {
                eatChar();
                break;
            }
        }

        // Create and return token
        return createToken(MULTI_LINE_COMMENT, null, start, _charIndex, aSpecialToken);
    }
}
