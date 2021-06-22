/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.*;
import snap.util.MathUtils;

/**
 * A Shape subclass to represent a simple geometric polygon.
 */
public class Polygon extends Shape implements Cloneable {

    // The points
    private double  _pnts[] = new double[0];

    /**
     * Creates a new Polygon.
     */
    public Polygon()  { }

    /**
     * Creates a new Polygon from given x y coords.
     */
    public Polygon(double ... theCoords)  { _pnts = theCoords; }

    /**
     * Creates a new Polygon from given x y coords.
     */
    public Polygon(Point ... thePoints)
    {
        int pc = thePoints.length;
        _pnts = new double[pc*2];
        for (int i=0;i<pc;i++) {
            _pnts[i*2] = thePoints[i].x;
            _pnts[i*2+1] = thePoints[i].y;
        }
    }

    /**
     * Returns the raw points array.
     */
    public double[] getPoints()  { return _pnts; }

    /**
     * Sets the points.
     */
    public void setPoints(double thePoints[])
    {
        _pnts = thePoints;
        shapeChanged();
    }

    /**
     * Returns the point count.
     */
    public int getPointCount()  { return _pnts.length/2; }

    /**
     * Returns the x at given point index.
     */
    public double getX(int anIndex)  { return _pnts[anIndex*2]; }

    /**
     * Returns the y at given point index.
     */
    public double getY(int anIndex)  { return _pnts[anIndex*2+1]; }

    /**
     * Returns the point at given index.
     */
    public Point getPoint(int anIndex)  { return new Point(_pnts[anIndex*2], _pnts[anIndex*2+1]); }

    /**
     * Adds a point at given x/y.
     */
    public void addPoint(double aX, double aY)
    {
        _pnts = Arrays.copyOf(_pnts, _pnts.length+2);
        _pnts[_pnts.length-2] = aX; _pnts[_pnts.length-1] = aY;
        shapeChanged();
    }

    /**
     * Sets a point at given point index to given x/y.
     */
    public void setPoint(int anIndex, double aX, double aY)
    {
        _pnts[anIndex*2] = aX;
        _pnts[anIndex*2+1] = aY;
        shapeChanged();
    }

    /**
     * Returns the last x point.
     */
    public double getLastX()
    {
        int plen = _pnts.length;
        return plen>0 ? _pnts[plen-2] : -1;
    }

    /**
     * Returns the last y point.
     */
    public double getLastY()
    {
        int plen = _pnts.length;
        return plen>0 ? _pnts[plen-1] : -1;
    }

    /**
     * Returns the last point.
     */
    public Point getLastPoint()
    {
        int plen = _pnts.length;
        return plen>0 ? new Point(_pnts[plen-2],_pnts[plen-1]) : null;
    }

    /**
     * Clears the polygon.
     */
    public void clear()
    {
        _pnts = new double[0];
        shapeChanged();
    }

    /**
     * Returns index of point at given x/y within given radius.
     */
    public int getPointAt(double aX, double aY, double aRad)
    {
        for (int i=0,pc=getPointCount();i<pc;i++)
            if (Point.getDistance(aX, aY, getX(i), getY(i))<=aRad)
                return i;
        return -1;
    }

    /**
     * Returns whether polygon has no intersecting lines.
     */
    public boolean isSimple()
    {
        // Get point count (if not at least 3 points, return false)
        int pc = getPointCount();
        if (pc < 3)
            return false;

        // Iterate over all lines
        for (int i=0; i<pc-1; i++) { int j = (i+1)%pc;

            // Get line endpoint and see if next point is collinear
            double x0 = getX(i), y0 = getY(i);
            double x1 = getX(j), y1 = getY(j);

            // If next point is collinear and backtracks over previous segment, return false.
            int jp1 = (j+1)%pc;
            double jp1x = getX(jp1);
            double jp1y = getY(jp1);
            boolean isCollinear = Line.isCollinear(x0, y0, x1, y1, jp1x, jp1y);
            if (isCollinear && (jp1x-x0)/(x1-x0) < 1)
                return false;

            // Iterate over remaining lines and see if they intersect
            for (int k=j+1;k<pc;k++) { int l = (k+1)%pc;
                double x2 = getX(k), y2 = getY(k);
                double x3 = getX(l), y3 = getY(l);
                boolean intersectsLine = Line.intersectsLine(x0, y0, x1, y1, x2, y2, x3, y3);
                if (intersectsLine && i != l) // Suppress last
                    return false;
            }
        }

        // Return true
        return true;
    }

    /**
     * Returns whether polygon is convex.
     */
    public boolean isConvex()
    {
        if (getPointCount() < 3) return true;
        double extAngles = Math.toDegrees(getExtAngleSum());
        return MathUtils.equals(extAngles, 360); // Could also do intAngles == (SideCount-2)*180
    }

    /**
     * Returns the interior angle at given point index.
     */
    public double getAngle(int anIndex)
    {
        // Get 3 points surrounding index, get vector points, and return angle between
        int pc = getPointCount(); if (pc < 3) return 0;
        int i0 = (anIndex-1+pc)%pc;
        int i1 = anIndex;
        int i2 = (anIndex+1)%pc;
        double x0 = getX(i0), y0 = getY(i0);
        double x1 = getX(i1), y1 = getY(i1);
        double x2 = getX(i2), y2 = getY(i2);

        // Get vector v0, from point to previous point, and v1, from point to next point
        double v0x = x0 - x1;
        double v0y = y0 - y1;
        double v1x = x2 - x1;
        double v1y = y2 - y1;

        // Return angle between vectors
        return Vect.getAngleBetween(v0x, v0y, v1x, v1y);
    }

    /**
     * Returns the exterior angle at given point index.
     */
    public double getExtAngle(int anIndex)  { return Math.PI - getAngle(anIndex); }

    /**
     * Returns the sum of exterior angles.
     */
    public double getExtAngleSum()
    {
        double angle = 0;
        int pc = getPointCount(); if (pc < 3) return 0;
        for (int i=0; i<pc; i++)
            angle += getExtAngle(i);
        return angle;
    }

    /**
     * Returns an array of polygons that are convex with max number of vertices.
     */
    public Polygon[] getConvexPolys(int aMax)
    {
        // If not simple, get simples
        if (!isSimple()) {
            System.err.println("Polygon.getConvexPolys: Not simple - shouldn't happen");
            Shape shp = Shape.makeSimple(this);
            PolygonList plist = new PolygonList(shp);
            Polygon[] polys0 = plist.getPolys();
            List<Polygon> polys1 = new ArrayList<>();
            for (Polygon poly : polys0) {
                Polygon[] plist1 = poly.getConvexPolys(aMax);
                Collections.addAll(polys1, plist1);
            }
            return new PolygonList(polys1).getPolys();
        }

        // Create list with clone of first poly
        Polygon poly = clone();
        List<Polygon> polys = new ArrayList<>();
        polys.add(poly);

        // While current is concave or has too many points, split
        while (!poly.isConvex() || poly.getPointCount() > aMax) {
            poly = poly.splitConvex(aMax);
            polys.add(poly);
        }

        // Return Polygon array
        return polys.toArray(new Polygon[0]);
    }

    /**
     * Splits this polygon into the first convex polygon and the remainder polygon and returns the remainder.
     */
    public Polygon splitConvex(int aMax)
    {
        // Iterate over points to find first one with enough convex segments to split
        int start = 0, cmax = 0;
        for (int i=0,pc=getPointCount();i<pc;i++) {
            int ccc = getConvexCrossbarCount(i, aMax);
            if (ccc>cmax) { //return split(i, ccc); }
                start = i; cmax = ccc; if (cmax==aMax) break; }
        }

        // Split on convex part with max points
        return split(start, cmax);
    }

    /**
     * Returns the number of contained crossbars from given index.
     */
    int getConvexCrossbarCount(int anIndex, int aMax)
    {
        // Iterate over crossbars from given index
        int ccc = 1;
        for (int i=anIndex+2;i<anIndex+aMax;i++,ccc++)
            if (!containsCrossbar(anIndex, i))
                break;

        // If viable count found for index, check next index to see if it supports it as well
        if (ccc>2 && aMax>0)
            ccc = Math.min(ccc, getConvexCrossbarCount(anIndex+1, aMax-1) + 1);

        // Return value
        return ccc;
    }

    /**
     * Returns whether Polygon totally contains line between to indexes.
     */
    boolean containsCrossbar(int ind0, int ind1)
    {
        // Make sure indexes are valid
        int pc = getPointCount(); ind0 %= pc; ind1 %= pc;

        // Get endpoints for crossbar
        double x0 = getX(ind0), y0 = getY(ind0);
        double x1 = getX(ind1), y1 = getY(ind1);

        // Iterate over polygon points and if any sides intersect crossbar, return false
        for (int i=0,iMax=getPointCount();i<iMax;i++) { int j = (i+1)%iMax;
            if (i==ind0 || i==ind1 || j==ind0 || j==ind1) continue;
            double px0 = getX(i), py0 = getY(i);
            double px1 = getX(j), py1 = getY(j);
            if (Line.intersectsLine(px0, py0, px1, py1, x0, y0, x1, y1))
                return false;
        }

        // If polygon also contains midpoint, it contains crossbar
        double mpx = x0 + (x1 - x0)/2, mpy = y0 + (y1 - y0)/2;
        return contains(mpx, mpy);
    }

    /**
     * Splits this polygon into the first convex polygon and the remainder polygon and returns the remainder.
     */
    Polygon split(int start, int len)
    {
        // Get points for remainder
        int pc = getPointCount(), end = start + len;
        int pcr = pc - len + 1;
        double pnts[] = new double[pcr*2];
        for (int i=end>pc ? end%pc : 0, k=0;k<pcr;i++) { if (i<=start || i>=end) {
            pnts[k*2] = getX(i%pc); pnts[k*2+1] = getY(i%pc); k++; } }
        //for (int j=0;j<start+1;j++) { pnts[j*2] = getX(j); pnts[j*2+1] = getY(j); }
        //for (int j=end,k=start+1;j<pc;j++,k++) { pnts[k*2] = getX(j%pc); pnts[k*2+1] = getY(j%pc); }
        Polygon remainder = new Polygon(pnts);

        // Get pnts
        int pc2 = len + 1; pnts = new double[pc2*2];
        for (int j=start,k=0;j<start+pc2;j++,k++) { pnts[k*2] = getX(j%pc); pnts[k*2+1] = getY(j%pc); }
        setPoints(pnts);

        // Create and return remainder
        return remainder;
    }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        if (_pnts.length==0) return new Rect();
        double xmin = _pnts[0], xmax = _pnts[0], ymin = _pnts[1], ymax = _pnts[1];
        for (int i=2;i<_pnts.length;i+=2) {
            double x = _pnts[i];
            double y = _pnts[i+1];
            xmin = Math.min(xmin, x);
            xmax = Math.max(xmax, x);
            ymin = Math.min(ymin, y);
            ymax = Math.max(ymax, y);
        }
        return new Rect(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Returns the path iterator.
     */
    public PathIter getPathIter(Transform aTrans)  { return new PolyIter(_pnts, aTrans); }

    /**
     * Standard clone implementation.
     */
    public Polygon clone()
    {
        try {
            Polygon clone = (Polygon)super.clone();
            clone._pnts = _pnts.clone();
            return clone;
        }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Returns an array of simple polygons for a shape (assuming there are potentially subpaths and such).
     */
    public static Polygon[] getSimplePolys(Shape aShape)
    {
        Shape shape = Shape.makeSimple(aShape);
        PolygonList plist = new PolygonList(shape);
        return plist.getPolys();
    }

    /**
     * Returns an array of convex polysgons for given max side count.
     */
    public static Polygon[] getConvexPolys(Shape aShape, int aMax)
    {
        // Get simple polygons
        Polygon[] simplePolys = getSimplePolys(aShape);

        // Get convex polygons
        List<Polygon> cpolys = new ArrayList<>();
        for (Polygon spoly : simplePolys) {
            Polygon[] cpolys2 = spoly.getConvexPolys(aMax);
            Collections.addAll(cpolys, cpolys2);
        }

        // Return convex polygons
        return cpolys.toArray(new Polygon[0]);
    }

    /**
     * PathIter for Line.
     */
    private static class PolyIter extends PathIter {

        // Ivars
        double _pnts[]; int plen, index;

        /** Create new LineIter. */
        PolyIter(double thePnts[], Transform at)  { super(at); _pnts = thePnts; plen = _pnts.length; }

        /** Returns whether there are more segments. */
        public boolean hasNext() { return plen>0 && index<plen+2; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double[] coords)
        {
            if (index==0)
                return moveTo(_pnts[index++], _pnts[index++], coords);
            if (index<plen)
                return lineTo(_pnts[index++], _pnts[index++], coords);
            if (index==plen) {
                index += 2; return close(); }
            throw new RuntimeException("PolygonIter: Index beyond bounds " + index + " " + plen);
        }
    }
}