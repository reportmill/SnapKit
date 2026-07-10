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

    // The runnable to call writeToSystemClipboard()
    private Runnable _writeToSystemClipboardRun;

    // Whether clipboard is loaded
    private boolean _loaded;

    // The shared clipboard for system copy/paste
    private static CJClipboard _shared;

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
    @Override
    public boolean hasDataForMimeType(String mimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.hasDataForMimeType(mimeType);

        // Handle FILE_LIST: Return true if at least one file
        if (mimeType == FILE_LIST)
            return _dataTrans.getFileCount() > 0;

        // Forward to DataTrans
        return _dataTrans.hasType(mimeType);
    }

    /**
     * Returns the clipboard content.
     */
    @Override
    public ClipboardData getDataForMimeType(String mimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.getDataForMimeType(mimeType);

        // Handle Files
        if (mimeType == FILE_LIST) {

            // Get files
            List<File> jsfiles = _dataTrans.getFiles();
            if (jsfiles == null)
                return null;

            // Iterate over jsFiles and create clipbard data
            List<ClipboardData> cfiles = ListUtils.map(jsfiles, CJClipboardData::new);

            // Return ClipboardData for files array
            return new ClipboardData(cfiles, mimeType);
        }

        // Handle anything else (String data)
        Object data = _dataTrans.getData(mimeType);
        return new ClipboardData(data, mimeType);
    }

    /**
     * Adds clipboard content.
     */
    @Override
    protected void addDataForMimeTypeImpl(ClipboardData aData, String aMimeType)
    {
        // Do normal implementation to populate ClipboardDatas map
        super.addDataForMimeTypeImpl(aData, aMimeType);

        // Handle DragDrop case
        if (_dataTrans != null) {
            if (aData.isString())
                _dataTrans.setData(aMimeType, aData.getString());
            else _dataTrans.setData(aMimeType, aData.getBytes());
        }

        // Handle system clipboard copy: Wait till all types added, then update clipboard
        else {
            if (_writeToSystemClipboardRun == null)
                ViewUtils.runLater(_writeToSystemClipboardRun = this::writeToSystemClipboard);
        }
    }

    /**
     * Writes this clipboard to system clipboard
     */
    private void writeToSystemClipboard()
    {
        // Clear run
        _writeToSystemClipboardRun = null;

        // Get list of ClipbardData
        Map<String,ClipboardData> clipboardDatasMap = getClipboardDatas();
        Collection<ClipboardData> clipboardDatas = clipboardDatasMap.values();

        // Convert to list of ClipboardItem
        List<ClipboardItem> clipboardItems = ListUtils.mapNonNull(clipboardDatas, CJClipboard::convertClipboardDataToClipboardItem);

        // Try to write items to clipboard
        try { snap.webapi.Clipboard.writeClipboardItems(clipboardItems); }
        catch (Exception e) { System.err.println("CJClipboard.addAllDataToClipboard failed: " + e); }

        // Clear datas
        clearData();
    }

    /**
     * Returns a ClipboardItem for given ClipboardData.
     */
    private static ClipboardItem convertClipboardDataToClipboardItem(ClipboardData clipboardData)
    {
        // Handle string: Return clipboard item of string
        if (clipboardData.isString()) {
            String mimeType = clipboardData.getMimeType();
            String string = clipboardData.getString();
            return new ClipboardItem(mimeType, string);
        }

        // Handle image: Return clipboard item of image as PNG blob
        if (clipboardData.isImage()) {
            Image image = clipboardData.getImage();
            byte[] bytes = image.getBytesPNG();
            Blob blob = new Blob(bytes, "image/png");
            return new ClipboardItem(blob);
        }

        // Handle anything else: Get type and bytes
        String mimeType = clipboardData.getMimeType();
        byte[] dataBytes = clipboardData.getBytes();

        // If valid, just wrap in ClipboardItem
        if (mimeType != null && dataBytes != null && dataBytes.length > 0) {
            Blob blob = new Blob(dataBytes, mimeType);
            return new ClipboardItem(blob);
        }

        // Complain and return null
        System.err.println("CJClipboard.convertClipboardDataToClipboardItem: Had problem with " + clipboardData);
        return null;
    }

    /**
     * Starts the drag.
     */
    public void startDrag()  { System.err.println("CJClipboard.startDrag: Not implemented"); }

    /** Called to indicate that drop is accepted. */
    public void acceptDrag()  { }

    /** Called to indicate that drop is complete. */
    public void dropComplete()  { }

    /**
     * Override to clear DataTrans.
     */
    @Override
    protected void clearData()
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
    @Override
    public void addLoadListener(Runnable loadLsnr)
    {
        // Create new data transfer
        _dataTrans = getDataTransferForSystemClipboard();

        // Call load listener
        ViewUtils.runLater(() -> {
            _loaded = true;
            loadLsnr.run();
            _loaded = false;
            _dataTrans = null;
        });
    }

    /**
     * Returns the data transfer for system clipboard.
     */
    private static DataTransfer getDataTransferForSystemClipboard()
    {
        DataTransfer dataTransfer = WebEnv.get().newDataTransfer();

        // Read clipboard items
        List<ClipboardItem> clipboardItems = snap.webapi.Clipboard.readClipboardItems();

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

        return dataTransfer;
    }
}