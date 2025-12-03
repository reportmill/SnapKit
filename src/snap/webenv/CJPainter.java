package snap.webenv;
import snap.geom.PathIter;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.gfx.*;
import snap.webapi.*;

/**
 * A snap Painter for rendering to a CheerpJ HTMLCanvasElement.
 */
public class CJPainter extends PainterImpl {

    // The canvas
    protected HTMLCanvasElement _canvas;

    // The image dpi scale (1 = normal, 2 for retina/hidpi)
    protected int _scale;

    // The RenderContext2D
    protected CanvasRenderingContext2D _cntx;

    /**
     * Constructor for given canvas.
     */
    public CJPainter(HTMLCanvasElement aCnvs, int aScale)
    {
        // Set canvas, scale and context
        _canvas = aCnvs;
        _scale = aScale;
        _cntx = (CanvasRenderingContext2D) _canvas.getContext("2d");
        _cntx.setTransform(1, 0, 0, 1, 0, 0);

        // Clip to canvas bounds
        int canvasW = _canvas.getWidth();
        int canvasH = _canvas.getHeight();
        clipRect(0, 0, canvasW, canvasH);

        // If hidpi, scale default transform
        if (_scale > 1)
            _cntx.scale(_scale, _scale);
    }

    /**
     * Sets the paint in painter.
     */
    public void setPaint(Paint aPaint)
    {
        super.setPaint(aPaint);

        // Handle Color
        if (aPaint instanceof Color) {
            String cstr = CJ.getColorJS((Color) aPaint);
            _cntx.setFillStyle(cstr);
            _cntx.setStrokeStyle(cstr);
        }

        // Handle ImagePaint
        else if (aPaint instanceof ImagePaint) {
            CanvasPattern canvasPattern = CJ.getTextureJS((ImagePaint) aPaint, _cntx);
            _cntx.setFillStyle(canvasPattern);
        }

        // Handle Gradient
        else if (aPaint instanceof GradientPaint) {
            CanvasGradient canvasGradient = CJ.getGradientJS((GradientPaint) aPaint, _cntx);
            _cntx.setFillStyle(canvasGradient);
        }
    }

    /**
     * Sets the current stroke.
     */
    public void setStroke(Stroke aStroke)
    {
        // Let's be nice and map null to reasonable default
        if (aStroke == null) aStroke = Stroke.Stroke1;

        // Do normal version
        super.setStroke(aStroke);

        // Set LineWidth
        _cntx.setLineWidth(aStroke.getWidth());

        // Set DashArray null:, DashOffset
        double[] dashArray = aStroke.getDashArray();
        _cntx.setLineDash(dashArray);

        // Set DashOffset
        _cntx.setLineDashOffset(aStroke.getDashOffset());

        // Set cap
        switch (aStroke.getCap()) {
            case Round: _cntx.setLineCap("round"); break;
            case Butt: _cntx.setLineCap("butt"); break;
            case Square: _cntx.setLineCap("square"); break;
        }

        // Set join
        switch (aStroke.getJoin()) {
            case Miter:
                _cntx.setLineJoin("miter");
                _cntx.setMiterLimit(aStroke.getMiterLimit());
                break;
            case Round: _cntx.setLineJoin("round"); break;
            case Bevel: _cntx.setLineJoin("bevel"); break;
        }
    }

    /**
     * Sets the opacity.
     */
    public void setOpacity(double aValue)
    {
        super.setOpacity(aValue);
        _cntx.setGlobalAlpha(aValue);
    }

    /**
     * Sets the font in painter.
     */
    public void setFont(Font aFont)
    {
        super.setFont(aFont);
        _cntx.setFont(CJ.getFontJS(aFont));
    }

    /**
     * Sets the current transform.
     */
    public void setTransform(Transform aTrans)
    {
        super.setTransform(aTrans);
        double[] m = aTrans.getMatrix();

        // Set transform with dpi scale (in case retina/hidpi)
        _cntx.setTransform(m[0] * _scale, m[1], m[2], m[3] * _scale, m[4], m[5]);
    }

    /**
     * Transform painter.
     */
    public void transform(Transform aTrans)
    {
        super.transform(aTrans);
        double[] m = aTrans.getMatrix();
        _cntx.transform(m[0], m[1], m[2], m[3], m[4], m[5]);
    }

    /**
     * Draws a shape in painter.
     */
    public void draw(Shape aShape)
    {
        // If gradient is set, resize to given shape
        if (getPaint() instanceof GradientPaint)
            sizeGradientPaintToShape(aShape);

        if (aShape instanceof Rect) {
            Rect rect = (Rect) aShape;
            _cntx.strokeRect(rect.x, rect.y, rect.width, rect.height);
        }
        else {
            setShape(aShape);
            _cntx.stroke();
        }
    }

    /**
     * Draws a shape in painter.
     */
    public void fill(Shape aShape)
    {
        // If gradient is set, resize to given shape
        if (getPaint() instanceof GradientPaint)
            sizeGradientPaintToShape(aShape);

        if (aShape instanceof Rect) {
            Rect rect = (Rect) aShape;
            _cntx.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
        else {
            setShape(aShape);
            _cntx.fill();
        }
    }

    /**
     * Resets gradient paint to given shape.
     */
    private void sizeGradientPaintToShape(Shape aShape)
    {
        GradientPaint gradientPaint = (GradientPaint) getPaint();
        if (!gradientPaint.isAbsolute()) {
            GradientPaint gradientPaint2 = gradientPaint.copyForRect(aShape.getBounds());
            setPaint(gradientPaint2);
        }
    }

    /**
     * Clips a shape in painter.
     */
    public void clip(Shape aShape)
    {
        setShape(aShape);
        _cntx.clip();
    }

    /**
     * Sets a shape.
     */
    public void setShape(Shape aShape)
    {
        _cntx.beginPath();

        if (aShape instanceof Rect) {
            Rect rect = (Rect) aShape;
            _cntx.rect(rect.x, rect.y, rect.width, rect.height);
            return;
        }

        double[] pnts = new double[6];
        PathIter pathIter = aShape.getPathIter(null);
        while (pathIter.hasNext()) {
            switch (pathIter.getNext(pnts)) {
                case MoveTo: _cntx.moveTo(pnts[0], pnts[1]); break;
                case LineTo: _cntx.lineTo(pnts[0], pnts[1]); break;
                case CubicTo: _cntx.bezierCurveTo(pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]); break;
                case Close: _cntx.closePath(); break;
            }
        }
    }

    /**
     * Draw image with transform.
     */
    public void drawImage(Image anImg, Transform xform)
    {
        CanvasImageSource image = (CanvasImageSource) anImg.getNative();
        save();
        transform(xform);
        _cntx.drawImage(image, 0, 0);
        restore();
    }

    /**
     * Draw image in rect.
     */
    public void drawImage(Image anImg, double srcX, double srcY, double srcW, double srcH, double dx, double dy, double dw, double dh)
    {
        // Correct source width/height for image dpi
        double scaleX = anImg.getDpiX() / 72;
        double scaleY = anImg.getDpiY() / 72;
        if (scaleX != 1 || scaleY != 1) {
            srcX *= scaleX;
            srcW *= scaleX;
            srcY *= scaleY;
            srcH *= scaleY;
        }

        // Get points for corner as ints and draw image
        CanvasImageSource img = anImg instanceof CJImage ? (CanvasImageSource) anImg.getNative() : null;
        _cntx.drawImage(img, srcX, srcY, srcW, srcH, dx, dy, dw, dh);
    }

    /**
     * Draw string at location.
     */
    public void drawString(String aStr, double aX, double aY, double charSpacing)
    {
        // Handle no char spacing
        if (charSpacing == 0)
            _cntx.fillText(aStr, aX, aY);

            // Handle char spacing
        else {
            Font font = getFont();
            double x = aX;
            for (int i = 0, iMax = aStr.length(); i < iMax; i++) {
                char c = aStr.charAt(i);
                _cntx.fillText(String.valueOf(c), x, aY);
                x += font.charAdvance(c) + charSpacing;
            }
        }
    }

    /**
     * Stroke string at location.
     */
    @Override
    public void strokeString(String aStr, double aX, double aY, double charSpacing)
    {
        // Handle no char spacing
        if (charSpacing == 0)
            _cntx.strokeText(aStr, aX, aY);

            // Handle char spacing
        else {
            Font font = getFont();
            double charX = aX;
            for (int i = 0, iMax = aStr.length(); i < iMax; i++) {
                char c = aStr.charAt(i);
                _cntx.strokeText(String.valueOf(c), charX, aY);
                charX += font.charAdvance(c) + charSpacing;
            }
        }
    }

    /**
     * Clears a rect.
     */
    public void clearRect(double aX, double aY, double aW, double aH)
    {
        _cntx.clearRect(aX, aY, aW, aH);
    }

    /**
     * Standard clone implementation.
     */
    public void save()
    {
        super.save();
        _cntx.save();
    }

    /**
     * Disposes of the painter.
     */
    public void restore()
    {
        super.restore();
        _cntx.restore();
    }

    /**
     * Sets image rendering quality.
     */
    public void setImageQuality(double aValue)
    {
        if (snap.util.MathUtils.equals(aValue, getImageQuality())) return;
        super.setImageQuality(aValue);
        if (aValue > .67)
            _cntx.setImageSmoothingQuality("high");
        else if (aValue >.33)
            _cntx.setImageSmoothingQuality("medium");
        else _cntx.setImageSmoothingQuality("low");
        _cntx.setImageSmoothingEnabled(aValue > .33);
    }

    /**
     * Sets the composite mode.
     */
    public void setComposite(Composite aComp)
    {
        super.setComposite(aComp);
        switch (aComp) {
            case SRC_OVER: _cntx.setGlobalCompositeOperation("source-over"); break;
            case SRC_IN: _cntx.setGlobalCompositeOperation("source-in"); break;
            case DST_IN: _cntx.setGlobalCompositeOperation("destination-in"); break;
            case DST_OUT: _cntx.setGlobalCompositeOperation("destination-out"); break;
        }
    }

    /**
     * Paints PainterDVR2.
     */
    public void paintStacks(int[] instructionStack, int instructionStackSize, int[] intStack, double[] doubleStack, String[] stringStack, Object[] objectStack)
    {
        _cntx.paintStacks(_scale, instructionStack, instructionStackSize, intStack, doubleStack, stringStack, objectStack);
    }
}