/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.*;

/**
 * A Shape subclass that represents a general path.
 */
public class Path extends Path2D implements Cloneable, XMLArchiver.Archivable {

    /**
     * Constructor.
     */
    public Path()
    {
        super();
    }

    /**
     * Constructor for given shape.
     */
    public Path(Shape aShape)
    {
        super(aShape);
    }

    /**
     * Constructor with given PathIter.
     */
    public Path(PathIter aPathIter)
    {
        super(aPathIter);
    }

    /**
     * Removes an element, reconnecting the elements on either side of the deleted element.
     */
    public void removeSeg(int anIndex)
    {
        // Range check
        int segCount = getSegCount();
        if (anIndex < 0 || anIndex >= segCount)
            throw new IndexOutOfBoundsException("Path.removeSeg: index out of bounds: " + anIndex);

        // If index is last seg, just remove and return (but don't leave dangling moveto)
        if (anIndex == segCount - 1) {
            removeLastSeg();
            if (getLastSeg() == Seg.MoveTo)
                removeLastSeg();
            return;
        }

        // Get some info
        Seg seg = getSeg(anIndex);
        int segPointIndex = getSegPointIndex(anIndex);
        int segPointCount = seg.getCount();
        int nDeletedPts = segPointCount;       // how many points to delete from the points array
        int nDeletedSegs = 1;                  // how many elements to delete (usually 1)
        int deleteIndex = anIndex;             // index to delete from element array (usually same as original index)

        // delete all points but the last of the next segment
        if (seg == Seg.MoveTo) {
            nDeletedPts = getSeg(anIndex + 1).getCount();
            deleteIndex++;  // delete the next element and preserve the MOVETO
        }

        else {
            // If next element is a curveTo, we are merging 2 curves into one, so delete points such that slopes
            // at endpoints of new curve match the starting and ending slopes of the originals.
            if (getSeg(anIndex + 1) == Seg.CubicTo)
                segPointIndex++;

                // Deleting the only curve or a line in a subpath can leave a stray moveto. If that happens, delete it, too
            else if (getSeg(anIndex - 1) == Seg.MoveTo && getSeg(anIndex + 1) == Seg.MoveTo) {
                nDeletedSegs++;
                deleteIndex--;
                nDeletedPts++;
                segPointIndex--;
            }
        }

        // Delete segs, seg point indexes
        int deleteEndIndex = deleteIndex + nDeletedSegs;
        int deleteLength = _segCount - deleteIndex - nDeletedSegs;
        System.arraycopy(_segs, deleteEndIndex, _segs, deleteIndex, deleteLength);
        System.arraycopy(_segPointIndexes, deleteEndIndex, _segPointIndexes, deleteIndex, deleteLength);
        _segCount -= nDeletedSegs;

        // Delete points
        int deletePointStartIndex = segPointIndex * 2;
        int deletePointEndIndex = (segPointIndex + nDeletedPts) * 2;
        int deletePointLength = (_pointCount - segPointIndex - nDeletedPts) * 2;
        System.arraycopy(_points, deletePointEndIndex, _points, deletePointStartIndex, deletePointLength);
        _pointCount -= nDeletedPts;
        shapeChanged();
    }

    /**
     * Returns current path point.
     */
    public Point getCurrentPoint()
    {
        if (getLastSeg() == Seg.Close && getPointCount() > 0)
            return getPoint(0);
        return getLastPoint();
    }

    /**
     * Returns the element index for the given point index.
     */
    public int getSegIndexForPointIndex(int anIndex)
    {
        int segIndex = 0;
        for (int pointIndex = 0; pointIndex <= anIndex; segIndex++)
            pointIndex += getSeg(segIndex).getCount();
        return segIndex - 1;
    }

    /**
     * Fits the path points to a curve starting at the given point index.
     */
    public void fitToCurve(int anIndex)
    {
        PathFitCurves.fitCurveFromPointIndex(this, anIndex);
    }

    /**
     * Override to return as path.
     */
    public Path copyFor(Rect aRect)
    {
        return (Path) super.copyFor(aRect);
    }

    /**
     * Override to return as path.
     */
    public Path clone()  { return (Path) super.clone(); }

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
}