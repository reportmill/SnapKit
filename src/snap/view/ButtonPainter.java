package snap.view;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Painter;
import snap.gfx.Stroke;

/**
 * This class has methods to paint a button.
 */
public abstract class ButtonPainter {

    // The center shape (RadioButton)
    private Shape  _radioShape;

    // Button states
    public static final int BUTTON_NORMAL = ButtonBase.BUTTON_NORMAL;
    public static final int BUTTON_OVER = ButtonBase.BUTTON_OVER;
    public static final int BUTTON_PRESSED = ButtonBase.BUTTON_PRESSED;

    // Button Mouse-over paint and Mouse-pressed paint
    private static Color BUTTON_MOUSE_OVER_PAINT = Color.get("#FFFFFF50");
    private static Color BUTTON_MOUSE_PRESSED_PAINT = Color.get("#0000001A");

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public void paintButton(Painter aPntr, ButtonBase aButton)
    {
        // Get button state
        int state = aButton.isPressed() ? BUTTON_PRESSED : aButton.isTargeted() ? BUTTON_OVER : BUTTON_NORMAL;
        boolean isSelected = aButton.isSelected();

        // Get button rect
        RoundRect buttonRect = getButtonRect(aButton);

        // Paint button
        paintButtonInShape(aPntr, buttonRect, state, isSelected);

        // Paint selected
        if (aButton.isSelected())
            paintButtonSelected(aPntr, aButton, buttonRect);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public abstract void paintButtonInShape(Painter aPntr, RectBase buttonRect, int aState, boolean isSelected);

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public void paintButtonSelected(Painter aPntr, ButtonBase _button, RectBase buttonRect)
    {
        // Handle CheckBox
        if (_button instanceof CheckBox || _button instanceof CheckBoxMenuItem) {
            Stroke oldStroke = aPntr.getStroke();
            int OFFSET = 5;
            int SIZE = 11;
            double x = buttonRect.x;
            double y = buttonRect.y;
            aPntr.setStroke(Stroke.Stroke2);
            aPntr.drawLineWithPaint(x + OFFSET, y + OFFSET, x + SIZE, y + SIZE, Color.BLACK);
            aPntr.drawLine(x + SIZE, y + OFFSET, x + OFFSET, y + SIZE);
            aPntr.setStroke(oldStroke);
        }

        // Handle RadioButton
        else if (_button instanceof RadioButton) {
            if (_radioShape == null)
                _radioShape = new Ellipse(3, 3, 10, 10);
            aPntr.fillWithPaint(_radioShape, Color.DARKGRAY);
        }

        // Handle other
        else aPntr.fillWithPaint(buttonRect, BUTTON_MOUSE_PRESSED_PAINT);
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
     * Paints a default button for given button.
     */
    public void paintDefaultButton(Painter aPntr, ButtonBase aButton)
    {
        // Get button state
        int state = aButton.isPressed() ? BUTTON_PRESSED : aButton.isTargeted() ? BUTTON_OVER : BUTTON_NORMAL;

        // Get button rect
        RoundRect buttonRect = getButtonRect(aButton);

        // Paint button
        paintDefaultButtonInShape(aPntr, buttonRect, state);
    }

    /**
     * Paints a default button for given button shape and state.
     */
    public void paintDefaultButtonInShape(Painter aPntr, RectBase buttonRect, int aState)
    {
        // Get rect
        double rectX = buttonRect.x;
        double rectY = buttonRect.y;
        double rectW = buttonRect.width;
        double rectH = buttonRect.height;

        // Reset stroke
        aPntr.setStroke(Stroke.Stroke1);

        // Background grad
        aPntr.fillWithPaint(buttonRect, back);

        // Paint outer bottom ring lt gray
        buttonRect.setRect( rectX + .5, rectY + .5, rectW - 1, rectH);
        aPntr.drawWithPaint(buttonRect, _c6);

        // Paint inner ring light gray
        buttonRect.setRect(rectX + 1.5,rectY + 1.5,rectW - 3,rectH - 4);
        aPntr.drawWithPaint(buttonRect, ring1);

        // Paint outer ring
        buttonRect.setRect(rectX + .5,rectY + .5,rectW - 1,rectH - 1);
        aPntr.drawWithPaint(buttonRect, ring2);

        // Reset ButtonRect
        buttonRect.setRect(rectX, rectY, rectW, rectH);

        // Handle BUTTON_OVER, BUTTON_PRESSED
        if (aState == BUTTON_OVER)
            aPntr.fillWithPaint(buttonRect, _over);

        // Handle BUTTON_PRESSED
        else if (aState == BUTTON_PRESSED)
            aPntr.fillWithPaint(buttonRect, _prsd);
    }

    /**
     * Returns the button rect.
     */
    public static RoundRect getButtonRect(ButtonBase aButton)
    {
        // Declare round rect parts
        double rectX = 0;
        double rectY = 0;
        double rectW = aButton.getWidth();
        double rectH = aButton.getHeight();
        double rectRad = aButton.getBorderRadius();
        Pos pos = aButton.getPosition();

        // Handle CheckBoxMenuItem
        if (aButton instanceof CheckBoxMenuItem) {
            double SIZE = 16;
            Insets ins = aButton.getInsetsAll();
            rectX = ins.left;
            rectY = ins.top + 2 + Math.round((rectH - ins.getHeight() - 2 - SIZE - 2) / 2);
            rectW = rectH = SIZE;
            rectRad = 3;
            pos = null;
        }

        // Handle CheckBox
        else if (aButton instanceof CheckBox) {
            double SIZE = 16;
            rectW = rectH = SIZE;
            rectRad = 3;
            pos = null;
        }

        // Handle RadioButton
        else if (aButton instanceof RadioButton) {
            double SIZE = 16;
            rectW = rectH = SIZE;
            rectRad = 8;
            pos = null;
        }

        // Create rect
        RoundRect rect = new RoundRect(rectX, rectY, rectW, rectH, rectRad);
        if (pos != null)
            rect = rect.copyForPosition(pos);

        // Return
        return rect;
    }

    /**
     * A ButtonPainter subclass to paint classic buttons.
     */
    public static class Flat extends ButtonPainter {

        // The Colors
        private Color BUTTON_COLOR;
        private Color BUTTON_OVER_COLOR;
        private Color BUTTON_PRESSED_COLOR;
        private Color BUTTON_BORDER_COLOR;
        private Color BUTTON_BORDER_PRESSED_COLOR;

        /**
         * Constructor.
         */
        public Flat(ViewTheme aTheme)
        {
            super();
            BUTTON_COLOR = aTheme.getButtonColor();
            BUTTON_OVER_COLOR = aTheme.getButtonOverColor();
            BUTTON_PRESSED_COLOR = aTheme.getButtonPressedColor();
            BUTTON_BORDER_COLOR = aTheme.getButtonBorderColor();
            BUTTON_BORDER_PRESSED_COLOR = aTheme.getButtonBorderPressedColor();
        }

        /**
         * Draws a button for the given rect with an option for pressed.
         */
        public void paintButtonInShape(Painter aPntr, RectBase buttonRect, int aState, boolean isSelected)
        {
            // Get fill color
            Color fillColor = BUTTON_COLOR;
            if (aState == BUTTON_OVER)
                fillColor = BUTTON_OVER_COLOR;
            else if (aState == BUTTON_PRESSED)
                fillColor = BUTTON_PRESSED_COLOR;
            else if (isSelected)
                fillColor = BUTTON_PRESSED_COLOR;

            // Get shape and paint fill
            aPntr.fillWithPaint(buttonRect, fillColor);

            // Get stroke color
            Color strokeColor = BUTTON_BORDER_COLOR;
            if (aState == BUTTON_OVER)
                strokeColor = BUTTON_BORDER_PRESSED_COLOR;
            else if (aState == BUTTON_PRESSED)
                strokeColor = BUTTON_BORDER_PRESSED_COLOR;

            // Draw outer ring
            aPntr.drawWithPaint(buttonRect, strokeColor);
        }
    }

    /**
     * A ButtonPainter subclass to paint classic buttons.
     */
    public static class Classic extends ButtonPainter {

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

        /**
         * Constructor.
         */
        public Classic()
        {
            super();
        }

        /**
         * Draws a button for the given rect with an option for pressed.
         */
        public void paintButtonInShape(Painter aPntr, RectBase buttonRect, int aState, boolean isSelected)
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
    }
}
