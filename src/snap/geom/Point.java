/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.*;

/**
 * A class to represent a simple geometric point.
 */
public class Point {

    // The x component of point
    public final double x;
    
    // The y component of point
    public final double y;

    // A shared instance for Zero point
    public static final Point ZERO = new Point(0, 0);

    /**
     * Constructor.
     */
    public Point(double aX, double aY)  { x = aX; y = aY; }

    /**
     * Returns the point resulting from this point adding given point.
     */
    public Point add(Point aPoint)  { return new Point(x + aPoint.x, y + aPoint.y); }

    /**
     * Returns the point resulting from this point adding given X/Y.
     */
    public Point addXY(double dx, double dy)  { return new Point(x + dx, y + dy); }

    /**
     * Returns the point resulting from this point subtracting given point.
     */
    public Point subtract(Point aPoint)  { return new Point(x - aPoint.x, y - aPoint.y); }

    /**
     * Returns the point resulting from this point multiplied by a value.
     */
    public Point multiply(double aValue)  { return new Point(x * aValue, y * aValue); }

    /**
     * Returns the point resulting from this point divided by a value.
     */
    public Point divide(double aValue)
    {
        if (aValue == 0 || aValue == 1)
            return this;
        return new Point(x / aValue, y / aValue);
    }

    /**
     * Returns transformed point for this point and given Transform.
     */
    public Point transformedBy(Transform aTrans)
    {
        double x2 = x * aTrans._a + y * aTrans._c + aTrans._tx;
        double y2 = x * aTrans._b + y * aTrans._d + aTrans._ty;
        return new Point(x2, y2);
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
     * Returns this point XY rounded to nearest integers.
     */
    public Point round()
    {
        double roundX = Math.round(x);
        double roundY = Math.round(y);
        return roundX != x || roundY != y ? new Point(roundX, roundY) : this;
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
    @Override
    public boolean equals(Object anObj)  { return anObj instanceof Point other && equals(x, y, other.x, other.y); }

    /**
     * Returns a string representation of the receiver in the form "[x y]".
     */
    @Override
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
        return Math.sqrt(getDistanceSquared(x0, y0, x1, y1));
    }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public static double getDistanceSquared(double x0, double y0, double x1, double y1)
    {
        double dx = x1 - x0, dy = y1 - y0; return dx * dx + dy * dy;
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