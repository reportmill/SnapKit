/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.SnapUtils;
import snap.util.URLUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

/**
 * A class to represent a URL for a WebSite and WebFile (it can be both for nested sources).
 * Has the form: [Scheme:][//Authority][/Path[!/Path]][?Query][#HashTag].
 * Authority has the form: [UserInfo@]Host[:Port].
 * <p>
 * WebURL is a thin wrapper around standard URL, but provides easy access to the WebSite and WebFile.
 */
public class WebURL {

    // The source object (String, File, URL)
    private Object  _src;

    // The source object as URL
    private URL  _srcURL;

    // The native URL
    private URL  _jurl;

    // The URL string
    private URLString  _urlString;

    // The URL of WebSite this WebURL belongs to (just this WebURL if no path)
    private WebURL  _siteURL;

    // The WebSite for the URL
    protected WebSite  _asSite;

    /**
     * Creates a WebURL for given source.
     */
    protected WebURL(Object aSource)
    {
        // Set source
        _src = aSource;

        // Get/set Source Java URL
        _srcURL = WebGetter.getJavaURL(aSource);

        // Get URLString for parts
        String urls = URLUtils.getString(_srcURL);
        _urlString = new URLString(urls);
    }

    /**
     * Returns a URL for given object.
     */
    public static WebURL getURL(Object anObj)
    {
        try { return getURLOrThrow(anObj); }
        catch (Exception e) { return null; }
    }

    /**
     * Returns a URL for given object.
     */
    public static WebURL getURLOrThrow(Object anObj)
    {
        // Handle null, WebURL, WebFile
        if (anObj == null || anObj instanceof WebURL)
            return (WebURL) anObj;
        if (anObj instanceof WebFile)
            return ((WebFile) anObj).getURL();

        // Get URL
        return new WebURL(anObj);
    }

    /**
     * Returns a URL for given class and resource name.
     */
    public static WebURL getURL(Class<?> aClass, String aName)
    {
        URL url = WebGetter.getJavaUrlForClass(aClass, aName);
        if (url != null)
            return new WebURL(url);
        return null;
    }

    /**
     * Returns the source of this URL (java.net.URL, File, String).
     */
    public Object getSource()  { return _src; }

    /**
     * Returns the source as standard Java URL. Might contain site-path separator. See getJavaURL().
     */
    public URL getSourceURL()  { return _srcURL; }

    /**
     * Returns the full URL string.
     */
    public String getString()
    {
        return _urlString.getString();
    }

    /**
     * Returns the URL Scheme (lower case).
     */
    public String getScheme()
    {
        return _urlString.getScheme();
    }

    /**
     * Returns the Host part of the URL (the Authority minus the optional UserInfo and Port).
     */
    public String getHost()
    {
        return _urlString.getHost();
    }

    /**
     * Returns the port of the URL.
     */
    public int getPort()
    {
        return _urlString.getPort();
    }

    /**
     * Returns the part of the URL string that describes the file path.
     */
    public String getPath()
    {
        return _urlString.getPath();
    }

    /**
     * Returns the last component of the file path.
     */
    public String getPathName()
    {
        return _urlString.getPathName();
    }

    /**
     * Returns the last component of the file path minus any '.' extension suffix.
     */
    public String getPathNameSimple()
    {
        return _urlString.getPathNameSimple();
    }

    /**
     * Returns the part of the URL string that describes the query.
     */
    public String getQuery()
    {
        return _urlString.getQuery();
    }

    /**
     * Returns the value for given Query key in URL, if available.
     */
    public String getQueryValue(String aKey)
    {
        return _urlString.getQueryValue(aKey);
    }

    /**
     * Returns the hash tag reference from the URL as a simple string.
     */
    public String getRef()
    {
        return _urlString.getRef();
    }

    /**
     * Returns the value for given HashTag key in URL, if available.
     */
    public String getRefValue(String aKey)
    {
        return _urlString.getRefValue(aKey);
    }

    /**
     * Returns the site that this URL belongs to.
     */
    public WebSite getSite()
    {
        WebURL siteURL = getSiteURL();
        return siteURL.getAsSite();
    }

    /**
     * Returns the URL for the site that this URL belongs to.
     */
    public WebURL getSiteURL()
    {
        // If already set, just return
        if (_siteURL != null) return _siteURL;

        // Get site for site string
        String siteURLString = _urlString.getSite();
        WebURL siteURL = getURL(siteURLString);

        // Set/return
        return _siteURL = siteURL;
    }

    /**
     * Returns the site for this URL (assumes this URL is a site).
     */
    public WebSite getAsSite()
    {
        // If already set, just return
        if (_asSite != null) return _asSite;

        // Get/set/return
        WebSite site = WebGetter.getSite(this);
        return _asSite = site;
    }

    /**
     * Returns the file for the URL.
     */
    public WebFile getFile()
    {
        WebSite site = getSite();
        String path = getPath();
        if (path != null)
            return site.getFileForPath(path);

        // Fallback to root dir?
        return site.getRootDir();
    }

    /**
     * Creates a file for the URL.
     */
    public WebFile createFile(boolean isDir)
    {
        String path = getPath();
        WebSite site = getSite();
        if (path != null)
            return site.createFileForPath(path, isDir);

        // Fallback to root dir?
        return site.getRootDir();
    }

    /**
     * Returns whether URL specifies only the file (no query/hashtags).
     */
    public boolean isFileURL()
    {
        return _urlString.isFileURL();
    }

    /**
     * Returns the URL for the file only (no query/hashtags).
     */
    public WebURL getFileURL()
    {
        if (isFileURL())
            return this;
        String fileUrlStr = _urlString.getFileURL();
        return getURL(fileUrlStr);
    }

    /**
     * Returns whether URL specifies only file and query (no hashtag references).
     */
    public boolean isQueryURL()
    {
        return _urlString.isQueryURL();
    }

    /**
     * Returns the URL for the file and query only (no hashtag references).
     */
    public WebURL getQueryURL()
    {
        if (isQueryURL())
            return this;
        String queryUrlStr = _urlString.getQueryURL();
        return getURL(queryUrlStr);
    }

    /**
     * Returns the source as standard Java URL (if possible).
     */
    public URL getJavaURL()
    {
        // If already set, just return
        if (_jurl != null) return _jurl;

        // If URL doesn't have site path, just set/return SourceURL
        if (getString().indexOf('!') < 0)
            return _jurl = _srcURL;

        // Get URL string without site path separator and create/set/return URL
        String urlString = getString().replace("!", "");
        try { return _jurl = new URL(urlString); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the source as standard Java File (if possible).
     */
    public File getJavaFile()
    {
        WebSite site = getSite();
        return site.getJavaFile(this);
    }

    /**
     * Returns whether URL can be found.
     */
    public boolean isFound()
    {
        // Handle File
        if (!SnapUtils.isTeaVM && _src instanceof File)
            return ((File) _src).exists();

        // Otherwise see if getHead() returns OK
        WebResponse headResp = getHead();
        return headResp.getCode() == WebResponse.OK;
    }

    /**
     * Returns the last modified time.
     */
    public long getLastModTime()
    {
        // For the time being, just return bogus value when TeaVM checks
        if (SnapUtils.isTeaVM) return 1000000L;

        // Handle File or URL
        if (_src instanceof File)
            return ((File) _src).lastModified();

        // Handle URL
        if (_src instanceof URL) {
            URL url = getJavaURL();
            try { return url.openConnection().getLastModified(); }
            catch (IOException e) { return 0; }
        }

        // Otherwise, return FileHeader.LastModTime
        FileHeader fileHeader = getFileHeader();
        return fileHeader.getModTime();
    }

    /**
     * Returns bytes for this URL.
     */
    public byte[] getBytes()
    {
        try { return getBytesOrThrow(); }
        catch (Exception e) { return null; }
    }

    /**
     * Returns bytes for this URL.
     */
    public byte[] getBytesOrThrow() throws IOException
    {
        // Handle File or URL
        if (!SnapUtils.isTeaVM && (_src instanceof File || _src instanceof URL))
            return SnapUtils.getBytesOrThrow(_src);

        // Otherwise get response and return bytes
        WebResponse resp = getResponse();
        if (resp.getException() != null)                // If response hit exception, throw it
            throw new ResponseException(resp);
        return resp.getBytes();
    }

    /**
     * Returns bytes for this URL.
     */
    public byte[] postBytes(byte[] theBytes)
    {
        WebSite site = getSite();
        WebRequest req = new WebRequest(this);
        req.setPostBytes(theBytes);
        WebResponse resp = site.getResponse(req);
        if (resp.getException() != null)                // If response hit exception, throw it
            throw new ResponseException(resp);
        return resp.getBytes();
    }

    /**
     * Returns the file bytes as a string.
     */
    public String getText()
    {
        byte[] bytes = getBytes();
        if (bytes == null)
            return null;
        return new String(bytes);
    }

    /**
     * Returns an input stream for file.
     */
    public InputStream getInputStream()
    {
        byte[] bytes = getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Returns the response for a HEAD request.
     */
    public WebResponse getHead()
    {
        WebSite site = getSite();
        WebRequest req = new WebRequest(this);
        req.setType(WebRequest.Type.HEAD);
        return site.getResponse(req);
    }

    /**
     * Returns the FileHeader.
     */
    public FileHeader getFileHeader()
    {
        WebResponse resp = getHead();
        return resp.getFileHeader();
    }

    /**
     * Returns Response for a Get request.
     */
    public WebResponse getResponse()
    {
        WebSite site = getSite();
        WebRequest req = new WebRequest(this);
        return site.getResponse(req);
    }

    /**
     * Returns bytes for this URL.
     */
    public void getResponseAndCall(Consumer<WebResponse> aCallback)
    {
        WebSite site = getSite();
        WebRequest req = new WebRequest(this);
        site.getResponseAndCall(req, aCallback);
    }

    /**
     * Returns the parent URL.
     */
    public WebURL getParent()
    {
        String path = getPath();
        if (path.equals("/"))
            return null;

        WebSite site = getSite();
        String parPath = PathUtils.getParent(path);
        return site.getURL(parPath);
    }

    /**
     * Returns the child URL for given name.
     */
    public WebURL getChild(String aName)
    {
        String path = getPath();
        String childPath = PathUtils.getChild(path, aName);
        WebSite site = getSite();
        return site.getURL(childPath);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        WebURL other = anObj instanceof WebURL ? (WebURL) anObj : null;
        if (other == null) return false;
        return _urlString.equals(other._urlString);
    }

    /**
     * Standard HashCode implementation.
     */
    @Override
    public int hashCode()
    {
        return _urlString.hashCode();
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "WebURL: " + getString();
    }
}