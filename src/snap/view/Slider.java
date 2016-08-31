package snap.view;
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
    public static final String Value_Prop = "Value";
    public static final String Minimum_Prop = "Minimum";
    public static final String Maximum_Prop = "Maximum";
    
    // Constants for knob size
    static final int SIZE = 16, HSIZE = 8;

/**
 * Creates a new Slider.
 */
public Slider()  { enableEvents(Action, MouseDragged); }

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
 * Returns the minimum.
 */
public double getMin()  { return _min; }

/**
 * Sets the minimum.
 */
public void setMin(double aValue)
{
    if(aValue==_min) return;
    firePropChange(Minimum_Prop, _min, _min=aValue);
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
    firePropChange(Maximum_Prop, _max, _max=aValue);
    repaint();
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
    double midy = isHorizontal()? Math.round(py+ph/2) : Math.round(ty + th*getValue()/(getMax()-getMin()));
    double midx = isHorizontal()? Math.round(tx + tw*getValue()/(getMax()-getMin())) : Math.round(px+pw/2);
    return new Point(midx, midy);
}

/**
 * Handle Events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseDragged
    if(anEvent.isMouseDragged()) {
        Rect tbnds = getTrackBounds();
        double mx = anEvent.getX(); if(mx<tbnds.getX()) mx = tbnds.getX(); if(mx>tbnds.getMaxX()) mx = tbnds.getMaxX();
        double my = anEvent.getY(); if(my<tbnds.getY()) my = tbnds.getY(); if(my>tbnds.getMaxY()) my = tbnds.getMaxY();
        double tp = isHorizontal()? (mx - tbnds.getX()) : (my - tbnds.getY());
        double ts = isHorizontal()? tbnds.getWidth() : tbnds.getHeight();
        double value = getMin() + (tp/ts)*(getMax() - getMin()); if(MathUtils.equals(value,getValue())) return;
        setValue(value);
        fireActionEvent();
    }
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXML(anArchiver);
    
    // Archive Minimum, Maximum and current value
    if(getMin()!=0) e.add("min", getMin());
    if(getMax()!=100) e.add("max", getMax());
    if(getValue()!=50) e.add("value", getValue());

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXML(anArchiver, anElement);

    // Unarchive Minimum, Maximum and current value
    setMin(anElement.getAttributeIntValue("min", 0));
    setMax(anElement.getAttributeIntValue("max", 100));
    setValue(anElement.getAttributeIntValue("value", 50));
    return this;
}

}