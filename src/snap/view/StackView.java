/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;

/**
 * A ChildView subclass to show overlapping children.
 */
public class StackView extends ChildView {

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return ColView.getPrefWidth(this, null, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, null, aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { layout(this, null, null, false, false); }

/**
 * Performs layout in content rect.
 */
public static void layout(ParentView aPar, View theChildren[], Insets theIns, boolean isFillWidth, boolean isFillHeight)
{
    // If no children, just return
    View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged(); if(children.length==0) return;
    
    // Get parent bounds for insets
    Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
    double px = ins.left, py = ins.top;
    double pw = aPar.getWidth() - px - ins.right; if(pw<0) pw = 0; if(pw<=0) return;
    double ph = aPar.getHeight() - py - ins.bottom; if(ph<0) ph = 0; if(ph<=0) return;
    double ay = ViewUtils.getAlignY(aPar), ax = ViewUtils.getAlignX(aPar);
    
    // Layout children
    for(View child : children) {
        double cw = isFillWidth || child.isGrowWidth()? pw : Math.min(child.getBestWidth(-1), pw);
        double ch = isFillHeight || child.isGrowHeight()? ph : Math.min(child.getBestHeight(-1), ph);
        double cx = px, cy = py;
        if(pw>cw) { double ax2 = child.getLeanX()!=null? ViewUtils.getLeanX(child) : ax;
            cx += Math.round((pw-cw)*ax2); }
        if(ph>ch) { double ay2 = child.getLeanY()!=null? ViewUtils.getLeanY(child) : ay;
            cy += Math.round((ph-ch)*ay2); }
        child.setBounds(cx, cy, cw, ch);
    }
}

}