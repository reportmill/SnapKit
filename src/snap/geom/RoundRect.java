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
    private double _radius;
    
    // The rounded corners
    private boolean _roundNW = true, _roundNE = true, _roundSW = true, _roundSE = true;
    
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
        x = aX; y = aY;
        width = aW; height = aH;
        _radius = aRadius;
    }

    /**
     * Returns the radius of the round.
     */
    public double getRadius()  { return _radius; }

    /**
     * Sets the radius of the round.
     */
    public void setRadius(double aValue)  { _radius = aValue;  }

    /**
     * Returns a copy with given radius.
     */
    public RoundRect copyForRadius(double aRad)
    {
        RoundRect copy = (RoundRect) clone();
        copy._radius = aRad;
        return copy;
    }

    /**
     * Returns a copy with only set corners rounded.
     */
    public RoundRect copyForCorners(boolean doNW, boolean doNE, boolean doSE, boolean doSW)
    {
        RoundRect copy = (RoundRect) clone();
        copy._roundNW = doNW; copy._roundNE = doNE;
        copy._roundSW = doSW; copy._roundSE = doSE;
        return copy;
    }

    /**
     * Returns a copy of this RoundRect only rounding corners appropriate for given position.
     */
    public RoundRect copyForPosition(Pos aPos)
    {
        if (aPos == null)
            return this;
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
        if (width <= 1 || height <= 1 || _radius < 1)
            return getBounds().getPathIter(aTrans);
        return new RoundRectIter(this, aTrans);
    }

    /**
     * Standard hashCode implementation.
     */
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(x);
        bits += Double.doubleToLongBits(y) * 37;
        bits += Double.doubleToLongBits(width) * 43;
        bits += Double.doubleToLongBits(height + _radius) * 47; // Bogus
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        RoundRect other = anObj instanceof RoundRect ? (RoundRect) anObj : null; if (other == null) return false;
        if (!super.equals(anObj)) return false;
        return MathUtils.equals(other._radius, _radius);
    }

    /**
     * PathIter for RoundRect.
     */
    private static class RoundRectIter extends PathIter {

        // Ivars
        double x, y, w, h, maxX, maxY;
        double roundW, roundH;
        boolean roundNW, roundNE, roundSW, roundSE;
        boolean noTopOrBottomEdges, noRightOrLeftEdges;
        int index;

        /** Create new RectIter. */
        RoundRectIter(RoundRect r, Transform at)
        {
            super(at);
            x = r.getX(); y = r.getY();
            w = r.getWidth(); h = r.getHeight();
            maxX = r.getMaxX(); maxY = r.getMaxY();
            double rad = r.getRadius();
            roundW = Math.min(rad, w / 2);
            roundH = Math.min(rad, h / 2);
            noTopOrBottomEdges = MathUtils.lte(w / 2, rad);
            noRightOrLeftEdges = MathUtils.lte(h / 2, rad);
            roundNW = r._roundNW; roundNE = r._roundNE;
            roundSW = r._roundSW; roundSE = r._roundSE;
            if (w < 0 || h < 0)
                index = 10;
        }

        /** Returns whether there are more segments. */
        public boolean hasNext() { return index<10; }

        /** Returns the coordinates and type of the current path segment in the iteration. */
        public Seg getNext(double[] coords)
        {
            // Switch on segment index to draw corners and edges
            switch(index++) {

                // Start point
                case 0: return moveTo(roundNW ? x + roundW : x, y, coords);

                // Top edge
                case 1:
                    if (noTopOrBottomEdges && roundNE)
                        return getNext(coords);
                    if (roundNE)
                        return lineTo(maxX - roundW, y, coords);
                    index++;
                    return lineTo(maxX, y, coords);

                // Upper right corner
                case 2: return arcTo(maxX - roundW, y, maxX, y, maxX, y + roundH, coords);

                // Right edge
                case 3:
                    if (noRightOrLeftEdges && roundSE)
                        return getNext(coords);
                    if (roundSE)
                        return lineTo(maxX, maxY - roundH, coords);
                    index++;
                    return lineTo(maxX, maxY, coords);

                // Lower right corner
                case 4: return arcTo(maxX, maxY - roundH, maxX, maxY, maxX - roundW, maxY, coords);

                // Bottom edge
                case 5:
                    if (noTopOrBottomEdges && roundSW)
                        return getNext(coords);
                    if (roundSW)
                        return lineTo(x + roundW, maxY, coords);
                    index++;
                    return lineTo(x, maxY, coords);

                // Lower left corner
                case 6: return arcTo(x + roundW, maxY, x, maxY, x, maxY - roundH, coords);

                // Left edge
                case 7:
                    if (noRightOrLeftEdges && roundNW)
                        return getNext(coords);
                    if (roundNW)
                        return lineTo(x, y + roundH, coords);
                    index += 2;
                    return close();

                // Upper left corner
                case 8: return arcTo(x, y + roundH, x, y, x + roundW, y, coords);

                // Close
                case 9: return close();

                // Impossible
                default: throw new RuntimeException("RoundRectIter: index beyond bounds");
            }
        }
    }
}