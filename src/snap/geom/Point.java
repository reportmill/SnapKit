/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.*;

/**
 * A class to represent a simple geometric point.
 */
public class Point implements Cloneable {

    // Ivars
    public double x, y;
    
    /**
     * Create new point.
     */
    public Point()  { }

    /**
     * Create new point.
     */
    public Point(double aX, double aY)  { x = aX; y = aY; }

    /**
     * Create new point.
     */
    public Point(Point aPoint)  { x = aPoint.getX(); y = aPoint.getY(); }

    /**
     * Return point x.
     */
    public double getX()  { return x; }

    /**
     * Set point x.
     */
    public void setX(double aValue)  { x = aValue; }

    /**
     * Return point y.
     */
    public double getY()  { return y; }

    /**
     * Set point y.
     */
    public void setY(double aValue)  { y = aValue; }

    /**
     * Sets the x/y.
     */
    public void setXY(Point aPoint)  { setX(aPoint.getX()); setY(aPoint.getY()); }

    /**
     * Sets the x/y.
     */
    public void setXY(double aX, double aY)  { setX(aX); setY(aY); }

    /**
     * Offsets the receiver by the given x and y.
     */
    public void offset(double dx, double dy)  { setXY(getX() + dx, getY() + dy); }

    /**
     * Returns the rounded x value (as int).
     */
    public int getRoundX()  { return (int)Math.round(x); }

    /**
     * Returns the rounded y value (as int).
     */
    public int getRoundY()  { return (int)Math.round(y); }

    /**
     * Adds the given point to this point.
     */
    public void add(Point aPoint)  { setXY(x + aPoint.getX(), y + aPoint.getY()); }

    /**
     * Subtracts the given point from this point.
     */
    public void subtract(Point aPoint)  { setXY(x - aPoint.getX(), y - aPoint.getY()); }

    /**
     * Multiplies this point by the given sx and sy.
     */
    public void multiply(double sx, double sy)  { setXY(x*sx, y*sy); }

    /**
     * Transforms this point by the given Transform.
     */
    public void transformBy(Transform aTrans)  { aTrans.transform(this, this); }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public double getDistance(Point aPoint)  { return getDistance(aPoint.getX(), aPoint.getY()); }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public double getDistance(double px, double py)  { px -= getX(); py -= getY(); return Math.sqrt(px*px + py*py); }

    /**
     * Rounds a point to neared integers.
     */
    public void snap()
    {
        setXY(Math.round(getX()), Math.round(getY()));
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        Point other = anObj instanceof Point ? (Point) anObj : null; if (other==null) return false;
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
    public static final double getDistance(double x0, double y0, double x1, double y1)
    {
        return Math.sqrt(getDistanceSquared(x0,y0,x1,y1));
    }

    /**
     * Returns the distance from this Point to a specified point.
     */
    public static final double getDistanceSquared(double x0, double y0, double x1, double y1)
    {
        double dx = x1 - x0, dy = y1 - y0; return dx*dx + dy*dy;
    }

    /**
     * Returns the angle to the mouse point.
     */
    public static final double getAngle(double x0, double y0, double x1, double y1)
    {
        double dx = x1 - x0, dy = y1 - y0;
        double angle = Math.toDegrees(Math.atan(dy/Math.abs(dx)));
        if (dx<0)
            angle = 180 - angle;
        else if (dx==0)
            angle = dy>0 ? -90 : dy<0 ? 90 : 0;
        return angle;
    }
}