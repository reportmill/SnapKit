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
    private double  _prog;

    // The animator
    private ViewAnim  _anim;

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
        _borderRadius = 4;
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
        if (aValue > 1) aValue = 1;
        if (aValue == _prog) return;
        firePropChange(Progress_Prop, _prog, _prog = aValue);

        // Reset animator
        setAnimating(isAnimNeeded());
        repaint();
    }

    /**
     * Returns whether progress bar is indeterminate.
     */
    public boolean isIndeterminate()  { return _prog < 0; }

    /**
     * Sets whether progress bar is indeterminate.
     */
    public void setIndeterminate(boolean aValue)
    {
        setProgress(aValue ? -1 : 0);
    }

    /**
     * Returns whether anim is needed.
     */
    private boolean isAnimNeeded()
    {
        return _prog < 0 && isShowing();
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
        // If already set, just return
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
     * Override to paint.
     */
    protected void paintFront(Painter aPntr)
    {
        // Paint ProgressBar background as button using ButtonArea
        double viewW = getWidth();
        double viewH = getHeight();

        // Paint normal bar
        if (_prog >= 0) {
            double areaX = 3;
            double areaY = 3;
            double areaW = Math.round(areaX + _prog * (viewW - 6));
            double areaH = viewH - 6;
            RoundRect areaRect = new RoundRect(areaX, areaY, areaW, areaH, 3);
            aPntr.fillWithPaint(areaRect, INNER_FILL);
        }

        // Paint indeterminate bar
        else {

            // Get bounds of indeterminate bar
            int areaX = 3;
            int areaY = 3;
            int areaW = (int) viewW - 6;
            int areaH = (int) viewH - 6;
            int aw = 50;
            int ix2 = areaX - aw;
            int iw2 = areaW + aw * 2;
            int imax2 = ix2 + iw2;
            int etime = _anim != null ? _anim.getTime() : 0;
            int imax3 = ix2 + (aw + etime / 10) % (iw2 * 2);
            int ix3 = imax3 - aw;
            boolean back = false;
            if (imax3 > imax2) {
                ix3 = imax2 - (imax3 - imax2);
                back = true;
            }

            // Create rect for anim and paint
            RoundRect rrect = new RoundRect(ix3, areaY, aw, areaH, 3);
            aPntr.save();
            aPntr.clip(new RoundRect(areaX, areaY, areaW, areaH, 3));
            aPntr.fillWithPaint(rrect, back ? INDET_BACK_FILL : INDET_FILL);
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
        // Do normal version
        if (aValue == isShowing()) return;
        super.setShowing(aValue);

        // If indeterminate, update Animating
        if (isIndeterminate())
            setAnimating(isAnimNeeded());
    }

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
    }

    /**
     * Returns the value for given prop name.
     */
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Progress, Indeterminate
            case Progress_Prop: return getProgress();
            case Indeterminate_Prop: return isIndeterminate();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the value for given prop name.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Progress, Indeterminate
            case Progress_Prop: setProgress(Convert.doubleValue(aValue)); break;
            case Indeterminate_Prop: setIndeterminate(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }
}