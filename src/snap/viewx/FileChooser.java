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
    boolean         _saving;
    
    // The file types
    String          _types[];
    
    // The description
    String          _desc;
    
    // The current file
    WebFile         _file;

    // The current file
    WebFile         _dir;

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
    _file = aFile;
    _dir = aFile!=null? aFile.getParent() : null;
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
    //JComponent aComp = rview!=null? rview.getNative(JComponent.class) : null;
    
    // Declare local variable for whether this is an open
    boolean save = isSaving(), open = !save;
    String types[] = getTypes();
    String type = types.length>0? types[0] : "";
    
    // Declare local variable for chooser
    //JFileChooser chooser = null;
    
    // Add file filter to chooser
    //chooser.setFileFilter(new UIUtilsFileFilter(theExtensions, aDesc));
    
    // If no file/dir set, get from prefs
    if(getDir()==null) {
    
        // Get last chosen file path from prefs for first given extension
        String path = Prefs.get().get("MostRecentDocument." + types[0], System.getProperty("user.home"));
    
        // Get last chosen file as File
        WebURL url = WebURL.getURL(path);
        WebFile file = url.getFile();
        if(file==null) {
            path = path + '.' + type;
            url = WebURL.getURL(path);
            file = url.getFile();
        }
    
       // Initialize chooser to last chosen directory and/or file
       if(file.isDir()) setDir(file);
       else setFile(file);
   }

    // Run chooser (use showDialog instead of showOpenDialog, because that version has no textfield)
    //int option = save? chooser.showSaveDialog(aComp) : chooser.showDialog(aComp, "Open");
    DialogBox dbox = new DialogBox(getTitle()); dbox.setContent(getUI());
    boolean value = dbox.showConfirmDialog(aView);
    if(!value)
        return null;
    
    // Get file and path of selection and save to preferences
    WebFile file = getFile();
    String path = file.getPath();
    
    // If "~", replace with user.home
    //if(path.indexOf("~")>=0)
    //    file = new File(path = System.getProperty("user.home") + File.separator + path.substring(path.indexOf('~')+1));

    // Get path extension
    String ext = "." + FilePathUtils.getExtension(path);
    if(ext.equals("."))
        ext = types[0];
    
    // Save selected filename in preferences for it's type (extension)
    Prefs.get().set("MostRecentDocument" + ext, path);
    Prefs.get().flush();
            
    // If user chose a directory, just run again
    if(file.isDir())
        return showChooser(aView);
    
    // If opening a file that doesn't exists, see if it just needs an extension
    if(open && !file.getExists()) {
        
        // If path doesn't contain an extension, add the first extension
        //if(path.indexOf(".") < 0)
        //    file = new File(path += theExtens[0]);

        // If file doesn't exist, run chooser again
        if(!file.getExists())
            return showChooser(aView);
    }
    
    // The open case can return file with invalid ext since we really run showDialog, so make sure path is OK
    if(open && !StringUtils.containsIC(types, FilePathUtils.getExtension(path)))
        return null;

    // If saving, make sure path has extension
    //if(save && path.indexOf(".") < 0)
    //    file = new File(path += theExtens[0]);

    // If user is trying to save over an existing file, warn them
    if(save && file.getExists()) {
        
        // Run option panel for whether to overwrite
        DialogBox dbox2 = new DialogBox("Replace File");
        dbox2.setWarningMessage("The file " + path + " already exists. Replace it?");
        dbox2.setOptions("Replace", "Cancel");
        int answer = dbox2.showOptionDialog(aView, "Replace");
        
        // If user chooses cancel, re-run chooser
        if(answer==1)
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
    BrowserView <WebFile> browser = getView("Browser", BrowserView.class);
    browser.setResolver(new FileResolver());
    browser.setItems(getFilteredFiles(getDir().getSite().getRootDir().getFiles()));
    browser.setSelectedItem(getFile()!=null? getFile() : getDir());
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