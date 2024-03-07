/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import snap.util.CharRange;

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
     * Returns the column index.
     */
    int getColumnIndex();

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

        // The CharRange line to get LineIndex and ColumnIndex
        private CharRange _charRangeLine;

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
        public int getLineIndex()
        {
            CharRange charRangeLine = getCharRangeLine();
            return charRangeLine.getLineIndex();
        }

        /** Returns the column index. */
        public int getColumnIndex()
        {
            CharRange charRangeLine = getCharRangeLine();
            return charRangeLine.getColumnIndex();
        }

        /** Returns the CharRangeLine. */
        private CharRange getCharRangeLine()
        {
            if (_charRangeLine != null) return _charRangeLine;
            return _charRangeLine = CharRange.getCharRangeLineForCharIndex(_text, _startCharIndex);
        }

        /** Returns the string. */
        public String getString()
        {
            if (_string != null) return _string;

            if (_startCharIndex == _endCharIndex)
                return "";

            CharSequence chars = _text.subSequence(_startCharIndex, _endCharIndex);
            return _string = chars.toString();
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

        // Build method
        public ParseToken build()  { return _token; }
    }
}