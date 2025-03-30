/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;
import java.util.Arrays;

/**
 * A basic implementation of a painter.
 */
public abstract class PainterImpl extends Painter {

    // The current graphics state
    protected GState _gfxState = new GState();
    
    // The GState stack
    protected GState[] _gfxStateStack = new GState[8];
    
    // The GState stack size
    protected int _gfxStateStackSize;
    
    // The current marked shape
    private Shape _markedShape;
    
    // Whether marked shape is opaque
    private boolean _opaque = true;

    /**
     * Constructor.
     */
    public PainterImpl()
    {
        super();
    }

    /** Returns the current font. */
    public Font getFont()  { return _gfxState.font; }

    /** Sets the current font. */
    public void setFont(Font aFont)  { _gfxState.font = aFont; }

    /** Returns the current paint. */
    public Paint getPaint()  { return _gfxState.paint; }

    /** Sets the current paint. */
    public void setPaint(Paint aPaint)  { _gfxState.paint = aPaint; }

    /** Returns the current stroke. */
    public Stroke getStroke()  { return _gfxState.stroke; }

    /** Sets the current stroke. */
    public void setStroke(Stroke aStroke)  { _gfxState.stroke = aStroke; }

    /** Returns the opacity. */
    public double getOpacity()  { return _gfxState.opacity; }

    /** Sets the opacity. */
    public void setOpacity(double aValue)  { _gfxState.opacity = aValue; }

    /** Stroke the given shape. */
    public void draw(Shape aShape)
    {
        updateMarkedBounds(aShape, false);
    }

    /** Fill the given shape. */
    public void fill(Shape aShape)
    {
        updateMarkedBounds(aShape, getPaint().isOpaque());
    }

    /** Draw image with transform. */
    public void drawImage(Image image, Transform aTrans)
    {
        Shape imageBounds = new Rect(0,0, image.getWidth(), image.getHeight()).copyFor(aTrans);
        updateMarkedBounds(imageBounds, !image.hasAlpha());
    }

    /** Draw image in rect. */
    public void drawImage(Image image, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
    {
        updateMarkedBounds(new Rect(dx, dy, dw, dh), !image.hasAlpha());
    }

    /** Draw string at location with char spacing. */
    public void drawString(String aStr, double aX, double aY, double charSpacing)
    {
        Rect rect = getFont().getStringBounds(aStr);
        if (charSpacing > 0 && aStr.length() > 1)
            rect.width += charSpacing * (aStr.length() - 1);
        rect.offset(aX,aY);
        updateMarkedBounds(rect, false);
    }

    /** Stroke string at location with char spacing. */
    public void strokeString(String aStr, double aX, double aY, double charSpacing)
    {
        Rect rect = getFont().getStringBounds(aStr);
        if (charSpacing > 0 && aStr.length() > 1)
            rect.width += charSpacing * (aStr.length() - 1);
        rect.offset(aX, aY);
        rect.inset(-getStroke().getWidth() / 2);
        updateMarkedBounds(rect, false);
    }

    /**
     * Updates marked bounds.
     */
    private void updateMarkedBounds(Shape aShape, boolean isOpaque)
    {
        // Get marked shape in world coords
        Shape mshp = aShape.copyFor(_gfxState.xform);

        // If shape not in clip, clip
        if (!_gfxState.clip.contains(mshp) && _gfxState.clip.intersectsShape(mshp))
            mshp = Shape.intersectShapes(_gfxState.clip, mshp);

        // If no marked shape yet, just set
        if (_markedShape == null) {
            _markedShape = mshp; _opaque = isOpaque;
        }

        // Otherwise if new shape doesn't fit in current marked bounds, set to new shape (if it ecompasses) or union shape
        else if (!_markedShape.contains(mshp)) {
            if (mshp.contains(_markedShape))  {
                _markedShape = mshp;
                _opaque = isOpaque;
            }
            else {
                _markedShape = _markedShape.getBounds().getUnionRect(mshp.getBounds());
                _opaque = false;
            }
        }
    }

    /**
     * Returns the marked shape.
     */
    public Shape getMarkedShape()  { return _markedShape; }

    /**
     * Returns whether marked shape is opaque.
     */
    public boolean isMarkedShapeOpaque()  { return _opaque; }

    /**
     * Transform by transform.
     */
    public Transform getTransform()  { return _gfxState.xform; }

    /**
     * Transform by transform.
     */
    public void setTransform(Transform aTrans)
    {
        // Transform clip & mark shape back to world coords
        _gfxState.clip = _gfxState.clip.copyFor(_gfxState.xform.getInverse());
        if (_markedShape != null)
            _markedShape = _markedShape.copyFor(_gfxState.xform.getInverse());

        // Set new transform
        _gfxState.xform = aTrans;

        // Transform clip and mark shape back to local coords
        _gfxState.clip = _gfxState.clip.copyFor(aTrans);
        if (_markedShape != null)
            _markedShape = _markedShape.copyFor(aTrans);
    }

    /**
     * Transform by transform.
     */
    public void transform(Transform aTrans)
    {
        _gfxState.xform.concat(aTrans);
        _gfxState.clip = _gfxState.clip.copyFor(aTrans);
        if (_markedShape != null)
            _markedShape = _markedShape.copyFor(aTrans);
    }

    /**
     * Return clip shape.
     */
    public Shape getClip()  { return _gfxState.clip; }

    /**
     * Clip by shape.
     */
    public void clip(Shape aShape)
    {
        _gfxState.clip = Shape.intersectShapes(_gfxState.clip, aShape);
    }

    /**
     * Saves the graphics state.
     */
    public void save()
    {
        if (_gfxStateStackSize == _gfxStateStack.length)
            _gfxStateStack = Arrays.copyOf(_gfxStateStack, _gfxStateStack.length*2);
        _gfxStateStack[_gfxStateStackSize++] = _gfxState;
        _gfxState = _gfxState.clone();
    }

    /**
     * Restores the graphics state.
     */
    public void restore()
    {
        _gfxState = _gfxStateStack[--_gfxStateStackSize];
    }

    /**
     * Returns the current gstate.
     */
    public GState getGState()  { return _gfxState; }

    /**
     * The graphics state.
     */
    public static class GState implements Cloneable {

        // Paint
        public Paint paint = Color.BLACK;

        // Font
        public Font font = Font.Arial12;

        // Stroke
        public Stroke stroke = Stroke.Stroke1;

        // Opacity
        public double opacity = 1;

        // Transform
        public Transform xform = new Transform();

        // Clip
        public Shape clip = NO_CLIP;

        // Default clip
        private static final Shape NO_CLIP = new Rect(-5000000,-5000000,10000000,10000000);

        /**
         * Constructor.
         */
        public GState()  { }

        /** Standard clone implementation. */
        public GState clone()
        {
            GState clone; try { clone = (GState)super.clone(); }
            catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
            clone.xform = xform.clone();
            return clone;
        }
    }
}