/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

/**
 * Get file from directory.
 */
protected FileHeader getFileHeader(String aPath) throws Exception
{
    WebFile dfile = getDirFile(aPath); if(dfile==null) return null;
    FileHeader file = new FileHeader(aPath, dfile.isDir());
    file.setLastModTime(dfile.getLastModTime()); file.setSize(dfile.getSize());
    return file;
}

/**
 * Returns file content (bytes for file, FileHeaders for dir).
 */
protected Object getFileContent(String aPath) throws Exception
{
    // Get dir file (just return if null)
    WebFile dfile = getDirFile(aPath); if(dfile==null) return null;
    
    // If plain file, just return bytes
    if(dfile.isFile())
        return dfile.getBytes();
    
    // Get file headers and return
    List <WebFile> dfiles = dfile.getFiles(); List <FileHeader> files = new ArrayList(dfiles.size());
    for(WebFile df : dfiles) {
        String path = aPath; if(!path.endsWith("/")) path += '/';
        FileHeader f = new FileHeader(path + df.getName(), df.isDir());
        f.setLastModTime(df.getLastModTime()); f.setSize(df.getSize());
        files.add(f);
    }
    return files;
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
@Override
protected File getStandardFile(WebFile aFile)
{
    WebFile dfile = getDirFile(aFile.getPath());
    return dfile!=null? dfile.getStandardFile() : null;
}

}