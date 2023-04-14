/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropObject;
import snap.props.PropSet;
import snap.text.StringBox;
import snap.util.*;

/**
 * A standard view implementation to show graphics and handle events and form the basis of all views (buttons, sliders,
 * text fields, etc.).
 */
public class View extends PropObject implements XMLArchiver.Archivable {

    // The parent of this view
    private ParentView  _parent;

    // The name of this view
    private String  _name;

    // The view location
    private double  _x, _y;

    // The view size
    private double  _width, _height;

    // The view translation from x and y
    private double  _transX, _transY;

    // The view rotation
    private double  _rotate;

    // The view scale from x and y
    private double  _scaleX, _scaleY;

    // The alignment of content in this view
    protected Pos  _align;

    // The margin to be provided around this view
    protected Insets  _margin;

    // The padding between the border and content in this view
    protected Insets  _padding;

    // Spacing between children
    protected double  _spacing;

    // The horizontal position this view would prefer to take when inside a pane
    private HPos  _leanX;

    // The vertical position this view would prefer to take when inside a pane
    private VPos  _leanY;

    // Whether this view would like to grow horizontally/vertically if possible when inside a pane
    private boolean  _growWidth, _growHeight;

    // Whether this view has a vertical orientation.
    private boolean  _vertical;

    // The view minimum width and height
    private double  _minWidth, _minHeight;

    // The view maximum width and height
    private double  _maxWidth, _maxHeight;

    // The view preferred width and height
    private double  _prefWidth, _prefHeight;

    // The view fill
    private Paint  _fill;

    // The view border
    private Border  _border;

    // The radius for border rounded corners
    protected double  _borderRadius;

    // The ViewEffect to manage effect rendering for this view and current effect
    protected ViewEffect  _effect;

    // The opacity
    private double  _opacity;

    // The view font
    protected Font  _font;

    // Whether view is disabled
    private boolean  _disabled;

    // Whether view is currently the RootView.FocusedView
    private boolean  _focused;

    // Whether view can receive focus
    private boolean  _focusable;

    // Whether view should request focus when pressed
    private boolean  _focusWhenPrsd;

    // Whether view focus should change when traveral key is pressed (Tab)
    private boolean  _focusKeysEnbld;

    // Whether view should paint focus ring when focused
    private boolean  _focusPainted;

    // Whether view can generate action events in response to certain user interactions
    private boolean  _actionable;

    // Whether view is visible
    private boolean  _visible;

    // Whether view is visible and has parent that is showing
    protected boolean  _showing;

    // Whether view can be hit by mouse
    private boolean  _pickable;

    // Whether view should be painted
    private boolean  _paintable;

    // Whether view should be included in layout
    private boolean  _managed;

    // The view cursor
    private Cursor  _cursor;

    // The tooltip
    private String  _toolTip;

    // The clip (if set)
    private Shape  _clip;

    // Bindings for this view
    private List <Binding>  _bindings = Collections.EMPTY_LIST;

    // Client properties
    private Map<String,Object>  _props = Collections.EMPTY_MAP;

    // The view best width and height
    private double  _bestWidth = -1, _bestHeight = -1, _bestWidthParam, _bestHeightParam;

    // The real class name, if shape component is really a custom subclass
    private String  _realClassName;

    // The event adapter
    private EventAdapter _eventAdapter;

    // Provides animation for View
    private ViewAnim  _anim;

    // The current rect that needs to be repainted in this view
    protected Rect  _repaintRect;

    // Provides information for physics simulations
    private ViewPhysics<?>  _physics;

    // The view owner of this view
    private ViewOwner  _owner;

    // Constants for properties
    public static final String Name_Prop = "Name";
    public static final String X_Prop = "X";
    public static final String Y_Prop = "Y";
    public static final String Width_Prop = "Width";
    public static final String Height_Prop = "Height";
    public static final String TransX_Prop = "TransX";
    public static final String TransY_Prop = "TransY";
    public static final String Rotate_Prop = "Rotate";
    public static final String ScaleX_Prop = "ScaleX";
    public static final String ScaleY_Prop = "ScaleY";
    public static final String MinWidth_Prop = "MinWidth";
    public static final String MinHeight_Prop = "MinHeight";
    public static final String MaxWidth_Prop = "MaxWidth";
    public static final String MaxHeight_Prop = "MaxHeight";
    public static final String PrefWidth_Prop = "PrefWidth";
    public static final String PrefHeight_Prop = "PrefHeight";
    public static final String GrowWidth_Prop = "GrowWidth";
    public static final String GrowHeight_Prop = "GrowHeight";
    public static final String LeanX_Prop = "LeanX";
    public static final String LeanY_Prop = "LeanY";
    public static final String Disabled_Prop = "Disabled";
    public static final String Visible_Prop = "Visible";
    public static final String Pickable_Prop = "Pickable";
    public static final String Paintable_Prop = "Paintable";
    public static final String Vertical_Prop = "Vertical";
    public static final String Focused_Prop = "Focused";
    public static final String Focusable_Prop = "Focusable";
    public static final String FocusWhenPressed_Prop = "FocusWhenPressed";
    public static final String Border_Prop = "Border";
    public static final String BorderRadius_Prop = "BorderRadius";
    public static final String Clip_Prop = "Clip";
    public static final String Cursor_Prop = "Cursor";
    public static final String Effect_Prop = "Effect";
    public static final String Fill_Prop = "Fill";
    public static final String Font_Prop = "Font";
    public static final String Align_Prop = "Align";
    public static final String Opacity_Prop = "Opacity";
    public static final String Margin_Prop = "Margin";
    public static final String Padding_Prop = "Padding";
    public static final String Spacing_Prop = "Spacing";
    public static final String Parent_Prop = "Parent";
    public static final String Showing_Prop = "Showing";
    public static final String Text_Prop = "Text";
    public static final String ToolTip_Prop = "ToolTip";

    // Constants for property defaults
    private static final boolean DEFAULT_VERTICAL = false;
    public static final Pos DEFAULT_ALIGN = Pos.TOP_LEFT;
    public static final Insets DEFAULT_MARGIN = Insets.EMPTY;
    public static final Insets DEFAULT_PADDING = Insets.EMPTY;
    public static final double DEFAULT_SPACING = 0;

    // Convenience for common events
    public static final ViewEvent.Type Action = ViewEvent.Type.Action;
    public static final ViewEvent.Type KeyPress = ViewEvent.Type.KeyPress;
    public static final ViewEvent.Type KeyRelease = ViewEvent.Type.KeyRelease;
    public static final ViewEvent.Type KeyType = ViewEvent.Type.KeyType;
    public static final ViewEvent.Type MousePress = ViewEvent.Type.MousePress;
    public static final ViewEvent.Type MouseDrag = ViewEvent.Type.MouseDrag;
    public static final ViewEvent.Type MouseRelease = ViewEvent.Type.MouseRelease;
    public static final ViewEvent.Type MouseEnter = ViewEvent.Type.MouseEnter;
    public static final ViewEvent.Type MouseMove = ViewEvent.Type.MouseMove;
    public static final ViewEvent.Type MouseExit = ViewEvent.Type.MouseExit;
    public static final ViewEvent.Type Scroll = ViewEvent.Type.Scroll;
    public static final ViewEvent.Type[] KeyEvents = ViewEvent.Type.KeyEvents;
    public static final ViewEvent.Type[] MouseEvents = ViewEvent.Type.MouseEvents;
    public static final ViewEvent.Type[] DragEvents = ViewEvent.Type.DragEvents;

    /**
     * Constructor.
     */
    public View()
    {
        super();

        // Set property defaults
        _scaleX = _scaleY = 1;
        _align = getDefaultAlign();
        _margin = (Insets) getPropDefault(Margin_Prop);
        _padding = (Insets) getPropDefault(Padding_Prop);
        _spacing = getPropDefaultDouble(Spacing_Prop);
        _vertical = getPropDefaultBool(Vertical_Prop);
        _minWidth = _minHeight = -1;
        _maxWidth = _maxHeight = -1;
        _prefWidth = _prefHeight = -1;
        _focusKeysEnbld = true;
        _focusPainted = true;
        _visible = true;
        _pickable = true;
        _paintable = true;
        _managed = true;
        _opacity = 1;
    }

    /**
     * Returns the name for the view.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name for the view.
     */
    public void setName(String aName)
    {
        if (Objects.equals(aName, _name)) return;
        firePropChange(Name_Prop, _name, _name = StringUtils.min(aName));
    }

    /**
     * Returns the X location of the view.
     */
    public double getX()  { return _x; }

    /**
     * Sets the X location of the view.
     */
    public void setX(double aValue)
    {
        if (aValue == _x) return;

        // Repaint in parent to mark old bounds
        repaintInParent(null);

        // Set value and fire prop change
        firePropChange(X_Prop, _x, _x = aValue);
    }

    /**
     * Returns the Y location of the view.
     */
    public double getY()  { return _y; }

    /**
     * Sets the Y location of the view.
     */
    public void setY(double aValue)
    {
        if (aValue == _y) return;

        // Repaint in parent to mark old bounds
        repaintInParent(null);

        // Set value and fire prop change
        firePropChange(Y_Prop, _y, _y = aValue);
    }

    /**
     * Returns the width of the view.
     */
    public double getWidth()  { return _width; }

    /**
     * Sets the width of the view.
     */
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;

        // Repaint max of new and old
        repaint(0, 0, Math.max(_width, aValue), getHeight());

        // Set value, fire prop change and register for relayout
        firePropChange(Width_Prop, _width, _width = aValue);
        relayout();
    }

    /**
     * Returns the height of the view.
     */
    public double getHeight()  { return _height; }

    /**
     * Sets the height of the view.
     */
    public void setHeight(double aValue)
    {
        if (aValue == _height) return;

        // Repaint max of new and old
        repaint(0, 0, getWidth(), Math.max(_height, aValue));

        // Set value, fire prop change and register for relayout
        firePropChange(Height_Prop, _height, _height = aValue);
        relayout();
    }

    /**
     * Returns the mid x.
     */
    public double getMidX()
    {
        return getX() + getWidth() / 2;
    }

    /**
     * Returns the mid y.
     */
    public double getMidY()
    {
        return getY() + getHeight() / 2;
    }

    /**
     * Returns the max x.
     */
    public double getMaxX()
    {
        return getX() + getWidth();
    }

    /**
     * Returns the max x.
     */
    public double getMaxY()
    {
        return getY() + getHeight();
    }

    /**
     * Returns the view x/y.
     */
    public Point getXY()
    {
        return new Point(getX(), getY());
    }

    /**
     * Sets the view x/y.
     */
    public void setXY(double aX, double aY)
    {
        setX(aX);
        setY(aY);
    }

    /**
     * Returns the view size.
     */
    public Size getSize()
    {
        return new Size(getWidth(), getHeight());
    }

    /**
     * Sets the size.
     */
    public void setSize(Size aSize)
    {
        setSize(aSize.width, aSize.height);
    }

    /**
     * Sets the size.
     */
    public void setSize(double aW, double aH)
    {
        setWidth(aW);
        setHeight(aH);
    }

    /**
     * Sets the size to preferred size.
     */
    public void setSizeToPrefSize()
    {
        double prefW = getPrefWidth();
        double prefH = getPrefHeight();
        setSize(prefW, prefH);
    }

    /**
     * Returns the bounds.
     */
    public Rect getBounds()
    {
        return new Rect(getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Sets the bounds.
     */
    public void setBounds(Rect aRect)
    {
        setBounds(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /**
     * Sets the bounds.
     */
    public void setBounds(double aX, double aY, double aW, double aH)
    {
        setX(aX);
        setY(aY);
        setWidth(aW);
        setHeight(aH);
    }

    /**
     * Returns the bounds inside view.
     */
    public Rect getBoundsLocal()
    {
        return new Rect(0, 0, getWidth(), getHeight());
    }

    /**
     * Sets the view bounds with given rect in current local coords such that it will have that rect as new local bounds.
     */
    public void setBoundsLocal(double aX, double aY, double aW, double aH)
    {
        setXYLocal(aX, aY);
        setSizeLocal(aW, aH);
    }

    /**
     * Sets the view x/y with given point in current local coords such that new origin will be at that point.
     */
    public void setXYLocal(double aX, double aY)
    {
        if (isLocalToParentSimple()) {
            setXY(getX() + aX, getY() + aY);
            return;
        }
        Point p0 = localToParent(0, 0);
        Point p1 = localToParent(aX, aY);
        double p2x = Math.round(getX() + p1.x - p0.x);
        double p2y = Math.round(getY() + p1.y - p0.y);
        setXY(p2x, p2y);
    }

    /**
     * Sets the view size such that it maintains it's location in parent.
     */
    public void setSizeLocal(double aW, double aH)
    {
        if (isLocalToParentSimple()) {
            setSize(aW, aH);
            return;
        }
        Point p0 = localToParent(0, 0);
        setSize(aW, aH);
        Point p1 = localToParent(0, 0);
        double p2x = Math.round(getX() - (p1.x - p0.x));
        double p2y = Math.round(getY() - (p1.y - p0.y));
        setXY(Math.round(p2x), p2y);
    }

    /**
     * Returns the bounds in parent coords.
     */
    public Rect getBoundsParent()
    {
        Shape boundsShapeInParent = getBoundsShapeParent();
        return boundsShapeInParent.getBounds();
    }

    /**
     * Returns the bounds shape in view coords.
     */
    public Shape getBoundsShape()
    {
        // If BorderRadius is set, return round rect
        double borderRadius = getBorderRadius();
        if (borderRadius > 0)
            return new RoundRect(0,0, getWidth(), getHeight(), borderRadius);

        // Return local bounds
        return getBoundsLocal();
    }

    /**
     * Returns the bounds shape in parent coords.
     */
    public Shape getBoundsShapeParent()
    {
        Shape boundsShape = getBoundsShape();
        return localToParent(boundsShape);
    }

    /**
     * Returns the translation of this view from X.
     */
    public double getTransX()  { return _transX; }

    /**
     * Sets the translation of this view from X.
     */
    public void setTransX(double aValue)
    {
        if (aValue == _transX) return;
        repaintInParent(null);
        firePropChange(TransX_Prop, _transX, _transX = aValue);
    }

    /**
     * Returns the translation of this view from Y.
     */
    public double getTransY()  { return _transY; }

    /**
     * Sets the translation of this view from Y.
     */
    public void setTransY(double aValue)
    {
        if (aValue == _transY) return;
        repaintInParent(null);
        firePropChange(TransY_Prop, _transY, _transY = aValue);
    }

    /**
     * Returns the rotation of the view in degrees.
     */
    public double getRotate()  { return _rotate; }

    /**
     * Turn to given angle.
     */
    public void setRotate(double theDegrees)
    {
        if (theDegrees == _rotate) return;
        repaintInParent(null);
        firePropChange(Rotate_Prop, _rotate, _rotate = theDegrees);
    }

    /**
     * Returns the scale of this view from X.
     */
    public double getScaleX()  { return _scaleX; }

    /**
     * Sets the scale of this view from X.
     */
    public void setScaleX(double aValue)
    {
        if (aValue == _scaleX) return;
        repaintInParent(null);
        firePropChange(ScaleX_Prop, _scaleX, _scaleX = aValue);
    }

    /**
     * Returns the scale of this view from Y.
     */
    public double getScaleY()  { return _scaleY; }

    /**
     * Sets the scale of this view from Y.
     */
    public void setScaleY(double aValue)
    {
        if (aValue == _scaleY) return;
        repaintInParent(null);
        firePropChange(ScaleY_Prop, _scaleY, _scaleY = aValue);
    }

    /**
     * Returns the scale of this view.
     */
    public double getScale()  { return _scaleX; }

    /**
     * Sets the scale of this view from Y.
     */
    public void setScale(double aValue)
    {
        setScaleX(aValue);
        setScaleY(aValue);
    }

    /**
     * Returns fill paint.
     */
    public Paint getFill()  { return _fill; }

    /**
     * Sets fill paint.
     */
    public void setFill(Paint aPaint)
    {
        if (Objects.equals(aPaint, getFill())) return;
        firePropChange(Fill_Prop, _fill, _fill = aPaint);
        repaint();
    }

    /**
     * Returns the fill as color.
     */
    public Color getFillColor()
    {
        Paint fill = getFill();
        if (fill == null || fill instanceof Color)
            return (Color) fill;
        if (fill instanceof GradientPaint)
            return ((GradientPaint) fill).getStopColor(0);
        return null;
    }

    /**
     * Returns the border.
     */
    public Border getBorder()  { return _border; }

    /**
     * Sets the border.
     */
    public void setBorder(Border aBorder)
    {
        if (Objects.equals(aBorder, getBorder())) return;
        firePropChange(Border_Prop, _border, _border = aBorder);
        relayout();
        relayoutParent();
        repaint();
    }

    /**
     * Convenience to set border to given color and line width.
     */
    public void setBorder(Color aColor, double aWidth)
    {
        Border border = aColor != null ? Border.createLineBorder(aColor, aWidth) : null;
        setBorder(border);
    }

    /**
     * Returns the radius for border rounded corners.
     */
    public double getBorderRadius()  { return _borderRadius; }

    /**
     * Sets the radius for border rounded corners.
     */
    public void setBorderRadius(double aValue)
    {
        if (aValue == getBorderRadius()) return;
        firePropChange(BorderRadius_Prop, _borderRadius, _borderRadius = aValue);
        repaint();
    }

    /**
     * Returns effect.
     */
    public Effect getEffect()
    {
        return _effect != null ? _effect._eff : null;
    }

    /**
     * Sets paint.
     */
    public void setEffect(Effect anEff)
    {
        // If already set, just return
        Effect old = getEffect();
        if (Objects.equals(anEff, getEffect())) return;

        // Set new ViewEffect, fire prop change and repaint
        repaintInParent(null);
        _effect = anEff != null ? new ViewEffect(this, anEff) : null;
        firePropChange(Effect_Prop, old, anEff);
    }

    /**
     * Returns the opacity of the view.
     */
    public double getOpacity()  { return _opacity; }

    /**
     * Sets the opacity of the view.
     */
    public void setOpacity(double aValue)
    {
        if (aValue == _opacity) return;
        firePropChange(Opacity_Prop, _opacity, _opacity = aValue);
        repaint();
    }

    /**
     * Returns the combined opacity of this view and it's parents.
     */
    public double getOpacityAll()
    {
        double opacity = getOpacity();
        ParentView par = getParent();
        return par != null ? opacity * par.getOpacityAll() : opacity;
    }

    /**
     * Returns whether font has been explicitly set for this view.
     */
    public boolean isFontSet()  { return _font != null; }

    /**
     * Returns the font for the view (defaults to parent font).
     */
    public Font getFont()
    {
        if (_font != null) return _font;
        return getDefaultFont();
    }

    /**
     * Sets the font for the view.
     */
    public void setFont(Font aFont)
    {
        // If already set, just return
        if (Objects.equals(aFont, _font)) return;

        // Set, fire prop change, relayout parent, repaint
        firePropChange(Font_Prop, _font, _font = aFont);
        relayoutParent();
        repaint();
    }

    /**
     * Called when parent font changes.
     */
    protected void parentFontChanged()
    {
        // If Font is explicitly set, just return
        if (isFontSet())
            return;

        // Trigger layout updates for new font
        relayoutParent();
        repaint();
    }

    /**
     * Returns the cursor.
     */
    public Cursor getCursor()
    {
        if (_cursor != null)
            return _cursor;
        ParentView parent = getParent();
        if (parent != null)
            return parent.getCursor();
        return Cursor.DEFAULT;
    }

    /**
     * Sets the cursor.
     */
    public void setCursor(Cursor aCursor)
    {
        // If already set, just return
        if (aCursor == _cursor) return;

        // Set and fire prop change
        firePropChange(Cursor_Prop, _cursor, _cursor = aCursor);

        // Notify window to update cursor
        WindowView win = getWindow();
        if (win != null)
            win.resetActiveCursor();
    }

    /**
     * Whether view is disabled.
     */
    public boolean isDisabled()  { return _disabled; }

    /**
     * Sets whether view is enabled.
     */
    public void setDisabled(boolean aValue)
    {
        if (aValue == _disabled) return;
        firePropChange(Disabled_Prop, _disabled, _disabled = aValue);
        repaint();
    }

    /**
     * Whether view is enabled.
     */
    public boolean isEnabled()  { return !_disabled; }

    /**
     * Sets whether view is enabled.
     */
    public void setEnabled(boolean aValue)
    {
        setDisabled(!aValue);
    }

    /**
     * Returns whether view should clip to bounds.
     */
    public boolean isClipToBounds()  { return _clip == ClipToBoundsRect; }

    /**
     * Sets whether view should clip to bounds.
     */
    public void setClipToBounds(boolean aValue)
    {
        if (aValue == isClipToBounds()) return;
        setClip(aValue ? ClipToBoundsRect : null);
    }

    // The shared rect to represent "ClipToBounds"
    private static Rect ClipToBoundsRect = new Rect();

    /**
     * Returns the clip shape.
     */
    public Shape getClip()
    {
        if (_clip == ClipToBoundsRect)
            return getBoundsShape();
        return _clip;
    }

    /**
     * Sets the clip shape.
     */
    public void setClip(Shape aShape)
    {
        if (Objects.equals(aShape, _clip)) return;
        firePropChange(Clip_Prop, _clip, _clip = aShape);
    }

    /**
     * Returns the clip bounds.
     */
    public Rect getClipBounds()
    {
        Shape clip = getClip();
        return clip != null ? clip.getBounds() : null;
    }

    /**
     * Returns the clip of this view due to all parents.
     */
    public Shape getClipAll()
    {
        Shape vshp = getParent() != null ? getParent().getClipAll() : null;
        if (vshp != null)
            vshp = parentToLocal(vshp);
        if (getClip() != null)
            vshp = vshp != null ? Shape.intersectShapes(vshp, getClip()) : getClip();
        return vshp;
    }

    /**
     * Returns the clip bounds due to all parents.
     */
    public Rect getClipAllBounds()
    {
        Shape clip = getClipAll();
        return clip != null ? clip.getBounds() : null;
    }

    /**
     * Returns the clipped shape for given shape.
     */
    public Rect getClippedRect(Rect aRect)
    {
        if (!isVisible()) return new Rect();
        Rect cbnds = getClipAllBounds();
        if (cbnds == null) return aRect;
        Rect crect = aRect.getIntersectRect(cbnds);
        crect.snap();
        return crect;
    }

    /**
     * Returns the visible bounds for a view based on ancestor clips (just bound local if no clipping found).
     */
    public Rect getVisRect()
    {
        Rect bnds = getBoundsLocal();
        return getClippedRect(bnds);
    }

    /**
     * Called to scroll the given shape in this view coords to visible.
     */
    public void scrollToVisible(Shape aShape)
    {
        ParentView parent = getParent();
        if (parent != null) {
            Shape shape = localToParent(aShape);
            parent.scrollToVisible(shape);
        }
    }

    /**
     * Returns whether transform to parent is simple (contains no rotate, scale, skew).
     */
    public boolean isLocalToParentSimple()
    {
        return _rotate == 0 && _scaleX == 1 && _scaleY == 1;
    }

    /**
     * Returns the transform.
     */
    public Transform getLocalToParent()
    {
        double viewX = getX();
        double viewY = getY();
        if (isLocalToParentSimple())
            return new Transform(viewX + _transX, viewY + _transY);

        // Get location, size, point of rotation, rotation, scale, skew
        double x = viewX + getTransX();
        double y = viewY + getTransY();
        double w = getWidth();
        double h = getHeight();
        double prx = w / 2, pry = h / 2;
        double rot = getRotate();
        double sx = getScaleX();
        double sy = getScaleY(); //skx = getSkewX(), sky = getSkewY();

        // Transform about point of rotation and return
        Transform xfm = new Transform(x + prx, y + pry);
        if (rot != 0) xfm.rotate(rot);
        if (sx != 1 || sy != 1)
            xfm.scale(sx, sy); //if (skx!=0 || sky!=0) t.skew(skx, sky);
        xfm.translate(-prx, -pry);
        return xfm;
    }

    /**
     * Returns the transform.
     */
    public Transform getLocalToParent(View aPar)
    {
        Transform xfm = getLocalToParent();
        for (View view = getParent(); view != aPar; ) {

            // If simple, just add translation
            if (view.isLocalToParentSimple())
                xfm.preTranslate(view._x + view._transX, view._y + view._transY);

            // Otherwise multiply full transform
            else {
                Transform localToParent = view.getLocalToParent();
                xfm.multiply(localToParent);
            }

            // Get next view (complain and break if given par not found
            view = view.getParent();
            if (view == null && aPar != null) {
                System.err.println("View.getLocalToParent: Parent not found " + aPar);
                break;
            }
        }

        // Return transform
        return xfm;
    }

    /**
     * Converts a point from local to parent.
     */
    public Point localToParent(double aX, double aY)
    {
        // If simple, just add offset
        if (isLocalToParentSimple())
            return new Point(aX + _x + _transX, aY + _y + _transY);

        // Otherwise do full transform
        Transform localToParent = getLocalToParent();
        return localToParent.transformXY(aX, aY);
    }

    /**
     * Converts a point from local to given parent.
     */
    public Point localToParent(double aX, double aY, View aPar)
    {
        Point point = new Point(aX, aY);

        // Iterate up parents to given parent (or null) and transform point for each
        for (View view = this; view != aPar && view != null; view = view.getParent()) {
            if (view.isLocalToParentSimple())
                point.offset(view._x + view._transX, view._y + view._transY);
            else point = view.localToParent(point.x, point.y);
        }

        // Return
        return point;
    }

    /**
     * Converts a shape from local to parent.
     */
    public Shape localToParent(Shape aShape)
    {
        Transform localToParent = getLocalToParent();
        return aShape.copyFor(localToParent);
    }

    /**
     * Converts a point from local to given parent.
     */
    public Shape localToParent(Shape aShape, View aPar)
    {
        Transform localToParent = getLocalToParent(aPar);
        return aShape.copyFor(localToParent);
    }

    /**
     * Returns the transform from parent to local coords.
     */
    public Transform getParentToLocal()
    {
        if (isLocalToParentSimple())
            return new Transform(-_x - _transX, -_y - _transY);
        Transform tfm = getLocalToParent();
        tfm.invert();
        return tfm;
    }

    /**
     * Returns the transform from parent to local coords.
     */
    public Transform getParentToLocal(View aPar)
    {
        Transform tfm = getLocalToParent(aPar);
        tfm.invert();
        return tfm;
    }

    /**
     * Converts a point from parent to local.
     */
    public Point parentToLocal(double aX, double aY)
    {
        // If simple, just subtract offset
        if (isLocalToParentSimple())
            return new Point(aX - _x - _transX, aY - _y - _transY);

        // Otherwise do full transform
        Transform parentToLocal = getParentToLocal();
        return parentToLocal.transformXY(aX, aY);
    }

    /**
     * Converts a point from given parent to local.
     */
    public Point parentToLocal(double aX, double aY, View aPar)
    {
        Transform parentToLocal = getParentToLocal(aPar);
        return parentToLocal.transformXY(aX, aY);
    }

    /**
     * Converts a shape from parent to local.
     */
    public Shape parentToLocal(Shape aShape)
    {
        Transform parentToLocal = getParentToLocal();
        return aShape.copyFor(parentToLocal);
    }

    /**
     * Converts a shape from parent to local.
     */
    public Shape parentToLocal(Shape aShape, View aView)
    {
        Transform parentToLocal = getParentToLocal(aView);
        return aShape.copyFor(parentToLocal);
    }

    /**
     * Converts a point from local to parent.
     */
    public Point localToScreen(double aX, double aY)
    {
        // Get Window (if none, just convert out of View tree)
        WindowView win = getWindow();
        if (win == null) {
            Point pnt = localToParent(aX, aY, null);
            return pnt;
        }

        // Ask WindowHpr to do it
        WindowView.WindowHpr hpr = win.getHelper();
        Point pnt = hpr.viewToScreen(this, aX, aY);
        return pnt;
    }

    /**
     * Returns whether view contains point.
     */
    public boolean contains(Point aPnt)
    {
        return contains(aPnt.x, aPnt.y);
    }

    /**
     * Returns whether view contains point.
     */
    public boolean contains(double aX, double aY)
    {
        return 0 <= aX && aX <= getWidth() && 0 <= aY && aY <= getHeight();
        //if (!(0<=aX && aX<=getWidth() && 0<=aY && aY<=getHeight())) return false;
        //if (isPickBounds()) return getBoundsInside().contains(aX, aY);
        //return getBoundsShape().contains(aX, aY);
    }

    /**
     * Returns whether view contains shape.
     */
    public boolean contains(Shape aShape)
    {
        //if (isPickBounds()) return getBoundsInside().contains(aShape,1);
        return getBoundsShape().contains(aShape);
    }

    /**
     * Returns whether view intersects shape.
     */
    public boolean intersects(Shape aShape)
    {
        //if (isPickBounds()) return getBoundsInside().intersects(aShape,1);
        return getBoundsShape().intersects(aShape, 1);
    }

    /**
     * Returns the parent of this view.
     */
    public ParentView getParent()  { return _parent; }

    /**
     * Sets the parent.
     */
    protected void setParent(ParentView aPar)
    {
        // FirePropChange
        firePropChange(Parent_Prop, _parent, _parent = aPar);

        // Update Showing property
        boolean showing = aPar != null && aPar.isShowing();
        setShowing(showing);

        // If inherit font, propagate to children
        if (!isFontSet())
            parentFontChanged();
    }

    /**
     * Returns the first parent with given class by iterating up parent hierarchy.
     */
    public <T extends View> T getParent(Class<T> aClass)
    {
        for (ParentView s = getParent(); s != null; s = s.getParent())
            if (aClass.isInstance(s)) return (T) s;
        return null; // Return null since parent of class wasn't found
    }

    /**
     * Returns the number of ancestors of this view.
     */
    public int getParentCount()
    {
        int pc = 0;
        for (View n = getParent(); n != null; n = n.getParent()) pc++;
        return pc;
    }

    /**
     * Returns whether given view is an ancestor of this view.
     */
    public boolean isAncestor(View aView)
    {
        for (View n = getParent(); n != null; n = n.getParent())
            if (n == aView) return true;
        return false;
    }

    /**
     * Returns the index of this view in parent.
     */
    public int indexInParent()
    {
        ParentView par = getParent();
        return par != null ? par.indexOfChild(this) : -1;
    }

    /**
     * Returns the ViewHost if this view is guest view of parent ViewHost.
     */
    public ViewHost getHost()
    {
        return isGuest() ? (ViewHost) getParent() : null;
    }

    /**
     * Returns whether view is a "guest" child of a ViewHost.
     */
    public boolean isGuest()
    {
        return indexInHost() >= 0;
    }

    /**
     * Returns the index of this view in ViewHost parent (or -1 if parent isn't host).
     */
    public int indexInHost()
    {
        return ViewHost.indexInHost(this);
    }

    /**
     * Returns whether this view is visible.
     */
    public boolean isVisible()  { return _visible; }

    /**
     * Sets whether this view is visible.
     */
    public void setVisible(boolean aValue)
    {
        // Set value, fire prop change
        if (aValue == _visible) return;
        firePropChange(Visible_Prop, _visible, _visible = aValue);

        // Update Showing
        setShowing(_visible && _parent != null && _parent.isShowing());

        // Repaint in parent
        repaintInParent(null);

        // Trigger Parent relayout
        ParentView par = getParent();
        if (par != null) {
            par._children._managed = null;
            relayoutParent();
        }
    }

    /**
     * Returns whether view is visible and has parent that is showing.
     */
    public boolean isShowing()  { return _showing; }

    /**
     * Sets whether view is showing.
     */
    protected void setShowing(boolean aValue)
    {
        // If already set, just return
        if (aValue == _showing) return;

        // Set value and firePropChange
        firePropChange(Showing_Prop, _showing, _showing = aValue);

        // If focused, turn off
        if (!aValue && isFocused())
            setFocused(false);

        // If Anim set, play/suspend
        ViewAnim anim = getAnim(-1);
        if (anim != null) {
            if (aValue && anim.isSuspended())
                anim.play();
            else if (!aValue && anim.isPlaying())
                anim.suspend();
        }
    }

    /**
     * Returns whether view can be hit by mouse.
     */
    public boolean isPickable()  { return _pickable; }

    /**
     * Sets whether view can be hit by mouse.
     */
    public void setPickable(boolean aValue)
    {
        if (aValue == isPickable()) return;
        firePropChange(Pickable_Prop, _pickable, _pickable = aValue);
    }

    /**
     * Returns whether view can be hit by mouse and visible.
     */
    public boolean isPickableVisible()
    {
        return isPickable() && isVisible();
    }

    /**
     * Returns whether view should be painted.
     */
    public boolean isPaintable()  { return _paintable; }

    /**
     * Sets whether view should be painted.
     */
    public void setPaintable(boolean aValue)
    {
        if (aValue == isPaintable()) return;
        firePropChange(Paintable_Prop, _paintable, _paintable = aValue);
        repaint();
    }

    /**
     * Returns whether view should be included when a parent does layout.
     */
    public boolean isManaged()  { return _managed; }

    /**
     * Sets whether view should be included when a parent does layout.
     */
    public void setManaged(boolean aValue)
    {
        _managed = aValue;
        ParentView par = getParent();
        if (par != null) {
            par._children._managed = null;
            relayoutParent();
        }
    }

    /**
     * Returns whether view should be included when a parent does layout.
     */
    public boolean isManagedVisible()
    {
        return isManaged() && isVisible();
    }

    /**
     * Returns whether current mouse location is over this view.
     */
    public boolean isMouseOver()
    {
        WindowView win = getWindow();
        if (win == null) return false;
        EventDispatcher disp = win.getDispatcher();
        return disp.isMouseOver(this);
    }

    /**
     * Returns whether current mouse location is over this view.
     */
    public boolean isMouseDown()
    {
        WindowView win = getWindow();
        if (win == null) return false;
        EventDispatcher disp = win.getDispatcher();
        return disp.isMouseDown(this);
    }

    /**
     * Returns the root view.
     */
    public RootView getRootView()
    {
        return _parent != null ? _parent.getRootView() : null;
    }

    /**
     * Returns the window.
     */
    public WindowView getWindow()
    {
        return _parent != null ? _parent.getWindow() : null;
    }

    /**
     * Returns the ViewUpdater.
     */
    public ViewUpdater getUpdater()
    {
        return _parent != null ? _parent.getUpdater() : null;
    }

    /**
     * Returns the position this view would prefer to take when inside a pane.
     */
    public Pos getLean()
    {
        return Pos.get(_leanX, _leanY);
    }

    /**
     * Sets the lean this view would prefer to take when inside a pane.
     */
    public void setLean(Pos aPos)
    {
        setLeanX(aPos != null ? aPos.getHPos() : null);
        setLeanY(aPos != null ? aPos.getVPos() : null);
    }

    /**
     * Returns the horizontal position this view would prefer to take when inside a pane.
     */
    public HPos getLeanX()  { return _leanX; }

    /**
     * Sets the horizontal lean this view would prefer to take when inside a pane.
     */
    public void setLeanX(HPos aPos)
    {
        if (aPos == _leanX) return;
        firePropChange(LeanX_Prop, _leanX, _leanX = aPos);
        relayoutParent();
    }

    /**
     * Returns the vertical position this view would prefer to take when inside a pane.
     */
    public VPos getLeanY()  { return _leanY; }

    /**
     * Sets the vertical position this view would prefer to take when inside a pane.
     */
    public void setLeanY(VPos aPos)
    {
        if (aPos == _leanY) return;
        firePropChange(LeanY_Prop, _leanY, _leanY = aPos);
        relayoutParent();
    }

    /**
     * Returns whether this view would like to grow horizontally if possible when inside a pane.
     */
    public boolean isGrowWidth()  { return _growWidth; }

    /**
     * Sets whether this view would like to grow horizontally if possible when inside a pane.
     */
    public void setGrowWidth(boolean aValue)
    {
        if (aValue == _growWidth) return;
        firePropChange(GrowWidth_Prop, _growWidth, _growWidth = aValue);
        relayoutParent();
    }

    /**
     * Returns whether this view would like to grow vertically if possible when inside a pane.
     */
    public boolean isGrowHeight()  { return _growHeight; }

    /**
     * Sets whether this view would like to grow vertically if possible when inside a pane.
     */
    public void setGrowHeight(boolean aValue)
    {
        if (aValue == _growHeight) return;
        firePropChange(GrowHeight_Prop, _growHeight, _growHeight = aValue);
        relayoutParent();
    }

    /**
     * Returns whether this view has a horizontal orientation.
     */
    public boolean isHorizontal()  { return !isVertical(); }

    /**
     * Returns whether this view has a vertical orientation.
     */
    public boolean isVertical()  { return _vertical; }

    /**
     * Sets whether this view has a vertical orientation.
     */
    public void setVertical(boolean aValue)
    {
        if (aValue == _vertical) return;
        firePropChange(Vertical_Prop, _vertical, _vertical = aValue);
        relayoutParent();
    }

    /**
     * Returns the list of bindings.
     */
    public List <Binding> getBindings()  { return _bindings; }

    /**
     * Returns the number of bindings.
     */
    public int getBindingCount()  { return _bindings.size(); }

    /**
     * Returns the individual binding at given index.
     */
    public Binding getBinding(int anIndex)
    {
        return _bindings.get(anIndex);
    }

    /**
     * Adds the individual binding to the shape's bindings list.
     */
    public void addBinding(Binding aBinding)
    {
        if (_bindings == Collections.EMPTY_LIST) _bindings = new ArrayList<>();
        removeBinding(aBinding.getPropName());     // Remove current binding for property (if it exists)
        _bindings.add(aBinding);
        aBinding.setView(this);
    }

    /**
     * Removes the binding at the given index from view bindings list.
     */
    public Binding removeBinding(int anIndex)
    {
        return _bindings.remove(anIndex);
    }

    /**
     * Returns the individual binding with the given property name.
     */
    public Binding getBinding(String aPropName)
    {
        // Iterate over bindings and if we find one for given property name, return it
        for (Binding b : _bindings)
            if (b.getPropName().equals(aPropName))
                return b;

        // If property name is mapped, try again
        String mappedName = aPropName.equals("Value") ? getValuePropName() : aPropName;
        if (!aPropName.equals(mappedName))
            return getBinding(mappedName);

        // Return null since binding with property name not found
        return null;
    }

    /**
     * Removes the binding with given property name.
     */
    public boolean removeBinding(String aPropName)
    {
        for (int i = 0, iMax = getBindingCount(); i < iMax; i++) {
            Binding binding = getBinding(i);
            if (binding.getPropName().equals(aPropName)) {
                removeBinding(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the property map.
     */
    public Map<String,Object> getProps()  { return _props; }

    /**
     * Returns a named client property.
     */
    public Object getProp(String aName)
    {
        return _props.get(aName);
    }

    /**
     * Puts a named client property.
     */
    public Object setProp(String aName, Object aValue)
    {
        if (_props == Collections.EMPTY_MAP) _props = new HashMap<>();
        Object val = _props.put(aName, aValue); //firePropChange(aName, val, aValue);
        return val;
    }

    /**
     * Returns whether view minimum width is set.
     */
    public boolean isMinWidthSet()  { return _minWidth >= 0; }

    /**
     * Returns the view minimum width.
     */
    public double getMinWidth()
    {
        return _minWidth >= 0 ? _minWidth : getMinWidthImpl();
    }

    /**
     * Sets the view minimum width.
     */
    public void setMinWidth(double aWidth)
    {
        if (aWidth == _minWidth) return;
        firePropChange(MinWidth_Prop, _minWidth, _minWidth = aWidth);
        relayoutParent();
    }

    /**
     * Returns whether view minimum height is set.
     */
    public boolean isMinHeightSet()  { return _minHeight >= 0; }

    /**
     * Returns the view minimum height.
     */
    public double getMinHeight()
    {
        return _minHeight >= 0 ? _minHeight : getMinHeightImpl();
    }

    /**
     * Sets the view minimum height.
     */
    public void setMinHeight(double aHeight)
    {
        if (aHeight == _minHeight) return;
        firePropChange(MinHeight_Prop, _minHeight, _minHeight = aHeight);
        relayoutParent();
    }

    /**
     * Calculates the minimum width.
     */
    protected double getMinWidthImpl()  { return 0; }

    /**
     * Calculates the minimum height.
     */
    protected double getMinHeightImpl()  { return 0; }

    /**
     * Returns the view minimum size.
     */
    public Size getMinSize()
    {
        return new Size(getMinWidth(), getMinHeight());
    }

    /**
     * Sets the view minimum size.
     */
    public void setMinSize(Size aSize)
    {
        setMinSize(aSize.getWidth(), aSize.getHeight());
    }

    /**
     * Sets the view minimum size.
     */
    public void setMinSize(double aWidth, double aHeight)
    {
        setMinWidth(aWidth);
        setMinHeight(aHeight);
    }

    /**
     * Returns whether view maximum width is set.
     */
    public boolean isMaxWidthSet()  { return _maxWidth >= 0; }

    /**
     * Returns the view maximum width.
     */
    public double getMaxWidth()
    {
        return _maxWidth >= 0 ? _maxWidth : Float.MAX_VALUE;
    }

    /**
     * Sets the view maximum width.
     */
    public void setMaxWidth(double aWidth)
    {
        if (aWidth == _maxWidth) return;
        firePropChange(MaxWidth_Prop, _maxWidth, _maxWidth = aWidth);
        relayoutParent();
    }

    /**
     * Returns whether view maximum height is set.
     */
    public boolean isMaxHeightSet()  { return _maxHeight >= 0; }

    /**
     * Returns the view maximum height.
     */
    public double getMaxHeight()
    {
        return _maxHeight >= 0 ? _maxHeight : Float.MAX_VALUE;
    }

    /**
     * Sets the view maximum height.
     */
    public void setMaxHeight(double aHeight)
    {
        if (aHeight == _maxHeight) return;
        firePropChange(MaxHeight_Prop, _maxHeight, _maxHeight = aHeight);
        relayoutParent();
    }

    /**
     * Returns the view maximum size.
     */
    public Size getMaxSize()
    {
        return new Size(getMaxWidth(), getMaxHeight());
    }

    /**
     * Sets the view maximum size.
     */
    public void setMaxSize(Size aSize)
    {
        setMaxSize(aSize.getWidth(), aSize.getHeight());
    }

    /**
     * Sets the view maximum size.
     */
    public void setMaxSize(double aWidth, double aHeight)
    {
        setMaxWidth(aWidth);
        setMaxHeight(aHeight);
    }

    /**
     * Returns whether preferred width is set.
     */
    public boolean isPrefWidthSet()  { return _prefWidth >= 0; }

    /**
     * Returns the view preferred width.
     */
    public double getPrefWidth()
    {
        return getPrefWidth(-1);
    }

    /**
     * Returns the view preferred width.
     */
    public double getPrefWidth(double aH)
    {
        if (_prefWidth >= 0) return _prefWidth;
        return getPrefWidthImpl(aH);
    }

    /**
     * Sets the view preferred width.
     */
    public void setPrefWidth(double aWidth)
    {
        if (aWidth == _prefWidth) return;
        firePropChange(PrefWidth_Prop, _prefWidth, _prefWidth = aWidth);
        relayoutParent();
    }

    /**
     * Returns whether preferred width is set.
     */
    public boolean isPrefHeightSet()  { return _prefHeight >= 0; }

    /**
     * Returns the view preferred height.
     */
    public double getPrefHeight()
    {
        return getPrefHeight(-1);
    }

    /**
     * Returns the view preferred height.
     */
    public double getPrefHeight(double aW)
    {
        if (_prefHeight >= 0) return _prefHeight;
        return getPrefHeightImpl(aW);
    }

    /**
     * Sets the view preferred height.
     */
    public void setPrefHeight(double aHeight)
    {
        if (aHeight == _prefHeight) return;
        firePropChange(PrefHeight_Prop, _prefHeight, _prefHeight = aHeight);
        relayoutParent();
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return 0; }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return 0; }

    /**
     * Returns the view preferred size.
     */
    public Size getPrefSize()
    {
        double prefW = getPrefWidth();
        double prefH = getPrefHeight();
        return new Size(prefW, prefH);
    }

    /**
     * Sets the view preferred size.
     */
    public void setPrefSize(Size aSize)
    {
        setPrefSize(aSize.width, aSize.height);
    }

    /**
     * Sets the view preferred size.
     */
    public void setPrefSize(double aWidth, double aHeight)
    {
        setPrefWidth(aWidth);
        setPrefHeight(aHeight);
    }

    /**
     * Returns the best width for view - accounting for pref/min/max.
     */
    public double getBestWidth(double aH)
    {
        // If cached case, return cached value
        if (MathUtils.equals(aH, _bestWidthParam) && _bestWidth >= 0)
            return _bestWidth;

        // Calculate best width
        double prefW = getPrefWidth(aH);
        double minW = getMinWidth();
        double maxW = getMaxWidth();
        double bestW = MathUtils.clamp(prefW, minW, maxW);

        // Set and return
        _bestWidthParam = aH;
        return _bestWidth = bestW;
    }

    /**
     * Returns the best height for view - accounting for pref/min/max.
     */
    public double getBestHeight(double aW)
    {
        // If common case, return cached value (set if needed)
        if (MathUtils.equals(aW, _bestHeightParam) && _bestHeight >= 0)
            return _bestHeight;

        // Calculate best height
        double prefH = getPrefHeight(aW);
        double minH = getMinHeight();
        double maxH = getMaxHeight();
        double bestH = MathUtils.clamp(prefH, minH, maxH);

        // Set and return
        _bestHeightParam = aW;
        return _bestHeight = bestH;
    }

    /**
     * Returns the best size.
     */
    public Size getBestSize()
    {
        // Handle Horizontal
        if (isHorizontal()) {
            double bestW = getBestWidth(-1);
            double bestH = getBestHeight(bestW);
            return new Size(bestW, bestH);
        }

        // Handle vertical
        double bestH = getBestHeight(-1);
        double bestW = getBestWidth(bestH);
        return new Size(bestW, bestH);
    }

    /**
     * Returns the alignment.
     */
    public Pos getAlign()  { return _align; }

    /**
     * Sets the alignment.
     */
    public void setAlign(Pos aPos)
    {
        if (aPos == _align) return;
        firePropChange(Align_Prop, _align, _align = aPos);
        relayout();
    }

    /**
     * Returns the horizontal alignment.
     */
    public HPos getAlignX()
    {
        return getAlign().getHPos();
    }

    /**
     * Sets the horizontal alignment.
     */
    public void setAlignX(HPos aPos)
    {
        setAlign(Pos.get(aPos, getAlignY()));
    }

    /**
     * Returns the vertical alignment.
     */
    public VPos getAlignY()
    {
        return getAlign().getVPos();
    }

    /**
     * Sets the vertical alignment.
     */
    public void setAlignY(VPos aPos)
    {
        setAlign(Pos.get(getAlignX(), aPos));
    }

    /**
     * Returns the spacing insets requested between parent/neighbors and the border of this view.
     */
    public Insets getMargin()  { return _margin; }

    /**
     * Sets the spacing insets requested between parent/neighbors and the border of this view.
     */
    public void setMargin(double aTp, double aRt, double aBtm, double aLt)
    {
        setMargin(new Insets(aTp, aRt, aBtm, aLt));
    }

    /**
     * Sets the spacing insets requested between parent/neighbors and the border of this view.
     */
    public void setMargin(Insets theIns)
    {
        // If given null, use default
        if (theIns == null)
            theIns = (Insets) getPropDefault(Margin_Prop);

        // If value already set, just return
        if (Objects.equals(theIns, _margin)) return;

        // Set value, fire prop change, relayout parent
        firePropChange(Margin_Prop, _margin, _margin = theIns);
        relayoutParent();
    }

    /**
     * Returns the spacing insets between the border of this view and it's content.
     */
    public Insets getPadding()  { return _padding; }

    /**
     * Sets the spacing insets between the border of this view and it's content.
     */
    public void setPadding(double aTp, double aRt, double aBtm, double aLt)
    {
        setPadding(new Insets(aTp, aRt, aBtm, aLt));
    }

    /**
     * Sets the spacing insets between the border of this view and it's content.
     */
    public void setPadding(Insets theIns)
    {
        // If given null, use default
        if (theIns == null)
            theIns = (Insets) getPropDefault(Padding_Prop);

        // If value already set, just return
        if (Objects.equals(theIns, _padding)) return;

        // Set value, fire prop change, relayout, relayout parent
        firePropChange(Padding_Prop, _padding, _padding = theIns);
        relayout();
        relayoutParent();
    }

    /**
     * Returns the spacing for content (usually children).
     */
    public double getSpacing()  { return _spacing; }

    /**
     * Sets the spacing for content (usually children).
     */
    public void setSpacing(double aValue)
    {
        if (aValue == _spacing) return;
        firePropChange(Spacing_Prop, _spacing, _spacing = aValue);
        relayout();
        relayoutParent();
    }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.TOP_LEFT; }

    /**
     * Returns the default border.
     */
    public Border getDefaultBorder()  { return null; }

    /**
     * Returns the default fill paint.
     */
    public Paint getDefaultFill()  { return null; }

    /**
     * Returns the default font.
     */
    public Font getDefaultFont()
    {
        View par = getParent();
        return par != null ? par.getFont() : Font.Arial11;
    }

    /**
     * Returns the insets due to border and/or padding.
     */
    public Insets getInsetsAll()
    {
        Insets ins = getPadding();
        Border border = getBorder();
        if (border != null)
            ins = Insets.add(ins, border.getInsets());
        return ins;
    }

    /**
     * Main paint method.
     */
    protected void paintAll(Painter aPntr)
    {
        // Set opacity
        double opacity = getOpacityAll(), opacityOld = 0;
        if (opacity != 1) {
            opacityOld = aPntr.getOpacity();
            aPntr.setOpacity(opacity);
        }

        // If focused, render focused
        if (isFocused() && isFocusPainted()) {
            ViewEffect focusViewEffect = ViewEffect.getFocusViewEffect(this);
            focusViewEffect.paintAll(aPntr);
        }

        // If view has effect, get/create effect painter to speed up successive paints
        else if (_effect != null)
            _effect.paintAll(aPntr);

        // Otherwise, do normal draw
        else {
            paintBack(aPntr);
            paintFront(aPntr);
        }

        // Restore opacity
        if (opacity != 1)
            aPntr.setOpacity(opacityOld);

        // Clear RepaintRect
        _repaintRect = null;
    }

    /**
     * Paints background.
     */
    protected void paintBack(Painter aPntr)
    {
        // Get fill and border (just return if both null)
        Paint fill = getFill();
        Border border = getBorder();
        if (fill == null && border == null)
            return;

        // Get BoundsShape
        Shape shape = getBoundsShape();

        // If fill set, fill inside BoundsShape
        if (fill != null)
            aPntr.fillWithPaint(shape, fill);

        // If border set, draw border around BoundsShape
        if (border != null)
            border.paint(aPntr, shape);
    }

    /**
     * Paints foreground.
     */
    protected void paintFront(Painter aPntr)
    {
        // For UI Builder
        if (getClass() == View.class)
            paintRealClassName(aPntr);
    }

    /**
     * Returns the tool tip text.
     */
    public String getToolTip()  { return _toolTip; }

    /**
     * Sets the tool tip text.
     */
    public void setToolTip(String aString)
    {
        if (Objects.equals(aString, _toolTip)) return;
        firePropChange(ToolTip_Prop, _toolTip, _toolTip = aString);
    }

    // Whether tool tip is enabled
    private boolean _toolTipEnabled;

    /**
     * Returns whether tooltip is enabled.
     */
    public boolean isToolTipEnabled()  { return _toolTipEnabled; }

    /**
     * Sets whether tooltip is enabled.
     */
    public void setToolTipEnabled(boolean aValue)
    {
        if (aValue == isToolTipEnabled()) return;
        firePropChange("ToolTipEnabled", _toolTipEnabled, _toolTipEnabled = aValue);
    }

    /**
     * Returns a tool tip string for given event.
     */
    public String getToolTip(ViewEvent anEvent)  { return null; }

    /**
     * Returns the substitution class name.
     */
    public String getRealClassName()  { return _realClassName; }

    /**
     * Sets the substitution class string.
     */
    public void setRealClassName(String aName)
    {
        if (Objects.equals(aName, getRealClassName()) || getClass().getName().equals(aName)) return;
        firePropChange("RealClassString", _realClassName, _realClassName = aName);
    }

    /**
     * Paints the RealClassName.
     */
    private void paintRealClassName(Painter aPntr)
    {
        // Paint back fill and border
        if (getFill() == null)
            aPntr.fillWithPaint(getBoundsLocal(), Color.LIGHTGRAY);
        if (getBorder() == null)
            Border.createLineBorder(Color.GRAY, 2).paint(aPntr, getBoundsLocal());

        // Get ClassName
        String className = getRealClassName();
        if (className == null) className = "Custom View";
        else className = className.substring(className.lastIndexOf('.') + 1);

        // Paint name
        StringBox stringBox = StringBox.getForStringAndAttributes(className, Font.Arial14.getBold(), Color.WHITE);
        stringBox.setCenteredXY(getWidth() / 2, getHeight() / 2);
        stringBox.paint(aPntr);
    }

    /**
     * Called to relayout.
     */
    public void relayout()  { }

    /**
     * Called to notify parents to relayout because preferred sizes have potentially changed.
     */
    public void relayoutParent()
    {
        _bestWidth = _bestHeight = -1;
        ParentView par = getParent();
        if (par == null) return;
        par.relayout();
        par.relayoutParent();
    }

    /**
     * Called to register view for repaint.
     */
    public void repaint()
    {
        repaint(0, 0, getWidth(), getHeight());
    }

    /**
     * Called to register view for repaint.
     */
    public void repaint(Rect aRect)
    {
        repaint(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /**
     * Called to register view for repaint.
     */
    public void repaint(double aX, double aY, double aW, double aH)
    {
        // If RepaintRect already set, just union with given bounds and return
        if (_repaintRect != null) {
            _repaintRect.union(aX, aY, aW, aH);
            return;
        }

        // If not showing or paintable, just return
        if (!isShowing() || !isPaintable())
            return;

        // Get ViewUpdater (if not available, just return)
        ViewUpdater updater = getUpdater();
        if (updater == null)
            return;

        // Create repaint rect, register for repaintLater and call Parent.setNeedsRepaintDeep()
        _repaintRect = new Rect(aX, aY, aW, aH);
        updater.repaintLater(this);
        if (_parent != null)
            _parent.setNeedsRepaintDeep(true);
    }

    /**
     * Called to repaint in parent for cases where transform might change.
     */
    protected void repaintInParent(Rect aRect)
    {
        // Get parent (just return if not set)
        ParentView par = getParent();
        if (par == null) return;

        // Do normal repaint
        if (aRect == null)
            repaint(0, 0, getWidth(), getHeight());
        else repaint(aRect);

        // Get expanded repaint rect and rect in parent, and have parent repaint
        Rect rectExp = getRepaintRect();
        if (rectExp == null) return;
        Rect parRect = localToParent(rectExp).getBounds();
        parRect.snap();
        parRect.inset(-1); // Shouldn't need this unless someone paints out of bounds (lookin at U, Button)
        par.repaint(parRect);
    }

    /**
     * Returns the rect of view that has been registered for repaint, expanded for focus/effects.
     */
    public Rect getRepaintRect()
    {
        Rect rect = _repaintRect;
        if (rect == null)
            return null;
        Rect rectExp = getRepaintRectExpanded(rect);
        return rectExp;
    }

    /**
     * Returns the given repaint rect, expanded for focus/effects.
     */
    protected Rect getRepaintRectExpanded(Rect aRect)
    {
        // If focused, combine with focus bounds
        Rect rect = aRect;
        if (isFocused() && isFocusPainted()) {
            Effect focusEffect = ViewEffect.getFocusEffect();
            rect = focusEffect.getBounds(rect);
        }

        // If effect, combine effect bounds
        else if (getEffect() != null) {
            Effect effect = getEffect();
            rect = effect.getBounds(rect);
        }

        // Return rect
        return rect;
    }

    /**
     * Returns whether needs repaint.
     */
    public boolean isNeedsRepaint()  { return _repaintRect != null; }

    /**
     * Returns whether this view is the RootView.FocusedView.
     */
    public boolean isFocused()  { return _focused; }

    /**
     * Sets whether this view is the RootView.FocusedView.
     */
    protected void setFocused(boolean aValue)
    {
        // if already set, just return
        if (aValue == _focused) return;

        // Set, fire prop change
        firePropChange(Focused_Prop, _focused, _focused = aValue);

        // Register for repaint
        if (isFocusPainted()) {
            if (aValue)
                repaint();
            else {
                Effect focusEffect = ViewEffect.getFocusEffect();
                Rect bounds = getBoundsLocal();
                Rect repaintRect = focusEffect.getBounds(bounds);
                repaint(repaintRect);
            }
        }
    }

    /**
     * Returns whether this view can get focus.
     */
    public boolean isFocusable()  { return _focusable; }

    /**
     * Sets whether this view can get focus.
     */
    public void setFocusable(boolean aValue)
    {
        if (aValue == _focusable) return;
        firePropChange(Focusable_Prop, _focusable, _focusable = aValue);
    }

    /**
     * Returns whether this view should request focus when pressed.
     */
    public boolean isFocusWhenPressed()  { return _focusWhenPrsd; }

    /**
     * Sets whether this view should request focus when pressed.
     */
    public void setFocusWhenPressed(boolean aValue)
    {
        if (aValue == _focusWhenPrsd) return;
        firePropChange(FocusWhenPressed_Prop, _focusWhenPrsd, _focusWhenPrsd = aValue);
    }

    /**
     * Returns whether focus should change when traversal keys are pressed (Tab).
     */
    public boolean isFocusKeysEnabled()  { return _focusKeysEnbld; }

    /**
     * Sets whether focus should change when traversal keys are pressed (Tab).
     */
    public void setFocusKeysEnabled(boolean aValue)  { _focusKeysEnbld = aValue; }

    /**
     * Returns whether focus ring should be painted when view is focused.
     */
    public boolean isFocusPainted()
    {
        if (!_focusPainted) return false;
        if (getWidth() * getHeight() > 90000)
            return false;
        return _focusPainted;
    }

    /**
     * Sets whether focus ring should be painted when view is focused.
     */
    public void setFocusPainted(boolean aValue)  { _focusPainted = aValue; }

    /**
     * Returns whether view can generate action events in response to certain user interactions.
     */
    public boolean isActionable()  { return _actionable; }

    /**
     * Sets whether view can generate action events in response to certain user interactions.
     */
    public void setActionable(boolean aValue)  { _actionable = aValue; }

    /**
     * Tells view to request focus.
     */
    public void requestFocus()
    {
        if (isFocused()) return;
        WindowView win = getWindow();
        if (win != null)
            win.requestFocus(this);
    }

    /**
     * Returns the next focus View.
     */
    public View getFocusNext()
    {
        ParentView par = getParent();
        return par != null ? par.getFocusNext(this) : null;
    }

    /**
     * Returns the next focus View.
     */
    public View getFocusPrev()
    {
        ParentView par = getParent();
        return par != null ? par.getFocusPrev(this) : null;
    }

    /**
     * Returns the view owner.
     */
    public ViewOwner getOwner()  { return _owner; }

    /**
     * Sets the owner.
     */
    public void setOwner(ViewOwner anOwner)
    {
        // If already set, just return - Owner cannot be reset
        if (_owner != null) return;
        _owner = anOwner;
    }

    /**
     * Returns the owner of given class.
     */
    public <T> T getOwner(Class<T> aClass)
    {
        // If ViewOwner is of given class, return it
        ViewOwner viewOwner = getOwner();
        if (viewOwner != null && aClass.isAssignableFrom(viewOwner.getClass()))
            return (T) viewOwner;

        // Otherwise, forward to parent
        ParentView par = getParent();
        return par != null ? par.getOwner(aClass) : null;
    }

    /**
     * Returns the view environment.
     */
    public ViewEnv getEnv()
    {
        return ViewEnv.getEnv();
    }

    /**
     * Returns the text value of this view.
     */
    public String getText()  { return null; }

    /**
     * Sets the text value of this view.
     */
    public void setText(String aString)  { }

    /**
     * Returns the event adapter for view.
     */
    public EventAdapter getEventAdapter()
    {
        if (_eventAdapter != null) return _eventAdapter;
        return _eventAdapter = new EventAdapter();
    }

    /**
     * Sets an array of enabled events.
     */
    protected void enableEvents(ViewEvent.Type... theTypes)
    {
        EventAdapter eventAdapter = getEventAdapter();
        eventAdapter.enableEvents(this, theTypes);
    }

    /**
     * Sets an array of enabled events.
     */
    protected void disableEvents(ViewEvent.Type... theTypes)
    {
        EventAdapter eventAdapter = getEventAdapter();
        eventAdapter.disableEvents(this, theTypes);
    }

    /**
     * Adds an event filter.
     */
    public void addEventFilter(EventListener aLsnr, ViewEvent.Type... theTypes)
    {
        EventAdapter eventAdapter = getEventAdapter();
        eventAdapter.addFilter(aLsnr, theTypes);
    }

    /**
     * Removes an event filter.
     */
    public void removeEventFilter(EventListener aLsnr, ViewEvent.Type... theTypes)
    {
        EventAdapter eventAdapter = getEventAdapter();
        eventAdapter.removeFilter(aLsnr, theTypes);
    }

    /**
     * Adds an event handler.
     */
    public void addEventHandler(EventListener aLsnr, ViewEvent.Type... theTypes)
    {
        EventAdapter eventAdapter = getEventAdapter();
        eventAdapter.addHandler(aLsnr, theTypes);
    }

    /**
     * Removes an event handler.
     */
    public void removeEventHandler(EventListener aLsnr, ViewEvent.Type... theTypes)
    {
        EventAdapter eventAdapter = getEventAdapter();
        eventAdapter.removeHandler(aLsnr, theTypes);
    }

    /**
     * Fires the action event.
     * This should mostly be called in response to user input events (mouse, key) when a complete change has been made
     * to the primary value of a control view.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        ViewEvent event = ViewEvent.createEvent(this, null, Action, null);
        if (anEvent != null)
            event.setParentEvent(anEvent);

        // Dispatch to View
        dispatchEventToView(event);
    }

    /**
     * Sends an event to this view through processEvent methods.
     */
    public void dispatchEventToView(ViewEvent anEvent)
    {
        // Forward to Filters - just return if consumed
        processEventFilters(anEvent);
        if (anEvent.isConsumed())
            return;

        // Forward to Handlers from last to first
        processEventHandlers(anEvent);
    }

    /**
     * Process ViewEvent for View EventFilters.
     */
    protected void processEventFilters(ViewEvent anEvent)
    {
        // Get event filters and event type
        EventAdapter eventAdapter = getEventAdapter();
        EventListener[] filters = eventAdapter._filters;
        ViewEvent.Type eventType = anEvent.getType();

        // Iterate over filters: If event type supported, send to filter
        for (EventListener lsnr : filters) {
            Set<ViewEvent.Type> types = eventAdapter._types.get(lsnr);
            if (types.contains(eventType)) {
                lsnr.listenEvent(anEvent);
                if (anEvent.isConsumed())
                    break;
            }
        }
    }

    /**
     * Process ViewEvent for View EventHandlers.
     */
    protected void processEventHandlers(ViewEvent anEvent)
    {
        // If event not consumed, send to view
        processEvent(anEvent);

        // If Action event, automatically forward to owner
        if (anEvent.isActionEvent() && _owner != null)
            _owner.dispatchEventToOwner(anEvent);

        // If handlers are set, forward event on
        EventAdapter eventAdapter = getEventAdapter();
        EventListener[] handlers = eventAdapter._handlers;
        if (handlers.length > 0) {
            ViewEvent.Type eventType = anEvent.getType();

            // Iterate over handlers: If event type supported, send to handler
            for (EventListener lsnr : handlers) {
                Set<ViewEvent.Type> types = eventAdapter._types.get(lsnr);
                if (types.contains(eventType))
                    lsnr.listenEvent(anEvent);
            }
        }
    }

    /**
     * Process ViewEvent.
     */
    protected void processEvent(ViewEvent anEvent)  { }

    /**
     * Called when ViewTheme changes.
     */
    protected void themeChanged()  { }

    /**
     * Returns the anim for the given time.
     */
    public ViewAnim getAnim(int aTime)
    {
        if (aTime < 0)
            return _anim;
        if (_anim == null)
            _anim = new ViewAnim(this, 0, 0);
        return aTime > 0 ? _anim.getAnim(aTime) : _anim;
    }

    /**
     * Returns a cleared anim at given time.
     */
    public ViewAnim getAnimCleared(int aTime)
    {
        ViewAnim viewAnim = getAnim(0);
        viewAnim.clear();
        return viewAnim.getAnim(aTime);
    }

    /**
     * Play animations deep.
     */
    public void playAnimDeep()
    {
        ViewAnim.playDeep(this);
    }

    /**
     * Stop animations deep.
     */
    public void stopAnimDeep()
    {
        ViewAnim.stopDeep(this);
    }

    /**
     * Returns the anim time.
     */
    public int getAnimTimeDeep()
    {
        return ViewAnim.getTimeDeep(this);
    }

    /**
     * Sets the anim time deep.
     */
    public void setAnimTimeDeep(int aValue)
    {
        ViewAnim.setTimeDeep(this, aValue);
    }

    /**
     * Returns the physics objects that provides information for physics simulations.
     */
    public ViewPhysics getPhysics()  { return _physics; }

    /**
     * Returns the physics objects that provides information for physics simulations.
     */
    public ViewPhysics getPhysics(boolean doCreate)
    {
        if (_physics != null || !doCreate) return _physics;
        return _physics = new ViewPhysics();
    }

    /**
     * Standard clone implementation.
     */
    @Override
    protected PropObject clone() throws CloneNotSupportedException
    {
        // Do normal version
        View clone;
        try { clone = (View) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Clear Parent, EventAdapter, Owner
        clone._parent = null;
        clone._eventAdapter = null;
        clone._owner = null;

        // Return
        return clone;
    }

    /**
     * Returns a mapped property name.
     */
    protected String getValuePropName()  { return "Value"; }

    /**
     * Initialize Props. Override to provide custom defaults.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Name
        aPropSet.addPropNamed(Name_Prop, String.class, null);

        // X, Y, Width, Height
        aPropSet.addPropNamed(X_Prop, double.class, 0d);
        aPropSet.addPropNamed(Y_Prop, double.class, 0d);
        aPropSet.addPropNamed(Width_Prop, double.class, 0d);
        aPropSet.addPropNamed(Height_Prop, double.class, 0d);

        // Rotate, ScaleX, ScaleY, TransX, TransY
        aPropSet.addPropNamed(Rotate_Prop, double.class, 0d);
        aPropSet.addPropNamed(ScaleX_Prop, double.class, 0d);
        aPropSet.addPropNamed(ScaleY_Prop, double.class, 0d);
        aPropSet.addPropNamed(TransX_Prop, double.class, 0d);
        aPropSet.addPropNamed(TransY_Prop, double.class, 0d);

        // Align, Margin, Padding, Spacing, Vertical
        aPropSet.addPropNamed(Align_Prop, Pos.class, DEFAULT_ALIGN);
        aPropSet.addPropNamed(Margin_Prop, Insets.class, DEFAULT_MARGIN);
        aPropSet.addPropNamed(Padding_Prop, Insets.class, DEFAULT_MARGIN);
        aPropSet.addPropNamed(Spacing_Prop, double.class, DEFAULT_SPACING);
        aPropSet.addPropNamed(Vertical_Prop, boolean.class, DEFAULT_VERTICAL);

        // LeanX, LeanY, GrowWidth, GrowHeight
        aPropSet.addPropNamed(LeanX_Prop, HPos.class, null);
        aPropSet.addPropNamed(LeanY_Prop, VPos.class, null);
        aPropSet.addPropNamed(GrowWidth_Prop, boolean.class, false);
        aPropSet.addPropNamed(GrowHeight_Prop, boolean.class, false);

        // MinWidth, MinHeight, MaxWidth, MaxHeight, PrefWidth, PrefHeight
        aPropSet.addPropNamed(MinWidth_Prop, double.class, 0d);
        aPropSet.addPropNamed(MinHeight_Prop, double.class, 0d);
        aPropSet.addPropNamed(MaxWidth_Prop, double.class, 0d);
        aPropSet.addPropNamed(MaxHeight_Prop, double.class, 0d);
        aPropSet.addPropNamed(PrefWidth_Prop, double.class, 0d);
        aPropSet.addPropNamed(PrefHeight_Prop, double.class, 0d);

        // Border, BorderRadius, Fill, Effect, Opacity
        aPropSet.addPropNamed(Border_Prop, Border.class, null);
        aPropSet.addPropNamed(BorderRadius_Prop, double.class, 0d);
        aPropSet.addPropNamed(Fill_Prop, Paint.class, null);
        aPropSet.addPropNamed(Effect_Prop, Effect.class, null);
        aPropSet.addPropNamed(Opacity_Prop, double.class, 1d);

        // Font, Text, ToolTip, Cursor, Clip
        aPropSet.addPropNamed(Font_Prop, Font.class, null);
        aPropSet.addPropNamed(Text_Prop, String.class, null).setSkipArchival(true);
        aPropSet.addPropNamed(ToolTip_Prop, String.class, null);
        aPropSet.addPropNamed(Cursor_Prop, Cursor.class, null).setSkipArchival(true);
        aPropSet.addPropNamed(Clip_Prop, Shape.class, null).setSkipArchival(true);

        // Disabled, Visible, Pickable, Paintable
        aPropSet.addPropNamed(Disabled_Prop, boolean.class, false);
        aPropSet.addPropNamed(Visible_Prop, boolean.class, true);
        aPropSet.addPropNamed(Pickable_Prop, boolean.class, true);
        aPropSet.addPropNamed(Paintable_Prop, boolean.class, true);

        // Focusable, FocusWhenPressed, Focused
        aPropSet.addPropNamed(Focusable_Prop, boolean.class, false);
        aPropSet.addPropNamed(FocusWhenPressed_Prop, boolean.class, true);
        aPropSet.addPropNamed(Focused_Prop, boolean.class, true);

        // Parent, Showing
        aPropSet.addPropNamed(Parent_Prop, ParentView.class, null).setSkipArchival(true);
        aPropSet.addPropNamed(Showing_Prop, boolean.class, false).setSkipArchival(true);
    }

    /**
     * Returns the value for given prop name.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Map property name
        String propName = aPropName.equals("Value") ? getValuePropName() : aPropName;

        // Handle properties
        switch (propName) {

            // Name
            case Name_Prop: return getName();

            // X, Y, Width, Height
            case X_Prop: return getX();
            case Y_Prop: return getY();
            case Width_Prop: return getWidth();
            case Height_Prop: return getHeight();

            // Rotate, ScaleX, ScaleY, TransX, TransY
            case Rotate_Prop: return getRotate();
            case ScaleX_Prop: return getScaleX();
            case ScaleY_Prop: return getScaleY();
            case TransX_Prop: return getTransX();
            case TransY_Prop: return getTransY();

            // Align, Margin, Padding, Spacing, Vertical
            case Align_Prop: return getAlign();
            case Margin_Prop: return getMargin();
            case Padding_Prop: return getPadding();
            case Spacing_Prop: return getSpacing();
            case Vertical_Prop: return isVertical();

            // LeanX, LeanY, GrowWidth, GrowHeight
            case LeanX_Prop: return getLeanX();
            case LeanY_Prop: return getLeanY();
            case GrowWidth_Prop: return isGrowWidth();
            case GrowHeight_Prop: return isGrowHeight();

            // MinWidth, MinHeight, MaxWidth, MaxHeight, PrefWidth, PrefHeight
            case View.MinWidth_Prop: return getMinWidth();
            case View.MinHeight_Prop: return getMinHeight();
            case View.MaxWidth_Prop: return getMaxWidth();
            case View.MaxHeight_Prop: return getMaxHeight();
            case View.PrefWidth_Prop: return getPrefWidth();
            case View.PrefHeight_Prop: return getPrefHeight();

            // Border, BorderRadius, Fill, Effect, Opacity
            case Border_Prop: return getBorder();
            case BorderRadius_Prop: return getBorderRadius();
            case Fill_Prop: return getFill();
            case Effect_Prop: return getEffect();
            case Opacity_Prop: return getOpacity();

            // Font, Text, ToolTip, Cursor, Clip
            case Font_Prop: return getFont();
            case Text_Prop: return getText();
            case ToolTip_Prop: return getToolTip();
            case Cursor_Prop: return getCursor();
            case Clip_Prop: return getClip();

            // Disabled, Visible, Pickable, Paintable
            case Disabled_Prop: return isDisabled();
            case Visible_Prop: return isVisible();
            case Pickable_Prop: return isPickable();
            case Paintable_Prop: return isPaintable();

            // Focusable, FocusWhenPressed, Focused
            case Focusable_Prop: return isFocusable();
            case FocusWhenPressed_Prop: return isFocusWhenPressed();
            case Focused_Prop: return isFocused();

            // Parent, Showing
            case Parent_Prop: return getParent();
            case Showing_Prop: return isShowing();

            // Items, SelItem, SelIndex
            case Selectable.Items_Prop: return ((Selectable<?>) this).getItems();
            case Selectable.SelItem_Prop: return ((Selectable<?>) this).getSelItem();
            case Selectable.SelIndex_Prop: return ((Selectable<?>) this).getSelIndex();

            // Do normal version
            default:
                System.out.println("View.getPropValue: Unknown property name: " + propName);
                return KeyChain.getValue(this, propName);
        }
    }

    /**
     * Sets the value for given prop name.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Map property name
        String propName = aPropName.equals("Value") ? getValuePropName() : aPropName;

        // Handle properties
        switch (propName) {

            // Name
            case Name_Prop: setName(Convert.stringValue(aValue)); break;

            // X, Y, Width, Height
            case X_Prop: setX(Convert.doubleValue(aValue)); break;
            case Y_Prop: setY(Convert.doubleValue(aValue)); break;
            case Width_Prop: setWidth(Convert.doubleValue(aValue)); break;
            case Height_Prop: setHeight(Convert.doubleValue(aValue)); break;

            // Rotate, ScaleX, ScaleY, TransX, TransY
            case Rotate_Prop: setRotate(Convert.doubleValue(aValue)); break;
            case ScaleX_Prop: setScaleX(Convert.doubleValue(aValue)); break;
            case ScaleY_Prop: setScaleY(Convert.doubleValue(aValue)); break;
            case TransX_Prop: setTransX(Convert.doubleValue(aValue)); break;
            case TransY_Prop: setTransY(Convert.doubleValue(aValue)); break;

            // Align, Margin, Padding, Spacing, Vertical
            case Align_Prop: setAlign((Pos) aValue); break;
            case Margin_Prop: setMargin((Insets) aValue); break;
            case Padding_Prop: setPadding((Insets) aValue); break;
            case Spacing_Prop: setSpacing(Convert.doubleValue(aValue)); break;
            case Vertical_Prop: setVertical(Convert.boolValue(aValue));

            // Alignment: LeanX, LeanY, GrowWidth, GrowHeight
            case LeanX_Prop: setLeanX((HPos) aValue); break;
            case LeanY_Prop: setLeanY((VPos) aValue); break;
            case GrowWidth_Prop: setGrowWidth(Convert.boolValue(aValue)); break;
            case GrowHeight_Prop: setGrowHeight(Convert.boolValue(aValue)); break;

            // MinWidth, MinHeight, MaxWidth, MaxHeight, PrefWidth, PrefHeight
            case MinWidth_Prop: setMinWidth(Convert.doubleValue(aValue)); break;
            case MinHeight_Prop: setMinHeight(Convert.doubleValue(aValue)); break;
            case MaxWidth_Prop: setMaxWidth(Convert.doubleValue(aValue)); break;
            case MaxHeight_Prop: setMaxHeight(Convert.doubleValue(aValue)); break;
            case PrefWidth_Prop: setPrefWidth(Convert.doubleValue(aValue)); break;
            case PrefHeight_Prop: setPrefHeight(Convert.doubleValue(aValue)); break;

            // Border, BorderRadius, Fill, Effect, Opacity
            case Border_Prop: setBorder((Border) aValue); break;
            case BorderRadius_Prop: setBorderRadius(Convert.doubleValue(aValue)); break;
            case Fill_Prop: setFill(aValue instanceof Paint ? (Paint) aValue : null); break;
            case Effect_Prop: setEffect((Effect) aValue); break;
            case Opacity_Prop: setOpacity(Convert.doubleValue(aValue)); break;

            // Font, Text, ToolTip, Cursor, Clip
            case Font_Prop: setFont((Font) aValue); break;
            case Text_Prop: setText(Convert.stringValue(aValue)); break;
            case ToolTip_Prop: setToolTip(Convert.stringValue(aValue)); break;
            case Cursor_Prop: setCursor((Cursor) aValue); break;
            case Clip_Prop: setClip((Shape) aValue); break;

            // Disabled, Visible, Pickable, Paintable
            case Disabled_Prop: setDisabled(Convert.boolValue(aValue)); break;
            case Visible_Prop: setVisible(Convert.boolValue(aValue)); break;
            case Pickable_Prop: setPickable(Convert.boolValue(aValue)); break;
            case Paintable_Prop: setPaintable(Convert.boolValue(aValue)); break;

            // Focusable, FocusWhenPressed, Focused
            case Focusable_Prop: setFocusable(Convert.boolValue(aValue)); break;
            case FocusWhenPressed_Prop: setFocusWhenPressed(Convert.boolValue(aValue)); break;
            case Focused_Prop: setFocused(Convert.boolValue(aValue)); break;

            // Parent, Showing
            case Parent_Prop: setParent((ParentView) aValue); break;
            case Showing_Prop: setShowing(Convert.boolValue(aValue)); break;

            // Items, SelItem, SelIndex
            case Selectable.Items_Prop: Selectable.setItems((Selectable<?>) this, aValue); break;
            case Selectable.SelItem_Prop: ((Selectable) this).setSelItem(aValue); break;
            case Selectable.SelIndex_Prop: ((Selectable<?>) this).setSelIndex(Convert.intValue(aValue)); break;

            // Do normal version
            default:
                System.out.println("View.setPropValue: Unknown prop name: " + propName);
                KeyChain.setValueSafe(this, propName, aValue);
        }
    }

    /**
     * Override property defaults for View.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Font
            case Font_Prop: getDefaultFont();

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get class name for element
        String className;
        for (Class<?> cls = getClass(); ; cls = cls.getSuperclass()) {
            if (cls == ParentView.class) continue;
            if (cls.getName().startsWith("snap.view")) {
                className = cls.getSimpleName();
                break;
            }
        }

        // Get new element with class name
        XMLElement e = new XMLElement(className);

        // Archive name
        if (getName() != null && getName().length() > 0)
            e.add(Name_Prop, getName());

        // Archive X, Y, Width, Height
        View par = getParent();
        if (this instanceof SpringView || par instanceof SpringView || par instanceof PageView) {
            if (getX() != 0) e.add("x", getX());
            if (getY() != 0) e.add("y", getY());
            if (getWidth() != 0) e.add("width", getWidth());
            if (getHeight() != 0) e.add("height", getHeight());
        }

        // Archive MinWidth, MinHeight, PrefWidth, PrefHeight
        if (isMinWidthSet())
            e.add(MinWidth_Prop, getMinWidth());
        if (isMinHeightSet())
            e.add(MinHeight_Prop, getMinHeight());
        if (isPrefWidthSet())
            e.add(PrefWidth_Prop, getPrefWidth());
        if (isPrefHeightSet())
            e.add(PrefHeight_Prop, getPrefHeight());

        // Archive TransX, TransY, Rotate, ScaleX, ScaleY
        if (getTransX() != 0)
            e.add(TransX_Prop, getTransX());
        if (getTransY() != 0)
            e.add(TransY_Prop, getTransY());
        if (getRotate() != 0)
            e.add(Rotate_Prop, getRotate());
        if (getScaleX() != 1)
            e.add(ScaleX_Prop, getScaleX());
        if (getScaleY() != 1)
            e.add(ScaleY_Prop, getScaleY());

        // Archive Vertical
        if (!isPropDefault(Vertical_Prop))
            e.add(Vertical_Prop, isVertical());

        // Archive Border, BorderRadius
        Border border = getBorder();
        if (border != null && !Objects.equals(border, getDefaultBorder()))
            e.add(anArchiver.toXML(border, this));
        double borderRadius = getBorderRadius();
        if (borderRadius > 0)
            e.add(BorderRadius_Prop, borderRadius);

        // Archive Fill
        Paint fill = getFill();
        if (fill != null && !Objects.equals(fill, getDefaultFill()))
            e.add(anArchiver.toXML(fill, this));

        // Archive Effect
        Effect effect = getEffect();
        if (effect != null)
            e.add(anArchiver.toXML(effect, this));

        // Archive font
        if (!Objects.equals(getFont(), getDefaultFont()))
            e.add(getFont().toXML(anArchiver));

        // Archive Disabled, Visible, Opacity
        if (isDisabled())
            e.add(Disabled_Prop, true);
        if (!isVisible())
            e.add(Visible_Prop, false);
        if (getOpacity() < 1)
            e.add(Opacity_Prop, getOpacity());

        // Archive Align, Margin, Padding, Spacing
        if (getAlign() != getDefaultAlign())
            e.add(Align_Prop, getAlign());
        if (!isPropDefault(Margin_Prop))
            e.add(Margin_Prop, getMargin().getString());
        if (!isPropDefault(Padding_Prop))
            e.add(Padding_Prop, getPadding().getString());
        if (!isPropDefault(Spacing_Prop))
            e.add(Spacing_Prop, getSpacing());

        // Archive GrowWidth, GrowHeight, LeanX, LeanY
        if (isGrowWidth())
            e.add(GrowWidth_Prop, true);
        if (isGrowHeight())
            e.add(GrowHeight_Prop, true);
        if (getLeanX() != null)
            e.add(LeanX_Prop, getLeanX());
        if (getLeanY() != null)
            e.add(LeanY_Prop, getLeanY());

        // Archive animation
        if (getAnim(-1) != null && !getAnim(0).isEmpty()) {
            ViewAnim anim = getAnim(0);
            e.add(anim.toXML(anArchiver));
        }

        // Archive bindings
        for (Binding b : getBindings())
            e.add(b.toXML(anArchiver));

        // Archive ToolTip
        if (getToolTip() != null)
            e.add(ToolTip_Prop, getToolTip());

        // Archive RealClassName
        className = getRealClassName();
        if (className != null && className.length() > 0)
            e.add("Class", className);

        // Return the element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive class property for subclass substitution, if available
        if (anElement.hasAttribute("Class"))
            setRealClassName(anElement.getAttributeValue("Class"));

        // Unarchive Name
        if (anElement.hasAttribute(Name_Prop))
            setName(anElement.getAttributeValue(Name_Prop));

        // Unarchive X, Y, Width, Height
        if (anElement.hasAttribute("x") || anElement.hasAttribute("y") ||
                anElement.hasAttribute("width") || anElement.hasAttribute("height")) {
            double x = anElement.getAttributeFloatValue("x");
            double y = anElement.getAttributeFloatValue("y");
            double w = anElement.getAttributeFloatValue("width");
            double h = anElement.getAttributeFloatValue("height");
            setBounds(x, y, w, h);
        }

        // Unarchive MinWidth, MinHeight, PrefWidth, PrefHeight
        if (anElement.hasAttribute(MinWidth_Prop))
            setMinWidth(anElement.getAttributeFloatValue(MinWidth_Prop));
        if (anElement.hasAttribute(MinHeight_Prop))
            setMinHeight(anElement.getAttributeFloatValue(MinHeight_Prop));
        if (anElement.hasAttribute(PrefWidth_Prop))
            setPrefWidth(anElement.getAttributeFloatValue(PrefWidth_Prop));
        if (anElement.hasAttribute(PrefHeight_Prop))
            setPrefHeight(anElement.getAttributeFloatValue(PrefHeight_Prop));

        // Unarchive TransX, TransY, Rotate, ScaleX, ScaleY
        if (anElement.hasAttribute(TransX_Prop))
            setTransX(anElement.getAttributeFloatValue(TransX_Prop));
        if (anElement.hasAttribute(TransY_Prop))
            setTransY(anElement.getAttributeFloatValue(TransY_Prop));
        if (anElement.hasAttribute(Rotate_Prop))
            setRotate(anElement.getAttributeFloatValue(Rotate_Prop));
        if (anElement.hasAttribute(ScaleX_Prop))
            setScaleX(anElement.getAttributeFloatValue(ScaleX_Prop));
        if (anElement.hasAttribute(ScaleY_Prop))
            setScaleY(anElement.getAttributeFloatValue(ScaleY_Prop));

        // Unarchive Vertical
        if (anElement.hasAttribute(Vertical_Prop))
            setVertical(anElement.getAttributeBoolValue(Vertical_Prop));

        // Unarchive Border
        int borderIndex = anArchiver.indexOf(anElement, Border.class);
        if (borderIndex >= 0) {
            Border border = (Border) anArchiver.fromXML(anElement.get(borderIndex), this);
            setBorder(border);
        }

        if (anElement.hasAttribute(BorderRadius_Prop))
            setBorderRadius(anElement.getAttributeFloatValue(BorderRadius_Prop));

        // Unarchive Fill
        int fillIndex = anArchiver.indexOf(anElement, Paint.class);
        if (fillIndex >= 0) {
            Paint fill = (Paint) anArchiver.fromXML(anElement.get(fillIndex), this);
            setFill(fill);
        }

        // Unarchive Effect
        int effectIndex = anArchiver.indexOf(anElement, Effect.class);
        if (effectIndex >= 0) {
            Effect eff = (Effect) anArchiver.fromXML(anElement.get(effectIndex), this);
            setEffect(eff);
        }

        // Unarchive Fill, Border (Legacy)
        XMLElement sxml = anElement.getElement("stroke");
        if (sxml != null) {
            String cstr = sxml.getAttributeValue("color");
            Color sc = cstr != null ? new Color(cstr) : Color.BLACK;
            double sw = sxml.getAttributeFloatValue("width", 1);
            setBorder(sc, sw);
        }
        XMLElement fxml = anElement.getElement("fill");
        if (fxml != null) {
            Paint fill = (Paint) anArchiver.fromXML(fxml, this);
            setFill(fill);
        }
        XMLElement bxml = anElement.getElement("border");
        if (bxml != null) // Only KeysPanel is left
            setBorder(null);

        // Unarchive font
        XMLElement fontXML = anElement.getElement(Font_Prop);
        if (fontXML != null)
            setFont((Font) anArchiver.fromXML(fontXML, this));

        // Unarchive Disabled, Visible, Opacity
        if (anElement.hasAttribute(Disabled_Prop))
            setDisabled(anElement.getAttributeBoolValue(Disabled_Prop));
        if (anElement.hasAttribute(Visible_Prop))
            setVisible(anElement.getAttributeBoolValue(Visible_Prop));
        if (anElement.hasAttribute(Opacity_Prop))
            setOpacity(anElement.getAttributeFloatValue(Opacity_Prop));

        // Unarchive Align, Margin, Padding, Spacing
        if (anElement.hasAttribute(Align_Prop))
            setAlign(Pos.get(anElement.getAttributeValue(Align_Prop)));
        if (anElement.hasAttribute(Margin_Prop)) {
            Insets ins = Insets.get(anElement.getAttributeValue(Margin_Prop));
            setMargin(ins);
        }
        if (anElement.hasAttribute(Padding_Prop)) {
            Insets ins = Insets.get(anElement.getAttributeValue(Padding_Prop));
            setPadding(ins);
        }
        if (anElement.hasAttribute(Spacing_Prop))
            setSpacing(anElement.getAttributeDoubleValue(Spacing_Prop));

        // Unarchive GrowWidth, GrowHeight, LeanX, LeanY
        if (anElement.hasAttribute(GrowWidth_Prop))
            setGrowWidth(anElement.getAttributeBoolValue(GrowWidth_Prop));
        if (anElement.hasAttribute(GrowHeight_Prop))
            setGrowHeight(anElement.getAttributeBoolValue(GrowHeight_Prop));
        if (anElement.hasAttribute(LeanX_Prop))
            setLeanX(HPos.get(anElement.getAttributeValue(LeanX_Prop)));
        if (anElement.hasAttribute(LeanY_Prop))
            setLeanY(VPos.get(anElement.getAttributeValue(LeanY_Prop)));

        // Unarchive animation
        XMLElement animXML = anElement.getElement("Anim");
        if (animXML == null) animXML = anElement.getElement("KeyFrame") != null ? anElement : null;
        if (animXML != null)
            getAnim(0).fromXML(anArchiver, animXML);

        // Unarchive bindings
        for (int i = anElement.indexOf("binding"); i >= 0; i = anElement.indexOf("binding", i + 1)) {
            XMLElement bx = anElement.get(i);
            addBinding(new Binding().fromXML(anArchiver, bx));
        }

        // Unarchive ToolTip
        if (anElement.hasAttribute("ttip"))
            setToolTip(anElement.getAttributeValue("ttip"));
        if (anElement.hasAttribute(ToolTip_Prop))
            setToolTip(anElement.getAttributeValue(ToolTip_Prop));

        // Unarchive class property for subclass substitution, if available
        if (anElement.hasAttribute("Class"))
            setRealClassName(anElement.getAttributeValue("Class"));

        // Return this shape
        return this;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toStringProps()
    {
        // Append Name
        StringBuffer sb = new StringBuffer();
        String name = getName();
        if (name != null && name.length() > 0)
            StringUtils.appendProp(sb, "Name", name);

        // Append Text
        String text = getText();
        if (text != null && text.length() > 0) {
            if (text.length() > 40)
                text = text.substring(0, 40) + "...";
            StringUtils.appendProp(sb, "Text", text);
        }

        // Append Bounds
        StringUtils.appendProp(sb, "Bounds", getBounds());

        // Return
        return sb.toString();
    }
}