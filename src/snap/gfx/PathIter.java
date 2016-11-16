/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * A class to iterate over segments in a shape, providing specific coordinate information.
 */
public abstract class PathIter {
    
    // The transform
    Transform     _trans;
    
    // Constants for segments
    public enum Seg {
        
        // Constants
        MoveTo(1), LineTo(1), QuadTo(2), CubicTo(3), Close(0);
            
        // Methods
        Seg(int count)  { _count = count; } int _count;
        public int getCount() { return _count; }
    }
    
/**
 * Creates a new PathIter.
 */
public PathIter()  { }

/**
 * Creates a new PathIter for given transform.
 */
public PathIter(Transform aTrans)  { _trans = aTrans; }

/**
 * Returns the next segment.
 */
public abstract Seg getNext(double coords[]);

/**
 * Returns the next segment (float coords).
 */
public Seg getNext(float coords[])
{
    double dcoords[] = new double[6]; Seg seg = getNext(dcoords);
    for(int i=0;i<6;i++) coords[i] = (float)dcoords[i];
    return seg;
}

/**
 * Returns whether has next segment.
 */
public abstract boolean hasNext();

/**
 * Returns a MoveTo for given coords.
 */
public final Seg moveTo(double aX, double aY, double coords[])
{
    coords[0] = aX; coords[1] = aY;
    if(_trans!=null) _trans.transform(coords, 1);
    return Seg.MoveTo;
}

/**
 * Returns a LineTo for given coords.
 */
public final Seg lineTo(double aX, double aY, double coords[])
{
    coords[0] = aX; coords[1] = aY;
    if(_trans!=null) _trans.transform(coords, 1);
    return Seg.LineTo;
}

/**
 * Returns a QuadTo for given coords.
 */
public final Seg quadTo(double aCPX, double aCPY, double aX, double aY, double coords[])
{
    coords[0] = aCPX; coords[1] = aCPY; coords[2] = aX; coords[3] = aY;
    if(_trans!=null) _trans.transform(coords, 2);
    return Seg.QuadTo;
}

/**
 * Returns a CubicTo for given coords.
 */
public final Seg cubicTo(double aCPX0, double aCPY0, double aCPX1, double aCPY1, double aX, double aY, double coords[])
{
    coords[0] = aCPX0; coords[1] = aCPY0; coords[2] = aCPX1; coords[3] = aCPY1; coords[4] = aX; coords[5] = aY;
    if(_trans!=null) _trans.transform(coords, 3);
    return Seg.CubicTo;
}

/**
 * Returns a CubicTo for start, corner and end points.
 */
public final Seg arcTo(double lx, double ly, double cx, double cy, double x, double y, double coords[])
{
    double magic = .5523f; // I calculated this in mathematica one time - probably only valid for 90 deg corner.
    double cpx1 = lx + (cx-lx)*magic, cpy1 = ly + (cy-ly)*magic;
    double cpx2 = x + (cx-x)*magic, cpy2 = y + (cy-y)*magic;
    return cubicTo(cpx1, cpy1, cpx2, cpy2,x,y, coords);
}

/**
 * Returns a close.
 */
public final Seg close()  { return Seg.Close; }

/**
 * Returns bounds rect for given PathIter.
 */
public static Rect getBounds(PathIter aPI)
{
    Rect bounds = new Rect(), bnds = null; double pts[] = new double[6], lastX = 0, lastY = 0;
    while(aPI.hasNext()) {
        switch(aPI.getNext(pts)) {
            
            // Handle MoveTo (reset bounds for initial move)
            case MoveTo: if(bnds==null) { bounds.setRect(lastX=pts[0],lastY=pts[1],0,0); continue; }
                
            // Handle LineTo
            case LineTo: bnds = Line.bounds(lastX, lastY, lastX=pts[0], lastY=pts[1], bnds); break;
                
            // Handle QuadTo
            case QuadTo: bnds = Quad.bounds(lastX, lastY, pts[0], pts[1], lastX=pts[2], lastY=pts[3], bnds); break;
                
            // Handle CubicTo
            case CubicTo:
                bnds = Cubic.bounds(lastX, lastY, pts[0], pts[1], pts[2], pts[3], lastX=pts[4], lastY=pts[5], bnds);
                break;
            
            // Handle Close
            case Close: break;
        }
        
        // Combine bounds for segment (I with this was union() instead, so it didn't include (0,0))
        bounds.add(bnds);
    }
 
    // Return bounds
    return bounds;
}

}