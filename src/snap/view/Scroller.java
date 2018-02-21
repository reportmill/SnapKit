/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.MathUtils;

/**
 * A class that can scroll a child view.
 */
public class Scroller extends HostView {

    // The content
    View            _content;
    
    // The scroll amounts
    double          _scrollH, _scrollV;
    
    // Whether to fit content to scroller width/height
    boolean         _fitWidth, _fitHeight;

    // Whether to grow content to scroller width/height if smaller than scroller (overrides content setting if true)
    boolean         _growContWidth = true, _growContHeight = true;

    // Constants for properties
    public static final String ScrollH_Prop = "ScrollH";
    public static final String ScrollV_Prop = "ScrollV";

/**
 * Creates a new Scroller.
 */
public Scroller()
{
    enableEvents(Scroll);
    setClipToBounds(true);
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
 * Returns the content size.
 */
public Size getContentSize()
{
    // If no content return (1,1)
    if(_content==null) return new Size(1,1);
    
    // Handle Horizontal
    if(_content.isHorizontal()) {
        double w = isFillWidth()? getWidth() : _content.getBestWidth(-1);
        if(w<getWidth() && (isGrowContentWidth() || _content.isGrowWidth())) w = getWidth();
        double h = isFillHeight()? getHeight() : _content.getBestHeight(w);
        if(h<getHeight() && (isGrowContentHeight() || _content.isGrowHeight())) h = getHeight();
        return new Size(w,h);
    }
    
    // Handle Vertical
    double h = isFillHeight()? getHeight() : _content.getBestHeight(-1);
    if(h<getHeight() && (isGrowContentHeight() || _content.isGrowHeight())) h = getHeight();
    double w = isFillWidth()? getWidth() : _content.getBestWidth(h);
    if(w<getWidth() && (isGrowContentWidth() || _content.isGrowWidth())) w = getWidth();
    return new Size(w,h);
}

/**
 * Returns the content width.
 */
public double getContentWidth()
{
    // If no content return 1. If FillWidth, return Width
    if(_content==null) return 1;
    if(isFillWidth()) return getWidth();
    
    // Handle Horizontal
    if(_content.isHorizontal()) {
        double w = _content.getBestWidth(-1);
        if(w<getWidth() && (isGrowContentWidth() || _content.isGrowWidth())) w = getWidth();
        return w;
    }
    
    // Handle Vertical
    double h = getContentHeight();
    if(h<getHeight() && (isGrowContentHeight() || _content.isGrowHeight())) h = getHeight();
    double w = _content.getBestWidth(h);
    if(w<getWidth() && (isGrowContentWidth() || _content.isGrowWidth())) w = getWidth();
    return w;
}

/**
 * Returns the content height.
 */
public double getContentHeight()
{
    // If no content return 1. If FillHeight, return Height
    if(_content==null) return 1;
    if(isFillHeight()) return getHeight();
    
    // Handle Horizontal
    if(_content.isHorizontal()) {
        double w = getContentWidth();
        double h = _content.getBestHeight(w);
        if(h<getHeight() && (isGrowContentHeight() || _content.isGrowHeight())) h = getHeight();
        return h;
    }
    
    // Handle Vertical
    double h = _content.getBestHeight(-1);
    if(h<getHeight() && (isGrowContentHeight() || _content.isGrowHeight())) h = getHeight();
    return h;
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
    // Get value rounded and in valid range
    aValue = Math.round(aValue);
    if(aValue<0) aValue = 0; else if(aValue>getScrollHMax()) aValue = getScrollHMax();
    
    // Set value and relayout/repaint (just return if already set)
    if(MathUtils.equals(aValue,_scrollH)) return;
    firePropChange(ScrollH_Prop, _scrollH, _scrollH=aValue);
    relayout(); repaint();
}

/**
 * Returns the maximum possible horizontal scroll.
 */
public double getScrollHMax()
{
    double cw = getContentWidth();
    return Math.round(Math.max(cw - getWidth(),0));
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
    // Get value rounded and in valid range
    aValue = Math.round(aValue);
    if(aValue<0) aValue = 0; else if(aValue>getScrollVMax()) aValue = getScrollVMax();
    
    // Set value and relayout/repaint (just return if already set)
    if(MathUtils.equals(aValue,_scrollV)) return;
    firePropChange(ScrollV_Prop, _scrollV, _scrollV=aValue);
    relayout(); repaint();
}

/**
 * Returns the maximum possible vertical scroll.
 */
public double getScrollVMax()
{
    double ch = getContentHeight();
    return Math.round(Math.max(ch - getHeight(),0));
}

/**
 * Returns the horizontal scroll.
 */
public double getRatioH()  { double shm = getScrollHMax(); return shm>0? _scrollH/shm : 0; }

/**
 * Sets the horizontal scroll.
 */
public void setRatioH(double aValue)  { setScrollH(aValue*getScrollHMax()); }

/**
 * Returns the vertical scroll.
 */
public double getRatioV()  { double svm = getScrollVMax(); return svm>0? _scrollV/svm : 0; }

/**
 * Sets the vertical scroll.
 */
public void setRatioV(double aValue)  { setScrollV(aValue*getScrollVMax()); }

/**
 * Returns whether this scroller fits content to its width.
 */
public boolean isFillWidth()  { return _fitWidth; }

/**
 * Sets whether this scroller fits content to its width.
 */
public void setFillWidth(boolean aValue)
{
    if(aValue==isFillWidth()) return;
    firePropChange("FitWidth", _fitWidth, _fitWidth = aValue);
}

/**
 * Returns whether this scroller fits content to its height.
 */
public boolean isFillHeight()  { return _fitHeight; }

/**
 * Sets whether this scroller fits content to its height.
 */
public void setFillHeight(boolean aValue)
{
    if(aValue==isFillHeight()) return;
    firePropChange("FitHeight", _fitHeight, _fitHeight = aValue);
}

/**
 * Returns whether to grow content to scroller width if smaller than scroller (overrides content setting if true).
 */
public boolean isGrowContentWidth()  { return _growContWidth; }

/**
 * Sets whether to grow content to scroller width if smaller than scroller (overrides content setting if true).
 */
public void setGrowContentWidth(boolean aValue)
{
    if(aValue==isGrowContentWidth()) return;
    firePropChange("GrowContentWidth", _growContWidth, _growContWidth = aValue);
}

/**
 * Returns whether to grow content to scroller height if smaller than scroller (overrides content setting if true).
 */
public boolean isGrowContentHeight()  { return _growContHeight; }

/**
 * Sets whether to grow content to scroller height if smaller than scroller (overrides content setting if true).
 */
public void setGrowContentHeight(boolean aValue)
{
    if(aValue==isGrowContentHeight()) return;
    firePropChange("GrowContentHeight", _growContHeight, _growContHeight = aValue);
}

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
    //setScrollH(nvx); setScrollV(nvy);
    getAnimCleared(250).setValue(ScrollH_Prop, nvx).setValue(ScrollV_Prop, nvy).play();
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
protected double getPrefWidthImpl(double aH)  { return _content!=null? _content.getBestWidth(aH) : 0; }

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _content!=null? _content.getBestHeight(aW) : 0; }

/**
 * Override to layout children.
 */
protected void layoutImpl()
{
    if(_content==null) return;
    double w = getWidth(), h = getHeight(); Size csize = getContentSize();
    double cw = csize.width, ch = csize.height;
    
    // Get content bounds
    double sx = getScrollH(); if(sx>cw-w) sx = Math.round(cw-w);
    double sy = getScrollV(); if(sy>ch-h) sy = Math.round(ch-h);
    _content.setBounds(-sx,-sy,cw,ch);
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

}