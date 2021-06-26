/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Path;
import snap.geom.PathIter;
import snap.geom.Polygon;
import snap.geom.Seg;

import java.util.*;

/**
 * This class represents a path in 3D space.
 */
public class Path3D extends Shape3D implements Cloneable {
    
    // The list of elements in this path
    private List<Seg>  _elements = new ArrayList<>();
    
    // The list of point3Ds in this path
    private List<Point3D>  _points = new ArrayList<>();
    
    // A list of Path3Ds to be drawn in front of this Path3D
    private List<Path3D>  _layers;

    // The path center point
    private Point3D  _center;
    
    // The path normal vector
    private Vector3D  _normal;
    
    // The path bounding box
    private Point3D[]  _bbox;
    
    // The cached path (2d)
    private Path  _path;

    // Cached array of this Path3D to efficiently satisfy getPath3Ds() method
    private Path3D[]  _path3Ds = { this };

    // The Triangle path
    private Path3D[]  _trianglePaths;
    
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
    public Path3D(Path aPath, double aDepth)
    {
        addPath(aPath, aDepth);
    }

    /**
     * Returns the number of elements in the path3d.
     */
    public int getElementCount()  { return _elements.size(); }

    /**
     * Returns the element type at the given index.
     */
    public Seg getElement(int anIndex)  { return _elements.get(anIndex); }

    /**
     * Returns the number of points in the path3d.
     */
    public int getPointCount()  { return _points.size(); }

    /**
     * Returns the point3d at the given index.
     */
    public Point3D getPoint(int anIndex)  { return _points.get(anIndex); }

    /**
     * Returns the element at the given index.
     */
    public Seg getElement(int anIndex, Point3D[] pts)
    {
        // Get element type (if no points, just return type)
        Seg type = getElement(anIndex);
        if (pts == null)
            return type;

        // If given index isn't equal to "next index" optimizer, reset next index ivar
        if (anIndex != _nextElementIndex) {
            _nextPointIndex = 0;
            for (int i=0; i<anIndex; i++) {
                Seg t = _elements.get(i);
                _nextPointIndex += t==MOVE_TO || t==LINE_TO ? 1 : t==QUAD_TO ? 2 : t==CURVE_TO ? 3 : 0;
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
        _elements.add(MOVE_TO);
        _points.add(new Point3D(x, y, z));
        clearCache();
    }

    /**
     * Adds a line to the path3d with the given 3D coords.
     */
    public void lineTo(double x, double y, double z)
    {
        _elements.add(LINE_TO);
        _points.add(new Point3D(x, y, z));
        clearCache();
    }

    /**
     * Adds a quad to to the path3d with the given 3D control point and coords.
     */
    public void quadTo(double cpx, double cpy, double cpz, double x, double y, double z)
    {
        _elements.add(QUAD_TO);
        _points.add(new Point3D(cpx, cpy, cpz));
        _points.add(new Point3D(x, y, z));
        clearCache();
    }

    /**
     * Adds a curve-to to the path3d with the given 3d coords.
     */
    public void curveTo(double cp1x,double cp1y,double cp1z,double cp2x,double cp2y,double cp2z,double x,double y,double z)
    {
        _elements.add(CURVE_TO);
        _points.add(new Point3D(cp1x, cp1y, cp1z));
        _points.add(new Point3D(cp2x, cp2y, cp2z));
        _points.add(new Point3D(x, y, z));
        clearCache();
    }

    /**
     * Adds a close element to the path3d.
     */
    public void close()
    {
        _elements.add(CLOSE);
    }

    /**
     * Adds a 2D path to the path3D at the given depth.
     */
    public void addPath(Path aPath, double aDepth)
    {
        // Iterate over elements in given path
        PathIter piter = aPath.getPathIter(null);
        double[] pts = new double[6];
        for (int i=0; piter.hasNext(); i++) {
            switch (piter.getNext(pts)) {
                case MoveTo:
                    if (i+1 < aPath.getSegCount() && aPath.getSeg(i+1) != Seg.MoveTo)
                        moveTo(pts[0], pts[1], aDepth);
                    break;
                case LineTo:
                    lineTo(pts[0], pts[1], aDepth);
                    break;
                case QuadTo:
                    quadTo(pts[0], pts[1], aDepth, pts[2], pts[3], aDepth);
                    break;
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
        // If already set, just return
        if (_center != null) return _center;

        // If center point hasn't been cached, calculate and cache it
        Point3D[] bbox = getBBox();
        double cx = bbox[0].x + (bbox[1].x - bbox[0].x) / 2;
        double cy = bbox[0].y + (bbox[1].y - bbox[0].y) / 2;
        double cz = bbox[0].z + (bbox[1].z - bbox[0].z) / 2;
        return _center = new Point3D(cx, cy, cz);
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
        for (int i=0, pc=getPointCount(); i<pc; i++) {
            Point3D cur = getPoint(i);
            Point3D next = getPoint((i+1) % pc);
            normal.x += (cur.y - next.y) * (cur.z + next.z);
            normal.y += (cur.z - next.z) * (cur.x + next.x);
            normal.z += (cur.x - next.x) * (cur.y + next.y);
        }

        // Normalize the result and swap sign so it matches right hand rule
        normal.normalize();
        normal.negate();
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
    }

    /**
     * Reverse method worker method.
     */
    private void reverse(int element, Point3D lastPoint, Point3D lastMoveTo)
    {
        // Simply return if element is beyond bounds
        if (element == getElementCount()) {
            _elements.clear(); _points.clear(); clearCache();
            return;
        }

        // Get info for this element
        Point3D[] pts = new Point3D[3];
        Point3D lp = null;
        Point3D lmt = lastMoveTo;
        Seg type = getElement(element, pts);
        switch (type) {
            case MoveTo: lmt = pts[0];
            case LineTo: lp = pts[0]; break;
            case QuadTo: lp = pts[1]; break;
            case CubicTo: lp = pts[2]; break;
            case Close: lp = lastMoveTo;
        }

        // Recursively add following elements before this one
        Seg nextType = element+1 < getElementCount() ? getElement(element+1,null) : null;
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
        for (int i=0, iMax=getPointCount(); i<iMax; i++)
            getPoint(i).transform(xform);
        clearCache();
    }

    /**
     * Transforms the path so the normal is aligned with the given vector.
     */
    public void align(Vector3D aVector)
    {
        // Get transform to vector (just return if IDENTITY)
        Transform3D xfm = getTransformToAlignToVector(aVector);
        if (xfm == Transform3D.IDENTITY)
            return;

        // Transform this Path3D
        transform(xfm);
    }

    /**
     * Returns the transform to make this Path3D align with given vector.
     */
    public Transform3D getTransformToAlignToVector(Vector3D aVector)
    {
        // Get angle between Path3D.Normal and given vector
        Vector3D norm = getNormal();
        double angle = norm.getAngleBetween(aVector);
        if (angle == 0 || angle == 180) // THIS IS WRONG - NO 180!!!
            return Transform3D.IDENTITY;

        // Get axis about which to rotate the path (its the cross product of Path3D.Normal and given vectors)
        Vector3D rotAxis = norm.getCrossProduct(aVector);

        // Create the rotation matrix
        Transform3D rotMatrix = new Transform3D();
        rotMatrix.rotate(rotAxis, angle);

        // The point of rotation is located at the shape's center
        Point3D rotOrigin = getCenter();

        Transform3D xform = new Transform3D();
        xform.translate(-rotOrigin.x, -rotOrigin.y, -rotOrigin.z);
        xform.multiply(rotMatrix);
        xform.translate(rotOrigin.x, rotOrigin.y, rotOrigin.z);
        return xform;
    }

    /**
     * Returns a path for the path3d.
     */
    public Path getPath()
    {
        // Create new path
        Path path = new Path();

        // Iterate over this path3d and add segments as 2D
        Point3D[] pts = new Point3D[3];
        for (int i=0, iMax=getElementCount(); i<iMax; i++) {
            Seg type = getElement(i, pts);
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
     * Returns the triangle paths.
     */
    public Path3D[] getTrianglePaths()
    {
        // If already set, just return
        if (_trianglePaths != null) return _trianglePaths;

        // Create, set, return
        Path3D[] triPaths = createTrianglePaths();
        return _trianglePaths = triPaths;
    }

    /**
     * Creates the triangle paths.
     */
    protected Path3D[] createTrianglePaths()
    {
        // If no normal, just return empty
        if (Double.isNaN(getNormal().x))
            return new Path3D[0];

        // Get copy facing Z
        Vector3D zFacing = new Vector3D(0, 0, 1);
        Transform3D xfmToZ = getTransformToAlignToVector(zFacing);
        Transform3D xfmFromZ = xfmToZ.invert();
        Path3D copy = copyForTransform(xfmToZ);

        // Get Path2D
        Path path2D = copy.getPath();
        Point3D[] bbox = copy.getBBox();
        Polygon[] triangles = Polygon.getConvexPolys(path2D, 3);
        double zVal = bbox[0].z;

        // Get Path3Ds
        Path3D[] triPaths = new Path3D[triangles.length];
        for (int i=0, iMax=triangles.length; i<iMax; i++) {
            Polygon triangle = triangles[i];
            Path3D triPath = new Path3D();
            triPath.moveTo(triangle.getX(0), triangle.getY(0), zVal);
            triPath.lineTo(triangle.getX(1), triangle.getY(1), zVal);
            triPath.lineTo(triangle.getX(2), triangle.getY(2), zVal);
            triPath.close();
            triPath.transform(xfmFromZ);
            triPaths[i] = triPath;
        }

        // Return Triangle paths
        return triPaths;
    }

    /**
     * Returns the bounding box for the path as {min,max}.
     */
    public Point3D[] getBBox()
    {
        // If already set, just return
        if (_bbox != null) return _bbox;

        // Set
        Point3D[] bbox = new Point3D[2];
        bbox[0] = new Point3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        bbox[1] = new Point3D(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        for (int i=0, iMax=getPointCount(); i<iMax; i++) { Point3D pt = getPoint(i);
            bbox[0].x = Math.min(bbox[0].x, pt.x);
            bbox[0].y = Math.min(bbox[0].y, pt.y);
            bbox[0].z = Math.min(bbox[0].z, pt.z);
            bbox[1].x = Math.max(bbox[1].x, pt.x);
            bbox[1].y = Math.max(bbox[1].y, pt.y);
            bbox[1].z = Math.max(bbox[1].z, pt.z);
        }
        return _bbox = bbox;
    }

    /**
     * Returns the max X for the path.
     */
    public double getXMin()  { return getBBox()[0].x; }

    /**
     * Returns the max X for the path.
     */
    public double getXMax()  { return getBBox()[1].x; }

    /**
     * Returns the max Y for the path.
     */
    public double getYMin()  { return getBBox()[0].y; }

    /**
     * Returns the max Y for the path.
     */
    public double getYMax()  { return getBBox()[1].y; }

    /**
     * Returns the max Z for the path.
     */
    public double getZMin()  { return getBBox()[0].z; }

    /**
     * Returns the max Z for the path.
     */
    public double getZMax()  { return getBBox()[1].z; }

    /**
     * Returns the mid Z for the path.
     */
    public double getZMid()  { return getZMin() + (getZMax() - getZMin())/2; }

    /**
     * Returns layers to be drawn in front of this path.
     */
    public List <Path3D> getLayers()  { return _layers!=null ? _layers : Collections.EMPTY_LIST; }

    /**
     * Adds a path to be drawn immediately in front of this path.
     */
    public void addLayer(Path3D aPath)
    {
        if (_layers == null) _layers = new ArrayList<>();
        _layers.add(aPath);
    }

    /**
     * Clears cached values when path changes.
     */
    protected void clearCache()
    {
        _center = null;
        _normal = null;
        _bbox = null;
        _path = null;
    }

    // Constants for comparison/ordering of Path3Ds
    public static final int ORDER_BACK_TO_FRONT = -1;
    public static final int ORDER_FRONT_TO_BACK = 1;
    public static final int ORDER_SAME = 0;
    public static final int ORDER_INEDETERMINATE = 2;

    /**
     * Compares ZMin for this path and given path.
     */
    public int compareZMin(Path3D path2)
    {
        double z0 = getZMin();
        double z1 = path2.getZMin();
        return z0 < z1 ? ORDER_BACK_TO_FRONT : z1 < z0 ? ORDER_FRONT_TO_BACK : 0;
    }

    /**
     * Returns whether this path is in front (FRONT_TO_BACK) or aPath in front (BACK_TO_FRONT).
     * Returns ORDER_SAME if the two paths are coplanar, or INDETERMINATE if they intersect.
     */
    public int comparePlane(Path3D aPath)
    {
        double d1 = 0;
        for (int i=0, iMax=aPath.getPointCount(); i<iMax; i++) {
            Point3D pnt = aPath.getPoint(i);
            double d2 = getDistance(pnt);
            if (d1 == 0)
                d1 = d2;
            if (d2 != 0 && d1*d2 < 0)
                return ORDER_INEDETERMINATE;
        }

        // If all points are above aPath's plane, return BACK_TO_FRONT (receiver in front), otherwise ORDER_DESCEND
        return d1 > 0 ? ORDER_BACK_TO_FRONT : d1 < 0 ? ORDER_FRONT_TO_BACK : ORDER_SAME;
    }

    /**
     * Returns the distance from a point to the plane of this polygon.
     */
    public double getDistance(Point3D aPoint)
    {
        Vector3D normal = getNormal();
        Point3D p0 = getPoint(0);
        double d = -normal.x * p0.x - normal.y * p0.y - normal.z * p0.z;
        double dist = normal.x * aPoint.x + normal.y * aPoint.y + normal.z * aPoint.z + d;
        return Math.abs(dist) < .01 ? 0 : dist;
    }

    /**
     * Copies path for given transform.
     */
    public Path3D copyForTransform(Transform3D aTrans)
    {
        Path3D copy = clone();
        copy.transform(aTrans);
        if (_layers != null)
            for (Path3D layer : copy._layers)
                layer.transform(aTrans);
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

        // Reset _path3ds
        clone._path3Ds = new Path3D[] { clone };

        // Copy elements
        clone._elements = new ArrayList<>(_elements);
        clone._points = new ArrayList<>(_points.size());
        for (Point3D pnt : _points)
            clone._points.add(pnt.clone());
        if (_layers != null) {
            clone._layers = new ArrayList<>(_layers.size());
            for (Path3D path3D : _layers)
                clone._layers.add(path3D.clone());
        }

        // Return clone
        return clone;
    }

    /**
     * Returns the array of Path3D that can render this shape.
     */
    public Path3D[] getPath3Ds()  { return _path3Ds; }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        Point3D[] bbox = getBBox();
        return "Path3D { PointCount=" + getPointCount() + ", BBox[0] = " + bbox[0] + ", BBox[1]=" + bbox[1] + " }";
    }

    /**
     * Resorts a Path3D list from back to front using Depth Sort Algorithm.
     */
    public static void sortPaths(List<Path3D> thePaths)
    {
        // Get list of paths and sort from front to back with simple Z min sort
        Collections.sort(thePaths, (p0,p1) -> p0.compareZMin(p1));

        // Sort again front to back with exhaustive sort satisfying Depth Sort Algorithm
        for (int i=thePaths.size()-1; i>0; i--) {

            // Get loop path
            Path3D path1 = thePaths.get(i);
            int i2 = i;

            // Iterate over remaining paths
            for (int j=0; j<i; j++) {

                // Get loop path (if same path, just skip)
                Path3D path2 = thePaths.get(j);
                if (path2 == path1)
                    continue;

                // If no X/Y/Z overlap, just continue
                if (path1.getZMin() >= path2.getZMax())
                    continue;
                if (path1.getXMax() <= path2.getXMin() || path1.getXMin() >= path2.getXMax())
                    continue;
                if (path1.getYMax() <= path2.getYMin() || path1.getYMin() >= path2.getYMax())
                    continue;

                // Test path planes - if on same plane or in correct order, they don't overlap
                int comp1 = path1.comparePlane(path2);
                if (comp1 == ORDER_SAME || comp1 == ORDER_BACK_TO_FRONT)
                    continue;
                int comp2 = path2.comparePlane(path1);
                if (comp2 == ORDER_FRONT_TO_BACK)
                    continue;

                // If 2d paths don't intersect, just continue
                if (!path1.getPath().intersects(path2.getPath(),0))
                    continue;

                // If all five tests fail, try next path up from path1
                if (i2 == 0) {  // Not sure why this can happen
                    System.err.println("Path3D.sort: Sort fail.");
                    i = 0;
                }
                else {
                    path1 = thePaths.get(--i2);
                    j = -1;
                }
            }

            // Move poly
            if (i2 != i) {
                thePaths.remove(i2);
                thePaths.add(i, path1);
            }
        }

        // Reverse child list so it is back to front (so front most shape will be drawn last)
        Collections.reverse(thePaths);
    }

    /**
     * Resorts a Path3D list from back to front using Depth Sort Algorithm.
     */
    public static void sortPaths2(List<Path3D> thePaths)
    {
        // Get list of paths and sort from front to back with simple Z min sort
        Collections.sort(thePaths, (p0,p1) -> p0.compareZMin(p1));

        // Sort again front to back with exhaustive sort satisfying Depth Sort Algorithm
        for (int i=thePaths.size()-1; i>0; i--) {

            // Get loop path
            Path3D path2 = thePaths.get(i);
            int i2 = i;

            // Iterate over remaining paths
            for (int j=0; j<i; j++) {

                // Get loop path (if same path, just skip)
                Path3D path1 = thePaths.get(j);
                if (path1 == path2)
                    continue;

                // If no X/Y/Z overlap, just continue
                if (path2.getZMin() >= path1.getZMax())
                    continue;
                if (path2.getXMax() <= path1.getXMin() || path2.getXMin() >= path1.getXMax())
                    continue;
                if (path2.getYMax() <= path1.getYMin() || path2.getYMin() >= path1.getYMax())
                    continue;

                // Test path planes - if on same plane or in correct order, they don't overlap
                int comp1 = path2.comparePlane(path1);
                if (comp1 == ORDER_SAME || comp1 == ORDER_FRONT_TO_BACK)
                    continue;
                int comp2 = path1.comparePlane(path2);
                if (comp2 == ORDER_BACK_TO_FRONT)
                    continue;

                // If 2d paths don't intersect, just continue
                if (!path2.getPath().intersects(path1.getPath(),0))
                    continue;

                // If all five tests fail, try next path up from path1
                if (i2 == 0) {  // Not sure why this can happen
                    System.err.println("Path3D.sort: Sort fail.");
                    i = 0;
                }
                else {
                    path2 = thePaths.get(--i2);
                    j = -1;
                }
            }

            // Move poly
            if (i2 != i) {
                thePaths.remove(i2);
                thePaths.add(i, path2);
            }
        }
    }
}