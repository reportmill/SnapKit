/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.ArrayUtils;
import snap.viewx.RecentFiles;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This WebSite manages recent files.
 */
public class RecentFilesSite extends WebSite {

    // The id
    private String  _id;

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
     * Override to create RootDir file.
     */
    @Override
    protected WebFile getFileForPathImpl(String filePath) throws ResponseException
    {
        // Handle RootDir
        if (filePath.equals("/")) {
            WebFile rootDir = createFileForPath("/", true);
            rootDir.save();
            return rootDir;
        }

        // Handle root dir file
        WebFile rootDir = getRootDir();
        WebFile[] rootDirFiles = rootDir.getFiles();
        WebFile rootDirFile = ArrayUtils.findMatch(rootDirFiles, file -> file.getPath().equals(filePath));
        if (rootDirFile != null)
            return rootDirFile;

        // This should never happen
        System.err.println("RecentFilesSite.getFileForPathImpl: Can't find file for path: " + filePath);
        return null;
    }

    /**
     * Override to provide root dir files.
     */
    @Override
    protected FileContents getContentsForFile(WebFile aFile)
    {
        String filePath = aFile.getPath();

        // Handle RootDir
        if (filePath.equals("/")) {

            // Get recent file
            WebFile[] recentFiles = getRecentFiles();
            WebFile[] rootDirFiles = new WebFile[recentFiles.length];

            // Iterate over rootDir files and recent files and set each as link
            for (int i = 0; i < recentFiles.length; i++) {
                WebFile recentFile = recentFiles[i];
                String rootDirPath = '/' + recentFile.getName();
                WebFile rootDirFile = rootDirFiles[i] = createFileForPath(rootDirPath, false);
                rootDirFile.setLinkFile(recentFile);
                rootDirFile._saved = true;
            }

            // Return
            return new FileContents(rootDirFiles, 0);
        }

        // Should never happen
        System.err.println("RecentFilesSite.getContentsForFile: Shouldn't need contents for file: " + aFile.getPath());
        return null;
    }

    /**
     * Override to suppress, since RecentFiles are really virtual files.
     */
    protected void doPost(WebRequest aReq, WebResponse aResp)  { }

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
        WebFile file = aURL.getFile();
        WebFile realFile = file.getRealFile();
        if (realFile != file)
            return realFile.getJavaFile();
        return null;
    }

    /**
     * Returns the recent files site for id.
     */
    public static RecentFilesSite getRecentFilesSiteForId(String anId)
    {
        // Get from sites cache - just return if found
        RecentFilesSite recentFilesSite = _recentFilesSites.get(anId);
        if (recentFilesSite != null)
            return recentFilesSite;

        // Create and add to cache
        recentFilesSite = new RecentFilesSite(anId);
        _recentFilesSites.put(anId, recentFilesSite);

        // Return
        return recentFilesSite;
    }
}