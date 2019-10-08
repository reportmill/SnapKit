/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A class to paint standard button backgrounds.
 */
public class ButtonArea {
    
    // The location
    double        _x, _y;

    // The size
    double        _w, _h;
    
    // The button fill
    Paint         _fill = FILL_NORMAL_BUTTON;
    
    // The rounding radius
    double        _rad = 4;
    
    // The position of the button when in a group (determines corner rendering).
    Pos           _pos;
    
    // The button state
    int           _state = Button.BUTTON_NORMAL;
    
    // Outer ring and outer lighted ring
    static Color ring2 = Color.get("#a6a6a6"), _c6 = Color.get("#ffffffBB");
    static Color _over = Color.get("#FFFFFF50"), _prsd = Color.get("#0000001A");
    
    // Button background gradient (light gray top to dark gray bottom)
    static Color _c1 = Color.get("#e8e8e8"), _c2 = Color.get("#d3d3d3");
    
    // Button inner ring gradient (light gray top to dark gray bottom)
    static Color _c3 = Color.get("#fbfbfb"), _c4 = Color.get("#dbdbdb");
    static GradientPaint ring1 = new GradientPaint(.5, 0, .5, 1, GradientPaint.getStops(0,_c3,1,_c4));
    
    // ProgressBar fill
    static Color _pb0 = Color.get("#efefef"), _pb1 = Color.get("#fefefe");
    static Color _pb2 = Color.get("#f7f7f7"), _pb3 = Color.get("#e9e9e9");
    static GradientPaint.Stop _pbstops[] = GradientPaint.getStops(0,_pb0,.33,_pb1,.66,_pb2,1,_pb3);

    // Constants for fill
    public static Paint FILL_NORMAL_BUTTON = new GradientPaint(.5, 0, .5, 1, GradientPaint.getStops(0,_c1,1,_c2));
    public static Paint FILL_PROGRESS_BAR = new GradientPaint(.5, 0, .5, 1, _pbstops);
    
/**
 * Returns the width.
 */
public double getWidth()  { return _w; }

/**
 * Sets the width.
 */
public void setWidth(double aWidth)  { _w = aWidth; }

/**
 * Returns the height.
 */
public double getHeight()  { return _h; }

/**
 * Sets the height.
 */
public void setHeight(double aHeight)  { _h = aHeight; }

/**
 * Returns the fill.
 */
public Paint getFill()  { return _fill; }

/**
 * Sets the fill.
 */
public void setFill(Paint aPaint)  { _fill = aPaint; }

/**
 * Returns the button state.
 */
public int getState()  { return _state; }

/**
 * Sets the button state.
 */
public void setState(int aState)  { _state = aState; }

/**
 * Returns the position of the button when in a group (determines corner rendering).
 */
public Pos getPosition()  { return _pos; }

/**
 * Sets the position of the button when in a group (determines corner rendering).
 */
public void setPosition(Pos aPos)  { _pos = aPos; }

/**
 * Returns the radius of the round.
 */
public double getRadius()  { return _rad; }

/**
 * Sets the radius of the round.
 */
public void setRadius(double aValue)  { _rad = aValue;  }

/**
 * Sets ButtonArea attributes from a button.
 */
public void configureFromButton(ButtonBase aButton)
{
    // Basic attrs
    setWidth(aButton.getWidth());
    setHeight(aButton.getHeight());
    setRadius(aButton.getRadius());
    setPosition(aButton.getPosition());
    
    // Get/set state
    boolean pressed = aButton.isPressed(), targeted = aButton.isTargeted();
    int state = pressed? Painter.BUTTON_PRESSED : targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
    setState(state);
    
    // Set fill
    Paint bfill = aButton.getButtonFill();
    if(bfill!=null)
        setFill(bfill);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void paint(Painter aPntr)
{
    // Get shape and paint fill
    RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad).copyForPosition(_pos);
    aPntr.setPaint(_fill); aPntr.fill(rect);
    
    // Draw rings: (1) Outer-bottom: light gray, (2) inner: light gray gradient, (3) outer: gray
    rect.setRect(_x+.5,_y+.5,_w-1,_h); aPntr.setColor(_c6); aPntr.draw(rect);
    rect.setRect(_x+1.5,_y+1.5,_w-3,_h-4); aPntr.setPaint(ring1); aPntr.draw(rect);
    rect.setRect(_x+.5,_y+.5,_w-1,_h-1); aPntr.setPaint(ring2); aPntr.draw(rect);
    
    // Handle BUTTON_OVER
    if(_state==Button.BUTTON_OVER) {
        aPntr.setPaint(_over);
        rect.setRect(_x,_y,_w,_h);
        aPntr.fill(rect);
    }
    
    // Handle BUTTON_PRESSED
    else if(_state==Button.BUTTON_PRESSED && _fill==FILL_NORMAL_BUTTON) {
        aPntr.setPaint(_prsd);
        rect.setRect(_x,_y,_w,_h);
        aPntr.fill(rect);
    }
}

}