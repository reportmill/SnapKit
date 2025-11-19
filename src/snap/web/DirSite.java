/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.props.PropChange;
import snap.util.FilePathUtils;
import snap.util.ListUtils;
import java.io.File;
import java.util.List;

/**
 * A data source implementation that draws from a directory WebFile.
 */
public class DirSite extends WebSite {

    // The directory WebFile
    private WebFile _dir;

    /**
     * Constructor.
     */
    public DirSite()
    {
        super();
    }

    /**
     * Returns the directory file this site represents.
     */
    public WebFile getDir()
    {
        if (_dir != null) return _dir;

        // Get dir
        WebURL siteURL = getURL();
        WebFile dir = siteURL.createFile(true);

        // Start listening to file prop changes
        WebSite dirFileSite = siteURL.getSite();
        dirFileSite.addFileChangeListener(this::handleDirSiteFilePropChange);

        // Set and return
        return _dir = dir;
    }

    /**
     * Handles a head request.
     */
    @Override
    protected void doHead(WebRequest aReq, WebResponse aResp)
    {
        // Get source file from Dir site - if not found, set Response.Code to NOT_FOUND and return
        String filePath = aReq.getFilePath();
        WebFile dirFile = getDirFileForPath(filePath);
        if (dirFile == null) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Set header info
        aResp.setDir(dirFile.isDir());
        aResp.setLastModTime(dirFile.getLastModTime());
        aResp.setSize(dirFile.getSize());
    }

    /**
     * Handles a get request.
     */
    @Override
    protected void doGet(WebRequest aReq, WebResponse aResp)
    {
        // Get source file from Dir site - if not found, set Response.Code to NOT_FOUND and return
        String filePath = aReq.getFilePath();
        WebFile dirFile = getDirFileForPath(filePath);
        if (dirFile == null) {
            aResp.setCode(WebResponse.NOT_FOUND);
            return;
        }

        // Set header info
        aResp.setDir(dirFile.isDir());
        aResp.setLastModTime(dirFile.getLastModTime());

        // If file, get/set file bytes
        if (dirFile.isFile()) {
            byte[] bytes = dirFile.getBytes();
            aResp.setBytes(bytes);
            return;
        }

        // Otherwise, get/set dir FileHeaders
        List<WebFile> dirFiles = dirFile.getFiles();
        List<FileHeader> fileHeaders = ListUtils.map(dirFiles, file -> createFileHeaderForFile(filePath, file));
        aResp.setFileHeaders(fileHeaders);
    }

    /**
     * Handle PUT request.
     */
    @Override
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        // Get remote file
        WebFile localFile = aReq.getFile();
        WebFile dirFile = createDirFileForPath(localFile.getPath(), localFile.isDir());

        // Update bytes and save
        if (localFile.isFile())
            dirFile.setBytes(localFile.getBytes());
        dirFile.save();

        // Update response
        long lastModTime = dirFile.getLastModTime();
        aResp.setLastModTime(lastModTime);
    }

    /**
     * Handle DELETE request.
     */
    @Override
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        String filePath = aReq.getFilePath();
        WebFile dirFile = getDirFileForPath(filePath);
        if (dirFile != null)
            dirFile.delete();
    }

    /**
     * Override to forward to dir site.
     */
    @Override
    protected void setLastModTimeForFileImpl(WebFile aFile, long aTime) throws Exception
    {
        WebFile dirFile = getDirFileForPath(aFile.getPath());
        if (dirFile != null) {
            WebSite.setLastModTimeForFile(dirFile, aTime);
            aFile.setLastModTime(dirFile.getLastModTime());
        }

        // If file not found, complain - should probably throw exception
        else System.err.println("DirSite.setLastModTimeForFileImpl() can't find dir file: " + aFile.getUrlAddress());
    }

    /**
     * Override to get Java file from dir file.
     */
    @Override
    protected File getJavaFileForUrl(WebURL aURL)
    {
        String filePath = aURL.getPath();
        WebFile dirFile = getDirFileForPath(filePath);
        return dirFile != null ? dirFile.getJavaFile() : null;
    }

    /**
     * Override to forward to dir file.
     */
    @Override
    protected void resetFile(WebFile aFile)
    {
        super.resetFile(aFile);
        WebFile dirFile = createDirFileForPath(aFile.getPath(), aFile.isDir());
        dirFile.reset();
    }

    /**
     * Called when dir file site has file change. Used to reset local file when source file is deleted.
     */
    private void handleDirSiteFilePropChange(PropChange propChange)
    {
        // If not Exists prop, just return
        if (propChange.getPropName() != WebFile.Exists_Prop)
            return;

        // If given file created, just return
        WebFile dirSiteFile = (WebFile) propChange.getSource();
        if (dirSiteFile.getExists())
            return;

        // If given file not in this site, just return
        String dirSiteFilePath = dirSiteFile.getPath();
        if (!dirSiteFilePath.startsWith(getDir().getPath()))
            return;

        // Get local file and reset if verified
        String localPath = dirSiteFilePath.substring(getDir().getPath().length());
        WebFile localFile = createFileForPath(localPath.isEmpty() ? "/" : localPath, dirSiteFile.isDir());
        if (localFile.isVerified())
            localFile.resetAndVerify();
    }

    /**
     * Returns the foreign file for a path from foreign site.
     */
    private WebFile getDirFileForPath(String aPath)
    {
        WebFile dir = getDir();
        WebSite dirSite = dir.getSite();
        String dirFilePath = dir.getPath() + aPath;
        return dirSite.getFileForPath(dirFilePath);
    }

    /**
     * Returns a new foreign file for a path from foreign site.
     */
    private WebFile createDirFileForPath(String aPath, boolean isDir)
    {
        WebFile dir = getDir();
        WebSite dirSite = dir.getSite();
        String dirFilePath = dir.getPath() + aPath;
        return dirSite.createFileForPath(dirFilePath, isDir);
    }

    /**
     * Returns a new FileHeader for given file.
     */
    private static FileHeader createFileHeaderForFile(String parentFilePath, WebFile aFile)
    {
        String filePath = FilePathUtils.getChildPath(parentFilePath, aFile.getName());
        FileHeader fileHeader = new FileHeader(aFile);
        fileHeader.setPath(filePath);
        return fileHeader;
    }
}