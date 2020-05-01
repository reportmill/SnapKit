/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;

/**
 * A request to a server.
 */
public class WebRequest {

    // The URL
    WebURL         _url;
    
    // The file - if request was for existing file
    WebFile        _file;
    
    // The type of request
    Type           _type = Type.GET;
    
    // The bytes to send with request
    byte           _sendBytes[];
    
    // Constants for request type
    public enum Type  { GET, HEAD, POST, PUT, DELETE }

    /**
     * Creates a new WebRequestL.
     */
    public WebRequest()  { }

    /**
     * Creates a new WebRequest for URL.
     */
    public WebRequest(WebURL aURL)  { _url = aURL; }

    /**
     * Creates a new WebRequest for URL.
     */
    public WebRequest(WebFile aFile)  { _file = aFile; _url = aFile.getURL(); }

    /**
     * Returns the site for the request.
     */
    public WebSite getSite()  { return _url.getSite(); }

    /**
     * Returns the URL.
     */
    public WebURL getURL()  { return _url; }

    /**
     * Returns the File if previously set or cached in site.
     */
    public WebFile getFile()
    {
        if (_file!=null) return _file;
        return _file = getSite().getFileCacheFile(_url.getPath());
    }

    /**
     * Returns the type of request.
     */
    public Type getType()  { return _type; }

    /**
     * Sets the type of request.
     */
    public void setType(Type aType)  { _type = aType; }

    /**
     * Returns the post bytes.
     */
    public byte[] getSendBytes()  { return _sendBytes; }

    /**
     * Sets the bytes to send.
     */
    public void setSendBytes(byte theBytes[])  { _sendBytes = theBytes; }

    /**
     * Sets the bytes to POST.
     */
    public void setPostBytes(byte theBytes[])  { setSendBytes(theBytes); _type = Type.POST; }

    /**
     * Sets the bytes to PUT.
     */
    public void setPutBytes(byte theBytes[])  { setSendBytes(theBytes); _type = Type.PUT; }

    /**
     * Standard toString implementation.
     */
    public String toString()  { return "Request " + getType() + ' ' + getURL().getString(); }

}