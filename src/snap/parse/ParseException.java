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

    // The last processed token
    private ParseToken _lastToken;

    /**
     * Creates a new parse exception.
     */
    public ParseException(Parser aParser, ParseRule aRule)
    {
        _parser = aParser;
        _rule = aRule;
        _lastToken = _parser.getLastProcessedToken();
        _charIndex = _lastToken != null ? _lastToken.getStartCharIndex() : _parser.getCharIndex();
        if (_parser.getToken() == null && _lastToken != null)
            _charIndex = _lastToken.getEndCharIndex();
        //_lineIndex = token != null ? token.getLineIndex() : 0;
        //_colIndex = token != null ? token.getEndCharIndexInLine() : 0;
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

        String ruleName = _rule.getName() != null ? _rule.getName() : _rule.toString();
        return _msg = "Expecting " + ruleName;
    }

    /**
     * Returns the characters from error to end of line or input.
     */
    public CharSequence getErrorChars()
    {
        CharSequence inputText = _parser.getInput();
        int lineEnd = CharSequenceUtils.indexOfNewline(inputText, _charIndex);
        if (lineEnd < 0) lineEnd = inputText.length();
        return inputText.subSequence(_charIndex, lineEnd);
    }

    /**
     * Returns the char index.
     */
    public int getCharIndex()  { return _charIndex; }
}