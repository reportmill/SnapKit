/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A class to select a file to open or save.
 */
public class FilePanel extends ViewOwner {

    // The sites
    private WebSite[]  _sites;

    // The FilesBrowser
    private FilesBrowser  _filesBrowser;

    // The description
    private String  _desc;

    // The DialogBox
    private DialogBox  _dialogBox;

    // The tab bar holding sites
    private TabBar  _sitesTabBar;

    // The sites
    private static WebSite[]  _defaultSites;

    // The default site
    private static WebSite  _defaultSite;

    /**
     * Constructor.
     */
    public FilePanel()
    {
        super();

        // Create/config FilesBrowser
        _filesBrowser = new CustomFilesBrowser();
        _filesBrowser.addPropChangeListener(pc -> filesBrowserDidPropChange());

        // Get sites
        _sites = getDefaultSites();
    }

    /**
     * Return sites available to open/save files.
     */
    public WebSite[] getSites()  { return _sites; }

    /**
     * Adds a sites available to open/save files.
     */
    public void addSites(WebSite aSite)
    {
        _sites = ArrayUtils.addId(_sites, aSite);
    }

    /**
     * Removes a sites available to open/save files.
     */
    public void removeSites(WebSite aSite)
    {
        _sites = ArrayUtils.remove(_sites, aSite);
    }

    /**
     * Returns whether is saving.
     */
    public boolean isSaving()  { return _filesBrowser.isSaving(); }

    /**
     * Sets whether is saving.
     */
    public void setSaving(boolean aValue)  { _filesBrowser.setSaving(aValue); }

    /**
     * Returns the window title.
     */
    public String getTitle()  { return isSaving() ? "Save Panel" : "Open Panel"; }

    /**
     * Returns the first file types.
     */
    public String getType()  { return _filesBrowser.getType(); }

    /**
     * Returns the file types.
     */
    public String[] getTypes()  { return _filesBrowser.getTypes(); }

    /**
     * Sets the file types.
     */
    public void setTypes(String... theExts)  { _filesBrowser.setTypes(theExts); }

    /**
     * Sets the description.
     */
    public void setDesc(String aValue)  { _desc = aValue; }

    /**
     * Returns the site currently being browsed.
     */
    public WebSite getSite()  { return _filesBrowser.getSite(); }

    /**
     * Sets the site for the panel.
     */
    public void setSite(WebSite aSite)  { _filesBrowser.setSite(aSite); }

    /**
     * Shows the panel.
     */
    public String showOpenPanel(View aView)
    {
        setSaving(false);
        return showFilePanel(aView);
    }

    /**
     * Shows the panel.
     */
    public String showSavePanel(View aView)
    {
        setSaving(true);
        return showFilePanel(aView);
    }

    /**
     * Runs a file chooser that remembers last open file and size.
     */
    protected String showFilePanel(View aView)
    {
        WebFile file = showFilePanelWeb(aView);
        return file != null ? file.getPath() : null;
    }

    /**
     * Runs a file chooser that remembers last open file and size.
     */
    protected WebFile showFilePanelWeb(View aView)
    {
        // If no file/dir set, set from RecentPath (prefs)
        WebFile selDir = _filesBrowser.getSelDir();
        if (selDir == null) {
            String path = getRecentPath(getType());
            WebSite site = getSite();
            WebFile file = site.getFileForPath(path);
            _filesBrowser.setSelFile(file);
        }

        // Run code to add new folder button
        if (isSaving())
            runLater(() -> addNewFolderButton());

        // Create/config DialogBox with FilePanel UI
        _dialogBox = new DialogBox(getTitle());
        _dialogBox.setContent(getUI());
        _dialogBox.setConfirmEnabled(_filesBrowser.getSelOrTargFile() != null);

        // Run FileChooser UI in DialogBox
        boolean value = _dialogBox.showConfirmDialog(aView);
        if (!value)
            return null;

        // Get file and path of selection and save to preferences
        WebFile selFile = _filesBrowser.getSelOrTargFile();
        String selFilePath = selFile.getPath();

        // Save selected filename in preferences for its type (extension)
        setRecentPath(getType(), selFilePath);

        // If user is trying to save over an existing file, warn them
        boolean save = isSaving();
        if (save && selFile.getExists()) {

            // Run option panel for whether to overwrite
            DialogBox dbox2 = new DialogBox("Replace File");
            dbox2.setWarningMessage("The file " + selFilePath + " already exists. Replace it?");
            dbox2.setOptions("Replace", "Cancel");
            int answer = dbox2.showOptionDialog(aView, "Replace");

            // If user chooses cancel, re-run chooser
            if (answer != 0)
                return showFilePanelWeb(aView);
        }

        // Give focus back to given view
        if (save && aView != null)
            aView.requestFocus();

        // If link file, replace with real file
        if (selFile.getLinkFile() != null)
            selFile = selFile.getLinkFile();

        // Return file
        return selFile;
    }

    /**
     * Override to return FileBrowser UI.
     */
    @Override
    protected View createUI()
    {
        // Create top level ColView
        ColView topColView = new ColView();
        topColView.setFillWidth(true);
        topColView.setSpacing(5);

        // Create/add TabBar
        _sitesTabBar = new TabBar();
        _sitesTabBar.setName("SitesTabBar");
        _sitesTabBar.setBorder(Color.GRAY8, 1);
        _sitesTabBar.setBorderRadius(4);
        _sitesTabBar.setPadding(5, 5, 5, 5);
        _sitesTabBar.setTabMinWidth(90);
        topColView.addChild(_sitesTabBar);

        // Add FilesBrowser UI
        View filesBrowserUI = _filesBrowser.getUI();
        topColView.addChild(filesBrowserUI);

        // Return
        return topColView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Add tabs for sites to SitesTabBar
        WebSite[] sites = getSites();
        Tab.Builder tabBuilder = new Tab.Builder(_sitesTabBar);
        for (WebSite site : sites) {
            String siteName = getNameForSite(site);
            tabBuilder.title(siteName).add();
        }
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle SitesTabBar
        if (anEvent.equals("SitesTabBar")) {
            int selIndex = _sitesTabBar.getSelIndex();
            WebSite newSelSite = getSites()[selIndex];
            _filesBrowser.setSite(newSelSite);
        }
    }

    /**
     * Called when FilesBrowser does prop change.
     */
    private void filesBrowserDidPropChange()
    {
        WebFile selOrTargFile = _filesBrowser.getSelOrTargFile();
        boolean isFileSet = selOrTargFile != null;
        if (_dialogBox != null)
            _dialogBox.setConfirmEnabled(isFileSet);
    }

    /**
     * Adds a new Folder button.
     */
    protected void addNewFolderButton()
    {
        Button newFolderButton = new Button("New Folder");
        newFolderButton.setMinWidth(100);
        newFolderButton.setMinHeight(24);
        newFolderButton.setName("NewFolderButton");
        newFolderButton.addEventHandler(e -> createNewFolder(), View.Action);
        _dialogBox.getButtonBox().addChild(newFolderButton, 0);
    }

    /**
     * Called when NewFolderButton clicked.
     */
    private void createNewFolder()
    {
        // Get new dir name
        String newDirName = DialogBox.showInputDialog(getUI(), "New Folder Panel", "Enter name:", null);
        if (newDirName == null)
            return;

        // Get new dir path and create new dir
        WebFile selDir = _filesBrowser.getSelDir();
        String newDirPath = selDir.getDirPath() + newDirName;
        WebFile newDir = getSite().createFileForPath(newDirPath, true);
        newDir.save();

        // Set new dir
        _filesBrowser.setSelFile(newDir);
    }

    /**
     * Shows an Open panel for given description and types.
     */
    public static String showOpenPanel(View aView, String aDesc, String... theTypes)
    {
        FilePanel filePanel = new FilePanel();
        filePanel.setDesc(aDesc);
        filePanel.setTypes(theTypes);
        return filePanel.showOpenPanel(aView);
    }

    /**
     * Shows a Save panel for given description and types.
     */
    public static String showSavePanel(View aView, String aDesc, String... theTypes)
    {
        FilePanel filePanel = new FilePanel();
        filePanel.setDesc(aDesc);
        filePanel.setTypes(theTypes);
        return filePanel.showSavePanel(aView);
    }

    /**
     * Shows a Open panel for given description and types.
     */
    public static WebFile showOpenPanelWeb(View aView, String aDesc, String... theTypes)
    {
        FilePanel filePanel = new FilePanel();
        filePanel.setDesc(aDesc);
        filePanel.setTypes(theTypes);
        return filePanel.showFilePanelWeb(aView);
    }

    /**
     * Shows a Save panel for given description and types.
     */
    public static WebFile showSavePanelWeb(View aView, String aDesc, String... theTypes)
    {
        FilePanel filePanel = new FilePanel();
        filePanel.setDesc(aDesc);
        filePanel.setTypes(theTypes);
        filePanel.setSaving(true);
        return filePanel.showFilePanelWeb(aView);
    }

    /**
     * Returns the most recent path for given type.
     */
    private static String getRecentPath(String aType)
    {
        WebSite defaultSite = getSiteDefault();
        String defaultPath = FilesBrowserUtils.getHomeDirPathForSite(defaultSite);
        return Prefs.getDefaultPrefs().getString("MostRecentDocument." + aType, defaultPath);
    }

    /**
     * Sets the most recent path for given type.
     */
    private static void setRecentPath(String aType, String aPath)
    {
        WebSite defaultSite = getSiteDefault();
        if (!defaultSite.getURL().getScheme().equalsIgnoreCase("file"))
            return;

        Prefs.getDefaultPrefs().setValue("MostRecentDocument." + aType, aPath);
        Prefs.getDefaultPrefs().flush();
    }

    /**
     * Returns the default site.
     */
    public static WebSite getSiteDefault()
    {
        // If already set, just return
        if (_defaultSite != null) return _defaultSite;

        // Get, set DefaultSite
        WebURL defaultSiteURL = WebURL.getURL("/");
        return _defaultSite = defaultSiteURL.getSite();
    }

    /**
     * Sets the default site.
     */
    public static void setSiteDefault(WebSite aSite)
    {
        _defaultSite = aSite;
    }

    /**
     * Returns the sites.
     */
    public static WebSite[] getDefaultSites()
    {
        if (_defaultSites != null) return _defaultSites;

        // Init to local site
        WebSite localSite = FilesBrowserUtils.getLocalFileSystemSite();
        WebSite[] defaultSites = new WebSite[] { localSite };

        // Set/return
        return _defaultSites = defaultSites;
    }

    /**
     * Adds a site.
     */
    public static void addDefaultSite(WebSite aSite)
    {
        getDefaultSites();
        _defaultSites = ArrayUtils.addId(_defaultSites, aSite);
    }

    /**
     * Returns a name for a given site.
     */
    private static String getNameForSite(WebSite aSite)
    {
        if (aSite instanceof FileSite)
            return "Local Files";
        if (aSite instanceof RecentFilesSite)
            return "Recent Files";
        //if (aSite instanceof DropBoxSite)
        //    return "DropBox";
        return "Files";
    }

    /**
     * Custom FilesBrowser.
     */
    private class CustomFilesBrowser extends FilesBrowser {

        /**
         * Called on FileBrowser double-click or FileText enter key.
         */
        @Override
        protected void fireActionEvent(ViewEvent anEvent)
        {
            if (_dialogBox.isConfirmEnabled())
                _dialogBox.confirm();
        }
    }
}