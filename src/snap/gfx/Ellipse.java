/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * Class describes an ellipse that is defined by a framing rectangle.
 */
public class Ellipse extends RectBase {

/**
 * Creates a new Ellipse.
 */
public Ellipse() { }

/**
 * Creates a new Ellipse
 */
public Ellipse(double aX, double aY, double aW, double aH) { x = aX; y = aY; width = aW; height = aH; }

/**
 * Contains.
 */
public boolean contains(double x, double y)
{
    double ellw = getWidth(); if(ellw<=0.0) return false;
    double normx = (x - getX()) / ellw - 0.5;
    double ellh = getHeight(); if(ellh <= 0.0) return false;
    double normy = (y - getY()) / ellh - 0.5;
    return (normx * normx + normy * normy) < 0.25;
}

/**
 * Returns an iteration object that defines the boundary of this Ellipse.
 */
public PathIter getPathIter(Transform at) { return new EllipseIter(this, at); }

/**
 * Returns the hashcode for this <code>Ellipse</code>.
 */
public int hashCode()
{
    long bits = java.lang.Double.doubleToLongBits(getX());
    bits += java.lang.Double.doubleToLongBits(getY()) * 37;
    bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
    bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
    return (((int) bits) ^ ((int) (bits >> 32)));
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    if(!(anObj instanceof Ellipse)) return false;
    return super.equals(anObj);
}

/**
 * Path iterator for ellipse.
 */
private static class EllipseIter implements PathIter {
    
    // Ivars
    double x, y, w, h; Transform affine; int index;

    /** Creates new EllipseIter. */
    EllipseIter(Ellipse e, Transform at)
    {
        x = e.getX(); y = e.getY(); w = e.getWidth(); h = e.getHeight(); affine = at;
        if(w<0 || h<0) index = 6;
    }

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index <= 5; }

    // Control points for set of 4 bezier curves that approximate a circle of radius 0.5 centered at 0.5, 0.5
    public static final double CtrlVal = 0.5522847498307933;     // ArcIterator.btan(Math.PI/2)
    private static final double pcv = 0.5 + CtrlVal * 0.5;
    private static final double ncv = 0.5 - CtrlVal * 0.5;
    private static double ctrlpts[][] = {
        {  1.0,  pcv,  pcv,  1.0,  0.5,  1.0 },
        {  ncv,  1.0,  0.0,  pcv,  0.0,  0.5 },
        {  0.0,  ncv,  ncv,  0.0,  0.5,  0.0 },
        {  pcv,  0.0,  1.0,  ncv,  1.0,  0.5 }
    };

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public Seg getNext(double[] coords)
    {
        if(!hasNext()) throw new RuntimeException("ellipse iterator out of bounds");
        if(index==5) { index++; return Seg.Close; }
        if(index==0) {
            double ctrls[] = ctrlpts[3];
            coords[0] = x + ctrls[4] * w; coords[1] = y + ctrls[5] * h;
            if(affine != null) affine.transform(coords, 1);
            index++; return Seg.MoveTo;
        }
        double ctrls[] = ctrlpts[index - 1];
        coords[0] = x + ctrls[0] * w; coords[1] = y + ctrls[1] * h;
        coords[2] = x + ctrls[2] * w; coords[3] = y + ctrls[3] * h;
        coords[4] = x + ctrls[4] * w; coords[5] = y + ctrls[5] * h;
        if(affine != null) affine.transform(coords, 3);
        index++; return Seg.CubicTo;
    }
}

}