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

    // The offset into ScrollSize
    private double _scroll;
    
    // The size of the viewable portion of ScrollSize
    private double _viewSize;
    
    // The size of the content being scrolled
    private double _scrollSize;
    
    // Whether button is pressed
    private boolean _pressed;
    
    // Whether button is under mouse
    private boolean _targeted;
    
    // The delta between last mouse press and value
    private double _dv;
    
    // Constants for properties
    public static final String Scroll_Prop = "Scroll";
    public static final String ScrollSize_Prop = "ScrollSize";

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
     * Returns the offset into ScrollSize.
     */
    public double getScroll()  { return _scroll; }

    /**
     * Sets the offset into ScrollSize.
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
        double scrollMax = getScrollSize() - getViewSize();
        return Math.round(Math.max(scrollMax, 0));
    }

    /**
     * Returns the size of the viewable portion of ScrollSize.
     */
    public double getViewSize()  { return _viewSize; }

    /**
     * Sets the size of the viewable portion of ScrollSize.
     */
    public void setViewSize(double aValue)
    {
        // If already set, just return
        if (MathUtils.equals(aValue, _viewSize)) return;

        // Set value and fire prop change
        firePropChange(Scroll_Prop, _viewSize, _viewSize = aValue);
        repaint();
    }

    /**
     * Returns the size of the content being scrolled.
     */
    public double getScrollSize()  { return _scrollSize; }

    /**
     * Sets the size of the content being scrolled.
     */
    public void setScrollSize(double aValue)
    {
        // If already set, just return
        if (MathUtils.equals(aValue, _scrollSize)) return;

        // Set value and fire prop change
        firePropChange(ScrollSize_Prop, _scrollSize, _scrollSize = aValue);
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
     * Returns the ratio of ViewSize to ScrollSize.
     */
    public double getSizeRatio()
    {
        double viewSize = getViewSize();
        double scrollSize = getScrollSize();
        return scrollSize > 0 ? viewSize / scrollSize : 1;
    }

    /**
     * Returns the thumb rect.
     */
    public Rect getThumbBounds()
    {
        boolean isHoriz = isHorizontal(), isVert = !isHoriz;
        double scrollRatio = getScrollRatio();
        double sizeRatio = getSizeRatio();
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
        double sizeRatio = getSizeRatio();
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
            double viewSize = isHorizontal ? getWidth() : getHeight();
            double thumbSize = getThumbSize();
            double contentSize = viewSize * viewSize / thumbSize;
            double dv = contentSize - viewSize;
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
        aPntr.setPaint(isHorizontal ? _backPntH : _backPntV);
        Shape boundsShape = getBoundsShape();
        if (boundsShape instanceof RoundRect)
            boundsShape = ((RoundRect) boundsShape).copyForPosition(isHorizontal ? Pos.BOTTOM_CENTER : Pos.CENTER_RIGHT);
        aPntr.fill(boundsShape);
        aPntr.setColor(_backRing);
        aPntr.draw(boundsShape);

        // If too small to draw thumb or thumb is larger than view, just return
        if (isHorizontal && viewW < 20 || isVertical && viewH < 20)
            return;
        if (getSizeRatio() >= 1)
            return;

        // Paint thumb
        paintThumb(aPntr);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    private void paintThumb(Painter aPntr)
    {
        Rect thumbBounds = getThumbBounds();
        double thumbX = thumbBounds.x;
        double thumbY = thumbBounds.y;
        double thumbW = thumbBounds.width;
        double thumbH = thumbBounds.height;
        boolean isHorizontal = isHorizontal();
        int aState = _pressed ? Button.BUTTON_PRESSED : _targeted ? Button.BUTTON_OVER : Button.BUTTON_NORMAL;

        // Paint background gradient
        double borderRadius = 3;
        RoundRect thumbRect = new RoundRect(thumbX, thumbY, thumbW, thumbH, borderRadius);
        aPntr.setPaint(isHorizontal ? _thumbPntH : _thumbPntV);
        aPntr.fill(thumbRect);

        // Paint out bottom ring light gray
        thumbRect.setRect(thumbX + .5, thumbY + .5, thumbW - 1, thumbH);
        aPntr.setColor(_c6);
        aPntr.draw(thumbRect);

        // Paint inner ring light gray
        thumbRect.setRect(thumbX + 1.5, thumbY + 1.5, thumbW - 3, thumbH - 4);
        aPntr.setPaint(isHorizontal ? _thumbPntH2 : _thumbPntV2);
        aPntr.draw(thumbRect);

        // Paint outer ring
        thumbRect.setRect(thumbX + .5, thumbY + .5, thumbW - 1, thumbH - 1);
        aPntr.setColor(_c0);
        aPntr.draw(thumbRect);

        // Handle BUTTON_OVER, BUTTON_PRESSED
        if (aState == Button.BUTTON_OVER) {
            aPntr.setPaint(_over);
            thumbRect.setRect(thumbX, thumbY, thumbW, thumbH);
            aPntr.fill(thumbRect);
        }
        else if (aState == Button.BUTTON_PRESSED) {
            aPntr.setPaint(_prsd);
            thumbRect.setRect(thumbX, thumbY, thumbW, thumbH);
            aPntr.fill(thumbRect);
        }
    }

    // Outer ring and outer lighted ring
    static Color _c0 = Color.get("#a6a6a6"), _c6 = Color.get("#ffffffBB");
    static Color _backRing = Color.get("#d8d8d8");
    static Color _over = Color.get("#FFFFFF50"), _prsd = Color.get("#00000004");

    // ScrollBar background gradient (light gray top to dark gray bottom)
    static Color _b1 = Color.get("#e6e6e6"), _b2 = Color.get("#f1f1f1");
    static Stop[] _backStops = { new Stop(0, _b1), new Stop(.5,_b2), new Stop(1, _b1) };
    static GradientPaint _backPntH = new GradientPaint(.5,0,.5,1, _backStops);
    static GradientPaint _backPntV = new GradientPaint(0,.5,1,.5, _backStops);

    // Thumb button gradient (light gray top to dark gray bottom)
    static Color _t1 = Color.get("#ebebeb"), _t2 = Color.get("#d6d6d6");
    static Stop[] _thumbStops = { new Stop(0,_t1), new Stop(1,_t2) };
    static GradientPaint _thumbPntH = new GradientPaint(.5,0,.5,1, _thumbStops);
    static GradientPaint _thumbPntV = new GradientPaint(0,.5,1,.5, _thumbStops);

    // Button inner ring gradient (light gray top to dark gray bottom)
    static Color _t3 = Color.get("#fbfbfb"), _t4 = Color.get("#dbdbdb");
    static Stop[] _thumbStops2 = { new Stop(0,_t3), new Stop(1, _t4) };
    static GradientPaint _thumbPntH2 = new GradientPaint(.5,0,.5,1, _thumbStops2);
    static GradientPaint _thumbPntV2 = new GradientPaint(0,.5,1,.5, _thumbStops2);
}