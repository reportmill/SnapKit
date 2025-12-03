package snap.webapi;

/**
 * This class is a wrapper for Web API Blob (https://developer.mozilla.org/en-US/docs/Web/API/Blob).
 */
public class Blob extends JSProxy {

    /**
     * Constructor.
     */
    public Blob(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Constructor for given bytes and type.
     */
    public Blob(byte[] byteArray, String aType)
    {
        super(WebEnv.get().newBlobJSForBytesAndType(byteArray, aType));
    }

    /**
     * Returns the blob text.
     */
    public String getText()
    {
        Object promiseJS = call("text");
        return (String) WebEnv.get().awaitForPromise(promiseJS);
    }

    /**
     * Returns a URL for this blob.
     */
    public String createURL()  { return WebEnv.get().createUrlForBlobJS(_jsObj); }
}
