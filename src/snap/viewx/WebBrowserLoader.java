/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.view.*;
import snap.web.*;

/**
 * A class to load files in the background.
 */
public class WebBrowserLoader {

    // The Browser
    WebBrowser           _browser;
    
    // The current URL loading
    WebRequest           _req;
    
    // The current runner loading a file
    TaskRunnerPanel      _runner;
    
/**
 * Creates a loader for a browser.
 */
public WebBrowserLoader(WebBrowser aBrowser)  { _browser = aBrowser; }

/**
 * Returns the current URL to set.
 */
public WebURL getURL()  { return _req!=null? _req.getURL() : null; }

/**
 * Sets the URL to load and set into browser.
 */
public void setURL(WebURL aURL)
{
    if(aURL==null) { _browser.setPage(null); return; }
    WebRequest req = new WebRequest(aURL);
    setRequest(req);
}

/**
 * Returns the current request to set.
 */
public WebRequest getRequest()  { return _req; }

/**
 * Sets the URL to load and set into browser.
 */
public void setRequest(WebRequest aReq)
{
    // If not EventDispatchThread, re-invoke in that thread
    ViewEnv env = ViewEnv.getEnv(); if(!env.isEventThread()) { env.runLater(() -> setRequest(aReq)); return; }
    
    // Set browser ActivityText
    _browser.setActivity("Loading " + aReq.getURL().getString());
    _browser.setLoading(true);
    
    // Create and start URLLoader
    _req = aReq; _runner = new URLLoader(aReq);
    _runner.start();
}

/**
 * Returns whether runner is still running.
 */
public boolean isLoading()  { return _runner!=null && _runner.isActive(); }

/**
 * Loads a URL.
 */
protected WebResponse loadURL(WebRequest aReq) throws Exception
{
    WebSite site = aReq.getURL().getSite();
    return site.getResponse(aReq);
}

/**
 * Handle success: Get WebPage for URL and start PageLoader.
 */
protected void loadURLSuccess(WebResponse aResp)
{
    if(aResp.getRequest()!=_req) return; // Just return if loader is already loading another request
    _browser.setResponse(aResp);
    _browser.setActivity("");
    _browser.setLoading(false);
    _req = null; _runner = null;
}

/**
 * Handle failure: Clear Browser.Activity and Loading and show exception.
 */
protected void loadURLFailure(WebRequest aReq, Throwable e)
{
    _browser.setActivity("");
    _browser.setLoading(false);
    _browser.showException(aReq.getURL(), e);
    _req = null; _runner = null;
}

/**
 * A JFXRunner subclass to load a URL file+bytes.
 */
private class URLLoader extends TaskRunnerPanel <WebResponse> {
    URLLoader(WebRequest aReq)  { _ldrReq = aReq; } WebRequest _ldrReq;
    public WebResponse run() throws Exception  { return loadURL(_ldrReq); }
    public void success(WebResponse aResponse)  { loadURLSuccess(aResponse); }
    public void failure(Exception e)  { loadURLFailure(_ldrReq, e); }
}

}