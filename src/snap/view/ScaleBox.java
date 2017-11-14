/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Insets;
import snap.gfx.Size;

/**
 * A Box subclass that scales it's content instead of resize.
 * FillWidth, FillHeight, KeepAspect, FillAlways default to true.
 */
public class ScaleBox extends BoxView {

/**
 * Creates a new ScaleBox.
 */
public ScaleBox()  { }

/**
 * Creates a new ScaleBox for content.
 */
public ScaleBox(View aContent)  { this(); setContent(aContent); }

/**
 * Creates a new ScaleBox for content with FillWidth, FillHeight params.
 */
public ScaleBox(View aContent, boolean isFillWidth, boolean isFillHeight)
{
    this(aContent); setFillWidth(isFillWidth); setFillHeight(isFillHeight);
}

/**
 * Returns the aspect of the content.
 */
protected double getAspect()
{
    View c = getContent();
    Size bs = c.getBestSize();
    return c.isHorizontal()? bs.width/bs.height : bs.height/bs.width;
}
    
/**
 * Returns preferred width of layout.
 */
public double getPrefWidthImpl(double aH)
{
    // If scaling and value provided, return value by aspect
    if(aH>=0 && (isFillHeight() || aH<getPrefHeight(-1))) return aH*getAspect();
    return super.getPrefWidthImpl(aH);
}

/**
 * Returns preferred height of layout.
 */
public double getPrefHeightImpl(double aW)
{
    // If scaling and value provided, return value by aspect
    if(aW>=0 && (isFillWidth() || aW<getPrefWidth(-1))) return aW/getAspect();
    return super.getPrefHeightImpl(aW);
}

/**
 * Performs layout.
 */
public void layoutImpl()  { layout(this, getContent(), null, isFillWidth(), isFillHeight()); }
    
/**
 * Performs Box layout for given parent, child and fill width/height.
 */
public static void layout(ParentView aPar, View aChild, Insets theIns, boolean isFillWidth, boolean isFillHeight)
{
    // If no child, just return
    if(aChild==null) return;
    
    // Get parent bounds for insets (just return if empty)
    Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
    double px = ins.left, py = ins.top;
    double pw = aPar.getWidth() - px - ins.right; if(pw<0) pw = 0; if(pw<=0) return;
    double ph = aPar.getHeight() - py - ins.bottom; if(ph<0) ph = 0; if(ph<=0) return;
    
    // Get content width/height
    double cw = aChild.getBestWidth(-1);
    double ch = aChild.getBestHeight(cw);
    
    // Handle ScaleToFit: Set content bounds centered, calculate scale and set
    if(isFillWidth || isFillHeight || cw>pw || ch>ph)  {
        double cx = px + (pw-cw)/2, cy = py + (ph-ch)/2;
        aChild.setBounds(cx, cy, cw, ch);
        double sx = isFillWidth || cw>pw? pw/cw : 1;
        double sy = isFillHeight || ch>ph? ph/ch : 1;
        if(isFillWidth && isFillHeight) sx = sy = Math.min(sx,sy); // KeepAspect?
        aChild.setScaleX(sx); aChild.setScaleY(sy);
        System.out.println("Scale: " + sx);
        return;
    }
    
    // Handle normal layout
    if(cw>pw) cw = pw; if(ch>ph) ch = ph;
    double dx = pw - cw, dy = ph - ch;
    double sx = aChild.getLeanX()!=null? ViewUtils.getLeanX(aChild) : ViewUtils.getAlignX(aPar);
    double sy = aChild.getLeanY()!=null? ViewUtils.getLeanY(aChild) : ViewUtils.getAlignY(aPar);
    aChild.setBounds(px+dx*sx, py+dy*sy, cw, ch);
}
    
}