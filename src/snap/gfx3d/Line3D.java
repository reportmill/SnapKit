/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Point;

/**
 * This class represents a 3D line.
 */
public class Line3D implements Cloneable{

    // The first point
    private Point3D  _p1;

    // The second point
    private Point3D  _p2;

    /**
     * Constructor.
     */
    public Line3D(Point3D p1, Point3D p2)
    {
        _p1 = p1; _p2 = p2;
    }

    /**
     * Constructor.
     */
    public Line3D(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        _p1 = new Point3D(x1, y1, z1);
        _p2 = new Point3D(x2, y2, z2);
    }

    /**
     * Returns the first point.
     */
    public Point3D getP1()  { return _p1; }

    /**
     * Returns the second point.
     */
    public Point3D getP2()  { return _p2; }

    /**
     * Returns the distance between the XY components (assumes the line is in View space).
     */
    public double getLength()
    {
        return Point3D.getDistance(_p1.x, _p1.y, _p1.z, _p2.x, _p2.y, _p2.z);
    }

    /**
     * Returns the distance between the XY components (assumes the line is in View space).
     */
    public double getLengthXY()
    {
        return Point.getDistance(_p1.x, _p1.y, _p2.x, _p2.y);
    }

    /**
     * Changes line end point for fractional value. E.g.: 2 doubles length of line.
     */
    public void extendEndPointForFraction(double aValue)
    {
        double length = getLength();
        double length2 = length * aValue;
        Vector3D dir = new Vector3D(_p1, _p2); dir.normalize();

        if (aValue >= 0) {
            _p2.x = _p1.x + dir.x * length2;
            _p2.y = _p1.y + dir.y * length2;
            _p2.z = _p1.z + dir.z * length2;
        }
        else {
            _p1.x = _p2.x + dir.x * length2;
            _p1.y = _p2.y + dir.y * length2;
            _p1.z = _p2.z + dir.z * length2;
        }
    }

    /**
     * Returns the angle between two XY points.
     */
    public double getAngle2DInDeg()
    {
        return getAngleBetweenPoints2DInDeg(_p1.x, _p1.y, _p2.x, _p2.y);
    }

    /**
     * Standard clone implementation.
     */
    public Line3D clone()
    {
        Line3D clone;
        try { clone = (Line3D) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        clone._p1 = _p1.clone();
        clone._p2 = _p2.clone();
        return clone;
    }

    /**
     * Returns the angle between two XY points.
     */
    public static double getAngleBetweenPoints2DInDeg(double x1, double y1, double x2, double y2)
    {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double angleDeg = Math.toDegrees(angle);
        return angleDeg;
    }
}
