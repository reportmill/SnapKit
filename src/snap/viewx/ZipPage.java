/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.List;
import snap.gfx.Image;
import snap.view.*;
import snap.web.*;

/**
 * A WebPage subclass for Zip/Jar files.
 */
public class ZipPage extends WebPage {

    // The file browser
    BrowserView <WebFile>  _fileBrsr;
    
    // The page browser
    WebBrowser             _pageBrsr;

/**
 * Returns the root files.
 */
public List <WebFile> getFiles()
{
    WebSite site = getFile().getURL().getAsSite();
    return site.getRootDir().getFiles();
}

/**
 * Returns the selected file in browser.
 */
public WebFile getSelectedFile()  { return _fileBrsr.getSelItem(); }

/**
 * Creates a file pane for the given file in the requested mode.
 */
protected View createUI()
{
    // Create FileBrowser and put in ScrollView
    _fileBrsr = new BrowserView(); _fileBrsr.setName("FileBrowser");
    _fileBrsr.setPrefColCount(3); _fileBrsr.setPrefHeight(350); //_fileBrsr.setGrowHeight(true);
    _fileBrsr.setResolver(new FileTreeResolver());
    _fileBrsr.setItems(getFiles());
    
    // Create PageBrowser
    _pageBrsr = new WebBrowser(); _pageBrsr.setGrowHeight(true);
    
    // Put FileBrowser and PageBrowser in VBox
    ColView vbox = new ColView(); vbox.setFillWidth(true);
    vbox.setChildren(_fileBrsr, _pageBrsr);
    return vbox;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Enable events on FileBrowser
    enableEvents(_fileBrsr, MouseRelease);
}

/**
 * Respond to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle FileBrowser click
    if(anEvent.equals("FileBrowser")) {
        WebFile file = _fileBrsr.getSelItem(); if(file==null) return;
        _pageBrsr.setFile(file.isFile()? file : null);
    }
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