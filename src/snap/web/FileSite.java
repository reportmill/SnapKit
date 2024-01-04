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

    // A cache for LastModTimes for local files in CheerpJ
    private static Prefs _lastModTimesPrefsNode;

    /**
     * Constructor.
     */
    public FileSite()
    {
        super();

        // If CheerpJ, get LastModTimes prefs node
        if (SnapUtils.isWebVM)
            _lastModTimesPrefsNode = Prefs.getPrefsForName("LastModTimes");
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
            FileHeader[] fileHeaders = getFileHeadersForJavaFile(javaFile);
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

        // If WebVM, get LastModTime from cache
        if (SnapUtils.isWebVM) {
            long lastModTime = getLastModTimeCached(javaFile);
            fileHeader.setLastModTime(lastModTime);
        }

        // Return
        return fileHeader;
    }

    /**
     * Returns the child file headers at given path.
     */
    protected FileHeader[] getFileHeadersForJavaFile(File parentFile)
    {
        // Get java file children (if null, just return)
        File[] dirFiles = parentFile.listFiles();
        if (dirFiles == null) {
            System.err.println("FileSite.getFileHeaders: error from list files for file: " + parentFile.getPath());
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
            FileHeader fileHeader = getFileHeaderForJavaFile(childFile);
            if (fileHeader != null) // Happens with links
                fileHeaders.add(fileHeader);
        }

        // Return
        return fileHeaders.toArray(new FileHeader[0]);
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

        // Make sure parent directories exist
        File fileDir = javaFile.getParentFile();
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                aResp.setException(new RuntimeException("FileSite.doPut: Error creating parent dir: " + fileDir.getPath()));
                return;
            }
        }

        // If directory, create
        if (aReq.isFileDir() && !javaFile.exists()) {
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

        // Get last modified time from java file and set in response
        long lastModTime = javaFile.lastModified();
        if (SnapUtils.isWebVM) {
            lastModTime = System.currentTimeMillis();
            setLastModTimeCached(javaFile, lastModTime);
        }
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

        // Remove cached LastModTime
        if (SnapUtils.isWebVM)
            setLastModTimeCached(javaFile, 0);
    }

    /**
     * Saves the modified time for a file to underlying file system.
     */
    @Override
    protected void saveLastModTimeForFile(WebFile aFile, long aTime)
    {
        // Get java file and set last modified time
        File javaFile = aFile.getJavaFile();
        if (!javaFile.setLastModified(aTime)) {
            if (!SnapUtils.isWebVM)
                System.err.println("FileSite.setModTimeForFile: Error setting mod time for file: " + javaFile.getPath());
        }

        // Hack support to save last mod times
        if (SnapUtils.isWebVM)
            setLastModTimeCached(javaFile, aTime);
    }

    /**
     * Returns the Java file for a WebURL.
     */
    @Override
    protected File getJavaFileForUrl(WebURL aURL)
    {
        String filePath = aURL.getPath();
        return getJavaFileForLocalPath(filePath);
    }

    /**
     * Returns the Java file for given local file path.
     */
    protected File getJavaFileForLocalPath(String filePath)
    {
        String javaFilePath = getJavaFilePathForPath(filePath);
        return new File(javaFilePath);
    }

    /**
     * Returns the local file path for given java file.
     */
    protected String getLocalPathForJavaFile(File javaFile)
    {
        String filePath = getPathForJavaFile(javaFile);
        String sitePath = getPath();
        if (sitePath == null || sitePath.length() == 0 || sitePath.equals("/"))
            return filePath;

        // Trim prefix
        int sitePathLength = sitePath.length();
        if (sitePath.endsWith("/")) sitePathLength--;
        return filePath.substring(sitePathLength);
    }

    /**
     * Returns the java file path for given site file path.
     */
    protected String getJavaFilePathForPath(String filePath)
    {
        String sitePath = getPath();
        if (sitePath == null)
            return filePath;
        return sitePath + filePath;
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

        // Get standardized path
        String filePathStd = PathUtils.getNormalized(filePath);

        // Return
        return filePathStd;
    }

    /**
     * Hack for get/set last mod time in cheerpJ.
     */
    private static long getLastModTimeCached(File javaFile)
    {
        String filePath = javaFile.getPath();
        long lastModTime = _lastModTimesPrefsNode.getLong(filePath, 0);
        //System.out.println("GetLastModTime: " + filePath + " = " + lastModTime);
        return lastModTime;
    }

    /**
     * Hack for get/set last mod time in cheerpJ.
     */
    private static void setLastModTimeCached(File javaFile, long aValue)
    {
        String filePath = javaFile.getPath();
        if (aValue == 0)
            _lastModTimesPrefsNode.remove(filePath);
        else _lastModTimesPrefsNode.setValue(filePath, aValue);
        //System.out.println("SetLastModTime: " + filePath + " = " + aValue);
    }
}