package snap.swing;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.gfx.*;

/**
 * A Painter implementation that uses Java2D (Graphics2D).
 */
public class J2DPainter extends Painter {

    // The Stroke
    private Stroke  _stroke = new Stroke(1);

    // The Font
    private Font  _font = Font.Arial12;

    // The Graphics
    private Graphics2D  _gfx;
    
    // The graphics stack
    private Graphics2D  _gfxs[] = new Graphics2D[8];
    
    // The size of graphics stack
    private int  _gsize;

    /**
     * Creates a new J2DPainter.
     */
    public J2DPainter(Graphics aGr)
    {
        _gfx = (Graphics2D)aGr.create();
        setAntialiasing(true);
        setAntialiasingText(true);
        setFractionalMetrics(true);
        //_gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //_gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Initialize clip to Graphics clip
        clip(AWT.awtToSnapShape(aGr.getClip()));
    }

    /**
     * Returns the current paint.
     */
    public Paint getPaint()
    {
        return AWT.awtToSnapPaint(_gfx.getPaint());
    }

    /**
     * Sets the paint in painter.
     */
    public void setPaint(Paint aPaint)
    {
        _gfx.setPaint(AWT.snapToAwtPaint(aPaint));
    }

    /**
     * Returns the current stroke.
     */
    public Stroke getStroke()  { return _stroke; }

    /**
     * Sets the stroke in painter.
     */
    public void setStroke(Stroke aStroke)
    {
        _gfx.setStroke(AWT.snapToAwtStroke(aStroke));
        _stroke = aStroke;
    }

    /**
     * Returns the opacity.
     */
    public double getOpacity()
    {
        java.awt.Composite comp = _gfx.getComposite();
        if (comp instanceof AlphaComposite)
            return ((AlphaComposite)comp).getAlpha();
        return 1;
    }

    /**
     * Sets the opacity in painter.
     */
    public void setOpacity(double aValue)
    {
        java.awt.Composite comp = _gfx.getComposite();
        if (comp instanceof AlphaComposite)
            comp = ((AlphaComposite)comp).derive((float)aValue);
        else comp = AlphaComposite.SrcOver.derive((float)aValue);
       _gfx.setComposite(comp);
    }

    /**
     * Returns the Composite.
     */
    public Composite getComposite()
    {
        java.awt.Composite comp = _gfx.getComposite();
        int rule = comp instanceof AlphaComposite ? ((AlphaComposite)comp).getRule() : AlphaComposite.SRC_OVER;
        switch(rule) {
            case AlphaComposite.SRC_IN: return Composite.SRC_IN;
            case AlphaComposite.DST_IN: return Composite.DST_IN;
            case AlphaComposite.DST_OUT: return Composite.DST_OUT;
            default: return Composite.SRC_OVER;
        }
    }

    /**
     * Sets the composite mode.
     */
    public void setComposite(Composite aComp)
    {
        float opac = (float)getOpacity();
        switch(aComp) {
            case SRC_OVER: _gfx.setComposite(AlphaComposite.SrcOver.derive(opac)); break;
            case SRC_IN: _gfx.setComposite(AlphaComposite.SrcIn.derive(opac)); break;
            case DST_IN: _gfx.setComposite(AlphaComposite.DstIn.derive(opac)); break;
            case DST_OUT: _gfx.setComposite(AlphaComposite.DstOut.derive(opac)); break;
        }
    }

    /**
     * Returns the current font.
     */
    public Font getFont()  { return _font; }

    /**
     * Sets the font in painter.
     */
    public void setFont(Font aFont)
    {
        _gfx.setFont(AWT.snapToAwtFont(aFont));
        _font = aFont;
    }

    /**
     * Clears a rect.
     */
    public void clearRect(double aX, double aY, double aW, double aH)  { }

    /**
     * Draws a shape in painter.
     */
    public void draw(Shape aShape)
    {
        _gfx.draw(AWT.snapToAwtShape(aShape));
    }

    /**
     * Fills a shape in painter.
     */
    public void fill(Shape aShape)
    {
        _gfx.fill(AWT.snapToAwtShape(aShape));
    }

    /**
     * Draw image with transform.
     */
    public void drawImage(Image anImg, Transform xform)
    {
        _gfx.drawImage(AWT.snapToAwtImage(anImg), AWT.snapToAwtTrans(xform), null);
    }

    /**
     * Draw image in rect.
     */
    public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
    {
        // Correct source width/height for image dpi
        if (img.getDPIX()!=72) sw *= img.getDPIX()/72;
        if (img.getDPIY()!=72) sh *= img.getDPIY()/72;

        // Get points for corner as ints and draw image
        int sx1 = rnd(sx), sy1 = rnd(sy), sx2 = sx1 + rnd(sw), sy2 = sy1 + rnd(sh);
        int dx1 = rnd(dx), dy1 = rnd(dy), dx2 = dx1 + rnd(dw), dy2 = dy1 + rnd(dh);
        _gfx.drawImage(AWT.snapToAwtImage(img), dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    /**
     * Draw string at location.
     */
    public void drawString(String aStr, double aX, double aY, double cs)
    {
        // Handle no char spacing
        if (cs==0)
            _gfx.drawString(aStr, (float)aX, (float)aY);

        // Handle char spacing
        else {

            // Get font render context
            Graphics2D g2d = _gfx; //str = str.trim();
            FontRenderContext c = g2d.getFontRenderContext();
            Font font = getFont();
            GlyphVector gv = g2d.getFont().createGlyphVector(c, aStr);

            // Adjust glyph positions for char spacing
            Point2D.Double p = new Point2D.Double(0, 0);
            for (int i=0, iMax=aStr.length()-1; i<iMax; i++) {
                double adv = font.charAdvance(aStr.charAt(i)); //if (!getUseFractionalMetrics()) adv = Math.ceil(adv);
                p.x += adv + cs;
                gv.setGlyphPosition(i+1, p);
            }

            // Return glyph vector
            g2d.drawGlyphVector(gv, (float)aX, (float)aY);
        }
    }

    /**
     * Return string bounds.
     */
    public Rect getStringBounds(String aStr)
    {
        Rectangle2D r = _gfx.getFont().getStringBounds(aStr, _gfx.getFontRenderContext());
        return new Rect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Returns the current transform.
     */
    public Transform getTransform()
    {
        return AWT.awtToSnapTrans(_gfx.getTransform());
    }

    /**
     * Sets the current transform.
     */
    public void setTransform(Transform aTrans)
    {
        _gfx.setTransform(AWT.snapToAwtTrans(aTrans));
    }

    /**
     * Transform painter.
     */
    public void transform(Transform aTrans)
    {
        _gfx.transform(AWT.snapToAwtTrans(aTrans));
    }

    /**
     * Return clip shape.
     */
    public Shape getClip()
    {
        return AWT.awtToSnapShape(_gfx.getClip());
    }

    /**
     * Clip by shape.
     */
    public void clip(Shape s)
    {
        _gfx.clip(AWT.snapToAwtShape(s));
    }

    /**
     * Returns whether antialiasing.
     */
    public boolean isAntialiasing()
    {
        return _gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING)==RenderingHints.VALUE_ANTIALIAS_ON;
    }

    /**
     * Sets whether antialiasing.
     */
    public boolean setAntialiasing(boolean aValue)
    {
        boolean old = isAntialiasing();
        if (aValue)
            _gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        else _gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        return old;
    }

    /**
     * Returns whether antialiasing.
     */
    public boolean isAntialiasingText()
    {
        return _gfx.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING)==RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
    }

    /**
     * Sets whether antialiasing.
     */
    public boolean setAntialiasingText(boolean aValue)
    {
        boolean old = isAntialiasingText();
        if (aValue)
            _gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        else _gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        return old;
    }

    /**
     * Returns whether using fractional text metrics.
     */
    public boolean isFractionalTextMetrics()
    {
        return _gfx.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS)==RenderingHints.VALUE_FRACTIONALMETRICS_ON;
    }

    /**
     * Sets whether using fractional text metrics.
     */
    public boolean setFractionalMetrics(boolean aValue)
    {
        boolean old = isFractionalTextMetrics();
        if (aValue)
            _gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        else _gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        return old;
    }

    /**
     * Sets image rendering quality.
     */
    public void setImageQuality(double aValue)
    {
        // Do normal version
        super.setImageQuality(aValue);

        // If above 2/3: BI-CUBIC interpolation
        if (aValue>.67)
            _gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // If above 1/3: Bilinear
        else if (aValue>.33)
            _gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Nearest neighbor
        else _gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    /**
     * Sets whether stroke is rounded to nearest pixel.
     */
    public void setStrokePure(boolean aValue)
    {
        // Do normal version
        super.setStrokePure(aValue);

        // If true, set STROKE_CONTROL to STROKE_PURE
        if (aValue)
            _gfx.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        else _gfx.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    }

    /**
     * Standard clone implementation.
     */
    public void save()
    {
        if (_gsize==_gfxs.length)
            _gfxs = Arrays.copyOf(_gfxs, _gfxs.length*2);
        _gfxs[_gsize++] = _gfx;
        _gfx = (Graphics2D) _gfx.create();
    }

    /**
     * Disposes of the painter.
     */
    public void restore()
    {
        _gfx = _gfxs[--_gsize];
    }

    /**
     * Returns the Graphics.
     */
    public Graphics2D getNative()  { return _gfx; }

    /** Rounds a value. */
    private static final int rnd(double aVal)  { return (int)Math.round(aVal); }
}