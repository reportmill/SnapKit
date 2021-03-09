package snap.util;

/**
 * A class to represent start and end values.
 */
public class Range {

    // The start/end values
    public final int start, end, length;
    
    // The index the range finished with
    public final int anchor;
    
    /**
     * Creates a range with start/end.
     */
    public Range(int aStart, int anEnd)  { this(aStart, anEnd, aStart<=anEnd? anEnd : aStart); }

    /**
     * Creates a range, with start/end/anchor.
     */
    public Range(int aStart, int anEnd, int anAnchor)
    {
        start = Math.min(aStart, anEnd); end = Math.max(aStart, anEnd); length = end - start; anchor = anAnchor;
    }
    
    /**
     * Returns whether range is empty.
     */
    public boolean isEmpty()  { return length==0; }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "Range { Start=" + start + ", End=" + end + ", Length=" + length + ", Anchor=" + anchor + " }";
    }
}