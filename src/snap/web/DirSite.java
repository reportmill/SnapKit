/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.ArrayUtils;
import snap.util.FilePathUtils;

import java.io.File;

/**
 * A data source implementation that draws from a directory WebFile.
 */
public class DirSite extends WebSite {

    // The directory WebFile
    private WebFile  _dir;

    /**
     * Constructor.
     */
    public DirSite()
    {
        super();
    }

    /**
     * Returns the directory file this site represents.
     */
    public WebFile getDir()
    {
        if (_dir != null) return _dir;
        WebURL siteURL = getURL();
        WebFile dir = siteURL.getFile();
        return _dir = dir;
    }

    /**
     * Handles a get or head request.
     */
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get URL and path
        WebURL fileURL = aReq.getURL();
        String filePath = fileURL.getPath();

        // Get WebFile from Dir site
        WebFile dirFile = getDirFileForPath(filePath);

        // If not found, set Response.Code to NOT_FOUND and return
        if (dirFile == null) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // If found, set response code to ok
        aResp.setCode(WebResponse.OK);
        aResp.setDir(dirFile.isDir());
        aResp.setModTime(dirFile.getModTime());
        aResp.setSize(dirFile.getSize());

        // If Head, just return
        if (isHead)
            return;

        // If file, get/set file bytes
        if (dirFile.isFile()) {
            byte[] bytes = dirFile.getBytes();
            aResp.setBytes(bytes);
            return;
        }

        // Otherwise, get/set dir FileHeaders
        WebFile[] dirFiles = dirFile.getFiles();
        FileHeader[] fileHeaders = ArrayUtils.map(dirFiles, file -> createFileHeaderForFile(filePath, file), FileHeader.class);
        aResp.setFileHeaders(fileHeaders);
    }

    /**
     * Handle POST request.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)
    {
        doPut(aReq, aResp);
    }

    /**
     * Handle PUT request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        // Get file we're trying to save
        WebURL fileURL = aReq.getURL();
        String filePath = fileURL.getPath();
        WebFile localFile = getFileForPath(filePath);

        // Get remote file
        WebFile dirFile = createDirFileForPath(localFile.getPath(), localFile.isDir());
        if (dirFile == null)
            return;
        if (localFile.isFile())
            dirFile.setBytes(localFile.getBytes());
        dirFile.save();

        // Update response
        aResp.setModTime(dirFile.getModTime());
    }

    /**
     * Handle DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Get file we're trying to save
        WebURL fileURL = aReq.getURL();
        String filePath = fileURL.getPath();

        // Do Delete
        WebFile dirFile = getDirFileForPath(filePath);
        if (dirFile != null)
            dirFile.delete();

        // Update response
        System.out.println("DirSite.doDelete: Probably need to do something more here");
    }

    /**
     * Override to get Java file from dir file.
     */
    protected File getJavaFile(WebURL aURL)
    {
        String filePath = aURL.getPath();
        WebFile dirFile = getDirFileForPath(filePath);
        return dirFile != null ? dirFile.getJavaFile() : null;
    }

    /**
     * Returns a new FileHeader for given file.
     */
    private FileHeader createFileHeaderForFile(String parentFilePath, WebFile aFile)
    {
        FileHeader fileHeader = new FileHeader(aFile);
        String filePath = FilePathUtils.getChild(parentFilePath, aFile.getName());
        fileHeader.setPath(filePath);
        return fileHeader;
    }

    /**
     * Returns the foreign file for a path from foreign site.
     */
    private WebFile getDirFileForPath(String aPath)
    {
        // Get dir file
        WebFile dir = getDir();
        if (dir == null || !dir.isDir())
            return null;

        // Get file path for dir file and fetch
        WebSite dirSite = dir.getSite();
        String dirFilePath = dir.getPath() + aPath;
        return dirSite.getFileForPath(dirFilePath);
    }

    /**
     * Returns a new foreign file for a path from foreign site.
     */
    private WebFile createDirFileForPath(String aPath, boolean isDir)
    {
        // Get dir file
        WebFile dir = getDir();
        if (dir == null || !dir.isDir())
            return null;

        // Get file path for dir file and create
        WebSite dirSite = dir.getSite();
        String dirFilePath = dir.getPath() + aPath;
        return dirSite.createFileForPath(dirFilePath, isDir);
    }
}