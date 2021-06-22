package snap.geom;
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
     * Creates a new SegList from given shape.
     */
    public SegList(Shape aShape)
    {
        _shape = aShape; if (_shape == null) return;

        // Iterate over segments, if any segment intersects cubic, return true
        PathIter pi = aShape.getPathIter(null);
        double[] pts = new double[6];
        double lx = 0, ly = 0;
        double mx = 0, my = 0;
        while (pi.hasNext()) {
            switch (pi.getNext(pts)) {
                case MoveTo: lx = mx = pts[0]; ly = my = pts[1]; break;
                case LineTo: _segs.add(new Line(lx,ly,lx=pts[0],ly=pts[1])); break;
                case QuadTo: _segs.add(new Quad(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3])); break;
                case CubicTo: _segs.add(new Cubic(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5])); break;
                case Close: if (lx != mx || ly != my) _segs.add(new Line(lx,ly,lx=mx,ly=my)); break;
            }
        }
    }

    /**
     * Returns the list of segements.
     */
    public List <Segment> getSegs()  { return _segs; }

    /**
     * Returns the number of segements.
     */
    public int getSegCount()  { return _segs.size(); }

    /**
     * Returns the individual segment at given index.
     */
    public Segment getSeg(int anIndex)  { return _segs.get(anIndex); }

    /**
     * Adds the given segement.
     */
    public void addSeg(Segment aSeg)  { addSeg(aSeg, getSegCount()); }

    /**
     * Adds the given segement.
     */
    public void addSeg(Segment aSeg, int anIndex)  { _segs.add(anIndex, aSeg); }

    /**
     * Removes a given segment.
     */
    public Segment removeSeg(int anIndex)  { return _segs.remove(anIndex); }

    /**
     * Returns whether this SegList contains given point.
     */
    public boolean contains(double x, double y)
    {
        boolean c1 = containsEndPoint(x, y);
        return c1 || _shape.contains(x,y);
    }

    /**
     * Returns whether this SegList contains given point.
     */
    public boolean containsSegMid(Segment aSeg)
    {
        double x = aSeg.getX(.5), y = aSeg.getY(.5);
        return contains(x, y);

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
     * Returns whether segement list contains segement (regardless of direction).
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
    public Segment getFirstSegOutside(SegList aShape)
    {
        for (Segment sg : getSegs())
            if (!aShape.containsSegMid(sg))
                return sg;
        return null;
    }

    /**
     * Returns the first Segment from this SegmentList outside of given SegList.
     */
    public Segment getFirstSegInside(SegList aShape)
    {
        for (Segment sg : getSegs())
            if (aShape.contains(sg.getX0(), sg.getY0()))
                return sg;
        return null;
    }

    /**
     * Returns the segements from list for end point of given seg.
     */
    public List <Segment> getSegments(Segment aSeg)
    {
        double x = aSeg.getX1(), y = aSeg.getY1();
        List <Segment> segs = Collections.EMPTY_LIST;

        for (Segment seg : _segs) {
            if (seg.equals(aSeg))
                continue;
            if (eq(x, seg.getX0()) && eq(y, seg.getY0()))
                segs = add(segs, seg);
            if (eq(x, seg.getX1()) && eq(y, seg.getY1()))
                segs = add(segs, seg.createReverse());
        }

        return segs;
    }

    /**
     * Creates the path from the list of segements.
     */
    public Path createPath()
    {
        // Iterate vars
        Path path = new Path(); double lx = 0, ly = 0, mx = 0, my = 0;

        // Iterate over segments, if any segment intersects cubic, return true
        for (Segment seg : _segs) {

            // If last end point was last move point, add moveTo
            if (lx == mx && ly == my)
                path.moveTo(mx = seg.x0, my = seg.y0);

            // Handle Seg (Line, Quad, Cubic)
            if (seg instanceof Line) { Line line = (Line) seg;
                if (mx == line.x1 && my == line.y1)
                    path.close();
                else path.lineTo(lx = line.x1, ly = line.y1);
            }
            else if (seg instanceof Quad) { Quad quad = (Quad) seg;
                path.quadTo(quad.cpx, quad.cpy, lx = quad.x1, ly = quad.y1);
            }
            else if (seg instanceof Cubic) { Cubic cubic = (Cubic) seg;
                path.curveTo(cubic.cp0x, cubic.cp0y, cubic.cp1x, cubic.cp1y, lx = cubic.x1, ly = cubic.y1);
            }
        }

        // Return new path
        return path;
    }

    /**
     * Creates an array of paths from the list of segements.
     */
    public Path[] createPaths()
    {
        // If single path, just create and return it
        if (!isMultiPath())
            return new Path[] { createPath() };

        // Iterate vars
        List <Path> paths = new ArrayList();
        Path path = null; double lx = 0, ly = 0, mx = 0, my = 0;

        // Iterate over segments, if any segment intersects cubic, return true
        for (Segment seg : _segs) {

            // If last end point was last move point, start new path
            if (lx == mx && ly == my) {
                paths.add(path = new Path());
                path.moveTo(mx = seg.x0, my = seg.y0);
            }

            // Handle Seg (Line, Quad, Cubic)
            if (seg instanceof Line) { Line line = (Line)seg;
                if (mx == line.x1 && my == line.y1)
                    path.close();
                else path.lineTo(lx = line.x1, ly = line.y1);
            }
            else if (seg instanceof Quad) { Quad quad = (Quad) seg;
                path.quadTo(quad.cpx, quad.cpy, lx = quad.x1, ly = quad.y1);
            }
            else if (seg instanceof Cubic) { Cubic cubic = (Cubic) seg;
                path.curveTo(cubic.cp0x, cubic.cp0y, cubic.cp1x, cubic.cp1y, lx = cubic.x1, ly = cubic.y1);
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
        // Iterate over list1 and split at all intersections with slist2
        int sc = getSegCount(); boolean didSplit = false;
        for (int i=0; i<sc; i++) { Segment seg1 = getSeg(i);
            for (int j=i+1; j<sc; j++) { Segment seg2 = getSeg(j);

                // If segments intersect
                if (seg1.intersectsSeg(seg2)) {

                    // Find intersection point for seg2 and split/add if inside
                    double hp2 = seg2.getHitPoint(seg1);
                    if (hp2 > .001 && hp2 < .999) {
                        Segment tail = seg2.split(hp2);
                        addSeg(tail, j+1); sc++;
                        didSplit = true;
                    }

                    // Find intersection point for seg1 and split/add if inside
                    double hp1 = seg1.getHitPoint(seg2);
                    if (hp1 > .001 && hp1 < .999) {
                        Segment tail = seg1.split(hp1);
                        addSeg(tail, i+1); sc++;
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
        int sc = getSegCount();
        for (int i=0; i<sc; i++) { Segment seg1 = getSeg(i);
            for (int j=i+1; j<sc; j++) { Segment seg2 = getSeg(j);

                // If segments intersect somewhere not on an end-point, return false
                if (seg1.intersectsSeg(seg2)) {
                    double hp2 = seg2.getHitPoint(seg1);
                    if (hp2 > .001 && hp2 < .999)
                        return false;
                    double hp1 = seg1.getHitPoint(seg2);
                    if (hp1 > .001 && hp1 < .999)
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
        boolean didSplit = splitSegments(); if (!didSplit) return false;

        // Create list of segments to be added at end
        List <Segment> subsegs = new ArrayList();
        int sc = getSegCount();

        // Extract subpath segments
        for (int i=0; i<sc; i++) { Segment seg0 = getSeg(i);
            for (int j=0; j<i; j++) { Segment seg1 = getSeg(j);

                // If primary segment endpoint equals second segment start point, move segments in range out to subsegs list
                if (Point.equals(seg0.getX1(), seg0.getY1(), seg1.getX0(), seg1.getY0())) {
                    for (;i>=j;i--)
                        subsegs.add(removeSeg(j));
                    i = j-1;
                    sc = getSegCount();
                    break;
                }
            }
        }

        // Add subsegs to end
        for (Segment seg : subsegs)
            addSeg(seg);
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
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)  { return new SegListIter(this, aTrans); }

    /**
     * Returns the intersection shape of two shapes.
     */
    public static Shape intersect(Shape aShape1, Shape aShape2)
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
        SegList shape3 = new SegList(null);

        // Split segments in shape1 & shape2
        shape1.splitSegments(shape2);

        // Find first segement contained by both shape1 and shape2
        Segment seg = shape1.getFirstSegInside(shape2);
        if (seg == null) { System.err.println("SegList.intersect: No points!"); return aShape1; } // Should never happen

        // Iterate over segements to find those with endpoints in opposing shape and add to new shape
        SegList owner = shape1;
        SegList opp = shape2; //if(seg==null) { seg = shape2.getSeg(0); owner = shape2; opp = shape1; }
        while (seg != null) {

            // Add segment to new list
            shape3.addSeg(seg);

            // Get segment at end point for current seg shape
            List <Segment> segs = owner.getSegments(seg);
            Segment nextSeg = null;
            for (Segment sg : segs) {
                if (opp.contains(sg.getX1(), sg.getY1()) && !shape3.hasSeg(sg)) {
                    nextSeg = sg; break; }
            }

            // If not found, look for seg from other shape
            if (nextSeg == null) {
                segs = opp.getSegments(seg);
                for (Segment sg : segs) {
                    if (owner.contains(sg.getX1(), sg.getY1()) && !shape3.hasSeg(sg)) {
                        nextSeg = sg; owner = opp; opp = opp==shape1? shape2 : shape1; break; }
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
     * Returns the intersection shape of two shapes.
     */
    public static Shape add(Shape aShape1, Shape aShape2)
    {
        // Simple case: If shapes don't intersect, just add them and return
        if (!aShape1.intersects(aShape2)) {
            Path path = new Path(aShape1);
            path.append(aShape2);
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
        SegList shape3 = new SegList(null);

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
     * Returns the next segment outside of both SegLists.
     */
    private static Segment getNextSegOutside(SegList aShp1, SegList aShp2, SegList aShp3, Segment aSeg)
    {
        List <Segment> segs = aShp1.getSegments(aSeg);
        for (Segment sg : segs) {
            boolean outside = !aShp2.containsSegMid(sg) || aShp2.hasSeg(sg);
            if (outside && !aShp3.hasSeg(sg))
                return sg;
        }
        return null;
    }

    /**
     * Returns the area of the first shape minus the overlapping area of second shape.
     */
    public static Shape subtract(Shape aShape1, Shape aShape2)
    {
        // Simple case: If shapes don't intersect, return shape 1 (should be null)
        if (!aShape1.intersects(aShape2))
            return aShape1;

        // If either shape contains the other, return concatenated shape
        if (aShape1.contains(aShape2)) {
            Path path = new Path(aShape1); path.append(aShape2); return path; }
        if (aShape2.contains(aShape1)) {
            Path path = new Path(aShape2); path.append(aShape1); return path; }

        // Create SegLists for given shapes and new shape
        SegList shape1 = new SegList(aShape1), refShp = shape1;
        SegList shape2 = new SegList(aShape2);
        SegList shape3 = new SegList(null);

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
     * Returns the next segment outside of both SegLists.
     */
    private static Segment getNextSegSubtract(SegList aRefShp, SegList aShp1, SegList aShp2, SegList aShp3, Segment aSeg)
    {
        List<Segment> segs = aShp1.getSegments(aSeg);
        for (Segment sg : segs) {
            boolean b1 = (aShp1 == aRefShp) != aShp2.containsSegMid(sg);
            if (b1 && !aShp3.hasSeg(sg))
                return sg;
        }
        return null;
    }

    /**
     * Returns a simple shape for complex shape.
     */
    public static Shape makeSimple(Shape aShape)
    {
        SegList slist = new SegList(aShape);
        slist.makeSimple();
        return slist;
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
        private double  _lx, _ly, _mx, _my;

        /** Constructor. */
        SegListIter(SegList aSL, Transform aTrans)
        {
            super(aTrans);
            _segs = aSL._segs.toArray(new Segment[0]);
        }

        /** Returns whether this iter has another segement. */
        public boolean hasNext()  { return _index < _segs.length; }

        /** Returns the next segment. */
        public Seg getNext(double[] coords)
        {
            Segment seg = _segs[_index];

            // If last end point was last move point, add moveTo
            if (_lx == _mx && _ly == _my) {
                _lx += .001;
                return moveTo(_mx = seg.x0, _my = seg.y0, coords);
            }
            _index++;

            // Handle Seg Line
            if (seg instanceof Line) {
                Line line = (Line) seg;
                if (_mx == line.x1 && _my == line.y1)
                    return close();
                return lineTo(_lx=line.x1, _ly=line.y1, coords);
            }

            // Handle Seg Quad
            if (seg instanceof Quad) {
                Quad quad = (Quad) seg;
                return quadTo(quad.cpx, quad.cpy, _lx=quad.x1, _ly=quad.y1, coords);
            }

            // Handle Seg Cubic)
            Cubic cubic = (Cubic) seg;
            return cubicTo(cubic.cp0x, cubic.cp0y, cubic.cp1x, cubic.cp1y, _lx = cubic.x1, _ly = cubic.y1, coords);
        }
    }
}