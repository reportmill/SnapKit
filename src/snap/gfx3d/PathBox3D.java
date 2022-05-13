/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;

/**
 * A Shape3D subclass that represents a path extruded to a box.
 */
public class PathBox3D extends ParentShape {
    
    // The 2D path shape
    private Shape  _pathShape;
    
    // The min/max depth
    private double  _z1, _z2;

    /**
     * Constructor for given path Shape and Z min/max.
     */
    public PathBox3D(Shape aPath, double z1, double z2)
    {
        _pathShape = aPath;
        _z1 = z1; _z2 = z2;

        // Register to rebuild children
        rebuildShape();
    }

    /**
     * Override to extrusion surfaces (sides and back).
     */
    @Override
    protected void buildShapeImpl()
    {
        // Create extrusion shapes for 2D shape
        Shape3D[] extrusionShapes = createExtrusionShape3Ds(_pathShape, _z1, _z2);

        // Get Color, Stroke, Opacity
        Color color = getColor();
        Color strokeColor = getStrokeColor();
        Stroke stroke = getStroke();
        double opacity = getOpacity();

        // Iterate over paths and set color, stroke, opacity
        for (Shape3D shape3D : extrusionShapes) {
            shape3D.setColor(color);
            shape3D.setOpacity(opacity);
            if (shape3D.getName() == null) continue;
            shape3D.setStrokeColor(strokeColor);
            shape3D.setStroke(stroke);
        }

        // Set children
        setChildren(extrusionShapes);
    }

    /**
     * Creates and returns an array of Shape3Ds for a given 2D shape and extrusion front/back z values.
     */
    public static Shape3D[] createExtrusionShape3Ds(Shape aPath, double z1, double z2)
    {
        // Get flattened path
        Shape flatPath = aPath.getFlat();

        // Create list to hold paths
        List<Shape3D> paths = new ArrayList<>();
        Path3D back = null;

        // If path is closed, create path3d for front from aPath and z1
        if (flatPath.isClosed()) {

            // Create path3d for front and back
            Path3D front = new Path3D(flatPath, z1);
            front.setName("BoxBack");
            back = new Path3D(flatPath, z2);
            back.setName("BoxFront");

            // Add front to paths list
            paths.add(front);

            // If front is pointing wrong way, reverse it
            if (front.getNormal().isAway(new Vector3D(0, 0, -1), true))
                front.reverse();

            // Otherwise, reverse back
            else {
                back.reverse();
                flatPath = back.getShape2D();
            }
        }

        // Get PathIter and loop vars
        PathIter piter = aPath.getPathIter(null);
        double[] pts = new double[6];
        double lastX = 0, lastY = 0;
        double lastMoveX = 0, lastMoveY = 0;
        int sideNum = 0;

        // Iterate over path elements
        while (piter.hasNext()) {
            Seg seg = piter.getNext(pts);
            switch (seg) {

                // Handle MoveTo:
                case MoveTo:
                    lastX = lastMoveX = pts[0];
                    lastY = lastMoveY = pts[1];
                    break;

                // Handle LineTo:
                case LineTo: {
                    Poly3D polyShape = lineTo(lastX, lastY, lastX = pts[0], lastY = pts[1], z1, z2);
                    polyShape.setName("BoxSide" + sideNum++);
                    paths.add(polyShape);
                }
                break;

                // Handle CurveTo
                case CubicTo:
                    curveToFlat(lastX, lastY, pts[0], pts[1], pts[2], pts[3], lastX = pts[4], lastY = pts[5], z1, z2, paths);
                    break;

                // Handle Close
                case Close: {
                    Poly3D polyShape = lineTo(lastX, lastY, lastX = lastMoveX, lastY = lastMoveY, z1, z2);
                    polyShape.setName("BoxSide" + sideNum++);
                    paths.add(polyShape);
                }
                break;

                // Handle unexpected
                default: System.err.println("PathBox3D.createExtrusionShape3Ds: Unexpected path seg: " + seg);
            }
        }

        // Add back face to paths
        if (back != null)
            paths.add(back);

        // Return paths
        return paths.toArray(new Shape3D[0]);
    }

    /**
     * Create a Poly3D quad surface for 2D lineTo points and depth1/depth2.
     */
    private static Poly3D lineTo(double x1, double y1, double x2, double y2, double z1, double z2)
    {
        if (Point.equals(x1, y1, x2, y2)) return null;
        Poly3D path = new Poly3D();
        path.addPoint(x1, y1, z1);
        path.addPoint(x2, y2, z1);
        path.addPoint(x2, y2, z2);
        path.addPoint(x1, y1, z2);
        return path;
    }

    /**
     * CubicTo by adding lineTo segments.
     */
    private static void curveToFlat(double lastX, double lastY, double cp1x, double cp1y, double cp2x, double cp2y, double x, double y,
                          double z1, double z2, List<Shape3D> polyList)
    {
        // If distance from control points to base line less than tolerance, just add line
        double dist1 = Line.getDistance(lastX, lastY, x, y, cp1x, cp1y);
        double dist2 = Line.getDistance(lastX, lastY, x, y, cp2x, cp2y);
        if (dist1 < .25 && dist2 < .25) {
            Poly3D polyShape = lineTo(lastX, lastY, x, y, z1, z2);
            polyList.add(polyShape);
            return;
        }

        // Split curve at midpoint and add parts
        Cubic c0 = new Cubic(lastX, lastY, cp1x, cp1y, cp2x, cp2y, x, y);
        Cubic c1 = c0.split(.5);
        curveToFlat(lastX, lastY, c0.cp0x, c0.cp0y, c0.cp1x, c0.cp1y, c0.x1, c0.y1, z1, z2, polyList);
        curveToFlat(c0.x1, c0.y1, c1.cp0x, c1.cp0y, c1.cp1x, c1.cp1y, c1.x1, c1.y1, z1, z2, polyList);
    }
}