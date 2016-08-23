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
public WebFile getSelectedFile()  { return _fileBrsr.getSelectedItem(); }

/**
 * Creates a file pane for the given file in the requested mode.
 */
protected View createUI()
{
    // Create FileBrowser and put in ScrollView
    _fileBrsr = new BrowserView(); _fileBrsr.setName("FileBrowser"); _fileBrsr.setVisColCount(3);
    _fileBrsr.setResolver(new FileTreeResolver());
    _fileBrsr.setItems(getFiles());
    ScrollView sview = new ScrollView(_fileBrsr); sview.setGrowHeight(true);
    
    // Create PageBrowser
    _pageBrsr = new WebBrowser(); _pageBrsr.setGrowHeight(true);
    
    // Put FileBrowser and PageBrowser in VBox
    VBox vbox = new VBox(); vbox.setFillWidth(true);
    vbox.setChildren(sview, _pageBrsr);
    return vbox;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Enable events on FileBrowser
    enableEvents(_fileBrsr, MouseClicked);
}

/**
 * Respond to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle FileBrowser click
    if(anEvent.equals("FileBrowser")) {
        WebFile file = _fileBrsr.getSelectedItem(); if(file==null) return;
        _pageBrsr.setFile(file.isFile()? file : null);
    }
}

/**
 * A TreeResolver for WebFile
 */
public static class FileTreeResolver extends TreeResolver <WebFile> {

    // Return whether file is directory
    public boolean isParent(WebFile anObj)  { return anObj.isDir(); }

    // Return child count
    public int getChildCount(WebFile aParent)  { return aParent.getFileCount(); }

    // Return child file
    public WebFile getChild(WebFile aParent, int anIndex)  { return aParent.getFile(anIndex); }

    // Return child file name
    public String getText(WebFile aFile)  { return aFile.getName(); }

    // Return child file icon
    public Image getImage(WebFile aFile)  { return ViewUtils.getFileIconImage(aFile); }
}

}