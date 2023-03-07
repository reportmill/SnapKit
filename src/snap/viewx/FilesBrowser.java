/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.util.ArrayUtils;
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

    // The InputText
    protected TextField  _inputText;

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
        _fileBrowser.setResolver(new FilesBrowserUtils.FileResolver());
        _fileBrowser.setCellConfigure(item -> configureFileBrowserCell(item));
        _fileBrowser.addEventFilter(e -> runLater(() -> fileBrowserMouseReleased(e)), MouseRelease);

        // Set FileBrowser Items
        WebFile rootDir = getSite().getRootDir();
        WebFile[] dirFiles = rootDir.getFiles();
        WebFile[] dirFilesFiltered = FilesBrowserUtils.getVisibleFiles(dirFiles);
        _fileBrowser.setItems(dirFilesFiltered);

        // Get/configure DirComboBox
        _dirComboBox = getView("DirComboBox", ComboBox.class);
        _dirComboBox.setItemTextFunction(item -> item.isRoot() ? "Root Directory" : item.getName());
        _dirComboBox.getListView().setRowHeight(24);

        // Get/configure InputText
        _inputText = getView("InputText", TextField.class);
        _inputText.setFireActionOnFocusLost(false);
        setFirstFocus(_inputText);

        // Initialize InputText
        WebFile selFile = getSelFile();
        String selFileName = selFile != null ? selFile.getName() : null;
        _inputText.setText(selFileName);
        _inputText.selectAll();

        // Set handler to update DialogBox.ConfirmEnabled when text changes
        _inputText.addEventHandler(e -> runLater(() -> inputTextDidKeyRelease()), KeyRelease);
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

        // Update InputText
        String fileName = selFile != null ? selFile.getName() : null;
        _inputText.setText(fileName);
        _inputText.selectAll();

        // Get selected dir and list of parents
        WebFile selDir = getSelDir();
        List<WebFile> selDirs = new ArrayList<>();
        for (WebFile dir = selDir; dir != null; dir = dir.getParent())
            selDirs.add(dir);

        // Update DirComboBox
        _dirComboBox.setItems(selDirs);
        _dirComboBox.setSelItem(selDir);

        // Update ConfirmEnabled
        boolean inputTextFileValid = FilesBrowserUtils.isInputTextFileValid(this);
        setConfirmEnabled(inputTextFileValid);
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

        // Handle InputText: If directory, set
        if (anEvent.equals("InputText")) {

            // If directory was entered, set file
            WebFile inputTextFile = FilesBrowserUtils.getInputTextAsFile(this);
            if (inputTextFile != null && inputTextFile.isDir())
                setSelFile(inputTextFile);

            // If valid filename entered, fire action
            else if (FilesBrowserUtils.isInputTextFileValid(this))
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
     * Called when FileBrowser gets MouseRelease.
     */
    private void fileBrowserMouseReleased(ViewEvent anEvent)
    {
        // If double-click and valid file, do confirm
        if (anEvent.getClickCount() == 2)
            fireActionEvent(anEvent);
    }

    /**
     * Called after InputText KeyRelease.
     */
    private void inputTextDidKeyRelease()
    {
        // Get whether InputText file is valid (exists and is right type)
        boolean inputTextFileValid = FilesBrowserUtils.isInputTextFileValid(this);

        // If not valid and opening, check for completion
        if (!inputTextFileValid && isOpening()) {

            WebFile inputTextFile = FilesBrowserUtils.getInputTextAsFile(this);
            String inputText = FilesBrowserUtils.getInputText(this);
            String inputTextPath = FilesBrowserUtils.getInputTextAsPath(this);
            if (inputTextFile == null && inputText.length() > 0) {
                WebFile completionFile = FilesBrowserUtils.getFileCompletionForPath(this, inputTextPath);

                // If completion found, set filename remainder in InputText and select
                if (completionFile != null) {
                    String completionPath = completionFile.getPath();
                    String completionFilename = completionFile.getName();
                    String completion = StringUtils.startsWithIC(inputTextPath, inputText) ? completionPath : completionFilename;
                    _inputText.setCompletionText(completion);
                    inputTextFileValid = true;
                }
            }
        }

        // Set confirm enabled
        setConfirmEnabled(inputTextFileValid);
    }

    /**
     * Called on FileBrowser double-click or InputText enter key.
     */
    protected void fireActionEvent(ViewEvent anEvent)  { }

    /**
     * Called when
     */
    protected void setConfirmEnabled(boolean inputTextFileValid) { }

    /**
     * Returns a file for a path.
     */
    protected WebFile getFileForPath(String aPath)
    {
        WebSite site = getSite();
        return site.getFileForPath(aPath);
    }

    /**
     * Returns the home directory path.
     */
    protected String getHomeDirPath()
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