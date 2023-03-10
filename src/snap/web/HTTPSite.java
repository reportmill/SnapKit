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
        HTTPRequest httpRequest = new HTTPRequest(url.getJavaURL());
        if (isHead)
            httpRequest.setMethod("HEAD");

        // Get HTTPResponse response (if IOException, set code/exception and return)
        HTTPResponse httpResp;
        try { httpResp = httpRequest.getResponse(); }
        catch(IOException e) {
            aResp.setException(e);
            return;
        }

        // Get/set return code
        int code = httpResp.getCode();
        aResp.setCode(code);

        // If not okay, just return
        if (code != HTTPResponse.OK)
            return;

        // Configure response info (just return if isHead)
        aResp.setModTime(httpResp.getLastModified());
        aResp.setSize(httpResp.getContentLength());
        boolean isdir = isDir(url, httpResp);
        aResp.setDir(isdir);
        if (isHead)
            return;

        // Set Bytes
        aResp.setBytes(httpResp.getBytes());

        // If directory, configure directory info and return
        if (isdir) {
            String path = url.getPath();
            if (path == null)
                path = "/";
            FileHeader[] fileHeaders = getFileHeaders(path, httpResp.getBytes());
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Returns whether given URL and Response indicates directory.
     */
    private boolean isDir(WebURL aURL, HTTPResponse aResp)
    {
        String contentType = aResp.getContentType();
        if (contentType == null)
            return false;
        contentType = contentType.toLowerCase();

        String filePath = aURL.getPath();
        if (filePath == null)
            filePath = "/";

        String fileExt = FilePathUtils.getExtension(filePath);
        boolean isdir = fileExt.length() == 0 && contentType.startsWith("text") && aURL.getQuery() == null;
        return isdir;
    }

    /**
     * Returns files at path.
     */
    private FileHeader[] getFileHeaders(String aPath, byte[] bytes)
    {
        // Create files list
        FileHeader[] fileFromHTML = getFilesFromHTMLBytes(aPath, bytes);
        if (fileFromHTML != null)
            return fileFromHTML;

        // Gets files from html
        List<FileHeader> files = new ArrayList<>();

        // If ".index" file exists, load children
        String indexFilePath = FilePathUtils.getChild(aPath, ".index");
        WebFile indexFile = getFileForPath(indexFilePath);
        if(indexFile != null) {
            String indexFileString = StringUtils.getISOLatinString(indexFile.getBytes());
            String[] fileEntries = indexFileString.split("\n");
            for (String fileEntry : fileEntries) {
                if (fileEntry.length() == 0)
                    continue;
                String[] fileInfo = fileEntry.split("\t");
                String filePath = FilePathUtils.getChild(aPath, fileInfo[0]);
                FileHeader file = new FileHeader(filePath, false);
                files.add(file);
            }
        }

        // Return files
        return files.toArray(new FileHeader[0]);
    }

    /**
     * Returns files from HTML.
     */
    private FileHeader[] getFilesFromHTMLBytes(String aPath, byte[] bytes)
    {
        String text = new String(bytes);
        int htmlTagIndex = text.indexOf("<HTML>");
        if (htmlTagIndex < 0)
            return null;
        List<FileHeader> files = new ArrayList<>();

        for (int i = text.indexOf("HREF=\"", htmlTagIndex); i > 0; i = text.indexOf("HREF=\"", i + 8)) {
            int end = text.indexOf("\"", i+6);
            if (end < 0)
                continue;
            String name = text.substring(i+6,end);
            if (name.length() < 2 || !Character.isLetterOrDigit(name.charAt(0)))
                continue;
            boolean isDir = false;
            if (name.endsWith("/")) {
                isDir = true;
                name = name.substring(0, name.length()-1);
            }
            String filePath = FilePathUtils.getChild(aPath, name);
            FileHeader file = new FileHeader(filePath, isDir);
            file.setModTime(System.currentTimeMillis());
            files.add(file);
        }

        // Return array
        return files.toArray(new FileHeader[0]);
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
        String urlStr = aReq.getURL().getString();
        HTTPRequest httpRequest = new HTTPRequest(urlStr);
        httpRequest.setBytes(aReq.getSendBytes());

        // Get HTTPResponse
        HTTPResponse httpResp;
        try { httpResp = httpRequest.getResponse(); }
        catch(Exception e) {
            aResp.setException(e);
            return;
        }

        // Configure response and return
        aResp.setCode(httpResp.getCode());
        aResp.setBytes(httpResp.getBytes());
    }

    /**
     * Override to return standard file for cache file.
     */
    protected File getJavaFile(WebURL aURL)
    {
        WebFile file = aURL.getFile();
        WebFile localFile = getLocalFile(file, false);
        return localFile.getJavaFile();
    }

    /**
     * Returns a local file for given file (with option to cache for future use).
     */
    public WebFile getLocalFile(WebFile aFile, boolean doCache)
    {
        WebFile cacheFile = getCacheFile(aFile.getPath());
        if (cacheFile.getExists() && cacheFile.getLastModTime() >= aFile.getLastModTime())
            return cacheFile;
        cacheFile.setBytes(aFile.getBytes());
        cacheFile.save();
        return cacheFile;
    }

    /**
     * Returns a cache file for path.
     */
    private WebFile getCacheFile(String aPath)
    {
        WebSite sandboxSite = getSandbox();
        WebFile sandboxCacheFile = sandboxSite.getFileForPath("/Cache" + aPath);
        if (sandboxCacheFile == null)
            sandboxCacheFile = sandboxSite.createFileForPath("/Cache" + aPath, false);
        return sandboxCacheFile;
    }
}