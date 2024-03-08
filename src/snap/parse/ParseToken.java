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

        // The ParseToken
        private ParseTokenImpl _token = new ParseTokenImpl();

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