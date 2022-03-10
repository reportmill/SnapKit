/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.gfx.*;

/**
 * This class represents a 3D shape to be rendered in a G3DView.
 */
public abstract class Shape3D {

    // The parent shape that holds this shape
    private ParentShape3D  _parent;

    // Shape name
    private String  _name;

    // Shape fill
    private Color  _color;
    
    // Shape stroke
    private Stroke  _stroke;
    
    // Shape Stroke color
    private Color  _strokeColor;
    
    // Shape opacity
    private double  _opacity = 1;

    // Whether shape surfaces are double-sided
    private boolean  _doubleSided;

    // The path bounding box
    private Box3D  _boundsBox;

    /**
     * Constructor.
     */
    public Shape3D()  { }

    /**
     * Returns the parent that holds this shape.
     */
    public ParentShape3D getParent()  { return _parent; }

    /**
     * Sets the parent that holds this shape.
     */
    protected void setParent(ParentShape3D aParent)
    {
        _parent = aParent;
    }

    /**
     * Returns the name of shape.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name of shape.
     */
    public void setName(String aName)  { _name = aName; }

    /**
     * Returns the color of shape.
     */
    public Color getColor()  { return _color; }

    /**
     * Sets the color of shape.
     */
    public void setColor(Color aColor)  { _color = aColor; }

    /**
     * Returns the stroke of shape.
     */
    public Stroke getStroke()  { return _stroke; }

    /**
     * Sets the stroke of shape.
     */
    public void setStroke(Stroke aStroke)
    {
        _stroke = aStroke;
    }

    /**
     * Returns the stroke color of shape.
     */
    public void setStroke(Color aColor, double aWidth)
    {
        setStrokeColor(aColor);
        setStroke(Stroke.getStrokeRound(aWidth));
    }

    /**
     * Returns the stroke color of shape.
     */
    public Color getStrokeColor()  { return _strokeColor; }

    /**
     * Sets the stroke color of shape.
     */
    public void setStrokeColor(Color aColor)
    {
        _strokeColor = aColor;
    }

    /**
     * Returns the opacity of shape.
     */
    public double getOpacity()  { return _opacity; }

    /**
     * Sets the opacity of shape.
     */
    public void setOpacity(double aValue)
    {
        _opacity = aValue;
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
     * Returns the bounds box.
     */
    public Box3D getBoundsBox()
    {
        if (_boundsBox != null) return _boundsBox;
        Box3D boundsBox = createBoundsBox();
        return _boundsBox = boundsBox;
    }

    /**
     * Creates the bounds box.
     */
    protected abstract Box3D createBoundsBox();

    /**
     * Sets the bounds box.
     */
    public void setBoundsBox(Box3D aBox)
    {
        _boundsBox = aBox;
    }

    /**
     * Returns the max X for the path.
     */
    public double getMinX()  { return getBoundsBox().getMinX(); }

    /**
     * Returns the max Y for the path.
     */
    public double getMinY()  { return getBoundsBox().getMinY(); }

    /**
     * Returns the max Z for the path.
     */
    public double getMinZ()  { return getBoundsBox().getMinZ(); }

    /**
     * Returns the max X for the path.
     */
    public double getMaxX()  { return getBoundsBox().getMaxX(); }

    /**
     * Returns the max Y for the path.
     */
    public double getMaxY()  { return getBoundsBox().getMaxY(); }

    /**
     * Returns the max Z for the path.
     */
    public double getMaxZ()  { return getBoundsBox().getMaxZ(); }

    /**
     * Returns the array of Path3D that can render this shape.
     */
    public abstract VertexArray getVertexArray();

    /**
     * Clears cached values when shape changes.
     */
    protected void clearCachedValues()
    {
        _boundsBox = null;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propsStr = toStringProps();
        return className + " { " + propsStr + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        StringBuffer sb = new StringBuffer();
        String name = getName();
        if (name != null) sb.append("Name=").append(name).append(", ");
        Box3D boundsBox = getBoundsBox();
        Point3D minXYZ = boundsBox.getMinXYZ();
        Point3D maxXYZ = boundsBox.getMaxXYZ();
        sb.append("MinXYZ").append(minXYZ).append(", MaxXYZ").append(maxXYZ);
        return sb.toString();
    }
}