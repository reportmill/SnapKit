/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.util.MathUtils;

/**
 * This class represents a 3D vector.
 */
public class Vector3D implements Cloneable {
    
    // X Y Z components
    public double x, y, z;
    
    /**
     * Creates a new vector from the given coords.
     */
    public Vector3D(double x, double y, double z)  { this.x = x; this.y = y; this.z = z; }

    /**
     * Creates a new vector from the given vector.
     */
    public Vector3D(Vector3D aVector)
    {
        x = aVector.x;
        y = aVector.y;
        z = aVector.z;
    }

    /**
     * Returns the magnitude of the vector.
     */
    public double getMagnitude()
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Makes the vector unit length.
     */
    public Vector3D normalize()
    {
        double mag = getMagnitude();
        x /= mag; y /= mag; z /= mag;
        return this;
    }

    /**
     * Add the given vector to this.
     */
    public void add(Vector3D aVector)
    {
        x += aVector.x;
        y += aVector.y;
        z += aVector.z;
    }

    /**
     * Returns the vector perpendicular to the receiver and the given vector.
     */
    public Vector3D getCrossProduct(Vector3D v2)
    {
        // Get cross components
        double a = y * v2.z - v2.y * z;
        double b = z * v2.x - v2.z * x;
        double c = x * v2.y - v2.x * y;

        // Return new vecotr with components (normalized)
        Vector3D cross = new Vector3D(a, b, c);
        cross.normalize();
        return cross;
    }

    /**
     * Returns the dot product of the receiver and the given vector.
     */
    public double getDotProduct(Vector3D v2)
    {
        return x * v2.x + y *v2.y + z * v2.z;
    }

    /**
     * Returns whether given vector is in same general direction of this (with option to include perpendiculars).
     */
    public boolean isAligned(Vector3D aVector, boolean includePerpendiculars)
    {
        return !isAway(aVector, !includePerpendiculars);
    }

    /**
     * Returns whether given vector is pointing away from the direction of this (with option to include perpendiculars).
     */
    public boolean isAway(Vector3D aVector, boolean includePerpendiculars)
    {
        // Get normalized version of this vector
        Vector3D v1 = getMagnitude() == 1 ? this : new Vector3D(this).normalize();

        // Get normalized version of given vector
        Vector3D v2 = aVector.getMagnitude() == 1 ? aVector : new Vector3D(aVector).normalize();

        // Dot of normalized vectors GT 0: angle<90deg, EQ 0: angle==90deg, LT 0: angle>90deg
        double dot = v1.getDotProduct(v2);

        // Return whether angle is less than zero (or equal zero for perpendicular)
        return dot < 0 || (dot == 0 && includePerpendiculars);
    }

    /**
     * Returns the angle between the receiver and the given vector.
     */
    public double getAngleBetween(Vector3D aVector)
    {
        double m1 = getMagnitude();
        double m2 = aVector.getMagnitude();
        double m3 = m1 * m2;
        double dot = getDotProduct(aVector);
        double angleRad = Math.acos(dot / m3);
        return Math.toDegrees(angleRad);
    }

    /**
     * Makes this receiver point in the opposite direction.
     */
    public void negate()  { x = -x; y = -y; z = -z; }

    /**
     * Transforms the vector by the given transform3d.
     */
    public Vector3D transform(Transform3D aTransform)
    {
        return aTransform.transform(this);
    }

    /**
     * Standard clone implementation.
     */
    public Vector3D clone()
    {
        try { return (Vector3D) super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        Vector3D v2 = (Vector3D) anObj;
        return equals(x, y, z, v2.x, v2.y, v2.z);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return "Vector3D [" + x + " " + y + " " + z + "]";
    }

    /**
     * Returns whether given vector components are equal.
     */
    public static boolean equals(double v0x, double v0y, double v0z, double v1x, double v1y, double v1z)
    {
        return MathUtils.equals(v0x, v1x) && MathUtils.equals(v0y, v1y) && MathUtils.equals(v0z, v1z);
    }
}