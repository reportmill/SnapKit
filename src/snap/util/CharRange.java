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
     * Returns a char range for line at given char index.
     */
    static CharRange getCharRangeLineForCharIndex(CharSequence theChars, int charIndex)
    {
        if (theChars instanceof CharSequenceX)
            return ((CharSequenceX) theChars).getCharRangeLineForCharIndex(charIndex);
        return CharRange.newCharRangeLineForCharsAndCharIndex(theChars, charIndex);
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
        while (nextLineStartCharIndex > 0 && nextLineStartCharIndex < charIndex) {
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
}
