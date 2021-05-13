/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.*;

/**
 * A class to describe strokes.
 */
public class Stroke implements Cloneable, XMLArchiver.Archivable {
    
    // The stroke width
    private double  _width = DEFAULT_WIDTH;
    
    // The cap - how a stroke renders endpoints
    private Cap  _cap = DEFAULT_CAP;
    
    // The join - how a stroke renders the join between two segements
    private Join  _join = DEFAULT_JOIN;
    
    // The limit to miter joins
    private double  _miterLimit = DEFAULT_MITER_LIMIT;
    
    // The dash array
    private double[]  _dashArray = DEFAULT_DASH_ARRAY;
    
    // The dash offset
    private double  _dashOffset = DEFAULT_DASH_OFFSET;
    
    // Constants for cap
    public enum Cap { Butt, Round, Square }
    public enum Join { Miter, Round, Bevel }
    
    // Constants for properties
    public static final String Width_Prop = "Width";
    public static final String Cap_Prop = "Cap";
    public static final String Join_Prop = "Join";
    public static final String MiterLimit_Prop = "MiterLimit";
    public static final String DashArray_Prop = "DashArray";
    public static final String DashOffset_Prop = "DashOffset";

    // Constants for property defaults
    public static final double DEFAULT_WIDTH = 1;
    public static final Cap DEFAULT_CAP = Cap.Butt;
    public static final Join DEFAULT_JOIN = Join.Miter;
    public static final double DEFAULT_MITER_LIMIT = 10;
    public static final double[] DEFAULT_DASH_ARRAY = null;
    public static final double DEFAULT_DASH_OFFSET = 0;

    // Constants for dashes
    public static final double[] DASH_SOLID = null;
    public static final double[] DASH_DOT = new double[] { 4, 4 };
    public static final double[] DASH_DASH = new double[] { 8, 8 };
    public static final double[] DASH_LONG_DASH = new double[] { 12, 12 };
    public static final double[] DASH_DASH_DOT = new double[] { 8, 4, 4, 4 };
    public static final double[] DASH_LONG_DASH_DOT = new double[] { 12, 8, 4, 8 };
    public static final String[] DASHES_ALL_NAMES = { "Solid", "Dot", "Dash", "LongDash", "DashDot", "LongDashDot" };
    public static final double[][] DASHES_ALL = { DASH_SOLID, DASH_DOT, DASH_DASH, DASH_LONG_DASH, DASH_DASH_DOT, DASH_LONG_DASH_DOT };

    // Constant for common strokes
    public static final Stroke Stroke1 = new Stroke();
    public static final Stroke Stroke2 = new Stroke(2);
    public static final Stroke StrokeRound1 = new Stroke(1, Cap.Round, Join.Round, 0);
    public static final Stroke StrokeRound2 = new Stroke(2, Cap.Round, Join.Round, 0);
    public static final Stroke StrokeDash1 = new Stroke(1, new double[] { 2,2 }, 0);

    /**
     * Creates a plain, black stroke.
     */
    public Stroke()
    {
        super();
    }

    /**
     * Creates a stroke with the given line width.
     */
    public Stroke(double aWidth)
    {
        _width = aWidth;
        _cap = Cap.Round;
    }

    /**
     * Creates a stroke with the given line width, cap, joint, miter-limit.
     */
    public Stroke(double aWidth, Cap aCap, Join aJoin, double aMiterLimit)
    {
        this(aWidth, aCap, aJoin, aMiterLimit, DEFAULT_DASH_ARRAY, DEFAULT_DASH_OFFSET);
    }

    /**
     * Creates a stroke with the given line width, dash array and offset.
     */
    public Stroke(double aWidth, float[] aDashAry, float aDashOffset)
    {
        this(aWidth, ArrayUtils.doubleArray(aDashAry), aDashOffset);
    }

    /**
     * Creates a stroke with the given line width, dash array and offset.
     */
    public Stroke(double aWidth, double[] aDashAry, double aDashOffset)
    {
        this(aWidth, DEFAULT_CAP, DEFAULT_JOIN, DEFAULT_MITER_LIMIT, aDashAry, aDashOffset);
    }

    /**
     * Creates a stroke with the given line width, dash array and offset.
     */
    public Stroke(double aWidth, Cap aCap, Join aJoin, double aMiterLimit, float[] aDashAry, float aDashOffset)
    {
        this(aWidth, aCap, aJoin, aMiterLimit, ArrayUtils.doubleArray(aDashAry), aDashOffset);
    }

    /**
     * Creates a stroke with the given line width, dash array and offset.
     */
    public Stroke(double aWidth, Cap aCap, Join aJoin, double aMiterLimit, double[] aDashAry, double aDashOffset)
    {
        _width = aWidth;
        _cap = aCap;
        _join = aJoin;
        _miterLimit = aMiterLimit;
        _dashArray = aDashAry;
        _dashOffset = aDashOffset;
    }

    /**
     * Returns the line width of this stroke.
     */
    public double getWidth()  { return _width; }

    /**
     * Returns the line cap - how a stroke renders endpoints.
     */
    public Cap getCap()  { return _cap; }

    /**
     * Returns join - how a stroke renders the join between two segements.
     */
    public Join getJoin()  { return _join; }

    /**
     * Returns the limit to miter joins.
     */
    public double getMiterLimit()  { return _miterLimit; }

    /**
     * Returns the dash array for this stroke.
     */
    public double[] getDashArray()  { return _dashArray; }

    /**
     * Returns the dash offset.
     */
    public double getDashOffset()  { return _dashOffset; }

    /**
     * Returns a copy of this stroke with given width.
     */
    public Stroke copyForWidth(double aWidth)
    {
        if (aWidth == _width) return this;
        Stroke clone = clone();
        clone._width = aWidth;
        return clone;
    }

    /**
     * Returns a copy of this stroke with given width.
     */
    public Stroke copyForCap(Cap aCap)
    {
        if (aCap == _cap) return this;
        Stroke clone = clone();
        clone._cap = aCap;
        return clone;
    }

    /**
     * Returns a dashed version of this stroke.
     */
    public Stroke copyForDashes(double ... theDashAry)
    {
        Stroke clone = clone();
        clone._dashArray = theDashAry;
        return clone;
    }

    /**
     * Returns a dashed version of this stroke.
     */
    public Stroke copyForDashOffset(double anOffset)
    {
        Stroke clone = clone();
        clone._dashOffset = anOffset;
        return clone;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity and get other
        if (anObj == this) return true;
        Stroke other = anObj instanceof Stroke ? (Stroke) anObj : null;
        if (other == null) return false;

        // Check Width, Cap, Join, MiterLimit
        if (!MathUtils.equals(other._width, _width))
            return false;
        if (other._cap != _cap)
            return false;
        if (other._join != _join)
            return false;
        if (!MathUtils.equals(other._miterLimit, _miterLimit))
            return false;

        // Check DashArray, DashPhase
        if (!ArrayUtils.equals(other._dashArray, _dashArray))
            return false;
        if (other._dashOffset != _dashOffset)
            return false;

        // Return true since all checks passed
        return true;
    }

    /**
     * Standard clone implementation.
     */
    public Stroke clone()
    {
        try { return (Stroke)super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = toXML(null).getString();
        return str;
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Create xml element
        XMLElement e = new XMLElement("Stroke");

        // Archive Width, Cap, Join, MiterLimit
        e.add(Width_Prop, getWidth());
        if (getCap() != DEFAULT_CAP)
            e.add(Cap_Prop, getCap());
        if (getJoin() != DEFAULT_JOIN)
            e.add(Join_Prop, getJoin());
        if (getMiterLimit() != DEFAULT_MITER_LIMIT)
            e.add(Join_Prop, getJoin());

        // Archive DashArray, DashOffset
        if (!ArrayUtils.equals(getDashArray(), DEFAULT_DASH_ARRAY))
            e.add(DashArray_Prop, getDashArrayString(this));
        if (getDashOffset() != DEFAULT_DASH_OFFSET)
            e.add(DashOffset_Prop, getDashOffset());

        // Return xml
        return e;
    }

    /**
     * XML Unarchival.
     */
    public Stroke fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive Width, Cap, Join, MiterLimit
        if(anElement.hasAttribute(Width_Prop))
            _width = anElement.getAttributeDoubleValue(Width_Prop);
        if(anElement.hasAttribute(Cap_Prop))
            _cap = Cap.valueOf(anElement.getAttributeValue(Cap_Prop));
        if(anElement.hasAttribute(Join_Prop))
            _join = Join.valueOf(anElement.getAttributeValue(Join_Prop));
        if(anElement.hasAttribute(MiterLimit_Prop))
            _miterLimit = anElement.getAttributeDoubleValue(MiterLimit_Prop);

        // Unarchive DashArray, DashOffset
        if(anElement.hasAttribute(DashArray_Prop))
            _dashArray = getDashArray(anElement.getAttributeValue(DashArray_Prop));
        if(anElement.hasAttribute(DashOffset_Prop))
            _dashOffset = anElement.getAttributeDoubleValue(DashOffset_Prop);

        // Return this stroke
        return this;
    }

    /**
     * Returns a stroke for given line width.
     */
    public static Stroke getStroke(double aLineWidth)
    {
        if (aLineWidth == 1) return Stroke1;
        if (aLineWidth == 2) return Stroke2;
        return new Stroke(aLineWidth);
    }

    /**
     * Returns a stroke for given line width.
     */
    public static Stroke getStrokeRound(double aLineWidth)
    {
        if (aLineWidth == 1) return StrokeRound1;
        if (aLineWidth == 2) return StrokeRound2;
        return StrokeRound1.copyForWidth(aLineWidth);
    }

    /**
     * Returns the dash array for given border as a string.
     */
    public static String getDashArrayString(Stroke aStroke)
    {
        double[] dashes = aStroke!=null ? aStroke.getDashArray() : null;
        return getDashArrayString(dashes);
    }

    /**
     * Returns the dash array for this stroke as a string.
     */
    private static String getDashArrayString(double[] dashArray)
    {
        return getDashArrayString(dashArray, ", ");
    }

    /**
     * Returns the dash array for this stroke as a string.
     */
    public static String getDashArrayString(double[] dashArray, String aDelimiter)
    {
        // Just return null if empty
        if (dashArray == null || dashArray.length == 0) return null;

        // Build dash array string
        String str = SnapUtils.stringValue(dashArray[0]);
        for (int i=1; i<dashArray.length; i++)
            str += aDelimiter + SnapUtils.stringValue(dashArray[i]);

        // Return dash array string
        return str;
    }

    /**
     * Returns the dash array for this stroke as a string.
     */
    public static String getDashArrayNameOrString(double[] dashArray)
    {
        // Handle null/empty dash array
        if (dashArray == null || dashArray.length == 0)
            return "Solid";

        // Handle dash array constants
        for (int i=1; i<DASHES_ALL.length; i++) {
            double[] constDashArray = DASHES_ALL[i];
            if (ArrayUtils.equals(dashArray, constDashArray))
                return DASHES_ALL_NAMES[i];
        }

        // Return explicit dash string (array not recognized as constant)
        return getDashArrayString(dashArray);
    }

    /**
     * Returns a dash array for given dash array string and delimeter.
     */
    public static double[] getDashArray(String aString)
    {
        // Just return null if empty
        if (aString == null || aString.length() == 0) return null;

        String[] dashStrings = aString.split(",");
        double[] dashArray = new double[dashStrings.length];
        for (int i=0; i<dashStrings.length; i++)
            dashArray[i] = SnapUtils.doubleValue(dashStrings[i]);
        return dashArray;
    }
}