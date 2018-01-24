package snap.gfx;

/**
 * A Segment is a Shape subclass that represents a part of a path: line, quadratic, cubic.
 */
public abstract class Segment extends Shape {

    // Ivars
    public double x0, y0, x1, y1;
    
/**
 * Returns the first point x.
 */
public double getX0()  { return x0; }

/**
 * Returns the first point y.
 */
public double getY0()  { return y0; }

/**
 * Returns the second point x.
 */
public double getX1()  { return x1; }

/**
 * Returns the second point y.
 */
public double getY1()  { return y1; }

/**
 * Returns the x value at given parametric location.
 */
public abstract double getX(double aLoc);

/**
 * Returns the y value at given parametric location.
 */
public abstract double getY(double aLoc);

/**
 * Splits the segement at given parametric location and return the remainder.
 */
public abstract Segment split(double aLoc);

/**
 * Creates and returns the reverse of this segement.
 */
public abstract Segment createReverse();

/**
 * Returns whether segement is equal to another, regardless of direction.
 */
public abstract boolean matches(Object anObj);

/**
 * Returns whether shape intersects shape.
 */
public boolean intersects(Shape aShape)
{
    // If Segment, do simple case
    if(aShape instanceof Segment)
        return getHitPoint((Segment)aShape)>=0;
        
    // Do normal version
    return super.intersects(aShape);
}

/**
 * Returns the hit point for shape 1 on shape 2.
 */
public double getHitPoint(Segment aShape2)
{
    if(this instanceof Line) { Line s1 = (Line)this;
        if(aShape2 instanceof Line) { Line s2 = (Line)aShape2;
            return Line.getHitPointLine(s1.x0, s1.y0, s1.x1, s1.y1, s2.x0, s2.y0, s2.x1, s2.y1, false); }
        if(aShape2 instanceof Quad) { Quad s2 = (Quad)aShape2;
            return Quad.getHitPointLine(s2.x0, s2.y0, s2.xc0, s2.yc0, s2.x1, s2.y1, s1.x0, s1.y0, s1.x1, s1.y1, true); }
        if(aShape2 instanceof Cubic) { Cubic s2 = (Cubic)aShape2;
            return Cubic.getHitPointLine(s2.x0, s2.y0, s2.xc0, s2.yc0, s2.xc1, s2.yc1, s2.x1, s2.y1, 
                s1.x0,s1.y0,s1.x1,s1.y1,true); }
        throw new RuntimeException("Segment: Unsupported hit class " + aShape2.getClass());
    }
    
    if(this instanceof Quad) { Quad s1 = (Quad)this;
        if(aShape2 instanceof Line) { Line s2 = (Line)aShape2;
            return Quad.getHitPointLine(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.x1, s1.y1, s2.x0, s2.y0, s2.x1, s2.y1,false); }
        if(aShape2 instanceof Quad) { Quad s2 = (Quad)aShape2;
            return Quad.getHitPointQuad(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.xc0, s2.yc0, s2.x1, s2.y1, false); }
        if(aShape2 instanceof Cubic) { Cubic s2 = (Cubic)aShape2;
            return Cubic.getHitPointQuad(s2.x0, s2.y0, s2.xc0, s2.yc0, s2.xc1, s2.yc1, s2.x1, s2.y1, 
                s1.x0, s1.y0, s1.xc0, s1.yc0, s1.x1, s1.y1, true); }
        throw new RuntimeException("Segment: Unsupported hit class " + aShape2.getClass());
    }
    
    if(this instanceof Cubic) { Cubic s1 = (Cubic)this;
        if(aShape2 instanceof Line) { Line s2 = (Line)aShape2;
            return Cubic.getHitPointLine(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.xc1, s1.yc1, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.x1, s2.y1, false); }
        if(aShape2 instanceof Quad) { Quad s2 = (Quad)aShape2;
            return Cubic.getHitPointQuad(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.xc1, s1.yc1, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.xc0, s2.yc0, s2.x1, s2.y1, false); }
        if(aShape2 instanceof Cubic) { Cubic s2 = (Cubic)aShape2;
            return Cubic.getHitPointCubic(s1.x0, s1.y0, s1.xc0, s1.yc0, s1.xc1, s1.yc1, s1.x1, s1.y1,
                s2.x0, s2.y0, s2.xc0, s2.yc0, s2.xc1, s2.yc1, s2.x1, s2.y1, false); }
        throw new RuntimeException("Segment: Unsupported hit class " + aShape2.getClass());
    }
    
    throw new RuntimeException("Segement: Unsupported hit class " + getClass());
}

/**
 * Returns whether double values are equal to nearest tenth of pixel.
 */
public static final boolean equals(double v1, double v2)  { return Math.abs(v1 - v2) < 0.1; }

}