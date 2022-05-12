/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;

/**
 * This Shape3D subclass just wraps around a VertexArray.
 */
public class VertexArrayShape extends Shape3D {

    // The VertexArray
    private VertexArray  _vertexArray;

    /**
     * Constructor.
     */
    public VertexArrayShape()
    {
        _vertexArray = new VertexArray();
    }

    /**
     * Constructor.
     */
    public VertexArrayShape(VertexArray aVertexArray)
    {
        _vertexArray = aVertexArray;
        setDoubleSided(_vertexArray.isDoubleSided());
    }

    /**
     * Returns the VertexArray.
     */
    public VertexArray getVertexArray()  { return _vertexArray; }

    /**
     * Returns the VertexArray.
     */
    @Override
    public VertexArray getTriangleArray()
    {
        return _vertexArray;
    }

    /**
     * Override to get from VertexArray.
     */
    @Override
    protected Bounds3D createBounds3D()
    {
        VertexArray vertexArray = getTriangleArray();
        return vertexArray.getBounds3D();
    }

    /**
     * Override to forward to VertexArray.
     */
    @Override
    public void setDoubleSided(boolean aValue)
    {
        super.setDoubleSided(aValue);
        _vertexArray.setDoubleSided(aValue);
    }
}
