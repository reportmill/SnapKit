/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.*;

import snap.geom.Path;
import snap.geom.PathIter;
import snap.geom.Point;
import snap.gfx.*;

/**
 * A Shape3D subclass that represents a path extruded to a box.
 */
public class PathBox3D extends Shape3D {
    
    // The path
    private Path  _path;
    
    // The min/max depth
    private double  _z1, _z2;
    
    // Indicates whether to stroke polygons created during extrusion, to try to make them mesh better
    private boolean  _fixEdges;
    
    // The path3ds
    private Path3D  _path3ds[];

    /**
     * Creates a PathBox3D from the given Path3D.
     */
    public PathBox3D(Path aPath, double z1, double z2, boolean fixEdges)
    {
        _path = aPath;
        _z1 = z1; _z2 = z2;
        _fixEdges = fixEdges;
    }

    /**
     * Returns the array of Path3D that can render this shape.
     */
    public Path3D[] getPath3Ds()
    {
        // If already set, just return
        if (_path3ds!=null) return _path3ds;

        // Create paths for Z1 & Z2
        Path3D paths[] = getPaths(_path, _z1, _z2, _fixEdges? .001 : 0);

        // Set Color, Stroke, Opacity
        Color color = getColor();
        Color strokeColor = getStrokeColor();
        Stroke stroke = getStroke();
        double op = getOpacity();
        for (int i=0, iMax=paths.length; i<iMax; i++) { Path3D p = paths[i];
            p.setColor(color);
            p.setOpacity(op);
            if (_fixEdges && i!=0 && i!=iMax-1) {
                p.setStroke(color, 1.5);
                p._fixEdges = true;
            }
            else {
                p.setStrokeColor(strokeColor);
                p.setStroke(stroke);
            }
        }

        // Return paths
        return _path3ds = paths;
    }

    /**
     * Creates and returns a list of paths in 3D for a given 2D path and extrusion.
     * Also can take into account the width of a stroke applied to the side (extrusion) panels.
     */
    public static Path3D[] getPaths(Path aPath, double z1, double z2, double strokeWidth)
    {
        // Create list to hold paths
        List <Path3D> paths = new ArrayList();
        Path3D back = null;

        // If path is closed, create path3d for front from aPath and z1
        if (aPath.isClosed()) {

            // Create path3d for front and back
            Path3D front = new Path3D(aPath, z1);
            back = new Path3D(aPath, z2);

            // Add front to paths list
            paths.add(front);

            // If front is pointing wrong way, reverse it
            if (front.getNormal().isAway(new Vector3D(0, 0, -1), true))
                front.reverse();

            // Otherwise, reverse back
            else {
                back.reverse();
                aPath = back.getPath();
            }
        }

        // Make room for path stroke
        z1 += strokeWidth; z2 -= strokeWidth;

        // Iterate over path elements
        PathIter piter = aPath.getPathIter(null);
        double pts[] = new double[6], lastX = 0, lastY = 0, lastMoveX = 0, lastMoveY = 0;
        while (piter.hasNext()) switch (piter.getNext(pts)) {

            // MoveTo
            case MoveTo: lastX = lastMoveX = pts[0]; lastY = lastMoveY = pts[1]; break;

            // LineTo
            case LineTo: {
                if (Point.equals(lastX,lastY,pts[0],pts[1])) continue;
                Path3D path = new Path3D(); path.moveTo(lastX, lastY, z1);
                path.lineTo(pts[0], pts[1], z1); path.lineTo(pts[0], pts[1], z2);
                path.lineTo(lastX, lastY, z2); path.close();
                paths.add(path);
                lastX = pts[0]; lastY = pts[1];
            } break;

            // QuadTo
            case QuadTo: {
                Path3D path = new Path3D(); path.moveTo(lastX, lastY, z1);
                path.quadTo(pts[0], pts[1], z1, pts[2], pts[3], z1); path.lineTo(pts[4], pts[5], z2);
                path.quadTo(pts[0], pts[1], z2, lastX, lastY, z2); path.close();
                paths.add(path);
                lastX = pts[2]; lastY = pts[3];
            } break;

            // CubicTo
            case CubicTo: {
                Path3D path = new Path3D(); path.moveTo(lastX, lastY, z1);
                path.curveTo(pts[0], pts[1], z1, pts[2], pts[3], z1, pts[4], pts[5], z1); path.lineTo(pts[4], pts[5], z2);
                path.curveTo(pts[2], pts[3], z2, pts[0], pts[1], z2, lastX, lastY, z2); path.close();
                paths.add(path);
                lastX = pts[4]; lastY = pts[5];
            } break;

            // Close
            case Close: {
                Path3D path = new Path3D(); path.moveTo(lastX, lastY, z1);
                path.lineTo(lastMoveX, lastMoveY, z1); path.lineTo(lastMoveX, lastMoveY, z2);
                path.lineTo(lastX, lastY, z2); path.close();
                paths.add(path);
            } break;
        }

        // Add back face to paths
        if (back != null)
            paths.add(back);

        // Return paths
        return paths.toArray(new Path3D[paths.size()]);
    }
}