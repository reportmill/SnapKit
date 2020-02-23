/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Size;
import snap.gfx.*;
import snap.util.*;

/**
 * A class that can scroll a child view.
 */
public class Scroller extends ParentView implements ViewHost {

    // The content
    View            _content;
    
    // Whether to fit content to scroller width/height
    boolean         _fitWidth, _fitHeight;

    // Whether to grow content to scroller width/height if smaller than scroller (overrides content setting if true)
    boolean         _growContWidth = true, _growContHeight = true;
    
    // The scroll amounts
    double          _scrollX, _scrollY;
    
    // The content width/height being scrolled
    double          _scrollWidth, _scrollHeight;

    // Constants for properties
    public static final String Content_Prop = "Content";
    public static final String ScrollX_Prop = "ScrollX";
    public static final String ScrollY_Prop = "ScrollY";
    public static final String ScrollWidth_Prop = "ScrollWidth";
    public static final String ScrollHeight_Prop = "ScrollHeight";

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
    // If already set, just return
    View old = _content; if(aView==old) return;
    
    // Remove old content, set and add new content
    if(_content!=null) removeChild(_content);
    _content = aView;
    if(_content!=null) addChild(aView);
    
    // Fire prop change
    firePropChange(Content_Prop, old, aView);
}

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
 * Returns the horizontal offset into content.
 */
public double getScrollX()  { return _scrollX; }

/**
 * Sets the horizontal offset into content.
 */
public void setScrollX(double aValue)
{
    // Get value rounded and in valid range (just return if already set)
    aValue = Math.round(aValue);
    if(aValue<0) aValue = 0;
    else if(aValue>getScrollXLimit()) aValue = getScrollXLimit();
    if(MathUtils.equals(aValue, _scrollX)) return;
    
    // Set value and relayout/repaint
    firePropChange(ScrollX_Prop, _scrollX, _scrollX=aValue);
    relayout(); repaint();
}

/**
 * Returns the maximum possible horizontal offset.
 */
public double getScrollXLimit()
{
    double val = getScrollWidth() - getWidth();
    return Math.round(Math.max(val, 0));
}

/**
 * Returns the vertical offset into content.
 */
public double getScrollY()  { return _scrollY; }

/**
 * Sets the vertical offset into content.
 */
public void setScrollY(double aValue)
{
    // Get value rounded and in valid range (just return if already set)
    aValue = Math.round(aValue);
    if(aValue<0) aValue = 0;
    else if(aValue>getScrollYLimit()) aValue = getScrollYLimit();
    if(MathUtils.equals(aValue, _scrollY)) return;
    
    // Set value and relayout/repaint
    firePropChange(ScrollY_Prop, _scrollY, _scrollY=aValue);
    relayout(); repaint();
}

/**
 * Returns the maximum possible vertical offset.
 */
public double getScrollYLimit()
{
    double val = getScrollHeight() - getHeight();
    return Math.round(Math.max(val, 0));
}

/**
 * Returns the width of the content being scrolled.
 */
public double getScrollWidth()  { return _scrollWidth; }

/**
 * Sets the width of the content being scrolled.
 */
protected void setScrollWidth(double aValue)
{
    if(aValue==_scrollWidth) return;
    firePropChange(ScrollWidth_Prop, _scrollWidth, _scrollWidth = aValue);
}

/**
 * Returns the height of the content being scrolled.
 */
public double getScrollHeight()  { return _scrollHeight; }

/**
 * Sets the height of the content being scrolled.
 */
protected void setScrollHeight(double aValue)
{
    if(aValue==_scrollHeight) return;
    firePropChange(ScrollHeight_Prop, _scrollHeight, _scrollHeight = aValue);
}

/**
 * Returns the ratio of ScrollX to ScrollXMax.
 */
public double getScrollXRatio()
{
    double shm = getScrollXLimit();
    return shm>0? _scrollX/shm : 0;
}

/**
 * Sets ScrollX from the given ratio of ScrollY to ScrollYMax.
 */
public void setScrollXRatio(double aValue)
{
    double sh = aValue*getScrollXLimit();
    setScrollX(sh);
}

/**
 * Returns the ratio of ScrollY to ScrollYMax.
 */
public double getScrollYRatio()
{
    double svm = getScrollYLimit();
    return svm>0? _scrollY/svm : 0;
}

/**
 * Sets ScrollY from the given ratio of ScrollY to ScrollYMax.
 */
public void setScrollYRatio(double aValue)
{
    double sv = aValue*getScrollYLimit();
    setScrollY(sv);
}

/**
 * Returns the ratio of Scroller.Width to Content.Width.
 */
public double getWidthRatio()
{
    double w = getWidth(), sw = getScrollWidth();
    return sw>0? w/sw : 1;
}

/**
 * Returns the ratio of Scroller.Height to Content.Height.
 */
public double getHeightRatio()
{
    double h = getHeight(), sh = getScrollHeight();
    return sh>0? h/sh : 1;
}

/**
 * Returns preferred size of content view in Scroller.
 */
protected Size getContentPrefSize()
{
    // If no content return (1,1)
    if(_content==null) return new Size(1,1);
    
    // Handle Horizontal
    if(_content.isHorizontal()) {
        
        // Get PrefWidth (expand to width if needed)
        double w = isFillWidth()? getWidth() : _content.getBestWidth(-1);
        if(w<getWidth() && (isGrowContentWidth() || _content.isGrowWidth()))
           w = getWidth();
           
        // Get PrefHeight (expand to height if needed)
        double h = isFillHeight()? getHeight() : _content.getBestHeight(w);
        if(h<getHeight() && (isGrowContentHeight() || _content.isGrowHeight()))
            h = getHeight();
            
        // Return size
        return new Size(w,h);
    }
    
    // Handle Vertical
    double h = isFillHeight()? getHeight() : _content.getBestHeight(-1);
    if(h<getHeight() && (isGrowContentHeight() || _content.isGrowHeight()))
        h = getHeight();
    double w = isFillWidth()? getWidth() : _content.getBestWidth(h);
    if(w<getWidth() && (isGrowContentWidth() || _content.isGrowWidth()))
        w = getWidth();
    return new Size(w,h);
}

/**
 * Called to scroll the given shape in this node coords to visible.
 */
public void scrollToVisible(Shape aShape)
{
    // Get Scroller scroll and size
    double vx = getScrollX(), vw = getWidth();
    double vy = getScrollY(), vh = getHeight(); if(vw==0 || vh==0) return;
    
    // Get given shape
    Rect srect = aShape.getBounds();
    double sx = vx + srect.x, sw = srect.getWidth();
    double sy = vy + srect.y, sh = srect.getHeight();
    
    // Calculate/set new visible x and y: If shape rect is outside vrect, shift vrect to it; if bigger, center it
    double nvx = sx<vx? sx : sx+sw>vx+vw? sx+sw-vw : vx; if(sw>vw) nvx += (sw-vw)/2;
    double nvy = sy<vy? sy : sy+sh>vy+vh? sy+sh-vh : vy; if(sh>vh) nvy += (sh-vh)/2;
    //setScrollX(nvx); setScrollY(nvy);
    
    // Set new value (with anim)
    getAnimCleared(250).setValue(ScrollX_Prop, nvx).setValue(ScrollY_Prop, nvy).play();
}

/**
 * ViewHost method: Override to return 1 if content is present.
 */
public int getGuestCount()  { return getContent()!=null? 1 : 0; }

/**
 * ViewHost method: Override to return content (and complain if index beyond 0).
 */
public View getGuest(int anIndex)
{
    if(anIndex>0) throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
    return getContent();
}

/**
 * ViewHost method: Override to set content.
 */
public void addGuest(View aChild, int anIndex)
{
    setContent(aChild);
}

/**
 * ViewHost method: Override to clear content (and complain if index beyond 0).
 */
public View removeGuest(int anIndex)
{
    if(anIndex>0) throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
    View cont = getContent(); setContent(null);
    return cont;
}

/**
 * Calculates the minimum width.
 */
protected double getMinWidthImpl()
{
    return _content!=null? _content.getMinWidth() : 0;
}

/**
 * Calculates the minimum height.
 */
protected double getMinHeightImpl()
{
    return _content!=null? _content.getMinHeight() : 0;
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    return _content!=null? _content.getBestWidth(aH) : 0;
}

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    return _content!=null? _content.getBestHeight(aW) : 0;
}

/**
 * Override to layout children.
 */
protected void layoutImpl()
{
    if(_content==null) return;
    double pw = getWidth(), ph = getHeight();
    Size csize = getContentPrefSize();
    double cw = csize.width, ch = csize.height;
    
    // Get content bounds
    double sx = getScrollX(); if(sx>cw-pw) sx = Math.round(cw-pw);
    double sy = getScrollY(); if(sy>ch-ph) sy = Math.round(ch-ph);
    _content.setBounds(-sx, -sy, cw, ch);
    
    // Update ScrollWidth/ScrollHeight
    setScrollWidth(cw);
    setScrollHeight(ch);
}

/**
 * Handle events.
 */
public void processEvent(ViewEvent anEvent)
{
    // Handle Scroll event
    if(anEvent.isScroll()) {
        
        // Handle Horizontal scroll
        double scrollX = anEvent.getScrollX();
        if(scrollX!=0) {
            double scroll2 = MathUtils.clamp(getScrollX() + scrollX*4, 0, getScrollXLimit());
            if(scroll2!=getScrollX()) {
                setScrollX(scroll2);
                anEvent.consume();
            }
        }
        
        // Handle vertical scroll
        double scrollY = anEvent.getScrollY();
        if(scrollY!=0 && getScrollYLimit()!=0) {
            double scroll2 = MathUtils.clamp(getScrollY() + scrollY*4, 0 , getScrollYLimit());
            if(scroll2!=getScrollY()) {
                setScrollY(scroll2);
                anEvent.consume();
            }
        }
    }
}

/**
 * Override for Scroller properties.
 */
public Object getValue(String aPropName)
{
    if(aPropName.equals(ScrollX_Prop)) return getScrollX();
    if(aPropName.equals(ScrollY_Prop)) return getScrollY();
    if(aPropName.equals(ScrollWidth_Prop)) return getScrollWidth();
    if(aPropName.equals(ScrollHeight_Prop)) return getScrollHeight();
    return super.getValue(aPropName);
}

/**
 * Override for Scroller properties.
 */
public void setValue(String aPropName, Object aValue)
{
    if(aPropName.equals(ScrollX_Prop)) setScrollX(SnapUtils.doubleValue(aValue));
    else if(aPropName.equals(ScrollY_Prop)) setScrollY(SnapUtils.doubleValue(aValue));
    else if(aPropName.equals(ScrollWidth_Prop)) setScrollWidth(SnapUtils.doubleValue(aValue));
    else if(aPropName.equals(ScrollHeight_Prop)) setScrollY(SnapUtils.doubleValue(aValue));
    else super.setValue(aPropName, aValue);
}

}