/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.net.*;
import snap.gfx.GFXEnv;
import snap.util.FilePathUtils;

/**
 * A class to represent a URL for a WebSite and WebFile (it can be both for nested sources).
 * Has the form: [Scheme:][//Authority][/Path[!/Path]][?Query][#HashTag].
 * Authority has the form: [UserInfo@]Host[:Port].
 * 
 * WebURL is a thin wrapper around standard URL, but provides easy access to the WebSite and WebFile.
 */
public class WebURL {

    // The URL source
    Object          _src;
    
    // The URL string
    String          _str;
    
    // The path string
    String          _scheme, _auth, _user, _host, _path, _query, _ref;
    
    // The port
    int             _port;
    
    // The site URL string
    String          _siteURLS;

    // The URL of WebSite this WebURL belongs to (just this WebURL if no path)
    WebURL          _siteURL;
    
    // The WebSite for the URL
    WebSite         _asSite;
    
    // The WebFile for the URL
    WebFile         _file;

/**
 * Creates a new WebURL for given source and URL string.
 */
public WebURL(Object aSource, String aStr)  { _src = aSource; setString(aStr); }

/**
 * Returns a URL for given object.
 */
public static WebURL getURL(Object anObj)  { return GFXEnv.getEnv().getURL(anObj); }

/**
 * Returns a URL for given class and resource name.
 */
public static WebURL getURL(Class aClass, String aName)  { return GFXEnv.getEnv().getURL(aClass, aName); }

/**
 * Returns the source of this URL (java.net.URL, File, String).
 */
public Object getSource()  { return _src; }

/**
 * Returns the URL string.
 */
public String getString()  { return _str; }

/**
 * Sets the standard URL.
 */
protected void setString(String aStr)
{
    // Set String
    _str = aStr;
    
    // Pick off reference
    String str = aStr;
    int rind = str.lastIndexOf('#');
    if(rind>0) {
        _ref = str.substring(rind+1); str = str.substring(0, rind); }
        
    // Pick off Query
    int qind = str.lastIndexOf('?');
    if(qind>0) {
        _query = str.substring(qind+1); str = str.substring(0, qind); }
        
    // Pick off nested path
    int npath = str.lastIndexOf('!');
    if(npath>0) {
        _path = str.substring(npath+1); str = str.substring(0, npath);
        _siteURLS = str; //return;
    }
    
    // Pick off scheme
    int sind = str.indexOf(':');
    if(sind>0) {
        _scheme = str.substring(0, sind).toLowerCase(); str = str.substring(sind+1); }
        
    // If nested, just return
    if(_siteURLS!=null) return;
        
    // Strip off '//'
    int astart = 0; while(astart<str.length() && astart<2 && str.charAt(astart)=='/') astart++;
    if(astart!=1) str = str.substring(astart);
        
    // Pick off path
    int pind = str.indexOf('/');
    if(pind>=0) {
        _path = str.substring(pind); str = str.substring(0, pind); }
        
    // Set SiteURL string
    _siteURLS = _scheme + "://" + str;
        
    // Pick off port
    int po_ind = str.lastIndexOf(':');
    if(po_ind>0) {
        _port = Integer.valueOf(str.substring(po_ind+1)); str = str.substring(0, po_ind); }
        
    // Pick off user
    int uind = str.indexOf('@');
    if(uind>0) {
        _user = str.substring(0, uind); str = str.substring(uind+1); }
        
    // Anything left is host!
    if(str.length()>0)
        _host = str;
}

/**
 * Returns the URL Scheme (lower case).
 */
public String getScheme()  { return _scheme; }

/**
 * Returns the Host part of the URL (the Authority minus the optional UserInfo and Port).
 */
public String getHost()  { return _host; }

/**
 * Returns the port of the URL.
 */
public int getPort()  { return _port; }

/**
 * Returns the part of the URL string that describes the file path.
 */
public String getPath()  { return _path; }

/**
 * Returns the last component of the file path.
 */
public String getPathName()  { return FilePathUtils.getFileName(getPath()); }

/**
 * Returns the last component of the file path minus any '.' extension suffix.
 */
public String getPathNameSimple()  { return FilePathUtils.getFileNameSimple(getPath()); }

/**
 * Returns the part of the URL string that describes the query.
 */
public String getQuery()  { return _query; }

/**
 * Returns the value for given Query key in URL, if available.
 */
public String getQueryValue(String aKey)  { return new MapString(getQuery()).getValue(aKey); }

/**
 * Returns the hash tag reference from the URL as a simple string.
 */
public String getRef()  { return _ref; }

/**
 * Returns the value for given HashTag key in URL, if available.
 */
public String getRefValue(String aKey)  { return getRefMap().getValue(aKey); }

// Returns the hash tag reference of the URL as a MapString.
private MapString getRefMap()  { return _rm!=null? _rm : (_rm=new MapString(getRef())); } MapString _rm;

/**
 * Returns the source of this URL.
 */
public WebSite getSite()  { return getSiteURL().getAsSite(); }

/**
 * Returns the URL for the source of this URL.
 */
public WebURL getSiteURL()  { return _siteURL!=null? _siteURL : (_siteURL=getURL(_siteURLS)); }

/**
 * Returns whether file has been set/loaded for this URL.
 */
public boolean isFileSet()  { return _file!=null; }

/**
 * Returns the file for the URL.
 */
public WebFile getFile()  { return _file!=null? _file : (_file=getFileImpl()); }

/**
 * Returns the file for the URL.
 */
protected WebFile getFileImpl()
{
    String path = getPath(); WebSite site = getSite();
    WebFile file = path!=null? site.getFile(path) : site.getRootDir();
    if(file!=null && file._url==null) file._url = this;
    return file;
}

/**
 * Creates a file for the URL.
 */
public WebFile createFile(boolean isDir)
{
    String path = getPath(); WebSite site = getSite();
    WebFile file = path!=null? site.createFile(path, isDir) : site.getRootDir();
    if(file!=null && file._url==null) file._url = this;
    if(_file==null) _file = file;
    return file;
}

/**
 * Returns whether URL specifies only the file (no query/hashtags).
 */
public boolean isFileURL()  { return getQuery()==null && getRef()==null; }

/**
 * Returns the URL for the file only (no query/hashtags).
 */
public WebURL getFileURL()  { return isFileURL()? this : getURL(getFileURLString()); }

/**
 * Returns the URL string for the file only (no query/hashtags).
 */
public String getFileURLString()
{
    String str = getString(); int ind = str.indexOf('?'); if(ind<0) ind = str.indexOf('#');
    if(ind>=0) str = str.substring(0, ind);
    return str;
}

/**
 * Returns whether URL specifies only file and query (no hashtag references).
 */
public boolean isQueryURL()  { return getRef()==null; }

/**
 * Returns the URL for the file and query only (no hashtag references).
 */
public WebURL getQueryURL()  { return isQueryURL()? this : getURL(getQueryURLString()); }

/**
 * Returns the URL string for the file and query only (no hashtag references).
 */
public String getQueryURLString()
{
    String str = getString(); int ind = str.indexOf('#'); if(ind>=0) str = str.substring(0, ind);
    return str;
}

/**
 * Returns bytes for this URL.
 */
public byte[] getBytes()
{
    WebSite site = getSite();
    WebRequest req = new WebRequest(this);
    WebResponse resp = site.getResponse(req);
    if(resp.getException()!=null)                // If response hit exception, throw it
        throw new ResponseException(resp);
    return resp.getBytes();
}

/**
 * Returns the file bytes as a string.
 */
public String getText()
{
    byte bytes[] = getBytes(); if(bytes==null) return null;
    return new String(bytes);
}

/**
 * Returns the site for the URL.
 */
public WebSite getAsSite()  { return _asSite!=null? _asSite : (_asSite=GFXEnv.getEnv().getSite(this)); }

/**
 * Returns the standard URL.
 */
public URL getURL()
{
    if(_src instanceof URL) return (URL)_src;
    try { return new URL(_str); }
    catch(MalformedURLException e) { throw new RuntimeException(e); }
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(anObj==this) return true;
    WebURL other = anObj instanceof WebURL? (WebURL)anObj : null; if(other==null) return false;
    return _str.equals(other._str);
}

/**
 * Standard HashCode implementation.
 */
public int hashCode()  { return _str.hashCode(); }

/**
 * Standard toString implementation.
 */
public String toString()  { return "WebURL: " + getString(); }

}