/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.MathUtils;

/**
 * A class that can scroll a child node.
 */
public class Scroller extends ParentView {

    // The content
    View            _content;
    
    // The scroll amounts
    double          _scrollH, _scrollV;
    
    // Whether to fit content to scroller width/height
    boolean         _fitWidth, _fitHeight;

    // Constants for properties
    public static final String ScrollH_Prop = "ScrollH";
    public static final String ScrollV_Prop = "ScrollV";

/**
 * Creates a new Scroller.
 */
public Scroller()
{
    enableEvents(Scroll);
}

/**
 * Returns the content.
 */
public View getContent()  { return _content; }

/**
 * Sets the content.
 */
public void setContent(View aView)
{
    View old = _content; if(aView==old) return;
    if(_content!=null) removeChild(_content);
    _content = aView;
    if(_content!=null) addChild(aView);
    firePropChange("Content", old, aView);
}

/**
 * Returns the horizontal scroll.
 */
public double getScrollH()  { return _scrollH; }

/**
 * Sets the horizontal scroll.
 */
public void setScrollH(double aValue)
{
    aValue = Math.round(aValue); if(aValue<0) aValue = 0; else if(aValue>getScrollHMax()) aValue = getScrollHMax();
    if(MathUtils.equals(aValue,_scrollH)) return;
    firePropChange(ScrollH_Prop, _scrollH, _scrollH=aValue);
    relayout(); repaint();
}

/**
 * Returns the maximum possible horizontal scroll.
 */
public double getScrollHMax()
{
    if(_content==null) return 0;
    return Math.round(Math.max(_content.getPrefWidth() - getWidth(),0));
}

/**
 * Returns the vertical scroll.
 */
public double getScrollV()  { return _scrollV; }

/**
 * Sets the vertical scroll.
 */
public void setScrollV(double aValue)
{
    aValue = Math.round(aValue); if(aValue<0) aValue = 0; else if(aValue>getScrollVMax()) aValue = getScrollVMax();
    if(MathUtils.equals(aValue,_scrollV)) return;
    firePropChange(ScrollV_Prop, _scrollV, _scrollV=aValue);
    relayout(); repaint();
}

/**
 * Returns the maximum possible vertical scroll.
 */
public double getScrollVMax()
{
    if(_content==null) return 0;
    return Math.round(Math.max(_content.getPrefHeight() - getHeight(),0));
}

/**
 * Returns the horizontal scroll.
 */
public double getRatioH()  { return _scrollH/getScrollHMax(); }

/**
 * Sets the horizontal scroll.
 */
public void setRatioH(double aValue)  { setScrollH(aValue*getScrollHMax()); }

/**
 * Returns the vertical scroll.
 */
public double getRatioV()  { return _scrollV/getScrollVMax(); }

/**
 * Sets the vertical scroll.
 */
public void setRatioV(double aValue)  { setScrollV(aValue*getScrollVMax()); }

/**
 * Returns whether this scroller fits content to its width.
 */
public boolean isFitWidth()  { return _fitWidth; }

/**
 * Sets whether this scroller fits content to its width.
 */
public void setFitWidth(boolean aValue)
{
    if(aValue==isFitWidth()) return;
    firePropChange("FitWidth", _fitWidth, _fitWidth = aValue);
}

/**
 * Returns whether this scroller fits content to its height.
 */
public boolean isFitHeight()  { return _fitHeight; }

/**
 * Sets whether this scroller fits content to its width.
 */
public void setFitHeight(boolean aValue)
{
    if(aValue==isFitHeight()) return;
    firePropChange("FitHeight", _fitHeight, _fitHeight = aValue);
}

/**
 * Returns whether content is fit width.
 */
public boolean isContentFitWidth()  { return isFitWidth() || _content!=null && _content.isScrollFitWidth(); }

/**
 * Returns whether content is fit height.
 */
public boolean isContentFitHeight()  { return isFitHeight() || _content!=null && _content.isScrollFitHeight(); }

/**
 * Called to scroll the given shape in this node coords to visible.
 */
public void scrollToVisible(Shape aShape)
{
    // Calculate/set new visible x and y: If shape rect is outside vrect, shift vrect to it; if bigger, center it
    double vx = getScrollH(), vy = getScrollV(), vw = getWidth(), vh = getHeight(); if(vw==0 || vh==0) return;
    Rect srect = aShape.getBounds();
    double sx = vx+srect.getX(), sy = vy+srect.getY(), sw = srect.getWidth(), sh = srect.getHeight();
    double nvx = sx<vx? sx : sx+sw>vx+vw? sx+sw-vw : vx; if(sw>vw) nvx += (sw-vw)/2;
    double nvy = sy<vy? sy : sy+sh>vy+vh? sy+sh-vh : vy; if(sh>vh) nvy += (sh-vh)/2;
    setScrollH(nvx); setScrollV(nvy);
}

/**
 * Handle events.
 */
public void processEvent(ViewEvent anEvent)
{
    if(anEvent.isScroll()) {
        setScrollH(getScrollH() + anEvent.getScrollX()*4);
        setScrollV(getScrollV() + anEvent.getScrollY()*4);
    }
}

/**
 * Calculates the minimum width.
 */
protected double getMinWidthImpl()  { return _content!=null? _content.getMinWidth() : 0; }

/**
 * Calculates the minimum height.
 */
protected double getMinHeightImpl()  { return _content!=null? _content.getMinHeight() : 0; }

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _content!=null? _content.getPrefWidth(aH) : 0; }

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _content!=null? _content.getPrefHeight(aW) : 0; }

/**
 * Override to layout children.
 */
protected void layoutChildren()
{
    if(_content==null) return;
    double w = getWidth(), h = getHeight(); View cnt = _content;
    double cpw = cnt.getPrefWidth(); if(isContentFitWidth() || cpw<w && cnt.isGrowWidth()) cpw = w;
    double cph = cnt.getPrefHeight(); if(isContentFitHeight() || cph<h && cnt.isGrowHeight()) cph = h;
    
    // Get content bounds
    double sx = getScrollH(); if(sx>cpw-w) sx = Math.round(cpw-w);
    double sy = getScrollV(); if(sy>cph-h) sy = Math.round(cph-h);
    setClip(getBoundsInside());
    cnt.setBounds(-sx,-sy,cpw,cph);
}

}