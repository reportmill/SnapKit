/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;
import snap.util.JSValue;
import snap.util.JSParser;
import snap.web.HTTPRequest.Header;

/**
 * A class to represent an HTTP response.
 */
public class HTTPResponse {

    // The return code
    protected int  _code = -1;
    
    // The return message
    protected String  _message;

    // List of headers
    private List<Header>  _headers = new ArrayList<>();
    
    // The content type
    protected String  _contentType;
    
    // The length
    protected int  _contentLength;
    
    // The last modified
    protected long  _lastModified;

    // The bytes
    protected byte[]  _bytes;
    
    // List of cookies
    protected List<String>  _cookies = new ArrayList<>();
    
    // The time the response took to return
    protected long  _time;
    
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
    public List<Header> getHeaders()  { return _headers; }

    /**
     * Adds a header for key.
     */
    public void addHeader(String aKey, String aValue)
    {
        if (_headers == null) _headers = new ArrayList<>();
        Header header = new Header(aKey, aValue);
        _headers.add(header);
    }

    /**
     * Returns the headers as a string.
     */
    public String getHeaderString()
    {
        if (getHeaders().size() == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (Header header : getHeaders())
            sb.append(header.key).append('=').append(header.value).append(", ");
        sb.delete(sb.length() - 2, sb.length());
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
        // If none or one, just return
        if (getCookies().size() == 0) return null;
        if (getCookies().size() == 1) return getCookies().get(0);

        // Add cookie strings joined
        StringBuilder sb = new StringBuilder();
        for (String cookie : getCookies())
            sb.append(cookie).append("; ");
        sb.delete(sb.length() - 2, sb.length());

        // Return
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
        byte[] bytes = getBytes();
        String text = bytes != null ? new String(bytes).trim() : null;
        return text;
    }

    /**
     * Returns the JSON.
     */
    public JSValue getJSON()
    {
        String text = getText();
        if (text == null)
            return null;
        JSParser parser = new JSParser();
        JSValue json = parser.readString(text);
        return json;
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
            case OK: return "OK";
            case BAD_REQUEST: return "BadRequest";
            case UNAUTHORIZED: return "Unauthorized";
            case FORBIDDEN: return "Forbidden";
            case NOT_FOUND: return "NotFound";
            default: return "Unknown code" + Integer.toString(aCode);
        }
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Returns a string representation.
     */
    public String toStringProps()
    {
        // Add ContentType
        StringBuilder sb = new StringBuilder();
        String contentType = getContentType();
        sb.append("ContentType: ").append(contentType).append(", ");

        // Add Headers
        for(Header hdr : getHeaders())
            sb.append("Header: ").append(hdr.key).append(" = ").append(hdr.value).append(", ");

        // Add Cookie
        String cookieStr = getCookieString();
        if (cookieStr != null)
            sb.append("Cookie: ").append(cookieStr).append(", ");

        // Add Bytes.Length
        int bytesLen = _bytes != null ? _bytes.length : 0;
        sb.append("Bytes.Length: ").append(bytesLen);

        // Return
        return sb.toString();
    }
}