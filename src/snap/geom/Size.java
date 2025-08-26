/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;
import snap.util.StringUtils;

/**
 * A class to represent a simple geometric size (width + height).
 */
public class Size {
    
    // The size width
    public final double width;
    
    // The size height
    public final double height;

    // A shared instance for Zero point
    public static final Size ZERO = new Size(0, 0);

    /**
     * Constructor.
     */
    public Size(double aW, double aH)  { width = aW; height = aH; }

    /**
     * Returns the square root of the sum of the squares of the width and height.
     */
    public double getMagnitude()  { return Math.sqrt(width * width + height * height); }

    /**
     * Returns the normalizes size by scaling width and height such that its magnitude will be 1.
     */
    public Size normalized()
    {
        double hyp = getMagnitude();
        return new Size(width / hyp, height / hyp);
    }

    /**
     * Return the negated size.
     */
    public Size negated()  { return new Size(-width, -height); }

    /**
     * Returns whether size is empty.
     */
    public boolean isEmpty()  { return MathUtils.equalsZero(width) || MathUtils.equalsZero(height); }

    /**
     * Returns whether size is equal to given width and height.
     */
    public boolean equals(double w, double h)  { return w == width && h == height; }

    /**
     * Returns size with given width.
     */
    public Size withWidth(double newWidth)  { return new Size(newWidth, height); }

    /**
     * Returns size with given height.
     */
    public Size withHeight(double newHeight)  { return new Size(width, newHeight); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        Size other = anObj instanceof Size ? (Size) anObj : null; if (other == null) return false;
        return other.width == width && other.height == height;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()  { return "{" + width + "," + height + "}"; }

    /**
     * Standard equals implementation.
     */
    public static boolean equals(double x0, double y0, double x1, double y1)
    {
        return MathUtils.equals(x0, x1) && MathUtils.equals(y0, y1);
    }

    /**
     * Creates a size from a string (assumes comma separated).
     */
    public static Size get(String aString)
    {
        double w = StringUtils.floatValue(aString);
        double h = StringUtils.doubleValue(aString, aString.indexOf(",") + 1);
        return new Size(w, h);
    }
}