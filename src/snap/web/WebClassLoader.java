/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.io.InputStream;
import java.net.URL;

/**
 * A class loader for a WebSite.
 */
public class WebClassLoader extends ClassLoader {

    // A WebSite
    WebSite             _site;
    
/**
 * Creates a new DataClassLoader.
 */
public WebClassLoader(WebSite aSite)  { this(WebSite.class.getClassLoader(), aSite); }
    
/**
 * Creates a new DataClassLoader.
 */
public WebClassLoader(ClassLoader aParent, WebSite aSite)  { super(aParent); _site = aSite; }

/**
 * Returns the WebSite.
 */
public WebSite getSite()  { return _site; }

/**
 * Returns resource as string.
 */
public WebURL getURL(String aPath)
{
    // Check for build file
    WebFile file = getBuildFile(aPath);
    if(file!=null)
        return file.getURL();
    
    // Get URL string for class and resource (decoded)
    String path = aPath.startsWith("/")? aPath.substring(1) : aPath;
    URL url = super.getResource(path);
    return WebURL.getURL(url);
}

/**
 * Returns resource as string.
 */
public URL getResource(String aPath)
{
    // If build file found, return input stream for bytes
    String path = aPath; if(!path.startsWith("/")) path = '/' + path;
    WebFile file = getBuildFile(path);
    if(file!=null)
        return file.getURL().getURL();

    // Do normal version
    return super.getResource(aPath);
}

/**
 * Returns resource as string.
 */
public InputStream getResourceAsStream(String aPath)
{
    // If build file found, return input stream for bytes
    String path = aPath; if(!path.startsWith("/")) path = '/' + path;
    WebFile file = getBuildFile(path);
    if(file!=null)
        return file.getInputStream();

    // Do normal version
    return super.getResourceAsStream(aPath);
}

/**
 * Override to find class.
 */
protected Class<?> findClass(String aName) throws ClassNotFoundException
{
    // Try normal version
    try { return super.findClass(aName); }
    catch(ClassNotFoundException e) { }

    // If class is build file, define class
    String path = '/' + aName.replace('.', '/').concat(".class");
    WebFile cfile = getBuildFile(path);
    if(cfile!=null) {
        byte bytes[] = cfile.getBytes();
        return defineClass(aName, bytes, 0, bytes.length);
    }
    
    // Do normal version
    return super.findClass(aName);
}

/**
 * Returns a class file for given class file path.
 */
protected WebFile getBuildFile(String aPath)
{
    try { return _site.getFile(aPath); }
    catch(ResponseException e)  { return null; }
}

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + ": " + getSite().getURL().getString(); }

}