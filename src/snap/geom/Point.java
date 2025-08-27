/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.*;

/**
 * A class to represent a simple geometric point.
 */
public class Point implements Cloneable {

    // The x component of point
    public double x;
    
    // The y component of point
    public double y;

    /**
     * Constructor.
     */
    public Point()  { x = y = 0; }

    /**
     * Constructor.
     */
    public Point(double aX, double aY)  { x = aX; y = aY; }

    /**
     * Return point x.
     */
    public final double getX()  { return x; }

    /**
     * Return point y.
     */
    public final double getY()  { return y; }

    /**
     * Sets the x/y.
     */
    public void setXY(double aX, double aY)  { x = aX; y = aY; }

    /**
     * Sets the x/y.
     */
    public void setPoint(Point aPoint)  { x = aPoint.x; y = aPoint.y; }

    /**
     * Offsets the receiver by the given x and y.
     */
    public void offset(double dx, double dy)
    {
        x = x + dx;
        y = y + dy;
    }

    /**
     * Returns the rounded x value (as int).
     */
    public int getRoundX()  { return (int) Math.round(x); }

    /**
     * Returns the rounded y value (as int).
     */
    public int getRoundY()  { return (int) Math.round(y); }

    /**
     * Adds the given point to this point.
     */
    public void add(Point aPoint)
    {
        x = x + aPoint.x;
        y = y + aPoint.y;
    }

    /**
     * Subtracts the given point from this point.
     */
    public void subtract(Point aPoint)
    {
        x = x - aPoint.x;
        y = y - aPoint.y;
    }

    /**
     * Multiplies this point by the given sx and sy.
     */
    public void multiply(double sx, double sy)
    {
        x = x * sx;
        y = y * sy;
    }

    /**
     * Transforms this point by the given Transform.
     */
    public void transformBy(Transform aTrans)
    {
        double x2 = x * aTrans._a + y * aTrans._c + aTrans._tx;
        double y2 = x * aTrans._b + y * aTrans._d + aTrans._ty;
        x = x2;
        y = y2;
    }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public double getDistance(Point aPoint)  { return getDistance(aPoint.x, aPoint.y); }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public double getDistance(double px, double py)
    {
        px -= x;
        py -= y;
        return Math.sqrt(px * px + py * py);
    }

    /**
     * Rounds a point to neared integers.
     */
    public void snap()
    {
        x = Math.round(x);
        y = Math.round(y);
    }

    /**
     * Returns a copy of this point with new X.
     */
    public Point withX(double aX)  { return new Point(aX, y); }

    /**
     * Returns a copy of this point with new Y.
     */
    public Point withY(double aY)  { return new Point(x, aY); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        Point other = anObj instanceof Point ? (Point) anObj : null; if (other == null) return false;
        return equals(x, y, other.x, other.y);
    }

    /**
     * Standard clone implementation.
     */
    public Point clone()
    {
        try { return (Point) super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns a string representation of the receiver in the form "[x y]".
     */
    public String toString()  { return "[" + x + " " + y + "]"; }

    /**
     * Standard equals implementation.
     */
    public static boolean equals(double x0, double y0, double x1, double y1)
    {
        return MathUtils.equals(x0, x1) && MathUtils.equals(y0, y1);
    }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public static double getDistance(double x0, double y0, double x1, double y1)
    {
        return Math.sqrt(getDistanceSquared(x0,y0,x1,y1));
    }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public static double getDistanceSquared(double x0, double y0, double x1, double y1)
    {
        double dx = x1 - x0, dy = y1 - y0; return dx*dx + dy*dy;
    }

    /**
     * Returns the angle to the mouse point.
     */
    public static double getAngle(double x0, double y0, double x1, double y1)
    {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double angle = Math.toDegrees(Math.atan(dy / Math.abs(dx)));
        if (dx < 0)
            angle = 180 - angle;
        else if (dx == 0)
            angle = dy > 0 ? -90 : dy < 0 ? 90 : 0;
        return angle;
    }
}