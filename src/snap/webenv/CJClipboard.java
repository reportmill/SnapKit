package snap.webenv;
import snap.gfx.Image;
import snap.util.ListUtils;
import snap.view.Clipboard;
import snap.view.ClipboardData;
import snap.view.ViewUtils;
import snap.webapi.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A snap Clipboard implementation for CJ.
 */
public class CJClipboard extends Clipboard {
    
    // The DataTransfer
    protected DataTransfer _dataTrans;

    // The runnable to call addAllDatas()
    private Runnable  _addAllDatasRun, ADD_ALL_DATAS_RUN = () -> addAllDataToClipboard();

    // Whether clipboard is loaded
    private boolean  _loaded;

    // A LoadListener to handle async browser clipboard
    private static Runnable  _loadListener;

    // The shared clipboard for system copy/paste
    private static CJClipboard  _shared;

    /**
     * Constructor.
     */
    public CJClipboard()
    {
        super();
    }

    /**
     * Returns the clipboard content.
     */
    protected boolean hasDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.hasDataImpl(aMimeType);

        // Handle FILE_LIST: Return true if at least one file
        if (aMimeType == FILE_LIST)
            return _dataTrans.getFileCount() > 0;

        // Forward to DataTrans
        return _dataTrans.hasType(aMimeType);
    }

    /**
     * Returns the clipboard content.
     */
    protected ClipboardData getDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.getDataImpl(aMimeType);

        // Handle Files
        if (aMimeType == FILE_LIST) {

            // Get files
            List<File> jsfiles = _dataTrans.getFiles();
            if (jsfiles == null)
                return null;

            // Iterate over jsFiles and create clipbard data
            List<ClipboardData> cfiles = ListUtils.map(jsfiles, jsfile -> new CJClipboardData(jsfile));

            // Return ClipboardData for files array
            return new ClipboardData(aMimeType, cfiles);
        }

        // Handle anything else (String data)
        Object data = _dataTrans.getData(aMimeType);
        return new ClipboardData(aMimeType, data);
    }

    /**
     * Adds clipboard content.
     */
    protected void addDataImpl(String aMimeType, ClipboardData aData)
    {
        // Do normal implementation to populate ClipboardDatas map
        super.addDataImpl(aMimeType, aData);

        // Handle DragDrop case
        if (_dataTrans != null) {

            // Handle string data
            if (aData.isString())
                _dataTrans.setData(aMimeType, aData.getString());
            else _dataTrans.setData(aMimeType, aData.getBytes());

            // Otherwise complain
            //else System.err.println("CJClipboard.addDataImpl: Unsupported data type: " + aMimeType + ", " + aData.getSource());
        }

        // Handle system clipboard copy: Wait till all types added, then update clipboard
        else {
            if (_addAllDatasRun == null)
                ViewUtils.runLater(_addAllDatasRun = ADD_ALL_DATAS_RUN);
        }
    }

    /**
     * Load datas into system clipboard
     */
    private void addAllDataToClipboard()
    {
        // Clear run
        _addAllDatasRun = null;

        // Get list of ClipbardData
        Map<String,ClipboardData> clipDataMap = getClipboardDatas();
        Collection<ClipboardData> clipboardDatas = clipDataMap.values();

        // Convert to JSArray of ClipboardItem
        ClipboardItem[] clipboardItems = ListUtils.mapNonNullToArray(clipboardDatas, cdata -> getClipboardItemForData(cdata), ClipboardItem.class);

        // Try to write items to clipboard
        try { snap.webapi.Clipboard.writeClipboardItems(clipboardItems); }
        catch (Exception e) { System.err.println("CJClipboard.addAllDataToClipboard failed: " + e); }

        // Clear datas
        clearData();
    }

    /**
     * Returns a ClipboardItem for given ClipboardData.
     */
    private ClipboardItem getClipboardItemForData(ClipboardData aData)
    {
        // Handle string
        if (aData.isString()) {
            String type = aData.getMIMEType();
            String string = aData.getString();
            return new ClipboardItem(type, string);
        }

        // Handle image
        if (aData.isImage()) {

            // Get image as PNG blob
            Image image = aData.getImage();
            byte[] bytes = image.getBytesPNG();
            Blob blob = new Blob(bytes, "image/png");

            // Get ClipboardItem array for blob
            return new ClipboardItem(blob);
        }

        // Handle anything else: Get type and bytes
        String type = aData.getMIMEType();
        byte[] bytes = aData.getBytes();

        // If valid, just wrap in ClipboardItem
        if (type != null && bytes != null && bytes.length > 0) {
            Blob blob = new Blob(bytes, type);
            return new ClipboardItem(blob);
        }

        // Complain and return null
        System.err.println("CJClipboard.getClipboardItemForClipboardData: Had problem with " + aData);
        return null;
    }

    /**
     * Starts the drag.
     */
    public void startDrag()  { System.err.println("CJClipboard.startDrag: Not implemented"); }

    /** Called to indicate that drop is accepted. */
    public void acceptDrag()  { System.err.println("CJClipboard.startDrag: Not implemented"); }

    /** Called to indicate that drop is complete. */
    public void dropComplete()  { System.err.println("CJClipboard.startDrag: Not implemented");  }

    /**
     * Override to clear DataTrans.
     */
    @Override
    public void clearData()
    {
        super.clearData();
        _dataTrans = null;
    }

    /**
     * Returns the shared CJClipboard.
     */
    public static CJClipboard get()
    {
        if (_shared != null) return _shared;
        return _shared = new CJClipboard();
    }

    /**
     * Returns whether clipboard is loaded.
     */
    public boolean isLoaded()  { return _loaded; }

    /**
     * Adds a callback to be triggered when resources loaded.
     * If Clipboard needs to be 'approved', get approved and call given load listener.
     */
    public void addLoadListener(Runnable aRun)
    {
        // Set LoadListener
        _loadListener = aRun;

        // Create new data transfer
        DataTransfer dataTransfer = WebEnv.get().newDataTransfer();

        // Read clipboard items
        ClipboardItem[] clipboardItems = snap.webapi.Clipboard.readClipboardItems();

        // Iterate over clipboard items and add to data transfer
        for (ClipboardItem clipboardItem : clipboardItems) {

            // Get clipboard item types
            String[] types = clipboardItem.getTypes();

            // Iterate over types and convert to blob and add to data transfer
            for (String type : types) {
                Blob blob = clipboardItem.getType(type);
                String blobStr = blob.getText();
                dataTransfer.setData(type, blobStr);
            }
        }

        // Set DataTransfer and trigger LoadListener
        _dataTrans = dataTransfer;
        notifyLoaded();
    }

    /**
     * Notify loaded.
     */
    private void notifyLoaded()
    {
        ViewUtils.runLater(() -> {
            _loaded = true;
            _loadListener.run();
            _loaded = false;
            _loadListener = null;
        });
    }
}