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
        protected void initViewStyles()
        {
            setViewStyleString(View.class, "TextColor: WHITE");
            super.initViewStyles();

            // RootView
            setViewStyleString(RootView.class, "Fill: #2B");
            setViewStyleStringForAlternate(RootView.class, "Fill: #15");

            // Button
            setViewStyleString(Button.class, "Fill: #45494A; Border: #BF");
            setViewStyleStringForHover(Button.class, "Fill: #7D8080"); // button-fill.brighter
            setViewStyleStringForActive(Button.class, "Fill: #A4A6A6; Border: #87AFDA"); // button-hover.brighter

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
            setViewStyleStringForActive(ListView.class, "Fill: #90");
            setViewStyleStringForHover(ListView.class, "Fill: #80");

            // TableView
            setViewStyleString(TableView.class, "Fill: 2B");

            // TreeView
            setViewStyleString(TreeView.class, "Fill: 2B");
        }
    }

    /**
     * The Light theme with a hint of blue.
     */
    private static class LightBlueTheme extends ViewTheme {

        @Override
        protected void initViewStyles()
        {
            super.initViewStyles();

            // RootView
            Color baseColor = new Color(165, 179, 216).brighter();
            Color backColor = baseColor.blend(Color.WHITE, .8);
            Color gutterColor = baseColor.blend(Color.WHITE, .6);
            setViewStyleString(RootView.class, "Fill: " + backColor.toColorString());
            setViewStyleStringForAlternate(RootView.class, "Fill: " + gutterColor.toColorString());

            // Button
            setViewStyleString(Button.class, "Fill: #FCFCFF; Border: #BDBDC0");
            setViewStyleStringForHover(Button.class, "Fill: #F6F6F8");
            setViewStyleStringForActive(Button.class, "Fill: #DDDDDF; Border: #86ADDA");

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
            String selectedColorStr = baseColor.blend(Color.WHITE, .6).toColorString();
            String hoverColorString = baseColor.blend(Color.WHITE, .7).toColorString();
            setViewStyleStringForActive(ListView.class, "Fill: " + selectedColorStr);
            setViewStyleStringForHover(ListView.class, "Fill: " + hoverColorString);
        }
    }

    /**
     * A Theme for Black and White rendering.
     */
    private static class BlackAndWhiteTheme extends ViewTheme {

        @Override
        protected void initViewStyles()
        {
            setViewStyleString(View.class, "TextColor: WHITE");
            super.initViewStyles();

            // RootView
            setViewStyleString(RootView.class, "Fill: WHITE");
            setViewStyleStringForAlternate(RootView.class, "Fill: WHITE");

            // Button
            setViewStyleString(Button.class, "Fill: WHITE; Border: BLACK");
            setViewStyleStringForHover(Button.class, "Fill: #F8");
            setViewStyleStringForActive(Button.class, "Fill: #F0");

            // TextView
            setViewStyleStringForActive(TextView.class, "TextColor: BLACK");
            setViewStyleStringForHover(TextView.class, "TextColor: WHITE");

            // ListView
            setViewStyleStringForActive(ListView.class, "Fill: #F0");
            setViewStyleStringForHover(ListView.class, "Fill: #F8");
        }
    }
}
