/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;
import snap.util.StringUtils;

/**
 * A class to represent a simple geometric size.
 */
public class Size implements Cloneable {
    
    // Ivars
    public double width, height;
    
    /**
     * Create a new Size.
     */
    public Size()  { }

    /**
     * Create a new Size.
     */
    public Size(double aW, double aH)  { width = aW; height = aH; }

    /**
     * Create a new Size.
     */
    public Size(Size aSize)
    {
        if (aSize==null) return;
        width = aSize.getWidth(); height = aSize.getHeight();
    }

    /**
     * Returns the width.
     */
    public double getWidth()  { return width; }

    /**
     * Sets the width.
     */
    public void setWidth(double aValue)  { width = aValue; }

    /**
     * Returns the height.
     */
    public double getHeight()  { return height; }

    /**
     * Sets the height.
     */
    public void setHeight(double aValue)  { height = aValue; }

    /**
     * Sets the size.
     */
    public void setSize(double aW, double aH)  { setWidth(aW); setHeight(aH); }

    /**
     * Returns the square root of the sum of the squares of the width and height.
     */
    public double getMagnitude()  { double w = getWidth(), h = getHeight(); return Math.sqrt(w*w + h*h); }

    /**
     * Normalizes the receiver by scaling its width and height such that its magnitude will be 1.
     */
    public void normalize()  { double hyp = getMagnitude(), w = getWidth(), h = getHeight(); setSize(w/hyp, h/hyp); }

    /**
     * Simply sets the width and height to their negatives.
     */
    public void negate()  { setSize(-getWidth(), -getHeight()); }

    /**
     * Returns whether size is equal to given width and height.
     */
    public boolean equals(double w, double h)  { return w==getWidth() && h==getHeight(); }

    /**
     * Standard clone implementation.
     */
    public Size clone()
    {
        try { return (Size) super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        Size other = anObj instanceof Size ? (Size) anObj : null; if (other==null) return false;
        return other.getWidth()==width && other.getHeight()==height;
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
        double w = StringUtils.floatValue(aString), h = StringUtils.doubleValue(aString, aString.indexOf(",") + 1);
        return new Size(w,h);
    }
}