/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * A class to iterate over segments in a shape, providing specific coordinate information.
 */
public abstract class PathIter {
    
    // The transform
    protected Transform  _trans;
    
    // Constants for winding
    public static final int WIND_EVEN_ODD = 0;
    public static final int WIND_NON_ZERO = 1;

    /**
     * Creates a new PathIter.
     */
    public PathIter()  { }

    /**
     * Creates a new PathIter for given transform.
     */
    public PathIter(Transform aTrans)  { _trans = aTrans; }

    /**
     * Returns the Transform associated with this PathIter.
     */
    public Transform getTransform()  { return _trans; }

    /**
     * Returns the winding - how a path determines what to fill when segments intersect.
     */
    public int getWinding()  { return WIND_EVEN_ODD; }

    /**
     * Returns the next segment.
     */
    public abstract Seg getNext(double[] coords);

    /**
     * Returns the next segment (float coords).
     */
    public Seg getNext(float[] coords)
    {
        double[] points = new double[6];
        Seg seg = getNext(points);
        for (int i = 0; i < 6; i++)
            coords[i] = (float) points[i];
        return seg;
    }

    /**
     * Returns whether has next segment.
     */
    public abstract boolean hasNext();

    /**
     * Returns a MoveTo for given coords.
     */
    protected final Seg moveTo(double aX, double aY, double[] coords)
    {
        coords[0] = aX;
        coords[1] = aY;
        if (_trans != null)
            _trans.transformXYArray(coords, 1);
        return Seg.MoveTo;
    }

    /**
     * Returns a LineTo for given coords.
     */
    protected final Seg lineTo(double aX, double aY, double[] coords)
    {
        coords[0] = aX;
        coords[1] = aY;
        if (_trans != null)
            _trans.transformXYArray(coords, 1);
        return Seg.LineTo;
    }

    /**
     * Returns a QuadTo for given coords.
     */
    protected final Seg quadTo(double aCPX, double aCPY, double aX, double aY, double[] coords)
    {
        coords[0] = aCPX;
        coords[1] = aCPY;
        coords[2] = aX;
        coords[3] = aY;
        if (_trans != null)
            _trans.transformXYArray(coords, 2);
        return Seg.QuadTo;
    }

    /**
     * Returns a CubicTo for given coords.
     */
    protected final Seg cubicTo(double aCPX0, double aCPY0, double aCPX1, double aCPY1, double aX, double aY, double[] coords)
    {
        coords[0] = aCPX0;
        coords[1] = aCPY0;
        coords[2] = aCPX1;
        coords[3] = aCPY1;
        coords[4] = aX;
        coords[5] = aY;
        if (_trans != null)
            _trans.transformXYArray(coords, 3);
        return Seg.CubicTo;
    }

    /**
     * Returns a CubicTo for start, corner and end points.
     */
    protected final Seg arcTo(double lx, double ly, double cx, double cy, double x, double y, double[] coords)
    {
        double magic = .5523f; // I calculated this in mathematica one time - probably only valid for 90 deg corner.
        double cpx1 = lx + (cx - lx) * magic;
        double cpy1 = ly + (cy - ly) * magic;
        double cpx2 = x + (cx - x) * magic;
        double cpy2 = y + (cy - y) * magic;
        return cubicTo(cpx1, cpy1, cpx2, cpy2, x, y, coords);
    }

    /**
     * Returns a close.
     */
    protected final Seg close()  { return Seg.Close; }

    /**
     * Returns bounds rect for given PathIter.
     */
    public static Rect getBounds(PathIter aPathIter)
    {
        // Get iter vars
        double[] points = new double[6];
        double lineX = 0;
        double lineY = 0;
        Rect shapeBounds = new Rect();
        Rect segBounds = null;

        // Iterate over segments
        while (aPathIter.hasNext()) {
            Seg seg = aPathIter.getNext(points);
            switch (seg) {

                // Handle MoveTo
                case MoveTo:
                    if (segBounds == null) {
                        shapeBounds.setRect(lineX = points[0], lineY = points[1],0,0);
                        continue;
                    }

                // Handle LineTo
                case LineTo:
                    segBounds = Line.getBounds(lineX, lineY, lineX = points[0], lineY = points[1], segBounds);
                    break;

                // Handle QuadTo
                case QuadTo:
                    segBounds = Quad.getBounds(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3], segBounds);
                    break;

                // Handle CubicTo
                case CubicTo:
                    segBounds = Cubic.getBounds(lineX, lineY, points[0], points[1], points[2], points[3], lineX = points[4], lineY = points[5], segBounds);
                    break;

                // Handle Close
                case Close: break;
            }

            // Combine bounds for segment (I wish this was union() instead, so it didn't include (0,0))
            shapeBounds.add(segBounds);
        }

        // Return
        return shapeBounds;
    }

    /**
     * Returns total length of segments for given PathIter.
     */
    public static double getArcLength(PathIter aPathIter)
    {
        // Get iter vars
        double[] points = new double[6];
        double lineX = 0;
        double lineY = 0;
        double arcLength = 0;
        double segLength = 0;

        // Iterate over segments
        while (aPathIter.hasNext()) {
            Seg seg = aPathIter.getNext(points);
            switch (seg) {

                // Handle MoveTo
                case MoveTo:
                    lineX = points[0];
                    lineY = points[1];
                    segLength = 0;
                    break;

                // Handle LineTo
                case LineTo:
                    segLength = Point.getDistance(lineX, lineY, lineX = points[0], lineY = points[1]);
                    break;

                // Handle QuadTo
                case QuadTo:
                    segLength = SegmentLengths.getArcLengthQuad(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3]);
                    break;

                // Handle CubicTo
                case CubicTo:
                    segLength = SegmentLengths.getArcLengthCubic(lineX, lineY, points[0], points[1], points[2], points[3], lineX = points[4], lineY = points[5]);
                    break;

                // Handle Close
                case Close: segLength = 0; break;
            }

            // Add SegLength
            arcLength += segLength;
        }

        // Return
        return arcLength;
    }

    /**
     * Returns a combined PathIter for given array of PathIter.
     */
    public static PathIter getPathIterForPathIterArray(PathIter[] pathIters)
    {
        return new ArrayPathIter(pathIters);
    }

    /**
     * PathIter for an array of PathIters.
     */
    private static class ArrayPathIter extends PathIter {

        // Ivars
        private PathIter[] _pathIters;
        private PathIter _pathIter;
        private int _polyIndex;

        /**
         * Constructor.
         */
        private ArrayPathIter(PathIter[] pathIters)
        {
            super(null);
            _pathIters = pathIters;
            _pathIter = _pathIters.length > 0 ? _pathIters[0] : null;
        }

        /**
         * Returns whether there are more segments.
         */
        public boolean hasNext()  { return _pathIter != null && _pathIter.hasNext(); }

        /**
         * Returns the coordinates and type of the current path segment in the iteration.
         */
        public Seg getNext(double[] coords)
        {
            // Get next segment + points
            Seg seg = _pathIter.getNext(coords);

            // If at end of current PathIter, get next
            while (!_pathIter.hasNext() && _polyIndex < _pathIters.length)
                _pathIter = _pathIters[_polyIndex++];

            // Return
            return seg;
        }
    }
}