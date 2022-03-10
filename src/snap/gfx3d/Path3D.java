/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.*;
import snap.gfx.Color;
import snap.util.MathUtils;
import java.util.*;

/**
 * This class represents a path in 3D space.
 */
public class Path3D extends Shape3D implements Cloneable {
    
    // The list of elements in this path
    private List<Seg>  _segs = new ArrayList<>();
    
    // The list of point3Ds in this path
    private List<Point3D>  _points = new ArrayList<>();
    
    // The list of colors in this path
    private List<Color>  _colors = new ArrayList<>();

    // A Painter3D to paint the surface of path
    private Painter3D  _painter;

    // The path normal vector
    private Vector3D  _normal;
    
    // The VertexArray holding triangles of Path3D
    private VertexArray  _vertexArray;
    
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
    public int getPointCount()  { return _points.size(); }

    /**
     * Returns the Point3D at given index.
     */
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
     * Adds a color.
     */
    public void addColor(Color aColor)
    {
        _colors.add(aColor);
    }

    /**
     * Adds a Shape path to this path3D at given depth.
     */
    public void addShapePath(Shape aPath, double aDepth)
    {
        // Iterate over elements in given path
        PathIter piter = aPath.getPathIter(null);
        double[] pts = new double[6];

        // Iterate over elements in given path
        while (piter.hasNext()) {

            // Get/handle Seg
            Seg seg = piter.getNext(pts);
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
     * Returns the center point of the path.
     */
    public Point3D getCenter()
    {
        Bounds3D bounds = getBounds3D();
        return bounds.getCenter();
    }

    /**
     * Returns the normal of the path3d. Right hand rule for clockwise/counter-clockwise defined polygons.
     */
    public Vector3D getNormal()
    {
        // If already set, just return
        if (_normal != null) return _normal;

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
        normal.normalize(); //if (Renderer.FRONT_FACE_IS_CW) normal.negate();
        return _normal = normal;
    }

    /**
     * Returns whether path is surface.
     */
    public boolean isSurface()
    {
        Vector3D normal = getNormal();
        return !Double.isNaN(normal.x);
    }

    /**
     * Reverses the path3d.
     */
    public void reverse()
    {
        reverse(0, null, null);

        // Reverse colors
        if (_colors.size() == 3) {
            Color color = _colors.remove(1);
            _colors.add(color);
        }
        else if (_colors.size() > 3)
            System.err.println("Path3D.reverse: Colors not supported");
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
     * Transforms the path by the given transform3d.
     */
    public void transform(Transform3D xform)
    {
        for (int i = 0, iMax = getPointCount(); i < iMax; i++) {
            Point3D point = getPoint(i);
            xform.transformPoint(point);
        }
        clearCachedValues();
    }

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
     * Returns the transform to make this Path3D align with given vector.
     */
    public Matrix3D getTransformToAlignToVector(double aX, double aY, double aZ)
    {
        // Get angle between Path3D.Normal and given vector
        Vector3D vector = new Vector3D(aX, aY, aZ);
        Vector3D norm = getNormal();
        double angle = norm.getAngleBetween(vector);
        if (angle == 0 || angle == 180) // THIS IS WRONG - NO 180!!!
            return Matrix3D.IDENTITY;

        // The point of rotation is located at the shape's center
        Point3D rotOrigin = getCenter();

        // Get axis about which to rotate the path (its the cross product of Path3D.Normal and given vector)
        Vector3D rotAxis = norm.getCrossProduct(vector);

        // Get transform
        Matrix3D xform = new Matrix3D();
        xform.translate(rotOrigin.x, rotOrigin.y, rotOrigin.z);
        xform.rotateAboutAxis(angle, rotAxis.x, rotAxis.y, rotAxis.z);
        xform.translate(-rotOrigin.x, -rotOrigin.y, -rotOrigin.z);
        return xform;
    }

    /**
     * Returns a path for the path3d.
     */
    public Path getPath()
    {
        // Create new path
        Path path = new Path();
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
     * Returns a VertexArray of path triangles.
     */
    public VertexArray getVertexArray()
    {
        // If already set, just return
        if (_vertexArray != null) return _vertexArray;

        // Create, set, return
        VertexArray triVA = createVertexArray();
        return _vertexArray = triVA;
    }

    /**
     * Creates a VertexArray of path triangles.
     */
    protected VertexArray createVertexArray()
    {
        // Create/configure VertexArray
        VertexArray vertexArray = new VertexArray();
        vertexArray.setColor(getColor());
        vertexArray.setDoubleSided(isDoubleSided());

        // If no normal, just return empty
        Vector3D pathNormal = getNormal();
        if (Double.isNaN(pathNormal.x))
            return vertexArray;

        // Get transform matrix to transform this path to/from facing Z
        Matrix3D xfmToZ = getTransformToAlignToVector(0, 0, 1);
        Matrix3D xfmFromZ = xfmToZ.clone().invert();

        // Get copy of path facing Z
        Path3D pathFacingZ = copyForMatrix(xfmToZ);
        double zVal = pathFacingZ.getMinZ();

        // Get Path2D, break into triangles
        Path path2D = pathFacingZ.getPath();
        Polygon[] triangles = Polygon.getConvexPolys(path2D, 3);

        // Create loop variables
        Point3D p0 = new Point3D(0, 0, 0);
        Point3D p1 = new Point3D(0, 0, 0);
        Point3D p2 = new Point3D(0, 0, 0);
        Point3D[] points = { p0, p1, p2 };
        Vector3D pointsNormal = new Vector3D(0, 0, 0);

        // Get Path3Ds
        for (Polygon triangle : triangles) {

            // Get triangle points
            p0.x = triangle.getX(0);
            p0.y = triangle.getY(0);
            p1.x = triangle.getX(1);
            p1.y = triangle.getY(1);
            p2.x = triangle.getX(2);
            p2.y = triangle.getY(2);
            p0.z = p1.z = p2.z = zVal;

            // Transform points back and add to VertexArray
            xfmFromZ.transformPoint(p0);
            xfmFromZ.transformPoint(p1);
            xfmFromZ.transformPoint(p2);

            // If points normal facing backwards, reverse points (swap p0 and p2)
            Vector3D.getNormalForPoints(pointsNormal, points);
            if (!pointsNormal.equals(pathNormal)) {
                double px = p0.x, py = p0.y, pz = p0.z;
                p0.x = p2.x; p0.y = p2.y; p0.z = p2.z;
                p2.x = px; p2.y = py; p2.z = pz;
            }

            // Add points to VertexArray
            vertexArray.addPoint(p0.x, p0.y, p0.z);
            vertexArray.addPoint(p1.x, p1.y, p1.z);
            vertexArray.addPoint(p2.x, p2.y, p2.z);
        }

        // Add colors
        for (Color color : _colors)
            vertexArray.addColor(color);

        // Handle Stroke: Create/add stroke VertexArray
        if (getStrokeColor() != null) {
            VertexArray strokeVA = getStrokeVertexArray();
            vertexArray.setLast(strokeVA);
        }

        // Handle Painter: Create/add painterVertexArray
        Painter3D painter3D = getPainter();
        if (painter3D != null) {
            VertexArray painterVA = getPainterVertexArray();
            vertexArray.setLast(painterVA);
        }

        // Return
        return vertexArray;
    }

    /**
     * Returns a VertexArray for path stroke.
     */
    protected VertexArray getStrokeVertexArray()
    {
        // Get vars
        VertexArray vertexArray = new VertexArray();
        Color strokeColor = getStrokeColor();
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
                    addLineStrokePoints(vertexArray, p0, p1);
                    lastPoint = p1;
                    break;
                }

                case Close: {
                    Point3D p0 = lastPoint, p1 = movePoint;
                    addLineStrokePoints(vertexArray, p0, p1);
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

    /**
     * Constructor.
     */
    private void addLineStrokePoints(VertexArray vertexArray, Point3D p0, Point3D p1)
    {
        // Get vector across line and perpendicular to line
        Vector3D pathNormal = getNormal();
        Vector3D acrossVector = new Vector3D(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z).normalize();
        Vector3D downVector = pathNormal.getCrossProduct(acrossVector).normalize();

        // Get offset so line moves 'above' path triangles
        Vector3D offsetNormal = pathNormal.clone();
        offsetNormal.scale(.5);

        // Upper left point
        Point3D p0a = p0.clone();
        p0a.x += downVector.x - acrossVector.x + offsetNormal.x;
        p0a.y += downVector.y - acrossVector.y + offsetNormal.y;
        p0a.z += downVector.z - acrossVector.z + offsetNormal.z;

        // Lower left point
        Point3D p0b = p0.clone();
        p0b.x += -downVector.x - acrossVector.x + offsetNormal.x;
        p0b.y += -downVector.y - acrossVector.y + offsetNormal.y;
        p0b.z += -downVector.z - acrossVector.z + offsetNormal.z;

        // Upper right point
        Point3D p1a = p1.clone();
        p1a.x += downVector.x + acrossVector.x + offsetNormal.x;
        p1a.y += downVector.y + acrossVector.y + offsetNormal.y;
        p1a.z += downVector.z + acrossVector.z + offsetNormal.z;

        // Lower right point
        Point3D p1b = p1.clone();
        p1b.x += -downVector.x + acrossVector.x + offsetNormal.x;
        p1b.y += -downVector.y + acrossVector.y + offsetNormal.y;
        p1b.z += -downVector.z + acrossVector.z + offsetNormal.z;

        // Get triangle A. If not aligned with normal, swap points
        Point3D[] triangleA = { p0a, p0b, p1b };
        Vector3D pointsNormal = Vector3D.getNormalForPoints(new Vector3D(0, 0, 0), triangleA);
        if (!pointsNormal.equals(pathNormal)) {
            triangleA[1] = p1b; triangleA[2] = p0b; }

        // Get triangle A. If not aligned with normal, swap points
        Point3D[] triangleB = { p1a, p0a, p1b };
        Vector3D.getNormalForPoints(pointsNormal, triangleB);
        if (!pointsNormal.equals(pathNormal)) {
            triangleB[1] = p1b; triangleB[2] = p0a; }

        // Add triangle points
        for (Point3D p3d : triangleA)
            vertexArray.addPoint(p3d.x, p3d.y, p3d.z);
        for (Point3D p3d : triangleB)
            vertexArray.addPoint(p3d.x, p3d.y, p3d.z);
    }

    // Error var
    private static boolean  _didRenderPath3DStrokedError;

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
     * Returns the painter to render texture for this path.
     */
    public Painter3D getPainter()  { return _painter; }

    /**
     * Sets the painter to render texture for this path.
     */
    public void setPainter(Painter3D aPntr)
    {
        _painter = aPntr;
    }

    /**
     * Returns the painter VertexArray for 'painted' triangles on shape surface.
     */
    public VertexArray getPainterVertexArray()
    {
        Matrix3D painterToLocal = getPainterToLocal();
        VertexArray vertexArray = _painter.getVertexArray();
        VertexArray vertexArrayLocal = vertexArray.copyForTransform(painterToLocal);
        return vertexArrayLocal;
    }

    /**
     * Returns the transform from Painter to this shape.
     */
    public Matrix3D getPainterToLocal()
    {
        // Create transform and translate to Path.Center
        Matrix3D painterToLocal = new Matrix3D();
        Point3D pathCenter = getCenter();
        painterToLocal.translate(pathCenter.x, pathCenter.y, pathCenter.z);

        // Rotate by angle between painter and path normals (around axis perpendicular to them)
        Vector3D pntrNormal = new Vector3D(0, 0, 1);
        Vector3D pathNormal = getNormal();
        double angle = pntrNormal.getAngleBetween(pathNormal);

        // If angle 180 deg, rotate about Y
        if (MathUtils.equals(angle, 180))
            painterToLocal.rotateY(180);

        // If angle non-zero, get perpendicular and rotate about that
        else if (!MathUtils.equalsZero(angle)) {
            Vector3D rotateAxis = pntrNormal.getCrossProduct(pathNormal);
            painterToLocal.rotateAboutAxis(angle, rotateAxis.x, rotateAxis.y, rotateAxis.z);
        }

        // Translate to Painter.Center
        double pntrW = _painter.getWidth();
        double pntrH = _painter.getHeight();
        Point3D pntrCenter = new Point3D(pntrW / 2, pntrH / 2, 0);
        painterToLocal.translate(-pntrCenter.x, -pntrCenter.y, -pntrCenter.z);

        // Return
        return painterToLocal;
    }

    /**
     * Clears cached values when path changes.
     */
    @Override
    protected void clearCachedValues()
    {
        super.clearCachedValues();
        _normal = null;
        _vertexArray = null;
    }

    /**
     * Returns the distance from a point to the plane of this path.
     */
    public double getDistanceFromPointToPathPlane(Point3D aPoint)
    {
        // Get plane normal
        Vector3D planeNormal = getNormal();

        // Get vector from plane point to given point
        Point3D planePoint = getPoint(0);
        double vx = aPoint.x - planePoint.x;
        double vy = aPoint.y - planePoint.y;
        double vz = aPoint.z - planePoint.z;

        // Distance is just the length of the projection of points vector onto normal vector (v dot n)
        double dist = vx * planeNormal.x + vy * planeNormal.y + vz * planeNormal.z;
        return Math.abs(dist) < .001 ? 0 : dist;
    }

    /**
     * Copies path for given transform.
     */
    public Path3D copyForTransform(Transform3D aTrans)
    {
        Path3D copy = clone();
        copy.transform(aTrans);
        return copy;
    }

    /**
     * Copies path for given transform matrix.
     */
    public Path3D copyForMatrix(Matrix3D aTrans)
    {
        Path3D copy = clone();
        copy.transform(aTrans);
        return copy;
    }

    /**
     * Standard clone implementation.
     */
    public Path3D clone()
    {
        // Normal clone
        Path3D clone;
        try { clone = (Path3D) super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }

        // Copy elements
        clone._segs = new ArrayList<>(_segs);
        clone._points = new ArrayList<>(_points.size());
        for (Point3D pnt : _points)
            clone._points.add(pnt.clone());

        // Clone colors
        clone._colors = new ArrayList<>(_colors);

        // Clone painter
        if (_painter != null)
            clone._painter = _painter.clone();

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