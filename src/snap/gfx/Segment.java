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
 * Override to return false (segment can never contain another segment (well, I suppose a weird Cubic could)).
 */
public boolean contains(Segment aSeg)  { return false; }

/**
 * Returns whether this segment intersects shape.
 */
public boolean intersects(Segment aSeg)  { return getHit(aSeg)!=null; }

/**
 * Returns the hit for given segment.
 */
public SegHit getHit(Segment aSeg)  { throw new RuntimeException("Segement.getHit: Unsupported class " + getClass()); }

/**
 * Returns the hit for given segment.
 */
public double getHitPoint(Segment aSeg)  { SegHit hit = getHit(aSeg); return hit!=null? hit.h0 : -1; }

/**
 * Returns whether double values are equal to nearest tenth of pixel.
 */
public static final boolean equals(double v1, double v2)  { return Math.abs(v1 - v2) < 0.1; }

}