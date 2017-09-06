package snap.web;
import java.io.*;
import java.net.*;
import java.util.*;
import snap.util.FilePathUtils;

/**
 * A class to handle loading of URL items.
 */
public class WebGetter extends Object {

    // A map of existing WebSites
    static Map <WebURL, WebSite>  _sites = Collections.synchronizedMap(new HashMap());

/**
 * Returns a URL for given source.
 */
public static WebURL getURL(Object anObj)
{
    // Handle null, WebURL, WebFile
    if(anObj==null || anObj instanceof WebURL) return (WebURL)anObj;
    if(anObj instanceof WebFile) return ((WebFile)anObj).getURL();
    
    // Handle String; 
    if(anObj instanceof String) { String str = (String)anObj;
    
        // If it's our silly "Jar:/com/rm" format, return class resource URL
        if(str.startsWith("Jar:/com/reportmill")) 
            return getURL(WebURL.class.getResource(str.substring(4)));
            
        // If string is Windows/Unix file path, make it a file URL
        if(str.indexOf('\\')>=0) { String strlc = str.toLowerCase();
            str = str.replace('\\', '/'); if(!str.startsWith("/") || !strlc.startsWith("file:")) str = '/' + str; }
        if(str.startsWith("/")) str = "file://" + str;
        
        // Get protocol for URL
        int ind = str.indexOf(':'); if(ind<0) throw new RuntimeException("Missing protocol in URL: " + str);
        String scheme = str.substring(0, ind).toLowerCase();
            
        // Get URL for string
        try { 
            if(scheme.equals("class") || scheme.equals("local") || scheme.equals("git"))
                anObj = new URL(null, str, new BogusURLStreamHandler());
            else anObj = new URL(str);
        }
        catch(Exception e) { throw new RuntimeException(e); }
    }
    
    // Handle File: Convert to Canonical URL to normalize path
    if(anObj instanceof File) { File file = (File)anObj;
        try { anObj = file.getCanonicalFile().toURI().toURL(); } catch(Exception e) { } }
    
    // Handle URL: Get string, decode and strip "jar:" prefix if found (we don't use that)
    if(anObj instanceof URL) { URL url = (URL)anObj;
        String urls = url.toExternalForm(); try { urls = URLDecoder.decode(urls, "UTF-8"); } catch(Exception e) { }
        if(url.getProtocol().equals("jar")) urls = urls.substring(4);
        else if(url.getProtocol().equals("wsjar")) urls = urls.substring(6);
        return new WebURL(url, urls);
    }
    
    // Handle Class
    if(anObj instanceof Class) return getURL((Class)anObj, null);
    throw new RuntimeException("No URL found for: " + anObj);
}

/**
 * Returns a URL for given class and name/path string.
 */
public static WebURL getURL(Class aClass, String aName)
{
    // Get absolute path to class/resource
    String path = '/' + aClass.getName().replace('.', '/') + ".class";
    if(aName!=null) {
        if(aName.startsWith("/")) path = aName;
        else { int sep = path.lastIndexOf('/'); path = path.substring(0, sep+1) + aName; }
    }
    
    // If class loader is DataClassLoader, have it return URL
    //ClassLoader cl=aClass.getClassLoader();if(cl instanceof WebClassLoader) return((WebClassLoader)cl).getURL(path);
    
    // Get URL string for class and resource (decoded)
    URL url = aClass.getResource(path); if(url==null) return null;
    
    // Handle URL: Get string, decode and strip "jar:" prefix if found (we don't use that) and install path separator
    String urls = url.toExternalForm(); try { urls = URLDecoder.decode(urls, "UTF-8"); } catch(Exception e) { }
    if(url.getProtocol().equals("jar")) urls = urls.substring(4);
    else if(url.getProtocol().equals("wsjar")) urls = urls.substring(6);
    else urls = urls.replace(path, '!' + path);
    return new WebURL(url, urls);
}

/**
 * A URLStreamHandlerFactory.
 */
private static class BogusURLStreamHandler extends URLStreamHandler {
    protected URLConnection openConnection(URL u) throws IOException  { return null; }}

/**
 * Returns a site for given source URL.
 */
public static synchronized WebSite getSite(WebURL aSiteURL)
{
    WebSite site = _sites.get(aSiteURL);
    if(site==null) _sites.put(aSiteURL, site = createSite(aSiteURL));
    return site;
}

/**
 * Creates a site for given URL.
 */
protected static WebSite createSite(WebURL aSiteURL)
{
    WebURL parentSiteURL = aSiteURL.getSiteURL();
    String scheme = aSiteURL.getScheme(), path = aSiteURL.getPath(); if(path==null) path = "";
    String type = FilePathUtils.getExtension(path).toLowerCase();
    WebSite site = null;
    
    // If url has path, see if it's jar or zip
    if(type.equals("jar") || path.endsWith(".jar.pack.gz")) site = new JarFileSite();
    else if(type.equals("zip") || type.equals("gfar")) site = new ZipFileSite();
    else if(parentSiteURL!=null && parentSiteURL.getPath()!=null) site = new DirSite();
    else if(scheme.equals("file")) site = new FileSite();
    else if(scheme.equals("http") || scheme.equals("https")) site = new HTTPSite();
    else if(scheme.equals("ftp")) site = new FTPSite();
    else if(scheme.equals("class")) site = new ClassSite();
    else if(scheme.equals("local")) site = new LocalSite();
    if(site!=null) WebUtils.setSiteURL(site, aSiteURL);
    return site;
}

}