/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.util.ArrayUtils;
import snap.util.CharSequenceUtils;
import java.util.regex.Matcher;

/**
 * A class to extract tokens from a char sequence.
 */
public class Tokenizer {

    // An array of regexes
    private Regex[] _regexes = new Regex[0];

    // The tokenizer input
    private CharSequence  _input;

    // The input length
    private int  _length;

    // The current char index
    protected int  _charIndex;

    // A map of char to matchers
    private Regex[][]  _charMatchers = new Regex[128][];

    // The current token line
    protected TokenLine _tokenLine;

    // The current token doc
    private TokenDoc _tokenDoc;

    // Whether to support standard Java style comments
    private boolean _codeComments;

    // Whether to ignore special tokens
    private boolean _specialTokens;

    // Constants for common special token names
    public static final String SINGLE_LINE_COMMENT = "SingleLineComment";
    public static final String MULTI_LINE_COMMENT = "MultiLineComment";

    /**
     * Constructor.
     */
    public Tokenizer()
    {
        super();
    }

    /**
     * Returns the array of regexes.
     */
    public Regex[] getRegexes()  { return _regexes; }

    /**
     * Sets the array of regexes.
     */
    public void setRegexes(Regex[] theRegexes)  { _regexes = theRegexes; }

    /**
     * Returns the current tokenizer input.
     */
    public CharSequence getInput()  { return _input; }

    /**
     * Sets the current tokenizer input.
     */
    public void setInput(CharSequence anInput)
    {
        // Set input
        _input = anInput;
        _length = _input.length();

        // Reset char index
        setCharIndex(0);

        // Reset token doc, line
        _tokenDoc = null;
        _tokenLine = null;
        getTokenLine();

        // Reset matchers
        for (Regex regex : _regexes)
            regex.getMatcher().reset(_input);
    }

    /**
     * CharSequence method.
     */
    public final int length()  { return _length; }

    /**
     * CharSequence method.
     */
    public final char charAt(int anIndex)
    {
        return _input.charAt(anIndex);
    }

    /**
     * Returns whether another char is available.
     */
    public final boolean hasChar()
    {
        return _charIndex < length();
    }

    /**
     * Returns whether another given number of chars is available.
     */
    public final boolean hasChars(int aVal)
    {
        return _charIndex + aVal <= length();
    }

    /**
     * Returns the next parse char.
     */
    public final char nextChar()  { return _input.charAt(_charIndex); }

    /**
     * Returns the char at the current index plus offset.
     */
    public final char nextCharAt(int anOffset)  { return _input.charAt(_charIndex + anOffset); }

    /**
     * Returns whether next chars start with given string.
     */
    public boolean nextCharsStartWith(CharSequence startChars)
    {
        // If not enough chars, return false
        int charsLength = startChars.length();
        if (!hasChars(charsLength))
            return false;

        // Iterate over startChars and return false if any don't match nextChars
        for (int charIndex = 0; charIndex < charsLength; charIndex++) {
            if (startChars.charAt(charIndex) != nextCharAt(charIndex))
                return false;
        }

        // Return true
        return true;
    }

    /**
     * Returns the char at the current index plus offset.
     */
    public final char eatChar()
    {
        // Get next char
        char eatChar = _input.charAt(_charIndex++);

        // If newline, look for Windows sister newline char and eat that too and clear token line
        if (eatChar == '\n' || eatChar == '\r') {
            if (eatChar == '\r' && hasChar() && nextChar() == '\n')
                _charIndex++;
            _tokenLine = null;
            getTokenLine();
        }

        // Return
        return eatChar;
    }

    /**
     * Advances charIndex by given char count.
     */
    public void eatChars(int charCount)
    {
        for (int i = 0; i < charCount; i++)
            eatChar();
    }

    /**
     * Eats the chars till line end.
     */
    public void eatCharsTillLineEnd()
    {
        // Eat chars till line end or text end
        while (hasChar() && !CharSequenceUtils.isLineEndChar(nextChar()))
            eatChar();

        // Eat line end
        if (hasChar())
            eatChar();
    }

    /**
     * Returns the current parse char location.
     */
    public final int getCharIndex()  { return _charIndex; }

    /**
     * Sets the current parse char location.
     */
    public void setCharIndex(int aValue)
    {
        _charIndex = aValue;
    }

    /**
     * Returns the next token.
     */
    public ParseToken getNextToken()
    {
        ParseToken nextToken = getNextTokenImpl();

        // If next token is special token and those are ignored, get next token
        while (nextToken != null && nextToken.isSpecial() && !_specialTokens)
            nextToken = getNextTokenImpl();

        return nextToken;
    }

    /**
     * Returns the next token.
     */
    protected ParseToken getNextTokenImpl()
    {
        // Look for special tokens
        ParseToken specialToken = getNextSpecialToken();
        if (specialToken != null)
            return specialToken;

        // Get list of matchers for next char
        char nextChar = hasChar() ? nextChar() : 0;
        Regex[] regexes = nextChar < 128 ? getRegexesForStartChar(nextChar) : getRegexes();

        // Iterate over regular expressions to find best match
        Regex match = null;
        int matchEnd = _charIndex;
        for (Regex regex : regexes) {

            // Get matcher
            Matcher matcher = regex.getMatcher();
            matcher.region(_charIndex, _input.length());

            // Find pattern
            if (matcher.lookingAt()) {
                if (match == null || matcher.end() > matchEnd ||
                        (matcher.end() == matchEnd && regex.getLiteralLength() > match.getLiteralLength())) {
                    match = regex;
                    matchEnd = matcher.end();
                }
            }
        }

        // If no match, return null
        if (match == null) {

            // If no more chars, just return null
            if (!hasChar())
                return null;

            // Get next chars and let tokenizerFailed() decide whether to throw, stop or provide alt token
            String nextChars = getInput().subSequence(_charIndex, Math.min(_charIndex + 30, length())).toString();
            ParseToken nextToken = tokenizerFailed(nextChars);
            return nextToken;
        }

        // Create new token for match
        String matchName = match.getName();
        String matchPattern = match.getPattern();
        ParseToken token = createTokenForProps(matchName, matchPattern, _charIndex, matchEnd);

        // Reset end and return
        _charIndex = matchEnd;
        return token;
    }

    /**
     * Returns list of Regex for a starting char.
     */
    private Regex[] getRegexesForStartChar(char aChar)
    {
        // Get cached regex array for char, just return if found
        Regex[] regexesForChar = _charMatchers[aChar];
        if (regexesForChar != null)
            return regexesForChar;

        // If bogus char, just return
        if (aChar == 0)
            return _charMatchers[aChar] = new Regex[0];

        // Get matching Regexes for char
        String charStr = Character.toString(aChar);
        Regex[] matchingRegexes = ArrayUtils.filter(_regexes, regex -> regex.isMatchingStartChar(aChar, charStr));
        return _charMatchers[aChar] = matchingRegexes;
    }

    /**
     * Returns the current token line.
     */
    public TokenLine getTokenLine()
    {
        if (_tokenLine != null) return _tokenLine;

        // Create new token line
        TokenDoc tokenDoc = getTokenDoc();
        TokenLine lastLine = tokenDoc.getLastLine();
        int startCharIndex = lastLine != null ? lastLine.getEndCharIndex() : 0;
        int endCharIndex = CharSequenceUtils.indexAfterNewlineOrEnd(_input, startCharIndex);
        TokenLine tokenLine = tokenDoc.addLineForCharRange(startCharIndex, endCharIndex);

        // Set and return
        return _tokenLine = tokenLine;
    }

    /**
     * Returns the current token doc.
     */
    public TokenDoc getTokenDoc()
    {
        if (_tokenDoc != null) return _tokenDoc;
        return _tokenDoc = new TokenDoc(_input);
    }

    /**
     * Creates a new token for given properties.
     */
    public ParseToken createTokenForProps(String aName, String aPattern, int aStart, int anEnd)
    {
        ParseTokenImpl token = new ParseTokenImpl();
        token._tokenLine = getTokenLine();
        token._name = aName;
        token._pattern = aPattern;
        token._startCharIndex = aStart;
        token._endCharIndex = anEnd;
        return token;
    }

    /**
     * Gobble input characters until next non-whitespace or input end.
     */
    protected void skipWhiteSpace()
    {
        while (hasChar() && Character.isWhitespace(nextChar()))
            eatChar();
    }

    /**
     * Called when next chars don't conform to any known token pattern.
     * Default implementation just throws ParseExcpetion.
     */
    protected ParseToken tokenizerFailed(String nextChars)
    {
        throw new ParseException("Token not found for: " + nextChars);
    }

    /**
     * Turns on Java style comments.
     */
    public void enableCodeComments()  { _codeComments = true; }

    /**
     * Turns on special tokens.
     */
    public void enableSpecialTokens()  { _specialTokens = true; }

    /**
     * Processes and returns a special token if found.
     * If more than one in a row, returns last one, which points to previous ones.
     */
    private ParseToken getNextSpecialToken()
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
    private ParseToken getNextSpecialTokenImpl()
    {
        // Skip whitespace
        skipWhiteSpace();

        // Look for standard Java single/multi line comments tokens
        if (_codeComments)
            return getCodeCommentToken();

        // Return not found
        return null;
    }

    /**
     * Processes and returns a single line comment token if next up in input.
     */
    protected ParseToken getCodeCommentToken()
    {
        // If next two chars are single line comment, return single line comment token for chars to line end
        if (nextCharsStartWith("//")) {
            int tokenStart = _charIndex;
            eatCharsTillLineEnd();
            return createTokenForProps(SINGLE_LINE_COMMENT, null, tokenStart, _charIndex);
        }

        // If next two chars are multi line comment (/*) prefix, return token
        if (nextCharsStartWith("/*"))
            return getMultiLineCommentTokenMore();

        // Return not found
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
            if (loopChar == '*' && hasChar() && nextChar() == '/') {
                eatChar();
                break;
            }
        }

        // Create and return token
        return createTokenForProps(MULTI_LINE_COMMENT, null, start, _charIndex);
    }
}