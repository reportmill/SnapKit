/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;
import java.util.List;

/**
 * A Shape subclass to represent an array of points as polygon, quad or triangles.
 */
public class PointArray extends Shape implements Cloneable {

    // The array of points components
    private double[]  _pointsArray = new double[0];

    // The length of points array
    private int  _pointsArrayLen;

    // The array of indexes
    private int[]  _indexArray = new int[0];

    // The length of index array
    private int  _indexArrayLen;

    // The
    public enum Ordering { Triangles };

    /**
     * Creates a new Polygon from given x y coords.
     */
    public PointArray(double ... theCoords)
    {
        _pointsArray = theCoords;
        boolean isClockwiseAll = isClockwise(theCoords);

        List<Integer> triangles = PointArrayEarcut.earcut(theCoords);
        int indexCount = triangles.size();
        _indexArray = new int[indexCount];
        _indexArrayLen = indexCount;

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
     * Returns the raw points array.
     */
    public double[] getPointsArray()
    {
        return _pointsArray;
    }

    /**
     * Sets the points.
     */
    public void setPointsArray(double[] thePoints)
    {
        _pointsArray = thePoints;
        shapeChanged();
    }

    /**
     * Returns the point count.
     */
    public int getPointCount()
    {
        return _pointsArrayLen / 2;
    }

    /**
     * Returns the point index at given index.
     */
    public int getPointsArrayIndex(int anIndex)
    {
        int pointIndex = _indexArrayLen > 0 ? _indexArray[anIndex] : anIndex;
        return pointIndex * 2;
    }

    /**
     * Returns the x at given point index.
     */
    public double getX(int anIndex)
    {
        int pointsIndex = getPointsArrayIndex(anIndex);
        return _pointsArray[pointsIndex];
    }

    /**
     * Returns the y at given point index.
     */
    public double getY(int anIndex)
    {
        int pointsIndex = getPointsArrayIndex(anIndex);
        return _pointsArray[pointsIndex + 1];
    }

    /**
     * Returns the index array.
     */
    public int[] getIndexArray()  { return _indexArray; }

    /**
     * Returns the number of items in index array.
     */
    public int getIndexCount()  { return _indexArrayLen; }

    /**
     * Returns the shape bounds.
     */
    protected Rect getBoundsImpl()
    {
        if (_pointsArray.length == 0) return new Rect();
        double xmin = _pointsArray[0], xmax = _pointsArray[0];
        double ymin = _pointsArray[1], ymax = _pointsArray[1];
        for (int i = 2; i < _pointsArray.length; i += 2) {
            double x = _pointsArray[i];
            double y = _pointsArray[i + 1];
            xmin = Math.min(xmin, x);
            xmax = Math.max(xmax, x);
            ymin = Math.min(ymin, y);
            ymax = Math.max(ymax, y);
        }
        return new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    /**
     * Returns the path iterator.
     */
    public PathIter getPathIter(Transform aTrans)
    {
        return new PolyIter(_pointsArray, aTrans);
    }

    /**
     * Standard clone implementation.
     */
    public PointArray clone()
    {
        try {
            PointArray clone = (PointArray) super.clone();
            clone._pointsArray = _pointsArray.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
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
            double thisX = pointsArray[pointsArrayIndex + 0];
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
    public static PointArray newTrianglePointArrayForShape(Shape aShape)
    {
        PolygonList polygonList = new PolygonList(aShape);
        Polygon[] polygons = polygonList.getPolys();
        double[] pointsArray = null;
        for (Polygon poly : polygons)
            pointsArray = poly.getPoints();
        if (pointsArray.length > 1)
            System.err.println("PointArray.newTrianglePointArrayForShape: Complex shape not supported");

        return newTrianglePointArrayForPolygonPoints(pointsArray);
    }

    /**
     * Returns a PointsArray with triangle ordering.
     */
    public static PointArray newTrianglePointArrayForPolygonPoints(double[] pointsArray)
    {
        return new PointArray(pointsArray);
    }

    /**
     * PathIter for Line.
     */
    private static class PolyIter extends PathIter {

        // Ivars
        double[]  _points;
        int  _pointCount;
        int  _pointIndex;

        /**
         * Create new LineIter.
         */
        PolyIter(double thePnts[], Transform at)
        {
            super(at);
            _points = thePnts;
            _pointCount = _points.length;
        }

        /**
         * Returns whether there are more segments.
         */
        public boolean hasNext()
        {
            return _pointCount > 0 && _pointIndex < _pointCount + 2;
        }

        /**
         * Returns the coordinates and type of the current path segment in the iteration.
         */
        public Seg getNext(double[] coords)
        {
            if (_pointIndex == 0)
                return moveTo(_points[_pointIndex++], _points[_pointIndex++], coords);
            if (_pointIndex < _pointCount)
                return lineTo(_points[_pointIndex++], _points[_pointIndex++], coords);
            if (_pointIndex == _pointCount) {
                _pointIndex += 2;
                return close();
            }
            throw new RuntimeException("PolygonIter: Index beyond bounds " + _pointIndex + " " + _pointCount);
        }
    }
}