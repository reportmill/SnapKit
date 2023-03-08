/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.util.ArrayUtils;
import snap.util.FilePathUtils;
import snap.util.StringUtils;
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
        // Get InputText string
        String inputText = getInputText(filesBrowser);

        // If empty just return dir path
        if (inputText.length() == 0) {
            WebFile selDir = filesBrowser.getSelDir();
            return selDir.getPath();
        }

        // If starts with ~ return home dir
        if (inputText.startsWith("~"))
            return filesBrowser.getHomeDirPath();

        // If starts with '..', return parent dir
        if (inputText.startsWith("..")) {
            WebFile selFile = filesBrowser.getSelFile();
            WebFile selDir = filesBrowser.getSelDir();
            if (selFile != null)
                return selDir.getPath();
            if (selDir != null && selDir.getParent() != null)
                return selDir.getParent().getPath();
            return "/";
        }

        // If starts with FileSeparator, just return
        if (inputText.startsWith("/") || inputText.startsWith("\\"))
            return inputText;

        // Get path
        WebFile selDir = filesBrowser.getSelDir();
        String inputTextPath = FilePathUtils.getChild(selDir.getPath(), inputText);
        return inputTextPath;
    }

    /**
     * Returns the file resolved from FilesBrowser InputText.
     */
    public static WebFile getInputTextAsFile(FilesBrowser filesBrowser)
    {
        // Get path and file for InputText
        String inputTextPath = getInputTextAsPath(filesBrowser);
        WebFile inputTextFile = filesBrowser.getFileForPath(inputTextPath);

        // If opening a file that doesn't exist, see if it just needs an extension
        if (inputTextFile == null && filesBrowser.isOpening() && !inputTextPath.contains(".")) {
            inputTextPath += filesBrowser.getType();
            inputTextFile = filesBrowser.getFileForPath(inputTextPath);
        }

        // If saving, make sure path has extension and create
        if (inputTextFile == null && filesBrowser.isSaving()) {
            if (!inputTextPath.contains("."))
                inputTextPath += '.' + filesBrowser.getType();
            inputTextFile = filesBrowser.getSite().createFileForPath(inputTextPath, false);
        }

        // Return file
        return inputTextFile;
    }

    /**
     * Returns whether the file resolved from FilesBrowser InputText is valid open/save file.
     */
    public static boolean isInputTextFileValid(FilesBrowser filesBrowser)
    {
        // If saving just return
        if (filesBrowser.isSaving()) {
            String inputText = getInputText(filesBrowser);
            return inputText.length() > 0;
        }

        // Get InputText file and return true
        WebFile inputTextFile = getInputTextAsFile(filesBrowser);

        // If InputText file is valid file, return true
        if (filesBrowser.isValidFile(inputTextFile))
                return true;

        // Return
        return false;
    }

    /**
     * Returns a file completion for given path.
     */
    public static WebFile getFileCompletionForPath(FilesBrowser filesBrowser, String aPath)
    {
        // Get parent directory for path
        String parentPath = FilePathUtils.getParent(aPath);
        WebFile parentDir = filesBrowser.getFileForPath(parentPath);
        if (parentDir == null)
            return null;

        // Get directory files and valid file types
        String fileName = FilePathUtils.getFileName(aPath);
        WebFile[] dirFiles = parentDir.getFiles();
        String[] fileTypes = filesBrowser.getTypes();

        // Look for completion file of any requested type (types are checked in order to allow for precedence)
        for (String type : fileTypes) {
            for (WebFile file : dirFiles) {
                if (StringUtils.startsWithIC(file.getName(), fileName) && file.getType().equals(type))
                    return file;
            }
        }

        // Look for completion of type dir
        for (WebFile file : dirFiles) {
            if (StringUtils.startsWithIC(file.getName(), fileName) && file.isDir())
                return file;
        }

        // Return not found
        return null;
    }

    /**
     * Returns the visible files for given array of files.
     */
    public static WebFile[] getVisibleFiles(WebFile[] theFiles)
    {
        return ArrayUtils.filter(theFiles, file -> !file.getName().startsWith("."));
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
