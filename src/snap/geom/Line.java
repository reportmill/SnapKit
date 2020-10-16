/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;

/**
 * A class to represent a mathematical line.
 */
public class Line extends Segment {

    /**
     * Creates a new line.
     */
    public Line(double aX0, double aY0, double aX1, double aY1)
    {
        x0 = aX0; y0 = aY0; x1 = aX1; y1 = aY1;
    }

    /**
     * Sets the line points.
     */
    public void setPoints(double aX0, double aY0, double aX1, double aY1)
    {
        x0 = aX0; y0 = aY0; x1 = aX1; y1 = aY1;
        shapeChanged();
    }

    /**
     * Calculates and returns length of this segment.
     */
    protected double getArcLengthImpl()
    {
        return Point.getDistance(x0, y0, x1, y1);
    }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        return getBounds(x0, y0, x1, y1, null);
    }

    /**
     * Returns the shape in rect.
     */
    public Shape copyFor(Rect aRect)
    {
        double x = Math.min(x0, x1), y = Math.min(y0, y1);
        double w = Math.max(x0, x1) - x, h = Math.max(y0, y1) - y;
        double dx = aRect.getX() - x, dy = aRect.getY() - y;
        double sx = w!=0 ? aRect.getWidth()/w : 0;
        double sy = h!=0 ? aRect.getHeight()/h : 0;
        double nx1 = x0*sx + dx, ny1 = y0*sy + dy;
        double nx2 = x1*sx + dx, ny2 = y1*sy + dy;
        return new Line(nx1, ny1, nx2, ny2);
    }

    /**
     * Returns the path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return new LineIter(aTrans);
    }

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
    public boolean contains(double aX, double aY, double aLineWidth)
    {
        return getDistance(aX,aY)<aLineWidth/2;
    }

    /**
     * Returns the x value at given parametric location.
     */
    public double getX(double aLoc)
    {
        return x0 + aLoc*(x1 - x0);
    }

    /**
     * Returns the y value at given parametric location.
     */
    public double getY(double aLoc)
    {
        return y0 + aLoc*(y1 - y0);
    }

    /**
     * Splits the line at given parametric location and return the remainder.
     */
    public Line split(double aLoc)
    {
        double x = x0 + aLoc*(x1 - x0);
        double y = y0 + aLoc*(y1 - y0);
        Line rem = new Line(x, y, x1, y1);
        x1 = x; y1 = y;
        shapeChanged();
        return rem;
    }

    /**
     * Creates and returns the reverse of this segement.
     */
    public Line createReverse()
    {
        return new Line(x1, y1, x0, y0);
    }

    /**
     * Returns the minimum distance from the given point to this line.
     */
    public double getDistance(double aX, double aY)
    {
        return Math.sqrt(getDistanceSquared(aX, aY));
    }

    /**
     * Returns the minimum distance from the given point to this line, squared.
     */
    public double getDistanceSquared(double aX, double aY)
    {
        return getDistanceSquared(x0, y0, x1, y1, aX, aY);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        Line other = anObj instanceof Line ? (Line)anObj : null; if (other==null) return false;
        return equals(x0,other.x0) && equals(y0,other.y0) &&
            equals(x1,other.x1) && equals(y1,other.y1);
    }

    /**
     * Returns whether line is equal to another, regardless of direction.
     */
    public boolean matches(Object anObj)
    {
        if (equals(anObj)) return true;
        Line other = anObj instanceof Line ? (Line)anObj : null; if (other==null) return false;
        return equals(x0,other.x1) && equals(y0,other.y1) &&
            equals(x1,other.x0) && equals(y1,other.y0);
    }

    /**
     * Returns the hit for given segment.
     */
    public SegHit getHit(Segment aSeg)
    {
        if (aSeg instanceof Cubic) { Cubic s2 = (Cubic)aSeg;
            return SegHit.getHitLineCubic(x0, y0, x1, y1, s2.x0, s2.y0, s2.cp0x, s2.cp0y, s2.cp1x, s2.cp1y, s2.x1, s2.y1);
        }
        if (aSeg instanceof Quad) { Quad s2 = (Quad)aSeg;
            return SegHit.getHitLineQuad(x0, y0, x1, y1, s2.x0, s2.y0, s2.cpx, s2.cpy, s2.x1, s2.y1);
        }
        return SegHit.getHitLineLine(x0, y0, x1, y1, aSeg.x0, aSeg.y0, aSeg.x1, aSeg.y1);
    }

    /**
     * Returns the distance from the given line points (p1,p2) to the given point.
     */
    public static double getDistance(double x0, double y0, double x1, double y1, double aX, double aY)
    {
        return Math.sqrt(getDistanceSquared(x0, y0, x1, y1, aX, aY));
    }

    /**
     * Returns the distance from the given line points (p1,p2) to the given point.
     */
    public static double getDistanceSquared(double x0, double y0, double x1, double y1, double aX, double aY)
    {
        // Get parametric location of closest point, clamped between 0-1
        double width = x1 - x0, height = y1 - y0;
        double lenSqrd = width*width + height*height;
        double r = lenSqrd>.0001 ? ((aX - x0)*width + (aY - y0)*height)/lenSqrd : 0;
        r = r>=1 ? 1 : r<0 ? 0 : r;

        // Calculate x/y of parametric location and return distance squared to point
        double x = x0 + r*(x1 - x0), y = y0 + r*(y1 - y0);
        double dx = aX - x, dy = aY - y;
        return dx*dx + dy*dy;
    }

    /**
     * Returns the bounds for given line points.
     */
    public static Rect getBounds(double x0, double y0, double x1, double y1, Rect aRect)
    {
        if (aRect==null)
            aRect = new Rect(x0, y0,0,0);
        else aRect.setRect(x0, y0,0,0);
        aRect.add(x1, y1);
        return aRect;
    }

    /**
     * Returns the number of crossings for the ray from given point extending to the right.
     */
    public static int crossings(double x0, double y0, double x1, double y1, double px, double py)
    {
        if (py<y0 && py<y1) return 0;
        if (py>=y0 && py>=y1) return 0;
        if (px>=x0 && px>=x1) return 0;
        if (px<x0 && px<x1) return y0<y1 ? 1 : -1;
        double xintercept = x0 + (py - y0) * (x1 - x0) / (y1 - y0);
        if (px>=xintercept) return 0;
        return y0<y1 ? 1 : -1;
    }

    /**
     * Returns whether line for given points is intersected by second line with given points.
     */
    public static boolean intersectsLine(double x0, double y0, double x1, double y1, double px0, double py0,
        double px1, double py1)
    {
        return SegHit.getHitLineLine(x0,y0,x1,y1,px0,py0,px1,py1)!=null;
    }

    /**
     * Returns whether given three points are collinear.
     */
    public static boolean isCollinear(double x0, double y0, double x1, double y1, double x2, double y2)
    {
        double twiceArea = x0*(y1 - y2) + x1*(y2 - y0) + x2*(y0 - y1);
        return MathUtils.equalsZero(twiceArea);
    }

    /**
     * PathIter for Line.
     */
    private class LineIter extends PathIter {

        /** Create new LineIter. */
        LineIter(Transform at) { super(at); } int index;

        /** Returns whether there are more segments. */
        public boolean hasNext() { return index<2; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double coords[])
        {
            switch (index++) {
                case 0: return moveTo(x0, y0, coords);
                case 1: return lineTo(x1, y1, coords);
                default: throw new RuntimeException("line iterator out of bounds");
            }
        }
    }
}