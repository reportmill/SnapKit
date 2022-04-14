/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.geom.Point;

/**
 * This class represents a 3D line.
 */
public class Line3D {

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
    public double getDistance2D()
    {
        return Point.getDistance(_p1.x, _p1.y, _p2.x, _p2.y);
    }
}
