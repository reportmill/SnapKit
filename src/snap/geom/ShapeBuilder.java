/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.Scanner;

/**
 * This shape subclass provides basic methods for constructing a shape.
 */
public abstract class ShapeBuilder extends Shape {

    /**
     * Moveto.
     */
    public abstract void moveTo(double aX, double aY);

    /**
     * LineTo.
     */
    public abstract void lineTo(double aX, double aY);

    /**
     * QuadTo.
     */
    public abstract void quadTo(double cpx, double cpy, double endX, double endY);

    /**
     * CubicTo.
     */
    public abstract void curveTo(double cp1x, double cp1y, double cp2x, double cp2y, double endX, double endY);

    /**
     * Closes the current polygon.
     */
    public abstract void close();

    /**
     * Returns the last point X.
     */
    public abstract double getLastPointX();

    /**
     * Returns the last point Y.
     */
    public abstract double getLastPointY();

    /**
     * ArcTo: Adds a Cubic using the corner point as a guide.
     */
    public void arcTo(double cx, double cy, double endX, double endY)
    {
        double magic = .5523f; // I calculated this in mathematica one time - probably only valid for 90 deg corner.
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        double cpx1 = lastX + (cx - lastX) * magic;
        double cpy1 = lastY + (cy - lastY) * magic;
        double cpx2 = endX + (cx - endX) * magic;
        double cpy2 = endY + (cy - endY) * magic;
        curveTo(cpx1, cpy1, cpx2, cpy2, endX, endY);
    }

    /**
     * LineTo.
     */
    public void lineBy(double aX, double aY)
    {
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        lineTo(lastX + aX, lastY + aY);
    }

    /**
     * Horizontal LineTo.
     */
    public void hlineTo(double aX)
    {
        double lastY = getLastPointY();
        lineTo(aX, lastY);
    }

    /**
     * Vertical LineTo.
     */
    public void vlineTo(double aY)
    {
        double lastX = getLastPointX();
        lineTo(lastX, aY);
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

    /**
     * Appends a path segment.
     */
    public void appendSegment(Segment aSegment)
    {
        if (aSegment instanceof Cubic) {
            Cubic seg = (Cubic) aSegment;
            curveTo(seg.cp0x, seg.cp0y, seg.cp1x, seg.cp1y, aSegment.x1, aSegment.y1);
        }
        else if (aSegment instanceof Quad) {
            Quad seg = (Quad) aSegment;
            quadTo(seg.cpx, seg.cpy, aSegment.x1, aSegment.y1);
        }
        else lineTo(aSegment.x1, aSegment.y1);
    }

    /**
     * Appends a path from an SVG path string.
     */
    public void appendSvgString(String aStr)
    {
        // Create scanner from string and new path
        Scanner scan = new Scanner(aStr);
        double endX, endY;
        double cp0x, cp0y;
        double cp1x, cp1y;

        // Iterate over scanner tokens
        while (scan.hasNext()) {
            String op = scan.next();
            switch (op) {

                // Handle MoveTo
                case "M":
                    endX = scan.nextDouble();
                    endY = scan.nextDouble();
                    moveTo(endX, endY);
                    break;

                // Handle LineTo
                case "L":
                    endX = scan.nextDouble();
                    endY = scan.nextDouble();
                    lineTo(endX, endY);
                    break;

                // Handle QuadTo
                case "Q":
                    cp0x = scan.nextDouble();
                    cp0y = scan.nextDouble();
                    endX = scan.nextDouble();
                    endY = scan.nextDouble();
                    quadTo(cp0x, cp0y, endX, endY);
                    break;

                // Handle CubicTo
                case "C":
                    cp0x = scan.nextDouble();
                    cp0y = scan.nextDouble();
                    cp1x = scan.nextDouble();
                    cp1y = scan.nextDouble();
                    endX = scan.nextDouble();
                    endY = scan.nextDouble();
                    curveTo(cp0x, cp0y, cp1x, cp1y, endX, endY);
                    break;

                // Handle close
                case "Z": close(); break;

                // Handle invalid
                default: System.err.println("ShapeBuilder.appendSVGString: Invalid op: " + op);
            }
        }
    }

    /**
     * QuadTo by adding lineTos.
     */
    public void quadToFlat(double cpx, double cpy, double x, double y, double flatDist)
    {
        // If distance from control point to base line less than tolerance, just add line
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        double dist0 = Point.getDistance(lastX, lastY, x, y);
        if (dist0 < flatDist)
            return;
        double dist1 = Line.getDistance(lastX, lastY, x, y, cpx, cpy);
        if (dist1 < flatDist) {
            lineTo(x, y);
            return;
        }

        // Split curve at midpoint and add parts
        Quad c0 = new Quad(lastX, lastY, cpx, cpy, x, y), c1 = c0.split(.5);
        quadToFlat(c0.cpx, c0.cpy, c0.x1, c0.y1, flatDist);
        quadToFlat(c1.cpx, c1.cpy, c1.x1, c1.y1, flatDist);
    }

    /**
     * CubicTo by adding lineTos.
     */
    public void curveToFlat(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y, double flatDist)
    {
        // If distance from control points to base line less than tolerance, just add line
        double lastX = getLastPointX();
        double lastY = getLastPointY();
        double dist0 = Point.getDistance(lastX, lastY, x, y);
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
        curveToFlat(c0.cp0x, c0.cp0y, c0.cp1x, c0.cp1y, c0.x1, c0.y1, flatDist);
        curveToFlat(c1.cp0x, c1.cp0y, c1.cp1x, c1.cp1y, c1.x1, c1.y1, flatDist);
    }
}
