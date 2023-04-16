/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.List;

/**
 * This class provides SegmentPath utility methods to do Constructive Area Geometry (CAG) operations,
 * like add, subtract, intersect shapes.
 */
public class SegmentPathCAG {

    /**
     * Returns the intersection shape of two shapes.
     */
    public static Shape addShapes(Shape aShape1, Shape aShape2)
    {
        // Simple case: If shapes don't intersect, just add them and return
        if (!aShape1.intersects(aShape2)) {
            Path2D path = new Path2D(aShape1);
            path.appendShape(aShape2);
            return path;
        }

        // Simple case: If either shape contains other, return outer shape
        if (aShape1.contains(aShape2))
            return aShape1;
        if (aShape2.contains(aShape1))
            return aShape2;

        // Create SegmentPaths for given shapes and new shape
        SegmentPath shape1 = new SegmentPath(aShape1);
        SegmentPath shape2 = new SegmentPath(aShape2);
        SegmentPath shape3 = new SegmentPath();

        // Split segments in shapes so each contains endpoint at every crossing
        shape1.splitIntersectingSegmentsAtIntersectionPoints(shape2);

        // Find first segment of shape1 outside shape2
        Segment seg = shape1.getFirstSegOutside(shape2);
        if (seg == null) { // Should never happen
            System.err.println("SegmentPathCAG.addShapes: No intersections!"); return aShape1; }

        // Iterate over segments to find those outside other shape and add to new shape
        while (seg != null) {

            // Add segment to new shape
            shape3.addSeg(seg);

            // Search SegmentPaths for next outside segment
            Segment nextSeg = getNextSegOutside(shape1, shape2, shape3, seg);

            // If not found, swap order to search second SegmentPath
            if (nextSeg == null) {
                SegmentPath swap = shape1; shape1 = shape2; shape2 = swap;
                nextSeg = getNextSegOutside(shape1, shape2, shape3, seg);
            }

            // Update seg and add to list if non-null
            seg = nextSeg;

            // Check to see if things are out of hand (should probably go)
            if (shape3.getSegCount() > 50) {
                seg = null; System.err.println("SegmentPathCAG: too many segs"); }
        }

        // Return path for segments list
        return new Path2D(shape3);
    }

    /**
     * Returns the area of the first shape minus the overlapping area of second shape.
     */
    public static Shape subtractShapes(Shape aShape1, Shape aShape2)
    {
        // Simple case: If shapes don't intersect, return shape 1 (should be null)
        if (!aShape1.intersects(aShape2))
            return aShape1;

        // If either shape contains the other, return concatenated shape
        if (aShape1.contains(aShape2)) {
            Path2D path = new Path2D(aShape1);
            path.appendShape(aShape2);
            return path;
        }
        if (aShape2.contains(aShape1)) {
            Path2D path = new Path2D(aShape2);
            path.appendShape(aShape1);
            return path;
        }

        // Create SegmentPaths for given shapes and new shape
        SegmentPath shape1 = new SegmentPath(aShape1), refShp = shape1;
        SegmentPath shape2 = new SegmentPath(aShape2);
        SegmentPath shape3 = new SegmentPath();

        // Split segments in shapes so each contains endpoint at every crossing
        shape1.splitIntersectingSegmentsAtIntersectionPoints(shape2);

        // Find first segment on perimeter of shape1 and shape2
        Segment seg = shape1.getFirstSegOutside(shape2);
        if (seg == null) { // Should never happen
            System.err.println("SegmentPathCAG.subtractShapes: No intersections!"); return aShape1; }

        // Iterate over segments to find those with endpoints in opposing shape and add to new shape
        while (seg != null) {

            // Add segment to new list
            shape3.addSeg(seg);

            // Search SegmentPaths for next subtract segment
            Segment nextSeg = getNextSegSubtract(refShp, shape1, shape2, shape3, seg);

            // If not found, swap order to search second SegmentPath
            if (nextSeg == null) {
                SegmentPath swap = shape1; shape1 = shape2; shape2 = swap;
                nextSeg = getNextSegSubtract(refShp, shape1, shape2, shape3, seg);
            }

            // Update seg and add to list if non-null
            seg = nextSeg;

            // Check to see if things are out of hand (should probably go)
            if (shape3.getSegCount() > 50) {
                seg = null; System.err.println("SegmentPathCAG: too many segs"); }
        }

        // Return path for segments list
        return new Path2D(shape3);
    }

    /**
     * Returns the intersection shape of two shapes.
     */
    public static Shape intersectShapes(Shape aShape1, Shape aShape2)
    {
        // Simple cases
        if (aShape1 instanceof Rect && aShape2 instanceof Rect)
            return ((Rect) aShape1).getIntersectRect((Rect)aShape2);
        if (!aShape1.intersects(aShape2))
            return new Rect();
        if (aShape1.contains(aShape2))
            return aShape2;
        if (aShape2.contains(aShape1))
            return aShape1;

        // Create SegmentPaths for given shapes and new shape
        SegmentPath shape1 = new SegmentPath(aShape1);
        SegmentPath shape2 = new SegmentPath(aShape2);
        SegmentPath shape3 = new SegmentPath();

        // Split segments in shape1 & shape2
        shape1.splitIntersectingSegmentsAtIntersectionPoints(shape2);

        // Find first segment contained by both shape1 and shape2
        Segment seg = shape1.getFirstSegInside(shape2);
        if (seg == null) { // Should never happen
            System.err.println("SegmentPathCAG.intersectShapes: No points!"); return aShape1; }

        // Iterate over segments to find those with endpoints in opposing shape and add to new shape
        SegmentPath owner = shape1;
        SegmentPath opposingShape = shape2;
        while (seg != null) {

            // Add segment to new list
            shape3.addSeg(seg);

            // Get segment at end point for current seg shape
            List<Segment> segs = owner.getSegmentsThatStartOrEndAtSegmentEndPoint(seg);
            Segment nextSeg = null;
            for (Segment sg : segs) {
                if (opposingShape.contains(sg.getX1(), sg.getY1()) && !shape3.hasSeg(sg)) {
                    nextSeg = sg; break; }
            }

            // If not found, look for seg from other shape
            if (nextSeg == null) {
                segs = opposingShape.getSegmentsThatStartOrEndAtSegmentEndPoint(seg);
                for (Segment sg : segs) {
                    if (owner.contains(sg.getX1(), sg.getY1()) && !shape3.hasSeg(sg)) {
                        nextSeg = sg;
                        owner = opposingShape; opposingShape = opposingShape==shape1? shape2 : shape1;
                        break;
                    }
                }
            }

            // Update seg and add to list if non-null
            seg = nextSeg;

            // Check to see if things are out of hand (should probably go)
            if (shape3.getSegCount() > 30) {
                seg = null; System.err.println("SegmentPathCAG: too many segs"); }
        }

        // Return path for segments list
        return new Path2D(shape3);
    }

    /**
     * Returns the next segment outside of both SegmentPath.
     */
    private static Segment getNextSegOutside(SegmentPath aShp1, SegmentPath aShp2, SegmentPath aShp3, Segment aSeg)
    {
        List <Segment> segs = aShp1.getSegmentsThatStartOrEndAtSegmentEndPoint(aSeg);
        for (Segment seg : segs) {
            boolean outside = !aShp2.containsSegMid(seg) || aShp2.hasSeg(seg);
            if (outside && !aShp3.hasSeg(seg))
                return seg;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the next segment outside of both SegmentPaths.
     */
    private static Segment getNextSegSubtract(SegmentPath aRefShp, SegmentPath aShp1, SegmentPath aShp2, SegmentPath aShp3, Segment aSeg)
    {
        List<Segment> segs = aShp1.getSegmentsThatStartOrEndAtSegmentEndPoint(aSeg);
        for (Segment seg : segs) {
            boolean b1 = (aShp1 == aRefShp) != aShp2.containsSegMid(seg);
            if (b1 && !aShp3.hasSeg(seg))
                return seg;
        }

        // Return not found
        return null;
    }
}
