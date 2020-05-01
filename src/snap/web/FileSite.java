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
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get URL, path and file
        WebURL url = aReq.getURL();
        String path = url.getPath(); if (path==null) path = "/";
        File file = getJavaFile(path);

        // Handle NOT_FOUND
        if (!file.exists() || !file.canRead()) {
            aResp.setCode(WebResponse.NOT_FOUND); return; }

        // Handle UNAUTHORIZED
        //if(!file.canRead()) { resp.setCode(WebResponse.UNAUTHORIZED); return resp; }

        // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
        aResp.setCode(WebResponse.OK);
        FileHeader fhdr = getFileHeader(path, file);
        aResp.setFileHeader(fhdr);
        if (isHead)
            return;

        // If file, just set bytes
        if (aResp.isFile()) {
            try { byte bytes[] = FileUtils.getBytesOrThrow(file); aResp.setBytes(bytes); }
            catch(IOException e) { aResp.setException(e); }
        }

        // If directory, configure directory info and return
        else {
            List <FileHeader> fhdrs = getFileHeaders(path, file);
            aResp.setFileHeaders(fhdrs);
        }
    }

    /**
     * Returns the file header for given path.
     */
    protected FileHeader getFileHeader(String aPath, File aFile)
    {
        // Get standard file for path
        File file = aFile!=null ? aFile : getJavaFile(aPath);

        // Get real path (fixes capitalization)
        String path = aPath, cpath = null;
        try { cpath = file.getCanonicalPath(); }
        catch(Exception e) { System.err.println("FileSite.getFileHeader:" + e); }
        if (cpath!=null && !cpath.endsWith(path) && StringUtils.endsWithIC(cpath,path))
            path = cpath.substring(cpath.length() - path.length());

        // Create and initialize FileHeader and return
        FileHeader fhdr = new FileHeader(path, file.isDirectory());
        fhdr.setModTime(file.lastModified());
        fhdr.setSize(file.length());
        return fhdr;
    }

    /**
     * Returns the child file headers at given path.
     */
    protected List <FileHeader> getFileHeaders(String aPath, File aFile)
    {
        // Get java file children (if null, just return)
        File cfiles[] = aFile.listFiles(); if (cfiles==null) return null;

        // Create files from child java files
        List <FileHeader> files = new ArrayList(cfiles.length);
        for (File cfile : cfiles) { String name = cfile.getName();
            if (name.equalsIgnoreCase(".DS_Store")) continue; // Skip funky apple files
            FileHeader fhdr = getFileHeader(FilePathUtils.getChild(aPath, name), null);
            if (fhdr!=null) files.add(fhdr); // Happens with links
        }

        // Return files
        return files;
    }

    /**
     * Handle a POST request.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)  { doPut(aReq, aResp); }

    /**
     * Handle a PUT request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        // Get standard file
        String path = aReq.getURL().getPath();
        WebFile wfile = aReq.getFile();
        File jfile = getJavaFile(path);

        // Make sure parent directories exist
        jfile.getParentFile().mkdirs();

        // If directory, create
        if (wfile!=null && wfile.isDir())
            jfile.mkdir();

        // Otherwise, write bytes
        else if (aReq.getSendBytes()!=null) {
            try { FileUtils.writeBytesSafely(jfile, aReq.getSendBytes()); }
            catch(IOException e) { aResp.setException(e); return; }
        }

        // Return standard file modified time
        aResp.setModTime(jfile.lastModified());
    }

    /**
     * Handle a DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Get standard file
        String path = aReq.getURL().getPath();
        File file = getJavaFile(path);

        // Do delete
        FileUtils.deleteDeep(file);
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    protected void setModTimeSaved(WebFile aFile, long aTime) throws Exception
    {
        File file = aFile.getJavaFile();
        file.setLastModified(aTime);
    }

    /**
     * Returns the Java file for a WebURL.
     */
    protected File getJavaFile(WebURL aURL)  { return getJavaFile(aURL.getPath()); }

    /**
     * Returns the Java file for RMFile.
     */
    protected File getJavaFile(String aPath)
    {
        String path = getPath()!=null? getPath() + aPath : aPath;
        return new File(path);
    }
}