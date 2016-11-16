/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.Arrays;

/**
 * A custom class.
 */
public class Polygon extends Shape {

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
private static class PolyIter extends PathIter {
    
    // Ivars
    double _pnts[]; int plen, index;

    /** Create new LineIter. */
    PolyIter(double thePnts[], Transform at)  { super(at); _pnts = thePnts; plen = _pnts.length; }

    /** Returns whether there are more segments. */
    public boolean hasNext() { return index<plen+2; }

    /** Returns the coordinates and type of the current path segment in the iteration. */
    public PathIter.Seg getNext(double[] coords)
    {
        if(index==0)
            return moveTo(_pnts[index++], _pnts[index++], coords);
        if(index<plen)
            return lineTo(_pnts[index++], _pnts[index++], coords);
        if(index>plen)
            throw new RuntimeException("PolygonIter: Index beyond bounds " + index + " " + plen);
        index += 2; return close();
    }
}

}