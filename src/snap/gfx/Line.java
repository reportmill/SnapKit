/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.MathUtils;

/**
 * A custom class.
 */
public class Line extends Shape {

    // Ivars
    public double x0, y0, x1, y1;
    
/**
 * Creates a new line.
 */
public Line(double aX0, double aY0, double aX1, double aY1)  { x0 = aX0; y0 = aY0; x1 = aX1; y1 = aY1; }
    
/**
 * Returns the first point x.
 */
public double getX0()  { return x0; }

/**
 * Returns the first point y.
 */
public double getY0()  { return y0; }

/**
 * Returns the second point x.
 */
public double getX1()  { return x1; }

/**
 * Returns the second point y.
 */
public double getY1()  { return y1; }

/**
 * Returns the shape bounds.
 */
public Rect getBounds()  { return bounds(x0, y0, x1, y1, null); }

/**
 * Returns the shape in rect.
 */
public Shape getShapeInRect(Rect aRect)
{
    double x = Math.min(x0, x1), y = Math.min(y0, y1);
    double w = Math.max(x0, x1) - x, h = Math.max(y0, y1) - y;
    double dx = aRect.getX() - x, dy = aRect.getY() - y;
    double sx = w!=0? aRect.getWidth()/w : 0, sy = h!=0? aRect.getHeight()/h : 0;
    double nx1 = x0*sx + dx, ny1 = y0*sy + dy;
    double nx2 = x1*sx + dx, ny2 = y1*sy + dy;
    return new Line(nx1,ny1,nx2,ny2);
}

/**
 * Returns the path iterator.
 */
public PathIter getPathIter(Transform aTrans)  { return new LineIter(aTrans); }

/**
 * Returns whether shape contains x/y.
 */
public boolean contains(double aX, double aY)  { return false; }

/**
 * Returns whether shape contains x/y/w/h.
 */
public boolean contains(Shape aShape)  { return false; }

/**
 * Returns whether shape with line width contains point.
 */
public boolean contains(double aX, double aY, double aLineWidth)  { return getDistance(aX,aY)<aLineWidth/2; }

/**
 * Splits the line at given parametric location and return the remainder.
 */
public Line split(double aLoc)
{
    double x = x0 + aLoc*(x1 - x0);
    double y = y0 + aLoc*(y1 - y0);
    Line rem = new Line(x, y, x1, y1);
    x1 = x; y1 = y;
    return rem;
}

/**
 * Returns the minimum distance from the given point to this line.
 */
public double getDistance(double aX, double aY)  { return Math.sqrt(getDistanceSquared(aX, aY)); }

/**
 * Returns the minimum distance from the given point to this line, squared.
 */
public double getDistanceSquared(double aX, double aY)  { return getDistanceSquared(x0, y0, x1, y1, aX, aY); }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    Line other = anObj instanceof Line? (Line)anObj : null; if(other==null) return false;
    return MathUtils.equals(x0,other.x0) && MathUtils.equals(y0,other.y0) &&
        MathUtils.equals(x1,other.x1) && MathUtils.equals(y1,other.y1);
}

/**
 * Returns the distance from the given line points (p1,p2) to the given point.
 */
public static double getDistanceSquared(double x0, double y0, double x1, double y1, double aX, double aY)
{
    // Get parametric location of closest point, clamped between 0-1
    double width = x1 - x0, height = y1 - y0;
    double lenSqrd = width*width + height*height;
    double r = lenSqrd>.0001? ((aX - x0)*width + (aY - y0)*height)/lenSqrd : 0;
    r = r>=1? 1 : r<0? 0 : r;
    
    // Calculate x/y of parametric location and return distance squared to point
    double x = x0 + r*(x1 - x0), y = y0 + r*(y1 - y0);
    double dx = aX - x, dy = aY - y;
    return dx*dx + dy*dy;
}

/**
 * Returns the bounds for given line points.
 */
public static Rect bounds(double x0, double y0, double x1, double y1, Rect aRect)
{
    if(aRect==null) aRect = new Rect(x0,y0,0,0); else aRect.setRect(x0,y0,0,0);
    aRect.add(x1, y1);
    return aRect;
}

/**
 * Returns the number of crossings for the ray from given point extending to the right.
 */
public static int crossings(double x0, double y0, double x1, double y1, double px, double py)
{
    if(py<y0 && py<y1) return 0;
    if(py>=y0 && py>=y1) return 0;
    if(px>=x0 && px>=x1) return 0;
    if(px<x0 && px<x1) return y0<y1? 1 : -1;
    double xintercept = x0 + (py - y0) * (x1 - x0) / (y1 - y0);
    if(px>=xintercept) return 0;
    return y0<y1? 1 : -1;
}

/**
 * Returns whether line for given points is intersected by second line with given points.
 */
public static boolean intersectsLine(double x0, double y0, double x1, double y1, double px0, double py0,
    double px1, double py1)
{
    return getHitPointLine(x0,y0,x1,y1,px0,py0,px1,py1,false)>=0;
}

/**
 * Returns whether line for given points is intersected by second line with given points.
 */
public static double getHitPointLine(double x0, double y0, double x1, double y1, double px0, double py0,
    double px1, double py1, boolean isOther)
{
    // Probably some line slope stuff, I can't really remember
    double numerator1 = (y0-py0)*(px1-px0) - (x0-px0)*(py1-py0); //(p1y-p3y)*(p4x-p3x) - (p1x-p3x)*(p4y-p3y);
    double numerator2 = (y0-py0)*(x1-x0) - (x0-px0)*(y1-y0);     //(p1y-p3y)*(p2x-p1x) - (p1x-p3x)*(p2y-p1y);
    double denominator = (x1-x0)*(py1-py0) - (y1-y0)*(px1-px0);  //(p2x-p1x)*(p4y-p3y) - (p2y-p1y)*(p4x-p3x)
    
    // Calculate parametric locations of intersection (line1:r, line2:s)
    double r = numerator1/denominator;
    double s = numerator2/denominator;
    
    // If parametric locations outside 0-1 range, then return false because lines don't intersect
    if(r<0 || r>1 || s<0 || s>1) return -1;
    return isOther? s : r;
}

/**
 * PathIter for Line.
 */
private class LineIter implements PathIter {
    
    /** Create new LineIter. */
    LineIter(Transform at) { trans = at; }  Transform trans; int index;

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<=1; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double pts[])
    {
        PathIter.Seg seg = null;
        if(index==0) { seg = PathIter.Seg.MoveTo; pts[0] = x0; pts[1] = y0; }
        else if(index==1) { seg = PathIter.Seg.LineTo; pts[0] = x1; pts[1] = y1; }
        else throw new RuntimeException("line iterator out of bounds");
        if(trans!=null) trans.transform(pts); index++;
        return seg;
    }
}

}