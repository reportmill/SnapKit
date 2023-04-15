/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.*;

import snap.util.*;

/**
 * A Shape subclass that represents a general path.
 */
public class Path extends Shape implements Cloneable, XMLArchiver.Archivable {

    // The array of segments
    private Seg[] _segs = new Seg[8];

    // The segment count
    private int _segCount;

    // The array of points
    private double[] _points = new double[16];

    // The number of points
    private int _pointCount;

    // The winding -how a path determines what to fill when segments intersect
    private int _wind = WIND_EVEN_ODD;

    /**
     * Constructor.
     */
    public Path()
    {
        super();
    }

    /**
     * Constructor with given path iterator.
     */
    public Path(PathIter aPI)
    {
        append(aPI);
    }

    /**
     * Constructor for given shape.
     */
    public Path(Shape aShape)
    {
        append(aShape.getPathIter(null));
    }

    /**
     * Returns the winding - how a path determines what to fill when segments intersect.
     */
    public int getWinding()  { return _wind; }

    /**
     * Sets the winding - how a path determines what to fill when segments intersect.
     */
    public void setWinding(int aValue)  { _wind = aValue; }

    /**
     * Returns the number of segments.
     */
    public int getSegCount()  { return _segCount; }

    /**
     * Returns the individual segement at index.
     */
    public Seg getSeg(int anIndex)
    {
        return _segs[anIndex];
    }

    /**
     * Adds a segment.
     */
    protected void addSeg(Seg aSeg)
    {
        // If at end of Segs array, extend by 2x
        if (_segCount + 1 > _segs.length)
            _segs = Arrays.copyOf(_segs, _segs.length * 2);

        // Add Seg at end, increment SegCount, notify shapeChanged
        _segs[_segCount++] = aSeg;
        shapeChanged();
    }

    /**
     * Returns the number of points.
     */
    public int getPointCount()  { return _pointCount; }

    /**
     * Returns individual point at given index.
     */
    public Point getPoint(int anIndex)
    {
        double px = _points[anIndex * 2];
        double py = _points[anIndex * 2 + 1];
        return new Point(px, py);
    }

    /**
     * Adds a point.
     */
    protected void addPoint(double x, double y)
    {
        // If at end of Points array, extend by 2x
        if (_pointCount * 2 + 1 > _points.length)
            _points = Arrays.copyOf(_points, _points.length * 2);

        // Add points at end and increment PointCount
        _points[_pointCount * 2] = x;
        _points[_pointCount * 2 + 1] = y;
        _pointCount++;
    }

    /**
     * Moveto.
     */
    public void moveTo(double x, double y)
    {
        addSeg(Seg.MoveTo);
        addPoint(x, y);
    }

    /**
     * LineTo.
     */
    public void lineTo(double x, double y)
    {
        addSeg(Seg.LineTo);
        addPoint(x, y);
    }

    /**
     * LineTo.
     */
    public void lineBy(double x, double y)
    {
        x += _points[_pointCount * 2 - 2];
        y += _points[_pointCount * 2 - 1];
        lineTo(x, y);
    }

    /**
     * Horizontal LineTo.
     */
    public void hlineTo(double x)
    {
        double y = _points[_pointCount * 2 - 1];
        lineTo(x, y);
    }

    /**
     * Vertical LineTo.
     */
    public void vlineTo(double y)
    {
        double x = _points[_pointCount * 2 - 2];
        lineTo(x, y);
    }

    /**
     * QuadTo.
     */
    public void quadTo(double cpx, double cpy, double x, double y)
    {
        addSeg(Seg.QuadTo);
        addPoint(cpx, cpy);
        addPoint(x, y);
    }

    /**
     * CubicTo.
     */
    public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y)
    {
        addSeg(Seg.CubicTo);
        addPoint(cp1x, cp1y);
        addPoint(cp2x, cp2y);
        addPoint(x, y);
    }

    /**
     * ArcTo: Adds a Cubic using the corner point as a guide.
     */
    public void arcTo(double cx, double cy, double x, double y)
    {
        double magic = .5523f; // I calculated this in mathematica one time - probably only valid for 90 deg corner.
        double lx = _points[_pointCount * 2 - 2];
        double ly = _points[_pointCount * 2 - 1];
        double cpx1 = lx + (cx - lx) * magic;
        double cpy1 = ly + (cy - ly) * magic;
        double cpx2 = x + (cx - x) * magic;
        double cpy2 = y + (cy - y) * magic;
        curveTo(cpx1, cpy1, cpx2, cpy2, x, y);
    }

    /**
     * Close.
     */
    public void close()
    {
        addSeg(Seg.Close);
    }

    /**
     * Returns the last segement.
     */
    public Seg getSegLast()
    {
        return _segCount > 0 ? _segs[_segCount - 1] : null;
    }

    /**
     * Removes the last element from the path.
     */
    public void removeLastSeg()
    {
        Seg seg = getSegLast();
        _pointCount -= seg.getCount();
        _segCount--;
        shapeChanged();
    }

    /**
     * Removes an element, reconnecting the elements on either side of the deleted element.
     */
    public void removeSeg(int anIndex)
    {
        // range check
        int segCount = getSegCount();
        if (anIndex < 0 || anIndex >= segCount)
            throw new IndexOutOfBoundsException("PathViewUtils.removeSeg: index out of bounds: " + anIndex);

        // If this is the last element, nuke it
        if (anIndex == segCount - 1) {
            removeLastSeg();
            if (getSeg(segCount - 1) == Seg.MoveTo) // but don't leave stray moveto sitting around
                removeLastSeg();
            return;
        }

        // Get some info
        int pindex = getSegPointIndex(anIndex);  // get the index to the first point for this element
        Seg seg = getSeg(anIndex);            // the type of element (MOVETO,LINETO,etc)
        int nPts = seg.getCount();                 // and how many points are associated with this element
        int nDeletedPts = nPts;                  // how many points to delete from the points array
        int nDeletedSegs = 1;                  // how many elements to delete (usually 1)
        int deleteIndex = anIndex;             // index to delete from element array (usually same as original index)

        // delete all poins but the last of the next segment
        if (seg == Seg.MoveTo) {
            nDeletedPts = getSeg(anIndex + 1).getCount();
            ++deleteIndex;  // delete the next element and preserve the MOVETO
        }

        else {
            // If next element is a curveTo, we are merging 2 curves into one, so delete points such that slopes
            // at endpoints of new curve match the starting and ending slopes of the originals.
            if (getSeg(anIndex + 1) == Seg.CubicTo)
                pindex++;

                // Deleting the only curve or a line in a subpath can leave a stray moveto. If that happens, delete it, too
            else if (getSeg(anIndex - 1) == Seg.MoveTo && getSeg(anIndex + 1) == Seg.MoveTo) {
                ++nDeletedSegs;
                --deleteIndex;
                ++nDeletedPts;
                --pindex;
            }
        }

        // Remove segement and points
        System.arraycopy(_segs, deleteIndex + nDeletedSegs, _segs, deleteIndex, _segCount - deleteIndex - nDeletedSegs);
        _segCount -= nDeletedSegs;
        System.arraycopy(_points, (pindex + nDeletedPts) * 2, _points, pindex * 2, (_pointCount - pindex - nDeletedPts) * 2);
        _pointCount -= nDeletedPts;
        shapeChanged();
    }

    /**
     * Returns last path point.
     */
    public Point getPointLast()
    {
        return _pointCount > 0 ? getPoint(_pointCount - 1) : null;
    }

    /**
     * Returns current path point.
     */
    public Point getCurrentPoint()
    {
        if (getSegLast() == Seg.Close && getPointCount() > 0)
            return getPoint(0);
        return getPointLast();
    }

    /**
     * Sets the individual point at given index.
     */
    public void setPoint(int anIndex, double aX, double aY)
    {
        _points[anIndex * 2] = aX;
        _points[anIndex * 2 + 1] = aY;
        shapeChanged();
    }

    /**
     * Returns the point index for a given segment index.
     */
    public int getSegPointIndex(int anIndex)
    {
        int pindex = 0;
        for (int i = 0; i < anIndex; i++) pindex += getSeg(i).getCount();
        return pindex;
    }

    /**
     * Returns the element index for the given point index.
     */
    public int getSegIndexForPointIndex(int anIndex)
    {
        int sindex = 0;
        for (int pindex = 0; pindex <= anIndex; sindex++) pindex += getSeg(sindex).getCount();
        return sindex - 1;
    }

    /**
     * Appends a path segment.
     */
    public void append(Segment aSeg)
    {
        if (aSeg instanceof Cubic) {
            Cubic seg = (Cubic) aSeg;
            curveTo(seg.cp0x, seg.cp0y, seg.cp1x, seg.cp1y, aSeg.x1, aSeg.y1);
        }
        else if (aSeg instanceof Quad) {
            Quad seg = (Quad) aSeg;
            quadTo(seg.cpx, seg.cpy, aSeg.x1, aSeg.y1);
        }
        else lineTo(aSeg.x1, aSeg.y1);
    }

    /**
     * Appends a path iterator.
     */
    public void append(PathIter aPI)
    {
        double[] points = new double[6];
        while (aPI.hasNext()) switch (aPI.getNext(points)) {
            case MoveTo: moveTo(points[0], points[1]); break;
            case LineTo: lineTo(points[0], points[1]); break;
            case QuadTo: quadTo(points[0], points[1], points[2], points[3]); break;
            case CubicTo: curveTo(points[0], points[1], points[2], points[3], points[4], points[5]); break;
            case Close: close(); break;
        }
    }

    /**
     * Appends a shape.
     */
    public void append(Shape aShape)
    {
        append(aShape.getPathIter(null));
    }

    /**
     * Fits the path points to a curve starting at the given point index.
     */
    public void fitToCurve(int anIndex)
    {
        PathFitCurves.fitCurveFromPointIndex(this, anIndex);
    }

    /**
     * Clears all segments from path.
     */
    public void clear()
    {
        _segCount = _pointCount = 0;
        shapeChanged();
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return new PathPathIter(this, aTrans);
    }

    /**
     * Override to return as path.
     */
    public Path copyFor(Rect aRect)
    {
        return (Path) super.copyFor(aRect);
    }

    /**
     * Standard clone implementation.
     */
    public Path clone()
    {
        Path copy;
        try { copy = (Path) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }
        copy._segs = Arrays.copyOf(_segs, _segs.length);
        copy._points = Arrays.copyOf(_points, _points.length);
        return copy;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity & class and get other path
        if (anObj == this) return true;
        Path path = anObj instanceof Path ? (Path) anObj : null;
        if (path == null) return false;

        // Check ElementCount, WindingRule, Elements and Points
        if (path._segCount != _segCount || path._pointCount != _pointCount) return false;
        if (!Arrays.equals(path._segs, _segs)) return false;
        if (!Arrays.equals(path._points, _points)) return false;
        return true; // Return true since all checks passed
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element named path
        XMLElement e = new XMLElement("path");

        // Archive winding rule
        //if (_windingRule!=WIND_NON_ZERO) e.add("wind", "even-odd");

        // Archive individual elements/points
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];
        while (pathIter.hasNext()) switch (pathIter.getNext(points)) {

            // Handle MoveTo
            case MoveTo:
                XMLElement move = new XMLElement("mv");
                move.add("x", points[0]);
                move.add("y", points[1]);
                e.add(move);
                break;

            // Handle LineTo
            case LineTo:
                XMLElement line = new XMLElement("ln");
                line.add("x", points[0]);
                line.add("y", points[1]);
                e.add(line);
                break;

            // Handle QuadTo
            case QuadTo:
                XMLElement quad = new XMLElement("qd");
                quad.add("cx", points[0]);
                quad.add("cy", points[1]);
                quad.add("x", points[2]);
                quad.add("y", points[3]);
                e.add(quad);
                break;

            // Handle CubicTo
            case CubicTo:
                XMLElement curve = new XMLElement("cv");
                curve.add("cp1x", points[0]);
                curve.add("cp1y", points[1]);
                curve.add("cp2x", points[2]);
                curve.add("cp2y", points[3]);
                curve.add("x", points[4]);
                curve.add("y", points[5]);
                e.add(curve);
                break;

            // Handle Close
            case Close:
                XMLElement close = new XMLElement("cl");
                e.add(close);
                break;
        }

        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive winding rule
        //if (anElement.getAttributeValue("wind", "non-zero").equals("even-odd")) setWindingRule(WIND_EVEN_ODD);

        // Unarchive individual elements/points
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement e = anElement.get(i);
            if (e.getName().equals("mv"))
                moveTo(e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
            else if (e.getName().equals("ln"))
                lineTo(e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
            else if (e.getName().equals("qd"))
                quadTo(e.getAttributeFloatValue("cx"), e.getAttributeFloatValue("cy"),
                        e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
            else if (e.getName().equals("cv"))
                curveTo(e.getAttributeFloatValue("cp1x"), e.getAttributeFloatValue("cp1y"),
                        e.getAttributeFloatValue("cp2x"), e.getAttributeFloatValue("cp2y"),
                        e.getAttributeFloatValue("x"), e.getAttributeFloatValue("y"));
            else if (e.getName().equals("cl"))
                close();
        }

        // Return this path
        return this;
    }

    /**
     * Returns a path from an SVG path string.
     */
    public static Path getPathFromSVG(String aStr)
    {
        try { return getPathFromSVGOrThrow(aStr); }
        catch (Exception e) {
            System.err.println("Path.getPathFromSVG: " + e);
            return null;
        }
    }

    /**
     * Returns a path from an SVG path string.
     */
    public static Path getPathFromSVGOrThrow(String aStr) throws InputMismatchException, NoSuchElementException
    {
        // Create scanner from string and new path
        Scanner scan = new Scanner(aStr);
        Path path = new Path();

        // Iterate over scanner tokens
        double x1, y1, cp0x, cp0y, cp1x, cp1y;
        while (scan.hasNext()) {
            String op = scan.next();
            switch (op) {
                case "M":
                    x1 = scan.nextDouble();
                    y1 = scan.nextDouble();
                    path.moveTo(x1, y1);
                    break;
                case "L":
                    x1 = scan.nextDouble();
                    y1 = scan.nextDouble();
                    path.lineTo(x1, y1);
                    break;
                case "Q":
                    cp0x = scan.nextDouble();
                    cp0y = scan.nextDouble();
                    x1 = scan.nextDouble();
                    y1 = scan.nextDouble();
                    path.quadTo(cp0x, cp0y, x1, y1);
                    break;
                case "C":
                    cp0x = scan.nextDouble();
                    cp0y = scan.nextDouble();
                    cp1x = scan.nextDouble();
                    cp1y = scan.nextDouble();
                    x1 = scan.nextDouble();
                    y1 = scan.nextDouble();
                    path.curveTo(cp0x, cp0y, cp1x, cp1y, x1, y1);
                    break;
                case "Z":
                    path.close();
                    break;
                default:
                    throw new NoSuchElementException("Invalid op: " + op);
            }
        }

        // Return
        return path;
    }

    /**
     * A PathIter for Path.
     */
    private static class PathPathIter extends PathIter {

        // Ivars
        Path _path;
        Transform _trans;
        int _sindex, _pindex;

        /**
         * Creates a new PathPathIter for Path.
         */
        PathPathIter(Path aPath, Transform aTrans)
        {
            _path = aPath;
            _trans = aTrans;
        }

        /**
         * Returns whether PathIter has another segement.
         */
        public boolean hasNext()
        {
            return _sindex < _path._segCount;
        }

        /**
         * Returns the next segment.
         */
        public Seg getNext(double[] coords)
        {
            Seg seg = _path._segs[_sindex++];
            int count = seg.getCount();
            for (int i = 0; i < count; i++) {
                coords[i * 2] = _path._points[_pindex++];
                coords[i * 2 + 1] = _path._points[_pindex++];
            }
            if (_trans != null) _trans.transformXYArray(coords, count);
            return seg;
        }

        /**
         * Returns the winding - how a path determines what to fill when segments intersect.
         */
        public int getWinding()
        {
            return _path.getWinding();
        }
    }
}