package snap.gfx;

/**
 * A Shape representing a Quadratic curve.
 */
public class Quad extends Shape {

    // The points
    double x0, y0, xc0, yc0, x1, y1;

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
public Rect getBounds()  { return bounds(x0, y0, xc0, yc0, x1, y1, null); }

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aT)  { return new QuadIter(aT); }

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
    return Line.getDistanceSquared(x0,y0,x1,y1,xc0,yc0)<.1;
}

/**
 * Returns whether Quad for given points is intersected by line with given points.
 */
public static boolean intersectsLine(double x0, double y0, double xc0, double yc0, double x1, double y1,
    double px0, double py0, double px1, double py1)
{
    // If quad is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, x1, y1))
        return Line.intersectsLine(x0, y0, x1, y1, px0, py0, px1, py1);

    // Calculate new control points to split quad in two
    double nxc0 = (x0 + xc0) / 2;
    double nyc0 = (y0 + yc0) / 2;
    double nxc1 = (x1 + xc0) / 2;
    double nyc1 = (y1 + yc0) / 2;
    double midpx = (nxc0 + nxc1) / 2;
    double midpy = (nyc0 + nyc1) / 2;
    
    // If either intersect, return true
    if(intersectsLine(x0, y0, nxc0, nyc0, midpx, midpy, px0, py0, px1, py1))
        return true;
    return intersectsLine(midpx, midpy, nxc1, nyc1, x1, y1, px0, py0, px1, py1);
}

/**
 * Returns whether Quad for given points is intersected by Quad with given points.
 */
public static boolean intersectsQuad(double x0, double y0, double xc0, double yc0, double x1, double y1,
    double px0, double py0, double pxc0, double pyc0, double px1, double py1)
{
    // If quad is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, x1, y1))
        return intersectsLine(px0, py0, pxc0, pyc0, px1, py1, x0, y0, x1, y1);

    // Calculate new control points to split quad in two
    double nxc0 = (x0 + xc0) / 2;
    double nyc0 = (y0 + yc0) / 2;
    double nxc1 = (x1 + xc0) / 2;
    double nyc1 = (y1 + yc0) / 2;
    double midpx = (nxc0 + nxc1) / 2;
    double midpy = (nyc0 + nyc1) / 2;
    
    // If either intersect, return true
    if(intersectsQuad(x0, y0, nxc0, nyc0, midpx, midpy, px0, py0, pxc0, pyc0, px1, py1))
        return true;
    return intersectsQuad(midpx, midpy, nxc1, nyc1, x1, y1, px0, py0, pxc0, pyc0, px1, py1);
}

/**
 * PathIter for Quad.
 */
private class QuadIter implements PathIter {
    
    /** Create new QuadIter. */
    QuadIter(Transform at) { trans = at; }  Transform trans; int index;

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<=1; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double pts[])
    {
        PathIter.Seg seg = null;
        if(index==0) { seg = PathIter.Seg.MoveTo; pts[0] = x0; pts[1] = y0; }
        else if(index==1) { seg = PathIter.Seg.QuadTo; pts[0] = xc0; pts[1] = yc0; pts[2] = x1; pts[3] = y1; }
        else throw new RuntimeException("line iterator out of bounds");
        if(trans!=null) trans.transform(pts); index++;
        return seg;
    }
}

}