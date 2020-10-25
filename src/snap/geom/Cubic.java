package snap.geom;

/**
 * A Segment representing a Cubic curve.
 */
public class Cubic extends Segment {

    // The control points
    public double cp0x, cp0y, cp1x, cp1y;

    /**
     * Creates a new Cubic.
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
    public double[] getEndCoords(double coords[])
    {
        if (coords==null) coords = new double[6];
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
     * Returns the x value at given parametric location. p' = (1-t)^3*p0 + 3*t*(1-t)^2*p1 + 3*t^2*(1-t)*p2 + t^3*p3.
     */
    public double getX(double aLoc)
    {
        //double t=aLoc, s=1-t, s2 = s*s, s3 = s2*s, t2 = t*t, t3 = t2*t; return s3*x0 + 3*t*s2*xc0 + 3*t2*s*xc1 + t3*x1;
        double nxc0 = average(x0, cp0x, aLoc);
        double xca = average(cp0x, cp1x, aLoc);
        double nxc1 = average(nxc0, xca, aLoc);
        double nxc3 = average(cp1x, x1, aLoc);
        double nxc2 = average(nxc3, xca, aLoc);
        return average(nxc1, nxc2, aLoc);
    }

    /**
     * Returns the y value at given parametric location. p' = (1-t)^3*p0 + 3*t*(1-t)^2*p1 + 3*t^2*(1-t)*p2 + t^3*p3.
     */
    public double getY(double aLoc)
    {
        //double t=aLoc, s=1-t, s2 = s*s, s3 = s2*s, t2 = t*t, t3 = t2*t; return s3*y0 + 3*t*s2*yc0 + 3*t2*s*yc1 + t3*y1;
        double nyc0 = average(y0, cp0y, aLoc);
        double yca = average(cp0y, cp1y, aLoc);
        double nyc1 = average(nyc0, yca, aLoc);
        double nyc3 = average(cp1y, y1, aLoc);
        double nyc2 = average(nyc3, yca, aLoc);
        return average(nyc1, nyc2, aLoc);
    }

    /**
     * Splits this Cubic at given parametric location and return the remainder.
     */
    public Cubic split(double aLoc)
    {
        // Calculate new x control points to split cubic into two
        double nxc0 = average(x0, cp0x, aLoc);
        double xca = average(cp0x, cp1x, aLoc);
        double nxc3 = average(cp1x, x1, aLoc);
        double nxc1 = average(nxc0, xca, aLoc);
        double nxc2 = average(xca, nxc3, aLoc);
        double midpx = average(nxc1, nxc2, aLoc);

        // Calculate new y control points to split cubic into two
        double nyc0 = average(y0, cp0y, aLoc);
        double yca = average(cp0y, cp1y, aLoc);
        double nyc3 = average(cp1y, y1, aLoc);
        double nyc1 = average(nyc0, yca, aLoc);
        double nyc2 = average(yca, nyc3, aLoc);
        double midpy = average(nyc1, nyc2, aLoc);

        // Create new remainder shape, update this shape and return remainder
        Cubic rem = new Cubic(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1);
        cp0x = nxc0; cp0y = nyc0;
        cp1x = nxc1; cp1y = nyc1;
        x1 = midpx; y1 = midpy;
        shapeChanged();
        return rem;
    }

    /**
     * Returns the weighted average of this point with another point.
     */
    private static final double average(double x0, double x1, double t)  { return x0 + t*(x1 - x0); }

    /**
     * Creates and returns the reverse of this segement.
     */
    public Cubic createReverse()
    {
        return new Cubic(x1, y1, cp1x, cp1y, cp0x, cp0y, x0, y0);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        Cubic other = anObj instanceof Cubic ? (Cubic)anObj : null; if (other==null) return false;
        return equals(x0,other.x0) && equals(y0,other.y0) && equals(cp0x,other.cp0x) && equals(cp0y,other.cp0y) &&
            equals(cp1x,other.cp1x) && equals(cp1y,other.cp1y) && equals(x1,other.x1) && equals(y1,other.y1);
    }

    /**
     * Returns whether cubic is equal to another, regardless of direction.
     */
    public boolean matches(Object anObj)
    {
        if (equals(anObj)) return true;
        Cubic other = anObj instanceof Cubic ? (Cubic)anObj : null; if (other==null) return false;
        return equals(x0,other.x1) && equals(y0,other.y1) && equals(cp0x,other.cp1x) && equals(cp0y,other.cp1y) &&
            equals(cp1x,other.cp0x) && equals(cp1y,other.cp0y) && equals(x1,other.x0) && equals(y1,other.y0);
    }

    /**
     * Returns the hit for given segment.
     */
    public SegHit getHit(Segment aSeg)
    {
        if (aSeg instanceof Cubic) { Cubic s2 = (Cubic)aSeg;
            return SegHit.getHitCubicCubic(x0,y0,cp0x,cp0y,cp1x,cp1y,x1,y1,
                  s2.x0,s2.y0,s2.cp0x,s2.cp0y,s2.cp1x,s2.cp1y,s2.x1,s2.y1);
        }

        if (aSeg instanceof Quad) { Quad s2 = (Quad)aSeg;
            return SegHit.getHitCubicQuad(x0,y0,cp0x,cp0y,cp1x,cp1y,x1,y1, s2.x0,s2.y0,s2.cpx,s2.cpy,s2.x1,s2.y1);
        }

        return SegHit.getHitCubicLine(x0,y0,cp0x,cp0y,cp1x,cp1y,x1,y1, aSeg.x0, aSeg.y0, aSeg.x1, aSeg.y1);
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
        double ax = -x0 + 3*xc0 - 3*xc1 + x1, bx = 3*x0 - 6*xc0 + 3*xc1, cx = -3*x0 + 3*xc0, dx = x0;
        double detx = (4*bx*bx - 12*ax*cx);
        if (detx<0) { } // No solutions
        else if (detx==0) { // One solution
           double tx = -2*bx/(6*ax);
           if (tx>0 && tx<1) aRect.addX(ax*tx*tx*tx + bx*tx*tx + cx*tx + dx); }
       else { // Two solutions
           detx = Math.sqrt(detx); double tx = (-2*bx + detx)/(6*ax);
           if (tx>0 && tx<1) aRect.addX(ax*tx*tx*tx + bx*tx*tx + cx*tx + dx);
           tx = (-2*bx - detx)/(6*ax);
           if (tx>0 && tx<1) aRect.addX(ax*tx*tx*tx + bx*tx*tx + cx*tx + dx);
       }

       // Do the same for y
       double ay = -y0 + 3*yc0 - 3*yc1 + y1;
       double by = 3*y0 - 6*yc0 + 3*yc1;
       double cy = -3*y0 + 3*yc0, dy = y0;
       double dety = (4*by*by - 12*ay*cy);
       if (dety<0) { } // No solutions
       else if (dety==0) { // One solution
           double ty = -2*by/(6*ay);
           if (ty>0 && ty<1)
               aRect.addY(ay*ty*ty*ty + by*ty*ty + cy*ty + dy);
       }
       else { // Two solutions
           dety = Math.sqrt(dety); double ty = (-2*by + dety)/(6*ay);
           if (ty>0 && ty<1)
               aRect.addY(ay*ty*ty*ty + by*ty*ty + cy*ty + dy);
           ty = (-2*by - dety)/(6*ay);
           if (ty>0 && ty<1)
               aRect.addY(ay*ty*ty*ty + by*ty*ty + cy*ty + dy);
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
        double aX = -x0 + 3*x1 - 3*x2 + x3;
        double bX = 2*x0 - 4*x1 + 2*x2;
        double cX = -x0 + x1, bSqrMin4acForX = bX*bX - 4*aX*cX;

        // If square root part x is at least zero, there is a local max & min on bezier curve for x.
        if (bSqrMin4acForX >= 0) {

            // Declare variables for the two solutions
            double t1 = -1, t2 = -1;

            // If A is zero, the eqn reduces to a simple linear equation (Using the quadratic here would give NaNs)
            if (aX==0)
                t1 = bX==0 ? 0 : -cX/bX;

            // Otherwise, solve for tMax(-b + sqrt(b^2-4ac)/2a) and tMin(-b - sqrt(b^2-4ac)/2a)
            else {
                t1 = (-bX - Math.sqrt(bSqrMin4acForX))/(2*aX);
                t2 = (-bX + Math.sqrt(bSqrMin4acForX))/(2*aX);
            }

            // If t1 is in valid range (0 to 1), solve for x value and use it to expand bounds
            if (t1>=0 && t1<=1) {
                double x = Math.pow(1-t1, 3)*x0 + 3*t1*Math.pow(1-t1, 2)*x1 + 3*Math.pow(t1, 2)*(1-t1)*x2 + Math.pow(t1,3)*x3;
                p1x = Math.min(p1x, x);
                p2x = Math.max(p2x, x);
            }

            // If t2 is in valid range (0 to 1), solve for x value and use it to expand bounds
            if (t2>=0 && t2<=1) {
                double x = Math.pow(1-t2, 3)*x0 + 3*t2*Math.pow(1-t2, 2)*x1 + 3*Math.pow(t2, 2)*(1-t2)*x2 + Math.pow(t2,3)*x3;
                p1x = Math.min(p1x, x);
                p2x = Math.max(p2x, x);
            }
        }

        // Do the same for y
        double aY = -y0 + 3*y1 - 3*y2 + y3;
        double bY = 2*y0 - 4*y1 + 2*y2;
        double cY = -y0 + y1, bSqrMin4acForY = bY*bY - 4*aY*cY;
        if (bSqrMin4acForY >= 0) {

            // Declare variables for the two solutions
            double t1 = -1, t2 = -1;

            // If A is zero, the eqn reduces to a linear. (or possibly a point if B is zero)
            if (aY==0)
                t1 = (bY==0) ? 0 : -cY/bY;

            // Otherwise, solve for tMax(-b + sqrt(b^2-4ac)/2a) and tMin(-b - sqrt(b^2-4ac)/2a)
            else {
                t1 = (-bY - Math.sqrt(bSqrMin4acForY))/(2*aY);
                t2 = (-bY + Math.sqrt(bSqrMin4acForY))/(2*aY);
            }

            // If tMin is in valid range (0 to 1), solve for x value and use it to expand bounds
            if ((t1 >=0) && (t1 <= 1)) {
                double y = Math.pow(1-t1, 3)*y0 + 3*t1*Math.pow(1-t1, 2)*y1 + 3*Math.pow(t1, 2)*(1-t1)*y2 +
                    Math.pow(t1,3)*y3;
                p1y = Math.min(p1y, y);
                p2y = Math.max(p2y, y);
            }

            // If tMax is in valid range (0 to 1), solve for x value and use it to expand bounds
            if ((t2 >=0) && (t2 <= 1)) {
                double y = Math.pow(1-t2, 3)*y0 + 3*t2*Math.pow(1-t2, 2)*y1 + 3*Math.pow(t2, 2)*(1-t2)*y2 +
                    Math.pow(t2,3)*y3;
                p1y = Math.min(p1y, y);
                p2y = Math.max(p2y, y);
            }
        }

        // Evaluate bounds expansion for curve endpoint
        p1x = Math.min(p1x, x3); p1y = Math.min(p1y, y3); p2x = Math.max(p2x, x3); p2y = Math.max(p2y, y3);

        // Set rect
        if (aRect==null) aRect = new Rect(); aRect.setRect(p1x, p1y, p2x - p1x, p2y - p1y);
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
        double nxc0 = (x0 + xc0) / 2;
        double nyc0 = (y0 + yc0) / 2;
        double xca = (xc0 + xc1) / 2;
        double yca = (yc0 + yc1) / 2;
        double nxc1 = (nxc0 + xca) / 2;
        double nyc1 = (nyc0 + yca) / 2;
        double nxc3 = (xc1 + x1) / 2;
        double nyc3 = (yc1 + y1) / 2;
        double nxc2 = (nxc3 + xca) / 2;
        double nyc2 = (nyc3 + yca) / 2;
        double midpx = (nxc1 + nxc2) / 2;
        double midpy = (nyc1 + nyc2) / 2;

        // If either intersect, return true
        double d1 = getDistanceSquared(x0, y0, nxc0, nyc0, nxc1, nyc1, midpx, midpy, aX, aY);
        double d2 = getDistanceSquared(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1, aX, aY);
        return Math.min(d1,d2);
    }

    /**
     * Returns the number of crossings for the ray from given point extending to the right.
     */
    public static int crossings(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1,
        double px, double py, int level)
    {
        // If point is above, below or to right of all curve points, return 0
        if (py>=y0 && py>=yc0 && py>=yc1 && py>=y1) return 0;
        if (py<y0 && py<yc0 && py<yc1 && py<y1) return 0;
        if (px>=x0 && px>=xc0 && px>=xc1 && px>=x1) return 0;

        // If point to the left of all curve points...
        if (px<x0 && px<xc0 && px<xc1 && px<x1) {
            if (py>=y0) {
                if (py<y1) return 1; }
            else { // py < y0
                if (py>=y1) return -1; }
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
        if (Double.isNaN(xmid) || Double.isNaN(ymid)) return 0;

        int c1 = crossings(x0, y0, xc0, yc0, xc0m, yc0m, xmid, ymid, px, py, level+1);
        int c2 = crossings(xmid, ymid, xmc1, ymc1, xc1, yc1, x1, y1, px, py, level+1);
        return c1 + c2;
    }

    /**
     * Returns whether Cubic for given points is effectively a line.
     */
    public static boolean isLine(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1)
    {
        //if (Point.getDistanceSquared(x0,y0,x1,y1)<.1) return true;
        return Line.getDistanceSquared(x0,y0,x1,y1,xc0,yc0)<.01 && Line.getDistanceSquared(x0,y0,x1,y1,xc1,yc1)<.01;
    }

    /**
     * Returns whether Cubic for given points is intersected by line with given points.
     */
    public static boolean intersectsLine(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
        double x1, double y1, double px0, double py0, double px1, double py1)
    {
        return SegHit.getHitCubicLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, px1, py1)!=null;
    }

    /**
     * Returns whether Cubic for given points is intersected by line with given points.
     */
    public static boolean intersectsQuad(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
        double x1, double y1, double px0, double py0, double pxc0, double pyc0, double px1, double py1)
    {
        return SegHit.getHitCubicQuad(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, pxc0, pyc0, px1, py1)!=null;
    }

    /**
     * Returns whether Cubic for given points is intersected by Quad with given points.
     */
    public static boolean intersectsCubic(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
        double x1, double y1, double px0, double py0, double pxc0, double pyc0, double pxc1, double pyc1, double px1,
        double py1)
    {
        return SegHit.getHitCubicCubic(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, pxc0, pyc0, pxc1, pyc1, px1,py1)!=null;
    }

    /**
     * PathIter for Cubic.
     */
    private class CubicIter extends PathIter {

        /** Create new CubicIter. */
        CubicIter(Transform at) { super(at); }  int index;

        /** Returns whether there are more segments. */
        public boolean hasNext() { return index<2; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double coords[])
        {
            switch (index++) {
                case 0: return moveTo(x0, y0, coords);
                case 1: return cubicTo(cp0x, cp0y, cp1x, cp1y, x1, y1, coords);
                default: throw new RuntimeException("line iterator out of bounds");
            }
        }
    }
}