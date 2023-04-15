/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.Arrays;
import java.util.Objects;

/**
 * A standard path shape that can be built/modified by standard moveTo/lineTo/curveTo methods.
 */
public class Path2D extends Shape implements Cloneable {

    // The array of segments
    protected Seg[] _segs = new Seg[8];

    // The segment count
    private int _segCount;

    // The array of points
    protected double[] _points = new double[16];

    // The number of points
    private int _pointCount;

    // The indexes to start point for each seg (or to ClosePointIndexes for Close seg)
    private int[] _segPointIndexes = new int[8];

    // Whether path is closed
    private boolean  _closed;

    // The next path (if segs added after close)
    private Path2D  _nextPath;

    // The winding - how a path determines what to fill when segments intersect
    private int _wind = WIND_EVEN_ODD;

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
     * Returns the winding - how a path determines what to fill when segments intersect.
     */
    public int getWinding()  { return _wind; }

    /**
     * Sets the winding - how a path determines what to fill when segments intersect.
     */
    public void setWinding(int aValue)  { _wind = aValue; }

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
        // If closed, forward
        if (_closed) {
            getNextPathWithIntentToExtend().moveTo(aX, aY);
            return;
        }

        // Check for consecutive moveTo
        if (getSegCount() > 0 && getSeg(getSegCount() - 1) == Seg.MoveTo)
            System.err.println("SegPoints.moveTo: Consecutive MoveTo");

        // Add MoveTo and point
        addSeg(Seg.MoveTo);
        addPoint(aX, aY);
    }

    /**
     * LineTo.
     */
    public void lineTo(double aX, double aY)
    {
        // If closed, forward
        if (_closed) {
            getNextPathWithIntentToExtend().lineTo(aX, aY);
            return;
        }

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
        // If closed, forward
        if (_closed) {
            getNextPathWithIntentToExtend().quadTo(cpX, cpY, endX, endY);
            return;
        }

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
        // If closed, forward
        if (_closed) {
            Path2D nextPath = getNextPathWithIntentToExtend();
            nextPath.curveTo(cp1x, cp1y, cp2x, cp2y, endX, endY);
            return;
        }

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
        // If closed, forward
        if (_closed) {
            getNextPathWithIntentToExtend().close();
            return;
        }

        // If no points, just return - don't close empty path
        if (getPointCount() == 0)
            return;

        // Add Close
        addSeg(Seg.Close);
        _closed = true;
    }

    /**
     * Appends given shape to end of path.
     */
    public void appendShape(Shape aShape)
    {
        PathIter pathIter = aShape.getPathIter(null);
        appendPathIter(pathIter);
    }

    /**
     * Appends given PathIter to end of path.
     */
    public void appendPathIter(PathIter aPathIter)
    {
        double[] points = new double[6];
        while (aPathIter.hasNext()) {
            switch (aPathIter.getNext(points)) {
                case MoveTo: moveTo(points[0], points[1]); break;
                case LineTo: lineTo(points[0], points[1]); break;
                case QuadTo: quadTo(points[0], points[1], points[2], points[3]); break;
                case CubicTo: curveTo(points[0], points[1], points[2], points[3], points[4], points[5]); break;
                case Close: close(); break;
            }
        }
    }

    /**
     * Returns the last point.
     */
    public Point getLastPoint()  { return _pointCount > 0 ? getPoint(_pointCount - 1) : null; }

    /**
     * Returns the last point X.
     */
    public double getLastPointX(int anIndex)  { return _pointCount > 0 ? _points[_pointCount * 2 - 2] : 0; }

    /**
     * Returns the last point Y.
     */
    public double getLastPointY(int anIndex)  { return _pointCount > 0 ? _points[_pointCount * 2 - 1] : 0; }

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
     * Removes the last segment.
     */
    public void removeLastSeg()
    {
        int lastSegIndex = getSegCount() - 1;
        Seg lastSeg = getSeg(lastSegIndex);
        int segPointCount = lastSeg.getCount();
        _segCount--;
        _pointCount -= segPointCount;
    }

    /**
     * Returns the next path.
     */
    public Path2D getNextPath()  { return _nextPath; }

    /**
     * Returns the next path.
     */
    protected Path2D getNextPathWithIntentToExtend()
    {
        if (_nextPath == null)
            _nextPath = new Path2D();
        shapeChanged();
        return _nextPath;
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

        // Copy Segs, SetPointIndexes, Points, NextPath
        copy._segs = _segs.clone();
        copy._segPointIndexes = _segPointIndexes.clone();
        copy._points = _points.clone();
        if (_nextPath != null)
            copy._nextPath = _nextPath.clone();

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

        // Check ElementCount, WindingRule, Elements and Points
        if (other._segCount != _segCount || other._pointCount != _pointCount)
            return false;
        if (!Arrays.equals(other._segs, _segs))
            return false;
        if (!Arrays.equals(other._points, _points))
            return false;
        if (!Objects.equals(other._nextPath, _nextPath))
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
        private PathIter _nextIter;

        /**
         * Constructor.
         */
        Path2DPathIter(Transform aTrans)
        {
            super(aTrans);
            _segCount = getSegCount();
            if (_nextPath != null)
                _nextIter = _nextPath.getPathIter(aTrans);
        }

        /**
         * Returns whether PathIter has another segment.
         */
        public boolean hasNext()
        {
            return _segIndex < _segCount || (_nextIter != null && _nextIter.hasNext());
        }

        /**
         * Returns the next segment.
         */
        public Seg getNext(double[] coords)
        {
            if (_segIndex < _segCount)
                return getSegAndPointsForIndex(_segIndex++, coords, _trans);
            return _nextIter.getNext(coords);
        }

        /**
         * Returns the winding - how a path determines what to fill when segments intersect.
         */
        public int getWinding()  { return Path2D.this.getWinding(); }
    }
}
