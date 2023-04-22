/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Size;
import snap.util.*;

/**
 * A class that can scroll a child view.
 */
public class Scroller extends ParentView implements ViewHost {

    // The content
    private View  _content;
    
    // Whether to fit content to scroller width/height
    private boolean  _fitWidth, _fitHeight;

    // Whether to grow content to scroller width/height if smaller than scroller (overrides content setting if true)
    private boolean  _growContWidth = true, _growContHeight = true;
    
    // The scroll amounts
    private double  _scrollX, _scrollY;
    
    // The content width/height being scrolled
    private double  _scrollWidth, _scrollHeight;

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
        View oldContent = _content; if (aView == oldContent) return;

        // Remove old content, set and add new content
        if (_content != null)
            removeChild(_content);
        _content = aView;
        if (_content != null)
            addChild(aView);

        // Fire prop change
        firePropChange(Content_Prop, oldContent, aView);
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
        if (aValue == isFillWidth()) return;
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
        if (aValue == isFillHeight()) return;
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
        if (aValue == isGrowContentWidth()) return;
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
        if (aValue == isGrowContentHeight()) return;
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
        if (aValue < 0)
            aValue = 0;
        else if (aValue > getScrollLimitX())
            aValue = getScrollLimitX();
        if (MathUtils.equals(aValue, _scrollX)) return;

        // Set value and relayout/repaint
        firePropChange(ScrollX_Prop, _scrollX, _scrollX = aValue);
        relayout();
        repaint();
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
        if (aValue < 0)
            aValue = 0;
        else if (aValue > getScrollLimitY())
            aValue = getScrollLimitY();
        if (MathUtils.equals(aValue, _scrollY)) return;

        // Set value and relayout/repaint
        firePropChange(ScrollY_Prop, _scrollY, _scrollY = aValue);
        relayout();
        repaint();
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
        if (aValue == _scrollWidth) return;
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
        if (aValue == _scrollHeight) return;
        firePropChange(ScrollHeight_Prop, _scrollHeight, _scrollHeight = aValue);
    }

    /**
     * Returns the maximum possible horizontal offset.
     */
    public double getScrollLimitX()
    {
        double scrollLimitX = getScrollWidth() - getWidth();
        return Math.round(Math.max(scrollLimitX, 0));
    }

    /**
     * Returns the maximum possible vertical offset.
     */
    public double getScrollLimitY()
    {
        double scrollLimitY = getScrollHeight() - getHeight();
        return Math.round(Math.max(scrollLimitY, 0));
    }

    /**
     * Returns the ratio of ScrollX to ScrollXMax.
     */
    public double getScrollRatioX()
    {
        double scrollLimitX = getScrollLimitX();
        return scrollLimitX > 0 ? _scrollX / scrollLimitX : 0;
    }

    /**
     * Sets ScrollX from the given ratio of ScrollY to ScrollYMax.
     */
    public void setScrollRatioX(double aValue)
    {
        double scrollX = aValue * getScrollLimitX();
        setScrollX(scrollX);
    }

    /**
     * Returns the ratio of ScrollY to ScrollYMax.
     */
    public double getScrollRatioY()
    {
        double scrollLimitY = getScrollLimitY();
        return scrollLimitY > 0 ? _scrollY / scrollLimitY : 0;
    }

    /**
     * Sets ScrollY from the given ratio of ScrollY to ScrollYMax.
     */
    public void setScrollRatioY(double aValue)
    {
        double scrollY = aValue * getScrollLimitY();
        setScrollY(scrollY);
    }

    /**
     * Returns the ratio of Scroller.Width to Content.Width.
     */
    public double getWidthRatio()
    {
        double viewW = getWidth();
        double scrollW = getScrollWidth();
        return scrollW > 0 ? viewW / scrollW : 1;
    }

    /**
     * Returns the ratio of Scroller.Height to Content.Height.
     */
    public double getHeightRatio()
    {
        double viewH = getHeight();
        double scrollH = getScrollHeight();
        return scrollH > 0 ? viewH / scrollH : 1;
    }

    /**
     * Returns preferred size of content view in Scroller.
     */
    protected Size getContentPrefSize()
    {
        // If no content return (1,1)
        if (_content == null) return new Size(1,1);

        // Get info
        Insets ins = getInsetsAll();
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Handle FixedWidth special
        boolean isFixedWidth = isFillWidth();
        boolean isFixedHeight = isFillHeight();
        if (isFixedWidth && isFixedHeight)
            return new Size(areaW, areaH);

        // Get Grow width/height values
        boolean isGrowW = isGrowContentWidth() || _content.isGrowWidth();
        boolean isGrowH = isGrowContentHeight() || _content.isGrowHeight();

        // Handle FixedWidth
        if (isFixedWidth) {
            double prefW = areaW;
            double prefH = _content.getBestHeight(prefW);
            if (prefH < areaH && isGrowH)
                prefH = areaH;
            return new Size(prefW, prefH);
        }

        // Handle FixedHeight
        if (isFixedHeight) {
            double prefH = areaH;
            double prefW = _content.getBestWidth(prefH);
            if (prefW < areaW && isGrowW)
                prefW = areaW;
            return new Size(prefW, prefH);
        }

        // Handle Horizontal
        if (_content.isHorizontal()) {

            // Get PrefWidth (expand to width if needed)
            double prefW = _content.getBestWidth(-1);
            if (prefW < areaW && isGrowW)
               prefW = areaW;

            // Get PrefHeight (expand to height if needed)
            double prefH = _content.getBestHeight(prefW);
            if (prefH < areaH && isGrowH)
                prefH = areaH;

            // Return size
            return new Size(prefW, prefH);
        }

        // Handle Vertical
        double prefH = _content.getBestHeight(-1);
        if (prefH < areaH && isGrowH)
            prefH = areaH;
        double prefW = _content.getBestWidth(prefH);
        if (prefW < areaW && isGrowW)
            prefW = areaW;
        return new Size(prefW, prefH);
    }

    /**
     * Called to scroll the given shape in this node coords to visible.
     */
    public void scrollToVisible(Shape aShape)
    {
        // Get Scroller scroll and size
        double scrollX = getScrollX();
        double scrollY = getScrollY();
        double scrollW = getWidth(); if (scrollW == 0) return;
        double scrollH = getHeight(); if (scrollH == 0) return;

        // Get shape bounds
        Rect shapeBnds = aShape.getBounds();
        double shapeX = shapeBnds.x;
        double shapeY = shapeBnds.y;
        double shapeW = shapeBnds.width;
        double shapeH = shapeBnds.height;

        // Calculate new ScrollX: If shape rect is outside vrect, shift vrect to it; if bigger, center it
        double scrollX2 = scrollX;
        if (shapeX < 0)
            scrollX2 = scrollX + shapeX;
        else if (shapeX + shapeW > scrollW)
            scrollX2 = scrollX + (shapeX + shapeW - scrollW);
        if (shapeW > scrollW)
            scrollX2 += Math.round((shapeW - scrollW) / 2);

        // Calculate new ScrollY: If shape rect is outside vrect, shift vrect to it; if bigger, center it
        double scrollY2 = scrollY;
        if (shapeY < 0)
            scrollY2 = scrollY + shapeY;
        else if (shapeY + shapeH > scrollH)
            scrollY2 = scrollY + (shapeY + shapeH - scrollH);
        if (shapeH > scrollH)
            scrollY2 += Math.round((shapeH - scrollH) / 2);

        // Set new value (with anim)
        ViewAnim anim = getAnimCleared(250);
        anim.setValue(ScrollX_Prop, scrollX2).setValue(ScrollY_Prop, scrollY2);
        anim.play();
    }

    /**
     * ViewHost method: Override to return 1 if content is present.
     */
    public int getGuestCount()  { return getContent()!=null ? 1 : 0; }

    /**
     * ViewHost method: Override to return content (and complain if index beyond 0).
     */
    public View getGuest(int anIndex)
    {
        if (anIndex>0) throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
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
        if (anIndex > 0)
            throw new IndexOutOfBoundsException("Index " + anIndex + " beyond 0");
        View content = getContent();
        setContent(null);
        return content;
    }

    /**
     * Calculates the minimum width.
     */
    protected double getMinWidthImpl()
    {
        return _content != null ? _content.getMinWidth() : 0;
    }

    /**
     * Calculates the minimum height.
     */
    protected double getMinHeightImpl()
    {
        return _content != null ? _content.getMinHeight() : 0;
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        double prefW = _content != null ? _content.getBestWidth(aH) : 0;
        return prefW + ins.getWidth();
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        double prefH = _content != null ? _content.getBestHeight(aW) : 0;
        return prefH + ins.getHeight();
    }

    /**
     * Override to layout children.
     */
    protected void layoutImpl()
    {
        // If no content, just return
        if (_content == null) return;

        // Get area bounds
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();

        // Get content size
        Size contentSize = getContentPrefSize();
        double contentW = contentSize.width;
        double contentH = contentSize.height;

        // Get content X from current scroll position (if now too far, pull back)
        double contentX = getScrollX();
        if (contentW < areaW) {
            double alignX = _content.getLeanX() != null ? ViewUtils.getLeanX(_content) : ViewUtils.getAlignX(this);
            contentX = areaX + Math.round(contentW - areaW) * alignX;
            _scrollX = 0;
        }
        else if (contentX > contentW - areaW) {
            contentX = contentW - areaW;
            _scrollX = contentX;
        }

        // Get content Y from current scroll position (if now too far, pull back)
        double contentY = getScrollY();
        if (contentH < areaH) {
            double alignY = _content.getLeanY() != null ? ViewUtils.getLeanY(_content) : ViewUtils.getAlignY(this);
            contentY = areaY + Math.round(contentH - areaH) * alignY;
            _scrollY = 0;
        }
        else if (contentY > contentH - areaH) {
            contentY = contentH - areaH;
            _scrollY = contentY;
        }

        // Set content bounds
        _content.setBounds(-contentX, -contentY, contentW, contentH);

        // Update ScrollWidth/ScrollHeight
        setScrollWidth(contentW);
        setScrollHeight(contentH);
    }

    /**
     * Handle events.
     */
    public void processEvent(ViewEvent anEvent)
    {
        // Handle Scroll event
        if (anEvent.isScroll()) {

            // Handle Horizontal scroll
            double scrollX = anEvent.getScrollX();
            if (scrollX != 0) {
                double scroll2 = MathUtils.clamp(getScrollX() + scrollX * 4, 0, getScrollLimitX());
                if (scroll2 != getScrollX()) {
                    setScrollX(scroll2);
                    anEvent.consume();
                }
            }

            // Handle vertical scroll
            double scrollY = anEvent.getScrollY();
            if (scrollY != 0 && getScrollLimitY() != 0) {
                double scroll2 = MathUtils.clamp(getScrollY() + scrollY * 4, 0 , getScrollLimitY());
                if (scroll2 != getScrollY()) {
                    setScrollY(scroll2);
                    anEvent.consume();
                }
            }
        }
    }

    /**
     * Override for Scroller properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // ScrollX, ScrollY, ScrollWidth, ScrollHeight
            case ScrollX_Prop: return getScrollX();
            case ScrollY_Prop: return getScrollY();
            case ScrollWidth_Prop: return getScrollWidth();
            case ScrollHeight_Prop: return getScrollHeight();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override for Scroller properties.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // ScrollX, ScrollY, ScrollWidth, ScrollHeight
            case ScrollX_Prop: setScrollX(Convert.doubleValue(aValue)); break;
            case ScrollY_Prop: setScrollY(Convert.doubleValue(aValue)); break;
            case ScrollWidth_Prop: setScrollWidth(Convert.doubleValue(aValue)); break;
            case ScrollHeight_Prop: setScrollY(Convert.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }
}