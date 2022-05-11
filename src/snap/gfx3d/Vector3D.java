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
     * Constructor.
     */
    public Vector3D()  { }

    /**
     * Constructor for the given vector components.
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
     * Constructor for vector from given point to second given point.
     */
    public Vector3D(Point3D p0, Point3D p1)
    {
        x = p1.x - p0.x;
        y = p1.y - p0.y;
        z = p1.z - p0.z;
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
     * Scales this vector by given factor.
     */
    public void scale(double aScale)
    {
        x *= aScale;
        y *= aScale;
        z *= aScale;
    }

    /**
     * Makes this receiver point in the opposite direction.
     */
    public void negate()
    {
        x = x != 0 ? -x : 0;
        y = y != 0 ? -y : 0;
        z = z != 0 ? -z : 0;
    }

    /**
     * Returns the vector perpendicular to the receiver and the given vector.
     */
    public Vector3D getCrossProduct(Vector3D v2)
    {
        double a = y * v2.z - v2.y * z;
        double b = z * v2.x - v2.z * x;
        double c = x * v2.y - v2.x * y;
        return new Vector3D(a, b, c);
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
     * Constructor for vector from given point to second given point.
     */
    public void setVector(double aX, double aY, double aZ)
    {
        x = aX;
        y = aY;
        z = aZ;
    }

    /**
     * Constructor for vector from given point to second given point.
     */
    public void setVectorBetweenPoints(Point3D p0, Point3D p1)
    {
        x = p1.x - p0.x;
        y = p1.y - p0.y;
        z = p1.z - p0.z;
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

    /**
     * Returns the normal of given points. See Path3D.getNormal().
     */
    public static Vector3D getNormalForPoints(Vector3D aVect, Point3D ... thePoints)
    {
        aVect.x = aVect.y = aVect.z = 0;
        for (int i = 0, iMax = thePoints.length; i < iMax; i++) {
            Point3D thisPoint = thePoints[i];
            Point3D nextPoint = thePoints[(i + 1) % iMax];
            aVect.x += (thisPoint.y - nextPoint.y) * (thisPoint.z + nextPoint.z);
            aVect.y += (thisPoint.z - nextPoint.z) * (thisPoint.x + nextPoint.x);
            aVect.z += (thisPoint.x - nextPoint.x) * (thisPoint.y + nextPoint.y);
        }

        // Normalize the result and swap sign so it matches right hand rule
        aVect.normalize(); // if (Renderer.FRONT_FACE_IS_CW) aVect.negate();
        return aVect;
    }

    /**
     * Returns the normal of given points. See Path3D.getNormal().
     */
    public static Vector3D getNormalForPoints3fv(Vector3D aVect, float[] pointsArray, int pointCount)
    {
        aVect.x = aVect.y = aVect.z = 0;
        for (int i = 0; i < pointCount; i++) {
            int pointsArrayIndex = i * 3;
            float thisX = pointsArray[pointsArrayIndex + 0];
            float thisY = pointsArray[pointsArrayIndex + 1];
            float thisZ = pointsArray[pointsArrayIndex + 2];
            int nextIndex = (i + 1) % pointCount * 3;
            float nextX = pointsArray[nextIndex];
            float nextY = pointsArray[nextIndex + 1];
            float nextZ = pointsArray[nextIndex + 2];
            aVect.x += (thisY - nextY) * (thisZ + nextZ);
            aVect.y += (thisZ - nextZ) * (thisX + nextX);
            aVect.z += (thisX - nextX) * (thisY + nextY);
        }

        // Normalize the result
        aVect.normalize();
        return aVect;
    }
}