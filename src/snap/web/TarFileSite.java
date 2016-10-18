/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import snap.web.JTar.*;

/**
 * A WebSite subclass for Tar files.
 */
public class TarFileSite extends WebSite {

    // The TarFile
    File                        _tarFile;
    
    // A map of paths to TarEntry
    Map <String,TarEntry>       _paths;
    
    // A map of directory paths to List of child paths
    Map <String,List<String>>   _dirs;
    
/**
 * Returns the TarFile.
 */
protected File getTarFile() throws Exception { return _tarFile!=null? _tarFile : (_tarFile=createTarFile()); }

/**
 * Creates the TarFile.
 */
protected File createTarFile() throws Exception
{
    File sfile = getStandardFile(); if(sfile==null) return null; // Get local file
    return sfile; // Create/return TarFile
}

/**
 * Returns a Java file for the tar file URL (copied to Sandbox if remote).
 */
protected File getStandardFile() throws Exception
{
    WebURL url = getURL();
    WebFile file = url.getFile(); if(file==null) return null;
    WebFile localFile = file.getSite().getLocalFile(file, true); // Get local file in case file is over http
    return localFile.getStandardFile();
}

/**
 * Returns a map of TarFile paths to TarEntry(s).
 */
protected synchronized Map <String,JTar.TarEntry> getPaths()
{
    if(_paths!=null) return _paths;
    _paths = new HashMap(); _dirs = new HashMap();
    File tarFile; try { tarFile = getTarFile(); } catch(Exception e) { throw new RuntimeException(e); }
    if(tarFile==null) return _paths;
    
    //Enumeration <? extends TarEntry> zentries = tarFile.entries();
    //while(zentries.hasMoreElements()) addTarEntry(zentries.nextElement());
    try {
        TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile)));
        TarEntry entry; while((entry=tis.getNextEntry())!=null) addTarEntry(entry);
    }
    catch(Exception e) { }
    return _paths;
}

/**
 * Adds a TarEntry to WebSite.
 */
protected void addTarEntry(TarEntry anEntry)
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
    TarEntry tentry = getPaths().get(aPath); if(tentry==null && _dirs.get(aPath)==null) return null;
    FileHeader file = new FileHeader(aPath, tentry==null || tentry.isDirectory());
    if(tentry!=null) file.setLastModifiedTime(tentry.getModTime().getTime());
    if(tentry!=null) file.setSize(tentry.getSize());
    return file;
}

/**
 * Returns file bytes.
 */
protected byte[] getFileBytes(String aPath) throws Exception
{
    TarEntry entry = _paths.get(aPath); if(entry==null) return null;
    //InputStream istream = _tarFile.getInputStream(entry);
    return null;//SnapUtils.getBytes2(istream);
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