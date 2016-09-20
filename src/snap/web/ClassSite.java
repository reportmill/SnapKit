/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.IOException;
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

protected List <FileHeader> getFileHeaders(String aPath)  { return Collections.emptyList(); }

protected byte[] getFileBytes(String aPath) throws IOException
{
    URL url = getClass().getResource(aPath); if(url==null) return null;
    return URLUtils.getBytes(url);
}

}