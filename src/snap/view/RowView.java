/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to layout child views horizontally, from left to right.
 */
public class RowView extends ChildView {
    
    // The spacing between nodes
    double        _spacing;
    
    // Whether to fill to height
    boolean       _fillHeight;
    
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
 * Returns whether children will be resized to fill height.
 */
public boolean isFillHeight()  { return _fillHeight; }

/**
 * Sets whether children will be resized to fill height.
 */
public void setFillHeight(boolean aValue)  { _fillHeight = aValue; }

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return getPrefWidth(this, null, getSpacing(), aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return getPrefHeight(this, null, aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { layout(this, null, null, isFillHeight(), getSpacing()); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Spacing, FillHeight
    if(getSpacing()!=0) e.add("Spacing", getSpacing());
    if(isFillHeight()) e.add("FillHeight", true);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);

    // Unarchive Spacing, FillHeight
    setSpacing(anElement.getAttributeFloatValue("Spacing", 0));
    setFillHeight(anElement.getAttributeBoolValue("FillHeight", false));
}

/**
 * Returns preferred width of given parent with given children.
 */
public static double getPrefWidth(ParentView aPar, View theChildren[], double aSpacing, double aH)
{
    // Get insets and children (just return if empty)
    Insets ins = aPar.getInsetsAll();
    View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged();
    int ccount = children.length; if(ccount==0) return ins.getWidth();
    
    // Get best width and return
    double bw = 0; for(View child : children) bw += child.getBestWidth(-1); if(ccount>1) bw += (ccount-1)*aSpacing;
    return bw + ins.getWidth();
}

/**
 * Returns preferred height of given parent with given children.
 */
public static double getPrefHeight(ParentView aPar, View theChildren[], double aW)
{
    // Get insets and children (just return if empty)
    Insets ins = aPar.getInsetsAll();
    View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged();
    if(children.length==0) return ins.getHeight();
    
    // Get best height and return
    double bh = 0; for(View child : children) bh = Math.max(bh, child.getBestHeight(-1));
    return bh + ins.getHeight();
}
    
/**
 * Performs layout for given parent with given children.
 */
public static void layout(ParentView aPar, View theChilds[], Insets theIns, boolean isFillHeight, double aSpacing)
{
    layout(aPar, theChilds, theIns, false, isFillHeight, aSpacing);
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
    double cx = px, ay = ViewUtils.getAlignY(aPar);
    int grow = 0;
    
    // Layout children
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
        double ch = isFillHeight || child.isGrowHeight()? ph : Math.min(child.getBestHeight(-1), ph);
        double cw = child.getBestWidth(ch), cy = py;
        if(ph>ch && !isFillHeight) { double ay2 = Math.max(ay,ViewUtils.getLeanY(child));
            cy += Math.round((ph-ch)*ay2); }
        cbnds[i] = new Rect(cx, cy, cw, ch); cx += cw + aSpacing;
        if(child.isGrowWidth()) grow++;
    }
    
    // Calculate extra space
    double extra = px + pw - (cx - aSpacing);
    
    // If grow shapes, add grow
    if(extra!=0 && grow>0) { double dx = 0;
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
            if(dx!=0) cbnd.setX(cbnd.getX() + dx);
            if(child.isGrowWidth()) { cbnd.setWidth(cbnd.getWidth()+extra/grow); dx += extra/grow; }
        }
    }
    
    // Otherwise, if FillWidth and last child doesn't fill width, extend it
    else if(isFillWidth && extra!=0 && grow==0)
        cbnds[children.length-1].width = px + pw - cbnds[children.length-1].x;
    
    // Otherwise, check for horizontal alignment/lean shift
    else if(extra>0) {
        double ax = ViewUtils.getAlignX(aPar);
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
            ax = Math.max(ax, ViewUtils.getLeanX(child)); double dx = extra*ax;
            if(dx>0) cbnd.setX(cbnd.getX() + extra*ax);
        }
    }
        
    // Reset child bounds
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = cbnds[i];
        child.setBounds(bnds); }
}

}