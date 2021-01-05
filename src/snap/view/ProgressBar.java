/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.RoundRect;
import snap.gfx.*;
import snap.util.*;

/**
 * A control to show progress.
 */
public class ProgressBar extends View {
    
    // The progress
    private double  _prog;
    
    // The animator
    private ViewAnim  _anim;
    
    // The ButtonArea used to paint background
    private ButtonArea  _btnArea;

    // Constants for properties
    public static final String Progress_Prop = "Progress";

    // ProgressBar fill normal
    private static Color _pbc0 = Color.get("#efefef");
    private static Color _pbc1 = Color.get("#fefefe");
    private static Color _pbc2 = Color.get("#f7f7f7");
    private static Color _pbc3 = Color.get("#e9e9e9");
    private static GradientPaint.Stop _pbfStops[] = GradientPaint.getStops(0, _pbc0, .33, _pbc1, .66, _pbc2, 1, _pbc3);
    public static Paint PROGRESS_BAR_FILL = new GradientPaint(.5, 0, .5, 1, _pbfStops);

    // ProgressBar fill for indeterminate
    private static Color _pb0 = Color.get("#008fbf");
    private static Color _pb1 = Color.get("#0096c9");
    private static Color _pb2 = Color.get("#0092c2");
    private static Color _pb3 = Color.get("#008ab7");
    private static GradientPaint.Stop _pbstops[] = GradientPaint.getStops(0, _pb0, .33, _pb1, .66, _pb2, 1, _pb3);
    private static GradientPaint.Stop _indetStops[] = GradientPaint.getStops(0, Color.WHITE, 1, _pb3);
    private static GradientPaint.Stop _indetStopsBack[] = GradientPaint.getStops(0, _pb3, 1, Color.WHITE);
    private static Paint INNER_FILL = new GradientPaint(.5, 0, .5, 1, _pbstops);
    private static Paint INDET_FILL = new GradientPaint(0, .5, 1, .5, _indetStops);
    private static Paint INDET_BACK_FILL = new GradientPaint(0, .5, 1, .5, _indetStopsBack);
    
    /**
     * Create ProgressBar.
     */
    public ProgressBar()
    {
        themeChanged();
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
        if (aValue>1) aValue = 1;
        if (aValue==_prog) return;
        firePropChange(Progress_Prop, _prog, _prog=aValue);

        // Reset animator
        setAnim(isAnimNeeded());
        repaint();
    }

    /**
     * Returns whether progress bar is indeterminate.
     */
    public boolean isIndeterminate()  { return _prog<0; }

    /**
     * Sets whether progress bar is indetermiante.
     */
    public void setIndeterminate(boolean aValue)
    {
        setProgress(aValue ? -1 : 0);
    }

    /**
     * Override to check animation.
     */
    public void setVisible(boolean aValue)  { super.setVisible(aValue); setAnim(isAnimNeeded()); }

    /**
     * Returns whether anim is needed.
     */
    private boolean isAnimNeeded()  { return _prog<0 && isVisible(); }

    /**
     * Returns whether ProgressBar is animating.
     */
    private boolean isAnim()  { return _anim!=null; }

    /**
     * Sets anim.
     */
    private void setAnim(boolean aValue)
    {
        // If already set, just return
        if (aValue==isAnim()) return;

        // If starting, create/configure/play anim
        if (aValue) {
            _anim = new ViewAnim(this);
            _anim.getAnim(Integer.MAX_VALUE).setOnFrame(() -> repaint()).play();
        }

        // Otherwise, stop and clear
        else { _anim.clear(); _anim = null; }
    }

    /**
     * Override to paint.
     */
    protected void paintFront(Painter aPntr)
    {
        // Paint ProgressBar background as button using ButtonArea
        double viewW = getWidth();
        double viewH = getHeight();
        _btnArea.setSize(viewW, viewH);
        _btnArea.paint(aPntr);

        // Paint normal bar
        if (_prog>=0) {
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
            int iw2 = areaW + aw*2;
            int imax2 = ix2 + iw2;
            int etime = _anim!=null ? _anim.getTime() : 0;
            int imax3 = ix2 + (aw + etime/10) % (iw2*2);
            int ix3 = imax3 - aw;
            boolean back = false;
            if (imax3>imax2) {
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
     * Override to set/reset ButtonArea.
     */
    protected void themeChanged()
    {
        super.themeChanged();
        _btnArea = (ButtonArea) ViewTheme.get().createArea(this);
        _btnArea.setFill(PROGRESS_BAR_FILL);
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Do normal archival, Archive Indeterimate and return
        XMLElement e = super.toXML(anArchiver);
        if (isIndeterminate()) e.add("indeterminate", true);
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Do normal unarchival, Unarchive Indeterminate and return
        super.fromXML(anArchiver, anElement);
        if (anElement.getAttributeBoolValue("indeterminate", false))
            setIndeterminate(true);
        return this;
    }
}