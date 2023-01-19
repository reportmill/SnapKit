package snap.util;

/**
 * This class adds some convenience methods for CharSequence.
 */
public class CharSequenceUtils {

    /**
     * Returns the last char.
     */
    public static char getLastChar(CharSequence theChars)
    {
        int len = theChars.length();
        return len > 0 ? theChars.charAt(len - 1) : 0;
    }

    /**
     * Returns whether char sequence ends with newline.
     */
    public static boolean isLastCharNewline(CharSequence theChars)
    {
        char c = getLastChar(theChars);
        return c == '\r' || c == '\n';
    }

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

    /**
     * Returns whether sequence starts with given string.
     */
    public static boolean startsWith(CharSequence theChars, String aString)
    {
        int thisLength = theChars.length();
        int strLength = aString.length();
        if (thisLength < strLength)
            return false;
        for (int i = 0; i < strLength; i++)
            if (theChars.charAt(i) != aString.charAt(i))
                return false;
        return true;
    }

    /**
     * Returns the length of leading whitespace chars for given char sequence.
     */
    public static int getIndentLength(CharSequence theChars)
    {
        // Get leading space chars
        int indentLength = 0;
        int lineLength = theChars.length();
        for (int i = 0; i < lineLength; i++) {
            char loopChar = theChars.charAt(i);
            if (Character.isWhitespace(loopChar) && loopChar != '\n')
                indentLength++;
            else break;
        }

        // Return
        return indentLength;
    }

    /**
     * Returns a string of any leading whitespace chars for given char sequence.
     */
    public static String getIndentString(CharSequence theChars)
    {
        // Get leading space chars
        StringBuffer sb = new StringBuffer();
        int lineLength = theChars.length();
        for (int i = 0; i < lineLength; i++) {
            char loopChar = theChars.charAt(i);
            if (Character.isWhitespace(loopChar) && loopChar != '\n')
                sb.append(loopChar);
            else break;
        }

        // Return
        return sb.toString();
    }

    /**
     * Returns whether given sequence is just whitespace.
     */
    public static boolean isWhiteSpace(CharSequence theChars)
    {
        int length = theChars.length();
        for (int i = 0; i < length; i++)
            if (!Character.isWhitespace(theChars.charAt(i)))
                return false;
        return true;
    }

    /**
     * Returns number of newlines in given chars.
     */
    public static int getNewlineCount(CharSequence theChars)
    {
        int newlineCount = 0;

        // Iterate over chars and look for newline, carriage return or CR/NL
        for (int i = 0, iMax = theChars.length(); i < iMax; i++) {
            char loopChar = theChars.charAt(i);

            // If newline, just bump count
            if (loopChar == '\n')
                newlineCount++;

            // If carriage return, bump count and check for newline
            else if (loopChar == '\r') {
                newlineCount++;
                if (i + 1 < iMax && theChars.charAt(i + 1) == '\n')
                    i++;
            }
        }

        // Return
        return newlineCount;
    }
}
