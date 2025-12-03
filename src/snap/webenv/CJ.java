package snap.webenv;
import snap.webapi.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;

/**
 * Utility methods for SnapKit + CheerpJ.
 */
public class CJ {

    /**
     * Returns JavaScript color for snap color.
     */
    public static String getColorJS(Color aColor)
    {
        if (aColor == null) return null;
        int r = aColor.getRedInt(), g = aColor.getGreenInt(), b = aColor.getBlueInt(), a = aColor.getAlphaInt();
        StringBuilder sb = new StringBuilder(a == 255 ? "rgb(" : "rgba(");
        sb.append(r).append(',').append(g).append(',').append(b);
        if (a == 255) sb.append(')');
        else sb.append(',').append(a / 255d).append(')');
        return sb.toString();
    }

    /**
     * Returns JavaScript CanvasGradient for snap GradientPaint.
     */
    public static CanvasGradient getGradientJS(GradientPaint gradientPaint, CanvasRenderingContext2D renderingContext)
    {
        // Create CanvasGradient for end points
        double startX = gradientPaint.getStartX();
        double startY = gradientPaint.getStartY();
        double endX = gradientPaint.getEndX();
        double endY = gradientPaint.getEndY();
        CanvasGradient canvasGradient = renderingContext.createLinearGradient(startX, startY, endX, endY);

        // Add stops
        for (int i = 0, iMax = gradientPaint.getStopCount(); i < iMax; i++)
            canvasGradient.addColorStop(gradientPaint.getStopOffset(i), getColorJS(gradientPaint.getStopColor(i)));

        // Return
        return canvasGradient;
    }

    /**
     * Returns JavaScript CanvasPattern for snap ImagePaint.
     */
    public static CanvasPattern getTextureJS(ImagePaint imagePaint, CanvasRenderingContext2D renderingContext)
    {
        // Get image
        Image image = imagePaint.getImage();

        // If HiDPI, reduce because CanvasPattern seems to render at pixel sizes
        if (image.getWidth() != image.getPixWidth())
            image = image.copyForSizeAndDpiScale(image.getPixWidth() / 4, image.getPixHeight() / 4, 1);

        // Get CanvasPattern and set
        CanvasImageSource imageSource = (CanvasImageSource) image.getNative();
        return renderingContext.createPattern(imageSource, "repeat");
    }

    /**
     * Returns JavaScript font for snap font.
     */
    public static String getFontJS(Font aFont)
    {
        String str = "";
        if (aFont.isBold()) str += "Bold ";
        if (aFont.isItalic()) str += "Italic ";
        str += ((int) aFont.getSize()) + "px ";
        str += aFont.getFamily();
        return str;
    }

    /**
     * Returns the offset.
     */
    public static Point getOffsetAll(HTMLElement anEmt)
    {
        // Update window location
        int top = 0;
        int left = 0;
        HTMLDocument doc = HTMLDocument.getDocument();
        for (Node emt = anEmt; emt != null && emt.getJS() != doc.getJS(); emt = emt.getParentNode()) {
            top += ((HTMLElement) emt).getOffsetTop();
            left += ((HTMLElement) emt).getOffsetLeft();
        }

        // Return point
        return new Point(left, top);
    }

    /**
     * Viewport size.
     */
    public static Rect getViewportBounds()
    {
        HTMLHtmlElement htmlElement = HTMLDocument.getDocument().getDocumentElement();
        double docW = htmlElement.getClientWidth();
        double docH = htmlElement.getClientHeight();
        return new Rect(0, 0, docW, docH);
    }
}