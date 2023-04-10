package snap.geom;

/**
 * This shape subclass provides basic methods for constructing a shape.
 */
public abstract class ShapeBuilder extends Shape {

    /**
     * Returns the last point X.
     */
    public abstract double getLastPointX();

    /**
     * Returns the last point Y.
     */
    public abstract double getLastPointY();

    /**
     * Returns the acceptable distance of control points to segment line when flattening curved segments.
     */
    public double getFlatDistance()  { return .25; }

    /**
     * Moveto.
     */
    public abstract void moveTo(double aX, double aY);

    /**
     * LineTo.
     */
    public abstract void lineTo(double aX, double aY);

    /**
     * Closes the current polygon.
     */
    public abstract void close();

    /**
     * QuadTo by adding lineTos.
     */
    public void quadTo(double cpx, double cpy, double x, double y)
    {
        // If distance from control point to base line less than tolerance, just add line
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        double dist0 = Point.getDistance(lastX, lastY, x, y);
        double flatDist = getFlatDistance();
        if (dist0 < flatDist)
            return;
        double dist1 = Line.getDistance(lastX, lastY, x, y, cpx, cpy);
        if (dist1 < flatDist) {
            lineTo(x, y);
            return;
        }

        // Split curve at midpoint and add parts
        Quad c0 = new Quad(lastX, lastY, cpx, cpy, x, y), c1 = c0.split(.5);
        quadTo(c0.cpx, c0.cpy, c0.x1, c0.y1);
        quadTo(c1.cpx, c1.cpy, c1.x1, c1.y1);
    }

    /**
     * CubicTo by adding lineTos.
     */
    public void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y)
    {
        // If distance from control points to base line less than tolerance, just add line
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        double dist0 = Point.getDistance(lastX, lastY, x, y);
        double flatDist = getFlatDistance();
        if (dist0 < flatDist)
            return;
        double dist1 = Line.getDistance(lastX, lastY, x, y, cp1x, cp1y);
        double dist2 = Line.getDistance(lastX, lastY, x, y, cp2x, cp2y);
        if (dist1 < flatDist && dist2 < flatDist) {
            lineTo(x, y);
            return;
        }

        // Split curve at midpoint and add parts
        Cubic c0 = new Cubic(lastX, lastY, cp1x, cp1y, cp2x, cp2y, x, y), c1 = c0.split(.5);
        curveTo(c0.cp0x, c0.cp0y, c0.cp1x, c0.cp1y, c0.x1, c0.y1);
        curveTo(c1.cp0x, c1.cp0y, c1.cp1x, c1.cp1y, c1.x1, c1.y1);
    }

    /**
     * ArcTo: Adds a Cubic using the corner point as a guide.
     */
    public void arcTo(double cx, double cy, double x, double y)
    {
        double magic = .5523f; // I calculated this in mathematica one time - probably only valid for 90 deg corner.
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        double cpx1 = lastX + (cx - lastX) * magic;
        double cpy1 = lastY + (cy - lastY) * magic;
        double cpx2 = x + (cx - x) * magic;
        double cpy2 = y + (cy - y) * magic;
        curveTo(cpx1, cpy1, cpx2, cpy2, x, y);
    }

    /**
     * LineTo.
     */
    public void lineBy(double x, double y)
    {
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        lineTo(lastX + x, lastY + y);
    }

    /**
     * Horizontal LineTo.
     */
    public void hlineTo(double x)
    {
        double lastY = getLastPointY();
        lineTo(x, lastY);
    }

    /**
     * Vertical LineTo.
     */
    public void vlineTo(double y)
    {
        double lastX = getLastPointX();
        lineTo(lastX, y);
    }

    /**
     * Appends given shape to this polygon path.
     */
    public void appendShape(Shape aShape)
    {
        PathIter pathIter = aShape.getPathIter(null);
        appendPathIter(pathIter);
    }

    /**
     * Appends given PathIter to this polygon path.
     */
    public void appendPathIter(PathIter aPathIter)
    {
        double[] points = new double[6];

        while (aPathIter.hasNext()) {
            Seg pathSeg = aPathIter.getNext(points);
            switch (pathSeg) {
                case MoveTo: moveTo(points[0], points[1]); break;
                case LineTo: lineTo(points[0], points[1]); break;
                case QuadTo: quadTo(points[0], points[1], points[2], points[3]); break;
                case CubicTo: curveTo(points[0], points[1], points[2], points[3], points[4], points[5]); break;
                case Close: close(); break;
            }
        }
    }
}
