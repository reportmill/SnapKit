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
    
    // Get best width and return
    double bw = 0; for(View child : children) bw = Math.max(bw, child.getBestWidth(-1));
    return bw + ins.getWidth();
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
    
    // Get best height and return
    double bh = 0; for(View child : children) bh += child.getBestHeight(-1); if(ccount>1) bh += (ccount-1)*aSpacing;
    return bh + ins.getHeight();
}

/**
 * Performs layout for given parent with given children.
 */
public static void layout(ParentView aPar, View theChildren[], Insets theIns, boolean isFillWidth, double aSpacing)
{
    // Get children (just return if empty)
    View children[] = theChildren!=null? theChildren : aPar.getChildrenManaged(); if(children.length==0) return;
    
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
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];
        double cw = isFillWidth || child.isGrowWidth()? pw : Math.min(child.getBestWidth(-1), pw);
        double ch = child.getBestHeight(cw), cx = px;
        if(pw>cw && !isFillWidth) { double ax2 = Math.max(ax,ViewUtils.getLeanX(child)); cx += Math.round((pw-cw)*ax2);}
        cbnds[i] = new Rect(cx,cy,cw,ch); cy += ch + aSpacing;
        if(child.isGrowHeight()) grow++;
    }
    
    // Calculate extra space (return if none)
    double extra = py + ph - (cy - aSpacing);
    
    // If grow shapes, add grow
    if(extra!=0 && grow>0) { double dy = 0;
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
            if(dy!=0) cbnd.setY(cbnd.getY() + dy);
            if(child.isGrowHeight()) { cbnd.setHeight(cbnd.getHeight()+extra/grow); dy += extra/grow; }
        }
    }
    
    // Otherwise, check for vertical alignment/lean shift
    else if(extra>0) {
        double ay = ViewUtils.getAlignY(aPar);
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect cbnd = cbnds[i];
            ay = Math.max(ay, ViewUtils.getLeanY(child)); double dy = extra*ay;
            if(dy>0) cbnd.setY(cbnd.getY() + extra*ay);
        }
    }

    // Reset children bounds
    for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i]; Rect bnds = cbnds[i];
        child.setBounds(bnds); }
}
    
}