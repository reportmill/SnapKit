package snap.view;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.util.*;
import snap.web.*;

/**
 * ClipboardData represents a data entry from a copy/paste or drag/drop.
 */
public class ClipboardData {

    // The data source
    private Object _source;
    
    // A URL to the file contents
    private WebURL _sourceURL;
    
    // The data name, if applicable
    private String _name;
    
    // The MIME type
    private String _mimeType;
    
    // The string
    private String _string;
    
    // The bytes
    private byte[] _bytes;
    
    // Whether data is loaded
    private boolean _loaded = true;

    // A consumer to be called when data is loaded
    private Consumer <ClipboardData> _loadLsnr;

    /**
     * Creates a ClipboardData from source.
     */
    public ClipboardData(Object aSource)
    {
        _source = aSource;
        if (_source instanceof String) {
            _string = (String) _source;
            _mimeType = MIMEType.TEXT;
        }
    }

    /**
     * Creates a ClipboardData from source.
     */
    public ClipboardData(String aMimeType, Object aSource)
    {
        _source = aSource;
        _mimeType = aMimeType;
        if (_mimeType == null)
            _mimeType = MIMEType.UKNOWN;
        if (_source instanceof String)
            _string = (String) _source;
    }

    /**
     * The source of the file.
     */
    public Object getSource()  { return _source; }

    /**
     * Returns the URL to the file.
     */
    public WebURL getSourceURL()
    {
        if (_sourceURL != null) return _sourceURL;
        if (_source instanceof byte[])
            return null;
        return _sourceURL = WebURL.getURL(_source);
    }

    /**
     * Returns whether data is String.
     */
    public boolean isString()  { return _source instanceof String; }

    /**
     * Returns whether data is File list.
     */
    public boolean isFileList()  { return _mimeType == Clipboard.FILE_LIST; }

    /**
     * Returns whether data is a File.
     */
    public boolean isFile()  { return _source instanceof File; }

    /**
     * Returns whether data is image.
     */
    public boolean isImage()  { return _source instanceof Image; }

    /**
     * Returns the file name.
     */
    public String getName()
    {
        if (_name != null) return _name;
        if (getSourceURL() != null)
            return _name = getSourceURL().getFilename();
        return _name;
    }

    /**
     * Sets the file name.
     */
    protected void setName(String aValue)
    {
        _name = aValue;
    }

    /**
     * Returns the file type.
     */
    public String getFileType()
    {
        String filename = getName();
        if (filename != null && filename.indexOf('.') > 0)
            return FilePathUtils.getFileType(filename);
        if (_mimeType != null)
            return MIMEType.getExtension(_mimeType);
        return null;
    }

    /**
     * Returns the file content type.
     */
    public String getMIMEType()  { return _mimeType; }

    /**
     * Returns whether data is loaded.
     */
    public boolean isLoaded()  { return _loaded; }

    /**
     * Sets whether data is loaded.
     */
    protected void setLoaded(boolean aValue)
    {
        if (aValue == _loaded) return;
        _loaded = aValue;
        if (aValue && _loadLsnr != null) {
            _loadLsnr.accept(this);
            _loadLsnr = null;
        }
    }

    /**
     * Adds a load listener.
     */
    public void addLoadListener(Consumer <ClipboardData> aLoadLsnr)
    {
        if (isLoaded())
            aLoadLsnr.accept(this);
        else if (_loadLsnr != null)
            System.err.println("ClipboardData.addLoadListener: Multiple listeners not yet supported");
        _loadLsnr = aLoadLsnr;
    }

    /**
     * Returns the data as string.
     */
    public String getString()
    {
        // If already set, just return
        if (_string != null)
            return _string;

        // Handle get string from bytes
        byte[] bytes = getBytes();
        if (bytes != null)
            return _string = new String(bytes);

        // Complain and return null
        System.err.println("ClipboardData.getString: String not available for source " + _source);
        return null;
    }

    /**
     * Returns the data as byte array.
     */
    public byte[] getBytes()
    {
        // If already set, just return
        if (_bytes != null) return _bytes;

        // If String set, return bytes
        if (_string != null)
            return _bytes = _string.getBytes();

        // Handle get bytes from byte array or InputStream
        if (_source instanceof byte[] || _source instanceof InputStream)
            return _bytes = SnapUtils.getBytes(_source);

        // Handle get bytes from Source URL
        if (getSourceURL() != null)
            return _bytes = getSourceURL().getBytes();

        // Complain and return null
        System.err.println("ClipboardData.getBytes: Bytes not available for source " + _source);
        return null;
    }

    /**
     * Sets the bytes.
     */
    protected void setBytes(byte[] theBytes)
    {
        _bytes = theBytes;
        setLoaded(true);
    }

    /**
     * Returns the data as input stream.
     */
    public InputStream getInputStream()
    {
        byte[] bytes = getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Returns the data as a list of files.
     */
    public List <ClipboardData> getFiles()
    {
        List <ClipboardData> files = new ArrayList<>();
        if (_source instanceof List) {
            List<?> list = (List<?>) _source;
            for (Object file : list)
                files.add(ClipboardData.get(file));
        }

        // Return
        return files;
    }

    /**
     * Returns a conventional Java file, if available.
     */
    public File getJavaFile()
    {
        if (_source instanceof File)
            return (File) _source;
        return null;
    }

    /**
     * Returns the data as list of Java files.
     */
    public List <File> getJavaFiles()
    {
        if (_source instanceof List)
            return (List<File>) _source;
        return null;
    }

    /**
     * Returns the data as image.
     */
    public Image getImage()
    {
        if (_source instanceof Image)
            return (Image) _source;
        return null;
    }

    /**
     * Returns a ClipboardData for given object.
     */
    public static ClipboardData get(Object theData)
    {
        // Handle ClipboardData
        if (theData instanceof ClipboardData)
            return (ClipboardData) theData;

        // Handle String
        if (theData instanceof String)
            return new ClipboardData(Clipboard.STRING, theData);

        // Handle File
        if (theData instanceof File) {
            File file = (File) theData;
            String mimeType = MIMEType.getType(file.getPath());
            return new ClipboardData(mimeType, file);
        }

        // Handle File List
        if (theData instanceof List) {
            List<?> list = (List<?>) theData;
            Object item0 = list.size() > 0? list.get(0) : null;
            if (item0 instanceof File || item0 instanceof WebURL)
                return new ClipboardData(Clipboard.FILE_LIST, list);
        }

        // Handle File array
        if (theData instanceof File[]) {
            File[] files = (File[]) theData;
            return new ClipboardData(Clipboard.FILE_LIST, Arrays.asList(files)); }

        // Handle Image
        if (theData instanceof Image) {
            Image image = (Image) theData;
            return new ClipboardData(Clipboard.IMAGE, image);

//            // Get image type and bytes
//            byte bytes[] = image.getBytes();
//            String mimeType = MIMEType.getType(image.getType());
//            return new ClipboardData(mimeType, bytes);
        }

        // Handle Color
        if (theData instanceof Color) {
            Color color = (Color) theData;
            return new ClipboardData(Clipboard.COLOR, color.toHexString());
        }

        // Complain
        System.err.println("ClipboardData.get: Unknown data type " + theData);
        return null;
    }
}