/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A panel to browser Snap files.
 */
public class WebBrowser extends TransitionPane {

    // The current WebPage
    WebPage                         _page;

    // A cache of WebPages for WebURLs
    Map <WebURL, WebPage>           _pages = new HashMap();
    
    // The file loader
    WebBrowserLoader                _loader = new WebBrowserLoader(this);
    
    // The file history
    WebBrowserHistory               _history = new WebBrowserHistory(this);
    
    // Whether browser is loading a page
    boolean                         _loading;
    
    // A string property that indicates what the browser is showing
    String                          _status;
    
    // A string property that indicates what the browser is doing
    String                          _activity;
    
    // Constants for properties
    public static final String      Page_Prop = "Page";
    public static final String      Loading_Prop = "Loading";
    public static final String      Status_Prop = "Status";
    public static final String      Activity_Prop = "Activity";

/**
 * Returns the current SnapURL.
 */
public WebURL getURL()  { WebPage page = getPage(); return page!=null? page.getURL() : null; }

/**
 * Sets a URL in browser immediately. This is usually called by setLoaderURL instead.
 */
public void setURL(WebURL aURL)
{
    // If url is null, clear page
    if(aURL==null) { setPage(null); return; }
    
    // Get page - if not found, have loader load it
    WebPage page = getPage(aURL); // Get page
    if(page==null) {
        getLoader().setURL(aURL); return; }
    
    // Update page display URL, set page and update history
    page.setURL(aURL); // Set/update URL in page
    setPage(page); // Set page in browser
}

/**
 * Returns the URL string of the current browser URL.
 */
public String getURLString()  { WebURL url = getURL(); return url!=null? url.getString() : null; }

/**
 * Sets the browser URL from given relative URL string to the current page.
 */
public void setURLString(String aURLString)
{
    WebFile file = getFile();
    WebURL url = file!=null? file.getURL(aURLString) : WebURL.getURL(aURLString);
    setURL(url);
}

/**
 * Returns the WebFile for the current page.
 */
public WebFile getFile()  { WebPage page = getPage(); return page!=null? page.getFile() : null; }

/**
 * Sets the browser to a page for given WebFile.
 */
public void setFile(WebFile aFile)
{
    if(aFile==null) { setPage(null); return; }              // Handle null page
    if(aFile==getFile()) return;
    WebPage page = getPage(aFile.getURL());                 // Get cached page for URL
    if(page==null) {                                        // Create and cache if necessary
        page = createPage(aFile); setPage(page.getURL(), page); }
    setPage(page);                                          // Set page in browser
}

/**
 * Sets the response in the browser.
 */
public void setResponse(WebResponse aResp)
{
    WebPage page = createPage(aResp); // Create page for URL
    if(aResp.getFile()!=null) setPage(page.getURL(), page);
    setPage(page); // Set page in browser
}

/**
 * Returns the current WebPage.
 */
public WebPage getPage()  { return _page; }

/**
 * Sets the current WebPage.
 */
public void setPage(WebPage aPage)
{
    // If already set, just return
    if(aPage==getPage()) return;

    // Notify current page of imminent removal
    if(_page!=null) _page.notifyPageRemoved(this);
    
    // Set page
    _page = aPage;
    
    // Get page UI - if already set, just return
    View pageUI = _page!=null? _page.getUI() : null;
    if(pageUI==getContent()) return;
    
    // Set component
    setContent(pageUI);
    
    // Focus default component
    if(_page!=null) {
        _page.setBrowser(this);
        _page.notifyPageAdded(this);                 // Notify PageAdded
        if(_page.getFirstFocus()!=null)              // Set Page Focus
            _page.requestFocus(_page.getFirstFocus());
        getHistory().setURL(_page.getURL());  // Update History
    }

    // Notify change
    firePropChange(Page_Prop, null, _page);
    
    // Flush WebSite for the heck of it?
    if(getFile()!=null)
        try { getFile().getSite().flush(); }
        catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns the WebPage for given WebURL.
 */
public WebPage getPage(WebURL aURL)  { return _pages.get(aURL.getQueryURL()); }

/**
 * Sets the WebPage for a given WebFile.
 */
public void setPage(WebURL aURL, WebPage aPage)
{
    WebURL url = aURL.getQueryURL();
    if(aPage!=null) {
        _pages.put(url, aPage); aPage.setBrowser(this); }
    else _pages.remove(url);
}

/**
 * Creates a WebPage for given WebFile.
 */
public WebPage createPage(WebFile aFile)
{
    WebURL url = aFile.getURL();
    WebRequest req = new WebRequest(url);
    WebResponse resp = new WebResponse(req); resp.setCode(WebResponse.OK);
    return createPage(resp);
}

/**
 * Creates a WebPage for given WebFile.
 */
protected WebPage createPage(WebResponse aResp)
{
    // If UNAUTHORIZED response, create LoginPage
    WebPage page = null;
    if(aResp.getCode()==WebResponse.UNAUTHORIZED)
        page = new LoginPage();
    
    // If not OK, create FileNotFoundPage
    else if(aResp.getCode()!=WebResponse.OK)
        page = new FileNotFoundPage();
        
    // Create WebPage in handler
    else try {
        Class <? extends WebPage> cls = getPageClass(aResp);
        page = ClassUtils.newInstance(cls);
    }
    
    // Return Exception WebPage
    catch(Throwable t) { aResp.setException(t); return createExceptionPage(aResp); }
    
    // Set response and browser and return
    page.setResponse(aResp); page.setBrowser(this);
    return page;
}

/**
 * Returns the specified WebPage subclass for given WebFile.
 */
protected Class <? extends WebPage> getPageClass(WebResponse aResp)
{
    String type = aResp.getPathType();
    
    WebFile file = aResp.getFile();
    if(file!=null && file.isDir())
        return DirFilePage.class;

    if(type.equals("jpg") || type.equals("jpeg") || type.equals("gif") || type.equals("png")) return ImagePage.class;
    if(type.equals("snp")) return SnapPage.class;
    if(type.equals("rpt")) return getPageClass("com.reportmill.app.ReportPage", TextPage.class);
    if(type.equals("wav") || type.equals("snd") || type.equals("mp3") || type.equals("m4a")) return SoundPage.class;
    if(type.equals("java")) return getPageClass("javakit.text.JavaPage", TextPage.class);
    if(type.equals("jar")) return ZipPage.class;
    if(type.equals("txt") || (file!=null && file.isText())) return TextPage.class;
    return UnknownPage.class;
}

/**
 * Returns the specified WebPage subclass for given WebFile.
 */
protected Class <? extends WebPage> getPageClass(String aClassName, Class <? extends WebPage> aDefault)
{
    try { return (Class)Class.forName(aClassName); }
    catch(Exception e) { System.err.println("WebBrowser: Page class not found: " + e); }
    return aDefault!=null? aDefault : UnknownPage.class;
}

/**
 * Returns the browser loader.
 */
public WebBrowserLoader getLoader()  { return _loader; }

/**
 * Returns the history.
 */
public WebBrowserHistory getHistory()  { return _history; }

/**
 * Returns the previous URL.
 */
public WebURL getLastURL()  { return getHistory().getLastURL(); }

/**
 * Returns the next URL (if browser has backtracked).
 */
public WebURL getNextURL()  { return getHistory().getNextURL(); }

/**
 * Sets the browser URL to the last URL (backtracking).
 */
public void trackBack()  { getHistory().trackBack(); }

/**
 * Sets the browser URL to the next URL (forward tracking).
 */
public void trackForward()  { getHistory().trackForward(); }

/**
 * Reloads the current page.
 */
public void reloadPage()  { if(getPage()!=null) getPage().reload(); }

/**
 * Reloads the given file..
 */
public void reloadFile(WebFile aFile)
{
    WebPage page = getPage(aFile.getURL());
    if(page!=null) page.reload();
}

/**
 * Shows an exception.
 */
public void showException(Throwable t)
{
    WebURL url = getFile()!=null? getFile().getURL() : WebURL.getURL("/Unknown.exception");
    showException(url, t);
}

/**
 * Shows an exception for given URL.
 */
public void showException(WebURL aURL, Throwable t)
{
    // If not EventDispatchThread, re-invoke in that thread
    if(!getEnv().isEventThread()) { getEnv().runLater(() -> showException(aURL, t)); return; }
    
    // Print stack trace to console
    t.printStackTrace();
    
    // Create request/response
    WebRequest req = new WebRequest(aURL);
    WebResponse resp = new WebResponse(req); resp.setException(t);
    
    // Create page and set
    TextPage textPage = createExceptionPage(resp);
    setPage(textPage);
}

/**
 * Creates an exception page for exception.
 */
protected TextPage createExceptionPage(WebResponse aResp)
{
    // Get URL and exception
    WebURL url = aResp.getURL(); Throwable t = aResp.getException();
    
    // Get default console text
    String text = String.format("WebBrowser Exception Console\nBrowser Build Date: %s, Version 1.0, JVM %s, User %s\n",
        SnapUtils.getBuildInfo(), System.getProperty("java.version"), System.getProperty("user.name"));
    text += t.toString() + '\n';
    text += "Caused by: " + StringUtils.getStackTraceString(t);
    
    // Handle FileNotFound
    if(aResp.getCode()==WebResponse.NOT_FOUND)
        text = "\nNot Found\n\nThe requested URL was not found on server.\n\n" + url.getString();
    
    // Create TextFilePage and install
    TextPage textPage = new TextPage(); textPage.setResponse(aResp);
    textPage.setBrowser(this);
    textPage.setText(text);
    return textPage;
}

/**
 * Returns whether browser is currently loading a page.
 */
public boolean isLoading()  { return _loading; }

/**
 * Sets whether browser is currently loading a page.
 */
public void setLoading(boolean aValue)
{
    if(aValue==_loading) return;
    firePropChange(Loading_Prop, _loading, _loading = aValue);
}

/**
 * Returns the status text.
 */
public String getStatus()  { return _status; }

/**
 * Sets the status text.
 */
public void setStatus(String aString)
{
    if(aString.equals(_status)) return;
    firePropChange(Status_Prop, _status, _status = aString);
}

/**
 * Returns the activity text.
 */
public String getActivity()  { return _activity; }

/**
 * Sets the activity text.
 */
public void setActivity(String aString)
{
    if(aString.equals(_activity)) return;
    firePropChange(Activity_Prop, _activity, _activity = aString);
}

/**
 * Handles a URL click.
 */
public boolean performURLClick(String aURL)  { setURLString(aURL); return true; }

}