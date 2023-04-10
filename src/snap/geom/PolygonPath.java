/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.ArrayUtils;

/**
 * This Shape subclass represents one or more polygons.
 */
public class PolygonPath extends Shape {

    // The polygons
    private Polygon[]  _polys = new Polygon[0];

    // The number of polygons
    private int  _polyCount;

    // The current polygon
    private Polygon  _poly;

    // The flatness
    private double  _flatDist = .5;

    /**
     * Constructor.
     */
    public PolygonPath()  { }

    /**
     * Constructor for given Shape.
     */
    public PolygonPath(Shape aShape)
    {
        this();
        appendShape(aShape);
    }

    /**
     * Appends given shape to this polygon path.
     */
    public void appendShape(Shape aShape)
    {
        PathIter pathIter = aShape.getPathIter(null);
        appendPathIter(pathIter);
    }

    /**
     * Appends given PathIter to this polygon path.
     */
    public void appendPathIter(PathIter aPathIter)
    {
        double[] points = new double[6];

        while (aPathIter.hasNext()) {
            Seg pathSeg = aPathIter.getNext(points);
            switch (pathSeg) {
                case MoveTo: moveTo(points[0], points[1]); break;
                case LineTo: lineTo(points[0], points[1]); break;
                case QuadTo: quadTo(points[0], points[1], points[2], points[3]); break;
                case CubicTo: curveTo(points[0], points[1], points[2], points[3], points[4], points[5]); break;
                case Close: break;
            }
        }
    }

    /**
     * Returns the Polygons.
     */
    public Polygon[] getPolygons()  { return _polys; }

    /**
     * Returns the number of polygons.
     */
    public int getPolyCount()  { return _polyCount; }

    /**
     * Returns the individual polygon at given index.
     */
    public Polygon getPoly(int anIndex)
    {
        return _polys[anIndex];
    }

    /**
     * Adds a polygon.
     */
    public void addPoly(Polygon aPoly, int anIndex)
    {
        _polys = ArrayUtils.add(_polys, aPoly, anIndex);
        _polyCount++;
        shapeChanged();
    }

    /**
     * Returns the last polygon.
     */
    public Polygon getLast()
    {
        if (_polyCount == 0)
            return null;
        return _polys[_polyCount - 1];
    }

    /**
     * Returns the last polygon last point.
     */
    public Point getLastPoint()
    {
        Polygon lastPoly = getLast();
        return lastPoly != null ? lastPoly.getLastPoint() : null;
    }

    /**
     * Moveto.
     */
    public void moveTo(double aX, double aY)
    {
        // Handle two consecutive MoveTos
        if (_poly != null && _poly.getPointCount() == 1)
            _poly.setPoint(0, aX, aY);

        // Create new poly
        else {
            _poly = new Polygon();
            addPoly(_poly, getPolyCount());
            _poly.addPoint(aX, aY);
        }

        // Notify shape changed
        shapeChanged();
    }

    /**
     * LineTo.
     */
    public void lineTo(double aX, double aY)
    {
        // If no poly, start one
        if (_poly == null)
            moveTo(0, 0);

        // If closing last poly, just return
        double lastX = _poly.getX(0);
        double lastY = _poly.getY(0);
        if (Point.equals(aX, aY, lastX, lastY))
            return;

        // Add point and clear bounds
        _poly.addPoint(aX, aY);

        // Notify shape changed
        shapeChanged();
    }

    /**
     * QuadTo by adding lineTos.
     */
    public void quadTo(double cpx, double cpy, double x, double y)
    {
        // If distance from control point to base line less than tolerance, just add line
        Point last = getLastPoint();
        double dist0 = Point.getDistance(last.x, last.y, x, y);
        if (dist0 < _flatDist)
            return;
        double dist1 = Line.getDistance(last.x, last.y, x, y, cpx, cpy);
        if (dist1 < _flatDist) {
            lineTo(x, y);
            return;
        }

        // Split curve at midpoint and add parts
        Quad c0 = new Quad(last.x, last.y, cpx, cpy, x, y), c1 = c0.split(.5);
        quadTo(c0.cpx, c0.cpy, c0.x1, c0.y1);
        quadTo(c1.cpx, c1.cpy, c1.x1, c1.y1);
    }

    /**
     * CubicTo by adding lineTos.
     */
    public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y)
    {
        // If distance from control points to base line less than tolerance, just add line
        Point last = getLastPoint();
        double dist0 = Point.getDistance(last.x, last.y, x, y);
        if (dist0 < _flatDist)
            return;
        double dist1 = Line.getDistance(last.x, last.y, x, y, cp1x, cp1y);
        double dist2 = Line.getDistance(last.x, last.y, x, y, cp2x, cp2y);
        if (dist1 < _flatDist && dist2 < _flatDist) {
            lineTo(x, y);
            return;
        }

        // Split curve at midpoint and add parts
        Cubic c0 = new Cubic(last.x, last.y, cp1x, cp1y, cp2x, cp2y, x, y), c1 = c0.split(.5);
        curveTo(c0.cp0x, c0.cp0y, c0.cp1x, c0.cp1y, c0.x1, c0.y1);
        curveTo(c1.cp0x, c1.cp0y, c1.cp1x, c1.cp1y, c1.x1, c1.y1);
    }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        // If no polys, return empty rect
        int polyCount = getPolyCount();
        if (polyCount == 0)
            return new Rect();

        // Get union of all polys
        Rect totalBounds = getPoly(0).getBounds();
        for (int i = 1; i < polyCount; i++)
            totalBounds.union(getPoly(i).getBounds());
        return totalBounds;
    }

    /**
     * Returns the path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return new PolygonPathIter(this, aTrans);
    }

    /**
     * PathIter for PolygonPath.
     */
    private static class PolygonPathIter extends PathIter {

        // Ivars
        private PathIter[] _pathIters;
        private PathIter _pathIter;
        private int _polyCount;
        private int _polyIndex;

        /**
         * Constructor.
         */
        private PolygonPathIter(PolygonPath polygonPath, Transform at)
        {
            super(at);

            Polygon[] polygons = polygonPath.getPolygons();
            _polyCount = polygons.length;
            _pathIters = new PathIter[_polyCount];
            for (int i = 0; i < _polyCount; i++)
                _pathIters[i] = polygons[i].getPathIter(at);
            _pathIter = _polyCount > 0 ? _pathIters[0] : null;
        }

        /**
         * Returns whether there are more segments.
         */
        public boolean hasNext()
        {
            return _pathIter != null && _pathIter.hasNext();
        }

        /**
         * Returns the coordinates and type of the current path segment in the iteration.
         */
        public Seg getNext(double[] coords)
        {
            Seg seg = _pathIter.getNext(coords);
            while (!_pathIter.hasNext() && _polyIndex < _polyCount)
                _pathIter = _pathIters[_polyIndex++];
            return seg;
        }
    }
}