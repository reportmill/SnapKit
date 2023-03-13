package snap.viewx;
import snap.util.ArrayUtils;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snap.web.RecentFiles;
import snap.web.WebFile;
import snap.web.WebSite;

/**
 * This class is the base class for WebSite open/save browsers.
 */
public class FilesPane extends ViewOwner {

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

    // The file types
    protected String[]  _types;

    // The FilePanel
    protected FilePanel  _filePanel;

    // Constants for properties
    public static final String SelFile_Prop = "SelFile";
    public static final String SelDir_Prop = "SelDir";
    public static final String TargFile_Prop = "TargFile";

    /**
     * Constructor.
     */
    public FilesPane()
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
        // Cache old file/dir
        WebFile oldSelFile = _selFile;
        WebFile oldSelDir = _selDir;

        // If file is dir, do that instead
        if (aFile != null && aFile.isDir()) {
            _selDir = aFile;
            _selFile = null;
            _selDir.resetContent();
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
     * Returns whether given file is valid.
     */
    public boolean isValidFile(WebFile aFile)
    {
        if (aFile == null)
            return false;
        String[] types = getTypes();
        if (types == null || types.length == 0)
            return true;

        boolean isValid = aFile != null && aFile.isFile() && ArrayUtils.contains(types, aFile.getType());
        return isValid;
    }

    /**
     * Called on FileBrowser double-click or InputText enter key.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        if (_filePanel != null)
            _filePanel.fireActionEvent(anEvent);
    }

    /**
     * Called when Showing changes.
     */
    @Override
    protected void showingChanged()
    {
        if (isShowing())
            runLater(() -> initFilesPane());
    }

    /**
     * Called when Showing changes.
     */
    protected void initFilesPane()
    {
        // Reload files
        WebSite site = getSite();
        WebFile rootDir = site.getRootDir();
        rootDir.resetContent();
        rootDir.getFiles();

        // Set the files in UI
        setSiteFilesInUI();

        // If no file/dir set, set from RecentPath (prefs)
        WebFile selDir = getSelDir();
        if (selDir == null) {
            String type = getType();
            WebFile recentFile = RecentFiles.getRecentFileForType(type);
            if (recentFile != null) {
                WebSite recentFileSite = recentFile.getSite();
                if (recentFileSite == getSite())
                    setSelFile(recentFile);
            }
        }
    }

    /**
     * Called to set the FilesPane WebFiles.
     */
    protected void setSiteFilesInUI()  { }
}
