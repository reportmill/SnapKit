/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.StringUtils;

/**
 * A custom class.
 */
public abstract class Shape {

/**
 * Returns the bounds.
 */
public Rect getBounds()  { return PathIter.getBounds(getPathIter(null)); }

/**
 * Returns a path iterator.
 */
public abstract PathIter getPathIter(Transform aT);

/**
 * Returns whether shape contains point.
 */
public boolean contains(Point aPnt)  { return contains(aPnt.getX(), aPnt.getY()); }

/**
 * Returns whether shape contains x/y.
 */
public boolean contains(double aX, double aY)
{
    if(!getBounds().contains(aX, aY)) return false; //return Path2D.contains(AWT.get(getPathIter(null)),aX,aY);
    int cross = getCrossings(aX, aY), mask = -1; //(pi.getWindingRule() == WIND_NON_ZERO ? -1 : 1);
    boolean c = ((cross & mask) != 0); return c; //System.out.println("Contains: " + c);
}

/**
 * Returns the number of crossings for the ray from given point extending to the right.
 */
public int getCrossings(double aX, double aY)
{
    int cross = 0;
    PathIter pi = getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0, mx = 0, my = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo:
                if(ly!=my) cross += Line.crossings(lx, ly, mx, my, aX, aY);
                lx = mx = pts[0]; ly = my = pts[1]; break;
            case LineTo: cross += Line.crossings(lx, ly, lx=pts[0], ly=pts[1], aX, aY); break;
            case QuadTo: cross += Quad.crossings(lx, ly, pts[0], pts[1], lx=pts[2], ly=pts[3], aX, aY, 0); break;
            case CubicTo:
                cross += Cubic.crossings(lx, ly, pts[0], pts[1], pts[2], pts[3], lx=pts[4], ly=pts[5], aX, aY, 0);
                break;
            case Close: if(ly!=my) cross += Line.crossings(lx, ly, lx=mx, ly=my, aX, aY); break;
        }
    }
    return cross;
}

/**
 * Returns whether shape contains shape.
 */
public boolean contains(Shape aShape)
{
    // If bounds don't contain shape, just return false
    if(!getBounds().contains(aShape.getBounds())) return false;
    
    // Iterate over shape segments, if any segment end point not contained, return false
    PathIter pi = aShape.getPathIter(null); double pts[] = new double[6];
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo:
            case LineTo: if(!contains(pts[0], pts[1])) return false; break;
            case QuadTo: if(!contains(pts[2], pts[3])) return false; break;
            case CubicTo: if(!contains(pts[4], pts[5])) return false; break;
        }
    }
    
    // Iterate over shape segments, if any segment edge intersects, return false
    pi = aShape.getPathIter(null); double lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = pts[0]; ly = pts[1]; break;
            case LineTo:
                if(intersects(lx,ly,lx=pts[0],ly=pts[1])) return false;
                break;
            case QuadTo:
                if(intersects(lx,ly,pts[0],pts[1],lx=pts[0],ly=pts[1])) return false;
                break;
            case CubicTo:
                if(intersects(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5])) return false;
                break;
        }
    }
    
    // Return true since all shape points are contained and no shape edges intersect
    return true; //Area area1=area(this),area2=(Area)area1.clone();area2.add(area(aShape));return area1.equals(area2);
}

/**
 * Returns whether shape intersects shape.
 */
public boolean intersects(Shape aShape)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(aShape.getBounds())) return false;
    
    // Iterate over shape segments, if any segment intersects, return true
    PathIter pi = aShape.getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = pts[0]; ly = pts[1]; break;
            case LineTo:
                if(intersects(lx,ly,lx=pts[0],ly=pts[1])) return true;
                break;
            case QuadTo:
                if(intersects(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3])) return true;
                break;
            case CubicTo:
                if(intersects(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5])) return true;
                break;
        }
    }
    
    // Return true if either shape contains the other
    return contains(aShape) || aShape.contains(this);
}

/**
 * Returns whether this shape intersects line defined by given points.
 */
public boolean intersects(double x0, double y0, double x1, double y1)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(Line.bounds(x0,y0,x1,y1,null))) return false;
    
    // Iterate over segments, if any segment intersects line, return true
    PathIter pi = getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = pts[0]; ly = pts[1]; break;
            case LineTo:
                if(Line.intersectsLine(lx,ly,lx=pts[0],ly=pts[1],x0,y0,x1,y1)) return true;
                break;
            case QuadTo:
                if(Quad.intersectsLine(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3],x0,y0,x1,y1)) return true;
                break;
            case CubicTo:
                if(Cubic.intersectsLine(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5],x0,y0,x1,y1))
                    return true;
                break;
        }
    }
    
    // Return false since line hits no segments
    return false;
}

/**
 * Returns whether this shape intersects quad defined by given points.
 */
public boolean intersects(double x0, double y0, double xc0, double yc0, double x1, double y1)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(Quad.bounds(x0,y0,xc0,yc0,x1,y1,null))) return false;
    
    // Iterate over segments, if any segment intersects quad, return true
    PathIter pi = getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = pts[0]; ly = pts[1]; break;
            case LineTo:
                if(Quad.intersectsLine(x0,y0,xc0,yc0,x1,y1,lx,ly,lx=pts[0],ly=pts[1])) return true;
                break;
            case QuadTo:
                if(Quad.intersectsQuad(x0,y0,xc0,yc0,x1,y1,lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3])) return true;
                break;
            case CubicTo:
                if(Cubic.intersectsQuad(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5],x0,y0,xc0,yc0,x1,y1))
                    return true;
                break;
        }
    }
    
    // Return false since line hits no segments
    return false;
}

/**
 * Returns whether this shape intersects cubic defined by given points.
 */
public boolean intersects(double x0, double y0, double xc0, double yc0, double xc1, double yc1,double x1, double y1)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(Cubic.bounds(x0,y0,xc0,yc0,xc1,yc1,x1,y1,null))) return false;
    
    // Iterate over segments, if any segment intersects cubic, return true
    PathIter pi = getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = pts[0]; ly = pts[1]; break;
            case LineTo:
                if(Cubic.intersectsLine(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,lx=pts[0],ly=pts[1])) return true;
                break;
            case QuadTo:
                if(Cubic.intersectsQuad(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3]))
                    return true;
                break;
            case CubicTo:
                if(Cubic.intersectsCubic(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,
                    pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5]))
                    return true;
                break;
        }
    }
    
    // Return false since line hits no segments
    return false;
}

/**
 * Returns the closest distance from given point to path.
 */
public double getDistance(double x, double y)
{
    // Iterate over segments, if any segment intersects cubic, return true
    double dist = Float.MAX_VALUE, d = dist;
    PathIter pi = getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: lx = pts[0]; ly = pts[1]; break;
            case LineTo: d = Line.getDistanceSquared(lx,ly,lx=pts[0],ly=pts[1],x,y); break;
            case QuadTo: d = Quad.getDistanceSquared(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3],x,y); break;
            case CubicTo:d = Cubic.getDistanceSquared(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5],x,y); break;
        }
        dist = Math.min(dist, d);
    }
    
    // Return false since line hits no segments
    return Math.sqrt(dist);
}

/**
 * Returns whether shape with line width contains point.
 */
public boolean contains(double aX, double aY, double aLineWidth)
{
    // If linewidth is small return normal version
    if(aLineWidth<=1) return contains(aX,aY);
    
    // If extended bounds don't contain point, return false
    if(!getBounds().getInsetRect(-aLineWidth/2).contains(aX,aY)) return false;
    
    // If distance less than line width or this shape contains point, return true
    double dist = getDistance(aX, aY);
    return dist<=aLineWidth/2 || contains(aX, aY);
}

/**
 * Returns whether shape with line width intersects point.
 */
public boolean intersects(double aX, double aY, double aLineWidth)
{
    // If extended bounds don't contain point, return false
    if(!getBounds().getInsetRect(-aLineWidth/2).contains(aX,aY)) return false;
    
    // If distance less than line width, return true
    double dist = getDistance(aX, aY);
    return dist<=aLineWidth/2;
}

/**
 * Returns whether shape with line width intersects shape.
 */
public boolean intersects(Shape aShape, double aLineWidth)
{
    // If linewidth is small return normal version
    if(aLineWidth<=1) return intersects(aShape);
    
    // If bounds don't intersect, return false
    if(!getBounds().getInsetRect(-aLineWidth/2).intersects(aShape)) return false;
    
    // We need to outset of shape or the other
    Shape shp1 = this, shp2 = aShape; //double ins = -aLineWidth/2;
    //if(aShape.isPolygonal()) shp2 = getInsetShape(ins); else shp1 = getInsetShape(ins);
    return shp1.intersects(shp2);
    
    // Get stroked shape, get area of stroked shape, and return whether stroke shape area intersects given shape
    //java.awt.Shape shape1 = AWT.get(this); BasicStroke bstroke = new BasicStroke((float)aLineWidth*8); 
    //java.awt.Shape shape2 = bstroke.createStrokedShape(shape1);
    //Area area1 = new Area(shape1), area2 = new Area(shape2); if(!area1.isEmpty()) area2.add(area1);
    //area2.intersect(area(aShape)); return !area2.isEmpty();
}

/**
 * Returns the shape in rect.
 */
public Shape getShapeInRect(Rect aRect)
{
    Rect bounds = getBounds(); if(bounds.equals(aRect)) return this;
    Transform trans = Transform.getTrans(aRect.getX() - bounds.getX(), aRect.getY() - bounds.getY());
    double bw = bounds.getWidth(), bh = bounds.getHeight();
    double sx = bw!=0? aRect.getWidth()/bw : 0, sy = bh!=0? aRect.getHeight()/bh : 0;
    trans.scale(sx, sy);
    return new Path(getPathIter(trans));
}

/**
 * Returns a string representation of Shape.
 */
public String getString()
{
    String str = "{ "; PathIter pi = getPathIter(null); double pts[] = new double[6];
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: str += "M " + fmt(pts[0]) + " " + fmt(pts[1]) + " "; break;
            case LineTo: str += "L " + fmt(pts[0]) + " " + fmt(pts[1]) + " "; break;
            case QuadTo: str += "Q " + fmt(pts[0]) + " " + fmt(pts[1]) + ' ' + fmt(pts[2]) + ' ' + fmt(pts[3]) + ' ';
                break;
            case CubicTo: str += "C " + fmt(pts[0]) + ' ' + fmt(pts[1]) + ' ' + fmt(pts[2]) + ' ' + fmt(pts[3]) + ' ' +
                fmt(pts[4]) + ' ' + fmt(pts[5]) + ' '; break;
            case Close: str += "CLS ";
        }
    }
    return str + "}";
}

/**
 * Standard to string implementation.
 */
public String toString()  { return getClass().getSimpleName() + " [" + getBounds().getString() + "] " + getString(); }

/**
 * Adds two shapes together.
 */
public static Shape add(Shape aShape1, Shape aShape2)
{
    java.awt.geom.Area a1 = area(aShape1), a2 = area(aShape2); a1.add(a2);
    return snap.swing.AWT.get(a1);
}

/**
 * Subtracts two shapes together.
 */
public static Shape subtract(Shape aShape1, Shape aShape2)
{
    java.awt.geom.Area a1 = area(aShape1), a2 = area(aShape2); a1.subtract(a2);
    return snap.swing.AWT.get(a1);
}

/**
 * Returns the intersection shape of two shapes.
 */
public static Shape intersect(Shape aShape1, Shape aShape2)
{
    if(aShape1 instanceof Rect && aShape2 instanceof Rect) return ((Rect)aShape1).getIntersectRect((Rect)aShape2);
    return ShapeMaker.intersect(aShape1, aShape2);
    //java.awt.geom.Area a1 = area(aShape1), a2 = area(aShape2); a1.intersect(a2); return snap.swing.AWT.get(a1);
}

/**
 * Returns an area for a Shape.
 */
static java.awt.geom.Area area(Shape aShape)  { return new java.awt.geom.Area(snap.swing.AWT.get(aShape)); }
private static String fmt(double aVal)  { return StringUtils.toString(aVal); }

}