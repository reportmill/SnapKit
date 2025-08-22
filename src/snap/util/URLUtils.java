/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for URL.
 */
public class URLUtils {

    /**
     * Returns the URL string for given object.
     */
    public static String getString(URL aURL)
    {
        // Get URL in normal form
        String urls = aURL.toExternalForm();
        //try { urls = URLDecoder.decode(urls, "UTF-8"); }
        //catch(Exception e) { }

        // If jar or wsjar, just strip it
        if (aURL.getProtocol().equals("jar"))
            urls = urls.substring(4);
        else if (aURL.getProtocol().equals("wsjar"))
            urls = urls.substring(6);
        return urls;
    }

    /** Returns a redirect string. */
    //public static String getRedirectString(String aURLString)
    //{ return "<meta http-equiv=\"refresh\" content=\"0; url=" + aURLString + "\">"; }

    /**
     * Tries to open the given URL with the platform reader.
     */
    public static void openURL(String aURL)
    {
        snap.gfx.GFXEnv.getEnv().openURL(aURL);
    }

    /**
     * Returns bytes for url.
     */
    public static byte[] getBytes(URL aURL) throws IOException
    {
        // If url is file, return bytes for file
        if (aURL.getProtocol().equals("file")) {
            File file = getFile(aURL); assert file != null;
            return FileUtils.getBytesOrThrow(file);
        }

        // Get connection (if desktop add User-Agent header because some servers need it)
        URLConnection urlConnection = aURL.openConnection();
        if (SnapEnv.isDesktop)
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (SnapKit)");

        // Return bytes
        try (InputStream inputStream = urlConnection.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Returns the last modified time of a URL.
     */
    public static long getLastModTime(URL aURL)
    {
        try {

            // Get connection (if desktop add User-Agent header because some servers need it)
            URLConnection urlConnection = aURL.openConnection();
            if (SnapEnv.isDesktop)
                urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (SnapKit)");

            // Return last mod time
            return urlConnection.getLastModified();
        }
        catch(IOException e) { return 0; }
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
        if (aURL.getProtocol().equals("jar")) {
            String urls = aURL.toExternalForm();
            int sep = urls.indexOf('!');
            urls = urls.substring(4, sep);
            try { aURL = new URL(urls); }
            catch(Exception e) { throw new RuntimeException(e); }
        }

        // Return file for URL
        String path = aURL.getPath();
        try { path = URLDecoder.decode(path, StandardCharsets.UTF_8); }
        catch(Exception e) { throw new RuntimeException(e); }
        return isLocal(aURL) ? new File(path) : null;
    }

    /**
     * Returns the given URL as a file (downloading to temp dir, if necessary).
     */
    public static File getLocalFile(URL aURL) throws IOException
    {
        return isLocal(aURL) ? getFile(aURL) : getLocalFile(aURL, null);
    }

    /**
     * Returns the given URL as given file by downloading it.
     */
    public static File getLocalFile(URL aURL, File aFile) throws IOException
    {
        // If "jar:" URL, recast as inner URL
        if (aURL.getProtocol().equals("jar")) {
            String urls = aURL.toExternalForm();
            int sep = urls.indexOf('!');
            urls = urls.substring(4, sep);
            aURL = new URL(urls);
        }

        // Get the destination file that URL should be saved to
        File localFile = getLocalFileDestination(aURL, aFile);

        // Create directories for this file
        localFile.getParentFile().mkdirs();

        // Get connection (if desktop add User-Agent header because some servers need it)
        URLConnection urlConnection = aURL.openConnection();
        if (SnapEnv.isDesktop)
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (SnapKit)");

        // Get last mod time - just return if local file exists and is newer than remote
        long lastModTime = urlConnection.getLastModified();
        if (localFile.exists() && lastModTime < localFile.lastModified())
            return localFile;

        // Get bytes and write to file
        try (InputStream inputStream = urlConnection.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            FileUtils.writeBytes(localFile, bytes);
        }

        // Return
        return localFile;
    }

    /**
     * Returns the destination file that the given URL would be saved to using the getLocalFile method.
     */
    public static File getLocalFileDestination(URL aURL, File aFile)
    {
        // If file is null, create from URL path in temp directory
        File file = aFile;
        if (file == null)
            file = new File(FileUtils.getTempDir(), aURL.getPath());

        // If file is directory, create from URL path file name in directory
        else if (file.isDirectory())
            file = new File(file, FilePathUtils.getFilename(aURL.getPath()));

        // Return file
        return file;
    }
}