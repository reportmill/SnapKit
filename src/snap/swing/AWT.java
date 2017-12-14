package snap.swing;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;

/**
 * A class to return AWT versions of Snap graphics objects.
 */
public class AWT {

/** Returns an awt point for snap point. */
public static java.awt.geom.Point2D get(Point p)  { return new java.awt.geom.Point2D.Double(p.getX(),p.getY()); }

/** Returns an awt Dimension for snap size. */
public static java.awt.Dimension get(Size s)  { return new java.awt.Dimension((int)s.getWidth(),(int)s.getHeight()); }

/** Returns awt image for snap image. */
public static Rectangle2D get(Rect r)  { return new Rectangle2D.Double(r.getX(),r.getY(),r.getWidth(),r.getHeight()); }

/** Returns awt image for snap image. */
public static Rect get(Rectangle2D r)  { return new Rect(r.getX(),r.getY(),r.getWidth(),r.getHeight()); }

/** Returns awt shape for snap shape. */
public static java.awt.Shape get(Shape aSC)
{
    if(aSC instanceof Rect) return get((Rect)aSC);
    if(aSC instanceof SnapShape) return ((SnapShape)aSC)._shp;
    return aSC!=null? new AWTShape(aSC) : null;
}

/** Returns awt shape for snap shape. */
public static Shape get(java.awt.Shape aSC)
{
    if(aSC instanceof Rectangle2D) return get((Rectangle2D)aSC);
    if(aSC instanceof AWTShape) return ((AWTShape)aSC)._shp;
    return aSC!=null? new SnapShape(aSC) : null;
}

/** Returns awt shape for snap shape. */
public static PathIterator get(PathIter aPI)  { return new AWTPathIter(aPI); }

/** Returns awt shape for snap shape. */
public static PathIter get(PathIterator aPI)  { return new SnapPathIter(aPI); }

/** Returns awt tranform for snap transform. */
public static AffineTransform get(Transform aTrans)
{ double m[] = new double[6]; aTrans.getMatrix(m); return new AffineTransform(m); }

/** Returns awt tranform for snap transform. */
public static Transform get(AffineTransform aTrans)
{ double m[] = new double[6]; aTrans.getMatrix(m); return new Transform(m); }

/** Returns awt paint for snap paint. */
public static java.awt.Paint get(Paint aSP)
{
    if(aSP==null) return null;
    if(aSP instanceof Color) return get((Color)aSP);
    if(aSP instanceof GradientPaint) return get((GradientPaint)aSP);
    if(aSP instanceof ImagePaint) return get((ImagePaint)aSP);
    throw SnapUtils.notImpl(new AWT(), "Can't convert paint " + aSP);
}

/** Returns snap Paint for awt Paint. */
public static Paint get(java.awt.Paint aSP)
{
    if(aSP==null) return null;
    if(aSP instanceof java.awt.Color) return get((java.awt.Color)aSP);
    //if(aSP instanceof GradientPaintX) return get((GradientPaintX)aSP);
    //if(aSP instanceof java.awt.TexturePaint) return get((java.awt.TexturePaint)aSP);
    throw SnapUtils.notImpl(new AWT(), "Can't convert paint " + aSP);
}

/** Returns awt paint for snap paint. */
public static java.awt.Color get(Color aSC)  { return aSC!=null? new java.awt.Color(aSC.getRGBA(), true) : null; }

/** Returns awt paint for snap paint. */
public static Color get(java.awt.Color aC)  { return new Color(aC.getRGB()); }

/** Returns awt paint for snap paint. */
public static GradientPaintX get(GradientPaint aGP)  { return new GradientPaintX(aGP); }

/** Returns awt TexturePaint for snap TexturePaint. */
public static java.awt.TexturePaint get(ImagePaint aTP)
{
    BufferedImage bi = AWTImageUtils.getBufferedImage((java.awt.Image)aTP.getImage().getNative());
    return new java.awt.TexturePaint(bi, get(aTP.getBounds()));
}

/** Returns awt stroke for snap stroke. */
public static java.awt.Stroke get(Stroke aStroke)
{
    float width = (float)aStroke.getWidth(), miter = (float)aStroke.getMiterLimit();
    int cap = get(aStroke.getCap()), join = get(aStroke.getJoin());
    float dary[] = ArrayUtils.getFloats(aStroke.getDashArray()), offset = (float)aStroke.getDashOffset();
    return new BasicStroke(width, cap, join, miter, dary, offset);
}

/** Return awt Stroke Cap for snap Stroke.Cap. */
static int get(Stroke.Cap aCap)
{
    return aCap==Stroke.Cap.Butt? BasicStroke.CAP_BUTT : aCap==Stroke.Cap.Round? BasicStroke.CAP_ROUND :
        BasicStroke.CAP_SQUARE;
}

/** Return awt Stroke Join for snap Stroke.Join. */
static int get(Stroke.Join aJoin)
{
    return aJoin==Stroke.Join.Miter? BasicStroke.JOIN_MITER : aJoin==Stroke.Join.Round? BasicStroke.JOIN_ROUND :
        BasicStroke.JOIN_BEVEL;
}

/** Returns awt font for snap font. */
public static java.awt.Font get(Font aFont)
{
    Object ntv = aFont.getNative(); if(ntv instanceof java.awt.Font) return (java.awt.Font)ntv;
    return AWTFontUtils.getFont(aFont.getName(), aFont.getSize());
}

/** Returns awt image for snap image. */
public static java.awt.Image get(Image anImage)  { return (java.awt.Image)anImage.getNative(); }

/** Returns awt image for snap image. */
public static Image get(java.awt.Image anImage)  { return Image.get(anImage); }

/** Returns awt Cursor for snap cursor. */
public static java.awt.Cursor get(snap.view.Cursor aCursor)
{
    if(aCursor==null) return null;
    String name = aCursor.getName(); name = name + "_CURSOR";
    if(name.equals("NONE_CURSOR")) return getHideCursor();
    Field field = null; try { field = java.awt.Cursor.class.getField(name); }
    catch(Exception e) { }
    if(field!=null) try { return java.awt.Cursor.getPredefinedCursor((int)field.get(null)); }
    catch(Exception e) { }
    System.err.println("AWT: get(snap.view.Cursor): Cursor not found for name: " + name);
    return java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR);
}

/** Returns snap Cursor awt snap cursor. */
public static snap.view.Cursor get(java.awt.Cursor aCursor)
{
    if(aCursor==null) return snap.view.Cursor.NONE;
    String name = aCursor.getName(); name = name.toUpperCase(); if(name.equals("NONE")) return snap.view.Cursor.NONE;
    name = name.replace(" ", "_"); name = name.replace("_CURSOR", "");
    Field field = null; try { field = snap.view.Cursor.class.getField(name); }
    catch(Exception e) { }
    if(field!=null) try { return (snap.view.Cursor)field.get(null); }
    catch(Exception e) { }
    System.err.println("AWT: get(java.awt.Cursor): Cursor not found for name: " + name);
    return snap.view.Cursor.DEFAULT;
}

/** Returns a hide cursor. */
public static java.awt.Cursor getHideCursor()
{
    if(_hcursor!=null) return _hcursor;
    BufferedImage img = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
    return _hcursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new java.awt.Point(0,0), "NONE");
} static java.awt.Cursor _hcursor;

/**
 * A Shape wrapper to provide snap shape as AWT.
 */
private static class AWTShape implements java.awt.Shape {
    
    /** Creates a new AWTShape for snap Shape. */
    public AWTShape(Shape aShape)  { _shp = aShape; } Shape _shp;
    
    /** Returns whether shape contains x/y. */
    public boolean contains(double x, double y)  { return _shp.contains(x,y); }
    
    /** Returns whether shape contains x/y/w/h. */
    public boolean contains(double x, double y, double w, double h)  { return _shp.contains(new Rect(x,y,w,h)); }

    /** Returns whether shape contains point. */
    public boolean contains(Point2D aPnt)  { return _shp.contains(aPnt.getX(), aPnt.getY()); }

    /** Returns whether shape contains rect. */
    public boolean contains(Rectangle2D r)  { return _shp.contains(get(r)); }

    /** Returns whether shape intersects x/y/w/h. */
    public boolean intersects(double x, double y, double w, double h)  { return _shp.intersects(new Rect(x,y,w,h)); }

    /** Returns whether shape intersects rect. */
    public boolean intersects(Rectangle2D r)  { return _shp.intersects(get(r)); }

    /** Returns whether shape contains rect. */
    public Rectangle getBounds()  { return get(_shp.getBounds()).getBounds(); }

    /** Returns whether shape contains rect. */
    public Rectangle2D getBounds2D()  { return get(_shp.getBounds()); }

    /** Returns whether shape contains rect. */
    public PathIterator getPathIterator(AffineTransform aT)
    {
        Transform t = aT!=null? get(aT) : null;
        return get(_shp.getPathIter(t));
    }

    /** Returns whether shape contains rect. */
    public PathIterator getPathIterator(AffineTransform aT, double f)
    { return new FlatteningPathIterator(getPathIterator(aT), f); }

}

/**
 * A Shape wrapper to provide AWT shape as snap.
 */
private static class SnapShape extends Shape {
    
    /** Creates a new SnapShape for AWT Shape. */
    public SnapShape(java.awt.Shape aShape)  { _shp = aShape; } java.awt.Shape _shp;
    
    /** Returns whether shape contains x/y. */
    public boolean contains(double x, double y)  { return _shp.contains(x,y); }
    
    /** Returns whether shape contains point. */
    public boolean contains(Point aPnt)  { return _shp.contains(aPnt.getX(), aPnt.getY()); }

    /** Returns whether shape contains rect. */
    public Rect getBounds()  { return get(_shp.getBounds()); }

    /** Returns whether shape contains rect. */
    public PathIter getPathIter(Transform aT)
    {
        AffineTransform t = aT!=null? get(aT) : null;
        return get(_shp.getPathIterator(t));
    }
}

/**
 * A Path iterator wrapper.
 */
private static class AWTPathIter implements PathIterator {
    
    /** Creates a new AWTPathIter for snap PathIter. */
    public AWTPathIter(PathIter aPI)  { _pi = aPI; }  PathIter _pi; PathIter.Seg _seg;
    
    /** Returns the current segment. */
    public int currentSegment(double coords[])
    {
        if(_seg!=null) System.err.println("J2DGfxEnv.AWTPATHIter: getNext called twice");
        switch(_seg = _pi.getNext(coords)) {
            case MoveTo: return PathIterator.SEG_MOVETO;
            case LineTo: return PathIterator.SEG_LINETO;
            case QuadTo: return PathIterator.SEG_QUADTO;
            case CubicTo: return PathIterator.SEG_CUBICTO;
            default: return PathIterator.SEG_CLOSE;
        }
    }
    
    /** Returns the current segment. */
    public int currentSegment(float coords[])
    {
        if(_seg!=null) System.err.println("J2DGfxEnv.AWTPATHIter: getNext called twice");
        switch(_seg = _pi.getNext(coords)) {
            case MoveTo: return PathIterator.SEG_MOVETO;
            case LineTo: return PathIterator.SEG_LINETO;
            case QuadTo: return PathIterator.SEG_QUADTO;
            case CubicTo: return PathIterator.SEG_CUBICTO;
            default: return PathIterator.SEG_CLOSE;
        }
    }
    
    /** Returns the winding rule. */
    public int getWindingRule()  { return _pi.getWinding(); }
    
    /** Returns whether is done. */
    public boolean isDone()  { return !_pi.hasNext(); }
    
    /** Returns whether is done. */
    public void next()  { _seg = null; }
}

/**
 * A Path iterator wrapper.
 */
private static class SnapPathIter extends PathIter {
    
    /** Creates a new SnapPathIter for AWT PathIterator. */
    public SnapPathIter(PathIterator aPI)  { _pi = aPI; }  PathIterator _pi;
    
    /** Returns the current segment. */
    public Seg getNext(double coords[])
    {
        int stype = _pi.currentSegment(coords); _pi.next();
        switch(stype) {
            case PathIterator.SEG_MOVETO: return PathIter.Seg.MoveTo;
            case PathIterator.SEG_LINETO: return PathIter.Seg.LineTo;
            case PathIterator.SEG_QUADTO: return PathIter.Seg.QuadTo;
            case PathIterator.SEG_CUBICTO: return PathIter.Seg.CubicTo;
            case PathIterator.SEG_CLOSE: return PathIter.Seg.Close;
            default: throw new RuntimeException("AWT.SnapPathIter: unknown segement type " + stype);
        }
    }
    
    /** Returns whether is done. */
    public boolean hasNext()  { return !_pi.isDone(); }
    
    /** Returns the winding rule. */
    public int getWinding()  { return _pi.getWindingRule(); }
}

}