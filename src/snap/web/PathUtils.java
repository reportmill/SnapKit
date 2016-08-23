/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;

/**
 * Utilities for paths.
 */
public class PathUtils {

/**
 * Returns the path in a normal format (strips any trailing file separators).
 */
public static String getNormalized(String aPath)
{
    String path = aPath;
    if(!path.startsWith("/")) path = '/' + path;  // Make sure it starts with slash
    if(path.length()>1 && path.endsWith("/")) path = path.substring(0, path.length()-1);  // But doesn't end with slash
    return path;
}

/**
 * Returns a path with a filename or relative path added.
 */
public static String getChild(String aPath, String aChildPath)
{
    String path = getNormalized(aPath);
    if(path.endsWith("/"))
        return path + (aChildPath.startsWith("/")? aChildPath.substring(1) : aChildPath);
    return path + (aChildPath.startsWith("/")? aChildPath : ("/" + aChildPath));
}

/**
 * Strips any trailing separator from end.
 */
public static String stripTrailingSlash(String aPath)
{
    String path = aPath; int plen = path.length();
    if(plen>1 && path.endsWith("/")) path = path.substring(0, plen-1);
    return path;
}

}