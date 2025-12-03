package snap.webapi;

/**
 * This class is a wrapper for Web API Uint8ClampedArray (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Uint8ClampedArray).
 */
public class Uint8ClampedArray extends TypedArray {

    /**
     * Constructor.
     */
    public Uint8ClampedArray(Object arrayJS)
    {
        super(arrayJS);
    }

    /**
     * Constructor.
     */
    public Uint8ClampedArray(short[] shortsArray)
    {
        super(WebEnv.get().getTypedArrayJSForClassAndObject(Uint8ClampedArray.class, shortsArray));
    }

    /**
     * Returns short value at given index.
     */
    public short get(int anIndex)
    {
        Number value = (Number) getSlot(anIndex);
        return value.shortValue();
    }
}
