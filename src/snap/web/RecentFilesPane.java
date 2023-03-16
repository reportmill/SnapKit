/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.geom.Insets;
import snap.gfx.Color;
import snap.util.ArrayUtils;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.viewx.FilesPane;
import java.util.function.Consumer;

/**
 * A class to manage UI for recent files (can show a panel or a menu).
 */
public class RecentFilesPane extends FilesPane {

    // The FilesTable
    private TableView<WebFile>  _filesTable;

    /**
     * Constructor for given name.
     */
    public RecentFilesPane()
    {
        super();
        _site = RecentFilesSite.getShared();
    }

    /**
     * Initialize UI panel.
     */
    @Override
    protected void initUI()
    {
        // Configure FilesTable
        _filesTable = getView("FilesTable", TableView.class);
        _filesTable.getScrollView().setFillWidth(false);
        _filesTable.getScrollView().setBarSize(14);
        _filesTable.addEventFilter(e -> runLater(() -> filesTableMousePressReleased(e)), MousePress, MouseRelease);

        // Configure FilesTable columns
        TableCol<WebFile> nameCol = _filesTable.getCol(0);
        nameCol.setCellPadding(new Insets(4, 8, 4, 5));
        nameCol.setCellConfigure(cell -> configureFilesTableNameColCell(cell));
        TableCol<WebFile> pathCol = _filesTable.getCol(1);
        pathCol.setCellPadding(new Insets(4, 5, 4, 5));
        pathCol.setCellConfigure(cell -> configureFilesTablePathColCell(cell));

        // Init SelFile
        WebFile[] recentFiles = RecentFiles.getFiles();
        if (recentFiles.length > 0)
            setSelFile(recentFiles[0]);
    }

    /**
     * Resets UI.
     */
    @Override
    public void resetUI()
    {
        // Update FilesTable.Items/SelItem
        _filesTable.setSelItem(getSelFile());
    }

    /**
     * Responds to UI changes.
     */
    @Override
    public void respondUI(ViewEvent anEvent)
    {
        // Handle FilesTable
        if (anEvent.equals("FilesTable")) {
            WebFile newSelFile = (WebFile) anEvent.getSelItem();
            setSelFile(newSelFile);
        }

        // Handle ClearRecentMenuItem
        if (anEvent.equals("ClearRecentMenuItem"))
            RecentFiles.clearRecentFiles();
    }

    /**
     * Called to set the FilesPane WebFiles.
     */
    @Override
    protected void setSiteFilesInUI()
    {
        // Get RootDir and reset files to make sure we have latest RecentFiles
        WebSite recentFilesSite = getSite();
        WebFile rootDir = recentFilesSite.getRootDir();
        rootDir.resetContent();

        // Get valid files
        WebFile[] recentFiles = rootDir.getFiles();
        WebFile[] recentFilesValid = ArrayUtils.filter(recentFiles, file -> isValidFile(file));
        _filesTable.setItems(recentFilesValid);

        // Set SelFile
        WebFile recentFile = recentFilesValid.length > 0 ? recentFilesValid[0] : null;
        setSelFile(recentFile);
    }

    /**
     * Shows the RecentFiles.
     */
    public WebFile showPanel(View aView)
    {
        // Create DialogBox with UI, and showConfirmDialog (just return if cancelled)
        DialogBox dialogBox = new DialogBox("Recent Files");
        dialogBox.setContent(getUI());
        dialogBox.setOptions("Open", "Cancel");
        if (!dialogBox.showConfirmDialog(aView))
            return null;

        // Return selected file
        return getSelOrTargFile();
    }

    /**
     * Called to configure a FilesTable.ListCell for Name Column.
     */
    private void configureFilesTableNameColCell(ListCell<WebFile> aCell)
    {
        WebFile file = aCell.getItem();
        if (file == null)
            return;
        String filename = file.getName();
        aCell.setText(filename);
    }

    /**
     * Called to configure a FilesTable.ListCell for Path Column.
     */
    private void configureFilesTablePathColCell(ListCell<WebFile> aCell)
    {
        WebFile file = aCell.getItem();
        if (file == null)
            return;

        // Get URL string and set in cell
        WebFile recentFile = file.getRealFile();
        WebURL directoryURL = recentFile.getURL().getParent();
        String directoryUrlString = RecentFiles.getNormalizedUrlString(directoryURL.getString());
        aCell.setText(directoryUrlString);
        aCell.setTextFill(Color.DARKGRAY);

        // Add button to clear item from recent files
        CloseBox closeBox = new CloseBox();
        closeBox.setMargin(0, 4, 0, 4);
        closeBox.addEventHandler(e -> handleCloseBoxClicked(closeBox), View.Action);
        aCell.setGraphic(closeBox);
    }

    /**
     * Called when FilesTable gets MouseRelease.
     */
    private void filesTableMousePressReleased(ViewEvent anEvent)
    {
        // If double-click and valid file, do confirm
        if (anEvent.isMouseRelease() && anEvent.getClickCount() == 2)
            fireActionEvent(anEvent);

        // Handle content menu
        else if (anEvent.isPopupTrigger()) {

            // Create context menu
            ViewBuilder<MenuItem> menuBuilder = new ViewBuilder<>(MenuItem.class);
            menuBuilder.text("Clear Recent Files").save().addEventHandler(e -> {
                RecentFiles.clearRecentFiles();
                resetLater();
                resetFilesUI();
            }, View.Action);
            Menu contextMenu = menuBuilder.buildMenu("ContextMenu", null);
            contextMenu.show(anEvent.getView(), anEvent.getX(), anEvent.getY());
        }
    }

    /**
     * Called when FilesTable.ListCell close box is clicked.
     */
    private void handleCloseBoxClicked(View aView)
    {
        // Get FilesTable ListCell holding given view
        ListCell<?> listCell = aView.getParent(ListCell.class);
        if (listCell == null)
            return;

        // Get recent file for ListCell
        WebFile file = (WebFile) listCell.getItem();
        if (file == null)
            return;

        // Clear RecentFile
        WebURL fileURL = file.getURL();
        RecentFiles.removeURL(fileURL);

        // Clear RecentFiles, SelFile and trigger reset
        if (getSelFile() == file)
            setSelFile(null);

        // Reset UI
        resetLater();
        resetFilesUI();
    }

    /**
     * Override to maybe swap local file.
     */
    @Override
    public void setSelFile(WebFile aFile)
    {
        // If file is from foreign site, swap with local file
        WebFile localFile = getLocalFile(aFile);

        // Do normal version
        super.setSelFile(localFile);
    }

    /**
     * Returns a local file for given file.
     */
    private WebFile getLocalFile(WebFile aFile)
    {
        WebFile localFile = aFile;
        WebSite recentFilesSite = getSite();
        if (aFile != null && aFile.getSite() != recentFilesSite) {
            WebFile rootDir = recentFilesSite.getRootDir();
            WebFile[] localRecentFiles = rootDir.getFiles();
            localFile = ArrayUtils.findMatch(localRecentFiles, locFile -> locFile.getLinkFile() == aFile);
        }

        // Return
        return localFile;
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static String showPathsPanel(View aView)
    {
        RecentFilesPane recentFiles = new RecentFilesPane();
        WebFile file = recentFiles.showPanel(aView);
        return file != null ? file.getPath() : null;
    }

    /**
     * Shows a recent files menu for given view.
     */
    public static void showPathsMenu(View aView, Consumer<String> aFunc)
    {
        Menu menu = new Menu();
        WebFile[] recentFiles = RecentFiles.getFiles();
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
        ci.addEventHandler(e -> RecentFiles.clearRecentFiles(), Action);
        menu.addItem(ci);

        // Show menu
        menu.show(aView, 0, aView.getHeight());
    }
}