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
    private String _msg;

    // The parser
    private Parser _parser;

    // The failed rule
    private ParseRule _rule;

    // The char index
    private int _charIndex;

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
        if (_msg != null) return _msg;
        return _msg = createMessage();
    }

    /**
     * Create message.
     */
    protected String createMessage()
    {
        // Get last valid token
        ParseToken token = _parser.getLastValidToken();
        if (token == null)
            token = _parser.getToken();

        // Get some useful line/char positions
        _charIndex = token != null ? token.getEndCharIndex() : _parser.getTokenizer().getCharIndex();
        int lineIndex = token != null ? token.getLineIndex() : 0;
        int colIndex = token != null ? token.getEndCharIndexInLine() : 0;

        // Get Error region
        CharSequence inputText = _parser.getInput();
        int lineEnd = CharSequenceUtils.indexOfNewline(inputText, _charIndex);
        if (lineEnd < 0)
            lineEnd = inputText.length();
        CharSequence errorChars = inputText.subSequence(_charIndex, lineEnd);

        // Basic message
        String ruleName = _rule.getName() != null ? _rule.getName() : _rule.toString();
        String msg = "Failed to parse at line " + (lineIndex + 1) + ", char " + colIndex + ": " + errorChars + '\n' +
                "Expecting: " + ruleName;

        // Return string
        return msg;
    }

    /**
     * Returns the char index.
     */
    public int getCharIndex()  { return _charIndex; }
}