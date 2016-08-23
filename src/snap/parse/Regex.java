/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.util.regex.*;

/**
 * A class for building regular expressions like a string buffer.
 */
public class Regex {

    // The name of this regex, if applicable
    String            _name;
    
    // The pattern of this regex
    String            _pattern;
    
    // Returns whether the pattern is literal
    boolean           _literal;
    
    // The compiled pattern
    Pattern           _patternCompiled;
    
    // A shared matcher
    Matcher           _matcher;
    
    // The literal length of the pattern
    int               _literalLength = -1;

    // String buffer to hold expression
    StringBuffer      _sb;

    // Operators
    public enum Op { And, Or };

    // Constants for expression parts
    public static final String LetterLower = "a-z";
    public static final String LetterUpper = "A-Z";
    public static final String Digit = "0-9";
    public static final String WhiteSpace = "\\s";

/**
 * Creates a new regex.
 */
public Regex()  { }

/**
 * Creates a new regex with given pattern and name.
 */
public Regex(String aName, String aPattern)  { setName(aName); setPattern(aPattern); }

/**
 * Creates a new regex with given pattern and name.
 */
public Regex(String aName, String aPattern, boolean isLiteral)
{
    setName(aName); setPattern(aPattern); setLiteral(isLiteral);
}

/**
 * Returns the regex name.
 */
public String getName()  { return _name; }

/**
 * Sets the regex name.
 */
public void setName(String aName)  { _name = aName!=null? aName.intern() : null; }

/**
 * Returns the regex pattern.
 */
public String getPattern()
{
    if(_pattern==null && _sb!=null) {
        _pattern = _sb.toString().intern();
        _sb = null;
    }
    return _pattern;
}

/**
 * Sets the pattern.
 */
public void setPattern(String aPattern)
{
    _pattern = aPattern!=null? aPattern.intern() : null;
    _literal = _pattern.length()<3 || _pattern.indexOf('[')<0;
}

/**
 * Returns whether pattern is literal.
 */
public boolean isLiteral()  { return _literal; }

/**
 * Sets whether pattern is literal.
 */
public void setLiteral(boolean aFlag)  { _literal = aFlag; }

/**
 * Returns the compiled pattern.
 */
public Pattern getPatternCompiled()
{
    if(_patternCompiled==null)
        try { _patternCompiled = Pattern.compile(getPattern(), getPatternCompileFlags()); }
        catch(PatternSyntaxException e)  { throw e; }
    return _patternCompiled;
}

/**
 * Returns pattern compile flags.
 */
public int getPatternCompileFlags()  { return isLiteral()? Pattern.LITERAL : 0; }

/**
 * Returns the shared matcher (initialized to the given char sequence if not yet created). 
 */
public Matcher getMatcher(CharSequence anInput)
{
    if(_matcher==null)
        _matcher = getPatternCompiled().matcher(anInput);
    return _matcher;
}

/**
 * Returns the literal length of this regex (the number of literal chars in the prefix).
 */
public int getLiteralLength()
{
    if(_literalLength<0)
        _literalLength = isLiteral()? getPattern().length() : getLiteralLength(getPattern());
    return _literalLength;
}

/**
 * Returns the first literal in regex pattern (or 0 if not literal).
 */
public char getLiteralChar()  { return isLiteral()? getPattern().charAt(0) : getLiteralChar(getPattern()); }    

/**
 * Adds a group.
 */
public void addGroup(Op anOp, String ... theParts)  { }

/**
 * Adds a letter.
 */
public void addChars(String ... theParts)  { }

/**
 * Adds a literal char.
 */
public Regex addChar(char c)
{
    // Handle special chars: '.', '-'
    if(c=='.' || c=='-')
        append("\\" + c);

    // Handle escape chars: \, \t, \n, \r, \f (form-feed), \a (alert/bell), \e (escape)
    else if(c=='\\' || c=='\t' || c=='\n' || c=='\r' || c=='\f')
        append("\\" + c);
    
    // Handle '[',']' (letter group), '^' (not letter, begin line anchor), '-' (dash), '&' ampersand
    else if(c=='[' || c==']'|| c=='^' || c=='-' || c=='&')
        append("\\" + c);
    
    // Handle anchors: '^' (begin line), '$' (end line), 
    else if(c=='^' || c=='$')
        append("\\" + c);
    
    // Handle Greedy quantifiers: '?' (optional), '*' (zero or more), '+' (one or more), '{', '}' (n times)
    else if(c=='?' || c=='*' || c=='+' || c=='{' || c=='}')
        append("\\" + c);
    
    // Handle logical operators: '|' (or), '(', ')' (capturing group), 
    else if(c=='|' || c=='(' || c==')' )
        append("\\" + c);
    
    // Otherwise, just append char
    else append(c);

    // Return this
    return this;
}

/**
 * Adds any letter.
 */
public Regex addLetter()  { addLetterLower(); return addLetterUpper(); }

/**
 * Adds any letter.
 */
public Regex addLetterLower()  { return append("a-z"); }

/**
 * Adds any letter.
 */
public Regex addLetterUpper()  { return append("a-z"); }

/**
 * Adds any char (doesn't include newlines).
 */
public Regex addAnyChar()  { return append('.'); }

/**
 * Adds a digit: [0-9].
 */
public Regex addDigit()  { return append("\\d"); }

/**
 * Adds a non-digit: [^0-9].
 */
public Regex addNonDigit()  { return append("\\D"); } // Also "^0-9"

/**
 * Adds whitespace char [ \t\n\x0B\f\r].
 */
public Regex addWhitespace()  { return append("\\s"); }

/**
 * Adds non-whitespace char: [^\s].
 */
public Regex addNonWhitespace()  { return append("\\S"); }

/**
 * Adds a word character: [a-zA-Z_0-9].
 */
public Regex addWordCharacter()  { return append("\\w"); }

/**
 * Adds a non-word character: [^\w].
 */
public Regex addNonWordCharacter()  { return append("\\W"); }

/**
 * Appends given char.
 */
private Regex append(char aChar)  { _sb.append(aChar); return this; }

/**
 * Appends given string.
 */
private Regex append(String aString)  { _sb.append(aString); return this; }

/**
 * Returns a string representation of regex.
 */
public String toString()  { return String.format("RMRegex \"%s\"", getPattern()); }

/**
 * Utility method to return whether given character is a special char.
 */
public static boolean isSpecialChar(char c)
{
    // Handle special chars: '.', '-'
    if(c=='.' || c=='-')
        return true;

    // Handle escape chars: \, \t, \n, \r, \f (form-feed), \a (alert/bell), \e (escape)
    else if(c=='\\')
        return true;
    
    // Handle '[',']' (letter group), '^' (not letter, begin line anchor), '-' (dash), '&' ampersand
    else if(c=='[' || c==']'|| c=='^' || c=='-' || c=='&')
        return true;
    
    // Handle anchors: '^' (begin line), '$' (end line), 
    else if(c=='^' || c=='$')
        return true;
    
    // Handle Greedy quantifiers: '?' (optional), '*' (zero or more), '+' (one or more), '{', '}' (n times)
    else if(c=='?' || c=='*' || c=='+' || c=='{' || c=='}')
        return true;
    
    // Handle logical operators: '|' (or), '(', ')' (capturing group), 
    else if(c=='|' || c=='(' || c==')' )
        return true;

    // Return this
    return false;
}

/**
 * Utility method to return whether char is an anchor char.
 */
public static boolean isAncharChar(char c)  { return c=='^' || c=='$'; }

/**
 * Returns the literal length of pattern.
 */
public static int getLiteralLength(String aPattern)
{
    int literalLength = 0;
    for(int i=0, iMax=aPattern.length(); i<iMax; i++) { char c = aPattern.charAt(i);
        if(Regex.isSpecialChar(c) && (i>0 || (c!=']' && c!='}' && c!='-' && c!='&'))) {
            if(Regex.isAncharChar(c))
                continue;
            else if(c=='\\') {
                literalLength++; i++; }
            else break;
        }
        else literalLength++; 
    }

    // Return literal length
    return literalLength;
}

/**
 * Returns the first char of a pattern, if it's a literal char.
 */
public static char getLiteralChar(String aPattern)
{
    char c = aPattern.charAt(0);
    if(!isSpecialChar(c) || (c==']' || c=='}' || c=='-' || c=='&'))
        return c;
    if(c=='\\')
        return aPattern.charAt(1);
    return 0;
}

}