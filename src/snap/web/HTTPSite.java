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
 * Handles a head request.
 */
protected WebResponse doHead(WebRequest aReq)  { return doHTTP(aReq, true); }

/**
 * Handle a get request.
 */
protected WebResponse doGet(WebRequest aReq)  { return doHTTP(aReq, false); }

/**
 * Handle a get request.
 */
protected WebResponse doHTTP(WebRequest aReq, boolean isHead)
{
    // Create empty WebResponse return value
    WebResponse resp = new WebResponse(); resp.setRequest(aReq);
 
    // Create HTTPRequest for java.net.URL
    WebURL url = aReq.getURL();
    HTTPRequest hreq = new HTTPRequest(url.getSourceURL()); if(isHead) hreq.setRequestMethod("HEAD");
    
    // Get HTTPResponse response (if IOException, set code/exception and return)
    HTTPResponse hresp = null; try { hresp = hreq.getResponse(); }
    catch(IOException e) {
        resp.setCode(WebResponse.EXCEPTION_THROWN); resp.setException(e); return resp; }
    
    // Handle NOT_FOUND
    if(hresp.getCode()==HTTPResponse.NOT_FOUND) {
        resp.setCode(WebResponse.NOT_FOUND); return resp; }
        
    // Handle UNAUTHORIZED
    if(hresp.getCode()==HTTPResponse.UNAUTHORIZED) {
        resp.setCode(WebResponse.UNAUTHORIZED); return resp; }
        
    // Handle anything else not okay
    if(hresp.getCode()!=HTTPResponse.OK) {
        resp.setCode(hresp.getCode()); return resp; }
        
    // Configure response info (just return if isHead)
    resp.setCode(WebResponse.OK);
    resp.setLastModTime(hresp.getLastModified());
    resp.setSize(hresp.getContentLength());
    if(isHead)
        return resp;
    
    // Set Bytes
    resp.setBytes(hresp.getBytes());
    
    // If directory, configure directory info and return
    if(isDir(url, hresp)) {
        resp.setDir(true);
        String path = url.getPath(); if(path==null) path = "/";
        List <FileHeader> fhdrs = getFileHeaders(path, hresp.getBytes());
        resp.setFileHeaders(fhdrs);
    }
    
    // Return response
    return resp;
}

/**
 * Returns whether given URL and Response indicates directory.
 */
private boolean isDir(WebURL aURL, HTTPResponse aResp)
{
    String ctype = aResp.getContentType().toLowerCase();
    String path = aURL.getPath(); if(path==null) path = "/";
    boolean isdir = FilePathUtils.getExtension(path).length()==0 && ctype.startsWith("text") &&
        aURL.getQuery()==null;
    return isdir;
}

/**
 * Returns files at path.
 */
private List <FileHeader> getFileHeaders(String aPath, byte bytes[])
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
private List <FileHeader> getFilesFromHTMLBytes(String aPath, byte bytes[])
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
        file.setLastModTime(System.currentTimeMillis());
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

/**
 * WebSite method.
 */
protected long saveFileImpl(WebFile aFile) throws Exception
{
    String urls = getURLString() + aFile.getPath();
    HTTPRequest req = new HTTPRequest(urls);
    req.setBytes(aFile.getBytes());
    req.getResponse();
    return aFile.getLastModTime();
}

/**
 * Override to return standard file for cache file.
 */
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
    if(cfile.getExists() && cfile.getLastModTime()>=aFile.getLastModTime())
        return cfile;
    cfile.setBytes(aFile.getBytes());
    cfile.save();
    return cfile;
}

/**
 * Returns a cache file for path.
 */
private WebFile getCacheFile(String aPath)
{
    WebSite sbox = getSandbox();
    WebFile dfile = sbox.getFile("/Cache" + aPath);
    if(dfile==null) dfile = sbox.createFile("/Cache" + aPath, false);
    return dfile;
}

}