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
    private static Color BACK_FILL = new Color("#E9E8EA");
    private static Color BACK_DARK_FILL = new Color("#C0C0C0");
    private static Color SEL_FILL = Color.LIGHTGRAY; //new Color("#0032D0");
    private static Color SEL_TEXT_FILL = Color.BLACK; //Color.WHITE;
    private static Color TARG_FILL = new Color("#4080F0");
    private static Color TARG_TEXT_FILL = Color.WHITE;

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
    public Paint getTextFill()  { return Color.BLACK; }

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
     * Creates an area for given view.
     */
    public Object createArea(View aView)
    {
        Object area = null;

        // Handle ButtonBase
        if (aView instanceof ButtonBase)
            area = createButtonArea();

        // Handle ProgressBar
        if (aView instanceof ProgressBar)
            area = createButtonArea();

        // Set View
        if (area instanceof ButtonArea)
            ((ButtonArea)area).setView(aView);

        // Return area
        return area;
    }

    /**
     * Creates a ButtonArea.
     */
    protected ButtonArea createButtonArea()  { return new ButtonArea(); }

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
        WindowView wins[] = WindowView.getOpenWindows();
        for (WindowView win : wins) {
            RootView rview = win.getRootView();
            rview.themeChanged();
            rview.setFill(_theme.getBackFill());
            rview.repaint();
        }
    }

    /**
     * The Standard theme with a hint of blue.
     */
    private static class StandardBlueTheme extends ViewTheme {

        // Color constants
        private static Color BASE = new Color(165, 179, 216).brighter();
        private static Color BACK_FILL = BASE.blend(Color.WHITE, .8);
        private static Color BACK_DARK_FILL = BACK_FILL.darker().darker();
        private static Color SEL_FILL = Color.LIGHTGRAY;
        private static Color SEL_TEXT_FILL = Color.BLACK;
        private static Color TARG_FILL = new Color("#4080F0");
        private static Color TARG_TEXT_FILL = Color.WHITE;

        /** Returns the background fill. */
        public Paint getBackFill()  { return BACK_FILL; }

        /** Returns the background fill. */
        public Paint getBackDarkFill()  { return BACK_DARK_FILL; }

        /** Returns the selection color. */
        public Paint getSelectFill()  { return SEL_FILL; }

        /** Returns the selection color. */
        public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }

        /** Returns the selection color. */
        public Paint getTargetFill()  { return TARG_FILL; }

        /** Returns the selection color. */
        public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }

        /** Creates a button area. */
        protected ButtonArea createButtonArea() { return new StandardBlueButtonArea(); }
    }

    /**
     * A Theme with lighter colors.
     */
    private static class LightTheme extends ViewTheme {

        // Color constants
        private static Color BACK_FILL = new Color("#F2F2F2");
        private static Color BACK_DARK_FILL = BACK_FILL.darker().darker();
        private static Color SEL_FILL = Color.LIGHTGRAY;
        private static Color SEL_TEXT_FILL = Color.BLACK;
        private static Color TARG_FILL = new Color("#4080F0");
        private static Color TARG_TEXT_FILL = Color.WHITE;

        /** Returns the background fill. */
        public Paint getBackFill()  { return BACK_FILL; }

        /** Returns the background fill. */
        public Paint getBackDarkFill()  { return BACK_DARK_FILL; }

        /** Returns the selection color. */
        public Paint getSelectFill()  { return SEL_FILL; }

        /** Returns the selection color. */
        public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }

        /** Returns the selection color. */
        public Paint getTargetFill()  { return TARG_FILL; }

        /** Returns the selection color. */
        public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }

        /** Creates a button area. */
        protected ButtonArea createButtonArea()
        {
            return new PlainButtonArea();
        }
    }

    /**
     * A ButtonArea for plain buttons.
     */
    private static class StandardBlueButtonArea extends ButtonArea {

        // Colors
        private static Color blue = Color.BLUE;//new Color(165, 179, 216).brighter();
        private static double fract = .01;
        protected static Color BUTTON_COLOR = Color.WHITE.blend(blue, fract);
        protected static Color BUTTON_RING_COLOR = new Color("#BFBFBF").blend(blue, fract);
        protected static Color BUTTON_OVER_COLOR = new Color("#F8F8F8").blend(blue, fract);
        protected static Color BUTTON_PRESSED_COLOR = new Color("#DFDFDF").blend(blue, fract);
        protected static Color BUTTON_BLUE_COLOR = new Color("#87AFDA").blend(blue, fract);

        /**
         * Draws a button for the given rect with an option for pressed.
         */
        public void paint(Painter aPntr)
        {
            // Update Area from View
            updateFromView();

            // Get fill color
            Color fillColor = BUTTON_COLOR;
            if (_state==BUTTON_OVER) fillColor = BUTTON_OVER_COLOR;
            else if (_state==BUTTON_PRESSED) fillColor = BUTTON_PRESSED_COLOR;
            else if (isSelected()) fillColor = BUTTON_PRESSED_COLOR;

            // Get shape and paint fill
            RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad).copyForPosition(_pos);
            aPntr.fillWithPaint(rect, fillColor);

            // Get stroke color
            Color strokeColor = BUTTON_RING_COLOR;
            if (_state==BUTTON_OVER) strokeColor = BUTTON_BLUE_COLOR;
            else if (_state==BUTTON_PRESSED) strokeColor = BUTTON_BLUE_COLOR;

            // Draw outer ring
            drawRect(aPntr, rect, _x, _y, _w, _h, strokeColor);

            // Handle Selected
            if (isSelected())
                paintSelected(aPntr);
        }
    }

        /**
     * A ButtonArea for plain buttons.
     */
    private static class PlainButtonArea extends ButtonArea {

        // Colors
        protected static Color BUTTON_COLOR = Color.WHITE;
        protected static Color BUTTON_RING_COLOR = new Color("#BFBFBF");
        protected static Color BUTTON_OVER_COLOR = new Color("#F8F8F8");
        protected static Color BUTTON_PRESSED_COLOR = new Color("#DFDFDF");
        protected static Color BUTTON_BLUE_COLOR = new Color("#87AFDA");

        /**
         * Draws a button for the given rect with an option for pressed.
         */
        public void paint(Painter aPntr)
        {
            // Update Area from View
            updateFromView();

            // Get fill color
            Color fillColor = BUTTON_COLOR;
            if (_state==BUTTON_OVER) fillColor = BUTTON_OVER_COLOR;
            else if (_state==BUTTON_PRESSED) fillColor = BUTTON_PRESSED_COLOR;
            else if (isSelected()) fillColor = BUTTON_PRESSED_COLOR;

            // Get shape and paint fill
            RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad).copyForPosition(_pos);
            aPntr.fillWithPaint(rect, fillColor);

            // Get stroke color
            Color strokeColor = BUTTON_RING_COLOR;
            if (_state==BUTTON_OVER) strokeColor = BUTTON_BLUE_COLOR;
            else if (_state==BUTTON_PRESSED) strokeColor = BUTTON_BLUE_COLOR;

            // Draw outer ring
            drawRect(aPntr, rect, _x, _y, _w, _h, strokeColor);

            // Handle Selected
            if (isSelected())
                paintSelected(aPntr);
        }
    }

    /**
     * A Theme for dark rendering.
     */
    private static class DarkTheme extends ViewTheme {

        // Color constants
        private static Color BACK_FILL = new Color("#3C3F41");
        private static Color BACK_DARK_FILL = BACK_FILL.darker().darker();
        private static Color SEL_FILL = Color.LIGHTGRAY;
        private static Color SEL_TEXT_FILL = Color.BLACK;
        private static Color TARG_FILL = new Color("#4080F0");
        private static Color TARG_TEXT_FILL = Color.WHITE;

        /** Returns the background fill. */
        public Paint getBackFill()  { return BACK_FILL; }

        /** Returns the background fill. */
        public Paint getBackDarkFill()  { return BACK_DARK_FILL; }

        /** Returns the text fill. */
        public Paint getTextFill()  { return Color.WHITE; }

        /** Returns the selection color. */
        public Paint getSelectFill()  { return SEL_FILL; }

        /** Returns the selection color. */
        public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }

        /** Returns the selection color. */
        public Paint getTargetFill()  { return TARG_FILL; }

        /** Returns the selection color. */
        public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }

        /** Creates a button area. */
        protected ButtonArea createButtonArea()
        {
            return new DarkButtonArea();
        }
    }

    /**
     * A ButtonArea for plain buttons.
     */
    private static class DarkButtonArea extends ButtonArea {

        // Colors
        private static Color BUTTON_COLOR = new Color("#45494A");
        private static Color BUTTON_RING_COLOR = new Color("#BFBFBF");
        private static Color BUTTON_OVER_COLOR = BUTTON_COLOR.brighter();
        private static Color BUTTON_PRESSED_COLOR = BUTTON_OVER_COLOR.brighter();
        private static Color BUTTON_BLUE_COLOR = new Color("#87AFDA");

        /**
         * Draws a button for the given rect with an option for pressed.
         */
        public void paint(Painter aPntr)
        {
            // Update Area from View
            updateFromView();

            // Get fill color
            Color fillColor = BUTTON_COLOR;
            if (_state==BUTTON_OVER) fillColor = BUTTON_OVER_COLOR;
            else if (_state==BUTTON_PRESSED) fillColor = BUTTON_PRESSED_COLOR;
            else if (isSelected()) fillColor = BUTTON_PRESSED_COLOR;

            // Get shape and paint fill
            RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad).copyForPosition(_pos);
            aPntr.fillWithPaint(rect, fillColor);

            // Get stroke color
            Color strokeColor = BUTTON_RING_COLOR;
            if (_state==BUTTON_OVER) strokeColor = BUTTON_BLUE_COLOR;
            else if (_state==BUTTON_PRESSED) strokeColor = BUTTON_BLUE_COLOR;

            // Draw outer ring
            drawRect(aPntr, rect, _x, _y, _w, _h, strokeColor);

            // Handle Selected
            if (isSelected())
                paintSelected(aPntr);
        }
    }

    /**
     * A Theme for Black and White rendering.
     */
    private static class BlackAndWhiteTheme extends ViewTheme {

        // Color constants
        private static Color BACK_FILL = Color.WHITE;
        private static Color BACK_DARK_FILL = Color.WHITE;
        private static Color SEL_FILL = new Color("#F0");
        private static Color SEL_TEXT_FILL = Color.BLACK;
        private static Color TARG_FILL = new Color("#F4");
        private static Color TARG_TEXT_FILL = Color.BLACK;

        /** Returns the background fill. */
        public Paint getBackFill()  { return BACK_FILL; }

        /** Returns the background fill. */
        public Paint getBackDarkFill()  { return BACK_DARK_FILL; }

        /** Returns the text fill. */
        public Paint getTextFill()  { return Color.BLACK; }

        /** Returns the selection color. */
        public Paint getSelectFill()  { return SEL_FILL; }

        /** Returns the selection color. */
        public Paint getSelectTextFill()  { return SEL_TEXT_FILL; }

        /** Returns the selection color. */
        public Paint getTargetFill()  { return TARG_FILL; }

        /** Returns the selection color. */
        public Paint getTargetTextFill()  { return TARG_TEXT_FILL; }

        /** Creates a button area. */
        protected ButtonArea createButtonArea()  { return new BlackAndWhiteButtonArea(); }
    }

    /**
     * A ButtonArea for Black and White buttons.
     */
    private static class BlackAndWhiteButtonArea extends ButtonArea {

        // Colors
        private static Color BUTTON_COLOR = Color.WHITE;
        private static Color BUTTON_RING_COLOR = Color.BLACK;
        private static Color BUTTON_OVER_COLOR = new Color("#F8");
        private static Color BUTTON_PRESSED_COLOR = new Color("#F0");
        private static Color BUTTON_BLUE_COLOR = new Color("#87AFDA");

        /**
         * Draws a button for the given rect with an option for pressed.
         */
        public void paint(Painter aPntr)
        {
            // Update Area from View
            updateFromView();

            // Get fill color
            Color fillColor = BUTTON_COLOR;
            if (_state==BUTTON_OVER) fillColor = BUTTON_OVER_COLOR;
            else if (_state==BUTTON_PRESSED) fillColor = BUTTON_PRESSED_COLOR;
            else if (isSelected()) fillColor = BUTTON_PRESSED_COLOR;

            // Get shape and paint fill
            RoundRect rect = new RoundRect(_x, _y, _w, _h, _rad).copyForPosition(_pos);
            aPntr.fillWithPaint(rect, fillColor);

            // Get stroke color
            Color strokeColor = BUTTON_RING_COLOR;
            if (_state==BUTTON_OVER) strokeColor = BUTTON_BLUE_COLOR;
            else if (_state==BUTTON_PRESSED) strokeColor = BUTTON_BLUE_COLOR;

            // Draw outer ring
            drawRect(aPntr, rect, _x, _y, _w, _h, strokeColor);

            // Handle Selected
            if (isSelected())
                paintSelected(aPntr);
        }
    }
}