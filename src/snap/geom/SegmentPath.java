/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.ListUtils;
import java.util.*;

/**
 * A Shape containing a list of Segment shapes (Line, Quad, Cubic) to facilitate complex shape operations like
 * constructive area geometry (CAG) operations (add, subtract, intersect paths).
 */
public class SegmentPath extends Shape {
    
    // The list of segments
    private List<Segment> _segs;

    // The original shape, if set
    private Shape _origShape;

    /**
     * Constructor.
     */
    public SegmentPath()
    {
        super();
        _segs = new ArrayList<>();
    }

    /**
     * Constructor for given shape.
     */
    public SegmentPath(Shape aShape)
    {
        this();
        appendShape(aShape);
        _origShape = aShape;
    }

    /**
     * Returns the list of segments.
     */
    public List <Segment> getSegs()  { return _segs; }

    /**
     * Returns the number of segments.
     */
    public int getSegCount()  { return _segs.size(); }

    /**
     * Returns the individual segment at given index.
     */
    public Segment getSeg(int anIndex)  { return _segs.get(anIndex); }

    /**
     * Adds the given segment.
     */
    public void addSeg(Segment aSeg)  { addSeg(aSeg, getSegCount()); }

    /**
     * Adds the given segment.
     */
    public void addSeg(Segment aSeg, int anIndex)
    {
        if (aSeg.x0 == aSeg.x1 && aSeg.y0 == aSeg.y1 && getArcLength() == 0) {
            System.err.println("SegmentPath.addSet: trying to add zero length segment");
            return;
        }

        _segs.add(anIndex, aSeg);
        shapeChanged(); _origShape = null;
    }

    /**
     * Removes a given segment.
     */
    public Segment removeSeg(int anIndex)
    {
        Segment seg = _segs.remove(anIndex);
        shapeChanged(); _origShape = null;
        return seg;
    }

    /**
     * Returns whether this SegmentPath contains given point.
     */
    public boolean contains(double aX, double aY)
    {
        // If given point is an endpoint, return true
        if (containsEndPoint(aX, aY))
            return true;

        // If original shape available, use it
        if (_origShape != null)
            return _origShape.contains(aX, aY);

        // Do normal version
        return super.contains(aX, aY);
    }

    /**
     * Returns whether this SegmentPath contains given segment mid point.
     */
    public boolean containsSegMid(Segment aSeg)
    {
        double midX = aSeg.getX(.5);
        double midY = aSeg.getY(.5);
        return contains(midX, midY);
    }

    /**
     * Returns whether given SegmentPath contains given point.
     */
    public boolean containsEndPoint(double x, double y)
    {
        return ListUtils.hasMatch(_segs, seg -> Segment.equals(seg.x1, x) && Segment.equals(seg.y1, y));
    }

    /**
     * Returns whether segment list contains segment (regardless of direction).
     */
    public boolean hasSeg(Segment aSeg)
    {
        return ListUtils.hasMatch(_segs, seg -> seg.matches(aSeg));
    }

    /**
     * Returns the first Segment from this SegmentList outside of given SegmentPath.
     */
    public Segment getFirstSegOutside(SegmentPath aShape)
    {
        return ListUtils.findMatch(_segs, seg -> !aShape.containsSegMid(seg));
    }

    /**
     * Returns the first Segment from this SegmentList inside given SegmentPath.
     */
    public Segment getFirstSegInside(SegmentPath aShape)
    {
        return ListUtils.findMatch(_segs, seg -> aShape.containsSegMid(seg));
    }

    /**
     * Returns the segments from list for end point of given seg.
     */
    public List <Segment> getSegmentsThatStartOrEndAtSegmentEndPoint(Segment aSeg)
    {
        // Get given seg end points
        double segEndX = aSeg.getX1();
        double segEndY = aSeg.getY1();
        List <Segment> segs = Collections.EMPTY_LIST;

        // Iterate over segments and add to list if start/end point matches given seg
        for (Segment seg : _segs) {

            // Skip given seg
            if (seg.equals(aSeg))
                continue;

            // If seg start point matches, add it
            if (Segment.equals(segEndX, seg.getX0()) && Segment.equals(segEndY, seg.getY0())) {
                if (segs == Collections.EMPTY_LIST) segs = new ArrayList<>();
                segs.add(seg);
            }

            // If seg end point matches, add it
            if (Segment.equals(segEndX, seg.getX1()) && Segment.equals(segEndY, seg.getY1())) {
                if (segs == Collections.EMPTY_LIST) segs = new ArrayList<>();
                segs.add(seg.createReverse());
            }
        }

        // Return
        return segs;
    }

    /**
     * Returns whether this SegmentPath has any intersecting segments.
     */
    public boolean isSelfIntersecting()
    {
        int segCount = getSegCount();

        // Iterate over all segments and return if intersects other segment
        for (int i = 0; i < segCount; i++) { Segment seg1 = getSeg(i);
            for (int j = i + 1; j < segCount; j++) { Segment seg2 = getSeg(j);

                // If segments intersect somewhere not on an end-point, return true
                SegHit segHit = seg1.getHit(seg2);
                if (segHit != null) {
                    if (segHit.h0 > .000001 && segHit.h0 < .999999)
                        return true;
                    if (segHit.h1 > .000001 && segHit.h1 < .999999)
                        return true;
                }
            }
        }

        // Return no intersections found
        return false;
    }

    /**
     * Makes the SegmentPath not intersecting by turning intersection areas into separate cycles.
     */
    public void makeNotSelfIntersecting()
    {
        // Split any self intersecting segments (return if none)
        boolean didSplit = splitIntersectingSegmentsAtIntersectionPoints();
        if (!didSplit)
            return;

        // Create list of segments to be added at end
        List <Segment> subsegs = new ArrayList<>();
        int segCount = getSegCount();

        // Extract subpath segments
        for (int i = 0; i < segCount; i++) { Segment seg0 = getSeg(i);
            for (int j = 0; j < i; j++) { Segment seg1 = getSeg(j);

                // If primary segment endpoint equals second segment start point, move segments in range out to subsegs list
                if (Point.equals(seg0.getX1(), seg0.getY1(), seg1.getX0(), seg1.getY0())) {
                    for (;i>=j;i--)
                        subsegs.add(removeSeg(j));
                    i = j-1;
                    segCount = getSegCount();
                    break;
                }
            }
        }

        // Add subsegs to end
        for (Segment seg : subsegs)
            addSeg(seg);
    }

    /**
     * Splits the segments in this SegmentPath wherever it intersects itself.
     */
    public boolean splitIntersectingSegmentsAtIntersectionPoints()
    {
        int segCount = getSegCount();
        boolean didSplit = false;

        // Iterate over all segments and split at all intersections with any other segment
        for (int i = 0; i < segCount; i++) { Segment seg1 = getSeg(i);
            for (int j = i + 1; j < segCount; j++) { Segment seg2 = getSeg(j);

                // If segments intersect
                SegHit segHit = seg1.getHit(seg2);
                if (segHit != null) {

                    // Find intersection point for seg2 and split/add if inside
                    double hitPoint2 = segHit.h1;
                    if (hitPoint2 > .000001 && hitPoint2 < .999999) {
                        Segment tail = seg2.split(hitPoint2);
                        addSeg(tail, j+1); segCount++;
                        didSplit = true;
                    }

                    // Find intersection point for seg1 and split/add if inside
                    double hitPoint1 = segHit.h0;
                    if (hitPoint1 > .000001 && hitPoint1 < .999999) {
                        Segment tail = seg1.split(hitPoint1);
                        addSeg(tail, i+1); segCount++;
                        didSplit = true;
                    }
                }
            }
        }

        // Return
        return didSplit;
    }

    /**
     * Splits the segments for this and given SegmentPath for every intersection of the two.
     */
    public void splitIntersectingSegmentsAtIntersectionPoints(SegmentPath aSegmentPath)
    {
        int segCount1 = getSegCount();
        int setCount2 = aSegmentPath.getSegCount();

        // Iterate over all segments and split at all intersections with other segment
        for (int i = 0; i < segCount1; i++) { Segment seg1 = getSeg(i);
            for (int j = 0; j < setCount2; j++) { Segment seg2 = aSegmentPath.getSeg(j);

                // If segments intersect
                SegHit segHit = seg1.getHit(seg2);
                if (segHit != null) {

                    // Find intersection point for seg1 and split/add if inside
                    double hp1 = segHit.h0;
                    if (hp1 > .000001 && hp1 < .999999) {
                        Segment tail = seg1.split(hp1);
                        addSeg(tail, i + 1);
                        segCount1++;
                    }

                    // Find intersection point for seg2 and split/add if inside
                    double hp2 = segHit.h1;
                    if (hp2 > .000001 && hp2 < .999999) {
                        Segment tail = seg2.split(hp2);
                        aSegmentPath.addSeg(tail, j + 1);
                        setCount2++;
                    }
                }
            }
        }
    }

    /**
     * Builds segments from shape.
     */
    private void appendShape(Shape aShape)
    {
        // Iterate over segments, if any segment intersects cubic, return true
        PathIter pathIter = aShape.getPathIter(null);
        double[] points = new double[6];
        double moveX = 0, moveY = 0;
        double lineX = 0, lineY = 0;

        while (pathIter.hasNext()) {
            switch (pathIter.getNext(points)) {

                // Handle MoveTo
                case MoveTo:
                    lineX = moveX = points[0];
                    lineY = moveY = points[1];
                    break;

                // Handle LineTo
                case LineTo: {
                    Line lineTo = new Line(lineX, lineY, lineX = points[0], lineY = points[1]);
                    addSeg(lineTo);
                    break;
                }

                // Handle QuadTo
                case QuadTo: {
                    Quad quadTo = new Quad(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3]);
                    addSeg(quadTo);
                    break;
                }

                // Handle CubicTo
                case CubicTo: {
                    Cubic cubicTo = new Cubic(lineX, lineY, points[0], points[1], points[2], points[3],lineX = points[4], lineY = points[5]);
                    addSeg(cubicTo);
                    break;
                }

                // Handle Close
                case Close:
                    if (lineX != moveX || lineY != moveY)
                        addSeg(new Line(lineX, lineY, lineX = moveX, lineY = moveY));
                    break;
            }
        }
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)  { return new SegmentPathIter(this, aTrans); }

    /**
     * A PathIter for SegmentPath.
     */
    private static class SegmentPathIter extends PathIter {

        // Ivars
        private Segment[] _segs;
        private int _segIndex;
        private double _moveX, _moveY;
        private double _lineX, _lineY;

        /** Constructor. */
        SegmentPathIter(SegmentPath aSL, Transform aTrans)
        {
            super(aTrans);
            _segs = aSL.getSegs().toArray(new Segment[0]);
        }

        /** Returns whether this iter has another segment. */
        public boolean hasNext()  { return _segIndex < _segs.length; }

        /** Returns the next segment. */
        public Seg getNext(double[] coords)
        {
            Segment seg = _segs[_segIndex];

            // If last end point was last move point, add moveTo
            if (_lineX == _moveX && _lineY == _moveY) {
                _lineX += .001;
                return moveTo(_moveX = seg.x0, _moveY = seg.y0, coords);
            }
            _segIndex++;

            // Make sure last segment closes path
            if (_segIndex == _segs.length) {
                seg.x1 = _moveX;
                seg.y1 = _moveY;
            }

            // Handle Seg Line
            if (seg instanceof Line) {
                Line line = (Line) seg;
                if (Point.equals(_moveX, _moveY, line.x1, line.y1))
                    return close();
                return lineTo(_lineX = line.x1, _lineY = line.y1, coords);
            }

            // Handle Seg Quad
            if (seg instanceof Quad) {
                Quad quad = (Quad) seg;
                return quadTo(quad.cpx, quad.cpy, _lineX = quad.x1, _lineY = quad.y1, coords);
            }

            // Handle Seg Cubic)
            Cubic cubic = (Cubic) seg;
            return cubicTo(cubic.cp0x, cubic.cp0y, cubic.cp1x, cubic.cp1y, _lineX = cubic.x1, _lineY = cubic.y1, coords);
        }
    }
}