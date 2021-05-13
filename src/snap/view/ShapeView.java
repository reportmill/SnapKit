/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.util.*;

/**
 * A View subclass to render shapes.
 */
public class ShapeView extends View {
    
    // The shape
    Shape _shape;
    
    // Whether to expand shape to view bounds
    boolean     _fillWidth, _fillHeight;

    // Constants for properties
    public static final String FillWidth_Prop = "FillWidth";
    public static final String FillHeight_Prop = "FillHeight";

    /**
     * Constructor.
     */
    public ShapeView()
    {
        super();
    }

    /**
     * Constructor.
     */
    public ShapeView(Shape aShape)
    {
        super();
        setShape(aShape);
        sizeToShape();
    }

    /**
     * Returns the shape.
     */
    public Shape getShape()
    {
        return _shape!=null ? _shape : (_shape=new Rect());
    }

    /**
     * Sets the shape.
     */
    public void setShape(Shape aShape)
    {
        _shape = aShape;
        repaint();
    }

    /**
     * Returns whether to fit shape width to view width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether to fill shape width to view width.
     */
    public void setFillWidth(boolean aValue)
    {
        if (aValue==_fillWidth) return;
        firePropChange(FillWidth_Prop, _fillWidth, _fillWidth=aValue);
    }

    /**
     * Returns whether to fit shape height to view height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether to fill shape height to view height.
     */
    public void setFillHeight(boolean aValue)
    {
        if (aValue==_fillHeight) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight=aValue);
    }

    /**
     * Returns whether to fit shape to view bounds.
     */
    public boolean isFillSize()  { return _fillWidth && _fillHeight; }

    /**
     * Sets whether to fill shape to view bounds.
     */
    public void setFillSize(boolean aValue)
    {
        setFillWidth(true);
        setFillHeight(true);
    }

    /**
     * Resizes view to shape size.
     */
    public void sizeToShape()
    {
        Insets ins = getInsetsAll();
        Shape shape = getShape();
        double shapeW = shape.getWidth() + ins.getWidth();
        double shapeH = shape.getHeight() + ins.getHeight();
        setSize(shapeW, shapeH);
    }

    /**
     * Override to return path as bounds shape.
     */
    public Shape getBoundsShape()
    {
        Shape shape = getShape();
        if (isFillSize())
            shape = shape.copyFor(getBoundsLocal());
        return shape;
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        return ins.left + getShape().getBounds().getMaxX() + ins.right;
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        return ins.top + getShape().getBounds().getMaxY() + ins.bottom;
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver);           // Archive basic shape attributes
        //e.add(_path.toXML(anArchiver));                 // Archive path
        return e;                                         // Return xml element
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);                         // Unarchive basic shape attributes
        XMLElement pathXML = anElement.get("path");                        // Unarchive path
        //_path = anArchiver.fromXML(pathXML, Path.class, this);
        return this;
    }
}