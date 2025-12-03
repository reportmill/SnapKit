package snap.webapi;

/**
 * This class is a wrapper for Web API Int8Array (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Int16Array).
 */
public class Int16Array extends TypedArray {

    /**
     * Constructor.
     */
    public Int16Array(short[] shortArray)
    {
        super(WebEnv.get().getTypedArrayJSForClassAndObject(Int16Array.class, shortArray));
    }
}
