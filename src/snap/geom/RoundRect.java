/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import snap.util.MathUtils;

/**
 * A Shape subclass to represent a rounded rectangle.
 */
public class RoundRect extends RectBase {
    
    // The radius of the round
    double      _rad;
    
    // The rounded corners
    boolean     _nw = true, _ne = true, _sw = true, _se = true;
    
    /**
     * Constructor.
     */
    public RoundRect()  { }

    /**
     * Constructor.
     */
    public RoundRect(Rect aRect, double aRadius)
    {
        this(aRect.x, aRect.y, aRect.width, aRect.height, aRadius);
    }

    /**
     * Constructor.
     */
    public RoundRect(double aX, double aY, double aW, double aH, double aRadius)
    {
        x = aX; y = aY; width = aW; height = aH; _rad = aRadius;
    }

    /**
     * Returns the radius of the round.
     */
    public double getRadius()  { return _rad; }

    /**
     * Sets the radius of the round.
     */
    public void setRadius(double aValue)  { _rad = aValue;  }

    /**
     * Returns a copy with given radius.
     */
    public RoundRect copyForRadius(double aRad)
    {
        RoundRect copy = (RoundRect)clone();
        copy._rad = aRad;
        return copy;
    }

    /**
     * Returns a copy with only set corners rounded.
     */
    public RoundRect copyForCorners(boolean doNW, boolean doNE, boolean doSE, boolean doSW)
    {
        RoundRect copy = (RoundRect)clone();
        copy._nw = doNW; copy._ne = doNE;
        copy._sw = doSW; copy._se = doSE;
        return copy;
    }

    /**
     * Returns a copy of this RoundRect only rounding corners appropriate for given position.
     */
    public RoundRect copyForPosition(Pos aPos)
    {
        if (aPos==null) return this;
        switch(aPos) {
            case CENTER_LEFT: return copyForCorners(true, false, false, true);
            case CENTER: return copyForCorners(false, false, false, false);
            case CENTER_RIGHT: return copyForCorners(false, true, true, false);
            case TOP_CENTER: return copyForCorners(true, true, false, false);
            case BOTTOM_CENTER: return copyForCorners(false, false, true, true);
            case TOP_LEFT: return copyForCorners(true, false, false, false);
            case TOP_RIGHT: return copyForCorners(false, true, false, false);
            case BOTTOM_LEFT: return copyForCorners(false, false, false, true);
            case BOTTOM_RIGHT: return copyForCorners(false, false, true, false);
            default: return this;
        }
    }

    /**
     * Returns a path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        if (getRadius()<=0) return getBounds().getPathIter(aTrans);
        return new RoundRectIter(this, aTrans);
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(x); bits += Double.doubleToLongBits(y)*37;
        bits += Double.doubleToLongBits(width)*43; bits += Double.doubleToLongBits(height+_rad)*47; // Bogus
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        RoundRect other = anObj instanceof RoundRect ? (RoundRect) anObj : null; if (other==null) return false;
        if (!super.equals(anObj)) return false;
        return MathUtils.equals(other._rad,_rad);
    }

    /**
     * PathIter for RoundRect.
     */
    private static class RoundRectIter extends PathIter {

        // Ivars
        double x, y, w, h, maxx, maxy, rw, rh; int index; boolean nw, ne, sw, se;

        /** Create new RectIter. */
        RoundRectIter(RoundRect r, Transform at)
        {
            super(at);
            x = r.getX(); y = r.getY(); w = r.getWidth(); h = r.getHeight(); maxx = r.getMaxX(); maxy = r.getMaxY();
            double rad = r.getRadius();
            rw = rad>w/2 ? w/2 : rad;
            rh = rad>h/2 ? h/2 : rad;
            nw = r._nw; ne = r._ne;
            sw = r._sw; se = r._se;
            if (w<0 || h<0) index = 10;
        }

        /** Returns whether there are more segments. */
        public boolean hasNext() { return index<10; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double[] coords)
        {
            // Switch on segment index to draw corners and edges
            switch(index++) {

                // Start point
                case 0: return moveTo(nw ? x+rw : x, y, coords);

                // Top edge
                case 1: if (ne) return lineTo(maxx-rw, y, coords);
                    index++; return lineTo(maxx, y, coords);

                // Upper right corner
                case 2: return arcTo(maxx-rw, y, maxx, y, maxx, y+rh, coords);

                // Right edge
                case 3: if (se) return lineTo(maxx, maxy-rh, coords);
                    index++; return lineTo(maxx, maxy, coords);

                // Lower right corner
                case 4: return arcTo(maxx, maxy-rh, maxx, maxy, maxx-rw, maxy, coords);

                // Bottom edge
                case 5: if (sw) return lineTo(x+rw, maxy, coords);
                    index++; return lineTo(x, maxy, coords);

                // Lower left corner
                case 6: return arcTo(x+rw, maxy, x, maxy, x, maxy-rh, coords);

                // Left edge
                case 7: if (nw) return lineTo(x, y+rh, coords);
                    index += 2; return close();

                // Upper left corner
                case 8: return arcTo(x, y+rh, x, y, x+rw, y, coords);

                // Close
                case 9: return close();

                // Impossible
                default: throw new RuntimeException("RoundRectIter: index beyond bounds");
            }
        }
    }
}