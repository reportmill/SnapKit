/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.util.MathUtils;
import java.util.List;

/**
 * This class supports sorting Shape3Ds (FacetShape) from back to front.
 */
public class Sort3D {

    // Constants for comparison/ordering of Path3Ds
    public static final int ORDER_BACK_TO_FRONT = -1;
    public static final int ORDER_FRONT_TO_BACK = 1;
    public static final int ORDER_SAME = 0;
    public static final int ORDER_INDETERMINATE = 2;

    public static void sortShapesBackToFront(List<FacetShape> theShapes)
    {
        try {
            theShapes.sort((s1, s2) -> Sort3D.compareShapeMinZs(s1, s2));
            theShapes.sort((s1, s2) -> Sort3D.compareFacetShapes(s1, s2));
        }
        catch (Exception e) {
            System.err.println("Sort3D.sortShapesBackToFront: Sort failed: " + e);
        }
    }

    /**
     * Compares two facet shapes.
     *    Returns -1 (Back_to_Front) if shape1 should be painted first
     *    Returns  1 (Front_to_Back) if shape2 should be painted first
     *    Returns 0 (Same) if either shape can be drawn first
     *
     *    1. Do the Z extents not overlap? Return Z order
     *    2. Do the X/Y extents not overlap? Return Z order
     *    3. Is P entirely on the opposite side of Q’s plane from the viewpoint?
     *    4. Is Q entirely on the same side of P ’s plane as the viewpoint?
     *    5. Do the projections of the polygons not overlap?
     */
    public static int compareFacetShapes(FacetShape shape1, FacetShape shape2)
    {
        // If all shape1 points are behind all shape2 points, return BACK_TO_FRONT (and vice versa)
        if (shape1.getMaxZ() <= shape2.getMinZ())
            return ORDER_BACK_TO_FRONT;
        if (shape2.getMaxZ() <= shape1.getMinZ())
            return ORDER_FRONT_TO_BACK;

        // Get simple min Z order (if same, use max Z)
        int zOrder = compareShapeMinZs(shape1, shape2);
        if (zOrder == ORDER_SAME)
            zOrder = compareShapeMaxZs(shape1, shape2);

        // Get ordering based on whether shape2 points are in front or behind shape1 points
        int comp1 = compareShapePlanes(shape1, shape2);
        int comp2 = compareShapePlanes(shape2, shape1);

        // If both shapes find each other in front or back, just return Z min order (they probably share a side)
        if (comp1 == comp2 && comp1 != ORDER_INDETERMINATE)
            return zOrder;

        // If all shape2 points in front or back of shape1, return that order
        if (comp1 == ORDER_BACK_TO_FRONT || comp1 == ORDER_FRONT_TO_BACK)
            return comp1;

        // If shape1/shape2 points are coplanar, return MinZ order
        if (comp1 == ORDER_SAME)
            return zOrder;

        // If all shape1 points in front or back of shape2, return reverse order
        if (comp2 == ORDER_BACK_TO_FRONT)
            return ORDER_FRONT_TO_BACK;
        if (comp2 == ORDER_FRONT_TO_BACK)
            return ORDER_BACK_TO_FRONT;

        // If no X/Y overlap, return MinZ order
        boolean noOverlapX = shape1.getMaxX() <= shape2.getMinX() || shape1.getMinX() >= shape2.getMaxX();
        boolean noOverlapXY = noOverlapX || shape1.getMaxY() <= shape2.getMinY() || shape1.getMinY() >= shape2.getMaxY();
        if (noOverlapXY)
            return zOrder;

        // This should never happen
        System.err.println("Sort3D.compareFacetShapes: Sort fail.");
        return zOrder;
    }

    /**
     * Returns whether (facet) shapes are ordered BACK_TO_FRONT OR FRONT_TO_BACK.
     * Returns ORDER_SAME if shapes are coplanar.
     * Returns INDETERMINATE if shape2 points lie on both sides of shape1 plane (straddle).
     */
    private static int compareShapePlanes(FacetShape shape1, FacetShape shape2)
    {
        int pointCount = shape2.getPointCount();
        double distToShape2 = 0;

        // Iterate over shape points to check distance for each to plane
        for (int i = 0; i < pointCount; i++) {

            // Get distance from shape point to plane
            Point3D shape2Point = shape2.getPoint(i);
            double pointDist = getDistanceFromShapePlaneToPoint(shape1, shape2Point);

            // If negligible distance, assume point is on path1 plane and skip
            if (MathUtils.equalsZero(pointDist))
                continue;

            // If ref distance not yet set, set
            if (distToShape2 == 0)
                distToShape2 = pointDist;

            // If distance from loop point is opposite side of shape plane (sign flipped), return indeterminate
            boolean pointsOnBothSidesOfPlane = pointDist != 0 && pointDist * distToShape2 < 0;
            if (pointsOnBothSidesOfPlane)
                return ORDER_INDETERMINATE;
        }

        // If positive distance, return BACK_TO_FRONT, if negative FRONT_TO_BACK, otherwise SAME (co-planar)
        if (distToShape2 > 0)
            return ORDER_BACK_TO_FRONT;
        if (distToShape2 < 0)
            return ORDER_FRONT_TO_BACK;
        return ORDER_SAME;
    }

    /**
     * Returns the distance from a given shape's plane to given point.
     */
    private static double getDistanceFromShapePlaneToPoint(FacetShape aShape, Point3D aPoint)
    {
        // Get plane normal
        Vector3D planeNormal = aShape.getNormal();

        // Get vector from plane point to given point
        Point3D planePoint = aShape.getBoundsCenter();
        double vx = aPoint.x - planePoint.x;
        double vy = aPoint.y - planePoint.y;
        double vz = aPoint.z - planePoint.z;

        // Distance is just the length of the projection of points vector onto normal vector (v dot n)
        double dist = vx * planeNormal.x + vy * planeNormal.y + vz * planeNormal.z;
        return Math.abs(dist) < .001 ? 0 : dist;
    }

    /**
     * Compares given shapes MinZ values.
     */
    public static int compareShapeMinZs(FacetShape shape1, FacetShape shape2)
    {
        double z0 = shape1.getMinZ();
        double z1 = shape2.getMinZ();
        return Double.compare(z0, z1); // z0 < z1 ? ORDER_BACK_TO_FRONT : z1 < z0 ? ORDER_FRONT_TO_BACK : 0;
    }

    /**
     * Compares given shapes MinZ values.
     */
    public static int compareShapeMaxZs(FacetShape shape1, FacetShape shape2)
    {
        double z0 = shape1.getMaxZ();
        double z1 = shape2.getMaxZ();
        return Double.compare(z0, z1); // z0 < z1 ? ORDER_BACK_TO_FRONT : z1 < z0 ? ORDER_FRONT_TO_BACK : 0;
    }
}
