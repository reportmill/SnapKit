/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * A custom class.
 */
public class Line extends Shape {

    // Ivars
    public double x1, y1, x2, y2;
    
/**
 * Creates a new line.
 */
public Line(double aX1, double aY1, double aX2, double aY2)  { x1 = aX1; y1 = aY1; x2 = aX2; y2 = aY2; }
    
/**
 * Returns the first point x.
 */
public double getX1()  { return x1; }

/**
 * Returns the first point y.
 */
public double getY1()  { return y1; }

/**
 * Returns the second point x.
 */
public double getX2()  { return x2; }

/**
 * Returns the second point y.
 */
public double getY2()  { return y2; }

/**
 * Returns the shape bounds.
 */
public Rect getBounds()
{
    double x = Math.min(x1, x2), y = Math.min(y1, y2);
    double w = Math.max(x1, x2) - x, h = Math.max(y1, y2) - y;
    return new Rect(x, y, w, h);
}

/**
 * Returns the shape in rect.
 */
public Shape getShapeInRect(Rect aRect)
{
    double x = Math.min(x1, x2), y = Math.min(y1, y2);
    double w = Math.max(x1, x2) - x, h = Math.max(y1, y2) - y;
    double dx = aRect.getX() - x, dy = aRect.getY() - y;
    double sx = w!=0? aRect.getWidth()/w : 0, sy = h!=0? aRect.getHeight()/h : 0;
    double nx1 = x1*sx + dx, ny1 = y1*sy + dy;
    double nx2 = x2*sx + dx, ny2 = y2*sy + dy;
    return new Line(nx1,ny1,nx2,ny2);
}

/**
 * Returns the path iterator.
 */
public PathIter getPathIter(Transform aTrans)  { return new LineIter(this, aTrans); }

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
 * Returns the minimum distance from the given point to this line.
 */
public double getDistance(double aX, double aY)  { return Math.sqrt(getDistanceSquared(aX, aY)); }

/**
 * Returns the minimum distance from the given point to this line, squared.
 */
public double getDistanceSquared(double aX, double aY)  { return getDistanceSquared(x1, y1, x2, y2, aX, aY); }

/**
 * Returns the distance from the given line points (p1,p2) to the given point.
 */
public static double getDistanceSquared(double p1x, double p1y, double p2x, double p2y, double aX, double aY)
{
    // Get parametric location of closest point, clamped between 0-1
    double width = p2x - p1x, height = p2y - p1y;
    double lenSqrd = width*width + height*height;
    double r = lenSqrd>.0001? ((aX - p1x)*width + (aY - p1y)*height)/lenSqrd : 0;
    r = r>=1? 1 : r<0? 0 : r;
    
    // Calculate x/y of parametric location and return distance squared to point
    double x = p1x + r*(p2x - p1x), y = p1y + r*(p2y - p1y);
    double dx = aX - x, dy = aY - y;
    return dx*dx + dy*dy;
}

/**
 * Returns the number of crossings for the ray from given point extending to the right.
 */
public static int crossings(double x0, double y0, double x1, double y1, double px, double py)
{
    if (py <  y0 && py <  y1) return 0;
    if (py >= y0 && py >= y1) return 0;
    if (px >= x0 && px >= x1) return 0;
    if (px <  x0 && px <  x1) return (y0 < y1) ? 1 : -1;
    double xintercept = x0 + (py - y0) * (x1 - x0) / (y1 - y0);
    if (px >= xintercept) return 0;
    return (y0 < y1) ? 1 : -1;
}

/**
 * PathIter for Line.
 */
private static class LineIter implements PathIter {
    
    // Ivars
    double x1, y1, x2, y2; Transform trans; int index;

    /** Create new LineIter. */
    LineIter(Line line, Transform at)
    {
        x1 = line.getX1(); y1 = line.getY1(); x2 = line.getX2(); y2 = line.getY2(); trans = at;
    }

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<=1; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double[] coords)
    {
        PathIter.Seg seg = null;
        if(index==0) { coords[0] = x1; coords[1] = y1; seg = PathIter.Seg.MoveTo; }
        else if(index==1) { coords[0] = x2; coords[1] = y2; seg = PathIter.Seg.LineTo; }
        else throw new RuntimeException("line iterator out of bounds");
        if(trans!=null) trans.transform(coords); index++;
        return seg;
    }
}

}