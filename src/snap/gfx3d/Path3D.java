/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Path;
import snap.geom.PathIter;
import snap.geom.Seg;

import java.util.*;

/**
 * This class represents a path in 3D space.
 */
public class Path3D extends Shape3D implements Cloneable {
    
    // The list of elements in this path
    private List <Seg>  _elements = new ArrayList();
    
    // The list of point3Ds in this path
    private List <Point3D>  _points = new ArrayList();
    
    // A list of Path3Ds to be drawn in front of this Path3D
    private List <Path3D>  _layers;

    // The path center point
    private Point3D  _center;
    
    // The path normal vector
    private Vector3D  _normal;
    
    // The path bounding box
    private Point3D  _bbox[];
    
    // The cached path (2d)
    private Path  _path;
    
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
    public Seg getElement(int anIndex, Point3D pts[])
    {
        // Get element type (if no points, just return type)
        Seg type = getElement(anIndex); if (pts==null) return type;

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
            case LineTo: pts[0] = getPoint(_nextPointIndex++); break;
            case QuadTo: pts[0] = getPoint(_nextPointIndex++); pts[1] = getPoint(_nextPointIndex++); break;
            case CubicTo: pts[0] = getPoint(_nextPointIndex++); pts[1] = getPoint(_nextPointIndex++);
                pts[2] = getPoint(_nextPointIndex++); break;
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
    public void close()  { _elements.add(CLOSE); }

    /**
     * Adds a 2D path to the path3D at the given depth.
     */
    public void addPath(Path aPath, double aDepth)
    {
        // Iterate over elements in given path
        PathIter piter = aPath.getPathIter(null); double pts[] = new double[6];
        for (int i=0; piter.hasNext(); i++) {
            switch (piter.getNext(pts)) {
                case MoveTo: if (i+1<aPath.getSegCount() && aPath.getSeg(i+1)!=Seg.MoveTo)
                    moveTo(pts[0], pts[1], aDepth); break;
                case LineTo: lineTo(pts[0], pts[1], aDepth); break;
                case QuadTo: quadTo(pts[0], pts[1], aDepth, pts[2], pts[3], aDepth); break;
                case CubicTo: curveTo(pts[0], pts[1], aDepth, pts[2], pts[3], aDepth, pts[4], pts[5], aDepth); break;
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
        if (_center!=null) return _center;

        // If center point hasn't been cached, calculate and cache it
        Point3D bbox[] = getBBox();
        double cx = bbox[0].x + (bbox[1].x-bbox[0].x)/2;
        double cy = bbox[0].y + (bbox[1].y-bbox[0].y)/2;
        double cz = bbox[0].z + (bbox[1].z-bbox[0].z)/2;
        return _center = new Point3D(cx, cy, cz);
    }

    /**
     * Returns the normal of the path3d. Right hand rule for clockwise/counter-clockwise defined polygons.
     */
    public Vector3D getNormal()
    {
        // If already set, just return
        if (_normal!=null) return _normal;

        // Calculate least-square-fit normal. Works for either convex or concave polygons.
        // Reference is Newell's Method for Computing the Plane Equation of a Polygon.
        //   Graphics Gems III, David Kirk (Ed.), AP Professional, 1992.
        Vector3D normal = new Vector3D(0, 0, 0);
        for (int i=0, pc=getPointCount(); i<pc; i++) { Point3D cur = getPoint(i), next = getPoint((i+1)%pc);
            normal.x += (cur.y - next.y) * (cur.z + next.z);
            normal.y += (cur.z - next.z) * (cur.x + next.x);
            normal.z += (cur.x - next.x) * (cur.y + next.y);
        }

        // Normalize the result and swap sign so it matches right hand rule
        normal.normalize(); normal.negate();
        return _normal = normal;
    }

    /**
     * Reverses the path3d.
     */
    public void reverse()  { reverse(0, null, null); }

    /**
     * Reverse method worker method.
     */
    private void reverse(int element, Point3D lastPoint, Point3D lastMoveTo)
    {
        // Simply return if element is beyond bounds
        if (element==getElementCount()) {
            _elements.clear(); _points.clear(); clearCache(); return; }

        // Get info for this element
        Point3D pts[] = new Point3D[3], lp = null, lmt = lastMoveTo;
        Seg type = getElement(element, pts);
        switch (type) {
            case MoveTo: lmt = pts[0];
            case LineTo: lp = pts[0]; break;
            case QuadTo: lp = pts[1]; break;
            case CubicTo: lp = pts[2]; break;
            case Close: lp = lastMoveTo;
        }

        // Recursively add following elements before this one
        Seg nextType = element+1<getElementCount() ? getElement(element+1,null) : null;
        reverse(element+1, lp, lmt);

        // Add reverse element to path for current element
        switch (type) {
            case MoveTo:
                if (nextType!=MOVE_TO)
                    close();
                break;
            case LineTo:
                if (!lastPoint.equals(lastMoveTo))
                    lineTo(lastPoint.x, lastPoint.y, lastPoint.z);
                break;
            case QuadTo: quadTo(pts[0].x, pts[0].y, pts[0].z, lastPoint.x, lastPoint.y, lastPoint.z); break;
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
        // The dot product of vector and path's normal gives the angle in the rotation plane by which to rotate the path
        Vector3D norm = getNormal();

        // Get angle between normal and given vector
        double angle = norm.getAngleBetween(aVector);

        // If angle, transform path
        if (angle != 0) {

            // The axis about which to rotate the path is given by the cross product of the two vectors
            Vector3D rotAxis = norm.getCrossProduct(aVector);
            Transform3D xform = new Transform3D();
            Transform3D rotMatrix = new Transform3D();

            // create the rotation matrix
            rotMatrix.rotate(rotAxis, angle);

            // The point of rotation is located at the shape's center
            Point3D rotOrigin = getCenter();

            xform.translate(-rotOrigin.x, -rotOrigin.y, -rotOrigin.z);
            xform.multiply(rotMatrix);
            xform.translate(rotOrigin.x, rotOrigin.y, rotOrigin.z);

            transform(xform);
        }
    }

    /**
     * Returns a path for the path3d.
     */
    public Path getPath()
    {
        // Create new path
        Path path = new Path();

        // Iterate over this path3d
        Point3D pts[] = new Point3D[3];
        for (int i=0, iMax=getElementCount(); i<iMax; i++) { Seg type = getElement(i, pts);

            // Do 2d operation
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
     * Returns the bounding box for the path as {min,max}.
     */
    public Point3D[] getBBox()
    {
        // If already set, just return
        if (_bbox!=null) return _bbox;

        // Set
        Point3D bbox[] = new Point3D[2];
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
        if (_layers==null) _layers = new ArrayList();
        _layers.add(aPath);
    }

    /**
     * Clears cached values when path changes.
     */
    protected void clearCache()  { _center = null; _normal = null; _bbox = null; _path = null; }

    // Constants for ordering
    public static final int ORDER_BACK_TO_FRONT = -1;
    public static final int ORDER_FRONT_TO_BACK = 1;
    public static final int ORDER_SAME = 0, ORDER_INEDETERMINATE = 2;

    /**
     * Compares ZMin for this path and given path.
     */
    public int compareZMin(Path3D path2)
    {
        double z0 = getZMin(), z1 = path2.getZMin();
        return z0<z1 ? ORDER_BACK_TO_FRONT : z1<z0 ? ORDER_FRONT_TO_BACK : 0;
    }

    /**
     * Returns whether this path is in front (FRONT_TO_BACK) or aPath in front (BACK_TO_FRONT).
     * Returns ORDER_SAME if the two paths are coplanar, or INDETERMINATE if they intersect.
     */
    public int comparePlane(Path3D aPath)
    {
        double d1 = 0;
        for (int i=0, iMax=aPath.getPointCount(); i<iMax; i++) { Point3D pnt = aPath.getPoint(i);
            double d2 = getDistance(pnt);
            if (d1==0) d1 = d2;
            if (d2!=0 && d1*d2<0) return 2; // Indeterminate
        }

        // If all points are above aPath's plane, return BACK_TO_FRONT (receiver in front), otherwise ORDER_DESCEND
        return d1>0 ? ORDER_BACK_TO_FRONT : d1<0 ? ORDER_FRONT_TO_BACK : ORDER_SAME;
    }

    /**
     * Returns the distance from a point to the plane of this polygon.
     */
    public double getDistance(Point3D aPoint)
    {
        Vector3D normal = getNormal();
        Point3D p0 = getPoint(0);
        double d = -normal.x*p0.x - normal.y*p0.y - normal.z*p0.z;
        double dist = normal.x*aPoint.x + normal.y*aPoint.y + normal.z*aPoint.z + d;
        return Math.abs(dist)<.01 ? 0 : dist;
    }

    /**
     * Copies path for given transform.
     */
    public Path3D copyFor(Transform3D aTrans)
    {
        Path3D copy = clone(); copy.transform(aTrans);
        if (_layers!=null)
            for (Path3D layer : copy._layers)
                layer.transform(aTrans);
        return copy;
    }

    /**
     * Standard clone implementation.
     */
    public Path3D clone()
    {
        Path3D clone = null; try { clone = (Path3D)super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
        clone._elements = new ArrayList(_elements);
        clone._points = new ArrayList(_points.size());
        for (Point3D p : _points)
            clone._points.add(p.clone());
        if (_layers!=null) {
            clone._layers = new ArrayList(_layers.size());
            for (Path3D p : _layers)
                clone._layers.add(p.clone());
        }
        return clone;
    }

    /**
     * Returns the array of Path3D that can render this shape.
     */
    public Path3D[] getPath3Ds()  { return _path3ds; }  Path3D _path3ds[] = { this };

    /**
     * Standard toString implementation.
     */
    public String toString()  { return "Path3D: " + getBBox()[0] + ", " + getBBox()[1]; }

    /**
     * Resorts a Path3D list from back to front using Depth Sort Algorithm.
     */
    public static void sort(List <Path3D> paths)
    {
        // Get list of paths and sort from front to back with simple Z min sort
        Collections.sort(paths, (p0,p1) -> p0.compareZMin(p1));

        // Sort again front to back with exhaustive sort satisfying Depth Sort Algorithm
        for (int i=paths.size()-1; i>0; i--) { Path3D path1 = paths.get(i); int i2 = i;

            // Iterate over remaining paths
            for (int j=0; j<i; j++) { Path3D path2 = paths.get(j); if (path2==path1) continue;

                // If no X/Y/Z overlap, just continue
                if (path1.getZMin()>=path2.getZMax()) continue;
                if (path1.getXMax()<=path2.getXMin() || path1.getXMin()>=path2.getXMax()) continue;
                if (path1.getYMax()<=path2.getYMin() || path1.getYMin()>=path2.getYMax()) continue;

                // Test path planes - if on same plane or in correct order, they don't overlap
                int comp1 = path1.comparePlane(path2); if (comp1==ORDER_SAME || comp1==ORDER_BACK_TO_FRONT) continue;
                int comp2 = path2.comparePlane(path1); if (comp2==ORDER_FRONT_TO_BACK) continue;

                // If 2d paths don't intersect, just continue
                if (!path1.getPath().intersects(path2.getPath(),0)) continue;

                // If all five tests fail, try next path up from path1
                if (i2==0) { System.err.println("Path3D.sort: Sort fail."); i = 0; } // Not sure why this can happen
                else { path1 = paths.get(--i2); j = -1; }
            }

            // Move poly
            if (i2!=i) {
                paths.remove(i2); paths.add(i, path1); }
        }

        // Reverse child list so it is back to front (so front most shape will be drawn last)
        Collections.reverse(paths);
    }
}