package snap.util;

/**
 * This class adds some convenience methods for CharSequence.
 */
public class CharSequenceUtils {

    /**
     * Returns index of the next newline (or carriage-return/newline) in given chars starting at given char index.
     */
    public static int indexOfNewline(CharSequence theChars, int aStart)
    {
        for (int i = aStart, iMax = theChars.length(); i < iMax; i++)
            if (isLineEndChar(theChars, i))
                return i;
        return -1;
    }

    /**
     * Returns index just beyond next newline (or carriage-return/newline) in given chars starting at given char index.
     */
    public static int indexAfterNewline(CharSequence theChars, int aStart)
    {
        for (int i = aStart, iMax = theChars.length(); i < iMax; i++) {
            char c = theChars.charAt(i);
            if (c == '\r')
                return i + 1 < iMax && theChars.charAt(i + 1) == '\n' ? (i + 2) : (i + 1);
            if (c == '\n')
                return i + 1;
        }
        return -1;
    }

    /**
     * Returns index of the previous newline (or carriage-return/newline) in given chars starting at given char index.
     */
    public static int lastIndexOfNewline(CharSequence theChars, int aStart)
    {
        for (int i = aStart - 1; i >= 0; i--) {
            char c = theChars.charAt(i);
            if (c == '\n')
                return i - 1 >= 0 && theChars.charAt(i - 1) == '\r' ? (i - 1) : i;
            if (c == '\r')
                return i;
        }
        return -1;
    }

    /**
     * Returns index just beyond previous newline (or carriage-return/newline) in given chars starting at given char index.
     */
    public static int lastIndexAfterNewline(CharSequence theChars, int aStart)
    {
        for (int i = aStart - 1; i >= 0; i--)
            if (isLineEndChar(theChars, i))
                return i + 1;
        return -1;
    }

    /**
     * Returns whether the index in the given char sequence is at a line end.
     */
    public static boolean isLineEnd(CharSequence theChars, int anIndex)
    {
        return anIndex < theChars.length() && isLineEndChar(theChars, anIndex);
    }

    /**
     * Returns whether the index in the given char sequence is at just after a line end.
     */
    public static boolean isAfterLineEnd(CharSequence theChars, int anIndex)
    {
        return anIndex - 1 >= 0 && isLineEndChar(theChars, anIndex - 1);
    }

    /**
     * Returns whether a char is a newline char.
     */
    public static boolean isLineEndChar(CharSequence theChars, int anIndex)
    {
        return isLineEndChar(theChars.charAt(anIndex));
    }

    /**
     * Returns whether a char is a newline char.
     */
    public static boolean isLineEndChar(char c)
    {
        return c == '\r' || c == '\n';
    }
}
