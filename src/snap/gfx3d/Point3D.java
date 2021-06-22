/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.util.StringUtils;

/**
 *  This class represents a 3D point.
 */
public class Point3D implements Cloneable {
    
    // X Y Z components
    public double x, y, z;
    
    /**
     * Creates a Point3D from the x, y, z coords.
     */
    public Point3D(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Transforms the point by the given transform3d.
     */
    public Point3D transform(Transform3D xform)
    {
        return xform.transform(this);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        Point3D p = (Point3D) anObj;
        return p == this || (p.x == x && p.y == y && p.z == z);
    }

    /**
     * Standard clone implementation.
     */
    public Point3D clone()
    {
        return new Point3D(x,y,z);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return "Point3D [" + StringUtils.toString(x) + " " + StringUtils.toString(y) + " " + StringUtils.toString(z) + "]";
    }
}