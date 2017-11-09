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
public void layoutChildren()  { }

/**
 * Returns the align x factor.
 */
public static double getAlignX(View aView)
{
    HPos hp = aView.getAlign().getHPos(); return hp==HPos.RIGHT? 1 : hp==HPos.CENTER? .5 : 0;
}
    
/**
 * Returns the align y factor.
 */
public static double getAlignY(View aView)
{
    VPos vp = aView.getAlign().getVPos(); return vp==VPos.BOTTOM? 1 : vp==VPos.CENTER? .5 : 0;
}
    
/**
 * Returns the lean x factor.
 */
public static double getLeanX(View aView)
{
    HPos hp = aView.getLeanX(); return hp==HPos.RIGHT? 1 : hp==HPos.CENTER? .5 : 0;
}
    
/**
 * Returns the lean y factor.
 */
public static double getLeanY(View aView)
{
    VPos vp = aView.getLeanY(); return vp==VPos.BOTTOM? 1 : vp==VPos.CENTER? .5 : 0;
}

/**
 * Returns the basic pref width for a view and child.
 */
public static double getPrefWidthBasic(View aPar, View aChild, double aH)
{
    Insets ins = aPar.getInsetsAll(); if(aChild==null) return ins.left + ins.right;
    double h = aH>=0? (aH - ins.getHeight()) : aH;
    double pw = aChild.getBestWidth(h);
    return pw + ins.getWidth();
}

/**
 * Returns the basic pref height for a view and child.
 */
public static double getPrefHeightBasic(View aPar, View aChild, double aW)
{
    Insets ins = aPar.getInsetsAll(); if(aChild==null) return ins.top + ins.bottom;
    double w = aW>=0? (aW - ins.getWidth()) : aW;
    double ph = aChild.getBestHeight(w);
    return ph + ins.getHeight();
}

/**
 * Does basic layout for a parent and child.
 */
public static void layoutBasic(View aPar, View aChild)
{
    if(aChild==null) return;
    Insets ins = aPar.getInsetsAll();
    double x = ins.left, w = aPar.getWidth() - ins.getWidth();
    double y = ins.top, h = aPar.getHeight() - ins.getHeight();
    aChild.setBounds(x, y, w, h);
}

}