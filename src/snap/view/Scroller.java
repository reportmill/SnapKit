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

    // The content view
    private View _content;

    // Whether to make content fill width
    private boolean _fillWidth;

    // Whether to make content fill height
    private boolean _fillHeight;

    // The scroll amounts
    private double _scrollX, _scrollY;
    
    // The content width/height being scrolled
    private double _contentWidth, _contentHeight;

    // Constants for properties
    public static final String Content_Prop = BoxView.Content_Prop;
    public static final String FillWidth_Prop = BoxView.FillWidth_Prop;
    public static final String FillHeight_Prop = BoxView.FillHeight_Prop;
    public static final String ScrollX_Prop = "ScrollX";
    public static final String ScrollY_Prop = "ScrollY";
    public static final String ContentWidth_Prop = "ContentWidth";
    public static final String ContentHeight_Prop = "ContentHeight";

    /**
     * Constructor.
     */
    public Scroller()
    {
        super();
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
        if (aView == _content) return;

        // Cache and remove old content
        View oldContent = _content;
        if (_content != null)
            removeChild(_content);

        // Set and add new content
        _content = aView;
        if (_content != null)
            addChild(aView);

        // Reset scroll
        _scrollX = _scrollY = 0;

        // If content is overflow scroll, update border radius to match
        if (_content != null && _content.getOverflow() == Overflow.Scroll)
            setBorderRadius(_content.getBorderRadius());

        // Fire prop change
        firePropChange(Content_Prop, oldContent, aView);
    }

    /**
     * Returns whether this scroller fits content to its width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether this scroller fits content to its width.
     */
    public void setFillWidth(boolean aValue)
    {
        if (aValue == isFillWidth()) return;
        firePropChange(FillWidth_Prop, _fillWidth, _fillWidth = aValue);
    }

    /**
     * Returns whether this scroller fits content to its height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether this scroller fits content to its height.
     */
    public void setFillHeight(boolean aValue)
    {
        if (aValue == isFillHeight()) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
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
        else if (aValue > getScrollMaxX())
            aValue = getScrollMaxX();
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
        else if (aValue > getScrollMaxY())
            aValue = getScrollMaxY();
        if (MathUtils.equals(aValue, _scrollY)) return;

        // Set value and relayout/repaint
        firePropChange(ScrollY_Prop, _scrollY, _scrollY = aValue);
        relayout();
        repaint();
    }

    /**
     * Returns the width of the content being scrolled.
     */
    public double getContentWidth()  { return _contentWidth; }

    /**
     * Sets the width of the content being scrolled.
     */
    protected void setContentWidth(double aValue)
    {
        if (aValue == _contentWidth) return;
        firePropChange(ContentWidth_Prop, _contentWidth, _contentWidth = aValue);
    }

    /**
     * Returns the height of the content being scrolled.
     */
    public double getContentHeight()  { return _contentHeight; }

    /**
     * Sets the height of the content being scrolled.
     */
    protected void setContentHeight(double aValue)
    {
        if (aValue == _contentHeight) return;
        firePropChange(ContentHeight_Prop, _contentHeight, _contentHeight = aValue);
    }

    /**
     * Returns the maximum possible scroll X offset.
     */
    public double getScrollMaxX()
    {
        double scrollMaxX = getContentWidth() - getWidth();
        return Math.round(Math.max(scrollMaxX, 0));
    }

    /**
     * Returns the maximum possible scroll Y offset.
     */
    public double getScrollMaxY()
    {
        double scrollMaxY = getContentHeight() - getHeight();
        return Math.round(Math.max(scrollMaxY, 0));
    }

    /**
     * Returns the ratio of ScrollX to ScrollMaxX.
     */
    public double getScrollRatioX()
    {
        double scrollMaxX = getScrollMaxX();
        return scrollMaxX > 0 ? _scrollX / scrollMaxX : 0;
    }

    /**
     * Sets ScrollX from the given ratio of ScrollY to ScrollMaxY.
     */
    public void setScrollRatioX(double aValue)
    {
        double scrollX = aValue * getScrollMaxX();
        setScrollX(scrollX);
    }

    /**
     * Returns the ratio of ScrollY to ScrollMaxY.
     */
    public double getScrollRatioY()
    {
        double scrollMaxY = getScrollMaxY();
        return scrollMaxY > 0 ? _scrollY / scrollMaxY : 0;
    }

    /**
     * Sets ScrollY from the given ratio of ScrollY to ScrollMaxY.
     */
    public void setScrollRatioY(double aValue)
    {
        double scrollY = aValue * getScrollMaxY();
        setScrollY(scrollY);
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
        boolean isFillWidth = isFillWidth();
        boolean isFillHeight = isFillHeight();
        boolean isHorizontal = isFillWidth || _content.isHorizontal();

        // Handle Horizontal: Get PrefWidth first (expand to scroller size if needed)
        if (isHorizontal) {
            double prefW = isFillWidth ? areaW : _content.getBestWidth(-1);
            if (prefW < areaW)
               prefW = areaW;
            double prefH = isFillHeight ? areaH : _content.getBestHeight(prefW);
            if (prefH < areaH)
                prefH = areaH;
            return new Size(prefW, prefH);
        }

        // Handle Vertical: Get PrefHeight first (expand to scroller size if needed)
        double prefH = isFillHeight ? areaH : _content.getBestHeight(-1);
        if (prefH < areaH)
            prefH = areaH;
        double prefW = _content.getBestWidth(prefH);
        if (prefW < areaW)
            prefW = areaW;
        return new Size(prefW, prefH);
    }

    /**
     * Called to scroll the given shape in this node coords to visible.
     */
    public void scrollToVisible(Shape aShape)
    {
        // If animating, just skip
        ViewUpdater viewUpdater = getUpdater();
        if (viewUpdater != null && viewUpdater.isViewAnimating(this))
            return;

        // Get Scroller scroll and size
        double scrollX = getScrollX();
        double scrollY = getScrollY();
        double scrollW = getWidth(); if (scrollW == 0) return;
        double scrollH = getHeight(); if (scrollH == 0) return;

        // Get shape bounds
        Rect shapeBounds = aShape.getBounds();
        double shapeX = shapeBounds.x;
        double shapeY = shapeBounds.y;
        double shapeW = shapeBounds.width;
        double shapeH = shapeBounds.height;

        // Calculate new ScrollX: If shape rect is visible bounds, shift scroll to it; if bigger, center it
        double newScrollX = scrollX;
        if (shapeX < 0)
            newScrollX = scrollX + shapeX;
        else if (shapeX + shapeW > scrollW)
            newScrollX = scrollX + (shapeX + shapeW - scrollW);
        if (shapeW > scrollW)
            newScrollX += Math.round((shapeW - scrollW) / 2);

        // Calculate new ScrollY: If shape rect is outside visible bounds, shift scroll to it; if bigger, center it
        double newScrollY = scrollY;
        if (shapeY < 0)
            newScrollY = scrollY + shapeY;
        else if (shapeY + shapeH > scrollH)
            newScrollY = scrollY + (shapeY + shapeH - scrollH);
        if (shapeH > scrollH)
            newScrollY += Math.round((shapeH - scrollH) / 2);

        // If not showing, just apply new scroll X/Y and return
        if (!isShowing()) {
            setScrollX(newScrollX);
            setScrollY(newScrollY);
            return;
        }

        // Otherwise scroll with anim
        ViewAnim anim = getAnimCleared(250);
        anim.setValue(ScrollX_Prop, newScrollX).setValue(ScrollY_Prop, newScrollY);
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

        // Update ContentWidth/ContentHeight
        setContentWidth(contentW);
        setContentHeight(contentH);
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
                double scroll2 = MathUtils.clamp(getScrollX() + scrollX * 4, 0, getScrollMaxX());
                if (scroll2 != getScrollX()) {
                    setScrollX(scroll2);
                    anEvent.consume();
                }
            }

            // Handle vertical scroll
            double scrollY = anEvent.getScrollY();
            if (scrollY != 0 && getScrollMaxY() != 0) {
                double scroll2 = MathUtils.clamp(getScrollY() + scrollY * 4, 0 , getScrollMaxY());
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

            // ScrollX, ScrollY
            case ScrollX_Prop: return getScrollX();
            case ScrollY_Prop: return getScrollY();

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

            // ScrollX, ScrollY
            case ScrollX_Prop: setScrollX(Convert.doubleValue(aValue)); break;
            case ScrollY_Prop: setScrollY(Convert.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }
}