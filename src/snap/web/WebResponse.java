/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.List;
import snap.util.FilePathUtils;
import snap.util.JSONNode;
import snap.util.JSONParser;

/**
 * The response.
 */
public class WebResponse {

    // The request that generated this response
    WebRequest         _request;
    
    // The response code
    int                _code = OK;
    
    // The response time
    long               _time;
    
    // Whether file is a directory
    boolean            _dir;
    
    // The file modified time
    long               _modTime;
    
    // The file size
    long               _size;
    
    // The MIME type
    String             _mimeType;
    
    // The response bytes
    byte               _bytes[];
    
    // The response text
    String             _text;
    
    // The response file header
    FileHeader         _fileHdr;
    
    // The response files (if directory get)
    List <FileHeader>  _files;
    
    // An exception if response represents an exception
    Throwable          _exception;
    
    // Constants for response codes (http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
    public static final int OK = 200;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int EXCEPTION_THROWN = 420;

    /**
     * Creates a new WebResponse for given request.
     */
    public WebResponse(WebRequest aReq)  { setRequest(aReq); }

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
    public WebURL getURL()  { return _request.getURL(); }

    /**
     * Returns the site for the request/response.
     */
    public WebSite getSite()  { return _request.getSite(); }

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
    public String getPath()  { return getURL().getPath(); }

    /**
     * Returns the path file name.
     */
    public String getPathName()  { return getURL().getPathName(); }

    /**
     * Returns the path file type (extension in lowercase, no dot).
     */
    public String getPathType()  { return FilePathUtils.getType(getPath()); }

    /**
     * Returns the response MIME type.
     */
    public String getMIMEType()
    {
        if (_mimeType!=null) return _mimeType;
        return _mimeType = MIMEType.getType(getURL().getPath());
    }

    /**
     * Sets the response MIME type.
     */
    protected void setMIMEType(String aMIMEType)  { _mimeType = aMIMEType; }

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
        if (_fileHdr!=null) return _fileHdr;

        // Create and return
        FileHeader fhdr = new FileHeader(getPath(), isDir());
        fhdr.setModTime(getModTime());
        fhdr.setSize(getSize());
        fhdr.setMIMEtype(getMIMEType());
        return _fileHdr = fhdr;
    }

    /**
     * Sets the file header. Should go soon.
     */
    public void setFileHeader(FileHeader aFHdr)
    {
        _fileHdr = aFHdr;
        _dir = aFHdr.isDir();
        _modTime = aFHdr.getModTime();
        _size = aFHdr.getSize();
        _mimeType = aFHdr.getMIMEType();
    }

    /**
     * Returns the files (for directory request).
     */
    public List <FileHeader> getFileHeaders()  { return _files; }

    /**
     * Sets the files (for directory request).
     */
    public void setFileHeaders(List <FileHeader> theFile)  { _files = theFile; }

    /**
     * Returns the bytes.
     */
    public byte[] getBytes()  { return _bytes; }

    /**
     * Sets the response bytes.
     */
    public void setBytes(byte theBytes[])  { _bytes = theBytes; }

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
        if (_code==0) _code = EXCEPTION_THROWN;
    }

    /**
     * Returns whether response is text (regardless of what the data type is).
     */
    public boolean isText()
    {
        byte bytes[] = getBytes(), junk = 0; if(bytes==null) return false;
        for (byte b : bytes) if((b & 0xFF) > 127) { junk++; if(junk>10) return false; }
        return true;
    }

    /**
     * Returns the text of the response.
     */
    public String getText()
    {
        if (_text!=null) return _text;
        if (_bytes!=null) _text = new String(_bytes);
        return _text;
    }

    /**
     * Returns the file.
     */
    public WebFile getFile()
    {
        WebFile file = getURL().getFile();
        if (file==null)
            file = getSite().createFile(getFileHeader());
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
    public JSONNode getJSON()
    {
        String text = getText();
        return text!=null ? new JSONParser().readString(text) : null;
    }

    /**
     * Standard toString implementation.
     */
    public String toString() { return "Response " + getCode() + ' ' + getCodeString() + ' ' + getURL().getString(); }

    /**
     * Returns the code message.
     */
    public static String getCodeString(int aCode)
    {
        switch (aCode) {
            case OK: return "OK";
            case UNAUTHORIZED: return "Unauthorized";
            case NOT_FOUND: return "Not Found";
            case METHOD_NOT_ALLOWED: return "Method Not Allowed";
            default: return "Unknown";
        }
    }

}