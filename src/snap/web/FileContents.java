package snap.web;

/**
 * This class represents the contents of a file as returned from a WebSite.
 */
public class FileContents {

    // The bytes
    private byte[]  _bytes;

    // The files
    private WebFile[]  _files;

    // The mod time
    private long  _modTime;

    /**
     * Constructor.
     */
    public FileContents(Object theContents, long modTime)
    {
        if (theContents instanceof byte[])
            _bytes = (byte[]) theContents;
        else if (theContents instanceof WebFile[])
            _files = (WebFile[]) theContents;
        else throw new RuntimeException("FileContents.init: Unsupported contents: " + theContents);
        _modTime = modTime;
    }

    /**
     * Returns the bytes.
     */
    public byte[] getBytes()  { return _bytes; }

    /**
     * Returns the files.
     */
    public WebFile[] getFiles()  { return _files; }

    /**
     * Returns the mod time.
     */
    public long getModTime()  { return _modTime; }
}
