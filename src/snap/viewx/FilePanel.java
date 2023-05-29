/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.props.PropChangeListener;
import snap.util.*;
import snap.view.*;
import snap.web.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A class to select a file to open or save.
 */
public class FilePanel extends ViewOwner {

    // Whether choosing file for save
    private boolean  _saving;

    // A function to determine if a given file is valid
    private Predicate<WebFile> _fileValidator;

    // The file types
    private String[]  _types;

    // ActionHandler
    private EventListener _actionHandler;

    // The sites
    private WebSite[]  _sites;

    // The currently selected site
    private WebSite  _selSite;

    // The WebSitePane
    private WebSitePane _sitePane;

    // The currently selected file
    private WebFile _selFile;

    // The DialogBox
    private DialogBox  _dialogBox;

    // The tab bar holding sites
    private TabBar  _sitesTabBar;

    // A view to animate WebSitePane changes
    private TransitionPane  _transitionPane;

    // Map of existing/cached WebSitePanes
    private Map<WebSite, WebSitePane> _sitePanes = new HashMap<>();

    // Listener for WebSitePane prop changes
    private PropChangeListener _sitePanePropChangeLsnr = pc -> sitePaneDidPropChange();

    // The sites
    private static WebSite[]  _defaultSites;

    // Constants for properties
    public static final String SelFile_Prop = "SelFile";
    public static final String SelSite_Prop = "SelSite";

    /**
     * Constructor.
     */
    public FilePanel()
    {
        super();

        // Get sites
        _sites = getDefaultSites();
        _selSite = _sites.length > 0 ? _sites[0] : null;
    }

    /**
     * Returns whether is saving.
     */
    public boolean isSaving()  { return _saving; }

    /**
     * Sets whether is saving.
     */
    public void setSaving(boolean aValue)
    {
        _saving = aValue;

        // If Saving, remove RecentFilesSite
        if (aValue) {
            RecentFilesSite recentFilesSite = RecentFilesSite.getShared();
            removeSite(recentFilesSite);
        }
    }

    /**
     * Returns the function that determines whether file can be selected.
     */
    public Predicate<WebFile> getFileValidator()  { return _fileValidator; }

    /**
     * Sets the function that determines whether file can be selected.
     */
    public void setFileValidator(Predicate<WebFile> fileValidator)
    {
        _fileValidator = fileValidator;
    }

    /**
     * Returns the file types.
     */
    public String[] getTypes()  { return _types; }

    /**
     * Sets the file types.
     */
    public void setTypes(String ... theExts)
    {
        _types = ArrayUtils.map(theExts, type -> WebSitePaneUtils.normalizeType(type), String.class);
        setFileValidator(file -> WebSitePaneUtils.isValidFileForTypes(file, _types));
    }

    /**
     * Returns the action event listener.
     */
    public EventListener getActionHandler()  { return _actionHandler; }

    /**
     * Sets the action event listener.
     */
    public void setActionHandler(EventListener actionHandler)
    {
        _actionHandler = actionHandler;
    }

    /**
     * Sets the description.
     */
    public void setDesc(String aValue)  { }

    /**
     * Return sites available to open/save files.
     */
    public WebSite[] getSites()  { return _sites; }

    /**
     * Adds a site available to open/save files.
     */
    public void addSite(WebSite aSite)
    {
        _sites = ArrayUtils.addId(_sites, aSite);
    }

    /**
     * Removes a site available to open/save files.
     */
    public void removeSite(WebSite aSite)
    {
        // Remove site
        _sites = ArrayUtils.remove(_sites, aSite);

        // If SelSite is removed site, reset SelSite
        if (aSite == getSelSite()) {
            WebSite newSelSite = _sites.length > 0 ? _sites[0] : null;
            setSelSite(newSelSite);
        }
    }

    /**
     * Returns the site currently being browsed.
     */
    public WebSite getSelSite()  { return _selSite; }

    /**
     * Sets the site currently being browsed.
     */
    public void setSelSite(WebSite aSite)
    {
        // If already set, just return
        if (aSite == _selSite) return;

        // Set site
        firePropChange(SelSite_Prop, _selSite, _selSite = aSite);

        // Get/set SitePane
        if (isUISet()) {
            WebSitePane sitePane = getSitePaneForSite(aSite);
            setSitePane(sitePane);
        }
    }

    /**
     * Returns the selected file.
     */
    public WebFile getSelFile()
    {
        // If SelFile is link file, replace with real file
        WebFile selFile = _selFile;
        if (selFile != null && selFile.getLinkFile() != null)
            selFile = selFile.getRealFile();

        // Return
        return selFile;
    }

    /**
     * Returns the selected file.
     */
    public void setSelFile(WebFile aFile)
    {
        // If already set, just return
        if (aFile == _selFile) return;

        // Set value and fire prop change
        firePropChange(SelFile_Prop, _selFile, _selFile = aFile);

        // Update Dialogbox.ConfirmEnabled
        if (_dialogBox != null)
            _dialogBox.setConfirmEnabled(aFile != null);
    }

    /**
     * Returns the selected file and adds it to recent files.
     */
    public WebFile getSelFileAndAddToRecentFiles()
    {
        // Get SelFile
        WebFile selFile = getSelFile();

        // Add to recent files
        WebURL selFileURL = selFile != null ? selFile.getURL() : null;
        if (selFileURL != null)
            RecentFiles.addURL(selFileURL);

        // Return
        return selFile;
    }

    /**
     * Runs a file chooser that remembers last open file and size.
     */
    public WebFile showFilePanel(View aView)
    {
        // Run code to add new folder button
        if (isSaving())
            runLater(() -> addNewFolderButton());

        // Create/config DialogBox with FilePanel UI
        String title = isSaving() ? "Save Panel" : "Open Panel";
        _dialogBox = new DialogBox(title);
        View sitePanelUI = getUI();
        _dialogBox.setContent(sitePanelUI);

        // Run FileChooser UI in DialogBox
        boolean value = _dialogBox.showConfirmDialog(aView);
        if (!value)
            return null;

        // Get file and path of selection and save to preferences
        WebFile selFile = getSelFileAndAddToRecentFiles();

        // If user is trying to save over an existing file, warn them
        boolean save = isSaving();
        if (save && selFile.getExists()) {

            // Run option panel for whether to overwrite
            DialogBox replaceFileDialogBox = new DialogBox("Replace File");
            String message = String.format("The file %s already exists. Replace it?", selFile.getPath());
            replaceFileDialogBox.setWarningMessage(message);
            replaceFileDialogBox.setOptions("Replace", "Cancel");
            int answer = replaceFileDialogBox.showOptionDialog(aView, "Replace");

            // If user chooses cancel, re-run chooser
            if (answer != 0)
                return showFilePanel(aView);
        }

        // Give focus back to given view
        if (save && aView != null)
            aView.requestFocus();

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
        _sitesTabBar.getTabsBox().setSpacing(5);
        _sitesTabBar.setTabMinWidth(100);
        _sitesTabBar.setFont(Font.Arial13);
        topColView.addChild(_sitesTabBar);

        // Create/add TransitionPane
        _transitionPane = new TransitionPane();
        _transitionPane.setGrowHeight(true);
        topColView.addChild(_transitionPane);

        // Return
        return topColView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get file sites
        WebSite[] sites = getSites();

        // If more than one, add tabs for sites to SitesTabBar
        if (sites.length > 1) {
            Tab.Builder tabBuilder = new Tab.Builder(_sitesTabBar);
            for (WebSite site : sites) {
                String siteName = getNameForSite(site);
                tabBuilder.title(siteName).add();
            }
        }

        // Otherwise, just hide SitesTabBar
        else _sitesTabBar.setVisible(false);

        // Set SitePane for SelSite
        WebSite selSite = getSelSite();
        WebSitePane sitePane = getSitePaneForSite(selSite);
        setSitePane(sitePane);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle SitesTabBar
        if (anEvent.equals("SitesTabBar"))
            resetSelSiteFromSitesTabBar();
    }

    /**
     * Called on FileBrowser double-click or InputText enter key.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        // If DialogBox set, confirm
        if (_dialogBox != null && _dialogBox.isConfirmEnabled())
            _dialogBox.confirm();

        // If ActionHandler set, forward event
        if (_actionHandler != null)
            _actionHandler.listenEvent(anEvent);
    }

    /**
     * Resets the SelSite from new SitesTabBar selection.
     */
    private void resetSelSiteFromSitesTabBar()
    {
        // Get current selected file/site
        WebFile selFile = getSelFile();
        boolean isRecentFilesSite = selFile != null && getSelSite() instanceof RecentFilesSite;

        // Get site at SitesTabBar.SelIndex and set
        int selIndex = _sitesTabBar.getSelIndex();
        WebSite[] fileSites = getSites();
        WebSite newSelSite = fileSites[selIndex];
        setSelSite(newSelSite);

        // If recent files site had selected file from this site, select in new SitePane
        if (isRecentFilesSite) {
            WebSite selFileSite = selFile.getSite();
            if (selFileSite == newSelSite)
                _sitePane.setSelFile(selFile);
        }
    }

    /**
     * Sets the SitePane.
     */
    private void setSitePane(WebSitePane aSitePane)
    {
        // If already set, just return
        if (aSitePane == _sitePane) return;

        // Get/set transition
        TransitionPane.Transition transition = getTransitionForFileBrowsers(_sitePane, aSitePane);
        _transitionPane.setTransition(transition);

        // If old, remove
        if (_sitePane != null)
            _sitePane.removePropChangeListener(_sitePanePropChangeLsnr);

        // Set SitePane
        _sitePane = aSitePane;

        // Update SitePane
        _sitePane.setSaving(isSaving());
        String[] types = getTypes();
        if (types != null)
            _sitePane.setTypes(types);
        _sitePane.setFileValidator(getFileValidator());

        // Update UI
        _sitePane.addPropChangeListener(_sitePanePropChangeLsnr);
        View sitePaneUI = _sitePane.getUI();
        _transitionPane.setContent(sitePaneUI);

        // Update
        sitePaneDidPropChange();
    }

    /**
     * Called when SitePane does prop change.
     */
    private void sitePaneDidPropChange()
    {
        WebFile selOrTargFile = _sitePane.getValidSelOrTargFile();
        setSelFile(selOrTargFile);
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
        WebFile selDir = _sitePane.getSelDir();
        String newDirPath = selDir.getDirPath() + newDirName;
        WebFile newDir = getSelSite().createFileForPath(newDirPath, true);
        newDir.save();

        // Set new dir
        _sitePane.setSelFile(newDir);
    }

    /**
     * Returns a WebSitePane for given site.
     */
    private WebSitePane getSitePaneForSite(WebSite aSite)
    {
        // Get from cache and just return if found
        WebSitePane webSitePane = _sitePanes.get(aSite);
        if (webSitePane != null)
            return webSitePane;

        // Create and add to cache
        webSitePane = createSitePaneForSite(aSite);
        webSitePane.setSite(aSite);
        webSitePane.setActionHandler(e -> fireActionEvent(e));
        _sitePanes.put(aSite, webSitePane);

        // Return
        return webSitePane;
    }

    /**
     * Creates a SitePane for given site.
     */
    private WebSitePane createSitePaneForSite(WebSite aSite)
    {
        if (aSite instanceof RecentFilesSite)
            return new RecentFilesSitePane();
        if (aSite instanceof DropBoxSite)
            return new DropBoxSitePane();
        return new WebSitePaneX();
    }

    /**
     * Returns the transition for given WebSitePanes so changing sites will slide left/right when new one selected.
     */
    private TransitionPane.Transition getTransitionForFileBrowsers(WebSitePane webSitePane1, WebSitePane webSitePane2)
    {
        if (webSitePane1 == null) return TransitionPane.Instant;
        int index1 = ArrayUtils.indexOf(getSites(), webSitePane1.getSite());
        int index2 = ArrayUtils.indexOf(getSites(), webSitePane2.getSite());
        return index1 < index2 ? TransitionPane.MoveRight : TransitionPane.MoveLeft;
    }

    /**
     * Shows an Open panel for given description and types.
     */
    public static String showOpenPanel(View aView, String aDesc, String... theTypes)
    {
        WebFile file = showOpenFilePanel(aView, aDesc, theTypes);
        return file != null ? file.getPath() : null;
    }

    /**
     * Shows a Save panel for given description and types.
     */
    public static String showSavePanel(View aView, String aDesc, String... theTypes)
    {
        WebFile file = showSaveFilePanel(aView, aDesc, theTypes);
        return file != null ? file.getPath() : null;
    }

    /**
     * Shows an Open panel for given description and types.
     */
    public static WebFile showOpenFilePanel(View aView, String aDesc, String... theTypes)
    {
        FilePanel filePanel = new FilePanel();
        filePanel.setDesc(aDesc);
        filePanel.setTypes(theTypes);
        return filePanel.showFilePanel(aView);
    }

    /**
     * Shows a Save panel for given description and types.
     */
    public static WebFile showSaveFilePanel(View aView, String aDesc, String... theTypes)
    {
        FilePanel filePanel = new FilePanel();
        filePanel.setDesc(aDesc);
        filePanel.setTypes(theTypes);
        filePanel.setSaving(true);
        return filePanel.showFilePanel(aView);
    }

    /**
     * Returns the sites.
     */
    public static WebSite[] getDefaultSites()
    {
        if (_defaultSites != null) return _defaultSites;

        // Init to local site
        WebSite localSite = WebSitePaneUtils.getLocalFileSystemSite();
        WebSite[] defaultSites = new WebSite[] { localSite };

        // Set/return
        return _defaultSites = defaultSites;
    }

    /**
     * Adds a site.
     */
    public static void addDefaultSite(WebSite aSite)
    {
        // Get sites
        WebSite[] defaultSites = getDefaultSites();

        // Make sure we don't double-add DropBoxSite (fix this!)
        if (aSite instanceof DropBoxSite) {
            WebSite oldDropBoxSite = ArrayUtils.findMatch(defaultSites, site -> site instanceof DropBoxSite);
            if (oldDropBoxSite != null)
                removeDefaultSite(oldDropBoxSite);
        }

        // Add site
        int index = aSite instanceof RecentFilesSite ? 0 : _defaultSites.length;
        _defaultSites = ArrayUtils.addId(_defaultSites, aSite, index);
    }

    /**
     * Removes a site.
     */
    public static void removeDefaultSite(WebSite aSite)
    {
        getDefaultSites();
        _defaultSites = ArrayUtils.removeId(_defaultSites, aSite);
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
        if (aSite instanceof DropBoxSite)
            return "Cloud Files";
        return "Files";
    }
}