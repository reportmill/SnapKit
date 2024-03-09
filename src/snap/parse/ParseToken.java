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
     * Returns the column index.
     */
    int getColumnIndex();

    /**
     * Returns the string.
     */
    String getString();

    /**
     * A Builder.
     */
    class Builder {

        // Attributes
        private CharSequence _chars;
        private String _name;
        private String _pattern;
        private int _startCharIndex;
        private int _endCharIndex;

        // Property Methods
        public Builder text(CharSequence aValue)  { _chars = aValue; return this; }
        public Builder name(String aName)  { _name = aName; return this; }
        public Builder pattern(String aString)  { _pattern = aString; return this; }
        public Builder startCharIndex(int aValue)  { _startCharIndex = aValue; return this; }
        public Builder endCharIndex(int aValue)  { _endCharIndex = aValue; return this; }

        // Build method
        public ParseToken build()
        {
            // Build new token
            TokenDoc tokenDoc = new TokenDoc(_chars);
            TokenLine tokenLine = tokenDoc.addLineForCharRange(0, _chars.length());
            ParseTokenImpl token = new ParseTokenImpl();
            token._tokenLine = tokenLine;
            token._name = _name;
            token._pattern = _pattern;
            token._startCharIndex = _startCharIndex;
            token._endCharIndex = _endCharIndex;

            // Reset
            _name = _pattern = null;
            _startCharIndex = _endCharIndex = 0;

            // Return
            return token;
        }
    }
}