/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.gfx.GradientPaint.Stop;
import snap.util.MathUtils;

/**
 * A ScrollBar for ScrollView.
 */
public class ScrollBar extends View {

    // The scroll value (0-1)
    double         _scroll;
    
    // The ratio of the thumb to total available size
    double         _thumbRatio;
    
    // Whether button is pressed
    boolean        _pressed;
    
    // Whether button is under mouse
    boolean        _targeted;
    
    // The delta between last mouse press and value
    double         _dv;
    
    // Constants for properties
    public static final String Scroll_Prop = "Scroll";
    
/**
 * Creates a new ScrollBar.
 */
public ScrollBar()  { enableEvents(MouseEvents); enableEvents(Scroll); }

/**
 * Returns the scroll value (0-1).
 */
public double getScroll()  { return _scroll; }

/**
 * Sets the scroll value (0-1).
 */
public void setScroll(double aValue)
{
    if(aValue<0) aValue = 0; else if(aValue>1) aValue = 1;
    if(MathUtils.equals(aValue,_scroll)) return;
    firePropChange(Scroll_Prop, _scroll, _scroll=aValue);
    repaint();
}

/**
 * Returns the visible amount.
 */
public double getThumbRatio()  { return _thumbRatio; }

/**
 * Sets the thumb ratio.
 */
public void setThumbRatio(double aValue)
{
    if(aValue==_thumbRatio) return;
    firePropChange("ThumbRatio", _thumbRatio, _thumbRatio=aValue);
    repaint();
}

/**
 * Returns the thumb rect.
 */
public Rect getThumbBounds()
{
    boolean hor = isHorizontal(), ver = !hor;
    double w = getWidth() - 4, h = getHeight() - 4;
    double tw = hor? Math.max(Math.round(getThumbRatio()*w),20) : w;
    double th = ver? Math.max(Math.round(getThumbRatio()*h),20) : h;
    double tx = hor? Math.round(getScroll()*(w-tw))+2 : 2;
    double ty = ver? Math.round(getScroll()*(h-th))+2 : 2;
    return new Rect(tx,ty,tw,th);
}

/**
 * Returns the thumb size (along primary axis).
 */
public double getThumbSize()
{
    boolean hor = isHorizontal(), ver = !hor;
    double size = hor? (getWidth() - 4) : (getHeight() - 4);
    return Math.max(Math.round(getThumbRatio()*size),20);
}

/**
 * Returns the value for the given point.
 */
public double getScroll(double aPnt)
{
    boolean hor = isHorizontal();
    double tsize = getThumbSize(), size = hor? getWidth() : getHeight(); size -= 4;
    return (aPnt-tsize/2)/(size-tsize);
}

/**
 * Handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    boolean hor = isHorizontal();
    
    // Handle MouseEnter
    if(anEvent.isMouseEnter()) { _targeted = true; repaint(); }
    
    // Handle MouseExit
    else if(anEvent.isMouseExit())  { _targeted = false; repaint(); }
    
    // Handle MousePress
    else if(anEvent.isMousePress())  {
        Rect tbnds = getThumbBounds(); double mx = anEvent.getX(), my = anEvent.getY();
        _pressed = true;
        _dv = hor? mx - tbnds.getMidX() : my - tbnds.getMidY();
        if(!tbnds.contains(mx,my)) { setScroll(getScroll(hor? mx : my)); _dv = 0; }
        repaint();
    }

    // Handle MouseRelease
    else if(anEvent.isMouseRelease())  { _pressed = false; repaint(); }
    
    // Handle MouseDragged
    else if(anEvent.isMouseDrag()) {
        double mv = (hor? anEvent.getX() : anEvent.getY()) - _dv;
        double val = getScroll(mv);
        setScroll(val);
        anEvent.consume();
    }
    
    // Handle scroll
    else if(anEvent.isScroll()) {
        double units = hor? anEvent.getScrollX()*4 : anEvent.getScrollY()*4;
        double size = hor? getWidth() : getHeight(), tsize = getThumbSize(), csize = size*size/tsize;
        double dv = csize - size;
        if(dv>0 && Math.abs(units)>0) setScroll(getScroll() + units/dv);
    }
}

/**
 * Paints the thumb.
 */
protected void paintFront(Painter aPntr)
{
    double w = getWidth(), h = getHeight(); boolean hor = isHorizontal(), ver = !hor;
    paintBack(aPntr, 0, 0, w, h, hor); if(hor && w<20 || ver && h<20) return;
    Rect tbnds = getThumbBounds(); if(getThumbRatio()>=1) return;
    int state = _pressed? Painter.BUTTON_PRESSED : _targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
    paintThumb(aPntr, tbnds.x, tbnds.y, tbnds.width, tbnds.height, hor, state);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public static void paintBack(Painter aPntr, double x, double y, double w, double h, boolean isHor)
{
    // Paint background gradient and outer ring
    Rect rect = new Rect(x,y,w,h); aPntr.setPaint(isHor? _backPntH : _backPntV); aPntr.fill(rect);
    rect.setRect(x+.5,y+.5,w-1,h-1); aPntr.setColor(_backRing); aPntr.draw(rect);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public static void paintThumb(Painter aPntr, double x, double y, double w, double h, boolean isHor, int aState)
{
    // Paint background gradient
    RoundRect rect = new RoundRect(x,y,w,h,3); aPntr.setPaint(isHor? _thumbPntH : _thumbPntV); aPntr.fill(rect);
    
    // Paint out bottom ring light gray
    rect.setRect(x+.5,y+.5,w-1,h); aPntr.setColor(_c6); aPntr.draw(rect);
    
    // Paint inner ring light gray
    rect.setRect(x+1.5,y+1.5,w-3,h-4); aPntr.setPaint(isHor? _thumbPntH2 : _thumbPntV2); aPntr.draw(rect);
    
    // Paint outer ring
    rect.setRect(x+.5,y+.5,w-1,h-1); aPntr.setColor(_c0); aPntr.draw(rect);
    
    // Handle BUTTON_OVER, BUTTON_PRESSED
    if(aState==Button.BUTTON_OVER) { aPntr.setPaint(_over); rect.setRect(x,y,w,h); aPntr.fill(rect); }
    else if(aState==Button.BUTTON_PRESSED) { aPntr.setPaint(_prsd); rect.setRect(x,y,w,h); aPntr.fill(rect); }
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