/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.view.*;
import snap.viewx.DialogBox;
import java.util.function.Consumer;

/**
 * A class to manage UI for recent files (can show a panel or a menu).
 */
public class RecentFilesPane extends ViewOwner {

    // The name
    private String  _name;

    // The DialogBox
    private DialogBox  _dialogBox;

    /**
     * Constructor for given name.
     */
    public RecentFilesPane(String aName)
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
        WebFile[] recentFiles = RecentFiles.getFiles(_name);
        setViewItems("FilesList", recentFiles);
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
            RecentFiles.clearPaths(_name);

        // Handle FilesList MouseClick
        if (anEvent.equals("FilesList") && anEvent.getClickCount() > 1)
            if (_dialogBox != null) _dialogBox.confirm();
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static String showPathsPanel(View aView, String aName)
    {
        RecentFilesPane recentFiles = new RecentFilesPane(aName);
        WebFile file = recentFiles.showPanel(aView);
        return file != null ? file.getPath() : null;
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static void showPathsMenu(View aView, String aName, Consumer<String> aFunc)
    {
        Menu menu = new Menu();
        WebFile[] recentFiles = RecentFiles.getFiles(aName);
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
        ci.addEventHandler(e -> RecentFiles.clearPaths(aName), Action);
        menu.addItem(ci);

        // Show menu
        menu.show(aView, 0, aView.getHeight());
    }
}