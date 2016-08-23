/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import snap.util.SnapUtils;

/**
 * A WebSite subclass for Zip files.
 */
public class ZipFileSite extends WebSite {

    // The ZipFile
    ZipFile                    _zipFile;
    
    // A map of paths to ZipEntry
    Map <String,ZipEntry>      _paths;
    
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
protected synchronized Map <String,ZipEntry> getPaths()
{
    if(_paths!=null) return _paths;
    _paths = new HashMap(); _dirs = new HashMap();
    ZipFile zipFile; try { zipFile = getZipFile(); } catch(Exception e) { throw new RuntimeException(e); }
    if(zipFile==null) return _paths;
    Enumeration <? extends ZipEntry> zentries = zipFile.entries();
    while(zentries.hasMoreElements()) addZipEntry(zentries.nextElement());
    //try { zipFile.close(); _zipFile = null; } catch(Exception e) { throw new RuntimeException(e); }
    return _paths;
}

/**
 * Adds a ZipEntry to WebSite.
 */
protected void addZipEntry(ZipEntry anEntry)
{
    // Get path
    String path = "/" + anEntry.getName();
    if(path.endsWith("/") && path.length()>1) path = path.substring(0, path.length()-1);
    
    // Add path to paths and dirs
    _paths.put(path, anEntry);
    if(path.length()>1)
        getDirList(path).add(path);
}

/**
 * Returns a dir list for a path.
 */
protected List <String> getDirList(String aPath)
{
    int index = aPath.lastIndexOf('/');
    String ppath = index>0? aPath.substring(0, index) : "/";
    List <String> dlist = _dirs.get(ppath);
    if(dlist==null) {
        _dirs.put(ppath, dlist=new ArrayList());
        if(ppath.length()>1 && !getDirList(ppath).contains(ppath))
            getDirList(ppath).add(ppath);
    }
    return dlist;
}

/**
 * Returns a data source file for given path (if file exists).
 */
protected FileHeader getFileHeader(String aPath) throws Exception
{
    ZipEntry zentry = getPaths().get(aPath); if(zentry==null && _dirs.get(aPath)==null) return null;
    FileHeader file = new FileHeader(aPath, zentry==null || zentry.isDirectory());
    if(zentry!=null) file.setLastModifiedTime(zentry.getTime());
    if(zentry!=null) file.setSize(zentry.getSize());
    return file;
}

/**
 * Returns file bytes.
 */
protected byte[] getFileBytes(String aPath) throws Exception
{
    ZipEntry zentry = _paths.get(aPath); if(zentry==null) return null;
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