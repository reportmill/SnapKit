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
    private WebURL  _url;

    // The response
    private WebResponse  _response;

    // The file
    private WebFile  _file;

    // The browser that owns this page
    private WebBrowser  _browser;

    /**
     * Constructor.
     */
    public WebPage()
    {
        super();
    }

    /**
     * Returns the WebBrowser for this WebPage.
     */
    public WebBrowser getBrowser()  { return _browser; }

    /**
     * Returns the WebBrowser for this WebPage.
     */
    public void setBrowser(WebBrowser aBrowser)
    {
        _browser = aBrowser;
    }

    /**
     * Returns the Page URL.
     */
    public WebURL getURL()
    {
        if (_url != null) return _url;
        return _url = getURLImpl();
    }

    /**
     * Returns the Page URL.
     */
    protected WebURL getURLImpl()
    {
        // If file, return from that
        if (_file != null)
            return _file.getURL();

        // If Response, return from that
        if (_response != null)
            return _response.getURL();

        // If subclass of WebPage, use Class file URL
        Class<? extends WebPage> pageClass = getClass();
        if (pageClass != WebPage.class)
            return WebURL.getURL(pageClass);

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
        if (SnapUtils.equals(aURL, _url))
            return;

        // Set URL and Response
        _url = aURL;
        WebRequest req = new WebRequest(aURL);
        WebResponse resp = new WebResponse(req);
        resp.setCode(WebResponse.OK);
        setResponse(resp);
    }

    /**
     * Returns the WebFile for this WebPage.
     */
    public WebFile getFile()
    {
        // if already set, just return
        if (_file != null) return _file;

        // Get file from URL
        WebURL url = getURL();
        WebFile file = url != null ? url.getFile() : null;

        // Set/return
        return _file = file;
    }

    /**
     * Sets the WebFile for this WebPage.
     */
    public void setFile(WebFile aFile)
    {
        _file = aFile;
    }

    /**
     * Returns the response that generated this page.
     */
    public WebResponse getResponse()
    {
        if (_response != null) return _response;

        // Create response from URL
        WebURL url = getURL();
        if (url == null)
            return null;
        WebRequest req = new WebRequest(url);
        WebResponse resp = new WebResponse(req);
        resp.setCode(WebResponse.OK);

        // Set/return
        return _response = resp;
    }

    /**
     * Sets the response that generated this page.
     */
    public void setResponse(WebResponse aResp)
    {
        _response = aResp;
    }

    /**
     * Returns the WebSite for this WebPage.
     */
    public WebSite getSite()
    {
        WebURL url = getURL();
        return url.getSite();
    }

    /**
     * Returns the page title.
     */
    public String getTitle()
    {
        // Get file name and path
        WebURL pageURL = getURL();
        String filePath = pageURL.getPath();
        String filename = pageURL.getPathName();
        filePath = filePath.substring(0, filePath.length() - filename.length() - 1);

        // Get Site.Name
        WebSite site = getSite();
        String siteName = site.getName();

        // Return
        return filename + " - " + filePath + " - " + siteName;
    }

    /**
     * Returns the icon for the file.
     */
    public Image getIcon()
    {
        WebFile file = getFile();
        return ViewUtils.getFileIconImage(file);
    }

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
    protected void invokeResetUI()
    {
        try { super.invokeResetUI(); }
        catch (Throwable t) {
            WebBrowser browser = getBrowser();
            browser.showException(getURL(), t);
        }
    }

    /**
     * Override to show exception page.
     */
    public void invokeRespondUI(ViewEvent anEvent)
    {
        try { super.invokeRespondUI(anEvent); }
        catch (Throwable t) {
            WebBrowser browser = getBrowser();
            if (browser != null)
                browser.showException(getURL(), t);
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
        if (file != null)
            file.reload();

        WebBrowser browser = getBrowser();
        if (browser != null && browser.getPage() == this)
            browser.getLoader().setURL(url);
    }

    /**
     * Runs a new show file panel for type.
     */
    public WebFile showNewFilePanel(View aView, WebFile aFile)
    {
        // Run input panel to get new file name
        String type = aFile.getType();
        String msg = "Enter " + type + " file name";
        String title = "New " + type + " File";
        DialogBox dialogBox = new DialogBox(title);
        dialogBox.setQuestionMessage(msg);
        String filename = dialogBox.showInputDialog(aView, aFile.getName());
        if (filename == null)
            return null;

        // Strip spaces from filename (for now) and make sure it has extension
        filename = filename.replace(" ", "");
        if (!filename.toLowerCase().endsWith('.' + type) && type.length() > 0)
            filename = filename + '.' + type;

        // Get file path for filename
        String filePath = filename;
        if (!filePath.startsWith("/")) {
            WebFile parentDir = aFile.getParent();
            filePath = parentDir.getDirPath() + filename;
        }

        // Get file for path
        WebSite site = getSite();
        WebFile newFile = site.getFileForPath(filePath);

        // If file exists, run option panel for replace
        if (newFile != null) {
            msg = "A file named " + newFile.getName() + " already exists.\n Do you want to replace it with new file?";
            dialogBox = new DialogBox(title);
            dialogBox.setWarningMessage(msg);
            if (!dialogBox.showConfirmDialog(aView))
                return showNewFilePanel(aView, aFile);
            return newFile;
        }

        // If directory, just create and return
        if (aFile.isDir())
            return site.createFileForPath(filePath, true);

        // Create and return new file
        return createNewFile(filePath);
    }

    /**
     * Creates a new file for use with showNewFilePanel method.
     */
    protected WebFile createNewFile(String aPath)
    {
        return getSite().createFileForPath(aPath, false);
    }

    /**
     * Returns a peer object, if this page was provided by another object.
     */
    public Object getPeer()  { return null; }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + ": " + getURL().getString();
    }
}