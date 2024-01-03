/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.io.IOException;
import java.util.*;
import snap.util.*;

/**
 * This class is a WebSite implementation for local file system.
 */
public class FileSite extends WebSite {

    /**
     * Constructor.
     */
    public FileSite()
    {
        super();
    }

    /**
     * Handle a get or head request.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get URL, path and file
        WebURL fileURL = aReq.getURL();
        String filePath = fileURL.getPath();
        if (filePath == null)
            filePath = "/";

        // Get Java file - if file doesn't exist or is not readable, return NOT_FOUND response
        File javaFile = getJavaFileForPath(filePath);
        if (!javaFile.exists() || !javaFile.canRead()) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Handle UNAUTHORIZED
        //if(!file.canRead()) { resp.setCode(WebResponse.UNAUTHORIZED); return resp; }

        // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
        aResp.setCode(WebResponse.OK);
        FileHeader fileHeader = getFileHeader(filePath, javaFile);
        aResp.setFileHeader(fileHeader);
        if (isHead)
            return;

        // If file, just set bytes
        if (aResp.isFile()) {
            try {
                byte[] bytes = FileUtils.getBytesOrThrow(javaFile);
                aResp.setBytes(bytes);
            }
            catch(IOException e) { aResp.setException(e); }
        }

        // If directory, configure directory info and return
        else {
            FileHeader[] fileHeaders = getFileHeaders(filePath, javaFile);
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Returns the file header for given path and java file.
     */
    protected FileHeader getFileHeader(String aFilePath, File aJavaFile)
    {
        // Get standard file for path
        File javaFile = aJavaFile != null ? aJavaFile : getJavaFileForPath(aFilePath);

        // Get real path (fixes capitalization)
        String filePath = aFilePath;
        try {
            String canonicalPath = javaFile.getCanonicalPath();
            if (!canonicalPath.endsWith(filePath) && StringUtils.endsWithIC(canonicalPath, filePath))
                filePath = canonicalPath.substring(canonicalPath.length() - filePath.length());
        }
        catch(Exception e) { System.err.println("FileSite.getFileHeader:" + e); }

        // Create and initialize FileHeader and return
        FileHeader fileHeader = new FileHeader(filePath, javaFile.isDirectory());
        fileHeader.setLastModTime(javaFile.lastModified());
        fileHeader.setSize(javaFile.length());

        // Return
        return fileHeader;
    }

    /**
     * Returns the child file headers at given path.
     */
    protected FileHeader[] getFileHeaders(String dirFilePath, File aJavaDirFile)
    {
        // Get java file children (if null, just return)
        File[] dirFiles = aJavaDirFile.listFiles();
        if (dirFiles == null) {
            System.err.println("FileSite.getFileHeaders: error from list files for file: " + aJavaDirFile.getPath());
            return new FileHeader[0];
        }

        // Create return list
        List<FileHeader> fileHeaders = new ArrayList<>(dirFiles.length);

        // Create file headers from child java files
        for (File childFile : dirFiles) {

            // Skip funky apple files
            String fileName = childFile.getName();
            if (fileName.equalsIgnoreCase(".DS_Store"))
                continue;

            // Get child file header and add to list
            String childFilePath = FilePathUtils.getChild(dirFilePath, fileName);
            FileHeader fileHeader = getFileHeader(childFilePath, null);
            if (fileHeader != null) // Happens with links
                fileHeaders.add(fileHeader);
        }

        // Return
        return fileHeaders.toArray(new FileHeader[0]);
    }

    /**
     * Handle a POST request.
     */
    @Override
    protected void doPost(WebRequest aReq, WebResponse aResp)  { doPut(aReq, aResp); }

    /**
     * Handle a PUT request.
     */
    @Override
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        // Get standard file
        WebURL pathURL = aReq.getURL();
        String path = pathURL.getPath();
        WebFile snapFile = aReq.getFile();
        File javaFile = getJavaFileForPath(path);

        // Make sure parent directories exist
        File fileDir = javaFile.getParentFile();
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                aResp.setException(new RuntimeException("FileSite.doPut: Error creating parent dir: " + fileDir.getPath()));
                return;
            }
        }

        // If directory, create
        if (snapFile != null && snapFile.isDir() && !javaFile.exists()) {
            if (!javaFile.mkdir()) {
                aResp.setException(new RuntimeException("FileSite.doPut: Error creating dir: " + javaFile.getPath()));
                return;
            }
        }

        // Otherwise, write bytes
        else {
            byte[] fileBytes = aReq.getSendBytes();
            if (fileBytes != null) {
                try { FileUtils.writeBytesSafely(javaFile, fileBytes); }
                catch(IOException e) { aResp.setException(e); return; }
            }
        }

        // Return standard file modified time
        long lastModTime = javaFile.lastModified();
        aResp.setLastModTime(lastModTime);
    }

    /**
     * Handle a DELETE request.
     */
    @Override
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Get standard file
        String path = aReq.getURL().getPath();
        File file = getJavaFileForPath(path);

        // Do delete
        FileUtils.deleteDeep(file);
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    @Override
    protected void saveLastModTimeForFile(WebFile aFile, long aTime)
    {
        File file = aFile.getJavaFile();
        if (!file.setLastModified(aTime))
            System.err.println("FileSite.setModTimeForFile: Error setting mod time for file: " + file.getPath());
    }

    /**
     * Returns the Java file for a WebURL.
     */
    @Override
    protected File getJavaFileForUrl(WebURL aURL)
    {
        String filePath = aURL.getPath();
        return getJavaFileForPath(filePath);
    }

    /**
     * Returns the Java file for given path.
     */
    protected File getJavaFileForPath(String filePath)
    {
        String sitePath = getPath();
        String javaFilePath = sitePath != null ? sitePath + filePath : filePath;
        return new File(javaFilePath);
    }
}