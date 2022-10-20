/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;

/**
 * An exception subclass for parser.
 */
public class ParseException extends RuntimeException {

    // The message
    String _msg;

    // The parser
    Parser _parser;

    // The failed rule
    ParseRule _rule;

    /**
     * Creates a new parse exception.
     */
    public ParseException(Parser aParser, ParseRule aRule)
    {
        _parser = aParser;
        _rule = aRule;
        getMessage();
    }

    /**
     * Creates a new parse exception for message.
     */
    public ParseException(String aMessage)
    {
        _msg = aMessage;
    }


    /**
     * Create message.
     */
    public String getMessage()
    {
        return _msg != null ? _msg : (_msg = createMessage());
    }

    /**
     * Create message.
     */
    protected String createMessage()
    {
        // Get some useful line/char positions
        ParseToken token = _parser.getToken();
        int charIndex = token != null ? token.getStartCharIndex() : _parser.getTokenizer().getCharIndex();
        int lineIndex = token != null ? token.getLineIndex() : _parser.getTokenizer().getLineIndex();
        int colIndex = token != null ? token.getColumnIndex() : 0;

        // Get Error region
        String inputText = _parser.getInput().toString();
        int lineEnd = inputText.indexOf('\n', charIndex);
        if (lineEnd < 0)
            lineEnd = inputText.length();
        CharSequence errorChars = inputText.subSequence(charIndex, lineEnd);

        // Basic message
        StringBuffer sb = new StringBuffer("Failed to parse at line " + (lineIndex + 1) + ", char " + colIndex);
        sb.append(": ").append(errorChars).append('\n');
        sb.append("Expecting: ").append(_rule.getName() != null ? _rule.getName() : _rule);

        // Return string
        return sb.toString();
    }

}