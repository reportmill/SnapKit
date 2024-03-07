/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * This class represents a line of chars in a CharLines.
 */
public interface CharRange extends CharSequenceX {

    /**
     * Returns the start char index in source CharLines.
     */
    int getStartCharIndex();

    /**
     * Returns the end char index in source CharLines.
     */
    int getEndCharIndex();

    /**
     * Returns the line (as a CharRange) for this range.
     */
    default CharRange getLine()  { return this; }

    /**
     * Returns the line index for this range.
     */
    default int getLineIndex()
    {
        CharRange line = getLine();
        return line.getLineIndex();
    }

    /**
     * Returns the column index for this range.
     */
    default int getColumnIndex()
    {
        int startCharIndex = getStartCharIndex();
        CharRange line = getLine();
        int lineStartCharIndex = line.getStartCharIndex();
        return startCharIndex - lineStartCharIndex;
    }

    /**
     * Creates a CharRange for given chars and start/end char indexes.
     */
    static CharRange newCharRangeForCharsAndRange(CharSequence theChars, int startCharIndex, int endCharIndex)
    {
        return new CharRangeImpl(theChars, startCharIndex, endCharIndex);
    }

    /**
     * Creates a CharRange for given chars and start/end char indexes.
     */
    static CharRange newCharRangeLineForCharsAndCharIndex(CharSequence theChars, int charIndex)
    {
        // Find start of line at or before given char index
        int lineStartCharIndex = 0;
        int nextLineStartCharIndex = CharSequenceUtils.indexAfterNewline(theChars, lineStartCharIndex);
        int lineIndex = 0;
        while (nextLineStartCharIndex >= 0 && nextLineStartCharIndex < charIndex) {
            lineStartCharIndex = nextLineStartCharIndex;
            lineIndex++;
            nextLineStartCharIndex = CharSequenceUtils.indexAfterNewline(theChars, lineStartCharIndex);
        }

        // Create char range for line start/end/index
        int lineEndCharIndex = CharSequenceUtils.indexOfNewline(theChars, lineStartCharIndex);
        CharRangeImpl charRange = new CharRangeImpl(theChars, lineStartCharIndex, lineEndCharIndex);
        charRange._line = charRange;
        charRange._lineIndex = lineIndex;

        // Return
        return charRange;
    }

    /**
     * This class is a simple CharRange implementation.
     */
    class CharRangeImpl implements CharRange {

        // The chars
        private CharSequence _chars;

        // The start char index
        private int _startCharIndex;

        // The end char index
        private int _endCharIndex;

        // The line
        private CharRange _line;

        // The line
        private int _lineIndex;

        /**
         * Constructor.
         */
        public CharRangeImpl(CharSequence theChars, int startCharIndex, int endCharIndex)
        {
            _chars = theChars;
            _startCharIndex = startCharIndex;
            _endCharIndex = endCharIndex;
        }

        @Override
        public int getStartCharIndex()  { return  _startCharIndex; }

        @Override
        public int getEndCharIndex()  { return _endCharIndex; }

        @Override
        public CharRange getLine()
        {
            if (_line != null) return _line;
            return _line = newCharRangeLineForCharsAndCharIndex(_chars, _startCharIndex);
        }

        @Override
        public int getLineIndex()
        {
            CharRange line = getLine();
            return line == this ? _lineIndex : line.getLineIndex();
        }

        @Override
        public int length()  { return _endCharIndex - _startCharIndex; }

        @Override
        public char charAt(int charIndex)
        {
            int startCharIndex = getStartCharIndex();
            int charIndexInChars = startCharIndex + charIndex;
            return _chars.charAt(charIndexInChars);
        }

        @Override
        public CharSequence subSequence(int startCharIndex, int endCharIndex)
        {
            int startCharIndexInRange = getStartCharIndex();
            int startCharIndexInChars = startCharIndexInRange + startCharIndex;
            int endCharIndexInChars = startCharIndexInRange + endCharIndex;
            return _chars.subSequence(startCharIndexInChars, endCharIndexInChars);
        }
    }
}
