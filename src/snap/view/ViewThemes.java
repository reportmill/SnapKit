package snap.view;
import snap.gfx.Color;

/**
 * Some ViewTheme implementations.
 */
public class ViewThemes {

    // The classic theme
    private static ViewTheme _classic;

    /**
     * Returns the classic theme.
     */
    public static ViewTheme getClassic()
    {
        if (_classic != null) return _classic;
        return _classic = new ViewThemes.Classic();
    }

    /**
     * Returns the theme for name.
     */
    public static ViewTheme getThemeForName(String aName)
    {
        // Set new theme
        switch (aName) {
            case "StandardBlue": return new ViewThemes.StandardBlueTheme();
            case "Light": return new ViewThemes.LightTheme();
            case "Dark": return new ViewThemes.DarkTheme();
            case "BlackAndWhite": return new ViewThemes.BlackAndWhiteTheme();
            default: return getClassic();
        }
    }

    /**
     * The classic theme.
     */
    private static class Classic extends ViewTheme {

        /** Creates a button area. */
        @Override
        public ButtonPainter createButtonPainter()  { return new ButtonPainter.Classic(); }
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
            BUTTON_BORDER_COLOR = new Color("#BFBFBF").blend(blue, fract);
            BUTTON_OVER_COLOR = new Color("#F8F8F8").blend(blue, fract);
            BUTTON_PRESSED_COLOR = new Color("#DFDFDF").blend(blue, fract);
            BUTTON_BORDER_PRESSED_COLOR = new Color("#87AFDA").blend(blue, fract);
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
            BUTTON_BORDER_COLOR = new Color("#BFBFBF");
            BUTTON_OVER_COLOR = BUTTON_COLOR.brighter();
            BUTTON_PRESSED_COLOR = BUTTON_OVER_COLOR.brighter();
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
            BUTTON_BORDER_COLOR = Color.BLACK;
            BUTTON_OVER_COLOR = new Color("#F8");
            BUTTON_PRESSED_COLOR = new Color("#F0");
        }
    }
}
