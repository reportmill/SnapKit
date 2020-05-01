/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;

/**
 * An exception wrapped around a failed response.
 */
public class ResponseException extends RuntimeException {

    // The Response
    WebResponse           _resp;
    
    /**
     * Creates a new ResponseException.
     */
    public ResponseException(WebResponse aResponse)  { super(aResponse.getException()); _resp = aResponse; }

    /**
     * Returns the response.
     */
    public WebResponse getResponse()  { return _resp; }

    /**
     * Returns the response code.
     */
    public int getResponseCode()  { return _resp.getCode(); }

}