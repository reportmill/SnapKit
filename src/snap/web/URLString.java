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
    String          _str;
    
    // The full site string: http://abc.com
    String          _site;
    
    // The scheme string (lowercase): http, file, etc.
    String          _scheme;

    // The full authority string: user@host:port
    String          _auth;

    // The user string
    String          _user;

    // The plain host string
    String          _host;

    // The port
    int             _port;
    
    // The plain path string: /dir/dir/file.ext
    String          _path;

    // The full query string: ?this=that;name=joe
    String          _query;

    // The ref string: #something
    String          _ref;

    // An object to help parse refs
    MapString       _rm;

    /**
     * Creates a URLString for given string.
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

        // Pick off reference
        String str = aStr;
        int rind = str.lastIndexOf('#');
        if (rind>0) {
            _ref = str.substring(rind+1); str = str.substring(0, rind); }

        // Pick off Query
        int qind = str.lastIndexOf('?');
        if (qind>0) {
            _query = str.substring(qind+1); str = str.substring(0, qind); }

        // Pick off nested path
        int npath = str.lastIndexOf('!');
        if (npath>0) {
            _path = str.substring(npath+1); str = str.substring(0, npath);
            _site = str;
        }

        // Pick off scheme
        int sind = str.indexOf(':');
        if (sind>0) {
            _scheme = str.substring(0, sind).toLowerCase(); str = str.substring(sind+1); }

        // If nested, just return
        if (_site!=null) return;

        // Strip off '//'
        int astart = 0; while(astart<str.length() && astart<2 && str.charAt(astart)=='/') astart++;
        if (astart!=1) str = str.substring(astart);

        // Pick off path
        int pind = str.indexOf('/');
        if (pind>=0) {
            _path = str.substring(pind); str = str.substring(0, pind); }

        // Set SiteURL string
        _site = _scheme + "://" + str;

        // Pick off port
        int po_ind = str.lastIndexOf(':');
        if (po_ind>0) {
            _port = Integer.valueOf(str.substring(po_ind+1)); str = str.substring(0, po_ind); }

        // Pick off user
        int uind = str.indexOf('@');
        if (uind>0) {
            _user = str.substring(0, uind); str = str.substring(uind+1); }

        // Anything left is host!
        if (str.length()>0)
            _host = str;
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
    public String getRefValue(String aKey)
    {
        if (_ref==null) return null;
        if (_rm==null) _rm = new MapString(_ref);
        return _rm.getValue(aKey);
    }

    /**
     * Returns whether URL specifies only the file (no query/hashtags).
     */
    public boolean isFileURL()  { return getQuery()==null && getRef()==null; }

    /**
     * Returns the URL string for the file only (no query/hashtags).
     */
    public String getFileURL()
    {
        String str = getString(); int ind = str.indexOf('?'); if(ind<0) ind = str.indexOf('#');
        if (ind>=0) str = str.substring(0, ind);
        return str;
    }

    /**
     * Returns whether URL specifies only file and query (no hashtag references).
     */
    public boolean isQueryURL()  { return getRef()==null; }

    /**
     * Returns the URL string for the file and query only (no hashtag references).
     */
    public String getQueryURL()
    {
        String str = getString();
        int ind = str.indexOf('#');
        if (ind>=0) str = str.substring(0, ind);
        return str;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj==this) return true;
        URLString other = anObj instanceof URLString ? (URLString)anObj : null; if (other==null) return false;
        return _str.equals(other._str);
    }

    /**
     * Standard HashCode implementation.
     */
    public int hashCode()  { return _str.hashCode(); }

    /**
     * Standard toString implementation.
     */
    public String toString()  { return "URLString: " + getString(); }

}