/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A control to show progress.
 */
public class ProgressBar extends View {
    
    // The progress
    double       _prog;
    
    // The animator
    ViewAnim     _anim;

    // Constants for properties
    public static final String Progress_Prop = "Progress";

    // ProgressBar fill
    static Color _pb0 = Color.get("#008fbf"), _pb1 = Color.get("#0096c9");
    static Color _pb2 = Color.get("#0092c2"), _pb3 = Color.get("#008ab7");
    static GradientPaint.Stop _pbstops[] = GradientPaint.getStops(0,_pb0,.33,_pb1,.66,_pb2,1,_pb3);
    static GradientPaint.Stop _indetStops[] = GradientPaint.getStops(0,Color.WHITE,1,_pb3);
    static GradientPaint.Stop _indetStopsBack[] = GradientPaint.getStops(0,_pb3,1,Color.WHITE);
    static Paint INNER_FILL = new GradientPaint(.5, 0, .5, 1, _pbstops);
    static Paint INDET_FILL = new GradientPaint(0,.5,1,.5, _indetStops);
    static Paint INDET_BACK_FILL = new GradientPaint(0,.5,1,.5, _indetStopsBack);
    
/**
 * Returns the value of progress bar.
 */
public double getProgress()  { return _prog; }

/**
 * Sets the value of the progress bar.
 */
public void setProgress(double aValue)
{
    if(aValue>1) aValue = 1;
    if(aValue==_prog) return;
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
public void setIndeterminate(boolean aValue)  { setProgress(aValue? -1 : 0); }

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
    if(aValue==isAnim()) return;
    if(aValue)
        getAnim(Integer.MAX_VALUE).setOnFrame(() -> repaint()).play();
    else getAnim(0).clear();
}

/**
 * Override to paint.
 */
protected void paintFront(Painter aPntr)
{
    ButtonPainter bp = new ButtonPainter(); bp.setWidth(getWidth()); bp.setHeight(getHeight());
    bp.setFill(ButtonPainter.FILL_PROGRESS_BAR);
    bp.paint(aPntr);
    
    // Paint normal bar
    if(_prog>=0) {
        double ix = 3, iy = 3, iw = getWidth() - 6, ih = getHeight() - 6; iw = Math.round(ix+_prog*iw);
        RoundRect rrect = new RoundRect(ix,iy,iw,ih,3);
        aPntr.setPaint(INNER_FILL); aPntr.fill(rrect);
    }
    
    // Paint indeterminate bar
    else {
        
        // Get bounds of indeterminate bar
        int ix = 3, iy = 3, iw = (int)getWidth() - 6, ih = (int)getHeight() - 6;
        int aw = 50, ix2 = ix - aw, iw2 = iw + aw*2, imax2 = ix2 + iw2;
        int etime = getAnim(0).getTime(), imax3 = ix2 + (aw + etime/10)%(iw2*2), ix3 = imax3 - aw;
        boolean back = false; if(imax3>imax2) { ix3 = imax2 - (imax3 - imax2); back = true; }
        
        // Create rect for anim and paint
        RoundRect rrect = new RoundRect(ix3,iy,aw,ih,3);
        aPntr.save(); aPntr.clip(new RoundRect(ix,iy,iw,ih,3));
        aPntr.setPaint(back? INDET_BACK_FILL : INDET_FILL); aPntr.fill(rrect);
        aPntr.restore();
    }
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = super.toXML(anArchiver);                        // Do normal archival
    if(isIndeterminate()) e.add("indeterminate", true);            // Archive isIndeterminate
    return e;                                                      // Return element
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXML(anArchiver, anElement);                // Unarchive basic attributes
    if(anElement.getAttributeBoolValue("indeterminate", false)) setIndeterminate(true); // Unarchive is indeterminate
    return this;
}

}