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
    List <Shape> slist1 = getShapeList(aShape1);
    List <Shape> slist2 = getShapeList(aShape2);
    List <Shape> slist3 = new ArrayList();
    
    // Iterate over list1 and split at all intersections with slist2
    for(int i=0;i<slist1.size();i++) { Shape shp1 = slist1.get(i);
        for(int j=0;j<slist2.size();j++) { Shape shp2 = slist2.get(j);
            if(shp1.intersects(shp2)) {
                double hp1 = getHitPoint(shp1,shp2);
                Shape shp1b = split(shp1, hp1);
                slist1.add(i+1,shp1b);
                double hp2 = getHitPoint(shp2,shp1);
                Shape shp2b = split(shp2,hp2);
                slist2.add(j+1,shp2b);
            }
        }
    }
    
    // Skip beginning segments not contained by shape2
    int i = 0;
    for(i=0;i<slist1.size(); i++) { Shape seg = slist1.get(i);
        if(aShape2.contains(getX0(seg), getY0(seg))) break; }
    
    // Iterate over slist1 util we find point that intersects or is contained by shape 2
    Shape owner = aShape1, opp = aShape2;
    Shape seg = null;
    if(i<slist1.size())
        seg = slist1.get(i);
    else { seg = slist2.get(0); owner = aShape2; opp = aShape1; }
    
    // 
    while(seg!=null) {
        
        // Add segment to new list
        slist3.add(seg); System.err.println("Add Seg " + seg);
    
        // Get segment at end point for current seg shape
        List <Shape> segs = getSegments(seg, owner==aShape1? slist1 : slist2);
        Shape nextSeg = null;
        for(Shape sg : segs) {
            if(opp.contains(getX1(sg), getY1(sg)) && !slist3.contains(sg)) {
                nextSeg = sg; break; }
        }
        
        // If not found, look for seg from other shape
        if(nextSeg==null) {
            segs = getSegments(seg, owner==aShape1? slist2 : slist1);
            for(Shape sg : segs) {
                if(owner.contains(getX1(sg), getY1(sg)) && !slist3.contains(sg)) {
                    nextSeg = sg; owner = opp; opp = opp==aShape1? aShape2 : aShape1; break; }
            }
        }
        
        // Update seg and add to list if non-null
        seg = nextSeg;
        if(slist3.size()>10) seg = null;
    }
    
    // Return new shape for segments list
    return getShape(slist3);
}

/** Returns end points for segment shapes. */
public static double getX0(Shape s)
{ return s instanceof Line? ((Line)s).x0 : s instanceof Quad? ((Quad)s).x0 : s instanceof Cubic? ((Cubic)s).x0 : 0; }
public static double getY0(Shape s)
{ return s instanceof Line? ((Line)s).y0 : s instanceof Quad? ((Quad)s).y0 : s instanceof Cubic? ((Cubic)s).y0 : 0; }
public static double getX1(Shape s)
{ return s instanceof Line? ((Line)s).x1 : s instanceof Quad? ((Quad)s).x1 : s instanceof Cubic? ((Cubic)s).x1 : 0; }
public static double getY1(Shape s)
{ return s instanceof Line? ((Line)s).y1 : s instanceof Quad? ((Quad)s).y1 : s instanceof Cubic? ((Cubic)s).y1 : 0; }

/**
 * Returns the segements from list for end point of given seg.
 */
public static List <Shape> getSegments(Shape aSeg, List <Shape> theSegs)
{
    double x = getX1(aSeg), y = getY1(aSeg);
    List <Shape> segs = Collections.EMPTY_LIST;
    
    for(Shape seg : theSegs) {
        if(seg.equals(aSeg)) continue;
        if(eq(x,getX0(seg)) && eq(y,getY0(seg)))
            segs = add(segs, seg);
        if(eq(x,getX1(seg)) && eq(y,getY1(seg)))
            segs = add(segs, reverse(seg));
    }
    
    return segs;
}

private static boolean eq(double v1, double v2)  { return MathUtils.equals(v1,v2); }

private static List <Shape> add(List <Shape> aList, Shape aShp)
{
    if(aList==Collections.EMPTY_LIST) aList = new ArrayList();
    aList.add(aShp); return aList;
}

private static Shape reverse(Shape aSeg)
{
    if(aSeg instanceof Line) { Line line = (Line)aSeg;
        return new Line(line.x1,line.y1,line.x0,line.y0); }
    else if(aSeg instanceof Quad) { Quad quad = (Quad)aSeg;
        return new Quad(quad.x1,quad.y1,quad.xc0, quad.yc0,quad.x0,quad.y0); }
    else if(aSeg instanceof Cubic) { Cubic cubic = (Cubic)aSeg;
        return new Cubic(cubic.x1,cubic.y1,cubic.xc1,cubic.yc1,cubic.xc0,cubic.yc0,cubic.x0,cubic.y0); }
    throw new RuntimeException("ShapeMaker: Unsupported Seg class " + aSeg.getClass());
}

/**
 * Creates a list of primitive shapes from a shape.
 */
public static List <Shape> getShapeList(Shape aShape)
{
    // Iterate over segments, if any segment intersects cubic, return true
    List <Shape> shapes = new ArrayList();
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
public static Shape getShape(List <Shape> slist)
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

/**
 * Returns the hit point for shape 1 on shape 2.
 */
public static double getHitPoint(Shape aShape1, Shape aShape2)
{
    if(aShape1 instanceof Line) { Line s1 = (Line)aShape1;
        if(aShape2 instanceof Line) { Line s2 = (Line)aShape2;
            return Line.getHitPointLine(s1.x0, s1.y0, s1.x1, s1.y1, s2.x0, s2.y0, s2.x1, s2.y1, false); }
        if(aShape2 instanceof Quad) { Quad s2 = (Quad)aShape2;
            return Quad.getHitPointLine(s2.x0, s2.y0, s2.xc0, s2.yc0, s2.x1, s2.y1, s1.x0, s1.y0, s1.x1, s1.y1, true); }
        if(aShape2 instanceof Cubic) { Cubic s2 = (Cubic)aShape2;
            return Cubic.getHitPointLine(s2.x0, s2.y0, s2.xc0, s2.yc0, s2.xc1, s2.yc1, s2.x1, s2.y1, 
                s1.x0,s1.y0,s1.x1,s1.y1,true); }
        throw new RuntimeException("ShapeMaker: Unsupported hit class " + aShape2.getClass());
    }
    
    if(aShape1 instanceof Quad) { Quad s1 = (Quad)aShape1;
        if(aShape2 instanceof Line) { Line s2 = (Line)aShape2;
            return Quad.getHitPointLine(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.x1, s1.y1, s2.x0, s2.y0, s2.x1, s2.y1,false); }
        if(aShape2 instanceof Quad) { Quad s2 = (Quad)aShape2;
            return Quad.getHitPointQuad(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.xc0, s2.yc0, s2.x1, s2.y1, false); }
        if(aShape2 instanceof Cubic) { Cubic s2 = (Cubic)aShape2;
            return Cubic.getHitPointQuad(s2.x0, s2.y0, s2.xc0, s2.yc0, s2.xc1, s2.yc1, s2.x1, s2.y1, 
                s1.x0, s1.y0, s1.xc0, s1.yc0, s1.x1, s1.y1, true); }
        throw new RuntimeException("ShapeMaker: Unsupported hit class " + aShape2.getClass());
    }
    
    if(aShape1 instanceof Cubic) { Cubic s1 = (Cubic)aShape1;
        if(aShape2 instanceof Line) { Line s2 = (Line)aShape2;
            return Cubic.getHitPointLine(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.xc1, s1.yc1, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.x1, s2.y1, true); }
        if(aShape2 instanceof Quad) { Quad s2 = (Quad)aShape2;
            return Cubic.getHitPointQuad(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.xc1, s1.yc1, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.xc0, s2.yc0, s2.x1, s2.y1, true); }
        if(aShape2 instanceof Cubic) { Cubic s2 = (Cubic)aShape2;
            return Cubic.getHitPointCubic(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.xc1, s1.yc1, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.xc0, s2.yc0, s2.xc1, s2.yc1, s2.x1, s2.y1, true); }
        throw new RuntimeException("ShapeMaker: Unsupported hit class " + aShape2.getClass());
    }
    throw new RuntimeException("ShapeMaker: Unsupported hit class " + aShape1.getClass());
}

/**
 * Splits a shape at given parametric location.
 */
public static Shape split(Shape aShape, double aLoc)
{
    if(aShape instanceof Line)
        return ((Line)aShape).split(aLoc);
    if(aShape instanceof Quad)
        return ((Quad)aShape).split(aLoc);
    if(aShape instanceof Cubic)
        return ((Cubic)aShape).split(aLoc);
    throw new RuntimeException("ShapeMaker.split: Unsupported split class " + aShape.getClass());
}

public static void main(String args[])
{
    Shape r1 = new Ellipse(0,0,200,200), r2 = new Ellipse(100,100,200,200);
    Shape r3 = intersect(r1,r2);
    System.err.println("Rect 3: " + r3);
    Image img = Image.get(500,500, false);
    Painter pntr = img.getPainter(); pntr.draw(r1); pntr.draw(r2); pntr.setColor(Color.PINK); pntr.fill(r3);
    SnapUtils.writeBytes(img.getBytesJPEG(), "/tmp/test.jpg");
}

}