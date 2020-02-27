/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Insets;
import snap.geom.Shape;
import snap.util.*;

/**
 * A class to represent a painted stroke.
 */
public abstract class Border implements XMLArchiver.Archivable {
    
    // Cached version of insets
    private Insets _insets = null;
    
    // Whether to paint above view
    private boolean _paintAbove;
    
    /**
     * Returns the insets.
     */
    public Insets getInsets()
    {
        if (_insets!=null) return _insets;
        return _insets = createInsets();
    }

    /**
     * Sets the insets.
     */
    public void setInsets(Insets theIns)  { _insets = theIns; }

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
     * Returns the name for border.
     */
    public String getName()  { return getClass().getSimpleName(); }

    /**
     * Returns whether the border paints above view.
     */
    public boolean isPaintAbove()  { return _paintAbove; }

    /**
     * Sets whether the border paints above view.
     */
    public void setPaintAbove(boolean aValue)  { _paintAbove = aValue; }

    /**
     * Paint border.
     */
    public void paint(Painter aPntr, Shape aShape)  { }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity and get other
        if(anObj==this) return true;
        Border other = anObj instanceof Border? (Border)anObj : null; if(other==null) return false;

        // Check Color, Width
        if(!other.getColor().equals(getColor())) return false;
        if(other.getWidth()!=getWidth()) return false;

        // Return true since all checks passed
        return true;
    }

    /**
     * Creates an empty border for inset.
     */
    public static Borders.EmptyBorder createEmptyBorder(double w)
    {
        return new Borders.EmptyBorder(w,w,w,w);
    }

    /**
     * Creates an empty border.
     */
    public static Borders.EmptyBorder createEmptyBorder(double tp, double rt, double bm, double lt)
    {
        return new Borders.EmptyBorder(tp,rt,bm,lt);
    }

    /**
     * Creates an empty border.
     */
    public static Borders.LineBorder createLineBorder(Color aColor, double aWidth)
    {
        return new Borders.LineBorder(aColor, aWidth);
    }

    /**
     * Creates a compound border.
     */
    public static Border createCompoundBorder(Border aB1, Border aB2)
    {
        return new Borders.CompoundBorder(aB1, aB2);
    }

    /**
     * Creates a compound border.
     */
    public static Borders.BevelBorder createLoweredBevelBorder()
    {
        return new Borders.BevelBorder(Borders.BevelBorder.LOWERED);
    }

    /**
     * XML unarchival.
     */
    public static Border fromXMLBorder(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Get type
        String type = anElement.getAttributeValue("type", "");

        // Create instance based on type
        Border border;
        if(type.equals("line")) border = new Borders.LineBorder();
        else if(type.equals("bevel")) border = new Borders.BevelBorder();
        else if(type.equals("etched")) border = new Borders.EtchBorder();
        else if(type.equals("empty")) border = new Borders.EmptyBorder();
        else border = new Borders.NullBorder();

        // Unarchive border and return
        border.fromXML(anArchiver, anElement);
        return border;
    }
}