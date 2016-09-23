/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * A custom class.
 */
public interface PathIter {
    
    // Constants for segments
    public enum Seg {
        
        // Constants
        MoveTo(1), LineTo(1), QuadTo(2), CubicTo(3), Close(0);
            
        // Methods
        Seg(int count)  { _count = count; } int _count;
        public int getCount() { return _count; }
    }

/**
 * Returns the next segment.
 */
public Seg getNext(double coords[]);

/**
 * Returns the next segment (float coords).
 */
default Seg getNext(float coords[])
{
    double dcoords[] = new double[6]; Seg seg = getNext(dcoords);
    for(int i=0;i<6;i++) coords[i] = (float)dcoords[i];
    return seg;
}

/**
 * Returns whether has next segment.
 */
public boolean hasNext();

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