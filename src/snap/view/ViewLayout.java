/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A custom class.
 */
public abstract class ViewLayout {

    // The parent
    ParentView       _parent;
    
    // The children
    View             _children[];
    
/**
 * Returns the parent.
 */
public ParentView getParent()  { return _parent; }

/**
 * Sets the parent.
 */
public void setParent(ParentView aPar)  { _parent = aPar; }

/**
 * Returns the (first) child.
 */
public View getChild()  { View chdrn[] = getChildren(); return chdrn!=null && chdrn.length>0? chdrn[0] : null; }

/**
 * Sets the child.
 */
public void setChild(View theChild)  { setChildren(new View[] { theChild }); }

/**
 * Returns the children.
 */
public View[] getChildren()  { return _children!=null? _children : _parent.getChildrenManaged(); }

/**
 * Sets the children.
 */
public void setChildren(View theChildren[])  { _children = theChildren; }

/**
 * Returns the number of children.
 */
public int getChildCount()  { return getChildren().length; }

/**
 * Returns the node insets.
 */
public Insets getInsets()  { return _parent.getInsetsAll(); }

/**
 * Returns whether vertical or horizontal, based on parent.
 */
public boolean isVertical()  { return _parent.isVertical(); }
    
/**
 * Returns preferred width of layout, including insets.
 */
public double getPrefWidth(double aH)
{
    Insets ins = getInsets(); if(getChildCount()==0) return ins.left + ins.right;
    double h = aH>=0? Math.max(aH - ins.top - ins.bottom, 0) : -1;
    double w = getPrefWidthImpl(h);
    return ins.left + w + ins.right;
}

/**
 * Returns preferred width of layout, excluding insets.
 */
protected double getPrefWidthImpl(double aH)  { return 0; }

/**
 * Returns preferred height of layout, including insets.
 */
public double getPrefHeight(double aW)
{
    Insets ins = getInsets(); if(getChildCount()==0) return ins.top + ins.bottom;
    double w = aW>=0? Math.max(aW - ins.left - ins.right, 0) : -1;
    double h = getPrefHeightImpl(w);
    return ins.top + h + ins.bottom;
}

/**
 * Returns preferred height of layout, excluding insets.
 */
protected double getPrefHeightImpl(double aW)  { return 0; }

/**
 * Performs layout.
 */
public void layoutChildren()
{
    if(getChildCount()==0) return;
    Insets ins = getInsets();
    double px = ins.left, py = ins.top;
    double pw = _parent.getWidth() - px - ins.right; if(pw<0) pw = 0;
    double ph = _parent.getHeight() - py - ins.bottom; if(ph<0) ph = 0;
    if(pw>0 && ph>0)
        layoutChildren(px, py, pw, ph);
}

/**
 * Performs layout.
 */
public void layoutChildren(double px, double py, double pw, double ph)  { }

/**
 * Returns the align x factor.
 */
protected double getAlignX(View aView)
{
    HPos hp = aView.getAlign().getHPos(); return hp==HPos.RIGHT? 1 : hp==HPos.CENTER? .5 : 0;
}
    
/**
 * Returns the align y factor.
 */
protected double getAlignY(View aView)
{
    VPos vp = aView.getAlign().getVPos(); return vp==VPos.BOTTOM? 1 : vp==VPos.CENTER? .5 : 0;
}
    
/**
 * Returns the lean x factor.
 */
protected double getLeanX(View aView)
{
    HPos hp = aView.getLeanX(); return hp==HPos.RIGHT? 1 : hp==HPos.CENTER? .5 : 0;
}
    
/**
 * Returns the lean y factor.
 */
protected double getLeanY(View aView)
{
    VPos vp = aView.getLeanY(); return vp==VPos.BOTTOM? 1 : vp==VPos.CENTER? .5 : 0;
}

}