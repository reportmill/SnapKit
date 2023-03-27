/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;
import snap.util.ArrayUtils;
import snap.util.Prefs;
import snap.view.*;

/**
 * A class to manage UI for recent files (can show a panel or a menu).
 */
public class RecentFiles extends ViewOwner {

    // The key used in preferences to store recent file urls
    private static String  _prefsKey = "RecentDocuments";

    // Constants
    private static int MAX_FILES = 20;

    /**
     * Returns the array of recent files.
     */
    public static WebFile[] getFiles()
    {
        WebURL[] urls = getURLs();
        return ArrayUtils.mapNonNull(urls, url -> url.getFile(), WebFile.class);
    }

    /**
     * Returns the array of recent file URLs.
     */
    public static WebURL[] getURLs()
    {
        String[] urlStrings = getUrlStrings();
        return ArrayUtils.mapNonNull(urlStrings, urlStr -> WebURL.getURL(urlStr),WebURL.class);
    }

    /**
     * Returns the array of recent file URL strings.
     */
    public static String[] getUrlStrings()
    {
        // Get prefs for RecentDocuments (just return if missing)
        Prefs prefsNode = getRecentFilesPrefsNode();

        // Add to the list only if the file is around and readable
        List<String> urlStrings = new ArrayList<>();
        for (int i = 0; ; i++) {
            String urlString = prefsNode.getString("index" + i, null);
            if (urlString == null)
                break;
            if (!urlStrings.contains(urlString))
                urlStrings.add(urlString);
        }

        // Return list
        return urlStrings.toArray(new String[0]);
    }

    /**
     * Adds a URL string to recent files and updates the users preferences.
     */
    public static void addUrlString(String urlString)
    {
        // Standardize URL
        urlString = getNormalizedUrlString(urlString);

        // Get URL strings from prefs
        String[] urlStrings = getUrlStrings();

        // If already first, just return
        if (urlStrings.length > 0 && urlStrings[0].equals(urlString))
            return;

        // Remove the URL (if it was there) and add to front of list
        urlStrings = ArrayUtils.remove(urlStrings, urlString);
        urlStrings = ArrayUtils.add(urlStrings, urlString, 0);

        // Add at most Max paths to the prefs list
        Prefs prefsNode = getRecentFilesPrefsNode();
        for (int i = 0; i < urlStrings.length && i < MAX_FILES; i++)
            prefsNode.setValue("index" + i, urlStrings[i]);

        // Flush prefs
        try { prefsNode.flush(); }
        catch (Exception e) { System.err.println(e.getMessage()); }
    }

    /**
     * Removes a URL string from recent files and updates the users preferences.
     */
    public static void removeUrlString(String urlString)
    {
        // Standardize URL
        urlString = getNormalizedUrlString(urlString);

        // Get URL strings from prefs
        String[] urlStrings = getUrlStrings();

        // Remove the URL (if it was there)
        urlStrings = ArrayUtils.remove(urlStrings, urlString);

        // Add at most Max paths to the prefs list
        Prefs prefsNode = getRecentFilesPrefsNode();
        for (int i = 0; i < urlStrings.length; i++)
            prefsNode.setValue("index" + i, urlStrings[i]);

        // Remove old end index
        prefsNode.remove("index" + urlStrings.length);

        // Flush prefs
        try { prefsNode.flush(); }
        catch (Exception e) { System.err.println(e.getMessage()); }
    }

    /**
     * Clears recent files from preferences.
     */
    public static void clearRecentFiles()
    {
        Prefs prefsNode = getRecentFilesPrefsNode();
        prefsNode.clear();

        // Flush prefs
        try { prefsNode.flush(); }
        catch (Exception e) { System.err.println(e.getMessage()); }
    }

    /**
     * Adds a URL string to recent files and updates the users preferences.
     */
    public static void addURL(WebURL aURL)
    {
        String urlString = aURL.getString();
        addUrlString(urlString);
    }

    /**
     * Removes a URL string from recent files and updates the users preferences.
     */
    public static void removeURL(WebURL aURL)
    {
        String urlString = aURL.getString();
        removeUrlString(urlString);
    }

    /**
     * Returns the most recent file for given type.
     */
    public static WebURL[] getRecentUrlsForTypes(String[] theTypes)
    {
        // Get URL String for type
        WebURL[] recentUrls = getURLs();
        return ArrayUtils.filter(recentUrls, url -> ArrayUtils.contains(theTypes, url.getType()));
    }

    /**
     * Returns a standard URL String for given URL string.
     */
    public static String getNormalizedUrlString(String urlString)
    {
        // If URL String is 'file:/', replace with path
        if (urlString.startsWith("file:")) {
            urlString = urlString.replace("file:", "");
            if (urlString.startsWith("//"))
                urlString = urlString.substring(1);
        }

        // Remove nested site separator
        if (urlString.contains("!"))
            urlString = urlString.replace("!", "");

        // Return
        return urlString;
    }

    /**
     * Returns the prefs node used to store recent files.
     */
    private static Prefs getRecentFilesPrefsNode()  { return Prefs.getDefaultPrefs().getChild(_prefsKey); }

    /**
     * Sets the Recent files key.
     */
    public static void setPrefsKey(String aKey)  { _prefsKey = aKey; }
}