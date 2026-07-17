package snap.geom;

// Constants for segments
public enum Seg {
    
    // Constants
    MoveTo(1), LineTo(1), QuadTo(2), CubicTo(3), Close(0);
        
    // Methods
    final int _count;
    Seg(int count)  { _count = count; }

    /**
     * Returns the number of points.
     */
    public int getCount() { return _count; }

    /**
     * Returns the arc length for segment and points.
     */
    public double getArcLengthForPoints(double[] points)
    {
        return switch (this) {
            case MoveTo -> 0;
            case LineTo -> Point.getDistance(points[0], points[1], points[2], points[3]);
            case QuadTo ->
                    SegmentLengths.getArcLengthQuad(points[0], points[1], points[2], points[3], points[4], points[5]);
            case CubicTo ->
                    SegmentLengths.getArcLengthCubic(points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7]);
            default -> throw new RuntimeException("Seg.getArcLengthForPoints: Unsuppored seg: " + this);
        };
    }
}