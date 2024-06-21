/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.File;

/**
 * Utilities for paths.
 */
public class PathUtils {

    /**
     * Returns the path in a normal format (strips any trailing file separators).
     */
    public static String getNormalized(String aPath)
    {
        // Get path with standard separator
        String filePath = aPath;
        if (File.separatorChar != '/')
            filePath = filePath.replace(File.separatorChar, '/');

        // Make sure path starts with slash, but doesn't end with slash
        if (!filePath.startsWith("/"))
            filePath = '/' + filePath;
        if (filePath.length() > 1 && filePath.endsWith("/"))
            filePath = filePath.substring(0, filePath.length()-1);

        // Return
        return filePath;
    }

    /**
     * Returns a path with a filename or relative path added.
     */
    public static String getParent(String aPath)
    {
        String path = getNormalized(aPath);
        int ind = path.lastIndexOf('/');
        if (ind>0)
            path = path.substring(0, ind);
        return path;
    }

    /**
     * Returns a path with a filename or relative path added.
     */
    public static String getChild(String aPath, String aChildPath)
    {
        String path = getNormalized(aPath);
        if (path.endsWith("/"))
            return path + (aChildPath.startsWith("/") ? aChildPath.substring(1) : aChildPath);
        return path + (aChildPath.startsWith("/") ? aChildPath : ("/" + aChildPath));
    }
}