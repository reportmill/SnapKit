/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;

/**
 * Represents a portion of a char sequence.
 */
public interface Token {

    /**
     * Returns the token name.
     */
    String getName();

    /**
     * Returns the pattern.
     */
    String getPattern();

    /**
     * Returns the index of the start of this token in input.
     */
    int getInputStart();

    /**
     * Returns the index of the end of this token in input.
     */
    int getInputEnd();

    /**
     * Returns the line index.
     */
    int getLineIndex();

    /**
     * Returns the line start.
     */
    int getLineStart();

    /**
     * Returns the column index.
     */
    default int getColumnIndex()
    {
        int textStartCharIndex = getInputStart();
        int lineStartCharIndex = getLineStart();
        return textStartCharIndex - lineStartCharIndex;
    }

    /**
     * Returns the special token.
     */
    Token getSpecialToken();

    /**
     * Returns the string.
     */
    String getString();

    /**
     * A basic implementation of a Token.
     */
    class BasicToken implements Token {

        // The tokenizer that provided this token
        protected Tokenizer  _tokenizer;

        // The name this token matches
        protected String  _name;

        // The pattern this token matches
        protected String  _pattern;

        // The start char index
        protected int  _startCharIndex;

        // The end char index
        protected int  _endCharIndex;

        // The line index
        protected int  _lineIndex;

        // The line start char index
        protected int  _lineStartCharIndex;

        // The special token that preceded this token, if available
        protected Token  _specialToken;

        // The string
        protected String  _string;

        /**
         * Constructor.
         */
        public BasicToken()
        {
            super();
        }

        /** Returns the name. */
        public String getName()  { return _name; }

        /** Returns the pattern. */
        public String getPattern()  { return _pattern; }

        /** Returns the char start. */
        public int getInputStart()  { return _startCharIndex; }

        /** Returns the char end. */
        public int getInputEnd()  { return _endCharIndex; }

        /** Returns the line index. */
        public int getLineIndex()  { return _lineIndex; }

        /** Returns the line start. */
        public int getLineStart()  { return _lineStartCharIndex; }

        /** Returns the special token. */
        public Token getSpecialToken()  { return _specialToken; }

        /** Returns the string. */
        public String getString()
        {
            if (_string != null) return _string;

            CharSequence chars = _tokenizer.getInput(_startCharIndex, _endCharIndex);
            String string = chars.toString();
            return _string = string;
        }

        /** Returns the string. */
        public String toString()
        {
            return "Token { start:" + _startCharIndex + ", end:" + _endCharIndex + " }: " + getString();
        }
    }
}