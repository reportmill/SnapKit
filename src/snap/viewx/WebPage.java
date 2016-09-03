/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.gfx.Image;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A visual representation of a WebFile used in WebBrowser.
 */
public class WebPage extends ViewOwner {

    // The browser that owns this page
    WebBrowser             _browser;
    
    // The response
    WebResponse               _response;
    
    // An array of listeners for object
    PropChangeSupport      _pcs = new PropChangeSupport(this);
    
    // A map of instances
    static HashMap         _instances = new HashMap();

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
public WebURL getURL()  { return getResponse().getRequestURL(); }

/**
 * Sets the Page URL.
 */
public void setURL(WebURL aURL)
{
    WebRequest req = new WebRequest(); req.setURL(aURL);
    WebResponse resp = new WebResponse(); resp.setRequest(req); resp.setCode(WebResponse.OK);
    setResponse(resp);
}

/**
 * Returns the WebFile for this WebPage.
 */
public WebFile getFile()  { return getResponse().getFile(); }

/**
 * Sets the WebFile for this WebPage.
 */
public void setFile(WebFile aFile)
{
    WebURL url = aFile.getURL(); WebRequest req = new WebRequest(); req.setURL(url);
    WebResponse resp = new WebResponse(); resp.setRequest(req); resp.setCode(WebResponse.OK);
    setResponse(resp);
}

/**
 * Returns the response that generated this page.
 */
public WebResponse getResponse()  { return _response!=null? _response : (_response=createResponse()); }

/**
 * Creates a response for this page.
 */
protected WebResponse createResponse()
{
    WebURL url = getInstanceURL(this);
    WebRequest req = new WebRequest(); req.setURL(url);
    WebResponse resp = new WebResponse(); resp.setRequest(req); resp.setCode(WebResponse.OK);
    return resp;
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
    if(file!=null && file.getType().equals("class") && url.getQueryValue("id")!=null) url = url.getFileURL();
    else if(file!=null) file.reload();
    if(getBrowser()!=null && getBrowser().getPage()==this)
        getBrowser().getLoader().setURL(url);
}

/** Add PropChangeListener. */
public void addPropChangeListener(PropChangeListener aPCL)  { _pcs.addPropChangeListener(aPCL); }

/** Remove PropChangeListener. */
public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

/** Fires a property change. */
protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
{
    _pcs.firePropChange(aProp, oldVal, newVal, anIndex);
}

/**
 * Runs a new show file panel for type.
 */
public WebFile showNewFilePanel(View aView)
{
    // Run input panel to get new file name
    String type = getFile().getType(), ext = '.' + type;
    String msg = "Enter " + type + " file name", title = "New " + type + " File";
    DialogBox dbox = new DialogBox(title); dbox.setQuestionMessage(msg);
    String fname = dbox.showInputDialog(aView, getFile().getName()); if(fname==null) return null;
    
    // Strip spaces (for now) and get file path
    fname = fname.replace(" ", "");
    String path = fname.startsWith("/")? fname : (getFile().getParent().getDirPath() + fname);
    if(!path.toLowerCase().endsWith(ext) && ext.length()>1) path = path + ext;
    
    // If file exists, run option panel for replace
    if(getSite().getFile(path)!=null) {
        String name = FilePathUtils.getFileName(path);
        msg = "A file named " + name + " already exists in this location.\n Do you want to replace it with new file?";
        dbox = new DialogBox(title); dbox.setWarningMessage(msg);
        if(!dbox.showConfirmDialog(aView)) return showNewFilePanel(aView);
    }
    
    // Create and return new file
    return createNewFile(path);
}

/**
 * Creates a new file for use with showNewFilePanel method.
 */
protected WebFile createNewFile(String aPath)  { return getSite().createFile(aPath, getFile().isDir()); }

/**
 * Returns a peer object, if this page was provided by another object.
 */
public Object getPeer()  { return null; }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + ": " + getURL().getString(); }

/**
 * Returns a URL for given instance.
 */
public static WebURL getInstanceURL(Object anObj)
{
    int id = System.identityHashCode(anObj);
    WebURL curl = WebURL.getURL(anObj.getClass());
    WebURL url = WebURL.getURL(curl.getString() + "?id=" + id);
    _instances.put(id, anObj);
    return url;
}

/**
 * Returns an instance for given id.
 */
public static Object getURLInstance(WebURL aURL)
{
    String ids = aURL.getQueryValue("id");
    Object obj = ids!=null? _instances.get(Integer.valueOf(ids)) : null;
    if(obj==null) System.err.println("ClassData: URL id not found " + ids);
    return obj;
}

}