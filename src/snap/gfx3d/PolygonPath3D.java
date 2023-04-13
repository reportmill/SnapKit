/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.*;
import snap.util.ArrayUtils;
import java.util.stream.Stream;

/**
 * This Shape subclass represents one or more polygons.
 */
public class PolygonPath3D extends FacetShape implements Cloneable {

    // The polygons
    private Polygon3D[] _polygons = new Polygon3D[0];

    // The last polygon
    private Polygon3D _lastPolygon;

    /**
     * Constructor.
     */
    public PolygonPath3D()  { }

    /**
     * Constructor for given Shape.
     */
    public PolygonPath3D(Shape aShape, double aDepth)
    {
        this();
        Shape flatShape = aShape.getFlattenedShape();
        appendShape(flatShape, aDepth);
    }

    /**
     * Returns the Polygons.
     */
    public Polygon3D[] getPolygons()  { return _polygons; }

    /**
     * Returns the number of polygons.
     */
    public int getPolygonCount()  { return _polygons.length; }

    /**
     * Adds a polygon.
     */
    public void addPolygon(Polygon3D aPoly, int anIndex)
    {
        _polygons = ArrayUtils.add(_polygons, aPoly, anIndex);
        clearCachedValues();
    }

    /**
     * Returns the last polygon.
     */
    public Polygon3D getLastPolygon()  { return _lastPolygon; }

    /**
     * Moveto.
     */
    public void moveTo(double aX, double aY, double aDepth)
    {
        // Handle two consecutive MoveTos
        if (_lastPolygon != null && _lastPolygon.getPointCount() == 1)
            _lastPolygon.setPointXYZ(0, aX, aY, aDepth);

        // Create new poly
        else {
            _lastPolygon = new Polygon3D();
            addPolygon(_lastPolygon, getPolygonCount());
            _lastPolygon.addPoint(aX, aY, aDepth);
        }

        // Notify shape changed
        clearCachedValues();
    }

    /**
     * LineTo.
     */
    public void lineTo(double aX, double aY, double aDepth)
    {
        // If no poly, start one
        if (_lastPolygon == null)
            moveTo(0, 0, aDepth);

        // If closing last poly, just return
        Point3D lastPoint = _lastPolygon.getPoint(0);
        if (Point.equals(aX, aY, lastPoint.x, lastPoint.y)) {
            close();
            return;
        }

        // Add point and clear bounds
        _lastPolygon.addPoint(aX, aY, aDepth);

        // Notify shape changed
        clearCachedValues();
    }

    /**
     * Closes the current polygon.
     */
    public void close()
    {
        clearCachedValues();
    }

    /**
     * Appends given shape to this polygon path.
     */
    public void appendShape(Shape aShape, double aDepth)
    {
        Shape flatShape = aShape.getFlattenedShape();
        PathIter pathIter = flatShape.getPathIter(null);
        appendPathIter(pathIter, aDepth);
    }

    /**
     * Appends given PathIter to this polygon path.
     */
    public void appendPathIter(PathIter aPathIter, double aDepth)
    {
        double[] points = new double[6];

        // Iterate over PathIter and add segments
        while (aPathIter.hasNext()) {
            Seg pathSeg = aPathIter.getNext(points);
            switch (pathSeg) {

                // Handle MoveTo
                case MoveTo: moveTo(points[0], points[1], aDepth); break;

                // Handle LineTo
                case LineTo: lineTo(points[0], points[1], aDepth); break;

                // Handle QuadTo/CubicTo
                case QuadTo: case CubicTo:
                    System.err.println("PolygonPath3D.appendPathIter: PathIter must be flat");
                    int index = pathSeg.getCount() * 2 - 2;
                    lineTo(points[index], points[index], aDepth);
                    break;

                // Handle Close
                case Close: close(); break;
            }
        }
    }

    /**
     * Override for this shape.
     */
    @Override
    protected Vector3D createNormal()
    {
        Polygon3D[] polygons = getPolygons();
        Polygon3D polygon = polygons.length > 0 ? polygons[0] : null;
        return polygon != null ? polygon.getNormal() : new Vector3D(1, 0, 0);
    }

    /**
     * Override to support polygon array.
     */
    @Override
    public int getPointCount()
    {
        // Get polygons (if only one, just return its point count)
        Polygon3D[] polygons = getPolygons();
        if (polygons.length == 1)
            return polygons[0].getPointCount();

        // Return sum of Polygons.PointCount
        return Stream.of(polygons).mapToInt(poly -> poly.getPointCount()).sum();
    }

    /**
     * Override to support polygon array.
     */
    @Override
    public Point3D getPoint(int anIndex)
    {
        // Get polygons (if only one, just return its point count)
        Polygon3D[] polygons = getPolygons();
        if (polygons.length == 1)
            return polygons[0].getPoint(anIndex);

        // Iterate over polygons
        int index = anIndex;
        for (Polygon3D polygon : polygons) {
            int pointCount = polygon.getPointCount();
            if (index < pointCount)
                return polygon.getPoint(index);
            index -= pointCount;
        }

        // Throw index out of bounds exception
        throw new ArrayIndexOutOfBoundsException("PolygonPath3D.getPoint");
    }

    /**
     * Override to return combined Shape2D for polygons.
     */
    @Override
    protected Bounds3D createBounds3D()
    {
        // Get Polygons (if only one, just return its shape)
        Polygon3D[] polygons = getPolygons();
        if (polygons.length == 0)
            return new Bounds3D();
        if (polygons.length == 1)
            return polygons[0].getBounds3D();

        // Get combined Bounds3D
        Bounds3D bounds3D = polygons[0].getBounds3D().clone();
        for (int i = 1; i < polygons.length; i++) {
            Polygon3D polygon3D = polygons[i];
            Bounds3D polygonBounds = polygon3D.getBounds3D();
            bounds3D.addXYZ(polygonBounds.getMinX(), polygonBounds.getMinY(), polygonBounds.getMinZ());
            bounds3D.addXYZ(polygonBounds.getMaxX(), polygonBounds.getMaxY(), polygonBounds.getMaxZ());
        }

        // Return
        return bounds3D;
    }

    /**
     * Override to return combined Shape2D for polygons.
     */
    @Override
    public Shape getShape2D()
    {
        // Get Polygons (if only one, just return its shape)
        Polygon3D[] polygons = getPolygons();
        if (polygons.length == 1)
            return polygons[0].getShape2D();

        // Create PolygonPath and add each polygon3D.Shape2D to it
        PolygonPath polygonPath = new PolygonPath();
        for (Polygon3D polygon3D : polygons) {
            Shape shape2D = polygon3D.getShape2D();
            polygonPath.appendShape(shape2D);
        }

        // Return
        return polygonPath;
    }

    /**
     * Returns a VertexArray for path stroke.
     */
    protected VertexArray getStrokeTriangleArray()
    {
        // Get Polygons (if only one, just return its StrokeTriangleArray)
        Polygon3D[] polygons = getPolygons();
        if (polygons.length == 0)
            return null;
        if (polygons.length == 1)
            return polygons[0].getStrokeTriangleArray();

        // Get combined Bounds3D
        VertexArray strokeTriangleArray = polygons[0].getStrokeTriangleArray();
        VertexArray strokeTriangleArrayTail = strokeTriangleArray;
        for (int i = 1; i < polygons.length; i++) {
            Polygon3D polygon3D = polygons[i];
            VertexArray polygonStrokeTriangleArray = polygon3D.getStrokeTriangleArray();
            strokeTriangleArrayTail.setNext(polygonStrokeTriangleArray);
            strokeTriangleArrayTail = polygonStrokeTriangleArray;
        }

        // Return
        return strokeTriangleArray;
    }

    /**
     * Override to support this class.
     */
    @Override
    public void reverse()
    {
        Polygon3D[] polygons = getPolygons();
        for (Polygon3D polygon : polygons)
            polygon.reverse();

        // Clear cached values
        clearCachedValues();
    }

    /**
     * Override to support this class.
     */
    @Override
    public PolygonPath3D copyForMatrix(Matrix3D aTrans)
    {
        PolygonPath3D clone = clone();
        for (int i = 0; i < _polygons.length; i++)
            clone._polygons[i] = _polygons[i].copyForMatrix(aTrans);

        // Clear cached values
        clone.clearCachedValues();

        // Return
        return clone;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public PolygonPath3D clone()
    {
        // Do normal version
        PolygonPath3D clone  = (PolygonPath3D) super.clone();

        // Clone Polygons array
        clone._polygons = _polygons.clone();

        // Return
        return clone;
    }

    /**
     * Creates a Shape3D (either Polygon3D or PolygonPath3D) for given shape and depth.
     */
    public static FacetShape createFacetShapeForShapeAndDepth(Shape aShape, double aDepth)
    {
        PolygonPath3D polygonPath3D = new PolygonPath3D(aShape, aDepth);
        Polygon3D[] polygons = polygonPath3D.getPolygons();
        if (polygons.length == 1)
            return polygons[0];
        return polygonPath3D;
    }
}