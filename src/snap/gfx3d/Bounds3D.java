/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;

/**
 * This class represents bounds for 3D shapes (XYZ min/max).
 */
public class Bounds3D implements Cloneable {

    // Min XYZ
    private double  _minX, _minY, _minZ;

    // Max XYZ
    private double  _maxX, _maxY, _maxZ;

    /**
     * Constructor.
     */
    public Bounds3D()  { }

    /**
     * Constructor.
     */
    public Bounds3D(double aMinX, double aMinY, double aMinZ, double aMaxX, double aMaxY, double aMaxZ)
    {
        _minX = aMinX; _minY = aMinY; _minZ = aMinZ;
        _maxX = aMaxX; _maxY = aMaxY; _maxZ = aMaxZ;
    }

    /**
     * Returns the box width.
     */
    public double getWidth()  { return getMaxX() - getMinX(); }

    /**
     * Returns the box height.
     */
    public double getHeight()  { return getMaxY() - getMinY(); }

    /**
     * Returns the box depth.
     */
    public double getDepth()  { return getMaxZ() - getMinZ(); }

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
    public void addBox(Bounds3D aBox)
    {
        addXYZ(aBox._minX, aBox._minY, aBox._minZ);
        addXYZ(aBox._maxX, aBox._maxY, aBox._maxZ);
    }

    /**
     * Returns the corner points.
     */
    public Point3D[] getCornerPoints()
    {
        Point3D[] points = new Point3D[8];
        points[0] = new Point3D(_minX, _minY, _minZ);
        points[1] = new Point3D(_maxX, _minY, _minZ);
        points[2] = new Point3D(_maxX, _maxY, _minZ);
        points[3] = new Point3D(_minX, _maxY, _minZ);
        points[4] = new Point3D(_minX, _minY, _maxZ);
        points[5] = new Point3D(_maxX, _minY, _maxZ);
        points[6] = new Point3D(_maxX, _maxY, _maxZ);
        points[7] = new Point3D(_minX, _maxY, _maxZ);
        return points;
    }

    /**
     * Transforms the path by the given transform3d.
     */
    public void transform(Matrix3D aMatrix)
    {
        // Get corner points in given transform coords
        Point3D[] cornerPoints = getCornerPoints();
        for (Point3D point : cornerPoints)
            aMatrix.transformPoint(point);

        // Reset min/max XYZ values and add transformed points
        setMinXYZ(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        setMaxXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (Point3D point : cornerPoints)
            addXYZ(point.x, point.y, point.z);
    }

    /**
     * Returns a copy of the bounds for given transform.
     */
    public Bounds3D copyForMatrix(Matrix3D aMatrix)
    {
        Bounds3D copy = clone();
        copy.transform(aMatrix);
        return copy;
    }

    /**
     * Override to support Cloneable.
     */
    @Override
    public Bounds3D clone()
    {
        Bounds3D clone;
        try { clone = (Bounds3D) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException("Box3D.clone: " + e); }
        return clone;
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
