/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Image;
import snap.view.*;
import snap.web.WebFile;

/**
 * A WebPage subclass for directories.
 */
public class DirFilePage extends WebPage {

    // The file browser
    BrowserView <WebFile>  _fileBrowser;
    
    // The page browser
    WebBrowser             _pageBrowser;

/**
 * Returns the selected file in browser.
 */
public WebFile getSelectedFile()  { return _fileBrowser.getSelectedItem(); }

/** Returns the selected files in browser. */
//public List <WebFile> getSelectedFiles()  { return _fileBrowser.getSelectedItems(); }

/**
 * Returns the file browser.
 */
public BrowserView <WebFile> getFileBrowser()  { getUI(); return _fileBrowser; }

/**
 * Returns the page browser.
 */
public WebBrowser getPageBrowser()  { getUI(); return _pageBrowser; }

/**
 * Creates a file pane for the given file in the requested mode.
 */
protected View createUI()
{
    HBox hbox = new HBox(); hbox.setFillHeight(true);
    _fileBrowser = new BrowserView(); _fileBrowser.setName("FileBrowser");
    _fileBrowser.setResolver(new FileTreeResolver());
    _fileBrowser.setItems(getFile().getFiles());
    ScrollView spane = new ScrollView(); spane.setContent(_fileBrowser); spane.setPrefWidth(400);
    _pageBrowser = new WebBrowser(); _pageBrowser.setGrowWidth(true); spane.setGrowWidth(true);
    hbox.setChildren(spane, _pageBrowser);
    return hbox;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Enable events on FileBrowser
    enableEvents(_fileBrowser, MouseRelease);
}

/**
 * Respond to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle FileBrowser MouseClick
    if(anEvent.isMouseClick()) {
        
        // Handle single click: set browser to file (or clear if directory)
        if(anEvent.getClickCount()==1) {
            WebFile file = _fileBrowser.getSelectedItem(); if(file==null) return;
            _pageBrowser.setFile(file.isFile()? file : null);
        }
        
        // Handle double click: set enclosing browser file
        else if(anEvent.getClickCount()==2)
            performFileBrowserDoubleClick();
    }
    
    // Handle FileBrowser click
    if(anEvent.equals("FileBrowser")) {
        WebFile file = _fileBrowser.getSelectedItem(); if(file==null) return;
        _pageBrowser.setFile(file.isFile()? file : null);
        getUI().relayout();
    }
}

/**
 * Handle file browser double click.
 */
protected void performFileBrowserDoubleClick()
{
    WebFile file = _fileBrowser.getSelectedItem(); if(file==null) return;
    getBrowser().setFile(file);
}

/**
 * A TreeResolver for WebFile
 */
public static class FileTreeResolver extends TreeResolver <WebFile> {

    /** Returns the parent of given item. */
    public WebFile getParent(WebFile anItem)  { return anItem.getParent(); }
    
    // Return whether file is directory
    public boolean isParent(WebFile anObj)  { return anObj.isDir(); }

    // Return child files
    public WebFile[] getChildren(WebFile aParent)  { return aParent.getFiles().toArray(new WebFile[0]); }

    // Return child file name
    public String getText(WebFile aFile)  { return aFile.getName(); }

    // Return child file icon
    public Image getImage(WebFile aFile)  { return ViewUtils.getFileIconImage(aFile); }
}

}