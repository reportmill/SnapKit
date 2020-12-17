/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.gfx.Color;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A class to select a file to open or save.
 */
public class FilePanel extends ViewOwner {

    // The site used to reference files
    private WebSite  _site;
    
    // Whether choosing file for save
    private boolean  _saving;
    
    // The file types
    private String  _types[];
    
    // The description
    private String  _desc;
    
    // The current file
    private WebFile  _file;

    // The current directory
    private WebFile  _dir;
    
    // The Directory ComboBox
    private ComboBox <WebFile>  _dirComboBox;
    
    // The FileBrowser
    private BrowserView <WebFile>  _fileBrowser;
    
    // The FileText
    private TextField  _fileText;
    
    // The DialogBox
    private DialogBox  _dbox;

    // The default site
    private static WebSite _defaultSite;

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
    public void setSaving(boolean aValue)  { _saving = aValue; }

    /**
     * Returns the window title.
     */
    public String getTitle()  { return isSaving() ? "Save Panel" : "Open Panel"; }

    /**
     * Returns the first file types.
     */
    public String getType()  { return _types!=null && _types.length>0 ? _types[0] : null; }

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
        for (int i=0;i<theExts.length;i++) {
            String type = theExts[i].trim().toLowerCase();
            if (type.startsWith(".")) type = type.substring(1);
            _types[i] = type;
        }
    }

    /**
     * Returns the description.
     */
    public String getDesc()  { return _desc; }

    /**
     * Sets the descrption.
     */
    public void setDesc(String aValue)  { _desc = aValue; }

    /**
     * Returns the site currently being browsed.
     */
    public WebSite getSite()
    {
        // If already set, just return
        if (_site!=null) return _site;

        // Get default site
        WebSite site = _dir!=null ? _dir.getSite() : getSiteDefault();
        return _site = site;
    }

    /**
     * Sets the site for the panel.
     */
    public void setSite(WebSite aSite)
    {
        if (aSite==_site) return;
        _site = aSite;
    }

    /**
     * Returns the current directory.
     */
    public WebFile getDir()
    {
        return _dir;
        //if (_dir!=null) return _dir;
        //WebFile defDir = getSite().getRootDir();
        //return _dir = defDir;
    }

    /**
     * Sets the current directory.
     */
    public void setDir(WebFile aFile)
    {
        // Set dir and clear file
        _dir = aFile; _file = null;

        // Reset dir file to get latest listing
        if (_dir!=null) _dir.resetContent();

        // If UI is set, set in browser and text
        setFileInUI();
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
        if (aFile==null)
            aFile = getFile(getHomeDirPath());

        // If file is dir, do that instead
        if (aFile!=null && aFile.isDir()) {
            setDir(aFile); return; }

        // Set file and dir
        _file = aFile;
        _dir = aFile!=null ? aFile.getParent() : null;

        // If UI is set, set in browser and text
        setFileInUI();
    }

    /**
     * Sets the file in the UI.
     */
    protected void setFileInUI()
    {
        if (!isUISet()) return;

        // Update FileBrowser
        _fileBrowser.setSelItem(getFile()!=null ? getFile() : getDir());

        // Update DirComboBox
        List <WebFile> dirs = new ArrayList();
        for (WebFile dir=getDir(); dir!=null; dir=dir.getParent()) dirs.add(dir);
        _dirComboBox.setItems(dirs);
        _dirComboBox.setSelIndex(0);

        // Update FileText
        _fileText.setText(getFile()!=null ? getFile().getName() : null);
        _fileText.selectAll();
        _fileText.requestFocus();
        _dbox.setConfirmEnabled(isFileTextFileValid());
    }

    /**
     * Returns a file for a path.
     */
    WebFile getFile(String aPath)
    {
        WebSite site = getSite(); //WebURL url = WebURL.getURL(aPath);
        WebFile file = site.getFile(aPath); //url.getFile();
        return file;
    }

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
     * Shows the panel.
     */
    public WebFile showSavePanelWeb(View aView)
    {
        setSaving(true);
        return showFilePanelWeb(aView);
    }

    /**
     * Runs a file chooser that remembers last open file and size.
     */
    protected String showFilePanel(View aView)
    {
        WebFile file = showFilePanelWeb(aView);
        return file!=null ? file.getPath() : null;
    }

    /**
     * Runs a file chooser that remembers last open file and size.
     */
    protected WebFile showFilePanelWeb(View aView)
    {
        // If no file/dir set, set from RecentPath (prefs)
        if (getDir()==null) {
            String path = getRecentPath(getType());
            WebFile file = getFile(path);
            setFile(file);
        }

        // Run code to add new folder button
        if (isSaving()) runLater(() -> addNewFolderButton());

        // Run FileChooser UI in DialogBox
        _dbox = new DialogBox(getTitle());
        _dbox.setContent(getUI());
        _dbox.setConfirmEnabled(isFileTextFileValid());
        boolean value = _dbox.showConfirmDialog(aView);
        if (!value)
            return null;

        // Get file and path of selection and save to preferences
        WebFile file = getFileTextFile();
        if (file==null) { System.err.println("FileChooser: null not possible"); return null; }
        String path = file.getPath();

        // Save selected filename in preferences for it's type (extension)
        setRecentPath(getType(), path);

        // If user is trying to save over an existing file, warn them
        boolean save = isSaving();
        if (save && file.getExists()) {

            // Run option panel for whether to overwrite
            DialogBox dbox2 = new DialogBox("Replace File");
            dbox2.setWarningMessage("The file " + path + " already exists. Replace it?");
            dbox2.setOptions("Replace", "Cancel");
            int answer = dbox2.showOptionDialog(aView, "Replace");

            // If user chooses cancel, re-run chooser
            if (answer!=0)
                return showFilePanelWeb(aView);
        }

        // Give focus back to given view
        if (save && aView!=null)
            aView.requestFocus();

        // Return file
        return file;
    }

    /**
     * Adds a new Folder button.
     */
    protected void addNewFolderButton()
    {
        Button btn = new Button("New Folder");
        btn.setMinWidth(100); btn.setMinHeight(24); btn.setName("NewFolderButton");
        btn.setOwner(this);
        _dbox.getButtonBox().addChild(btn, 0);
    }

    /**
     * Returns the filtered files for given list of files.
     */
    protected WebFile[] getFilteredFiles(List <WebFile> theFiles)
    {
        List <WebFile> files = new ArrayList();
        for (WebFile file : theFiles) {
            if (file.getName().startsWith(".")) continue;
            //if (file.isDir() || ArrayUtils.contains(getTypes(), file.getType()))
                files.add(file);
        }
        return files.toArray(new WebFile[0]);
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Get FileBrowser and configure
        _fileBrowser = getView("FileBrowser", BrowserView.class);
        _fileBrowser.setRowHeight(22);
        _fileBrowser.addEventFilter(e -> runLater(() -> fileBrowserMouseReleased(e)), MouseRelease);
        _fileBrowser.setResolver(new FileResolver());
        _fileBrowser.setCellConfigure(itm -> configureFileBrowserCell(itm));

        // Set FileBrowser Items, SelItem
        List<WebFile> dirFiles = getSite().getRootDir().getFiles();
        WebFile dirFilesFiltered[] = getFilteredFiles(dirFiles);
        _fileBrowser.setItems(dirFilesFiltered);
        _fileBrowser.setSelItem(getFile()!=null ? getFile() : getDir());

        // Get/configure DirComboBox
        _dirComboBox = getView("DirComboBox", ComboBox.class);
        _dirComboBox.setItemTextFunction(itm -> itm.isRoot() ? "Root Directory" : itm.getName());
        _dirComboBox.getListView().setRowHeight(24);
        List <WebFile> dirs = new ArrayList();
        for (WebFile dir=getDir(); dir!=null; dir=dir.getParent()) dirs.add(dir);
        _dirComboBox.setItems(dirs);
        _dirComboBox.setSelIndex(0);

        // Get FileText
        _fileText = getView("FileText", TextField.class);
        _fileText.setText(getFile()!=null? getFile().getName() : null);
        _fileText.selectAll();
        setFirstFocus(_fileText);

        // Set handler to update DialogBox.ConfirmEnabled when text changes
        _fileText.addEventHandler(e -> runLater(() -> fileTextDidKeyRelease()), KeyRelease);
    }

    /**
     * Called when FileBrowser gets MouseRelease.
     */
    private void fileBrowserMouseReleased(ViewEvent anEvent)
    {
        // If double-click and valid file, do confirm
        if (anEvent.getClickCount()==2 && _dbox.isConfirmEnabled())
            _dbox.confirm();

        // I don't know about this
        else if (getFile()==null && getDir()!=null) {
            WebFile dir = getDir();
            setFile(dir.getParent());
            setFile(dir);
        }
    }

    /**
     * Configures a FileBrowser cell.
     */
    protected void configureFileBrowserCell(ListCell<WebFile> aCell)
    {
        WebFile file = aCell.getItem(); if (file==null) return;
        if (file==null || file.isDir() || ArrayUtils.contains(getTypes(), file.getType())) return;
        aCell.setEnabled(false);
        aCell.setTextFill(Color.LIGHTGRAY);
    }

    /**
     * Respond to UI changes.
     */
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
            if (file!=null && file.isDir())
                setFile(file);
            else if (isFileTextFileValid())
                _dbox.confirm();
            anEvent.consume();
        }

        // Handle HomeButton
        if(anEvent.equals("HomeButton"))
            setFile(getFile(getHomeDirPath()));

        // Handle DirComboBox
        if (anEvent.equals("DirComboBox"))
            setFile(_dirComboBox.getSelItem());

        // Handle NewFolderButton
        if (anEvent.equals("NewFolderButton")) {
            String name = DialogBox.showInputDialog(getUI(), "New Folder Panel", "Enter name:", null);
            if (name==null) return;
            String path = getDir().getDirPath() + name;
            WebFile newDir = getSite().createFile(path, true);
            newDir.save();
            setDir(getDir().getParent()); setDir(newDir);
        }
    }

    /**
     * Returns the FileText path.
     */
    private String getFileTextPath()
    {
        // Get FileText string
        String ftext = _fileText.getText().trim();

        // If empty just return dir path
        if(ftext==null || ftext.length()==0)
            return getDir().getPath();

        // If starts with ~ return home dir
        if(ftext.startsWith("~"))
            return getHomeDirPath();

        // If starts with '..', return parent dir
        if(ftext.startsWith("..")) {
            if(getFile()!=null)
                return getDir().getPath();
            if(getDir()!=null && getDir().getParent()!=null)
                return getDir().getParent().getPath();
            return "/";
        }

        // If starts with FileSeparator, just return
        if(ftext.startsWith("/") || ftext.startsWith("\\"))
            return ftext;

         // Get path
         String path = FilePathUtils.getChild(getDir().getPath(), ftext);
         return path;
    }

    /**
     * Returns the FileText path.
     */
    private WebFile getFileTextFile()
    {
        // Get path and file for FileText
        String path = getFileTextPath();
        WebFile file = getFile(path);

        // If opening a file that doesn't exists, see if it just needs an extension
        if(file==null && isOpening() && path.indexOf(".")<0) {
            path += getType();
            file = getFile(path);
        }

        // If saving, make sure path has extension and create
        if (file==null && isSaving()) {
            if (path.indexOf(".")<0) path += '.' + getType();
            //String dpath = FilePathUtils.getParent(path);
            //WebFile dir = getFile(dpath);
            //if (dir!=null && dir.isDir())
            file = getSite().createFile(path, false);
        }

        // Return file
        return file;
    }

    /**
     * Returns the FileText path.
     */
    private boolean isFileTextFileValid()
    {
        // If saving just return
        if (isSaving() && getFileTextPath().length()>0)
            return true;

        // Get file for path based on FilePanel Dir and FileText (filename) - just return false if null
        WebFile file = getFileTextFile();
        if (file==null) return false;

        // If file is plain file and matches requested type, return true
        if (file.isFile() && ArrayUtils.contains(getTypes(), file.getType()))
            return true;
        return false;
    }

    /**
     * Returns a file completion file if found.
     */
    private WebFile getFileCompletion(String aPath)
    {
        // Get directory for path and file name
        String dirPath = FilePathUtils.getParent(aPath);
        String fname = FilePathUtils.getFileName(aPath);
        WebFile dir = getFile(dirPath);
        if (dir==null)
            return null;

        // Look for completion file of any requested type (types are checked in order to allow for precidence)
        for (String type : getTypes()) {
            for (WebFile file : dir.getFiles()) {
                if (StringUtils.startsWithIC(file.getName(), fname) && file.getType().equals(type))
                    return file; }
        }

        // Look for completion of type dir
        for (WebFile file : dir.getFiles()) {
            if (StringUtils.startsWithIC(file.getName(), fname) && file.isDir())
                return file; }
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
            String ftext = _fileText.getText().trim();
            String path = getFileTextPath();
            WebFile cfile = file==null && ftext.length()>0 ? getFileCompletion(path) : null;

            // If completion found, set filename remainder in FileText and select
            if (cfile!=null) {
                String cpath = cfile.getPath(), cname = cfile.getName();
                String completion = StringUtils.startsWithIC(path, ftext)? cpath : cname;
                _fileText.setCompletionText(completion);
                fileTextFileValid = true;
            }
        }

        // Set confirm enabled
        _dbox.setConfirmEnabled(fileTextFileValid);
    }

    /**
     * Shows an Open panel for given description and types.
     */
    public static String showOpenPanel(View aView, String aDesc, String ... theTypes)
    {
        FilePanel fp = new FilePanel();
        fp.setDesc(aDesc); fp.setTypes(theTypes);
        return fp.showOpenPanel(aView);
    }

    /**
     * Shows a Save panel for given description and types.
     */
    public static String showSavePanel(View aView, String aDesc, String ... theTypes)
    {
        FilePanel fp = new FilePanel();
        fp.setDesc(aDesc); fp.setTypes(theTypes);
        return fp.showSavePanel(aView);
    }

    /**
     * Shows a Save panel for given description and types.
     */
    public static WebFile showSavePanelWeb(View aView, String aDesc, String ... theTypes)
    {
        FilePanel fp = new FilePanel();
        fp.setDesc(aDesc); fp.setTypes(theTypes);
        return fp.showSavePanelWeb(aView);
    }

    /**
     * Returns the home directory path.
     */
    private static String getHomeDirPath()
    {
        if (!getSiteDefault().getURL().getScheme().equalsIgnoreCase("file"))
            return "/";
        return System.getProperty("user.home");
    }

    /**
     * Returns the most recent path for given type.
     */
    public static String getRecentPath(String aType)
    {
        if (!getSiteDefault().getURL().getScheme().equalsIgnoreCase("file"))
            return "/";

        String defaultPath = getHomeDirPath();
        return Prefs.get().getString("MostRecentDocument." + aType, defaultPath);
    }

    /**
     * Sets the most recent path for given type.
     */
    public static void setRecentPath(String aType, String aPath)
    {
        if (!getSiteDefault().getURL().getScheme().equalsIgnoreCase("file"))
            return;

        Prefs.get().setValue("MostRecentDocument." + aType, aPath);
        Prefs.get().flush();
    }

    /**
     * Returns the default site.
     */
    public static WebSite getSiteDefault()
    {
        if (_defaultSite!=null) return _defaultSite;
        return _defaultSite = WebURL.getURL("/").getSite();
    }

    /**
     * Sets the default site.
     */
    public static void setSiteDefault(WebSite aSite)
    {
        _defaultSite = aSite;
    }

    /**
     * The TreeResolver to provide data to File browser.
     */
    private class FileResolver extends TreeResolver <WebFile> {

        /** Returns the parent of given item. */
        public WebFile getParent(WebFile anItem)  { return anItem.getParent(); }

        /** Whether given object is a parent (has children). */
        public boolean isParent(WebFile anItem)  { return anItem.isDir(); }

        /** Returns the children. */
        public WebFile[] getChildren(WebFile aPar)  { return getFilteredFiles(aPar.getFiles()); }

        /** Returns the text to be used for given item. */
        public String getText(WebFile anItem)  { return anItem.getName(); }
    }
}