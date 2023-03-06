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
    private WebFile  _selFile;

    // The current directory
    private WebFile  _selDir;

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

    // Constants for properties
    public static final String SelFile_Prop = "SelFile";
    public static final String SelDir_Prop = "SelDir";

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

        // If no file, use home dir
        if (aFile == null) {
            String homeDirPath = getHomeDirPath();
            aFile = getFileForPath(homeDirPath);
        }

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

        // Reset UI
        resetLater();
    }

    /**
     * Returns the selected directory.
     */
    public WebFile getSelDir()  { return _selDir; }

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

        // Set FileBrowser Items
        WebFile rootDir = getSite().getRootDir();
        WebFile[] dirFiles = rootDir.getFiles();
        WebFile[] dirFilesFiltered = FilesBrowserUtils.getVisibleFiles(dirFiles);
        _fileBrowser.setItems(dirFilesFiltered);

        // Get/configure DirComboBox
        _dirComboBox = getView("DirComboBox", ComboBox.class);
        _dirComboBox.setItemTextFunction(item -> item.isRoot() ? "Root Directory" : item.getName());
        _dirComboBox.getListView().setRowHeight(24);

        // Get/configure FileText
        _fileText = getView("FileText", TextField.class);
        _fileText.setFireActionOnFocusLost(false);
        setFirstFocus(_fileText);

        // Initialize FileText
        WebFile selFile = getSelFile();
        String selFileName = selFile != null ? selFile.getName() : null;
        _fileText.setText(selFileName);
        _fileText.selectAll();

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
        WebFile selFile = getSelFile();
        WebFile selFileOrDir = selFile != null ? selFile : getSelDir();
        _fileBrowser.setSelItem(selFileOrDir);

        // Update FileText
        String fileName = selFile != null ? selFile.getName() : null;
        _fileText.setText(fileName);
        _fileText.selectAll();

        // Get selected dir and list of parents
        WebFile selDir = getSelDir();
        List<WebFile> selDirs = new ArrayList<>();
        for (WebFile dir = selDir; dir != null; dir = dir.getParent())
            selDirs.add(dir);

        // Update DirComboBox
        _dirComboBox.setItems(selDirs);
        _dirComboBox.setSelItem(selDir);

        // Update ConfirmEnabled
        boolean fileTextFileValid = isFileTextFileValid();
        setConfirmEnabled(fileTextFileValid);
    }

    /**
     * Respond to UI changes.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle FileBrowser
        if (anEvent.equals("FileBrowser")) {
            WebFile newSelFile = _fileBrowser.getSelItem();
            setSelFile(newSelFile);
        }

        // Handle FileText: If directory, set
        if (anEvent.equals("FileText")) {

            // If directory was entered, set file
            WebFile file = getFileTextFile();
            if (file != null && file.isDir())
                setSelFile(file);

            // If valid filename entered, fire action
            else if (isFileTextFileValid())
                fireActionEvent(anEvent);
            anEvent.consume();
        }

        // Handle HomeButton
        if (anEvent.equals("HomeButton")) {
            WebFile homeDir = getFileForPath(getHomeDirPath());
            setSelFile(homeDir);
        }

        // Handle DirComboBox
        if (anEvent.equals("DirComboBox")) {
            WebFile newSelDir = _dirComboBox.getSelItem();
            setSelFile(newSelDir);
        }
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
            WebFile selDir = getSelDir();
            return selDir.getPath();
        }

        // If starts with ~ return home dir
        if (fileText.startsWith("~"))
            return getHomeDirPath();

        // If starts with '..', return parent dir
        if (fileText.startsWith("..")) {
            WebFile selFile = getSelFile();
            WebFile selDir = getSelDir();
            if (selFile != null)
                return selDir.getPath();
            if (selDir != null && selDir.getParent() != null)
                return selDir.getParent().getPath();
            return "/";
        }

        // If starts with FileSeparator, just return
        if (fileText.startsWith("/") || fileText.startsWith("\\"))
            return fileText;

        // Get path
        WebFile selDir = getSelDir();
        String path = FilePathUtils.getChild(selDir.getPath(), fileText);
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
     * Called when FileBrowser gets MouseRelease.
     */
    private void fileBrowserMouseReleased(ViewEvent anEvent)
    {
        // If double-click and valid file, do confirm
        if (anEvent.getClickCount() == 2)
            fireActionEvent(anEvent);
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

        // Get directory files and valid file types
        WebFile[] dirFiles = dir.getFiles();
        String[] fileTypes = getTypes();

        // Look for completion file of any requested type (types are checked in order to allow for precedence)
        for (String type : fileTypes) {
            for (WebFile file : dirFiles) {
                if (StringUtils.startsWithIC(file.getName(), fileName) && file.getType().equals(type))
                    return file;
            }
        }

        // Look for completion of type dir
        for (WebFile file : dirFiles) {
            if (StringUtils.startsWithIC(file.getName(), fileName) && file.isDir())
                return file;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the home directory path.
     */
    private String getHomeDirPath()
    {
        WebSite site = getSite();
        return FilesBrowserUtils.getHomeDirPathForSite(site);
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
}