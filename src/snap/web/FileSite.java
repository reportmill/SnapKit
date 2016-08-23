/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.util.*;
import snap.util.*;

/**
 * A data source to read/write data and files to a file system.
 */
public class FileSite extends WebSite {

/**
 * Returns the file header for given path.
 */
protected FileHeader getFileHeader(String aPath)
{
    // Get standard file for path, create and initialize file info and return
    File file = getStandardFile(aPath); if(!file.exists()) return null;
    FileHeader fhdr = new FileHeader(aPath, file.isDirectory());
    fhdr.setLastModifiedTime(file.lastModified());
    fhdr.setSize(file.length());
    return fhdr;
}

/**
 * Returns the child file headers at given path.
 */
protected List <FileHeader> getFileHeaders(String aPath) throws Exception
{
    // Get standard file for path
    File file = getStandardFile(aPath);
    if(!file.exists() || !file.isDirectory())
        return null;
    
    // Get java file children (if null, just return)
    File cfiles[] = file.listFiles(); if(cfiles==null) return null;
    
    // Create files from child java files
    List <FileHeader> files = new ArrayList(cfiles.length);
    for(File cfile : cfiles) { String name = cfile.getName();
        if(name.equalsIgnoreCase(".DS_Store")) continue; // Skip funky apple files
        FileHeader fhdr = getFileHeader(StringUtils.getPathChild(aPath, name));
        if(fhdr!=null) files.add(fhdr); // Happens with links
    }
    
    // Return files
    return files;
}

/**
 * Returns the file bytes at given path.
 */
protected byte[] getFileBytes(String aPath) throws Exception
{
    File file = getStandardFile(aPath);
    return FileUtils.getBytes(file);
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
@Override
protected void setLastModifiedTime(WebFile aFile, long aTime) throws Exception
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