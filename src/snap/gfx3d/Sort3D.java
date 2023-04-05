/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.util.MathUtils;
import java.util.ArrayList;
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

    /**
     * Sorts the given list in back-to-front paint order.
     */
    public static void sortShapesBackToFront(List<FacetShape> theShapes)
    {
        // Just return if no shapes
        if (theShapes.size() == 0) return;

        // Do a simple Z order sort
        theShapes.sort((s1, s2) -> Sort3D.compareShapeMinZs(s1, s2));

        // This was back when I hoped I could compare shapes definitively
        //try { theShapes.sort((s1, s2) -> Sort3D.compareShapesForPaintOrder(s1, s2, true)); }
        //catch (Exception e) { System.err.println("Sort3D.sortShapesBackToFront: Sort failed: " + e); }

        // Create new SortedList seeded with last shape
        List<FacetShape> shapesSorted = new ArrayList<>(theShapes.size());
        FacetShape lastShape = theShapes.remove(theShapes.size() - 1);
        shapesSorted.add(lastShape);

        // Iterate until all original shapes added to SortedShapes
        while (theShapes.size() > 0) {

            // Try to add one sorted shape if possible using definitive sorting (fall back using best guess)
            boolean didAdd = addOneShapeSorted(theShapes, shapesSorted, false);
            if (!didAdd)
                addOneShapeSorted(theShapes, shapesSorted, true);
        }

        // Re-add SortedShapes to Shapes
        theShapes.addAll(shapesSorted);
    }

    /**
     * Iterates over source shapes in given list to try to add shape to sorted shapes list.
     */
    private static boolean addOneShapeSorted(List<FacetShape> theShapes, List<FacetShape> sortedShapes, boolean bestGuess)
    {
        // Iterate over source shapes list and try to find a place for each in SortedShapes
        for (int i = 0, iMax = theShapes.size(); i < iMax; i++) {
            FacetShape shape1 = theShapes.get(i);
            int paintOrder = 0;

            // Iterate over SortedShapes and if source shape is ordered before, add source shape at index and return
            for (int j = 0, jMax = sortedShapes.size(); j < jMax; j++) {

                // Get shape and paint order
                FacetShape shape2 = sortedShapes.get(j);
                paintOrder = compareShapesForPaintOrder(shape1, shape2, bestGuess);

                // If ordered BACK_TO_FRONT, add at SortedShapes index and return
                if (paintOrder == ORDER_BACK_TO_FRONT) {
                    theShapes.remove(i);
                    sortedShapes.add(j, shape1);
                    return true;
                }
            }

            // If last shape was ordered FRONT_TO_BACK or SAME (only if best guess), add shape at end of SortedShapes
            if (paintOrder == ORDER_FRONT_TO_BACK || paintOrder == ORDER_SAME) {
                theShapes.remove(i);
                sortedShapes.add(shape1);
                return true;
            }
        }

        // If at end of rope, just add last shape - not sure this ever happens
        if (bestGuess) {
            System.err.println("Sort3D.addOneShapeSorted: Failed to add sorted shape");
            sortedShapes.add(theShapes.remove(theShapes.size() - 1));
            return true;
        }

        // Return failure
        return false;
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
    public static int compareShapesForPaintOrder(FacetShape shape1, FacetShape shape2, boolean bestGuess)
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

        // If not guessing, return INDETERMINATE
        if (!bestGuess)
            return ORDER_INDETERMINATE;

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
        // A plane is defined by a normal (ABC) and a point on the plane (xyz): Ax + By + Cz + D = 0
        Vector3D normal = aShape.getNormal();
        Point3D planePoint = aShape.getPoint(0);

        // Calculate D from Ax + By + Cz + D = 0
        double Ax = normal.x * planePoint.x;
        double By = normal.y * planePoint.y;
        double Cz = normal.z * planePoint.z;
        double D = -Ax - By - Cz;

        // Distance is Ax + By + Cz + D / NormalMagnitude (magnitude of normal is 1)
        double dist = normal.x * aPoint.x + normal.y * aPoint.y + normal.z * aPoint.z + D;
        return Math.abs(dist) < .01 ? 0 : dist;
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
