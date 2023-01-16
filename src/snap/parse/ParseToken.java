/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;

/**
 * Represents an individual 'word' from parsed text.
 */
public interface ParseToken {

    /**
     * Returns the token name.
     */
    String getName();

    /**
     * Returns the pattern.
     */
    String getPattern();

    /**
     * Returns the start char index of this token in input.
     */
    int getStartCharIndex();

    /**
     * Returns the end char index of this token in input.
     */
    int getEndCharIndex();

    /**
     * Returns the line index.
     */
    int getLineIndex();

    /**
     * Returns the start char index of this token in line.
     */
    int getStartCharIndexInLine();

    /**
     * Returns the column index.
     */
    default int getColumnIndex()
    {
        int textStartCharIndex = getStartCharIndex();
        int lineStartCharIndex = getStartCharIndexInLine();
        return textStartCharIndex - lineStartCharIndex;
    }

    /**
     * Returns the string.
     */
    String getString();

    /**
     * A basic implementation of a Token.
     */
    class BasicToken implements ParseToken {

        // The text that provided this token
        protected CharSequence  _text;

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
        protected int  _startCharIndexInLine;

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
        public int getStartCharIndex()  { return _startCharIndex; }

        /** Returns the char end. */
        public int getEndCharIndex()  { return _endCharIndex; }

        /** Returns the line index. */
        public int getLineIndex()  { return _lineIndex; }

        /** Returns the line start. */
        public int getStartCharIndexInLine()  { return _startCharIndexInLine; }

        /** Returns the string. */
        public String getString()
        {
            if (_string != null) return _string;

            CharSequence chars = _text.subSequence(_startCharIndex, _endCharIndex);
            String string = chars.toString();
            return _string = string;
        }

        /** Returns the string. */
        public String toString()
        {
            return "Token { start:" + _startCharIndex + ", end:" + _endCharIndex + " }: " + getString();
        }
    }

    /**
     * A Builder.
     */
    class Builder {

        // The ParseToken
        private BasicToken  _token = new BasicToken();

        // Property Methods
        public Builder text(CharSequence aValue)  { _token._text = aValue; return this; }
        public Builder name(String aName)  { _token._name = aName; return this; }
        public Builder pattern(String aString)  { _token._pattern = aString; return this; }
        public Builder startCharIndex(int aValue)  { _token._startCharIndex = aValue; return this; }
        public Builder endCharIndex(int aValue)  { _token._endCharIndex = aValue; return this; }
        public Builder lineIndex(int aValue)  { _token._lineIndex = aValue; return this; }
        public Builder startCharIndexInLine(int aValue)  { _token._startCharIndexInLine = aValue; return this; }

        // Build method
        public ParseToken build()  { return _token; }
    }
}