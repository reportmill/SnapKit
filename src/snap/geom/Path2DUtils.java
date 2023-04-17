package snap.geom;

/**
 * Utility methods for Path2D.
 */
public class Path2DUtils {

    /**
     * Removes the seg at given index, reconnecting the segs on either side of deleted seg.
     */
    public static void removeSegAtIndexSmoothly(Path2D aPath, int anIndex)
    {
        // If index is last seg, just remove and return (but don't leave dangling moveto)
        int segCount = aPath.getSegCount();
        if (anIndex == segCount - 1) {
            aPath.removeLastSeg();
            if (aPath.getLastSeg() == Seg.MoveTo)
                aPath.removeLastSeg();
            return;
        }

        // Get delete seg and next seg
        Seg deleteSeg = aPath.getSeg(anIndex);
        Seg nextSeg = aPath.getSeg(anIndex + 1);

        // Get delete seg and point index + count
        int deleteSegIndex = anIndex;
        int deleteSegCount = 1;
        int deletePointIndex = aPath.getSegPointIndex(anIndex);
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
        else if (nextSeg == Seg.MoveTo && anIndex > 0 && aPath.getSeg(anIndex - 1) == Seg.MoveTo) {
            deleteSegIndex--; deleteSegCount++;
            deletePointIndex--; deletePointCount++;
        }

        // Delete segs, seg point indexes
        int deleteSegEndIndex = deleteSegIndex + deleteSegCount;
        int deleteSegTailLength = aPath._segCount - deleteSegEndIndex;
        System.arraycopy(aPath._segs, deleteSegEndIndex, aPath._segs, deleteSegIndex, deleteSegTailLength);
        aPath._segCount -= deleteSegCount;

        // Delete points ( x2 for XY coords array)
        int deletePointEndIndex = deletePointIndex + deletePointCount;
        int deletePointTailLength = aPath._pointCount - deletePointEndIndex;
        System.arraycopy(aPath._points, deletePointEndIndex * 2, aPath._points, deletePointIndex * 2, deletePointTailLength * 2);
        aPath._pointCount -= deletePointCount;

        // Update SegPointIndexes
        for (int i = deleteSegIndex + 1; i < aPath._segCount; i++)
            aPath._segPointIndexes[i] = aPath._segPointIndexes[i - 1] + aPath.getSeg(i - 1).getCount();

        // Notify shape changed
        aPath.shapeChanged();
    }
}
