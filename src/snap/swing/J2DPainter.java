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
import snap.gfx.*;

/**
 * A Painter implementation that uses Java2D (Graphics2D).
 */
public class J2DPainter extends Painter {
    
    // The Graphics
    Graphics2D    _gfx;
    
    // The graphics stack
    Graphics2D    _gfxs[] = new Graphics2D[8];
    
    // The size of graphics stack
    int           _gsize;

/**
 * Creates a new J2DPainter.
 */
public J2DPainter(Graphics aGr)
{
    _gfx = (Graphics2D)aGr.create();
    setAntialiasing(true); setAntialiasingText(true); setFractionalMetrics(true);
    _gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    _gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    _gfx.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    
    // Initialize clip to Graphics clip
    clip(AWT.get(aGr.getClip()));
}

/**
 * Returns the current paint.
 */
public Paint getPaint()  { return AWT.get(_gfx.getPaint()); }

/**
 * Sets the paint in painter.
 */
public void setPaint(Paint aPaint)  { _gfx.setPaint(AWT.get(aPaint)); }

/**
 * Returns the current stroke.
 */
public Stroke getStroke()  { return _stroke; } Stroke _stroke = new Stroke(1);

/**
 * Sets the stroke in painter.
 */
public void setStroke(Stroke aStroke)  { _gfx.setStroke(AWT.get(aStroke)); _stroke = aStroke; }

/**
 * Returns the opacity.
 */
public double getOpacity()  { return _opacity; } double _opacity = 1;

/**
 * Sets the opacity in painter.
 */
public void setOpacity(double aValue)
{
   _gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)aValue)); _opacity = aValue;
}

/**
 * Returns the current font.
 */
public Font getFont()  { return _font; } Font _font = Font.Arial12;

/**
 * Sets the font in painter.
 */
public void setFont(Font aFont)  {_gfx.setFont(AWT.get(aFont)); _font = aFont; }

/**
 * Clears a rect.
 */
public void clearRect(double aX, double aY, double aW, double aH)  { }

/**
 * Draws a shape in painter.
 */
public void draw(Shape aShape)  { _gfx.draw(AWT.get(aShape)); }

/**
 * Fills a shape in painter.
 */
public void fill(Shape aShape)  { _gfx.fill(AWT.get(aShape)); }

/**
 * Draw image with transform.
 */
public void drawImage(Image anImg, Transform xform)  { _gfx.drawImage(AWT.get(anImg), AWT.get(xform), null); }

/**
 * Draw image in rect.
 */
public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
{
    // Correct source width/height for image dpi
    if(img.getWidthDPI()!=72) sw *= img.getWidthDPI()/72;
    if(img.getHeightDPI()!=72) sh *= img.getHeightDPI()/72;
    
    // Get points for corner as ints and draw image
    int sx1 = rnd(sx), sy1 = rnd(sy), sx2 = sx1 + rnd(sw), sy2 = sy1 + rnd(sh);
    int dx1 = rnd(dx), dy1 = rnd(dy), dx2 = dx1 + rnd(dw), dy2 = dy1 + rnd(dh);
    _gfx.drawImage(AWT.get(img), dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
}

/**
 * Draw string at location.
 */
public void drawString(String aStr, double aX, double aY, double cs)
{
    // Handle no char spacing
    if(cs==0)
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
        for(int i=0, iMax=aStr.length()-1; i<iMax; i++) {
            double adv = font.charAdvance(aStr.charAt(i)); //if(!getUseFractionalMetrics()) adv = Math.ceil(adv);
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
public Transform getTransform()  { return AWT.get(_gfx.getTransform()); }

/**
 * Sets the current transform.
 */
public void setTransform(Transform aTrans)  { _gfx.setTransform(AWT.get(aTrans)); }

/**
 * Transform painter.
 */
public void transform(Transform aTrans)  { _gfx.transform(AWT.get(aTrans)); }

/**
 * Return clip shape.
 */
public Shape getClip()  { return AWT.get(_gfx.getClip()); }

/**
 * Clip by shape.
 */
public void clip(Shape s)  { _gfx.clip(AWT.get(s)); }

/**
 * Sets the composite mode.
 */
public void setComposite(Composite aComp)
{
    switch(aComp) {
        case SRC_OVER: _gfx.setComposite(AlphaComposite.SrcOver); break;
        case SRC_IN: _gfx.setComposite(AlphaComposite.SrcIn); break;
        case DST_IN: _gfx.setComposite(AlphaComposite.DstIn); break;
    }
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
    if(aValue) _gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
    if(aValue) _gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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
    if(aValue) _gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    else _gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    return old;
}

/**
 * Sets image rendering quality.
 */
public void setImageQuality(double aValue)
{
    if(aValue>.67) _gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    else if(aValue>.33)
        _gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    else _gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
}

/**
 * Standard clone implementation.
 */
public void save()
{
    if(_gsize==_gfxs.length) _gfxs = Arrays.copyOf(_gfxs, _gfxs.length*2);
    _gfxs[_gsize++] = _gfx; _gfx = (Graphics2D)_gfx.create();
}

/**
 * Disposes of the painter.
 */
public void restore()  { _gfx = _gfxs[--_gsize]; }

/**
 * Returns the Graphics.
 */
public Graphics2D getNative()  { return _gfx; }

/** Rounds a value. */
private static final int rnd(double aVal)  { return (int)Math.round(aVal); }

}