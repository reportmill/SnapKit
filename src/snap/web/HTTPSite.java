/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import snap.util.*;

/**
 * A WebSite for HTTP sources.
 */
public class HTTPSite extends WebSite {

/**
 * Returns the string identifying the prefix for URLs in this data source.
 */
public String getURLScheme()  { return "http"; }

/**
 * Returns a data source file for given path (if file exists).
 */
protected FileHeader getFileHeader(String aPath) throws IOException
{
    // Fetch URL
    String urls = getURLString() + aPath;
    HTTPRequest req = new HTTPRequest(urls); req.setRequestMethod("HEAD");
    HTTPResponse resp = req.getResponse();
    
    // Handle non-success response codes
    if(resp.getCode()==HTTPResponse.NOT_FOUND)
        return null; // throw new FileNotFoundException(aPath);
    if(resp.getCode()==HTTPResponse.UNAUTHORIZED)
        throw new AccessException();
    if(resp.getCode()!=HTTPResponse.OK)
        throw new IOException(resp.getMessage());
    
    // Create file, set bytes and return
    String contentType = resp.getContentType().toLowerCase();
    boolean isDir = FilePathUtils.getExtension(aPath).length()==0 && contentType.startsWith("text");
    FileHeader file = new FileHeader(aPath, isDir);
    file.setLastModifiedTime(resp.getLastModified());
    file.setSize(resp.getContentLength());
    return file;
}

/**
 * Handle a get request.
 */
protected synchronized WebResponse doGet(WebRequest aRequest)
{
    // Get URL and path and create basic response
    WebURL url = aRequest.getURL();
    String path = url.getPath(); if(path==null) path = "/";
    WebResponse resp = new WebResponse(); resp.setRequest(aRequest);
 
    // Get content   
    Object content = null;
    try {
        // Fetch URL
        String urls = url.getString();
        HTTPRequest hreq = new HTTPRequest(url.getURL());
        HTTPResponse hresp = hreq.getResponse();
        
        // Handle non-success response codes
        if(hresp.getCode()==HTTPResponse.NOT_FOUND)
            return null; // throw new FileNotFoundException(aPath);
        if(hresp.getCode()==HTTPResponse.UNAUTHORIZED)
            throw new AccessException();
        if(hresp.getCode()!=HTTPResponse.OK)
            throw new IOException(hresp.getMessage());
        
        // Create file, set bytes and return
        String contentType = hresp.getContentType().toLowerCase();
        boolean isDir = FilePathUtils.getExtension(path).length()==0 && contentType.startsWith("text") &&
            url.getQuery()==null;
        
        // If directory, return
        if(isDir) {
            List <FileHeader> fhdrs = getFileHeaders(path, hresp.getBytes());
            content = fhdrs;
        }
        
        // Return bytes
        if(content==null)
            content = hresp.getBytes();
    }
    catch(Exception e) { System.err.println("HTTPSite.doGet: " + e); }
    
    // Handle file
    if(content instanceof byte[]) { byte bytes[] = (byte[])content;
        resp.setBytes(bytes); resp.setCode(WebResponse.OK);
        FileHeader fhdr = new FileHeader(path, false); fhdr.setSize(bytes.length);
        resp.setFileHeader(fhdr);
    }
        
    // Handle directory
    else if(content instanceof List) { List <FileHeader> fhdrs = (List)content;
        resp.setFileHeaders(fhdrs); resp.setCode(WebResponse.OK);
        FileHeader fhdr = new FileHeader(path, true);
        resp.setFileHeader(fhdr);
    }
        
    // Handle FILE_NOT_FOUND
    else resp.setCode(WebResponse.NOT_FOUND);
    
    // Return response
    return resp;
}

/**
 * Returns files at path.
 */
public List <FileHeader> getFileHeaders(String aPath, byte bytes[]) throws IOException
{
    // Create files list
    List <FileHeader> files = getFilesFromHTMLBytes(aPath, bytes);
    if(files!=null)
        return files;
    
    // Gets files from html
    files = new ArrayList();
    
    // If ".index" file exists, load children
    WebFile indexFile = getFile(FilePathUtils.getChild(aPath, ".index"));
    if(indexFile!=null) {
        String indexFileString = StringUtils.getISOLatinString(indexFile.getBytes());
        String fileEntries[] = indexFileString.split("\n");
        for(String fileEntry : fileEntries) {
            if(fileEntry.length()==0) continue;
            String fileInfo[] = fileEntry.split("\t");
            FileHeader file = new FileHeader(FilePathUtils.getChild(aPath, fileInfo[0]), false);
            files.add(file);
        }
    }
    
    // Return files
    return files;
}

/**
 * Returns files from HTML.
 */
List <FileHeader> getFilesFromHTMLBytes(String aPath, byte bytes[]) throws IOException
{
    String text = new String(bytes);
    int htag = text.indexOf("<HTML>"); if(htag<0) return null;
    List <FileHeader> files = new ArrayList();
    
    for(int i=text.indexOf("HREF=\"", htag); i>0; i=text.indexOf("HREF=\"", i+8)) {
        int end = text.indexOf("\"", i+6); if(end<0) continue;
        String name = text.substring(i+6,end);
        if(name.length()<2 || !Character.isLetterOrDigit(name.charAt(0))) continue;
        boolean isDir = false; if(name.endsWith("/")) { isDir = true; name = name.substring(0, name.length()-1); }
        String path = FilePathUtils.getChild(aPath, name);
        FileHeader file = new FileHeader(path, isDir);
        file.setLastModifiedTime(System.currentTimeMillis());
        files.add(file);
    }
    return files;
}

/**
 * Handle a get request.
 */
protected WebResponse doPost(WebRequest aReq)
{
    // Fetch URL
    String path = aReq.getURL().getPath();
    String urls = aReq.getURL().getString();
    HTTPRequest req = new HTTPRequest(urls); req.setBytes(aReq.getPostBytes());
    
    HTTPResponse resp;
    try { resp = req.getResponse(); }
    catch(Exception e) { throw new RuntimeException(e); }
    
    // Configure response and return
    WebResponse resp2 = new WebResponse(); resp2.setRequest(aReq);
    resp2.setCode(resp.getCode());
    resp2.setBytes(resp.getBytes());
    return resp2;
}

/** WebSite method. */
protected long saveFileImpl(WebFile aFile) throws Exception
{
    String urls = getURLString() + aFile.getPath();
    HTTPRequest req = new HTTPRequest(urls);
    req.setBytes(aFile.getBytes());
    req.getResponse();
    return aFile.getLastModifiedTime();
}

/**
 * Override to return standard file for cache file.
 */
@Override
protected File getStandardFile(WebFile aFile)
{
    WebFile cfile = getLocalFile(aFile, false);
    return cfile.getStandardFile();
}

/**
 * Returns a local file for given file (with option to cache for future use).
 */
public WebFile getLocalFile(WebFile aFile, boolean doCache)
{
    WebFile cfile = getCacheFile(aFile.getPath());
    if(cfile.getExists() && cfile.getLastModifiedTime()>=aFile.getLastModifiedTime())
        return cfile;
    cfile.setBytes(aFile.getBytes());
    cfile.save();
    return cfile;
}

/**
 * Returns a cache file for path.
 */
public WebFile getCacheFile(String aPath)
{
    WebSite sbox = getSandbox();
    WebFile dfile = sbox.getFile("/Cache" + aPath);
    if(dfile==null) dfile = sbox.createFile("/Cache" + aPath, false);
    return dfile;
}

}