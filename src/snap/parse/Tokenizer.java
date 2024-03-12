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
     * Sets regexes for pattern rules in given rule.
     */
    public void setRegexesForPatternRulesInRule(ParseRule aRule)
    {
        ParseRule[] patternRules = ParseUtils.getPatternRulesForRule(aRule);
        Regex[] regexes = ArrayUtils.map(patternRules, rule -> new Regex(rule.getName(), rule.getPattern()), Regex.class);
        setRegexes(regexes);
    }

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
     * Returns the current parse char.
     */
    public final char getChar()
    {
        return _input.charAt(_charIndex);
    }

    /**
     * Returns the char at the current index plus offset.
     */
    public final char getChar(int anOffset)
    {
        return _input.charAt(_charIndex + anOffset);
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
            if (eatChar == '\r' && hasChar() && getChar() == '\n')
                _charIndex++;
            _tokenLine = null;
            getTokenLine();
        }

        // Return
        return eatChar;
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
        // Get next special token
        ParseToken specialToken = getNextSpecialToken();
        if (specialToken != null) {
            TokenLine tokenLine = getTokenLine();
            tokenLine.addSpecialToken(specialToken);
        }

        // Get list of matchers for next char
        char nextChar = hasChar() ? getChar() : 0;
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
     * Processes and returns a special token if found.
     * This version just skips whitespace.
     */
    public ParseToken getNextSpecialToken()
    {
        skipWhiteSpace();
        return null;
    }

    /**
     * Gobble input characters until next non-whitespace or input end.
     */
    protected void skipWhiteSpace()
    {
        while (hasChar() && Character.isWhitespace(getChar()))
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
}