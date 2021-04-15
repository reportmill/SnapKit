/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
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
     * Override to just return this rect.
     */
    protected Rect getBoundsImpl()
    {
        if (width>=0 && height>=0) return this;
        return super.getBoundsImpl();
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return new RectIter(this, aTrans);
    }

    /**
     * Returns a point for given position on rect.
     */
    public Point getPoint(Pos aPos)
    {
        return getPointForPosition(x, y, width, height, aPos);
    }

    /**
     * Returns the rect that is an intersection of this rect and given rect.
     */
    public void intersect(Rect aRect)
    {
        double x2 = Math.max(x, aRect.x), w2 = Math.min(getMaxX(), aRect.getMaxX()) - x2;
        double y2 = Math.max(y, aRect.y), h2 = Math.min(getMaxY(), aRect.getMaxY()) - y2;
        if (w2<0 || h2<0) { x2 = x; y2 = y; w2 = h2 = 0; }
        setRect(x2, y2, w2, h2);
    }

    /**
     * Returns the rect that is an intersection of this rect and given rect.
     */
    public Rect getIntersectRect(Rect aRect)
    {
        double x2 = Math.max(x, aRect.x), w2 = Math.min(getMaxX(), aRect.getMaxX()) - x2;
        double y2 = Math.max(y, aRect.y), h2 = Math.min(getMaxY(), aRect.getMaxY()) - y2;
        if (w2<0 || h2<0) { x2 = x; y2 = y; w2 = h2 = 0; }
        return new Rect(x2, y2, w2, h2);
    }

    /**
     * Returns whether rect contains x/y.
     */
    public boolean contains(double aX, double aY)
    {
        return (x<=aX) && (aX<=x+width) && (y<=aY) && (aY<=y+height);
    }

    /**
     * Override to simplify since rect contains any shape if-and-only-if it contians that shapes bounds.
     */
    public boolean contains(Shape aShape)
    {
        return containsRect(aShape.getBounds());
    }

    /**
     * Override to omptimize rect-to-rect case.
     */
    public boolean intersects(Shape aShape)
    {
        if (aShape instanceof Rect)
            return intersectsRect((Rect)aShape);
        return super.intersects(aShape);
    }

    /**
     * Returns whether rect contains another rect.
     */
    public boolean containsRect(Rect aRect)
    {
        return contains(aRect.x, aRect.y) && contains(aRect.getMaxX(), aRect.getMaxY());
    }

    /**
     * Returns whether the receiver intersects with the given rect.
     */
    public boolean intersectsRect(Rect aRect)
    {
        double aX = aRect.x, aY = aRect.y, aW = aRect.width, aH = aRect.height;
        return x<aX+aW && aX<x+width && y<aY+aH && aY<y+height;
    }

    /**
     * Returns whether this rect intersects with given rect and both are not empty.
     */
    public boolean intersectsRectAndNotEmpty(Rect aRect)
    {
        boolean intersectsRect = intersectsRect(aRect);
        return intersectsRect && !isEmpty() && !aRect.isEmpty();
    }

    /**
     * Returns whether shape with line width contains point.
     */
    public boolean contains(double x, double y, double aLineWidth)
    {
        if (aLineWidth<=1) return contains(x,y);
        return getInsetRect(-aLineWidth/2).contains(x,y);
    }

    /**
     * Returns whether shape with line width intersects shape.
     */
    public boolean intersects(Shape aShape, double aLineWidth)
    {
        if (aShape instanceof Rect)
            return getInsetRect(-aLineWidth/2).intersectsRect((Rect)aShape);
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
     * Returns a copy of this rect inset by given insets.
     */
    public Rect getInsetRect(Insets anIns)
    {
        Rect rect = clone();
        if (anIns!=null)
            rect.setRect(x+anIns.left,y+anIns.top,width - anIns.getWidth(),height - anIns.getHeight());
        return rect;
    }

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
     * Sets this rect to combined bounds of both rects, even if either is empty.
     */
    public void add(Rect r2)
    {
        //if (r2==null) return;
        double nx = Math.min(x, r2.x), nw = Math.max(x + width, r2.x + r2.width) - nx;
        double ny = Math.min(y, r2.y), nh = Math.max(y + height, r2.y + r2.height) - ny;
        setRect(nx, ny, nw, nh);
    }

    /**
     * Returns rect for combined bounds of both rects, even if either is empty.
     */
    public Rect getAddRect(Rect r2)  { Rect r = clone(); r.add(r2); return r; }

    /**
     * Sets this rect to combined area with given rect. If either rect is empty, bounds are set to other rect.
     */
    public void union(Rect r2)  { union(r2.x, r2.y, r2.width, r2.height); }

    /**
     * Sets this rect to combined area with given rect. If either rect is empty, bounds are set to other rect.
     */
    public void union(double aX, double aY, double aW, double aH)
    {
        if (width<=0 || height<=0) { setRect(aX, aY, aW, aH); return; }
        if (aW<=0 || aH<=0) return;
        double nx = Math.min(x, aX), nw = Math.max(x + width, aX + aW) - nx;
        double ny = Math.min(y, aY), nh = Math.max(y + height, aY + aH) - ny;
        setRect(nx, ny, nw, nh);
    }

    /**
     * Returns rect for combined area with given rect. If either rect is empty, bounds are from other rect.
     */
    public Rect getUnionRect(Rect r2)  { Rect r = clone(); r.union(r2); return r; }

    /**
     * Unions the receiver rect with the given rect.
     */
    public void unionEvenIfEmpty(Rect r2)  { add(r2); }

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
        double x2 = Math.floor(x), w2 = Math.ceil(x+width) - x2;
        double y2 = Math.floor(y), h2 = Math.ceil(y+height) - y2;
        setRect(x2, y2, w2, h2);
    }

    /**
     * Returns an array of four points containing each corner of the rect.
     */
    public Point[] getPoints()
    {
        double x2 = x + width, y2 = y + height;
        return new Point[] { new Point(x,y), new Point(x2,y), new Point(x2,y2), new Point(x,y2) };
    }

    /**
     * Returns the rect centered inside for given size.
     */
    public Rect getRectCenteredInside(double aW, double aH)
    {
        double x = Math.round((getWidth() - aW)/2);
        double y = Math.round((getHeight() - aH)/2);
        return new Rect(x, y, aW, aH);
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
        double a = width/2, b = height/2; if (a==0 || b==0) return new Point();

        // If not elliptical, change a & b to min length so we use normal circle instead of elliptical radians
        if (!doEllipse)
            a = b = Math.min(a, b);

        // Calculate the coordinates of the point on the ellipse/circle for the given angle
        double x1 = a * MathUtils.cos(anAngle);
        double y1 = b * MathUtils.sin(anAngle);

        // First, let's assume the perimeter x coord is on the rect's left or right border
        double x2 = width/2 * MathUtils.sign(x1);

        // Then calculate the y perimeter coord by assuming y2/x2 = y1/x1
        double y2 = x2 * y1/x1;

        // If final perimeter height outside rect height, recalc but assume final perimeter y is top or bottom border
        if (Math.abs(y2)>b) {
            y2 = height/2 * MathUtils.sign(y1);
            x2 = y2 * x1/y1;
        }

        // Get point in rect coords
        return new Point(getMidX() + x2, getMidY() + y2);
    }

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
        if (anObj==this) return true;
        Rect other = anObj instanceof Rect ? (Rect) anObj : null; if (other==null) return false;
        return super.equals(other);
    }

    /**
     * Returns whether given rect parts contains given x/y.
     */
    public static boolean contains(double x, double y, double w, double h, double aX, double aY)
    {
        return (x<=aX) && (aX<=x+w) && (y<=aY) && (aY<=y+h);
    }

    /**
     * Returns a point for x, y, w, h and position.
     */
    public static Point getPointForPosition(double aX, double aY, double aW, double aH, Pos aPos)
    {
        switch(aPos) {
            case TOP_LEFT: return new Point(aX, aY);
            case TOP_CENTER: return new Point(aX+aW/2, aY);
            case TOP_RIGHT: return new Point(aX+aW, aY);
            case CENTER_LEFT: return new Point(aX, aY+aH/2);
            case CENTER: return new Point(aX+aW/2, aY+aH/2);
            case CENTER_RIGHT: return new Point(aX+aW, aY+aH/2);
            case BOTTOM_LEFT: return new Point(aX, aY+aH);
            case BOTTOM_CENTER: return new Point(aX+aW/2, aY+aH);
            case BOTTOM_RIGHT: return new Point(aX+aW, aY+aH);
            default: return null;
        }
    }

    /**
     * Returns a point for x, y, w, h and position.
     */
    public static Point getPointForPositionAndSize(Rect aRect, Pos aPos, double aW2, double aH2)
    {
        return getPointForPositionAndSize(aRect.x, aRect.y, aRect.width, aRect.height, aPos, aW2, aH2);
    }

    /**
     * Returns a point for x, y, w, h and position.
     */
    public static Point getPointForPositionAndSize(double aX, double aY, double aW, double aH, Pos aPos, double aW2, double aH2)
    {
        Point point = getPointForPosition(aX, aY, aW, aH, aPos);
        point.x -= aW2 * aPos.getHPos().doubleValue();
        point.y -= aH2 * aPos.getVPos().doubleValue();
        return point;
    }

    /**
     * Creates a rect enclosing the given array of points.
     */
    public static Rect getRectForPoints(Point ... thePoints)
    {
        // If no points, just return empty rect
        if (thePoints.length==0) return new Rect(0,0,0,0);

        // Get initial x/y/w/h
        double x = thePoints[0].x;
        double y = thePoints[0].y;
        double w = x, h = y;

        // Iterate over remaining points to get min/max
        for (int i=1; i<thePoints.length; i++) {
            x = Math.min(x, thePoints[i].x);
            y = Math.min(y, thePoints[i].y);
            w = Math.max(w, thePoints[i].x);
            h = Math.max(h, thePoints[i].y);
        }

        // Return new rect
        return new Rect(x, y, w-x, h-y);
    }

    /**
     * Creates a rect from an String in XML format as defined in toXMLString().
     */
    public static Rect getRectForString(String aString)
    {
        double x = StringUtils.doubleValue(aString);
        int start = aString.indexOf(' ', 0);
        double y = StringUtils.doubleValue(aString, start + 1);
        start = aString.indexOf(' ', start + 1);
        double w = StringUtils.doubleValue(aString, start + 1);
        start = aString.indexOf(' ', start + 1);
        double h = StringUtils.doubleValue(aString, start + 1);
        return new Rect(x, y, w, h);
    }

    /** Delete these. */
    public static Rect get(Point ... points)  { return getRectForPoints(points); }
    public static Rect get(String aString)  { return getRectForString(aString); }

    /**
     * PathIter for Rect.
     */
    private static class RectIter extends PathIter {

        // Ivars
        double x, y, w, h;
        int index;

        /** Create new RectIter. */
        RectIter(Rect r, Transform t)
        {
            super(t);
            x = r.x; y = r.y;
            w = r.width; h = r.height;
            if (w<0 || h<0) index = 5;
        }

        /** Returns whether there are more segments. */
        public boolean hasNext() { return index<5; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double[] coords)
        {
            switch(index++) {
                case 0: return moveTo(x, y, coords);
                case 1: return lineTo(x+w, y, coords);
                case 2: return lineTo(x+w, y+h, coords);
                case 3: return lineTo(x, y+h, coords);
                case 4: return close();
                default: throw new RuntimeException("Rect path iterator out of bounds " + index);
            }
        }
    }
}