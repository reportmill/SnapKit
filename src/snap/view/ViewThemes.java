package snap.view;
import snap.gfx.Color;

/**
 * Some ViewTheme implementations.
 */
public class ViewThemes {

    // Cached themes
    private static ViewTheme _light, _dark;

    /**
     * Returns the light theme.
     */
    public static ViewTheme getLight()
    {
        if (_light != null) return _light;
        return _light = new LightTheme();
    }

    /**
     * Returns the dark theme.
     */
    public static ViewTheme getDark()
    {
        if (_dark != null) return _dark;
        return _dark = new DarkTheme();
    }

    /**
     * Returns the theme for name.
     */
    public static ViewTheme getThemeForName(String aName)
    {
        // Set new theme
        return switch (aName) {
            case "Light" -> getLight();
            case "Dark" -> getDark();
            case "LightBlue" -> new LightBlueTheme();
            case "BlackAndWhite" -> new BlackAndWhiteTheme();
            default -> {
                System.err.println("ViewThemes.getThemeForName: Unknown theme name: " + aName);
                yield getLight();
            }
        };
    }

    /**
     * A Theme with lighter colors.
     */
    private static class LightTheme extends ViewTheme {

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
            SEL_FILL = new Color("#90");
            TARG_FILL = new Color("#80");

            // Reset Button colors
            BUTTON_COLOR = new Color("#45494A");
            BUTTON_BORDER_COLOR = new Color("#BF");
            BUTTON_OVER_COLOR = BUTTON_COLOR.brighter();
            BUTTON_PRESSED_COLOR = BUTTON_OVER_COLOR.brighter();
        }

        @Override
        protected void initViewStyles()
        {
            setViewStyleString(View.class, "TextColor: WHITE");
            super.initViewStyles();

            // TextField
            setViewStyleString(TextField.class, "Fill: #2B; TextColor: WHITE");

            // TextView
            setViewStyleString(TextView.class, "Fill: #2B; TextColor: WHITE");
            setViewStyleStringForActive(TextView.class, "TextColor: BLACK");
            setViewStyleStringForHover(TextView.class, "TextColor: WHITE");

            // ListView
            setViewStyleString(ListView.class, "Fill: #2B");
            String contentAltColorStr = Color.get("#2B").brighter().toColorString();
            setViewStyleStringForAlternate(ListView.class, "Fill: " + contentAltColorStr);
            //setViewStyleStringForActive(ListView.class, "Fill: #DA");
            //setViewStyleStringForHover(ListView.class, "Fill: #E6; TextColor: WHITE");
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
            SEL_FILL = BASE.blend(Color.WHITE, .6);
            TARG_FILL = BASE.blend(Color.WHITE, .7);

            // Reset Button colors
            Color blue = Color.BLUE;
            double fract = .01;
            BUTTON_COLOR = Color.WHITE.blend(blue, fract);
            BUTTON_BORDER_COLOR = new Color("#BFBFBF").blend(blue, fract);
            BUTTON_OVER_COLOR = new Color("#F8F8F8").blend(blue, fract);
            BUTTON_PRESSED_COLOR = new Color("#DFDFDF").blend(blue, fract);
            BUTTON_BORDER_PRESSED_COLOR = new Color("#87AFDA").blend(blue, fract);
        }

        @Override
        protected void initViewStyles()
        {
            super.initViewStyles();

            // TextView
            Color contentColor = Color.WHITE.blend(Color.BLUE, .025);
            String contentColorStr = contentColor.toColorString();
            setViewStyleString(TextView.class, "Fill: " + contentColorStr);
            setViewStyleStringForActive(TextView.class, "TextColor: BLACK");
            setViewStyleStringForHover(TextView.class, "TextColor: WHITE");

            // ListView
            setViewStyleString(ListView.class, "Fill: " + contentColorStr);
            String contentAltColorStr = Color.get("#F8").blend(Color.BLUE, .075).toColorString();
            setViewStyleStringForAlternate(ListView.class, "Fill: " + contentAltColorStr);
        }
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

            // Reset Button colors
            BUTTON_COLOR = Color.WHITE;
            BUTTON_BORDER_COLOR = Color.BLACK;
            BUTTON_OVER_COLOR = new Color("#F8");
            BUTTON_PRESSED_COLOR = new Color("#F0");
        }

        @Override
        protected void initViewStyles()
        {
            setViewStyleString(View.class, "TextColor: WHITE");
            super.initViewStyles();

            // TextView
            setViewStyleStringForActive(TextView.class, "TextColor: BLACK");
            setViewStyleStringForHover(TextView.class, "TextColor: WHITE");
        }
    }
}
