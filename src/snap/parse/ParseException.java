/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.util.CharSequenceUtils;

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
        int lineIndex = token != null ? token.getLineIndex() : 0;
        int colIndex = token != null ? token.getColumnIndex() : 0;

        // Get Error region
        CharSequence inputText = _parser.getInput();
        int lineEnd = CharSequenceUtils.indexOfNewline(inputText, charIndex);
        if (lineEnd < 0)
            lineEnd = inputText.length();
        CharSequence errorChars = inputText.subSequence(charIndex, lineEnd);

        // Basic message
        String ruleName = _rule.getName() != null ? _rule.getName() : _rule.toString();
        String msg = "Failed to parse at line " + (lineIndex + 1) + ", char " + colIndex + ": " + errorChars + '\n' +
                "Expecting: " + ruleName;

        // Return string
        return msg;
    }

}