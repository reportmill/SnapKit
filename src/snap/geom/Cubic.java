package snap.geom;
import snap.util.FormatUtils;

/**
 * A Segment representing a Cubic curve.
 */
public class Cubic extends Segment {

    // The control points
    public double cp0x, cp0y, cp1x, cp1y;

    /**
     * Constructor.
     */
    public Cubic(double aX0, double aY0, double aXC0, double aYC0, double aXC1, double aYC1, double aX1, double aY1)
    {
        x0 = aX0; y0 = aY0; cp0x = aXC0; cp0y = aYC0; cp1x = aXC1; cp1y = aYC1; x1 = aX1; y1 = aY1;
    }

    /**
     * Returns the points.
     */
    @Override
    public Point[] getPoints()
    {
        return new Point[] { new Point(x0, y0), new Point(cp0x, cp0y), new Point(cp1x, cp1y), new Point(x1, y1) };
    }

    /**
     * Sets the quad points.
     */
    public void setPoints(double aX0, double aY0, double aXC0, double aYC0, double aXC1, double aYC1, double aX1, double aY1)
    {
        x0 = aX0; y0 = aY0; cp0x = aXC0; cp0y = aYC0; cp1x = aXC1; cp1y = aYC1; x1 = aX1; y1 = aY1;
        shapeChanged();
    }

    /**
     * Returns the point coords.
     */
    public double[] getEndCoords(double[] coords)
    {
        if (coords == null) coords = new double[6];
        coords[0] = cp1x; coords[1] = cp1y; coords[2] = cp1x; coords[3] = cp1y; coords[4] = x1; coords[5] = y1;
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
        return getBounds(x0, y0, cp0x, cp0y, cp1x, cp1y, x1, y1, null);
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aT)
    {
        return new CubicIter(aT);
    }

    /**
     * Returns the X value at given parametric location using De Casteljau's algorithm.
     * p' = (1-t)^3*p0 + 3*t*(1-t)^2*p1 + 3*t^2*(1-t)*p2 + t^3*p3.
     */
    public double getX(double aLoc)
    {
        double A = lerp(x0, cp0x, aLoc);
        double B = lerp(cp0x, cp1x, aLoc);
        double C = lerp(cp1x, x1, aLoc);
        double D = lerp(A, B, aLoc);
        double E = lerp(B, C, aLoc);
        return lerp(D, E, aLoc);
    }

    /**
     * Returns the Y value at given parametric location using De Casteljau's algorithm.
     */
    public double getY(double aLoc)
    {
        double A = lerp(y0, cp0y, aLoc);
        double B = lerp(cp0y, cp1y, aLoc);
        double C = lerp(cp1y, y1, aLoc);
        double D = lerp(A, B, aLoc);
        double E = lerp(B, C, aLoc);
        return lerp(D, E, aLoc);
    }

    /**
     * Splits this Cubic at given parametric location and return the remainder.
     */
    public Cubic split(double aLoc)
    {
        if (aLoc <= 0 || aLoc >= 1)
            System.err.println("Cubic.split: illegal split location: " + aLoc);

        // Calculate new x control points to split cubic into two
        double AX = lerp(x0, cp0x, aLoc);
        double BX = lerp(cp0x, cp1x, aLoc);
        double CX = lerp(cp1x, x1, aLoc);
        double DX = lerp(AX, BX, aLoc);
        double EX = lerp(BX, CX, aLoc);
        double FX = lerp(DX, EX, aLoc);

        // Calculate new y control points to split cubic into two
        double AY = lerp(y0, cp0y, aLoc);
        double BY = lerp(cp0y, cp1y, aLoc);
        double CY = lerp(cp1y, y1, aLoc);
        double DY = lerp(AY, BY, aLoc);
        double EY = lerp(BY, CY, aLoc);
        double FY = lerp(DY, EY, aLoc);

        // Create new remainder shape, update this shape and return remainder
        Cubic rem = new Cubic(FX, FY, EX, EY, CX, CY, x1, y1);
        setPoints(x0, y0, AX, AY, DX, DY, FX, FY);
        return rem;
    }

    /**
     * Returns the linear interpolation of given two values at given parametric value t.
     */
    private static double lerp(double p0, double p1, double t)  { return p0 + (p1 - p0) * t; }

    /**
     * Creates and returns the reverse of this segment.
     */
    public Cubic createReverse()  { return new Cubic(x1, y1, cp1x, cp1y, cp0x, cp0y, x0, y0); }

    /**
     * Returns the hit for given segment.
     */
    public SegHit getHit(Segment aSeg)
    {
        if (aSeg instanceof Cubic) {
            Cubic s2 = (Cubic) aSeg;
            return SegHit.getHitCubicCubic(x0, y0, cp0x, cp0y, cp1x, cp1y, x1, y1,
                    s2.x0, s2.y0, s2.cp0x, s2.cp0y, s2.cp1x, s2.cp1y, s2.x1, s2.y1);
        }

        if (aSeg instanceof Quad) {
            Quad s2 = (Quad) aSeg;
            return SegHit.getHitCubicQuad(x0, y0, cp0x, cp0y, cp1x, cp1y, x1, y1, s2.x0, s2.y0, s2.cpx, s2.cpy, s2.x1, s2.y1);
        }

        return SegHit.getHitCubicLine(x0, y0, cp0x, cp0y, cp1x, cp1y, x1, y1, aSeg.x0, aSeg.y0, aSeg.x1, aSeg.y1);
    }

    /**
     * Returns whether cubic is equal to another, regardless of direction.
     */
    public boolean matches(Object anObj)
    {
        if (equals(anObj)) return true;
        Cubic other = anObj instanceof Cubic ? (Cubic) anObj : null;
        if (other == null) return false;
        return equals(x0, other.x1) && equals(y0, other.y1) && equals(cp0x, other.cp1x) && equals(cp0y, other.cp1y) &&
                equals(cp1x, other.cp0x) && equals(cp1y, other.cp0y) && equals(x1, other.x0) && equals(y1, other.y0);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        Cubic other = anObj instanceof Cubic ? (Cubic) anObj : null;
        if (other == null) return false;
        return equals(x0, other.x0) && equals(y0, other.y0) && equals(cp0x, other.cp0x) && equals(cp0y, other.cp0y) &&
                equals(cp1x, other.cp1x) && equals(cp1y, other.cp1y) && equals(x1, other.x1) && equals(y1, other.y1);
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return String.format("Cubic { p0:(%s, %s), p1:(%s, %s), p2:(%s, %s), p3:(%s, %s) }",
                FormatUtils.formatNum(x0), FormatUtils.formatNum(y0), FormatUtils.formatNum(cp0x),
                FormatUtils.formatNum(cp0y), FormatUtils.formatNum(cp1x), FormatUtils.formatNum(cp1y),
                FormatUtils.formatNum(x1), FormatUtils.formatNum(y1));
    }

    /**
     * Returns the bounds for given quad points.
     */
    public static Rect getBounds(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1, Rect aRect)
    {
        // Add end points
        aRect = Line.getBounds(x0, y0, x1, y1, aRect);

        // This curve might have extrema:
        // f = a*t*t*t+b*t*t+c*t+d
        // df/dt = 3*a*t*t+2*b*t+c
        // A = 3*a, B = 2*b, C = c
        // t = [-B+-sqrt(B^2-4*A*C)]/(2A)
        // t = (-2*b+-sqrt(4*b*b-12*a*c)]/(6*a)
        double ax = -x0 + 3 * xc0 - 3 * xc1 + x1;
        double bx = 3 * x0 - 6 * xc0 + 3 * xc1;
        double cx = -3 * x0 + 3 * xc0;
        double dx = x0;
        double detx = (4 * bx * bx - 12 * ax * cx);

        // No solutions
        if (detx < 0) {
        }

        // One solution
        else if (detx == 0) {
            double tx = -2 * bx / (6 * ax);
            if (tx > 0 && tx < 1)
                aRect.addX(ax * tx * tx * tx + bx * tx * tx + cx * tx + dx);
        }

        // Two solutions
        else {
            detx = Math.sqrt(detx);
            double tx = (-2 * bx + detx) / (6 * ax);
            if (tx > 0 && tx < 1)
                aRect.addX(ax * tx * tx * tx + bx * tx * tx + cx * tx + dx);
            tx = (-2 * bx - detx) / (6 * ax);
            if (tx > 0 && tx < 1)
                aRect.addX(ax * tx * tx * tx + bx * tx * tx + cx * tx + dx);
        }

        // Do the same for y
        double ay = -y0 + 3 * yc0 - 3 * yc1 + y1;
        double by = 3 * y0 - 6 * yc0 + 3 * yc1;
        double cy = -3 * y0 + 3 * yc0, dy = y0;
        double dety = (4 * by * by - 12 * ay * cy);

        // No solutions
        if (dety < 0) {
        }

        // One solution
        else if (dety == 0) {
            double ty = -2 * by / (6 * ay);
            if (ty > 0 && ty < 1)
                aRect.addY(ay * ty * ty * ty + by * ty * ty + cy * ty + dy);
        }

        // Two solutions
        else {
            dety = Math.sqrt(dety);
            double ty = (-2 * by + dety) / (6 * ay);
            if (ty > 0 && ty < 1)
                aRect.addY(ay * ty * ty * ty + by * ty * ty + cy * ty + dy);
            ty = (-2 * by - dety) / (6 * ay);
            if (ty > 0 && ty < 1)
                aRect.addY(ay * ty * ty * ty + by * ty * ty + cy * ty + dy);
        }

        return aRect;
    }

    /**
     * Returns the bounds of the bezier.
     */
    public static Rect getBounds2(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, Rect aRect)
    {
        // Declare coords for min/max points
        double p1x = x0, p1y = y0, p2x = x0, p2y = y0;

        // Get coeficients of b-curve parametric equations (1-t)^3*x0 + 3*t*(1-t)^2*x1 + 3*t^2*(1-t)*x2 + t^3*x3
        // Take derivative of above function and solve for t where derivative equation = 0 (I used Mathematica).
        //   Since derivative of bezier cubic is quadradic, solution is of form (-b +- sqrt(b^2-4ac))/2a.
        double aX = -x0 + 3 * x1 - 3 * x2 + x3;
        double bX = 2 * x0 - 4 * x1 + 2 * x2;
        double cX = -x0 + x1, bSqrMin4acForX = bX * bX - 4 * aX * cX;

        // If square root part x is at least zero, there is a local max & min on bezier curve for x.
        if (bSqrMin4acForX >= 0) {

            // Declare variables for the two solutions
            double t1 = -1, t2 = -1;

            // If A is zero, the eqn reduces to a simple linear equation (Using the quadratic here would give NaNs)
            if (aX == 0)
                t1 = bX == 0 ? 0 : -cX / bX;

            // Otherwise, solve for tMax(-b + sqrt(b^2-4ac)/2a) and tMin(-b - sqrt(b^2-4ac)/2a)
            else {
                t1 = (-bX - Math.sqrt(bSqrMin4acForX)) / (2 * aX);
                t2 = (-bX + Math.sqrt(bSqrMin4acForX)) / (2 * aX);
            }

            // If t1 is in valid range (0 to 1), solve for x value and use it to expand bounds
            if (t1 >= 0 && t1 <= 1) {
                double x = Math.pow(1 - t1, 3) * x0 + 3 * t1 * Math.pow(1 - t1, 2) * x1 + 3 * Math.pow(t1, 2) * (1 - t1) * x2 + Math.pow(t1, 3) * x3;
                p1x = Math.min(p1x, x);
                p2x = Math.max(p2x, x);
            }

            // If t2 is in valid range (0 to 1), solve for x value and use it to expand bounds
            if (t2 >= 0 && t2 <= 1) {
                double x = Math.pow(1 - t2, 3) * x0 + 3 * t2 * Math.pow(1 - t2, 2) * x1 + 3 * Math.pow(t2, 2) * (1 - t2) * x2 + Math.pow(t2, 3) * x3;
                p1x = Math.min(p1x, x);
                p2x = Math.max(p2x, x);
            }
        }

        // Do the same for y
        double aY = -y0 + 3 * y1 - 3 * y2 + y3;
        double bY = 2 * y0 - 4 * y1 + 2 * y2;
        double cY = -y0 + y1, bSqrMin4acForY = bY * bY - 4 * aY * cY;
        if (bSqrMin4acForY >= 0) {

            // Declare variables for the two solutions
            double t1 = -1, t2 = -1;

            // If A is zero, the eqn reduces to a linear. (or possibly a point if B is zero)
            if (aY == 0)
                t1 = (bY == 0) ? 0 : -cY / bY;

            // Otherwise, solve for tMax(-b + sqrt(b^2-4ac)/2a) and tMin(-b - sqrt(b^2-4ac)/2a)
            else {
                t1 = (-bY - Math.sqrt(bSqrMin4acForY)) / (2 * aY);
                t2 = (-bY + Math.sqrt(bSqrMin4acForY)) / (2 * aY);
            }

            // If tMin is in valid range (0 to 1), solve for x value and use it to expand bounds
            if ((t1 >= 0) && (t1 <= 1)) {
                double y = Math.pow(1 - t1, 3) * y0 + 3 * t1 * Math.pow(1 - t1, 2) * y1 + 3 * Math.pow(t1, 2) * (1 - t1) * y2 +
                        Math.pow(t1, 3) * y3;
                p1y = Math.min(p1y, y);
                p2y = Math.max(p2y, y);
            }

            // If tMax is in valid range (0 to 1), solve for x value and use it to expand bounds
            if ((t2 >= 0) && (t2 <= 1)) {
                double y = Math.pow(1 - t2, 3) * y0 + 3 * t2 * Math.pow(1 - t2, 2) * y1 + 3 * Math.pow(t2, 2) * (1 - t2) * y2 +
                        Math.pow(t2, 3) * y3;
                p1y = Math.min(p1y, y);
                p2y = Math.max(p2y, y);
            }
        }

        // Evaluate bounds expansion for curve endpoint
        p1x = Math.min(p1x, x3);
        p1y = Math.min(p1y, y3);
        p2x = Math.max(p2x, x3);
        p2y = Math.max(p2y, y3);

        // Set rect
        if (aRect == null)
            aRect = new Rect();
        aRect.setRect(p1x, p1y, p2x - p1x, p2y - p1y);

        // Return
        return aRect;
    }

    /**
     * Returns whether Cubic for given points is intersected by line with given points.
     */
    public static double getDistanceSquared(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
                                            double x1, double y1, double aX, double aY)
    {
        // If cubic is really a line, return line version
        if (isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
            return Line.getDistanceSquared(x0, y0, x1, y1, aX, aY);

        // Calculate new control points to split cubic into two
        double AX = (x0 + xc0) / 2;
        double BX = (xc0 + xc1) / 2;
        double CX = (xc1 + x1) / 2;
        double DX = (AX + BX) / 2;
        double EX = (BX + CX) / 2;
        double FX = (DX + EX) / 2;

        double AY = (y0 + yc0) / 2;
        double BY = (yc0 + yc1) / 2;
        double CY = (yc1 + y1) / 2;
        double DY = (AY + BY) / 2;
        double EY = (BY + CY) / 2;
        double FY = (DY + EY) / 2;

        // If either intersect, return true
        double d1 = getDistanceSquared(x0, y0, AX, AY, DX, DY, FX, FY, aX, aY);
        double d2 = getDistanceSquared(FX, FY, EX, EY, CX, CY, x1, y1, aX, aY);
        return Math.min(d1, d2);
    }

    /**
     * Returns the number of crossings for the ray from given point extending to the right.
     */
    public static int crossings(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1,
                                double px, double py, int level)
    {
        // If point is above, below or to right of all curve points, return 0
        if (py >= y0 && py >= yc0 && py >= yc1 && py >= y1)
            return 0;
        if (py < y0 && py < yc0 && py < yc1 && py < y1)
            return 0;
        if (px >= x0 && px >= xc0 && px >= xc1 && px >= x1)
            return 0;

        // If point to the left of all curve points...
        if (px < x0 && px < xc0 && px < xc1 && px < x1) {
            if (py >= y0) {
                if (py < y1) return 1;
            }
            else { // py < y0
                if (py >= y1) return -1;
            }
            return 0;  // py outside of y01 range, and/or y0==yc0
        }

        // Double precision only has 52 bits of mantissa
        if (level > 52) return Line.crossings(x0, y0, x1, y1, px, py);
        double xmid = (xc0 + xc1) / 2;
        double ymid = (yc0 + yc1) / 2;
        xc0 = (x0 + xc0) / 2;
        yc0 = (y0 + yc0) / 2;
        xc1 = (xc1 + x1) / 2;
        yc1 = (yc1 + y1) / 2;
        double xc0m = (xc0 + xmid) / 2;
        double yc0m = (yc0 + ymid) / 2;
        double xmc1 = (xmid + xc1) / 2;
        double ymc1 = (ymid + yc1) / 2;
        xmid = (xc0m + xmc1) / 2;
        ymid = (yc0m + ymc1) / 2;

        // [xy]mid are NaN if any of [xy]c0m or [xy]mc1 are NaN
        // [xy]c0m or [xy]mc1 are NaN if any of [xy][c][01] are NaN
        // These values are also NaN if opposing infinities are added
        if (Double.isNaN(xmid) || Double.isNaN(ymid))
            return 0;

        int c1 = crossings(x0, y0, xc0, yc0, xc0m, yc0m, xmid, ymid, px, py, level + 1);
        int c2 = crossings(xmid, ymid, xmc1, ymc1, xc1, yc1, x1, y1, px, py, level + 1);
        return c1 + c2;
    }

    /**
     * Returns whether Cubic for given points is effectively a line.
     */
    public static boolean isLine(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1)
    {
        return Line.getDistanceSquared(x0, y0, x1, y1, xc0, yc0) < .00001 && Line.getDistanceSquared(x0, y0, x1, y1, xc1, yc1) < .00001;
    }

    /**
     * PathIter for Cubic.
     */
    private class CubicIter extends PathIter {

        int index;

        /** Constructor. */
        CubicIter(Transform at)  { super(at); }

        /** Returns whether there are more segments. */
        public boolean hasNext()  { return index < 2; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double[] coords)
        {
            switch (index++) {
                case 0: return moveTo(x0, y0, coords);
                case 1: return cubicTo(cp0x, cp0y, cp1x, cp1y, x1, y1, coords);
                default: throw new RuntimeException("line iterator out of bounds");
            }
        }
    }
}