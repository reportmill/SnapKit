package snap.gfx;

/**
 * A Segment representing a Cubic curve.
 */
public class Cubic extends Segment {

    // The control points
    double xc0, yc0, xc1, yc1;

/**
 * Creates a new Cubic.
 */
public Cubic(double aX0, double aY0, double aXC0, double aYC0, double aXC1, double aYC1, double aX1, double aY1)
{
    x0 = aX0; y0 = aY0; xc0 = aXC0; yc0 = aYC0; xc1 = aXC1; yc1 = aYC1; x1 = aX1; y1 = aY1;
}

/**
 * Returns the bounds.
 */
public Rect getBounds()  { return bounds(x0, y0, xc0, yc0, xc1, yc1, x1, y1, null); }

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aT)  { return new CubicIter(aT); }

/**
 * Returns whether this shape intersects line defined by given points.
 */
public boolean intersects(double px0, double py0, double px1, double py1)
{
    if(!getBounds().intersects(Line.bounds(px0,py0,px1,py1,null))) return false;
    return intersectsLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, px1, py1);
}

/**
 * Returns whether this shape intersects quad defined by given points.
 */
public boolean intersects(double px0, double py0, double pxc0, double pyc0, double px1, double py1)
{
    if(!getBounds().intersects(Quad.bounds(px0,py0,pxc0,pyc0,px1,py1,null))) return false;
    return intersectsQuad(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, pxc0, pyc0, px1, py1);
}

/**
 * Returns whether this shape intersects cubic defined by given points.
 */
public boolean intersects(double px0, double py0, double pxc0,double pyc0,double pxc1,double pyc1,double px1,double py1)
{
    if(!getBounds().intersects(Cubic.bounds(px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1, null))) return false;
    return intersectsCubic(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1);
}

/**
 * Returns the x value at given parametric location. p' = (1-t)^3*p0 + 3*t*(1-t)^2*p1 + 3*t^2*(1-t)*p2 + t^3*p3.
 */
public double getX(double aLoc)
{
    //double t=aLoc, s=1-t, s2 = s*s, s3 = s2*s, t2 = t*t, t3 = t2*t; return s3*x0 + 3*t*s2*xc0 + 3*t2*s*xc1 + t3*x1;
    double nxc0 = average(x0, xc0, aLoc);
    double xca = average(xc0, xc1, aLoc);
    double nxc1 = average(nxc0, xca, aLoc);
    double nxc3 = average(xc1, x1, aLoc);
    double nxc2 = average(nxc3, xca, aLoc);
    return average(nxc1, nxc2, aLoc);
}

/**
 * Returns the y value at given parametric location. p' = (1-t)^3*p0 + 3*t*(1-t)^2*p1 + 3*t^2*(1-t)*p2 + t^3*p3.
 */
public double getY(double aLoc)
{
    //double t=aLoc, s=1-t, s2 = s*s, s3 = s2*s, t2 = t*t, t3 = t2*t; return s3*y0 + 3*t*s2*yc0 + 3*t2*s*yc1 + t3*y1;
    double nyc0 = average(y0, yc0, aLoc);
    double yca = average(yc0, yc1, aLoc);
    double nyc1 = average(nyc0, yca, aLoc);
    double nyc3 = average(yc1, y1, aLoc);
    double nyc2 = average(nyc3, yca, aLoc);
    return average(nyc1, nyc2, aLoc);
}

/**
 * Splits this Cubic at given parametric location and return the remainder.
 */
public Cubic split(double aLoc)
{
    // Calculate new x control points to split cubic into two
    double nxc0 = average(x0, xc0, aLoc);
    double xca = average(xc0, xc1, aLoc);
    double nxc3 = average(xc1, x1, aLoc);
    double nxc1 = average(nxc0, xca, aLoc);
    double nxc2 = average(xca, nxc3, aLoc);
    double midpx = average(nxc1, nxc2, aLoc);
    
    // Calculate new y control points to split cubic into two
    double nyc0 = average(y0, yc0, aLoc);
    double yca = average(yc0, yc1, aLoc);
    double nyc3 = average(yc1, y1, aLoc);
    double nyc1 = average(nyc0, yca, aLoc);
    double nyc2 = average(yca, nyc3, aLoc);
    double midpy = average(nyc1, nyc2, aLoc);
    
    // Create new remainder shape, update this shape and return remainder
    Cubic rem = new Cubic(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1);
    xc0 = nxc0; yc0 = nyc0; xc1 = nxc1; yc1 = nyc1; x1 = midpx; y1 = midpy;
    return rem;
}

/**
 * Returns the weighted average of this point with another point.
 */
private static final double average(double x0, double x1, double t)  { return x0 + t*(x1 - x0); }

/**
 * Creates and returns the reverse of this segement.
 */
public Cubic createReverse()  { return new Cubic(x1, y1, xc1, yc1, xc0, yc0, x0, y0); }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    Cubic other = anObj instanceof Cubic? (Cubic)anObj : null; if(other==null) return false;
    return equals(x0,other.x0) && equals(y0,other.y0) && equals(xc0,other.xc0) && equals(yc0,other.yc0) &&
        equals(xc1,other.xc1) && equals(yc1,other.yc1) && equals(x1,other.x1) && equals(y1,other.y1);
}

/**
 * Returns whether cubic is equal to another, regardless of direction.
 */
public boolean matches(Object anObj)
{
    if(equals(anObj)) return true;
    Cubic other = anObj instanceof Cubic? (Cubic)anObj : null; if(other==null) return false;
    return equals(x0,other.x1) && equals(y0,other.y1) && equals(xc0,other.xc1) && equals(yc0,other.yc1) &&
        equals(xc1,other.xc0) && equals(yc1,other.yc0) && equals(x1,other.x0) && equals(y1,other.y0);
}

/**
 * Returns the bounds for given quad points.
 */
public static Rect bounds(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1,
    Rect aRect)
{
    // Add end points
    aRect = Line.bounds(x0, y0, x1, y1, aRect);

    // This curve might have extrema:
    // f = a*t*t*t+b*t*t+c*t+d
    // df/dt = 3*a*t*t+2*b*t+c
    // A = 3*a, B = 2*b, C = c
    // t = [-B+-sqrt(B^2-4*A*C)]/(2A)
    // t = (-2*b+-sqrt(4*b*b-12*a*c)]/(6*a)
    double ax = -x0 + 3*xc0 - 3*xc1 + x1, bx = 3*x0 - 6*xc0 + 3*xc1, cx = -3*x0 + 3*xc0, dx = x0;
    double detx = (4*bx*bx - 12*ax*cx);
    if(detx<0) { } // No solutions
    else if(detx==0) { // One solution
        double tx = -2*bx/(6*ax);
        if(tx>0 && tx<1) aRect.addX(ax*tx*tx*tx + bx*tx*tx + cx*tx + dx); }
   else { // Two solutions
       detx = Math.sqrt(detx); double tx = (-2*bx + detx)/(6*ax);
       if(tx>0 && tx<1) aRect.addX(ax*tx*tx*tx + bx*tx*tx + cx*tx + dx);
       tx = (-2*bx - detx)/(6*ax);
       if(tx>0 && tx<1) aRect.addX(ax*tx*tx*tx + bx*tx*tx + cx*tx + dx);
   }

   // Do the same for y
   double ay = -y0 + 3*yc0 - 3*yc1 + y1, by = 3*y0 - 6*yc0 + 3*yc1, cy = -3*y0 + 3*yc0, dy = y0;
   double dety = (4*by*by - 12*ay*cy);
   if(dety<0) { } // No solutions
   else if(dety==0) { // One solution
       double ty = -2*by/(6*ay);
       if(ty>0 && ty<1) aRect.addY(ay*ty*ty*ty + by*ty*ty + cy*ty + dy); }
   else { // Two solutions
       dety = Math.sqrt(dety); double ty = (-2*by + dety)/(6*ay);
       if(ty>0 && ty<1) aRect.addY(ay*ty*ty*ty + by*ty*ty + cy*ty + dy);
       ty = (-2*by - dety)/(6*ay);
       if(ty>0 && ty<1) aRect.addY(ay*ty*ty*ty + by*ty*ty + cy*ty + dy);
   }
   
   return aRect;
}

/**
 * Returns whether Cubic for given points is intersected by line with given points.
 */
public static double getDistanceSquared(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double aX, double aY)
{
    // If cubic is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
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
    if(py>=y0 && py>=yc0 && py>=yc1 && py>=y1) return 0;
    if(py<y0 && py<yc0 && py<yc1 && py<y1) return 0;
    if(px>=x0 && px>=xc0 && px>=xc1 && px>=x1) return 0;
    
    // If point to the left of all curve points...
    if(px<x0 && px<xc0 && px<xc1 && px<x1) {
        if(py>=y0) {
            if(py<y1) return 1; }
        else { // py < y0
            if(py>=y1) return -1; }
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
    return Line.getDistanceSquared(x0,y0,x1,y1,xc0,yc0)<.1 && Line.getDistanceSquared(x0,y0,x1,y1,xc1,yc1)<.01;
}

/**
 * Returns whether Cubic for given points is intersected by line with given points.
 */
public static boolean intersectsLine(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double px1, double py1)
{
    return getHitPointLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, px1, py1, false)>=0;
}

/**
 * Returns whether Cubic for given points is intersected by line with given points.
 */
public static boolean intersectsQuad(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double pxc0, double pyc0, double px1, double py1)
{
    return getHitPointQuad(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, pxc0, pyc0, px1, py1, false)>=0;
}

/**
 * Returns whether Cubic for given points is intersected by Quad with given points.
 */
public static boolean intersectsCubic(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double pxc0, double pyc0, double pxc1, double pyc1, double px1,
    double py1)
{
    return getHitPointCubic(x0, y0, xc0, yc0, xc1, yc1, x1, y1, px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1, false)>=0;
}

/**
 * Returns whether Cubic for given points is intersected by line with given points.
 */
public static double getHitPointLine(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double px1, double py1, boolean isOther)
{
    // If cubic is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
        return Line.getHitPointLine(x0, y0, x1, y1, px0, py0, px1, py1, isOther);

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
    double hp1 = getHitPointLine(x0, y0, nxc0, nyc0, nxc1, nyc1, midpx, midpy, px0, py0, px1, py1, isOther);
    if(hp1>=0)
        return isOther? hp1 : hp1/2;
    double hp2 = getHitPointLine(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1, px0, py0, px1, py1, isOther);
    if(hp2>=0)
        return isOther? hp2 : hp2/2 + .5;
    return -1;
}

/**
 * Returns whether Cubic for given points is intersected by line with given points.
 */
public static double getHitPointQuad(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double pxc0, double pyc0, double px1, double py1, boolean isOther)
{
    // If cubic is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
        return Quad.getHitPointLine(px0, py0, pxc0, pyc0, px1, py1, x0, y0, x1, y1, !isOther);

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
    double hp1 = getHitPointQuad(x0, y0, nxc0, nyc0, nxc1, nyc1, midpx, midpy, px0, py0, pxc0, pyc0, px1, py1, isOther);
    if(hp1>=0)
        return isOther? hp1 : hp1/2;
    double hp2 = getHitPointQuad(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1, px0, py0, pxc0, pyc0, px1, py1, isOther);
    if(hp2>=0)
        return isOther? hp2 : hp2/2 + .5;
    return -1;
}

/**
 * Returns whether Cubic for given points is intersected by Quad with given points.
 */
public static double getHitPointCubic(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double pxc0, double pyc0, double pxc1, double pyc1, double px1,
    double py1, boolean isOther)
{
    // If cubic is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
        return getHitPointLine(px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1, x0, y0, x1, y1, !isOther);

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
    double hp1 = getHitPointCubic(x0,y0,nxc0,nyc0,nxc1,nyc1,midpx,midpy,px0,py0,pxc0,pyc0,pxc1,pyc1,px1,py1,isOther);
    if(hp1>=0)
        return isOther? hp1 : hp1/2;
    double hp2 = getHitPointCubic(midpx,midpy,nxc2,nyc2,nxc3,nyc3,x1,y1,px0,py0,pxc0,pyc0,pxc1,pyc1,px1,py1,isOther);
    if(hp2>=0)
        return isOther? hp2 : hp2/2 + .5;
    return -1;
}

/**
 * PathIter for Cubic.
 */
private class CubicIter implements PathIter {
    
    /** Create new CubicIter. */
    CubicIter(Transform at) { trans = at; }  Transform trans; int index;

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<=1; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double pts[])
    {
        PathIter.Seg seg = null;
        if(index==0) { seg = PathIter.Seg.MoveTo; pts[0] = x0; pts[1] = y0; }
        else if(index==1) { seg = PathIter.Seg.CubicTo;
            pts[0] = xc0; pts[1] = yc0; pts[2] = xc1; pts[3] = yc1; pts[4] = x1; pts[5] = y1; }
        else throw new RuntimeException("line iterator out of bounds");
        if(trans!=null) trans.transform(pts); index++;
        return seg;
    }
}

}