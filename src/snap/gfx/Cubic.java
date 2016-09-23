package snap.gfx;

/**
 * A Shape representing a Cubic curve.
 */
public class Cubic extends Shape {

    // The points
    double x0, y0, xc0, yc0, xc1, yc1, x1, y1;

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
public PathIter getPathIter(Transform aT)  { return null; }

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
    return Line.getDistanceSquared(x0,y0,x1,y1,xc0,yc0)<.1 && Line.getDistanceSquared(x0,y0,x1,y1,xc1,yc1)<.1;
}

/**
 * Returns whether Cubic for given points is intersected by line with given points.
 */
public static boolean intersectsLine(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double px1, double py1)
{
    // If cubic is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
        return Line.intersectsLine(x0, y0, x1, y1, px0, py0, px1, py1);

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
    if(intersectsLine(x0, y0, nxc0, nyc0, nxc1, nyc1, midpx, midpy, px0, py0, px1, py1))
        return true;
    return intersectsLine(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1, px0, py0, px1, py1);
}

/**
 * Returns whether Cubic for given points is intersected by line with given points.
 */
public static boolean intersectsQuad(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double pxc0, double pyc0, double px1, double py1)
{
    // If cubic is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
        return Quad.intersectsLine(px0, py0, pxc0, pyc0, px1, py1, x0, y0, x1, y1);

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
    if(intersectsQuad(x0, y0, nxc0, nyc0, nxc1, nyc1, midpx, midpy, px0, py0, pxc0, pyc0, px1, py1))
        return true;
    return intersectsQuad(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1, px0, py0, pxc0, pyc0, px1, py1);
}

/**
 * Returns whether Cubic for given points is intersected by Quad with given points.
 */
public static boolean intersectsCubic(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
    double x1, double y1, double px0, double py0, double pxc0, double pyc0, double pxc1, double pyc1, double px1,
    double py1)
{
    // If cubic is really a line, return line version
    if(isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
        return intersectsLine(px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1, x0, y0, x1, y1);

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
    if(intersectsCubic(x0, y0, nxc0, nyc0, nxc1, nyc1, midpx, midpy, px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1))
        return true;
    return intersectsCubic(midpx, midpy, nxc2, nyc2, nxc3, nyc3, x1, y1, px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1);
}

}