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

/**
 * A class to select a file to open or save.
 */
public class FilePanel extends ViewOwner {

    // Whether choosing file for save
    private boolean  _saving;

    // The file types
    private String[]  _types;

    // The description
    private String  _desc;

    // The sites
    private WebSite[]  _sites;

    // ActionHandler
    private EventListener  _actionHandler;

    // The currently selected site
    private WebSite  _selSite;

    // The FilesPane
    private FilesPane  _filesPane;

    // Whether confirm enabled
    private boolean  _confirmEnabled;

    // The DialogBox
    private DialogBox  _dialogBox;

    // The tab bar holding sites
    private TabBar  _sitesTabBar;

    // A view to animate FilesPane changes
    private TransitionPane  _transitionPane;

    // Map of existing/cached FilesPanes
    private Map<WebSite,FilesPane>  _filesPanes = new HashMap<>();

    // Listener for FilePane prop changes
    private PropChangeListener  _filesPanePropChangeLsnr = pc -> filesPaneDidPropChange();

    // The sites
    private static WebSite[]  _defaultSites;

    // Constants for properties
    public static final String ConfirmEnabled_Prop = "ConfirmEnabled";

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
     * Returns the first file types.
     */
    public String getType()
    {
        return _types != null && _types.length > 0 ? _types[0] : null;
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
        _types = ArrayUtils.map(theExts, type -> FilesBrowserUtils.normalizeType(type), String.class);
    }

    /**
     * Sets the description.
     */
    public void setDesc(String aValue)  { _desc = aValue; }

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
        _selSite = aSite;

        // Get/set FilesPane
        if (isUISet()) {
            FilesPane filesPane = getFilesPaneForSite(aSite);
            setFilesPane(filesPane);
        }
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
     * Returns the selected file.
     */
    public WebFile getSelFile()
    {
        // Get current FilesPane selected/targeted file
        WebFile selFile = getSelFileImpl();

        // Add to recent files
        WebURL selFileURL = selFile != null ? selFile.getURL() : null;
        if (selFileURL != null)
            RecentFiles.addURL(selFileURL);

        // Return
        return selFile;
    }

    /**
     * Returns the selected file.
     */
    protected WebFile getSelFileImpl()
    {
        // Get current FilesPane selected/targeted file
        WebFile selFile = _filesPane.getSelOrTargFile();

        // If link file, replace with real
        if (selFile != null && selFile.getLinkFile() != null)
            selFile = selFile.getRealFile();

        // Return
        return selFile;
    }

    /**
     * Returns whether confirm enabled.
     */
    public boolean isConfirmEnabled()  { return _confirmEnabled; }

    /**
     * Sets whether confirm enabled.
     */
    public void setConfirmEnabled(boolean aValue)
    {
        if (aValue == _confirmEnabled) return;
        firePropChange(ConfirmEnabled_Prop, _confirmEnabled, _confirmEnabled = aValue);

        if (_dialogBox != null)
            _dialogBox.setConfirmEnabled(aValue);
    }

    /**
     * Runs a file chooser that remembers last open file and size.
     */
    public WebFile showFilePanel(View aView)
    {
        // Get UI
        View filesPanelUI = getUI();

        // Run code to add new folder button
        if (isSaving())
            runLater(() -> addNewFolderButton());

        // Create/config DialogBox with FilePanel UI
        String title = isSaving() ? "Save Panel" : "Open Panel";
        _dialogBox = new DialogBox(title);
        _dialogBox.setContent(filesPanelUI);
        setConfirmEnabled(_filesPane.getSelOrTargFile() != null);
        _filesPane._filePanel = this;

        // Run FileChooser UI in DialogBox
        boolean value = _dialogBox.showConfirmDialog(aView);
        if (!value)
            return null;

        // Get file and path of selection and save to preferences
        WebFile selFile = getSelFile();

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

        // Set FilesPane for SelSite
        WebSite selSite = getSelSite();
        FilesPane filesPane = getFilesPaneForSite(selSite);
        setFilesPane(filesPane);
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
        WebFile selFile = getSelFileImpl();
        boolean isRecentFilesSite = selFile != null && getSelSite() instanceof RecentFilesSite;

        // Get site at SitesTabBar.SelIndex and set
        int selIndex = _sitesTabBar.getSelIndex();
        WebSite[] fileSites = getSites();
        WebSite newSelSite = fileSites[selIndex];
        setSelSite(newSelSite);

        // If recent files site had selected file from this site, select in new FilesPane
        if (isRecentFilesSite) {
            WebSite selFileSite = selFile.getSite();
            if (selFileSite == newSelSite)
                _filesPane.setSelFile(selFile);
        }
    }

    /**
     * Sets the FilesPane.
     */
    private void setFilesPane(FilesPane filesPane)
    {
        // If already set, just return
        if (filesPane == _filesPane) return;

        // Get/set transition
        TransitionPane.Transition transition = getTransitionForFileBrowsers(_filesPane, filesPane);
        _transitionPane.setTransition(transition);

        // If old, remove
        if (_filesPane != null)
            _filesPane.removePropChangeListener(_filesPanePropChangeLsnr);

        // Set FilesPane
        _filesPane = filesPane;

        // Update FilesPane
        _filesPane.setSaving(isSaving());
        _filesPane.setTypes(getTypes());

        // Update UI
        _filesPane.addPropChangeListener(_filesPanePropChangeLsnr);
        View filesPaneUI = _filesPane.getUI();
        _transitionPane.setContent(filesPaneUI);

        // Update
        filesPaneDidPropChange();
    }

    /**
     * Called when FilesPane does prop change.
     */
    private void filesPaneDidPropChange()
    {
        WebFile selOrTargFile = _filesPane.getSelOrTargFile();
        boolean isFileSet = selOrTargFile != null;
        setConfirmEnabled(isFileSet);
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
        WebFile selDir = _filesPane.getSelDir();
        String newDirPath = selDir.getDirPath() + newDirName;
        WebFile newDir = getSelSite().createFileForPath(newDirPath, true);
        newDir.save();

        // Set new dir
        _filesPane.setSelFile(newDir);
    }

    /**
     * Returns a FilesPane for given site.
     */
    private FilesPane getFilesPaneForSite(WebSite aSite)
    {
        // Get from cache and just return if found
        FilesPane filesPane = _filesPanes.get(aSite);
        if (filesPane != null)
            return filesPane;

        // Create and add to cache
        filesPane = createFilesPaneForSite(aSite);
        filesPane.setSite(aSite);
        filesPane._filePanel = this;
        _filesPanes.put(aSite, filesPane);

        // Return
        return filesPane;
    }

    /**
     * Creates a FilesPane for given site.
     */
    private FilesPane createFilesPaneForSite(WebSite aSite)
    {
        if (aSite instanceof RecentFilesSite)
            return new RecentFilesPane();
        if (aSite instanceof DropBoxSite)
            return new DropBoxPane();
        return new FilesBrowser();
    }

    /**
     * Returns the transition for given FilesPanes so changing sites will slide left/right when new one selected.
     */
    private TransitionPane.Transition getTransitionForFileBrowsers(FilesPane filesPane1, FilesPane filesPane2)
    {
        if (filesPane1 == null) return TransitionPane.Instant;
        int index1 = ArrayUtils.indexOf(getSites(), filesPane1.getSite());
        int index2 = ArrayUtils.indexOf(getSites(), filesPane2.getSite());
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