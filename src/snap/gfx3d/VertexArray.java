/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.gfx.Color;
import snap.gfx.Image;

import java.util.Arrays;

/**
 * This class manages raw vertex data (points, colors, normals, texture coords).
 */
public class VertexArray implements Cloneable {

    // The float array to hold vertex point components
    private float[]  _pointArray = new float[24];

    // The number of components in vertex points array
    private int  _pointArrayLen = 0;

    // The float array to hold vertex color components
    private float[]  _colorArray = new float[0];

    // The number of colors in vertex colors array
    private int  _colorArrayLen = 0;

    // The float array to hold vertex texture coords
    private float[]  _texCoordArray = new float[0];

    // The number of entries in vertex texture coords array
    private int  _texCoordArrayLen = 0;

    // The index array
    private int[]  _indexArray = new int[0];

    // The number of entries in vertex index array
    private int  _indexArrayLen = 0;

    // The number of components per vertex point
    private int  _pointCompCount = 3;

    // The number of components per vertex color
    private int  _colorCompCount = 3;

    // A global color
    private Color  _color;

    // Whether triangles are double-sided
    private boolean  _doubleSided;

    // A texture associated with the geometry
    private Texture  _texture;

    // The next VertexArray if this one is part of a chain
    private VertexArray  _next;

    // Constant for number of components in texture coords
    private static final int TEX_COORD_COMP_COUNT = 2;

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
        return getPoint3D(new Point3D(), anIndex);
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
    public void addColor(double aRed, double aGreen, double aBlue, double anAlpha)
    {
        // Expand color components array if needed
        if (_colorArrayLen + _colorCompCount > _colorArray.length)
            _colorArray = Arrays.copyOf(_colorArray, Math.max(_colorArray.length * 2, 24));

        // Add values
        _colorArray[_colorArrayLen++] = (float) aRed;
        _colorArray[_colorArrayLen++] = (float) aGreen;
        _colorArray[_colorArrayLen++] = (float) aBlue;
        if (_colorCompCount > 3)
            _colorArray[_colorArrayLen++] = (float) anAlpha;
    }

    /**
     * Adds a color to vertex color components array.
     */
    public void addColor(Color aColor)
    {
        addColor(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), aColor.getAlpha());
    }

    /**
     * Adds a texture coord to vertex texture coords array.
     */
    public void addTexCoord(double aU, double aV)
    {
        // Expand color components array if needed
        if (_texCoordArrayLen + TEX_COORD_COMP_COUNT > _texCoordArray.length)
            _texCoordArray = Arrays.copyOf(_texCoordArray, Math.max(_texCoordArray.length * 2, 24));

        // Add values
        _texCoordArray[_texCoordArrayLen++] = (float) aU;
        _texCoordArray[_texCoordArrayLen++] = (float) aV;
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
     * Returns the texture to render on geometry surface.
     */
    public Texture getTexture()  { return _texture; }

    /**
     * Sets the texture to render on geometry surface.
     */
    public void setTexture(Texture aTexture)
    {
        _texture = aTexture;
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
    public float[] getPointArray()
    {
        trim();
        return _pointArray;
    }

    /**
     * Sets the vertex points components array.
     */
    public void setPointArray(float[] pointArray)
    {
        _pointArray = pointArray;
        _pointArrayLen = pointArray.length;
    }

    /**
     * Returns the vertex colors components array.
     */
    public float[] getColorArray()
    {
        trim();
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
     * Returns the vertex texture coords array.
     */
    public float[] getTexCoordArray()
    {
        trim();
        return _texCoordArray;
    }

    /**
     * Sets the vertex texture coords array.
     */
    public void setTexCoordArray(float[] texCoordArray)
    {
        _texCoordArray = texCoordArray;
        _texCoordArrayLen = texCoordArray.length;
    }

    /**
     * Returns whether texture coords array is set.
     */
    public boolean isTexCoordArraySet()
    {
        return _texCoordArrayLen > 0 && getTexture() != null;
    }

    /**
     * Returns whether texture is set and loaded and text coords are set.
     */
    public boolean isTextureSetAndReady()
    {
        Texture texture = getTexture(); if (texture == null) return false;
        Image image = texture.getImage(); if (!image.isLoaded()) return false;
        return isTexCoordArraySet();
    }

    /**
     * Returns the index array, if points have special ordering.
     */
    public int[] getIndexArray()
    {
        trim();
        return _indexArray;
    }

    /**
     * Sets the index array, if points have special ordering.
     */
    public void setIndexArray(int[] indexArray)
    {
        _indexArray = indexArray;
        _indexArrayLen = indexArray.length;
    }

    /**
     * Returns whether index array is set.
     */
    public boolean isIndexArraySet()  { return _indexArray != null && _indexArrayLen > 0; }

    /**
     * Returns bounds.
     */
    public Bounds3D getBounds3D()
    {
        // Create and init bounds
        Bounds3D bounds = new Bounds3D();
        bounds.setMinXYZ(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        bounds.setMaxXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        float[] pointArray = getPointArray();
        for (int i = 0, iMax = pointArray.length; i < iMax; ) {
            float x = pointArray[i++];
            float y = pointArray[i++];
            float z = pointArray[i++];
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
        // Trim PointArray
        if (_pointArray.length != _pointArrayLen)
            _pointArray = Arrays.copyOf(_pointArray, _pointArrayLen);

        // Trim ColorArray
        if (_colorArray.length != _colorArrayLen)
            _colorArray = Arrays.copyOf(_colorArray, _colorArrayLen);

        // Trim TexCoordArray
        if (_texCoordArray.length != _texCoordArrayLen)
            _texCoordArray = Arrays.copyOf(_texCoordArray, _texCoordArrayLen);

        // Trim IndexArray
        if (_indexArray.length != _indexArrayLen)
            _indexArray = Arrays.copyOf(_indexArray, _indexArrayLen);
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
            _pointArray[i * 3] = (float) point.x;
            _pointArray[i * 3 + 1] = (float) point.y;
            _pointArray[i * 3 + 2] = (float) point.z;
        }

        // If Next VertexArray, forward on
        if (_next != null)
            _next.transformPoints(aTrans);
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
        clone._pointArray = _pointArray.clone();
        clone._colorArray = _colorArray.clone();
        clone._texCoordArray = _texCoordArray.clone();
        clone._indexArray = _indexArray.clone();

        // Clone next
        if (_next != null)
            clone._next = _next.clone();

        // Return clone
        return clone;
    }
}
