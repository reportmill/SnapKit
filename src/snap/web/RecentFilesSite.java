/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.viewx.RecentFiles;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This WebSite manages recent files.
 */
public class RecentFilesSite extends WebSite {

    // The id
    private String  _id;

    // The root dir
    private WebFile  _rootDir;

    // A map of all known RecentFiles sites by Id
    private static Map<String,RecentFilesSite>  _recentFilesSites = new HashMap<>();

    /**
     * Constructor.
     */
    private RecentFilesSite(String anId)
    {
        super();
        _id = anId;

        // Create/set URL
        String DROPBOX_ROOT = "recent://";
        String urls = DROPBOX_ROOT + _id;
        WebURL url = WebURL.getURL(urls);
        setURL(url);
    }

    /**
     * Returns the id.
     */
    public String getId()  { return _id; }

    /**
     * Returns the recent files.
     */
    public WebFile[] getRecentFiles()
    {
        WebFile[] recentFiles = RecentFiles.getFiles(_id);
        return recentFiles;
    }

    @Override
    public WebFile getRootDir()
    {
        if (_rootDir != null) return _rootDir;

        // Create RootDir
        _rootDir = createFileForPath("/", true);
        _rootDir.save();

        // Iterate over rootDir files and recent files and set each as link
        WebFile[] rootDirFiles = _rootDir.getFiles();
        WebFile[] recentFiles = getRecentFiles();

        // Iterate over rootDir files and recent files and set each as link
        for (int i = 0; i < rootDirFiles.length; i++) {
            WebFile rootDirFile = rootDirFiles[i];
            WebFile recentFile = recentFiles[i];
            rootDirFile.setLinkFile(recentFile);
        }

        // Set/return
        return _rootDir;
    }

    /**
     * Returns a file from this site for a path.
     */
    private WebFile getRecentFileForPath(String aPath)
    {
        // Get RootDir
        WebFile rootDir = getRootDir();

        // If root dir, just return
        if (aPath.equals("/"))
            return rootDir;

        // Find matching RootDirFile for path and return recent file
        WebFile localFile = getFileForPath(aPath);
        if (localFile != null)
            return localFile.getLinkFile();

        // Return not found
        return null;
    }

    /**
     * Adds a recent file.
     */
    public void addRecentFile(WebFile aFile)
    {
        String localFilePath = "/" + aFile.getName();
        WebFile newLocalFile = createFileForPath(localFilePath, false);
        newLocalFile.setLinkFile(aFile);
        newLocalFile.save();
    }

    /**
     * Handles a get or head request.
     */
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get URL and path
        WebURL fileURL = aReq.getURL();
        String filePath = fileURL.getPath();

        // Handle root dir
        if (filePath == null || filePath.equals("/")) {
            aResp.setCode(WebResponse.OK);
            aResp.setDir(true);
        }

        // Handle root dir file
        else {

            // Get WebFile from Dir site
            WebFile recentFile = getRecentFileForPath(filePath);

            // If not found, set Response.Code to NOT_FOUND and return
            if (recentFile == null) {
                aResp.setCode(WebResponse.NOT_FOUND);
                return;
            }

            // If found, set response code to ok
            aResp.setCode(WebResponse.OK);
            aResp.setDir(recentFile.isDir());
            aResp.setModTime(recentFile.getModTime());
            aResp.setSize(recentFile.getSize());
            return;
        }

        // If Head, just return
        if (isHead)
            return;

        // Handle dir: Get/set dir FileHeaders - should only get called for root dir
        assert (filePath.equals("/"));

        // Get RecentFiles and FileHeaders list
        WebFile[] recentFiles = getRecentFiles();
        List<FileHeader> fileHeaders = new ArrayList<>(recentFiles.length);

        // Iterate over RecentFiles and create/add FileHeader for each
        for (WebFile recFile : recentFiles) {
            String localFilePath = "/" + recFile.getName();
            FileHeader fileHeader = new FileHeader(localFilePath, recFile.isDir());
            fileHeader.setModTime(recFile.getModTime());
            fileHeader.setSize(recFile.getSize());
            fileHeaders.add(fileHeader);
        }

        // Set FileHeaders
        aResp.setFileHeaders(fileHeaders);
    }

    /**
     * Handle POST request.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)
    {
        doPut(aReq, aResp);
    }

    /**
     * Override to suppress, since RecentFiles are really virtual files.
     */
    @Override
    protected void doPut(WebRequest aReq, WebResponse aResp)  { }

    /**
     * Override to suppress, since RecentFiles are really virtual files.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)  { }

    /**
     * Override to get Java file from dir file.
     */
    @Override
    protected File getJavaFile(WebURL aURL)
    {
        String filePath = aURL.getPath();
        WebFile recentFile = getRecentFileForPath(filePath);
        return recentFile != null ? recentFile.getJavaFile() : null;
    }

    /**
     * Returns the recent files site for id.
     */
    public static RecentFilesSite getRecentFilesSiteForId(String anId)
    {
        RecentFilesSite recentFilesSite = _recentFilesSites.get(anId);
        if (recentFilesSite == null) {
            recentFilesSite = new RecentFilesSite(anId);
            _recentFilesSites.put(anId, recentFilesSite);
        }
        return recentFilesSite;
    }
}