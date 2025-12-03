package snap.webapi;

/**
 * This class is a wrapper for Web API Int8Array (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Int8Array).
 */
public class Int8Array extends TypedArray {

    /**
     * Constructor.
     */
    public Int8Array(byte[] byteArray)
    {
        super(WebEnv.get().getTypedArrayJSForClassAndObject(Int8Array.class, byteArray));
    }
}
