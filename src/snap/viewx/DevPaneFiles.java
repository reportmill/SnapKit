package snap.viewx;
import snap.view.*;
import snap.web.*;

import java.util.Date;
import java.util.Objects;

/**
 * A DevPane tab for inspecting Graphics.
 */
public class DevPaneFiles extends ViewOwner {

    // The root URL string
    private String _rootUrlString;

    // The currently selected file
    protected WebFile  _selFile;

    // The FileBrowser
    private BrowserView<WebFile>  _fileBrowser;

    // Constants for properties
    public static final String SelFile_Prop = "SelFile";

    /**
     * Constructor.
     */
    public DevPaneFiles()
    {
        super();
        setRootUrlString("/");
    }

    /**
     * Returns the root dir url string.
     */
    public String getRootUrlString()  { return _rootUrlString; }

    /**
     * Sets the root dir url string.
     */
    public void setRootUrlString(String aString)
    {
        if (Objects.equals(aString, getRootUrlString())) return;
        _rootUrlString = aString;

        // Reset files
        resetFileBrowser();
    }

    /**
     * Returns the selected file.
     */
    public WebFile getSelFile()  { return _selFile; }

    /**
     * Sets the selected file or directory.
     */
    public void setSelFile(WebFile aFile)
    {
        // If already set, just return
        if (aFile == _selFile) return;

        // Fire prop changes
        firePropChange(SelFile_Prop, _selFile, _selFile = aFile);

        // Reset UI
        resetLater();
    }

    /**
     * Returns the selected directory.
     */
    public WebFile getSelDir()
    {
        WebFile selFile = getSelFile();
        if (selFile == null)
            return null;
        if (selFile.isDir())
            return selFile;
        return selFile.getParent();
    }

    /**
     * Returns the selected file info.
     */
    public String getSelFileInfoString()
    {
        WebFile selFile = getSelFile();
        if (selFile == null)
            return "";

        // Add size
        StringBuilder sb = new StringBuilder();
        if (selFile.isFile())
            sb.append("Size: ").append(selFile.getSize()).append(" bytes\n");
        else sb.append("File count: ").append(selFile.getFiles().length).append('\n');

        // Add last modified date
        Date lastModDate = selFile.getLastModDate();
        sb.append("Last Mod: ").append(lastModDate);

        // Return
        return sb.toString();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get FileBrowser and configure
        _fileBrowser = getView("FileBrowser", BrowserView.class);
        _fileBrowser.setRowHeight(22);
        _fileBrowser.setResolver(new WebSitePaneUtils.FileResolver());
        //_fileBrowser.setCellConfigure(item -> configureFileBrowserCell(item));
        _fileBrowser.addEventFilter(e -> runLater(() -> fileBrowserMouseReleased(e)), MouseRelease);

        // Config FileTextView.TextArea
        TextView textView = getView("FileTextView", TextView.class);
        TextArea textArea = textView.getTextArea();
        textArea.setPadding(4, 4, 4, 4);
    }

    @Override
    protected void initShowing()
    {
        resetFileBrowser();
        setSelFile(getHomeDir());
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update RootFileText
        setViewValue("RootFileText", _rootUrlString);

        // Update FileBrowser
        WebFile selFile = getSelFile();
        WebFile selFileOrDir = selFile != null ? selFile : getSelDir();
        _fileBrowser.setSelItem(selFileOrDir);

        // Update FileLabel
        String labelText = selFileOrDir != null ? (selFileOrDir.isDir() ? "Dir: " : "File: ") + selFileOrDir.getName() : "";
        setViewText("FileLabel", labelText);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle RootFileText
        if (anEvent.equals("RootFileText")) {
            _rootUrlString = anEvent.getStringValue();
            resetFileBrowser();
        }

        // Handle HomeButton
        if (anEvent.equals("HomeButton")) {
            setRootUrlString("/");
            setSelFile(getHomeDir());
        }

        // Handle DeleteButton
        if (anEvent.equals("DeleteButton"))
            deleteSelFile();

        // Handle FileBrowser
        if (anEvent.equals("FileBrowser")) {

            // Set sel file
            WebFile newSelFile = _fileBrowser.getSelItem();
            setSelFile(newSelFile);

            // Set FileTextView to sel file info
            String fileText = getSelFileInfoString();
            setViewValue("FileTextView", fileText);
        }
    }

    /**
     * Resets the file browser.
     */
    private void resetFileBrowser()
    {
        if (_fileBrowser == null) return;
        WebFile[] rootFiles = getRootFiles();
        _fileBrowser.setItems(rootFiles);
        setSelFile(null);
        resetLater();
    }

    /**
     * Called to delete selected file.
     */
    private void deleteSelFile()
    {
        WebFile selFile = getSelFile();
        String title = "Delete file: " + selFile.getName();
        String msg = "Are you sure you want to delete file: " + selFile.getName() + "?";
        boolean returnValue = DialogBox.showConfirmDialog(getUI(), title, msg);
        if (!returnValue)
            return;

        // Delete file
        WebFile parent = selFile.getParent();
        selFile.delete();
        parent.resetAndVerify();
        setSelFile(parent);
        _fileBrowser.updateItems(new WebFile[] { parent });
    }

    /**
     * Called when FileBrowser gets mouse released.
     */
    private void fileBrowserMouseReleased(ViewEvent anEvent)
    {
        // Handle double-click: Either show file text or change root
        if (anEvent.getClickCount() == 2) {

            // If no SelFile, just return
            WebFile selFile = getSelFile();
            if (selFile == null)
                return;

            // If zip or jar or alt-key down, set as root
            if (selFile.getType().equals("zip") || selFile.getType().equals("jar") || anEvent.isAltDown())
                setRootUrlString(selFile.getUrlString());

            // Otherwise open file text
            else if (selFile.isFile()) {
                String fileText = selFile.getText();
                setViewValue("FileTextView", fileText);
            }
        }
    }

    /**
     * Returns the root site.
     */
    private WebSite getRootSite()
    {
        if (_rootUrlString == null || _rootUrlString.length() == 0 || _rootUrlString.equals("/"))
            return getRootFileSite();

        WebURL rootUrl = WebURL.getURL(_rootUrlString);
        if (rootUrl == null)
            return getRootFileSite();
        return rootUrl.getAsSite();
    }

    /**
     * Returns the root files.
     */
    private WebFile[] getRootFiles()
    {
        WebSite rootSite = getRootSite();
        WebFile rootDir = rootSite != null ? rootSite.getRootDir() : null;
        return rootDir != null ? rootDir.getFiles() : new WebFile[0];
    }

    /**
     * Returns the root file site.
     */
    private WebSite getRootFileSite()
    {
        WebURL fileSiteURL = WebURL.getURL("/");
        assert (fileSiteURL != null);
        return fileSiteURL.getSite();
    }

    /**
     * Returns the home directory.
     */
    private WebFile getHomeDir()
    {
        String homeDirPath = System.getProperty("user.home");
        WebURL homeDirUrl = WebURL.getURL(homeDirPath);
        return homeDirUrl != null ? homeDirUrl.getFile() : null;
    }
}
