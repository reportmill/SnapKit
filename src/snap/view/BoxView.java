/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View that holds another view.
 */
public class BoxView extends HostView {

    // The content
    View         _child;
    
    // Whether to fill width, height
    boolean      _fillWidth, _fillHeight;
    
/**
 * Creates a new Box.
 */
public BoxView()  { }

/**
 * Creates a new Box for content.
 */
public BoxView(View aContent)  { setContent(aContent); }

/**
 * Creates a new Box for content with FillWidth, FillHeight params.
 */
public BoxView(View aContent, boolean isFillWidth, boolean isFillHeight)
{
    setContent(aContent); setFillWidth(isFillWidth); setFillHeight(isFillHeight);
}

/**
 * Returns the box content.
 */
public View getContent()  { return _child; }

/**
 * Sets the box content.
 */
public void setContent(View aView)
{
    if(aView==_child) return;
    if(_child!=null) removeChild(_child);
    _child = aView;
    if(_child!=null) addChild(_child);
}

/**
 * Returns whether children will be resized to fill width.
 */
public boolean isFillWidth()  { return _fillWidth; }

/**
 * Sets whether children will be resized to fill width.
 */
public void setFillWidth(boolean aValue)
{
    _fillWidth = aValue;
    repaint(); relayoutParent();
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
    _fillHeight = aValue;
    repaint(); relayoutParent();
}

/**
 * HostView method.
 */
public int getGuestCount()  { return getContent()!=null? 1 : 0; }

/**
 * HostView method.
 */
public View getGuest(int anIndex)  { return getContent(); }

/**
 * HostView method.
 */
public void addGuest(View aChild, int anIndex)
{
    setContent(aChild);
    fireGuestPropChange(null, aChild, 0);
}

/**
 * HostView method.
 */
public View removeGuest(int anIndex)
{
    View cont = getContent(); setContent(null);
    fireGuestPropChange(cont, null, 0);
    return cont;
}

/**
 * Override to change to CENTER.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Override.
 */
protected double getPrefWidthImpl(double aH)  { return getPrefWidth(this, getContent(), aH); }

/**
 * Override.
 */
protected double getPrefHeightImpl(double aW)  { return getPrefHeight(this, getContent(), aW); }

/**
 * Override.
 */
protected void layoutImpl()  { layout(this, getContent(), null, _fillWidth, _fillHeight); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive FillWidth
    if(isFillWidth()) e.add("FillWidth", true);
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

    // Unarchive Spacing, FillWidth
    if(anElement.hasAttribute("FillWidth")) setFillWidth(anElement.getAttributeBoolValue("FillWidth"));
    if(anElement.hasAttribute("FillHeight")) setFillHeight(anElement.getAttributeBoolValue("FillHeight"));
}

/**
 * Returns preferred width of layout.
 */
public static double getPrefWidth(ParentView aPar, View aChild, double aH)
{
    // Get insets (just return if empty)
    Insets ins = aPar.getInsetsAll(); if(aChild==null) return ins.getWidth();
    
    // Get height without insets, get best width and return
    double h = aH>=0? (aH - ins.getHeight()) : aH;
    double bw = aChild.getBestWidth(h);
    return bw + ins.getWidth();
}

/**
 * Returns preferred height of layout.
 */
public static double getPrefHeight(ParentView aPar, View aChild, double aW)
{
    // Get insets (just return if empty)
    Insets ins = aPar.getInsetsAll(); if(aChild==null) return ins.getHeight();
    
    // Get width without insets, get best height and return
    double w = aW>=0? (aW - ins.getWidth()) : aW;
    double bh = aChild.getBestHeight(w);
    return bh + ins.getHeight();
}

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
    double cw = isFillWidth || aChild.isGrowWidth()? pw : aChild.getBestWidth(-1); if(cw>pw) cw = pw;
    double ch = isFillHeight? ph : aChild.getBestHeight(cw);
    
    // Handle normal layout
    double dx = pw - cw, dy = ph - ch;
    double sx = aChild.getLeanX()!=null? ViewUtils.getLeanX(aChild) : ViewUtils.getAlignX(aPar);
    double sy = aChild.getLeanY()!=null? ViewUtils.getLeanY(aChild) : ViewUtils.getAlignY(aPar);
    aChild.setBounds(px+dx*sx, py+dy*sy, cw, ch);
}
    
}