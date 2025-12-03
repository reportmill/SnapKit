package snap.webenv;
import netscape.javascript.JSObject;
import snap.webapi.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a wrapper for Web API DataTransfer (https://developer.mozilla.org/en-US/docs/Web/API/DataTransfer).
 */
public class CJDataTransfer extends DataTransfer {

    /**
     * Constructor.
     */
    public CJDataTransfer(Object objectJS)
    {
        super(objectJS);
        if (_jsObj == null)
            _jsObj = newDataTransfer();
    }

    /**
     * Returns the types.
     */
    @Override
    protected List<String> getTypesImpl()
    {
        // If cached drag/drop data transfer, get cached types
        if (_cached) {
            String[] types = getDropDataTransferTypesImpl();
            return List.of(types);
        }

        // Get DataTransfer.types
        Object typesArrayJS = getMember("types");
        Array<String> typesArray = new Array<>(typesArrayJS);
        return typesArray.toList();
    }

    /**
     * Returns the files.
     */
    @Override
    protected List<File> getFilesImpl()
    {
        // If cached drag/drop data transfer, get cached files
        if (_cached) {
            Object dropFilesArrayJS = getDropDataTransferFilesImpl();
            Array<JSObject> dropFilesArray = new Array<>(dropFilesArrayJS);
            List<JSObject> dropFilesJS = dropFilesArray.toList();
            return dropFilesJS.stream().map(dropFileJS -> new File(dropFileJS)).toList();
        }

        // Get DataTransfer.files
        Object fileListJS = getMember("files");
        int length = WebEnv.get().getMemberInt(fileListJS, "length");
        List<File> files = new ArrayList<>(length);

        // Iterate over file list and get files
        for (int i = 0; i < length; i++) {
            Object fileJS = WebEnv.get().call(fileListJS, "item", i);
            files.add(new File(fileJS));
        }

        // Return
        return files;
    }

    /**
     * Starts a drag for this data transfer and given element.
     */
    @Override
    public void startDrag(HTMLElement image, double dx, double dy)
    {
        startDragImpl((JSObject) _jsObj, (JSObject) image.getJS(), dx, dy);
    }

    /**
     * Returns the DataTransfer from last drop.
     */
    public static CJDataTransfer getDropDataTransfer()
    {
        // Get cached data transfer
        Object dropDataTransferJS = getDropDataTransferImpl();
        if (dropDataTransferJS == null)
            return null;

        // Wrap in DataTransfer, set cached and return
        CJDataTransfer dataTransfer = new CJDataTransfer(dropDataTransferJS);
        dataTransfer._cached = true;
        return dataTransfer;
    }

    /**
     * CJDataTransfer: newDataTransfer().
     */
    private static native JSObject newDataTransfer();

    /**
     * CJDataTransfer: getDropDataTransfer().
     */
    private static native JSObject getDropDataTransferImpl();

    /**
     * CJDataTransfer: getDropDataTransferTypesImpl().
     */
    private static native String[] getDropDataTransferTypesImpl();

    /**
     * CJDataTransfer: getDropDataTransferFilesImpl().
     */
    private static native JSObject getDropDataTransferFilesImpl();

    /**
     * CJDataTransfer: startDragImpl().
     */
    private static native void startDragImpl(JSObject dataTransfer, JSObject image, double dx, double dy);
}
