package snap.view;
import snap.geom.*;
import snap.gfx.*;

/**
 * This class has methods to paint a button.
 */
public class ButtonPainter {

    // Button states
    public static final int BUTTON_NORMAL = ButtonBase.BUTTON_NORMAL;
    public static final int BUTTON_OVER = ButtonBase.BUTTON_OVER;
    public static final int BUTTON_PRESSED = ButtonBase.BUTTON_PRESSED;

    // Button Mouse-pressed paint
    private static Color BUTTON_MOUSE_PRESSED_PAINT = Color.get("#0000001A");

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public static void paintButton(Painter aPntr, ButtonBase aButton)
    {
        // Get button rect
        RoundRect buttonRect = getButtonRect(aButton);

        // Get fill color and paint fill
        Color fillColor = aButton.getFillColor();
        if (fillColor != null)
            aPntr.fillWithPaint(buttonRect, fillColor);

        // Get stroke color and paint stroke
        Border border = aButton.getBorder();
        Color strokeColor = border != null ? border.getColor() : null;
        if (strokeColor != null)
            aPntr.drawWithPaint(buttonRect, strokeColor);

        // Paint selected
        if (aButton.isSelected())
            aPntr.fillWithPaint(buttonRect, BUTTON_MOUSE_PRESSED_PAINT);
    }

    /**
     * Returns the button rect for given button.
     */
    private static RoundRect getButtonRect(ButtonBase aButton)
    {
        // Create rect
        double rectW = aButton.getWidth();
        double rectH = aButton.getHeight();
        double rectRad = aButton.getBorderRadius();
        RoundRect rect = new RoundRect(0, 0, rectW, rectH, rectRad);
        if (aButton.getPosition() != null)
            rect = rect.copyForPosition(aButton.getPosition());

        // Return
        return rect;
    }

    /**
     * Paints a default button for given button.
     */
    public static void paintDefaultButton(Painter aPntr, ButtonBase aButton)
    {
        // Get button state and rect
        int aState = aButton.isPressed() ? BUTTON_PRESSED : aButton.isTargeted() ? BUTTON_OVER : BUTTON_NORMAL;
        RoundRect buttonRect = getButtonRect(aButton);

        // Get button rect
        double buttonX = buttonRect.x;
        double buttonY = buttonRect.y;
        double buttonW = buttonRect.width;
        double buttonH = buttonRect.height;

        // Reset stroke
        aPntr.setStroke(Stroke.Stroke1);

        // Background grad
        aPntr.fillWithPaint(buttonRect, back);

        // Paint outer bottom ring lt gray
        buttonRect.setRect( buttonX + .5, buttonY + .5, buttonW - 1, buttonH);
        aPntr.drawWithPaint(buttonRect, _c6);

        // Paint inner ring light gray
        buttonRect.setRect(buttonX + 1.5,buttonY + 1.5,buttonW - 3,buttonH - 4);
        aPntr.drawWithPaint(buttonRect, ring1);

        // Paint outer ring
        buttonRect.setRect(buttonX + .5,buttonY + .5,buttonW - 1,buttonH - 1);
        aPntr.drawWithPaint(buttonRect, ring2);

        // Reset ButtonRect
        buttonRect.setRect(buttonX, buttonY, buttonW, buttonH);

        // Handle BUTTON_OVER, BUTTON_PRESSED
        if (aState == BUTTON_OVER)
            aPntr.fillWithPaint(buttonRect, _over);

        // Handle BUTTON_PRESSED
        else if (aState == BUTTON_PRESSED)
            aPntr.fillWithPaint(buttonRect, _prsd);
    }

    // Outer ring and outer lighted ring
    private static Color _c6 = Color.get("#ffffffBB");
    private static Color _over = Color.get("#FFFFFF50"), _prsd = Color.get("#0000001A");

    // Button background gradient for default buttons (light gray top to dark gray bottom)
    private static Color ring2 = Color.get("#83a6b6");
    private static Color _b1 = Color.get("#b1daed"), _b2 = Color.get("#9ec7db");
    private static GradientPaint back = new GradientPaint(.5, 0, .5, 1,GradientPaint.getStops(0, _b1, 1, _b2));

    // Button background gradient for default buttons (light gray top to dark gray bottom)
    private static Color _b3 = Color.get("#d6ecf6"), _b4 = Color.get("#a5d1e4");
    private static GradientPaint ring1 = new GradientPaint(.5, 0, .5, 1, GradientPaint.getStops(0, _b3, 1, _b4));

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public static void paintClassicButtonInShape(Painter aPntr, RectBase buttonRect, int aState)
    {
        // Get Rect
        double rectX = buttonRect.x;
        double rectY = buttonRect.y;
        double rectW = buttonRect.width;
        double rectH = buttonRect.height;

        // Fill rect
        aPntr.fillWithPaint(buttonRect, BUTTON_FILL);

        // Paint bottom highlite ring (white)
        buttonRect.setRect(rectX + .5, rectY + .5, rectW - 1, rectH);
        aPntr.drawWithPaint(buttonRect, BOTTOM_HIGHLITE_PAINT);

        // Paint inner ring (light gray gradient)
        buttonRect.setRect(rectX + 1.5, rectY + 1.5, rectW - 3, rectH - 4);
        aPntr.drawWithPaint(buttonRect, INNER_RING_PAINT);

        // Paint outer ring (gray)
        buttonRect.setRect(rectX + .5, rectY + .5, rectW - 1, rectH - 1);
        aPntr.drawWithPaint(buttonRect, OUTER_RING_PAINT);

        // Reset rect
        buttonRect.setRect(rectX, rectY, rectW, rectH);

        // Handle BUTTON_OVER
        if (aState == BUTTON_OVER)
            aPntr.fillWithPaint(buttonRect, BUTTON_MOUSE_OVER_PAINT);

        // Handle BUTTON_PRESSED
        else if (aState == BUTTON_PRESSED)
            aPntr.fillWithPaint(buttonRect, BUTTON_MOUSE_PRESSED_PAINT);
    }

    // Button background fill (gradient, light gray top to dark gray bottom)
    private static Color _bfc1 = Color.get("#f4");
    private static Color _bfc2 = Color.get("#e0");
    private static GradientPaint.Stop[]  _bfillStops = GradientPaint.getStops(0, _bfc1, 1, _bfc2);
    private static GradientPaint BUTTON_FILL = new GradientPaint(.5, 0, .5, 1, _bfillStops);

    // Button inner ring paint (gradient, light gray top to dark gray bottom)
    private static Color _irc1 = Color.get("#fbfbfb");
    private static Color _irc2 = Color.get("#dbdbdb");
    private static GradientPaint.Stop[]  _ring1Stops = GradientPaint.getStops(0, _irc1, 1, _irc2);
    private static GradientPaint INNER_RING_PAINT = new GradientPaint(.5, 0, .5, 1, _ring1Stops);

    // Button outer ring paint
    private static Color OUTER_RING_PAINT = Color.get("#a6a6a6");

    // Button bottom highlight paint
    private static Color BOTTOM_HIGHLITE_PAINT = Color.get("#ffffffBB");

    // Button Mouse-over paint and Mouse-pressed paint
    private static Color BUTTON_MOUSE_OVER_PAINT = Color.get("#FFFFFF50");
}
