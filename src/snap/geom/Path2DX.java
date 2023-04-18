/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * This extended Path2D provides extra features like ArcLength measurement.
 */
public class Path2DX extends Path2D {

    // The total arc length of path
    private double  _arcLength;

    // The arc lengths for each seg
    private double[]  _arcLengths;

    /**
     * Constructor.
     */
    public Path2DX()
    {
        super();
    }

    /**
     * Constructor with given shape.
     */
    public Path2DX(Shape aShape)
    {
        this();
        appendShape(aShape);
    }

    /**
     * Returns the segment at index.
     */
    public Segment getSegment(int anIndex, Transform aTrans)
    {
        double[] points = new double[8];
        Seg seg = getSegAndSegmentPointsForIndex(anIndex, points, aTrans);
        return Segment.newSegmentForSegAndPoints(seg, points);
    }

    /**
     * Returns all points (start/control/end) for a given seg index, by copying to given array (should be length of 8).
     */
    public Seg getSegAndSegmentPointsForIndex(int anIndex, double[] theCoords, Transform aTrans)
    {
        // Get Seg and Seg PointIndex
        Seg seg = getSeg(anIndex);
        int pointIndex = getSegPointIndex(anIndex) * 2 - 2;

        // Handle Close special: PointIndex is to index in ClosePointIndexes (an array of each close {start,end} index)
        if (seg == Seg.Close) {

            // Copy last point
            theCoords[0] = _points[pointIndex];
            theCoords[1] = _points[pointIndex + 1];

            // Get previous MoveTo SegIndex
            int moveToSegIndex = anIndex;
            while (moveToSegIndex > 0 && getSeg(moveToSegIndex) != Seg.MoveTo)
                moveToSegIndex--;

            // Get MoveTo PointIndex and copy points
            int moveToPointIndex = getSegPointIndex(moveToSegIndex) * 2;
            theCoords[2] = _points[moveToPointIndex];
            theCoords[3] = _points[moveToPointIndex + 1];
        }

        // Handle MoveTo: Probably not used, but copy move to point
        else if (seg == Seg.MoveTo) {
            System.arraycopy(_points, pointIndex + 2, theCoords, 0, 2);
        }

        // Copy Seg points to given point coord array
        else {
            int pointCount = seg.getCount() + 1;
            System.arraycopy(_points, pointIndex, theCoords, 0, pointCount * 2);
        }

        // If Transform, transform
        if (aTrans != null)
            aTrans.transformXYArray(theCoords, seg.getCount() + 1);

        // Return seg
        return seg;
    }

    /**
     * Returns the total arc length of path.
     */
    public double getArcLength()
    {
        if (_arcLengths != null) return _arcLength;
        getArcLengths();
        return _arcLength;
    }

    /**
     * Returns the total arc length of path.
     */
    public double getSegArcLength(int anIndex)
    {
        double[] arcLens = getArcLengths();
        return arcLens[anIndex];
    }

    /**
     * Returns the lengths array.
     */
    public double[] getArcLengths()
    {
        // If already set, just return
        if (_arcLengths != null) return _arcLengths;

        // Get iter vars
        int segCount = getSegCount();
        double[] arcLengths = new double[segCount];
        double[] points = new double[8];
        double arcLength = 0;

        // Iterate over segs and calc lengths
        for (int i = 0; i < segCount; i++) {

            Seg seg = getSegAndSegmentPointsForIndex(i, points, null);
            double len = 0;

            // Get arcLength for seg
            switch (seg) {
                case MoveTo: break;
                case LineTo: len = Point.getDistance(points[0], points[1], points[2], points[3]); break;
                case QuadTo:
                    len = SegmentLengths.getArcLengthQuad(points[0], points[1], points[2], points[3], points[4], points[5]);
                    break;
                case CubicTo:
                    len = SegmentLengths.getArcLengthCubic(points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7]);
                    break;
                default: throw new RuntimeException("SegPoints.getArcLengths: Unsuppored seg: " + seg);
            }

            // Update arcLengths
            arcLengths[i] = len;
            arcLength += len;
        }

        // Set/return arcLengths
        _arcLength = arcLength;
        return _arcLengths = arcLengths;
    }

    /**
     * Override to clear ArcLengths.
     */
    @Override
    protected void shapeChanged()
    {
        super.shapeChanged();
        _arcLengths = null;
    }
}
