/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.PathIter;
import snap.geom.Seg;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Stroke;

import java.util.Arrays;

/**
 * Utility methods for VertexArray.
 */
public class VertexArrayUtils {

    /**
     * Returns a VertexArray for stroked path.
     */
    public static VertexArray getStrokedShapeTriangleArray(PathIter pathIter, Color aColor, Stroke aStroke, double anOffset)
    {
        // Create VertexArray
        VertexArray vertexArray = new VertexArray();
        vertexArray.setColor(aColor);

        // Get PathIter and path iteration vars
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

        // Add the points
        int pointCount = vertexArray.getPointCount();
        vertexArray.addPoint(p0a.x, p0a.y, p0a.z);
        vertexArray.addPoint(p1a.x, p1a.y, p1a.z);
        vertexArray.addPoint(p0b.x, p0b.y, p0b.z);
        vertexArray.addPoint(p1b.x, p1b.y, p1b.z);

        // Get indexes
        int i0a = pointCount;
        int i1a = pointCount + 1;
        int i0b = pointCount + 2;
        int i1b = pointCount + 3;

        // Get current index array and current size
        int[] indexArray = vertexArray.getIndexArray();
        int indexCount = indexArray.length;

        // Extend index array, set triangle point indexes
        indexArray = Arrays.copyOf(indexArray, indexCount + 6);
        indexArray[indexCount] = i0a;
        indexArray[indexCount + 1] = i0b;
        indexArray[indexCount + 2] = i1b;
        indexArray[indexCount + 3] = i1a;
        indexArray[indexCount + 4] = i0a;
        indexArray[indexCount + 5] = i1b;

        // Set new indexArray in VertexArray. If double-sided, just return
        vertexArray.setIndexArray(indexArray);
        if (vertexArray.isDoubleSided())
            return;

        // Get triangle A. If not aligned with normal, swap points
        Point3D[] triangleA = { p0a, p0b, p1b };
        Vector3D pointsNormal = Vector3D.getNormalForPoints(new Vector3D(0, 0, 0), triangleA);
        if (!pointsNormal.equals(lineNormal)) {
            indexArray[indexCount + 1] = i1b;
            indexArray[indexCount + 2] = i0b;
        }

        // Get triangle B. If not aligned with normal, swap points
        Point3D[] triangleB = { p1a, p0a, p1b };
        Vector3D.getNormalForPoints(pointsNormal, triangleB);
        if (!pointsNormal.equals(lineNormal)) {
            indexArray[indexCount + 4] = i1b;
            indexArray[indexCount + 5] = i0a;
        }
    }
}
