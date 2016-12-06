/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.net.URL;
import java.util.*;
import snap.util.URLUtils;

/**
 * A data source that pulls from the class path.
 */
public class ClassSite extends WebSite {

/**
 * Override to return name for ClassSite.
 */
public String getName()  { return "Class"; }

/**
 * Override to return Protocol for ClassSite.
 */
public String getURLScheme()  { return "class"; }

/**
 * Returns a WebFile for given path (if file exists).
 */
protected FileHeader getFileHeader(String aPath)
{
    URL url = getClass().getResource(aPath); if(url==null) return null;
    FileHeader file = new FileHeader(aPath, aPath.equals("/"));
    return file;
}

/**
 * Returns file content (bytes for file, FileHeaders for dir).
 */
protected Object getFileContent(String aPath) throws Exception
{
    // Get URL
    URL url = getClass().getResource(aPath); if(url==null) return null;
    
    // If directory, return emtpy list
    if(aPath.equals("/"))
        return Collections.emptyList();
        
    // Return bytes
    return URLUtils.getBytes(url);
}

}