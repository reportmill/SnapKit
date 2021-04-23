/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;
import snap.util.StringUtils;

/**
 * A Shape subclass to form the base class for generic rectangular shapes (Rect, Oval, Arc).
 */
public abstract class RectBase extends Shape implements Cloneable {

    // X value
    public double  x;

    // Y value
    public double  y;

    // Width value
    public double  width;

    // Height value
    public double  height;
    
    /**
     * Returns the x.
     */
    public double getX()  { return x; }

    /**
     * Sets the x.
     */
    public void setX(double aValue)
    {
        x = aValue;
        shapeChanged();
    }

    /**
     * Returns the y.
     */
    public double getY()  { return y; }

    /**
     * Sets the y.
     */
    public void setY(double aValue)
    {
        y = aValue;
        shapeChanged();
    }

    /**
     * Returns the width.
     */
    public double getWidth()  { return width; }

    /**
     * Sets the width.
     */
    public void setWidth(double aValue)
    {
        width = aValue;
        shapeChanged();
    }

    /**
     * Returns the height.
     */
    public double getHeight()  { return height; }

    /**
     * Sets the height.
     */
    public void setHeight(double aValue)
    {
        height = aValue;
        shapeChanged();
    }

    /**
     * Returns the rectangle x/y as a point.
     */
    public Point getXY()  { return new Point(x,y); }

    /**
     * Sets the rectangle x/y.
     */
    public void setXY(double aX, double aY)
    {
        setX(aX); setY(aY);
    }

    /**
     * Returns the size.
     */
    public Size getSize()  { return new Size(width, height); }

    /**
     * Sets the size.
     */
    public void setSize(double aWidth, double aHeight)
    {
        setWidth(aWidth); setHeight(aHeight);
    }

    /**
     * Sets the rect.
     */
    public void setRect(Rect r)
    {
        setRect(r.x,r.y,r.width,r.height);
    }

    /**
     * Sets the rect.
     */
    public void setRect(double x, double y, double w, double h)
    {
        setX(x); setY(y); setWidth(w); setHeight(h);
    }

    /**
     * Returns the max x.
     */
    public double getMinX()  { return x; }

    /**
     * Returns the max y.
     */
    public double getMinY()  { return y; }

    /**
     * Returns the x mid-point of the rect.
     */
    public double getMidX()  { return x + width/2; }

    /**
     * Returns the y mid-point of the rect.
     */
    public double getMidY()  { return y + height/2; }

    /**
     * Returns the max x.
     */
    public double getMaxX()  { return x + width; }

    /**
     * Returns the max y.
     */
    public double getMaxY()  { return y + height; }

    /**
     * Sets the box centered around given X/Y.
     */
    public void setCenteredXY(double aX, double aY)
    {
        int x2 = (int) Math.round(aX - width/2d);
        int y2 = (int) Math.round(aY - height/2d);
        setXY(x2, y2);
    }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        Rect rect = new Rect(x,y,width,height);
        if (width<0) { rect.x += width; rect.width = -width; }
        if (height<0) { rect.y += height; rect.height = -height; }
        return rect;
    }

    /**
     * Returns whether rect is empty.
     */
    public boolean isEmpty()  { return width<=0 || height<=0; }

    /**
     * Insets the receiver rect by the given amount.
     */
    public void inset(double anInset)
    {
        inset(anInset, anInset);
    }

    /**
     * Insets the receiver rect by the given amount.
     */
    public void inset(double xIns, double yIns)
    {
        setRect(x+xIns,y+yIns,width-2*xIns,height-2*yIns);
    }

    /**
     * Insets the receiver rect by the given amount.
     */
    public void inset(Insets anIns)
    {
        if (anIns==null) return;
        setRect(x + anIns.left,y + anIns.top,width - anIns.getWidth(),height - anIns.getHeight());
    }

    /**
     * Offsets the receiver by the given x & y.
     */
    public void offset(double dx, double dy)  { setXY(x + dx, y + dy); }

    /**
     * Returns the shape in rect.
     */
    public Shape copyFor(Rect aRect)
    {
        RectBase clone = clone();
        clone.setRect(aRect);
        return clone;
    }

    /**
     * Returns a copy of this shape transformed by given transform.
     */
    public Shape copyFor(Transform aTrans)
    {
        // If just translation, return cloned offset rect
        if (aTrans.isSimple()) {
            RectBase clone = clone();
            clone.offset(aTrans._tx, aTrans._ty);
            return clone;
        }

        // if just scale+translation, return scaled rect
        else if (!aTrans.isRotated()) {
            RectBase clone = clone();
            clone.x = clone.x*aTrans._a + aTrans._tx;
            clone.y = clone.y*aTrans._d + aTrans._ty;
            clone.width *= aTrans._a;
            clone.height *= aTrans._d;
            return clone;
        }

        // Otherwise do full version
        return super.copyFor(aTrans);
    }

    /**
     * Returns a String reprsentation of this rect.
     */
    public String getString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtils.toString(x)).append(' ').append(StringUtils.toString(y)).append(' ');
        sb.append(StringUtils.toString(width)).append(' ').append(StringUtils.toString(height));
        return sb.toString();
    }

    /**
     * Standard clone implementation.
     */
    public RectBase clone()
    {
        try { return (RectBase)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        RectBase other = anObj instanceof RectBase ? (RectBase)anObj : null; if(other==null) return false;
        return MathUtils.equals(other.x, x) && MathUtils.equals(other.y, y) &&
            MathUtils.equals(other.width, width) && MathUtils.equals(other.height, height);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + " [" + getString() + "]";
    }

    /**
     * Standard equals implementation.
     */
    public static boolean equals(Rect aRect, double aX, double aY, double aW, double aH)
    {
        return Point.equals(aRect.x, aRect.y, aX, aY) && Size.equals(aRect.width, aRect.height, aW, aH);
    }
}