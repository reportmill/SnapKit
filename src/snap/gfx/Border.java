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
    
    // Constants for properties
    public static final String Stroke_Prop = "Stroke";
    public static final String Color_Prop = "Color";

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
    protected Insets createInsets()  { return getWidth() > 0 ? new Insets(getWidth()) : Insets.EMPTY; }

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
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Stroke, Color
        aPropSet.addPropNamed(Stroke_Prop, Stroke.class, null);
        aPropSet.addPropNamed(Color_Prop, Color.class, null);
    }

    /**
     * Returns a value for a key.
     */
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Stroke, Color
            case Stroke_Prop: return getStroke();
            case Color_Prop: return getColor();

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

            // Stroke, Color
            case Stroke_Prop: break;
            case Color_Prop: break;

            // Do normal version
            default: throw new RuntimeException("Border.setPropValue: Unknown key: " + aPropName);
        }
    }

    /**
     * Standard clone implementation - only used internally (by copyFor methods).
     */
    @Override
    protected Border clone()
    {
        try {
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

    /**
     * Creates a border from given string.
     */
    public static Border of(Object anObj)
    {
        // Handle Border or null
        if (anObj instanceof Border || anObj == null)
            return (Border) anObj;

        // Parse string
        String str = anObj.toString().trim();
        String[] parts = str.split("\\s");

        // Assume color string or "color width" string
        Color color = Color.get(parts[0]);
        double width = Math.max(parts.length > 1 ? Convert.doubleValue(parts[1]) : 1, 1);
        return createLineBorder(color, width);
    }
}