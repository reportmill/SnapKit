/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.pdf.write;
import java.util.*;
import snap.gfx.*;

/**
 * Represents a PDF graphics state stack with graphic state objects.
 */
public class PDFGStateStack {

    // The current graphics state
    GState             _gstate = new GState();

    // The graphics state stack
    List <GState>      _gstates = new ArrayList();

/**
 * Creates a new PDF graphics state stack.
 */
public PDFGStateStack()
{
    // Add default gstate
    _gstates.add(_gstate);
}

/**
 * Returns the current gstate.
 */
public GState getGState()  { return _gstate; }

/**
 * Saves the current graphics state of the writer.
 */
public void gsave()  { _gstates.add(_gstate = _gstate.clone()); }

/**
 * Restores the last graphics state of the writer.
 */
public void grestore()
{
    _gstates.remove(_gstates.size()-1);
    _gstate = _gstates.get(_gstates.size()-1);
}

/**
 * Returns the fill color.
 */
public Color getFillColor()  { return _gstate.fillColor; }

/**
 * Sets the fill color.
 */
public void setFillColor(Color aColor)  { _gstate.fillColor = aColor; }

/**
 * Returns the stroke color.
 */
public Color getStrokeColor()  { return _gstate.strokeColor; }

/**
 * Sets the stroke color.
 */
public void setStrokeColor(Color aColor)  { _gstate.strokeColor = aColor; }

/**
 * Returns the stroke width.
 */
public double getStrokeWidth()  { return _gstate.strokeWidth; }

/**
 * Sets the stroke width.
 */
public void setStrokeWidth(double aWidth)  { _gstate.strokeWidth = aWidth; }

/**
 * Returns the line cap.
 */
public int getLineCap()  { return _gstate.lineCap; }

/**
 * Sets the line cap.
 */
public void setLineCap(int aLineCap)  { _gstate.lineCap = aLineCap; }

/**
 * Returns the line join.
 */
public int getLineJoin()  { return _gstate.lineJoin; }

/**
 * Sets the line join.
 */
public void setLineJoin(int aLineJoin)  { _gstate.lineJoin = aLineJoin; }

/**
 * Returns the fill opacity.
 */
public double getFillOpacity()  { return _gstate.fillOpacity; }

/**
 * Sets the fill opacity.
 */
public void setFillOpacity(double anOpacity)  { _gstate.fillOpacity = anOpacity; }

/**
 * Returns the stroke opacity.
 */
public double getStrokeOpacity()  { return _gstate.strokeOpacity; }

/**
 * Sets the stroke opacity.
 */
public void setStrokeOpacity(double anOpacity)  { _gstate.strokeOpacity = anOpacity; }

/**
 * Returns the shape opacity.
 */
public double getShapeOpacity()  { return _gstate.shapeOpacity; }

/**
 * Sets the shape opacity.
 */
public void setShapeOpacity(double anOpacity)  { _gstate.shapeOpacity = anOpacity; }

/**
 * Returns the stack size.
 */
public int getStackSize()  { return _gstates.size(); }

/**
 * An inner class for GStates.
 */
public static class GState implements Cloneable {

    // The fill color
    Color     fillColor = null;
    
    // The stroke color
    Color     strokeColor = null;
    
    // The stroke width
    double      strokeWidth = -1;
    
    // The line cap
    int         lineCap = 0;
    
    // The line join
    int         lineJoin = 0;
    
    // The fill opacity
    double      fillOpacity = 1;
    
    // The stroke opacity
    double      strokeOpacity = 1;
    
    // The shape opacity
    double      shapeOpacity = 1;
    
    /**
     * Returns a clone of this gstate.
     */    
    public GState clone()
    {
        try { return (GState)super.clone(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }
}

}