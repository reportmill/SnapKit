/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;
import snap.util.ListUtils;
import snap.util.Prefs;

/**
 * A class to manage UI for recent files (can show a panel or a menu).
 */
public class RecentFiles {

    // The key used in preferences to store recent file urls
    private static String _prefsKey = "RecentDocuments";

    // Constants
    private static int MAX_FILES = 20;

    /**
     * Returns the array of recent files.
     */
    public static List<WebFile> getFiles()
    {
        List<WebURL> urls = getUrls();
        return ListUtils.mapNonNull(urls, url -> createFileForURL(url));
    }

    /**
     * Returns the array of recent file URLs.
     */
    public static List<WebURL> getUrls()
    {
        List<String> urlStrings = getUrlStrings();
        return ListUtils.mapNonNull(urlStrings, urlStr -> WebURL.getUrl(urlStr));
    }

    /**
     * Returns the array of recent file URL strings.
     */
    public static List<String> getUrlStrings()  { return Prefs.getDefaultPrefs().getStringsForKey(_prefsKey); }

    /**
     * Adds a URL string to recent files and updates the users preferences.
     */
    public static void addUrlString(String urlString)
    {
        // Standardize URL
        urlString = getNormalizedUrlString(urlString);

        // Get URL strings from prefs - just return if already first
        List<String> urlStrings = getUrlStrings();
        if (!urlStrings.isEmpty() && urlStrings.get(0).equals(urlString))
            return;

        // Remove the URL (if it was there) and add to front of list, and trim length to MAX_FILES
        urlStrings = new ArrayList<>(urlStrings);
        urlStrings.remove(urlString);
        urlStrings.add(0, urlString);
        if (urlStrings.size() > MAX_FILES)
            urlStrings = urlStrings.subList(0, MAX_FILES);

        // Set strings
        Prefs.getDefaultPrefs().setStringsForKey(urlStrings, _prefsKey);
    }

    /**
     * Removes a URL string from recent files and updates the users preferences.
     */
    public static void removeUrlString(String urlString)
    {
        // Standardize URL
        urlString = getNormalizedUrlString(urlString);

        // Get URL strings from prefs and remove url
        List<String> urlStrings = getUrlStrings();
        urlStrings = new ArrayList<>(urlStrings);
        urlStrings.remove(urlString);

        // Set strings
        Prefs.getDefaultPrefs().setStringsForKey(urlStrings, _prefsKey);
    }

    /**
     * Clears recent files from preferences.
     */
    public static void clearRecentFiles()  { Prefs.getDefaultPrefs().clearStringsForKey(_prefsKey); }

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
     * Creates a file for given URL to avoid connection.
     */
    private static WebFile createFileForURL(WebURL aURL)
    {
        boolean isDir = aURL.getFileType().isEmpty();
        if (aURL.getSite() == null)
            return null;
        return aURL.createFile(isDir);
    }

    /**
     * Sets the Recent files key.
     */
    public static void setPrefsKey(String aKey)  { _prefsKey = aKey; }
}