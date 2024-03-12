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
public class RegexBuilder {

    // The name of this regex, if applicable
    private String  _name;

    // String builder to hold pattern
    private StringBuilder  _sb;

    // Operators
    public enum Op { And, Or }

    // Constants for expression parts
    public static final String LetterLower = "a-z";
    public static final String LetterUpper = "A-Z";
    public static final String Digit = "0-9";
    public static final String WhiteSpace = "\\s";

    /**
     * Constructor with given pattern and name.
     */
    public RegexBuilder(String aName)
    {
        _name = aName != null ? aName.intern() : null;
    }

    /**
     * Builds and returns a regex.
     */
    public Regex build()  { return new Regex(_name, getPattern()); }

    /**
     * Returns the regex pattern.
     */
    public String getPattern()  { return _sb.toString(); }

    /**
     * Adds a group.
     */
    public void addGroup(Op anOp, String... theParts)  { }

    /**
     * Adds a letter.
     */
    public void addChars(String... theParts)  { }

    /**
     * Adds a literal char.
     */
    public RegexBuilder addChar(char c)
    {
        // Handle special chars: '.', '-'
        if (c == '.' || c == '-')
            append("\\" + c);

        // Handle escape chars: \, \t, \n, \r, \f (form-feed), \a (alert/bell), \e (escape)
        else if (c == '\\' || c == '\t' || c == '\n' || c == '\r' || c == '\f')
            append("\\" + c);

        // Handle '[',']' (letter group), '^' (not letter, begin line anchor), '-' (dash), '&' ampersand
        else if (c == '[' || c == ']' || c == '^' || c == '-' || c == '&')
            append("\\" + c);

        // Handle anchors: '^' (begin line), '$' (end line),
        else if (c == '^' || c == '$')
            append("\\" + c);

        // Handle Greedy quantifiers: '?' (optional), '*' (zero or more), '+' (one or more), '{', '}' (n times)
        else if (c == '?' || c == '*' || c == '+' || c == '{' || c == '}')
            append("\\" + c);

        // Handle logical operators: '|' (or), '(', ')' (capturing group),
        else if (c == '|' || c == '(' || c == ')')
            append("\\" + c);

        // Otherwise, just append char
        else append(c);

        // Return this
        return this;
    }

    /**
     * Adds any letter.
     */
    public RegexBuilder addLetter()
    {
        addLetterLower();
        return addLetterUpper();
    }

    /**
     * Adds any letter.
     */
    public RegexBuilder addLetterLower()  { return append("a-z"); }

    /**
     * Adds any letter.
     */
    public RegexBuilder addLetterUpper()  { return append("a-z"); }

    /**
     * Adds any char (doesn't include newlines).
     */
    public RegexBuilder addAnyChar()  { return append('.'); }

    /**
     * Adds a digit: [0-9].
     */
    public RegexBuilder addDigit()  { return append("\\d"); }

    /**
     * Adds a non-digit: [^0-9].
     */
    public RegexBuilder addNonDigit()  { return append("\\D"); } // Also "^0-9"

    /**
     * Adds whitespace char [ \t\n\x0B\f\r].
     */
    public RegexBuilder addWhitespace()  { return append("\\s"); }

    /**
     * Adds non-whitespace char: [^\s].
     */
    public RegexBuilder addNonWhitespace()  { return append("\\S"); }

    /**
     * Adds a word character: [a-zA-Z_0-9].
     */
    public RegexBuilder addWordCharacter()  { return append("\\w"); }

    /**
     * Adds a non-word character: [^\w].
     */
    public RegexBuilder addNonWordCharacter()  { return append("\\W"); }

    /**
     * Appends given char.
     */
    private RegexBuilder append(char aChar)
    {
        _sb.append(aChar);
        return this;
    }

    /**
     * Appends given string.
     */
    private RegexBuilder append(String aString)
    {
        _sb.append(aString);
        return this;
    }
}