/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Insets;
import snap.geom.Shape;
import snap.props.PropObject;
import snap.props.PropSet;
import snap.util.*;

/**
 * A class to represent a painted stroke.
 */
public abstract class Border extends PropObject implements Cloneable, XMLArchiver.Archivable {
    
    // Cached version of insets
    private Insets  _insets;
    
    // Whether to paint above view
    private boolean  _paintAbove;

    // Constants for properties
    public static final String Stroke_Prop = "Stroke";
    public static final String Color_Prop = "Color";
    public static final String Insets_Prop = "Insets";
    public static final String PaintAbove_Prop = "PaintAbove";

    /**
     * Returns the insets.
     */
    public Insets getInsets()
    {
        if (_insets != null) return _insets;
        return _insets = createInsets();
    }

    /**
     * Creates the insets.
     */
    protected Insets createInsets()  { return Insets.EMPTY; }

    /**
     * Returns the basic color of the border.
     */
    public Color getColor()  { return Color.BLACK; }

    /**
     * Returns the basic width of the border.
     */
    public double getWidth()  { return 1; }

    /**
     * Returns the stroke of the border (maybe not be entirely accurate for fancy strokes).
     */
    public Stroke getStroke()  { return Stroke.getStroke(getWidth()); }

    /**
     * Returns the name for border.
     */
    public String getName()  { return getClass().getSimpleName(); }

    /**
     * Returns whether the border paints above view.
     */
    public boolean isPaintAbove()  { return _paintAbove; }

    /**
     * Paint border.
     */
    public void paint(Painter aPntr, Shape aShape)  { }

    /**
     * Copies border for given color.
     */
    public Border copyForColor(Color aColor)  { return this; }

    /**
     * Copies border for given stroke.
     */
    public Border copyForStroke(Stroke aStroke)  { return this; }

    /**
     * Copies border for given stroke width.
     */
    public Border copyForStrokeWidth(double aWidth)
    {
        Stroke newStroke = getStroke().copyForWidth(aWidth);
        return copyForStroke(newStroke);
    }

    /**
     * Copies border for given insets.
     */
    public Border copyForInsets(Insets theIns)
    {
        Border copy = clone();
        copy.setPropValue(Insets_Prop, theIns);
        return copy;
    }

    /**
     * Returns a border with given insets.
     */
    public Border copyFor(String aPropName, Object aValue)
    {
        Border copy = clone();
        copy.setPropValue(aPropName, aValue);
        return copy;
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Stroke, Color, Insets, PaintAbove
        aPropSet.addPropNamed(Stroke_Prop, Stroke.class, null);
        aPropSet.addPropNamed(Color_Prop, Color.class, null);
        aPropSet.addPropNamed(Insets_Prop, Insets.class, null);
        aPropSet.addPropNamed(PaintAbove_Prop, Insets.class, null);
    }

    /**
     * Returns a value for a key.
     */
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Stroke, Color, Insets, PaintAbove
            case Stroke_Prop: return getStroke();
            case Color_Prop: return getColor();
            case Insets_Prop: return getInsets();
            case PaintAbove_Prop: return isPaintAbove();

            // Do normal version
            default: throw new RuntimeException("Border.getPropValue: Unknown key: " + aPropName);
        }
    }

    /**
     * Sets a value for a key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Stroke, Color, Insets, PaintAbove
            case Stroke_Prop: break;
            case Color_Prop: break;
            case Insets_Prop: _insets = (Insets) aValue; break;
            case PaintAbove_Prop: _paintAbove = Convert.boolValue(aValue); break;

            // Do normal version
            default: throw new RuntimeException("Border.setPropValue: Unknown key: " + aPropName);
        }
    }

    /**
     * Standard clone implementation - only used internally (by copyFor methods).
     */
    protected Border clone()
    {
        try
        {
            Border copy = (Border) super.clone();
            copy._insets = null;
            return copy;
        }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity and get other
        if (anObj == this) return true;
        Border other = anObj instanceof Border ? (Border) anObj : null; if (other == null) return false;
        if (other.getClass() != getClass()) return false;

        // Check Color, Width
        if (!other.getColor().equals(getColor())) return false;
        if (other.getWidth() != getWidth()) return false;
        if (other.isPaintAbove() != isPaintAbove()) return false;

        // Return equal
        return true;
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        String className = getClass().getSimpleName();
        XMLElement e = new XMLElement(className);
        return e;
    }

    /**
     * XML Unarchival.
     */
    public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        return this;
    }

    /**
     * Returns a simple black border.
     */
    public static Border blackBorder()  { return Borders.BLACK_BORDER; }

    /**
     * Returns a simple empty border.
     */
    public static Border emptyBorder()  { return Borders.EMPTY_BORDER; }

    /**
     * Creates a line border for given color and width.
     */
    public static Borders.LineBorder createLineBorder(Color aColor, double aWidth)
    {
        return new Borders.LineBorder(aColor, aWidth);
    }

    /**
     * Creates a beveled border.
     */
    public static Borders.BevelBorder createLoweredBevelBorder()
    {
        return new Borders.BevelBorder();
    }
}