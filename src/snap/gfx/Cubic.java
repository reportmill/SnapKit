package snap.gfx;

/**
 * A Shape representing a Cubic curve.
 */
public class Cubic extends Shape {

    // The points
    double x0, y0, xc0, yc0, xc1, yc1, x1, y1;

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

   //do the same thing for y:
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