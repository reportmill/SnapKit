/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;

/**
 * An exception subclass for parser.
 */
public class ParseException extends RuntimeException {

    // The message
    String               _msg;
    
    // The parser
    Parser               _parser;
    
    // The failed rule
    ParseRule            _rule;

/**
 * Creates a new parse exception.
 */
public ParseException(Parser aParser, ParseRule aRule)  { _parser = aParser; _rule = aRule; getMessage(); }

/**
 * Creates a new parse exception for message.
 */
public ParseException(String aMessage)  { _msg = aMessage; }


/**
 * Create message.
 */
public String getMessage()  { return _msg!=null? _msg : (_msg=createMessage()); }

/**
 * Create message.
 */
protected String createMessage()
{
    // Get some useful line/char positions
    Token token = _parser.getToken();
    int charIndex = token!=null? token.getInputStart() : _parser.getTokenizer().getCharIndex();
    int lineIndex = token!=null? token.getLineIndex() : _parser.getTokenizer().getLineIndex();
    int colIndex = token!=null? token.getColumnIndex() : 0;
    int lineEnd = _parser.getInput().toString().indexOf('\n', charIndex);
    if(lineEnd<0) lineEnd = _parser.getInput().length();
    
    // Basic message
    StringBuffer sb = new StringBuffer("Failed to parse at line " + lineIndex + ", char " + colIndex);
    sb.append(": ").append(_parser.getInput(), charIndex, lineEnd).append('\n');
    sb.append("Expecting: ").append(_rule.getName()!=null? _rule.getName() : _rule);
    
    // Return string
    return sb.toString();
}

}