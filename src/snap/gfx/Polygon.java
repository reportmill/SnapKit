package snap.gfx;
import java.util.Arrays;

/**
 * A custom class.
 */
public class Polygon implements Shape {

    // The points
    double      _pnts[] = new double[0];

/**
 * Creates a new Polygon from given x y coords.
 */
public Polygon(double ... theCoords)  { _pnts = theCoords; }

/**
 * Adds a point at given x/y.
 */
public void lineTo(double aX, double aY)
{
    _pnts = Arrays.copyOf(_pnts, _pnts.length+2);
    _pnts[_pnts.length-2] = aX; _pnts[_pnts.length-1] = aY;
}

/**
 * Returns the shape bounds.
 */
public Rect getBounds()
{
    if(_pnts.length==0) return new Rect();
    double xmin = _pnts[0], xmax = _pnts[0], ymin = _pnts[1], ymax = _pnts[1];
    for(int i=2;i<_pnts.length;i+=2) { double x = _pnts[i], y = _pnts[i+1];
        xmin = Math.min(xmin,x); xmax = Math.max(xmax,x); ymin = Math.min(ymin,y); ymax = Math.max(ymax,y); }
    return new Rect(xmin, ymin, xmax-xmin, ymax-ymin);
}

/**
 * Returns the path iterator.
 */
public PathIter getPathIter(Transform aTrans)  { return new PolyIter(_pnts, aTrans); }

/**
 * PathIter for Line.
 */
private static class PolyIter implements PathIter {
    
    // Ivars
    double _pnts[]; Transform trans; int index;

    /** Create new LineIter. */
    PolyIter(double thePnts[], Transform at)  { _pnts = thePnts; trans = at; }

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<_pnts.length+2; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double[] coords)
    {
        int ind = index;
        if(ind<_pnts.length) { coords[0] = _pnts[index]; coords[1] = _pnts[index+1]; }
        if(trans!=null) trans.transform(coords); index += 2;
        if(ind==0) return PathIter.Seg.MoveTo;
        if(ind<_pnts.length) return PathIter.Seg.LineTo;
        return PathIter.Seg.Close;
    }
}

}