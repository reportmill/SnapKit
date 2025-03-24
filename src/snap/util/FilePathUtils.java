/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.File;
import java.net.URL;

/**
 * Utility methods for file path strings.
 */
public class FilePathUtils {
    
    // Separator
    public static final String    SEPARATOR = SnapEnv.isWindows ? "\\" : "/"; //java.io.File.separator;
    public static final char      SEPARATOR_CHAR = SnapEnv.isWindows ? '\\' : '/'; //java.io.File.separatorChar;
    public static final String    PATH_SEPARATOR = SnapEnv.isWindows ? ";" : ":"; //java.io.File.pathSeparator;
    public static final char      PATH_SEPARATOR_CHAR = SnapEnv.isWindows ? ';' : ':'; //java.io.File.pathSeparator;

    /**
     * Returns the path in standard unix format (forward slash separator with leading slash and no trailing slash).
     */
    public static String getNormalizedPath(String aPath)
    {
        // Get path with standard separator
        String filePath = aPath;
        if (File.separatorChar != '/')
            filePath = filePath.replace(File.separatorChar, '/');

        // Make sure path starts with slash, but doesn't end with slash
        if (!filePath.startsWith("/") && !isProbablyUrl(filePath))
            filePath = '/' + filePath;
        if (filePath.length() > 1 && filePath.endsWith("/"))
            filePath = filePath.substring(0, filePath.length()-1);

        // Return
        return filePath;
    }

    /**
     * Returns the file name component of the given string path (everything after last file separator).
     */
    public static String getFilename(String aPath)
    {
        String filePath = getNormalizedPath(aPath);
        int filenameCharIndex = getFilenameCharIndex(filePath);
        return filenameCharIndex == 0 ? filePath : filePath.substring(filenameCharIndex);
    }

    /**
     * Returns the simple file name for a given path (just the file name without extension).
     */
    public static String getFilenameSimple(String aPath)
    {
        String filename = getFilename(aPath);
        int extensionIndex = filename.lastIndexOf('.');
        return extensionIndex < 0 ? filename : filename.substring(0, extensionIndex);
    }

    /**
     * Returns the index to the first character of the filename component of the given path.
     */
    private static int getFilenameCharIndex(String aPath)
    {
        String filePath = getNormalizedPath(aPath);
        int separatorIndex = filePath.lastIndexOf('/');
        return separatorIndex + 1;
    }

    /**
     * Returns the extension of the given string path (everything after last '.').
     */
    public static String getExtension(String aPath)
    {
        if (aPath == null) return "";
        String filename = getFilename(aPath);
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot+1) : "";
    }

    /**
     * Returns the file type of the given string path (everything after last '.' in lowercase).
     */
    public static String getFileType(String aPath)
    {
        String ext = getExtension(aPath);
        return ext.toLowerCase();
    }

    /**
     * Returns the given string path minus any extension.
     */
    public static String getPathWithoutExtension(String aPath)
    {
        String extension = getExtension(aPath);
        return !extension.isEmpty() ? aPath.substring(0, aPath.length() - extension.length() - 1) : aPath;
    }

    /**
     * Returns the given string path minus the file name component (everything after last file separator).
     */
    public static String getParentPath(String aPath)
    {
        String filePath = getNormalizedPath(aPath);
        int lastSeparatorIndex = filePath.lastIndexOf('/');
        if (lastSeparatorIndex > 0)
            filePath = filePath.substring(0, lastSeparatorIndex);
        else filePath = filePath.length() > 1 ? "/" : "";
        return filePath;
    }

    /**
     * Returns a path with a filename or relative path added.
     */
    public static String getChildPath(String aPath, String aChildPath)
    {
        String parentPath = getNormalizedPath(aPath);
        String childPath = getNormalizedPath(aChildPath);
        if (parentPath.length() <= 1)
            return childPath;
        return parentPath + childPath;
    }

    /**
     * Returns a peer path for given path and new filename by stripping filename from given path and using given name.
     */
    public static String getPeerPath(String aPath, String aName)
    {
        String parentPath = getParentPath(aPath);
        return getChildPath(parentPath, aName);
    }

    /**
     * Returns a sister path for given path and new extension by stripping extension from path and using given extension.
     */
    public static String getSisterPath(String aPath, String anExtension)
    {
        String path = getPathWithoutExtension(aPath);
        if (!anExtension.startsWith("."))
            path += ".";
        return path + anExtension;
    }

    /**
     * Returns a native path for given absolute path using platform native File separator char.
     */
    public static String getNativePath(String aPath)
    {
        if (SEPARATOR_CHAR == '/') return aPath;
        String filePath = aPath.replace('/', SEPARATOR_CHAR);

        // If Windows drive designator, remove leading backslash - this is bogus
        if (filePath.length() > 2 && filePath.charAt(2) == ':')
            filePath = filePath.substring(1);

        // Return
        return filePath;
    }

    /**
     * Returns the native paths for given absolute paths using platform native File separator char.
     */
    public static String[] getNativePaths(String[] thePaths)
    {
        if (SEPARATOR_CHAR == '/')
            return thePaths;
        return ArrayUtils.map(thePaths, path -> getNativePath(path), String.class);
    }

    /**
     * Returns the total string when joining the given paths by the platform path separator.
     */
    public static String getJoinedPath(String[] thePaths)
    {
        return StringUtils.join(thePaths, PATH_SEPARATOR);
    }

    /**
     * Returns a URL for given path.
     */
    public static URL getUrlForPath(String aPath)
    {
        try { return new File(aPath).toURI().toURL(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the URLs for given paths.
     */
    public static URL[] getUrlsForPaths(String[] thePaths)
    {
        return ArrayUtils.map(thePaths, path -> getUrlForPath(path), URL.class);
    }

    /**
     * Returns whether given URL string is probably a URL.
     */
    private static boolean isProbablyUrl(String aPath)
    {
        int colonIndex = aPath.indexOf(':');
        if (colonIndex < 0)
            return false;
        String protocol = aPath.substring(0, colonIndex).toLowerCase();
        return ArrayUtils.contains(KNOWN_PROTOCOLS, protocol);
    }
    private static String[] KNOWN_PROTOCOLS = { "file", "http", "https" };
}