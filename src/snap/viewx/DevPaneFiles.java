package snap.viewx;
import snap.view.*;
import snap.web.*;

/**
 * A DevPane tab for inspecting Graphics.
 */
public class DevPaneFiles extends ViewOwner {

    // The DevPane
    private DevPane  _devPane;

    // The root file name
    private String _rootFilename;

    // The current WebSitePane
    private WebSitePane _sitePane;

    /**
     * Constructor.
     */
    public DevPaneFiles(DevPane aDevPane)
    {
        super();
        _devPane = aDevPane;
        _rootFilename = "/";
    }

   /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Configure SitePaneBox
        BoxView sitePaneBox = getView("SitePaneBox", BoxView.class);
        sitePaneBox.setFillWidth(true);
        sitePaneBox.setFillHeight(true);
    }

    @Override
    protected void initShowing()
    {
        resetSitePane();
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        setViewValue("RootFileText", _rootFilename);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle RootFileText
        if (anEvent.equals("RootFileText")) {
            _rootFilename = anEvent.getStringValue();
            resetSitePane();
        }
    }

    /**
     * Reset SitePane.
     */
    private void resetSitePane()
    {
        BoxView sitePaneBox = getView("SitePaneBox", BoxView.class);

        WebURL rootFileURL = WebURL.getURL(_rootFilename);
        WebSite site = rootFileURL != null ? rootFileURL.getAsSite() : null;
        if (site == null) {
            sitePaneBox.setContent(new Label("Can't find site for: " + _rootFilename));
            return;
        }

        // Create SitePane and set in SitePaneBox
        WebSitePane sitePane = new WebSitePaneX();
        sitePane.setSite(site);
        sitePaneBox.setContent(sitePane.getUI());
    }
}
