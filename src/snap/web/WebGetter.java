/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.gfx.GFXEnv;
import snap.util.FilePathUtils;
import snap.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to handle loading of URL items.
 */
public class WebGetter {

    // A map of existing WebSites
    static Map<WebURL,WebSite>  _sites = new HashMap<>();

    /**
     * Returns a java.net.URL for given source.
     */
    public static URL getJavaURL(Object anObj) // throws MalformedURLException, IOException, IllegalArgumentException
    {
        // Handle String
        if (anObj instanceof String)
            return getJavaUrlForString((String) anObj);

        // Handle File: Convert to Canonical URL to normalize path
        if (anObj instanceof File) {
            File file = (File) anObj;
            try { return file.getCanonicalFile().toURI().toURL(); }
            catch (IOException ignore) { }
        }

        // Handle URL: Get string, decode and strip "jar:" prefix if found (we don't use that)
        if (anObj instanceof URL)
            return (URL) anObj;

        // Handle Class
        if (anObj instanceof Class)
            return getJavaUrlForClass((Class<?>) anObj, null);

        // Complain
        throw new IllegalArgumentException("WebGetter.getJavaURL: No URL found for: " + anObj);
    }

    /**
     * Returns a java.net.URL for given source.
     */
    public static URL getJavaUrlForString(String urlString)
    {
        // If it's our silly "Jar:/com/rm" format, return class resource URL
        if (urlString.startsWith("Jar:/com/reportmill"))
            return getJavaUrlForClass(WebURL.class, urlString.substring(4));
        if (urlString.startsWith("Jar:/reportmill"))
            return getJavaUrlForClass(WebURL.class, urlString.substring(4));

        // If string is Windows file path, make it a file URL
        if (urlString.indexOf('\\') >= 0) {
            urlString = urlString.replace('\\', '/');
            if (!urlString.startsWith("/") && !StringUtils.startsWithIC(urlString, "file:"))
                urlString = '/' + urlString;
        }

        // If url string is path, add 'file://' scheme prefix
        if (urlString.startsWith("/"))
            urlString = "file://" + urlString;

        // If no protocol declared, complain
        boolean containsSchemeSeparator = urlString.contains(":");
        if (!containsSchemeSeparator)
            throw new RuntimeException("Missing protocol in URL: " + urlString);

        // Try to return URL
        try { return new URL(urlString); }
        catch (MalformedURLException ignore) { }

        // Try to return URL with bogus stream handler
        try { return new URL(null, urlString, new BogusURLStreamHandler()); }
        catch (IOException ignore) { }

        // Complain
        throw new IllegalArgumentException("WebGetter.getJavaUrlForString: No URL found for: " + urlString);
    }

    /**
     * Returns a URL for given class and name/path string.
     */
    public static URL getJavaUrlForClass(Class<?> aClass, String aName)
    {
        // Get absolute path to class/resource
        String className = aClass.getName();
        String classPath = '/' + className.replace('.', '/');
        String resourcePath = classPath + ".class";
        if (aName != null) {
            if (aName.startsWith("/"))
                resourcePath = aName;
            else {
                int sep = resourcePath.lastIndexOf('/');
                resourcePath = resourcePath.substring(0, sep + 1) + aName;
            }
        }

        // Get URL for full path
        GFXEnv env = GFXEnv.getEnv();
        return env.getResource(aClass, resourcePath);
    }

    /**
     * Returns a site for given source URL.
     */
    public static synchronized WebSite getSite(WebURL aSiteURL)
    {
        // Get Site from map and return
        WebSite site = _sites.get(aSiteURL);
        if (site != null)
            return site;

        // If File URL and nested (contains '!' path separator), flatten URL and use that to avoid unnecessary dir references
        if (aSiteURL.getScheme().equals("file") && aSiteURL.getPath().contains("!")) {
            String flatSiteAddress = aSiteURL.getString().replace("!", "");
            WebURL flatSiteURL = WebURL.getURL(flatSiteAddress); assert (flatSiteURL != null);
            WebSite flatSite = flatSiteURL.getAsSite();
            setSite(aSiteURL, flatSite);
            return flatSite;
        }

        // Otherwise, create site, set URL and return
        site = createSiteForURL(aSiteURL);
        if (site != null)
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
    protected static WebSite createSiteForURL(WebURL aSiteURL)
    {
        // Get scheme, path and type
        String scheme = aSiteURL.getScheme();
        String sitePath = aSiteURL.getPath();
        String fileType = FilePathUtils.getExtension(sitePath).toLowerCase();

        // Try platform env
        WebSite site = GFXEnv.getEnv().createSiteForURL(aSiteURL);
        if (site != null)
            return site;

        // Handle ZipSite and JarSite
        if (fileType.equals("zip") || fileType.equals("jar") || fileType.equals("gfar"))
            return new ZipFileSite();

        // Handle DirSite
        WebURL parentSiteURL = aSiteURL.getSiteURL();
        String parentSitePath = parentSiteURL != null ? parentSiteURL.getPath() : "";
        if (!parentSitePath.isEmpty())
            return new DirSite();

        // Handle FileSite: If no site path, use FileSite, otherwise use DirSite
        if (scheme.equals("file")) {
            if (sitePath.isEmpty())
                return new FileSite();
            return new DirSite();
        }

        // Handle HTTPSite
        if (scheme.equals("http") || scheme.equals("https"))
            return new HTTPSite();

        // Handle DirSite
        if (!sitePath.isEmpty() && aSiteURL.getSite() != null)
            return new DirSite();

        // Return site
        System.err.println("WebGetter: Site not found for " + aSiteURL);
        return null;
    }

    /**
     * A URLStreamHandlerFactory.
     */
    private static class BogusURLStreamHandler extends URLStreamHandler {
        protected URLConnection openConnection(URL u)
        {
            return null;
        }
    }
}