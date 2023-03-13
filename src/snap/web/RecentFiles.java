/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import java.util.*;
import java.util.function.Consumer;
import snap.util.ArrayUtils;
import snap.util.Prefs;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A class to manage UI for recent files (can show a panel or a menu).
 */
public class RecentFiles extends ViewOwner {

    // The name
    private String  _name;

    // The DialogBox
    private DialogBox  _dialogBox;

    /**
     * Constructor for given name.
     */
    public RecentFiles(String aName)
    {
        super();
        _name = aName;
    }

    /**
     * Shows the RecentFiles.
     */
    public WebFile showPanel(View aView)
    {
        // Create DialogBox with UI, and showConfirmDialog (just return if cancelled)
        _dialogBox = new DialogBox("Recent Files");
        _dialogBox.setContent(getUI());
        _dialogBox.setOptions("Open", "Cancel");
        if (!_dialogBox.showConfirmDialog(aView)) return null;

        // If not cancelled, return selected file
        WebFile file = (WebFile) getViewSelItem("FilesList");
        return file;
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        ListView<WebFile> listView = new ListView<>();
        listView.setName("FilesList");
        listView.setPrefSize(250, 300);
        enableEvents(listView, MouseRelease);
        return listView;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        setViewItems("FilesList", getFiles(_name));
        getView("FilesList", ListView.class).setItemKey("Name");
        if (getViewSelIndex("FilesList") < 0)
            setViewSelIndex("FilesList", 0);
    }

    /**
     * Respond to any selection from the RecentFiles menu
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle ClearRecentMenuItem
        if (anEvent.equals("ClearRecentMenuItem"))
            clearPaths(_name);

        // Handle FilesList MouseClick
        if (anEvent.equals("FilesList") && anEvent.getClickCount() > 1)
            if (_dialogBox != null) _dialogBox.confirm();
    }

    /**
     * Returns the list of recent paths for name.
     */
    public static String[] getPaths(String aName)
    {
        // Get prefs for RecentDocuments (just return if missing)
        Prefs prefs = Prefs.getDefaultPrefs().getChild(aName);

        // Add to the list only if the file is around and readable
        List<String> list = new ArrayList<>();
        for (int i = 0; ; i++) {
            String path = prefs.getString("index" + i, null);
            if (path == null) break;
            if (!list.contains(path))
                list.add(path);
        }

        // Return list
        return list.toArray(new String[0]);
    }

    /**
     * Adds a new file to the list and updates the users preferences.
     */
    public static void addPath(String aName, String aPath, int aMax)
    {
        // Get the doc list from the preferences
        String[] paths = getPaths(aName);

        // Remove the path (if it was there) and add to front of list
        paths = ArrayUtils.remove(paths, aPath);
        paths = ArrayUtils.add(paths, aPath, 0);

        // Add at most Max paths to the prefs list
        Prefs prefs = Prefs.getDefaultPrefs().getChild(aName);
        for (int i = 0; i < paths.length && i < aMax; i++)
            prefs.setValue("index" + i, paths[i]);

        // Flush prefs
        try { prefs.flush(); }
        catch (Exception e) { System.err.println(e.getMessage()); }
    }

    /**
     * Removes a file from the list and updates the users preferences.
     */
    public static void removePath(String aName, String aPath)
    {
        // Get the doc list from the preferences
        String[] paths = getPaths(aName);

        // Remove the path (if it was there)
        paths = ArrayUtils.remove(paths, aPath);

        // Add at most Max paths to the prefs list
        Prefs prefs = Prefs.getDefaultPrefs().getChild(aName);
        for (int i = 0; i < paths.length; i++)
            prefs.setValue("index" + i, paths[i]);
        prefs.remove("index" + paths.length);

        // Flush prefs
        try { prefs.flush(); }
        catch (Exception e) { System.err.println(e.getMessage()); }
    }

    /**
     * Clears recent documents from preferences.
     */
    public static void clearPaths(String aName)
    {
        Prefs.getDefaultPrefs().getChild(aName).clear();
    }

    /**
     * Returns the list of the recent paths as WebFiles.
     */
    public static WebURL[] getURLs(String aName)
    {
        // Get RecentPaths
        String[] paths = getPaths(aName);
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
     * Returns the list of the recent paths as WebFiles.
     */
    public static WebFile[] getFiles(String aName)
    {
        // Get RecentPaths
        WebURL[] urls = getURLs(aName);
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
     * Shows a recent files menu for given view.
     */
    public static String showPathsPanel(View aView, String aName)
    {
        RecentFiles recentFiles = new RecentFiles(aName);
        WebFile file = recentFiles.showPanel(aView);
        return file != null ? file.getPath() : null;
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static void showPathsMenu(View aView, String aName, Consumer<String> aFunc)
    {
        Menu menu = new Menu();
        WebFile[] recentFiles = getFiles(aName);
        for (WebFile recentFile : recentFiles) {
            MenuItem menuItem = new MenuItem();
            menuItem.setText(recentFile.getName());
            menuItem.addEventHandler(e -> aFunc.accept(recentFile.getPath()), Action);
            menu.addItem(menuItem);
        }

        // Add clear menu
        menu.addSeparator();
        MenuItem ci = new MenuItem();
        ci.setText("Clear Recents");
        ci.addEventHandler(e -> clearPaths(aName), Action);
        menu.addItem(ci);

        // Show menu
        menu.show(aView, 0, aView.getHeight());
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
     * Clears recent documents from preferences.
     */
    public static void clearRecentFiles()
    {
        Prefs.getDefaultPrefs().getChild("RecentDocuments").clear();
    }
}