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
            theShapes.sort((s1, s2) -> Sort3D.compareShapesForPaintOrder(s1, s2));
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
     *    2. Is Shape2 entirely in front of Shape1’s plane?
     *    3. Is Shape1 entirely in front of Shape2’s plane?
     *    4. If best guess, return min Z order.
     */
    public static int compareShapesForPaintOrder(FacetShape shape1, FacetShape shape2)
    {
        // If all shape1 points are behind all shape2 points, return BACK_TO_FRONT (and vice versa)
        if (shape1.getMaxZ() <= shape2.getMinZ())
            return ORDER_BACK_TO_FRONT;
        if (shape2.getMaxZ() <= shape1.getMinZ())
            return ORDER_FRONT_TO_BACK;

        // Get ordering based on whether shape2 points are in front or behind shape1 points
        int comp1 = compareShapePlanes(shape1, shape2);
        int comp2 = compareShapePlanes(shape2, shape1);

        // If plane comparisons differ, we may have definitive result
        if (comp1 != comp2) {
            if (comp1 == ORDER_BACK_TO_FRONT || comp1 == ORDER_FRONT_TO_BACK)
                return comp1;
            if (comp2 == ORDER_BACK_TO_FRONT || comp2 == ORDER_FRONT_TO_BACK)
                return -comp2;
        }

        // Get simple min Z order (if same, use max Z)
        int zOrder = compareShapeMinZs(shape1, shape2);
        if (zOrder == ORDER_SAME)
            zOrder = compareShapeMaxZs(shape1, shape2);
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

            // Get distance from shape point to plane - if zero distance, just skip (point is on path1 plane)
            Point3D shape2Point = shape2.getPoint(i);
            double pointDist = getDistanceFromShapePlaneToPoint(shape1, shape2Point);
            if (MathUtils.equalsZero(pointDist))
                continue;

            // If reference distance not yet set, set
            if (distToShape2 == 0)
                distToShape2 = pointDist;

            // If distance from loop point is opposite side of shape plane (sign flipped), return indeterminate
            else {
                boolean pointsOnBothSidesOfPlane = pointDist * distToShape2 < 0;
                if (pointsOnBothSidesOfPlane)
                    return ORDER_INDETERMINATE;
            }
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
        if (MathUtils.equals(z0, z1))
            return ORDER_SAME;
        return Double.compare(z0, z1); // z0 < z1 ? ORDER_BACK_TO_FRONT : z1 < z0 ? ORDER_FRONT_TO_BACK : 0;
    }

    /**
     * Compares given shapes MinZ values.
     */
    public static int compareShapeMaxZs(FacetShape shape1, FacetShape shape2)
    {
        double z0 = shape1.getMaxZ();
        double z1 = shape2.getMaxZ();
        if (MathUtils.equals(z0, z1))
            return ORDER_SAME;
        return Double.compare(z0, z1); // z0 < z1 ? ORDER_BACK_TO_FRONT : z1 < z0 ? ORDER_FRONT_TO_BACK : 0;
    }
}
