/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * A class to represent a generic geometric shape (Line, Rect, Ellipse, etc.).
 */
public abstract class Shape {
    
    // The cached bounds
    protected Rect         _bounds;

/**
 * Returns the shape bounds x.
 */
public double getX()  { return getBounds().x; }

/**
 * Returns the shape bounds y.
 */
public double getY()  { return getBounds().y; }

/**
 * Returns the shape bounds width.
 */
public double getWidth()  { return getBounds().width; }

/**
 * Returns the shape bounds height.
 */
public double getHeight()  { return getBounds().height; }

/**
 * Returns the bounds.
 */
public Rect getBounds()  { return _bounds!=null? _bounds : (_bounds=getBoundsImpl()); }

/**
 * Returns the bounds.
 */
protected Rect getBoundsImpl()  { return PathIter.getBounds(getPathIter(null)); }

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
    int cross = getCrossings(aX, aY), mask = -1;
    boolean c = ((cross & mask) != 0); return c;
}

/**
 * Returns the number of crossings for the ray from given point extending to the right.
 */
public int getCrossings(double aX, double aY)
{
    int cross = 0;
    PathIter pi = getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0, mx = 0, my = 0;
    while(pi.hasNext()) {
        Seg seg = pi.getNext(pts);
        switch(seg) {
            case MoveTo:
                if(ly!=my) cross += Line.crossings(lx, ly, mx, my, aX, aY);
                lx = mx = pts[0]; ly = my = pts[1]; break;
            case LineTo: cross += Line.crossings(lx, ly, lx=pts[0], ly=pts[1], aX, aY); break;
            case QuadTo: cross += Quad.crossings(lx, ly, pts[0], pts[1], lx=pts[2], ly=pts[3], aX, aY, 0); break;
            case CubicTo:
                cross += Cubic.crossings(lx, ly, pts[0], pts[1], pts[2], pts[3], lx=pts[4], ly=pts[5], aX, aY, 0);
                break;
            case Close: if(ly!=my) cross += Line.crossings(lx, ly, lx=mx, ly=my, aX, aY); break;
        }
    }
    return cross;
}

/**
 * Returns whether shape contains shape.
 */
public boolean contains(Shape aShape)
{
    // If given shape is segment, do segment version instead
    if(aShape instanceof Segment) return containsSeg((Segment)aShape);
    
    // If bounds don't contain shape, just return false
    if(!getBounds().contains(aShape.getBounds())) return false;
    
    // Iterate over shape segments, if any segment edge intersects, return false
    PathIter pi = aShape.getPathIter(null); Line line = new Line(0,0,0,0); Quad quad = null; Cubic cub = null;
    double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        Seg seg = pi.getNext(pts);
        switch(seg) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                line.setPoints(lx, ly, lx = pts[0], ly = pts[1]);
                if(!containsSeg(line)) return false;
                break;
            case QuadTo:
                if(quad==null) quad = new Quad(0,0,0,0,0,0);
                quad.setPoints(lx, ly, pts[0], pts[1], lx = pts[2], ly = pts[3]);
                if(!containsSeg(quad)) return false;
                break;
            case CubicTo:
                if(cub==null) cub = new Cubic(0,0,0,0,0,0,0,0);
                cub.setPoints(lx, ly, pts[0], pts[1], pts[2], pts[3], lx = pts[4], ly = pts[5]);
                if(!containsSeg(cub)) return false;
                break;
            case Close:
                line.setPoints(lx, ly, lx = mx, ly = my);
                if(!containsSeg(line)) return false;
                break;
        }
    }
    
    // Return true since all shape segments are contained
    return true;
}

/**
 * Returns whether this shape intersects given shape.
 */
public boolean intersects(Shape aShape)
{
    // If given shape is segment, do segment version instead
    if(aShape instanceof Segment) return intersectsSeg((Segment)aShape);
    
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(aShape.getBounds())) return false;
    
    // Iterate over shape segments, if any segment intersects, return true
    PathIter pi = aShape.getPathIter(null); Line line = new Line(0,0,0,0); Quad quad = null; Cubic cub = null;
    double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        Seg seg = pi.getNext(pts);
        switch(seg) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                line.setPoints(lx, ly, lx = pts[0], ly = pts[1]);
                if(intersectsSeg(line)) return true;
                break;
            case QuadTo:
                if(quad==null) quad = new Quad(0,0,0,0,0,0);
                quad.setPoints(lx, ly, pts[0], pts[1], lx = pts[2], ly = pts[3]);
                if(intersectsSeg(quad)) return true;
                break;
            case CubicTo:
                if(cub==null) cub = new Cubic(0,0,0,0,0,0,0,0);
                cub.setPoints(lx, ly, pts[0], pts[1], pts[2], pts[3], lx = pts[4], ly = pts[5]);
                if(intersectsSeg(cub)) return true;
                break;
            case Close:
                line.setPoints(lx, ly, lx = mx, ly = my);
                if(intersectsSeg(line)) return true;
                break;
        }
    }
    
    // Return false since no segments intersects
    return false;
}

/**
 * Returns whether this shape contains given segment.
 */
public boolean containsSeg(Segment aSeg)
{
    // Segment is contained if this shape contains both endpoints and doesn't intersect
    if(!contains(aSeg.x0, aSeg.y0)) return false;
    if(!contains(aSeg.x1, aSeg.y1)) return false;
    if(aSeg instanceof Line) return true;
    return !crossesSeg(aSeg);
}

/**
 * Returns whether this shape intersects given segment (crosses or contains).
 */
public boolean intersectsSeg(Segment aSeg)
{
    // If segment crosses this shape, return true
    if(crossesSeg(aSeg))
        return true;
    
    // Return true if shape contains segment start point (implies that whole segment is inside)
    return contains(aSeg.x0, aSeg.y0);
}

/**
 * Returns whether any segments of this shape cross given segment.
 */
public boolean crossesSeg(Segment aSeg)
{
    // If bounds don't intersect, just return false
    if(!getBounds().intersects(aSeg.getBounds())) return false;
    
    // Iterate over local segments, if any segment intersects, return true
    PathIter pi = getPathIter(null); Line line = new Line(0,0,0,0); Quad quad = null; Cubic cub = null;
    double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0;
    while(pi.hasNext()) {
        Seg seg = pi.getNext(pts);
        switch(seg) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo:
                line.setPoints(lx, ly, lx = pts[0], ly = pts[1]);
                if(aSeg.crossesSeg(line)) return true;
                break;
            case QuadTo:
                if(quad==null) quad = new Quad(0,0,0,0,0,0);
                quad.setPoints(lx, ly, pts[0], pts[1], lx = pts[2], ly = pts[3]);
                if(aSeg.crossesSeg(quad)) return true;
                break;
            case CubicTo:
                if(cub==null) cub = new Cubic(0,0,0,0,0,0,0,0);
                cub.setPoints(lx, ly, pts[0], pts[1], pts[2], pts[3], lx = pts[4], ly = pts[5]);
                if(aSeg.crossesSeg(cub)) return true;
                break;
            case Close:
                line.setPoints(lx, ly, lx = mx, ly = my);
                if(aSeg.crossesSeg(line)) return true;
                break;
        }
    }
    
    // Return false since shape isn't crossed by segment
    return false;
}

/**
 * Returns the closest distance from given point to path.
 */
public double getDistance(double x, double y)
{
    // Iterate over segments, if any segment intersects cubic, return true
    double dist = Float.MAX_VALUE, d = dist;
    PathIter pi = getPathIter(null); double pts[] = new double[6], lx = 0, ly = 0, mx = 0, my = 0;
    while(pi.hasNext()) {
        Seg seg = pi.getNext(pts);
        switch(seg) {
            case MoveTo: mx = lx = pts[0]; my = ly = pts[1]; break;
            case LineTo: d = Line.getDistanceSquared(lx,ly,lx=pts[0],ly=pts[1],x,y); break;
            case QuadTo: d = Quad.getDistanceSquared(lx,ly,pts[0],pts[1],lx=pts[2],ly=pts[3],x,y); break;
            case CubicTo:d = Cubic.getDistanceSquared(lx,ly,pts[0],pts[1],pts[2],pts[3],lx=pts[4],ly=pts[5],x,y); break;
            case Close: d = Line.getDistanceSquared(lx,ly,lx=mx,ly=my,x,y); break;
        }
        dist = Math.min(dist, d);
    }
    
    // Return false since line hits no segments
    return Math.sqrt(dist);
}

/**
 * Returns whether shape with line width contains point.
 */
public boolean contains(double aX, double aY, double aLineWidth)
{
    // If linewidth is small return normal version
    if(aLineWidth<=1) return contains(aX,aY);
    
    // If extended bounds don't contain point, return false
    if(!getBounds().getInsetRect(-aLineWidth/2).contains(aX,aY)) return false;
    
    // If distance less than line width or this shape contains point, return true
    double dist = getDistance(aX, aY);
    return dist<=aLineWidth/2 || contains(aX, aY);
}

/**
 * Returns whether shape with line width intersects point.
 */
public boolean intersects(double aX, double aY, double aLineWidth)
{
    // If extended bounds don't contain point, return false
    if(!getBounds().getInsetRect(-aLineWidth/2).contains(aX,aY)) return false;
    
    // If distance less than line width, return true
    double dist = getDistance(aX, aY);
    return dist<=aLineWidth/2;
}

/**
 * Returns whether shape with line width intersects shape.
 */
public boolean intersects(Shape aShape, double aLineWidth)
{
    // If linewidth is small return normal version
    if(aLineWidth<=1) return intersects(aShape);
    
    // If bounds don't intersect, return false
    if(!getBounds().getInsetRect(-aLineWidth/2).intersects(aShape)) return false;
    
    // We need to outset of shape or the other
    Shape shp1 = this, shp2 = aShape; //double ins = -aLineWidth/2;
    //if(aShape.isPolygonal()) shp2 = getInsetShape(ins); else shp1 = getInsetShape(ins);
    return shp1.intersects(shp2);
}

/**
 * Returns whether shape forms a closed polygon/path, either explicitly (last segment is close) or implicitly (last
 * segment ends at last move to). Supports multiple subpaths.
 */
public boolean isClosed()
{
    // Iterate over path
    PathIter piter = getPathIter(null);
    double pts[] = new double[6], mx = 0, my = 0, lx = 0, ly = 0; boolean closed = true;
    while(piter.hasNext()) switch(piter.getNext(pts)) {
        
        // Handle MoveTo: If we were in a path, and last move-to isn't equal, return false
        case MoveTo:
            if(!closed && !Point.equals(lx,ly,mx,my))
                return false;
            mx = pts[0]; my = pts[1]; closed = true; break;
            
        // Handle LineTo
        case LineTo: lx = pts[0]; ly = pts[1]; closed = false; break;
        case QuadTo: lx = pts[2]; ly = pts[3]; closed = false; break;
        case CubicTo: lx = pts[4]; ly = pts[5]; closed = false; break;
            
        // Handle Close
        case Close: closed = true; break;
    }
    
    // Return true if last segment was an explicit close or ended at last move to point
    return closed || Point.equals(lx,ly,mx,my);
}

/**
 * Returns whether this shape is made up of only line segements.
 */
public boolean isFlat()
{
    PathIter piter = getPathIter(null); double pnts[] = new double[6];
    while(piter.hasNext()) switch(piter.getNext(pnts)) {
        case QuadTo: case CubicTo: return false; }
    return true;
}

/**
 * Returns a flattented version of this shape (just this shape if already flat).
 */
public Shape getFlat()
{
    // If already flat, just return this shape
    if(isFlat()) return this;
    
    // Create path iterate over segments to generate flat path
    Path path = new Path();
    PathIter piter = getPathIter(null); double pnts[] = new double[6];
    while(piter.hasNext()) switch(piter.getNext(pnts)) {
        case MoveTo: path.moveTo(pnts[0], pnts[1]); break;
        case LineTo: path.lineTo(pnts[0], pnts[1]); break;
        case QuadTo: path.quadToFlat(pnts[0], pnts[1], pnts[2], pnts[3]); break;
        case CubicTo: path.curveToFlat(pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]); break;
        case Close: path.close(); break;
    }
    
    // Return new path
    return path;
}

/**
 * Returns whether path is a single path (as opposed to having multiple subpaths).
 */
public boolean isSinglePath()
{
    PathIter piter = getPathIter(null); double pnts[] = new double[6]; int moveCount = 0;
    while(piter.hasNext()) switch(piter.getNext(pnts)) {
        case MoveTo: moveCount++; break;
        case LineTo: case QuadTo: case CubicTo: if(moveCount>1) return false; break;
        case Close: break;
    }
    
    // Return true
    return true;
}

/**
 * Returns whether path made up of multiple subpaths.
 */
public boolean isMultiPath()  { return !isSinglePath(); }

/**
 * Returns whether shape has no intersecting lines.
 */
public boolean isSimple()
{
    SegList slist = new SegList(this);
    return slist.isSimple();
}

/**
 * Returns the first segment end point.
 */
public Point getFirstMoveTo()
{
    PathIter pi = getPathIter(null); double pts[] = new double[6], mx = 0, my = 0;
    while(pi.hasNext()) { switch(pi.getNext(pts)) {
            case MoveTo: mx = pts[0]; my = pts[1]; break;
            case LineTo: case QuadTo: case CubicTo: return new Point(mx, my);
        }
    }
    return new Point(mx,my);
}

/**
 * Returns the shape in rect.
 */
public Shape copyFor(Rect aRect)
{
    Rect bnds = getBounds(); if(bnds.equals(aRect)) return this;
    double bw = bnds.width, bh = bnds.height;
    double sx = bw!=0? aRect.width/bw : 0, sy = bh!=0? aRect.height/bh : 0;
    Transform trans = Transform.getScale(sx, sy);
    trans.translate(aRect.x - bnds.x, aRect.y - bnds.y);
    return new Path(getPathIter(trans));
}

/**
 * Returns a copy of this shape transformed by given transform.
 */
public Shape copyFor(Transform aTrans)  { return new Path(getPathIter(aTrans)); }

/**
 * Returns a string representation of Shape.
 */
public String getString()
{
    StringBuilder sb = new StringBuilder();
    PathIter pi = getPathIter(null); double pts[] = new double[6];
    while(pi.hasNext()) {
        switch(pi.getNext(pts)) {
            case MoveTo: sb.append("M ").append(fmt(pts[0])).append(' ').append(fmt(pts[1])).append('\n'); break;
            case LineTo: sb.append("L ").append(fmt(pts[0])).append(' ').append(fmt(pts[1])).append('\n'); break;
            case QuadTo: sb.append("Q ").append(fmt(pts[0])).append(' ').append(fmt(pts[1])).append(' ')
                .append(fmt(pts[2])).append(' ').append(fmt(pts[3])).append('\n');
                break;
            case CubicTo: sb.append("C ").append(fmt(pts[0])).append(' ').append(fmt(pts[1])).append(' ')
                .append(fmt(pts[2])).append(' ').append(fmt(pts[3])).append(' ').append(fmt(pts[4])).append(' ')
                .append(fmt(pts[5])).append('\n'); break;
            case Close: sb.append("Z\n");
        }
    }
    return sb.toString();
}

/**
 * Standard to string implementation.
 */
public String toString()  { return getClass().getSimpleName() + " [" + getBounds().getString() + "] " + getString(); }

/**
 * Adds two shapes together.
 */
public static Shape add(Shape aShape1, Shape aShape2)  { return SegList.add(aShape1, aShape2); }

/**
 * Subtracts two shapes together.
 */
public static Shape subtract(Shape aShape1, Shape aShape2)  { return SegList.subtract(aShape1, aShape2); }

/**
 * Returns the intersection shape of two shapes.
 */
public static Shape intersect(Shape aShape1, Shape aShape2)  { return SegList.intersect(aShape1, aShape2); }

/**
 * Returns a simple shape for complex shape.
 */
public static Shape makeSimple(Shape aShape)  { return SegList.makeSimple(aShape); }

/** Helper. */
private static String fmt(double aVal)  { return _fmt.format(aVal); }
static java.text.DecimalFormat _fmt = new java.text.DecimalFormat("#");

}