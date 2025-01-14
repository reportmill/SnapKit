/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.*;
import snap.util.FilePathUtils;
import snap.util.SnapUtils;

/**
 * A WebSite subclass for Zip and Jar files.
 */
public class ZipFileSite extends WebSite {

    // The ZipFile
    private ZipFile _zipFile;
    
    // A map of paths to ZipEntry
    private Map<String,ZipEntry> _entries;
    
    // A map of directory paths to List of child paths
    private Map<String,List<String>> _dirs;
    
    // Whether Zip is really a Jar
    private boolean _jar;

    // Whether to trim entries via isInterestingPath (silly Jar feature)
    private boolean _trim;

    /**
     * Constructor.
     */
    public ZipFileSite()
    {
        super();
    }

    /**
     * Returns the ZipFile.
     */
    protected ZipFile getZipFile()
    {
        // If already set, just return
        if (_zipFile != null) return _zipFile;

        // Get java file
        File javaFile = getJavaFile();
        if (javaFile == null)
            return null;

        // Return file
        try {

            // If Jar, use JarFile
            if (_jar)
                return _zipFile = new JarFile(javaFile);

            // Otherwise return ZipFile
            return _zipFile = new ZipFile(javaFile);
        }

        // Rethrow exception
        catch(IOException e) { throw new RuntimeException("ZipFileSize.getZipFile: Error opening " + javaFile.getPath(), e); }
    }

    /**
     * Returns a map of ZipFile paths to ZipEntry(s).
     */
    protected synchronized Map <String,ZipEntry> getEntries()
    {
        // If already set, just return
        if (_entries != null) return _entries;

        // Create maps
        _entries = new HashMap<>();
        _dirs = new HashMap<>();

        // Get ZipFile
        ZipFile zipFile = getZipFile();
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
    protected void addZipEntry(ZipEntry anEntry)
    {
        // If performing trim, check entry name
        if (_trim && !anEntry.isDirectory() && !isInterestingPath(anEntry.getName()))
            return;

        // Get path and add entry to entries and path to dirs lists
        String filePath = FilePathUtils.getNormalizedPath('/' + anEntry.getName());
        _entries.put(filePath, anEntry);
        addDirListPath(filePath);
    }

    /**
     * Returns a dir list for a path.
     */
    protected List <String> getDirList(String aPath)
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
    protected void addDirListPath(String aPath)
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
            try {
                ZipEntry zipEntry = getEntries().get(filePath);
                InputStream inputStream = _zipFile.getInputStream(zipEntry);
                byte[] bytes = SnapUtils.getInputStreamBytes(inputStream);
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
            aResp.setFileHeaders(fileHeaders.toArray(new FileHeader[0]));
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

    /**
     * Returns a Java file for the zip file URL (copied to Sandbox if remote).
     */
    protected File getJavaFile()
    {
        WebURL url = getURL();
        WebFile file = url.getFile();
        if (file == null)
            return null;

        // Get local file in case file is over http
        WebFile localFile = file.getSite().getLocalFile(file, true);
        return localFile.getJavaFile();
    }

    /**
     * Override to turn on file trimming from system jars.
     */
    public void setURL(WebURL aURL)
    {
        // Do normal version
        super.setURL(aURL);

        // Turn on file trimming if system jar
        String urls = aURL.getString().toLowerCase();
        _jar = urls.endsWith(".jar");
        _trim = _jar && (urls.contains("/rt.jar") || urls.contains("/jfxrt.jar"));
    }

    /**
     * Adds an entry (override to ignore).
     */
    protected boolean isInterestingPath(String aPath)
    {
        // Bogus excludes
        if (aPath.startsWith("sun")) return false;
        if (aPath.startsWith("com/sun")) return false;
        if (aPath.startsWith("com/apple")) return false;
        if (aPath.startsWith("javax/swing/plaf")) return false;
        if (aPath.startsWith("org/omg")) return false;
        int dollar = aPath.endsWith(".class")? aPath.lastIndexOf('$') : -1;
        if (dollar > 0 && Character.isDigit(aPath.charAt(dollar+1))) return false;
        return true;
    }
}