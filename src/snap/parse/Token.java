/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;

/**
 * Represents a portion of a char sequence.
 */
public interface Token {

    /**
     * The Tokenizer that provided this token.
     */
    public Tokenizer getTokenizer();

    /**
     * Returns the name.
     */
    public String getName();
    
    /**
     * Returns the pattern.
     */
    public String getPattern();
    
    /**
     * Returns the index of the start of this token in input.
     */
    public int getInputStart();
    
    /**
     * Returns the index of the end of this token in input.
     */
    public int getInputEnd();
    
    /**
     * Returns the line index.
     */
    public int getLineIndex();
    
    /**
     * Returns the line start.
     */
    public int getLineStart();
    
    /**
     * Returns the column index.
     */
    public int getColumnIndex();
    
    /**
     * Returns the special token.
     */
    public Token getSpecialToken();
    
    /**
     * Returns the string.
     */
    public String getString();

/**
 * A basic implementation of a Token.
 */
public static class BasicToken implements Token {

    // The tokenizer that provided this token
    Tokenizer    _tokenizer;

    // The name this token matches
    String       _name;
    
    // The pattern this token matches
    String       _pattern;
    
    // The start/end char index
    int          _start, _end;
    
    // The line index and line start char index
    int          _lineIndex, _lineStart;
    
    // The string
    String       _string;
    
    // The special token that preceded this token, if available
    Token        _specialToken;

    /** The Tokenizer that provided this token. */
    public Tokenizer getTokenizer()  { return _tokenizer; }

    /** Returns the name. */
    public String getName()  { return _name; }
    
    /** Returns the pattern. */
    public String getPattern()  { return _pattern; }
    
    /** Returns the char start. */
    public int getInputStart()  { return _start; }
    
    /** Returns the char end. */
    public int getInputEnd()  { return _end; }
    
    /** Returns the line index. */
    public int getLineIndex()  { return _lineIndex; }
    
    /** Returns the line start. */
    public int getLineStart()  { return _lineStart; }
    
    /** Returns the column index. */
    public int getColumnIndex()  { return getInputStart() - getLineStart(); }
    
    /** Returns the special token. */
    public Token getSpecialToken()  { return _specialToken; }
    
    /** Returns the string. */
    public String getString()  { return _string!=null? _string : (_string=createString()); }
    
    /** Creates the string. */
    protected String createString()  { return getTokenizer().getInput(_start, _end).toString(); }
    
    /** Returns the string. */
    public String toString()  { return "Token { start:" + _start + ", end:" + _end + " }: " + getString(); }
}

}