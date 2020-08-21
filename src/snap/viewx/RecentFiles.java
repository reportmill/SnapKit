package snap.viewx;
import java.util.*;
import java.util.function.Consumer;
import snap.util.Prefs;
import snap.view.*;
import snap.web.*;

/**
 * A class to manage UI for recent files (can show a panel or a menu).
 */
public class RecentFiles extends ViewOwner {
    
    // The name
    private String  _name;

    // The DialogBox
    private DialogBox  _dbox;
    
    /**
     * Creates a RecentFiles for given name.
     */
    public RecentFiles(String aName)  { _name = aName; }

    /**
     * Shows the RecentFiles.
     */
    public WebFile showFilesPanel(View aView)
    {
        // Create DialogBox with UI, and showConfirmDialog (just return if cancelled)
        _dbox = new DialogBox("Recent Files");
        _dbox.setContent(getUI());
        _dbox.setOptions("Open", "Cancel");
        if (!_dbox.showConfirmDialog(aView)) return null;

        // If not cancelled, return selected file
        WebFile file = (WebFile) getViewSelItem("FilesList");
        return file;
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        ListView lview = new ListView(); lview.setName("FilesList"); lview.setPrefSize(250,300);
        enableEvents(lview, MouseRelease);
        return lview;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        setViewItems("FilesList", getFiles(_name));
        getView("FilesList", ListView.class).setItemKey("Name");
        if (getViewSelIndex("FilesList")<0) setViewSelIndex("FilesList", 0);
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
        if (anEvent.equals("FilesList") && anEvent.getClickCount()>1)
            if (_dbox!=null) _dbox.confirm();
    }

    /**
     * Returns the list of recent paths for name.
     */
    public static List <String> getPaths(String aName)
    {
        // Get prefs for RecentDocuments (just return if missing)
        Prefs prefs = Prefs.get().getChild(aName);

        // Add to the list only if the file is around and readable
        List<String> list = new ArrayList<>();
        for (int i=0; ; i++) {
            String path = prefs.getString("index" + i, null); if (path==null) break;
            if (!list.contains(path))
                list.add(path);
        }

        // Return list
        return list;
    }

    /**
     * Adds a new file to the list and updates the users preferences.
     */
    public static void addPath(String aName, String aPath, int aMax)
    {
        // Get the doc list from the preferences
        List <String> paths = getPaths(aName);

        // Remove the path (if it was there) and add to front of list
        paths.remove(aPath); paths.add(0, aPath);

        // Add at most Max paths to the prefs list
        Prefs prefs = Prefs.get().getChild(aName);
        for (int i=0; i<paths.size() && i<aMax; i++)
            prefs.setValue("index" + i, paths.get(i));

        // Flush prefs
        try { prefs.flush(); } catch(Exception e)  { System.err.println(e); }
    }

    /**
     * Clears recent documents from preferences.
     */
    public static void clearPaths(String aName)  { Prefs.get().getChild(aName).clear(); }

    /**
     * Returns the list of the recent paths as WebFiles.
     */
    public static List <WebURL> getURLs(String aName)
    {
        // Get RecentPaths
        List <String> paths = getPaths(aName);
        List <WebURL> urls = new ArrayList<>();
        for (String path : paths) {
            WebURL url = WebURL.getURL(path);
            if (url!=null)
                urls.add(url);
        }
        return urls;
    }

    /**
     * Returns the list of the recent paths as WebFiles.
     */
    public static List <WebFile> getFiles(String aName)
    {
        // Get RecentPaths
        List <WebURL> urls = getURLs(aName);
        List <WebFile> files = new ArrayList<>();
        for (WebURL url : urls) {
            WebFile file = url.getFile();
            if (file!=null)
                files.add(file);
        }
        return files;
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static String showPathsPanel(View aView, String aName)
    {
        RecentFiles rf = new RecentFiles(aName);
        WebFile file = rf.showFilesPanel(aView);
        return file!=null ? file.getPath() : null;
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static void showPathsMenu(View aView, String aName, Consumer <String> aFunc)
    {
        Menu menu = new Menu();
        List <WebFile> files = getFiles(aName);
        for (WebFile file : files) {
            MenuItem mi = new MenuItem(); mi.setText(file.getName());
            mi.addEventHandler(e -> aFunc.accept(file.getPath()), Action);
            menu.addItem(mi);
        }

        // Add clear menu
        menu.addSeparator();
        MenuItem ci = new MenuItem(); ci.setText("Clear Recents");
        ci.addEventHandler(e -> clearPaths(aName), Action);
        menu.addItem(ci);

        // Show menu
        menu.show(aView, 0, aView.getHeight());
    }
}