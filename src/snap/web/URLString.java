/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.FilePathUtils;

/**
 * A class to read a URL string and provide the parts.
 * Has the form: [Scheme:][//Authority][/Path[!/Path]][?Query][#HashTag].
 * Authority has the form: [UserInfo@]Host[:Port].
 */
public class URLString {

    // The URL string
    private String  _str;
    
    // The full site string: http://abc.com
    private String  _site;
    
    // The scheme string (lowercase): http, file, etc.
    private String  _scheme;

    // The full authority string: user@host:port
    //private String  _auth;

    // The user string
    private String  _user;

    // The plain host string
    private String  _host;

    // The port
    private int  _port;
    
    // The plain path string: /dir/dir/file.ext
    private String  _path;

    // The full query string: ?this=that;name=joe
    private String  _query;

    // The ref string: #something
    private String  _ref;

    // An object to help parse refs
    private MapString _refMap;

    /**
     * Constructor for given string.
     */
    public URLString(String aStr)  { setString(aStr); }

    /**
     * Returns the URL string.
     */
    public String getString()  { return _str; }

    /**
     * Sets the string and it's pieces.
     */
    protected void setString(String aStr)
    {
        // Set String
        _str = aStr;

        // Pick off scheme
        String str = aStr;
        int schemeEnd = str.indexOf(':');
        if (schemeEnd > 0) {
            _scheme = str.substring(0, schemeEnd).toLowerCase();
            str = str.substring(schemeEnd + 1);
        }

        // Pick off reference
        int referenceStart = str.lastIndexOf('#');
        if (referenceStart > 0) {
            _ref = str.substring(referenceStart + 1);
            str = str.substring(0, referenceStart);
        }

        // Pick off Query
        int queryStart = str.lastIndexOf('?');
        if (queryStart > 0) {
            _query = str.substring(queryStart + 1);
            str = str.substring(0, queryStart);
        }

        // If nested path char '!', get Site/Path and return
        int nestedPathStart = str.lastIndexOf('!');
        if (nestedPathStart > 0) {
            _site = _scheme + ':' + str.substring(0, nestedPathStart);
            _path = str.substring(nestedPathStart + 1);
            return;
        }

        // Strip off '//'
        if (str.startsWith("//"))
            str = str.substring(2);

        // Pick off path
        int pathIndex = str.indexOf('/');
        if (pathIndex >= 0) {
            _path = str.substring(pathIndex);
            str = str.substring(0, pathIndex);
        }

        // Set SiteURL string
        _site = _scheme + "://" + str;

        // Pick off port
        int portStart = str.lastIndexOf(':');
        if (portStart > 0) {
            String portStr = str.substring(portStart + 1);
            _port = Integer.parseInt(portStr);
            str = str.substring(0, portStart);
        }

        // Pick off user
        int userNameStart = str.indexOf('@');
        if (userNameStart > 0) {
            _user = str.substring(0, userNameStart);
            str = str.substring(userNameStart + 1);
        }

        // Anything left is host!
        if (str.length() > 0)
            _host = str;

        // Handle JRT special
        if (_scheme.equals("jrt"))
            fixJRT();
    }

    /**
     * Returns the site string.
     */
    public String getSite()  { return _site; }

    /**
     * Returns the URL Scheme (lower case).
     */
    public String getScheme()  { return _scheme; }

    /**
     * Returns the User part of the URL.
     */
    public String getUser()  { return _user; }

    /**
     * Returns the Host part of the URL (the Authority minus the optional UserInfo and Port).
     */
    public String getHost()  { return _host; }

    /**
     * Returns the port of the URL.
     */
    public int getPort()  { return _port; }

    /**
     * Returns the part of the string that describes the file path.
     */
    public String getPath()  { return _path; }

    /**
     * Returns the last component of the file path.
     */
    public String getPathName()
    {
        String path = getPath();
        return FilePathUtils.getFileName(path);
    }

    /**
     * Returns the last component of the file path minus any '.' extension suffix.
     */
    public String getPathNameSimple()
    {
        String path = getPath();
        return FilePathUtils.getFileNameSimple(path);
    }

    /**
     * Returns the part of the URL string that describes the query.
     */
    public String getQuery()  { return _query; }

    /**
     * Returns the value for given Query key in URL, if available.
     */
    public String getQueryValue(String aKey)
    {
        String queryStr = getQuery();
        MapString queryMapStr = new MapString(queryStr);
        return queryMapStr.getValue(aKey);
    }

    /**
     * Returns the hash tag reference from the URL as a simple string.
     */
    public String getRef()  { return _ref; }

    /**
     * Returns the value for given HashTag key in URL, if available.
     */
    public String getRefValue(String aKey)
    {
        if (_ref == null)
            return null;
        if (_refMap == null)
            _refMap = new MapString(_ref);
        return _refMap.getValue(aKey);
    }

    /**
     * Returns whether URL specifies only the file (no query/hashtags).
     */
    public boolean isFileURL()  { return getQuery() == null && getRef() == null; }

    /**
     * Returns the URL string for the file only (no query/hashtags).
     */
    public String getFileURL()
    {
        // Get normal url string
        String urlStr = getString();

        // If query or ref, strip it off
        int queryOrRefStart = urlStr.indexOf('?');
        if(queryOrRefStart < 0)
            queryOrRefStart = urlStr.indexOf('#');
        if (queryOrRefStart >= 0)
            urlStr = urlStr.substring(0, queryOrRefStart);

        // Return
        return urlStr;
    }

    /**
     * Returns whether URL specifies only file and query (no hashtag references).
     */
    public boolean isQueryURL()  { return getRef() == null; }

    /**
     * Returns the URL string for the file and query only (no hashtag references).
     */
    public String getQueryURL()
    {
        String urlStr = getString();
        int refStart = urlStr.indexOf('#');
        if (refStart >= 0)
            urlStr = urlStr.substring(0, refStart);
        return urlStr;
    }

    /**
     * Converts URL to JRT (Java runtime module?) form by moving module name from Path to Site.
     */
    private void fixJRT()
    {
        int moduleNameEnd = _path.indexOf('/', 1);
        if (moduleNameEnd < 0) {
            _site = "jrt:" + _path;
            _path = "/";
        }
        else {
            _site = "jrt:" + _path.substring(0, moduleNameEnd);
            _path = _path.substring(moduleNameEnd);
        }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        URLString other = anObj instanceof URLString ? (URLString) anObj : null; if (other == null) return false;
        return _str.equals(other._str);
    }

    /**
     * Standard HashCode implementation.
     */
    @Override
    public int hashCode()  { return _str.hashCode(); }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()  { return "URLString: " + getString(); }
}