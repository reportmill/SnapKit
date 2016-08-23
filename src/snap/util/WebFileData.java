package snap.util;
import java.io.*;
import snap.web.*;

/**
 * Represents the data (from a file) 
 */
public class WebFileData {

    // The source of the data
    Object           _source;
    
    // The URL for the source of this data
    WebURL           _url;

/**
 * Returns the source.
 */
public Object getSource()  { return _source; }

/**
 * Sets the source.
 */
protected void setSource(Object aSource)  { _source = aSource; }

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return _url!=null? _url : (_url=createSourceURL()); }

/**
 * Creates the source URL from source if possible.
 */
protected WebURL createSourceURL()  { try { return WebURL.getURL(_source); } catch(Exception e) { return null; } }

/**
 * Returns the source file.
 */
public WebFile getSourceFile()  { WebURL url = getSourceURL(); return url!=null? url.getFile() : null; }

/**
 * Returns the site for this data.
 */
public WebSite getSite()  { WebURL url = getSourceURL(); return url!=null? url.getSite() : null; }

/**
 * Returns the bytes.
 */
public byte[] getBytes()  { return getSourceFile().getBytes(); }

/**
 * Sets the bytes.
 */
public void setBytes(byte theBytes[])  { getSourceFile().setBytes(theBytes); }

/**
 * Returns an input stream for the data.
 */
public InputStream getInputStream()  { return getSourceFile().getInputStream(); }

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + ' ' + getSourceURL(); }

/**
 * Returns whether given file is of given data type.
 */
public static boolean is(WebFile aFile, Class aClass)
{
    try { return (Boolean)aClass.getMethod("is", WebFile.class).invoke(null, aFile); }
    catch(Exception e) { System.out.println("SnapData: No is method for " + aClass); return false; }
}

/**
 * Returns the data for given file as given SnapData subclass.
 */
public synchronized static <T extends WebFileData> T get(WebFile aFile, Class <T> aClass)
{
    T data = (T)aFile.getProp(aClass.getName());
    if(data==null) {
        try { data = aClass.newInstance(); } catch(Exception e) { throw new RuntimeException(e); }
        data.setSource(aFile); aFile.setProp(aClass.getName(), data);
    }
    return data;
}

}