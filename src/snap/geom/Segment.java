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
    public double[] getEndCoords(double coords[])
    {
        if (coords==null) coords = new double[2];
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
        if (_len>=0) return _len;
        return _len = getArcLengthImpl();
    }

    /**
     * Calculates and returns length of this segment.
     */
    protected abstract double getArcLengthImpl();

    /**
     * Splits the segement at given parametric location and return the remainder.
     */
    public abstract Segment split(double aLoc);

    /**
     * Creates and returns the reverse of this segement.
     */
    public abstract Segment createReverse();

    /**
     * Returns whether segement is equal to another, regardless of direction.
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
        return getHit(aSeg)!=null;
    }

    /**
     * Returns whether this segment intersects given segment.
     */
    public boolean crossesSeg(Segment aSeg)
    {
        return getHit(aSeg)!=null;
    }

    /**
     * Returns the hit for given segment.
     */
    public SegHit getHit(Segment aSeg)
    {
        throw new RuntimeException("Segement.getHit: Unsupported class " + getClass());
    }

    /**
     * Returns the hit for given segment.
     */
    public double getHitPoint(Segment aSeg)
    {
        SegHit hit = getHit(aSeg);
        return hit!=null ? hit.h0 : -1;
    }

    /**
     * Like getPoint(), but also returns angle of tangent at point t (in degrees).
     *   calculated point along curve is stored in aPoint
     */
    public double getPointAndAngle(double t, Point aPoint)
    {
        Point cpts[] = getPoints();
        Size tangent = new Size();

        evaluateBezierAndTangent(cpts.length-1, t, cpts, aPoint, tangent);
        return Math.atan2(tangent.height, tangent.width)*180/Math.PI;
    }

    /**
     * Simultaneously find point on curve, as well as the tangent at that point.
     */
    private static void evaluateBezierAndTangent(int degree, double t, Point cpts[], Point tpoint, Size tan)
    {
        // Special case for endpoints.  If one (or more) of the control points is the same as an endpoint, the tangent calculation
        // in the de Casteljau algorithm will return a point instead of the real tangent.
        if (t==0) {
            tpoint.setXY(cpts[0]);
            for (int i=1; i<=degree; ++i)
                if (!cpts[i].equals(cpts[0])) {
                    tan.width = cpts[i].x - cpts[0].x;
                    tan.height = cpts[i].y - cpts[0].y;
                    break;
                }
        }

        else if (t==1) {
            tpoint.setXY(cpts[degree]);
            for (int i=degree-1; i>=0; --i)
                if (!cpts[i].equals(cpts[degree])) {
                    tan.width = cpts[degree].x - cpts[i].x;
                    tan.height = cpts[degree].y - cpts[i].y;
                    break;
                }
        }

        else {
            int nfloats = 2*(degree+1);
            double points[] = new double[nfloats];

            int i = 0, j = 0;
            while (i<nfloats) {
                points[i++] = cpts[j].x;
                points[i++] = cpts[j++].y;
            }

            // Triangle computation
            for (i=1; i<= degree; i++) {
                if (i==degree) {
                    tan.width = points[2] - points[0];
                    tan.height = points[3] - points[1];
                }
                for (j=0; j <= 2*(degree-i)+1; j++) {
                    points[j] = (1.0 - t) * points[j] + t * points[j+2];
                }
            }
            tpoint.x = points[0];
            tpoint.y = points[1];
        }

        tan.normalize();
    }

    /**
     * Returns whether double values are equal to nearest tenth of pixel.
     */
    public static final boolean equals(double v1, double v2)
    {
        return Math.abs(v1 - v2) < 0.1;
    }

    /**
     * Creates a Segment for given Seg and points.
     */
    public static Segment newSegmentForSegAndPoints(Seg aSeg, double coords[])
    {
        if (aSeg==Seg.LineTo)
            return new Line(coords[0], coords[1], coords[2], coords[3]);
        if (aSeg==Seg.QuadTo)
            return new Quad(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
        if (aSeg==Seg.CubicTo)
            return new Cubic(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]);
        return null;
    }
}