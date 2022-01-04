/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;

/**
 * This class represents a 3D box.
 */
public class Box3D {

    // Min XYZ
    private double  _minX, _minY, _minZ;

    // Max XYZ
    private double  _maxX, _maxY, _maxZ;

    /**
     * Constructor.
     */
    public Box3D()  { }

    /**
     * Constructor.
     */
    public Box3D(double aMinX, double aMinY, double aMinZ, double aMaxX, double aMaxY, double aMaxZ)
    {
        _minX = aMinX; _minY = aMinY; _minZ = aMinZ;
        _maxX = aMaxX; _maxY = aMaxY; _maxZ = aMaxZ;
    }

    /**
     * Returns the min X value.
     */
    public double getMinX()  { return _minX; }

    /**
     * Sets the min X value.
     */
    public void setMinX(double aValue)  { _minX = aValue; }

    /**
     * Returns the min Y value.
     */
    public double getMinY()  { return _minY; }

    /**
     * Sets the min Y value.
     */
    public void setMinY(double aValue)  { _minY = aValue; }

    /**
     * Returns the min Z value.
     */
    public double getMinZ()  { return _minZ; }

    /**
     * Sets the min Z value.
     */
    public void setMinZ(double aValue)  { _minZ = aValue; }

    /**
     * Returns the max X value.
     */
    public double getMaxX()  { return _maxX; }

    /**
     * Sets the max X value.
     */
    public void setMaxX(double aValue)
    {
        _maxX = aValue;
    }

    /**
     * Returns the max Y value.
     */
    public double getMaxY()  { return _maxY; }

    /**
     * Sets the max Y value.
     */
    public void setMaxY(double aValue)
    {
        _maxY = aValue;
    }

    /**
     * Returns the max Z value.
     */
    public double getMaxZ()  { return _maxZ; }

    /**
     * Sets the max Z value.
     */
    public void setMaxZ(double aValue)
    {
        _maxZ = aValue;
    }

    /**
     * Returns the mid X for the path.
     */
    public double getMidX()
    {
        return _minX + (_maxX - _minX) / 2;
    }

    /**
     * Returns the mid Y for the path.
     */
    public double getMidY()
    {
        return _minY + (_maxY - _minY) / 2;
    }

    /**
     * Returns the mid Z for the path.
     */
    public double getMidZ()
    {
        return _minZ + (_maxZ - _minZ) / 2;
    }

    /**
     * Returns the min XYZ point.
     */
    public Point3D getMinXYZ()
    {
        return new Point3D(_minX, _minY, _minZ);
    }

    /**
     * Sets the min XYZ.
     */
    public void setMinXYZ(double aX, double aY, double aZ)
    {
        setMinX(aX); setMinY(aY); setMinZ(aZ);
    }

    /**
     * Returns the max XYZ point.
     */
    public Point3D getMaxXYZ()
    {
        return new Point3D(_maxX, _maxY, _maxZ);
    }

    /**
     * Sets the max XYZ.
     */
    public void setMaxXYZ(double aX, double aY, double aZ)
    {
        setMaxX(aX); setMaxY(aY); setMaxZ(aZ);
    }

    /**
     * Returns the center point of this box.
     */
    public Point3D getCenter()
    {
        double cx = getMidX();
        double cy = getMidY();
        double cz = getMidZ();
        return new Point3D(cx, cy, cz);
    }

    /**
     * Expands this Box3D to include given point.
     */
    public void addXYZ(double aX, double aY, double aZ)
    {
        _minX = Math.min(_minX, aX);
        _minY = Math.min(_minY, aY);
        _minZ = Math.min(_minZ, aZ);
        _maxX = Math.max(_maxX, aX);
        _maxY = Math.max(_maxY, aY);
        _maxZ = Math.max(_maxZ, aZ);
    }

    /**
     * Expands this Box3D to include given point.
     */
    public void addBox(Box3D aBox)
    {
        addXYZ(aBox._minX, aBox._minY, aBox._minZ);
        addXYZ(aBox._maxX, aBox._maxY, aBox._maxZ);
    }

    /**
     * Transforms the path by the given transform3d.
     */
    public void transform(Transform3D xform)
    {
        Point3D minXYZ = xform.transformPoint(_minX, _minY, _minZ);
        Point3D maxXYZ = xform.transformPoint(_maxX, _maxY, _maxZ);
        setMinXYZ(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        setMinXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        addXYZ(minXYZ.x, minXYZ.y, minXYZ.z);
        addXYZ(maxXYZ.x, maxXYZ.y, maxXYZ.z);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String propsStr = toStringProps();
        return "Box3D { " + propsStr + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        Point3D minXYZ = getMinXYZ();
        Point3D maxXYZ = getMaxXYZ();
        return "MinXYZ=" + minXYZ + ", MaxXYZ=" + maxXYZ;
    }
}
