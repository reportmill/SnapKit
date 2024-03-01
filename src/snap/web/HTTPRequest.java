/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.SnapUtils;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A class to represent an HTTP request and generate a response.
 */
public class HTTPRequest {

    // The URL
    private URL  _url;
    
    // The request method
    private String  _method = "GET";
    
    // The headers
    private List<Header>  _headers;
    
    // The cookie
    private String  _cookie;
    
    // The bytes to post
    private byte[]  _bytes;
    
    /**
     * Creates a new URL from Java URL.
     */
    public HTTPRequest(URL aURL)
    {
        _url = aURL;
    }

    /**
     * Creates a new URL from string.
     */
    public HTTPRequest(String aString)
    {
        try { _url = new URL(aString); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the URL.
     */
    public URL getURL()  { return _url; }

    /**
     * Returns the URL string.
     */
    public String getURLString()  { return _url.toExternalForm(); }

    /**
     * Returns the request method (always upper case).
     */
    public String getMethod()  { return _method; }

    /**
     * Sets the request method.
     */
    public void setMethod(String aMethod)  { _method = aMethod.toUpperCase(); }

    /**
     * Returns the cookie.
     */
    public String getCookie()  { return _cookie; }

    /**
     * Sets the cookie.
     */
    public void setCookie(String aString)  { _cookie = aString; }

    /**
     * Returns the headers.
     */
    public List <Header> getHeaders()  { return _headers; }

    /**
     * Adds a header for key.
     */
    public void addHeader(String aKey, String aValue)
    {
        if (_headers == null) _headers = new ArrayList<>();
        _headers.add(new Header(aKey, aValue));
    }

    /**
     * A class to hold headers.
     */
    public static class Header {
        String key;
        String value;
        public Header(String aKey, String aValue)
        {
            key = aKey;
            value = aValue;
        }
    }

    /**
     * Returns the bytes associated with request (POST).
     */
    public byte[] getBytes()  { return _bytes; }

    /**
     * Sets the bytes associated with the request (POST).
     */
    public void setBytes(byte[] theBytes)
    {
        _bytes = theBytes;
        if (getMethod().equals("GET"))
            setMethod("POST");
    }

    /**
     * Executes this request and returns a response.
     */
    public HTTPResponse getResponse() throws IOException
    {
        // Create response
        HTTPResponse resp = new HTTPResponse();

        // Get start time
        resp._time = System.currentTimeMillis();

        // Get connection
        URL url = getURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Some servers require a User-Agent header
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (SnapKit)");

        // Set request method
        String method = getMethod();
        if (!method.equals("GET"))
            connection.setRequestMethod(method);

        // Append additional headers
        List<HTTPRequest.Header> headers = getHeaders();
        if (headers != null) {
            for (Header header : headers)
                connection.setRequestProperty(header.key, header.value);
        }

        // Append cookies
        String cookie = getCookie();
        if (cookie != null)
            connection.setRequestProperty("Cookie", cookie);

        // If bytes are provided append them
        byte[] putBytes = getBytes();
        if (putBytes != null) {
            connection.setDoOutput(true);
            OutputStream outStream = connection.getOutputStream();
            outStream.write(putBytes);
            outStream.flush();
        }

        // Get the response code
        resp._code = connection.getResponseCode();
        resp._message = connection.getResponseMessage();

        // Get headers and cookies
        for (int i = 1; true; i++) {
            String headerKey = connection.getHeaderFieldKey(i);
            if (headerKey == null)
                break;
            String headerValue = connection.getHeaderField(i);
            resp.addHeader(headerKey, headerValue);
            if (headerKey.equals("Set-Cookie"))
                resp._cookies.add(headerValue);
        }

        // Set response time
        resp._time = System.currentTimeMillis() - resp._time;

        // If response code not success, just return
        if (resp._code != HTTPResponse.OK)
            return resp;

        // Get ContentType, Length, LastModified
        resp._contentType = connection.getContentType();
        resp._contentLength = SnapUtils.isTeaVM ? 0 : connection.getContentLength();
        resp._lastModified = SnapUtils.isTeaVM || SnapUtils.isWebVM ? 0 : connection.getLastModified();

        // Get response bytes
        if (!method.equals("HEAD")) {
            InputStream inputStream = connection.getInputStream();
            resp._bytes = SnapUtils.getInputStreamBytes(inputStream);
        }

        // Return response
        return resp;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + ": " + getURLString();
    }
}