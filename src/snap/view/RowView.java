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
    
    // Constants for properties
    public static final String FillHeight_Prop = "FillHeight";
    
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
public void setFillHeight(boolean aValue)
{
    if(aValue==_fillHeight) return;
    firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
    relayout();
}

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
    
    // Iterate over children and add spacing, margin-left (collapsable) and child width
    double pw = 0, spc = 0;
    for(View child : children) {
        Insets marg = child.getMargin();
        double cbw = child.getBestWidth(-1);
        pw += Math.max(spc, marg.left) + cbw;
        spc = Math.max(aSpacing, marg.right);
    }
    
    // Add margin for final child
    pw += children[ccount-1].getMargin().getRight();
    
    // Return preferred width + inset width (rounded)
    double pw2 = pw + ins.getWidth(); pw2 = Math.round(pw2);
    return pw2;
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
    
    // Get max best height of children (including margins)
    double ph = 0;
    for(View child : children) {
        double marH = child.getMargin().getHeight();
        double cbh = child.getBestHeight(-1);
        ph = Math.max(ph, cbh + marH);
    }
    
    // Return preferred height + inset height (rounded is best)
    double ph2 = ph + ins.getHeight(); ph2 = Math.round(ph2);
    return ph2;
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
    double px = ins.left, pw = aPar.getWidth() - ins.getWidth(); if(pw<0) pw = 0; //if(pw<=0) return;
    double py = ins.top, ph = aPar.getHeight() - ins.getHeight(); if(ph<0) ph = 0; //if(ph<=0) return;
    
    // Get child bounds
    Rect cbnds[] = new Rect[children.length];
    double ay = ViewUtils.getAlignY(aPar);
    int grow = 0;
    
    // Layout children
    double cx = px, spc = 0;
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
    
        // Get child margin
        Insets marg = child.getMargin();
        double marL = marg.left, marR = marg.right;
        
        // Get child height 
        double maxH = Math.max(ph - marg.getHeight(), 0);
        double ch = isFillHeight || child.isGrowHeight()? maxH : Math.min(child.getBestHeight(-1), maxH);
        
        // Calc y accounting for margin and alignment
        double cy = py + marg.getTop();
        if(ch<maxH) {
            double ay2 = Math.max(ay,ViewUtils.getLeanY(child));
            cy = Math.max(cy, py + Math.round((ph-ch)*ay2));
        }
        
        // Get child width and update child x for spacing/margin-left
        double cw = child.getBestWidth(ch);
        cx += Math.max(spc, marL);
        
        // Set child bounds
        cbnds[i] = new Rect(cx, cy, cw, ch);
        
        // Update spacing, current child x and grow count
        spc = Math.max(aSpacing, marR); cx += cw;
        if(child.isGrowWidth()) grow++;
    }
    
    // Add margin for last child, calculate extra space and add to growers or alignment
    cx += children[children.length-1].getMargin().getRight();
    int extra = (int)Math.round(px + pw - cx);
    if(extra!=0)
        addExtraSpace(aPar, children, cbnds, extra, grow, isFillWidth);
    
    // Reset child bounds
    for(int i=0;i<children.length;i++) children[i].setBounds(cbnds[i]);
}

/**
 * Adds extra space to growers or alignment.
 */
private static void addExtraSpace(View par, View children[], Rect cbnds[], int extra, int grow, boolean fillW)
{
    // If grow shapes, add grow
    if(grow>0)
        addExtraSpaceToGrowers(children, cbnds, extra, grow);
    
    // Otherwise, if FillWidth, extend last child
    else if(fillW)
        cbnds[children.length-1].width = extra;
    
    // Otherwise, check for horizontal alignment/lean shift
    else if(extra>0)
        addExtraSpaceToAlign(par, children, cbnds, extra);
}

/**
 * Adds extra space to growers.
 */
private static void addExtraSpaceToGrowers(View children[], Rect cbnds[], int extra, int grow)
{
    // Get amount to add to each grower (plus 1 for some if not evenly divisible by grow)
    int each = extra/grow;
    int eachP1 = each + MathUtils.sign(extra);
    int count2 = Math.abs(extra%grow);

    // Iterate over children and add their share (plus 1 for some if not evenly divisible by grow)
    for(int i=0, j=0, dx = 0,iMax=children.length;i<iMax;i++) { View child = children[i];
        Rect cbnd = cbnds[i];
        if(dx!=0)
            cbnd.setX(cbnd.x + dx);
        if(child.isGrowWidth()) {
            int each3 = j<count2? eachP1 : each;
            cbnd.setWidth(cbnd.width + each3);
            dx += each3; j++;
        }
    }
}

/**
 * Adds extra space to alignment.
 */
private static void addExtraSpaceToAlign(View par, View children[], Rect cbnds[], double extra)
{
    double ax = ViewUtils.getAlignX(par);
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
        Rect cbnd = cbnds[i];
        ax = Math.max(ax, ViewUtils.getLeanX(child)); double dx = extra*ax;
        if(dx>0) cbnd.setX(cbnd.x + extra*ax);
    }
}

}