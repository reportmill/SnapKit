package snap.geom;

/**
 * A Segment is a Shape subclass that represents a part of a path: line, quadratic, cubic.
 */
public abstract class Segment extends Shape {

    // Ivars
    public double x0, y0, x1, y1;

    // The cached length of the segment
    private double  _len = -1;
    
    /**
     * Returns the first point x.
     */
    public double getX0()  { return x0; }

    /**
     * Returns the first point y.
     */
    public double getY0()  { return y0; }

    /**
     * Returns the second point x.
     */
    public double getX1()  { return x1; }

    /**
     * Returns the second point y.
     */
    public double getY1()  { return y1; }

    /**
     * Returns the points.
     */
    public Point[] getPoints()
    {
        return new Point[] { new Point(x0, y0), new Point(x1, y1) };
    }

    /**
     * Returns the point coords.
     */
    public double[] getEndCoords(double[] coords)
    {
        if (coords == null) coords = new double[2];
        coords[0] = x1; coords[1] = y1;
        return coords;
    }

    /**
     * Returns the x value at given parametric location.
     */
    public abstract double getX(double aLoc);

    /**
     * Returns the y value at given parametric location.
     */
    public abstract double getY(double aLoc);

    /**
     * Returns the length of this segment.
     */
    public double getArcLength()
    {
        if (_len >= 0) return _len;
        return _len = getArcLengthImpl();
    }

    /**
     * Calculates and returns length of this segment.
     */
    protected abstract double getArcLengthImpl();

    /**
     * Splits the segment at given parametric location and return the remainder.
     */
    public abstract Segment split(double aLoc);

    /**
     * Creates and returns the reverse of this segment.
     */
    public abstract Segment createReverse();

    /**
     * Returns whether segment is equal to another, regardless of direction.
     */
    public abstract boolean matches(Object anObj);

    /**
     * Override to return false (segment can never contain another segment (well, I suppose a weird Cubic could)).
     */
    public boolean containsSeg(Segment aSeg)  { return false; }

    /**
     * Returns whether this segment intersects given segment.
     */
    public boolean intersectsSeg(Segment aSeg)
    {
        return getHit(aSeg) != null;
    }

    /**
     * Returns whether this segment intersects given segment.
     */
    public boolean crossesSeg(Segment aSeg)
    {
        return getHit(aSeg) != null;
    }

    /**
     * Returns the hit for given segment.
     */
    public SegHit getHit(Segment aSeg)
    {
        throw new RuntimeException("Segment.getHit: Unsupported class " + getClass());
    }

    /**
     * Returns the hit for given segment.
     */
    public double getHitPoint(Segment aSeg)
    {
        SegHit hit = getHit(aSeg);
        return hit != null ? hit.h0 : -1;
    }

    /**
     * Like getPoint(), but also returns angle of tangent at point t (in degrees).
     */
    public PointAndAngle getPointAndAngle(double aLoc)
    {
        Point[] cpts = getPoints();
        int degree = cpts.length - 1;
        double pointX, pointY;
        double tanW = 0;
        double tanH = 0;

        // Special case for endpoints.  If one (or more) of the control points is the same as an endpoint, the tangent calculation
        // in the de Casteljau algorithm will return a point instead of the real tangent.
        if (aLoc == 0) {
            pointX = cpts[0].x;
            pointY = cpts[0].y;
            for (int i = 1; i <= degree; ++i)
                if (!cpts[i].equals(cpts[0])) {
                    tanW = cpts[i].x - cpts[0].x;
                    tanH = cpts[i].y - cpts[0].y;
                    break;
                }
        }

        else if (aLoc == 1) {
            pointX = cpts[degree].x;
            pointY = cpts[degree].y;
            for (int i = degree-1; i >= 0; --i)
                if (!cpts[i].equals(cpts[degree])) {
                    tanW = cpts[degree].x - cpts[i].x;
                    tanH = cpts[degree].y - cpts[i].y;
                    break;
                }
        }

        else {
            int nfloats = 2 * (degree + 1);
            double[] points = new double[nfloats];

            int i = 0, j = 0;
            while (i < nfloats) {
                points[i++] = cpts[j].x;
                points[i++] = cpts[j++].y;
            }

            // Triangle computation
            for (i = 1; i <= degree; i++) {
                if (i == degree) {
                    tanW = points[2] - points[0];
                    tanH = points[3] - points[1];
                }
                for (j = 0; j <= 2 * (degree - i) + 1; j++) {
                    points[j] = (1 - aLoc) * points[j] + aLoc * points[j+2];
                }
            }
            pointX = points[0];
            pointY = points[1];
        }

        // Get angle at location
        double magnitude = Math.sqrt(tanW * tanW + tanH * tanH);
        tanW /= magnitude; tanH /= magnitude;
        double tanAngle = Math.atan2(tanW, tanH) * 180 / Math.PI;

        // Return point and angle
        return new PointAndAngle(pointX, pointY, tanAngle);
    }

    /**
     * Holds a point and angle.
     */
    public record PointAndAngle(double x, double y, double angle) {
        public Point point() { return new Point(x,y); }
    }

    /**
     * Returns whether double values are equal to nearest tenth of pixel.
     */
    public static boolean equals(double v1, double v2)  { return Math.abs(v1 - v2) < 0.1; }

    /**
     * Creates a Segment for given Seg and points.
     */
    public static Segment newSegmentForSegAndPoints(Seg aSeg, double[] coords)
    {
        switch (aSeg) {
            case LineTo: return new Line(coords[0], coords[1], coords[2], coords[3]);
            case QuadTo: return new Quad(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
            case CubicTo: return new Cubic(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]);
            default: return null;
        }
    }
}