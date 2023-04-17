/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.Arrays;

/**
 * A standard path shape that can be built/modified by standard moveTo/lineTo/curveTo methods.
 */
public class Path2D extends ShapeBuilder implements Cloneable {

    // The array of segments
    protected Seg[] _segs = new Seg[8];

    // The segment count
    protected int _segCount;

    // The array of points
    protected double[] _points = new double[16];

    // The number of points
    protected int _pointCount;

    // The indexes to start point for each seg
    protected int[] _segPointIndexes = new int[8];

    // The winding - how a path determines what to fill when segments intersect
    private int _winding = WIND_EVEN_ODD;

    /**
     * Constructor.
     */
    public Path2D()
    {
        super();
    }

    /**
     * Constructor with given shape.
     */
    public Path2D(Shape aShape)
    {
        this();
        appendShape(aShape);
    }

    /**
     * Constructor with given PathIter.
     */
    public Path2D(PathIter aPathIter)
    {
        this();
        appendPathIter(aPathIter);
    }

    /**
     * Returns the number of segments.
     */
    public int getSegCount()  { return _segCount; }

    /**
     * Returns the individual segment at given index.
     */
    public Seg getSeg(int anIndex)  { return _segs[anIndex]; }

    /**
     * Adds a segment.
     */
    protected void addSeg(Seg aSeg)
    {
        // If at end of Segs array, extend by 2x
        if (_segCount + 1 > _segs.length) {
            _segs = Arrays.copyOf(_segs, _segs.length * 2);
            _segPointIndexes = Arrays.copyOf(_segPointIndexes, _segPointIndexes.length * 2);
        }

        // Update SegPointIndexes
        _segPointIndexes[_segCount] = getPointCount();

        // Add Seg at end, increment SegCount, notify shapeChanged
        _segs[_segCount++] = aSeg;
        shapeChanged();
    }

    /**
     * Returns the number of points.
     */
    public int getPointCount()  { return _pointCount; }

    /**
     * Returns individual point at given index.
     */
    public Point getPoint(int anIndex)
    {
        double px = _points[anIndex * 2];
        double py = _points[anIndex * 2 + 1];
        return new Point(px, py);
    }

    /**
     * Sets the individual point at given index.
     */
    public void setPoint(int anIndex, double aX, double aY)
    {
        _points[anIndex * 2] = aX;
        _points[anIndex * 2 + 1] = aY;
        shapeChanged();
    }

    /**
     * Adds a point.
     */
    protected void addPoint(double aX, double aY)
    {
        // If at end of Points array, extend by 2x
        if (_pointCount * 2 + 1 > _points.length)
            _points = Arrays.copyOf(_points, _points.length * 2);

        // Add points at end and increment PointCount
        _points[_pointCount * 2] = aX;
        _points[_pointCount * 2 + 1] = aY;
        _pointCount++;
    }

    /**
     * Moveto.
     */
    public void moveTo(double aX, double aY)
    {
        // Check for consecutive moveTo
        if (getSegCount() > 0 && getSeg(getSegCount() - 1) == Seg.MoveTo)
            System.err.println("Path2D.moveTo: Consecutive MoveTo should be avoided");

        // Add MoveTo and point
        addSeg(Seg.MoveTo);
        addPoint(aX, aY);
    }

    /**
     * LineTo.
     */
    public void lineTo(double aX, double aY)
    {
        // Make sure there is a starting MoveTo
        if (getPointCount() == 0)
            moveTo(0, 0);

        // Add LineTo and Point
        addSeg(Seg.LineTo);
        addPoint(aX, aY);
    }

    /**
     * QuadTo.
     */
    public void quadTo(double cpX, double cpY, double endX, double endY)
    {
        // Make sure there is a starting MoveTo
        if (getPointCount() == 0)
            moveTo(0, 0);

        // Add QuadTo and points
        addSeg(Seg.QuadTo);
        addPoint(cpX, cpY);
        addPoint(endX, endY);
    }

    /**
     * CubicTo.
     */
    public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double endX, double endY)
    {
        // Make sure there is a starting MoveTo
        if (getPointCount() == 0)
            moveTo(0, 0);

        // Add CubicTo and points
        addSeg(Seg.CubicTo);
        addPoint(cp1x, cp1y);
        addPoint(cp2x, cp2y);
        addPoint(endX, endY);
    }

    /**
     * Close.
     */
    public void close()
    {
        // If last seg has no points, just return - don't close empty path
        Seg lastSeg = getLastSeg();
        if (lastSeg == Seg.MoveTo || lastSeg == Seg.Close || lastSeg == null) {
            System.err.println("Path2D.close: Attempting to close empty path");
            return;
        }

        // Add Close
        addSeg(Seg.Close);
    }

    /**
     * Clears all segments from path.
     */
    public void clear()
    {
        _segCount = _pointCount = 0;
        shapeChanged();
    }

    /**
     * Returns the last seg.
     */
    public Seg getLastSeg()  { return _segCount > 0 ? getSeg(_segCount - 1) : null; }

    /**
     * Returns the last point.
     */
    public Point getLastPoint()  { return _pointCount > 0 ? getPoint(_pointCount - 1) : null; }

    /**
     * Returns the last point X.
     */
    public double getLastPointX()  { return _pointCount > 0 ? _points[_pointCount * 2 - 2] : 0; }

    /**
     * Returns the last point Y.
     */
    public double getLastPointY()  { return _pointCount > 0 ? _points[_pointCount * 2 - 1] : 0; }

    /**
     * Returns the array of point-indexes for given seg index.
     */
    public int getSegPointIndex(int anIndex)  { return _segPointIndexes[anIndex]; }

    /**
     * Returns the control points and end point for a given seg index, by copying to given array (should be length of 6).
     */
    public Seg getSegAndPointsForIndex(int anIndex, double[] theCoords, Transform aTrans)
    {
        // Get seg
        Seg seg = getSeg(anIndex);

        // Copy end points to given point coord array and transform if needed
        int segPointCount = seg.getCount();
        if (segPointCount > 0) {
            int pointIndex = getSegPointIndex(anIndex) * 2;
            System.arraycopy(_points, pointIndex, theCoords, 0, segPointCount * 2);
            if (aTrans != null)
                aTrans.transformXYArray(theCoords, segPointCount);
        }

        // Return
        return seg;
    }

    /**
     * Returns the seg index for given point index.
     */
    public int getSegIndexForPointIndex(int anIndex)
    {
        int segIndex = 0;
        for (int pointIndex = 0; pointIndex <= anIndex && segIndex < _segCount; segIndex++)
            pointIndex += getSeg(segIndex).getCount();
        return segIndex - 1;
    }

    /**
     * Removes the last segment.
     */
    public void removeLastSeg()
    {
        Seg lastSeg = getLastSeg();
        int segPointCount = lastSeg.getCount();
        _segCount--;
        _pointCount -= segPointCount;
    }

    /**
     * Returns the winding - how a path determines what to fill when segments intersect.
     */
    public int getWinding()  { return _winding; }

    /**
     * Sets the winding - how a path determines what to fill when segments intersect.
     */
    public void setWinding(int aValue)  { _winding = aValue; }

    /**
     * Fits the path points to a curve starting at the given point index.
     */
    public void fitToCurveFromPointIndex(int pointIndex)
    {
        PathFitCurves.fitCurveFromPointIndex(this, pointIndex);
    }

    /**
     * Override to return as path.
     */
    public Path2D copyFor(Rect aRect)
    {
        return (Path2D) super.copyFor(aRect);
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public Path2D clone()
    {
        // Do normal version
        Path2D copy;
        try { copy = (Path2D) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Copy Segs, Points, SetPointIndexes
        copy._segs = _segs.clone();
        copy._points = _points.clone();
        copy._segPointIndexes = _segPointIndexes.clone();

        // Return
        return copy;
    }

    /**
     * Standard equals implementation.
     */
    @Override
    public boolean equals(Object anObj)
    {
        // Check identity & class and get other path
        if (anObj == this) return true;
        Path2D other = anObj instanceof Path2D ? (Path2D) anObj : null;
        if (other == null)
            return false;

        // Check SegCount/PointCount, Segs, Points and Winding
        if (other._segCount != _segCount || other._pointCount != _pointCount)
            return false;
        if (!Arrays.equals(other._segs, _segs))
            return false;
        if (!Arrays.equals(other._points, _points))
            return false;
        if (other._winding != _winding)
            return false;

        // Return true since all checks passed
        return true;
    }

    /**
     * Returns a path iterator.
     */
    @Override
    public PathIter getPathIter(Transform aTrans)
    {
        return new Path2DPathIter(aTrans);
    }

    /**
     * A PathIter for Path2D.
     */
    private class Path2DPathIter extends PathIter {

        // Ivars
        private int _segCount;
        private int _segIndex;

        /**
         * Constructor.
         */
        Path2DPathIter(Transform aTrans)
        {
            super(aTrans);
            _segCount = getSegCount();
        }

        /**
         * Returns whether PathIter has another segment.
         */
        public boolean hasNext()  { return _segIndex < _segCount; }

        /**
         * Returns the next segment.
         */
        public Seg getNext(double[] coords)
        {
            if (_segIndex >= _segCount)
                throw new RuntimeException("Path2DPathIter.getNext: No more path segments");

            // Return Seg and points
            return getSegAndPointsForIndex(_segIndex++, coords, _trans);
        }

        /**
         * Returns the winding - how a path determines what to fill when segments intersect.
         */
        public int getWinding()  { return Path2D.this.getWinding(); }
    }
}
