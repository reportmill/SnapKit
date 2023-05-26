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

import java.util.function.Predicate;

/**
 * Utilities for WebSitePane.
 */
class WebSitePaneUtils {

    /**
     * Returns the InputText string.
     */
    public static String getInputText(WebSitePaneX sitePane)
    {
        return sitePane._inputText.getText().trim();
    }

    /**
     * Returns the path resolved from WebSitePane InputText.
     */
    public static String getInputTextAsPath(WebSitePaneX sitePane)
    {
        // Get InputText string
        String inputText = getInputText(sitePane);

        // If empty just return dir path
        if (inputText.length() == 0) {
            WebFile selDir = sitePane.getSelDir();
            return selDir.getPath();
        }

        // If starts with ~ return home dir
        if (inputText.startsWith("~"))
            return sitePane.getHomeDirPath();

        // If starts with '..', return parent dir
        if (inputText.startsWith("..")) {
            WebFile selFile = sitePane.getSelFile();
            WebFile selDir = sitePane.getSelDir();
            if (selFile != null)
                return selDir.getPath();
            if (selDir != null) {
                WebFile selDirParent = selDir.getParent();
                if (selDirParent != null)
                    return selDirParent.getPath();
            }
            return "/";
        }

        // If starts with FileSeparator, just return
        if (inputText.startsWith("/") || inputText.startsWith("\\"))
            return inputText;

        // Get path
        WebFile selDir = sitePane.getSelDir();
        String inputTextPath = FilePathUtils.getChild(selDir.getPath(), inputText);
        return inputTextPath;
    }

    /**
     * Returns the file resolved from WebSitePane InputText.
     */
    public static WebFile getInputTextAsFile(WebSitePaneX sitePane)
    {
        // Get file for InputText path
        String inputTextPath = getInputTextAsPath(sitePane);
        WebFile inputTextFile = sitePane.getFileForPath(inputTextPath);

        // If file not found and path is missing extension, try again with extension
        if (inputTextFile == null && !inputTextPath.contains(".")) {
            String[] types = sitePane.getTypes();
            if (types != null && types.length > 0) {
                String type = types[0];
                inputTextPath += '.' + type;
                inputTextFile = sitePane.getFileForPath(inputTextPath);
            }
        }

        // If file not found and isSaving, create file
        if (inputTextFile == null && sitePane.isSaving()) {
            WebSite site = sitePane.getSite();
            inputTextFile = site.createFileForPath(inputTextPath, false);
        }

        // Return file
        return inputTextFile;
    }

    /**
     * Performs file name completion on input text.
     */
    public static WebFile performFileCompletionOnInputText(WebSitePaneX sitePane)
    {
        // Get InputText - just return if empty
        String inputText = WebSitePaneUtils.getInputText(sitePane);
        if (inputText.length() == 0)
            return null;

        // Get completion candidate for InputText path - just return if not found
        String inputTextPath = WebSitePaneUtils.getInputTextAsPath(sitePane);
        WebFile completionFile = WebSitePaneUtils.getFileCompletionForPath(sitePane, inputTextPath);
        if (completionFile == null)
            return null;

        // Get completion string - usually just completion file filename, but it could be whole path
        String completionStr = completionFile.getName();
        if (StringUtils.startsWithIC(inputTextPath, inputText))
            completionStr = completionFile.getPath();

        // Set completion string in InputText
        sitePane._inputText.setCompletionText(completionStr);

        // Return
        return completionFile;
    }

    /**
     * Returns a file completion for given path.
     */
    private static WebFile getFileCompletionForPath(WebSitePaneX sitePane, String aPath)
    {
        // Get parent directory for path
        String parentPath = FilePathUtils.getParentPath(aPath);
        WebFile parentDir = sitePane.getFileForPath(parentPath);
        if (parentDir == null)
            return null;

        // Get directory files and valid file types
        String fileName = FilePathUtils.getFilename(aPath);
        WebFile[] dirFiles = parentDir.getFiles();

        // Look for valid completion file
        Predicate<WebFile> fileValidator = file -> StringUtils.startsWithIC(file.getName(), fileName) && sitePane.isValidFile(file);
        WebFile firstValidFile = ArrayUtils.findMatch(dirFiles, fileValidator);
        if (firstValidFile != null)
            return firstValidFile;

        // Look for completion of type dir
        for (WebFile file : dirFiles) {
            if (StringUtils.startsWithIC(file.getName(), fileName) && file.isDir())
                return file;
        }

        // Return not found
        return null;
    }

    /**
     * Returns whether given file is valid for given types.
     */
    public static boolean isValidFileForTypes(WebFile aFile, String[] theTypes)
    {
        if (aFile == null)
            return false;
        if (theTypes == null || theTypes.length == 0)
            return true;

        return aFile.isFile() && ArrayUtils.contains(theTypes, aFile.getType());
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
        assert (defaultSiteURL != null);
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
