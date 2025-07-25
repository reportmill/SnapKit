/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.FilePathUtils;
import snap.util.JSValue;
import snap.util.JSParser;

import java.util.Arrays;
import java.util.List;

/**
 * The response.
 */
public class WebResponse {

    // The request that generated this response
    private WebRequest  _request;
    
    // The response code
    private int  _code = OK;
    
    // Whether file is a directory
    private boolean  _dir;
    
    // The file last modified time
    private long _lastModTime;
    
    // The file size
    private long  _size;
    
    // The MIME type
    private String  _mimeType;
    
    // The response bytes
    private byte[]  _bytes;
    
    // The response text
    private String  _text;
    
    // The response file header
    private FileHeader  _fileHeader;
    
    // The response files (if directory get)
    private List<FileHeader> _fileHeaders;
    
    // An exception if response represents an exception
    private Throwable  _exception;
    
    // Constants for response codes (http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
    public static final int OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int EXCEPTION_THROWN = 420;
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * Creates a new WebResponse for given request.
     */
    public WebResponse(WebRequest aReq)
    {
        super();
        setRequest(aReq);
    }

    /**
     * Returns the request.
     */
    public WebRequest getRequest()  { return _request; }

    /**
     * Sets the request.
     */
    public void setRequest(WebRequest aRequest)  { _request = aRequest; }

    /**
     * Returns the request URL.
     */
    public WebURL getURL()
    {
        return _request.getURL();
    }

    /**
     * Returns the site for the request/response.
     */
    public WebSite getSite()
    {
        return _request.getSite();
    }

    /**
     * Returns the response code.
     */
    public int getCode()  { return _code; }

    /**
     * Sets the response code.
     */
    public void setCode(int aCode)  { _code = aCode; }

    /**
     * Returns the code message.
     */
    public String getCodeString()  { return getCodeString(_code); }

    /**
     * Returns the path.
     */
    public String getPath()
    {
        WebURL url = getURL();
        return url.getPath();
    }

    /**
     * Returns the path file name.
     */
    public String getPathName()
    {
        WebURL url = getURL();
        return url.getFilename();
    }

    /**
     * Returns the file type (extension in lowercase, no dot).
     */
    public String getFileType()
    {
        String path = getPath();
        return FilePathUtils.getFileType(path);
    }

    /**
     * Returns the response MIME type.
     */
    public String getMimeType()
    {
        if (_mimeType != null) return _mimeType;
        String path = getPath();
        String mimeType = MIMEType.getMimeTypeForPath(path);
        return _mimeType = mimeType;
    }

    /**
     * Sets the response MIME type.
     */
    protected void setMimeType(String aMIMEType)
    {
        _mimeType = aMIMEType;
    }

    /**
     * Returns whether file is a directory.
     */
    public boolean isDir()  { return _dir; }

    /**
     * Sets whether file is a directory.
     */
    public void setDir(boolean aValue)  { _dir = aValue; }

    /**
     * Returns whether file is a plain file.
     */
    public boolean isFile()  { return !_dir; }

    /**
     * Returns the file last modified time.
     */
    public long getLastModTime()  { return _lastModTime; }

    /**
     * Sets the file last modified time.
     */
    public void setLastModTime(long aTime)  { _lastModTime = aTime; }

    /**
     * Returns the file size.
     */
    public long getSize()  { return _size; }

    /**
     * Sets the file size.
     */
    public void setSize(long aSize)  { _size = aSize; }

    /**
     * Returns the file header.
     */
    public FileHeader getFileHeader()
    {
        // If already set, just return
        if (_fileHeader != null) return _fileHeader;

        // Create and return
        FileHeader fileHeader = new FileHeader(getPath(), isDir());
        fileHeader.setLastModTime(getLastModTime());
        fileHeader.setSize(getSize());
        fileHeader.setMimeType(getMimeType());
        return _fileHeader = fileHeader;
    }

    /**
     * Sets the file header. Should go soon.
     */
    public void setFileHeader(FileHeader aFHdr)
    {
        _fileHeader = aFHdr;
        _dir = aFHdr.isDir();
        _lastModTime = aFHdr.getLastModTime();
        _size = aFHdr.getSize();
        _mimeType = aFHdr.getMimeType();
    }

    /**
     * Returns the files (for directory request).
     */
    public List<FileHeader> getFileHeaders()  { return _fileHeaders; }

    /**
     * Sets the files (for directory request).
     */
    public void setFileHeaders(List<FileHeader> fileHeaders)
    {
        _fileHeaders = fileHeaders;
    }

    /**
     * Returns the bytes.
     */
    public byte[] getBytes()  { return _bytes; }

    /**
     * Sets the response bytes.
     */
    public void setBytes(byte[] theBytes)  { _bytes = theBytes; }

    /**
     * Returns the exception.
     */
    public Throwable getException()  { return _exception; }

    /**
     * Sets the exception.
     */
    public void setException(Throwable aThrowable)
    {
        _exception = aThrowable;
        _code = EXCEPTION_THROWN;
    }

    /**
     * Returns whether response is text (regardless of what the data type is).
     */
    public boolean isText()
    {
        byte[] bytes = getBytes();
        if (bytes == null)
            return false;
        byte junk = 0;
        for (byte b : bytes) {
            if ((b & 0xFF) > 127) {
                junk++;
                if (junk > 10)
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns the text of the response.
     */
    public String getText()
    {
        if (_text != null) return _text;
        if (_bytes != null)
            _text = new String(_bytes);
        return _text;
    }

    /**
     * Returns the file.
     */
    public WebFile getFile()
    {
        WebURL url = getURL();
        WebFile file = url.getFile();
        //if (file == null) file = getSite().createFile(getFileHeader()); // This seems wrong

        // Return
        return file;
    }

    /**
     * Copies an HTTP Response.
     */
    public void copyResponse(HTTPResponse aResp)
    {
        setCode(aResp.getCode());
        setBytes(aResp.getBytes());
    }

    /**
     * Returns the error string.
     */
    public String getErrorString()
    {
        if (getException() != null)
            return getException().getMessage();
        return getCodeString();
    }

    /**
     * Returns the JSON.
     */
    public JSValue getJSON()
    {
        String text = getText(); if (text == null) return null;
        JSParser parser = new JSParser();
        JSValue json = parser.readString(text);
        return json;
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
        // Add Code
        String sb = "Code: " + getCode() + ", " +
                "CodeString: " + getCodeString() + ", " +
                "URL: " + getURL().getString();

        // Return
        return sb;
    }

    /**
     * Returns the code message.
     */
    public static String getCodeString(int aCode)
    {
        switch (aCode) {
            case OK: return "OK";
            case BAD_REQUEST: return "Bad or malformed request";
            case UNAUTHORIZED: return "Unauthorized";
            case FORBIDDEN: return "Client is forbidden from accessing valid URL";
            case NOT_FOUND: return "File not Found";
            case METHOD_NOT_ALLOWED: return "Method Not Allowed";
            case INTERNAL_SERVER_ERROR: return "Internal Server Error";
            default: return "Unknown error code";
        }
    }
}