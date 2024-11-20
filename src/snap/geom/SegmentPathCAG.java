/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.ListUtils;
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
        if (!aShape1.intersectsShape(aShape2)) {
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
        SegmentPath mainPath = new SegmentPath(aShape1);
        SegmentPath otherPath = new SegmentPath(aShape2);
        SegmentPath newPath = new SegmentPath();
        int maxSegments = mainPath.getSegCount() + otherPath.getSegCount() + 10;

        // Split segments in shapes so each contains endpoint at every crossing
        mainPath.splitIntersectingSegmentsAtIntersectionPoints(otherPath);

        // Find first segment of shape1 outside shape2
        Segment loopSeg = mainPath.getFirstSegOutside(otherPath);
        if (loopSeg == null) { // Should never happen
            System.err.println("SegmentPathCAG.addShapes: No intersections!"); return aShape1; }

        // Iterate over segments to find those outside other shape and add to new shape
        while (loopSeg != null) {

            // Add segment to new path - stop if things are going wrong
            newPath.addSeg(loopSeg);
            if (newPath.getSegCount() > maxSegments) {
                System.err.println("SegmentPathCAG: too many segs"); break; }

            // Search SegmentPaths for next outside segment
            Segment nextSeg = getNextSegOutside(mainPath, loopSeg, otherPath, newPath);

            // If not found, swap order to search second SegmentPath
            if (nextSeg == null) {
                SegmentPath swap = mainPath; mainPath = otherPath; otherPath = swap;
                nextSeg = getNextSegOutside(mainPath, loopSeg, otherPath, newPath);
            }

            // Update seg
            loopSeg = nextSeg;
        }

        // Return path for segments list
        return new Path2D(newPath);
    }

    /**
     * Returns the area of the first shape minus the overlapping area of second shape.
     */
    public static Shape subtractShapes(Shape aShape1, Shape aShape2)
    {
        // Simple case: If shapes don't intersect, return shape 1 (should be null)
        if (!aShape1.intersectsShape(aShape2))
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
        SegmentPath mainPath = new SegmentPath(aShape1), refPath = mainPath;
        SegmentPath otherPath = new SegmentPath(aShape2);
        SegmentPath newPath = new SegmentPath();
        int maxSegments = mainPath.getSegCount() + otherPath.getSegCount() + 10;

        // Split segments in shapes so each contains endpoint at every crossing
        mainPath.splitIntersectingSegmentsAtIntersectionPoints(otherPath);

        // Find first segment on perimeter of shape1 and shape2
        Segment loopSeg = mainPath.getFirstSegOutside(otherPath);
        if (loopSeg == null) { // Should never happen
            System.err.println("SegmentPathCAG.subtractShapes: No intersections!"); return aShape1; }

        // Iterate over segments to find those with endpoints in opposing shape and add to new shape
        while (loopSeg != null) {

            // Add segment to new path - stop if things are going wrong
            newPath.addSeg(loopSeg);
            if (newPath.getSegCount() > maxSegments) {
                System.err.println("SegmentPathCAG: too many segs"); break; }

            // Search SegmentPaths for next subtract segment
            Segment nextSeg = getNextSegSubtract(mainPath, loopSeg, otherPath, refPath, newPath);

            // If not found, swap order to search second SegmentPath
            if (nextSeg == null) {
                SegmentPath swap = mainPath; mainPath = otherPath; otherPath = swap;
                nextSeg = getNextSegSubtract(mainPath, loopSeg, otherPath, refPath, newPath);
            }

            // Update seg
            loopSeg = nextSeg;
        }

        // Return path for segments list
        return new Path2D(newPath);
    }

    /**
     * Returns the intersection shape of two shapes.
     */
    public static Shape intersectShapes(Shape aShape1, Shape aShape2)
    {
        // Simple cases
        if (aShape1 instanceof Rect && aShape2 instanceof Rect)
            return ((Rect) aShape1).getIntersectRect((Rect)aShape2);
        if (!aShape1.intersectsShape(aShape2))
            return new Rect();
        if (aShape1.contains(aShape2))
            return aShape2;
        if (aShape2.contains(aShape1))
            return aShape1;

        // Create SegmentPaths for given shapes and new shape
        SegmentPath mainPath = new SegmentPath(aShape1);
        SegmentPath otherPath = new SegmentPath(aShape2);
        SegmentPath newPath = new SegmentPath();
        int maxSegments = mainPath.getSegCount() + otherPath.getSegCount() + 10;

        // Split segments in segPaths so all intersections are segment end points
        mainPath.splitIntersectingSegmentsAtIntersectionPoints(otherPath);

        // Find first segment contained by both segPaths
        Segment loopSeg = mainPath.getFirstSegInside(otherPath);
        if (loopSeg == null) { // Should never happen
            System.err.println("SegmentPathCAG.intersectShapes: No points!");
            return aShape1;
        }

        // Iterate over segments to find those with endpoints in opposing shape and add to new shape
        while (loopSeg != null) {

            // Add segment to new path - stop if things are going wrong
            newPath.addSeg(loopSeg);
            if (newPath.getSegCount() > maxSegments) {
                System.err.println("SegmentPathCAG: too many segs"); break; }

            // Get the next segment inside other path
            Segment nextSeg = getNextSegInside(mainPath, loopSeg, otherPath, newPath);

            // If not found, look for seg from other path (if found, swap paths)
            if (nextSeg == null) {
                nextSeg = getNextSegInside(otherPath, loopSeg, mainPath, newPath);
                if (nextSeg != null) {
                    SegmentPath swap = mainPath; mainPath = otherPath; otherPath = swap; }
            }

            // Update seg
            loopSeg = nextSeg;
        }

        // Return path for segments list
        return new Path2D(newPath);
    }

    /**
     * Returns the next segment outside other path (but not in new path).
     */
    private static Segment getNextSegOutside(SegmentPath mainPath, Segment prevSeg, SegmentPath otherPath, SegmentPath newPath)
    {
        List<Segment> segs = mainPath.getSegmentsThatStartOrEndAtSegmentEndPoint(prevSeg);
        return ListUtils.findMatch(segs, seg -> isSegOutsideSegmentPaths(otherPath, seg, newPath));
    }

    /**
     * Returns the next segment inside other path (but not in new path).
     */
    private static Segment getNextSegInside(SegmentPath mainPath, Segment prevSeg, SegmentPath otherPath, SegmentPath newPath)
    {
        List<Segment> segs = mainPath.getSegmentsThatStartOrEndAtSegmentEndPoint(prevSeg);
        return ListUtils.findMatch(segs, seg -> otherPath.containsSegMid(seg) && !newPath.hasSeg(seg));
    }

    /**
     * Returns the next segment outside both SegmentPaths.
     */
    private static Segment getNextSegSubtract(SegmentPath mainPath, Segment prevSeg, SegmentPath otherPath, SegmentPath refPath, SegmentPath newPath)
    {
        List<Segment> segs = mainPath.getSegmentsThatStartOrEndAtSegmentEndPoint(prevSeg);
        for (Segment seg : segs) {
            boolean b1 = (mainPath == refPath) != otherPath.containsSegMid(seg);
            if (b1 && !newPath.hasSeg(seg))
                return seg;
        }

        // Return not found
        return null;
    }

    /**
     * Returns whether segment is outside given SegmentPaths.
     */
    private static boolean isSegOutsideSegmentPaths(SegmentPath segmentPath, Segment seg, SegmentPath newPath)
    {
        boolean outside = !segmentPath.containsSegMid(seg) || segmentPath.hasSeg(seg);
        return outside && !newPath.hasSeg(seg);
    }
}
