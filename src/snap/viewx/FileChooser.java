/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A class to select a file to open or save.
 */
public class FileChooser extends ViewOwner {
    
    // Whether choosing file for save
    boolean                _saving;
    
    // The file types
    String                 _types[];
    
    // The description
    String                 _desc;
    
    // The current file
    WebFile                _file;

    // The current file
    WebFile                _dir;
    
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
 * Sets the file in the UI.
 */
protected void setFileInUI()
{
    if(!isUISet()) return;
    _fileBrowser.setSelectedItem(getFile()!=null? getFile() : getDir());
    _fileText.setText(getFile()!=null? getFile().getName() : null);
    _fileText.selectAll();
    _fileText.requestFocus();
    _dbox.setConfirmEnabled(isFileTextFileValid());
}

/**
 * Returns the home directory path.
 */
String getHomeDirPath()  { return System.getProperty("user.home"); }

/**
 * Returns a file for a path.
 */
WebFile getFile(String aPath)
{
    WebURL url = WebURL.getURL(aPath);
    WebFile file = url.getFile();
    return file;
}

/**
 * Returns the most recent path for given type.
 */
public String getRecentPath(String aType)
{
    return Prefs.get().get("MostRecentDocument." + aType, getHomeDirPath());
}

/**
 * Sets the most recent path for given type.
 */
public void setRecentPath(String aType, String aPath)
{
    Prefs.get().set("MostRecentDocument." + aType, aPath);
    Prefs.get().flush();
}

/**
 * Shows the panel.
 */
public String showOpenPanel(View aView)
{
    setSaving(false);
    return showChooser(aView);
}

/**
 * Shows the panel.
 */
public String showSavePanel(View aView)
{
    setSaving(true);
    return showChooser(aView);
}

/**
 * Runs a file chooser that remembers last open file and size.
 */
protected String showChooser(View aView)
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
            return showChooser(aView);
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
        if(file.isDir()) { files.add(file); continue; }
        if(ArrayUtils.contains(getTypes(), file.getType()))
            files.add(file);
    }
    return files.toArray(new WebFile[0]);
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Get BrowserView and configure
    _fileBrowser = getView("FileBrowser", BrowserView.class);
    _fileBrowser.setResolver(new FileResolver());
    _fileBrowser.setItems(getFilteredFiles(getDir().getSite().getRootDir().getFiles()));
    _fileBrowser.setSelectedItem(getFile()!=null? getFile() : getDir());
    
    // Get FileText
    _fileText = getView("FileText", TextField.class);
    _fileText.setText(getFile()!=null? getFile().getName() : null);
    _fileText.selectAll();
    setFirstFocus(_fileText);
    
    // Set handler to update DialogBox.ConfirmEnabled when text changes
    _fileText.addEventHandler(e -> runLater(() -> handleFileTextKeyReleased()), KeyRelease);
}

/**
 * Respond to UI changes.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle FileBrowser
    if(anEvent.equals("FileBrowser")) {
        WebFile file = _fileBrowser.getSelectedItem();
        setFile(file);
    }
    
    // Handle FileText: If directory, set
    if(anEvent.equals("FileText")) {
        WebFile file = getFileTextFile();
        if(file!=null && file.isDir())
            setFile(file);
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
 * Called after FileText KeyRelease.
 */
private void handleFileTextKeyReleased()
{
    _dbox.setConfirmEnabled(isFileTextFileValid());
}

/**
 * Shows an Open panel for given description and types.
 */
public static String showOpenPanel(View aView, String aDesc, String ... theTypes)
{
    FileChooser fc = new FileChooser();
    fc.setSaving(false); fc.setDesc(aDesc); fc.setTypes(theTypes);
    return fc.showChooser(aView);
}

/**
 * Shows a Save panel for given description and types.
 */
public static String showSavePanel(View aView, String aDesc, String ... theTypes)
{
    FileChooser fc = new FileChooser();
    fc.setSaving(true); fc.setDesc(aDesc); fc.setTypes(theTypes);
    return fc.showChooser(aView);
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