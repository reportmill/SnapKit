/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import snap.util.FilePathUtils;

/**
 * A data source implementation that draws from a directory WebFile.
 */
public class DirSite extends WebSite {

    // The directory WebFile
    WebFile          _dir;

/**
 * Returns the directory.
 */
public WebFile getDir()  { return getURL().getFile(); }

/**
 * Handles a get or head request.
 */
protected WebResponse doGetOrHead(WebRequest aReq, boolean isHead)
{
    // Get URL and path and create empty response
    WebURL url = aReq.getURL();
    String path = url.getPath(); if(path==null) path = "/";
    WebResponse resp = new WebResponse(aReq);
    
    // Get WebFile from Dir site
    WebFile dfile = getDirFile(path);
    
    // If not found, set Response.Code to NOT_FOUND and return
    if(dfile==null) {
        resp.setCode(WebResponse.NOT_FOUND); return resp; }
    
    // If found, set response code to ok
    resp.setCode(WebResponse.OK);
    resp.setDir(dfile.isDir());
    resp.setLastModTime(dfile.getLastModTime());
    resp.setSize(dfile.getSize());
        
    // If Head, just return
    if(isHead)
        return resp;
        
    // If file, get/set file bytes
    if(dfile.isFile()) {
        byte bytes[] = dfile.getBytes();
        resp.setBytes(bytes);
    }
        
    // Otherwise, get/set dir FileHeaders
    else {
        List <WebFile> dfiles = dfile.getFiles();
        List <FileHeader> fhdrs = new ArrayList(dfiles.size());
        for(WebFile df : dfiles) {
            String hpath = FilePathUtils.getChild(path, df.getName());
            FileHeader fhdr = new FileHeader(hpath, df.isDir());
            fhdr.setLastModTime(df.getLastModTime()); fhdr.setSize(df.getSize());
            fhdrs.add(fhdr);
        }
        resp.setFileHeaders(fhdrs);
    }
    
    // Set FileHeaderReturn response
    return resp;
}

/**
 * Save file.
 */
protected long saveFileImpl(WebFile aFile) throws Exception
{
    WebFile dfile = createDirFile(aFile.getPath(), aFile.isDir());
    if(aFile.isFile()) dfile.setBytes(aFile.getBytes());
    dfile.save();
    return dfile.getLastModTime();
}

/**
 * Delete file.
 */
protected void deleteFileImpl(WebFile aFile) throws Exception
{
    WebFile dfile = getDirFile(aFile.getPath());
    if(dfile!=null) dfile.delete();
}

/**
 * Override to get standard file from dir file getStandardFile.
 */
protected File getStandardFile(WebFile aFile)
{
    WebFile dfile = getDirFile(aFile.getPath());
    return dfile!=null? dfile.getStandardFile() : null;
}

/**
 * Returns the directory file for a path.
 */
protected WebFile getDirFile(String aPath)
{
    WebFile dir = getDir(); if(dir==null || !dir.isDir()) return null;
    WebSite ds = dir.getSite();
    String path = dir.getPath() + aPath;
    return ds.getFile(path);
}

/**
 * Returns the directory file for a path.
 */
protected WebFile createDirFile(String aPath, boolean isDir)
{
    WebFile dir = getDir(); if(dir==null || !dir.isDir()) return null;
    WebSite ds = dir.getSite();
    String path = dir.getPath() + aPath;
    return ds.createFile(path, isDir);
}

}