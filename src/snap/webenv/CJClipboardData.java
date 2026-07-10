package snap.webenv;
import snap.view.ClipboardData;
import snap.webapi.*;

/**
 * A ClipboardData subclass to read JS File bytes asynchronously.
 */
class CJClipboardData extends ClipboardData {

    /**
     * Constructor for given JS File and starts loading.
     */
    public CJClipboardData(File aFile)
    {
        super(null, aFile.getType());
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
