/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.*;
import snap.gfx.Color;
import java.util.*;

/**
 * This class represents a path in 3D space.
 */
public class Path3D extends FacetShape implements Cloneable {
    
    // The list of elements in this path
    private List<Seg>  _segs = new ArrayList<>();
    
    // The list of point3Ds in this path
    private List<Point3D>  _points = new ArrayList<>();
    
    // Cached pointers for iterating efficiently over the path
    private int  _nextElementIndex = -100;
    private int  _nextPointIndex = -100;

    // Constants for path construction element types
    public static final Seg MOVE_TO = Seg.MoveTo;
    public static final Seg LINE_TO = Seg.LineTo;
    public static final Seg QUAD_TO = Seg.QuadTo;
    public static final Seg CURVE_TO = Seg.CubicTo;
    public static final Seg CLOSE = Seg.Close;

    /**
     * Constructor.
     */
    public Path3D()
    {
        super();
    }

    /**
     * Constructor for a 2D path with a depth.
     */
    public Path3D(Shape aShape, double aDepth)
    {
        addShapePath(aShape, aDepth);
    }

    /**
     * Returns the number of segments in this Path3d.
     */
    public int getSegCount()  { return _segs.size(); }

    /**
     * Returns the Seg at given index.
     */
    public Seg getSeg(int anIndex)  { return _segs.get(anIndex); }

    /**
     * Returns the number of points in this Path3d.
     */
    @Override
    public int getPointCount()  { return _points.size(); }

    /**
     * Returns the Point3D at given index.
     */
    @Override
    public Point3D getPoint(int anIndex)  { return _points.get(anIndex); }

    /**
     * Returns the element at the given index.
     */
    public Seg getSeg(int anIndex, Point3D[] pts)
    {
        // Get element type (if no points, just return type)
        Seg type = getSeg(anIndex);
        if (pts == null)
            return type;

        // If given index isn't equal to "next index" optimizer, reset next index ivar
        if (anIndex != _nextElementIndex) {
            _nextPointIndex = 0;
            for (int i = 0; i < anIndex; i++) {
                Seg seg = _segs.get(i);
                _nextPointIndex += seg == MOVE_TO || seg == LINE_TO ? 1 : seg == QUAD_TO ? 2 : seg == CURVE_TO ? 3 : 0;
            }
        }

        // Handle element types
        switch (type) {
            case MoveTo:
            case LineTo:
                pts[0] = getPoint(_nextPointIndex++);
                break;
            case QuadTo:
                pts[0] = getPoint(_nextPointIndex++);
                pts[1] = getPoint(_nextPointIndex++);
                break;
            case CubicTo:
                pts[0] = getPoint(_nextPointIndex++);
                pts[1] = getPoint(_nextPointIndex++);
                pts[2] = getPoint(_nextPointIndex++);
                break;
            case Close: break;
        }

        // Update next element pointer and return
        _nextElementIndex = anIndex+1;
        return type;
    }

    /**
     * Adds a moveto to the path3d with the given 3D coords.
     */
    public void moveTo(double x, double y, double z)
    {
        // If previous Seg is MoveTo, remove Seg/Point to avoid consecutive MoveTos
        int segCount = getSegCount();
        if (segCount > 0 && getSeg(segCount - 1) == Seg.MoveTo) {
            _segs.remove(segCount - 1);
            int pointCount = getPointCount();
            _points.remove(pointCount - 1);
        }

        // Add Seg and Point
        _segs.add(MOVE_TO);
        _points.add(new Point3D(x, y, z));
        clearCachedValues();
    }

    /**
     * Adds a line to the path3d with the given 3D coords.
     */
    public void lineTo(double x, double y, double z)
    {
        _segs.add(LINE_TO);
        _points.add(new Point3D(x, y, z));
        clearCachedValues();
    }

    /**
     * Adds a quad to to the path3d with the given 3D control point and coords.
     */
    public void quadTo(double cpx, double cpy, double cpz, double x, double y, double z)
    {
        _segs.add(QUAD_TO);
        _points.add(new Point3D(cpx, cpy, cpz));
        _points.add(new Point3D(x, y, z));
        clearCachedValues();
    }

    /**
     * Adds a curve-to to the path3d with the given 3d coords.
     */
    public void curveTo(double cp1x,double cp1y,double cp1z,double cp2x,double cp2y,double cp2z,double x,double y,double z)
    {
        _segs.add(CURVE_TO);
        _points.add(new Point3D(cp1x, cp1y, cp1z));
        _points.add(new Point3D(cp2x, cp2y, cp2z));
        _points.add(new Point3D(x, y, z));
        clearCachedValues();
    }

    /**
     * Adds a close element to the path3d.
     */
    public void close()
    {
        _segs.add(CLOSE);
    }

    /**
     * Adds a Shape path to this path3D at given depth.
     */
    public void addShapePath(Shape aPath, double aDepth)
    {
        // Iterate over elements in given path
        PathIter pathIter = aPath.getPathIter(null);
        double[] pts = new double[6];

        // Iterate over elements in given path
        while (pathIter.hasNext()) {

            // Get/handle Seg
            Seg seg = pathIter.getNext(pts);
            switch (seg) {

                // Handle MoveTo
                case MoveTo:
                    moveTo(pts[0], pts[1], aDepth);
                    break;

                // Handle LineTo
                case LineTo:
                    lineTo(pts[0], pts[1], aDepth);
                    break;

                // Handle QuadTo
                case QuadTo:
                    quadTo(pts[0], pts[1], aDepth, pts[2], pts[3], aDepth);
                    break;

                // Handle CubicTo
                case CubicTo:
                    curveTo(pts[0], pts[1], aDepth, pts[2], pts[3], aDepth, pts[4], pts[5], aDepth);
                    break;
                case Close: close(); break;
            }
        }
    }

    /**
     * Returns the normal of the path3d. Right hand rule for clockwise/counter-clockwise defined polygons.
     */
    @Override
    protected Vector3D createNormal()
    {
        // Calculate least-square-fit normal. Works for either convex or concave polygons.
        // Reference is Newell's Method for Computing the Plane Equation of a Polygon.
        //   Graphics Gems III, David Kirk (Ed.), AP Professional, 1992.
        Vector3D normal = new Vector3D(0, 0, 0);
        for (int i = 0, pointCount = getPointCount(); i < pointCount; i++) {
            Point3D thisPoint = getPoint(i);
            Point3D nextPoint = getPoint((i+1) % pointCount);
            normal.x += (thisPoint.y - nextPoint.y) * (thisPoint.z + nextPoint.z);
            normal.y += (thisPoint.z - nextPoint.z) * (thisPoint.x + nextPoint.x);
            normal.z += (thisPoint.x - nextPoint.x) * (thisPoint.y + nextPoint.y);
        }

        // Normalize the result and swap sign so it matches right hand rule
        normal.normalize();
        return normal;
    }

    /**
     * Returns the 2D shape for the path3d (should only be called when path is facing Z).
     */
    @Override
    public Shape getShape2D()
    {
        // Create new path
        Path2D path = new Path2D();
        Point3D[] pts = new Point3D[3];

        // Iterate over this path3d and add segments as 2D
        for (int i = 0, iMax = getSegCount(); i < iMax; i++) {
            Seg type = getSeg(i, pts);
            switch (type) {
                case MoveTo: path.moveTo(pts[0].x, pts[0].y); break;
                case LineTo: path.lineTo(pts[0].x, pts[0].y); break;
                case QuadTo: path.quadTo(pts[0].x, pts[0].y, pts[1].x, pts[1].y); break;
                case CubicTo: path.curveTo(pts[0].x, pts[0].y, pts[1].x, pts[1].y, pts[2].x, pts[2].y); break;
                case Close: path.close();
            }
        }

        // Draw surface normals - handy for debugging
        //Point3D c = getCenter(); Vector3D n = getNormal(); path.moveTo(c.x,c.y); path.lineTo(c.x+n.x*20,c.y+.y*20);
        return path;
    }

    /**
     * Returns a VertexArray for path stroke.
     */
    protected VertexArray getStrokeTriangleArray()
    {
        // Get info
        Vector3D pathNormal = getNormal();
        Color strokeColor = getStrokeColor();
        double strokeWidth = getStroke() != null ? getStroke().getWidth() : 1;

        // Create/configure VertexArray
        VertexArray vertexArray = new VertexArray();
        vertexArray.setColor(strokeColor != null ? strokeColor : Color.BLACK);

        // Path3D iteration vars
        int segCount = getSegCount();
        Point3D[] points = new Point3D[3];
        Point3D movePoint = new Point3D(0, 0, 0);
        Point3D lastPoint = movePoint;

        // Iterate over segments and render lines
        for (int i = 0; i < segCount; i++) {
            Seg seg = getSeg(i, points);
            switch (seg) {

                case MoveTo:
                    movePoint = lastPoint = points[0];
                    break;

                case LineTo: {
                    Point3D p0 = lastPoint, p1 = points[0];
                    VertexArrayUtils.addLineStrokePoints(vertexArray, p0, p1, pathNormal, strokeWidth);
                    lastPoint = p1;
                    break;
                }

                case Close: {
                    Point3D p0 = lastPoint, p1 = movePoint;
                    VertexArrayUtils.addLineStrokePoints(vertexArray, p0, p1, pathNormal, strokeWidth);
                    lastPoint = p1;
                    break;
                }

                default:
                    if (!_didRenderPath3DStrokedError) {
                        System.err.println("Path3D:getStrokeVertexArray: Unsupported Seg: " + seg);
                        _didRenderPath3DStrokedError = true;
                    }
                    break;
            }
        }

        // Return
        return vertexArray;
    }

    // Error var
    private static boolean  _didRenderPath3DStrokedError;

    /**
     * Transforms the path by the given transform matrix.
     */
    public void transform(Matrix3D xform)
    {
        for (int i = 0, iMax = getPointCount(); i < iMax; i++) {
            Point3D point = getPoint(i);
            xform.transformPoint(point);
        }
        clearCachedValues();
    }

    /**
     * Copies path for given transform matrix.
     */
    @Override
    public Path3D copyForMatrix(Matrix3D aTrans)
    {
        Path3D copy = clone();
        copy.transform(aTrans);
        return copy;
    }

    /**
     * Reverses the path3d.
     */
    @Override
    public void reverse()
    {
        reverse(0, null, null);
    }

    /**
     * Reverse method worker method.
     */
    private void reverse(int element, Point3D lastPoint, Point3D lastMoveTo)
    {
        // Simply return if element is beyond bounds
        if (element == getSegCount()) {
            _segs.clear(); _points.clear(); clearCachedValues();
            return;
        }

        // Get info for this element
        Point3D[] pts = new Point3D[3];
        Point3D lp = null;
        Point3D lmt = lastMoveTo;
        Seg type = getSeg(element, pts);
        switch (type) {
            case MoveTo: lmt = pts[0];
            case LineTo: lp = pts[0]; break;
            case QuadTo: lp = pts[1]; break;
            case CubicTo: lp = pts[2]; break;
            case Close: lp = lastMoveTo;
        }

        // Recursively add following elements before this one
        Seg nextType = element+1 < getSegCount() ? getSeg(element+1,null) : null;
        reverse(element+1, lp, lmt);

        // Add reverse element to path for current element
        switch (type) {
            case MoveTo:
                if (nextType != MOVE_TO)
                    close();
                break;
            case LineTo:
                if (!lastPoint.equals(lastMoveTo))
                    lineTo(lastPoint.x, lastPoint.y, lastPoint.z);
                break;
            case QuadTo:
                quadTo(pts[0].x, pts[0].y, pts[0].z, lastPoint.x, lastPoint.y, lastPoint.z);
                break;
            case CubicTo:
                curveTo(pts[1].x, pts[1].y, pts[1].z, pts[0].x, pts[0].y, pts[0].z, lastPoint.x, lastPoint.y, lastPoint.z);
                break;
            case Close:
                moveTo(lastMoveTo.x, lastMoveTo.y, lastMoveTo.z);
                lineTo(lastPoint.x, lastPoint.y, lastPoint.z);
                break;
        }
    }

    /**
     * Returns the bounds.
     */
    @Override
    protected Bounds3D createBounds3D()
    {
        // Create and init bounds
        Bounds3D bounds = new Bounds3D();
        bounds.setMinXYZ(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        bounds.setMaxXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (int i = 0, iMax = getPointCount(); i < iMax; i++) {
            Point3D pt = getPoint(i);
            bounds.addXYZ(pt.x, pt.y, pt.z);
        }

        // Return
        return bounds;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public Path3D clone()
    {
        // Normal clone
        Path3D clone  = (Path3D) super.clone();

        // Copy elements
        clone._segs = new ArrayList<>(_segs);
        clone._points = new ArrayList<>(_points.size());
        for (Point3D pnt : _points)
            clone._points.add(pnt.clone());

        // Return clone
        return clone;
    }

    /**
     * Standard toStringProps implementation.
     */
    @Override
    public String toStringProps()
    {
        String superProps = super.toStringProps();
        return superProps + ", PointCount=" + getPointCount();
    }
}