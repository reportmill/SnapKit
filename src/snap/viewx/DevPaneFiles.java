package snap.viewx;
import snap.view.*;
import snap.web.*;

/**
 * A DevPane tab for inspecting Graphics.
 */
public class DevPaneFiles extends ViewOwner {

    // The root file name
    private String _rootFilename;

    // The current WebSitePane
    private WebSitePane _sitePane;

    /**
     * Constructor.
     */
    public DevPaneFiles()
    {
        super();
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
     * Sets SitePane.
     */
    private void setSitePane(WebSitePane sitePane)
    {
        _sitePane = sitePane;
        BoxView sitePaneBox = getView("SitePaneBox", BoxView.class);
        sitePane.setActionHandler(e -> sitePaneDidFireAction());
        sitePane.addPropChangeListener(pc -> sitePaneSelFileChanged(), WebSitePane.SelFile_Prop);
        sitePaneBox.setContent(sitePane.getUI());
    }
    /**
     * Reset SitePane.
     */
    private void resetSitePane()
    {
        // Get WebSite for RootFilename (show error if not found)
        WebURL rootFileURL = WebURL.getURL(_rootFilename);
        WebSite site = rootFileURL != null ? rootFileURL.getAsSite() : null;
        if (site == null) {
            BoxView sitePaneBox = getView("SitePaneBox", BoxView.class);
            sitePaneBox.setContent(new Label("Can't find site for: " + _rootFilename));
            return;
        }

        // Create SitePane and set in SitePaneBox
        WebSitePane sitePane = new WebSitePaneX();
        sitePane.setSite(site);
        setSitePane(sitePane);
    }

    /**
     * Called when SitePane super-selects a file (double-clicks or text field entry) to fetch contents in TextView.
     */
    private void sitePaneDidFireAction()
    {
        WebFile selFile = _sitePane.getSelFile();
        if (selFile.isFile()) {
            String fileText = selFile.getText();
            setViewValue("FileTextView", fileText);
        }
    }

    /**
     * Called when SitePane selects a new file.
     */
    private void sitePaneSelFileChanged()
    {
        setViewValue("FileTextView", null);
    }
}
