package snap.util;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Utilities for URL.
 */
public class URLUtils {

/**
 * Returns a URL for given string.
 */
public static URI getURI(URL aURL)
{
    try { return aURL.toURI(); }
    catch(URISyntaxException e) { throw new RuntimeException(e); }
}

/**
 * Returns a redirect string.
 */
public static String getRedirectString(String aURLString)
{
    return "<meta http-equiv=\"refresh\" content=\"0; url=" + aURLString + "\">";
}

/**
 * Tries to open the given URL with the platform reader.
 */
public static void openURL(String aURL)  { snap.gfx.GFXEnv.getEnv().openURL(aURL); }

/**
 * Returns bytes for url.
 */
public static byte[] getBytes(URL aURL) throws IOException
{
    // If url is file, return bytes for file
    if(aURL.getProtocol().equals("file"))
        return FileUtils.getBytes(getFile(aURL));
    
    // Get connection, stream, stream bytes, then close stream and return bytes
    URLConnection conn = aURL.openConnection();
    return getBytes(conn);
}

/**
 * Returns bytes for connection.
 */
private static byte[] getBytes(URLConnection aConnection) throws IOException
{
    InputStream stream = aConnection.getInputStream();  // Get stream for URL
    byte bytes[] = SnapUtils.getBytes2(stream);  // Get bytes for stream, close and return bytes
    stream.close();
    return bytes;
}

/**
 * Returns whether a URL is local.
 */
public static boolean isLocal(URL aURL)
{
    String url = aURL.toExternalForm().toLowerCase();
    return url.startsWith("file:") || url.startsWith("jar:file:");
}

/**
 * Returns the URL as a file.
 */
public static File getFile(URL aURL)
{
    // If "jar:" url, recast as inner URL
    if(aURL.getProtocol().equals("jar")) {
        String urls = aURL.toExternalForm(); int sep = urls.indexOf('!'); urls = urls.substring(4, sep);
        try { aURL = new URL(urls); } catch(Exception e) { throw new RuntimeException(e); }
    }
    
    // Return file for URL
    String path = aURL.getPath();
    try { path = URLDecoder.decode(path, "UTF-8"); }
    catch(Exception e) { throw new RuntimeException(e); }
    return isLocal(aURL)? new File(path) : null;
}

/**
 * Returns the given URL as a file (downloading to temp dir, if necessary).
 */
public static File getLocalFile(URL aURL) throws IOException
{
    return isLocal(aURL)? getFile(aURL) : getLocalFile(aURL, null);
}

/**
 * Returns the given URL as given file by downloading it.
 */
public static File getLocalFile(URL aURL, File aFile) throws IOException
{
    // If "jar:" URL, recast as inner URL
    if(aURL.getProtocol().equals("jar")) {
        String urls = aURL.toExternalForm(); int sep = urls.indexOf('!'); urls = urls.substring(4, sep);
        aURL = new URL(urls);
    }
    
    // Get the destination file that URL should be saved to
    File file = getLocalFileDestination(aURL, aFile);
    
    // Create directories for this file
    file.getParentFile().mkdirs();

    // Get URL connection and lastModified time
    URLConnection connection = aURL.openConnection();
    long lastModified = connection.getLastModified();
    
    // If local file doesn't exist or is older than URL, rewrite it
    if(!file.exists() || (lastModified>0 && file.lastModified()<lastModified)) {
        byte bytes[] = getBytes(connection);
        FileUtils.writeBytes(file, bytes);
    }
    
    // Return file
    return file;
}

/**
 * Returns the destination file that the given URL would be saved to using the getLocalFile method.
 */
public static File getLocalFileDestination(URL aURL, File aFile)
{
    // If file is null, create from URL path in temp directory
    File file = aFile;
    if(file==null)
        file = new File(FileUtils.getTempDir(), aURL.getPath());

    // If file is directory, create from URL path file name in directory
    else if(file.isDirectory())
        file = new File(file, StringUtils.getPathFileName(aURL.getPath()));

    // Return file
    return file;
}

/**
 * Send CGI Email.
 */
public static Exception sendCGIEmail(String aURLString, Map aMap)
{
    try { return sendCGIEmail(new URL(aURLString), aMap); }
    catch(Exception e) { return e; }
}

/**
 * Send CGI Email.
 */
public static Exception sendCGIEmail(URL aURL, Map aMap)
{
    // Send email with cgiemail utility available through verio (more reliable than SMPT).
    try {

        // Set keys user-email, user-name, user-comment, and exception represent (they are used in cgiemail template)
        StringBuffer buffer = new StringBuffer();
        for(Map.Entry entry : (Set<Map.Entry>)aMap.entrySet())
            buffer.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        if(aMap.size()>0)
            buffer.delete(buffer.length()-1, buffer.length());

        // open the connection and set it up for posting
        HttpURLConnection connection = (HttpURLConnection)aURL.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        
        // Get connection's output stream and write to it
        OutputStream os = new BufferedOutputStream(connection.getOutputStream());
        OutputStreamWriter osw = new OutputStreamWriter(os, "ASCII");
        osw.write(buffer.toString());
        osw.close();
        
        // Must read response to complete transaction (the post doesn't really happen until we do this)
        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        for(String resp=br.readLine(); resp!=null; resp=br.readLine()) { }
        br.close();
        return null;
    }
    
    // If email failed to send, run message dialog and print stack trace
    catch (Exception e) { return e; }
}

}