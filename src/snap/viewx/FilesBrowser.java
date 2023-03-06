/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.util.ArrayUtils;
import snap.util.FilePathUtils;
import snap.util.StringUtils;
import snap.view.*;
import snap.web.WebFile;
import snap.web.WebSite;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to select a file to open or save.
 */
public class FilesBrowser extends ViewOwner {

    // The site used to reference files
    private WebSite  _site;

    // The current file
    private WebFile  _file;

    // The current directory
    private WebFile  _dir;

    // Whether choosing file for save
    private boolean  _saving;

    // The file types
    private String[]  _types;

    // The Directory ComboBox
    private ComboBox<WebFile>  _dirComboBox;

    // The FileBrowser
    private BrowserView<WebFile>  _fileBrowser;

    // The FileText
    private TextField  _fileText;

    /**
     * Constructor.
     */
    public FilesBrowser()
    {
        super();

        // Set default site
        _site = FilesBrowserUtils.getLocalFileSystemSite();
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
    }

    /**
     * Returns the current directory.
     */
    public WebFile getDir()  { return _dir; }

    /**
     * Sets the current directory.
     */
    public void setDir(WebFile aFile)
    {
        // Set dir and clear file
        _dir = aFile;
        _file = null;

        // Reset dir file to get latest listing
        if (_dir != null) _dir.resetContent();

        // Reset UI
        resetLater();
        resetDirComboBox();
    }

    /**
     * Returns the current file.
     */
    public WebFile getFile()  { return _file; }

    /**
     * Sets the current file.
     */
    public void setFile(WebFile aFile)
    {
        // If no file, use home dir
        if (aFile == null)
            aFile = getFileForPath(getHomeDirPath());

        // If file is dir, do that instead
        if (aFile != null && aFile.isDir()) {
            setDir(aFile);
            return;
        }

        // Set file and dir
        _file = aFile;
        _dir = aFile != null ? aFile.getParent() : null;

        // Reset UI
        resetLater();
        resetDirComboBox();
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
        _types = new String[theExts.length];
        for (int i = 0; i < theExts.length; i++) {
            String type = theExts[i].trim().toLowerCase();
            if (type.startsWith("."))
                type = type.substring(1);
            _types[i] = type;
        }
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get FileBrowser and configure
        _fileBrowser = getView("FileBrowser", BrowserView.class);
        _fileBrowser.setRowHeight(22);
        _fileBrowser.addEventFilter(e -> runLater(() -> fileBrowserMouseReleased(e)), MouseRelease);
        _fileBrowser.setResolver(new FilesBrowserUtils.FileResolver());
        _fileBrowser.setCellConfigure(item -> configureFileBrowserCell(item));

        // Set FileBrowser Items, SelItem
        WebFile rootDir = getSite().getRootDir();
        WebFile[] dirFiles = rootDir.getFiles();
        WebFile[] dirFilesFiltered = FilesBrowserUtils.getVisibleFiles(dirFiles);
        _fileBrowser.setItems(dirFilesFiltered);
        _fileBrowser.setSelItem(getFile() != null ? getFile() : getDir());

        // Get/configure DirComboBox
        _dirComboBox = getView("DirComboBox", ComboBox.class);
        _dirComboBox.setItemTextFunction(item -> item.isRoot() ? "Root Directory" : item.getName());
        _dirComboBox.getListView().setRowHeight(24);
        List<WebFile> dirs = new ArrayList<>();
        for (WebFile dir = getDir(); dir != null; dir = dir.getParent())
            dirs.add(dir);
        _dirComboBox.setItems(dirs);
        _dirComboBox.setSelIndex(0);

        // Get FileText
        _fileText = getView("FileText", TextField.class);
        _fileText.setText(getFile() != null ? getFile().getName() : null);
        _fileText.selectAll();
        setFirstFocus(_fileText);

        // Set handler to update DialogBox.ConfirmEnabled when text changes
        _fileText.addEventHandler(e -> runLater(() -> fileTextDidKeyRelease()), KeyRelease);
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update FileBrowser
        WebFile file = getFile();
        WebFile fileOrDir = file != null ? file : getDir();
        _fileBrowser.setSelItem(fileOrDir);

        // Update FileText
        String fileName = file != null ? file.getName() : null;
        _fileText.setText(fileName);
        _fileText.selectAll();

        // Update ConfirmEnabled
        boolean fileTextFileValid = isFileTextFileValid();
        setConfirmEnabled(fileTextFileValid);
    }

    /**
     * Resets the DirComboBox.
     */
    protected void resetDirComboBox()
    {
        if (!isUISet()) return;

        // Update DirComboBox
        List<WebFile> dirs = new ArrayList<>();
        for (WebFile dir = getDir(); dir != null; dir = dir.getParent())
            dirs.add(dir);
        _dirComboBox.setItems(dirs);
        _dirComboBox.setSelIndex(0);
    }

    /**
     * Respond to UI changes.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle FileBrowser
        if (anEvent.equals("FileBrowser")) {
            WebFile file = _fileBrowser.getSelItem();
            setFile(file);
        }

        // Handle FileText: If directory, set
        if (anEvent.equals("FileText")) {
            WebFile file = getFileTextFile();
            if (file != null && file.isDir())
                setFile(file);
            else if (isFileTextFileValid())
                fireActionEvent(anEvent);
            anEvent.consume();
        }

        // Handle HomeButton
        if (anEvent.equals("HomeButton")) {
            WebFile homeDir = getFileForPath(getHomeDirPath());
            setFile(homeDir);
        }

        // Handle DirComboBox
        if (anEvent.equals("DirComboBox"))
            setFile(_dirComboBox.getSelItem());
    }

    /**
     * Called when FileBrowser gets MouseRelease.
     */
    private void fileBrowserMouseReleased(ViewEvent anEvent)
    {
        // If double-click and valid file, do confirm
        if (anEvent.getClickCount() == 2)
            fireActionEvent(anEvent);
    }

    /**
     * Configures a FileBrowser cell.
     */
    protected void configureFileBrowserCell(ListCell<WebFile> aCell)
    {
        WebFile file = aCell.getItem();
        if (file == null || file.isDir() || ArrayUtils.contains(getTypes(), file.getType()))
            return;

        aCell.setEnabled(false);
        aCell.setTextFill(Color.LIGHTGRAY);
    }

    /**
     * Returns the FileText path.
     */
    protected String getFileTextPath()
    {
        // Get FileText string
        String fileText = _fileText.getText().trim();

        // If empty just return dir path
        if (fileText.length() == 0) {
            WebFile dir = getDir();
            return dir.getPath();
        }

        // If starts with ~ return home dir
        if (fileText.startsWith("~"))
            return getHomeDirPath();

        // If starts with '..', return parent dir
        if (fileText.startsWith("..")) {
            WebFile file = getFile();
            WebFile dir = getDir();
            if (file != null)
                return dir.getPath();
            if (dir != null && dir.getParent() != null)
                return dir.getParent().getPath();
            return "/";
        }

        // If starts with FileSeparator, just return
        if (fileText.startsWith("/") || fileText.startsWith("\\"))
            return fileText;

        // Get path
        WebFile dir = getDir();
        String path = FilePathUtils.getChild(dir.getPath(), fileText);
        return path;
    }

    /**
     * Returns the FileText path.
     */
    protected WebFile getFileTextFile()
    {
        // Get path and file for FileText
        String path = getFileTextPath();
        WebFile file = getFileForPath(path);

        // If opening a file that doesn't exist, see if it just needs an extension
        if (file == null && isOpening() && !path.contains(".")) {
            path += getType();
            file = getFileForPath(path);
        }

        // If saving, make sure path has extension and create
        if (file == null && isSaving()) {
            if (!path.contains("."))
                path += '.' + getType();
            //String dpath = FilePathUtils.getParent(path);
            //WebFile dir = getFile(dpath);
            //if (dir!=null && dir.isDir())
            file = getSite().createFileForPath(path, false);
        }

        // Return file
        return file;
    }

    /**
     * Returns the FileText path.
     */
    protected boolean isFileTextFileValid()
    {
        // If saving just return
        if (isSaving()) {
            String fileTextPath = _fileText.getText().trim();
            return fileTextPath.length() > 0;
        }

        // Get file for path based on FilePanel Dir and FileText (filename) - just return false if null
        WebFile file = getFileTextFile();
        if (file == null)
            return false;

        // If file is plain file and matches requested type, return true
        if (file.isFile() && ArrayUtils.contains(getTypes(), file.getType()))
            return true;

        // Return
        return false;
    }

    /**
     * Returns a file for a path.
     */
    private WebFile getFileForPath(String aPath)
    {
        WebSite site = getSite();
        return site.getFileForPath(aPath);
    }

    /**
     * Returns a file completion file if found.
     */
    private WebFile getFileCompletionForPath(String aPath)
    {
        // Get directory for path and file name
        String dirPath = FilePathUtils.getParent(aPath);
        String fileName = FilePathUtils.getFileName(aPath);
        WebFile dir = getFileForPath(dirPath);
        if (dir == null)
            return null;

        // Look for completion file of any requested type (types are checked in order to allow for precedence)
        for (String type : getTypes()) {
            for (WebFile file : dir.getFiles()) {
                if (StringUtils.startsWithIC(file.getName(), fileName) && file.getType().equals(type))
                    return file;
            }
        }

        // Look for completion of type dir
        for (WebFile file : dir.getFiles()) {
            if (StringUtils.startsWithIC(file.getName(), fileName) && file.isDir())
                return file;
        }

        // Return not found
        return null;
    }

    /**
     * Called after FileText KeyRelease.
     */
    private void fileTextDidKeyRelease()
    {
        // Get whether FileTextFile is valid (exists and is right type)
        boolean fileTextFileValid = isFileTextFileValid();

        // If not valid and opening, check for completion
        if (!fileTextFileValid && isOpening()) {
            WebFile file = getFileTextFile();
            String fileText = _fileText.getText().trim();
            String path = getFileTextPath();
            WebFile cfile = file == null && fileText.length() > 0 ? getFileCompletionForPath(path) : null;

            // If completion found, set filename remainder in FileText and select
            if (cfile != null) {
                String cpath = cfile.getPath();
                String cname = cfile.getName();
                String completion = StringUtils.startsWithIC(path, fileText) ? cpath : cname;
                _fileText.setCompletionText(completion);
                fileTextFileValid = true;
            }
        }

        // Set confirm enabled
        setConfirmEnabled(fileTextFileValid);
    }

    /**
     * Called on FileBrowser double-click or FileText enter key.
     */
    protected void fireActionEvent(ViewEvent anEvent)  { }

    /**
     * Called when
     */
    protected void setConfirmEnabled(boolean fileTextFileValid) { }

    /**
     * Returns the home directory path.
     */
    private String getHomeDirPath()
    {
        WebSite site = getSite();
        return FilesBrowserUtils.getHomeDirPathForSite(site);
    }
}