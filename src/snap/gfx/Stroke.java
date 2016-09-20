/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.*;

/**
 * A class to describe strokes.
 */
public class Stroke implements Cloneable {
    
    // The stroke width
    double       _width = 1;
    
    // The dash array
    float        _dashArray[];
    
    // The dash phase
    float        _dashPhase = 0;
    
    // Constants for cap
    public enum Cap { Butt, Round, Square }
    
    // Constant for common stroke
    public static final Stroke Stroke1 = new Stroke();
    public static final Stroke StrokeDash1 = new Stroke(1, new double[] { 2,2 }, 0);

/**
 * Creates a plain, black stroke.
 */
public Stroke()  { }

/**
 * Creates a stroke with the given line width.
 */
public Stroke(double aWidth)  { _width = aWidth; }

/**
 * Creates a stroke with the given line width, dash array and phase.
 */
public Stroke(double aWidth, float aDashAry[], float aDashPhase)
{
    _width = aWidth; _dashArray = aDashAry; _dashPhase = aDashPhase;
}

/**
 * Creates a stroke with the given line width, dash array and phase.
 */
public Stroke(double aWidth, double aDashAry[], double aDashPhase)
{
    float da[] = null;
    if(aDashAry!=null) { da = new float[aDashAry.length]; for(int i=0;i<aDashAry.length;i++) da[i] = (float)aDashAry[i]; }
    _width = aWidth; _dashArray = da; _dashPhase = (float)aDashPhase;
}

/**
 * Returns the line width of this stroke.
 */
public double getWidth()  { return _width; }

/**
 * Returns the dash array for this stroke.
 */
public float[] getDashArray()  { return _dashArray; }

/**
 * Returns the dash array for this stroke as a string.
 */
public String getDashArrayString()  { return getDashArrayString(getDashArray(), ", "); }

/**
 * Returns a dash array for given dash array string and delimeter.
 */
public static float[] getDashArray(String aString, String aDelimeter)
{
    // Just return null if empty
    if(aString==null || aString.length()==0) return null;
    
    String dashStrings[] = aString.split(",");
    float dashArray[] = new float[dashStrings.length];
    for(int i=0; i<dashStrings.length; i++) dashArray[i] = SnapUtils.floatValue(dashStrings[i]);
    return dashArray;
}

/**
 * Returns the dash array for this stroke as a string.
 */
public static String getDashArrayString(float dashArray[], String aDelimiter)
{
    // Just return null if empty
    if(dashArray==null || dashArray.length==0) return null;
    
    // Build dash array string
    String dashArrayString = SnapUtils.stringValue(dashArray[0]);
    for(int i=1; i<dashArray.length; i++) dashArrayString += aDelimiter + SnapUtils.stringValue(dashArray[i]);
    
    // Return dash array string
    return dashArrayString;
}

/**
 * Returns the dash phase.
 */
public float getDashPhase()  { return _dashPhase; }

/**
 * Returns a copy of this stroke with given width.
 */
public Stroke copyForWidth(double aWidth)
{
    if(_width==aWidth) return this;
    Stroke clone = clone(); clone._width = aWidth; return clone;
}

/**
 * Returns a dashed version of this stroke.
 */
public Stroke copyForDashes(float ... theDashAry)
{
    Stroke clone = clone(); clone._dashArray = theDashAry; return clone;
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and get other
    if(anObj==this) return true;
    Stroke other = anObj instanceof Stroke? (Stroke)anObj : null; if(other==null) return false;
    
    // Check Width, DashArray, DashPhase
    if(!MathUtils.equals(other._width, _width)) return false;
    if(!ArrayUtils.equals(other._dashArray, _dashArray)) return false;
    if(other._dashPhase!=_dashPhase) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard clone implementation.
 */
public Stroke clone()
{
    try { return (Stroke)super.clone(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

}