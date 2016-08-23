package snap.gfx;
import snap.util.MathUtils;

/**
 * A custom class.
 */
public class RoundRect extends RectBase {
    
    // The radius of the round
    double      _rad;

/**
 * Creates new Rect.
 */
public RoundRect()  { }

/**
 * Creates new Rect.
 */
public RoundRect(double aX, double aY, double aW, double aH, double aR)  { x=aX; y=aY; width=aW; height=aH; _rad=aR; }

/**
 * Returns the radius of the round.
 */
public double getRadius()  { return _rad; }

/**
 * Sets the radius of the round.
 */
public void setRadius(double aValue)  { _rad = aValue; }

/**
 * Returns a path iterator.
 */
public PathIter getPathIter(Transform aTrans)
{
    if(getRadius()<=0) return getBounds().getPathIter(aTrans);
    return new RoundRectIter(this, aTrans);
}

/**
 * Returns the shape in rect.
 */
public Shape getShapeInRect(Rect aRect)
{
    return new RoundRect(aRect.x, aRect.y, aRect.width, aRect.height, _rad);
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
    if(anObj==this) return true;
    RoundRect other = anObj instanceof RoundRect? (RoundRect)anObj : null; if(other==null) return false;
    if(!super.equals(anObj)) return false;
    return MathUtils.equals(other._rad,_rad);
}

/**
 * PathIter for RoundRect.
 */
private static class RoundRectIter implements PathIter {
    
    // Ivars
    double x, y, w, h, rad; Transform trans; int index;

    /** Create new RectIter. */
    RoundRectIter(RoundRect r, Transform at)
    {
        x = r.getX(); y = r.getY(); w = r.getWidth(); h = r.getHeight(); rad = r.getRadius(); trans = at;
        if(w<0 || h<0) index = 10;
    }

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<=9; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double[] coords)
    {
        // Get half width/height, radius width and radius height (which can't exceed half width/height)
        double hw = w/2, hh = h/2, rw = rad>hw? hw : rad, rh = rad>hh? hh : rad;
        double of = .5523f; // I calculated this in mathematica one time
        
        // Switch on segment index to draw corners and edges
        switch(index) {
            
            // Top edge
            case 0: coords[0] = x+rw; coords[1] = y; index++;
                if(trans!=null) trans.transform(coords,1); return Seg.MoveTo;
            case 1: coords[0] = x+w-rw; coords[1] = y; index++;
                if(trans!=null) trans.transform(coords,1); return Seg.LineTo;
            
            // Upper right corner
            case 2:
                coords[0] = x+w-rw+rw*of; coords[1] = y;
                coords[2] = x+w; coords[3] = y+rh*of;
                coords[4] = x+w; coords[5] = y+rh; index++;
                if(trans!=null) trans.transform(coords,3); return Seg.CubicTo;
                
            // Right edge
            case 3: coords[0] = x+w; coords[1] = y+h-rh; index++;
                if(trans!=null) trans.transform(coords,1); return Seg.LineTo;
            
            // Lower right corner
            case 4:
                coords[0] = x+w; coords[1] = y+h-rh+rh*of;
                coords[2] = x+w-rw+rw*of; coords[3] = y+h;
                coords[4] = x+w-rw; coords[5] = y+h; index++;
                if(trans!=null) trans.transform(coords,3); return Seg.CubicTo;
                
            // Bottom edge
            case 5: coords[0] = x+rw; coords[1] = y+h; index++;
                if(trans!=null) trans.transform(coords,1); return Seg.LineTo;
            
            // Lower left corner
            case 6:
                coords[0] = x+rw-rw*of; coords[1] = y+h;
                coords[2] = x; coords[3] = y+h-rh+rh*of;
                coords[4] = x; coords[5] = y+h-rh; index++;
                if(trans!=null) trans.transform(coords,3); return Seg.CubicTo;
                
            // Left edge
            case 7: coords[0] = x; coords[1] = y+rh; index++;
                if(trans!=null) trans.transform(coords,1); return Seg.LineTo;
            
            // Upper left corner
            case 8:
                coords[0] = x; coords[1] = y+rh-rh*of;
                coords[2] = x+rw-rw*of; coords[3] = y;
                coords[4] = x+rw; coords[5] = y; index++;
                if(trans!=null) trans.transform(coords,3); return Seg.CubicTo;
                
            // Close
            case 9: index++; return Seg.Close;
            
            // Impossible
            default: throw new RuntimeException("RoundRectIter: index beyond bounds");
        }
    }
}

}