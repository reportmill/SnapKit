/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * Class describes an ellipse that is defined by a framing rectangle.
 */
public class Ellipse extends RectBase {

    /**
     * Constructor.
     */
    public Ellipse()
    {
        super();
    }

    /**
     * Constructor with bounds.
     */
    public Ellipse(double aX, double aY, double aW, double aH)
    {
        super();
        x = aX;
        y = aY;
        width = aW;
        height = aH;
    }

    /**
     * Contains.
     */
    public boolean contains(double aX, double aY)
    {
        double shapeW = getWidth();
        if (shapeW <= 0)
            return false;
        double shapeH = getHeight();
        if (shapeH <= 0)
            return false;

        double normX = (aX - getX()) / shapeW - 0.5;
        double normY = (aY - getY()) / shapeH - 0.5;
        return (normX * normX + normY * normY) < 0.25;
    }

    /**
     * Returns an iteration object that defines the boundary of this Ellipse.
     */
    public PathIter getPathIter(Transform at)
    {
        return new EllipseIter(this, at);
    }

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
        if (anObj == this) return true;
        if (!(anObj instanceof Ellipse)) return false;
        return super.equals(anObj);
    }

    /**
     * Path iterator for ellipse.
     */
    private static class EllipseIter extends PathIter {

        // Ivars
        double x, y, w, h, midx, midy, maxx, maxy;
        int index;

        /**
         * Creates new EllipseIter.
         */
        EllipseIter(Ellipse e, Transform at)
        {
            super(at);
            x = e.x;
            y = e.y;
            w = e.width;
            h = e.height;
            midx = e.getMidX();
            midy = e.getMidY();
            maxx = e.getMaxX();
            maxy = e.getMaxY();
            if (w < 0 || h < 0) index = 6;
        }

        /**
         * Returns whether there are more segments.
         */
        public boolean hasNext()  { return index < 6; }

        /**
         * Returns the coordinates and type of the current path segment in the iteration.
         */
        public Seg getNext(double[] coords)
        {
            switch (index++) {
                case 0: return moveTo(midx, y, coords);
                case 1: return arcTo(midx, y, maxx, y, maxx, midy, coords);
                case 2: return arcTo(maxx, midy, maxx, maxy, midx, maxy, coords);
                case 3: return arcTo(midx, maxy, x, maxy, x, midy, coords);
                case 4: return arcTo(x, midy, x, y, midx, y, coords);
                case 5: return close();
                default: throw new RuntimeException("line iterator out of bounds");
            }
        }
    }
}