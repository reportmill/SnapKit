/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import snap.util.FilePathUtils;
import snap.util.SnapUtils;

/**
 * A WebSite subclass for Zip files.
 */
public class ZipFileSite extends WebSite {

    // The ZipFile
    ZipFile                    _zipFile;
    
    // A map of paths to ZipEntry
    Map <String,ZipEntry>      _entries;
    
    // A map of directory paths to List of child paths
    Map <String,List<String>>  _dirs;
    
/**
 * Returns the ZipFile.
 */
protected ZipFile getZipFile() throws Exception { return _zipFile!=null? _zipFile : (_zipFile=createZipFile()); }

/**
 * Creates the ZipFile.
 */
protected ZipFile createZipFile() throws Exception
{
    File sfile = getStandardFile(); if(sfile==null) return null; // Get local file
    return new ZipFile(sfile); // Create/return ZipFile
}

/**
 * Returns a Java file for the zip file URL (copied to Sandbox if remote).
 */
protected File getStandardFile() throws Exception
{
    WebURL url = getURL();
    WebFile file = url.getFile(); if(file==null) return null;
    WebFile localFile = file.getSite().getLocalFile(file, true); // Get local file in case file is over http
    return localFile.getStandardFile();
}

/**
 * Returns a map of ZipFile paths to ZipEntry(s).
 */
protected synchronized Map <String,ZipEntry> getEntries()
{
    // If already set, just return
    if(_entries!=null) return _entries;
    
    // Create maps
    _entries = new HashMap(); _dirs = new HashMap();
    
    // Get ZipFile
    ZipFile zipFile; try { zipFile = getZipFile(); } catch(Exception e) { throw new RuntimeException(e); }
    if(zipFile==null) return _entries;
    
    // Get ZipEntries and add
    Enumeration <? extends ZipEntry> zentries = zipFile.entries();
    while(zentries.hasMoreElements())
        addZipEntry(zentries.nextElement());
    
    // Close and return 
    //try { zipFile.close(); _zipFile = null; } catch(Exception e) { throw new RuntimeException(e); }
    return _entries;
}

/**
 * Adds a ZipEntry to WebSite.
 */
protected void addZipEntry(ZipEntry anEntry)
{
    // Get path and add entry to entries and path to dirs lists
    String path = FilePathUtils.getStandardized('/' + anEntry.getName());
    _entries.put(path, anEntry);
    addDirListPath(path);
}

/**
 * Returns a dir list for a path.
 */
protected List <String> getDirList(String aPath)
{
    // Get parent path and return list for path
    String ppath =  FilePathUtils.getParent(aPath);
    List <String> dlist = _dirs.get(ppath); if(dlist!=null) return dlist;
    
    // If list not found, create, set and return
    _dirs.put(ppath, dlist=new ArrayList());
    addDirListPath(ppath);
    return dlist;
}

/**
 * Returns a dir list for a path.
 */
protected void addDirListPath(String aPath)
{
    if(aPath.length()<=1) return;
    String path = FilePathUtils.getStandardized(aPath);

    List <String> dlist = getDirList(path);
    if(!dlist.contains(path))
        dlist.add(path);
}

/**
 * Returns a data source file for given path (if file exists).
 */
protected FileHeader getFileHeader(String aPath) throws Exception
{
    ZipEntry zentry = getEntries().get(aPath); if(zentry==null && _dirs.get(aPath)==null) return null;
    FileHeader file = new FileHeader(aPath, zentry==null || zentry.isDirectory());
    if(zentry!=null) file.setLastModTime(zentry.getTime());
    if(zentry!=null) file.setSize(zentry.getSize());
    return file;
}

/**
 * Returns file content (bytes for file, FileHeaders for dir).
 */
protected Object getFileContent(String aPath) throws Exception
{
    ZipEntry zentry = getEntries().get(aPath); if(zentry==null && _dirs.get(aPath)==null) return null;
    boolean isDir = zentry==null || zentry.isDirectory();
    if(isDir)
        return getFileHeaders(aPath);
    return getFileBytes(aPath);
}

/**
 * Returns file bytes.
 */
protected byte[] getFileBytes(String aPath) throws Exception
{
    ZipEntry zentry = _entries.get(aPath); if(zentry==null) return null;
    InputStream istream = _zipFile.getInputStream(zentry);
    return SnapUtils.getBytes2(istream);
}

/**
 * Returns a list of file headers for directory path.
 */
protected List <FileHeader> getFileHeaders(String aPath) throws Exception
{
    List <String> dlist = _dirs.get(aPath); if(dlist==null) return null;
    List <FileHeader> flist = new ArrayList(dlist.size());
    for(String path : dlist) {
        FileHeader file = getFileHeader(path); if(file==null) continue;
        flist.add(file);
    }
    return flist;
}

}