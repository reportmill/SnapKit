/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.RoundRect;
import snap.geom.Shape;

/**
 * A View subclass for rects.
 */
public class RectView extends View {

    // The radius
    private double  _radius;

    // The rect
    private RoundRect  _rect;

    // Constants for properties
    public static final String Radius_Prop = "Radius";

    /**
     * Constructor.
     */
    public RectView()  { }

    /**
     * Creates a new RectView for given bounds.
     */
    public RectView(double aX, double aY, double aW, double aH)
    {
        setBounds(aX, aY, aW, aH);
        setPrefSize(aW, aH);
    }

    /**
     * Returns the rect.
     */
    public RoundRect getRect()
    {
        if (_rect != null) return _rect;
        RoundRect rect = new RoundRect(0, 0, getWidth(), getHeight(), getRadius());
        return _rect = rect;
    }

    /**
     * Returns the radius.
     */
    public double getRadius()  { return _radius; }

    /**
     * Sets the radius.
     */
    public void setRadius(double anAngle)
    {
        if (anAngle == _radius) return;
        repaint();
        firePropChange(Radius_Prop, _radius, _radius = anAngle);
        _rect = null;
    }

    /**
     * Returns the bounds path.
     */
    public Shape getBoundsShape()
    {
        return _radius > 0 ? getRect() : getRect().getBounds();
    }

    /**
     * Override to reset rect.
     */
    public void setWidth(double aValue)
    {
        if (aValue == getWidth()) return;
        super.setWidth(aValue);
        _rect = null;
    }

    /**
     * Override to reset rect.
     */
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        _rect = null;
    }
}