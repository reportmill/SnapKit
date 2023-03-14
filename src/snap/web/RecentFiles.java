/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;
import java.util.function.Consumer;
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
     * Constructor for given name.
     */
    public RecentFiles()
    {
        super();
    }

    /**
     * Returns the list of the recent paths as WebFiles.
     */
    public static WebURL[] getURLs()
    {
        // Get RecentPaths
        String[] paths = getPaths();
        List<WebURL> urls = new ArrayList<>();
        for (String path : paths) {
            WebURL url = WebURL.getURL(path);
            if (url != null)
                urls.add(url);
        }

        // Return array
        return urls.toArray(new WebURL[0]);
    }

    /**
     * Returns the list of recent paths for name.
     */
    public static String[] getPaths()
    {
        // Get prefs for RecentDocuments (just return if missing)
        Prefs prefsNode = getRecentFilesPrefsNode();

        // Add to the list only if the file is around and readable
        List<String> list = new ArrayList<>();
        for (int i = 0; ; i++) {
            String path = prefsNode.getString("index" + i, null);
            if (path == null)
                break;
            if (!list.contains(path))
                list.add(path);
        }

        // Return list
        return list.toArray(new String[0]);
    }

    /**
     * Adds a new file to the list and updates the users preferences.
     */
    public static void addPath(String aPath)
    {
        // Get the doc list from the preferences
        String[] paths = getPaths();

        // Remove the path (if it was there) and add to front of list
        paths = ArrayUtils.remove(paths, aPath);
        paths = ArrayUtils.add(paths, aPath, 0);

        // Add at most Max paths to the prefs list
        Prefs prefsNode = getRecentFilesPrefsNode();
        for (int i = 0; i < paths.length && i < MAX_FILES; i++)
            prefsNode.setValue("index" + i, paths[i]);

        // Flush prefs
        try { prefsNode.flush(); }
        catch (Exception e) { System.err.println(e.getMessage()); }
    }

    /**
     * Removes a file from the list and updates the users preferences.
     */
    public static void removePath(String aPath)
    {
        // Get the doc list from the preferences
        String[] paths = getPaths();

        // Remove the path (if it was there)
        paths = ArrayUtils.remove(paths, aPath);

        // Add at most Max paths to the prefs list
        Prefs prefsNode = getRecentFilesPrefsNode();
        for (int i = 0; i < paths.length; i++)
            prefsNode.setValue("index" + i, paths[i]);
        prefsNode.remove("index" + paths.length);

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
    }

    /**
     * Returns the list of the recent paths as WebFiles.
     */
    public static WebFile[] getFiles()
    {
        // Get RecentPaths
        WebURL[] urls = getURLs();
        List<WebFile> files = new ArrayList<>();
        for (WebURL url : urls) {
            WebFile file = url.getFile();
            if (file != null)
                files.add(file);
        }

        // Return as array
        return files.toArray(new WebFile[0]);
    }

    /**
     * Returns the prefs node used to store recent files.
     */
    private static Prefs getRecentFilesPrefsNode()  { return Prefs.getDefaultPrefs().getChild(_prefsKey); }

    /**
     * Shows a recent files menu for given view.
     */
    public static String showPathsPanel(View aView)
    {
        return RecentFilesPane.showPathsPanel(aView);
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static void showPathsMenu(View aView, Consumer<String> aFunc)
    {
        RecentFilesPane.showPathsMenu(aView, aFunc);
    }

    /**
     * Returns the most recent file for given type.
     */
    public static WebFile getRecentFileForType(String aType)
    {
        String recentFileUrlStr = Prefs.getDefaultPrefs().getString("MostRecentDocument." + aType);
        WebURL recentFileURL = recentFileUrlStr != null ? WebURL.getURL(recentFileUrlStr) : null;
        if (recentFileURL == null)
            return null;
        return recentFileURL.getFile();
    }

    /**
     * Sets the most recent file for given type.
     */
    public static void setRecentFileForType(String aType, WebFile recentFile)
    {
        WebURL recentFileURL = recentFile.getURL();
        String recentFileUrlStr = recentFileURL.getString();
        Prefs.getDefaultPrefs().setValue("MostRecentDocument." + aType, recentFileUrlStr);
        Prefs.getDefaultPrefs().flush();
    }

    /**
     * Sets the Recent files key.
     */
    public static void setPrefsKey(String aKey)  { _prefsKey = aKey; }
}