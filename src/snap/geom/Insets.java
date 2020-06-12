/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.StringUtils;

import java.util.Objects;

/**
 * This class represents a margin inset.
 */
public class Insets implements Cloneable {

    // Insets
    public double top, right, bottom, left;
    
    // Shared emtpy insets
    public static final Insets EMPTY = new Insets(0);
    
    /**
     * Create new Insets.
     */
    public Insets(double aVal)  { this(aVal, aVal, aVal, aVal); }

    /**
     * Create new Insets.
     */
    public Insets(double aTop, double aRight, double aBottom, double aLeft)
    {
        top = aTop; right = aRight; bottom = aBottom; left = aLeft;
    }

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
     * Returns the width of the insets (left + right).
     */
    public double getWidth()  { return left + right; }

    /**
     * Returns the height of the insets (top + bottom).
     */
    public double getHeight()  { return top + bottom; }

    /**
     * Returns a string representation of this Insets.
     */
    public String getString()
    {
        return top==right && top==bottom && top==left? StringUtils.toString(top) : getStringLong();
    }

    /**
     * Returns a string representation of this Insets.
     */
    public String getStringLong()
    {
        String t = StringUtils.toString(top), r = StringUtils.toString(right);
        String b = StringUtils.toString(bottom), l = StringUtils.toString(left);
        return t + "," + r + "," + b + "," + l;
    }

    /**
     * Returns a copy of this gradient paint.
     */
    public Insets clone()
    {
        try { return (Insets)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Standard equals method.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Insets insets = (Insets) o;
        return Double.compare(insets.top, top) == 0 &&
                Double.compare(insets.right, right) == 0 &&
                Double.compare(insets.bottom, bottom) == 0 &&
                Double.compare(insets.left, left) == 0;
    }

    /**
     * Standard hashCode method.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(top, right, bottom, left);
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
        if (aIns1==null || aIns1.isEmpty()) return aIns2; if (aIns2==null || aIns2.isEmpty()) return aIns1;
        return new Insets(aIns1.top+aIns2.top, aIns1.right+aIns2.right, aIns1.bottom+aIns2.bottom, aIns1.left+aIns2.left);
    }

    /**
     * Adds two insets together.
     */
    public static Insets add(Insets aIns1, double aTop, double aRight, double aBottom, double aLeft)
    {
        if (aIns1==null || aIns1.isEmpty()) return new Insets(aTop, aRight, aBottom, aLeft);
        if (aTop==0 && aRight==0 && aBottom==0 && aLeft==0) return aIns1;
        return new Insets(aIns1.top+aTop, aIns1.right+aRight, aIns1.bottom+aBottom, aIns1.left+aLeft);
    }

    /**
     * Returns an Insets instance from given string.
     */
    public static Insets get(String aString)
    {
        if (aString==null || aString.length()==0) return null;
        String margins[] = aString.split("\\,");
        int top = margins.length>0 ? StringUtils.intValue(margins[0]) : 0;
        int right = margins.length>1 ? StringUtils.intValue(margins[1]) : 0;
        int bottom = margins.length>2 ? StringUtils.intValue(margins[2]) : 0;
        int left = margins.length>3 ? StringUtils.intValue(margins[3]) : 0;
        if (margins.length==2) { left = right; bottom = top; }
        else if (margins.length==1) left = right = bottom = top;
        return new Insets(top, right, bottom, left);
    }
}