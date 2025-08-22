/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.*;
import snap.util.FilePathUtils;

/**
 * A WebSite subclass for Zip and Jar files.
 */
public class ZipFileSite extends WebSite {

    // The JRE ZipFile
    private ZipFile _javaZipFile;

    // A map of paths to ZipEntry
    private Map<String,ZipEntry> _entries;
    
    // A map of directory paths to List of child paths
    private Map<String,List<String>> _dirs;
    
    /**
     * Constructor.
     */
    public ZipFileSite()
    {
        super();
    }

    /**
     * Returns the file for the zip file URL.
     */
    public WebFile getZipFile()
    {
        WebURL zipFileUrl = getURL();
        return zipFileUrl.getFile();
    }

    /**
     * Returns the local file for the zip file URL (copied to Sandbox if remote).
     */
    private WebFile getLocalZipFile()
    {
        WebFile zipFile = getZipFile();
        if (zipFile == null)
            return null;

        // Get local file in case file is over http
        WebSite zipFileSite = zipFile.getSite();
        return zipFileSite.getLocalFileForFile(zipFile);
    }

    /**
     * Returns the ZipFile.
     */
    private ZipFile getJavaZipFile()
    {
        // If already set, just return
        if (_javaZipFile != null) return _javaZipFile;

        // Get java file
        WebFile localZipFile = getLocalZipFile();
        File localZipFileJavaFile = localZipFile != null ? localZipFile.getJavaFile() : null;
        if (localZipFileJavaFile == null)
            return null;

        // Return file
        try {

            // If Jar, use JarFile
            if (localZipFile.getFileType().equals("jar"))
                return _javaZipFile = new JarFile(localZipFileJavaFile);

            // Otherwise return ZipFile
            return _javaZipFile = new ZipFile(localZipFileJavaFile);
        }

        // Rethrow exception
        catch(IOException e) { throw new RuntimeException("ZipFileSite.getJavaZipFile: Error opening " + localZipFileJavaFile.getPath(), e); }
    }

    /**
     * Returns a map of ZipFile paths to ZipEntry(s).
     */
    private synchronized Map <String,ZipEntry> getEntries()
    {
        // If already set, just return
        if (_entries != null) return _entries;

        // Create maps
        _entries = new HashMap<>();
        _dirs = new HashMap<>();

        // Get ZipFile
        ZipFile zipFile = getJavaZipFile();
        if (zipFile == null)
            return _entries;

        // Get ZipEntries and add
        Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
        while (zipFileEntries.hasMoreElements())
            addZipEntry(zipFileEntries.nextElement());

        // Return
        return _entries;
    }

    /**
     * Adds a ZipEntry to WebSite.
     */
    private void addZipEntry(ZipEntry anEntry)
    {
        // Get path and add entry to entries and path to dirs lists
        String filePath = FilePathUtils.getNormalizedPath('/' + anEntry.getName());
        _entries.put(filePath, anEntry);
        addDirListPath(filePath);
    }

    /**
     * Returns a dir list for a path.
     */
    private List <String> getDirList(String aPath)
    {
        // Get parent path and return list for path
        String parentPath =  FilePathUtils.getParentPath(aPath);
        List<String> dirList = _dirs.get(parentPath);
        if (dirList != null)
            return dirList;

        // If list not found, create, set and return
        _dirs.put(parentPath, dirList = new ArrayList<>());
        addDirListPath(parentPath);
        return dirList;
    }

    /**
     * Returns a dir list for a path.
     */
    private void addDirListPath(String aPath)
    {
        if (aPath.length() <= 1) return;
        String path = FilePathUtils.getNormalizedPath(aPath);

        List <String> dirList = getDirList(path);
        if (!dirList.contains(path))
            dirList.add(path);
    }

    /**
     * Handles a get or head request.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get request file path
        String filePath = aReq.getFilePath();

        // Get FileHeader for path
        FileHeader fileHeader = getFileHeaderForFilePath(filePath);

        // If not found, set Response.Code to NOT_FOUND and return
        if (fileHeader == null) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Otherwise configure response
        aResp.setCode(WebResponse.OK);
        aResp.setFileHeader(fileHeader);

        // If Head, just return
        if (isHead)
            return;

        // If file, get/set file bytes
        if (fileHeader.isFile()) {
            ZipEntry zipEntry = getEntries().get(filePath);
            try (InputStream inputStream = _javaZipFile.getInputStream(zipEntry)) {
                byte[] bytes = inputStream.readAllBytes();
                aResp.setBytes(bytes);
            }
            catch (IOException e) { aResp.setException(e); }
        }

        // If directory, get/set dir FileHeaders
        else {

            // Get directory paths
            List<String> dirPaths = _dirs.get(filePath);
            if (dirPaths == null)
                dirPaths = Collections.EMPTY_LIST;

            // Get file headers
            List <FileHeader> fileHeaders = new ArrayList<>();
            for (String dirPath : dirPaths) {
                FileHeader fileHdr = getFileHeaderForFilePath(dirPath);
                if (fileHdr == null)
                    continue;
                fileHeaders.add(fileHdr);
            }

            // Set file headers
            aResp.setFileHeaders(fileHeaders);
        }
    }

    /**
     * Returns a data source file for given path (if file exists).
     */
    private FileHeader getFileHeaderForFilePath(String aPath)
    {
        // Get ZipEntry for path - if not found and not directory, just return
        ZipEntry zipEntry = getEntries().get(aPath);
        if (zipEntry == null && _dirs.get(aPath) == null)
            return null;

        // Create FileHeader and return
        FileHeader fileHeader = new FileHeader(aPath, zipEntry == null || zipEntry.isDirectory());
        fileHeader.setLastModTime(1000);
        if (zipEntry != null) {
            long lastModTime = zipEntry.getTime();
            if (lastModTime == 0)
                lastModTime = zipEntry.getLastModifiedTime().toMillis();
            if (lastModTime == 0)
                lastModTime = 1000;
            fileHeader.setLastModTime(lastModTime);
            fileHeader.setSize(zipEntry.getSize());
        }

        // Return
        return fileHeader;
    }
}