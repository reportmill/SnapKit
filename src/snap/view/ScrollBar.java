/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Rect;
import snap.geom.RoundRect;
import snap.gfx.*;
import snap.gfx.GradientPaint.Stop;
import snap.util.MathUtils;

/**
 * A ScrollBar for ScrollView.
 */
public class ScrollBar extends View {

    // The offset into ScrollSize
    private double  _scroll;
    
    // The size of the viewable portion of ScrollSize
    private double  _viewSize;
    
    // The size of the content being scrolled
    private double  _scrollSize;
    
    // Whether button is pressed
    private boolean  _pressed;
    
    // Whether button is under mouse
    private boolean  _targeted;
    
    // The delta between last mouse press and value
    private double  _dv;
    
    // Constants for properties
    public static final String Scroll_Prop = "Scroll";
    public static final String ScrollSize_Prop = "ScrollSize";
    
    /**
     * Creates a new ScrollBar.
     */
    public ScrollBar()  { enableEvents(MouseEvents); enableEvents(Scroll); }

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
        double value = MathUtils.clamp(aValue, 0, getScrollLimit());
        value = Math.round(value);
        if (MathUtils.equals(value, _scroll)) return;

        // Set value and fire prop change
        firePropChange(Scroll_Prop, _scroll, _scroll=value);
        repaint();
    }

    /**
     * Returns the scroll limit.
     */
    public double getScrollLimit()
    {
        double val = getScrollSize() - getViewSize();
        return Math.round(Math.max(val, 0));
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
        firePropChange(Scroll_Prop, _viewSize, _viewSize=aValue);
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
        firePropChange(ScrollSize_Prop, _scrollSize, _scrollSize=aValue);
        repaint();
    }

    /**
     * Returns the ratio of Scroll to ScrollLimit (0-1).
     */
    public double getScrollRatio()
    {
        double smax = getScrollLimit();
        return smax>0 ? _scroll/smax : 0;
    }

    /**
     * Sets the ratio of Scroll to ScrollLimit (0-1).
     */
    public void setScrollRatio(double aValue)
    {
        double val = aValue*getScrollLimit();
        setScroll(val);
    }

    /**
     * Returns the ratio of ViewSize to ScrollSize.
     */
    public double getSizeRatio()
    {
        double vsize = getViewSize(), ssize = getScrollSize();
        return ssize>0 ? vsize/ssize : 1;
    }

    /**
     * Returns the thumb rect.
     */
    public Rect getThumbBounds()
    {
        boolean hor = isHorizontal(), ver = !hor;
        double scrollRatio = getScrollRatio();
        double sizeRatio = getSizeRatio();
        double w = getWidth() - 4;
        double h = getHeight() - 4;
        double tw = hor ? Math.max(Math.round(sizeRatio*w),20) : w;
        double th = ver ? Math.max(Math.round(sizeRatio*h),20) : h;
        double tx = hor ? Math.round(scrollRatio*(w-tw)) + 2 : 2;
        double ty = ver ? Math.round(scrollRatio*(h-th)) + 2 : 2;
        return new Rect(tx, ty, tw, th);
    }

    /**
     * Returns the thumb size (along primary axis).
     */
    private double getThumbSize()
    {
        boolean hor = isHorizontal(), ver = !hor;
        double ratio = getSizeRatio();
        double size = hor ? (getWidth() - 4) : (getHeight() - 4);
        return Math.max(Math.round(ratio*size), 20);
    }

    /**
     * Returns the resulting ScrollRatio for the given point.
     */
    private double getScrollRatio(double aPnt)
    {
        boolean hor = isHorizontal();
        double tsize = getThumbSize();
        double size = hor ? getWidth() : getHeight(); size -= 4;
        return (aPnt-tsize/2)/(size-tsize);
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        boolean hor = isHorizontal();

        // Handle MouseEnter
        if (anEvent.isMouseEnter()) { _targeted = true; repaint(); }

        // Handle MouseExit
        else if (anEvent.isMouseExit())  { _targeted = false; repaint(); }

        // Handle MousePress
        else if (anEvent.isMousePress())  {
            double mx = anEvent.getX();
            double my = anEvent.getY();
            Rect tbnds = getThumbBounds();
            _pressed = true;
            _dv = hor ? mx - tbnds.getMidX() : my - tbnds.getMidY();
            if (!tbnds.contains(mx,my)) {
                setScrollRatio(getScrollRatio(hor ? mx : my));
                _dv = 0;
            }
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease())  {
            _pressed = false; repaint();
        }

        // Handle MouseDragged
        else if (anEvent.isMouseDrag()) {
            double mv = (hor ? anEvent.getX() : anEvent.getY()) - _dv;
            double val = getScrollRatio(mv);
            setScrollRatio(val);
        }

        // Handle scroll
        else if (anEvent.isScroll()) {
            double units = hor ? anEvent.getScrollX()*4 : anEvent.getScrollY()*4;
            double size = hor ? getWidth() : getHeight();
            double tsize = getThumbSize();
            double csize = size*size/tsize;
            double dv = csize - size;
            if (dv>0 && Math.abs(units)>0)
                setScrollRatio(getScrollRatio() + units/dv);
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
        boolean hor = isHorizontal(), ver = !hor;
        double w = getWidth(), h = getHeight();

        // Paint back (just return if too small to draw thumb)
        paintBack(aPntr, 0, 0, w, h, hor);
        if (hor && w<20 || ver && h<20) return;

        // Get thumb bounds and paint thumb
        Rect tbnds = getThumbBounds(); if (getSizeRatio()>=1) return;
        int state = _pressed ? Button.BUTTON_PRESSED : _targeted ? Button.BUTTON_OVER : Button.BUTTON_NORMAL;
        paintThumb(aPntr, tbnds.x, tbnds.y, tbnds.width, tbnds.height, hor, state);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public static void paintBack(Painter aPntr, double x, double y, double w, double h, boolean isHor)
    {
        // Paint background gradient
        Rect rect = new Rect(x,y,w,h); aPntr.setPaint(isHor ? _backPntH : _backPntV); aPntr.fill(rect);

        // Paint outer ring
        rect.setRect(x+.5,y+.5,w-1,h-1); aPntr.setColor(_backRing); aPntr.draw(rect);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public static void paintThumb(Painter aPntr, double x, double y, double w, double h, boolean isHor, int aState)
    {
        // Paint background gradient
        RoundRect rect = new RoundRect(x,y,w,h,3); aPntr.setPaint(isHor ? _thumbPntH : _thumbPntV); aPntr.fill(rect);

        // Paint out bottom ring light gray
        rect.setRect(x+.5,y+.5,w-1,h); aPntr.setColor(_c6); aPntr.draw(rect);

        // Paint inner ring light gray
        rect.setRect(x+1.5,y+1.5,w-3,h-4); aPntr.setPaint(isHor ? _thumbPntH2 : _thumbPntV2); aPntr.draw(rect);

        // Paint outer ring
        rect.setRect(x+.5,y+.5,w-1,h-1); aPntr.setColor(_c0); aPntr.draw(rect);

        // Handle BUTTON_OVER, BUTTON_PRESSED
        if (aState==Button.BUTTON_OVER) { aPntr.setPaint(_over); rect.setRect(x,y,w,h); aPntr.fill(rect); }
        else if (aState==Button.BUTTON_PRESSED) { aPntr.setPaint(_prsd); rect.setRect(x,y,w,h); aPntr.fill(rect); }
    }

    // Outer ring and outer lighted ring
    static Color _c0 = Color.get("#a6a6a6"), _c6 = Color.get("#ffffffBB");
    static Color _backRing = Color.get("#d8d8d8");
    static Color _over = Color.get("#FFFFFF50"), _prsd = Color.get("#00000004");

    // ScrollBar background gradient (light gray top to dark gray bottom)
    static Color _b1 = Color.get("#e6e6e6"), _b2 = Color.get("#f1f1f1");
    static Stop _backStops[] = { new Stop(0,_b1), new Stop(.5,_b2), new Stop(1,_b1) };
    static GradientPaint _backPntH = new GradientPaint(.5,0,.5,1,_backStops);
    static GradientPaint _backPntV = new GradientPaint(0,.5,1,.5,_backStops);

    // Thumb button gradient (light gray top to dark gray bottom)
    static Color _t1 = Color.get("#ebebeb"), _t2 = Color.get("#d6d6d6");
    static Stop _thumbStops[] = { new Stop(0,_t1), new Stop(1,_t2) };
    static GradientPaint _thumbPntH = new GradientPaint(.5,0,.5,1,_thumbStops);
    static GradientPaint _thumbPntV = new GradientPaint(0,.5,1,.5,_thumbStops);

    // Button inner ring gradient (light gray top to dark gray bottom)
    static Color _t3 = Color.get("#fbfbfb"), _t4 = Color.get("#dbdbdb");
    static Stop _thumbStops2[] = { new Stop(0,_t3), new Stop(1,_t4) };
    static GradientPaint _thumbPntH2 = new GradientPaint(.5,0,.5,1,_thumbStops2);
    static GradientPaint _thumbPntV2 = new GradientPaint(0,.5,1,.5,_thumbStops2);
}