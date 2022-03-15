/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.gfx.Color;
import java.util.Arrays;

/**
 * This class manages raw vertex data (points, colors, normals, texture coords).
 */
public class VertexArray implements Cloneable {

    // The float array to hold actual vertex point components
    private float[]  _pointsArray = new float[24];

    // The number of components in vertex points array
    private int  _pointsArrayLen = 0;

    // The float array to hold actual vertex color components
    private float[]  _colorsArray = new float[0];

    // The number of components in vertex colors array
    private int  _colorsArrayLen = 0;

    // The number of components per vertex point
    private int  _pointCompCount = 3;

    // The number of components per vertex color
    private int  _colorCompCount = 3;

    // A global color
    private Color  _color;

    // Whether triangles are double-sided
    private boolean  _doubleSided;

    // The next VertexArray if this one is part of a chain
    private VertexArray  _next;

    /**
     * Constructor.
     */
    public VertexArray()  { }

    /**
     * Returns the number of vertex points in array.
     */
    public int getPointCount()  { return _pointsArrayLen / _pointCompCount; }

    /**
     * Adds value triplet to array.
     */
    public void addPoint(double aVal1, double aVal2, double aVal3)
    {
        // Expand Vertex components array if needed
        if (_pointsArrayLen + 3 > _pointsArray.length)
            _pointsArray = Arrays.copyOf(_pointsArray, Math.max(_pointsArray.length * 2, 24));

        // Add values
        _pointsArray[_pointsArrayLen++] = (float) aVal1;
        _pointsArray[_pointsArrayLen++] = (float) aVal2;
        _pointsArray[_pointsArrayLen++] = (float) aVal3;
    }

    /**
     * Returns the Point3D at index.
     */
    public Point3D getPoint3D(int anIndex)
    {
        return getPoint3D(new Point3D(0, 0, 0), anIndex);
    }

    /**
     * Returns the Point3D at index.
     */
    public Point3D getPoint3D(Point3D aPoint, int anIndex)
    {
        int index = anIndex * _pointCompCount;
        aPoint.x = _pointsArray[index];
        aPoint.y = _pointsArray[index + 1];
        aPoint.z = _pointsArray[index + 2];
        return aPoint;
    }

    /**
     * Adds points for given doubles assumed to be in (x, y, z, ...) format where each new two points after the first
     * form a triangle.
     */
    public void addStripPoints(double ... points)
    {
        // Get vars
        int compCount = points.length;
        Point3D p1 = new Point3D(0, 0, 0);
        Point3D p2 = new Point3D(0, 0, 0);
        Point3D p3 = new Point3D(0, 0, 0);
        Point3D[] cw = { p1, p2, p3 };
        Point3D[] ccw = { p3, p2, p1 };
        Vector3D normal = new Vector3D(0, 0, 0);
        Vector3D normal0 = null;

        // While there are still 3 points (9 components) add 3 points
        for (int compIndex = 0; compIndex + 9 <= compCount; compIndex += 3) {

            // Get next 3 points components for next triangle
            p1.x = points[compIndex + 0];
            p1.y = points[compIndex + 1];
            p1.z = points[compIndex + 2];
            p2.x = points[compIndex + 3];
            p2.y = points[compIndex + 4];
            p2.z = points[compIndex + 5];
            p3.x = points[compIndex + 6];
            p3.y = points[compIndex + 7];
            p3.z = points[compIndex + 8];

            // Get triangle normal (init first triangle normal on first pass)
            Vector3D.getNormalForPoints(normal, p1, p2, p3);
            if (normal0 == null)
                normal0 = normal.clone();

            // Make sure all triangles have same normal as first
            Point3D[] pnts = cw;
            if (!normal.equals(normal0))
                pnts = ccw;

            // Add the 3 points
            addPoint(pnts[0].x, pnts[0].y, pnts[0].z);
            addPoint(pnts[1].x, pnts[1].y, pnts[1].z);
            addPoint(pnts[2].x, pnts[2].y, pnts[2].z);
        }
    }

    /**
     * Adds a color to vertex color components array.
     */
    public void addColor(Color aColor)
    {
        // Expand color components array if needed
        if (_colorsArrayLen + _colorCompCount > _colorsArray.length)
            _colorsArray = Arrays.copyOf(_colorsArray, Math.max(_colorsArray.length * 2, 24));

        // Add values
        _colorsArray[_colorsArrayLen++] = (float) aColor.getRed();
        _colorsArray[_colorsArrayLen++] = (float) aColor.getGreen();
        _colorsArray[_colorsArrayLen++] = (float) aColor.getBlue();
        if (_colorCompCount > 3)
            _colorsArray[_colorsArrayLen++] = (float) aColor.getAlpha();
    }

    /**
     * Returns the number of components for a vertex point.
     */
    public int getPointCompCount()  { return _pointCompCount; }

    /**
     * Returns the number of components for a vertex color.
     */
    public int getColorCompCount()  { return _colorCompCount; }

    /**
     * Returns the global color.
     */
    public Color getColor()  { return _color; }

    /**
     * Sets the global color if set.
     */
    public void setColor(Color aColor)
    {
        _color = aColor;
    }

    /**
     * Returns whether shape surfaces are double-sided.
     */
    public boolean isDoubleSided()  { return _doubleSided; }

    /**
     * Sets whether shape surfaces are double-sided.
     */
    public void setDoubleSided(boolean aValue)
    {
        _doubleSided = aValue;
    }

    /**
     * Returns the next VertexArray, if part of a chain.
     */
    public VertexArray getNext()  { return _next; }

    /**
     * Sets the next VertexArray, if part of a chain.
     */
    public void setNext(VertexArray aVertexArray)  { _next = aVertexArray; }

    /**
     * Returns the last VertexArray (just returns this if no more).
     */
    public VertexArray getLast()
    {
        for (VertexArray va = this; ; va = va._next)
            if (va._next == null)
                return va;
    }

    /**
     * Sets the last VertexArray, if part of a chain.
     */
    public void setLast(VertexArray aVertexArray)
    {
        VertexArray vertexArrayLast = getLast();
        vertexArrayLast._next = aVertexArray;
    }

    /**
     * Returns the vertex points components array.
     */
    public float[] getPointsArray()
    {
        trim();
        return _pointsArray;
    }

    /**
     * Returns the vertex colorscomponents array.
     */
    public float[] getColorsArray()
    {
        trim();
        return _colorsArray;
    }

    /**
     * Returns whether color components array is set.
     */
    public boolean isColorsArraySet()
    {
        // If no colors, just return false
        if (_colorsArrayLen == 0)
            return false;

        // If fewer colors than points, complain and return false
        int colorCount = _colorsArrayLen / getColorCompCount();
        int pointCount = getPointCount();
        if (colorCount < pointCount) {
            System.err.println("VertexArray.isColorsArraySet: Insufficient colors for vertex count");
            return false;
        }

        // Return true
        return true;
    }

    /**
     * Returns bounds.
     */
    public Bounds3D getBounds3D()
    {
        // Create and init bounds
        Bounds3D bounds = new Bounds3D();
        bounds.setMinXYZ(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        bounds.setMaxXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        float[] pointsArray = getPointsArray();
        for (int i = 0, iMax = pointsArray.length; i < iMax; ) {
            float x = pointsArray[i++];
            float y = pointsArray[i++];
            float z = pointsArray[i++];
            bounds.addXYZ(x, y, z);
        }

        // Return
        return bounds;
    }

    /**
     * Trims arrays.
     */
    public void trim()
    {
        // Trim PointsArray
        if (_pointsArray.length != _pointsArrayLen)
            _pointsArray = Arrays.copyOf(_pointsArray, _pointsArrayLen);

        // Trim ColorsArray
        if (_colorsArray.length != _colorsArrayLen)
            _colorsArray = Arrays.copyOf(_colorsArray, _colorsArrayLen);
    }

    /**
     * Transforms points by transform.
     */
    public void transformPoints(Matrix3D aTrans)
    {
        Point3D point = new Point3D(0, 0, 0);
        for (int i = 0, iMax = getPointCount(); i < iMax; i++) {
            getPoint3D(point, i);
            aTrans.transformPoint(point);
            _pointsArray[i * 3] = (float) point.x;
            _pointsArray[i * 3 + 1] = (float) point.y;
            _pointsArray[i * 3 + 2] = (float) point.z;
        }
    }

    /**
     * Copies VertexArray for given transform.
     */
    public VertexArray copyForTransform(Matrix3D aTrans)
    {
        VertexArray clone = clone();
        clone.transformPoints(aTrans);
        return clone;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public VertexArray clone()
    {
        // Trim
        trim();

        // Do normal version
        VertexArray clone;
        try { clone = (VertexArray) super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Clone arrays
        clone._pointsArray = _pointsArray.clone();
        clone._colorsArray = _colorsArray.clone();

        // Clone next
        if (_next != null)
            clone._next = _next.clone();

        // Return clone
        return clone;
    }
}
