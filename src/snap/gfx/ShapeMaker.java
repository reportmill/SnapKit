package snap.gfx;
import java.util.*;
import snap.util.*;

/**
 * A Shape helper class to construct shapes from other shapes.
 */
public class ShapeMaker {

/**
 * Returns the intersection shape of two shapes.
 */
public static Shape intersect(Shape aShape1, Shape aShape2)
{
    List <Segment> slist1 = getShapeList(aShape1);
    List <Segment> slist2 = getShapeList(aShape2);
    List <Segment> slist3 = new ArrayList();
    
    // Iterate over list1 and split at all intersections with slist2
    for(int i=0;i<slist1.size();i++) { Segment shp1 = slist1.get(i);
        for(int j=0;j<slist2.size();j++) { Segment shp2 = slist2.get(j);
            if(shp1.intersects(shp2)) {
                double hp1 = shp1.getHitPoint(shp2);
                if(MathUtils.equalsZero(hp1) || MathUtils.equals(hp1,1)) continue;
                Segment shp1b = shp1.split(hp1);
                slist1.add(i+1,shp1b);
                double hp2 = shp2.getHitPoint(shp1);
                Segment shp2b = shp2.split(hp2);
                slist2.add(j+1,shp2b);
            }
        }
    }
    
    // Skip beginning segments not contained by shape2
    int i = 0;
    for(i=0;i<slist1.size(); i++) { Segment seg = slist1.get(i);
        if(contains(aShape2, slist2, seg.getX0(), seg.getY0())) break; }
    
    // Iterate over slist1 util we find point that intersects or is contained by shape 2
    List <Segment> owner = slist1, opp = slist2;
    Segment seg = null;
    if(i<slist1.size())
        seg = slist1.get(i);
    else { seg = slist2.get(0); owner = slist2; opp = slist1; }
    
    // 
    while(seg!=null) {
        
        // Add segment to new list
        slist3.add(seg); //System.err.println("Add Seg " + seg);
    
        // Get segment at end point for current seg shape
        List <Segment> segs = getSegments(seg, owner);
        Segment nextSeg = null;
        for(Segment sg : segs) {
            if(contains(opp==slist1? aShape1 : aShape2, opp, sg.getX1(), sg.getY1()) && !contains(slist3, sg)) {
                nextSeg = sg; break; }
        }
        
        // If not found, look for seg from other shape
        if(nextSeg==null) {
            segs = getSegments(seg, opp);
            for(Segment sg : segs) {
                if(contains(owner==slist1? aShape1 : aShape2, owner, sg.getX1(), sg.getY1()) && !contains(slist3,sg)) {
                    nextSeg = sg; owner = opp; opp = opp==slist1? slist2 : slist1; break; }
            }
        }
        
        // Update seg and add to list if non-null
        seg = nextSeg;
        if(slist3.size()>10) seg = null;
    }
    
    // Return new shape for segments list
    return getShape(slist3);
}

/**
 * Returns the segements from list for end point of given seg.
 */
public static List <Segment> getSegments(Segment aSeg, List <Segment> theSegs)
{
    double x = aSeg.getX1(), y = aSeg.getY1();
    List <Segment> segs = Collections.EMPTY_LIST;
    
    for(Segment seg : theSegs) {
        if(seg.equals(aSeg)) continue;
        if(eq(x,seg.getX0()) && eq(y,seg.getY0()))
            segs = add(segs, seg);
        if(eq(x,seg.getX1()) && eq(y,seg.getY1()))
            segs = add(segs, seg.createReverse());
    }
    
    return segs;
}

/**
 * Returns whether given seg list contains given point.
 */
private static boolean contains(Shape aShape, List <Segment> theSegs, double x, double y)
{
    for(Shape seg : theSegs)
        if(contains(seg,x,y))
            return true;
    return aShape.contains(x,y);
}

/**
 * Returns whether given seg list contains given point.
 */
private static boolean contains(Shape aSeg, double x, double y)
{
    if(aSeg instanceof Line) { Line s = (Line)aSeg;
        return eq(x,s.x1) && eq(y,s.y1); }
    else if(aSeg instanceof Quad) { Quad s = (Quad)aSeg;
        return eq(x,s.x1) && eq(y,s.y1); }
    else if(aSeg instanceof Cubic) { Cubic s = (Cubic)aSeg;
        return eq(x,s.x1) && eq(y,s.y1); }
    throw new RuntimeException("ShapeMaker: Unsupported Seg class " + aSeg.getClass());
}

private static boolean eq(double v1, double v2)  { return MathUtils.equals(v1,v2); }

/**
 * Returns whether list contains segement (regardless of direction).
 */
private static boolean contains(List <Segment> theSegs, Segment aSeg)
{
    for(Segment seg : theSegs)
        if(seg.matches(aSeg))
            return true;
    return false;
}

private static List <Segment> add(List <Segment> aList, Segment aShp)
{
    if(aList==Collections.EMPTY_LIST) aList = new ArrayList();
    aList.add(aShp); return aList;
}

/**
 * Creates a list of primitive shapes from a shape.
 */
public static List <Segment> getShapeList(Shape aShape)
{
    // Iterate over segments, if any segment intersects cubic, return true
    List <Segment> shapes = new ArrayList();
    PathIter pi = aShape.getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0, mx = 0, my = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = mx = pts[0]; ly = my = pts[1]; break;
            case LineTo: shapes.add(new Line(lx,ly,lx=pts[0],ly=pts[1])); break;
            case QuadTo: shapes.add(new Quad(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3])); break;
            case CubicTo: shapes.add(new Cubic(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5])); break;
            case Close: if(lx!=mx || ly!=my) shapes.add(new Line(lx,ly,lx=mx,ly=my)); break;
        }
    }
    
    // Return false since line hits no segments
    return shapes;
}

/**
 * Creates a list of primitive shapes from a shape.
 */
public static Shape getShape(List <Segment> slist)
{
    // Iterate over segments, if any segment intersects cubic, return true
    Path path = new Path(); double lx = 0, ly = 0, mx = 0, my = 0;
    for(Shape seg : slist) {
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

public static void main(String args[])
{
    //Shape r1 = new Rect(0,0,200,200), r2 = new Rect(100,100,200,200);
    Shape r1 = new Ellipse(0,0,200,200), r2 = new Ellipse(100,100,200,200);
    Shape r3 = intersect(r1,r2);
    System.err.println("Rect 3: " + r3);
    Image img = Image.get(500,500, false);
    Painter pntr = img.getPainter(); pntr.draw(r1); pntr.draw(r2); pntr.setColor(Color.PINK); pntr.fill(r3);
    SnapUtils.writeBytes(img.getBytesJPEG(), "/tmp/test.jpg");
}

}