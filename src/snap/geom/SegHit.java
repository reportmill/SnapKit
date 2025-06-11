package snap.geom;

/**
 * A class to represent hit information for Segment intersections.
 */
public class SegHit {
    
    // Parametric location (from 0-1) of hit on primary segment
    public double     h0;
    
    // Parametric location (from 0-1) of hit on secondary segment
    public double     h1;

    /**
     * Constructor for given hits.
     */
    public SegHit(double aH0, double aH1)
    {
        h0 = aH0>=0 && aH0<=1? aH0 : -1; h1 = aH1>=0 && aH1<=1? aH1 : -1;
    }

    /**
     * Returns whether line for given points is intersected by second line with given points.
     */
    public static SegHit getHitLineLine(double x0, double y0, double x1, double y1,
        double px0, double py0, double px1, double py1)
    {
        // Some line slope stuff
        double num1 = (y0 - py0) * (px1 - px0) - (x0 - px0) * (py1 - py0);
        double num2 = (y0 - py0) * (x1 - x0) - (x0 - px0) * (y1 - y0);
        double den = (x1 - x0) * (py1 - py0) - (y1 - y0) * (px1 - px0);

        // Calculate parametric locations of intersection (line1:r, line2:s)
        double r = num1 / den;
        double s = num2 / den;

        // If parametric locations outside 0-1 range, then return false because lines don't intersect
        if (r < 0 || r > 1 || s < 0 || s > 1)
            return null;

        // Return
        return new SegHit(r, s);
    }

    /**
     * Returns hit for given line and quad points.
     */
    public static SegHit getHitLineQuad(double x0, double y0, double x1, double y1,
        double qx0, double qy0, double cpx, double cpy, double qx1, double qy1)
    {
        SegHit hit = getHitQuadLine(qx0, qy0, cpx, cpy, qx1, qy1, x0, y0, x1, y1);
        if (hit != null) {
            double tmp = hit.h0; hit.h0 = hit.h1; hit.h1 = tmp; }
        return hit;
    }

    /**
     * Returns hit for given line and cubic points.
     */
    public static SegHit getHitLineCubic(double x0, double y0, double x1, double y1,
        double cx0, double cy0, double cp0x, double cp0y, double cp1x, double cp1y, double cx1, double cy1)
    {
        SegHit hit = getHitCubicLine(cx0, cy0, cp0x, cp0y, cp1x, cp1y, cx1, cy1, x0, y0, x1, y1);
        if (hit != null) {
            double tmp = hit.h0; hit.h0 = hit.h1; hit.h1 = tmp; }
        return hit;
    }

    /**
     * Returns hit for given quad and line points.
     */
    public static SegHit getHitQuadLine(double x0, double y0, double cpx, double cpy, double x1, double y1,
        double px0, double py0, double px1, double py1)
    {
        // If quad is really a line, return line version
        if (Quad.isLine(x0, y0, cpx, cpy, x1, y1))
            return getHitLineLine(x0, y0, x1, y1, px0, py0, px1, py1);

        // Calculate new control points to split quad in two
        double AX = (x0 + cpx) / 2;
        double BX = (cpx + x1) / 2;
        double CX = (AX + BX) / 2;

        double AY = (y0 + cpy) / 2;
        double BY = (cpy + y1) / 2;
        double CY = (AY + BY) / 2;

        // If either intersect, return hit
        SegHit h1 = getHitQuadLine(x0, y0, AX, AY, CX, CY, px0, py0, px1, py1);
        if (h1 != null) { h1.h0 /= 2;
            return h1; }
        SegHit h2 = getHitQuadLine(CX, CY, BX, BY, x1, y1, px0, py0, px1, py1);
        if (h2 != null) { h2.h0 = h2.h0 / 2 + .5;
            return h2; }
        return null;
    }

    /**
     * Returns hit for given quad and quad points.
     */
    public static SegHit getHitQuadQuad(double x0, double y0, double xc0, double yc0, double x1, double y1,
        double px0, double py0, double pxc0, double pyc0, double px1, double py1)
    {
        // If quad is really a line, return line version
        if (Quad.isLine(x0, y0, xc0, yc0, x1, y1))
            return getHitLineQuad(x0, y0, x1, y1, px0, py0, pxc0, pyc0, px1, py1);

        // Calculate new control points to split quad in two
        double AX = (x0 + xc0) / 2;
        double BX = (xc0 + x1) / 2;
        double CX = (AX + BX) / 2;

        double AY = (y0 + yc0) / 2;
        double BY = (yc0 + y1) / 2;
        double CY = (AY + BY) / 2;

        // If either intersect, return hit
        SegHit h1 = getHitQuadQuad(x0, y0, AX, AY, CX, CY, px0, py0, pxc0, pyc0, px1, py1);
        if (h1 != null) { h1.h0 /= 2;
            return h1; }
        SegHit h2 = getHitQuadQuad(CX, CY, BX, BY, x1, y1, px0, py0, pxc0, pyc0, px1, py1);
        if (h2 != null) { h2.h0 = h2.h0 / 2 + .5;
            return h2; }
        return null;
    }

    /**
     * Returns hit for given quad and cubic points.
     */
    public static SegHit getHitQuadCubic(double x0, double y0, double cpx, double cpy, double x1, double y1,
        double px0, double py0, double pcp0x, double pcp0y, double pcp1x, double pcp1y, double px1, double py1)
    {
        SegHit hit = getHitCubicQuad(px0, py0, pcp0x, pcp0y, pcp1x, pcp1y, px1, py1, x0, y0, cpx, cpy, x1, y1);
        if (hit != null) {
            double tmp = hit.h0; hit.h0 = hit.h1; hit.h1 = tmp; }
        return hit;
    }

    /**
     * Returns hit for given cubic and line points.
     */
    public static SegHit getHitCubicLine(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
        double x1, double y1, double px0, double py0, double px1, double py1)
    {
        // If cubic is really a line, return line version
        if (Cubic.isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
            return getHitLineLine(x0, y0, x1, y1, px0, py0, px1, py1);

        // Calculate new x control points to split cubic into two
        double AX = (x0 + xc0) / 2;
        double BX = (xc0 + xc1) / 2;
        double CX = (xc1 + x1) / 2;
        double DX = (AX + BX) / 2;
        double EX = (BX + CX) / 2;
        double FX = (DX + EX) / 2;

        // Calculate new y control points to split cubic into two
        double AY = (y0 + yc0) / 2;
        double BY = (yc0 + yc1) / 2;
        double CY = (yc1 + y1) / 2;
        double DY = (AY + BY) / 2;
        double EY = (BY + CY) / 2;
        double FY = (DY + EY) / 2;

        // If either intersect, return hit
        SegHit h1 = getHitCubicLine(x0, y0, AX, AY, DX, DY, FX, FY, px0, py0, px1, py1);
        if (h1 != null) { h1.h0 /= 2;
            return h1; }
        SegHit h2 = getHitCubicLine(FX, FY, EX, EY, CX, CY, x1, y1, px0, py0, px1, py1);
        if (h2 != null) { h2.h0 = h2.h0/2 + .5;
            return h2; }

        // Return no hit
        return null;
    }

    /**
     * Returns hit for given cubic and quad points.
     */
    public static SegHit getHitCubicQuad(double x0, double y0, double xc0, double yc0, double xc1, double yc1,
        double x1, double y1, double px0, double py0, double pxc0, double pyc0, double px1, double py1)
    {
        // If cubic is really a line, return line version
        if (Cubic.isLine(x0, y0, xc0, yc0, xc1, yc1, x1, y1))
            return getHitLineQuad(x0, y0, x1, y1, px0, py0, pxc0, pyc0, px1, py1);

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

        // If either intersect, return hit
        SegHit h1 = getHitCubicQuad(x0, y0, AX, AY, DX, DY, FX, FY, px0, py0, pxc0, pyc0, px1, py1);
        if (h1 != null) { h1.h0 /= 2;
            return h1; }
        SegHit h2 = getHitCubicQuad(FX, FY, EX, EY, CX, CY, x1, y1, px0, py0, pxc0, pyc0, px1, py1);
        if (h2 != null) { h2.h0 = h2.h0/2 + .5;
            return h2; }

        // Return no hit
        return null;
    }

    /**
     * Returns whether Cubic for given points is intersected by Quad with given points.
     */
    public static SegHit getHitCubicCubic(double x0, double y0, double cp0x, double cp0y, double cp1x, double cp1y,
        double x1, double y1, double px0, double py0, double pxc0, double pyc0, double pxc1, double pyc1, double px1,
        double py1)
    {
        // If cubic is really a line, return line version
        if (Cubic.isLine(x0, y0, cp0x, cp0y, cp1x, cp1y, x1, y1))
            return getHitLineCubic(x0, y0, x1, y1, px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1);

        // Calculate new control points to split cubic into two
        double AX = (x0 + cp0x) / 2;
        double BX = (cp0x + cp1x) / 2;
        double CX = (cp1x + x1) / 2;
        double DX = (AX + BX) / 2;
        double EX = (BX + CX) / 2;
        double FX = (DX + EX) / 2;

        double AY = (y0 + cp0y) / 2;
        double BY = (cp0y + cp1y) / 2;
        double CY = (cp1y + y1) / 2;
        double DY = (AY + BY) / 2;
        double EY = (BY + CY) / 2;
        double FY = (DY + EY) / 2;

        // If either intersect, return hit
        SegHit h1 = getHitCubicCubic(x0, y0, AX, AY, DX, DY, FX, FY, px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1);
        if (h1 != null) { h1.h0 /= 2;
            return h1; }
        SegHit h2 = getHitCubicCubic(FX, FY, EX, EY, CX, CY, x1, y1, px0, py0, pxc0, pyc0, pxc1, pyc1, px1, py1);
        if (h2 != null) { h2.h0 = h2.h0 / 2 + .5;
            return h2; }

        // Return no hit
        return null;
    }
}