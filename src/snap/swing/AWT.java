package snap.swing;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import snap.geom.*;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.*;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.gfx.Paint;
import snap.gfx.Stroke;
import snap.util.Convert;

/**
 * A class to return AWT versions of Snap graphics objects.
 */
public class AWT {

    // The Hide Cursor
    private static java.awt.Cursor _hcursor;

    /**
     * Converts snap rect to awt.
     */
    public static Rectangle2D snapToAwtRect(Rect snapRect)
    {
        return new Rectangle2D.Double(snapRect.x, snapRect.y, snapRect.width, snapRect.height);
    }

    /**
     * Converts awt rect to snap.
     */
    public static Rect awtToSnapRect(Rectangle2D awtRect)
    {
        return new Rect(awtRect.getX(), awtRect.getY(), awtRect.getWidth(), awtRect.getHeight());
    }

    /**
     * Converts snap shape to awt.
     */
    public static java.awt.Shape snapToAwtShape(Shape snapShape)
    {
        if (snapShape instanceof Rect snapRect)
            return snapToAwtRect(snapRect);
        if (snapShape instanceof SnapShape)
            return ((SnapShape) snapShape)._awtShape;
        return snapShape != null ? new AWTShape(snapShape) : null;
    }

    /**
     * Converts awt shape to snap.
     */
    public static Shape awtToSnapShape(java.awt.Shape awtShape)
    {
        if (awtShape instanceof Rectangle2D awtRect)
            return awtToSnapRect(awtRect);
        if (awtShape instanceof AWTShape)
            return ((AWTShape) awtShape)._snapShape;
        return awtShape != null ? new SnapShape(awtShape) : null;
    }

    /**
     * Converts snap path iter to awt.
     */
    public static PathIterator snapToAwtPathIter(PathIter snapPathIter)  { return new AWTPathIter(snapPathIter); }

    /**
     * Converts awt path iter to snap.
     */
    public static PathIter awtToSnapPathIter(PathIterator awtPathIter)  { return new SnapPathIter(awtPathIter); }

    /**
     * Converts snap transform to awt.
     */
    public static AffineTransform snapToAwtTrans(Transform snapTrans)  { return new AffineTransform(snapTrans.getMatrix()); }

    /**
     * Converts awt transform to snap.
     */
    public static Transform awtToSnapTrans(AffineTransform awtTrans)
    {
        double[] m = new double[6];
        awtTrans.getMatrix(m);
        return new Transform(m);
    }

    /**
     * Returns awt paint for snap paint.
     */
    public static java.awt.Color snapToAwtColor(Color snapColor)
    {
        return snapColor != null ? new java.awt.Color(snapColor.getRGBA(), true) : null;
    }

    /**
     * Returns awt paint for snap paint.
     */
    public static Color awtToSnapColor(java.awt.Color awtColor)  { return new Color(awtColor.getRGB()); }

    /**
     * Returns awt paint for snap paint.
     */
    public static java.awt.Paint snapToAwtPaint(Paint snapPaint)
    {
        if (snapPaint == null) return null;
        if (snapPaint instanceof Color snapColor)
            return snapToAwtColor(snapColor);
        if (snapPaint instanceof GradientPaint snapGradientPaint)
            return snapToAwtGradientPaint(snapGradientPaint);
        if (snapPaint instanceof ImagePaint snapImagePaint)
            return snapToAwtImagePaint(snapImagePaint);
        throw new RuntimeException("AWT.snapToAwtPaint: Can't convert paint " + snapPaint);
    }

    /**
     * Returns snap Paint for awt Paint.
     */
    public static Paint awtToSnapPaint(java.awt.Paint awtPaint)
    {
        if (awtPaint == null) return null;
        if (awtPaint instanceof java.awt.Color awtColor)
            return awtToSnapColor(awtColor);
        if(awtPaint instanceof MultipleGradientPaint) {
            System.out.println("AWT.awtToSnapPaint: Gradient paint conversion not implemented yet.");
            return Color.LIGHTGRAY;
        }
        //if(aSP instanceof java.awt.TexturePaint) return get((java.awt.TexturePaint)aSP);
        throw new RuntimeException("AWT.awtToSnapPaint: Not implemented: Can't convert paint " + awtPaint);
    }

    /**
     * Returns awt paint for snap paint.
     */
    private static MultipleGradientPaint snapToAwtGradientPaint(GradientPaint snapGradientPaint)
    {
        GradientPaint.Stop[] stops = snapGradientPaint.getStops();
        float[] fractions = new float[stops.length];
        java.awt.Color[] colors = new java.awt.Color[stops.length];
        for (int i = 0; i < stops.length; i++) {
            fractions[i] = (float) stops[i].offset();
            snap.gfx.Color c = stops[i].color();
            colors[i] = new java.awt.Color(c.getRedInt(), c.getGreenInt(), c.getBlueInt(), c.getAlphaInt());
        }

        float x0 = (float) snapGradientPaint.getStartX();
        float y0 = (float) snapGradientPaint.getStartY();
        float x1 = (float) snapGradientPaint.getEndX();
        float y1 = (float) snapGradientPaint.getEndY();

        if (snapGradientPaint.getType() == GradientPaint.Type.LINEAR)
            return new java.awt.LinearGradientPaint(x0, y0, x1, y1, fractions, colors);

        float radius = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        return new java.awt.RadialGradientPaint(x0, y0, radius, fractions, colors);
    }

    /**
     * Returns awt TexturePaint for snap TexturePaint.
     */
    public static java.awt.TexturePaint snapToAwtImagePaint(ImagePaint snapImagePaint)
    {
        BufferedImage bufferedImage = AWTImageUtils.getBufferedImage((java.awt.Image)
                snapImagePaint.getImage().getNative());
        return new java.awt.TexturePaint(bufferedImage, snapToAwtRect(snapImagePaint.getBounds()));
    }

    /**
     * Returns awt stroke for snap stroke.
     */
    public static java.awt.Stroke snapToAwtStroke(Stroke snapStroke)
    {
        float width = (float) snapStroke.getWidth();
        float miter = (float) snapStroke.getMiterLimit();
        int cap = snapToAwtStrokeCap(snapStroke.getCap());
        int join = snapToAwtStrokeJoin(snapStroke.getJoin());
        float[] dashArray = Convert.doubleArrayToFloat(snapStroke.getDashArray());
        float offset = (float) snapStroke.getDashOffset();
        return new BasicStroke(width, cap, join, miter, dashArray, offset);
    }

    /**
     * Return awt Stroke Cap for snap Stroke.Cap.
     */
    private static int snapToAwtStrokeCap(Stroke.Cap snapStrokeCap)
    {
        return switch (snapStrokeCap) {
            case Butt -> BasicStroke.CAP_BUTT;
            case Round -> BasicStroke.CAP_ROUND;
            default -> BasicStroke.CAP_SQUARE;
        };
    }

    /**
     * Return awt Stroke Join for snap Stroke.Join.
     */
    private static int snapToAwtStrokeJoin(Stroke.Join snapStrokeJoin)
    {
        return switch (snapStrokeJoin) {
            case Miter -> BasicStroke.JOIN_MITER;
            case Round -> BasicStroke.JOIN_ROUND;
            default -> BasicStroke.JOIN_BEVEL;
        };
    }

    /**
     * Returns awt stroke for snap stroke.
     */
    public static Stroke awtToSnapStroke(java.awt.Stroke awtStroke)
    {
        BasicStroke stroke = awtStroke instanceof BasicStroke ? (BasicStroke) awtStroke : null;
        if (stroke == null) {
            System.err.println("AWT.awtToSnapStroke: Unknown stroke class " + awtStroke);
            return Stroke.Stroke1;
        }

        double width = stroke.getLineWidth();
        double miter = stroke.getMiterLimit();
        Stroke.Cap cap = awtToSnapStrokeCap(stroke.getEndCap());
        Stroke.Join join = awtToSnapStrokeJoin(stroke.getLineJoin());
        float[] dary = stroke.getDashArray();
        float offset = stroke.getDashPhase();
        return new Stroke(width, cap, join, miter, dary, offset);
    }

    /**
     * Return snap Stroke.Cap for awt Stroke Cap.
     */
    private static Stroke.Cap awtToSnapStrokeCap(int awtStrokeCap)
    {
        return switch (awtStrokeCap) {
            case BasicStroke.CAP_BUTT -> Stroke.Cap.Butt;
            case BasicStroke.CAP_ROUND -> Stroke.Cap.Round;
            default -> Stroke.Cap.Square;
        };
    }

    /**
     * Return snap Stroke.Join for awt Stroke Join.
     */
    private static Stroke.Join awtToSnapStrokeJoin(int awtStrokeJoin)
    {
        return switch (awtStrokeJoin) {
            case BasicStroke.JOIN_MITER -> Stroke.Join.Miter;
            case BasicStroke.JOIN_ROUND -> Stroke.Join.Round;
            default -> Stroke.Join.Bevel;
        };
    }

    /**
     * Returns awt font for snap font.
     */
    public static java.awt.Font snapToAwtFont(Font snapFont)
    {
        Object ntv = snapFont.getNative();
        if (ntv instanceof java.awt.Font awtFont)
            return awtFont;
        return AWTFontUtils.getFont(snapFont.getName(), snapFont.getSize());
    }

    /**
     * Returns snap font for awt font.
     */
    public static Font awtToSnapFont(java.awt.Font awtFont)
    {
        String name = awtFont.getFontName();
        double size = awtFont.getSize2D();
        return Font.getFont(name, size);
    }

    /**
     * Returns awt image for snap image.
     */
    public static java.awt.Image snapToAwtImage(Image snapImage)  { return (java.awt.Image) snapImage.getNative(); }

    /**
     * Returns awt image for snap image.
     */
    public static Image awtToSnapImage(java.awt.Image awtImage)  { return Image.getImageForSource(awtImage); }

    /**
     * Returns awt Cursor for snap cursor.
     */
    public static java.awt.Cursor snapToAwtCursor(snap.view.Cursor snapCursor)
    {
        // If null, return null
        if (snapCursor == null) return null;

        // Get AWT name for Snap name
        String name = snapCursor.getName();
        name = name + "_CURSOR";
        if (name.equals("NONE_CURSOR"))
            return getHideCursor();

        // Get AWT id for name and cursor for id
        try {
            Field field = java.awt.Cursor.class.getField(name);
            int cursorId = (Integer) field.get(null);
            java.awt.Cursor cursor = java.awt.Cursor.getPredefinedCursor(cursorId);
            return cursor;
        }

        // If not found, return default
        catch (Exception e) {
            System.err.println("AWT.snapToAwtCursor: Cursor not found for name: " + name);
            return java.awt.Cursor.getDefaultCursor();
        }
    }

    /**
     * Returns snap Cursor awt snap cursor.
     */
    public static snap.view.Cursor awtToSnapCursor(java.awt.Cursor awtCursor)
    {
        // If null, return null
        if (awtCursor == null) return snap.view.Cursor.NONE;

        // Get Snap name for AWT name
        String name = awtCursor.getName();
        name = name.toUpperCase();
        if (name.equals("NONE"))
            return snap.view.Cursor.NONE;
        name = name.replace(" ", "_");
        name = name.replace("_CURSOR", "");

        // Get Snap cursor
        try {
            Field field = snap.view.Cursor.class.getField(name);
            snap.view.Cursor cursor = (snap.view.Cursor) field.get(null);
            return cursor;
        }

        // If not found, return default
        catch (Exception e)
        {
            System.err.println("AWT.awtToSnapCursor: Cursor not found for name: " + name);
            return snap.view.Cursor.DEFAULT;
        }
    }

    /**
     * Returns a hide cursor.
     */
    private static java.awt.Cursor getHideCursor()
    {
        if (_hcursor != null) return _hcursor;
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        return _hcursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new java.awt.Point(0, 0), "NONE");
    }

    /**
         * A Shape wrapper to provide snap shape as AWT.
         */
        private record AWTShape(Shape _snapShape) implements java.awt.Shape {

            /**
             * Returns whether shape contains x/y.
             */
            public boolean contains(double x, double y)  { return _snapShape.contains(x, y); }

            /**
             * Returns whether shape contains x/y/w/h.
             */
            public boolean contains(double x, double y, double w, double h)  { return _snapShape.contains(new Rect(x, y, w, h)); }

            /**
             * Returns whether shape contains point.
             */
            public boolean contains(Point2D aPnt)  { return _snapShape.contains(aPnt.getX(), aPnt.getY()); }

            /**
             * Returns whether shape contains rect.
             */
            public boolean contains(Rectangle2D rect)  { return _snapShape.contains(awtToSnapRect(rect)); }

            /**
             * Returns whether shape intersects x/y/w/h.
             */
            public boolean intersects(double x, double y, double w, double h)
            {
                return _snapShape.intersectsShape(new Rect(x, y, w, h));
            }

            /**
             * Returns whether shape intersects rect.
             */
            public boolean intersects(Rectangle2D rect)  { return _snapShape.intersectsShape(awtToSnapRect(rect)); }

            /**
             * Returns whether shape contains rect.
             */
            public Rectangle getBounds()  { return snapToAwtRect(_snapShape.getBounds()).getBounds(); }

            /**
             * Returns whether shape contains rect.
             */
            public Rectangle2D getBounds2D()  { return snapToAwtRect(_snapShape.getBounds()); }

            /**
             * Returns whether shape contains rect.
             */
            public PathIterator getPathIterator(AffineTransform aTrans)
            {
                Transform snapTrans = aTrans != null ? awtToSnapTrans(aTrans) : null;
                return snapToAwtPathIter(_snapShape.getPathIter(snapTrans));
            }

            /**
             * Returns whether shape contains rect.
             */
            public PathIterator getPathIterator(AffineTransform aT, double f)
            {
                return new FlatteningPathIterator(getPathIterator(aT), f);
            }
        }

    /**
     * A Shape wrapper to provide AWT shape as snap.
     */
    private static class SnapShape extends Shape {

        private final java.awt.Shape _awtShape;

        /**
         * Constructor.
         */
        public SnapShape(java.awt.Shape aShape)  { _awtShape = aShape; }

        /**
         * Returns whether shape contains x/y.
         */
        public boolean contains(double x, double y)  { return _awtShape.contains(x, y); }

        /**
         * Returns whether shape contains point.
         */
        public boolean contains(Point aPnt)  { return _awtShape.contains(aPnt.x, aPnt.y); }

        /**
         * Returns whether shape contains rect.
         */
        protected Rect getBoundsImpl()  { return awtToSnapRect(_awtShape.getBounds()); }

        /**
         * Returns whether shape contains rect.
         */
        public PathIter getPathIter(Transform aTrans)
        {
            AffineTransform awtTrans = aTrans != null ? snapToAwtTrans(aTrans) : null;
            return awtToSnapPathIter(_awtShape.getPathIterator(awtTrans));
        }
    }

    /**
     * A Path iterator wrapper.
     */
    private static class AWTPathIter implements PathIterator {

        private final PathIter _snapPathIter;
        private Seg _seg;

        /**
         * Constructor.
         */
        public AWTPathIter(PathIter snapPathIter)  { _snapPathIter = snapPathIter; }

        @Override
        public int currentSegment(double[] coords)
        {
            if (_seg != null)
                System.err.println("AWT.AWTPathIter: getNext called twice");
            return getSegType(_seg = _snapPathIter.getNext(coords));
        }

        @Override
        public int currentSegment(float[] coords)
        {
            if (_seg != null)
                System.err.println("AWT.AWTPathIter: getNext called twice");
            return getSegType(_seg = _snapPathIter.getNext(coords));
        }

        @Override
        public int getWindingRule()  { return _snapPathIter.getWinding(); }

        @Override
        public boolean isDone()  { return !_snapPathIter.hasNext(); }

        @Override
        public void next()  { _seg = null; }

        // Return AWT segment type
        private int getSegType(Seg seg)
        {
            return switch (seg) {
                case MoveTo -> PathIterator.SEG_MOVETO;
                case LineTo -> PathIterator.SEG_LINETO;
                case QuadTo -> PathIterator.SEG_QUADTO;
                case CubicTo -> PathIterator.SEG_CUBICTO;
                default -> PathIterator.SEG_CLOSE;
            };
        }
    }

    /**
     * A Path iterator wrapper.
     */
    private static class SnapPathIter extends PathIter {

        private final PathIterator _awtPathIter;

        /**
         * Constructor.
         */
        public SnapPathIter(PathIterator awtPathIter)  { _awtPathIter = awtPathIter; }

        @Override
        public Seg getNext(double[] coords)
        {
            int segType = _awtPathIter.currentSegment(coords);
            _awtPathIter.next();
            return switch (segType) {
                case PathIterator.SEG_MOVETO -> Seg.MoveTo;
                case PathIterator.SEG_LINETO -> Seg.LineTo;
                case PathIterator.SEG_QUADTO -> Seg.QuadTo;
                case PathIterator.SEG_CUBICTO -> Seg.CubicTo;
                case PathIterator.SEG_CLOSE -> Seg.Close;
                default -> throw new RuntimeException("AWT.SnapPathIter.getNext: Unknown segment type " + segType);
            };
        }

        @Override
        public boolean hasNext()  { return !_awtPathIter.isDone(); }

        @Override
        public int getWinding()  { return _awtPathIter.getWindingRule(); }
    }
}