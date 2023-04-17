/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * A class to represent a generic geometric shape (Line, Rect, Ellipse, etc.).
 */
public abstract class Shape {
    
    // The cached bounds
    protected Rect  _bounds;

    // The cached length
    private double  _arcLen = -1;

    // Constants for winding
    public static final int WIND_EVEN_ODD = PathIter.WIND_EVEN_ODD;
    public static final int WIND_NON_ZERO = PathIter.WIND_NON_ZERO;

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
    public Rect getBounds()
    {
        if (_bounds!=null) return _bounds;
        return _bounds = getBoundsImpl();
    }

    /**
     * Returns the bounds.
     */
    protected Rect getBoundsImpl()
    {
        PathIter pathIter = getPathIter(null);
        return PathIter.getBounds(pathIter);
    }

    /**
     * Returns the total length of all shape segements.
     */
    public double getArcLength()
    {
        if (_arcLen>=0) return _arcLen;
        PathIter pathIter = getPathIter(null);
        return _arcLen = PathIter.getArcLength(pathIter);
    }

    /**
     * Returns a path iterator.
     */
    public abstract PathIter getPathIter(Transform aT);

    /**
     * Returns whether shape contains point.
     */
    public boolean contains(Point aPoint)
    {
        return contains(aPoint.x, aPoint.y);
    }

    /**
     * Returns whether shape contains x/y.
     */
    public boolean contains(double aX, double aY)
    {
        if (!getBounds().contains(aX, aY))
            return false;
        int cross = getCrossings(aX, aY);
        int mask = -1;
        boolean contains = ((cross & mask) != 0);
        return contains;
    }

    /**
     * Returns the number of crossings for the ray from given point extending to the right.
     */
    public int getCrossings(double aX, double aY)
    {
        // Get path iterator and declare iter vars
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];
        double moveX = 0;
        double moveY = 0;
        double lineX = 0;
        double lineY = 0;
        int cross = 0;

        // Iterate over path segments
        while (pathIter.hasNext()) {
            Seg seg = pathIter.getNext(points);
            switch (seg) {
                case MoveTo:
                    if (lineY != moveY)
                        cross += Line.crossings(lineX, lineY, moveX, moveY, aX, aY);
                    lineX = moveX = points[0];
                    lineY = moveY = points[1];
                    break;
                case LineTo:
                    cross += Line.crossings(lineX, lineY, lineX = points[0], lineY = points[1], aX, aY);
                    break;
                case QuadTo:
                    cross += Quad.crossings(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3], aX, aY, 0);
                    break;
                case CubicTo:
                    cross += Cubic.crossings(lineX, lineY, points[0], points[1], points[2], points[3],
                            lineX = points[4], lineY = points[5], aX, aY, 0);
                    break;
                case Close:
                    if (lineY!=moveY)
                        cross += Line.crossings(lineX, lineY, lineX = moveX, lineY = moveY, aX, aY);
                    break;
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
        if (aShape instanceof Segment)
            return containsSeg((Segment)aShape);

        // If bounds don't contain shape, just return false
        Rect bnds0 = getBounds(), bnds1 = aShape.getBounds();
        if (!bnds0.containsRect(bnds1))
            return false;

        // Get path iterator and declare iter vars
        PathIter pathIter = aShape.getPathIter(null);
        double[] points = new double[6];
        double moveX = 0;
        double moveY = 0;
        double lineX = 0;
        double lineY = 0;
        Line line = new Line(0,0,0,0);
        Quad quad = null;
        Cubic cub = null;

        // Iterate over shape segments, if any segment edge intersects, return false
        while (pathIter.hasNext()) {
            Seg seg = pathIter.getNext(points);
            switch (seg) {
                case MoveTo:
                    moveX = lineX = points[0];
                    moveY = lineY = points[1];
                    break;
                case LineTo:
                    line.setPoints(lineX, lineY, lineX = points[0], lineY = points[1]);
                    if (!containsSeg(line))
                        return false;
                    break;
                case QuadTo:
                    if (quad==null)
                        quad = new Quad(0,0,0,0,0,0);
                    quad.setPoints(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3]);
                    if (!containsSeg(quad))
                        return false;
                    break;
                case CubicTo:
                    if (cub==null)
                        cub = new Cubic(0,0,0,0,0,0,0,0);
                    cub.setPoints(lineX, lineY, points[0], points[1], points[2], points[3], lineX = points[4], lineY = points[5]);
                    if (!containsSeg(cub))
                        return false;
                    break;
                case Close:
                    line.setPoints(lineX, lineY, lineX = moveX, lineY = moveY);
                    if (!containsSeg(line))
                        return false;
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
        if (aShape instanceof Segment)
            return intersectsSeg((Segment)aShape);

        // If bounds don't intersect, just return false
        Rect bnds0 = getBounds(), bnds1 = aShape.getBounds();
        if (!bnds0.intersectsRect(bnds1))
            return false;

        // If other shape bounds contains this shape bounds, have other shape do check
        if (bnds1.containsRect(bnds0))
            return aShape.intersects(this);

        // Get path iterator and declare iter vars
        PathIter pathIter = aShape.getPathIter(null);
        double[] points = new double[6];
        double moveX = 0;
        double moveY = 0;
        double lineX = 0;
        double lineY = 0;
        Line line = new Line(0,0,0,0);
        Quad quad = null;
        Cubic cub = null;

        // Iterate over shape segments, if any segment intersects, return true
        while (pathIter.hasNext()) {
            Seg seg = pathIter.getNext(points);
            switch (seg) {
                case MoveTo:
                    moveX = lineX = points[0];
                    moveY = lineY = points[1];
                    break;
                case LineTo:
                    line.setPoints(lineX, lineY, lineX = points[0], lineY = points[1]);
                    if (intersectsSeg(line))
                        return true;
                    break;
                case QuadTo:
                    if (quad==null)
                        quad = new Quad(0,0,0,0,0,0);
                    quad.setPoints(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3]);
                    if (intersectsSeg(quad))
                        return true;
                    break;
                case CubicTo:
                    if (cub==null)
                        cub = new Cubic(0,0,0,0,0,0,0,0);
                    cub.setPoints(lineX, lineY, points[0], points[1], points[2], points[3], lineX = points[4], lineY = points[5]);
                    if (intersectsSeg(cub))
                        return true;
                    break;
                case Close:
                    line.setPoints(lineX, lineY, lineX = moveX, lineY = moveY);
                    if (intersectsSeg(line))
                        return true;
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
        if (!contains(aSeg.x0, aSeg.y0))
            return false;
        if (!contains(aSeg.x1, aSeg.y1))
            return false;
        if (aSeg instanceof Line)
            return true;
        return !crossesSeg(aSeg);
    }

    /**
     * Returns whether this shape intersects given segment (crosses or contains).
     */
    public boolean intersectsSeg(Segment aSeg)
    {
        // If segment crosses this shape, return true
        if (crossesSeg(aSeg))
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
        if (!getBounds().intersectsRect(aSeg.getBounds()))
            return false;

        // Get path iterator and declare iter vars
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];
        double moveX = 0;
        double moveY = 0;
        double lineX = 0;
        double lineY = 0;
        Line line = new Line(0,0,0,0);
        Quad quad = null;
        Cubic cub = null;

        // Iterate over local segments, if any segment intersects, return true
        while (pathIter.hasNext()) {
            Seg seg = pathIter.getNext(points);
            switch (seg) {
                case MoveTo:
                    moveX = lineX = points[0]; moveY = lineY = points[1];
                    break;
                case LineTo:
                    line.setPoints(lineX, lineY, lineX = points[0], lineY = points[1]);
                    if (aSeg.crossesSeg(line))
                        return true;
                    break;
                case QuadTo:
                    if (quad==null)
                        quad = new Quad(0,0,0,0,0,0);
                    quad.setPoints(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3]);
                    if (aSeg.crossesSeg(quad))
                        return true;
                    break;
                case CubicTo:
                    if (cub==null)
                        cub = new Cubic(0,0,0,0,0,0,0,0);
                    cub.setPoints(lineX, lineY, points[0], points[1], points[2], points[3], lineX = points[4], lineY = points[5]);
                    if (aSeg.crossesSeg(cub))
                        return true;
                    break;
                case Close:
                    line.setPoints(lineX, lineY, lineX = moveX, lineY = moveY);
                    if (aSeg.crossesSeg(line))
                        return true;
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
        // Get path iterator and declare iter vars
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];
        double moveX = 0;
        double moveY = 0;
        double lineX = 0;
        double lineY = 0;
        double minDist = Float.MAX_VALUE;
        double dist = minDist;

        // Iterate over segments, track minimum distanceSquared to segment
        while (pathIter.hasNext()) {
            Seg seg = pathIter.getNext(points);
            switch (seg) {
                case MoveTo:
                    moveX = lineX = points[0];
                    moveY = lineY = points[1];
                    break;
                case LineTo:
                    dist = Line.getDistanceSquared(lineX, lineY, lineX = points[0], lineY = points[1], x, y);
                    break;
                case QuadTo:
                    dist = Quad.getDistanceSquared(lineX, lineY, points[0], points[1], lineX = points[2], lineY = points[3], x, y);
                    break;
                case CubicTo:
                    dist = Cubic.getDistanceSquared(lineX, lineY, points[0], points[1], points[2], points[3],
                            lineX = points[4], lineY = points[5], x, y);
                    break;
                case Close:
                    dist = Line.getDistanceSquared(lineX, lineY, lineX = moveX, lineY = moveY, x, y);
                    break;
            }
            minDist = Math.min(minDist, dist);
        }

        // Return min distance
        return Math.sqrt(minDist);
    }

    /**
     * Returns whether shape with line width contains point.
     */
    public boolean contains(double aX, double aY, double aLineWidth)
    {
        // If linewidth is small return normal version
        if (aLineWidth<=1)
            return contains(aX,aY);

        // If extended bounds don't contain point, return false
        if (!getBounds().getInsetRect(-aLineWidth/2).contains(aX,aY))
            return false;

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
        if (!getBounds().getInsetRect(-aLineWidth/2).contains(aX,aY))
            return false;

        // If distance less than line width, return true
        double dist = getDistance(aX, aY);
        return dist <= aLineWidth/2;
    }

    /**
     * Returns whether shape with line width intersects shape.
     */
    public boolean intersects(Shape aShape, double aLineWidth)
    {
        // If linewidth is small return normal version
        if (aLineWidth<=1)
            return intersects(aShape);

        // If bounds don't intersect, return false
        if (!getBounds().getInsetRect(-aLineWidth/2).intersects(aShape))
            return false;

        // We need to outset of shape or the other
        Shape shp1 = this;
        Shape shp2 = aShape; //double ins = -aLineWidth/2;
        //if (aShape.isPolygonal()) shp2 = getInsetShape(ins); else shp1 = getInsetShape(ins);
        return shp1.intersects(shp2);
    }

    /**
     * Returns whether shape forms a closed polygon/path, either explicitly (last segment is close) or implicitly (last
     * segment ends at last move to). Supports multiple subpaths.
     */
    public boolean isClosed()
    {
        // Get path iterator and declare iter vars
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];
        double moveX = 0;
        double moveY = 0;
        double lineX = 0;
        double lineY = 0;
        boolean closed = true;

        // Iterate over path
        while (pathIter.hasNext()) {
            switch (pathIter.getNext(points)) {

                // Handle MoveTo: If we were in a path, and last move-to isn't equal, return false
                case MoveTo:
                    if (!closed && !Point.equals(lineX, lineY, moveX, moveY))
                        return false;
                    moveX = points[0];
                    moveY = points[1];
                    closed = true;
                    break;
                case LineTo:
                    lineX = points[0];
                    lineY = points[1];
                    closed = false;
                    break;
                case QuadTo:
                    lineX = points[2];
                    lineY = points[3];
                    closed = false;
                    break;
                case CubicTo:
                    lineX = points[4];
                    lineY = points[5];
                    closed = false;
                    break;
                case Close:
                    closed = true;
                    break;
            }
        }

        // Return true if last segment was an explicit close or ended at last move to point
        return closed || Point.equals(lineX,lineY,moveX,moveY);
    }

    /**
     * Returns whether this shape is made up of only line segements.
     */
    public boolean isFlat()
    {
        // Get path iterator and declare iter vars
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];

        // Iterate over path
        while (pathIter.hasNext()) switch (pathIter.getNext(points)) {
            case QuadTo: case CubicTo: return false; }
        return true;
    }

    /**
     * Returns a flattented version of this shape (just this shape if already flat).
     */
    public Shape getFlattenedShape()
    {
        return getFlattenedShape(PolygonPath.DEFAULT_FLAT_DISTANCE);
    }

    /**
     * Returns a flattented version of this shape (just this shape if already flat).
     */
    public Shape getFlattenedShape(double aFlatDistance)
    {
        // If already flat, just return this shape
        if (isFlat())
            return this;

        // Create and return PolygonPath for shape
        return new PolygonPath(this, aFlatDistance);
    }

    /**
     * Returns whether path has only a single moveto.
     */
    public boolean isSingleCycle()
    {
        // Get path iterator and declare iter vars
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];
        int moveCount = 0;

        // Iterate over segments to generate flat path
        while (pathIter.hasNext()) switch (pathIter.getNext(points)) {
            case MoveTo: moveCount++; break;
            case LineTo: case QuadTo: case CubicTo:
                if (moveCount > 1)
                    return false;
                break;
            case Close: break;
        }

        // Return true
        return true;
    }

    /**
     * Returns whether path made up of multiple subpaths (more than one moveto).
     */
    public boolean isMultiCycle()  { return !isSingleCycle(); }

    /**
     * Returns whether shape has intersecting segments.
     */
    public boolean isSelfIntersecting()
    {
        SegmentPath segmentPath = new SegmentPath(this);
        return segmentPath.isSelfIntersecting();
    }

    /**
     * Called when shape changes.
     */
    protected void shapeChanged()
    {
        _bounds = null;
        _arcLen = -1;
    }

    /**
     * Returns the shape in rect.
     */
    public Shape copyFor(Rect aRect)
    {
        return copyForBounds(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /**
     * Returns the shape in rect.
     */
    public Shape copyForBounds(double aX, double aY, double aW, double aH)
    {
        // Get this shape bounds
        Rect bnds = getBounds();
        if (RectBase.equals(bnds, aX, aY, aW, aH)) return this;
        double bndsW = bnds.width;
        double bndsH = bnds.height;

        // Get scale to rect
        double scaleX = bndsW != 0 ? aW/bndsW : 0;
        double scaleY = bndsH != 0 ? aH/bndsH : 0;
        double transX = aX - bnds.x;
        double transY = aY - bnds.y;
        Transform xfm = Transform.getScale(scaleX, scaleY);
        xfm.translate(transX, transY);
        PathIter pathIter = getPathIter(xfm);
        return new Path2D(pathIter);
    }

    /**
     * Returns a copy of this shape transformed by given transform.
     */
    public Shape copyFor(Transform aTrans)
    {
        PathIter pathIter = getPathIter(aTrans);
        return new Path2D(pathIter);
    }

    /**
     * Returns an SVG string representation of Shape.
     */
    public String getSvgString()
    {
        // Get path iterator and declare iter vars
        PathIter pathIter = getPathIter(null);
        double[] points = new double[6];
        StringBuilder sb = new StringBuilder();

        // Iterate over path segments
        while (pathIter.hasNext()) {
            switch (pathIter.getNext(points)) {
                case MoveTo:
                    sb.append("M ").append(fmt(points[0])).append(' ').append(fmt(points[1])).append('\n');
                    break;
                case LineTo:
                    sb.append("L ").append(fmt(points[0])).append(' ').append(fmt(points[1])).append('\n');
                    break;
                case QuadTo:
                    sb.append("Q ").append(fmt(points[0])).append(' ').append(fmt(points[1])).append(' ');
                    sb.append(fmt(points[2])).append(' ').append(fmt(points[3])).append('\n');
                    break;
                case CubicTo:
                    sb.append("C ").append(fmt(points[0])).append(' ').append(fmt(points[1])).append(' ');
                    sb.append(fmt(points[2])).append(' ').append(fmt(points[3])).append(' ').append(fmt(points[4])).append(' ');
                    sb.append(fmt(points[5])).append('\n');
                    break;
                case Close:
                    sb.append("Z\n");
            }
        }
        return sb.toString();
    }

    /**
     * Standard to string implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + " [" + getBounds().getSvgString() + "] " + getSvgString();
    }

    /**
     * Adds two shapes together.
     */
    public static Shape addShapes(Shape aShape1, Shape aShape2)
    {
        return SegmentPathCAG.addShapes(aShape1, aShape2);
    }

    /**
     * Subtracts two shapes together.
     */
    public static Shape subtractShapes(Shape aShape1, Shape aShape2)
    {
        return SegmentPathCAG.subtractShapes(aShape1, aShape2);
    }

    /**
     * Returns the intersection shape of two shapes.
     */
    public static Shape intersectShapes(Shape aShape1, Shape aShape2)
    {
        return SegmentPathCAG.intersectShapes(aShape1, aShape2);
    }

    /**
     * Returns a simple shape for complex shape.
     */
    public static Shape getNotSelfIntersectingShape(Shape aShape)
    {
        SegmentPath segmentPath = new SegmentPath(aShape);
        segmentPath.makeNotSelfIntersecting();
        return segmentPath;
    }

    /** Helper. */
    private static String fmt(double aVal)  { return _fmt.format(aVal); }
    static java.text.DecimalFormat _fmt = new java.text.DecimalFormat("#");
}