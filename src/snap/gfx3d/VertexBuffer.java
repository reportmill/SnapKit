/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.nio.DoubleBuffer;

/**
 * This class manages raw vertex data (position, normal vector, color).
 */
public class VertexBuffer {

    // The DoubleBuffer to hold actual doubles
    private DoubleBuffer  _doubleBuf = DoubleBuffer.allocate(24);

    // The number of vertexes
    private int  _vertexCount = 0;

    // The number of values per vertex
    private int  _vertexSize = 3;

    /**
     * Returns the number of vertices in buffer.
     */
    public int getCount()  { return _vertexCount; }

    /**
     * Returns the value for given vertex and offset.
     */
    public double getValueForVertexAndOffset(int anIndex, int anOffset)
    {
        int index = anIndex * _vertexSize + anOffset;
        return _doubleBuf.get(index);
    }

    /**
     * Adds value triplet to buffer.
     */
    public void addValues3(double aVal1, double aVal2, double aVal3)
    {
        _doubleBuf.put(aVal1);
        _doubleBuf.put(aVal2);
        _doubleBuf.put(aVal3);
        _vertexCount++;
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
        int index = anIndex * _vertexSize;
        aPoint.x = _doubleBuf.get(index);
        aPoint.y = _doubleBuf.get(index + 1);
        aPoint.z = _doubleBuf.get(index + 2);
        return aPoint;
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
        for (int i = 0, iMax = getCount(); i < iMax; i++) {
            getPoint3D(point, i);
            boundsBox.addXYZ(point.x, point.y, point.z);
        }

        // Return
        return boundsBox;
    }
}
