package snap.webapi;

/**
 * This class is a wrapper for Web API File (https://developer.mozilla.org/en-US/docs/Web/API/File).
 */
public class File extends Blob {

    /**
     * Constructor.
     */
    public File(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Constructor for name, type and bytes.
     */
    public File(String aName, String aType, byte[] byteArray)
    {
        super(WebEnv.get().newFileJSForNameAndTypeAndBytes(aName, aType, byteArray));
    }

    /**
     * Returns the file name.
     */
    public String getName()  { return getMemberString("name"); }

    /**
     * Returns the file type.
     */
    public String getType()  { return getMemberString("type"); }
}
