package snap.webapi;

/**
 * This class is a wrapper for Web API ClipboardItem (https://developer.mozilla.org/en-US/docs/Web/API/ClipboardItem).
 */
public class ClipboardItem extends JSProxy {

    /**
     * Constructor.
     */
    public ClipboardItem(Object eventJS)
    {
        super(eventJS);
    }

    /**
     * Constructor.
     */
    public ClipboardItem(String mimeType, String dataString)
    {
        super(WebEnv.get().newClipboardItemForMimeTypeAndDataString(mimeType, dataString));
    }

    /**
     * Constructor.
     */
    public ClipboardItem(Blob aBlob)
    {
        super(WebEnv.get().newClipboardItemForBlobJS(aBlob.getJS()));
    }

    /**
     * Returns the types.
     */
    public String[] getTypes()
    {
        Object typesArrayJS = getMember("types");
        Array<String> typesArray = new Array<>(typesArrayJS);
        return typesArray.toArray(String.class);
    }

    /**
     * Returns a type.
     */
    public Blob getType(String aType)
    {
        Object promiseJS = call("getType", aType);
        Object blobJS = WebEnv.get().awaitForPromise(promiseJS);
        return new Blob(blobJS);
    }
}