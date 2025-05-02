/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import snap.util.*;

/**
 * This class is a WebSite implementation for local file system.
 */
public class FileSite extends WebSite {

    // The drive letter path prefix for Windows
    private String _windowsDriveLetterPath;

    /**
     * Constructor.
     */
    public FileSite()
    {
        super();
    }

    /**
     * Override to set drive letter path on Windows.
     */
    @Override
    public void setURL(WebURL aURL)
    {
        super.setURL(aURL);

        // If Windows, get drive letter path
        if (SnapEnv.isWindows)
            _windowsDriveLetterPath = aURL.getWindowsDriveLetterPath();
    }

    /**
     * Handle a get or head request.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get request file path
        String filePath = aReq.getFilePath();

        // Get Java file - if file doesn't exist or is not readable, return NOT_FOUND response
        File javaFile = getJavaFileForLocalPath(filePath);
        if (!javaFile.exists() || !javaFile.canRead()) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // If case doesn't match, return not found - case-insensitive file systems could be supported, but it could get tricky
        String filePathReal = getLocalPathForJavaFile(javaFile);
        if (!filePath.equals(filePathReal)) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Configure response info (just return if isHead). Need to pre-create FileHeader to fix capitalization.
        aResp.setCode(WebResponse.OK);
        FileHeader fileHeader = getFileHeaderForJavaFile(javaFile);
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
            List<FileHeader> fileHeaders = getFileHeadersForJavaFile(javaFile);
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Returns the file header for given path and java file.
     */
    protected FileHeader getFileHeaderForJavaFile(File javaFile)
    {
        // If file doesn't exist or is not readable, return null
        if (!javaFile.exists() || !javaFile.canRead())
            return null;

        // Get file path
        String filePath = getLocalPathForJavaFile(javaFile);

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
    protected List<FileHeader> getFileHeadersForJavaFile(File parentFile)
    {
        // Get java file children (if null, just return)
        File[] dirFiles = parentFile.listFiles();
        if (dirFiles == null) {
            System.err.println("FileSite.getFileHeaders: error from list files for file: " + parentFile.getPath());
            return Collections.emptyList();
        }

        // Return file headers for files
        return ArrayUtils.mapNonNullToList(dirFiles, file -> getFileHeaderForJavaFile(file));
    }

    /**
     * Handle a PUT request.
     */
    @Override
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        // Get java file
        String filePath = aReq.getFilePath();
        File javaFile = getJavaFileForLocalPath(filePath);
        boolean fileExists = javaFile.exists();

        // If directory and missing, create directory
        if (aReq.isFileDir() && !fileExists) {
            if (!javaFile.mkdir()) {
                aResp.setException(new RuntimeException("FileSite.doPut: Error creating dir: " + javaFile.getPath()));
                return;
            }
        }

        // Otherwise, write bytes
        else {
            byte[] fileBytes = aReq.getSendBytes();
            if (fileBytes != null) {
                try { FileUtils.writeBytes(javaFile, fileBytes); }
                catch(IOException e) { aResp.setException(e); return; }
            }
        }

        // Get last modified time from java file and set in response
        long lastModTime = javaFile.lastModified();

        // Set LastModTime in response
        aResp.setLastModTime(lastModTime);
    }

    /**
     * Handle a DELETE request.
     */
    @Override
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Get java file
        String filePath = aReq.getFilePath();
        File javaFile = getJavaFileForLocalPath(filePath);

        // Do delete
        FileUtils.deleteDeep(javaFile);
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    @Override
    protected void saveLastModTimeForFile(WebFile aFile, long aTime) throws Exception
    {
        // Set in file
        aFile.setLastModTime(aTime);

        // Get java file and set last modified time
        File javaFile = aFile.getJavaFile();
        if (!javaFile.setLastModified(aTime))
            System.err.println("FileSite.setModTimeForFile: Error setting mod time for file: " + javaFile.getPath());

        // Do normal version
        super.saveLastModTimeForFile(aFile, aTime);
    }

    /**
     * Returns the Java file for a WebURL.
     */
    @Override
    protected File getJavaFileForUrl(WebURL aURL)
    {
        String filePath = aURL.getPath();
        if (filePath.isEmpty())
            filePath = "/";
        return getJavaFileForLocalPath(filePath);
    }

    /**
     * Returns the Java file for given local file path.
     */
    protected File getJavaFileForLocalPath(String filePath)
    {
        // If WindowsDriveLetterPath is set, append to path
        if (_windowsDriveLetterPath != null)
            filePath = _windowsDriveLetterPath + filePath;

        // Return file
        return new File(filePath);
    }

    /**
     * Returns the local file path for given java file.
     */
    protected String getLocalPathForJavaFile(File javaFile)
    {
        String filePath = getPathForJavaFile(javaFile);

        // If WindowsDriveLetterPath is set, append to path
        if (_windowsDriveLetterPath != null) {
            if (StringUtils.startsWithIC(filePath, _windowsDriveLetterPath)) {
                filePath = filePath.substring(_windowsDriveLetterPath.length());
                if (filePath.isEmpty())
                    filePath = "/";
            }
        }

        // Return
        return filePath;
    }

    /**
     * Returns the file path for given java file.
     */
    private static String getPathForJavaFile(File javaFile)
    {
        String filePath = javaFile.getPath();

        // Try CanonicalPath (fixes capitalization)
        try {
            String canonicalPath = javaFile.getCanonicalPath();
            if (!canonicalPath.endsWith(filePath) && StringUtils.endsWithIC(canonicalPath, filePath))
                filePath = canonicalPath.substring(canonicalPath.length() - filePath.length());
        }
        catch(Exception e) { System.err.println("FileSite.getPathForJavaFile:" + e); }

        // Return normalized path
        return FilePathUtils.getNormalizedPath(filePath);
    }
}