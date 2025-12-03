package snap.webapi;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is a wrapper for Web API CanvasRenderingContext2D (https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D).
 */
public class CanvasRenderingContext2D extends JSProxy {

    /**
     * Constructor.
     */
    public CanvasRenderingContext2D(Object renderingContextJS)
    {
        super(renderingContextJS);
    }

    /**
     * CanvasRenderingContext2D: setTransform().
     */
    public void setTransform(double arg1, double arg2, double arg3, double arg4, double arg5, double arg6)
    {
        call("setTransform", arg1, arg2, arg3, arg4, arg5, arg6);
    }

    /**
     * CanvasRenderingContext2D: transform().
     */
    public void transform(double arg1, double arg2, double arg3, double arg4, double arg5, double arg6)
    {
        call("transform", arg1, arg2, arg3, arg4, arg5, arg6);
    }

    /**
     * CanvasRenderingContext2D: translate().
     */
    public void translate(double var1, double var2)  { call("translate", var1, var2); }

    /**
     * CanvasRenderingContext2D: rotate().
     */
    public void rotate(double var1)  { call("rotate", var1); }

    /**
     * CanvasRenderingContext2D: rotate().
     */
    public void scale(double var1, double var3)  { call("scale", var1, var3); }

    /**
     * CanvasRenderingContext2D: save().
     */
    public void save()  { call("save"); }

    /**
     * CanvasRenderingContext2D: restore().
     */
    public void restore()  { call("restore"); }

    /**
     * CanvasRenderingContext2D: restore().
     */
    public void fill()  { call("fill"); }

    /**
     * CanvasRenderingContext2D: restore().
     */
    public void stroke()  { call("stroke"); }

    /**
     * CanvasRenderingContext2D: restore().
     */
    public void clip()  { call("clip"); }

    /**
     * CanvasRenderingContext2D: fillRect().
     */
    public void fillRect(double aX, double aY, double aW, double aH)  { call("fillRect", aX, aY, aW, aH); }

    /**
     * CanvasRenderingContext2D: strokeRect().
     */
    public void strokeRect(double aX, double aY, double aW, double aH)  { call("strokeRect", aX, aY, aW, aH); }

    /**
     * Returns the color, gradient or pattern used inside shapes.
     */
    public Object getFillStyle()  { return null; }

    /**
     * Sets the color to use inside shapes.
     */
    public void setFillStyle(String fillStyleStr)  { setMemberString("fillStyle", fillStyleStr); }

    /**
     * Sets the gradient to use inside shapes.
     */
    public void setFillStyle(CanvasGradient gradient)  { setMember("fillStyle", gradient._jsObj); }

    /**
     * Sets the pattern to use inside shapes.
     */
    public void setFillStyle(CanvasPattern pattern)  { setMember("fillStyle", pattern._jsObj); }

    /**
     * Returns the color, gradient or pattern used for the strokes (outlines) around shapes.
     */
    public Object getStrokeStyle()  { return null; }

    /**
     * Sets the color to use for the strokes (outlines) around shapes.
     */
    public void setStrokeStyle(String strokeStyleStr)  { setMemberString("strokeStyle", strokeStyleStr); }

    /**
     * Sets the gradient to use for the strokes (outlines) around shapes.
     */
    public void setStrokeStyle(CanvasGradient gradient)  { setMember("strokeStyle", gradient._jsObj); }

    /**
     * Sets the pattern to use for the strokes (outlines) around shapes.
     */
    public void setStrokeStyle(CanvasPattern pattern)  { setMember("strokeStyle", pattern._jsObj); }

    /**
     * CanvasRenderingContext2D: getGlobalAlpha().
     */
    public double getGlobalAlpha()  { return getMemberDouble("globalAlpha"); }

    /**
     * CanvasRenderingContext2D: setGlobalAlpha().
     */
    public void setGlobalAlpha(double var1)  { setMemberDouble("globalAlpha", var1); }

    /**
     * CanvasRenderingContext2D: getGlobalCompositeOperation().
     */
    public String getGlobalCompositeOperation()  { return getMemberString("globalCompositeOperation"); }

    /**
     * CanvasRenderingContext2D: getGlobalCompositeOperation().
     */
    public void setGlobalCompositeOperation(String var1)  { setMemberString("globalCompositeOperation", var1); }

    /**
     * CanvasRenderingContext2D: getLineCap().
     */
    public String getLineCap()  { return getMemberString("lineCap"); }

    /**
     * CanvasRenderingContext2D: getLineCap().
     */
    public void setLineCap(String var1)  { setMemberString("lineCap", var1); }

    /**
     * CanvasRenderingContext2D: getLineDash().
     */
    public Array<Number> getLineDash()
    {
        Object arrayJS = getMember("getLineDash");
        return new Array<>(arrayJS);
    }

    /**
     * CanvasRenderingContext2D: setLineDash().
     */
    public void setLineDash(double[] dashArray)
    {
        List<Double> dashList = dashArray != null ? Arrays.stream(dashArray).boxed().toList() : Collections.emptyList();
        call("setLineDash", dashList);
    }

    /**
     * CanvasRenderingContext2D: getLineDashOffset().
     */
    public double getLineDashOffset()  { return getMemberDouble("lineDashOffset"); }

    /**
     * CanvasRenderingContext2D: setLineDashOffset().
     */
    public void setLineDashOffset(double var1)  { setMemberDouble("lineDashOffset", var1); }

    /**
     * CanvasRenderingContext2D: getLineJoin().
     */
    public String getLineJoin()  { return getMemberString("lineJoin"); }

    /**
     * CanvasRenderingContext2D: setLineJoin().
     */
    public void setLineJoin(String var1)  { setMemberString("lineJoin", var1); }

    /**
     * CanvasRenderingContext2D: getLineWidth().
     */
    public double getLineWidth()  { return getMemberDouble("lineWidth"); }

    /**
     * CanvasRenderingContext2D: setLineWidth().
     */
    public void setLineWidth(double var1)  { setMemberDouble("lineWidth", var1); }

    /**
     * CanvasRenderingContext2D: getLineJoin().
     */
    public double getMiterLimit()  { return getMemberDouble("miterLimit"); }

    /**
     * CanvasRenderingContext2D: getLineJoin().
     */
    public void setMiterLimit(double var1)  { setMemberDouble("miterLimit", var1); }

    /**
     * CanvasRenderingContext2D: beginPath().
     */
    public void beginPath()  { call("beginPath"); }

    /**
     * CanvasRenderingContext2D: moveTo().
     */
    public void moveTo(double aX, double aY)  { call("moveTo", aX, aY); }

    /**
     * CanvasRenderingContext2D: lineTo().
     */
    public void lineTo(double aX, double aY)  { call("lineTo", aX, aY); }

    /**
     * CanvasRenderingContext2D: bezierCurveTo().
     */
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double endX, double endY)
    {
        call("bezierCurveTo", cp1x, cp1y, cp2x, cp2y, endX, endY);
    }

    /**
     * CanvasRenderingContext2D: closePath().
     */
    public void closePath()  { call("closePath"); }

    /**
     * CanvasRenderingContext2D: rect().
     */
    public void rect(double aX, double aY, double aW, double aH)  { call("rect", aX, aY, aW, aH); }

    /**
     * CanvasRenderingContext2D: clearRect().
     */
    public void clearRect(double aX, double aY, double aW, double aH)  { call("clearRect", aX, aY, aW, aH); }

    //void arc(double var1, double var3, double var5, double var7, double var9, boolean var11);
    //void arc(double var1, double var3, double var5, double var7, double var9);
    //void arcTo(double var1, double var3, double var5, double var7, double var9);
    //void quadraticCurveTo(double var1, double var3, double var5, double var7);
    //void rect(double var1, double var3, double var5, double var7);

    /**
     * Return the current font.
     */
    public String getFont()  { return getMemberString("font"); }

    /**
     * Sets the current font.
     */
    public void setFont(String fontName)  { setMemberString("font", fontName); }

    /**
     * Return the TextMetrics for given string.
     */
    public TextMetrics measureText(String aString)
    {
        Object textMetricsJS = call("measureText", aString);
        return new TextMetrics(textMetricsJS);
    }

    /**
     * CanvasRenderingContext2D: fillText().
     */
    public void fillText(String aString, double aX, double aY)  { call("fillText", aString, aX, aY); }

    /**
     * CanvasRenderingContext2D: fillText().
     */
    public void fillText(String aString, double aX, double aY, double maxWidth)  { call("fillText", aString, aX, aY, maxWidth); }

    /**
     * CanvasRenderingContext2D: strokeText().
     */
    public void strokeText(String aString, double aX, double aY)  { call("strokeText", aString, aX, aY); }

    /**
     * CanvasRenderingContext2D: strokeText().
     */
    public void strokeText(String aString, double aX, double aY, double maxWidth)  { call("strokeText", aString, aX, aY, maxWidth); }

    /**
     * CanvasRenderingContext2D: drawImage().
     */
    public void drawImage(CanvasImageSource anImage, double aX, double aY)
    {
        JSProxy jsProxy = (JSProxy) anImage;
        call("drawImage", jsProxy.getJS(), aX, aY);
    }

    /**
     * CanvasRenderingContext2D: drawImage().
     */
    public void drawImage(CanvasImageSource anImage, double aX, double aY, double aW, double aH)
    {
        JSProxy jsProxy = (JSProxy) anImage;
        call("drawImage", jsProxy.getJS(), aX, aY, aW, aH);
    }

    /**
     * CanvasRenderingContext2D: drawImage().
     */
    public void drawImage(CanvasImageSource anImage, double srcX, double srcY, double srcW, double srcH, double destX, double destY, double destW, double destH)
    {
        JSProxy jsProxy = (JSProxy) anImage;
        call("drawImage", jsProxy.getJS(), srcX, srcY, srcW, srcH, destX, destY, destW, destH);
    }

    /**
     * getImageData().
     */
    public ImageData getImageData(int aX, int aY, int aW, int aH)
    {
        Object imageDataJS = call("getImageData", aX, aY, aW, aH);
        return new ImageData(imageDataJS);
    }

    /**
     * Put image data.
     */
    public void putImageData(ImageData imageData, double aX, double aY)  { call("putImageData", imageData.getJS(), aX, aY); }

    /**
     * Put image data.
     */
    public void putImageData(ImageData imageData, double aX, double aY, double dirtyX, double dirtyY, double dirtyW, double dirtyH)
    {
        call("putImageData", imageData.getJS(), aX, aY, dirtyX, dirtyY, dirtyW, dirtyH);
    }

    /**
     * Creates a linear gradient along the line connecting two given coordinates.
     */
    public CanvasGradient createLinearGradient(double x0, double y0, double x1, double y1)
    {
        Object gradientJS = call("createLinearGradient", x0, y0, x1, y1);
        return new CanvasGradient(gradientJS);
    }

    /**
     * Creates a radial gradient using the size and coordinates of two circles.
     */
    public CanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1)
    {
        Object gradientJS = call("createRadialGradient", x0, y0, r0, x1, y1, r1);
        return new CanvasGradient(gradientJS);
    }

    /**
     * Creates a pattern using the specified image and repetition.
     */
    public CanvasPattern createPattern(CanvasImageSource image, String repetition)
    {
        JSProxy imageProxy = (JSProxy) image;
        Object patternJS = call("createPattern", imageProxy.getJS(), repetition);
        return new CanvasPattern(patternJS);
    }

    /**
     * Returns the amount of blur applied to shadows.
     */
    public double getShadowBlur()  { return getMemberDouble("shadowBlur"); }

    /**
     * Returns the amount of blur applied to shadows.
     */
    public void setShadowBlur(double value)  { setMemberDouble("shadowBlur", value); }

    /**
     * Returns the color of shadows.
     */
    public String getShadowColor()  { return getMemberString("shadowColor"); }

    /**
     * Returns the color of shadows.
     */
    public void setShadowColor(String colorStr)  { setMemberString("shadowColor", colorStr); }

    /**
     * Returns the distance that shadows will be offset horizontally
     */
    public double getShadowOffsetX()  { return getMemberDouble("shadowOffsetX"); }

    /**
     * Returns the distance that shadows will be offset horizontally
     */
    public void setShadowOffsetX(double value)  { setMemberDouble("shadowOffsetX", value); }

    /**
     * Returns the distance that shadows will be offset vertically.
     */
    public double getShadowOffsetY()  { return getMemberDouble("shadowOffsetY"); }

    /**
     * Returns the distance that shadows will be offset vertically
     */
    public void setShadowOffsetY(double value)  { setMemberDouble("shadowOffsetY", value); }

    /**
     * CanvasRenderingContext2D: getImageSmoothingQuality().
     */
    public String getImageSmoothingQuality()  { return getMemberString("imageSmoothingQuality"); }

    /**
     * CanvasRenderingContext2D: setImageSmoothingQuality().
     */
    public void setImageSmoothingQuality(String var1)  { setMemberString("imageSmoothingQuality", var1); }

    /**
     * CanvasRenderingContext2D: isImageSmoothingEnabled().
     */
    public boolean isImageSmoothingEnabled(String var1)  { return true; } // getMemberBoolean("imageSmoothingEnabled");

    /**
     * CanvasRenderingContext2D: setImageSmoothingEnabled().
     */
    public void setImageSmoothingEnabled(boolean var1)  { } //setMemberBoolean("imageSmoothingEnabled", var1);

    /**
     * CanvasRenderingContext2D: paintStacks().
     */
    public void paintStacks(double contextScale, int[] instructionStack, int instructionStackSize, int[] intStack, double[] doubleStack, String[] stringStack, Object[] objectStack)
    {
        System.err.println("CanvasRenderingContext2D.paintStacks: Not implemented yet");
    }

    //boolean isPointInPath(double var1, double var3);
    //boolean isPointInStroke(double var1, double var3);
    //void scrollPathIntoView();
    //ImageData createImageData(double var1, double var3);
    //boolean drawCustomFocusRing(Element var1);
    //void drawSystemFocusRing(Element var1);
    //String getTextAlign();
    //void setTextAlign(String var1);
    //String getTextBaseline();
    //void setTextBaseline(String var1);
    //HTMLCanvasElement getCanvas();
}
