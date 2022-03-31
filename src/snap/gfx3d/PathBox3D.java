/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.*;

import snap.geom.PathIter;
import snap.geom.Point;
import snap.geom.Shape;
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
        // Create list to hold paths
        List<Shape3D> paths = new ArrayList<>();
        Path3D back = null;

        // If path is closed, create path3d for front from aPath and z1
        if (aPath.isClosed()) {

            // Create path3d for front and back
            Path3D front = new Path3D(aPath, z1);
            front.setName("BoxBack");
            back = new Path3D(aPath, z2);
            back.setName("BoxFront");

            // Add front to paths list
            paths.add(front);

            // If front is pointing wrong way, reverse it
            if (front.getNormal().isAway(new Vector3D(0, 0, -1), true))
                front.reverse();

            // Otherwise, reverse back
            else {
                back.reverse();
                aPath = back.getShape2D();
            }
        }

        // Get PathIter and loop vars
        PathIter piter = aPath.getPathIter(null);
        double[] pts = new double[6];
        double lastX = 0, lastY = 0;
        double lastMoveX = 0, lastMoveY = 0;
        int sideNum = 0;

        // Iterate over path elements
        while (piter.hasNext()) switch (piter.getNext(pts)) {

            // MoveTo
            case MoveTo:
                lastX = lastMoveX = pts[0];
                lastY = lastMoveY = pts[1];
                break;

            // LineTo
            case LineTo: {
                if (Point.equals(lastX, lastY, pts[0], pts[1])) continue;
                Poly3D path = new Poly3D();
                path.setName("BoxSide" + sideNum++);
                path.addPoint(lastX, lastY, z1);
                path.addPoint(pts[0], pts[1], z1);
                path.addPoint(pts[0], pts[1], z2);
                path.addPoint(lastX, lastY, z2);
                paths.add(path);
                lastX = pts[0]; lastY = pts[1];
            } break;

            // QuadTo
            case QuadTo: {
                Path3D path = new Path3D(); path.setName("BoxSide" + sideNum++);
                path.moveTo(lastX, lastY, z1);
                path.quadTo(pts[0], pts[1], z1, pts[2], pts[3], z1);
                path.lineTo(pts[4], pts[5], z2);
                path.quadTo(pts[0], pts[1], z2, lastX, lastY, z2);
                path.close();
                paths.add(path);
                lastX = pts[2]; lastY = pts[3];
            } break;

            // CubicTo
            case CubicTo: {
                Path3D path = new Path3D(); path.setName("BoxSide" + sideNum++);
                path.moveTo(lastX, lastY, z1);
                path.curveTo(pts[0], pts[1], z1, pts[2], pts[3], z1, pts[4], pts[5], z1);
                path.lineTo(pts[4], pts[5], z2);
                path.curveTo(pts[2], pts[3], z2, pts[0], pts[1], z2, lastX, lastY, z2);
                path.close();
                paths.add(path);
                lastX = pts[4]; lastY = pts[5];
            } break;

            // Close
            case Close: {
                Poly3D path = new Poly3D();
                path.setName("BoxSide" + sideNum++);
                path.addPoint(lastX, lastY, z1);
                path.addPoint(lastMoveX, lastMoveY, z1);
                path.addPoint(lastMoveX, lastMoveY, z2);
                path.addPoint(lastX, lastY, z2);
                paths.add(path);
            } break;
        }

        // Add back face to paths
        if (back != null)
            paths.add(back);

        // Return paths
        return paths.toArray(new Shape3D[0]);
    }
}