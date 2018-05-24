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
    
    // Whether choosing file for save
    boolean                _saving;
    
    // The file types
    String                 _types[];
    
    // The description
    String                 _desc;
    
    // The current file
    WebFile                _file;

    // The current directory
    WebFile                _dir;
    
    // The Directory ComboBox
    ComboBox <WebFile>     _dirComboBox;
    
    // The FileBrowser
    BrowserView <WebFile>  _fileBrowser;
    
    // The FileText
    TextField              _fileText;
    
    // The DialogBox
    DialogBox              _dbox;

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
public String getTitle()  { return isSaving()? "Save Panel" : "Open Panel"; }

/**
 * Returns the first file types.
 */
public String getType()  { return _types!=null && _types.length>0? _types[0] : null; }

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
    for(int i=0;i<theExts.length;i++) { String type = theExts[i].trim().toLowerCase();
        if(type.startsWith(".")) type = type.substring(1); _types[i] = type; }
}

// Should go soon
public String[] getExts()  { return _types; }
public void setExts(String ... theExts)  { setTypes(theExts); }

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
public WebSite getSite()  { return _dir!=null? _dir.getSite() : WebURL.getURL("/").getSite(); }

/**
 * Returns the current directory.
 */
public WebFile getDir()  { return _dir; }

/**
 * Sets the current directory.
 */
public void setDir(WebFile aFile)
{
    _dir = aFile;
    _file = null;
    
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
    if(aFile==null)
        aFile = getFile(getHomeDirPath());

    // If file is dir, do that instead
    if(aFile!=null && aFile.isDir()) {
        setDir(aFile); return; }

    // Set file and dir
    _file = aFile;
    _dir = aFile!=null? aFile.getParent() : null;
    
    // If UI is set, set in browser and text
    setFileInUI();
}

/**
 * Sets the file in the UI.
 */
protected void setFileInUI()
{
    if(!isUISet()) return;
    
    // Update FileBrowser
    _fileBrowser.setSelItem(getFile()!=null? getFile() : getDir());
    
    // Update DirComboBox
    List <WebFile> dirs = new ArrayList(); for(WebFile dir=getDir();dir!=null;dir=dir.getParent()) dirs.add(dir);
    _dirComboBox.setItems(dirs);
    _dirComboBox.setSelIndex(0);
    
    // Update FileText
    _fileText.setText(getFile()!=null? getFile().getName() : null);
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
public String showOpenPanel(View aView)  { setSaving(false); return showFilePanel(aView); }

/**
 * Shows the panel.
 */
public String showSavePanel(View aView)  { setSaving(true); return showFilePanel(aView); }

/**
 * Runs a file chooser that remembers last open file and size.
 */
protected String showFilePanel(View aView)
{
    // Get component
    RootView rview = aView!=null? aView.getRootView() : null;
    
    // Declare local variable for chooser
    //chooser.setFileFilter(new UIUtilsFileFilter(theExtensions, aDesc));
    
    // If no file/dir set, set from RecentPath (prefs)
    if(getDir()==null) {
        String path = getRecentPath(getType());
        WebFile file = getFile(path);
        setFile(file);
   }

    // Run FileChooser UI in DialogBox
    _dbox = new DialogBox(getTitle()); _dbox.setContent(getUI()); _dbox.setConfirmEnabled(isFileTextFileValid());
    boolean value = _dbox.showConfirmDialog(aView);
    if(!value)
        return null;
    
    // Get file and path of selection and save to preferences
    WebFile file = getFileTextFile(); if(file==null) {System.err.println("FileChooser: null not possible");return null;}
    String path = file.getPath();
    
    // Save selected filename in preferences for it's type (extension)
    setRecentPath(getType(), path);
            
    // If user is trying to save over an existing file, warn them
    boolean save = isSaving();
    if(save && file.getExists()) {
        
        // Run option panel for whether to overwrite
        DialogBox dbox2 = new DialogBox("Replace File");
        dbox2.setWarningMessage("The file " + path + " already exists. Replace it?");
        dbox2.setOptions("Replace", "Cancel");
        int answer = dbox2.showOptionDialog(aView, "Replace");
        
        // If user chooses cancel, re-run chooser
        if(answer!=0)
            return showFilePanel(aView);
    }
        
    // Give focus back to given view
    if(save && aView!=null)
        aView.requestFocus();

    // Return path
    return path;
}

/**
 * Returns the filtered files for given list of files.
 */
protected WebFile[] getFilteredFiles(List <WebFile> theFiles)
{
    List <WebFile> files = new ArrayList();
    for(WebFile file : theFiles) {
        if(file.getName().startsWith(".")) continue;
        //if(file.isDir() || ArrayUtils.contains(getTypes(), file.getType()))
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
    _fileBrowser = getView("FileBrowser", BrowserView.class); _fileBrowser.setRowHeight(22);
    enableEvents(_fileBrowser, MouseRelease);
    _fileBrowser.setResolver(new FileResolver());
    _fileBrowser.setCellConfigure(itm -> configureFileBrowserCell(itm));
    
    // Set FileBrowser Items, SelItem
    _fileBrowser.setItems(getFilteredFiles(getDir().getSite().getRootDir().getFiles()));
    _fileBrowser.setSelItem(getFile()!=null? getFile() : getDir());
    
    // Get/configure DirComboBox
    _dirComboBox = getView("DirComboBox", ComboBox.class);
    _dirComboBox.setItemTextFunction(itm -> itm.isRoot()? "Root Directory" : itm.getName());
    _dirComboBox.getListView().setRowHeight(24);
    List <WebFile> dirs = new ArrayList();
    for(WebFile dir=getDir();dir!=null;dir=dir.getParent()) dirs.add(dir);
    _dirComboBox.setItems(dirs);
    _dirComboBox.setSelIndex(0);
    
    // Get FileText
    _fileText = getView("FileText", TextField.class);
    _fileText.setText(getFile()!=null? getFile().getName() : null);
    _fileText.selectAll();
    setFirstFocus(_fileText);
    
    // Set handler to update DialogBox.ConfirmEnabled when text changes
    _fileText.addEventHandler(e -> runLater(() -> handleFileTextKeyReleased()), KeyRelease);
}

/**
 * Configures a FileBrowser cell.
 */
protected void configureFileBrowserCell(ListCell<WebFile> aCell)
{
    WebFile file = aCell.getItem(); if(file==null) return;
    if(file==null || file.isDir() || ArrayUtils.contains(getTypes(), file.getType())) return;
    aCell.setEnabled(false);
    aCell.setTextFill(Color.LIGHTGRAY);
}

/**
 * Respond to UI changes.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle FileBrowser
    if(anEvent.equals("FileBrowser")) {
        
        // Handle MouseRelease
        if(anEvent.isMouseRelease()) {
            if(anEvent.getClickCount()==2 && _dbox.isConfirmEnabled())
                _dbox.confirm();
        }
        
        // Handle Action
        else {
            WebFile file = _fileBrowser.getSelItem();
            setFile(file);
        }
    }
    
    // Handle FileText: If directory, set
    if(anEvent.equals("FileText")) {
        WebFile file = getFileTextFile();
        if(file!=null && file.isDir())
            setFile(file);
    }
    
    // Handle HomeButton
    if(anEvent.equals("HomeButton"))
        setFile(getFile(getHomeDirPath()));
        
    // Handle DirComboBox
    if(anEvent.equals("DirComboBox"))
        setFile(_dirComboBox.getSelItem());
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
    if(file==null && isSaving()) {
        if(path.indexOf(".")<0) path += '.' + getType();
        String dpath = FilePathUtils.getParent(path);
        WebFile dir = getFile(dpath);
        if(dir!=null && dir.isDir())
            file = dir.getSite().createFile(path, false);
    }
    
    // Return file
    return file;
}

/**
 * Returns the FileText path.
 */
private boolean isFileTextFileValid()
{
    WebFile file = getFileTextFile();
    return file!=null && file.isFile() && ArrayUtils.contains(getTypes(), file.getType());
}

/**
 * Returns a file completion.
 */
private String getFileCompletion(String aPath)
{
    String dirPath = FilePathUtils.getParent(aPath), fname = FilePathUtils.getFileName(aPath);
    WebFile dir = getFile(dirPath);
    
    for(WebFile file : dir.getFiles()) {
        if(StringUtils.startsWithIC(file.getName(), fname))
            return file.getName();
    }
    return null;
}

/**
 * Called after FileText KeyRelease.
 */
private void handleFileTextKeyReleased()
{
    // Get whether FileTextFileValid
    boolean fileTextFileValid = isFileTextFileValid();
    
    // If not valid and opening, check for completion
    if(!fileTextFileValid && isOpening() && getFileTextFile()==null && _fileText.getText().trim().length()>0) {
        String path = getFileTextPath();
        String fname = getFileCompletion(path);
        
        // If completion found, set filename remainder in FileText and select
        if(fname!=null) {
            _fileText.setCompletionText(fname);
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
    FilePanel fp = new FilePanel(); fp.setDesc(aDesc); fp.setTypes(theTypes);
    return fp.showOpenPanel(aView);
}

/**
 * Shows a Save panel for given description and types.
 */
public static String showSavePanel(View aView, String aDesc, String ... theTypes)
{
    FilePanel fp = new FilePanel(); fp.setDesc(aDesc); fp.setTypes(theTypes);
    return fp.showSavePanel(aView);
}

/**
 * Returns the home directory path.
 */
static String getHomeDirPath()  { return System.getProperty("user.home"); }

/**
 * Returns the most recent path for given type.
 */
public static String getRecentPath(String aType)
{
    return Prefs.get().get("MostRecentDocument." + aType, getHomeDirPath());
}

/**
 * Sets the most recent path for given type.
 */
public static void setRecentPath(String aType, String aPath)
{
    Prefs.get().set("MostRecentDocument." + aType, aPath);
    Prefs.get().flush();
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