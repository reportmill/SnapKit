/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;

import snap.util.FilePathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A data source implementation that draws from a directory WebFile.
 */
public class DirSite extends WebSite {

    // The directory WebFile
    WebFile _dir;

    /**
     * Returns the directory.
     */
    public WebFile getDir()
    {
        return getURL().getFile();
    }

    /**
     * Handles a get or head request.
     */
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get URL and path
        WebURL url = aReq.getURL();
        String path = url.getPath();
        if (path == null) path = "/";

        // Get WebFile from Dir site
        WebFile dirFile = getDirFile(path);

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
            byte bytes[] = dirFile.getBytes();
            aResp.setBytes(bytes);
        }

        // Otherwise, get/set dir FileHeaders
        else {
            WebFile[] dirFiles = dirFile.getFiles();
            List<FileHeader> fhdrs = new ArrayList(dirFiles.length);
            for (WebFile df : dirFiles) {
                String hpath = FilePathUtils.getChild(path, df.getName());
                FileHeader fhdr = new FileHeader(hpath, df.isDir());
                fhdr.setModTime(df.getModTime());
                fhdr.setSize(df.getSize());
                fhdrs.add(fhdr);
            }
            aResp.setFileHeaders(fhdrs);
        }
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
        String path = aReq.getURL().getPath();
        WebFile file = getFileForPath(path);

        // Get remote file
        WebFile dfile = createDirFile(file.getPath(), file.isDir());
        if (file.isFile()) dfile.setBytes(file.getBytes());
        dfile.save();

        // Update response
        aResp.setModTime(dfile.getModTime());
    }

    /**
     * Handle DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Get file we're trying to save
        String path = aReq.getURL().getPath();
        //WebFile file = getFile(path);

        // Do Delete
        WebFile dfile = getDirFile(path);
        if (dfile != null) dfile.delete();

        // Update response
        System.out.println("DirSite.doDelete: Probably need to do something more here");
    }

    /**
     * Override to get Java file from dir file.
     */
    protected File getJavaFile(WebURL aURL)
    {
        WebFile dfile = getDirFile(aURL.getPath());
        return dfile != null ? dfile.getJavaFile() : null;
    }

    /**
     * Returns the directory file for a path.
     */
    protected WebFile getDirFile(String aPath)
    {
        WebFile dir = getDir();
        if (dir == null || !dir.isDir()) return null;
        WebSite ds = dir.getSite();
        String path = dir.getPath() + aPath;
        return ds.getFileForPath(path);
    }

    /**
     * Returns the directory file for a path.
     */
    protected WebFile createDirFile(String aPath, boolean isDir)
    {
        WebFile dir = getDir();
        if (dir == null || !dir.isDir()) return null;
        WebSite ds = dir.getSite();
        String path = dir.getPath() + aPath;
        return ds.createFileForPath(path, isDir);
    }
}