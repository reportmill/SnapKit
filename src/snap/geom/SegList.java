package snap.geom;
import snap.util.ListUtils;
import java.util.*;

/**
 * A Shape containing a list of Segment shapes (Line, Quad, Cubic) to facilitate complex shape operations like
 * constructive area geometry (CAG) operations (add, subtract, intersect paths).
 */
public class SegList extends Shape {
    
    // The original shape
    private Shape  _shape;
    
    // The list of segements
    private List<Segment>  _segs = new ArrayList<>();

    /**
     * Constructor.
     */
    public SegList()
    {
        super();
    }

    /**
     * Constructor for given shape.
     */
    public SegList(Shape aShape)
    {
        super();
        _shape = aShape;
        buildSegmentsFromShape();
    }

    /**
     * Returns the list of segments.
     */
    public List <Segment> getSegs()  { return _segs; }

    /**
     * Returns the number of segments.
     */
    public int getSegCount()  { return _segs.size(); }

    /**
     * Returns the individual segment at given index.
     */
    public Segment getSeg(int anIndex)  { return _segs.get(anIndex); }

    /**
     * Adds the given segment.
     */
    public void addSeg(Segment aSeg)  { addSeg(aSeg, getSegCount()); }

    /**
     * Adds the given segment.
     */
    public void addSeg(Segment aSeg, int anIndex)  { _segs.add(anIndex, aSeg); }

    /**
     * Removes a given segment.
     */
    public Segment removeSeg(int anIndex)  { return _segs.remove(anIndex); }

    /**
     * Returns whether this SegList contains given point.
     */
    public boolean contains(double aX, double aY)
    {
        boolean c1 = containsEndPoint(aX, aY);
        return c1 || _shape.contains(aX,aY);
    }

    /**
     * Returns whether this SegList contains given point.
     */
    public boolean containsSegMid(Segment aSeg)
    {
        double midX = aSeg.getX(.5);
        double midY = aSeg.getY(.5);
        return contains(midX, midY);
    }

    /**
     * Returns whether given seg list contains given point.
     */
    public boolean containsEndPoint(double x, double y)
    {
        for (Segment seg : _segs)
            if (eq(seg.x1, x) && eq(seg.y1, y))
                return true;
        return false;
    }

    /**
     * Returns whether segment list contains segment (regardless of direction).
     */
    public boolean hasSeg(Segment aSeg)
    {
        for (Segment seg : _segs)
            if (seg.matches(aSeg))
                return true;
        return false;
    }

    /**
     * Returns the first Segment from this SegmentList outside of given SegList.
     */
    private Segment getFirstSegOutside(SegList aShape)
    {
        return ListUtils.findMatch(_segs, seg -> !aShape.containsSegMid(seg));
    }

    /**
     * Returns the first Segment from this SegmentList outside of given SegList.
     */
    private Segment getFirstSegInside(SegList aShape)
    {
        return ListUtils.findMatch(_segs, seg -> aShape.contains(seg.getX0(), seg.getY0()));
    }

    /**
     * Returns the segments from list for end point of given seg.
     */
    private List <Segment> getSegmentsThatStartOrEndAtSegmentEndPoint(Segment aSeg)
    {
        double segEndX = aSeg.getX1();
        double segEndY = aSeg.getY1();
        List <Segment> segs = Collections.EMPTY_LIST;

        for (Segment seg : _segs) {
            if (seg.equals(aSeg))
                continue;
            if (eq(segEndX, seg.getX0()) && eq(segEndY, seg.getY0()))
                segs = add(segs, seg);
            if (eq(segEndX, seg.getX1()) && eq(segEndY, seg.getY1()))
                segs = add(segs, seg.createReverse());
        }

        // Return
        return segs;
    }

    /**
     * Creates the path from the list of segments.
     */
    public Path createPath()
    {
        // Iterate vars
        Path path = new Path();
        double moveX = 0, moveY = 0;
        double lineX = 0, lineY = 0;

        // Iterate over segments, if any segment intersects cubic, return true
        for (Segment seg : _segs) {

            // If last end point was last move point, add moveTo
            if (lineX == moveX && lineY == moveY)
                path.moveTo(moveX = seg.x0, moveY = seg.y0);

            // Handle Line
            if (seg instanceof Line) {
                Line line = (Line) seg;
                if (moveX == line.x1 && moveY == line.y1)
                    path.close();
                else path.lineTo(lineX = line.x1, lineY = line.y1);
            }

            // Handle Quad
            else if (seg instanceof Quad) {
                Quad quad = (Quad) seg;
                path.quadTo(quad.cpx, quad.cpy, lineX = quad.x1, lineY = quad.y1);
            }

            // Handle Cubic
            else if (seg instanceof Cubic) {
                Cubic cubic = (Cubic) seg;
                path.curveTo(cubic.cp0x, cubic.cp0y, cubic.cp1x, cubic.cp1y, lineX = cubic.x1, lineY = cubic.y1);
            }
        }

        // Return new path
        return path;
    }

    /**
     * Creates an array of paths from the list of segments.
     */
    public Path[] createPaths()
    {
        // If single path, just create and return it
        if (!isMultiPath())
            return new Path[] { createPath() };

        // Iterate vars
        List<Path> paths = new ArrayList<>();
        Path path = null;
        double moveX = 0, moveY = 0;
        double lineX = 0, lineY = 0;

        // Iterate over segments, if any segment intersects cubic, return true
        for (Segment seg : _segs) {

            // If last end point was last move point, start new path
            if (lineX == moveX && lineY == moveY) {
                paths.add(path = new Path());
                path.moveTo(moveX = seg.x0, moveY = seg.y0);
            }

            // Handle Line segment
            if (seg instanceof Line) {
                Line line = (Line) seg;
                if (moveX == line.x1 && moveY == line.y1)
                    path.close();
                else path.lineTo(lineX = line.x1, lineY = line.y1);
            }

            // Handle Quad segment
            else if (seg instanceof Quad) {
                Quad quad = (Quad) seg;
                path.quadTo(quad.cpx, quad.cpy, lineX = quad.x1, lineY = quad.y1);
            }

            // Handle Cubic segment
            else if (seg instanceof Cubic) {
                Cubic cubic = (Cubic) seg;
                path.curveTo(cubic.cp0x, cubic.cp0y, cubic.cp1x, cubic.cp1y, lineX = cubic.x1, lineY = cubic.y1);
            }
        }

        // Return new path
        return paths.toArray(new Path[0]);
    }

    /**
     * Splits the segments in this SegList wherever it intersects itself.
     */
    public boolean splitSegments()
    {
        int segCount = getSegCount();
        boolean didSplit = false;

        // Iterate over list1 and split at all intersections with slist2
        for (int i = 0; i < segCount; i++) { Segment seg1 = getSeg(i);
            for (int j = i + 1; j < segCount; j++) { Segment seg2 = getSeg(j);

                // If segments intersect
                if (seg1.intersectsSeg(seg2)) {

                    // Find intersection point for seg2 and split/add if inside
                    double hitPoint2 = seg2.getHitPoint(seg1);
                    if (hitPoint2 > .001 && hitPoint2 < .999) {
                        Segment tail = seg2.split(hitPoint2);
                        addSeg(tail, j+1); segCount++;
                        didSplit = true;
                    }

                    // Find intersection point for seg1 and split/add if inside
                    double hitPoint1 = seg1.getHitPoint(seg2);
                    if (hitPoint1 > .001 && hitPoint1 < .999) {
                        Segment tail = seg1.split(hitPoint1);
                        addSeg(tail, i+1); segCount++;
                        didSplit = true;
                    }
                }
            }
        }

        // Return whether SegList did split somewhere
        return didSplit;
    }

    /**
     * Returns whether this SegList is simple (not self-intersecting).
     */
    public boolean isSimple()
    {
        // Iterate over list1 and split at all intersections with slist2
        int segCount = getSegCount();
        for (int i = 0; i < segCount; i++) { Segment seg1 = getSeg(i);
            for (int j = i + 1; j < segCount; j++) { Segment seg2 = getSeg(j);

                // If segments intersect somewhere not on an end-point, return false
                if (seg1.intersectsSeg(seg2)) {
                    double hitPoint2 = seg2.getHitPoint(seg1);
                    if (hitPoint2 > .001 && hitPoint2 < .999)
                        return false;
                    double hitPoint1 = seg1.getHitPoint(seg2);
                    if (hitPoint1 > .001 && hitPoint1 < .999)
                        return false;
                }
            }
        }

        // Return whether SegList did split somewhere
        return true;
    }

    /**
     * Makes the seg list simple (non intersecting).
     */
    public boolean makeSimple()
    {
        // Split any self intersecting segments (return if none)
        boolean didSplit = splitSegments();
        if (!didSplit)
            return false;

        // Create list of segments to be added at end
        List <Segment> subsegs = new ArrayList<>();
        int segCount = getSegCount();

        // Extract subpath segments
        for (int i = 0; i < segCount; i++) { Segment seg0 = getSeg(i);
            for (int j = 0; j < i; j++) { Segment seg1 = getSeg(j);

                // If primary segment endpoint equals second segment start point, move segments in range out to subsegs list
                if (Point.equals(seg0.getX1(), seg0.getY1(), seg1.getX0(), seg1.getY0())) {
                    for (;i>=j;i--)
                        subsegs.add(removeSeg(j));
                    i = j-1;
                    segCount = getSegCount();
                    break;
                }
            }
        }

        // Add subsegs to end
        for (Segment seg : subsegs)
            addSeg(seg);

        // Return success
        return true;
    }

    /**
     * Splits the segments for this and given SegList for every intersection of the two.
     */
    public void splitSegments(SegList aSegList)
    {
        // Iterate over list1 and split at all intersections with slist2
        int sc = getSegCount(), sc2 = aSegList.getSegCount();
        for (int i=0; i<sc; i++) { Segment seg1 = getSeg(i);
            for (int j=0; j<sc2; j++) { Segment seg2 = aSegList.getSeg(j);

                // If segments intersect
                if (seg1.intersectsSeg(seg2)) {

                    // Find intersection point for seg1 and split/add if inside
                    double hp1 = seg1.getHitPoint(seg2);
                    if (hp1 > .001 && hp1 < .999) {
                        Segment tail = seg1.split(hp1);
                        addSeg(tail, i+1); sc++;
                    }

                    // Find intersection point for seg2 and split/add if inside
                    double hp2 = seg2.getHitPoint(seg1);
                    if (hp2 > .001 && hp2 < .999) {
                        Segment tail = seg2.split(hp2);
                        aSegList.addSeg(tail, j+1); sc2++;
                    }
                }
            }
        }
    }

    /**
     * Builds segments from shape.
     */
    private void buildSegmentsFromShape()
    {
        // Iterate over segments, if any segment intersects cubic, return true
        PathIter pathIter = _shape.getPathIter(null);
        double[] points = new double[6];
        double moveX = 0, moveY = 0;
        double lineX = 0, lineY = 0;

        while (pathIter.hasNext()) {
            switch (pathIter.getNext(points)) {

                // Handle MoveTo
                case MoveTo:
                    lineX = moveX = points[0];
                    lineY = moveY = points[1];
                    break;

                // Handle LineTo
                case LineTo: {
                    Line lineTo = new Line(lineX, lineY, lineX = points[0], lineY = points[1]);
                    _segs.add(lineTo);
                    break;
                }

                // Handle QuadTo
                case QuadTo: {
                    Quad quadTo = new Quad(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3]);
                    _segs.add(quadTo);
                    break;
                }

                // Handle CubicTo
                case CubicTo: {
                    Cubic cubicTo = new Cubic(lineX, lineY, points[0], points[1], points[2], points[3],lineX = points[4], lineY = points[5]);
                    _segs.add(cubicTo);
                    break;
                }

                // Handle Close
                case Close:
                    if (lineX != moveX || lineY != moveY)
                        _segs.add(new Line(lineX, lineY, lineX = moveX, lineY = moveY));
                    break;
            }
        }
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)  { return new SegListIter(this, aTrans); }

    /**
     * Returns a simple shape for complex shape.
     */
    public static Shape makeSimple(Shape aShape)
    {
        SegList slist = new SegList(aShape);
        slist.makeSimple();
        return slist;
    }

    /**
     * Returns the intersection shape of two shapes.
     */
    public static Shape addShapes(Shape aShape1, Shape aShape2)
    {
        // Simple case: If shapes don't intersect, just add them and return
        if (!aShape1.intersects(aShape2)) {
            Path path = new Path(aShape1);
            path.appendShape(aShape2);
            return path;
        }

        // Simple case: If either shape contains other, return outer shape
        if (aShape1.contains(aShape2))
            return aShape1;
        if (aShape2.contains(aShape1))
            return aShape2;

        // Create SegLists for given shapes and new shape
        SegList shape1 = new SegList(aShape1);
        SegList shape2 = new SegList(aShape2);
        SegList shape3 = new SegList();

        // Split segments in shapes so each contains endpoint at every crossing
        shape1.splitSegments(shape2);

        // Find first segement of shape1 outside shape2
        Segment seg = shape1.getFirstSegOutside(shape2);
        if (seg == null) { System.err.println("SegList.add: No intersections!"); return aShape1; } // Should never happen

        // Iterate over segements to find those outside other shape and add to new shape
        while (seg != null) {

            // Add segment to new shape
            shape3.addSeg(seg);

            // Search SegLists for next outside segment
            Segment nextSeg = getNextSegOutside(shape1, shape2, shape3, seg);

            // If not found, swap order to search second seglist
            if (nextSeg == null) {
                SegList swap = shape1; shape1 = shape2; shape2 = swap;
                nextSeg = getNextSegOutside(shape1, shape2, shape3, seg);
            }

            // Update seg and add to list if non-null
            seg = nextSeg;

            // Check to see if things are out of hand (should probably go)
            if (shape3.getSegCount() > 50) {
                seg = null; System.err.println("SegList: too many segs"); }
        }

        // Return path for segments list
        return shape3.createPath();
    }

    /**
     * Returns the area of the first shape minus the overlapping area of second shape.
     */
    public static Shape subtractShapes(Shape aShape1, Shape aShape2)
    {
        // Simple case: If shapes don't intersect, return shape 1 (should be null)
        if (!aShape1.intersects(aShape2))
            return aShape1;

        // If either shape contains the other, return concatenated shape
        if (aShape1.contains(aShape2)) {
            Path path = new Path(aShape1); path.appendShape(aShape2); return path; }
        if (aShape2.contains(aShape1)) {
            Path path = new Path(aShape2); path.appendShape(aShape1); return path; }

        // Create SegLists for given shapes and new shape
        SegList shape1 = new SegList(aShape1), refShp = shape1;
        SegList shape2 = new SegList(aShape2);
        SegList shape3 = new SegList();

        // Split segments in shapes so each contains endpoint at every crossing
        shape1.splitSegments(shape2);

        // Find first segment on perimeter of shape1 and shape2
        Segment seg = shape1.getFirstSegOutside(shape2);
        if (seg == null) {  // Should never happen
            System.err.println("SegList.subtr: No intersections!"); return aShape1;
        }

        // Iterate over segments to find those with endpoints in opposing shape and add to new shape
        while (seg != null) {

            // Add segment to new list
            shape3.addSeg(seg);

            // Search SegLists for next subtract segment
            Segment nextSeg = getNextSegSubtract(refShp, shape1, shape2, shape3, seg);

            // If not found, swap order to search second seglist
            if (nextSeg == null) {
                SegList swap = shape1; shape1 = shape2; shape2 = swap;
                nextSeg = getNextSegSubtract(refShp, shape1, shape2, shape3, seg);
            }

            // Update seg and add to list if non-null
            seg = nextSeg;

            // Check to see if things are out of hand (should probably go)
            if (shape3.getSegCount() > 50) {
                seg = null; System.err.println("SegList: too many segs"); }
        }

        // Return path for segments list
        return shape3.createPath();
    }

    /**
     * Returns the intersection shape of two shapes.
     */
    public static Shape intersectShapes(Shape aShape1, Shape aShape2)
    {
        // Simple cases
        if (aShape1 instanceof Rect && aShape2 instanceof Rect)
            return ((Rect)aShape1).getIntersectRect((Rect)aShape2);
        if (!aShape1.intersects(aShape2))
            return new Rect();
        if (aShape1.contains(aShape2))
            return aShape2;
        if (aShape2.contains(aShape1))
            return aShape1;

        // Create SegLists for given shapes and new shape
        SegList shape1 = new SegList(aShape1);
        SegList shape2 = new SegList(aShape2);
        SegList shape3 = new SegList();

        // Split segments in shape1 & shape2
        shape1.splitSegments(shape2);

        // Find first segment contained by both shape1 and shape2
        Segment seg = shape1.getFirstSegInside(shape2);
        if (seg == null) { System.err.println("SegList.intersect: No points!"); return aShape1; } // Should never happen

        // Iterate over segments to find those with endpoints in opposing shape and add to new shape
        SegList owner = shape1;
        SegList opposingShape = shape2; //if(seg==null) { seg = shape2.getSeg(0); owner = shape2; opp = shape1; }
        while (seg != null) {

            // Add segment to new list
            shape3.addSeg(seg);

            // Get segment at end point for current seg shape
            List <Segment> segs = owner.getSegmentsThatStartOrEndAtSegmentEndPoint(seg);
            Segment nextSeg = null;
            for (Segment sg : segs) {
                if (opposingShape.contains(sg.getX1(), sg.getY1()) && !shape3.hasSeg(sg)) {
                    nextSeg = sg; break; }
            }

            // If not found, look for seg from other shape
            if (nextSeg == null) {
                segs = opposingShape.getSegmentsThatStartOrEndAtSegmentEndPoint(seg);
                for (Segment sg : segs) {
                    if (owner.contains(sg.getX1(), sg.getY1()) && !shape3.hasSeg(sg)) {
                        nextSeg = sg;
                        owner = opposingShape; opposingShape = opposingShape==shape1? shape2 : shape1;
                        break;
                    }
                }
            }

            // Update seg and add to list if non-null
            seg = nextSeg;

            // Check to see if things are out of hand (should probably go)
            if (shape3.getSegCount() > 30) {
                seg = null; System.err.println("SegList: too many segs"); }
        }

        // Return path for segments list
        return shape3.createPath();
    }

    /**
     * Returns the next segment outside of both SegLists.
     */
    private static Segment getNextSegOutside(SegList aShp1, SegList aShp2, SegList aShp3, Segment aSeg)
    {
        List <Segment> segs = aShp1.getSegmentsThatStartOrEndAtSegmentEndPoint(aSeg);
        for (Segment seg : segs) {
            boolean outside = !aShp2.containsSegMid(seg) || aShp2.hasSeg(seg);
            if (outside && !aShp3.hasSeg(seg))
                return seg;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the next segment outside of both SegLists.
     */
    private static Segment getNextSegSubtract(SegList aRefShp, SegList aShp1, SegList aShp2, SegList aShp3, Segment aSeg)
    {
        List<Segment> segs = aShp1.getSegmentsThatStartOrEndAtSegmentEndPoint(aSeg);
        for (Segment seg : segs) {
            boolean b1 = (aShp1 == aRefShp) != aShp2.containsSegMid(seg);
            if (b1 && !aShp3.hasSeg(seg))
                return seg;
        }

        // Return not found
        return null;
    }

    // Helpers
    private static boolean eq(double v1, double v2)  { return Segment.equals(v1,v2); }
    private static List<Segment> add(List<Segment> aList, Segment aShp)
    { if (aList == Collections.EMPTY_LIST) aList = new ArrayList<>(); aList.add(aShp); return aList; }

    /**
     * A PathIter for SegList.
     */
    private static class SegListIter extends PathIter {

        // Ivars
        private Segment[]  _segs;
        private int  _index;
        private double _moveX, _moveY;
        private double _lineX, _lineY;

        /** Constructor. */
        SegListIter(SegList aSL, Transform aTrans)
        {
            super(aTrans);
            _segs = aSL.getSegs().toArray(new Segment[0]);
        }

        /** Returns whether this iter has another segement. */
        public boolean hasNext()  { return _index < _segs.length; }

        /** Returns the next segment. */
        public Seg getNext(double[] coords)
        {
            Segment seg = _segs[_index];

            // If last end point was last move point, add moveTo
            if (_lineX == _moveX && _lineY == _moveY) {
                _lineX += .001;
                return moveTo(_moveX = seg.x0, _moveY = seg.y0, coords);
            }
            _index++;

            // Handle Seg Line
            if (seg instanceof Line) {
                Line line = (Line) seg;
                if (_moveX == line.x1 && _moveY == line.y1)
                    return close();
                return lineTo(_lineX =line.x1, _lineY =line.y1, coords);
            }

            // Handle Seg Quad
            if (seg instanceof Quad) {
                Quad quad = (Quad) seg;
                return quadTo(quad.cpx, quad.cpy, _lineX =quad.x1, _lineY =quad.y1, coords);
            }

            // Handle Seg Cubic)
            Cubic cubic = (Cubic) seg;
            return cubicTo(cubic.cp0x, cubic.cp0y, cubic.cp1x, cubic.cp1y, _lineX = cubic.x1, _lineY = cubic.y1, coords);
        }
    }
}