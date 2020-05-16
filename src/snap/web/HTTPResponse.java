/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;

import snap.util.JSONNode;
import snap.util.JSONParser;
import snap.web.HTTPRequest.Header;

/**
 * A class to represent an HTTP response.
 */
public class HTTPResponse {

    // The return code
    int            _code = -1;
    
    // The return message
    String         _message;

    // List of headers
    List <Header>  _headers = new ArrayList();
    
    // The content type
    String         _contentType;
    
    // The length
    int            _contentLength;
    
    // The last modified
    long           _lastModified;

    // The bytes
    byte           _bytes[];
    
    // List of cookies
    List <String>  _cookies = new ArrayList();
    
    // The time the response took to return
    long           _time;
    
    // Response codes
    public static final int OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;

    /**
     * Returns the response return code.
     */
    public int getCode()  { return _code; }

    /**
     * Returns the response return message.
     */
    public String getMessage()  { return _message; }

    /**
     * Returns the headers.
     */
    public List <Header> getHeaders()  { return _headers; }

    /**
     * Adds a header for key.
     */
    public void addHeader(String aKey, String aValue)
    {
        if (_headers==null) _headers = new ArrayList();
        _headers.add(new Header(aKey, aValue));
    }

    /**
     * Returns the headers as a string.
     */
    public String getHeaderString()
    {
        if (getHeaders().size()==0) return "";
        StringBuffer sb = new StringBuffer();
        for (Header header : getHeaders()) sb.append(header.key).append('=').append(header.value).append(", ");
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }

    /**
     * Returns the cookies.
     */
    public List <String> getCookies()  { return _cookies; }

    /**
     * Returns the cookie string.
     */
    public String getCookieString()
    {
        if (getCookies().size()==0) return null;
        if (getCookies().size()==1) return getCookies().get(0);
        StringBuffer sb = new StringBuffer();
        for (String cookie : getCookies()) sb.append(cookie).append("; ");
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }

    /**
     * Returns the content type.
     */
    public String getContentType()  { return _contentType; }

    /**
     * Returns the content length.
     */
    public int getContentLength()  { return _contentLength; }

    /**
     * Returns the last modified time.
     */
    public long getLastModified()  { return _lastModified; }

    /**
     * Returns the bytes.
     */
    public byte[] getBytes()  { return _bytes; }

    /**
     * Returns the response byte string.
     */
    public String getText()
    {
        return getBytes()!=null? new String(getBytes()).trim() : null;
    }

    /**
     * Returns the JSON.
     */
    public JSONNode getJSON()
    {
        String text = getText();
        return text!=null ? new JSONParser().readString(text) : null;
    }

    /**
     * Returns the response time in milliseconds.
     */
    public long getResponseTime()  { return _time; }

    /**
     * Returns a response summary string.
     */
    public String getSummary()
    {
        return String.format("(%d - %s) %s (time=%.3f)", getCode(), getCodeString(getCode()),
            getHeaderString(), getResponseTime()/1000f);
    }

    /**
     * Returns a string for an HTTP response code.
     */
    static String getCodeString(int aCode)
    {
        switch (aCode) {
            case OK: return "OK"; case BAD_REQUEST: return "BadRequest";
            case UNAUTHORIZED: return "Unauthorized"; case FORBIDDEN: return "Forbidden";
            case NOT_FOUND: return "NotFound"; default: return "Unknown code" + Integer.toString(aCode);
        }
    }

    /**
     * Returns a string representation.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Content Type: ").append(getContentType()).append('\n');
        for(Header hdr : getHeaders()) sb.append("Header: ").append(hdr.key).append(" = ").append(hdr.value).append('\n');
        if (getCookieString()!=null) sb.append("Cookie: ").append(getCookieString()).append('\n');
        sb.append("Response len: " + (_bytes!=null? _bytes.length : 0)).append('\n');
        return sb.toString();
    }
}