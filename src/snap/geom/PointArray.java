/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.geom;

/**
 * This class represents an array of points assumed to form a polygon, quads or triangles.
 */
public class PointArray implements Cloneable {

    // The array of point XY components
    protected double[]  _pointArray;

    // The length of point array
    protected int  _pointArrayLen;

    // The ording of the points
    public enum Ordering { Polygon, Quads, Triangles };

    /**
     * Creates a new Polygon from given x y coords.
     */
    public PointArray(double ... theCoords)
    {
        _pointArray = theCoords != null ? theCoords : null;
        _pointArrayLen = theCoords != null ? theCoords.length : 0;
    }

    /**
     * Returns the raw points XY components array.
     */
    public double[] getPointArray()
    {
        return _pointArray;
    }

    /**
     * Returns the point count.
     */
    public int getPointCount()
    {
        return _pointArrayLen / 2;
    }

    /**
     * Returns the x at given point index.
     */
    public double getX(int anIndex)
    {
        int pointIndex = anIndex * 2;
        return _pointArray[pointIndex];
    }

    /**
     * Returns the y at given point index.
     */
    public double getY(int anIndex)
    {
        int pointIndex = anIndex * 2;
        return _pointArray[pointIndex + 1];
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public PointArray clone()
    {
        // Do normal version
        PointArray clone;
        try { clone = (PointArray) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Clone PointArray, return
        clone._pointArray = _pointArray.clone();
        return clone;
    }
}