/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.text.DecimalFormat;
import java.util.Objects;

import snap.geom.Pos;
import snap.props.PropSet;
import snap.util.*;

/**
 * A control to show a text value with convenient up/down buttons.
 */
public class Spinner <T> extends ParentView {

    // The current value of spinner
    private T  _value;
    
    // The step size of spinner
    private double  _step = 1;
    
    // The min of spinner
    private double  _min = Integer.MIN_VALUE;

    // The max of spinner
    private double _max = Integer.MAX_VALUE;
    
    // The text field
    private TextField  _text;
    
    // The up down buttons
    private ArrowView  _arrowView;
    
    // Constants for properties
    public static final String Min_Prop = "Min";
    public static final String Max_Prop = "Max";
    public static final String Step_Prop = "Step";
    public static final String Value_Prop = "Value";
    
    // Constant
    private static DecimalFormat _fmt = new DecimalFormat("#.##");

    /**
     * Constructor.
     */
    public Spinner()
    {
        // Enable Action event
        setActionable(true);

        // Create/configure Text
        _text = new TextField();
        _text.setGrowWidth(true);
        _text.addEventHandler(e -> textChanged(), Action);
        addChild(_text);

        // Create/configure arrow view
        _arrowView = new ArrowView();
        _arrowView.addEventHandler(e -> handleAarrowViewActionEvent(), Action);
        addChild(_arrowView);
    }

    /**
     * Returns the text field.
     */
    public TextField getTextField()  { return _text; }

    /**
     * Returns the spinner value.
     */
    public T getValue()  { return _value; }

    /**
     * Sets the spinner value.
     */
    public void setValue(T aValue)
    {
        if (Objects.equals(aValue,getValue())) return;
        firePropChange(Value_Prop, _value, _value = aValue);
        _text.setText(getValueAsString());
    }

    /**
     * Returns the text for value.
     */
    public String getValueAsString()
    {
        if (_value == null) return "";
        if (_value instanceof Double) return _fmt.format(_value);
        if (_value instanceof Float) return _fmt.format(_value);
        return _value.toString();
    }

    /**
     * Returns the value type.
     */
    public Class<?> getValueClass()
    {
        Object val = getValue();
        if (val instanceof Integer) return int.class;
        else if (val instanceof Long) return long.class;
        else if (val instanceof Byte) return byte.class;
        else if (val instanceof Short) return short.class;
        else if (val instanceof Float) return float.class;
        else if (val instanceof Double) return double.class;
        return val!=null? val.getClass() : null;
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
        if (aValue == _max) return;
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
        if (aValue == _max) return;
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
        if (aValue == _step) return;
        firePropChange(Step_Prop, _step, _step = aValue);
    }

    /**
     * Called when ArrowView fires action.
     */
    protected void handleAarrowViewActionEvent()
    {
        if (_arrowView.isUp()) increment();
        else if (_arrowView.isDown()) decrement();
    }

    /**
     * Increments spinner.
     */
    public void increment()
    {
        Object val = getValue();
        if (val instanceof Integer) { int i1 = (Integer) val, i2 = (int) Math.round(getStep()); val = i1 + i2; }
        else if (val instanceof Long) { long i1 = (long) val, i2 = (long) getStep(); val = i1 + i2; }
        else if (val instanceof Byte) { byte i1 = (byte) val, i2 = (byte) getStep(); val = i1 + i2; }
        else if (val instanceof Short) { short i1 = (short) val, i2 = (short) getStep(); val = i1 + i2; }
        else if (val instanceof Float) { float i1 = (Float) val, i2 = (float) getStep(); val = i1 + i2; }
        else if (val instanceof Double) { double i1 = (Double) val, i2 = getStep(); val = i1 + i2; }
        else { System.err.println("Spinner: Unsupported value/step type: " + getValueClass()); return; }

        // If new value, set and fire action
        if (Objects.equals(val, getValue())) return;
        setValue((T) val);
        fireActionEvent(null);
    }

    /**
     * Increments spinner.
     */
    public void decrement()
    {
        Object val = getValue();
        if (val instanceof Integer) { int i1 = (Integer) val, i2 = (int) Math.round(getStep()); val = i1 - i2; }
        else if (val instanceof Long) { long i1 = (long) val, i2 = (long) getStep(); val = i1 - i2; }
        else if (val instanceof Byte) { byte i1 = (byte) val, i2 = (byte) getStep(); val = i1 - i2; }
        else if (val instanceof Short) { short i1 = (short) val, i2 = (short) getStep(); val = i1 - i2; }
        else if (val instanceof Float) { float i1 = (Float) val, i2 = (float) getStep(); val = i1 - i2; }
        else if (val instanceof Double) { double i1 = (Double) val, i2 = getStep(); val = i1 - i2; }
        else { System.err.println("Spinner: Unsupported value/step type: " + getValueClass()); return; }

        // If new value, set and fire action
        if (Objects.equals(val, getValue())) return;
        setValue((T) val);
        fireActionEvent(null);
    }

    /**
     * Called when text changes.
     */
    public void textChanged()
    {
        // Get Text value based as same current type of spinner
        Object oval = getValue(), nval = null;
        String str = _text.getText();
        if (oval instanceof Integer) nval = Convert.intValue(str);
        else if (oval instanceof Long) nval = Convert.longValue(str);
        else if (oval instanceof Byte) nval = (byte) Convert.intValue(str);
        else if (oval instanceof Short) nval = (short) Convert.intValue(str);
        else if (oval instanceof Float) nval = Convert.floatValue(str);
        else if (oval instanceof Double) nval = Convert.doubleValue(str);
        else { System.err.println("Spinner: Unsupported value type: " + getValueClass()); return; }

        setValue((T)nval);
        fireActionEvent(null);
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return RowView.getPrefWidth(this, aH);
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return RowView.getPrefHeight(this, aW);
    }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        RowView.layout(this, true);
    }

    /**
     * Override to forward to StringView.
     */
    public void setAlign(Pos aPos)
    {
        super.setAlign(aPos);
        _text.setAlignX(getAlignX());
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Min, Max, Step
        aPropSet.addPropNamed(Min_Prop, double.class, Integer.MAX_VALUE);
        aPropSet.addPropNamed(Max_Prop, double.class, Integer.MIN_VALUE);
        aPropSet.addPropNamed(Step_Prop, double.class, 1);
        aPropSet.addPropNamed(Value_Prop, double.class, 0);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Min, Max, Step, Value
            case Min_Prop: return getMin();
            case Max_Prop: return getMax();
            case Step_Prop: return getStep();
            case Value_Prop: return getValue();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Min, Max, Step
            case Min_Prop: setMin(Convert.doubleValue(aValue)); break;
            case Max_Prop: setMax(Convert.doubleValue(aValue)); break;
            case Step_Prop: setStep(Convert.doubleValue(aValue)); break;
            case Value_Prop: setValue((T) (Double) Convert.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive Min, Max, Step, Value
        if (getMin()!=Integer.MIN_VALUE) e.add(Min_Prop, getMin());
        if (getMax()!=Integer.MAX_VALUE) e.add(Max_Prop, getMax());
        if (getStep()!=1) e.add(Step_Prop, getStep());
        if (getValue() != null && !getValue().equals(0d)) e.add(Value_Prop, getValue());

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
        if (anElement.hasAttribute(Min_Prop))
            setMin(anElement.getAttributeFloatValue(Min_Prop));
        if (anElement.hasAttribute(Max_Prop))
            setMax(anElement.getAttributeFloatValue(Max_Prop));
        if (anElement.hasAttribute(Step_Prop))
            setStep(anElement.getAttributeFloatValue(Step_Prop));
        if (anElement.hasAttribute(Value_Prop))
            setValue((T) (Double) anElement.getAttributeDoubleValue(Value_Prop));
    }
}