package snap.gfx;
import java.util.*;

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
    List <Shape> slist = slist1;
    int i1 = 0, i2 = 0, len1 = slist1.size(), len2 = slist2.size();
    
    // Iterate until
    Shape seg = slist.get(0);
    while(!seg.intersects(aShape2)) {
        i1 = (i1+1)%len1;
        seg = slist.get(i1);
    }
    
    return null;
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

}