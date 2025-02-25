/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.*;
import snap.gfx.GradientPaint.Stop;
import snap.util.MathUtils;

/**
 * A ScrollBar for ScrollView.
 */
public class ScrollBar extends View {

    // The offset into content
    private double _scroll;
    
    // The size of the scroller showing content
    private double _scrollerSize;
    
    // The size of the content being scrolled
    private double _contentSize;
    
    // Whether button is pressed
    private boolean _pressed;
    
    // Whether button is under mouse
    private boolean _targeted;
    
    // The delta between last mouse press and value
    private double _dv;
    
    // Constants for properties
    public static final String Scroll_Prop = "Scroll";

    // Constant for border insets
    private static final int BORDER_INSET = 2;
    private static final int BORDER_INSET_WIDTH = BORDER_INSET * 2;

    /**
     * Constructor.
     */
    public ScrollBar()
    {
        super();
        enableEvents(MouseEvents);
        enableEvents(Scroll);
    }

    /**
     * Returns the offset into ContentSize.
     */
    public double getScroll()  { return _scroll; }

    /**
     * Sets the offset into ContentSize.
     */
    public void setScroll(double aValue)
    {
        // Get value clamped to valid range and rounded (return if already set)
        double value = MathUtils.clamp(aValue, 0, getScrollMax());
        value = Math.round(value);
        if (MathUtils.equals(value, _scroll)) return;

        // Set value and fire prop change
        firePropChange(Scroll_Prop, _scroll, _scroll = value);
        repaint();
    }

    /**
     * Returns the maximum value of scroll (scroll size - view size)
     */
    public double getScrollMax()
    {
        double scrollMax = getContentSize() - getScrollerSize();
        return Math.round(Math.max(scrollMax, 0));
    }

    /**
     * Returns the size of the scroller showing content.
     */
    public double getScrollerSize()  { return _scrollerSize; }

    /**
     * Sets the size of the scroller showing content.
     */
    public void setScrollerSize(double aValue)
    {
        if (MathUtils.equals(aValue, _scrollerSize)) return;
        _scrollerSize = aValue;
        repaint();
    }

    /**
     * Returns the size of the content being scrolled.
     */
    public double getContentSize()  { return _contentSize; }

    /**
     * Sets the size of the content being scrolled.
     */
    public void setContentSize(double aValue)
    {
        if (MathUtils.equals(aValue, _contentSize)) return;
        _contentSize = aValue;
        repaint();
    }

    /**
     * Returns the ratio of current scroll to maximum scroll (0-1).
     */
    public double getScrollRatio()
    {
        double scrollMax = getScrollMax();
        return scrollMax > 0 ? _scroll / scrollMax : 0;
    }

    /**
     * Sets the ratio of current scroll to maximum scroll (0-1).
     */
    public void setScrollRatio(double aValue)
    {
        double scroll = aValue * getScrollMax();
        setScroll(scroll);
    }

    /**
     * Returns the ratio of ScrollerSize to ContentSize.
     */
    private double getScrollerToContentSizeRatio()
    {
        double scrollerSize = getScrollerSize();
        double contentSize = getContentSize();
        return contentSize > 0 ? scrollerSize / contentSize : 1;
    }

    /**
     * Returns the thumb rect.
     */
    public Rect getThumbBounds()
    {
        boolean isHoriz = isHorizontal(), isVert = !isHoriz;
        double scrollRatio = getScrollRatio();
        double sizeRatio = getScrollerToContentSizeRatio();
        double viewW = getWidth() - BORDER_INSET_WIDTH;
        double viewH = getHeight() - BORDER_INSET_WIDTH;
        double thumbW = isHoriz ? Math.max(Math.round(sizeRatio * viewW), 20) : viewW;
        double thumbH = isVert ? Math.max(Math.round(sizeRatio * viewH), 20) : viewH;
        double thumbX = BORDER_INSET + (isHoriz ? Math.round(scrollRatio * (viewW - thumbW)) : 0);
        double thumbY = BORDER_INSET + (isVert ? Math.round(scrollRatio * (viewH - thumbH)) : 0);
        return new Rect(thumbX, thumbY, thumbW, thumbH);
    }

    /**
     * Returns the thumb size (along primary axis).
     */
    private double getThumbSize()
    {
        boolean isHoriz = isHorizontal();
        double sizeRatio = getScrollerToContentSizeRatio();
        double areaSize = (isHoriz ? getWidth() : getHeight()) - BORDER_INSET_WIDTH;
        return Math.max(Math.round(sizeRatio * areaSize), 20);
    }

    /**
     * Returns the resulting ScrollRatio for the given point.
     */
    private double getScrollRatioAtOffset(double anOffset)
    {
        boolean isHoriz = isHorizontal();
        double thumbSize = getThumbSize();
        double areaSize = (isHoriz ? getWidth() : getHeight()) - BORDER_INSET_WIDTH;
        return (anOffset - thumbSize / 2) / (areaSize - thumbSize);
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        boolean isHorizontal = isHorizontal();

        // Handle MouseEnter
        if (anEvent.isMouseEnter()) {
            _targeted = true;
            repaint();
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit())  {
            _targeted = false;
            repaint();
        }

        // Handle MousePress
        else if (anEvent.isMousePress())  {
            double mouseX = anEvent.getX();
            double mouseY = anEvent.getY();
            Rect thumbBounds = getThumbBounds();
            _pressed = true;
            _dv = isHorizontal ? mouseX - thumbBounds.getMidX() : mouseY - thumbBounds.getMidY();
            if (!thumbBounds.contains(mouseX, mouseY)) {
                double offset = isHorizontal ? mouseX : mouseY;
                double scrollRatioAtOffset = getScrollRatioAtOffset(offset);
                setScrollRatio(scrollRatioAtOffset);
                _dv = 0;
            }
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease())  {
            _pressed = false;
            repaint();
        }

        // Handle MouseDragged
        else if (anEvent.isMouseDrag()) {
            double offset = (isHorizontal ? anEvent.getX() : anEvent.getY()) - _dv;
            double scrollRatioAtOffset = getScrollRatioAtOffset(offset);
            setScrollRatio(scrollRatioAtOffset);
        }

        // Handle scroll
        else if (anEvent.isScroll()) {
            double units = isHorizontal ? anEvent.getScrollX() * 4 : anEvent.getScrollY() * 4;
            double scrollerSize = getScrollerSize();
            double thumbSize = getThumbSize();
            double contentSize = scrollerSize * scrollerSize / thumbSize;
            double dv = contentSize - scrollerSize;
            if (dv > 0 && Math.abs(units) > 0) {
                double newScrollRatio = getScrollRatio() + units / dv;
                setScrollRatio(newScrollRatio);
            }
            else return;
        }

        // Consume event
        anEvent.consume();
    }

    /**
     * Paints the thumb.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get orientation, size
        boolean isHorizontal = isHorizontal(), isVertical = !isHorizontal;
        double viewW = getWidth();
        double viewH = getHeight();

        // Paint back: Paint background gradient and outer ring
        aPntr.setPaint(isHorizontal ? BACK_PAINT_H : BACK_PAINT_V);
        aPntr.setOpacity(.1);
        Shape boundsShape = getBoundsShape();
        if (boundsShape instanceof RoundRect)
            boundsShape = ((RoundRect) boundsShape).copyForPosition(isHorizontal ? Pos.BOTTOM_CENTER : Pos.CENTER_RIGHT);
        aPntr.fill(boundsShape);
        aPntr.setColor(BACK_BORDER_COLOR);
        aPntr.draw(boundsShape);
        aPntr.setOpacity(1);

        // If too small to draw thumb or thumb is larger than view, just return
        if (isHorizontal && viewW < 20 || isVertical && viewH < 20)
            return;
        if (getScrollerToContentSizeRatio() >= 1)
            return;

        // Paint thumb
        paintThumb(aPntr);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    private void paintThumb(Painter aPntr)
    {
        // Set thumb color
        Color thumbColor = THUMB_COLOR;
        if (_pressed || _targeted)
            thumbColor = THUMB_COLOR_OVER;
        aPntr.setPaint(thumbColor);

        // Paint thumb shape
        Rect thumbBounds = getThumbBounds();
        double borderRadius = 3;
        if (isHorizontal() && getHeight() >= 14 || isVertical() && getWidth() >= 14) {
            if (isHorizontal()) { thumbBounds.x += 2; thumbBounds.width -= 2; }
            else { thumbBounds.y += 2; thumbBounds.height -= 2; }
            borderRadius = 4;
        }
        RoundRect thumbRect = new RoundRect(thumbBounds, borderRadius);
        aPntr.fill(thumbRect);
    }

    // Outer ring and outer lighted ring
    private static Color THUMB_COLOR = Color.get("#C2");
    private static Color THUMB_COLOR_OVER = Color.get("#7F");
    private static Color BACK_BORDER_COLOR = Color.get("#d8d8d8");

    // ScrollBar background gradient (light gray top to dark gray bottom)
    private static Color _b1 = Color.get("#e6e6e6"), _b2 = Color.get("#f1f1f1");
    private static Stop[] _backStops = { new Stop(0, _b1), new Stop(.5,_b2), new Stop(1, _b1) };
    private static GradientPaint BACK_PAINT_H = new GradientPaint(.5,0,.5,1, _backStops);
    private static GradientPaint BACK_PAINT_V = new GradientPaint(0,.5,1,.5, _backStops);
}