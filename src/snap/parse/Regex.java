/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A class for building regular expressions like a string buffer.
 */
public class Regex {

    // The name of this regex, if applicable
    private String  _name;

    // The pattern of this regex
    private String  _pattern;

    // Returns whether the pattern is literal
    private boolean  _literal;

    // The compiled pattern
    private Pattern  _patternCompiled;

    // A shared matcher
    private Matcher  _matcher;

    // The literal length of the pattern
    private int  _literalLength = -1;

    /**
     * Constructor with given pattern and name.
     */
    public Regex(String aName, String aPattern)
    {
        _name = aName != null ? aName.intern() : null;
        _pattern = aPattern.intern();
        _literal = _pattern.length() < 3 || _pattern.indexOf('[') < 0;
    }

    /**
     * Constructor with given pattern and name.
     */
    public Regex(String aName, String aPattern, boolean isLiteral)
    {
        _name = aName != null ? aName.intern() : null;
        _pattern = aPattern.intern();
        _literal = isLiteral;
    }

    /**
     * Returns the regex name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the regex pattern.
     */
    public String getPattern()  { return _pattern; }

    /**
     * Returns whether pattern is literal.
     */
    public boolean isLiteral()  { return _literal; }

    /**
     * Returns the compiled pattern.
     */
    public Pattern getPatternCompiled()
    {
        // If already set, just return
        if (_patternCompiled != null) return _patternCompiled;

        // Get, set
        String pattern = getPattern();
        int compileFlags = isLiteral() ? Pattern.LITERAL : 0;
        try { _patternCompiled = Pattern.compile(pattern, compileFlags); }
        catch (PatternSyntaxException e) { throw e; }

        // Return
        return _patternCompiled;
    }

    /**
     * Returns the shared matcher.
     */
    public Matcher getMatcher()
    {
        // If already set, just return
        if (_matcher != null) return _matcher;

        // Get, set, return
        Pattern pattern = getPatternCompiled();
        return _matcher = pattern.matcher("");
    }

    /**
     * Returns the literal length of this regex (the number of literal chars in the prefix).
     */
    public int getLiteralLength()
    {
        // If already set, just return
        if (_literalLength >= 0) return _literalLength;

        // Get, set, return
        _literalLength = isLiteral() ? getPattern().length() : getLiteralLength(getPattern());
        return _literalLength;
    }

    /**
     * Returns the first literal in regex pattern (or 0 if not literal).
     */
    public char getLiteralChar()
    {
        return isLiteral() ? getPattern().charAt(0) : getLiteralChar(getPattern());
    }

    /**
     * Returns whether given regex matches given start char.
     */
    public boolean isMatchingStartChar(char aChar, String charStr)
    {
        // If regex char matches given char, return true
        char loopChar = getLiteralChar();
        if (loopChar == aChar)
            return true;

        // If "char.startsWith(regex)", return true
        if (loopChar == 0) {
            Pattern pattern = getPatternCompiled();
            Matcher matcher = pattern.matcher(charStr);
            matcher.matches();
            if (matcher.hitEnd())
                return true;
        }

        // Return not matching
        return false;
    }

    /**
     * Returns a string representation of regex.
     */
    public String toString()
    {
        return "Regex { Name: " + _name + ", Pattern: \"" + _pattern + "\" }";
    }

    /**
     * Returns the literal length of pattern.
     */
    private static int getLiteralLength(String aPattern)
    {
        int literalLength = 0;

        // Iterate over chars
        for (int i = 0, iMax = aPattern.length(); i < iMax; i++) {
            char c = aPattern.charAt(i);
            if (isSpecialChar(c) && (i > 0 || (c != ']' && c != '}' && c != '-' && c != '&'))) {
                if (isAnchorChar(c))
                    continue;
                else if (c == '\\') {
                    literalLength++;
                    i++;
                }
                else break;
            }
            else literalLength++;
        }

        // Return
        return literalLength;
    }

    /**
     * Utility method to return whether char is an anchor char.
     */
    private static boolean isAnchorChar(char c)  { return c == '^' || c == '$'; }

    /**
     * Returns the first char of a pattern, if it's a literal char.
     */
    private static char getLiteralChar(String aPattern)
    {
        char c = aPattern.charAt(0);
        if (!isSpecialChar(c) || (c == ']' || c == '}' || c == '-' || c == '&'))
            return c;
        if (c == '\\')
            return aPattern.charAt(1);
        return 0;
    }

    /**
     * Utility method to return whether given character is a special char.
     */
    private static boolean isSpecialChar(char c)
    {
        // Handle special chars: '.', '-'
        if (c == '.' || c == '-')
            return true;

        // Handle escape chars: \, \t, \n, \r, \f (form-feed), \a (alert/bell), \e (escape)
        if (c == '\\')
            return true;

        // Handle '[',']' (letter group), '^' (not letter, begin line anchor), '-' (dash), '&' ampersand
        if (c == '[' || c == ']' || c == '^' || c == '-' || c == '&')
            return true;

        // Handle anchors: '^' (begin line), '$' (end line),
        if (c == '^' || c == '$')
            return true;

        // Handle Greedy quantifiers: '?' (optional), '*' (zero or more), '+' (one or more), '{', '}' (n times)
        if (c == '?' || c == '*' || c == '+' || c == '{' || c == '}')
            return true;

        // Handle logical operators: '|' (or), '(', ')' (capturing group),
        if (c == '|' || c == '(' || c == ')')
            return true;

        // Return this
        return false;
    }
}