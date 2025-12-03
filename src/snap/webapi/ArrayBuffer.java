package snap.webapi;

/**
 * This class is a wrapper for Web API ArrayBuffer (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer).
 */
public class ArrayBuffer extends JSProxy {

    /**
     * Constructor.
     */
    public ArrayBuffer(Object arrayBufferJS)
    {
        super(arrayBufferJS);
    }

    /**
     * Returns the ArrayBuffer.byteLength property.
     */
    public int getByteLength()  { return getMemberInt("byteLength"); }

    /**
     * Returns array buffer bytes.
     */
    public byte[] getBytes()  { return WebEnv.get().getBytesArrayForArrayBufferJS(_jsObj); }
}
