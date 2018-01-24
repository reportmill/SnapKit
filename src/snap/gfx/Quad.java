package snap.gfx;

/**
 * A Segment representing a Quadratic curve.
 */
public class Quad extends Segment {

    // The control point
    double xc0, yc0;

/**
 * Creates a new Quad.
 */
public Quad(double aX0, double aY0, double aXC0, double aYC0, double aX1, double aY1)
{
    x0 = aX0; y0 = aY0; xc0 = aXC0; yc0 = aYC0; x1 = aX1; y1 = aY1;
}

/**
 * Returns the bounds.
 */
protected Rect getBoundsImpl()  { return bounds(x0, y0, xc0, yc0, x1, y1, null); }

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aT)  { return new QuadIter(aT); }

/**
 * Returns whether this shape intersects line defined by given points.
 */
public boolean intersects(double px0, double py0, double px1, double py1)
{
    return intersectsLine(x0, y0, xc0, yc0, x1, y1, px0, py0, px1, py1);
}

/**
 * Returns whether this shape intersects quad defined by given points.
 */
public boolean intersects(double px0, double py0, double pxc0, double pyc0, double px1, double py1)
{
    return intersectsQuad(x0, y0, xc0, yc0, x1, y1, px0, py0, pxc0, pyc0, px1, py1);
}

/**
 * Returns whether this shape intersects cubic defined by given points.
 */
public boolean intersects(double px0, double py0, double pxc0,double pyc0,double pxc1,double pyc1,double px1,double py1)
{
    return Cubic.intersectsQuad(px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1, x0, y0, xc0, yc0, x1, y1);
}

/**
 * Returns the x value at given parametric location.
 */
public double getX(double aLoc)
{
    double nxc0 = x0 + aLoc*(xc0 - x0);
    double nxc1 = xc0 + aLoc*(x1 - xc0);
    return nxc0 + aLoc*(nxc1 - nxc0); //double t=aLoc, s = 1 - t, s2 = s*s, t2 = t*t; return s2*x0 + 2*t*s*xc0 + t2*x1;
}

/**
 * Returns the y value at given parametric location.
 */
public double getY(double aLoc)
{
    double nyc0 = y0 + aLoc*(yc0 - y0);
    double nyc1 = yc0 + aLoc*(y1 - yc0);
    return nyc0 + aLoc*(nyc1 + nyc0); //double t=aLoc, s = 1 - t, s2 = s*s, t2 = t*t; return s2*y0 + 2*t*s*yc0 + t2*y1;
}

/**
 * Splits this Quad at given parametric location and return the remainder.
 */
public Quad split(double aLoc)
{
    // Calculate new control points to split quad in two
    double nxc0 = x0 + aLoc*(xc0 - x0);
    double nyc0 = y0 + aLoc*(yc0 - y0);
    double nxc1 = xc0 + aLoc*(x1 - xc0);
    double nyc1 = yc0 + aLoc*(y1 - yc0);
    double midpx = nxc0 + aLoc*(nxc1 - nxc0);
    double midpy = nyc0 + aLoc*(nyc1 - nyc0);
    
    // If either intersect, return true
    Quad rem = new Quad(midpx, midpy, nxc1, nyc1, x1, y1);
    xc0 = nxc0; yc0 = nyc0; x1 = midpx; y1 = midpy; _bounds = null;
    return rem;
}

/**
 * Creates and returns the reverse of this segement.
 */
public Quad createReverse()  { return new Quad(x1, y1, xc0, yc0, x0, y0); }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    Quad other = anObj instanceof Quad? (Quad)anObj : null; if(other==null) return false;
    return equals(x0,other.x0) && equals(y0,other.y0) &&
        equals(xc0,other.xc0) && equals(yc0,other.yc0) &&
        equals(x1,other.x1) && equals(y1,other.y1);
}

/**
 * Returns whether quad is equal to another, regardless of direction.
 */
public boolean matches(Object anObj)
{
    if(equals(anObj)) return true;
    Quad other = anObj instanceof Quad? (Quad)anObj : null; if(other==null) return false;
    return equals(x0,other.x1) && equals(y0,other.y1) &&
        equals(xc0,other.xc0) && equals(yc0,other.yc0) &&
        equals(x1,other.x0) && equals(y1,other.y0);
}

/**
 * Returns the bounds for given quad points.
 */
public static Rect bounds(double x0, double y0, double xc0, double yc0, double x1, double y1, Rect aRect)
{
    // Add end points
    aRect = Line.bounds(x0, y0, x1, y1, aRect);

    // this curve might have extrema:
    double ax = x0 - 2*xc0 + x1, bx = -2*x0 + 2*xc0, cx = x0, tx = -bx/(2*ax);
    if(tx>0 && tx<1) aRect.addX(ax*tx*tx + bx*tx + cx);

    double ay = y0 - 2*yc0 + y1, by = -2*y0 + 2*yc0, cy = y0, ty = -by/(2*ay);
    if(ty>0 && ty<1)  aRect.addY(ay*ty*ty + by*ty + cy);
    
    return aRect;
}

/**
 * Returns the minimum distance from the given point to the curve.
 */
public static double getDistanceSquared(double x0, double y0, double xc0, double yc0, double x1, double y1,
    double aX, double aY)
{
    // If quad is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, x1, y1))
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
    if(py<y0 && py<yc && py<y1) return 0;
    if(py>=y0 && py>=yc && py>=y1) return 0;
    
    // Note y0 could equal y1...
    if(px>=x0 && px>=xc && px>=x1) return 0;
    if(px<x0 && px<xc && px<x1) {
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
    if(Double.isNaN(xc) || Double.isNaN(yc)) return 0;

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
    return getHitPointLine(x0, y0, xc0, yc0, x1, y1, px0, py0, px1, py1, false)>=0;
}

/**
 * Returns whether Quad for given points is intersected by Quad with given points.
 */
public static boolean intersectsQuad(double x0, double y0, double xc0, double yc0, double x1, double y1,
    double px0, double py0, double pxc0, double pyc0, double px1, double py1)
{
    return getHitPointQuad(x0, y0, xc0, yc0, x1, y1, px0, py0, pxc0, pyc0, px1, py1, false)>=0;
}

/**
 * Returns whether Quad for given points is intersected by line with given points.
 */
public static double getHitPointLine(double x0, double y0, double xc0, double yc0, double x1, double y1,
    double px0, double py0, double px1, double py1, boolean isOther)
{
    // If quad is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, x1, y1))
        return Line.getHitPointLine(x0, y0, x1, y1, px0, py0, px1, py1, isOther);

    // Calculate new control points to split quad in two
    double nxc0 = (x0 + xc0) / 2;
    double nyc0 = (y0 + yc0) / 2;
    double nxc1 = (x1 + xc0) / 2;
    double nyc1 = (y1 + yc0) / 2;
    double midpx = (nxc0 + nxc1) / 2;
    double midpy = (nyc0 + nyc1) / 2;
    
    // If either intersect, return true
    double hp1 = getHitPointLine(x0, y0, nxc0, nyc0, midpx, midpy, px0, py0, px1, py1, isOther);
    if(hp1>=0)
        return isOther? hp1 : hp1/2;
    double hp2 = getHitPointLine(midpx, midpy, nxc1, nyc1, x1, y1, px0, py0, px1, py1, isOther);
    if(hp2>=0)
        return isOther? hp2 : hp2/2 + .5;
    return -1;
}

/**
 * Returns whether Quad for given points is intersected by Quad with given points.
 */
public static double getHitPointQuad(double x0, double y0, double xc0, double yc0, double x1, double y1,
    double px0, double py0, double pxc0, double pyc0, double px1, double py1, boolean isOther)
{
    // If quad is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, x1, y1))
        return getHitPointLine(px0, py0, pxc0, pyc0, px1, py1, x0, y0, x1, y1, !isOther);

    // Calculate new control points to split quad in two
    double nxc0 = (x0 + xc0) / 2;
    double nyc0 = (y0 + yc0) / 2;
    double nxc1 = (x1 + xc0) / 2;
    double nyc1 = (y1 + yc0) / 2;
    double midpx = (nxc0 + nxc1) / 2;
    double midpy = (nyc0 + nyc1) / 2;
    
    // If either intersect, return true
    double hp1 = getHitPointQuad(x0, y0, nxc0, nyc0, midpx, midpy, px0, py0, pxc0, pyc0, px1, py1, isOther);
    if(hp1>=0)
        return isOther? hp1 : hp1/2;
    double hp2 = getHitPointQuad(midpx, midpy, nxc1, nyc1, x1, y1, px0, py0, pxc0, pyc0, px1, py1, isOther);
    if(hp2>=0)
        return isOther? hp2 : hp2/2 + .5;
    return -1;
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
    public PathIter.Seg getNext(double coords[])
    {
        switch(index++) {
            case 0: return moveTo(x0, y0, coords);
            case 1: return quadTo(xc0, yc0, x1, y1, coords);
            default: throw new RuntimeException("line iterator out of bounds");
        }
    }
}

}