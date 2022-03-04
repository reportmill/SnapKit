/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.gfx.Color;
import java.util.Arrays;

/**
 * This class manages raw vertex data (points, colors, normals, texture coords).
 */
public class VertexArray {

    // The float array to hold actual vertex point components
    private float[]  _pointArray = new float[24];

    // The number of components in vertex point array
    private int  _pointArrayLen = 0;

    // The float array to hold actual vertex color components
    private float[]  _colorArray = new float[0];

    // The number of components in vertex color array
    private int  _colorArrayLen = 0;

    // A global color
    private Color  _color;

    // The number of components per vertex point
    private int  _pointCompCount = 3;

    // The number of components per vertex color
    private int  _colorCompCount = 3;

    /**
     * Constructor.
     */
    public VertexArray()  { }

    /**
     * Returns the number of vertex points in array.
     */
    public int getPointCount()  { return _pointArrayLen / _pointCompCount; }

    /**
     * Adds value triplet to array.
     */
    public void addPoint(double aVal1, double aVal2, double aVal3)
    {
        // Expand Vertex components array if needed
        if (_pointArrayLen + 3 > _pointArray.length)
            _pointArray = Arrays.copyOf(_pointArray, Math.max(_pointArray.length * 2, 24));

        // Add values
        _pointArray[_pointArrayLen++] = (float) aVal1;
        _pointArray[_pointArrayLen++] = (float) aVal2;
        _pointArray[_pointArrayLen++] = (float) aVal3;
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
        aPoint.x = _pointArray[index];
        aPoint.y = _pointArray[index + 1];
        aPoint.z = _pointArray[index + 2];
        return aPoint;
    }

    /**
     * Adds a color to vertex color components array.
     */
    public void addColor(Color aColor)
    {
        // Expand color components array if needed
        if (_colorArrayLen + _colorCompCount > _colorArray.length)
            _colorArray = Arrays.copyOf(_colorArray, Math.max(_colorArray.length * 2, 24));

        // Add values
        _colorArray[_colorArrayLen++] = (float) aColor.getRed();
        _colorArray[_colorArrayLen++] = (float) aColor.getGreen();
        _colorArray[_colorArrayLen++] = (float) aColor.getBlue();
        if (_colorCompCount > 3)
            _colorArray[_colorArrayLen++] = (float) aColor.getAlpha();
    }

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
     * Returns the number of components for a vertex point.
     */
    public int getPointCompCount()  { return _pointCompCount; }

    /**
     * Returns the number of components for a vertex color.
     */
    public int getColorCompCount()  { return _colorCompCount; }

    /**
     * Returns the vertex points components array.
     */
    public float[] getPointArray()
    {
        // Trim VertexArray
        if (_pointArray.length != _pointArrayLen)
            _pointArray = Arrays.copyOf(_pointArray, _pointArrayLen);

        // Return
        return _pointArray;
    }

    /**
     * Returns the vertex color components array.
     */
    public float[] getColorArray()
    {
        // Trim ColorArray
        if (_colorArray.length != _colorArrayLen)
            _colorArray = Arrays.copyOf(_colorArray, _colorArrayLen);

        // Return
        return _colorArray;
    }

    /**
     * Returns whether color components array is set.
     */
    public boolean isColorArraySet()
    {
        // If no colors, just return false
        if (_colorArrayLen == 0)
            return false;

        // If fewer colors than points, complain and return false
        int colorCount = _colorArrayLen / getColorCompCount();
        int pointCount = getPointCount();
        if (colorCount < pointCount) {
            System.err.println("VertexArray.isColorArraySet: Insufficient colors for vertex count");
            return false;
        }

        // Return true
        return true;
    }

    /**
     * Returns bounds box.
     */
    public Box3D getBoundsBox()
    {
        // Create and init bounds box
        Box3D boundsBox = new Box3D();
        boundsBox.setMinXYZ(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        boundsBox.setMaxXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        Point3D point = new Point3D(0, 0, 0);
        for (int i = 0, iMax = getPointCount(); i < iMax; i++) {
            getPoint3D(point, i);
            boundsBox.addXYZ(point.x, point.y, point.z);
        }

        // Return
        return boundsBox;
    }
}
