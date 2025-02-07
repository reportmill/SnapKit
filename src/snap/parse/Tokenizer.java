/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.util.ArrayUtils;
import snap.util.CharSequenceUtils;
import java.util.regex.Matcher;
import java.util.stream.Stream;

/**
 * A class to extract tokens from a char sequence.
 */
public class Tokenizer {

    // An array of regexes
    private Regex[] _regexes = new Regex[0];

    // The tokenizer input
    private CharSequence _input;

    // The input length
    private int _length;

    // The current char index
    protected int _charIndex;

    // A map of char to matchers
    private Regex[][] _charMatchers = new Regex[128][];

    // The current token line
    protected TokenLine _tokenLine;

    // The current token doc
    private TokenDoc _tokenDoc;

    // Whether to support standard Java style comments
    private boolean _codeComments;

    // Whether to ignore special tokens
    private boolean _specialTokens;

    // The last token
    private ParseToken _lastToken;

    // Multiline regexes
    private Regex[] _multilineRegexes;

    // TextBlock regexes
    private Regex[] _textBlockRegexes;

    // Constants for common special token names
    public static final String SKIP = "Skip";
    public static final String SINGLE_LINE_COMMENT = "SingleLineComment";
    public static final String MULTI_LINE_COMMENT = "MultiLineComment";
    public static final String TEXT_BLOCK = "TextBlock";
    public static final String TEXT_BLOCK_PATTERN = "\"\"\"";

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
    public void setRegexes(Regex[] theRegexes)
    {
        _regexes = theRegexes;
    }

    /**
     * Sets the regexes for given grammar.
     */
    public void setRegexesForGrammar(Grammar aGrammar)
    {
        // If no Skip rule, add it
        if (!aGrammar.isRuleSetForName(SKIP)) {
            ParseRule skipRule = aGrammar.addRuleForName(SKIP);
            skipRule.setPattern("\\s+");
        }

        // If no SingleLineComment rule, add it
        if (_codeComments && !aGrammar.isRuleSetForName(SINGLE_LINE_COMMENT)) {
            ParseRule singleLineCommentRule = aGrammar.addRuleForName(SINGLE_LINE_COMMENT);
            singleLineCommentRule.setPattern("//.*");
            ParseRule multiLineCommentRule = aGrammar.addRuleForName(MULTI_LINE_COMMENT);
            multiLineCommentRule.setPattern("/*");
            _multilineRegexes = new Regex[2];
            _multilineRegexes[0] = new Regex(MULTI_LINE_COMMENT, "(?s).*?(?=\\*/|\\z)");
            _multilineRegexes[1] = new Regex(MULTI_LINE_COMMENT, "*/");
        }

        // If no TextBlock rule, add it
        if (aGrammar.isRuleSetForName(TEXT_BLOCK)) {
            aGrammar.getRuleForName(TEXT_BLOCK).setPattern(TEXT_BLOCK_PATTERN);
            _textBlockRegexes = new Regex[2];
            _textBlockRegexes[0] = new Regex(TEXT_BLOCK, "(?s).*?(?=\"\"\"|\\z)");
            _textBlockRegexes[1] = new Regex("TextBlockEnd", TEXT_BLOCK_PATTERN);
        }

        Regex[] regexes = aGrammar.getAllRegexes();
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
        _lastToken = null;

        // Reset char index
        setCharIndex(0);

        // Reset token doc, line
        _tokenDoc = null;
        _tokenLine = null;
        getTokenLine();

        // Reset matchers
        for (Regex regex : _regexes)
            regex.getMatcher().reset(_input);
        if (_multilineRegexes != null)
            Stream.of(_multilineRegexes).forEach(regex -> regex.getMatcher().reset(_input));
        if (_textBlockRegexes != null)
            Stream.of(_textBlockRegexes).forEach(regex -> regex.getMatcher().reset(_input));
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
     * Returns whether next char is equal to given char.
     */
    public boolean nextCharEquals(char aChar)
    {
        return  hasChar() && nextChar() == aChar;
    }

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
            if (eatChar == '\r' && nextCharEquals('\n'))
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
     * Returns the last token.
     */
    public ParseToken getLastToken()  { return _lastToken; }

    /**
     * Sets the last token.
     */
    public void setLastToken(ParseToken aToken)
    {
        _lastToken = aToken;
    }

    /**
     * Returns the next token.
     */
    public ParseToken getNextToken()
    {
        ParseToken nextToken = _lastToken = getNextTokenImpl();

        // If next token is special token and those are ignored, get next token
        while (nextToken != null && nextToken.isSpecial() && !_specialTokens)
            nextToken = _lastToken = getNextTokenImpl();

        return nextToken;
    }

    /**
     * Returns the next token.
     */
    protected ParseToken getNextTokenImpl()
    {
        // If no chars, just return
        if (!hasChar())
            return null;

        // Get list of matchers for next char
        char nextChar = nextChar();
        Regex[] regexes = nextChar < 128 ? getRegexesForStartChar(nextChar) : getRegexes();
        Regex match = null;
        int matchEnd = _charIndex;

        // Iterate over regular expressions to find best match
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

        // If no match, return bogus token
        if (match == null) {

            // Get next chars and let tokenizerFailed() decide whether to throw, stop or provide alt token
            String nextChars = getInput().subSequence(_charIndex, Math.min(_charIndex + 30, length())).toString();
            ParseToken nextToken = tokenizerFailed(nextChars);
            return nextToken;
        }

        // Handle skip
        if (match.getName() == SKIP) {
            _charIndex = matchEnd;
            return getNextTokenImpl();
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
        // If LastToken is MultiLineComment, use special regexes
        if (_lastToken != null) {
            String lastTokenName = _lastToken.getName();
            if (lastTokenName == MULTI_LINE_COMMENT && !_lastToken.getPattern().equals("*/"))
                return _multilineRegexes;
            if (lastTokenName == TEXT_BLOCK)
                return _textBlockRegexes;
        }

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
}