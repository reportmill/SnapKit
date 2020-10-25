package snap.geom;

/**
 * A Segment representing a Quadratic curve.
 */
public class Quad extends Segment {

    // The control point
    double cpx, cpy;

    /**
     * Creates a new Quad.
     */
    public Quad(double aX0, double aY0, double aCPX, double aCPY, double aX1, double aY1)
    {
        x0 = aX0; y0 = aY0; cpx = aCPX; cpy = aCPY; x1 = aX1; y1 = aY1;
    }

    /**
     * Returns the points.
     */
    @Override
    public Point[] getPoints()
    {
        return new Point[] { new Point(x0, y0), new Point(cpx, cpy), new Point(x1, y1) };
    }

    /**
     * Sets the quad points.
     */
    public void setPoints(double aX0, double aY0, double aCPX, double aCPY, double aX1, double aY1)
    {
        x0 = aX0; y0 = aY0; cpx = aCPX; cpy = aCPY; x1 = aX1; y1 = aY1;
        shapeChanged();
    }

    /**
     * Returns the point coords.
     */
    public double[] getEndCoords(double coords[])
    {
        if (coords==null) coords = new double[4];
        coords[0] = cpx; coords[1] = cpy; coords[2] = x1; coords[3] = y1;
        return coords;
    }

    /**
     * Calculates and returns length of this segment.
     */
    protected double getArcLengthImpl()
    {
        return SegmentLengths.getArcLength(this, 0, 1);
    }

    /**
     * Returns the bounds.
     */
    protected Rect getBoundsImpl()
    {
        return getBounds(x0, y0, cpx, cpy, x1, y1, null);
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aT)
    {
        return new QuadIter(aT);
    }

    /**
     * Returns the x value at given parametric location.
     */
    public double getX(double aLoc)
    {
        double nxc0 = x0 + aLoc*(cpx - x0);
        double nxc1 = cpx + aLoc*(x1 - cpx);
        return nxc0 + aLoc*(nxc1 - nxc0); //double t=aLoc, s = 1 - t, s2 = s*s, t2 = t*t; return s2*x0 + 2*t*s*xc0 + t2*x1;
    }

    /**
     * Returns the y value at given parametric location.
     */
    public double getY(double aLoc)
    {
        double nyc0 = y0 + aLoc*(cpy - y0);
        double nyc1 = cpy + aLoc*(y1 - cpy);
        return nyc0 + aLoc*(nyc1 + nyc0); //double t=aLoc, s = 1 - t, s2 = s*s, t2 = t*t; return s2*y0 + 2*t*s*yc0 + t2*y1;
    }

    /**
     * Splits this Quad at given parametric location and return the remainder.
     */
    public Quad split(double aLoc)
    {
        // Calculate new control points to split quad in two
        double nxc0 = x0 + aLoc*(cpx - x0);
        double nyc0 = y0 + aLoc*(cpy - y0);
        double nxc1 = cpx + aLoc*(x1 - cpx);
        double nyc1 = cpy + aLoc*(y1 - cpy);
        double midpx = nxc0 + aLoc*(nxc1 - nxc0);
        double midpy = nyc0 + aLoc*(nyc1 - nyc0);

        // If either intersect, return true
        Quad rem = new Quad(midpx, midpy, nxc1, nyc1, x1, y1);
        cpx = nxc0; cpy = nyc0; x1 = midpx; y1 = midpy;
        shapeChanged();
        return rem;
    }

    /**
     * Creates and returns the reverse of this segement.
     */
    public Quad createReverse()  { return new Quad(x1, y1, cpx, cpy, x0, y0); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        Quad other = anObj instanceof Quad ? (Quad)anObj : null; if (other==null) return false;
        return equals(x0,other.x0) && equals(y0,other.y0) &&
            equals(cpx,other.cpx) && equals(cpy,other.cpy) &&
            equals(x1,other.x1) && equals(y1,other.y1);
    }

    /**
     * Returns whether quad is equal to another, regardless of direction.
     */
    public boolean matches(Object anObj)
    {
        if (equals(anObj)) return true;
        Quad other = anObj instanceof Quad ? (Quad)anObj : null; if (other==null) return false;
        return equals(x0,other.x1) && equals(y0,other.y1) &&
            equals(cpx,other.cpx) && equals(cpy,other.cpy) &&
            equals(x1,other.x0) && equals(y1,other.y0);
    }

    /**
     * Returns the hit for given segment.
     */
    public SegHit getHit(Segment aSeg)
    {
        if (aSeg instanceof Cubic) { Cubic s2 = (Cubic)aSeg;
            return SegHit.getHitQuadCubic(x0,y0,cpx,cpy,x1,y1, s2.x0,s2.y0,s2.cp0x,s2.cp0y,s2.cp1x,s2.cp1y,s2.x1,s2.y1);
        }

        if (aSeg instanceof Quad) { Quad s2 = (Quad)aSeg;
            return SegHit.getHitQuadQuad(x0,y0,cpx,cpy,x1,y1, s2.x0,s2.y0,s2.cpx,s2.cpy,s2.x1,s2.y1);
        }

        return SegHit.getHitQuadLine(x0,y0,cpx,cpy,x1,y1, aSeg.x0, aSeg.y0, aSeg.x1, aSeg.y1);
    }

    /**
     * Returns the bounds for given quad points.
     */
    public static Rect getBounds(double x0, double y0, double xc0, double yc0, double x1, double y1, Rect aRect)
    {
        // Add end points
        aRect = Line.getBounds(x0, y0, x1, y1, aRect);

        // this curve might have extrema:
        double ax = x0 - 2*xc0 + x1;
        double bx = -2*x0 + 2*xc0;
        double cx = x0;
        double tx = -bx/(2*ax);
        if (tx>0 && tx<1)
            aRect.addX(ax*tx*tx + bx*tx + cx);

        double ay = y0 - 2*yc0 + y1;
        double by = -2*y0 + 2*yc0;
        double cy = y0;
        double ty = -by/(2*ay);
        if (ty>0 && ty<1)
            aRect.addY(ay*ty*ty + by*ty + cy);

        return aRect;
    }

    /**
     * Returns the bounds of the give quadratic.
     */
    public static Rect getBounds2(double x0, double y0, double x1, double y1, double x2, double y2, Rect aRect)
    {
        // Declare coords for min/max points
        double p1x = x0, p1y = y0, p2x = x0, p2y = y0;

        // For quadratic, slope at point t is just linear interpolation of slopes at the endpoints.
        // Find solution to LERP(slope0,slope1,t) == 0
        double d = x0 - 2*x1 + x2;
        double t = d==0 ? 0 : (x0 - x1) / d;

        // If there's a valid solution, get actual x point at t and add it to the rect
        if (t>0 && t<1) {
            double turningpoint = x0*(1-t)*(1-t) + 2*x1*(1-t)*t + x2*t*t;
            p1x = Math.min(p1x, turningpoint);
            p2x = Math.max(p2x, turningpoint);
        }

        // Do the same for y
        d = y0 - 2*y1 + y2;
        t = d==0 ? 0 : (y0 - y1)/d;
        if (t>0 && t<1) {
            double turningpoint = y0*(1-t)*(1-t) + 2*y1*(1-t)*t + y2*t*t;
            p1y = Math.min(p1y, turningpoint);
            p2y = Math.max(p2y, turningpoint);
        }

        // Include endpoint
        p1x = Math.min(p1x, x2); p2x = Math.max(p2x, x2);
        p1y = Math.min(p1y, y2); p2y = Math.max(p2y, y2);

        // Set rect
        if (aRect==null)
            aRect = new Rect();
        aRect.setRect(p1x, p1y, p2x - p1x, p2y - p1y);
        return aRect;
    }

    /**
     * Returns the minimum distance from the given point to the curve.
     */
    public static double getDistanceSquared(double x0, double y0, double xc0, double yc0, double x1, double y1,
        double aX, double aY)
    {
        // If quad is really a line, return line version
        if (isLine(x0, y0, xc0, yc0, x1, y1))
            return Line.getDistanceSquared(x0, y0, x1, y1, aX, aY);

        // Calculate new control points to split quad in two
        double nxc0 = (x0 + xc0) / 2;
        double nyc0 = (y0 + yc0) / 2;
        double nxc1 = (x1 + xc0) / 2;
        double nyc1 = (y1 + yc0) / 2;
        double midpx = (nxc0 + nxc1) / 2;
        double midpy = (nyc0 + nyc1) / 2;

        // If either intersect, return true
        double d1 = getDistanceSquared(x0, y0, nxc0, nyc0, midpx, midpy, aX, aY);
        double d2 = getDistanceSquared(midpx, midpy, nxc1, nyc1, x1, y1, aX, aY);
        return Math.min(d1,d2);
    }

    /**
     * Returns the number of crossings for the ray from given point extending to the right.
     */
    public static int crossings(double x0, double y0, double xc, double yc, double x1, double y1, double px, double py,
        int level)
    {
        if (py<y0 && py<yc && py<y1) return 0;
        if (py>=y0 && py>=yc && py>=y1) return 0;

        // Note y0 could equal y1...
        if (px>=x0 && px>=xc && px>=x1) return 0;
        if (px<x0 && px<xc && px<x1) {
            if (py >= y0) {
                if (py < y1) return 1; }
            else { // py < y0
                if (py >= y1) return -1; }
            return 0; // py outside of y01 range, and/or y0==y1
        }

        // double precision only has 52 bits of mantissa
        if (level > 52) return Line.crossings(x0, y0, x1, y1, px, py);
        double x0c = (x0 + xc) / 2;
        double y0c = (y0 + yc) / 2;
        double xc1 = (xc + x1) / 2;
        double yc1 = (yc + y1) / 2;
        xc = (x0c + xc1) / 2;
        yc = (y0c + yc1) / 2;

        // [xy]c are NaN if any of [xy]0c or [xy]c1 are NaN
        // [xy]0c or [xy]c1 are NaN if any of [xy][0c1] are NaN
        // These values are also NaN if opposing infinities are added
        if (Double.isNaN(xc) || Double.isNaN(yc))
            return 0;

        int c1 = crossings(x0, y0, x0c, y0c, xc, yc, px, py, level+1);
        int c2 = crossings(xc, yc, xc1, yc1, x1, y1, px, py, level+1);
        return c1 + c2;
    }

    /**
     * Returns whether Quad for given points is effectively a line.
     */
    public static boolean isLine(double x0, double y0, double xc0, double yc0, double x1, double y1)
    {
        return Line.getDistanceSquared(x0,y0,x1,y1,xc0,yc0)<.01;
    }

    /**
     * Returns whether Quad for given points is intersected by line with given points.
     */
    public static boolean intersectsLine(double x0, double y0, double xc0, double yc0, double x1, double y1,
        double px0, double py0, double px1, double py1)
    {
        return SegHit.getHitQuadLine(x0, y0, xc0, yc0, x1, y1, px0, py0, px1, py1)!=null;
    }

    /**
     * Returns whether Quad for given points is intersected by Quad with given points.
     */
    public static boolean intersectsQuad(double x0, double y0, double xc0, double yc0, double x1, double y1,
        double px0, double py0, double pxc0, double pyc0, double px1, double py1)
    {
        return SegHit.getHitQuadQuad(x0, y0, xc0, yc0, x1, y1, px0, py0, pxc0, pyc0, px1, py1)!=null;
    }

    /**
     * PathIter for Quad.
     */
    private class QuadIter extends PathIter {

        /** Create new QuadIter. */
        QuadIter(Transform at) { super(at); } int index;

        /** Returns whether there are more segments. */
        public boolean hasNext() { return index<2; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double coords[])
        {
            switch (index++) {
                case 0: return moveTo(x0, y0, coords);
                case 1: return quadTo(cpx, cpy, x1, y1, coords);
                default: throw new RuntimeException("line iterator out of bounds");
            }
        }
    }
}