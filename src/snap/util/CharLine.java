/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * This class represents a line of chars in a CharLines.
 */
public interface CharLine extends CharSequenceX {

    /**
     * Returns the source char block.
     */
    CharBlock getCharBlock();

    /**
     * Returns the index of this line
     */
    int getIndex();

    /**
     * Returns the start char index in source CharLines.
     */
    int getStartCharIndex();

    /**
     * Returns the end char index in source CharLines.
     */
    int getEndCharIndex();

    /**
     * Returns the next line.
     */
    default CharLine getNext()
    {
        CharBlock charBlock = getCharBlock();
        int nextLineIndex = getIndex() + 1;
        return nextLineIndex < charBlock.getLineCount() ? charBlock.getLine(nextLineIndex) : null;
    }
}
