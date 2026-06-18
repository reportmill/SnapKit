/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A Slider control.
 */
public class Slider extends View {
    
    // The slider value
    private double  _value = 50;

    // The slider min value
    private double  _min = 0;
    
    // The slider max value
    private double  _max = 100;
    
    // Constants for properties
    public static final String Min_Prop = "Min";
    public static final String Max_Prop = "Max";
    public static final String Value_Prop = "Value";
    
    // Constants for knob size
    private static final int SIZE = 16;
    private static final int HSIZE = 8;

    /**
     * Constructor.
     */
    public Slider()
    {
        super();
        enableEvents(MousePress, MouseDrag);
    }

    /**
     * Returns the value.
     */
    public double getValue()  { return _value; }

    /**
     * Sets the value.
     */
    public void setValue(double aValue)
    {
        if (aValue == _value) return;
        firePropChange(Value_Prop, _value, _value = aValue);
        repaint();
    }

    /**
     * Returns the value as a number from 0-1, based on where the value is in the range (min=0, max=1).
     */
    public double getValueRatio()
    {
        double val = getValue();
        double min = getMin();
        double max = getMax();
        double range = max - min;
        if (range == 0)
            return 0;
        return (val - min) / (max - min);
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
        if (aValue == _min) return;
        firePropChange(Min_Prop, _min, _min = aValue);
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
        if (aValue == _max) return;
        firePropChange(Max_Prop, _max, _max = aValue);
        repaint();
    }

    /**
     * Calculates the preferred width.
     */
    protected double computePrefWidth(double aH)
    {
        return isHorizontal() ? 100 : (SIZE + 4);
    }

    /**
     * Calculates the preferred height.
     */
    protected double computePrefHeight(double aW)
    {
        return isVertical() ? 100 : (SIZE + 4);
    }

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
        aPntr.setPaint(Color.LIGHTGRAY);
        aPntr.fill(trectRound);
    }

    /**
     * Override to wrap in Painter and forward.
     */
    protected void paintKnob(Painter aPntr)
    {
        Point kpnt = getKnobPoint();
        Arc circle = new Arc(kpnt.x - HSIZE, kpnt.y - HSIZE, SIZE, SIZE, 0, 360);
        aPntr.setColor(Color.WHITE);
        aPntr.fill(circle);
        aPntr.setPaint(Color.LIGHTGRAY);
        aPntr.draw(circle);
    }

    /**
     * Returns the track rect.
     */
    protected Rect getTrackBounds()
    {
        // Get area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Handle horizontal
        if (isHorizontal()) {
            double trackX = areaX + HSIZE + 1;
            double trackW = areaW - SIZE - 2;
            double midy = Math.round(areaY + areaH / 2);
            return new Rect(trackX, midy - 2, trackW, 4);
        }

        // Handle vertical
        double trackY = areaY + HSIZE + 1;
        double trackH = areaH - SIZE - 2;
        double midx = Math.round(areaX + areaW / 2);
        return new Rect(midx - 2, trackY, 4, trackH);
    }

    /**
     * Returns the knob point.
     */
    protected Point getKnobPoint()
    {
        // Get area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Get track bounds
        double trackX = areaX + HSIZE + 1;
        double trackY = areaY + HSIZE + 1;
        double trackW = areaW - SIZE - 2;
        double trackH = areaH - SIZE - 2;

        // Calc point x/y and return
        double midy = isHorizontal() ? Math.round(areaY + areaH / 2) : Math.round(trackY + trackH * getValueRatio());
        double midx = isHorizontal() ? Math.round(trackX + trackW * getValueRatio()) : Math.round(areaX + areaW / 2);
        return new Point(midx, midy);
    }

    /**
     * Handle Events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseDragged
        if (anEvent.isMouseDrag()) {

            // Get Mouse X/Y
            Rect tbnds = getTrackBounds();
            double mx = anEvent.getX();
            if (mx < tbnds.x)
                mx = tbnds.x;
            if (mx > tbnds.getMaxX())
                mx = tbnds.getMaxX();
            double my = anEvent.getY();
            if (my < tbnds.y)
                my = tbnds.y;
            if (my > tbnds.getMaxY())
                my = tbnds.getMaxY();

            // Calc value at point
            double tp = isHorizontal() ? (mx - tbnds.x) : (my - tbnds.y);
            double ts = isHorizontal() ? tbnds.width : tbnds.height;
            double value = getMin() + (tp / ts) * (getMax() - getMin());
            if (MathUtils.equals(value, getValue()))
                return;

            // Set value, fire Action event
            setValue(value);
            fireActionEvent(anEvent);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Min, Max, Value
        aPropSet.addPropNamed(Min_Prop, double.class, 0d);
        aPropSet.addPropNamed(Max_Prop, double.class, 100d);
        aPropSet.addPropNamed(Value_Prop, double.class, 50d);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // Min, Max, Value
            case Min_Prop -> getMin();
            case Max_Prop -> getMax();
            case Value_Prop -> getValue();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Min, Max, Value
            case Min_Prop -> setMin(Convert.doubleValue(aValue));
            case Max_Prop -> setMax(Convert.doubleValue(aValue));
            case Value_Prop -> setValue(Convert.doubleValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }
}