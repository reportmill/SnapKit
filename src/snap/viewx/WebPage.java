/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.gfx.Image;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A visual representation of a WebFile used in WebBrowser.
 */
public class WebPage extends ViewOwner {

    // The URL
    WebURL                 _url;
    
    // The response
    WebResponse            _response;
    
    // The file
    WebFile                _file;
    
    // The browser that owns this page
    WebBrowser             _browser;
    
/**
 * Returns the WebBrowser for this WebPage.
 */
public WebBrowser getBrowser()  { return _browser; }

/**
 * Returns the WebBrowser for this WebPage.
 */
public void setBrowser(WebBrowser aBrowser)  { _browser = aBrowser; }

/**
 * Returns the Page URL.
 */
public WebURL getURL()  { return _url!=null? _url : (_url=getURLImpl()); }

/**
 * Returns the Page URL.
 */
protected WebURL getURLImpl()
{
    // If file, return from that
    if(_file!=null)
        return _file.getURL();
        
    // If Response, return from that
    if(_response!=null)
        return _response.getURL();
        
    // If subclass of WebPage, use Class file URL
    if(getClass()!=WebPage.class)
        return WebURL.getURL(getClass());
        
    // Return null
    System.err.println("WebPage.getURL: No page URL");
    return null;
}

/**
 * Sets the Page URL.
 */
public void setURL(WebURL aURL)
{
    // If already set, just return
    if(SnapUtils.equals(aURL, _url)) return;
    
    // Set URL and Response
    _url = aURL;
    WebRequest req = new WebRequest(aURL);
    WebResponse resp = new WebResponse(req); resp.setCode(WebResponse.OK);
    setResponse(resp);
}

/**
 * Returns the WebFile for this WebPage.
 */
public WebFile getFile()
{
    // if already set, just return
    if(_file!=null) return _file;
    
    // Get file from URL
    WebURL url = getURL(); if(url==null) return null;
    return _file = url.getFile();
}

/**
 * Sets the WebFile for this WebPage.
 */
public void setFile(WebFile aFile)
{
    _file = aFile;
    //WebURL url = aFile.getURL(); WebRequest req = new WebRequest(url);
    //WebResponse resp = new WebResponse(); resp.setRequest(req); resp.setCode(WebResponse.OK);
    //setResponse(resp);
}

/**
 * Returns the response that generated this page.
 */
public WebResponse getResponse()
{
    if(_response!=null) return _response;

    // Create response from URL
    WebURL url = getURL(); if(url==null) return null;
    WebRequest req = new WebRequest(url);
    WebResponse resp = new WebResponse(req); resp.setCode(WebResponse.OK);
    return _response = resp;
}

/**
 * Sets the response that generated this page.
 */
public void setResponse(WebResponse aResp)  { _response = aResp; }

/**
 * Returns the WebSite for this WebPage.
 */
public WebSite getSite()  { return getURL().getSite(); }

/**
 * Returns the page title.
 */
public String getTitle()
{
    WebURL url = getURL();
    String path = url.getPath(), name = url.getPathName(); path = path.substring(0, path.length() - name.length() - 1);
    return name + " - " + path + " - " + url.getSite().getName();
}

/**
 * Returns the icon for the file.
 */
public Image getIcon()  { return ViewUtils.getFileIconImage(getFile()); }

/**
 * Notification that WebPage was installed in WebBrowser.
 */
public void notifyPageAdded(WebBrowser aBrowser)  { }

/**
 * Notification that WebPage was removed from WebBrowser.
 */
public void notifyPageRemoved(WebBrowser aBrowser)  { }

/**
 * Override to show exception page. 
 */
protected void processResetUI()
{
    try { super.processResetUI(); }
    catch(Throwable t) { getBrowser().showException(getURL(), t); }
}

/**
 * Override to show exception page. 
 */
public void processEvent(ViewEvent anEvent)
{
    try { super.processEvent(anEvent); }
    catch(Throwable t) {
        if(getBrowser()!=null) getBrowser().showException(getURL(), t);
        else throw new RuntimeException(t);
    }
}

/**
 * Reloads a given page.
 */
public void reload()
{
    // Reload page
    WebURL url = getURL();
    WebFile file = url.getFile();
    if(file!=null) file.reload();
    if(getBrowser()!=null && getBrowser().getPage()==this)
        getBrowser().getLoader().setURL(url);
}

/**
 * Runs a new show file panel for type.
 */
public WebFile showNewFilePanel(View aView, WebFile aFile)
{
    // Run input panel to get new file name
    String type = aFile.getType(), ext = '.' + type;
    String msg = "Enter " + type + " file name", title = "New " + type + " File";
    DialogBox dbox = new DialogBox(title); dbox.setQuestionMessage(msg);
    String fname = dbox.showInputDialog(aView, aFile.getName()); if(fname==null) return null;
    
    // Strip spaces (for now) and get file path
    fname = fname.replace(" ", "");
    String path = fname.startsWith("/")? fname : (aFile.getParent().getDirPath() + fname);
    if(!path.toLowerCase().endsWith(ext) && ext.length()>1) path = path + ext;
    
    // If file exists, run option panel for replace
    if(getSite().getFile(path)!=null) {
        String name = FilePathUtils.getFileName(path);
        msg = "A file named " + name + " already exists in this location.\n Do you want to replace it with new file?";
        dbox = new DialogBox(title); dbox.setWarningMessage(msg);
        if(!dbox.showConfirmDialog(aView)) return showNewFilePanel(aView, aFile);
    }
    
    // If directory, just create and return
    if(aFile.isDir())
        return getSite().createFile(path, true);
    
    // Create and return new file
    return createNewFile(path);
}

/**
 * Creates a new file for use with showNewFilePanel method.
 */
protected WebFile createNewFile(String aPath)  { return getSite().createFile(aPath, false); }

/**
 * Returns a peer object, if this page was provided by another object.
 */
public Object getPeer()  { return null; }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + ": " + getURL().getString(); }

}