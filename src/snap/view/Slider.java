/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A Slider control.
 */
public class Slider extends View {
    
    // The slider value
    double           _value;

    // The slider min value
    double           _min = 0;
    
    // The slider max value
    double           _max = 1;
    
    // Constants for properties
    public static final String Min_Prop = "Min";
    public static final String Max_Prop = "Max";
    public static final String Value_Prop = "Value";
    
    // Constants for knob size
    static final int SIZE = 16, HSIZE = 8;

/**
 * Creates a new Slider.
 */
public Slider()  { enableEvents(Action, MouseDrag); }

/**
 * Returns the value.
 */
public double getValue()  { return _value; }

/**
 * Sets the value.
 */
public void setValue(double aValue)
{
    if(aValue==_value) return;
    firePropChange(Value_Prop, _value, _value=aValue);
    repaint();
}

/**
 * Returns the value as a number from 0-1, based on where the value is in the range (min=0, max=1).
 */
public double getValueRatio()  { return (getValue() - getMin())/(getMax() - getMin()); }

/**
 * Returns the minimum.
 */
public double getMin()  { return _min; }

/**
 * Sets the minimum.
 */
public void setMin(double aValue)
{
    if(aValue==_min) return;
    firePropChange(Min_Prop, _min, _min=aValue);
    repaint();
}

/**
 * Returns the maximum.
 */
public double getMax()  { return _max; }

/**
 * Sets the maximum.
 */
public void setMax(double aValue)
{
    if(aValue==_max) return;
    firePropChange(Max_Prop, _max, _max=aValue);
    repaint();
}

/**
 * Returns the value for given key.
 */
public Object getValue(String aPropName)
{
    if(aPropName.equals("Value"))
        return getValue();
    return super.getValue(aPropName);
}

/**
 * Sets the value for given key.
 */
public void setValue(String aPropName, Object aValue)
{
    if(aPropName.equals("Value"))
        setValue(SnapUtils.doubleValue(aValue));
    else super.setValue(aPropName, aValue);
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return isHorizontal()? 100 : (SIZE+4); }

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return isVertical()? 100 : (SIZE+4); }

/**
 * Override to wrap in Painter and forward.
 */
protected void paintFront(Painter aPntr)
{
    paintTrack(aPntr);
    paintKnob(aPntr);
}

/**
 * Override to wrap in Painter and forward.
 */
protected void paintTrack(Painter aPntr)
{
    Rect trect = getTrackBounds();
    RoundRect trectRound = new RoundRect(trect.x, trect.y, trect.width, trect.height, 2);
    aPntr.setPaint(Color.LIGHTGRAY); aPntr.fill(trectRound);
}

/**
 * Override to wrap in Painter and forward.
 */
protected void paintKnob(Painter aPntr)
{
    Point kpnt = getKnobPoint();
    Arc circle = new Arc(kpnt.x-HSIZE, kpnt.y-HSIZE, SIZE, SIZE, 0, 360);
    aPntr.setColor(Color.WHITE); aPntr.fill(circle);
    aPntr.setPaint(Color.LIGHTGRAY); aPntr.draw(circle);
}

/**
 * Returns the track rect.
 */
protected Rect getTrackBounds()
{
    double w = getWidth(), h = getHeight(); Insets ins = getInsetsAll();
    double px = ins.left, py = ins.top, pw = w - px - ins.right, ph = h - py - ins.bottom;
    
    // Handle horizontal
    if(isHorizontal()) {
        double tx = px + HSIZE + 1, tw = pw - SIZE - 2, midy = Math.round(py+ph/2);
        return new Rect(tx, midy-2, tw, 4);
    }
    
    // Handle vertical
    double ty = py + HSIZE + 1, th = ph - SIZE - 2;
    double midx = Math.round(px+pw/2);
    return new Rect(midx-2, ty, 4, th);
}

/**
 * Returns the knob point.
 */
protected Point getKnobPoint()
{
    double w = getWidth(), h = getHeight(); Insets ins = getInsetsAll();
    double px = ins.left, py = ins.top, pw = w - px - ins.right, ph = h - py - ins.bottom;
    double tx = px + HSIZE + 1, tw = pw - SIZE - 2;
    double ty = py + HSIZE + 1, th = ph - SIZE - 2;
    double midy = isHorizontal()? Math.round(py+ph/2) : Math.round(ty + th*getValueRatio());
    double midx = isHorizontal()? Math.round(tx + tw*getValueRatio()) : Math.round(px+pw/2);
    return new Point(midx, midy);
}

/**
 * Handle Events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseDragged
    if(anEvent.isMouseDrag()) {
        Rect tbnds = getTrackBounds();
        double mx = anEvent.getX(); if(mx<tbnds.getX()) mx = tbnds.getX(); if(mx>tbnds.getMaxX()) mx = tbnds.getMaxX();
        double my = anEvent.getY(); if(my<tbnds.getY()) my = tbnds.getY(); if(my>tbnds.getMaxY()) my = tbnds.getMaxY();
        double tp = isHorizontal()? (mx - tbnds.getX()) : (my - tbnds.getY());
        double ts = isHorizontal()? tbnds.getWidth() : tbnds.getHeight();
        double value = getMin() + (tp/ts)*(getMax() - getMin()); if(MathUtils.equals(value,getValue())) return;
        setValue(value);
        fireActionEvent(anEvent);
    }
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXML(anArchiver);
    
    // Archive Min, Max, Value
    if(getMin()!=0) e.add(Min_Prop, getMin());
    if(getMax()!=100) e.add(Max_Prop, getMax());
    if(getValue()!=50) e.add(Value_Prop, getValue());
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXML(anArchiver, anElement);

    // Unarchive Min, Max, Value
    setMin(anElement.getAttributeIntValue(Min_Prop, 0));
    setMax(anElement.getAttributeIntValue(Max_Prop, 100));
    setValue(anElement.getAttributeIntValue(Value_Prop, 50));
    return this;
}

}