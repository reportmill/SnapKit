package snap.webenv;
import snap.webapi.*;
import netscape.javascript.JSObject;

/**
 * This class is a wrapper for Web API CanvasRenderingContext2D (https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D).
 */
public class CJCanvasRenderingContext2D extends CanvasRenderingContext2D {

    // The rendering context object
    private JSObject _cntxJS;

    /**
     * Constructor.
     */
    public CJCanvasRenderingContext2D(Object renderingContextJS)
    {
        super(renderingContextJS);
        _cntxJS = (JSObject) _jsObj;
    }

    /**
     * CanvasRenderingContext2D: setLineDash().
     */
    @Override
    public void setLineDash(double[] dashArray)
    {
        setLineDashImpl(_cntxJS, dashArray != null ? dashArray : new double[0]);
    }

    /**
     * CanvasRenderingContext2D: fillText().
     */
    @Override
    public void fillText(String aString, double aX, double aY)  { fillTextImpl(_cntxJS, aString, aX, aY); }

    /**
     * CanvasRenderingContext2D: fillText().
     */
    @Override
    public void fillText(String aString, double aX, double aY, double maxWidth)  { fillTextImpl2(_cntxJS, aString, aX, aY, maxWidth); }

    /**
     * CanvasRenderingContext2D: strokeText().
     */
    @Override
    public void strokeText(String aString, double aX, double aY)  { strokeTextImpl(_cntxJS, aString, aX, aY); }

    /**
     * CanvasRenderingContext2D: strokeText().
     */
    @Override
    public void strokeText(String aString, double aX, double aY, double maxWidth)  { strokeTextImpl2(_cntxJS, aString, aX, aY, maxWidth); }

    /**
     * CanvasRenderingContext2D: drawImage().
     */
    @Override
    public void drawImage(CanvasImageSource anImage, double aX, double aY)  { drawImageImpl(_cntxJS, (JSObject) ((JSProxy) anImage).getJS(), aX, aY); }

    /**
     * CanvasRenderingContext2D: drawImage().
     */
    @Override
    public void drawImage(CanvasImageSource anImage, double aX, double aY, double aW, double aH)
    {
        drawImageImpl2(_cntxJS, (JSObject) ((JSProxy) anImage).getJS(), aX, aY, aW, aH);
    }

    /**
     * CanvasRenderingContext2D: drawImage().
     */
    @Override
    public void drawImage(CanvasImageSource anImage, double srcX, double srcY, double srcW, double srcH, double destX, double destY, double destW, double destH)
    {
        drawImageImpl3(_cntxJS, (JSObject) ((JSProxy) anImage).getJS(), srcX, srcY, srcW, srcH, destX, destY, destW, destH);
    }

    /**
     * getImageData().
     */
    @Override
    public ImageData getImageData(int aX, int aY, int aW, int aH)
    {
        JSObject imageDataJS = getImageDataImpl(_cntxJS, aX, aY, aW, aH);
        return new ImageData(imageDataJS);
    }

    /**
     * Put image data.
     */
    @Override
    public void putImageData(ImageData imageData, double aX, double aY, double dirtyX, double dirtyY, double dirtyW, double dirtyH)
    {
        putImageDataImpl(_cntxJS, (JSObject) imageData.getJS(), aX, aY, dirtyX, dirtyY, dirtyW, dirtyH);
    }

    /**
     * Creates a linear gradient along the line connecting two given coordinates.
     */
    @Override
    public CanvasGradient createLinearGradient(double x0, double y0, double x1, double y1)
    {
        JSObject gradientJS = createLinearGradientImpl(_cntxJS, x0, y0, x1, y1);
        return new CanvasGradient(gradientJS);
    }

    /**
     * Creates a radial gradient using the size and coordinates of two circles.
     */
    @Override
    public CanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1)
    {
        JSObject gradientJS = createRadialGradientImpl(_cntxJS, x0, y0, r0, x1, y1, r1);
        return new CanvasGradient(gradientJS);
    }

    /**
     * Creates a pattern using the specified image and repetition.
     */
    @Override
    public CanvasPattern createPattern(CanvasImageSource image, String repetition)
    {
        JSObject patternJS = createPatternImpl(_cntxJS, (JSObject) ((JSProxy) image).getJS(), repetition);
        return new CanvasPattern(patternJS);
    }

    /**
     * CanvasRenderingContext2D: paintStacks().
     */
    public void paintStacks(double contextScale, int[] instructionStack, int instructionStackSize, int[] intStack, double[] doubleStack, String[] stringStack, Object[] objectStack)
    {
        paintStacksImpl(_cntxJS, contextScale, instructionStack, instructionStackSize, intStack, doubleStack, stringStack, objectStack);
    }

    /**
     * CanvasRenderingContext2D: setLineDashImpl().
     */
    private static native void setLineDashImpl(JSObject cntxJS, double[] dashArray);

    /**
     * CanvasRenderingContext2D: fillTextImpl().
     */
    private static native void fillTextImpl(JSObject cntxJS, String aString, double aX, double aY);

    /**
     * CanvasRenderingContext2D: fillTextImpl().
     */
    private static native void fillTextImpl2(JSObject cntxJS, String aString, double aX, double aY, double maxWidth);

    /**
     * CanvasRenderingContext2D: strokeText().
     */
    private static native void strokeTextImpl(JSObject cntxJS, String aString, double aX, double aY);

    /**
     * CanvasRenderingContext2D: strokeText().
     */
    private static native void strokeTextImpl2(JSObject cntxJS, String aString, double aX, double aY, double maxWidth);

    /**
     * CanvasRenderingContext2D: drawImageImpl().
     */
    private static native void drawImageImpl(JSObject cntxJS, JSObject imageJS, double aX, double aY);

    /**
     * CanvasRenderingContext2D: drawImageImpl().
     */
    private static native void drawImageImpl2(JSObject cntxJS, JSObject imageJS, double aX, double aY, double aW, double aH);

    /**
     * CanvasRenderingContext2D: drawImageImpl().
     */
    private static native void drawImageImpl3(JSObject cntxJS, JSObject imageJS, double srcX, double srcY, double srcW, double srcH, double destX, double destY, double destW, double destH);

    /**
     * CanvasRenderingContext2D_getImageDataImpl().
     */
    private static native JSObject getImageDataImpl(JSObject canvasJS, int aX, int aY, int aW, int aH);

    /**
     * CanvasRenderingContext2D_putImageDataImpl().
     */
    private static native void putImageDataImpl(JSObject canvasJS, JSObject imageDataJS, double aX, double aY, double dirtyX, double dirtyY, double dirtyW, double dirtyH);

    /**
     * CanvasRenderingContext2D: createLinearGradientImpl().
     */
    private static native JSObject createLinearGradientImpl(JSObject contextJS, double x0, double y0, double x1, double y1);

    /**
     * CanvasRenderingContext2D: createRadialGradientImpl().
     */
    private static native JSObject createRadialGradientImpl(JSObject contextJS, double x0, double y0, double r0, double x1, double y1, double r1);

    /**
     * CanvasRenderingContext2D: createPatternImpl().
     */
    private static native JSObject createPatternImpl(JSObject contextJS, JSObject imageJS, String repetition);

    /**
     * CanvasRenderingContext2D: paintStacks().
     */
    private static native void paintStacksImpl(JSObject contextJS, double contextScale, int[] instructionStack, int instructionStackSize, int[] intStack, double[] doubleStack, String[] stringStack, Object[] objectStack);
}
