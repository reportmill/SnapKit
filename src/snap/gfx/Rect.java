/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.*;

/**
 * Represents a rectangle - a Quadrilateral with parallel sides.
 */
public class Rect extends RectBase {

    // Constant rects
    public static final Rect ZeroRect = new Rect();
    public static final Rect UnitRect = new Rect(0,0,1,1);

/**
 * Creates new Rect.
 */
public Rect()  { }

/**
 * Creates new Rect.
 */
public Rect(double aX, double aY, double aW, double aH)  { x = aX; y = aY; width = aW; height = aH; }

/**
 * Returns the shape bounds.
 */
public Rect getBounds()  { return this; }

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aTrans)  { return new RectIter(this, aTrans); }

/**
 * Returns whether the receiver intersects with the given rect.
 */
public boolean intersectsRect(Rect aRect)
{
    if(width<=0 || height<=0 || aRect.getWidth()<=0 || aRect.getHeight()<=0) return false;
    return intersectsEvenIfEmpty(aRect);
}

/**
 * Returns whether the receiver intersects with the given rect.
 */
public boolean intersectsEvenIfEmpty(Rect r)
{
    return intersectsEvenIfEmpty(r.getX(),r.getY(),r.getWidth(),r.getHeight());
}

/**
 * Returns whether the receiver intersects with the given rect.
 */
public boolean intersectsEvenIfEmpty(double aX, double aY, double aW, double aH)
{
    if(getX()<aX) { if(getMaxX()<=aX) return false; }
    else if(aX+aW <= getX()) return false;
    if(getY()<aY) { if(getMaxY()<=aY) return false; }
    else if(aY+aH <= getY()) return false;
    return true;
}

/**
 * Returns the rect that is an intersection of this rect and given rect.
 */
public Rect getIntersectRect(Rect aRect)
{
    double x1 = Math.max(getMinX(), aRect.getMinX());
    double y1 = Math.max(getMinY(), aRect.getMinY());
    double x2 = Math.min(getMaxX(), aRect.getMaxX());
    double y2 = Math.min(getMaxY(), aRect.getMaxY());
    return new Rect(x1, y1, x2-x1, y2-y1);
}

/**
 * Returns whether rect contains x/y.
 */
public boolean contains(double aX, double aY)  { return x<=aX && aX<x+width && y<=aY && aY<y+height; }

/**
 * Returns whether rect contains x/y/w/h.
 */
public boolean contains(Shape aShape)
{
    if(isEmpty()) return false;
    Rect bnds = aShape.getBounds(); // if(bnds.isEmpty()) return false;
    double aX = bnds.x, aY = bnds.y, aW = bnds.width, aH = bnds.height;
    return x<=aX && (aX+aW)<=x+width && y<=aY && (aY+aH)<=y+height;
}

/**
 * Returns whether rect intersects x/y/w/h.
 */
public boolean intersects(Rect aRect)
{
    // if(isEmpty() || aRect.isEmpty()) return false;
    double aX = aRect.x, aY = aRect.y, aW = aRect.width, aH = aRect.height;
    return x<aX+aW && aX<x+width && y<aY+aH && aY<y+height;
}

/**
 * Returns whether shape with line width contains point.
 */
public boolean contains(double x, double y, double aLineWidth)
{
    if(aLineWidth<=1) return contains(x,y);
    return getInsetRect(-aLineWidth/2).contains(x,y);
}

/**
 * Returns whether shape with line width intersects shape.
 */
public boolean intersects(Shape aShape, double aLineWidth)
{
    if(aShape instanceof Rect)
        return getInsetRect(-aLineWidth/2).intersects((Rect)aShape);
    return super.intersects(aShape, aLineWidth);
}

/**
 * Returns a copy of this rect inset by given amount.
 */
public Rect getInsetRect(double anInset)  { return getInsetRect(anInset, anInset); }

/**
 * Returns a copy of this rect inset by given amount.
 */
public Rect getInsetRect(double xIns, double yIns)  { Rect r = clone(); r.inset(xIns,yIns); return r; }

/**
 * Offsets the receiver by the given x & y.
 */
public Rect getOffsetRect(double dx, double dy)  { return new Rect(x+dx, y+dy, width, height); }

/**
 * Scales the receiver rect by the given amount.
 */
public void scale(double anAmt)  { setRect(getX()*anAmt, getY()*anAmt, getWidth()*anAmt, getHeight()*anAmt); }

/**
 * Creates a rect derived from the receiver scaled by the given amount.
 */
public Rect getScaledRect(double anAmt)  { Rect r = clone(); r.scale(anAmt); return r; }

/**
 * Unions the receiver rect with the given rect.
 */
public void union(Rect r2)  { unionEvenIfEmpty(r2); }

/**
 * Unions the receiver rect with the given rect.
 */
public Rect getUnionRect(Rect r2)  { Rect r = clone(); r.unionEvenIfEmpty(r2); return r; }

/**
 * Unions the receiver rect with the given rect.
 */
public void unionEvenIfEmpty(Rect r2)
{
    if(r2==null) return;                         // If given rect is null, just return
    double minX = getX(), maxX = getMaxX(), minY = getY(), maxY = getMaxY();
    double x = Math.min(minX, r2.getX()), y = Math.min(minY, r2.getY());
    double w = Math.max(maxX, r2.getMaxX()) - x, h = Math.max(maxY, r2.getMaxY()) - y;
    setRect(x,y,w,h);
}

/**
 * Adds a point to this rect.  The resulting rect is the smallest that contains both the original and given point.
 */
public void add(double newx, double newy)
{
    double x1 = Math.min(getMinX(), newx), x2 = Math.max(getMaxX(), newx);
    double y1 = Math.min(getMinY(), newy), y2 = Math.max(getMaxY(), newy);
    setRect(x1, y1, x2 - x1, y2 - y1);
}

/**
 * Adds a point to this rect.  The resulting rect is the smallest that contains both the original and given point.
 */
public void addX(double newx)
{
    double x1 = Math.min(getMinX(), newx), x2 = Math.max(getMaxX(), newx);
    setRect(x1, y, x2 - x1, height);
}

/**
 * Adds a point to this rect.  The resulting rect is the smallest that contains both the original and given point.
 */
public void addY(double newy)
{
    double y1 = Math.min(getMinY(), newy), y2 = Math.max(getMaxY(), newy);
    setRect(x, y1, width, y2 - y1);
}

/**
 * Rounds the rect to nearest pixel.
 */
public void snap()
{
    double x = getX(), y = getY(), w = getWidth(), h = getHeight();
    double x1 = Math.floor(x), y1 = Math.floor(y), x2 = Math.ceil(x+w), y2 = Math.ceil(y+h);
    setRect((int)x1, (int)y1, (int)(x2 - x1), (int)(y2 - y1));
}

/**
 * Returns an array of four points containing each corner of the rect.
 */
public Point[] getPoints()
{
    double x = getX(), y = getY(), w = getWidth(), h = getHeight();
    return new Point[] { Point.get(x,y), Point.get(x+w,y), Point.get(x+w,y+h), Point.get(x,y+h) }; 
}

/**
 * Returns the point on the rectangle's perimeter that is intersected by a radial at the given angle from the
 * center of the rect. Zero degrees is at the 3 o'clock position.
 * 
 * @param anAngle Angle in degrees.
 * @param doEllipse Whether to scale radials into ellipse or leave them normal.
 * @return Returns point on perimeter of rect intersected by radial at given angle.
 */
public Point getPerimeterPointForRadial(double anAngle, boolean doEllipse)
{
    // Equation for ellipse is: x = a cos(n), y = b sin(n)
    // Define the ellipse a & b axis length constants as half the rect width & height
    double a = width/2, b = height/2; if(a==0 || b==0) return new Point();
    
    // If not elliptical, change a & b to min length so we use normal circle instead of elliptical radians 
    if(!doEllipse)
        a = b = Math.min(a, b);
    
    // Calculate the coordinates of the point on the ellipse/circle for the given angle
    double x1 = a * MathUtils.cos(anAngle);
    double y1 = b * MathUtils.sin(anAngle);
    
    // First, let's assume the perimeter x coord is on the rect's left or right border
    double x2 = width/2 * MathUtils.sign(x1);
    
    // Then calculate the y perimeter coord by assuming y2/x2 = y1/x1
    double y2 = x2 * y1/x1;
    
    // If final perimeter height outside rect height, recalc but assume final perimeter y is top or bottom border
    if(Math.abs(y2)>b) {
        y2 = height/2 * MathUtils.sign(y1);
        x2 = y2 * x1/y1;
    }
    
    // Get point in rect coords
    return Point.get(getMidX() + x2, getMidY() + y2);
}

/**
 * Returns the shape in rect.
 */
public Shape getShapeInRect(Rect aRect)  { return aRect; }

/**
 * Standard clone implementation.
 */
public Rect clone()  { return new Rect(x,y,width,height); }

/**
 * Standard hashCode implementation.
 */
public int hashCode()
{
    long bits = Double.doubleToLongBits(getX()); bits += Double.doubleToLongBits(getY()) * 37;
    bits += Double.doubleToLongBits(getWidth()) * 43; bits += Double.doubleToLongBits(getHeight()) * 47;
    return (((int) bits) ^ ((int) (bits >> 32)));
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    Rect other = anObj instanceof Rect? (Rect)anObj : null; if(other==null) return false;
    return super.equals(other);
}

/**
 * Creates a rect enclosing the given array of points.
 */
public static Rect get(Point ... points)
{
    if(points.length==0) return new Rect(0,0,0,0);                    // If no points return empty rect
    double x = points[0].getX(), y = points[0].getY(), w = x, h = y;  // Get initial x, y, w, h
    for(int i=1; i<points.length; i++) {                              // Iterate over remaining points to get min/max
        x = Math.min(x, points[i].getX()); y = Math.min(y, points[i].getY());
        w = Math.max(w, points[i].getX()); h = Math.max(h, points[i].getY()); }
    return new Rect(x, y, w-x, h-y);                                  // Return rect
}

/**
 * Creates a rect from an String in XML format as defined in toXMLString().
 */
public static Rect get(String aString)
{
    double x = StringUtils.doubleValue(aString); int start = aString.indexOf(' ', 0);
    double y = StringUtils.doubleValue(aString, start + 1); start = aString.indexOf(' ', start + 1);
    double w = StringUtils.doubleValue(aString, start + 1); start = aString.indexOf(' ', start + 1);
    double h = StringUtils.doubleValue(aString, start + 1);
    return new Rect(x, y, w, h);
}

/**
 * PathIter for Rect.
 */
private static class RectIter implements PathIter {
    
    // Ivars
    double x, y, w, h; Transform affine; int index;

    /** Create new RectIter. */
    RectIter(Rect r, Transform at)
    {
        x = r.getX(); y = r.getY(); w = r.getWidth(); h = r.getHeight(); affine = at;
        if(w<0 || h<0) index = 6;
    }

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<=5; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double[] coords)
    {
        if(!hasNext()) throw new RuntimeException("rect iterator out of bounds");
        if(index==5) { index++; return PathIter.Seg.Close; }
        coords[0] = x; coords[1] = y;
        if(index==1 || index==2) coords[0] += w;
        if(index==2 || index==3) coords[1] += h;
        if(affine!=null) affine.transform(coords); index++;
        return index==1? PathIter.Seg.MoveTo : PathIter.Seg.LineTo;
    }
}

}