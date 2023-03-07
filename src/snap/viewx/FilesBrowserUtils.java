/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.ArrayUtils;
import snap.util.FilePathUtils;
import snap.view.TreeResolver;
import snap.web.WebFile;
import snap.web.WebSite;
import snap.web.WebURL;

/**
 * Utilities for FilesBrowser.
 */
class FilesBrowserUtils {

    /**
     * Returns the InputText string.
     */
    public static String getInputText(FilesBrowser filesBrowser)
    {
        return filesBrowser._inputText.getText().trim();
    }

    /**
     * Returns the path resolved from FilesBrowser InputText.
     */
    public static String getInputTextAsPath(FilesBrowser filesBrowser)
    {
        // Get FileText string
        String fileText = getInputText(filesBrowser);

        // If empty just return dir path
        if (fileText.length() == 0) {
            WebFile selDir = filesBrowser.getSelDir();
            return selDir.getPath();
        }

        // If starts with ~ return home dir
        if (fileText.startsWith("~"))
            return filesBrowser.getHomeDirPath();

        // If starts with '..', return parent dir
        if (fileText.startsWith("..")) {
            WebFile selFile = filesBrowser.getSelFile();
            WebFile selDir = filesBrowser.getSelDir();
            if (selFile != null)
                return selDir.getPath();
            if (selDir != null && selDir.getParent() != null)
                return selDir.getParent().getPath();
            return "/";
        }

        // If starts with FileSeparator, just return
        if (fileText.startsWith("/") || fileText.startsWith("\\"))
            return fileText;

        // Get path
        WebFile selDir = filesBrowser.getSelDir();
        String path = FilePathUtils.getChild(selDir.getPath(), fileText);
        return path;
    }

    /**
     * Returns the file resolved from FilesBrowser InputText.
     */
    public static WebFile getInputTextAsFile(FilesBrowser filesBrowser)
    {
        // Get path and file for FileText
        String path = getInputTextAsPath(filesBrowser);
        WebFile file = filesBrowser.getFileForPath(path);

        // If opening a file that doesn't exist, see if it just needs an extension
        if (file == null && filesBrowser.isOpening() && !path.contains(".")) {
            path += filesBrowser.getType();
            file = filesBrowser.getFileForPath(path);
        }

        // If saving, make sure path has extension and create
        if (file == null && filesBrowser.isSaving()) {
            if (!path.contains("."))
                path += '.' + filesBrowser.getType();
            file = filesBrowser.getSite().createFileForPath(path, false);
        }

        // Return file
        return file;
    }

    /**
     * Returns whether the file resolved from FilesBrowser InputText is valid open/save file.
     */
    public static boolean isInputTextFileValid(FilesBrowser filesBrowser)
    {
        // If saving just return
        if (filesBrowser.isSaving()) {
            String fileTextPath = getInputText(filesBrowser);
            return fileTextPath.length() > 0;
        }

        // Get file for path based on FilePanel Dir and FileText (filename) - just return false if null
        WebFile file = getInputTextAsFile(filesBrowser);
        if (file == null)
            return false;

        // If file is plain file and matches requested type, return true
        if (file.isFile()) {
            if (ArrayUtils.contains(filesBrowser.getTypes(), file.getType()))
                return true;
        }

        // Return
        return false;
    }

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
            return getVisibleFiles(aPar.getFiles());
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
