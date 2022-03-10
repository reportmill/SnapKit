/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.util.MathUtils;

/**
 * This class supports various 3D sorting functionality.
 */
public class Sort3D {

    // Constants for comparison/ordering of Path3Ds
    public static final int ORDER_BACK_TO_FRONT = -1;
    public static final int ORDER_FRONT_TO_BACK = 1;
    public static final int ORDER_SAME = 0;
    public static final int ORDER_INEDETERMINATE = 2;

    /**
     * Compares two paths.
     *
     *    Returns -1 (Back_to_Front) if path1 should be painted first
     *    Returns  1 (Front_to_Back) if path2 should be painted first
     *    Returns 0 (Same) if either path can be drawn first
     *
     *    1. Do the Z extents not overlap? Return Z order
     *    2. Do the X/Y extents not overlap? Return Z order
     *    3. Is P entirely on the opposite side of Q’s plane from the viewpoint?
     *    4. Is Q entirely on the same side of P ’s plane as the viewpoint?
     *    5. Do the projections of the polygons not overlap?
     */
    public static int comparePath3Ds(Path3D path1, Path3D path2)
    {
        // If all path1 points are behind all path2 points, return BACK_TO_FRONT
        if (path1.getMaxZ() <= path2.getMinZ())
            return ORDER_BACK_TO_FRONT;

        // If all path1 points are in front of all path2 points, return FRONT_TO_BACK
        if (path1.getMinZ() >= path2.getMaxZ())
            return ORDER_FRONT_TO_BACK;

        // Get simple Z min order
        int compZ = comparePath3D_MinZs(path1, path2);

        // If no X/Y overlap, return MinZ order
        if (path1.getMaxX() <= path2.getMinX() || path1.getMinX() >= path2.getMaxX() ||
            path1.getMaxY() <= path2.getMinY() || path1.getMinY() >= path2.getMaxY()) {
            return compZ;
        }

        // Get ordering based on whether path (2) points are in front or behind path (1) plane
        int comp1 = comparePath3D_Planes(path1, path2);
        int comp2 = comparePath3D_Planes(path2, path1);

        // If both paths find each other in front or back, just return Z min order (they probably share a side)
        if (comp1 == comp2 && comp1 != ORDER_INEDETERMINATE)
            return compZ;

        // If all path2 points in front or back of path1, return that order
        if (comp1 == ORDER_BACK_TO_FRONT || comp1 == ORDER_FRONT_TO_BACK)
            return comp1;

        // If path1/path2 points are coplanar, return MinZ order
        if (comp1 == ORDER_SAME)
            return compZ;

        // If all path1 points in front or back of path2, return reverse order
        if (comp2 == ORDER_BACK_TO_FRONT)
            return ORDER_FRONT_TO_BACK;
        if (comp2 == ORDER_FRONT_TO_BACK)
            return ORDER_BACK_TO_FRONT;

        // This should never happen
        System.err.println("Path3D.comparePath3Ds: Sort fail.");
        return compZ;
    }

    /**
     * Returns whether path1/path are ordered BACK_TO_FRONT OR FRONT_TO_BACK.
     * Returns ORDER_SAME if the two paths are coplanar, or INDETERMINATE if they intersect.
     */
    private static int comparePath3D_Planes(Path3D path1, Path3D path2)
    {
        // Iterate over path points, get distance for each to plane, if distance ever flips sign
        double path2Dist = 0;
        for (int i = 0, iMax = path2.getPointCount(); i < iMax; i++) {

            // Get distance from path point to plane
            Point3D point = path2.getPoint(i);
            double pointDist = getDistanceFromPathPlaneToPoint(path1, point);

            // If negligible distance, assume point is on path1 plane and skip
            if (MathUtils.equalsZero(pointDist))
                continue;

            // If ref distance not yet set, set
            if (path2Dist == 0)
                path2Dist = pointDist;

            // If distance from loop point is opposite side of path plane (sign flipped), return indeterminate
            boolean pointsOnBothSidesOfPlane = pointDist != 0 && pointDist * path2Dist < 0;
            if (pointsOnBothSidesOfPlane)
                return Sort3D.ORDER_INEDETERMINATE;
        }

        // If path1 to path2 distance is positive, return BACK_TO_FRONT
        if (path2Dist > 0)
            return Sort3D.ORDER_BACK_TO_FRONT;

        // If path1 to path2 distance is negative, return FRONT_TO_BACK
        if (path2Dist < 0)
            return Sort3D.ORDER_FRONT_TO_BACK;

        // Planes are co-planar
        return Sort3D.ORDER_SAME;
    }

    /**
     * Returns the distance from a point to the plane of this path.
     */
    private static double getDistanceFromPathPlaneToPoint(Path3D aPath3D, Point3D aPoint)
    {
        // Get plane normal
        Vector3D planeNormal = aPath3D.getNormal();

        // Get vector from plane point to given point
        Point3D planePoint = aPath3D.getBoundsCenter();
        double vx = aPoint.x - planePoint.x;
        double vy = aPoint.y - planePoint.y;
        double vz = aPoint.z - planePoint.z;

        // Distance is just the length of the projection of points vector onto normal vector (v dot n)
        double dist = vx * planeNormal.x + vy * planeNormal.y + vz * planeNormal.z;
        return Math.abs(dist) < .001 ? 0 : dist;
    }

    /**
     * Compares given paths MinZ values.
     */
    public static int comparePath3D_MinZs(Path3D path1, Path3D path2)
    {
        double z0 = path1.getMinZ();
        double z1 = path2.getMinZ();
        return z0 < z1 ? Sort3D.ORDER_BACK_TO_FRONT : z1 < z0 ? Sort3D.ORDER_FRONT_TO_BACK : 0;
    }
}
