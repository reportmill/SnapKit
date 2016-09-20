/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A control to show a text value with convenient up/down buttons.
 */
public class Spinner <T> extends ParentView {

    // The current value of spinner
    T                _value;
    
    // The step size of spinner
    double           _step = 1;
    
    // The min/max of spinner
    double           _min = Integer.MIN_VALUE, _max = Integer.MAX_VALUE;
    
    // The text field
    TextField        _text;
    
    // The up down buttons
    Button           _upBtn, _dnBtn;
    
    // The arrow images
    Image            _upImg, _dnImg;
  
    // Constants for properties
    public static final String Min_Prop = "Min";
    public static final String Max_Prop = "Max";
    public static final String Step_Prop = "StepSize";
    public static final String Value_Prop = "Value";

/**
 * Creates a new Spinner.
 */
public Spinner()
{
    enableEvents(Action);
    _text = new TextField();
    _text.addEventHandler(e -> textChanged(), Action);
    _upBtn = new Button(); _upBtn.setImage(getUpArrowImage());
    _upBtn.addEventHandler(e -> increment(), Action);
    _dnBtn = new Button(); _dnBtn.setImage(getDownArrowImage());
    _dnBtn.addEventHandler(e -> decrement(), Action);
    setChildren(_text, _upBtn, _dnBtn);
}

/**
 * Returns the spinner value.
 */
public T getValue()  { return _value; }

/**
 * Sets the spinner value.
 */
public void setValue(T aValue)
{
    if(SnapUtils.equals(aValue,getValue())) return;
    firePropChange(Value_Prop, _value, _value = aValue);
    _text.setText(_value!=null? _value.toString() : "");
}

/**
 * Returns the spinner min.
 */
public double getMin()  { return _min; }

/**
 * Sets the spinner min.
 */
public void setMin(double aValue)
{
    firePropChange(Min_Prop, _min, _min = aValue);
}

/**
 * Returns the spinner max.
 */
public double getMax()  { return _max; }

/**
 * Sets the spinner min.
 */
public void setMax(double aValue)
{
    firePropChange(Max_Prop, _max, _max = aValue);
}

/**
 * Returns the spinner step size.
 */
public double getStep()  { return _step; }

/**
 * Sets the spinner step size.
 */
public void setStep(double aValue)
{
    firePropChange(Step_Prop, _step, _step = aValue);
}

/**
 * Increments spinner.
 */
public void increment()
{
    Object value = getValue();
    if(value instanceof Integer) { int i1 = (Integer)value, i2 = (int)Math.round(getStep()); value = i1 + i2; }
    else if(value instanceof Float) { float i1 = (Float)value, i2 = (float)getStep(); value = i1 + i2; }
    else if(value instanceof Double) { double i1 = (Double)value, i2 = getStep(); value = i1 + i2; }
    else System.out.println("Spinner: Unsuported value/step type");
    
    // If new value, set and fire action
    if(SnapUtils.equals(value, getValue())) return;
    setValue((T)value);
    fireActionEvent();
}

/**
 * Increments spinner.
 */
public void decrement()
{
    Object value = getValue();
    if(value instanceof Integer) { int i1 = (Integer)value, i2 = (int)Math.round(getStep()); value = i1 - i2; }
    else if(value instanceof Float) { float i1 = (Float)value, i2 = (float)getStep(); value = i1 - i2; }
    else if(value instanceof Double) { double i1 = (Double)value, i2 = getStep(); value = i1 - i2; }
    else System.out.println("Spinner: Unsuported value/step type");
    
    // If new value, set and fire action
    if(SnapUtils.equals(value, getValue())) return;
    setValue((T)value);
    fireActionEvent();
}

/**
 * Called when text changes.
 */
public void textChanged()
{
    if(getValue() instanceof Integer) {
        Object value = SnapUtils.intValue(_text.getText());
        setValue((T)value);
        fireActionEvent();
    }
    else System.out.println("Spinner: Unsuported value text type");
}

/**
 * Returns an Icon of a down arrow.
 */
private Image getUpArrowImage()
{
    if(_upImg!=null) return _upImg;
    Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
    Polygon poly = new Polygon(1.5, 5.5, 7.5, 5.5, 4.5, 1.5);
    pntr.setColor(new Color("#FFFFFF99")); pntr.drawLine(4.5,8,2,2); pntr.drawLine(4.5,8,7,2);
    pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly); pntr.flush();
    return _upImg = img;
}

/**
 * Returns an Icon of a down arrow.
 */
private Image getDownArrowImage()
{
    if(_dnImg!=null) return _dnImg;
    Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
    Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
    pntr.setColor(new Color("#FFFFFF99")); pntr.drawLine(4.5,8,2,2); pntr.drawLine(4.5,8,7,2);
    pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly); pntr.flush();
    return _dnImg = img;
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _text.getPrefWidth()+16; }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _text.getPrefHeight(); }

/**
 * Layout children.
 */
protected void layoutChildren()
{
    double w = getWidth(), h = getHeight();
    Insets ins = getInsetsAll(); double px = ins.left, py = ins.top, pw = w - px - ins.right, ph = h - py - ins.bottom;
    double by = Math.round(py+ph/2);
    
    _text.setBounds(px, py, pw-18,ph);
    _upBtn.setBounds(px+pw-16,0,16,by-py);
    _dnBtn.setBounds(px+pw-16,by,16,py+ph-by);
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Min, Max, Step, Value
    if(getMin()!=Integer.MIN_VALUE) e.add(Min_Prop, getMin());
    if(getMax()!=Integer.MAX_VALUE) e.add(Max_Prop, getMax());
    if(getStep()!=1) e.add(Step_Prop, getStep());
    if(getValue()!=null) e.add(Value_Prop, getValue());
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Unarchive Min, Max, Step, Value
    if(anElement.hasAttribute(Min_Prop)) setMin(anElement.getAttributeFloatValue(Min_Prop));
    if(anElement.hasAttribute(Max_Prop)) setMax(anElement.getAttributeFloatValue(Max_Prop));
    if(anElement.hasAttribute(Step_Prop)) setStep(anElement.getAttributeFloatValue(Step_Prop));
    if(anElement.hasAttribute(Value_Prop)) setValue((T)(Integer)anElement.getAttributeIntValue(Value_Prop));
}

}