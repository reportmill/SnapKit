package snap.gfx;
import snap.util.StringUtils;

public class Insets {

    // Insets
    public double top, left, bottom, right;
    
    // Dec
    
/**
 * Create new Insets.
 */
public Insets(double aTop, double aRight, double aBottom, double aLeft)
{
    top = aTop; left = aLeft; bottom = aBottom; right = aRight;
}

/**
 * Create new Insets.
 */
public Insets(double aVal)  { this(aVal, aVal, aVal, aVal); }

/**
 * Returns the top margin.
 */
public double getTop()  { return top; }

/**
 * Returns the right margin.
 */
public double getRight()  { return right; }

/**
 * Returns the bottom margin.
 */
public double getBottom()  { return bottom; }

/**
 * Returns the left margin.
 */
public double getLeft()  { return left; }

/**
 * Returns whether insets are empty.
 */
public boolean isEmpty()  { return top==0 && right==0 && bottom==0 && left==0; }

/**
 * Returns a string representation of this Insets.
 */
public String getString()
{
    if(top==right && top==bottom && top==left) return StringUtils.toString(top);
    String t = StringUtils.toString(top), r = StringUtils.toString(right);
    String b = StringUtils.toString(bottom), l = StringUtils.toString(left);
    return t + "," + r + "," + b + "," + l;
}

/**
 * Standard toString implementation.
 */
public String toString()  { return getString(); }

/**
 * Adds two insets together.
 */
public static Insets add(Insets aIns1, Insets aIns2)
{
    if(aIns1==null || aIns1.isEmpty()) return aIns2; if(aIns2==null || aIns2.isEmpty()) return aIns1;
    return new Insets(aIns1.top+aIns2.top, aIns1.right+aIns2.right, aIns1.bottom+aIns2.bottom, aIns1.left+aIns2.left);
}

/**
 * Adds two insets together.
 */
public static Insets add(Insets aIns1, double aTop, double aRight, double aBottom, double aLeft)
{
    if(aIns1==null || aIns1.isEmpty()) return new Insets(aTop,aRight,aBottom,aLeft);
    if(aTop==0 && aRight==0 && aBottom==0 && aLeft==0) return aIns1;
    return new Insets(aIns1.top+aTop, aIns1.right+aRight, aIns1.bottom+aBottom, aIns1.left+aLeft);
}

/**
 * Returns an Insets instance from given string.
 */
public static Insets get(String aString)
{
    if(aString==null || aString.length()==0) return null;
    String margins[] = aString.split("\\,");
    int top = margins.length>0? StringUtils.intValue(margins[0]) : 0;
    int left = margins.length>1? StringUtils.intValue(margins[1]) : 0;
    int bottom = margins.length>2? StringUtils.intValue(margins[2]) : 0;
    int right = margins.length>3? StringUtils.intValue(margins[3]) : 0;
    return new Insets(top, left, bottom, right);
}

}