/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.*;
import snap.gfx.*;

/**
 * A class to paint standard button backgrounds.
 */
public class ButtonArea {
    
    // The button associated with this area
    private ButtonBase  _button;
    
    // The location
    protected double  _x, _y;

    // The size
    protected double  _w, _h;
    
    // The rounding radius
    protected double  _rad = 4;
    
    // The position of the button when in a group (determines corner rendering).
    protected Pos  _pos;
    
    // The button state with regard to mouse (mouse over, mouse pressed)
    protected int  _state = Button.BUTTON_NORMAL;
    
    // Whether button is selected (toggle button only)
    private boolean  _selected;
    
    // The button shape
    private RoundRect  _rect = new RoundRect();
    
    // The center shape (RadioButton)
    private Shape  _radioShape;
    
    // Button states
    public static final int BUTTON_NORMAL = ButtonBase.BUTTON_NORMAL;
    public static final int BUTTON_OVER = ButtonBase.BUTTON_OVER;
    public static final int BUTTON_PRESSED = ButtonBase.BUTTON_PRESSED;

    // Button background fill (gradient, light gray top to dark gray bottom)
    private static Color _bfc1 = Color.get("#e8e8e8");
    private static Color _bfc2 = Color.get("#d3d3d3");
    private static GradientPaint.Stop[]  _bfillStops = GradientPaint.getStops(0, _bfc1, 1, _bfc2);
    private static GradientPaint BUTTON_FILL = new GradientPaint(.5, 0, .5, 1, _bfillStops);
    
    // Button inner ring paint (gradient, light gray top to dark gray bottom)
    private static Color _irc1 = Color.get("#fbfbfb");
    private static Color _irc2 = Color.get("#dbdbdb");
    private static GradientPaint.Stop[]  _ring1Stops = GradientPaint.getStops(0, _irc1, 1, _irc2);
    public static GradientPaint INNER_RING_PAINT = new GradientPaint(.5, 0, .5, 1, _ring1Stops);
    
    // Button outer ring paint
    public static Color OUTER_RING_PAINT = Color.get("#a6a6a6");
    
    // Button bottom highlight paint
    public static Color BOTTOM_HIGHLITE_PAINT = Color.get("#ffffffBB");
    
    // Button Mouse-over paint and Mouse-pressed paint
    private static Color BUTTON_MOUSE_OVER_PAINT = Color.get("#FFFFFF50");
    private static Color BUTTON_MOUSE_PRESSED_PAINT = Color.get("#0000001A");

    /**
     * Constructor.
     */
    public ButtonArea(ButtonBase aButton)
    {
        super();
        _button = aButton;

        // Handle CheckBox
        if (_button instanceof CheckBox || _button instanceof CheckBoxMenuItem) {
            setBounds(0, 0, 16, 16);
            setBorderRadius(3);
        }

        // Handle RadioButton
        else if (_button instanceof RadioButton) {
            setBounds(0, 0, 16, 16);
            setBorderRadius(8);
        }
    }

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
     * Sets the radius of the round.
     */
    public void setBorderRadius(double aValue)  { _rad = aValue;  }

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
     * Sets ButtonArea attributes from a button.
     */
    protected void updateFromView()
    {
        // Handle CheckBox, RadioButton
        if (_button instanceof CheckBox || _button instanceof RadioButton) {

            // Update ButtonArea.State and ButtonArea.Selected
            ToggleButton btn = (ToggleButton) _button;
            int state = btn.isPressed() ? BUTTON_PRESSED : btn.isTargeted() ? BUTTON_OVER : BUTTON_NORMAL;
            setState(state);
            setSelected(btn.isSelected());
        }

        // Handle CheckBoxMenuItem
        else if (_button instanceof CheckBoxMenuItem) {

            // Update ButtonArea.State and ButtonArea.Selected
            MenuItem btn = (MenuItem) _button;
            int state = btn.isPressed()? BUTTON_PRESSED : btn.isTargeted()? BUTTON_OVER : BUTTON_NORMAL;
            setState(state);
            setSelected(btn.isSelected());

            // Update x/y
            Insets ins = _button.getInsetsAll();
            double x = ins.left;
            double y = ins.top + 2 + Math.round((_button.getHeight() - ins.getHeight() - 2 - 16 - 2)/2);
            setXY(x, y);
        }

        // Handle normal button
        else if (_button != null) {

            // Basic attrs
            setSize(_button.getWidth(), _button.getHeight());
            setBorderRadius(_button.getBorderRadius());
            setPosition(_button.getPosition());
            setSelected(_button.isSelected());

            // Get/set state
            int state = _button.isPressed() ? BUTTON_PRESSED : _button.isTargeted() ? BUTTON_OVER : BUTTON_NORMAL;
            setState(state);
        }
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public void paint(Painter aPntr)
    {
        // Update Area from View
        updateFromView();

        // Get RoundRect shape for bounds, Radius and Position
        _rect.setRadius(_rad);
        if (_pos != null)
            _rect = _rect.copyForPosition(_pos);

        // Fill rect
        fillRect(aPntr, _rect, _x, _y, _w, _h, BUTTON_FILL);

        // Paint bottom highlite ring (white)
        drawRect(aPntr, _rect, _x + .5, _y + .5, _w - 1, _h, BOTTOM_HIGHLITE_PAINT);

        // Paint inner ring (light gray gradient)
        drawRect(aPntr, _rect, _x + 1.5, _y + 1.5, _w - 3, _h - 4, INNER_RING_PAINT);

        // Paint outer ring (gray)
        drawRect(aPntr, _rect, _x + .5, _y + .5, _w - 1, _h - 1, OUTER_RING_PAINT);

        // Handle BUTTON_OVER
        if (_state == BUTTON_OVER)
            fillRect(aPntr, _rect, _x, _y, _w, _h, BUTTON_MOUSE_OVER_PAINT);

        // Handle BUTTON_PRESSED
        else if (_state == BUTTON_PRESSED)
            fillRect(aPntr, _rect, _x, _y, _w, _h, BUTTON_MOUSE_PRESSED_PAINT);

        // Handle Selected
        if (isSelected())
            paintSelected(aPntr);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public void paintSelected(Painter aPntr)
    {
        // Handle CheckBox
        if (_button instanceof CheckBox || _button instanceof CheckBoxMenuItem) {
            Stroke str = aPntr.getStroke();
            double x = getX();
            double y = getY();
            aPntr.setStroke(Stroke.Stroke2);
            aPntr.drawLineWithPaint(x + 5, y + 5, x + 11, y + 11, Color.BLACK);
            aPntr.drawLine(x + 11, y + 5, x + 5, y + 11);
            aPntr.setStroke(str);
        }

        // Handle RadioButton
        else if (_button instanceof RadioButton) {
            if (_radioShape == null)
                _radioShape = new Ellipse(3, 3, 10, 10);
            aPntr.fillWithPaint(_radioShape, Color.DARKGRAY);
        }

        // Handle other
        else fillRect(aPntr, _rect, _x, _y, _w, _h, BUTTON_MOUSE_PRESSED_PAINT);
    }

    /**
     * Convenience to draw rect shape in bounds with color.
     */
    public static void drawRect(Painter aPntr, RectBase aRect, double aX, double aY, double aW, double aH, Paint aPnt)
    {
        aRect.setRect(aX, aY, aW, aH);
        aPntr.drawWithPaint(aRect, aPnt);
    }

    /**
     * Convenience to draw rect shape in bounds with color.
     */
    public static void fillRect(Painter aPntr, RectBase aRect, double aX, double aY, double aW, double aH, Paint aPnt)
    {
        aRect.setRect(aX, aY, aW, aH);
        aPntr.fillWithPaint(aRect, aPnt);
    }
}