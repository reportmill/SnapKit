package snap.webapi;

/**
 * This class is a wrapper for Web API ArrayBufferView (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBufferView).
 */
public abstract class TypedArray extends JSProxy {

    /**
     * Constructor.
     */
    public TypedArray(Object objJS)
    {
        super(objJS);
    }

    public int getLength()  { return getMemberInt("length"); }

    /**
     * Returns the bytes.
     */
    public byte[] getBytes()  { return WebEnv.get().getBytesArrayForTypedArrayJS(_jsObj); }

    /**
     * Returns an array of shorts for this array.
     */
    public short[] getShortsArray()  { return WebEnv.get().getShortsArrayForTypedArrayJS(_jsObj); }

    /**
     * Returns an array of shorts for this array.
     */
    public short[] getShortsArrayForChannelIndexAndCount(int channelIndex, int channelCount)
    {
        return WebEnv.get().getShortsArrayForChannelIndexAndCount(_jsObj, channelIndex, channelCount);
    }
}
