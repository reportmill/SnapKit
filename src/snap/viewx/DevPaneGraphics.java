package snap.viewx;
import snap.geom.Insets;
import snap.view.*;

/**
 * A DevPane tab for inspecting Graphics.
 */
public class DevPaneGraphics extends ViewOwner {

    // The DevPane
    private DevPane  _devPane;

    // The Themes
    private static final String STANDARD_THEME = "Standard";
    private static final String STANDARD_BLUE_THEME = "StandardBlue";
    private static final String LIGHT_THEME = "Light";
    private static final String DARK_THEME = "Dark";
    private static final String BLACK_AND_WHITE_THEME = "BlackAndWhite";
    private static final String[] ALL_THEMES = {
            STANDARD_THEME, STANDARD_BLUE_THEME, LIGHT_THEME, DARK_THEME, BLACK_AND_WHITE_THEME
    };

    /**
     * Constructor.
     */
    public DevPaneGraphics(DevPane aDevPane)
    {
        super();
        _devPane = aDevPane;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Init themeList
        ListView<String> themeList = getView("ThemeList", ListView.class);
        themeList.setItems(ALL_THEMES);
        themeList.setSelItem(STANDARD_THEME);
        themeList.getListArea().setCellPadding(new Insets(4, 8, 4, 8));
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update ThemeList
        String themeName = ViewTheme.get().getClass().getSimpleName().replace("Theme", "");
        if (themeName.equals("View")) themeName = STANDARD_THEME;
        setViewSelItem("ThemeList", themeName);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ShowFlashButton
        if (anEvent.equals("ShowFlashButton"))
            ViewUpdater.setPaintDebug(anEvent.getBoolValue());

        // Handle ShowFrameRateButton
        if (anEvent.equals("ShowFrameRateButton"))
            ViewUpdater.setShowFrameRate(anEvent.getBoolValue());

        // Handle ThemeList
        if (anEvent.equals("ThemeList")) {
            String themeName = anEvent.getStringValue();
            ViewTheme.setThemeForName(themeName);
        }
    }
}
