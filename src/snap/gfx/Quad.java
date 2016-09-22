package snap.gfx;

/**
 * A Shape representing a Quadratic curve.
 */
public class Quad extends Shape {

    // The points
    double x0, y0, xc0, yc0, x1, y1;

/**
 * Returns the bounds.
 */
public Rect getBounds()  { return bounds(x0, y0, xc0, yc0, x1, y1, null); }

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aT)  { return null; }

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

}