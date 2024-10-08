package snap.viewx;
import snap.geom.Insets;
import snap.gfx3d.RendererFactory;
import snap.view.*;

/**
 * A DevPane tab for inspecting Graphics.
 */
public class DevPaneGraphics extends ViewOwner {

    // The DevPane
    private DevPane  _devPane;

    // The Themes
    private static final String LIGHT_THEME = "Light";
    private static final String DARK_THEME = "Dark";
    private static final String LIGHT_BLUE_THEME = "LightBlue";
    private static final String CLASSIC_THEME = "Classic";
    private static final String BLACK_AND_WHITE_THEME = "BlackAndWhite";
    private static final String[] ALL_THEMES = {
            LIGHT_THEME, DARK_THEME, LIGHT_BLUE_THEME, CLASSIC_THEME, BLACK_AND_WHITE_THEME
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
        // Init ThemesListView
        ListView<String> themesListView = getView("ThemesListView", ListView.class);
        themesListView.setItems(ALL_THEMES);
        themesListView.setSelItem(CLASSIC_THEME);
        themesListView.setCellPadding(new Insets(4, 8, 4, 8));

        // Init RenderersListView
        ListView<RendererFactory> rendererListView = getView("RenderersListView", ListView.class);
        rendererListView.setItemTextFunction(itm -> itm.getRendererName());
        rendererListView.setItems(RendererFactory.getFactories());
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update ShowFlashButton, ShowFrameRateButton
        setViewValue("ShowFlashButton", ViewUpdater.isPaintDebug());
        setViewValue("ShowFrameRateButton", ViewUpdater.isShowFrameRate());

        // Update ThemesListView
        String themeName = ViewTheme.get().getClass().getSimpleName().replace("Theme", "");
        if (themeName.equals("View")) themeName = CLASSIC_THEME;
        setViewSelItem("ThemesListView", themeName);

        // Update RenderersListView
        RendererFactory rendererFactory = RendererFactory.getDefaultFactory();
        setViewSelItem("RenderersListView", rendererFactory);
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

        // Handle ThemesListView
        if (anEvent.equals("ThemesListView")) {
            String themeName = anEvent.getStringValue();
            ViewTheme.setThemeForName(themeName);
        }

        // Handle RenderersListView
        if (anEvent.equals("RenderersListView")) {
            RendererFactory rendererFactory = (RendererFactory) anEvent.getSelItem();
            RendererFactory.setDefaultFactory(rendererFactory);
        }
    }
}
