/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;
import snap.util.StringUtils;

/**
 * This class represents a vector.
 */
public class Vect implements Cloneable {
    
    // X Y components
    public double x, y;
    
    /**
     * Constructor.
     */
    public Vect()  { }

    /**
     * Constructor for given XY coords.
     */
    public Vect(double aX, double aY)
    {
        x = aX;
        y = aY;
    }

    /**
     * Returns the magnitude of the vector.
     */
    public double getMagnitude()
    {
        return getMagnitude(x, y);
    }

    /**
     * Makes the vector unit length.
     */
    public void normalize()
    {
        double t = getMagnitude();
        x /= t;
        y /= t;
    }

    /**
     * Add the given vector to this.
     */
    public void add(Vect aVect)
    {
        x += aVect.x;
        y += aVect.y;
    }

    /**
     * Returns the dot product of the receiver and the given vector.
     */
    public double getDotProduct(Vect v2)
    {
        return getDotProduct(x, y, v2.x, v2.y);
    }

    /**
     * Returns whether given vector is in same general direction of this (with option to include perpendiculars).
     */
    public boolean isAligned(Vect aVect, boolean includePerpendiculars)
    {
        return !isAway(aVect, !includePerpendiculars);
    }

    /**
     * Returns whether given vector is pointing away from the direction of this (with option to include perpendiculars).
     */
    public boolean isAway(Vect aVect, boolean includePerpendiculars)
    {
        // Get normalized version of this vector
        Vect v1 = getMagnitude() == 1 ? this : clone(); v1.normalize();

        // Get normalized version of given vector
        Vect v2 = aVect.getMagnitude() == 1 ? aVect : aVect.clone();
        v2.normalize();

        // Dot of normalized vectors GT 0: angle<90deg, EQ 0: angle==90deg, LT 0: angle>90deg
        double dot = v1.getDotProduct(v2);

        // Return whether angle is less than zero (or equal zero for perpendicular)
        return dot < 0 || (dot == 0 && includePerpendiculars);
    }

    /**
     * Returns the angle between the receiver and the given vector.
     */
    public double getAngleBetween(Vect aVect)
    {
        return getAngleBetween(x, y, aVect.x, aVect.y);
    }

    /**
     * Makes this receiver point in the opposite direction.
     */
    public void negate()  { x = -x; y = -y; }

    /**
     * Transforms this vector by the given transform.
     */
    public void transform(Transform aTrans)
    {
        Point p1 = aTrans.transformXY(0, 0);
        Point p2 = aTrans.transformXY(x, y);
        x = p2.x - p1.x;
        y = p2.y - p1.y;
    }

    /**
     * Sets the X/Y values.
     */
    public void setXY(Vect aVect)
    {
        x = aVect.x;
        y = aVect.y;
    }

    /**
     * Sets the X/Y values.
     */
    public void setXY(double aX, double aY)
    {
        x = aX;
        y = aY;
    }

    /**
     * Standard clone implementation.
     */
    public Vect clone()
    {
        return new Vect(x,y);
    }

    /**
     * Returns a string representation of the vector.
     */
    public String toString()
    {
        return "Vector [ " + StringUtils.toString(x) + " " + StringUtils.toString(y) + " ]";
    }

    /**
     * Returns the magnitude of a vector.
     */
    public static double getMagnitude(double aX, double aY)
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
        double m1 = getMagnitude(aX, aY);
        double m2 = getMagnitude(bX, bY);
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
        double mag = getMagnitude(aX, aY);
        return mag * cosTheta;
    }
}