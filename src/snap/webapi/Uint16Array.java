package snap.webapi;

/**
 * This class is a wrapper for Web API Uint16Array (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Uint16Array).
 */
public class Uint16Array extends TypedArray {

    /** Constructor. */
    //public Uint16Array(short[] theValues)
    //{ super(null); _jsObj = newArrayForJavaArray(theValues, theValues.length); }

    /**
     * Constructor.
     */
    public Uint16Array(int[] intArray)
    {
        super(WebEnv.get().getTypedArrayJSForClassAndObject(Uint16Array.class, intArray));
    }
}
