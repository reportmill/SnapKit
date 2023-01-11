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
        return CharSequenceUtils.isLastCharNewline(this);
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
     * Returns whether sequence starts with given string.
     */
    default boolean startsWith(String aString)
    {
        return CharSequenceUtils.startsWith(this, aString);
    }

    /**
     * Returns length of leading whitespace chars.
     */
    default int getIndentLength()
    {
        return CharSequenceUtils.getIndentLength(this);
    }

    /**
     * Returns a string of any leading whitespace chars.
     */
    default String getIndentString()
    {
        return CharSequenceUtils.getIndentString(this);
    }

    /**
     * Returns whether this sequence is just whitespace.
     */
    default boolean isWhiteSpace()
    {
        return CharSequenceUtils.isWhiteSpace(this);
    }
}
