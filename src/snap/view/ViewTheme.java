package snap.view;
import snap.geom.RoundRect;
import snap.gfx.*;

/**
 * A class to provide view area classes to define UI look.
 */
public class ViewTheme {
    
    // The current theme
    private static ViewTheme     _theme = new ViewTheme();

    // The last theme
    private static ViewTheme     _lastTheme;

    // Color constants
    protected Color BACK_FILL = new Color("#E9E8EA");
    protected Color BACK_DARK_FILL = new Color("#C0"); // Color.LIGHTGRAY
    protected Color SEL_FILL = new Color("#C0"); // Color.LIGHTGRAY
    protected Color TARG_FILL = new Color("#D0");
    protected Color TEXT_FILL = Color.BLACK;
    protected Color SEL_TEXT_FILL = Color.BLACK;
    protected Color TARG_TEXT_FILL = Color.WHITE;

    // Button colors
    protected Color BUTTON_COLOR = Color.WHITE;
    protected Color BUTTON_RING_COLOR = new Color("#BFBFBF");
    protected Color BUTTON_OVER_COLOR = new Color("#F8F8F8");
    protected Color BUTTON_PRESSED_COLOR = new Color("#DFDFDF");
    protected Color BUTTON_BLUE_COLOR = new Color("#87AFDA");

    /**
     * Returns the background fill.
     */
    public Paint getBackFill()  { return BACK_FILL; }

    /**
     * Returns the background fill.
     */
    public Paint getBackDarkFill()  { return BACK_DARK_FILL; }

    /**
     * Returns the text fill.
     */
    public Paint getTextFill()  { return TEXT_FILL; }

    /**
     * Returns the selection color.
     */
    public Paint getSelectFill()  { return SEL_FILL; }

    /**
     * Returns the selection color.
     */
    public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }

    /**
     * Returns the selection color.
     */
    public Paint getTargetFill()  { return TARG_FILL; }

    /**
     * Returns the selection color.
     */
    public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }

    /**
     * Returns the button color.
     */
    public Color getButtonColor()  { return BUTTON_COLOR; }

    /**
     * Returns the button border color.
     */
    public Color getButtonBorderColor()  { return BUTTON_RING_COLOR; }

    /**
     * Creates a ButtonArea.
     */
    public ButtonArea createButtonArea(ButtonBase aButton)
    {
        return new ButtonArea(aButton);
    }

    /**
     * Returns the current theme.
     */
    public static ViewTheme get()  { return _theme; }

    /**
     * Returns the last theme.
     */
    public static ViewTheme getLast()  { return _lastTheme; }

    /**
     * Sets the theme by name.
     */
    public static void setThemeForName(String aName)
    {
        // Set LastTheme
        _lastTheme = _theme;

        // Set new theme
        switch(aName) {
            case "StandardBlue": _theme = new StandardBlueTheme(); break;
            case "Light": _theme = new LightTheme(); break;
            case "Dark": _theme = new DarkTheme(); break;
            case "BlackAndWhite": _theme = new BlackAndWhiteTheme(); break;
            default: _theme = new ViewTheme();
        }

        // Update windows
        WindowView[] openWindows = WindowView.getOpenWindows();
        for (WindowView openWindow : openWindows) {
            RootView rootView = openWindow.getRootView();
            rootView.themeChanged();
            rootView.setFill(_theme.getBackFill());
            rootView.repaint();
        }
    }

    /**
     * A Theme with lighter colors.
     */
    private static class LightTheme extends ViewTheme {

        // Reset Color constants
        {
            BACK_FILL = new Color("#FA");
            BACK_DARK_FILL = new Color("#F0");
            SEL_FILL = new Color("#DA");
            TARG_FILL = new Color("#E6");
            SEL_TEXT_FILL = Color.BLACK;
            TARG_TEXT_FILL = Color.WHITE;
        }

        /** Creates a button area. */
        public ButtonArea createButtonArea(ButtonBase aButton)
        {
            return new PlainButtonArea(aButton);
        }
    }

    /**
     * A ButtonArea for plain buttons.
     */
    private class PlainButtonArea extends ButtonArea {

        /**
         * Constructor.
         */
        public PlainButtonArea(ButtonBase aButton)
        {
            super(aButton);
        }

        /**
         * Draws a button for the given rect with an option for pressed.
         */
        public void paint(Painter aPntr)
        {
            // Update Area from View
            updateFromView();

            // Get fill color
            Color fillColor = BUTTON_COLOR;
            if (_state == BUTTON_OVER)
                fillColor = BUTTON_OVER_COLOR;
            else if (_state == BUTTON_PRESSED)
                fillColor = BUTTON_PRESSED_COLOR;
            else if (isSelected())
                fillColor = BUTTON_PRESSED_COLOR;

            // Get shape and paint fill
            RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad);
            if (_pos != null)
                rect = rect.copyForPosition(_pos);
            aPntr.fillWithPaint(rect, fillColor);

            // Get stroke color
            Color strokeColor = BUTTON_RING_COLOR;
            if (_state == BUTTON_OVER)
                strokeColor = BUTTON_BLUE_COLOR;
            else if (_state == BUTTON_PRESSED)
                strokeColor = BUTTON_BLUE_COLOR;

            // Draw outer ring
            drawRect(aPntr, rect, _x, _y, _w, _h, strokeColor);

            // Handle Selected
            if (isSelected())
                paintSelected(aPntr);
        }
    }

    /**
     * The Standard theme with a hint of blue.
     */
    private static class StandardBlueTheme extends ViewTheme {

        // Reset Color constants
        {
            // Reset Color constants
            Color BASE = new Color(165, 179, 216).brighter();
            BACK_FILL = BASE.blend(Color.WHITE, .8);
            BACK_DARK_FILL = BASE.blend(Color.WHITE, .6);
            SEL_FILL = BASE.blend(Color.WHITE, .6);
            TARG_FILL = BASE.blend(Color.WHITE, .7);
            SEL_TEXT_FILL = Color.BLACK;
            TARG_TEXT_FILL = Color.WHITE;

            // Reset Button colors
            Color blue = Color.BLUE;
            double fract = .01;
            BUTTON_COLOR = Color.WHITE.blend(blue, fract);
            BUTTON_RING_COLOR = new Color("#BFBFBF").blend(blue, fract);
            BUTTON_OVER_COLOR = new Color("#F8F8F8").blend(blue, fract);
            BUTTON_PRESSED_COLOR = new Color("#DFDFDF").blend(blue, fract);
            BUTTON_BLUE_COLOR = new Color("#87AFDA").blend(blue, fract);
        }
    }

    /**
     * A Theme for dark rendering.
     */
    private static class DarkTheme extends ViewTheme {

        // Reset Color constants
        {
            // Reset Color constants
            BACK_FILL = new Color("#3C3F41");
            BACK_DARK_FILL = BACK_FILL.darker().darker();
            SEL_FILL = new Color("#C0"); // Color.LIGHTGRAY;
            TARG_FILL = new Color("#90");
            TEXT_FILL = Color.WHITE;
            SEL_TEXT_FILL = Color.BLACK;
            TARG_TEXT_FILL = Color.WHITE;

            // Reset Button colors
            BUTTON_COLOR = new Color("#45494A");
            BUTTON_RING_COLOR = new Color("#BFBFBF");
            BUTTON_OVER_COLOR = BUTTON_COLOR.brighter();
            BUTTON_PRESSED_COLOR = BUTTON_OVER_COLOR.brighter();
            BUTTON_BLUE_COLOR = new Color("#87AFDA");
        }
    }

    /**
     * A Theme for Black and White rendering.
     */
    private static class BlackAndWhiteTheme extends ViewTheme {

        // Reset Color constants
        {
            // Reset Color constants
            BACK_FILL = Color.WHITE;
            BACK_DARK_FILL = Color.WHITE;
            SEL_FILL = new Color("#F0");
            TARG_FILL = new Color("#F8");
            SEL_TEXT_FILL = Color.BLACK;
            TARG_TEXT_FILL = Color.BLACK;

            // Reset Button colors
            BUTTON_COLOR = Color.WHITE;
            BUTTON_RING_COLOR = Color.BLACK;
            BUTTON_OVER_COLOR = new Color("#F8");
            BUTTON_PRESSED_COLOR = new Color("#F0");
            BUTTON_BLUE_COLOR = new Color("#87AFDA");
        }
    }
}