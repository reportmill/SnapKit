package snap.web;
import java.io.*;
import java.net.*;
import java.util.*;
import snap.util.FilePathUtils;

/**
 * A class to handle loading of URL items.
 */
public class WebGetter {

    // A map of existing WebSites
    static Map <WebURL, WebSite>  _sites = Collections.synchronizedMap(new HashMap());

/**
 * Returns a java.net.URL for given source.
 */
public static URL getJavaURL(Object anObj)
{
    // Handle String 
    if(anObj instanceof String) { String str = (String)anObj;
    
        // If it's our silly "Jar:/com/rm" format, return class resource URL
        if(str.startsWith("Jar:/com/reportmill")) 
            return WebURL.class.getResource(str.substring(4));
            
        // If string is Windows/Unix file path, make it a file URL
        if(str.indexOf('\\')>=0) { String strlc = str.toLowerCase();
            str = str.replace('\\', '/'); if(!str.startsWith("/") || !strlc.startsWith("file:")) str = '/' + str; }
        if(str.startsWith("/")) str = "file://" + str;
        
        // Get protocol for URL
        int ind = str.indexOf(':'); if(ind<0) throw new RuntimeException("Missing protocol in URL: " + str);
        String scheme = str.substring(0, ind).toLowerCase();
        
        // Try to return URL
        try { return new URL(str); }
        catch(Exception e) { }
        
        // Try to return URL with bogus stream handler
        try { return new URL(null, str, new BogusURLStreamHandler()); }
        catch(Exception e) { }
    }
    
    // Handle File: Convert to Canonical URL to normalize path
    if(anObj instanceof File) { File file = (File)anObj;
        try { return file.getCanonicalFile().toURI().toURL(); }
        catch(Exception e) { }
    }
    
    // Handle URL: Get string, decode and strip "jar:" prefix if found (we don't use that)
    if(anObj instanceof URL)
        return (URL)anObj;
    
    // Handle Class
    if(anObj instanceof Class)
        return getJavaURL((Class)anObj, null);
    
    // Complain
    throw new RuntimeException("No URL found for: " + anObj);
}

/**
 * Returns a URL for given class and name/path string.
 */
public static URL getJavaURL(Class aClass, String aName)
{
    // Get absolute path to class/resource
    String path = '/' + aClass.getName().replace('.', '/') + ".class";
    if(aName!=null) {
        if(aName.startsWith("/")) path = aName;
        else { int sep = path.lastIndexOf('/'); path = path.substring(0, sep+1) + aName; }
    }
    
    // Get URL for full path
    return aClass.getResource(path);
}

/**
 * Returns the URL string for given object.
 */
public static String getURLString(Object anObj)
{
    // Handle URL
    if(anObj instanceof URL) { URL url = (URL)anObj;
    
        // Get URL in normal form
        String urls = url.toExternalForm();
        try { urls = URLDecoder.decode(urls, "UTF-8"); } catch(Exception e) { }
        
        // If jar or wsjar, just strip it
        if(url.getProtocol().equals("jar")) urls = urls.substring(4);
        else if(url.getProtocol().equals("wsjar")) urls = urls.substring(6);
        return urls;
    }
    
    // Handle anything else
    return anObj.toString();
}

/**
 * Returns a site for given source URL.
 */
public static synchronized WebSite getSite(WebURL aSiteURL)
{
    // Get Site from map and return
    WebSite site = _sites.get(aSiteURL);
    if(site!=null)
        return site;
        
    // Otherwise, create site, set URL and return        
    site = createSite(aSiteURL);
    site.setURL(aSiteURL);
    return site;
}

/**
 * Sets a site for given source URL.
 */
protected static synchronized void setSite(WebURL aSiteURL, WebSite aSite)
{
    _sites.put(aSiteURL, aSite);
}

/**
 * Creates a site for given URL.
 */
protected static WebSite createSite(WebURL aSiteURL)
{
    // Get parentSiteURL, scheme, path and type
    WebURL parentSiteURL = aSiteURL.getSiteURL();
    String scheme = aSiteURL.getScheme();
    String path = aSiteURL.getPath(); if(path==null) path = "";
    String type = FilePathUtils.getExtension(path).toLowerCase();
    
    // Handle JarSite and ZipSite
    if(type.equals("jar") || path.endsWith(".jar.pack.gz")) return new JarFileSite();
    else if(type.equals("zip") || type.equals("gfar")) return new ZipFileSite();
    
    // Handle DirSite
    else if(parentSiteURL!=null && parentSiteURL.getPath()!=null) return new DirSite();
    
    // Handle FileSite
    else if(scheme.equals("file")) return new FileSite();
    
    // Handle HTTPSite
    else if(scheme.equals("http") || scheme.equals("https")) return new HTTPSite();

    // Handle LocalSite
    else if(scheme.equals("local")) return new LocalSite();
    
    // Return site
    System.err.println("WebGetter: Site not found for " + aSiteURL);
    return null;
}

/**
 * A URLStreamHandlerFactory.
 */
private static class BogusURLStreamHandler extends URLStreamHandler {
    protected URLConnection openConnection(URL u) throws IOException  { return null; }}

}