/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.util.ListUtils;
import snap.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to extract tokens from a char sequence.
 */
public class Tokenizer {

    // The tokenizer input
    private CharSequence  _input;

    // The input length
    private int  _length;

    // Whether to support standard Java style single line comments and multiple line comments
    private boolean  _slc, _mlc;

    // The current char index
    protected int  _charIndex;

    // The current line index and line start char index
    protected int  _lineIndex, _lineStart;

    // The list of regular expression objects
    private List<Regex>  _regexList = new ArrayList<>();

    // An array of regexes
    private Regex[]  _regexes;

    // A map of char to matchers
    private Regex[][]  _charMatchers = new Regex[128][];

    // Constants for common special token names
    public static final String SINGLE_LINE_COMMENT = "SingleLineComment";
    public static final String MULTI_LINE_COMMENT = "MultiLineComment";

    /**
     * Returns the current tokenizer input.
     */
    public CharSequence getInput()
    {
        return _input;
    }

    /**
     * Sets the current tokenizer input.
     */
    public void setInput(CharSequence anInput)
    {
        _input = anInput;
        _length = _input.length();
        _charIndex = _lineIndex = _lineStart = 0;

        // Reset matchers
        for (Regex regex : _regexList)
            regex.getMatcher(_input).reset(_input);
    }

    /**
     * Returns the input subsequence for the given range of characters in input.
     */
    public CharSequence getInput(int aStart, int anEnd)
    {
        return _input.subSequence(aStart, anEnd);
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

        // If newline, look for Windows sister newline char and eat that too
        if (eatChar == '\n' || eatChar == '\r') {
            if (eatChar == '\r' && hasChar() && getChar() == '\n')
                _charIndex++;
            _lineIndex++;
            _lineStart = _charIndex;
        }

        // Return
        return eatChar;
    }

    /**
     * Returns the next given number of chars as a string.
     */
    public final String getChars(int aValue)
    {
        return _input.subSequence(_charIndex, _charIndex + aValue).toString();
    }

    /**
     * Returns the current parse char location.
     */
    public final int getCharIndex()
    {
        return _charIndex;
    }

    /**
     * Sets the current parse char location.
     */
    public void setCharIndex(int aValue)
    {
        _charIndex = aValue;
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
     * Adds a pattern.
     */
    public void addPattern(String aName, String aPattern)
    {
        // Get unique pattern string - if already in list, just return
        String pattern = aPattern.intern();
        for (Regex regex : _regexList)
            if (regex.getPattern() == pattern)
                return;

        // Create and add new regex
        _regexList.add(new Regex(aName, pattern));
        _regexes = null;
    }

    /**
     * Adds a pattern.
     */
    public void addPattern(String aName, String aPattern, boolean isLiteral)
    {
        // Get unique pattern string - if already in list, just return
        String pattern = aPattern.intern();
        for (Regex regex : _regexList)
            if (regex.getPattern() == pattern)
                return;

        // Create and add new regex
        _regexList.add(new Regex(aName, pattern, isLiteral));
        _regexes = null;
    }

    /**
     * Adds patterns to this tokenizer for given rule.
     */
    public void addPatterns(ParseRule aRule)
    {
        addPatterns(aRule, new ArrayList<>(128));
    }

    /**
     * Adds patterns to this tokenizer for given rule.
     */
    private void addPatterns(ParseRule aRule, List<ParseRule> theRules)
    {
        theRules.add(aRule);
        if (aRule.getPattern() != null)
            addPattern(aRule.getName(), aRule.getPattern(), aRule.isLiteral());

        ParseRule r0 = aRule.getChild0();
        if (r0 != null && !ListUtils.containsId(theRules, r0))
            addPatterns(r0, theRules);

        ParseRule r1 = aRule.getChild1();
        if (r1 != null && !ListUtils.containsId(theRules, r1))
            addPatterns(r1, theRules);
    }

    /**
     * Returns the array of regexes (creating it if missing).
     */
    protected Regex[] getRegexes()
    {
        return _regexes != null ? _regexes : (_regexes = _regexList.toArray(new Regex[0]));
    }

    /**
     * Returns the current line index.
     */
    public final int getLineIndex()  { return _lineIndex; }

    /**
     * Returns the current line number.
     */
    public final int getLineNum()  { return _lineIndex + 1; }

    /**
     * Returns the current line start index.
     */
    public final int getLineStart()  { return _lineStart; }

    /**
     * Returns the current column index in the current line.
     */
    public final int getColumnIndex()
    {
        return getCharIndex() - getLineStart();
    }

    /**
     * Returns the current line number.
     */
    public final int getLineNumber()
    {
        return getLineIndex() + 1;
    }

    /**
     * Returns the current column number.
     */
    public final int getColumnNumber()
    {
        return getCharIndex() - getColumnIndex() + 1;
    }

    /**
     * Returns the next token.
     */
    public Token getNextToken()
    {
        // Get next special token
        Token specialToken = getNextSpecialToken();

        // Get list of matchers for next char
        char nextChar = hasChar() ? getChar() : 0;
        Regex[] regexes = nextChar < 128 ? getRegexesForStartChar(nextChar) : getRegexes();

        // Iterate over regular expressions to find best match
        Regex match = null;
        int matchEnd = _charIndex;
        for (Regex regex : regexes) {

            // Get matcher
            Matcher matcher = regex.getMatcher(_input);
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
            if (!hasChar()) return null;
            throw new ParseException("Token not found for: " + getInput(_charIndex, Math.min(_charIndex + 30, length())));
        }

        // Create new token, reset end and return new token
        String matchName = match.getName();
        String matchPattern = match.getPattern();
        Token token = createToken(matchName, matchPattern, _charIndex, matchEnd, specialToken);
        _charIndex = matchEnd;
        return token;
    }

    /**
     * Returns list of Regex for a starting char.
     */
    public Regex[] getRegexesForStartChar(char aChar)
    {
        // Get cached regex array for char, just return if found
        Regex[] regexesForChar = _charMatchers[aChar];
        if (regexesForChar != null)
            return regexesForChar;

        // If bogus char, just return
        if (aChar == 0)
            return _charMatchers[aChar] = new Regex[0];

        // Get Regexes and string for char
        List<Regex> regexList = new ArrayList<>();
        String charStr = Character.toString(aChar);

        // Iterate over all regexes to find those that start with given literal char
        for (Regex regex : getRegexes()) {

            // If regex char matches given char, add to list
            char loopChar = regex.getLiteralChar();
            if (loopChar == aChar)
                regexList.add(regex);

            // Check "char.startsWith(regex)"
            else if (loopChar == 0) {
                Pattern p = regex.getPatternCompiled();
                Matcher m = p.matcher(charStr);
                m.matches();
                if (m.hitEnd())
                    regexList.add(regex);
            }
        }

        // Get, set, return regex array
        return _charMatchers[aChar] = regexList.toArray(new Regex[0]);
    }

    /**
     * Creates a new token.
     */
    protected Token createToken(String aName, String aPattern, int aStart, int anEnd, Token aSpclTkn)
    {
        Token.BasicToken token = new Token.BasicToken();
        token._tokenizer = this;
        token._name = aName;
        token._pattern = aPattern;
        token._startCharIndex = aStart;
        token._endCharIndex = anEnd;
        token._lineIndex = _lineIndex;
        token._lineStartCharIndex = _lineStart;
        token._specialToken = aSpclTkn;
        return token;
    }

    /**
     * Processes and returns next special token.
     */
    public Token getNextSpecialToken()
    {
        // Get next special token
        Token nextSpecialToken = getNextSpecialToken(null);
        Token specialToken = null;

        // Keep getting special tokens until we have the last one
        while (nextSpecialToken != null) {
            specialToken = nextSpecialToken;
            nextSpecialToken = getNextSpecialToken(nextSpecialToken);
        }

        // Return
        return specialToken;
    }

    /**
     * Processes and returns next special token.
     */
    protected Token getNextSpecialToken(Token aSpecialToken)
    {
        // Skip whitespace
        skipWhiteSpace();

        // Look for standard Java single/multi line comments tokens
        if (!(_slc || _mlc))
            return null;
        if (!hasChar() || getChar() != '/')
            return null;

        // Look for SingleLine or DoubleLine comments
        Token specialToken = _slc ? getSingleLineCommentToken(aSpecialToken) : null;
        if (specialToken == null && _mlc)
            specialToken = getMultiLineCommentToken(aSpecialToken);

        // Return special token
        return specialToken;
    }

    /**
     * Returns next special token or token.
     */
    public Token getNextSpecialTokenOrToken()
    {
        Token token = getNextSpecialToken();
        if (token == null)
            token = getNextToken();
        return token;
    }

    /**
     * Gobble input characters until next non-whitespace or input end.
     */
    protected void skipWhiteSpace()
    {
        while (hasChar() && Character.isWhitespace(getChar())) eatChar();
    }

    /**
     * Processes and returns a single line comment token if next up in input.
     */
    protected Token getSingleLineCommentToken(Token aSpecialToken)
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
    public Token getMultiLineCommentToken(Token aSpecialToken)
    {
        // If next two chars are multi line comment (/*) prefix, return token
        if (hasChars(2) && getChar() == '/' && getChar(1) == '*')
            return getMultiLineCommentTokenMore(aSpecialToken);
        return null;
    }

    /**
     * Returns a token from the current char to multi-line comment termination or input end.
     */
    public Token getMultiLineCommentTokenMore(Token aSpecialToken)
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