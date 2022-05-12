/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.PathIter;
import snap.geom.Seg;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Stroke;

/**
 * Utility methods for VertexArray.
 */
public class VertexArrayUtils {

    /**
     * Returns a VertexArray for stroked path.
     */
    public static VertexArray getStrokedShapeTriangleArray(Shape aShape, Color aColor, Stroke aStroke, double anOffset)
    {
        // Create VertexArray
        VertexArray vertexArray = new VertexArray();
        vertexArray.setColor(aColor);

        // Get PathIter and path iteration vars
        PathIter pathIter = aShape.getPathIter(null);
        double[] pnts = new double[6];
        double moveX = 0, moveY = 0;
        double lastX = 0, lastY = 0;
        Point3D p1 = new Point3D(0, 0, anOffset);
        Point3D p2 = new Point3D(0, 0, anOffset);
        Vector3D zNormal = new Vector3D(0, 0, 1);

        // Iterate over PathIter segs to add with moveTo, lineTo, etc.
        while (pathIter.hasNext()) {

            // Get next segment. Add stand-in for QuadT/CubicTo
            Seg seg = pathIter.getNext(pnts);
            if (seg == Seg.QuadTo || seg == Seg.CubicTo) {
                System.err.println("VertexArrayUtils.getStrokedShapeVertexArray: " + seg + " not implemented");
                pnts[0] = seg == Seg.QuadTo ? pnts[2] : pnts[4];
                pnts[1] = seg == Seg.QuadTo ? pnts[3] : pnts[5];
                seg = Seg.LineTo;
            }

            // Handle Seg
            switch (seg) {

                // Handle MoveTo
                case MoveTo:
                    moveX = lastX = pnts[0];
                    moveY = lastY = pnts[1];
                    break;

                // Handle LineTo
                case LineTo:
                    p1.x = lastX; p1.y = lastY;
                    p2.x = pnts[0]; p2.y = pnts[1];
                    addLineStrokePoints(vertexArray, p1, p2, zNormal, aStroke.getWidth());
                    lastX = pnts[0];
                    lastY = pnts[1];
                    break;

                // Handle close
                case Close:
                    p1.x = lastX; p1.y = lastY;
                    p2.x = moveX; p2.y = moveY;
                    addLineStrokePoints(vertexArray, p1, p2, zNormal, aStroke.getWidth());
                    lastX = moveX;
                    lastY = moveY;
                    break;
            }
        }

        // Return
        return vertexArray;
    }

    /**
     * Adds triangles to VertexArray for a stroked line between given points.
     */
    public static void addLineStrokePoints(VertexArray vertexArray, Point3D p0, Point3D p1, Vector3D lineNormal, double lineWidth)
    {
        // Get vector across line and perpendicular to line
        Vector3D acrossVector = new Vector3D(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z).normalize();
        Vector3D downVector = lineNormal.getCrossProduct(acrossVector).normalize();

        // Scale by lineWidth
        double halfWidth = lineWidth / 2 * 1.2; // Let's fudge up a little
        acrossVector.scale(halfWidth);
        downVector.scale(halfWidth);

        // Get offset so line moves 'above' path triangles
        Vector3D offsetNormal = lineNormal.clone();
        offsetNormal.scale(.5);

        // Upper left point
        Point3D p0a = p0.clone();
        p0a.x += downVector.x - acrossVector.x + offsetNormal.x;
        p0a.y += downVector.y - acrossVector.y + offsetNormal.y;
        p0a.z += downVector.z - acrossVector.z + offsetNormal.z;

        // Lower left point
        Point3D p0b = p0.clone();
        p0b.x += -downVector.x - acrossVector.x + offsetNormal.x;
        p0b.y += -downVector.y - acrossVector.y + offsetNormal.y;
        p0b.z += -downVector.z - acrossVector.z + offsetNormal.z;

        // Upper right point
        Point3D p1a = p1.clone();
        p1a.x += downVector.x + acrossVector.x + offsetNormal.x;
        p1a.y += downVector.y + acrossVector.y + offsetNormal.y;
        p1a.z += downVector.z + acrossVector.z + offsetNormal.z;

        // Lower right point
        Point3D p1b = p1.clone();
        p1b.x += -downVector.x + acrossVector.x + offsetNormal.x;
        p1b.y += -downVector.y + acrossVector.y + offsetNormal.y;
        p1b.z += -downVector.z + acrossVector.z + offsetNormal.z;

        // Get triangle A. If not aligned with normal, swap points
        Point3D[] triangleA = { p0a, p0b, p1b };
        Vector3D pointsNormal = Vector3D.getNormalForPoints(new Vector3D(0, 0, 0), triangleA);
        if (!pointsNormal.equals(lineNormal)) {
            triangleA[1] = p1b; triangleA[2] = p0b; }

        // Get triangle A. If not aligned with normal, swap points
        Point3D[] triangleB = { p1a, p0a, p1b };
        Vector3D.getNormalForPoints(pointsNormal, triangleB);
        if (!pointsNormal.equals(lineNormal)) {
            triangleB[1] = p1b; triangleB[2] = p0a; }

        // Add triangle points
        for (Point3D p3d : triangleA)
            vertexArray.addPoint(p3d.x, p3d.y, p3d.z);
        for (Point3D p3d : triangleB)
            vertexArray.addPoint(p3d.x, p3d.y, p3d.z);
    }
}
