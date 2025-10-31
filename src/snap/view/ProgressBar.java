/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.RoundRect;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A control to show progress.
 */
public class ProgressBar extends View {

    // The progress
    private double _prog;

    // Whether progress is indeterminate
    private boolean _indeterminate;

    // The animator
    private ViewAnim _anim;

    // Constants for properties
    public static final String Progress_Prop = "Progress";
    public static final String Indeterminate_Prop = "Indeterminate";

    // ProgressBar fill normal
    private static Color _pbc0 = Color.get("#efefef");
    private static Color _pbc1 = Color.get("#fefefe");
    private static Color _pbc2 = Color.get("#f7f7f7");
    private static Color _pbc3 = Color.get("#e9e9e9");
    private static GradientPaint.Stop[] _pbfStops = GradientPaint.getStops(0, _pbc0, .33, _pbc1, .66, _pbc2, 1, _pbc3);
    public static Paint PROGRESS_BAR_FILL = new GradientPaint(.5, 0, .5, 1, _pbfStops);

    // ProgressBar fill for indeterminate
    private static Color _pb0 = Color.get("#008fbf");
    private static Color _pb1 = Color.get("#0096c9");
    private static Color _pb2 = Color.get("#0092c2");
    private static Color _pb3 = Color.get("#008ab7");
    private static GradientPaint.Stop[] _pbstops = GradientPaint.getStops(0, _pb0, .33, _pb1, .66, _pb2, 1, _pb3);
    private static GradientPaint.Stop[] _indetStops = GradientPaint.getStops(0, Color.WHITE, 1, _pb3);
    private static GradientPaint.Stop[] _indetStopsBack = GradientPaint.getStops(0, _pb3, 1, Color.WHITE);
    private static Paint INNER_FILL = new GradientPaint(.5, 0, .5, 1, _pbstops);
    private static Paint INDET_FILL = new GradientPaint(0, .5, 1, .5, _indetStops);
    private static Paint INDET_BACK_FILL = new GradientPaint(0, .5, 1, .5, _indetStopsBack);

    /**
     * Constructor.
     */
    public ProgressBar()
    {
        super();
    }

    /**
     * Returns the value of progress bar.
     */
    public double getProgress()  { return _prog; }

    /**
     * Sets the value of the progress bar.
     */
    public void setProgress(double aValue)
    {
        aValue = MathUtils.clamp(aValue, 0, 1);
        if (aValue == _prog) return;
        firePropChange(Progress_Prop, _prog, _prog = aValue);

        // Reset animator
        updateAnimating();
        repaint();
    }

    /**
     * Returns whether progress bar is indeterminate.
     */
    public boolean isIndeterminate()  { return _indeterminate; }

    /**
     * Sets whether progress bar is indeterminate.
     */
    public void setIndeterminate(boolean aValue)
    {
        if (aValue == _indeterminate) return;
        firePropChange(Indeterminate_Prop, _indeterminate, _indeterminate = aValue);
        updateAnimating();
    }

    /**
     * Returns whether ProgressBar is animating.
     */
    private boolean isAnimating()  { return _anim != null; }

    /**
     * Sets anim.
     */
    private void setAnimating(boolean aValue)
    {
        if (aValue == isAnimating()) return;

        // If starting, create/configure/play anim
        if (aValue) {
            _anim = new ViewAnim(this);
            _anim.getAnim(Integer.MAX_VALUE).setOnFrame(() -> repaint()).play();
        }

        // Otherwise, stop and clear
        else {
            _anim.clear();
            _anim = null;
        }
    }

    /**
     * Updates animating.
     */
    private void updateAnimating()  { setAnimating(isIndeterminate() && isShowing()); }

    /**
     * Override to paint.
     */
    protected void paintFront(Painter aPntr)
    {
        // Paint ProgressBar background as button using ButtonArea
        int areaX = 3;
        int areaY = 3;
        int areaW = (int) getWidth() - 6;
        int areaH = (int) getHeight() - 6;

        // Paint normal bar
        if (!isIndeterminate()) {
            double pbarW = Math.round(_prog * areaW);
            RoundRect areaRect = new RoundRect(areaX, areaY, pbarW, areaH, 3);
            aPntr.fillWithPaint(areaRect, INNER_FILL);
        }

        // Paint indeterminate bar
        else {

            // Get bounds of indeterminate bar
            int pbarW = 50;
            int pbarAreaX = areaX - pbarW;
            int pbarAreaW = areaW + pbarW * 2;
            int pbarAreaMaxW = areaX + areaW + pbarW;
            int elapsedTime = _anim != null ? _anim.getTime() : 0;
            int pbarMaxX = pbarAreaX + (pbarW + elapsedTime / 10) % (pbarAreaW * 2);
            int pbarX = pbarMaxX - pbarW;
            boolean animatingBack = pbarMaxX > pbarAreaMaxW;
            if (animatingBack)
                pbarX = pbarAreaMaxW - (pbarMaxX - pbarAreaMaxW);

            // Create rect for anim and paint
            RoundRect pbarBounds = new RoundRect(pbarX, areaY, pbarW, areaH, 3);
            aPntr.save();
            aPntr.clip(new RoundRect(areaX, areaY, areaW, areaH, 3));
            aPntr.fillWithPaint(pbarBounds, animatingBack ? INDET_BACK_FILL : INDET_FILL);
            aPntr.restore();
        }
    }

    /**
     * Override to paint.
     */
    @Override
    protected void paintBack(Painter aPntr)
    {
        // Get RoundRect shape for bounds, Radius and Position
        double pbarX = 0;
        double pbarY = 0;
        double pbarW = getWidth();
        double pbarH = getHeight();
        RoundRect rect = new RoundRect(pbarX, pbarY, pbarW, pbarH, getBorderRadius());

        // Standard theme
        if (ViewTheme.get() == ViewTheme.getClassic()) {

            // Fill rect
            aPntr.fillWithPaint(rect, PROGRESS_BAR_FILL);

            // Paint bottom highlite ring (white)
            rect.setRect(pbarX + .5, pbarY + .5, pbarW - 1, pbarH);
            aPntr.drawWithPaint(rect, ButtonPainter.Classic.BOTTOM_HIGHLITE_PAINT);

            // Paint inner ring (light gray gradient)
            rect.setRect(pbarX + 1.5, pbarY + 1.5, pbarW - 3, pbarH - 4);
            aPntr.drawWithPaint(rect, ButtonPainter.Classic.INNER_RING_PAINT);

            // Paint outer ring (gray)
            rect.setRect(pbarX + .5, pbarY + .5, pbarW - 1, pbarH - 1);
            aPntr.drawWithPaint(rect, ButtonPainter.Classic.OUTER_RING_PAINT);
        }

        // Other themes
        else {

            // Get shape and paint fill
            Color fillColor = ViewTheme.get().getButtonColor();
            aPntr.fillWithPaint(rect, fillColor);

            // Draw outer ring
            Color strokeColor = ViewTheme.get().getButtonBorderColor();
            aPntr.drawWithPaint(rect, strokeColor);
        }
    }

    /**
     * Override to trigger animation.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        if (aValue == isShowing()) return;
        super.setShowing(aValue);
        updateAnimating();
    }

    /**
     * Returns a mapped property name.
     */
    public String getValuePropName()  { return Progress_Prop; }

    /**
     * Initialize Props. Override to provide custom defaults.
     */
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Add properties for this subclass
        aPropSet.addPropNamed(Progress_Prop, double.class, 0);
        aPropSet.addPropNamed(Indeterminate_Prop, boolean.class, false);

        // Override defaults
        aPropSet.getPropForName(PrefWidth_Prop).setDefaultValue(120d);
        aPropSet.getPropForName(PrefHeight_Prop).setDefaultValue(16d);
    }

    /**
     * Returns the value for given prop name.
     */
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // Progress, Indeterminate
            case Progress_Prop -> getProgress();
            case Indeterminate_Prop -> isIndeterminate();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Sets the value for given prop name.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Progress, Indeterminate
            case Progress_Prop, "Value" -> setProgress(Convert.doubleValue(aValue));
            case Indeterminate_Prop -> setIndeterminate(Convert.boolValue(aValue));

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXML(anArchiver);
        if (!isPropDefault(Indeterminate_Prop))
            e.add(Indeterminate_Prop, isIndeterminate());
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXML(anArchiver, anElement);
        if (anElement.hasAttribute(Indeterminate_Prop))
            setIndeterminate(anElement.getAttributeBoolValue(Indeterminate_Prop));
        return this;
    }
}