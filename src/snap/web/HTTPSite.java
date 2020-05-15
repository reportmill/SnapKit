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
     * Handle a get or head request.
     */
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Create HTTPRequest for java.net.URL
        WebURL url = aReq.getURL();
        HTTPRequest hreq = new HTTPRequest(url.getJavaURL()); if (isHead) hreq.setMethod("HEAD");

        // Get HTTPResponse response (if IOException, set code/exception and return)
        HTTPResponse hresp;
        try { hresp = hreq.getResponse(); }
        catch(IOException e) {
            aResp.setException(e); return; }

        // Get/set return code
        int code = hresp.getCode();
        aResp.setCode(code);

        // If not okay, just return
        if (code!=HTTPResponse.OK)
            return;

        // Configure response info (just return if isHead)
        aResp.setModTime(hresp.getLastModified());
        aResp.setSize(hresp.getContentLength());
        boolean isdir = isDir(url, hresp); aResp.setDir(isdir);
        if(isHead)
            return;

        // Set Bytes
        aResp.setBytes(hresp.getBytes());

        // If directory, configure directory info and return
        if (isdir) {
            String path = url.getPath(); if(path==null) path = "/";
            List <FileHeader> fhdrs = getFileHeaders(path, hresp.getBytes());
            aResp.setFileHeaders(fhdrs);
        }
    }

    /**
     * Returns whether given URL and Response indicates directory.
     */
    private boolean isDir(WebURL aURL, HTTPResponse aResp)
    {
        String ctype = aResp.getContentType(); if(ctype==null) return false; ctype = ctype.toLowerCase();
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
        if (files!=null)
            return files;

        // Gets files from html
        files = new ArrayList();

        // If ".index" file exists, load children
        WebFile indexFile = getFile(FilePathUtils.getChild(aPath, ".index"));
        if(indexFile!=null) {
            String indexFileString = StringUtils.getISOLatinString(indexFile.getBytes());
            String fileEntries[] = indexFileString.split("\n");
            for (String fileEntry : fileEntries) {
                if (fileEntry.length()==0) continue;
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
        int htag = text.indexOf("<HTML>"); if (htag<0) return null;
        List <FileHeader> files = new ArrayList();

        for (int i=text.indexOf("HREF=\"", htag); i>0; i=text.indexOf("HREF=\"", i+8)) {
            int end = text.indexOf("\"", i+6); if(end<0) continue;
            String name = text.substring(i+6,end);
            if (name.length()<2 || !Character.isLetterOrDigit(name.charAt(0))) continue;
            boolean isDir = false; if (name.endsWith("/")) { isDir = true; name = name.substring(0, name.length()-1); }
            String path = FilePathUtils.getChild(aPath, name);
            FileHeader file = new FileHeader(path, isDir);
            file.setModTime(System.currentTimeMillis());
            files.add(file);
        }
        return files;
    }

    /**
     * Handle a POST request.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)  { doPut(aReq, aResp); }

    /**
     * Handle a POST request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        // Create/configure HTTP request
        String urls = aReq.getURL().getString();
        HTTPRequest hreq = new HTTPRequest(urls);
        hreq.setBytes(aReq.getSendBytes());

        // Get HTTPResponse
        HTTPResponse hresp;
        try { hresp = hreq.getResponse(); }
        catch(Exception e) {
            aResp.setException(e); return; }

        // Configure response and return
        aResp.setCode(hresp.getCode());
        aResp.setBytes(hresp.getBytes());
    }

    /**
     * Override to return standard file for cache file.
     */
    protected File getJavaFile(WebURL aURL)
    {
        WebFile file = aURL.getFile();
        WebFile cfile = getLocalFile(file, false);
        return cfile.getJavaFile();
    }

    /**
     * Returns a local file for given file (with option to cache for future use).
     */
    public WebFile getLocalFile(WebFile aFile, boolean doCache)
    {
        WebFile cfile = getCacheFile(aFile.getPath());
        if (cfile.getExists() && cfile.getLastModTime()>=aFile.getLastModTime())
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
        if (dfile==null) dfile = sbox.createFile("/Cache" + aPath, false);
        return dfile;
    }
}