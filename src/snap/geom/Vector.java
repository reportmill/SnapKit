/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;
import snap.util.StringUtils;

/**
 * This class represents a vector.
 */
public class Vector implements Cloneable{
    
    // X component
    public final double x;
    
    // Y component
    public final double y;

    // A shared instance for Zero vector
    public static final Vector ZERO = new Vector(0, 0);

    /**
     * Constructor for given XY coords.
     */
    public Vector(double aX, double aY)
    {
        x = aX;
        y = aY;
    }

    /**
     * Return the direction of this vector (in degrees).
     */
    public double getAngle()  { return Math.toDegrees(Math.atan2(x, y)); }

    /**
     * Returns the length of the vector.
     */
    public double getLength()  { return getLength(x, y); }

    /**
     * Returns the version of this vector where length is 1.
     */
    public Vector normalized()
    {
        double length = getLength();
        if (length == 1)
            return this;
        return new Vector(x / length, y / length);
    }

    /**
     * Add the given vector to this.
     */
    public Vector add(Vector aVector)  { return new Vector(x + aVector.x, y + aVector.y); }

    /**
     * Returns the dot product of the receiver and the given vector.
     */
    public double getDotProduct(Vector v2)  { return getDotProduct(x, y, v2.x, v2.y); }

    /**
     * Returns whether given vector is in same general direction of this (with option to include perpendiculars).
     */
    public boolean isAligned(Vector aVector, boolean includePerpendiculars)
    {
        return !isAway(aVector, !includePerpendiculars);
    }

    /**
     * Returns whether given vector is pointing away from the direction of this (with option to include perpendiculars).
     */
    public boolean isAway(Vector aVector, boolean includePerpendiculars)
    {
        // Get normalized version of this vector
        Vector v1 = normalized();

        // Get normalized version of given vector
        Vector v2 = aVector.normalized();

        // Dot of normalized vectors GT 0: angle<90deg, EQ 0: angle==90deg, LT 0: angle>90deg
        double dot = v1.getDotProduct(v2);

        // Return whether angle is less than zero (or equal zero for perpendicular)
        return dot < 0 || (dot == 0 && includePerpendiculars);
    }

    /**
     * Returns the angle between the receiver and the given vector.
     */
    public double getAngleBetween(Vector aVector)
    {
        return getAngleBetween(x, y, aVector.x, aVector.y);
    }

    /**
     * Makes this receiver point in the opposite direction.
     */
    public Vector negated()  { return new Vector(-x, -y); }

    /**
     * Transforms this vector by the given transform.
     */
    public Vector transformed(Transform aTrans)
    {
        Point p1 = aTrans.transformXY(0, 0);
        Point p2 = aTrans.transformXY(x, y);
        return new Vector(p2.x - p1.x, p2.y - p1.y);
    }

    /**
     * Returns a copy of this vector with new X.
     */
    public Vector withX(double aX)  { return new Vector(aX, y); }

    /**
     * Returns a copy of this vector with new Y.
     */
    public Vector withY(double aY)  { return new Vector(x, aY); }

    /**
     * Delete soon! Vector is immutable so makes no sense.
     */
    public Vector clone()  { return this; }

    /**
     * Returns a string representation of the vector.
     */
    public String toString()
    {
        return "Vector [ " + StringUtils.toString(x) + " " + StringUtils.toString(y) + " ]";
    }

    /**
     * Returns the length of given vector XY.
     */
    public static double getLength(double aX, double aY)
    {
        return Math.sqrt(aX * aX + aY * aY);
    }

    /**
     * Returns the dot product of two vectors.
     */
    public static double getDotProduct(double aX, double aY, double bX, double bY)
    {
        return aX * bX + aY * bY;
    }

    /**
     * Returns cosine of angle between given vectors (using given vector points).
     */
    private static double getCosAngleBetween(double aX, double aY, double bX, double bY)
    {
        // Get dot product of normalized vector points (make sure value in cosine range - could be off by rounding error)
        double m1 = getLength(aX, aY);
        double m2 = getLength(bX, bY);
        double dot = getDotProduct(aX, aY, bX, bY) / (m1 * m2);

        // Make sure value in cosine range (could be off by rounding error) and return
        double cosTheta = MathUtils.clamp(dot, -1, 1);
        return cosTheta;
    }

    /**
     * Returns the angle between vectors (using given vector points).
     */
    public static double getAngleBetween(double aX, double aY, double bX, double bY)
    {
        double cosTheta = getCosAngleBetween(aX, aY, bX, bY);
        return Math.acos(cosTheta);
    }

    /**
     * Returns the length of V1 as projected onto V2 (using given vector points).
     */
    public static double getProjectedDistance(double aX, double aY, double bX, double bY)
    {
        double cosTheta = getCosAngleBetween(aX, aY, bX, bY);
        double mag = getLength(aX, aY);
        return mag * cosTheta;
    }

    /**
     * Returns Vector for given angle and length.
     */
    public static Vector getVectorForAngleAndLength(double anAngle, double length)
    {
        double vectX = length * Math.cos(Math.toRadians(anAngle));
        double vectY = length * Math.sin(Math.toRadians(anAngle));
        return new Vector(vectX, vectY);
    }
}