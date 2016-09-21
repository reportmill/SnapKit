/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.text.DecimalFormat;
import snap.swing.AWT;

/**
 * A custom class.
 */
public abstract class Shape {

/**
 * Returns the bounds.
 */
public abstract Rect getBounds();

/**
 * Returns a path iterator.
 */
public abstract PathIter getPathIter(Transform aT);

/**
 * Returns whether shape contains point.
 */
public boolean contains(Point aPnt)  { return contains(aPnt.getX(), aPnt.getY()); }

/**
 * Returns whether shape contains x/y.
 */
public boolean contains(double aX, double aY)
{
    if(!getBounds().contains(aX, aY)) return false;
    return java.awt.geom.Path2D.contains(AWT.get(getPathIter(null)), aX, aY);
}

/**
 * Returns whether shape contains shape.
 */
public boolean contains(Shape aShape)
{
    if(!getBounds().contains(aShape)) return false;
    Area area1 = area(this), area2 = (Area)area1.clone(); area2.add(area(aShape));
    return area1.equals(area2);
}

/**
 * Returns whether shape intersects shape.
 */
public boolean intersects(Shape aShape)
{
    if(!getBounds().intersects(aShape.getBounds())) return false;
    Area area1 = area(this), area2 = area(aShape); area1.intersect(area2);
    return !area1.isEmpty();
}

/**
 * Returns whether shape with line width contains point.
 */
public boolean contains(double aX, double aY, double aLineWidth)
{
    // If linewidth is small return normal version
    if(aLineWidth<=1) return contains(aX,aY);
    
    // If bounds don't contain point, return false
    Rect bounds = getBounds().getInsetRect(-aLineWidth/2); if(!bounds.contains(aX,aY)) return false;
    
    // Get stroked shape
    java.awt.Shape shape1 = AWT.get(this);
    BasicStroke bstroke = new BasicStroke((float)aLineWidth);
    java.awt.Shape shape2 = bstroke.createStrokedShape(shape1);
    
    // Get area of stroked shape
    Area area1 = new Area(shape1); if(area1.isEmpty()) return shape2.contains(aX,aY);
    Area area2 = new Area(shape2); area2.add(area1);
        
    // Return whether stroked shape area contains given point
    return area2.contains(aX,aY);
}

/**
 * Returns whether shape with line width intersects shape.
 */
public boolean intersects(Shape aShape, double aLineWidth)
{
    // If bounds don't intersect, return false
    if(!getBounds().getInsetRect(-aLineWidth/2).intersects(aShape)) return false;
    
    // Get stroked shape
    java.awt.Shape shape1 = AWT.get(this);
    BasicStroke bstroke = new BasicStroke((float)aLineWidth*8); 
    java.awt.Shape shape2 = bstroke.createStrokedShape(shape1);
    
    // Get area of stroked shape
    Area area1 = new Area(shape1);
    Area area2 = new Area(shape2); if(!area1.isEmpty()) area2.add(area1);
    
    // Return whether stroked shape area intersects given shape
    area2.intersect(area(aShape));
    return !area2.isEmpty();
}

/**
 * Returns the shape in rect.
 */
public Shape getShapeInRect(Rect aRect)
{
    Rect bounds = getBounds(); if(bounds.equals(aRect)) return this;
    Transform trans = Transform.getTrans(aRect.getX() - bounds.getX(), aRect.getY() - bounds.getY());
    double bw = bounds.getWidth(), bh = bounds.getHeight();
    double sx = bw!=0? aRect.getWidth()/bw : 0, sy = bh!=0? aRect.getHeight()/bh : 0;
    trans.scale(sx, sy);
    return Path.get(getPathIter(trans));
}

/**
 * Returns a string representation of Shape.
 */
public String getString()
{
    String str = "{ "; PathIter pi = getPathIter(null); double pts[] = new double[6];
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: str += "M " + fmt(pts[0]) + "," + fmt(pts[1]) + " "; break;
            case LineTo: str += "L " + fmt(pts[0]) + "," + fmt(pts[1]) + " "; break;
            case QuadTo: str += "Q " + fmt(pts[2]) + "," + fmt(pts[3]) + " "; break;
            case CubicTo: str += "C " + fmt(pts[4]) + "," + fmt(pts[5]) + " "; break;
            case Close: str += "CLS ";
        }
    }
    return str + "}";
}

// Formater
private static String fmt(double aVal)  { return _fmt.format(aVal); }
private static DecimalFormat _fmt = new DecimalFormat("0.##");

/**
 * Standard to string implementation.
 */
public String toString()  { return getClass().getSimpleName() + getString(); }

/**
 * Returns bounds rect for given PathIter.
 */
public static Rect getBounds(Shape aShape)  { return PathIter.getBounds(aShape.getPathIter(null)); }

/**
 * Adds two shapes together.
 */
public static Shape add(Shape aShape1, Shape aShape2)
{
    Area a1 = area(aShape1), a2 = area(aShape2);
    a1.add(a2);
    return AWT.get(a1);
}

/**
 * Subtracts two shapes together.
 */
public static Shape subtract(Shape aShape1, Shape aShape2)
{
    Area a1 = area(aShape1), a2 = area(aShape2);
    a1.subtract(a2);
    return AWT.get(a1);
}

/**
 * Returns the intersection shape of two shapes.
 */
public static Shape intersect(Shape aShape1, Shape aShape2)
{
    if(aShape1 instanceof Rect && aShape2 instanceof Rect)
        return ((Rect)aShape1).getIntersectRect((Rect)aShape2);
    Area a1 = area(aShape1), a2 = area(aShape2);
    a1.intersect(a2);
    return AWT.get(a1);
}

/**
 * Returns an area for a Shape.
 */
static Area area(Shape aShape)  { return new Area(AWT.get(aShape)); }

}