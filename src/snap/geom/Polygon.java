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
    private double[] _pointArray = new double[8];

    // The point count
    private int _pointCount;

    // Whether polygon is closed or not
    private boolean _closed = true;

    /**
     * Constructor.
     */
    public Polygon()
    {
        super();
    }

    /**
     * Creates a new Polygon from given x y coords.
     */
    public Polygon(double ... theCoords)
    {
        super();
        _pointArray = theCoords;
        _pointCount = theCoords.length / 2;
    }

    /**
     * Creates a new Polygon from given x y coords.
     */
    public Polygon(Point ... thePoints)
    {
        super();
        _pointCount = thePoints.length;
        _pointArray = new double[_pointCount * 2];
        for (int i = 0; i < _pointCount; i++) {
            _pointArray[i * 2] = thePoints[i].x;
            _pointArray[i * 2 + 1] = thePoints[i].y;
        }
    }

    /**
     * Returns the raw points array.
     */
    public double[] getPointArray()
    {
        // Trim array to exact point count
        int pointArrayLen = _pointCount * 2;
        if (pointArrayLen != _pointArray.length)
            _pointArray = Arrays.copyOf(_pointArray, pointArrayLen);

        // Return
        return _pointArray;
    }

    /**
     * Sets the points.
     */
    public void setPointArray(double[] thePoints)
    {
        _pointArray = thePoints;
        _pointCount = thePoints.length / 2;
        shapeChanged();
    }

    /**
     * Returns the point count.
     */
    public int getPointCount()  { return _pointCount; }

    /**
     * Returns the x at given point index.
     */
    public double getPointX(int anIndex)  { return _pointArray[anIndex * 2]; }

    /**
     * Returns the y at given point index.
     */
    public double getPointY(int anIndex)  { return _pointArray[anIndex * 2 + 1]; }

    /**
     * Returns the point at given index.
     */
    public Point getPoint(int anIndex)
    {
        int coordIndex = anIndex * 2;
        double pointX = _pointArray[coordIndex];
        double pointY = _pointArray[coordIndex + 1];
        return new Point(pointX, pointY);
    }

    /**
     * Adds a point at given x/y.
     */
    public void addPoint(double aX, double aY)
    {
        // Make sure array is long enough
        int coordIndex = _pointCount * 2;
        if (coordIndex + 2 >= _pointArray.length)
            _pointArray = Arrays.copyOf(_pointArray, Math.max(_pointArray.length * 2, 8));

        // Add point
        _pointArray[coordIndex] = aX;
        _pointArray[coordIndex + 1] = aY;
        _pointCount++;
        shapeChanged();
    }

    /**
     * Sets a point at given point index to given x/y.
     */
    public void setPoint(int anIndex, double aX, double aY)
    {
        int coordIndex = anIndex * 2;
        _pointArray[coordIndex] = aX;
        _pointArray[coordIndex + 1] = aY;
        shapeChanged();
    }

    /**
     * Returns whether the polygon is closed.
     */
    public boolean isClosed()  { return _closed; }

    /**
     * Sets whether the polygon is closed.
     */
    public void setClosed(boolean aValue)
    {
        if (aValue == _closed) return;
        _closed = aValue;
    }

    /**
     * Returns the last point.
     */
    public Point getLastPoint()
    {
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        return new Point(lastX, lastY);
    }

    /**
     * Returns the last x point.
     */
    public double getLastPointX()
    {
        int coordIndex = _pointCount * 2 - 2;
        return coordIndex >= 0 ? _pointArray[coordIndex] : 0;
    }

    /**
     * Returns the last y point.
     */
    public double getLastPointY()
    {
        int coordIndex = _pointCount * 2 - 1;
        return coordIndex > 0 ? _pointArray[coordIndex] : 0;
    }

    /**
     * Clears the polygon.
     */
    public void clear()
    {
        _pointCount = 0;
        shapeChanged();
    }

    /**
     * Returns index of point at given x/y within given radius.
     */
    public int getPointIndexForXY(double aX, double aY, double aRad)
    {
        for (int i = 0, pc = getPointCount(); i < pc; i++)
            if (Point.getDistance(aX, aY, getPointX(i), getPointY(i)) <= aRad)
                return i;
        return -1;
    }

    /**
     * Returns whether polygon has no intersecting lines.
     */
    public boolean isSelfIntersecting()
    {
        // Get point count (if not at least 3 points, return false)
        int pointCount = getPointCount();
        if (pointCount < 3)
            return false;

        // Iterate over all lines
        for (int i = 0; i < pointCount - 1; i++) {
            int j = (i + 1) % pointCount;

            // Get line endpoint and see if next point is collinear
            double x0 = getPointX(i), y0 = getPointY(i);
            double x1 = getPointX(j), y1 = getPointY(j);

            // If next point is collinear and backtracks over previous segment, return true.
            int jp1 = (j + 1) % pointCount;
            double jp1x = getPointX(jp1);
            double jp1y = getPointY(jp1);
            boolean isCollinear = Line.isCollinear(x0, y0, x1, y1, jp1x, jp1y);
            if (isCollinear && (jp1x - x0) / (x1 - x0) < 1)
                return true;

            // Iterate over remaining lines and see if they intersect
            for (int k = j + 1; k < pointCount; k++) {
                int l = (k + 1) % pointCount;
                double x2 = getPointX(k), y2 = getPointY(k);
                double x3 = getPointX(l), y3 = getPointY(l);
                boolean intersectsLine = Line.intersectsLine(x0, y0, x1, y1, x2, y2, x3, y3);
                if (intersectsLine && i != l) // Suppress last
                    return true;
            }
        }

        // Return no intersections
        return false;
    }

    /**
     * Returns whether polygon is convex.
     */
    public boolean isConvex()
    {
        if (getPointCount() < 3)
            return true;
        double extAngles = Math.toDegrees(getExtAngleSum());
        return MathUtils.equals(extAngles, 360); // Could also do intAngles == (SideCount-2)*180
    }

    /**
     * Returns the interior angle at given point index.
     */
    public double getAngle(int anIndex)
    {
        // Get 3 points surrounding index, get vector points, and return angle between
        int pointCount = getPointCount();
        if (pointCount < 3)
            return 0;
        int i0 = (anIndex - 1 + pointCount) % pointCount;
        int i1 = anIndex;
        int i2 = (anIndex + 1) % pointCount;
        double x0 = getPointX(i0), y0 = getPointY(i0);
        double x1 = getPointX(i1), y1 = getPointY(i1);
        double x2 = getPointX(i2), y2 = getPointY(i2);

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
    public double getExtAngle(int anIndex)
    {
        return Math.PI - getAngle(anIndex);
    }

    /**
     * Returns the sum of exterior angles.
     */
    public double getExtAngleSum()
    {
        double angle = 0;
        int pointCount = getPointCount();
        if (pointCount < 3)
            return 0;
        for (int i = 0; i < pointCount; i++)
            angle += getExtAngle(i);
        return angle;
    }

    /**
     * Returns an array of polygons that are convex with max number of vertices.
     */
    public Polygon[] getConvexPolygonsWithMaxSideCount(int aMax)
    {
        // If not simple, get simples
        if (isSelfIntersecting()) {

            // Complain
            System.err.println("Polygon.getConvexPolygonsWithMaxSideCount: Is self intersecting - shouldn't happen");

            // Get simple polygons
            Shape simpleShape = Shape.getNotSelfIntersectingShape(this);
            PolygonPath polygonPath = new PolygonPath(simpleShape);
            Polygon[] simplePolygons = polygonPath.getPolygons();

            // Get convex polygons for simple polygons
            List<Polygon> convexPolygons = new ArrayList<>();
            for (Polygon polygon : simplePolygons) {
                Polygon[] convexPolygons2 = polygon.getConvexPolygonsWithMaxSideCount(aMax);
                Collections.addAll(convexPolygons, convexPolygons2);
            }

            // Return
            return convexPolygons.toArray(new Polygon[0]);
        }

        // Create list with clone of first poly
        Polygon polygon = clone();
        List<Polygon> convexPolygons = new ArrayList<>();
        convexPolygons.add(polygon);

        // While current is concave or has too many points, split
        while (!polygon.isConvex() || polygon.getPointCount() > aMax) {
            polygon = polygon.splitConvex(aMax);
            convexPolygons.add(polygon);
        }

        // Return Polygon array
        return convexPolygons.toArray(new Polygon[0]);
    }

    /**
     * Splits this polygon into the first convex polygon and the remainder polygon and returns the remainder.
     */
    public Polygon splitConvex(int aMax)
    {
        // Iterate over points to find first one with enough convex segments to split
        int start = 0;
        int cmax = 0;
        for (int i = 0, pc = getPointCount(); i < pc; i++) {
            int ccc = getConvexCrossbarCount(i, aMax);
            if (ccc > cmax) { //return split(i, ccc); }
                start = i;
                cmax = ccc;
                if (cmax == aMax) break;
            }
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
        for (int i = anIndex + 2; i < anIndex + aMax; i++, ccc++)
            if (!containsCrossbar(anIndex, i))
                break;

        // If viable count found for index, check next index to see if it supports it as well
        if (ccc > 2 && aMax > 0) {
            ccc = Math.min(ccc, getConvexCrossbarCount(anIndex + 1, aMax - 1) + 1);
        }

        // Return value
        return ccc;
    }

    /**
     * Returns whether Polygon totally contains line between to indexes.
     */
    boolean containsCrossbar(int ind0, int ind1)
    {
        // Make sure indexes are valid
        int pc = getPointCount();
        ind0 %= pc;
        ind1 %= pc;

        // Get endpoints for crossbar
        double x0 = getPointX(ind0), y0 = getPointY(ind0);
        double x1 = getPointX(ind1), y1 = getPointY(ind1);

        // Iterate over polygon points and if any sides intersect crossbar, return false
        for (int i = 0, iMax = getPointCount(); i < iMax; i++) {
            int j = (i + 1) % iMax;
            if (i == ind0 || i == ind1 || j == ind0 || j == ind1) continue;
            double px0 = getPointX(i), py0 = getPointY(i);
            double px1 = getPointX(j), py1 = getPointY(j);
            if (Line.intersectsLine(px0, py0, px1, py1, x0, y0, x1, y1))
                return false;
        }

        // If polygon also contains midpoint, it contains crossbar
        double mpx = x0 + (x1 - x0) / 2, mpy = y0 + (y1 - y0) / 2;
        return contains(mpx, mpy);
    }

    /**
     * Splits this polygon into the first convex polygon and the remainder polygon and returns the remainder.
     */
    Polygon split(int start, int len)
    {
        // Get points for remainder
        int pointCount = getPointCount();
        int end = start + len;
        int remainderPointCount = pointCount - len + 1;
        double[] points = new double[remainderPointCount * 2];
        for (int i = end > pointCount ? end % pointCount : 0, k = 0; k < remainderPointCount; i++) {
            if (i <= start || i >= end) {
                points[k * 2] = getPointX(i % pointCount);
                points[k * 2 + 1] = getPointY(i % pointCount);
                k++;
            }
        }

        // Create remainder
        Polygon remainder = new Polygon(points);

        // Get points for this
        int pointCount2 = len + 1;
        points = new double[pointCount2 * 2];
        for (int j = start, k = 0; j < start + pointCount2; j++, k++) {
            points[k * 2] = getPointX(j % pointCount);
            points[k * 2 + 1] = getPointY(j % pointCount);
        }
        setPointArray(points);

        // Return
        return remainder;
    }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        // Simple case
        if (_pointCount == 0) return new Rect();

        double minX = _pointArray[0], maxX = _pointArray[0];
        double minY = _pointArray[1], maxY = _pointArray[1];
        int coordCount = _pointCount * 2;
        for (int i = 2; i < coordCount; i += 2) {
            double x = _pointArray[i];
            double y = _pointArray[i + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        // Return
        return new Rect(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Returns the path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return new PolygonIter(this, aTrans);
    }

    /**
     * Standard clone implementation.
     */
    public Polygon clone()
    {
        Polygon clone;
        try { clone = (Polygon) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        clone._pointArray = _pointArray.clone();
        return clone;
    }

    /**
     * Returns an array of convex polygons for given max side count.
     */
    public static Polygon[] getConvexPolygonsWithMaxSideCount(Shape aShape, int aMax)
    {
        // Get simple polygons
        Shape simpleShape = Shape.getNotSelfIntersectingShape(aShape);
        PolygonPath polygonPath = new PolygonPath(simpleShape);
        Polygon[] simplePolygons = polygonPath.getPolygons();

        // Get convex polygons
        List<Polygon> convexPolygons = new ArrayList<>();
        for (Polygon simplePolygon : simplePolygons) {
            Polygon[] convexPolygons2 = simplePolygon.getConvexPolygonsWithMaxSideCount(aMax);
            Collections.addAll(convexPolygons, convexPolygons2);
        }

        // Return convex polygons
        return convexPolygons.toArray(new Polygon[0]);
    }

    /**
     * PathIter for Polygon.
     */
    private static class PolygonIter extends PathIter {

        // Ivars
        private double[] _points;
        private int _coordCount;
        private int _coordIndex;
        private boolean _needsClose;

        /**
         * Constructor.
         */
        private PolygonIter(Polygon aPolygon, Transform at)
        {
            super(at);
            _points = aPolygon._pointArray;
            _coordCount = aPolygon.getPointCount() * 2;
            _needsClose = aPolygon.isClosed();
        }

        /**
         * Returns whether there are more segments.
         */
        public boolean hasNext()
        {
            return _coordIndex < _coordCount || _needsClose;
        }

        /**
         * Returns the coordinates and type of the current path segment in the iteration.
         */
        public Seg getNext(double[] coords)
        {
            // MoveTo first point
            if (_coordIndex == 0)
                return moveTo(_points[_coordIndex++], _points[_coordIndex++], coords);

            // LineTo successive points
            if (_coordIndex < _coordCount)
                return lineTo(_points[_coordIndex++], _points[_coordIndex++], coords);

            // Close on last point
            if (_needsClose) {
                _needsClose = false;
                return close();
            }

            // Throw exception if beyond
            throw new RuntimeException("PolygonIter: Index beyond bounds " + _coordIndex + " " + _coordCount);
        }
    }
}