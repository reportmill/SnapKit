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
        // If index is last seg, just remove and return (but don't leave dangling moveto)
        int segCount = getSegCount();
        if (anIndex == segCount - 1) {
            removeLastSeg();
            if (getLastSeg() == Seg.MoveTo)
                removeLastSeg();
            return;
        }

        // Get delete seg and next seg
        Seg deleteSeg = getSeg(anIndex);
        Seg nextSeg = getSeg(anIndex + 1);

        // Get delete seg and point index + count
        int deleteSegIndex = anIndex;
        int deleteSegCount = 1;
        int deletePointIndex = getSegPointIndex(anIndex);
        int deletePointCount = deleteSeg.getCount();

        // If seg is MoveTo, delete next seg instead (but take MoveTo point and leave NextSeg end point)
        if (deleteSeg == Seg.MoveTo) {
            deleteSegIndex++;
            deletePointCount = nextSeg.getCount();
        }

        // If next seg is a curve, we are merging 2 curves into one, so delete next control point instead,
        // so that slopes at endpoints of new curve match the starting and ending slopes of the originals.
        else if (nextSeg == Seg.CubicTo || nextSeg == Seg.QuadTo)
            deletePointIndex++;

        // If next and last segs are MoveTos (deleting only curve/line between two MoveTos), delete previous MoveTo as well
        else if (nextSeg == Seg.MoveTo && anIndex > 0 && getSeg(anIndex - 1) == Seg.MoveTo) {
            deleteSegIndex--; deleteSegCount++;
            deletePointIndex--; deletePointCount++;
        }

        // Delete segs, seg point indexes
        int deleteSegEndIndex = deleteSegIndex + deleteSegCount;
        int deleteSegTailLength = _segCount - deleteSegEndIndex;
        System.arraycopy(_segs, deleteSegEndIndex, _segs, deleteSegIndex, deleteSegTailLength);
        _segCount -= deleteSegCount;

        // Delete points ( x2 for XY coords array)
        int deletePointEndIndex = deletePointIndex + deletePointCount;
        int deletePointTailLength = _pointCount - deletePointEndIndex;
        System.arraycopy(_points, deletePointEndIndex * 2, _points, deletePointIndex * 2, deletePointTailLength * 2);
        _pointCount -= deletePointCount;

        // Update SegPointIndexes
        for (int i = 1; i < _segCount; i++)
            _segPointIndexes[i] = _segPointIndexes[i - 1] + getSeg(i - 1).getCount();

        // Notify shape changed
        shapeChanged();
    }

    /**
     * Returns the element index for the given point index.
     */
    public int getSegIndexForPointIndex(int anIndex)
    {
        int segIndex = 0;
        for (int pointIndex = 0; pointIndex <= anIndex && segIndex < _segCount; segIndex++)
            pointIndex += getSeg(segIndex).getCount();
        return segIndex - 1;
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