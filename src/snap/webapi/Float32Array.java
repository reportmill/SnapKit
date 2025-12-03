package snap.webapi;

/**
 * This class is a wrapper for Web API Float32Array (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Float32Array).
 */
public class Float32Array extends TypedArray {

    /**
     * Constructor.
     */
    public Float32Array(float[] floatArray)
    {
        super(WebEnv.get().getTypedArrayJSForClassAndObject(Float32Array.class, floatArray));
    }

    /**
     * Constructor.
     */
    public Float32Array(double[] doubleArray)
    {
        super(WebEnv.get().getTypedArrayJSForClassAndObject(Float32Array.class, doubleArray));
    }
}
