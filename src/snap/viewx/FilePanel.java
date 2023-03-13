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

    // The FilesBrowser
    private FilesBrowser  _filesBrowser;

    // The currently selected site
    private WebSite  _selSite;

    // The DialogBox
    private DialogBox  _dialogBox;

    // The tab bar holding sites
    private TabBar  _sitesTabBar;

    // A view to animate FilesBrowser changes
    private TransitionPane  _transitionPane;

    // Map of existing/cached file browsers
    private Map<WebSite,FilesBrowser>  _filesBrowsers = new HashMap<>();

    // Listener for FileBrowser prop changes
    private PropChangeListener  _fileBrowserPropChangeLsnr = pc -> filesBrowserDidPropChange();

    // The sites
    private static WebSite[]  _defaultSites;

    /**
     * Constructor.
     */
    public FilePanel()
    {
        super();

        // Get sites
        _sites = getDefaultSites();
        _selSite = FilesBrowserUtils.getLocalFileSystemSite();
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

        // Get/set FilesBrowser
        FilesBrowser filesBrowser = getFilesBrowserForSite(aSite);
        setFilesBrowser(filesBrowser);
    }

    /**
     * Runs a file chooser that remembers last open file and size.
     */
    public WebFile showFilePanel(View aView)
    {
        // Get UI
        View filesPanelUI = getUI();

        // If no file/dir set, set from RecentPath (prefs)
        WebFile selDir = _filesBrowser.getSelDir();
        if (selDir == null) {
            WebFile recentFile = RecentFiles.getRecentFileForType(getType());
            if (recentFile != null) {
                WebSite recentFileSite = recentFile.getSite();
                setSelSite(recentFileSite);
                _filesBrowser.setSelFile(recentFile);
            }
        }

        // Run code to add new folder button
        if (isSaving())
            runLater(() -> addNewFolderButton());

        // Create/config DialogBox with FilePanel UI
        String title = isSaving() ? "Save Panel" : "Open Panel";
        _dialogBox = new DialogBox(title);
        _dialogBox.setContent(filesPanelUI);
        _dialogBox.setConfirmEnabled(_filesBrowser.getSelOrTargFile() != null);

        // Run FileChooser UI in DialogBox
        boolean value = _dialogBox.showConfirmDialog(aView);
        if (!value)
            return null;

        // Get file and path of selection and save to preferences
        WebFile selFile = _filesBrowser.getSelOrTargFile();

        // Save selected filename in preferences for its type (extension)
        RecentFiles.setRecentFileForType(getType(), selFile);

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
        _sitesTabBar.getTabsBox().setSpacing(5);
        _sitesTabBar.setTabMinWidth(100);
        _sitesTabBar.setFont(Font.Arial14);
        topColView.addChild(_sitesTabBar);

        // Create/add TransitionPane
        _transitionPane = new TransitionPane();
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
        // Add tabs for sites to SitesTabBar
        WebSite[] sites = getSites();
        Tab.Builder tabBuilder = new Tab.Builder(_sitesTabBar);
        for (WebSite site : sites) {
            String siteName = getNameForSite(site);
            tabBuilder.title(siteName).add();
        }

        // Set FilesBrowser for SelSite
        WebSite selSite = getSelSite();
        FilesBrowser filesBrowser = getFilesBrowserForSite(selSite);
        setFilesBrowser(filesBrowser);
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
            setSelSite(newSelSite);
        }
    }

    /**
     * Sets the FilesBrowser.
     */
    private void setFilesBrowser(FilesBrowser filesBrowser)
    {
        // If already set, just return
        if (filesBrowser == _filesBrowser) return;

        // Get/set transition
        TransitionPane.Transition transition = getTransitionForFileBrowsers(_filesBrowser, filesBrowser);
        _transitionPane.setTransition(transition);

        // If old, remove
        if (_filesBrowser != null)
            _filesBrowser.removePropChangeListener(_fileBrowserPropChangeLsnr);

        // Set FilesBrowser
        _filesBrowser = filesBrowser;

        // Update FilesBrowser
        _filesBrowser.setSaving(isSaving());
        _filesBrowser.setTypes(getTypes());

        // Update UI
        _filesBrowser.addPropChangeListener(_fileBrowserPropChangeLsnr);
        View filesBrowserUI = _filesBrowser.getUI();
        _transitionPane.setContent(filesBrowserUI);

        // Update
        filesBrowserDidPropChange();
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
        WebFile newDir = getSelSite().createFileForPath(newDirPath, true);
        newDir.save();

        // Set new dir
        _filesBrowser.setSelFile(newDir);
    }

    /**
     * Returns a FilesBrowser for given site.
     */
    private FilesBrowser getFilesBrowserForSite(WebSite aSite)
    {
        // Get from cache and just return if found
        FilesBrowser filesBrowser = _filesBrowsers.get(aSite);
        if (filesBrowser != null)
            return filesBrowser;

        // Create and add to cache
        filesBrowser = createFilesBrowserForSite(aSite);
        filesBrowser.setSite(aSite);
        _filesBrowsers.put(aSite, filesBrowser);

        // Return
        return filesBrowser;
    }

    /**
     * Creates a FilesBrowser for given site.
     */
    private FilesBrowser createFilesBrowserForSite(WebSite aSite)
    {
        if (aSite instanceof DropBoxSite) {
            return new DropBoxPane() {
                @Override
                protected void fireActionEvent(ViewEvent anEvent)
                {
                    if (_dialogBox.isConfirmEnabled())
                        _dialogBox.confirm();
                }
            };
        }

        return new FilesBrowser() {
            @Override
            protected void fireActionEvent(ViewEvent anEvent)
            {
                if (_dialogBox.isConfirmEnabled())
                    _dialogBox.confirm();
            }
        };
    }

    /**
     * Returns the transition for given FileBrowsers so changing sites will slide left/right when new one selected.
     */
    private TransitionPane.Transition getTransitionForFileBrowsers(FilesBrowser filesBrowser1, FilesBrowser filesBrowser2)
    {
        if (filesBrowser1 == null) return TransitionPane.Instant;
        int index1 = ArrayUtils.indexOf(getSites(), filesBrowser1.getSite());
        int index2 = ArrayUtils.indexOf(getSites(), filesBrowser2.getSite());
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
        _defaultSites = ArrayUtils.addId(_defaultSites, aSite);
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
            return "DropBox Files";
        return "Files";
    }
}