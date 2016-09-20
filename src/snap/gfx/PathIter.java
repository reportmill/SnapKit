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
    Rect bounds = null;
    double f[] = new double[6], lastX = 0, lastY = 0;
    double a, b, c, d, t, x, y, det;
    
    while(aPI.hasNext()) {
        switch(aPI.getNext(f)) {
            
            // Handle MoveTo, LineTo
            case MoveTo: case LineTo:
                
                // Add end point
                if(bounds==null) bounds = new Rect(f[0], f[1], 0, 0);
                bounds.add(f[0], f[1]); lastX = f[0]; lastY = f[1]; break;
            
            // Handle Close
            case Close: break;
                
            // Handle QuadTo
            case QuadTo:
                
                // add the end point:
                if(bounds==null) bounds = new Rect(f[0], f[1], 0, 0);
                bounds.add(f[2], f[3]);
            
                // this curve might have extrema:
                a = lastX-2*f[0]+f[2]; b = -2*lastX+2*f[0]; c = lastX; t = -b/(2*a);
                if(t>0 && t<1) bounds.addX(a*t*t+b*t+c);
            
                a = lastY-2*f[1]+f[3]; b = -2*lastY+2*f[1]; c = lastY; t = -b/(2*a);
                if(t>0 && t<1)  bounds.addY(a*t*t+b*t+c);
                lastX = f[2]; lastY = f[3]; break;
                
            // Handle CubicTo
            case CubicTo:
                
                //add the end point:
                if(bounds==null) bounds = new Rect(f[0], f[1], 0, 0);
                bounds.add(f[4], f[5]);
            
                //this curve might have extrema:
                //f = a*t*t*t+b*t*t+c*t+d
                //df/dt = 3*a*t*t+2*b*t+c
                //A = 3*a, B = 2*b, C = c
                //t = [-B+-sqrt(B^2-4*A*C)]/(2A)
                //t = (-2*b+-sqrt(4*b*b-12*a*c)]/(6*a)
                a = -lastX+3*f[0]-3*f[2]+f[4]; b = 3*lastX-6*f[0]+3*f[2]; c = -3*lastX+3*f[0]; d = lastX;
                det = (4*b*b-12*a*c);
                if(det<0) { } //there are no solutions!  nothing to do here
                else if(det==0) { //there is 1 solution
                    t = -2*b/(6*a);
                    if(t>0 && t<1) {
                        x = a*t*t*t+b*t*t+c*t+d; bounds.addX(x); }
               }
               
               //there are 2 solutions:
               else {
                   det = Math.sqrt(det); t = (-2*b+det)/(6*a);
                   if(t>0 && t<1) { x = a*t*t*t+b*t*t+c*t+d; bounds.addX(x); }
                   t = (-2*b-det)/(6*a);
                   if(t>0 && t<1) { x = a*t*t*t+b*t*t+c*t+d; bounds.addX(x); }
               }
            
               //do the same thing for y:
               a = -lastY+3*f[1]-3*f[3]+f[5]; b = 3*lastY-6*f[1]+3*f[3]; c = -3*lastY+3*f[1]; d = lastY;
               det = (4*b*b-12*a*c);
               if(det<0) { } //there are no solutions!  nothing to do here
               
               //there is 1 solution
               else if(det==0) {
                   t = -2*b/(6*a);
                   if(t>0 && t<1) { y = a*t*t*t+b*t*t+c*t+d; bounds.addY(y); }
               }
               //there are 2 solutions:
               else {
                   det = Math.sqrt(det); t = (-2*b+det)/(6*a);
                   if(t>0 && t<1) { y = a*t*t*t+b*t*t+c*t+d; bounds.addY(y); }
                   t = (-2*b-det)/(6*a);
                   if(t>0 && t<1) { y = a*t*t*t+b*t*t+c*t+d; bounds.addY(y); }
               }
               lastX = f[4]; lastY = f[5]; break;
        }
    }
 
    // Return bounds
    return bounds!=null? bounds : new Rect();
}

}