package snap.javafx;
import java.util.Arrays;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import snap.gfx.*;

/**
 * A Painter implementation that uses JavaFX Canvas.
 */
public class JFXPainter extends Painter {
    
    // The canvas
    Canvas           _cnvs;
    
    // The graphics context
    GraphicsContext  _gc;
    
    // The writable image, if provided
    WritableImage    _image;
    
    // The current clip
    Shape            _clip;
    
    // The stack of clips
    Shape            _clips[] = new Shape[8];
    
    // The clip stack size
    int              _clipCount;

/**
 * Creates a new Painter.
 */
public JFXPainter(Canvas aCnvs)
{
    _cnvs = aCnvs; _gc = aCnvs.getGraphicsContext2D();
    _clip = new Rect(0, 0, aCnvs.getWidth(), aCnvs.getHeight());
    _gc.setFontSmoothingType(FontSmoothingType.LCD);
}

/**
 * Creates a new Painter with given WritableImage.
 */
public JFXPainter(WritableImage anImage)
{
    _image = anImage;
    _cnvs = new Canvas(anImage.getWidth(), anImage.getHeight());
    _gc = _cnvs.getGraphicsContext2D();
    _clip = new Rect(0, 0, _cnvs.getWidth(), _cnvs.getHeight());
    _gc.drawImage(_image,0,0);
}

/**
 * Returns the current font.
 */
public Font getFont()  { return JFX.get(_gc.getFont()); }

/**
 * Sets the current font.
 */
public void setFont(Font aFont)  { _gc.setFont(JFX.get(aFont)); }

/**
 * Returns the current paint.
 */
public Paint getPaint()  { return JFX.get(_gc.getFill()); }

/**
 * Sets the current paint.
 */
public void setPaint(Paint aPaint)  { javafx.scene.paint.Paint p = JFX.get(aPaint); _gc.setFill(p); _gc.setStroke(p); }

/**
 * Returns the current stroke.
 */
public Stroke getStroke()
{
    return new Stroke(_gc.getLineWidth(), _gc.getLineDashes(), _gc.getLineDashOffset());
}

/**
 * Sets the current stroke.
 */
public void setStroke(Stroke aStroke)
{
    _gc.setLineWidth(aStroke.getWidth());
    float da[] = aStroke.getDashArray();
    double da2[] = null; if(da!=null) { da2 = new double[da.length]; for(int i=0;i<da.length;i++) da2[i] = da[i]; }
    _gc.setLineDashes(da2);
    _gc.setLineDashOffset(aStroke.getDashPhase());
}

/**
 * Returns the opacity.
 */
public double getOpacity()  { return _gc.getGlobalAlpha(); }

/**
 * Sets the opacity.
 */
public void setOpacity(double aValue)  { _gc.setGlobalAlpha(aValue); }

/**
 * Clears a rect.
 */
public void clearRect(double aX, double aY, double aW, double aH) { _gc.clearRect(aX,aY,aW,aH); }

/**
 * Stroke the given shape.
 */
public void draw(Shape aShape)  { setPath(aShape); _gc.stroke(); }

/**
 * Fill the given shape.
 */
public void fill(Shape aShape)  { setPath(aShape); _gc.fill(); }

/**
 * Sets a path.
 */
public void setPath(Shape aShape)
{
    _gc.beginPath();
    PathIter piter = aShape.getPathIter(null); double pts[] = new double[6];
    while(piter.hasNext()) switch(piter.getNext(pts)) {
        case MoveTo: _gc.moveTo(pts[0],pts[1]); break;
        case LineTo: _gc.lineTo(pts[0],pts[1]); break;
        case QuadTo: _gc.quadraticCurveTo(pts[0],pts[1],pts[2],pts[3]); break;
        case CubicTo: _gc.bezierCurveTo(pts[0],pts[1],pts[2],pts[3],pts[4],pts[5]); break;
        case Close: _gc.closePath();
    }
}

/**
 * Draw image in rect.
 */
public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
{
    // Correct source width/height for image dpi
    if(img.getWidthDPI()!=72) sw *= img.getWidthDPI()/72;
    if(img.getHeightDPI()!=72) sh *= img.getHeightDPI()/72;
    
    // Get native image and draw
    javafx.scene.image.Image img2 = (javafx.scene.image.Image)img.getNative();
    _gc.drawImage(img2, sx, sy, sw, sh, dx, dy, dw, dh);
}

/**
 * Draw string at location with char spacing.
 */
public void drawString(String aStr, double aX, double aY, double cs)
{
    // If char spacing is non-zero, paint each char
    if(cs!=0) {
        double x = aX;
        for(int i=0,iMax=aStr.length();i<iMax; i++) { char c = aStr.charAt(i);
            _gc.fillText(String.valueOf(c), x, aY); x += getFont().getCharAdvance(c,true) + cs; }
    }
    
    // Otherwise just fill text
    else _gc.fillText(aStr, aX, aY);
    
}

/**
 * Returns the current transform.
 */
public Transform getTransform()  { return JFX.get(_gc.getTransform()); }

/**
 * Sets the current transform.
 */
public void setTransform(Transform aTrans)
{
    if(_clip!=null) _clip = _clip.copyFor(getTransform());
    _gc.setTransform(JFX.get(aTrans));
    if(_clip!=null) _clip = _clip.copyFor(aTrans.getInverse());
}

/**
 * Transform by transform.
 */
public void transform(Transform aTrans)
{
    _gc.transform(JFX.get(aTrans));
    if(_clip!=null) _clip = _clip.copyFor(aTrans.getInverse());
}

/**
 * Return clip shape.
 */
public Shape getClip()  { return _clip; }

/**
 * Clip by shape.
 */
public void clip(Shape aShape)
{
    setPath(aShape);
    _gc.clip();
    _clip = _clip!=null? Shape.intersect(_clip, aShape) : aShape;
}

/**
 * Sets whether antialiasing.
 */
public boolean setAntialiasing(boolean aValue)  { return false; }

/**
 * Sets image rendering quality.
 */
public void setImageQuality(double aValue)  { }

/**
 * Standard clone implementation.
 */
public void save()
{
    _gc.save();
    if(_clipCount==_clips.length) _clips = Arrays.copyOf(_clips, _clips.length*2);
    _clips[_clipCount++] = _clip;
}

/**
 * Disposes this painter.
 */
public void restore()
{
    _gc.restore();
    _clip = _clips[--_clipCount];
}

/**
 * Override to copy canvas to image, if needed.
 */
public void flush()
{
    if(_image==null) return;
    SnapshotParameters sp = new SnapshotParameters(); sp.setFill(Color.TRANSPARENT);
    _cnvs.snapshot(sp, _image);
}

/**
 * Override to disable warning.
 */
public void applyEffect(Effect anEff, Rect eBounds)  { }

}