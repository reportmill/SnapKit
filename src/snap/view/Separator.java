/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * This class provides a RM shape/inspector for editing JSeparator.
 */
public class Separator extends View {
    
    // Color
    Color    _sideColor = new Color(1,1,1,.5);

/**
 * Override to wrap in Painter and forward.
 */
protected void paintFront(Painter aPntr)
{
    double w = getWidth(), h = getHeight();
    Insets ins = getInsetsAll(); double px = ins.left, py = ins.top, pw = w - px - ins.right, ph = h - py - ins.bottom;
    aPntr.clearRect(0,0,w,h); aPntr.setStroke(Stroke.Stroke1);

    if(isHorizontal()) {
        double ly = Math.floor(py+ph/2) + .5;
        aPntr.setPaint(_sideColor); aPntr.drawLine(px,ly-1,px+pw,ly-1); aPntr.drawLine(px,ly+1,px+pw,ly+1);
        aPntr.setPaint(Color.LIGHTGRAY); aPntr.drawLine(px,ly,px+pw,ly);
    }
    else {
        double lx = Math.floor(px+pw/2) + .5;
        aPntr.setPaint(_sideColor); aPntr.drawLine(lx-1,py,lx-1,py+ph); aPntr.drawLine(lx+1,py,lx+1,py+ph);
        aPntr.setPaint(Color.LIGHTGRAY); aPntr.drawLine(lx,py,lx,py+ph);
    }
}

/**
 * Override to return default preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return isVertical()? 3 : 0; }

/**
 * Override to return default preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return isHorizontal()? 3 : 0; }

}