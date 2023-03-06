/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.ArrayUtils;
import snap.view.TreeResolver;
import snap.web.WebFile;
import snap.web.WebSite;
import snap.web.WebURL;

/**
 * Utilities for FilesBrowser.
 */
class FilesBrowserUtils {

    /**
     * Returns the visible files for given array of files.
     */
    public static WebFile[] getVisibleFiles(WebFile[] theFiles)
    {
        return ArrayUtils.filter(theFiles, file -> isVisibleFile(file));
    }

    /**
     * Returns whether given file is visible (not hidden).
     */
    public static boolean isVisibleFile(WebFile aFile)
    {
        return !aFile.getName().startsWith(".");
    }

    /**
     * Returns the home directory path.
     */
    public static String getHomeDirPathForSite(WebSite aSite)
    {
        if (aSite != getLocalFileSystemSite())
            return "/";
        return System.getProperty("user.home");
    }

    /**
     * Returns the local file system site.
     */
    public static WebSite getLocalFileSystemSite()
    {
        WebURL defaultSiteURL = WebURL.getURL("/");
        return defaultSiteURL.getSite();
    }

    /**
     * Returns a normalized type.
     */
    public static String normalizeType(String aType)
    {
        String type = aType.trim().toLowerCase();
        if (type.startsWith("."))
            type = type.substring(1);
        return type;
    }

    /**
     * The TreeResolver to provide data to File browser.
     */
    public static class FileResolver extends TreeResolver<WebFile> {

        /**
         * Constructor.
         */
        public FileResolver()
        {
            super();
        }

        /**
         * Returns the parent of given item.
         */
        public WebFile getParent(WebFile anItem)
        {
            return anItem.getParent();
        }

        /**
         * Whether given object is a parent (has children).
         */
        public boolean isParent(WebFile anItem)
        {
            return anItem.isDir();
        }

        /**
         * Returns the children.
         */
        public WebFile[] getChildren(WebFile aPar)
        {
            return FilesBrowserUtils.getVisibleFiles(aPar.getFiles());
        }

        /**
         * Returns the text to be used for given item.
         */
        public String getText(WebFile anItem)
        {
            return anItem.getName();
        }
    }
}
