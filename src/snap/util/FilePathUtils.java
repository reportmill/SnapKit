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
    public static final String    SEPARATOR = SnapUtils.isWindows ? "\\" : "/"; //java.io.File.separator;
    public static final char      SEPARATOR_CHAR = SnapUtils.isWindows ? '\\' : '/'; //java.io.File.separatorChar;
    public static final String    PATH_SEPARATOR = SnapUtils.isWindows ? ";" : ":"; //java.io.File.pathSeparator;
    public static final char      PATH_SEPARATOR_CHAR = SnapUtils.isWindows ? ';' : ':'; //java.io.File.pathSeparator;

    /**
     * Returns the file name component of the given string path (everything after last file separator).
     */
    public static String getFileName(String aPath)
    {
        String path = getStandardized(aPath);
        int index = getFileNameIndex(path);
        return index==0 ? path : path.substring(index);
    }

    /**
     * Returns the simple file name for a given path (just the file name without extension).
     */
    public static String getFileNameSimple(String aPath)
    {
        String fileName = getFileName(getStandardized(aPath));
        int index = fileName.lastIndexOf('.');
        return index<0 ? fileName : fileName.substring(0, index);
    }

    /**
     * Returns the index to the first character of the filename component of the given path.
     */
    public static int getFileNameIndex(String aPath)
    {
        String path = getStandardized(aPath);
        int index = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (index<0)
            index = path.lastIndexOf(SEPARATOR);
        return Math.max(index+1, 0);
    }

    /**
     * Returns the extension of the given string path (everything after last '.').
     */
    public static String getExtension(String aPath)
    {
        if (aPath==null) return null;
        String fname = getFileName(aPath);
        int dot = fname.lastIndexOf('.');
        return dot>=0 ? fname.substring(dot+1) : "";
    }

    /**
     * Returns the file type of the given string path (everything after last '.' in lowercase).
     */
    public static String getType(String aPath)
    {
        String ext = getExtension(aPath);
        return ext.toLowerCase();
    }

    /**
     * Returns the given string path minus any extension.
     */
    public static String getSimple(String aPath)
    {
        String ext = getExtension(aPath);
        return ext!=null && ext.length()>0 ? aPath.substring(0, aPath.length()-ext.length()-1) : aPath;
    }

    /**
     * Returns the given string path minus the file name component (everything after last file separator).
     */
    public static String getParent(String aPath)
    {
        int index = getFileNameIndex(aPath);
        return index>0 && aPath.length()>1 ? getStandardized(aPath.substring(0, index)) : "";
    }

    /**
     * Returns a path with a filename or relative path added.
     */
    public static String getChild(String aPath, String aChildPath)
    {
        String path = getStandardized(aPath);
        if (path.endsWith("/") ^ aChildPath.startsWith("/"))
            return path + aChildPath;
        if (aChildPath.startsWith("/"))
            return path + aChildPath.substring(1);
        return path + '/' + aChildPath;
    }

    /**
     * Returns a peer path for given path and new filename by stripping filename from given path and using given name.
     */
    public static String getPeer(String aPath, String aName)
    {
        String parent = getParent(aPath);
        if (parent.length()==0)
            parent = "/";
        return getChild(parent, aName);
    }

    /**
     * Returns a sister path for given path and new extension by stripping extension from path and using given extension.
     */
    public static String getSister(String aPath, String anExtension)
    {
        String path = getSimple(aPath);
        if (!anExtension.startsWith("."))
            path += ".";
        return path + anExtension;
    }

    /**
     * Returns the path in a standard, normal format (strips any trailing file separators).
     */
    public static String getStandardized(String aPath)
    {
        // Get path stripped of trailing file separator
        String path = aPath;
        if ((path.endsWith("/") || path.endsWith("\\")) && path.length()>1)
            path = path.substring(0, path.length()-1);
        else if (path.endsWith(SEPARATOR) && path.length()>SEPARATOR.length())
            path = path.substring(0, path.length()-SEPARATOR.length());

        // Return path
        return path;
    }

    /**
     * Returns a native path for given absolute path using platform native File separator char.
     */
    public static String getNativePath(String aPath)
    {
        if (SEPARATOR_CHAR == '/') return aPath;
        String path = aPath.replace('/', SEPARATOR_CHAR);
        if (path.length() > 2 && path.charAt(2) == ':')
            path = path.substring(1);
        return path;
    }

    /**
     * Returns the native paths for given absolute paths using platform native File separator char.
     */
    public static String[] getNativePaths(String thePaths[])
    {
        if (SEPARATOR_CHAR=='/') return thePaths;
        String npaths[] = new String[thePaths.length];
        for (int i=0; i<thePaths.length; i++)
            npaths[i] = getNativePath(thePaths[i]);
        return npaths;
    }

    /**
     * Returns the total string when joining the given paths by the platform path separator.
     */
    public static String getJoinedPath(String thePaths[])
    {
        StringBuffer sb = new StringBuffer();
        for (int i=0, iMax=thePaths.length; i<iMax; i++)
            sb.append(i==0 ? "" : PATH_SEPARATOR).append(thePaths[i]);
        return sb.toString();
    }

    /**
     * Returns a File for given path.
     */
    public static File getFile(String aPath)
    {
        return new File(aPath);
    }

    /**
     * Returns the Files for given paths.
     */
    public static File[] getFiles(String thePaths[])
    {
        File[] files = new File[thePaths.length];
        for (int i=0; i<thePaths.length; i++)
            files[i] = getFile(thePaths[i]);
        return files;
    }

    /**
     * Returns a URL for given path.
     */
    public static URL getURL(String aPath)
    {
        try { return getFile(aPath).toURI().toURL(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Returns the URLs for given paths.
     */
    public static URL[] getURLs(String thePaths[])
    {
        URL[] urls = new URL[thePaths.length];
        for (int i=0;i<thePaths.length;i++)
            urls[i] = getURL(thePaths[i]);
        return urls;
    }
}