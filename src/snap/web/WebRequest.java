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
    
    // The type of request
    Type           _type = Type.GET;
    
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
 * Returns the URL.
 */
public WebURL getURL()  { return _url; }

/**
 * Sets the URL.
 */
public void setURL(WebURL aURL)  { _url = aURL; }

/**
 * Returns the site for the request.
 */
public WebSite getSite()  { return _url.getSite(); }

/**
 * Returns the type of request.
 */
public Type getType()  { return _type; }

/**
 * Sets the type of request.
 */
public void setType(Type aType)  { _type = aType; }

/**
 * Standard toString implementation.
 */
public String toString()  { return "Request " + getType() + ' ' + getURL().getString(); }

}