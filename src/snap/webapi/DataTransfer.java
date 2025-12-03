package snap.webapi;
import java.util.Collections;
import java.util.List;

/**
 * This class is a wrapper for Web API DataTransfer (https://developer.mozilla.org/en-US/docs/Web/API/DataTransfer).
 */
public class DataTransfer extends JSProxy {

    // The types
    private List<String> _types;

    // The files
    private List<File> _files;

    // Whether this DataTransfer was cached
    protected boolean _cached;

    /**
     * Constructor.
     */
    public DataTransfer(Object objectJS)
    {
        super(objectJS);
    }

    /**
     * Returns the types.
     */
    public List<String> getTypes()
    {
        if (_types != null) return _types;
        return _types = getTypesImpl();
    }

    /**
     * Returns the types.
     */
    protected List<String> getTypesImpl()  { System.err.println("DataTransfer.getTypesImpl() not implemented"); return Collections.emptyList(); }

    /**
     * Returns the data for a given type.
     */
    public String getData(String aType)  { return (String) call("getData", aType); }

    /**
     * Set the data for a given type.
     */
    public void setData(String aType, String theData)  { call("setData", aType, theData); }

    /**
     * Set the data for a given type.
     */
    public void setData(String aType, byte[] theData)  { call("setData", aType, theData); }

    /**
     * Returns the number of files.
     */
    public int getFileCount()  { return getFiles().size(); }

    /**
     * Returns the files.
     */
    public List<File> getFiles()
    {
        if (_files != null) return _files;
        return _files = getFilesImpl();
    }

    /**
     * Returns the files.
     */
    protected List<File> getFilesImpl()  { System.err.println("DataTransfer.getFilesImpl() not implemented"); return Collections.emptyList(); }

    /**
     * Starts a drag for this data transfer and given element.
     */
    public void startDrag(HTMLElement image, double dx, double dy)  { System.err.println("DataTransfer.startDrag() not implemented"); }

    /**
     * Returns whether DataTransfer has given type.
     */
    public boolean hasType(String aType)  { return getTypes().contains(aType); }
}
