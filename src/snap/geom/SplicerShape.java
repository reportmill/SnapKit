package snap.geom;

/**
 * This is a shape that wraps around another shape and can provide parts.
 */
public class SplicerShape extends Shape {

    // The original shape
    private Path2DX _path;

    // The start parametric ratio
    private double _start;

    // The end parametric ratio
    private double  _end;

    // The tail point
    private Point  _tailPoint = Point.ZERO;

    // The tail angle
    private double  _tailAngle = Double.NaN;

    /**
     * Constructor.
     */
    public SplicerShape(Shape aShape, double aStart, double anEnd)
    {
        _path = aShape instanceof Path2DX ? (Path2DX) aShape : new Path2DX(aShape);
        _start = aStart;
        _end = anEnd;
    }

    /**
     * Returns the tail point.
     */
    public Point getTailPoint()  { return _tailPoint; }

    /**
     * Returns the tail angle.
     */
    public double getTailAngle()
    {
        return !Double.isNaN(_tailAngle) ? _tailAngle : 0;
    }

    /**
     * Override to return iterator.
     */
    @Override
    public PathIter getPathIter(Transform aTrans)
    {
        // Get pathIter from shape (just return if start/end at ends)
        PathIter pathIter = _path.getPathIter(aTrans);
        if (_start<=0 && _end>=1)
            return pathIter;

        // Create SplicerIter and return
        return new SplicerIter(aTrans);
    }

    /**
     * A PathIter for splicer.
     */
    private class SplicerIter extends PathIter {

        // The total length of shape
        private double  _lenAll;

        // The max length
        private double  _lenMax;

        // The current running length
        private double  _lenRun;

        // Total number of original path Segs
        private int  _segCount;

        // Current path Seg index
        private int  _segIndex;

        /**
         * Constructor.
         */
        public SplicerIter(Transform aTrans)
        {
            super(aTrans);
            _segCount = _path.getSegCount();
            _lenAll = _path.getArcLength();
            _lenMax = _lenAll * _end;
            if (_start>0)
                System.err.println("SplicerShape: Start value not supported yet");
        }

        /**
         * Return true if at end of range or out of Segs.
         */
        @Override
        public boolean hasNext()  { return _segIndex < _segCount; }

        /**
         * Return next Seg and points, trimming if at end of range.
         */
        @Override
        public Seg getNext(double[] coords)
        {
            // Get seg and points (just return if zero length - (MoveTo or Close))
            Seg seg = _path.getSegAndPointsForIndex(_segIndex, coords, _trans);
            if (seg == Seg.MoveTo || seg == Seg.Close) {
                _segIndex++;
                return seg;
            }

            // If running length plus current seg length under max, just return full seg
            double len = _path.getSegArcLength(_segIndex);
            if (_lenRun + len <= _lenMax) {
                _lenRun += len;
                _segIndex++;
                return seg;
            }

            // Get length remainder and ratio of final seg
            double lenRem = _lenMax - _lenRun;
            double segRatio = lenRem / len;

            // Get partial segment for ratio of final seg
            Segment segment = _path.getSegment(_segIndex, _trans);
            segment.split(segRatio);
            segment.getEndCoords(coords);

            // Get Tail angle
            Segment.PointAndAngle pointAndAngle = segment.getPointAndAngle(1);
            _tailPoint = _trans == null ? pointAndAngle.point() : _trans.transformXY(pointAndAngle.x(), pointAndAngle.y());
            _tailAngle = pointAndAngle.angle();

            // Set finished and return
            _lenRun = _lenMax;
            _segIndex = _segCount;
            return seg;
        }
    }
}
