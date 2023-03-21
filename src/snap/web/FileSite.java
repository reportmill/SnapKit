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
        WebURL fileURL = aReq.getURL();
        String filePath = fileURL.getPath();
        if (filePath == null)
            filePath = "/";
        File file = getJavaFile(filePath);

        // Handle NOT_FOUND
        if (!file.exists() || !file.canRead()) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Handle UNAUTHORIZED
        //if(!file.canRead()) { resp.setCode(WebResponse.UNAUTHORIZED); return resp; }

        // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
        aResp.setCode(WebResponse.OK);
        FileHeader fileHeader = getFileHeader(filePath, file);
        aResp.setFileHeader(fileHeader);
        if (isHead)
            return;

        // If file, just set bytes
        if (aResp.isFile()) {
            try {
                byte[] bytes = FileUtils.getBytesOrThrow(file);
                aResp.setBytes(bytes);
            }
            catch(IOException e) { aResp.setException(e); }
        }

        // If directory, configure directory info and return
        else {
            List<FileHeader> fileHeaders = getFileHeaders(filePath, file);
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Returns the file header for given path.
     */
    protected FileHeader getFileHeader(String aPath, File aFile)
    {
        // Get standard file for path
        File file = aFile != null ? aFile : getJavaFile(aPath);

        // Get real path (fixes capitalization)
        String path = aPath;
        try {
            String canonicalPath = file.getCanonicalPath();
            if (canonicalPath != null && !canonicalPath.endsWith(path) && StringUtils.endsWithIC(canonicalPath, path))
                path = canonicalPath.substring(canonicalPath.length() - path.length());
        }
        catch(Exception e) { System.err.println("FileSite.getFileHeader:" + e); }

        // Create and initialize FileHeader and return
        FileHeader fileHeader = new FileHeader(path, file.isDirectory());
        fileHeader.setModTime(file.lastModified());
        fileHeader.setSize(file.length());
        return fileHeader;
    }

    /**
     * Returns the child file headers at given path.
     */
    protected List <FileHeader> getFileHeaders(String aPath, File aFile)
    {
        // Get java file children (if null, just return)
        File[] dirFiles = aFile.listFiles();
        if (dirFiles == null)
            return null;

        // Create return list
        List<FileHeader> fileHeaders = new ArrayList<>(dirFiles.length);

        // Create files from child java files
        for (File dirFile : dirFiles) {

            // Skip funky apple files
            String fileName = dirFile.getName();
            if (fileName.equalsIgnoreCase(".DS_Store"))
                continue;

            FileHeader fileHeader = getFileHeader(FilePathUtils.getChild(aPath, fileName), null);
            if (fileHeader != null) // Happens with links
                fileHeaders.add(fileHeader);
        }

        // Return
        return fileHeaders;
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
        WebURL pathURL = aReq.getURL();
        String path = pathURL.getPath();
        WebFile wfile = aReq.getFile();
        File file = getJavaFile(path);

        // Make sure parent directories exist
        File fileDir = file.getParentFile();
        fileDir.mkdirs();

        // If directory, create
        if (wfile != null && wfile.isDir())
            file.mkdir();

        // Otherwise, write bytes
        else {
            byte[] fileBytes = aReq.getSendBytes();
            if (fileBytes != null) {
                try { FileUtils.writeBytesSafely(file, fileBytes); }
                catch(IOException e) { aResp.setException(e); return; }
            }
        }

        // Return standard file modified time
        aResp.setModTime(file.lastModified());
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
    protected void setModTimeForFile(WebFile aFile, long aTime)
    {
        File file = aFile.getJavaFile();
        file.setLastModified(aTime);
    }

    /**
     * Returns the Java file for a WebURL.
     */
    protected File getJavaFile(WebURL aURL)
    {
        String path = aURL.getPath();
        return getJavaFile(path);
    }

    /**
     * Returns the Java file for RMFile.
     */
    protected File getJavaFile(String aPath)
    {
        String sitePath = getPath();
        String path = sitePath != null ? sitePath + aPath : aPath;
        return new File(path);
    }
}