/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * A Shape subclass that represents a general path.
 */
public class Path extends Path2D implements Cloneable {

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
        for (int i = deleteSegIndex + 1; i < _segCount; i++)
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
}