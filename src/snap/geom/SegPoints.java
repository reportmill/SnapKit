package snap.geom;
import java.util.Arrays;

/**
 * A class to manage an array of Segs, an array of points, and an array of Seg-to-Point-Indexes so that it can quickly
 * return all points for a given Seg index.
 */
public class SegPoints extends Shape {

    // The array of segments
    protected Seg  _segs[] = new Seg[8];

    // The segment count
    private int  _scount;

    // The array of points
    protected double  _points[] = new double[16];

    // The number of points
    private int  _pcount;

    // The indexes to start point for each seg (or to ClosePointIndexes for Close seg)
    private int  _segPointIndexes[] = new int[8];

    // The array of { startPointIndex, endPointIndex } for each close seg
    private int  _closePointIndexes[][] = new int[0][];

    // The winding - how a path determines what to fill when segments intersect
    private int  _wind = WIND_EVEN_ODD;

    // Constants for winding
    public static final int WIND_EVEN_ODD = PathIter.WIND_EVEN_ODD;
    public static final int WIND_NON_ZERO = PathIter.WIND_NON_ZERO;

    /**
     * Constructor.
     */
    public SegPoints(Shape aShape)
    {
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
    public int getSegCount()  { return _scount; }

    /**
     * Returns the individual segement at index.
     */
    public Seg getSeg(int anIndex)  { return _segs[anIndex]; }

    /**
     * Adds a segment.
     */
    protected void addSeg(Seg aSeg)
    {
        // If at end of Segs array, extend by 2x
        if (_scount+1>_segs.length) {
            _segs = Arrays.copyOf(_segs, _segs.length * 2);
            _segPointIndexes = Arrays.copyOf(_segPointIndexes, _segPointIndexes.length * 2);
        }

        // Add Seg at end, increment SegCount, notify shapeChanged
        _segs[_scount++] = aSeg;
        shapeChanged();
    }

    /**
     * Returns the number of points.
     */
    public int getPointCount()  { return _pcount; }

    /**
     * Returns individual point at given index.
     */
    public Point getPoint(int anIndex)
    {
        double px = _points[anIndex*2];
        double py = _points[anIndex*2+1];
        return new Point(px, py);
    }

    /**
     * Adds a point.
     */
    protected void addPoint(double x, double y)
    {
        // If at end of Points array, extend by 2x
        if (_pcount*2+1>_points.length)
            _points = Arrays.copyOf(_points, _points.length*2);

        // Add points at end and increment PointCount
        _points[_pcount*2] = x;
        _points[_pcount*2+1] = y;
        _pcount++;
    }

    /**
     * Returns the array of point-indexes for given seg index.
     */
    public int getSegPointIndex(int anIndex)
    {
        return _segPointIndexes[anIndex];
    }

    /**
     * Adds an array of point-indexes.
     */
    protected void addSegPointIndex(int pointIndex)
    {
        _segPointIndexes[_scount-1] = pointIndex;
    }

    /**
     * Returns the points for a given seg index, by copying to given array (should be length of 8).
     */
    public Seg getSegAndPointsForIndex(int anIndex, double theCoords[])
    {
        // Get Seg and Seg PointIndex
        Seg seg = getSeg(anIndex);
        int pointIndex = getSegPointIndex(anIndex);

        // Handle Close special: PointIndex is to index in ClosePointIndexes (an array of each close {start,end} index)
        if (seg==Seg.Close) {
            int pointIndexes[] = _closePointIndexes[pointIndex];
            int index0 = pointIndexes[0];
            int index1 = pointIndexes[1];
            theCoords[0] = _points[index0];
            theCoords[1] = _points[index0+1];
            theCoords[2] = _points[index1];
            theCoords[3] = _points[index1+1];
        }

        // Copy Seg points to given point coord array
        else {
            int pointCount = seg.getCount() + 1;
            for (int i=0; i<pointCount; i++)
                theCoords[i] = _points[pointIndex + i];
        }

        // Return seg
        return seg;
    }

    /**
     * Returns the points for a given seg index, by copying to given array (should be length of 8).
     */
    public Seg getSegEndPointsForIndex(int anIndex, double theCoords[])
    {
        // Get seg and point index
        Seg seg = getSeg(anIndex);
        int pointIndex = getSegPointIndex(anIndex) + 1;

        // Copy end points to given point coord array
        int pointCount = seg.getCount();
        for (int i=0; i<pointCount; i++)
            theCoords[i] = _points[pointIndex + i];

        // Return seg
        return seg;
    }

    /**
     * Moveto.
     */
    public void moveTo(double x, double y)
    {
        // Add MoveTo
        addSeg(Seg.MoveTo);

        // Get pointIndex, add point, add pointIndexes for pointIndex
        int pointIndex = getPointCount();
        addPoint(x, y);
        addSegPointIndex(pointIndex);
    }

    /**
     * LineTo.
     */
    public void lineTo(double x, double y)
    {
        // Add MoveTo
        addSeg(Seg.LineTo);

        // Get pointIndex to last point (make sure there is a moveTo)
        int pointIndex = getPointCount() - 1;
        if (pointIndex<0) { moveTo(0, 0); pointIndex = 0; }

        // Add point and pointIndex
        addPoint(x, y);
        addSegPointIndex(pointIndex);
    }

    /**
     * QuadTo.
     */
    public void quadTo(double cpx, double cpy, double x, double y)
    {
        // Add QuadTo
        addSeg(Seg.QuadTo);

        // Get pointIndex to last point (make sure there is a moveTo)
        int pointIndex = getPointCount() - 1;
        if (pointIndex<0) { moveTo(0, 0); pointIndex = 0; }

        // Add control point, end point
        addPoint(cpx, cpy);
        addPoint(x,y);

        // Add SegPointIndex
        addSegPointIndex(pointIndex);
    }

    /**
     * CubicTo.
     */
    public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y)
    {
        // Add CubicTo
        addSeg(Seg.CubicTo);

        // Get pointIndex to last point (make sure there is a moveTo)
        int pointIndex = getPointCount() - 1;
        if (pointIndex<0) { moveTo(0, 0); pointIndex = 0; }

        // Add control points and end point
        addPoint(cp1x, cp1y);
        addPoint(cp2x, cp2y);
        addPoint(x, y);

        // Add SegPointIndex
        addSegPointIndex(pointIndex);
    }

    /**
     * Close.
     */
    public void close()
    {
        // Add Close
        addSeg(Seg.Close);

        // Get pointIndex to last point (make sure there is a moveTo)
        int pointIndex = getPointCount() - 1;
        if (pointIndex<0) { moveTo(0, 0); pointIndex = 0; }
        int moveToPointIndex = getLastMoveToPointIndex();

        // Add ClosePointIndexes close seg { lastPointIndex, lastMoveToPointIndex }
        int endIndex = _closePointIndexes.length;
        _closePointIndexes = Arrays.copyOf(_closePointIndexes, endIndex + 1);
        _closePointIndexes[endIndex] = new int[] { moveToPointIndex, pointIndex };
    }

    /**
     * Returns the last move to point.
     */
    private int getLastMoveToPointIndex()
    {
        // Iterate back through segs and return last MoveTo Index
        for (int segIndex = getSegCount() - 1; segIndex>=0; segIndex--) {

            // If MoveTo, return PointIndex
            Seg seg = getSeg(segIndex);
            if (seg==Seg.MoveTo)
                return getSegPointIndex(segIndex);

            // If Close (no MoveTo?) return close line end point
            if (seg==Seg.Close) {
                int closePointIndexes[] = _closePointIndexes[_closePointIndexes.length-1];
                return closePointIndexes[1];
            }
        }

        // No MoveTo (shouldn't happen)
        return 0;
    }

    /**
     * Standard clone implementation.
     */
    public SegPoints clone()
    {
        SegPoints copy; try { copy = (SegPoints) super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
        copy._segs = Arrays.copyOf(_segs, _segs.length);
        copy._segPointIndexes = Arrays.copyOf(_segPointIndexes, _segs.length);
        copy._points = Arrays.copyOf(_points, _points.length);
        return copy;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity & class and get other path
        if (anObj==this) return true;
        SegPoints other = anObj instanceof Path ? (SegPoints) anObj : null; if (other==null) return false;

        // Check ElementCount, WindingRule, Elements and Points
        if (other._scount!=_scount || other._pcount!=_pcount) return false;
        if (!Arrays.equals(other._segs, _segs)) return false;
        if (!Arrays.equals(other._points, _points)) return false;
        return true; // Return true since all checks passed
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return new SPPathIter(aTrans);
    }

    /**
     * A PathIter for Path.
     */
    private class SPPathIter extends PathIter {

        // Ivars
        private int  _segCount;
        private int  _sindex;
        private double  _pnts[] = new double[8];

        /** Constructor. */
        SPPathIter(Transform aTrans)
        {
            super(aTrans);
            _segCount = getSegCount();
        }

        /** Returns whether PathIter has another segement. */
        public boolean hasNext()
        {
            return _sindex < _segCount;
        }

        /** Returns the next segment. */
        public Seg getNext(double coords[])
        {
            return getSegEndPointsForIndex(_sindex++, _pnts);
        }

        /** Returns the winding - how a path determines what to fill when segments intersect. */
        public int getWinding()  { return SegPoints.this.getWinding(); }
    }
}
