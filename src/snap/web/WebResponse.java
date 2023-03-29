/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.List;
import snap.util.FilePathUtils;
import snap.util.JSValue;
import snap.util.JSParser;

/**
 * The response.
 */
public class WebResponse {

    // The request that generated this response
    private WebRequest  _request;
    
    // The response code
    private int  _code = OK;
    
    // The response time
    private long  _time;
    
    // Whether file is a directory
    private boolean  _dir;
    
    // The file modified time
    private long  _modTime;
    
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
    private FileHeader[]  _fileHeaders;
    
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
     * Returns the response time.
     */
    public long getTime()  { return _time; }

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
     * Returns the path file type (extension in lowercase, no dot).
     */
    public String getPathType()
    {
        String path = getPath();
        return FilePathUtils.getType(path);
    }

    /**
     * Returns the response MIME type.
     */
    public String getMimeType()
    {
        if (_mimeType != null) return _mimeType;
        String path = getPath();
        String mimeType = MIMEType.getType(path);
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
     * Returns the file modification time.
     */
    public long getModTime()  { return _modTime; }

    /**
     * Sets the file modification time.
     */
    public void setModTime(long aTime)  { _modTime = aTime; }

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
        fileHeader.setModTime(getModTime());
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
        _modTime = aFHdr.getModTime();
        _size = aFHdr.getSize();
        _mimeType = aFHdr.getMimeType();
    }

    /**
     * Returns the files (for directory request).
     */
    public FileHeader[] getFileHeaders()  { return _fileHeaders; }

    /**
     * Sets the files (for directory request).
     */
    public void setFileHeaders(FileHeader[] fileHeaders)
    {
        _fileHeaders = fileHeaders;
    }

    /**
     * Sets the files (for directory request).
     */
    @Deprecated
    public void setFileHeaders(List<FileHeader> theFile)
    {
        _fileHeaders = theFile.toArray(new FileHeader[0]);
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
        if (_code == 0)
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
            default: return "Unknown error code";
        }
    }
}