package snap.web;

/**
 * This class represents the contents of a file as returned from a WebSite.
 */
public class FileContents {

    // The bytes
    private byte[]  _bytes;

    // The files
    private WebFile[]  _files;

    // The file last modified time
    private long _lastModTime;

    /**
     * Constructor.
     */
    public FileContents(Object theContents, long lastModTime)
    {
        if (theContents instanceof byte[])
            _bytes = (byte[]) theContents;
        else if (theContents instanceof WebFile[])
            _files = (WebFile[]) theContents;
        else throw new RuntimeException("FileContents.init: Unsupported contents: " + theContents);
        _lastModTime = lastModTime;
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
     * Returns the file last modified time.
     */
    public long getLastModTime()  { return _lastModTime; }
}
