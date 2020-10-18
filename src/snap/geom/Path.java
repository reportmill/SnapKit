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
    private Seg  _segs[] = new Seg[8];

    // The segment count
    private int  _scount;
    
    // The array of points
    private double  _points[] = new double[16];
    
    // The number of points
    private int  _pcount;
    
    // The winding -how a path determines what to fill when segments intersect
    private int  _wind = WIND_EVEN_ODD;
    
    // Constants for winding
    public static final int WIND_EVEN_ODD = PathIter.WIND_EVEN_ODD;
    public static final int WIND_NON_ZERO = PathIter.WIND_NON_ZERO;

    /**
     * Creates a new path.
     */
    public Path() { }

    /**
     * Creates a new path with given path iterator.
     */
    public Path(PathIter aPI)  { append(aPI); }

    /**
     * Creates a path for given shape.
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
    public int getSegCount()  { return _scount; }

    /**
     * Returns the individual segement at index.
     */
    public Seg getSeg(int anIndex)  { return _segs[anIndex]; }

    /**
     * Adds a segment.
     */
    protected void addSeg(Seg aSeg)
    {
        // If at end of Segs array, extend by 2x
        if (_scount+1>_segs.length)
            _segs = Arrays.copyOf(_segs, _segs.length*2);

        // Add Seg at end, increment SegCount, notify shapeChanged
        _segs[_scount++] = aSeg;
        shapeChanged();
    }

    /**
     * Returns the number of points.
     */
    public int getPointCount()  { return _pcount; }

    /**
     * Returns individual point at given index.
     */
    public Point getPoint(int anIndex)
    {
        double px = _points[anIndex*2];
        double py = _points[anIndex*2+1];
        return new Point(px, py);
    }

    /**
     * Adds a point.
     */
    protected void addPoint(double x, double y)
    {
        // If at end of Points array, extend by 2x
        if (_pcount*2+1>_points.length)
            _points = Arrays.copyOf(_points, _points.length*2);

        // Add points at end and increment PointCount
        _points[_pcount*2] = x;
        _points[_pcount*2+1] = y;
        _pcount++;
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
        x += _points[_pcount*2-2];
        y += _points[_pcount*2-1];
        lineTo(x, y);
    }

    /**
     * Horizontal LineTo.
     */
    public void hlineTo(double x)
    {
        double y = _points[_pcount*2-1];
        lineTo(x,y);
    }

    /**
     * Vertical LineTo.
     */
    public void vlineTo(double y)
    {
        double x = _points[_pcount*2-2];
        lineTo(x,y);
    }

    /**
     * QuadTo.
     */
    public void quadTo(double cpx, double cpy, double x, double y)
    {
        addSeg(Seg.QuadTo);
        addPoint(cpx,cpy);
        addPoint(x,y);
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
        double lx = _points[_pcount*2-2];
        double ly = _points[_pcount*2-1];
        double cpx1 = lx + (cx-lx)*magic;
        double cpy1 = ly + (cy-ly)*magic;
        double cpx2 = x + (cx-x)*magic;
        double cpy2 = y + (cy-y)*magic;
        curveTo(cpx1, cpy1, cpx2, cpy2, x, y);
    }

    /**
     * QuadTo by adding lineTo segments.
     */
    public void quadToFlat(double cpx, double cpy, double x, double y)
    {
        // If distance from control point to base line less than tolerance, just add line
        Point last = getCurrentPoint();
        double dist = Line.getDistance(last.x, last.y, x, y, cpx, cpy);
        if (dist<.25) {
            lineTo(x,y); return; }

        // Split curve at midpoint and add parts
        Quad c0 = new Quad(last.x, last.y, cpx, cpy, x, y);
        Quad c1 = c0.split(.5);
        quadToFlat(c0.cpx, c0.cpy, c0.x1, c0.y1);
        quadToFlat(c1.cpx, c1.cpy, c1.x1, c1.y1);
    }

    /**
     * CubicTo by adding lineTo segments.
     */
    public void curveToFlat(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y)
    {
        // If distance from control points to base line less than tolerance, just add line
        Point last = getCurrentPoint();
        double dist1 = Line.getDistance(last.x, last.y, x, y, cp1x, cp1y);
        double dist2 = Line.getDistance(last.x, last.y, x, y, cp2x, cp2y);
        if (dist1<.25 && dist2<.25) {
            lineTo(x,y); return; }

        // Split curve at midpoint and add parts
        Cubic c0 = new Cubic(last.x, last.y, cp1x, cp1y, cp2x, cp2y, x, y);
        Cubic c1 = c0.split(.5);
        curveToFlat(c0.cp0x, c0.cp0y, c0.cp1x, c0.cp1y, c0.x1, c0.y1);
        curveToFlat(c1.cp0x, c1.cp0y, c1.cp1x, c1.cp1y, c1.x1, c1.y1);
    }

    /**
     * Close.
     */
    public void close() { addSeg(Seg.Close); }

    /**
     * Returns the last segement.
     */
    public Seg getSegLast()  { return _scount>0 ? _segs[_scount-1] : null; }

    /**
     * Removes the last element from the path.
     */
    public void removeLastSeg()
    {
        Seg seg = getSegLast();
        _pcount -= seg.getCount();
        _scount--;
        shapeChanged();
    }

    /**
     * Removes an element, reconnecting the elements on either side of the deleted element.
     */
    public void removeSeg(int anIndex)
    {
        // range check
        int scount = getSegCount();
        if (anIndex<0 || anIndex>=scount)
            throw new IndexOutOfBoundsException("PathViewUtils.removeSeg: index out of bounds: " + anIndex);

        // If this is the last element, nuke it
        if (anIndex==scount-1) {
            removeLastSeg();
            if (scount>0 && getSeg(scount-1)==Seg.MoveTo) // but don't leave stray moveto sitting around
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
        if (seg==Seg.MoveTo) {
            nDeletedPts = getSeg(anIndex+1).getCount();
            ++deleteIndex;  // delete the next element and preserve the MOVETO
        }

        else {
            // If next element is a curveTo, we are merging 2 curves into one, so delete points such that slopes
            // at endpoints of new curve match the starting and ending slopes of the originals.
            if (getSeg(anIndex+1)==Seg.CubicTo)
                pindex++;

            // Deleting the only curve or a line in a subpath can leave a stray moveto. If that happens, delete it, too
            else if (getSeg(anIndex-1)==Seg.MoveTo && getSeg(anIndex+1)==Seg.MoveTo) {
                ++nDeletedSegs; --deleteIndex; ++nDeletedPts; --pindex; }
        }

        // Remove segement and points
        System.arraycopy(_segs, deleteIndex+nDeletedSegs, _segs, deleteIndex, _scount-deleteIndex-nDeletedSegs);
        _scount -= nDeletedSegs;
        System.arraycopy(_points, (pindex+nDeletedPts)*2, _points, pindex*2, (_pcount-pindex-nDeletedPts)*2);
        _pcount -= nDeletedPts;
        shapeChanged();
    }

    /**
     * Returns last path point.
     */
    public Point getPointLast()
    {
        return _pcount>0 ? getPoint(_pcount-1) : null;
    }

    /**
     * Returns current path point.
     */
    public Point getCurrentPoint()
    {
        if (getSegLast()==Seg.Close && getPointCount()>0)
            return getPoint(0);
        return getPointLast();
    }

    /**
     * Sets the individual point at given index.
     */
    public void setPoint(int anIndex, double aX, double aY)
    {
        _points[anIndex*2] = aX;
        _points[anIndex*2+1] = aY;
        shapeChanged();
    }

    /**
     * Returns the point index for a given segment index.
     */
    public int getSegPointIndex(int anIndex)
    {
        int pindex = 0; for (int i=0; i<anIndex; i++) pindex += getSeg(i).getCount();
        return pindex;
    }

    /**
     * Returns the element index for the given point index.
     */
    public int getSegIndexForPointIndex(int anIndex)
    {
        int sindex = 0;
        for (int pindex=0; pindex<=anIndex; sindex++) pindex += getSeg(sindex).getCount();
        return sindex - 1;
    }

    /**
     * Appends a path segment.
     */
    public void append(Segment aSeg)
    {
        if (aSeg instanceof Cubic) { Cubic seg = (Cubic)aSeg;
            curveTo(seg.cp0x, seg.cp0y, seg.cp1x, seg.cp1y, aSeg.x1, aSeg.y1); }
        else if (aSeg instanceof Quad) { Quad seg = (Quad)aSeg;
            quadTo(seg.cpx, seg.cpy, aSeg.x1, aSeg.y1); }
        else lineTo(aSeg.x1, aSeg.y1);
    }

    /**
     * Appends a path iterator.
     */
    public void append(PathIter aPI)
    {
        double crds[] = new double[6];
        while (aPI.hasNext()) switch (aPI.getNext(crds)) {
            case MoveTo: moveTo(crds[0], crds[1]); break;
            case LineTo: lineTo(crds[0], crds[1]); break;
            case QuadTo: quadTo(crds[0], crds[1], crds[2], crds[3]); break;
            case CubicTo: curveTo(crds[0], crds[1], crds[2], crds[3], crds[4], crds[5]); break;
            case Close: close(); break;
        }
    }

    /**
     * Appends a shape.
     */
    public void append(Shape aShape)  { append(aShape.getPathIter(null)); }

    /**
     * Fits the path points to a curve starting at the given point index.
     */
    public void fitToCurve(int anIndex)  { PathFitCurves.fitCurveFromPointIndex(this, anIndex); }

    /**
     * Transforms the points in the path by the given transform.
     */
    public void transformBy(Transform aTrans)
    {
        for (int i=0, iMax=getPointCount(); i<iMax; i++) {
            Point p = getPoint(i);
            aTrans.transform(p,p);
            setPoint(i, p.x, p.y);
        }
    }

    /**
     * Clears all segments from path.
     */
    public void clear()
    {
        _scount = _pcount = 0;
        shapeChanged();
    }

    /**
     * Returns a path with only moveto, lineto.
     */
    public Path getPathFlattened()
    {
        // Get a new path and point-array for path segment iteration and iterate over path segments
        Path path = new Path();
        PathIter piter = getPathIter(null); double pts[] = new double[6];
        while (piter.hasNext()) switch (piter.getNext(pts)) {
            case MoveTo: path.moveTo(pts[0], pts[1]); break;
            case LineTo: path.lineTo(pts[0], pts[1]); break;
            case QuadTo: path.quadToFlat(pts[0], pts[1], pts[2], pts[3]); break;
            case CubicTo: path.curveToFlat(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]); break;
            case Close: path.close(); break;
        }

        // Return new path
        return path;
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)  { return new PathPathIter(this, aTrans); }

    /**
     * Override to return as path.
     */
    public Path copyFor(Rect aRect)  { return (Path)super.copyFor(aRect); }

    /**
     * Standard clone implementation.
     */
    public Path clone()
    {
        Path copy; try { copy = (Path)super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
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
        if (anObj==this) return true;
        Path path = anObj instanceof Path ? (Path) anObj : null; if (path==null) return false;

        // Check ElementCount, WindingRule, Elements and Points
        if (path._scount!=_scount || path._pcount!=_pcount) return false;
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
        PathIter piter = getPathIter(null); double pts[] = new double[6];
        while (piter.hasNext()) switch (piter.getNext(pts)) {

            // Handle MoveTo
            case MoveTo: XMLElement move = new XMLElement("mv");
                move.add("x", pts[0]); move.add("y", pts[1]); e.add(move); break;

            // Handle LineTo
            case LineTo: XMLElement line = new XMLElement("ln");
                line.add("x", pts[0]); line.add("y", pts[1]); e.add(line); break;

            // Handle QuadTo
            case QuadTo: XMLElement quad = new XMLElement("qd");
                quad.add("cx", pts[0]); quad.add("cy", pts[1]);
                quad.add("x", pts[2]); quad.add("y", pts[3]);
                e.add(quad); break;

            // Handle CubicTo
            case CubicTo: XMLElement curve = new XMLElement("cv");
                curve.add("cp1x", pts[0]); curve.add("cp1y", pts[1]);
                curve.add("cp2x", pts[2]); curve.add("cp2y", pts[3]);
                curve.add("x", pts[4]); curve.add("y", pts[5]);
                e.add(curve); break;

            // Handle Close
            case Close: XMLElement close = new XMLElement("cl"); e.add(close); break;
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
        for (int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement e = anElement.get(i);
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
        catch(Exception e) { System.err.println("Path.getPathFromSVG: " + e); return null; }
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
                case "M": x1 = scan.nextDouble(); y1 = scan.nextDouble(); path.moveTo(x1, y1); break;
                case "L": x1 = scan.nextDouble(); y1 = scan.nextDouble(); path.lineTo(x1, y1); break;
                case "Q": cp0x = scan.nextDouble(); cp0y = scan.nextDouble();
                    x1 = scan.nextDouble(); y1 = scan.nextDouble(); path.quadTo(cp0x, cp0y, x1, y1); break;
                case "C": cp0x = scan.nextDouble(); cp0y = scan.nextDouble();
                    cp1x = scan.nextDouble(); cp1y = scan.nextDouble();
                    x1 = scan.nextDouble(); y1 = scan.nextDouble(); path.curveTo(cp0x, cp0y, cp1x, cp1y,x1, y1); break;
                case "Z": path.close(); break;
                default: throw new NoSuchElementException("Invalid op: " + op);
            }
        }

        // Return path
        return path;
    }

    /**
     * A PathIter for Path.
     */
    private static class PathPathIter extends PathIter {

        // Ivars
        Path _path; Transform _trans; int _sindex, _pindex;

        /** Creates a new PathPathIter for Path. */
        PathPathIter(Path aPath, Transform aTrans)  { _path = aPath; _trans = aTrans; }

        /** Returns whether PathIter has another segement. */
        public boolean hasNext()  { return _sindex<_path._scount; }

        /** Returns the next segment. */
        public Seg getNext(double coords[])
        {
            Seg seg = _path._segs[_sindex++]; int count = seg.getCount();
            for (int i=0;i<count;i++) { coords[i*2] = _path._points[_pindex++]; coords[i*2+1] = _path._points[_pindex++]; }
            if (_trans!=null) _trans.transform(coords, count);
            return seg;
        }

        /** Returns the winding - how a path determines what to fill when segments intersect. */
        public int getWinding()  { return _path.getWinding(); }

    }
}