/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.gfx.*;
import snap.props.PropObject;

/**
 * This class represents a 3D shape to be rendered in a G3DView.
 */
public abstract class Shape3D extends PropObject {

    // The parent shape that holds this shape
    private ParentShape _parent;

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

    // Whether shape is visible
    private boolean  _visible = true;

    // Whether shape surfaces are double-sided
    private boolean  _doubleSided;

    // The path bounding box
    private Bounds3D  _bounds3D;

    // Whether to try to render sides smoothly (for paths with curves)
    protected boolean _smoothSides;

    /**
     * Constructor.
     */
    public Shape3D()  { }

    /**
     * Returns the parent that holds this shape.
     */
    public ParentShape getParent()  { return _parent; }

    /**
     * Sets the parent that holds this shape.
     */
    protected void setParent(ParentShape aParent)
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
    public void setColor(Color aColor)
    {
        _color = aColor;
        if (_smoothSides)
            _strokeColor = _color;
    }

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
     * Returns whether shape is visible.
     */
    public boolean isVisible()  { return _visible; }

    /**
     * Sets whether shape is visible.
     */
    public void setVisible(boolean aValue)
    {
        _visible = aValue;
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
     * Called to make sides smooth.
     */
    public void setSmoothSides(boolean aValue)
    {
        _smoothSides = aValue;
    }

    /**
     * Returns the bounds.
     */
    public Bounds3D getBounds3D()
    {
        if (_bounds3D != null) return _bounds3D;
        Bounds3D bounds3D = createBounds3D();
        return _bounds3D = bounds3D;
    }

    /**
     * Creates the bounds.
     */
    protected abstract Bounds3D createBounds3D();

    /**
     * Sets the bounds.
     */
    public void setBounds3D(Bounds3D aBox)
    {
        _bounds3D = aBox;
    }

    /**
     * Returns the max X for the path.
     */
    public double getMinX()  { return getBounds3D().getMinX(); }

    /**
     * Returns the max Y for the path.
     */
    public double getMinY()  { return getBounds3D().getMinY(); }

    /**
     * Returns the max Z for the path.
     */
    public double getMinZ()  { return getBounds3D().getMinZ(); }

    /**
     * Returns the max X for the path.
     */
    public double getMaxX()  { return getBounds3D().getMaxX(); }

    /**
     * Returns the max Y for the path.
     */
    public double getMaxY()  { return getBounds3D().getMaxY(); }

    /**
     * Returns the max Z for the path.
     */
    public double getMaxZ()  { return getBounds3D().getMaxZ(); }

    /**
     * Returns the center point of the path.
     */
    public Point3D getBoundsCenter()
    {
        Bounds3D bounds = getBounds3D();
        return bounds.getCenter();
    }

    /**
     * Returns the array of Path3D that can render this shape.
     */
    public abstract VertexArray getTriangleArray();

    /**
     * Clears cached values when shape changes.
     */
    protected void clearCachedValues()
    {
        _bounds3D = null;
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        StringBuilder sb = new StringBuilder();
        String name = getName();
        if (name != null)
            sb.append("Name=").append(name);

        // Append Bounds
        if (_bounds3D != null) {
            Point3D minXYZ = _bounds3D.getMinXYZ();
            Point3D maxXYZ = _bounds3D.getMaxXYZ();
            sb.append(", MinXYZ=").append(minXYZ).append(", MaxXYZ=").append(maxXYZ);
        }

        // Return
        return sb.toString();
    }
}