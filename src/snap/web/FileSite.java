/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.io.IOException;
import java.util.*;
import snap.util.*;

/**
 * A data source to read/write data and files to a file system.
 */
public class FileSite extends WebSite {

/**
 * Handle a get or head request.
 */
protected WebResponse doGetOrHead(WebRequest aReq, boolean isHead)
{
    // Create empty WebResponse return value
    WebResponse resp = new WebResponse(aReq);
 
    // Get URL, path and file
    WebURL url = aReq.getURL();
    String path = url.getPath(); if(path==null) path = "/";
    File file = getStandardFile(path);
    
    // Handle NOT_FOUND
    if(!file.exists() || !file.canRead()) {
        resp.setCode(WebResponse.NOT_FOUND); return resp; }
        
    // Handle UNAUTHORIZED
    //if(!file.canRead()) { resp.setCode(WebResponse.UNAUTHORIZED); return resp; }
        
    // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
    resp.setCode(WebResponse.OK);
    FileHeader fhdr = getFileHeader(path, file);
    resp.setFileHeader(fhdr);
    if(isHead)
        return resp;
        
    // If file, just set bytes
    if(resp.isFile()) {
        try { byte bytes[] = FileUtils.getBytesOrThrow(file); resp.setBytes(bytes); }
        catch(IOException e) { resp.setException(e); }
    }
    
    // If directory, configure directory info and return
    else {
        List <FileHeader> fhdrs = getFileHeaders(path, file);
        resp.setFileHeaders(fhdrs);
    }
    
    // Return response
    return resp;
}

/**
 * Returns the file header for given path.
 */
protected FileHeader getFileHeader(String aPath, File aFile)
{
    // Get standard file for path
    File file = aFile!=null? aFile : getStandardFile(aPath);
    
    // Get real path (fixes capitalization)
    String path = aPath, cpath = null; try { cpath = file.getCanonicalPath(); }
    catch(Exception e) { System.err.println("FileSite.getFileHeader:" + e); }
    if(cpath!=null && !cpath.endsWith(path) && StringUtils.endsWithIC(cpath,path))
        path = cpath.substring(cpath.length() - path.length());
    
    // Create and initialize FileHeader and return
    FileHeader fhdr = new FileHeader(path, file.isDirectory());
    fhdr.setLastModTime(file.lastModified());
    fhdr.setSize(file.length());
    return fhdr;
}

/**
 * Returns the child file headers at given path.
 */
protected List <FileHeader> getFileHeaders(String aPath, File aFile)
{
    // Get java file children (if null, just return)
    File cfiles[] = aFile.listFiles(); if(cfiles==null) return null;
    
    // Create files from child java files
    List <FileHeader> files = new ArrayList(cfiles.length);
    for(File cfile : cfiles) { String name = cfile.getName();
        if(name.equalsIgnoreCase(".DS_Store")) continue; // Skip funky apple files
        FileHeader fhdr = getFileHeader(FilePathUtils.getChild(aPath, name), null);
        if(fhdr!=null) files.add(fhdr); // Happens with links
    }
    
    // Return files
    return files;
}

/**
 * Writes file bytes.
 */
protected long saveFileImpl(WebFile aFile) throws Exception
{
    // Get standard file
    File file = getStandardFile(aFile);
    
    // Make sure parent directories exist
    file.getParentFile().mkdirs();
    
    // If directory, create
    if(aFile.isDir())
        file.mkdir();
    
    // Otherwise, write bytes
    else if(aFile.getBytes()!=null)
        FileUtils.writeBytesSafely(file, aFile.getBytes());
    
    // Return standard file modified time
    return file.lastModified();
}

/**
 * Deletes file.
 */
protected void deleteFileImpl(WebFile aFile) throws Exception
{
    File file = getStandardFile(aFile);
    FileUtils.deleteDeep(file);
}

/**
 * Saves the modified time for a file to underlying file system.
 */
protected void setLastModTime(WebFile aFile, long aTime) throws Exception
{
    File file = getStandardFile(aFile);
    file.setLastModified(aTime);
}

/**
 * Returns the Java file for a WebFile.
 */
protected File getStandardFile(WebFile aFile)  { return getStandardFile(aFile.getPath()); }

/**
 * Returns the Java file for RMFile.
 */
protected File getStandardFile(String aPath)
{
    String path = getPath()!=null? getPath() + aPath : aPath;
    return new File(path);
}

}