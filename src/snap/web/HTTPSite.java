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

        // If directory, get directory file headers and set in response
        if (isdir) {
            List<FileHeader> fileHeaders = getFileHeadersForDirResponse(aResp);
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
     * Returns directory file headers for directory file response.
     */
    private List<FileHeader> getFileHeadersForDirResponse(WebResponse aResp)
    {
        // Get directory
        String dirPath = aResp.getURL().getPath();
        if (dirPath.isEmpty())
            dirPath = "/";

        // If response text is HTML file, return file headers
        String respText = aResp.getText();
        if (respText.contains("<HTML>"))
            return getFileHeadersFromDirPathAndIndexHtmlText(dirPath, respText);

        // Get ".index" file (just return if not found)
        String indexFilePath = FilePathUtils.getChildPath(dirPath, ".index");
        WebFile indexFile = getFileForPath(indexFilePath);
        String indexFileText = indexFile != null ? indexFile.getText() : null;
        if (indexFileText == null)
            return Collections.emptyList();

        // Return file headers for index file text
        return getFileHeadersFromDirPathAndIndexText(dirPath, indexFileText);
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
        WebFile sandboxDir = getSandboxDir();
        String localFilePath = "/Cache" + aFile.getPath();
        if (SnapEnv.isWebVM) // Shorten name to avoid prefs 'key too long' error
            localFilePath = "/Cache/" + aFile.getName();

        // Get local sandbox file (just return if exists and is up to date)
        WebFile localFile = sandboxDir.createChildFileForPath(localFilePath, false);
        if (localFile.getExists() && localFile.getLastModTime() >= aFile.getLastModTime())
            return localFile;

        // Copy bytes from remote file and save
        byte[] remoteFileBytes = aFile.getBytes();
        localFile.setBytes(remoteFileBytes);
        localFile.save();

        // Return
        return localFile;
    }

    /**
     * Returns file headers for directory path and HTML text.
     */
    private static List<FileHeader> getFileHeadersFromDirPathAndIndexHtmlText(String dirPath, String indexText)
    {
        int htmlTagIndex = indexText.indexOf("<HTML>");
        List<FileHeader> fileHeaders = new ArrayList<>();

        for (int i = indexText.indexOf("HREF=\"", htmlTagIndex); i > 0; i = indexText.indexOf("HREF=\"", i + 8)) {
            int end = indexText.indexOf("\"", i+6);
            if (end < 0)
                continue;
            String name = indexText.substring(i+6,end);
            if (name.length() < 2 || !Character.isLetterOrDigit(name.charAt(0)))
                continue;
            boolean isDir = false;
            if (name.endsWith("/")) {
                isDir = true;
                name = name.substring(0, name.length()-1);
            }
            String filePath = FilePathUtils.getChildPath(dirPath, name);
            FileHeader file = new FileHeader(filePath, isDir);
            file.setLastModTime(System.currentTimeMillis());
            fileHeaders.add(file);
        }

        // Return
        return fileHeaders;
    }

    /**
     * Returns file headers for directory path and text.
     */
    private static List<FileHeader> getFileHeadersFromDirPathAndIndexText(String dirPath, String indexText)
    {
        String[] fileEntries = indexText.split("\n");
        return ArrayUtils.mapNonNullToList(fileEntries, fileEntry -> createFileHeaderForDirPathAndIndexEntry(dirPath, fileEntry));
    }

    /**
     * Converts a directory path + filename to a file header.
     */
    private static FileHeader createFileHeaderForDirPathAndIndexEntry(String dirPath, String indexEntry)
    {
        if (indexEntry.isEmpty())
            return null;

        String[] fileInfo = indexEntry.split("\t");
        String filePath = FilePathUtils.getChildPath(dirPath, fileInfo[0]);
        return new FileHeader(filePath, false);
    }
}