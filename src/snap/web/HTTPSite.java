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
     * Constructor.
     */
    public HTTPSite()
    {
        super();
    }

    /**
     * Handle a get or head request.
     */
    @Override
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
        catch(Throwable e) {
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
        aResp.setLastModTime(httpResp.getLastModified());
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
            if (path.isEmpty())
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
        if (filePath.isEmpty())
            filePath = "/";

        String fileExt = FilePathUtils.getExtension(filePath);
        boolean isdir = fileExt.isEmpty() && contentType.startsWith("text") && aURL.getQuery() == null;
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
        String indexFilePath = FilePathUtils.getChildPath(aPath, ".index");
        WebFile indexFile = getFileForPath(indexFilePath);
        if(indexFile != null) {
            String indexFileString = StringUtils.getISOLatinString(indexFile.getBytes());
            String[] fileEntries = indexFileString.split("\n");
            for (String fileEntry : fileEntries) {
                if (fileEntry.isEmpty())
                    continue;
                String[] fileInfo = fileEntry.split("\t");
                String filePath = FilePathUtils.getChildPath(aPath, fileInfo[0]);
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
            String filePath = FilePathUtils.getChildPath(aPath, name);
            FileHeader file = new FileHeader(filePath, isDir);
            file.setLastModTime(System.currentTimeMillis());
            files.add(file);
        }

        // Return array
        return files.toArray(new FileHeader[0]);
    }

    /**
     * Handle a POST request.
     */
    @Override
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
    @Override
    protected File getJavaFileForUrl(WebURL aURL)
    {
        WebFile file = aURL.getFile();
        WebFile localFile = getLocalFileForFile(file);
        return localFile.getJavaFile();
    }

    /**
     * Returns a local file for given file.
     */
    @Override
    public WebFile getLocalFileForFile(WebFile aFile)
    {
        WebSite sandboxSite = getSandboxSite();
        String localFilePath = "/Cache" + aFile.getPath();
        if (SnapEnv.isWebVM) // Shorten name to avoid prefs 'key too long' error
            localFilePath = "/Cache/" + aFile.getName();

        // Get local sandbox file (just return if exists and is up to date)
        WebFile localFile = sandboxSite.createFileForPath(localFilePath, false);
        if (localFile.getExists() && localFile.getLastModTime() >= aFile.getLastModTime())
            return localFile;

        // Copy bytes from remote file and save
        byte[] remoteFileBytes = aFile.getBytes();
        localFile.setBytes(remoteFileBytes);
        localFile.save();

        // Return
        return localFile;
    }
}