/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import snap.gfx.*;

/**
 * This class represents a 3D shape to be rendered in a G3DView.
 */
public abstract class Shape3D {

    // Shape fill
    private Color  _color;
    
    // Shape stroke
    private Stroke  _stroke = Stroke.Stroke1;
    
    // Shape Stroke color
    private Color  _strokeColor;
    
    // Shape opacity
    private double  _opacity = 1;
    
    /**
     * Constructor.
     */
    public Shape3D()  { }

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
     * Returns the array of Path3D that can render this shape.
     */
    public abstract Path3D[] getPath3Ds();
}