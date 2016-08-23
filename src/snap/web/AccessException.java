/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;

/**
 * An exception that indicates that data source doesn't have permission to access data.
 */
public class AccessException extends RuntimeException {

    // The data site
    WebSite     _site;

    // The URL that failed
    WebURL      _url;

public AccessException()  { }

public AccessException(WebSite aSite, String aMsg)  { super(aMsg); _site = aSite; }

public AccessException(WebSite aSite, Throwable aCause)  { super(aCause); _site = aSite; }

/**
 * Returns the data site that threw the exception.
 */
public WebSite getSite()  { return _site; }

/**
 * Returns the URL that caused the exception (null if exception isn't file specific).
 */
public WebURL getURL()  { return _url; }

}