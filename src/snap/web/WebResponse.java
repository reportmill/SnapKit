/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.List;

/**
 * The response.
 */
public class WebResponse {

    // The request that generated this response
    WebRequest         _request;
    
    // The response code
    int                _code;
    
    // The response time
    long               _time;
    
    // The response content/data type
    DataType           _dataType;
    
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
public WebURL getRequestURL()  { return _request.getURL(); }

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
 * Returns the response type.
 */
public DataType getDataType()
{
    if(_dataType!=null) return _dataType;
    return _dataType = DataType.getPathDataType(getRequestURL().getPath());
}

/**
 * Sets the data type.
 */
protected void setDataType(DataType aType)  { _dataType = aType; }

/**
 * Returns the file header.
 */
public FileHeader getFileHeader()  { return _fileHdr; }

/**
 * Sets the file header.
 */
public void setFileHeader(FileHeader aFileHdr)  { _fileHdr = aFileHdr; }

/**
 * Returns the file.
 */
public WebFile getFile()
{
    if(_file!=null) return _file;
    if(_fileHdr!=null) _file = getRequestURL().getSite().createFile(_fileHdr);
    else _file = getRequestURL().getFile();
    return _file;
} WebFile _file;

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
 * Returns whether response is text (regardless of what the data type is).
 */
public boolean isText()
{
    byte bytes[] = getBytes(), junk = 0; if(bytes==null) return false;
    for(byte b : bytes) if((b & 0xFF) > 127) { junk++; if(junk>10) return false; }
    return true;
}

/**
 * Returns the text of the response.
 */
public String getText()
{
    if(_text!=null) return _text;
    if(_bytes!=null) _text = new String(_bytes);
    return _text;
}

/**
 * Returns the exception.
 */
public Throwable getException()  { return _exception; }

/**
 * Sets the exception.
 */
public void setException(Throwable aThrowable)  { _exception = aThrowable; }

/**
 * Standard toString implementation.
 */
public String toString() { return "Response " + getCode() + ' ' + getCodeString() + ' ' + getRequestURL().getString(); }

/**
 * Returns the code message.
 */
public static String getCodeString(int aCode)
{
    switch(aCode) {
        case OK: return "OK";
        case UNAUTHORIZED: return "Unauthorized";
        case NOT_FOUND: return "Not Found";
        case METHOD_NOT_ALLOWED: return "Method Not Allowed";
        default: return "Unknown";
    }
}

}