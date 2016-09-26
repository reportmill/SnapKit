package snap.gfx;
import java.util.*;
import snap.util.SnapUtils;

/**
 * A Shape helper class to construct shapes from other shapes.
 */
public class ShapeMaker {
    
    // The original shape
    Shape            _shape;
    
    // The list of segements
    List <Segment>   _segs = new ArrayList();

/**
 * Creates a new ShapeMaker from given shape.
 */
public ShapeMaker(Shape aShape)
{
    _shape = aShape; if(_shape==null) return;
    
    // Iterate over segments, if any segment intersects cubic, return true
    PathIter pi = aShape.getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0, mx = 0, my = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = mx = pts[0]; ly = my = pts[1]; break;
            case LineTo: _segs.add(new Line(lx,ly,lx=pts[0],ly=pts[1])); break;
            case QuadTo: _segs.add(new Quad(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3])); break;
            case CubicTo: _segs.add(new Cubic(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5])); break;
            case Close: if(lx!=mx || ly!=my) _segs.add(new Line(lx,ly,lx=mx,ly=my)); break;
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
 * Returns whether given seg list contains given point.
 */
public boolean contains(double x, double y)
{
    boolean c1 = containsEndPoint(x, y);
    return c1 || _shape.contains(x,y);
}

/**
 * Returns whether given seg list contains given point.
 */
public boolean containsEndPoint(double x, double y)
{
    for(Segment seg : _segs)
        if(eq(seg.x1,x) && eq(seg.y1,y))
            return true;
    return false;
}

/**
 * Returns whether segement list contains segement (regardless of direction).
 */
public boolean contains(Segment aSeg)
{
    for(Segment seg : _segs)
        if(seg.matches(aSeg))
            return true;
    return false;
}

/**
 * Returns the segements from list for end point of given seg.
 */
public List <Segment> getSegments(Segment aSeg)
{
    double x = aSeg.getX1(), y = aSeg.getY1();
    List <Segment> segs = Collections.EMPTY_LIST;
    
    for(Segment seg : _segs) {
        if(seg.equals(aSeg)) continue;
        if(eq(x,seg.getX0()) && eq(y,seg.getY0()))
            segs = add(segs, seg);
        if(eq(x,seg.getX1()) && eq(y,seg.getY1()))
            segs = add(segs, seg.createReverse());
    }
    
    return segs;
}

/**
 * Creates the shape from the list of segements.
 */
public Shape createShape()
{
    // Iterate over segments, if any segment intersects cubic, return true
    Path path = new Path(); double lx = 0, ly = 0, mx = 0, my = 0;
    for(Shape seg : _segs) {
        if(seg instanceof Line) { Line line = (Line)seg;
            if(lx==mx && ly==my) path.moveTo(mx=line.x0, my=line.y0);
            if(mx==line.x1 && my==line.y1) path.close();
            else path.lineTo(lx=line.x1, ly=line.y1);
        }
        else if(seg instanceof Quad) { Quad quad = (Quad)seg;
            if(lx==mx && ly==my) path.moveTo(mx=quad.x0, my=quad.y0);
            path.quadTo(quad.xc0, quad.yc0, lx=quad.x1, ly=quad.y1);
        }
        else if(seg instanceof Cubic) { Cubic cubic = (Cubic)seg;
            if(lx==mx && ly==my) path.moveTo(mx=cubic.x0, my=cubic.y0);
            path.curveTo(cubic.xc0, cubic.yc0, cubic.xc1, cubic.yc1, lx=cubic.x1, ly=cubic.y1);
        }
    }
        
    // Return new path
    return path;
}

/**
 * Splits the segements for given shape.
 */
public void splitSegments(ShapeMaker shape2)
{
    // Iterate over list1 and split at all intersections with slist2
    for(int i=0;i<getSegCount();i++) { Segment shp1 = getSeg(i);
        for(int j=0;j<shape2.getSegCount();j++) { Segment shp2 = shape2.getSeg(j);
            if(shp1.intersects(shp2)) {
                double hp1 = shp1.getHitPoint(shp2);
                if(Segment.equals(hp1,0) || Segment.equals(hp1,1)) continue;
                double hp2 = shp2.getHitPoint(shp1);
                Segment shp1b = shp1.split(hp1);
                Segment shp2b = shp2.split(hp2);
                addSeg(shp1b, i+1);
                shape2.addSeg(shp2b, j+1);
            }
        }
    }
}

/**
 * Returns the intersection shape of two shapes.
 */
public static Shape intersect(Shape aShape1, Shape aShape2)
{
    // Simple cases
    if(aShape1 instanceof Rect && aShape2 instanceof Rect)
        return ((Rect)aShape1).getIntersectRect((Rect)aShape2);
    if(!aShape1.intersects(aShape2))
        return new Rect();
    if(aShape1.contains(aShape2))
        return aShape2;
    if(aShape2.contains(aShape1))
        return aShape1;

    // Create ShapeMakers for given shapes and new shape
    ShapeMaker shape1 = new ShapeMaker(aShape1);
    ShapeMaker shape2 = new ShapeMaker(aShape2);
    ShapeMaker shape3 = new ShapeMaker(null);
    
    // Split segments in shape1 & shape2
    shape1.splitSegments(shape2);
    
    // Find first segement contained by both shape1 and shape2
    Segment seg = null;
    for(int i=0,iMax=shape1.getSegCount(); i<iMax && seg==null; i++) { Segment sg = shape1.getSeg(i);
        if(shape2.contains(sg.getX0(), sg.getY0())) seg = sg; }
    if(seg==null) { System.err.println("ShapeMaker.intersect: No points!"); return aShape1; } // Should never happen
        
    // Iterate over segements to find those with endpoints in opposing shape and add to new shape
    ShapeMaker owner = shape1, opp = shape2; //if(seg==null) { seg = shape2.getSeg(0); owner = shape2; opp = shape1; }
    while(seg!=null) {
        
        // Add segment to new list
        shape3.addSeg(seg); //System.err.println("Add Seg " + seg);
    
        // Get segment at end point for current seg shape
        List <Segment> segs = owner.getSegments(seg);
        Segment nextSeg = null;
        for(Segment sg : segs) {
            if(opp.contains(sg.getX1(), sg.getY1()) && !shape3.contains(sg)) {
                nextSeg = sg; break; }
        }
        
        // If not found, look for seg from other shape
        if(nextSeg==null) {
            segs = opp.getSegments(seg);
            for(Segment sg : segs) {
                if(owner.contains(sg.getX1(), sg.getY1()) && !shape3.contains(sg)) {
                    nextSeg = sg; owner = opp; opp = opp==shape1? shape2 : shape1; break; }
            }
        }
        
        // Update seg and add to list if non-null
        seg = nextSeg; if(shape3.getSegCount()>30) { seg = null; System.err.println("ShapeMaker: too many segs"); }
    }
    
    // Return new shape for segments list
    return shape3.createShape();
}

/**
 * Returns the intersection shape of two shapes.
 */
public static Shape add(Shape aShape1, Shape aShape2)
{
    // Simple cases
    if(!aShape1.intersects(aShape2)) { Path path = new Path(aShape1);
        path.append(aShape2); return path; }
    if(aShape1.contains(aShape2))
        return aShape1;
    if(aShape2.contains(aShape1))
        return aShape2;

    // Create ShapeMakers for given shapes and new shape
    ShapeMaker shape1 = new ShapeMaker(aShape1);
    ShapeMaker shape2 = new ShapeMaker(aShape2);
    ShapeMaker shape3 = new ShapeMaker(null);
    
    // Split segments in shape1 & shape2
    shape1.splitSegments(shape2);
    
    // Find first segement on perimeter of shape1 and shape2
    Segment seg = null;
    for(int i=0,iMax=shape1.getSegCount(); i<iMax && seg==null; i++) { Segment sg = shape1.getSeg(i);
        double x = sg.getX(.5), y = sg.getY(.5);
        if(!shape2.contains(x,y)) seg = sg; }
    if(seg==null) { System.err.println("ShapeMaker.add: No intersections!"); return aShape1; } // Should never happen
    
    // Iterate over segements to find those with endpoints in opposing shape and add to new shape
    ShapeMaker owner = shape1, opp = shape2; //if(seg==null) { seg = shape2.getSeg(0); owner = shape2; opp = shape1; }
    while(seg!=null) {
        
        // Add segment to new list
        shape3.addSeg(seg); //System.err.println("Add Seg " + seg);
    
        // Get segment at end point for current seg shape
        List <Segment> segs = owner.getSegments(seg);
        Segment nextSeg = null;
        for(Segment sg : segs) { double x = sg.getX(.5), y = sg.getY(.5);
            if(!opp.contains(x, y) && !shape3.contains(sg)) {
                nextSeg = sg; break; }
        }
        
        // If not found, look for seg from other shape
        if(nextSeg==null) {
            segs = opp.getSegments(seg);
            for(Segment sg : segs) { double x = sg.getX(.5), y = sg.getY(.5);
                if(!owner.contains(x,y) && !shape3.contains(sg)) {
                    nextSeg = sg; owner = opp; opp = opp==shape1? shape2 : shape1; break; }
            }
        }
        
        // Update seg and add to list if non-null
        seg = nextSeg; if(shape3.getSegCount()>30) { seg = null; System.err.println("ShapeMaker: too many segs"); }
    }
    
    // Return new shape for segments list
    return shape3.createShape();
}

/**
 * Returns the area of the first shape minus the overlapping area of second shape.
 */
public static Shape subtract(Shape aShape1, Shape aShape2)
{
    // Simple cases
    if(!aShape1.intersects(aShape2) || aShape2.contains(aShape1))
        return aShape1;
    if(aShape1.contains(aShape2)) {
        Path path = new Path(aShape1); path.append(aShape2); return path; }

    // Create ShapeMakers for given shapes and new shape
    ShapeMaker shape1 = new ShapeMaker(aShape1);
    ShapeMaker shape2 = new ShapeMaker(aShape2);
    ShapeMaker shape3 = new ShapeMaker(null);
    
    // Split segments in shape1 & shape2
    shape1.splitSegments(shape2);
    
    // Find first segement on perimeter of shape1 and shape2
    Segment seg = null;
    for(int i=0,iMax=shape1.getSegCount(); i<iMax && seg==null; i++) { Segment sg = shape1.getSeg(i);
        double x = sg.getX(.5), y = sg.getY(.5);
        if(!shape2.contains(x,y)) seg = sg; }
    if(seg==null) { System.err.println("ShapeMaker.add: No intersections!"); return aShape1; } // Should never happen
    
    // Iterate over segements to find those with endpoints in opposing shape and add to new shape
    ShapeMaker owner = shape1, opp = shape2; //if(seg==null) { seg = shape2.getSeg(0); owner = shape2; opp = shape1; }
    while(seg!=null) {
        
        // Add segment to new list
        shape3.addSeg(seg); //System.err.println("Add Seg " + seg);
    
        // Get segment at end point for current seg shape
        List <Segment> segs = owner.getSegments(seg);
        Segment nextSeg = null;
        for(Segment sg : segs) { double x = sg.getX(.5), y = sg.getY(.5);
            boolean b1 = owner==shape1? !shape2.contains(x,y) : shape1.contains(x,y);
            if(b1 && !shape3.contains(sg)) {
                nextSeg = sg; break; }
        }
        
        // If not found, look for seg from other shape
        if(nextSeg==null) {
            segs = opp.getSegments(seg);
            for(Segment sg : segs) { double x = sg.getX(.5), y = sg.getY(.5);
                boolean b1 = opp==shape1? !shape2.contains(x,y) : shape1.contains(x,y);
                if(b1 && !shape3.contains(sg)) {
                    nextSeg = sg; owner = opp; opp = opp==shape1? shape2 : shape1; break; }
            }
        }
        
        // Update seg and add to list if non-null
        seg = nextSeg; if(shape3.getSegCount()>30) { seg = null; System.err.println("ShapeMaker: too many segs"); }
    }
    
    // Return new shape for segments list
    return shape3.createShape();
}

// Helpers
private static boolean eq(double v1, double v2)  { return Segment.equals(v1,v2); }
private static List <Segment> add(List <Segment> aList, Segment aShp)
{ if(aList==Collections.EMPTY_LIST) aList = new ArrayList(); aList.add(aShp); return aList; }

/**
 * Main method for testing.
 */
public static void main(String args[])
{
    //Shape r1 = new Rect(0,0,200,200), r2 = new Path(new Rect(100,100,200,200));
    //Shape r1 = new Ellipse(0,0,200,200), r2 = new Ellipse(100,100,200,200);
    Shape r1 = new Rect(0,0,200,200), r2 = new Ellipse(125,125,200,200);
    Shape r3 = subtract(r1,r2);
    System.err.println("Rect 3: " + r3);
    Image img = Image.get(500,500, false);
    Painter pntr = img.getPainter(); pntr.draw(r1); pntr.draw(r2); pntr.setColor(Color.PINK); pntr.fill(r3);
    SnapUtils.writeBytes(img.getBytesJPEG(), "/tmp/test.jpg");
}

}