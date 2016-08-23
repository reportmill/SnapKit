package snap.javafx;
import java.lang.reflect.Field;
import javafx.collections.ObservableList;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import snap.gfx.*;

/**
 * A class to hold utility methods to convert to/from Snap and JavaFX.
 */
public class JFX {

/**
 * Returns JFX Insets for snap Insets.
 */
public static javafx.geometry.Bounds get(Rect aRect)
{
    if(aRect==null) return null;
    return new javafx.geometry.BoundingBox(aRect.x, aRect.y, aRect.width, aRect.height);
}

/**
 * Returns snap Insets for JFX Insets.
 */
public static Rect get(javafx.geometry.Bounds aRect)
{
    if(aRect==null) return null;
    return new Rect(aRect.getMinX(), aRect.getMinY(), aRect.getWidth(), aRect.getHeight());
}

/**
 * Returns JFX Insets for snap Insets.
 */
public static javafx.geometry.Insets get(Insets theIns)
{
    if(theIns==null) return null;
    return new javafx.geometry.Insets(theIns.top, theIns.right, theIns.bottom, theIns.right);
}

/**
 * Returns snap Insets for JFX Insets.
 */
public static Insets get(javafx.geometry.Insets theIns)
{
    if(theIns==null) return null;
    return new Insets(theIns.getTop(), theIns.getRight(), theIns.getBottom(), theIns.getRight());
}

/**
 * Returns JFX Affine for snap Transform.
 */
public static Affine get(Transform aTrans)
{
    if(aTrans==null) return null;
    double m[] = aTrans.getMatrix();
    return new Affine(m[0], m[2], m[4], m[1], m[3], m[5]);
}

/**
 * Returns snap Transform for JFX Affine.
 */
public static Transform get(Affine aTrans)
{
    if(aTrans==null) return null;
    double m[] = aTrans.toArray(MatrixType.MT_2D_2x3);
    //return Transform.get(m[0], m[1], m[3], m[4], m[2], m[5]);
    return Transform.get(m[0], m[3], m[1], m[4], m[2], m[5]);
}

/**
 * Returns a JFX Color for snap Color.
 */
public static Color get(snap.gfx.Color aColor)
{
    if(aColor==null) return null;
    return new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), aColor.getAlpha());
}

/**
 * Returns a snap Color for JFX Color.
 */
public static snap.gfx.Color get(Color aColor)
{
    if(aColor==null) return null;
    return new snap.gfx.Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), aColor.getOpacity());
}

/**
 * Returns a JFX Paint for snap Paint.
 */
public static javafx.scene.paint.Paint get(Paint aPaint)
{
    if(aPaint==null) return null;
    if(aPaint instanceof snap.gfx.Color) return get((snap.gfx.Color)aPaint);
    if(aPaint instanceof GradientPaint) return get((GradientPaint)aPaint);
    if(aPaint instanceof ImagePaint) return get((ImagePaint)aPaint);
    throw new RuntimeException("JFX.get(paint): Paint not supported: " + aPaint);
}

/**
 * Returns a snap Paint for JFX Paint.
 */
public static Paint get(javafx.scene.paint.Paint aPaint)
{
    if(aPaint==null) return null;
    if(aPaint instanceof Color) return get((Color)aPaint);
    if(aPaint instanceof LinearGradient) return get((LinearGradient)aPaint);
    if(aPaint instanceof ImagePattern) return get((ImagePattern)aPaint);
    throw new RuntimeException("JFX.get(paint): Paint not supported: " + aPaint);
}

/**
 * Return a JFX Gradient for snap Gradient.
 */
public static javafx.scene.paint.Paint get(GradientPaint aGP)
{
    int count = aGP.getStopCount();
    javafx.scene.paint.Stop stops[] = new javafx.scene.paint.Stop[count];
    for(int i=0; i<count; i++)
        stops[i] = new javafx.scene.paint.Stop(aGP.getStopOffset(i), JFX.get(aGP.getStopColor(i)));
    
    // Handle Linear
    //if(aGradFill instanceof RMRadialGradientFill) { RMRadialGradientFill rg = (RMRadialGradientFill)aGradFill;
    //    return (V)new RadialGradient(rg.getFocusAngle(), 0, .5, .5, rg.getRadius(), true,
    //        CycleMethod.NO_CYCLE, stops); }
    
    // Handle Linear
    double sx = aGP.getStartX(), sy = aGP.getStartY(), ex = aGP.getEndX(), ey = aGP.getEndY();
    boolean prop = Math.abs(ex-sx)<2 && Math.abs(ey-sy)<2;  // So bogus
    return new javafx.scene.paint.LinearGradient(sx, sy, ex, ey, prop, javafx.scene.paint.CycleMethod.NO_CYCLE, stops);
}

/**
 * Returns a JFX ImagePattern for snap ImagePaint.
 */
public static ImagePattern get(ImagePaint anIP)
{
    double x = anIP.getX(), y = anIP.getY(), w = anIP.getWidth(), h = anIP.getHeight();
    return new ImagePattern(get(anIP.getImage()), x, y, w, h, !anIP.isAbsolute());
}

/**
 * Returns a snap ImagePaint for JFX ImagePattern
 */
public static ImagePaint get(ImagePattern anIP)
{
    double x = anIP.getX(), y = anIP.getY(), w = anIP.getWidth(), h = anIP.getHeight();
    return new ImagePaint(get(anIP.getImage()), x, y, w, h, !anIP.isProportional());
}

/**
 * Return a snap Gradient for JFX Gradient.
 */
public static GradientPaint get(LinearGradient aGP)
{
    int count = aGP.getStops().size();
    GradientPaint.Stop stops[] = new GradientPaint.Stop[count];
    for(int i=0; i<count; i++) { javafx.scene.paint.Stop stop = aGP.getStops().get(i);
        stops[i] = new GradientPaint.Stop(stop.getOffset(), JFX.get(stop.getColor())); }
    
    // Handle Linear
    //if(aGradFill instanceof RMRadialGradientFill) { RMRadialGradientFill rg = (RMRadialGradientFill)aGradFill;
    //    return (V)new RadialGradient(rg.getFocusAngle(), 0, .5, .5, rg.getRadius(), true,
    //        CycleMethod.NO_CYCLE, stops);
    //}
    
    // Handle Linear
    double sx = aGP.getStartX(), sy = aGP.getStartY(), ex = aGP.getEndX(), ey = aGP.getEndY();
    return new GradientPaint(sx, sy, ex, ey, stops);
}

/**
 * Returns a JFX Font for snap Font.
 */
public static Font get(snap.gfx.Font aFont)
{
    if(aFont==null) return null;
    FontPosture posture = aFont.isItalic()? FontPosture.ITALIC : FontPosture.REGULAR;
    FontWeight weight = aFont.isBold()? FontWeight.BOLD : FontWeight.NORMAL;
    return Font.font(aFont.getFamily(), weight, posture, aFont.getSize());
}

/**
 * Returns a snap Font for JFX Font.
 */
public static snap.gfx.Font get(Font aFont)
{
    if(aFont==null) return null;
    snap.gfx.Font font = snap.gfx.Font.get(aFont.getName(), aFont.getSize());
    return font;
}

/**
 * Returns a JFX Effect for snap Effect.
 */
public static javafx.scene.effect.Effect get(Effect anEff)
{
    if(anEff==null) return null;
    if(anEff instanceof ShadowEffect) return get((ShadowEffect)anEff);
    if(anEff instanceof EmbossEffect) return get((EmbossEffect)anEff);
    throw new RuntimeException("JFX.get(eff): Not supported: " + anEff.getClass().getName());
}

/**
 * Returns a JFX Effect for snap Effect.
 */
public static Effect get(javafx.scene.effect.Effect anEff)
{
    if(anEff instanceof DropShadow) return get((DropShadow)anEff);
    if(anEff instanceof Lighting) return get((Lighting)anEff);
    throw new RuntimeException("JFX.get(eff): Not supported: " + anEff.getClass().getName());
}

/**
 * Returns a JFX DropShadow for snap ShadowEffect.
 */
public static javafx.scene.effect.Effect get(ShadowEffect anEff)
{
    DropShadow shadow = new DropShadow();                                    // Create new shadow
    shadow.setRadius(anEff.getRadius());                                   // Set color and offset
    shadow.setColor(JFX.get(anEff.getColor()));
    shadow.setOffsetX(anEff.getDX()); shadow.setOffsetY(anEff.getDY());
    return shadow;   // Return shadow
}

/**
 * Returns a JFX DropShadow for snap ShadowEffect.
 */
public static Effect get(javafx.scene.effect.DropShadow anEff)
{
    return new ShadowEffect(anEff.getRadius(), JFX.get(anEff.getColor()), anEff.getOffsetX(), anEff.getOffsetY());
}

/**
 * Returns a JFX Lighting Effect for snap EmbossEffect.
 */
public static javafx.scene.effect.Effect get(EmbossEffect anEff)
{
    Light.Distant effect = new Light.Distant();                             // Set azimuth and elevation
    effect.setAzimuth(180-anEff.getAzimuth());
    effect.setElevation(180-anEff.getAltitude());
    Lighting lighting = new Lighting(); lighting.setSpecularConstant(.15);  // Create new lighting
    lighting.setLight(effect);
    lighting.setSurfaceScale(anEff.getRadius());
    return lighting;
}

/**
 * Returns a Snap EmbossEffect Effect for JFX Lighting.
 */
public static Effect get(Lighting anEff)
{
    Light light = anEff.getLight(); if(!(light instanceof Light.Distant)) return new EmbossEffect();
    Light.Distant dlight = (Light.Distant)light;
    return new EmbossEffect(180-dlight.getElevation(), 180-dlight.getAzimuth(), anEff.getSurfaceScale());
}

/**
 * Returns a JFX Image for snap Image.
 */
public static javafx.scene.image.Image get(Image anImage)  { return (javafx.scene.image.Image)anImage.getNative(); }

/**
 * Returns a snap Image for JFX Image.
 */
public static Image get(javafx.scene.image.Image anImage)  { return Image.get(anImage); }

/**
 * Returns a JFX HPos for snap HPos.
 */
public static javafx.geometry.HPos get(HPos aPos)
{
    switch(aPos) {
        case LEFT: return javafx.geometry.HPos.LEFT;
        case CENTER: return javafx.geometry.HPos.CENTER;
        case RIGHT: return javafx.geometry.HPos.RIGHT;
        default: throw new RuntimeException("JFX.get(HPos): Unsupported Pos " + aPos);
    }
}

/**
 * Returns a snap HPos for JavaFX HPos.
 */
public static HPos get(javafx.geometry.HPos aPos)
{
    switch(aPos) {
        case LEFT: return HPos.LEFT;
        case CENTER: return HPos.CENTER;
        case RIGHT: return HPos.RIGHT;
        default: throw new RuntimeException("JFX.get(HPos): Unsupported Pos " + aPos);
    }
}

/**
 * Returns a JFX VPos for snap VPos.
 */
public static javafx.geometry.VPos get(VPos aPos)
{
    switch(aPos) {
        case TOP: return javafx.geometry.VPos.TOP;
        case CENTER: return javafx.geometry.VPos.CENTER;
        case BOTTOM: return javafx.geometry.VPos.BOTTOM;
        default: throw new RuntimeException("JFX.get(VPos): Unsupported Pos " + aPos);
    }
}

/**
 * Returns a snap VPos for JavaFX VPos.
 */
public static VPos get(javafx.geometry.VPos aPos)
{
    switch(aPos) {
        case TOP: return VPos.TOP;
        case CENTER: return VPos.CENTER;
        case BOTTOM: return VPos.BOTTOM;
        default: throw new RuntimeException("JFX.get(HPos): Unsupported Pos " + aPos);
    }
}

/**
 * Returns a JFX Pos for snap Pos.
 */
public static javafx.geometry.Pos get(Pos aPos)
{
    switch(aPos) {
        case TOP_LEFT: return javafx.geometry.Pos.TOP_LEFT;
        case TOP_CENTER: return javafx.geometry.Pos.TOP_CENTER;
        case TOP_RIGHT: return javafx.geometry.Pos.TOP_RIGHT;
        case CENTER_LEFT: return javafx.geometry.Pos.CENTER_LEFT;
        case CENTER: return javafx.geometry.Pos.CENTER;
        case CENTER_RIGHT: return javafx.geometry.Pos.CENTER_RIGHT;
        case BOTTOM_LEFT: return javafx.geometry.Pos.BOTTOM_LEFT;
        case BOTTOM_CENTER: return javafx.geometry.Pos.BOTTOM_CENTER;
        case BOTTOM_RIGHT: return javafx.geometry.Pos.BOTTOM_RIGHT;
        default: throw new RuntimeException("JFX.get(Pos): Unsupported Pos " + aPos);
    }
}

/**
 * Returns a snap Pos for JavaFX Pos.
 */
public static Pos get(javafx.geometry.Pos aPos)
{
    HPos hp = get(aPos.getHpos()); VPos vp = get(aPos.getVpos()); return Pos.get(hp,vp);
}

/**
 * Returns a JFX Path for a Snap Path.
 */
public static javafx.scene.shape.Shape get(snap.gfx.Shape aShape)
{
    if(aShape instanceof Rect) { Rect rect = (Rect)aShape;
        return new javafx.scene.shape.Rectangle(rect.x,rect.y,rect.width,rect.height); }
    return getPath(aShape);
}

/**
 * Returns a JFX Path for a Snap Path.
 */
public static javafx.scene.shape.Path getPath(snap.gfx.Shape aShape)
{
    // Create new path
    javafx.scene.shape.Path path = new javafx.scene.shape.Path();
    ObservableList <PathElement> ops = path.getElements();
    
    // Iterate over path elements    
    PathIter piter = aShape.getPathIter(null); double pts[] = new double[6];
    while(piter.hasNext()) {
        switch(piter.getNext(pts)) {
            case MoveTo: ops.add(new javafx.scene.shape.MoveTo(pts[0], pts[1])); break;
            case LineTo: ops.add(new javafx.scene.shape.LineTo(pts[0], pts[1])); break;
            case QuadTo: ops.add(new QuadCurveTo(pts[0], pts[1], pts[2], pts[3])); break;
            case CubicTo: ops.add(new CubicCurveTo(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5])); break;
            case Close: ops.add(new ClosePath()); break;
            default: break;
        }
    }
    
    // Return the path
    return path;
}

/**
 * Returns a snap path for given JFX Shape.
 */
public static snap.gfx.Shape get(javafx.scene.shape.Shape aShape)
{
    if(aShape instanceof javafx.scene.shape.Rectangle) {
        javafx.scene.shape.Rectangle rect = (javafx.scene.shape.Rectangle)aShape;
        return new Rect(rect.getX(), rect.getY(),rect.getWidth(),rect.getHeight());
    }
    
    if(aShape instanceof javafx.scene.shape.Path) {
        javafx.scene.shape.Path path = (javafx.scene.shape.Path)aShape;
        snap.gfx.Path path2 = new snap.gfx.Path();
        for(PathElement pe : path.getElements()) {
            if(pe instanceof MoveTo) { MoveTo mt = (MoveTo)pe;
                path2.moveTo(mt.getX(),mt.getY()); }
            else if(pe instanceof LineTo) { LineTo mt = (LineTo)pe;
                path2.lineTo(mt.getX(),mt.getY()); }
            else if(pe instanceof QuadCurveTo) { QuadCurveTo mt = (QuadCurveTo)pe;
                path2.quadTo(mt.getControlX(),mt.getControlY(),mt.getX(),mt.getY()); }
            else if(pe instanceof CubicCurveTo) { CubicCurveTo c = (CubicCurveTo)pe;
                path2.curveTo(c.getControlX1(),c.getControlY1(),c.getControlX2(),c.getControlY2(),c.getX(),c.getY()); }
            else if(pe instanceof ClosePath) path2.close();
        }
        return path2;
    }
    
    if(aShape!=null) System.err.println("JFX.get(): Unsupported shape " + aShape);
    return null;
}

/** Returns awt Cursor for snap cursor. */
public static javafx.scene.Cursor get(snap.view.Cursor aCursor)
{
    if(aCursor==null) return null;
    String name = aCursor.getName();
    Field field = null; try { field = javafx.scene.Cursor.class.getField(name); } catch(Exception e) { }
    if(field!=null) try { return (javafx.scene.Cursor)field.get(null); }
    catch(Exception e) { }
    System.err.println("JFX: get(snap.view.Cursor): Cursor not found for name: " + name);
    return javafx.scene.Cursor.DEFAULT;
}

/** Returns snap Cursor awt snap cursor. */
public static snap.view.Cursor get(javafx.scene.Cursor aCursor)
{
    if(aCursor==null) return snap.view.Cursor.NONE;
    String name = aCursor.toString();
    Field field = null; try { field = snap.view.Cursor.class.getField(name); } catch(Exception e) { }
    if(field!=null) try { return (snap.view.Cursor)field.get(null); }
    catch(Exception e) { }
    System.err.println("JFX: get(javafx.scene.Cursor): Cursor not found for name: " + name);
    return snap.view.Cursor.DEFAULT;
}

/**
 * Returns a Snap keycode for JFX key code.
 */
public static int get(KeyCode aKC)
{
    int kcode = _keyCodes[aKC.ordinal()]; if(kcode>0) return kcode;
    return _keyCodes[aKC.ordinal()] = snap.view.KeyCode.get(aKC.toString());
}

// Holds cached JFX KeyCode.ordinal to Snap keycodes.
private static int[] _keyCodes = new int[KeyCode.values().length];

}