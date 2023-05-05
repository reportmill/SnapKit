/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.FilePathUtils;

/**
 * A class to parse a URL string and provide the parts.
 * Has the form: [Scheme:][//Authority][/Path[!/Path]][?Query][#HashTag].
 * Authority has the form: [UserInfo@]Host[:Port].
 */
public class ParsedURL {

    // The URL string
    private String  _str;
    
    // The scheme string (lowercase): http, file, etc.
    private String  _scheme;

    // The full site string: http://abc.com
    private String  _siteUrl;

    // The site identifier - generally a name or path
    private String  _siteId;

    // The full authority string: user@host:port
    //private String  _auth;

    // The user string
    private String  _user;

    // The port
    private int  _port;
    
    // The plain path string: /dir/dir/file.ext
    private String  _path;

    // The full query string: ?this=that;name=joe
    private String  _query;

    // The hashtag string: #something
    private String  _hashtag;

    /**
     * Constructor for given string.
     */
    public ParsedURL(String aStr)
    {
        parseString(aStr);
    }

    /**
     * Returns the URL string.
     */
    public String getString()  { return _str; }

    /**
     * Sets the string and it's pieces.
     */
    protected void parseString(String aStr)
    {
        // Set String
        _str = aStr;

        // Parse scheme
        String str = parseScheme(aStr);

        // Parse parameters (query and hashtag)
        str = parseParameters(str);

        // If nested path char '!', get Site/Path and return
        int nestedPathStart = str.lastIndexOf('!');
        if (nestedPathStart > 0) {
            _siteId = str.substring(0, nestedPathStart);
            _path = str.substring(nestedPathStart + 1);
            _siteUrl = _scheme + ':' + _siteId;
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
        //else _path = "/";

        // Set SiteURL string
        _siteUrl = _scheme + "://" + str;

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

        // Anything left is host
        if (str.length() > 0)
            _siteId = str;

        // Handle JRT special
        if (_scheme.equals("jrt"))
            fixJRT();
    }

    /**
     * Parses the URL scheme.
     */
    private String parseScheme(String urlString)
    {
        // Parse scheme
        int schemeEnd = urlString.indexOf(':');
        if (schemeEnd > 0) {
            _scheme = urlString.substring(0, schemeEnd).toLowerCase();
            urlString = urlString.substring(schemeEnd + 1);
        }

        // Return
        return urlString;
    }

    /**
     * Parses the URL parameters (query and hashtab) and returns the plain URL.
     */
    private String parseParameters(String urlString)
    {
        // Pick off reference
        int referenceStart = urlString.lastIndexOf('#');
        if (referenceStart > 0) {
            _hashtag = urlString.substring(referenceStart + 1);
            urlString = urlString.substring(0, referenceStart);
        }

        // Pick off Query
        int queryStart = urlString.lastIndexOf('?');
        if (queryStart > 0) {
            _query = urlString.substring(queryStart + 1);
            urlString = urlString.substring(0, queryStart);
        }

        // Return
        return urlString;
    }

    /**
     * Returns the URL Scheme (lower case).
     */
    public String getScheme()  { return _scheme; }

    /**
     * Returns the site id string.
     */
    public String getSiteId()  { return _siteId; }

    /**
     * Returns the site string.
     */
    public String getSiteUrl()  { return _siteUrl; }

    /**
     * Returns the User part of the URL.
     */
    public String getUser()  { return _user; }

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
    public String getFilename()
    {
        String path = getPath();
        return FilePathUtils.getFilename(path);
    }

    /**
     * Returns the filename without extension.
     */
    public String getFilenameSimple()
    {
        String path = getPath();
        return FilePathUtils.getFilenameSimple(path);
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
        ParsedUrlArgs queryArgs = new ParsedUrlArgs(queryStr);
        return queryArgs.getValue(aKey);
    }

    /**
     * Returns the hashtag reference from the URL as a simple string.
     */
    public String getHashtag()  { return _hashtag; }

    /**
     * Returns the value for given hashtag key in URL, if available.
     */
    public String getHashtagValue(String aKey)
    {
        if (_hashtag == null)
            return null;
        ParsedUrlArgs hashtagArgs = new ParsedUrlArgs(_hashtag);
        return hashtagArgs.getValue(aKey);
    }

    /**
     * Returns whether URL specifies only the file (no query/hashtags).
     */
    public boolean isFileURL()  { return getQuery() == null && getHashtag() == null; }

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
    public boolean isQueryURL()  { return getHashtag() == null; }

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
            _siteUrl = "jrt:" + _path;
            _path = "/";
        }
        else {
            _siteUrl = "jrt:" + _path.substring(0, moduleNameEnd);
            _path = _path.substring(moduleNameEnd);
        }
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        ParsedURL other = anObj instanceof ParsedURL ? (ParsedURL) anObj : null; if (other == null) return false;
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