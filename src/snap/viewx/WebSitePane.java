package snap.viewx;
import snap.util.ArrayUtils;
import snap.view.EventListener;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snap.web.RecentFiles;
import snap.web.WebFile;
import snap.web.WebSite;
import snap.web.WebURL;

import java.util.function.Predicate;

/**
 * This class is the base class for WebSite open/save browsers.
 */
public class WebSitePane extends ViewOwner {

    // The site used to reference files
    protected WebSite  _site;

    // The currently selected file
    protected WebFile  _selFile;

    // The current selected directory
    protected WebFile  _selDir;

    // A file targeted by input text
    protected WebFile  _targFile;

    // Whether choosing file for save
    protected boolean  _saving;

    // A function to determine if a given file is valid
    private Predicate<WebFile> _fileValidator;

    // The file types
    protected String[]  _types;

    // An EventListener to be called when action is fired
    protected EventListener _actionHandler;

    // Constants for properties
    public static final String SelFile_Prop = "SelFile";
    public static final String SelDir_Prop = "SelDir";
    public static final String TargFile_Prop = "TargFile";

    /**
     * Constructor.
     */
    public WebSitePane()
    {
        super();
    }

    /**
     * Returns the site currently being browsed.
     */
    public WebSite getSite()  { return _site; }

    /**
     * Sets the site for the panel.
     */
    public void setSite(WebSite aSite)
    {
        if (aSite == _site) return;
        _site = aSite;

        // Reset selected file
        WebFile rootDir = aSite.getRootDir();
        setSelFile(rootDir);
    }

    /**
     * Returns the selected file.
     */
    public WebFile getSelFile()  { return _selFile; }

    /**
     * Sets the selected file or directory.
     */
    public void setSelFile(WebFile aFile)
    {
        // If already set, just return
        if (aFile == _selFile) return;

        // Cache old file/dir
        WebFile oldSelFile = _selFile;
        WebFile oldSelDir = _selDir;

        // If given file is dir, set SelDir
        if (aFile != null && aFile.isDir()) {
            _selFile = null;
            _selDir = aFile;
        }

        // Set file and dir
        else {
            _selFile = aFile;
            _selDir = aFile != null ? aFile.getParent() : null;
        }

        // Fire prop changes
        if (_selFile != oldSelFile)
            firePropChange(SelFile_Prop, oldSelFile, _selFile);
        if (_selDir != oldSelDir)
            firePropChange(SelDir_Prop, oldSelDir, _selDir);

        // Clear TargFile
        setTargFile(null);

        // Reset UI
        resetLater();
    }

    /**
     * Returns the selected directory.
     */
    public WebFile getSelDir()  { return _selDir; }

    /**
     * Returns the file targeted by the input text.
     */
    public WebFile getTargFile()  { return _targFile; }

    /**
     * Sets the file targeted by the input text.
     */
    public void setTargFile(WebFile aFile)
    {
        if (aFile == _targFile) return;
        firePropChange(TargFile_Prop, _targFile, _targFile = aFile);
    }

    /**
     * Returns the selected or targeted file.
     */
    public WebFile getSelOrTargFile()
    {
        if (isValidFile(_targFile))
            return _targFile;
        if (isValidFile(_selFile))
            return _selFile;
        return null;
    }

    /**
     * Returns whether is opening.
     */
    public boolean isOpening()  { return !_saving; }

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
     * Returns the listener triggered when action event is fired.
     */
    public EventListener getActionHandler()  { return _actionHandler; }

    /**
     * Sets the listener triggered when action event is fired.
     */
    public void setActionHandler(EventListener actionListener)  { _actionHandler = actionListener; }

    /**
     * Returns whether given file is valid.
     */
    public boolean isValidFile(WebFile aFile)
    {
        return _fileValidator == null || _fileValidator.test(aFile);
    }

    /**
     * Called on FileBrowser double-click or InputText enter key.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        if (_actionHandler != null)
            _actionHandler.listenEvent(anEvent);
    }

    /**
     * Override to reset files UI when showing.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        // Do normal version
        if (aValue == isShowing()) return;
        super.setShowing(aValue);

        // Handle Showing: Reset Files UI
        if (aValue)
            runLater(() -> resetFilesUI());
    }

    /**
     * Called when Showing changes.
     */
    protected void resetFilesUI()
    {
        // Set the files in UI
        setSiteFilesInUI();

        // If no file/dir set, set from RecentPath (prefs)
        WebFile selDir = getSelDir();
        if (selDir == null) {
            WebFile newSelFile = getDefaultSelFile();
            setSelFile(newSelFile);
        }
    }

    /**
     * Resets the selected file.
     */
    protected WebFile getDefaultSelFile()
    {
        // Get recent file
        WebURL[] recentUrls = getRecentUrlsForSiteAndFileValidator();
        WebURL recentURL = recentUrls.length > 0 ? recentUrls[0] : null;
        WebFile defaultSelFile = recentURL != null ? recentURL.getFile() : null;

        // If null, just root dir
        if (defaultSelFile == null)
            defaultSelFile = getSite().getRootDir();

        // Return
        return defaultSelFile;
    }

    /**
     * Returns valid recent URLs.
     */
    private WebURL[] getRecentUrlsForSiteAndFileValidator()
    {
        // Get recent URLs for site
        WebURL[] recentURLs = RecentFiles.getURLs();
        WebSite site = getSite();
        WebURL[] recentUrlsForSite = ArrayUtils.filter(recentURLs, url -> url.getSite() == site);

        // If Types set, do simple version on URLs to avoid resolving files
        String[] types = getTypes();
        if (types != null) {
            WebURL[] validUrlsForSite = ArrayUtils.filter(recentUrlsForSite, url -> ArrayUtils.contains(types, url.getType()));
            return validUrlsForSite;
        }

        // Get RecentURLs for site
        Predicate<WebFile> fileValidator = getFileValidator();
        if (fileValidator == null)
            return recentURLs;

        // Get valid URLs
        WebFile[] recentFilesForSite = ArrayUtils.map(recentUrlsForSite, url -> url.getFile(), WebFile.class);
        WebFile[] validRecentFiles = ArrayUtils.filter(recentFilesForSite, fileValidator);
        WebURL[] validRecentURLs = ArrayUtils.map(validRecentFiles, file -> file.getURL(), WebURL.class);
        return validRecentURLs;
    }

    /**
     * Called to set the WebFiles in UI.
     */
    protected void setSiteFilesInUI()  { }
}
