/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.StringUtils;

/**
 * A class to represent a generic geometric shape (Line, Rect, Ellipse, etc.).
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
    if(!getBounds().contains(aX, aY)) return false;
    int cross = getCrossings(aX, aY), mask = -1;
    boolean c = ((cross & mask) != 0); return c;
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
    
    // Iterate over shape segments, if any segment edge intersects, return false
    PathIter pi = aShape.getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                if(!contains(lx,ly,lx=pts[0],ly=pts[1])) return false;
                break;
            case QuadTo:
                if(!contains(lx,ly,pts[0],pts[1],lx=pts[0],ly=pts[1])) return false;
                break;
            case CubicTo:
                if(!contains(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5])) return false;
                break;
            case Close:
                if(!contains(lx,ly,lx=mx,ly=my)) return false;
                break;
        }
    }
    
    // Return true since all shape points are contained and no shape edges intersect
    return true;
}

/**
 * Returns whether this shape contains line defined by given points.
 */
public boolean contains(double x0, double y0, double x1, double y1)
{
    // If bounds don't contain, just return false
    if(!getBounds().contains(x0, y0, x1, y1)) return false;
    
    // If either endpoint not contained, return false
    if(!contains(x0,y0) || !contains(x1,y1)) return true;

    // Iterate over segments, if any segment doesn't contain line, return false
    PathIter pi = getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                if(Line.intersectsLine(lx,ly,lx=pts[0],ly=pts[1],x0,y0,x1,y1)) return false;
                break;
            case QuadTo:
                if(Quad.intersectsLine(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3],x0,y0,x1,y1)) return false;
                break;
            case CubicTo:
                if(Cubic.intersectsLine(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5],x0,y0,x1,y1))
                    return false;
                break;
            case Close:
                if(Line.intersectsLine(lx,ly,lx=mx,ly=my,x0,y0,x1,y1)) return false;
                break;
        }
    }
    
    // Return true since no segements intersect
    return true;
}

/**
 * Returns whether this shape contains quad defined by given points.
 */
public boolean contains(double x0, double y0, double xc0, double yc0, double x1, double y1)
{
    // If bounds don't contain, just return false
    if(!getBounds().contains(x0, y0, xc0, yc0, x1, y1)) return false;
    
    // If either endpoint not contained, return false
    if(!contains(x0,y0) || !contains(x1,y1)) return false;

    // Iterate over segments, if any segment intersects quad, return false
    PathIter pi = getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                if(Quad.intersectsLine(x0,y0,xc0,yc0,x1,y1,lx,ly,lx=pts[0],ly=pts[1])) return false;
                break;
            case QuadTo:
                if(Quad.intersectsQuad(x0,y0,xc0,yc0,x1,y1,lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3])) return false;
                break;
            case CubicTo:
                if(Cubic.intersectsQuad(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5],x0,y0,xc0,yc0,x1,y1))
                    return false;
                break;
            case Close:
                if(Quad.intersectsLine(x0,y0,xc0,yc0,x1,y1,lx,ly,lx=mx,ly=my)) return false;
                break;
        }
    }
    
    // Return true since no segements intersect
    return true;
}

/**
 * Returns whether this shape contains cubic defined by given points.
 */
public boolean contains(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1)
{
    // If bounds don't contain, just return false
    if(!getBounds().contains(x0, y0, xc0, yc0, xc1, yc1, x1, y1)) return false;
    
    // If either endpoint not contained, return false
    if(!contains(x0,y0) || !contains(x1,y1)) return false;

    // Iterate over segments, if any segment intersects cubic, return false
    PathIter pi = getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                if(Cubic.intersectsLine(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,lx=pts[0],ly=pts[1])) return false;
                break;
            case QuadTo:
                if(Cubic.intersectsQuad(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3]))
                    return false;
                break;
            case CubicTo:
                if(Cubic.intersectsCubic(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,
                    pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5]))
                    return false;
                break;
            case Close:
                if(Cubic.intersectsLine(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,lx=mx,ly=my)) return false;
                break;
        }
    }
    
    // Return true since no segements intersect
    return true;
}

/**
 * Returns whether shape intersects shape.
 */
public boolean intersects(Shape aShape)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(aShape.getBounds())) return false;
    
    // Iterate over shape segments, if any segment intersects, return true
    PathIter pi = aShape.getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                if(intersects(lx,ly,lx=pts[0],ly=pts[1])) return true;
                break;
            case QuadTo:
                if(intersects(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3])) return true;
                break;
            case CubicTo:
                if(intersects(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5])) return true;
                break;
            case Close:
                if(intersects(lx,ly,lx=mx,ly=my)) return true;
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
    if(!getBounds().intersects(x0, y0, x1, y1)) return false;
    
    // If either endpoint contained, return true
    if(contains(x0,y0) || contains(x1,y1)) return true;

    // Iterate over segments, if any segment intersects line, return true
    PathIter pi = getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
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
            case Close:
                if(Line.intersectsLine(lx,ly,lx=mx,ly=my,x0,y0,x1,y1)) return true;
                break;
        }
    }
    
    // Return false since no segements intersect
    return false;
}

/**
 * Returns whether this shape intersects quad defined by given points.
 */
public boolean intersects(double x0, double y0, double xc0, double yc0, double x1, double y1)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(x0, y0, xc0, yc0, x1, y1)) return false;
    
    // If either endpoint contained, return true
    if(contains(x0,y0) || contains(x1,y1)) return true;

    // Iterate over segments, if any segment intersects quad, return true
    PathIter pi = getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
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
            case Close:
                if(Quad.intersectsLine(x0,y0,xc0,yc0,x1,y1,lx,ly,lx=mx,ly=my)) return true;
                break;
        }
    }
    
    // Return false since no segements intersect
    return false;
}

/**
 * Returns whether this shape intersects cubic defined by given points.
 */
public boolean intersects(double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(x0, y0, xc0, yc0, xc1, yc1, x1, y1)) return false;
    
    // If either endpoint contained, return true
    if(contains(x0,y0) || contains(x1,y1)) return true;

    // Iterate over segments, if any segment intersects cubic, return true
    PathIter pi = getPathIter(null); double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
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
            case Close:
                if(Cubic.intersectsLine(x0,y0,xc0,yc0,xc1,yc1,x1,y1,lx,ly,lx=mx,ly=my)) return true;
                break;
        }
    }
    
    // Return false since no segements intersect
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
}

/**
 * Returns whether this shape is made up of only line segements.
 */
public boolean isFlat()
{
    PathIter piter = getPathIter(null); double pnts[] = new double[6];
    while(piter.hasNext()) switch(piter.getNext(pnts)) {
        case QuadTo: case CubicTo: return false; }
    return true;
}

/**
 * Returns a flattented version of this shape (just this shape if already flat).
 */
public Shape getFlat()
{
    // If already flat, just return this shape
    if(isFlat()) return this;
    
    // Create path iterate over segments to generate flat path
    Path path = new Path();
    PathIter piter = getPathIter(null); double pnts[] = new double[6];
    while(piter.hasNext()) switch(piter.getNext(pnts)) {
        case MoveTo: path.moveTo(pnts[0], pnts[1]); break;
        case LineTo: path.lineTo(pnts[0], pnts[1]); break;
        case QuadTo: path.quadToFlat(pnts[0], pnts[1], pnts[2], pnts[3]); break;
        case CubicTo: path.curveToFlat(pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]); break;
        case Close: path.close(); break;
    }
    
    // Return new path
    return path;
}

/**
 * Returns the shape in rect.
 */
public Shape copyFor(Rect aRect)
{
    Rect bnds = getBounds(); if(bnds.equals(aRect)) return this;
    double bw = bnds.width, bh = bnds.height;
    double sx = bw!=0? aRect.width/bw : 0, sy = bh!=0? aRect.height/bh : 0;
    Transform trans = Transform.getScale(sx, sy);
    trans.translate(aRect.x - bnds.x, aRect.y - bnds.y);
    return new Path(getPathIter(trans));
}

/**
 * Returns a copy of this shape transformed by given transform.
 */
public Shape copyFor(Transform aTrans)  { return new Path(getPathIter(aTrans)); }

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
public static Shape add(Shape aShape1, Shape aShape2)  { return ShapeMaker.add(aShape1, aShape2); }

/**
 * Subtracts two shapes together.
 */
public static Shape subtract(Shape aShape1, Shape aShape2)  { return ShapeMaker.subtract(aShape1, aShape2); }

/**
 * Returns the intersection shape of two shapes.
 */
public static Shape intersect(Shape aShape1, Shape aShape2)  { return ShapeMaker.intersect(aShape1, aShape2); }

/** Helper. */
private static String fmt(double aVal)  { return StringUtils.toString(aVal); }

}