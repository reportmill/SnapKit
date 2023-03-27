/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Color;
import snap.view.*;
import snap.web.WebFile;
import snap.web.WebSite;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to select a file to open or save.
 */
public class FilesBrowser extends FilesPane {

    // The Directory ComboBox
    protected ComboBox<WebFile>  _dirComboBox;

    // The FileBrowser
    private BrowserView<WebFile>  _fileBrowser;

    // The InputText
    protected TextField  _inputText;

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
            WebFile targFile = getTargFile();
            WebFile selFile = targFile != null ? targFile : getSelFile();
            if (selFile != null) {

                // If dir, just update SelFile
                if (selFile.isDir())
                    setSelFile(selFile);

                // Otherwise, fire action
                else fireActionEvent(anEvent);
            }

            // Consume event
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
     * Override to reset browser items.
     */
    @Override
    public void setSite(WebSite aSite)
    {
        // Do normal version
        if (aSite == _site) return;
        super.setSite(aSite);

        // Reset FilesBrowser.Items
        setSiteFilesInUI();
    }

    /**
     * Called to set the FilesPane WebFiles.
     */
    @Override
    protected void setSiteFilesInUI()
    {
        // If UI not loaded, just return
        if (_fileBrowser == null) return;

        // Get root dir files and set in browser
        WebFile rootDir = getSite().getRootDir();
        WebFile[] dirFiles = rootDir.getFiles();
        WebFile[] dirFilesFiltered = FilesBrowserUtils.getVisibleFiles(dirFiles);
        _fileBrowser.setItems(dirFilesFiltered);

        // If SelFile, set
        WebFile selFile = getSelFile();
        if (selFile != null)
            _fileBrowser.setSelItem(selFile);
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
        // Get file for InputText
        WebFile inputTextFile = FilesBrowserUtils.getInputTextAsFile(this);

        // If not valid and opening file, try file completion
        if (inputTextFile == null && isOpening())
            inputTextFile = FilesBrowserUtils.performFileCompletionOnInputText(this);

        // Set the target file
        setTargFile(inputTextFile);
    }

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
        if (file == null || file.isDir() || isValidFile(file))
            return;

        aCell.setEnabled(false);
        aCell.setTextFill(Color.LIGHTGRAY);
    }
}