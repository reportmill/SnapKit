package snap.parse;
import snap.util.CharSequenceUtils;

/**
 * This Tokenizer subclass supports Java/C single-line and multi-line comments.
 */
public class CodeTokenizer extends Tokenizer {

    // Whether to support standard Java style single line comments
    private boolean  _slc;

    // Whether to support standard Java style multi line comments
    private boolean  _mlc;

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
     * Returns next special token or token.
     */
    public ParseToken getNextSpecialTokenOrToken()
    {
        ParseToken token = getNextSpecialToken();
        if (token == null)
            token = getNextToken();
        return token;
    }

    /**
     * Processes and returns a special token if found.
     * If more than one in a row, returns last one, which points to previous ones.
     */
    @Override
    public ParseToken getNextSpecialToken()
    {
        // Get next special token - just return null if not found
        ParseToken specialToken = getNextSpecialTokenImpl();
        if (specialToken == null)
            return null;

        // Keep getting special tokens until we have the last one
        while (true) {

            // Look for another special token - just stop if not found
            ParseToken nextSpecialToken = getNextSpecialTokenImpl();
            if (nextSpecialToken == null)
                break;

            // Set NextSpecialToken.SpecialToken to current loop token
            //nextSpecialToken._specialToken = specialToken;
            specialToken = nextSpecialToken;
        }

        // Return
        return specialToken;
    }

    /**
     * Processes and returns next special token.
     */
    protected ParseToken getNextSpecialTokenImpl()
    {
        // Skip whitespace
        skipWhiteSpace();

        // Look for standard Java single/multi line comments tokens
        if (!(_slc || _mlc))
            return null;
        if (!hasChar() || getChar() != '/')
            return null;

        // Look for SingleLine or DoubleLine comments
        ParseToken specialToken = _slc ? getSingleLineCommentToken() : null;
        if (specialToken == null && _mlc)
            specialToken = getMultiLineCommentToken();

        // Return special token
        return specialToken;
    }

    /**
     * Processes and returns a single line comment token if next up in input.
     */
    protected ParseToken getSingleLineCommentToken()
    {
        // If next two chars are single line comment (//), return token
        if (hasChars(2) && getChar() == '/' && getChar(1) == '/') {

            // Update CharIndex to line end
            int tokenStart = _charIndex;
            _charIndex += 2;
            _charIndex = CharSequenceUtils.indexAfterNewline(getInput(), _charIndex);

            // If no newline in input, set to end
            if (_charIndex < 0)
                _charIndex = length();

                // Otherwise, update LineIndex, LineStart for next line
            else {
                _lineIndex++;
                _lineStart = _charIndex;
            }

            // Create/return new special token
            return createToken(SINGLE_LINE_COMMENT, null, tokenStart, _charIndex, null);
        }

        // Return not found
        return null;
    }

    /**
     * Process and return a multi-line comment if next up in input.
     */
    public ParseToken getMultiLineCommentToken()
    {
        // If next two chars are multi line comment (/*) prefix, return token
        if (hasChars(2) && getChar() == '/' && getChar(1) == '*')
            return getMultiLineCommentTokenMore();
        return null;
    }

    /**
     * Returns a token from the current char to multi-line comment termination or input end.
     */
    public ParseToken getMultiLineCommentTokenMore()
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
        return createToken(MULTI_LINE_COMMENT, null, start, _charIndex, null);
    }
}
