/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.ArrayUtils;
import java.io.File;

/**
 * This WebSite manages recent files.
 */
public class RecentFilesSite extends WebSite {

    // The shared site
    private static RecentFilesSite  _shared;

    /**
     * Constructor.
     */
    private RecentFilesSite()
    {
        super();

        // Create/set URL
        String RECENT_FILES_ROOT = "recent:";
        String urls = RECENT_FILES_ROOT;
        WebURL url = WebURL.getURL(urls);
        setURL(url);

        // Set this to shared
        _shared = this;
    }

    /**
     * Override to create RootDir file.
     */
    @Override
    protected WebFile getFileForPathImpl(String filePath) throws ResponseException
    {
        // Handle RootDir
        if (filePath.equals("/"))
            return createFileForPath("/", true);

        // Handle any other path: Return first (any) RootDir file with matching name
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
            WebFile[] recentFiles = RecentFiles.getFiles();
            WebFile[] rootDirFiles = new WebFile[recentFiles.length];

            // Iterate over rootDir files and recent files and set each as link
            for (int i = 0; i < recentFiles.length; i++) {
                WebFile recentFile = recentFiles[i];
                String rootDirPath = '/' + recentFile.getName();
                WebFile rootDirFile = rootDirFiles[i] = createFileForPath(rootDirPath, false);
                rootDirFile.setLinkFile(recentFile);
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
    public static RecentFilesSite getShared()
    {
        if (_shared != null) return _shared;
        return _shared = new RecentFilesSite();
    }
}