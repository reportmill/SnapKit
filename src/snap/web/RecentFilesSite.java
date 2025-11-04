/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;

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
        WebURL url = WebURL.getUrl(urls);
        setURL(url);

        // Set this to shared
        _shared = this;
    }

    /**
     * Handles a get or head request.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Get file path - if not root dir, complain
        String filePath = aReq.getFilePath();
        if (!filePath.equals("/"))
            throw new RuntimeException("RecentFilesSite: doGetOrHead: Requesting file for illegal path: " + filePath);

        // Create file header and set in response
        FileHeader fileHeader = new FileHeader("/", true);
        aResp.setFileHeader(fileHeader);

        // If Head, just return
        if (isHead)
            return;

        // Get recent files
        List<WebFile> recentFiles = RecentFiles.getFiles();
        List<FileHeader> fileHeaders = new ArrayList<>(recentFiles.size());

        // Iterate over rootDir files and recent files and set each as link
        for (int i = 0; i < recentFiles.size(); i++) {
            WebFile recentFile = recentFiles.get(i);
            WebURL recentFileURL = recentFile.getUrl();
            String recentFilePath = "/RecentFile-" + i + "-" + recentFileURL.getFilename();
            FileHeader fileHdr = new FileHeader(recentFilePath, recentFile.isDir());
            fileHdr.setLastModTime(1000);
            fileHdr.setLinkUrl(recentFileURL);
            fileHeaders.add(fileHdr);
        }

        // Set FileHeaders
        aResp.setFileHeaders(fileHeaders);
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