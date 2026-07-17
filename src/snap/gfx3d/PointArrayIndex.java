/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Polygon;
import snap.geom.PolygonPath;
import snap.geom.Shape;
import java.util.List;

/**
 * This class represents an array of indexes into a PointArray assumed to form a polygon, quads or triangles.
 */
class PointArrayIndex implements Cloneable {

    // The raw XY point coords
    private double[] _pointArray;

    // The array of indexes
    private int[]  _indexArray;

    /**
     * Constructor for given XY coords double array.
     */
    public PointArrayIndex(double[] theCoords)
    {
        _pointArray = theCoords;
        boolean isClockwiseAll = isClockwise(theCoords);

        List<Integer> triangles = PointArrayEarcut.earcut(theCoords);
        int indexCount = triangles.size();
        _indexArray = new int[indexCount];

        for (int i = 0; i < indexCount; i+= 3) {
            int p1 = triangles.get(i);
            int p2 = triangles.get(i + 1);
            int p3 = triangles.get(i + 2);
            double p1x = theCoords[p1 * 2];
            double p1y = theCoords[p1 * 2 + 1];
            double p2x = theCoords[p2 * 2];
            double p2y = theCoords[p2 * 2 + 1];
            double p3x = theCoords[p3 * 2];
            double p3y = theCoords[p3 * 2 + 1];
            boolean isClockwise = isClockwise(p1x, p1y, p2x, p2y, p3x, p3y);
            boolean orderedRight = isClockwise == isClockwiseAll;
            _indexArray[i] = orderedRight ? p1 : p3;
            _indexArray[i + 1] = p2;
            _indexArray[i + 2] = orderedRight ? p3 : p1;
        }
    }

    /**
     * Returns the index array.
     */
    public int[] getIndexArray()  { return _indexArray; }

    /**
     * Returns the PointArray index at given index.
     */
    public int getIndex(int anIndex)  { return _indexArray[anIndex]; }

    /**
     * Returns the x at given point index.
     */
    public double getX(int anIndex)
    {
        int pointsIndex = _indexArray[anIndex];
        int pointArrayIndex = pointsIndex * 2;
        return _pointArray[pointArrayIndex];
    }

    /**
     * Returns the y at given point index.
     */
    public double getY(int anIndex)
    {
        int pointsIndex = _indexArray[anIndex];
        int pointArrayIndex = pointsIndex * 2;
        return _pointArray[pointArrayIndex + 1];
    }

    /**
     * Standard clone implementation.
     */
    public PointArrayIndex clone()
    {
        PointArrayIndex clone;
        try { clone = (PointArrayIndex) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        clone._pointArray = _pointArray.clone();
        clone._indexArray = _indexArray.clone();
        return clone;
    }

    /**
     * Returns the normal of given points. See Path3D.getNormal().
     */
    private static boolean isClockwise(double ... thePoints)
    {
        int pointCount = thePoints.length / 2;
        return isClockwise(thePoints, pointCount);
    }

    /**
     * Returns the normal of given points. See Path3D.getNormal().
     */
    private static boolean isClockwise(double[] pointsArray, int pointCount)
    {
        double zval = 0;
        for (int i = 0; i < pointCount; i++) {
            int pointsArrayIndex = i * 2;
            double thisX = pointsArray[pointsArrayIndex];
            double thisY = pointsArray[pointsArrayIndex + 1];
            int nextIndex = (i + 1) % pointCount * 2;
            double nextX = pointsArray[nextIndex];
            double nextY = pointsArray[nextIndex + 1];
            zval += (thisX - nextX) * (thisY + nextY);
        }

        // Normalize the result
        return zval > 0;
    }

    /**
     * Returns a PointsArray with triangle ordering.
     */
    public static PointArrayIndex newTrianglePointArrayForShape(Shape aShape)
    {
        PolygonPath polygonPath = new PolygonPath(aShape);
        Polygon[] polygons = polygonPath.getPolygons();
        double[] pointsArray = null;
        for (Polygon poly : polygons)
            pointsArray = poly.getPointArray();
        //if (pointsArray.length > 1)
        //    System.err.println("PointArray.newTrianglePointArrayForShape: Complex shape not supported");

        return newTrianglePointArrayForPolygonPoints(pointsArray);
    }

    /**
     * Returns a PointsArray with triangle ordering.
     */
    public static PointArrayIndex newTrianglePointArrayForPolygonPoints(double[] pointsArray)
    {
        return new PointArrayIndex(pointsArray);
    }
}