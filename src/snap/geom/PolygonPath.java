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
    private Polygon[] _polygons = new Polygon[0];

    // The current polygon
    private Polygon _lastPolygon;

    // The flatness
    private double  _flatDist = DEFAULT_FLAT_DISTANCE;

    // Constant for default flatness
    public static final double DEFAULT_FLAT_DISTANCE = .25;

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
     * Constructor for given Shape.
     */
    public PolygonPath(Shape aShape, double aFlatDistance)
    {
        this();
        setFlatDistance(aFlatDistance);
        appendShape(aShape);
    }

    /**
     * Returns the Polygons.
     */
    public Polygon[] getPolygons()  { return _polygons; }

    /**
     * Returns the number of polygons.
     */
    public int getPolygonCount()  { return _polygons.length; }

    /**
     * Returns the individual polygon at given index.
     */
    public Polygon getPolygon(int anIndex)  { return _polygons[anIndex]; }

    /**
     * Adds a polygon.
     */
    public void addPoly(Polygon aPoly, int anIndex)
    {
        _polygons = ArrayUtils.add(_polygons, aPoly, anIndex);
        shapeChanged();
    }

    /**
     * Returns the last polygon.
     */
    public Polygon getLastPolygon()  { return _lastPolygon; }

    /**
     * Returns the last polygon last point.
     */
    public Point getLastPoint()
    {
        Polygon lastPoly = getLastPolygon();
        return lastPoly != null ? lastPoly.getLastPoint() : null;
    }

    /**
     * Moveto.
     */
    public void moveTo(double aX, double aY)
    {
        // Handle two consecutive MoveTos
        if (_lastPolygon != null && _lastPolygon.getPointCount() == 1)
            _lastPolygon.setPoint(0, aX, aY);

        // Create new poly
        else {
            _lastPolygon = new Polygon();
            addPoly(_lastPolygon, getPolygonCount());
            _lastPolygon.addPoint(aX, aY);
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
        if (_lastPolygon == null)
            moveTo(0, 0);

        // If closing last poly, just return
        double lastX = _lastPolygon.getPointX(0);
        double lastY = _lastPolygon.getPointY(0);
        if (Point.equals(aX, aY, lastX, lastY)) {
            close();
            return;
        }

        // Add point and clear bounds
        _lastPolygon.addPoint(aX, aY);

        // Notify shape changed
        shapeChanged();
    }

    /**
     * Closes the current polygon.
     */
    public void close()
    {
        if (_lastPolygon != null)
            _lastPolygon.setClosed(true);
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
     * Returns the acceptable distance of control points to segment line when flattening curved segments.
     */
    public double getFlatDistance()  { return _flatDist; }

    /**
     * Sets the acceptable distance of control points to segment line when flattening curved segments.
     */
    public void setFlatDistance(double aValue)
    {
        if (aValue == getFlatDistance()) return;
        _flatDist = aValue;
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

        // Iterate over PathIter
        while (aPathIter.hasNext()) {

            // Get next segment and handle
            Seg pathSeg = aPathIter.getNext(points);
            switch (pathSeg) {

                // Handle MoveTo
                case MoveTo: moveTo(points[0], points[1]); break;

                // Handle LineTo
                case LineTo: lineTo(points[0], points[1]); break;

                // Handle QuadTo
                case QuadTo: quadTo(points[0], points[1], points[2], points[3]); break;

                // Handle CubicTo
                case CubicTo: curveTo(points[0], points[1], points[2], points[3], points[4], points[5]); break;

                // Handle Close
                case Close: close(); break;
            }
        }
    }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        // If no polys, return empty rect
        int polyCount = getPolygonCount();
        if (polyCount == 0)
            return new Rect();

        // Get union of all polys
        Rect totalBounds = getPolygon(0).getBounds();
        for (int i = 1; i < polyCount; i++)
            totalBounds.union(getPolygon(i).getBounds());
        return totalBounds;
    }

    /**
     * Returns the path iterator.
     */
    public PathIter getPathIter(Transform aTransform)
    {
        // Array of Polygon.PathIters
        Polygon[] polygons = getPolygons();
        PathIter[] pathIters = ArrayUtils.map(polygons, poly -> poly.getPathIter(aTransform), PathIter.class);
        return PathIter.getPathIterForPathIterArray(pathIters);
    }
}