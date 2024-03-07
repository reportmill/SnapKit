/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * This class manages a block of char lines.
 */
public interface CharBlock extends CharSequenceX {

    /**
     * Returns the number of lines.
     */
    int getLineCount();

    /**
     * Returns the individual line at give index.
     */
    CharLine getLine(int anIndex);

    /**
     * Returns the last line.
     */
    default CharLine getLastLine()
    {
        int lineCount = getLineCount();
        return lineCount > 0 ? getLine(lineCount - 1) : null;
    }

    /**
     * Returns the line for given char index.
     */
    CharLine getLineForCharIndex(int charIndex);
}
