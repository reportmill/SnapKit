package snap.view;
import snap.gfx.Color;

/**
 * Some ViewTheme implementations.
 */
public class ViewThemes {

    // Cached themes
    private static ViewTheme _light, _classic;

    /**
     * Returns the light theme.
     */
    public static ViewTheme getLight()
    {
        if (_light != null) return _light;
        return _light = new LightTheme();
    }

    /**
     * Returns the classic theme.
     */
    public static ViewTheme getClassic()
    {
        if (_classic != null) return _classic;
        return _classic = new ClassicTheme();
    }

    /**
     * Returns the theme for name.
     */
    public static ViewTheme getThemeForName(String aName)
    {
        // Set new theme
        switch (aName) {
            case "Light": return getLight();
            case "Dark": return new ViewThemes.DarkTheme();
            case "LightBlue": return new LightBlueTheme();
            case "Classic": return getClassic();
            case "BlackAndWhite": return new ViewThemes.BlackAndWhiteTheme();
            default: System.err.println("ViewThemes.getThemeForName: Unknown theme name: " + aName); return getLight();
        }
    }

    /**
     * A Theme with lighter colors.
     */
    private static class LightTheme extends ViewTheme {

        @Override
        protected void initColors()
        {
            BACK_FILL = new Color("#FA");
            GUTTER_FILL = new Color("#F0");
            SEL_FILL = new Color("#DA");
            TARG_FILL = new Color("#E6");
            TEXT_SEL_COLOR = Color.BLACK;
            TEXT_TARG_COLOR = Color.WHITE;
        }
    }

    /**
     * A Theme for dark rendering.
     */
    private static class DarkTheme extends ViewTheme {

        @Override
        protected void initColors()
        {
            // Reset Color constants
            BACK_FILL = new Color("#2B");
            GUTTER_FILL = BACK_FILL.darker().darker();
            CONTENT_COLOR = BACK_FILL;
            CONTENT_ALT_COLOR = BACK_FILL.brighter();
            SEL_FILL = new Color("#90");
            TARG_FILL = new Color("#80");
            TEXT_COLOR = Color.WHITE;
            TEXT_SEL_COLOR = Color.BLACK;
            TEXT_TARG_COLOR = Color.WHITE;

            // Reset Button colors
            BUTTON_COLOR = new Color("#45494A");
            BUTTON_BORDER_COLOR = new Color("#BF");
            BUTTON_OVER_COLOR = BUTTON_COLOR.brighter();
            BUTTON_PRESSED_COLOR = BUTTON_OVER_COLOR.brighter();
        }
    }

    /**
     * The Light theme with a hint of blue.
     */
    private static class LightBlueTheme extends ViewTheme {

        @Override
        protected void initColors()
        {
            // Reset Color constants
            Color BASE = new Color(165, 179, 216).brighter();
            BACK_FILL = BASE.blend(Color.WHITE, .8);
            GUTTER_FILL = BASE.blend(Color.WHITE, .6);
            CONTENT_COLOR = CONTENT_COLOR.blend(Color.BLUE, .025);
            CONTENT_ALT_COLOR = CONTENT_ALT_COLOR.blend(Color.BLUE, .075);
            SEL_FILL = BASE.blend(Color.WHITE, .6);
            TARG_FILL = BASE.blend(Color.WHITE, .7);
            TEXT_SEL_COLOR = Color.BLACK;
            TEXT_TARG_COLOR = Color.WHITE;

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
     * The classic theme.
     */
    private static class ClassicTheme extends ViewTheme {

        /** Creates a button area. */
        @Override
        public ButtonPainter createButtonPainter()  { return new ButtonPainter.Classic(); }
    }

    /**
     * A Theme for Black and White rendering.
     */
    private static class BlackAndWhiteTheme extends ViewTheme {

        @Override
        protected void initColors()
        {
            // Reset Color constants
            BACK_FILL = Color.WHITE;
            GUTTER_FILL = Color.WHITE;
            SEL_FILL = new Color("#F0");
            TARG_FILL = new Color("#F8");
            TEXT_SEL_COLOR = Color.BLACK;
            TEXT_TARG_COLOR = Color.BLACK;

            // Reset Button colors
            BUTTON_COLOR = Color.WHITE;
            BUTTON_BORDER_COLOR = Color.BLACK;
            BUTTON_OVER_COLOR = new Color("#F8");
            BUTTON_PRESSED_COLOR = new Color("#F0");
        }
    }
}
