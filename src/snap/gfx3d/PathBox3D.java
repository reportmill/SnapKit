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
            //if (shape3D.getName() == null) continue;
            shape3D.setStrokeColor(strokeColor);
            shape3D.setStroke(stroke);
            if (_smoothSides)
                shape3D.setSmoothSides(true);
        }

        if (_smoothSides) {
            extrusionShapes[0].setSmoothSides(false);
            extrusionShapes[extrusionShapes.length-1].setSmoothSides(false);
        }

        // Set children
        setChildren(extrusionShapes);
    }

    /**
     * Creates and returns an array of Shape3Ds for a given 2D shape and extrusion front/back z values.
     */
    private Shape3D[] createExtrusionShape3Ds(Shape aShape, double z1, double z2)
    {
        // Get flattened path
        Shape flatPath = aShape.getFlattenedShape();

        // Create list to hold paths
        List<Shape3D> paths = new ArrayList<>();
        FacetShape back = null;
        boolean reverse = true;

        // If path is closed, create facet shapes for front/back from shape and z1/z2
        if (flatPath.isClosed()) {

            // Create front for shape
            FacetShape front = PolygonPath3D.createFacetShapeForShapeAndDepth(flatPath, z1);
            front.setName("BoxFront");

            // Create back for path
            back = PolygonPath3D.createFacetShapeForShapeAndDepth(flatPath, z2);
            back.setName("BoxBack");

            // Add front to paths list
            paths.add(front);

            // If front is pointing wrong way, reverse it
            Vector3D frontNormal = front.getNormal();
            if (frontNormal.isAway(new Vector3D(0, 0, -1), true)) {
                front.reverse();
                reverse = false;
            }

            // Otherwise, reverse back
            else back.reverse();
        }

        // Make room for path stroke
        if (_smoothSides) {
            z1 += 1;
            z2 -= 1;
        }

        // Get PathIter and loop vars
        PathIter pathIter = flatPath.getPathIter(null);
        double[] points = new double[6];
        double moveX = 0;
        double moveY = 0;
        double lineX = 0;
        double lineY = 0;
        int sideNum = 0;

        // Iterate over path elements
        while (pathIter.hasNext()) {
            Seg seg = pathIter.getNext(points);
            switch (seg) {

                // Handle MoveTo:
                case MoveTo:
                    lineX = moveX = points[0];
                    lineY = moveY = points[1];
                    break;

                // Handle LineTo:
                case LineTo: {
                    Polygon3D polyShape = lineTo(lineX, lineY, lineX = points[0], lineY = points[1], z1, z2);
                    if (polyShape == null)
                        break;
                    polyShape.setName("BoxSide" + sideNum++);
                    paths.add(polyShape);
                    if (reverse)
                        polyShape.reverse();
                }
                break;

                // Handle Close
                case Close: {
                    Polygon3D polyShape = lineTo(lineX, lineY, lineX = moveX, lineY = moveY, z1, z2);
                    if (polyShape == null)
                        break;
                    polyShape.setName("BoxSide" + sideNum++);
                    paths.add(polyShape);
                    if (reverse)
                        polyShape.reverse();
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
    private static Polygon3D lineTo(double x1, double y1, double x2, double y2, double z1, double z2)
    {
        if (Point.equals(x1, y1, x2, y2)) return null;
        Polygon3D path = new Polygon3D();
        path.addPoint(x1, y1, z1);
        path.addPoint(x2, y2, z1);
        path.addPoint(x2, y2, z2);
        path.addPoint(x1, y1, z2);
        return path;
    }
}