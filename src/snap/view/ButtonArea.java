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
    
    // The rounding radius
    double        _rad = 4;
    
    // The position of the button when in a group (determines corner rendering).
    Pos           _pos;
    
    // The button state with regard to mouse (mouse over, mouse pressed)
    int           _state = Button.BUTTON_NORMAL;
    
    // Whether button is selected (toggle button only)
    boolean       _selected;
    
    // The button fill
    Paint         _fill = BUTTON_FILL;
    
    // Button states
    public static final int BUTTON_NORMAL = ButtonBase.BUTTON_NORMAL;
    public static final int BUTTON_OVER = ButtonBase.BUTTON_OVER;
    public static final int BUTTON_PRESSED = ButtonBase.BUTTON_PRESSED;

    // Button background fill (gradient, light gray top to dark gray bottom)
    private static Color _bfc1 = Color.get("#e8e8e8");
    private static Color _bfc2 = Color.get("#d3d3d3");
    private static GradientPaint.Stop _bfillStops[] = GradientPaint.getStops(0, _bfc1, 1, _bfc2);
    private static GradientPaint BUTTON_FILL = new GradientPaint(.5, 0, .5, 1, _bfillStops);
    
    // Button inner ring paint (gradient, light gray top to dark gray bottom)
    private static Color _irc1 = Color.get("#fbfbfb");
    private static Color _irc2 = Color.get("#dbdbdb");
    private static GradientPaint.Stop _ring1Stops[] = GradientPaint.getStops(0, _irc1, 1, _irc2);
    private static GradientPaint INNER_RING_PAINT = new GradientPaint(.5, 0, .5, 1, _ring1Stops);
    
    // Button outer ring paint
    private static Color OUTER_RING_PAINT = Color.get("#a6a6a6");
    
    // Button bottom highlight paint
    private static Color BOTTOM_HIGHLITE_PAINT = Color.get("#ffffffBB");
    
    // Button Mouse-over paint and Mouse-pressed paint
    private static Color BUTTON_MOUSE_OVER_PAINT = Color.get("#FFFFFF50");
    private static Color BUTTON_MOUSE_PRESSED_PAINT = Color.get("#0000001A");
    
/**
 * Returns the X value.
 */
public double getX()  { return _x; }

/**
 * Sets the X value.
 */
public void setX(double aX)  { _x = aX; }

/**
 * Returns the Y value.
 */
public double getY()  { return _y; }

/**
 * Sets the Y value.
 */
public void setY(double aY)  { _y = aY; }

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
 * Sets X/Y.
 */
public void setXY(double aX, double aY)
{
    setX(aX); setY(aY);
}

/**
 * Sets size.
 */
public void setSize(double aW, double aH)
{
    setWidth(aW); setHeight(aH);
}

/**
 * Sets the bounds.
 */
public void setBounds(double aX, double aY, double aW, double aH)
{
    setX(aX); setY(aY); setWidth(aW); setHeight(aH);
}

/**
 * Returns the radius of the round.
 */
public double getRadius()  { return _rad; }

/**
 * Sets the radius of the round.
 */
public void setRadius(double aValue)  { _rad = aValue;  }

/**
 * Returns the position of the button when in a group (determines corner rendering).
 */
public Pos getPosition()  { return _pos; }

/**
 * Sets the position of the button when in a group (determines corner rendering).
 */
public void setPosition(Pos aPos)  { _pos = aPos; }

/**
 * Returns the button state.
 */
public int getState()  { return _state; }

/**
 * Sets the button state.
 */
public void setState(int aState)  { _state = aState; }

/**
 * Returns whether button is selected (toggle button only).
 */
public boolean isSelected()  { return _selected; }

/**
 * Set whether button is selected (toggle button only).
 */
public void setSelected(boolean aValue)  { _selected = aValue; }

/**
 * Returns the fill.
 */
public Paint getFill()  { return _fill; }

/**
 * Sets the fill.
 */
public void setFill(Paint aPaint)  { _fill = aPaint; }

/**
 * Sets ButtonArea attributes from a button.
 */
public void configureFromButton(ButtonBase aButton)
{
    // Basic attrs
    setSize(aButton.getWidth(), aButton.getHeight());
    setRadius(aButton.getRadius());
    setPosition(aButton.getPosition());
    
    // Get/set state
    boolean pressed = aButton.isPressed();
    boolean targeted = aButton.isTargeted();
    int state = pressed? BUTTON_PRESSED : targeted? BUTTON_OVER : BUTTON_NORMAL;
    setState(state);
    
    // Handle Selected (ToggleButton only)
    boolean isSel = aButton instanceof ToggleButton && ((ToggleButton)aButton).isSelected();
    setSelected(isSel);
    
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
    // Get RoundRect shape for bounds, Radius and Position
    RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad).copyForPosition(_pos);
    
    // Fill rect
    fillRect(aPntr, rect, _x, _y, _w, _h, _fill);
    
    // Paint bottom highlite ring (white)
    drawRect(aPntr, rect, _x+.5, _y+.5, _w-1, _h, BOTTOM_HIGHLITE_PAINT);
    
    // Paint inner ring (light gray gradient)
    drawRect(aPntr, rect, _x+1.5, _y+1.5, _w-3, _h-4, INNER_RING_PAINT);
    
    // Paint outer ring (gray)
    drawRect(aPntr, rect, _x+.5, _y+.5, _w-1, _h-1, OUTER_RING_PAINT);
    
    // Handle BUTTON_OVER
    if(_state==BUTTON_OVER)
        fillRect(aPntr, rect, _x, _y, _w, _h, BUTTON_MOUSE_OVER_PAINT);
    
    // Handle BUTTON_PRESSED
    else if(_state==BUTTON_PRESSED && _fill==BUTTON_FILL)
        fillRect(aPntr, rect, _x, _y, _w, _h, BUTTON_MOUSE_PRESSED_PAINT);
}

/**
 * Convenience to draw rect shape in bounds with color.
 */
public static final void drawRect(Painter aPntr, RectBase aRect, double aX, double aY, double aW, double aH, Paint aPnt)
{
    aRect.setRect(aX, aY, aW, aH);
    aPntr.drawWithPaint(aRect, aPnt);
}

/**
 * Convenience to draw rect shape in bounds with color.
 */
public static final void fillRect(Painter aPntr, RectBase aRect, double aX, double aY, double aW, double aH, Paint aPnt)
{
    aRect.setRect(aX, aY, aW, aH);
    aPntr.fillWithPaint(aRect, aPnt);
}

}