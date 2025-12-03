package snap.webenv;
import snap.view.ClipboardData;
import snap.webapi.*;

/**
 * A ClipboardData subclass to read JS File bytes asynchronously.
 */
class CJClipboardData extends ClipboardData {

    /**
     * Creates ClipboardData for given JS File and starts loading.
     */
    public CJClipboardData(File aFile)
    {
        super(aFile.getType(), null);
        setName(aFile.getName());
        setLoaded(false);

        // Read bytes
        FileReader fileReader = new FileReader();
        fileReader.readBytesAndRunLater(aFile, () -> fileReaderDidLoad(fileReader));
    }

    /**
     * Called when FileReader finishes reading bytes.
     */
    private void fileReaderDidLoad(FileReader aFR)
    {
        byte[] bytes = aFR.getResultBytes();
        setBytes(bytes);
    }
}
