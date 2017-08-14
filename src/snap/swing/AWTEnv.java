package snap.swing;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A GFXEnv implementation using AWT.
 */
public class AWTEnv extends GFXEnv {
    
    // The shared AWTEnv
    static AWTEnv     _shared = new AWTEnv();

/**
 * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
 */
public String[] getFontNames()  { return AWTFontUtils.getFontNames(); }

/**
 * Returns a list of all system family names.
 */
public String[] getFamilyNames()  { return AWTFontUtils.getFamilyNames(); }

/**
 * Returns a list of all font names for a given family name.
 */
public String[] getFontNames(String aFamilyName)  { return AWTFontUtils.getFontNames(aFamilyName); }

/**
 * Returns a font file for given name.
 */
public FontFile getFontFile(String aName)  { return new AWTFontFile(aName); }

/**
 * Creates a new image from source.
 */
public Image getImage(Object aSource)  { return new J2DImage(aSource); }

/**
 * Creates a new image for width, height and alpha.
 */
public Image getImage(int aWidth, int aHeight, boolean hasAlpha)  { return new J2DImage(aWidth,aHeight,hasAlpha); }

/**
 * Returns a sound for given source.
 */
public SoundClip getSound(Object aSource)
{
    try {
        Class cls = Class.forName("snap.javafx.SoundData");
        return (SoundClip)cls.getDeclaredConstructor(Object.class).newInstance(aSource);
    }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Creates a sound for given source.
 */
public SoundClip createSound()
{
    try {
        Class cls = Class.forName("snap.javafx.SoundData");
        return (SoundClip)cls.newInstance();
    }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns a URL for given source.
 */
public WebURL getURL(Object anObj)
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
public WebURL getURL(Class aClass, String aName)
{
    // Get absolute path to class/resource
    String path = '/' + aClass.getName().replace('.', '/') + ".class";
    if(aName!=null) {
        if(aName.startsWith("/")) path = aName;
        else { int sep = path.lastIndexOf('/'); path = path.substring(0, sep+1) + aName; }
    }
    
    // If class loader is DataClassLoader, have it return URL
    ClassLoader cldr = aClass.getClassLoader();
    if(cldr instanceof WebClassLoader)
        return ((WebClassLoader)cldr).getURL(path);
    
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

// A map of existing WebSites
Map <WebURL, WebSite>  _sites = Collections.synchronizedMap(new HashMap());

/**
 * Returns a site for given source URL.
 */
public synchronized WebSite getSite(WebURL aSiteURL)
{
    WebSite site = _sites.get(aSiteURL);
    if(site==null) _sites.put(aSiteURL, site = createSite(aSiteURL));
    return site;
}

/**
 * Creates a site for given URL.
 */
protected WebSite createSite(WebURL aSiteURL)
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

/**
 * Tries to open the given file source with the platform reader.
 */
public void openFile(Object aSource)
{
    if(aSource instanceof WebFile) aSource = ((WebFile)aSource).getStandardFile();
    if(aSource instanceof WebURL) aSource = ((WebURL)aSource).getURL();
    File file = FileUtils.getFile(aSource);
    try { Desktop.getDesktop().open(file); return; }
    catch(Throwable e) { System.err.println(e.getMessage()); }
}

/**
 * Tries to open the given URL with the platform reader.
 */
public void openURL(Object aSource)
{
    WebURL url = WebURL.getURL(aSource);
    String urls = url!=null? url.getString() : null;
    try { Desktop.getDesktop().browse(new URI(urls)); return; } // RM13 has a pre-JVM 6 implementation
    catch(Throwable e) { System.err.println(e.getMessage()); }
}

/**
 * Returns the screen resolution.
 */
public double getScreenResolution()
{
    try { return Toolkit.getDefaultToolkit().getScreenResolution(); }
    catch(java.awt.HeadlessException he) { return 72; }
}

/**
 * Plays a beep.
 */
public void beep()  { Toolkit.getDefaultToolkit().beep(); }

/**
 * Override to return AWTPrefs for name.
 */
public Prefs getPrefs(String aName)  { return AWTPrefs.getPrefs(aName); }

/**
 * Sets this JVM to be headless.
 */
public void setHeadless()
{
    try { System.setProperty("java.awt.headless", "true"); }
    catch(Throwable e) { }
}

/**
 * Returns the current platform.
 */
public SnapUtils.Platform getPlatform()
{
    if(System.getProperty("os.name").indexOf("Windows") >= 0) return SnapUtils.Platform.WINDOWS;
    if(System.getProperty("os.name").indexOf("Mac OS X") >= 0) return SnapUtils.Platform.MAC;
    return SnapUtils.Platform.UNKNOWN;
}

/**
 * Returns a key value.
 */
public Object getKeyValue(Object anObj, String aKey)  { return Key.getValue(anObj, aKey); }

/**
 * Sets a key value.
 */
public void setKeyValue(Object anObj, String aKey, Object aValue)  { Key.setValueSafe(anObj, aKey, aValue); }

/**
 * Returns a key chain value.
 */
public Object getKeyChainValue(Object anObj, String aKeyChain)  { return KeyChain.getValue(anObj, aKeyChain); }

/**
 * Sets a key chain value.
 */
public void setKeyChainValue(Object anObj, String aKC, Object aValue)  { KeyChain.setValueSafe(anObj, aKC, aValue); }

/**
 * Returns a key list value.
 */
public Object getKeyListValue(Object anObj, String aKey, int anIndex) { return KeyList.getValue(anObj, aKey, anIndex); }

/**
 * Adds a key list value.
 */
public void setKeyListValue(Object anObj, String aKey, Object aValue, int anIndex)
{
    KeyList.setValue(anObj, aKey, aValue, anIndex);
}

/**
 * Returns a shared instance.
 */
public static AWTEnv get()  { return _shared; }

/**
 * Sets AWTEnv to be the default env.
 */
public static void set()  { GFXEnv.setEnv(get()); }

}