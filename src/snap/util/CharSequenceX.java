package snap.util;

/**
 * This CharSequence sub interface adds some convenience methods.
 */
public interface CharSequenceX extends CharSequence {

    /**
     * Returns the last char.
     */
    default char getLastChar()
    {
        int len = length();
        return len > 0 ? charAt(len - 1) : 0;
    }

    /**
     * Returns whether run ends with newline.
     */
    default boolean isLastCharNewline()
    {
        char c = getLastChar();
        return c == '\r' || c == '\n';
    }

    /**
     * Returns index of next newline (or carriage-return/newline) starting at given char index.
     */
    default int indexOfNewline(int aStart)
    {
        return CharSequenceUtils.indexOfNewline(this, aStart);
    }

    /**
     * Returns index just beyond next newline (or carriage-return/newline) starting at given char index.
     */
    default int indexAfterNewline(int aStart)
    {
        return CharSequenceUtils.indexAfterNewline(this, aStart);
    }

    /**
     * Returns index of the previous newline (or carriage-return/newline) starting at given char index.
     */
    default int lastIndexOfNewline(int aStart)
    {
        return CharSequenceUtils.lastIndexOfNewline(this, aStart);
    }

    /**
     * Returns index just beyond previous newline (or carriage-return/newline) starting at given char index.
     */
    default int lastIndexAfterNewline(int aStart)
    {
        return CharSequenceUtils.lastIndexAfterNewline(this, aStart);
    }

    /**
     * Returns whether the index in the given char sequence is at a line end.
     */
    default boolean isLineEnd(int anIndex)
    {
        return CharSequenceUtils.isLineEnd(this, anIndex);
    }

    /**
     * Returns whether the index in the given char sequence is at just after a line end.
     */
    default boolean isAfterLineEnd(int anIndex)
    {
        return CharSequenceUtils.isAfterLineEnd(this, anIndex);
    }

    /**
     * Returns whether a char is a newline char.
     */
    default boolean isLineEndChar(int anIndex)
    {
        return CharSequenceUtils.isLineEndChar(this, anIndex);
    }

    /**
     * Returns a string of any leading whitespace chars.
     */
    default String getIndentString()
    {
        // Get leading space chars
        StringBuffer sb = new StringBuffer();
        int lineLength = length();
        for (int i = 0; i < lineLength; i++) {
            char loopChar = charAt(i);
            if (Character.isWhitespace(loopChar) && loopChar != '\n')
                sb.append(loopChar);
            else break;
        }

        // Return
        return sb.toString();
    }
}
