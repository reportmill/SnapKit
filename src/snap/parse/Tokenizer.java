/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.util.ListUtils;
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
     * Constructor.
     */
    public Tokenizer()
    {
        super();
    }

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
            regex.getMatcher().reset(_input);
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
        _lineStart = aValue;
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
        Regex regex = new Regex(aName, pattern);
        _regexList.add(regex);
        _regexes = null;
    }

    /**
     * Adds patterns to this tokenizer for given rule.
     */
    public void addPatternsForRule(ParseRule aRule)
    {
        addPatternsForRule(aRule, new ArrayList<>(128));
    }

    /**
     * Adds patterns to this tokenizer for given rule.
     */
    private void addPatternsForRule(ParseRule aRule, List<ParseRule> theRules)
    {
        theRules.add(aRule);
        if (aRule.getPattern() != null)
            addPattern(aRule.getName(), aRule.getPattern());

        ParseRule r0 = aRule.getChild0();
        if (r0 != null && !ListUtils.containsId(theRules, r0))
            addPatternsForRule(r0, theRules);

        ParseRule r1 = aRule.getChild1();
        if (r1 != null && !ListUtils.containsId(theRules, r1))
            addPatternsForRule(r1, theRules);
    }

    /**
     * Returns the array of regexes (creating it if missing).
     */
    protected Regex[] getRegexes()
    {
        if (_regexes != null) return _regexes;
        return _regexes = _regexList.toArray(new Regex[0]);
    }

    /**
     * Returns the current line index.
     */
    public final int getLineIndex()  { return _lineIndex; }

    /**
     * Returns the current line index.
     */
    public final void setLineIndex(int aValue)  { _lineIndex = aValue; }

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
     * Returns the next token.
     */
    public ParseToken getNextToken()
    {
        // Get next special token
        ParseToken specialToken = getNextSpecialToken();

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
            String nextChars = getInput(_charIndex, Math.min(_charIndex + 30, length())).toString();
            ParseToken nextToken = tokenizerFailed(nextChars);
            return nextToken;
        }

        // Create new token for match
        String matchName = match.getName();
        String matchPattern = match.getPattern();
        ParseToken token = createToken(matchName, matchPattern, _charIndex, matchEnd, specialToken);

        // Reset end and return
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
    protected ParseToken createToken(String aName, String aPattern, int aStart, int anEnd, ParseToken aSpecialToken)
    {
        ParseToken.BasicToken token = new ParseToken.BasicToken();
        token._text = _input;
        token._name = aName;
        token._pattern = aPattern;
        token._startCharIndex = aStart;
        token._endCharIndex = anEnd;
        token._lineIndex = _lineIndex;
        token._startCharIndexInLine = _lineStart;
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