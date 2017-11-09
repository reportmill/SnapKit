/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A ChildView subclass to show overlapping children.
 */
public class StackView extends ChildView {

    // The layout
    StackLayout _layout = new StackLayout(this);

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(-1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(-1); }

/**
 * Layout children.
 */
protected void layoutImpl()  { _layout.layoutChildren(); }

/**
 * A Stack layout.
 */
public static class StackLayout extends ViewLayout {
    
    // Whether to fill width/height
    boolean       _fillWidth, _fillHeight;
    
    /** Creates a new StackLayout for given parent. */
    public StackLayout(ParentView aPar)  { setParent(aPar); }
    
    /** Returns whether layout should fill width. */
    public boolean isFillWidth()  { return _fillWidth; }
    
    /** Sets whether to fill width. */
    public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }
    
    /** Returns whether layout should fill height. */
    public boolean isFillHeight()  { return _fillHeight; }
    
    /** Sets whether to fill height. */
    public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }
    
    /** Returns preferred width of layout. */
    protected double getPrefWidthImpl(double aH)
    {
        View children[] = getChildren();
        double w = 0; for(View child : children) w = Math.max(w, child.getBestWidth(aH));
        return w;
    }
    
    /** Returns preferred height of layout. */
    protected double getPrefHeightImpl(double aW)
    {
        View children[] = getChildren();
        double h = 0; for(View child : children) h = Math.max(h, child.getBestHeight(aW));
        return h;
    }
    
    /** Performs layout in content rect. */
    public void layoutChildren()  { layout(_parent, getChildren(), null, _fillWidth, _fillHeight); }
    
    /** Performs layout in content rect. */
    public static void layout(View aPar, View children[], Insets theIns, boolean isFillWidth, boolean isFillHeight)
    {
        // If no children, just return
        if(children.length==0) return;
        
        // Get parent bounds for insets
        Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
        double px = ins.left, py = ins.top;
        double pw = aPar.getWidth() - px - ins.right; if(pw<0) pw = 0; if(pw<=0) return;
        double ph = aPar.getHeight() - py - ins.bottom; if(ph<0) ph = 0; if(ph<=0) return;
        double ay = getAlignY(aPar), ax = getAlignX(aPar);
        
        // Layout children
        for(View child : children) {
            double cw = isFillWidth || child.isGrowWidth()? pw : Math.min(child.getBestWidth(-1), pw);
            double ch = isFillHeight || child.isGrowHeight()? ph : Math.min(child.getBestHeight(-1), ph);
            double cx = px, cy = py;
            if(pw>cw) { double ax2 = child.getLeanX()!=null? getLeanX(child) : ax;
                cx += Math.round((pw-cw)*ax2); }
            if(ph>ch) { double ay2 = child.getLeanY()!=null? getLeanY(child) : ay;
                cy += Math.round((ph-ch)*ay2); }
            child.setBounds(cx, cy, cw, ch);
        }
    }        
}

}