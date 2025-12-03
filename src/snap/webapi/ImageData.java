package snap.webapi;

/**
 * This class is a wrapper for Web API ImageData (https://developer.mozilla.org/en-US/docs/Web/API/ImageData).
 */
public class ImageData extends JSProxy {

    /**
     * Constructor.
     */
    public ImageData(Object imageDataJS)
    {
        super(imageDataJS);
    }

    /**
     * Constructor.
     */
    public ImageData(short[] shortsArray, int aWidth, int aHeight)
    {
        super(WebEnv.get().newImageDataJSForRgbaArrayAndWidthAndHeight(shortsArray, aWidth, aHeight));
    }

    /**
     * Returns ImageData.width.
     */
    public int getWidth()  { return getMemberInt("width"); }

    /**
     * Returns ImageData.width.
     */
    public int getHeight()  { return getMemberInt("height"); }

    /**
     * Returns ImageData.width.
     */
    public Uint8ClampedArray getData()
    {
        Object arrayJS = getMember("data");
        return new Uint8ClampedArray(arrayJS);
    }
}
