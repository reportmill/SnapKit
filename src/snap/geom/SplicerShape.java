package snap.geom;

/**
 * This is a shape that wraps around another shape and can provide parts.
 */
public class SplicerShape extends Shape {

    // The original shape
    private Shape  _shape;

    // The start parametric ratio
    private double _start;

    // The end parametric ratio
    private double  _end;

    // The tail point
    private Point  _tailPoint = new Point();

    // The tail angle
    private double  _tailAngle = Double.NaN;

    /**
     * Constructor.
     */
    public SplicerShape(Shape aShape, double aStart, double anEnd)
    {
        _shape = aShape;
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
    public PathIter getPathIter(Transform aT)
    {
        // Get pathIter from shape (just return if start/end at ends)
        PathIter pathIter = _shape.getPathIter(aT);
        if (_start<=0 && _end>=1)
            return pathIter;

        // Create SplicerIter and return
        return new SplicerIter(pathIter);
    }

    /**
     * A PathIter for splicer.
     */
    private class SplicerIter extends PathIter {

        // The Shape.PathIter
        private PathIter  _pathIter;

        // The current set of points
        private double  _pnts[] = new double[6];

        // The last x/y
        private double  _lx, _ly;

        // The current segment
        private Seg  _seg;

        // The total length of shape
        private double  _lenAll;

        // The max length
        private double  _lenMin;

        // The max length
        private double  _lenMax;

        // The current running length
        private double  _lenRun;

        // Whether there are more segs
        private boolean  _hasNext;

        /**
         * Constructor.
         */
        public SplicerIter(PathIter aPathIter)
        {
            _pathIter = aPathIter;
            _lenAll = _shape.getArcLength();
            _lenMin = _lenAll * _start;
            _lenMax = _lenAll * _end;
            _hasNext = _pathIter.hasNext();
            if (_start>0)
                System.err.println("SplicerShape: Start value not supported yet");
        }

        /**
         * Returns the legnth of given segment.
         */
        private double getSegLength(Seg aSeg, double lastX, double lastY, double pnts[])
        {
            switch (aSeg) {
                case LineTo: return Point.getDistance(_lx, _ly, pnts[0], pnts[1]);
                case QuadTo: return SegmentLengths.getArcLengthQuad(_lx, _ly, pnts[0], pnts[1], pnts[2], pnts[3]);
                case CubicTo: return SegmentLengths.getArcLengthCubic(_lx, _ly, pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]);
                default: return 0;
            }
        }

        @Override
        public Seg getNext(double[] coords)
        {
            // Get next seg
            _seg = _pathIter.getNext(_pnts);

            // Copy points
            int count = _seg.getCount() * 2;
            System.arraycopy(_pnts, 0, coords, 0, count);

            // Get len of current segment
            double len = getSegLength(_seg, _lx, _ly, _pnts);

            // If running length plus current seg length under max, just return full seg
            if (_lenRun + len <= _lenMax) {
                _lx = _pnts[count-2];
                _ly = _pnts[count-1];
                _lenRun += len;
                _hasNext = _pathIter.hasNext();
                return _seg;
            }

            // Get length remainder and ratio of final seg
            double lenRem = _lenMax - _lenRun;
            double segRatio = lenRem / len;

            // Handle segment
            switch (_seg) {
                case LineTo: {
                    double px = _pnts[0], py = _pnts[1];
                    Line line = new Line(_lx, _ly, px, py);
                    line.split(segRatio);
                    coords[0] = line.x1; coords[1] = line.y1;
                    _tailAngle = line.getPointAndAngle(1, _tailPoint);
                    break;
                }
                case QuadTo: {
                    double cpx = _pnts[0], cpy = _pnts[1];
                    double px = _pnts[2], py = _pnts[3];
                    Quad quad = new Quad(_lx, _ly, cpx, cpy, px, py);
                    quad.split(segRatio);
                    coords[0] = quad.cpx; coords[1] = quad.cpy;
                    coords[2] = quad.x1; coords[3] = quad.y1;
                    _tailAngle = quad.getPointAndAngle(1, _tailPoint);
                    break;
                }
                case CubicTo: {
                    double cp0x = _pnts[0], cp0y = _pnts[1], cp1x = _pnts[2], cp1y = _pnts[3];
                    double px = _pnts[4], py = _pnts[5];
                    Cubic cubic = new Cubic(_lx, _ly, cp0x, cp0y, cp1x, cp1y, px, py);
                    cubic.split(segRatio);
                    coords[0] = cubic.cp0x; coords[1] = cubic.cp0y;
                    coords[2] = cubic.cp1x; coords[3] = cubic.cp1y;
                    coords[4] = cubic.x1; coords[5] = cubic.y1;
                    _tailAngle = cubic.getPointAndAngle(1, _tailPoint);
                    break;
                }
            }

            // Set finished and return
            _lenRun = _lenMax;
            _hasNext = false;
            return _seg;
        }

        @Override
        public boolean hasNext()  { return _hasNext; }
    }
}
