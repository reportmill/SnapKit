/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.util.*;
import snap.web.*;

/**
 * A class to manage history for a WebBrowser.
 */
public class WebBrowserHistory {

    // The browser
    WebBrowser              _browser;
    
    // The current browser URL
    WebURL                  _url;
    
    // Whether history is currently tracking browser
    boolean                 _enabled = true;

    // A list of back-tracking URLs
    List <WebURL>           _lastURLs = new ArrayList();

    // A list of forward-tracking URLs
    List <WebURL>           _nextURLs = new ArrayList();

/**
 * Creates a new browser history.
 */
protected WebBrowserHistory(WebBrowser aBrowser)  { _browser = aBrowser; }

/**
 * Returns the browser.
 */
public WebBrowser getBrowser()  { return _browser; }

/**
 * Returns whether history is currently tracking browser.
 */
public boolean isEnabled()  { return _enabled; }

/**
 * Returns whether history is currently tracking browser.
 */
public void setEnabled(boolean aFlag)  { _enabled = aFlag; }

/**
 * Returns the current browser URL.
 */
public WebURL getURL()  { return _url; }

/**
 * Sets the file.
 */
protected void setURL(WebURL aURL)
{
    // If URL hasn't changed, just return
    if(!isEnabled() || SnapUtils.equals(aURL, _url)) return;
    
    // If first URL to be added, just set and return
    if(_url==null && _lastURLs.size()==0 && _nextURLs.size()==0) {
        _url = aURL; return; }
    
    // Add to LastURLs, clear NextURLs and set URL
    if(!SnapUtils.equals(_url, getLastURL())) addLastURL(_url);
    _nextURLs.clear();
    _url = aURL;
    
    // Set URL
    _url = aURL;
}

/**
 * Sets the browser URL to the last URL (backtracking).
 */
public void trackBack()
{
    WebURL lastURL = removeLastURL(); if(lastURL==null) return;
    getBrowser().setTransition(WebBrowser.MoveUp);
    setEnabled(false); getBrowser().setURL(lastURL); setEnabled(true);
    addNextURL(_url); _url = lastURL;
}

/**
 * Sets the browser URL to the next URL (forward tracking).
 */
public void trackForward()
{
    WebURL nextURL = removeNextURL(); if(nextURL==null) return;
    setEnabled(false); getBrowser().setURL(nextURL); setEnabled(true);
    addLastURL(_url); _url = nextURL;
}

/**
 * Returns the last URL (backtracking).
 */
public WebURL getLastURL()  { return _lastURLs.size()>0? _lastURLs.get(_lastURLs.size()-1) : null; }

/**
 * Adds a last URL.
 */
public void addLastURL(WebURL aURL)  { _lastURLs.add(aURL); }

/**
 * Removes the last URL.
 */
public WebURL removeLastURL()  { int index = _lastURLs.size() - 1; return index>=0? _lastURLs.remove(index) : null; }

/**
 * Returns the list of last URLs.
 */
public WebURL[] getLastURLs()
{
    WebURL urls[] = _lastURLs.toArray(new WebURL[_lastURLs.size()]); ArrayUtils.reverse(urls); return urls;
}

/**
 * Returns the next URL (forward tracking).
 */
public WebURL getNextURL()  { return _nextURLs.size()>0? _nextURLs.get(_nextURLs.size()-1) : null; }

/**
 * Adds a next URL.
 */
public void addNextURL(WebURL aURL)  { _nextURLs.add(aURL); }

/**
 * Removes the next URL.
 */
public WebURL removeNextURL()  { int index = _nextURLs.size() - 1; return index>=0? _nextURLs.remove(index) : null; }

/**
 * Returns the list of next URLs.
 */
public WebURL[] getNextURLs()
{
    WebURL urls[] = _nextURLs.toArray(new WebURL[_nextURLs.size()]); ArrayUtils.reverse(urls); return urls;
}

/**
 * Removes a URL from history.
 */
public void removeURL(WebURL aURL)
{
    _lastURLs.remove(aURL);
    _nextURLs.remove(aURL);
    if(getBrowser().getURL()==aURL) getBrowser().setURL(null);
    getBrowser().setPage(aURL, null);
}

/**
 * Removes a file from history.
 */
public void removeFile(WebFile aFile)
{
    WebURL lastURLs[] = _lastURLs.toArray(new WebURL[0]), nextURLs[] = _nextURLs.toArray(new WebURL[0]);
    for(WebURL url : lastURLs) if(url.getFile()==aFile) _lastURLs.remove(url);
    for(WebURL url : nextURLs) if(url.getFile()==aFile) _lastURLs.remove(url);
    if(getBrowser().getFile()==aFile) getBrowser().setURL(null);
    getBrowser().setPage(aFile.getURL(), null);
}

/**
 * Clears the browser history.
 */
public void clearHistory()
{
    getBrowser().setURL(null);
    getBrowser()._pages.clear();
    _lastURLs.clear(); _nextURLs.clear();
}

}