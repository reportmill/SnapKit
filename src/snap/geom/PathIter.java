/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * A class to iterate over segments in a shape, providing specific coordinate information.
 */
public abstract class PathIter {
    
    // The transform
    protected Transform  _trans;
    
    // Constants for winding
    public static final int WIND_EVEN_ODD = 0;
    public static final int WIND_NON_ZERO = 1;

    /**
     * Creates a new PathIter.
     */
    public PathIter()  { }

    /**
     * Creates a new PathIter for given transform.
     */
    public PathIter(Transform aTrans)  { _trans = aTrans; }

    /**
     * Returns the winding - how a path determines what to fill when segments intersect.
     */
    public int getWinding()  { return WIND_EVEN_ODD; }

    /**
     * Returns the next segment.
     */
    public abstract Seg getNext(double coords[]);

    /**
     * Returns the next segment (float coords).
     */
    public Seg getNext(float coords[])
    {
        double dcoords[] = new double[6];
        Seg seg = getNext(dcoords);
        for (int i=0;i<6;i++)
            coords[i] = (float) dcoords[i];
        return seg;
    }

    /**
     * Returns whether has next segment.
     */
    public abstract boolean hasNext();

    /**
     * Returns a MoveTo for given coords.
     */
    protected final Seg moveTo(double aX, double aY, double coords[])
    {
        coords[0] = aX; coords[1] = aY;
        if (_trans!=null)
            _trans.transform(coords, 1);
        return Seg.MoveTo;
    }

    /**
     * Returns a LineTo for given coords.
     */
    protected final Seg lineTo(double aX, double aY, double coords[])
    {
        coords[0] = aX; coords[1] = aY;
        if (_trans!=null)
            _trans.transform(coords, 1);
        return Seg.LineTo;
    }

    /**
     * Returns a QuadTo for given coords.
     */
    protected final Seg quadTo(double aCPX, double aCPY, double aX, double aY, double coords[])
    {
        coords[0] = aCPX; coords[1] = aCPY;
        coords[2] = aX; coords[3] = aY;
        if (_trans!=null)
            _trans.transform(coords, 2);
        return Seg.QuadTo;
    }

    /**
     * Returns a CubicTo for given coords.
     */
    protected final Seg cubicTo(double aCPX0, double aCPY0, double aCPX1, double aCPY1, double aX, double aY, double coords[])
    {
        coords[0] = aCPX0; coords[1] = aCPY0;
        coords[2] = aCPX1; coords[3] = aCPY1;
        coords[4] = aX; coords[5] = aY;
        if (_trans!=null)
            _trans.transform(coords, 3);
        return Seg.CubicTo;
    }

    /**
     * Returns a CubicTo for start, corner and end points.
     */
    protected final Seg arcTo(double lx, double ly, double cx, double cy, double x, double y, double coords[])
    {
        double magic = .5523f; // I calculated this in mathematica one time - probably only valid for 90 deg corner.
        double cpx1 = lx + (cx-lx)*magic;
        double cpy1 = ly + (cy-ly)*magic;
        double cpx2 = x + (cx-x)*magic;
        double cpy2 = y + (cy-y)*magic;
        return cubicTo(cpx1, cpy1, cpx2, cpy2, x, y, coords);
    }

    /**
     * Returns a close.
     */
    protected final Seg close()  { return Seg.Close; }

    /**
     * Returns bounds rect for given PathIter.
     */
    public static Rect getBounds(PathIter aPathIter)
    {
        // Get iter vars
        double pts[] = new double[6];
        double lx = 0, ly = 0;
        Rect bounds = new Rect();
        Rect bnds = null;

        // Iterate over segments
        while (aPathIter.hasNext()) {
            switch (aPathIter.getNext(pts)) {
                case MoveTo:
                    if (bnds==null) {
                        bounds.setRect(lx=pts[0],ly=pts[1],0,0);
                        continue;
                    }
                case LineTo:
                    bnds = Line.getBounds(lx, ly, lx=pts[0], ly=pts[1], bnds);
                    break;
                case QuadTo:
                    bnds = Quad.getBounds(lx, ly, pts[0], pts[1], lx=pts[2], ly=pts[3], bnds);
                    break;
                case CubicTo:
                    bnds = Cubic.getBounds(lx, ly, pts[0], pts[1], pts[2], pts[3], lx=pts[4], ly=pts[5], bnds);
                    break;
                case Close: break;
            }

            // Combine bounds for segment (I wish this was union() instead, so it didn't include (0,0))
            bounds.add(bnds);
        }

        // Return bounds
        return bounds;
    }

    /**
     * Returns total length of segments for given PathIter.
     */
    public static double getArcLength(PathIter aPathIter)
    {
        // Get iter vars
        double pts[] = new double[6];
        double lx = 0, ly = 0;
        double lenAll = 0;
        double len = 0;

        // Iterate over segments
        while (aPathIter.hasNext()) {
            switch (aPathIter.getNext(pts)) {
                case MoveTo:
                    lx = pts[0]; ly = pts[1];
                    len = 0;
                    break;
                case LineTo:
                    len = Point.getDistance(lx, ly, lx=pts[0], ly=pts[1]);
                    break;
                case QuadTo:
                    len = SegmentLengths.getArcLengthQuad(lx, ly, pts[0], pts[1], lx=pts[2], ly=pts[3]);
                    break;
                case CubicTo:
                    len = SegmentLengths.getArcLengthCubic(lx, ly, pts[0], pts[1], pts[2], pts[3], lx=pts[4], ly=pts[5]);
                    break;
                case Close: len = 0; break;
            }

            // Combine len for segment
            lenAll += len;
        }

        // Return length all
        return lenAll;
    }
}