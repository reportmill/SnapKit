/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to layout child views vertically, from top to bottom.
 */
public class ColView extends ChildView {

    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill to with
    boolean       _fillWidth;
    
/**
 * Returns the spacing.
 */
public double getSpacing()  { return _spacing; }

/**
 * Sets the spacing.
 */
public void setSpacing(double aValue)
{
    if(aValue==_spacing) return;
    firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
    relayout(); relayoutParent();
}

/**
 * Returns whether children will be resized to fill width.
 */
public boolean isFillWidth()  { return _fillWidth; }

/**
 * Sets whether children will be resized to fill width.
 */
public void setFillWidth(boolean aValue)  { _fillWidth = aValue; }

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.TOP_LEFT; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return getPrefWidth(this, null, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return getPrefHeight(this, null, _spacing, aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { layout(this, null, null, isFillWidth(), getSpacing()); }

/**
 * Override to return true.
 */
public boolean getDefaultVertical()  { return true; }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Spacing, FillWidth
    if(getSpacing()!=0) e.add("Spacing", getSpacing());
    if(isFillWidth()) e.add("FillWidth", true);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);

    // Unarchive Spacing, FillWidth
    if(anElement.hasAttribute("Spacing")) setSpacing(anElement.getAttributeFloatValue("Spacing"));
    if(anElement.hasAttribute("FillWidth")) setFillWidth(anElement.getAttributeBoolValue("FillWidth"));
}

/**
 * Returns preferred width of given parent with given children.
 */
public static final double getPrefWidth(ParentView aPar, View theChildren[], double aH)
{
    // Get insets and children (just return if empty)
    Insets ins = aPar.getInsetsAll();
    View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged();
    if(children.length==0) return ins.getWidth();
    
    // Get max best width of children (including margins)
    double pw = 0;
    for(View child : children) {
        double marW = child.getMargin().getWidth();
        double cbw = child.getBestWidth(-1);
        pw = Math.max(pw, cbw + marW);
    }
    
    // Return preferred width + pad width
    return pw + ins.getWidth();
}

/**
 * Returns preferred height of given parent with given children.
 */
public static final double getPrefHeight(ParentView aPar, View theChildren[], double aSpacing, double aW)
{
    // Get insets and children (just return if empty)
    Insets ins = aPar.getInsetsAll();
    View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged();
    int ccount = children.length; if(ccount==0) return ins.getHeight();
    
    // Iterate over children and add spacing, margin-top (collapsable) and child height
    double ph = 0, spc = 0;
    for(View child : children) {
        Insets marg = child.getMargin(); double marT = marg.getTop(), marB = marg.getBottom();
        double cbh = child.getBestHeight(-1);
        ph += Math.max(spc, marT) + cbh;
        spc = Math.max(aSpacing, marB);
    }
    
    // Add margin for final child
    ph += children[ccount-1].getMargin().getBottom();
    
    // Return preferred height + pad height
    return ph + ins.getHeight();
}

/**
 * Performs layout for given parent with given children.
 */
public static void layout(ParentView aPar, View theChilds[], Insets theIns, boolean isFillWidth, double aSpacing)
{
    layout(aPar, theChilds, theIns, isFillWidth, false, aSpacing);
}

/**
 * Performs layout for given parent with given children.
 */
public static void layout(ParentView aPar, View theChilds[], Insets theIns, boolean isFillWidth, boolean isFillHeight,
    double aSpacing)
{
    // Get children (just return if empty)
    View children[] = theChilds!=null? theChilds : aPar.getChildrenManaged(); if(children.length==0) return;
    
    // Get parent bounds for insets
    Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
    double px = ins.left, py = ins.top;
    double pw = aPar.getWidth() - px - ins.right; if(pw<0) pw = 0; if(pw<=0) return;
    double ph = aPar.getHeight() - py - ins.bottom; if(ph<0) ph = 0; if(ph<=0) return;
    
    // Get child bounds
    Rect cbnds[] = new Rect[children.length];
    double cy = py, ax = ViewUtils.getAlignX(aPar);
    int grow = 0;
    
    // Layout children
    double spc = 0;
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
    
        // Get child margin
        Insets marg = child.getMargin(); double marT = marg.getTop(), marB = marg.getBottom();
        
        // Get child width
        double maxW = Math.max(pw - marg.getWidth(), 0);
        double cw = isFillWidth || child.isGrowWidth()? maxW : Math.min(child.getBestWidth(-1), maxW);
        
        // Calc x accounting for margin and alignment
        double cx = px + marg.getLeft();
        if(cw<maxW) { double ax2 = Math.max(ax,ViewUtils.getLeanX(child));
            cx = Math.max(cx, px + Math.round((pw-cw)*ax2)); }
        
        // Get child height and update child y for spacing/margin-top
        double ch = child.getBestHeight(cw);
        cy += Math.max(spc, marT);
        
        // Set child bounds
        cbnds[i] = new Rect(cx, cy, cw, ch);
        
        // Update spacing, current child y and grow count
        spc = Math.max(aSpacing, marB); cy += ch;
        if(child.isGrowHeight()) grow++;
    }
    
    // Add margin for final child
    cy += children[children.length-1].getMargin().getBottom();
    
    // Calculate extra space
    double extra = py + ph - cy;
    
    // If grow shapes, add grow
    if(extra!=0 && grow>0) { double dy = 0;
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
            if(dy!=0) cbnd.setY(cbnd.getY() + dy);
            if(child.isGrowHeight()) { cbnd.setHeight(cbnd.getHeight()+extra/grow); dy += extra/grow; }
        }
    }
    
    // Otherwise, if FillHeight and last child doesn't fill height, extend it
    else if(isFillHeight && extra!=0 && grow==0)
        cbnds[children.length-1].height = py + ph - cbnds[children.length-1].y;
    
    // Otherwise, check for vertical alignment/lean shift
    else if(extra>0) {
        double ay = ViewUtils.getAlignY(aPar);
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
            ay = Math.max(ay, ViewUtils.getLeanY(child)); double dy = extra*ay;
            if(dy>0) cbnd.setY(cbnd.getY() + extra*ay);
        }
    }

    // Reset child bounds
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = cbnds[i];
        child.setBounds(bnds); }
}
    
}