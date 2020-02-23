/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.StringUtils;

/**
 * This class represents a vector.
 */
public class Vect implements Cloneable {
    
    // X Y components
    public double x, y;
    
/**
 * Creates a new vector from the given coords.
 */
public Vect(double aX, double aY)  { x = aX; y = aY; }

/**
 * Returns the magnitude of the vector.
 */
public double getMagnitude()  { return getMagnitude(x, y); }
    
/**
 * Makes the vector unit length.
 */
public void normalize()  { double t = getMagnitude(); x /= t; y /= t; }

/**
 * Add the given vector to this.
 */
public void add(Vect aVect)  { x += aVect.x; y += aVect.y; }
    
/**
 * Returns the dot product of the receiver and the given vector.
 */
public double getDotProduct(Vect v2)  { return getDotProduct(x, y, v2.x, v2.y); }

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
    Vect v1 = getMagnitude()==1? this : clone(); v1.normalize();
    
    // Get normalized version of given vector
    Vect v2 = aVect.getMagnitude()==1? aVect : aVect.clone(); v2.normalize();
    
    // Dot of normalized vectors GT 0: angle<90deg, EQ 0: angle==90deg, LT 0: angle>90deg
    double dot = v1.getDotProduct(v2);
    
    // Return whether angle is less than zero (or equal zero for perpendicular)
    return dot<0 || (dot==0 && includePerpendiculars);
}
    
/**
 * Returns the angle between the receiver and the given vector.
 */
public double getAngleBetween(Vect aVect)  { return getAngleBetween(x, y, aVect.x, aVect.y); }
    
/**
 * Makes this receiver point in the opposite direction.
 */
public void negate()  { x = -x; y = -y; }
    
/**
 * Transforms this vector by the given transform.
 */
public void transform(Transform aTrans)
{
    Point p1 = aTrans.transform(0, 0);
    Point p2 = aTrans.transform(x, y);
    x = p2.x - p1.x; y = p2.y - p1.y;
}

/**
 * Standard clone implementation.
 */
public Vect clone()  { return new Vect(x,y); }

/**
 * Returns a string representation of the vector.
 */
public String toString()  { return "Vector [ " + StringUtils.toString(x) + " " + StringUtils.toString(y) + " ]"; }

/**
 * Returns the magnitude of a vector.
 */
public static double getMagnitude(double aX, double aY)  { return Math.sqrt(aX*aX + aY*aY); }
    
/**
 * Returns the dot product of two vectors.
 */
public static double getDotProduct(double aX, double aY, double bX, double bY)  { return aX*bX + aY*bY; }

/**
 * Returns the angle between the receiver and the given vector.
 */
public static double getAngleBetween(double aX, double aY, double bX, double bY)
{
    double m1 = getMagnitude(aX, aY);
    double m2 = getMagnitude(bX, bY);
    double m3 = m1*m2;
    return Math.acos(getDotProduct(aX, aY, bX, bY)/m3);
}

}