package snap.gfx;

/**
 * A Shape representing a Cubic curve.
 */
public class Cubic extends Shape {

    // The points
    double x0, y0, x1, y1, x2, y2;

/**
 * Returns the bounds.
 */
public Rect getBounds()
{
    return null;
}

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aT)
{
    return null;
}

/**
 * Returns the number of crossings for the ray from given point extending to the right.
 */
public static int crossings(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1,
    double px, double py, int level)
{
    if (py <  y0 && py <  yc0 && py <  yc1 && py <  y1) return 0;
    if (py >= y0 && py >= yc0 && py >= yc1 && py >= y1) return 0;
    // Note y0 could equal yc0...
    if (px >= x0 && px >= xc0 && px >= xc1 && px >= x1) return 0;
    if (px <  x0 && px <  xc0 && px <  xc1 && px <  x1) {
        if (py >= y0) {
            if (py < y1) return 1; }
        else { // py < y0
            if (py >= y1) return -1; }
        // py outside of y01 range, and/or y0==yc0
        return 0;
    }
    
    // double precision only has 52 bits of mantissa
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

}