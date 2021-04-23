/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;

import snap.geom.*;
import snap.gfx.*;
import snap.text.StringBox;
import snap.util.*;

/**
 * A standard view implementation to show graphics and handle events and form the basis of all views (buttons, sliders,
 * text fields, etc.).
 */
public class View extends PropObject implements XMLArchiver.Archivable {

    // The name of this view
    private String  _name;
    
    // The view location
    private double  _x, _y;
    
    // The view size
    private double  _width, _height;
    
    // The view translation from x and y
    private double  _tx, _ty;
    
    // The view rotation
    private double  _rot;
    
    // The view scale from x and y
    private double  _sx = 1, _sy = 1;
    
    // The alignment of content in this view
    private Pos  _align = getDefaultAlign();
    
    // The margin to be provided around this view
    private Insets  _margin = getDefaultMargin();
    
    // The padding between the border and content in this view
    private Insets  _padding = getDefaultPadding();
    
    // The horizontal position this view would prefer to take when inside a pane
    private HPos  _leanX;
    
    // The vertical position this view would prefer to take when inside a pane
    private VPos  _leanY;
    
    // Whether this view would like to grow horizontally/vertically if possible when inside a pane
    private boolean  _growWidth, _growHeight;
    
    // Whether this view has a vertical orientation.
    private boolean  _vertical = getDefaultVertical();
    
    // The view minimum width and height
    private double  _minWidth = -1, _minHeight = -1;
    
    // The view maximum width and height
    private double  _maxWidth = -1, _maxHeight = -1;
    
    // The view preferred width and height
    private double  _prefWidth = -1, _prefHeight = -1;
    
    // The view best width and height
    private double  _bestWidth = -1, _bestHeight = -1, _bestWidthParam, _bestHeightParam;
    
    // Whether view is disabled
    private boolean  _disabled;
    
    // Whether view is currently the RootView.FocusedView
    private boolean  _focused;
    
    // Whether view can receive focus
    private boolean  _focusable;
    
    // Whether view should request focus when pressed
    private boolean  _focusWhenPrsd;

    // Whether view focus should change when traveral key is pressed (Tab)
    private boolean  _focusKeysEnbld = true;
    
    // Whether view should paint focus ring when focused
    private boolean  _focusPainted = true;
 
    // Whether view is visible
    private boolean  _visible = true;
    
    // Whether view is visible and has parent that is showing
    protected boolean  _showing;
 
    // Whether view can be hit by mouse
    private boolean  _pickable = true;
    
    // Whether view should be painted
    private boolean  _paintable = true;
    
    // Whether view should be included in layout
    private boolean  _managed = true;
    
    // The view fill
    private Paint  _fill;
    
    // The view border
    private Border  _border;
    
    // The ViewEffect to manage effect rendering for this view and current effect
    protected ViewEffect  _effect;
    
    // The opacity
    private double  _opacity = 1;
    
    // The view font
    protected Font  _font;
    
    // The view cursor
    private Cursor  _cursor = Cursor.DEFAULT;
    
    // The tooltip
    private String  _ttip;
    
    // The clip (if set)
    private Shape  _clip;
    
    // Bindings for this view
    private List <Binding>  _bindings = Collections.EMPTY_LIST;
    
    // Client properties
    private Map  _props = Collections.EMPTY_MAP;
    
    // The parent of this view
    private ParentView  _parent;
    
    // The real class name, if shape component is really a custom subclass
    private String  _realClassName;
    
    // The event adapter
    private EventAdapter  _evtAdptr;
    
    // Provides animation for View
    private ViewAnim  _anim;
    
    // The current rect that needs to be repainted in this view
    protected Rect  _repaintRect;
    
    // Provides information for physics simulations
    private ViewPhysics  _physics;
    
    // The view owner of this view
    private ViewOwner  _owner;
    
    // Shared empty insets
    private static final Insets _emptyIns = new Insets(0);
    
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
    public static final String Vertical_Prop = "Vertical";
    public static final String Focused_Prop = "Focused";
    public static final String Focusable_Prop = "Focusable";
    public static final String FocusWhenPressed_Prop = "FocusWhenPressed";
    public static final String Border_Prop = "Border";
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
    public static final ViewEvent.Type DragEnter = ViewEvent.Type.DragEnter;
    public static final ViewEvent.Type DragOver = ViewEvent.Type.DragOver;
    public static final ViewEvent.Type DragExit = ViewEvent.Type.DragExit;
    public static final ViewEvent.Type DragDrop = ViewEvent.Type.DragDrop;
    public static final ViewEvent.Type DragGesture = ViewEvent.Type.DragGesture;
    public static final ViewEvent.Type DragSourceEnter = ViewEvent.Type.DragSourceEnter;
    public static final ViewEvent.Type DragSourceExit = ViewEvent.Type.DragSourceExit;
    public static final ViewEvent.Type DragSourceOver = ViewEvent.Type.DragSourceOver;
    public static final ViewEvent.Type DragSourceEnd = ViewEvent.Type.DragSourceEnd;
    public static final ViewEvent.Type WinActivate = ViewEvent.Type.WinActivate;
    public static final ViewEvent.Type WinDeactivate = ViewEvent.Type.WinDeactivate;
    public static final ViewEvent.Type WinOpen = ViewEvent.Type.WinOpen;
    public static final ViewEvent.Type WinClose = ViewEvent.Type.WinClose;
    public static final ViewEvent.Type KeyEvents[] = { KeyPress, KeyRelease, KeyType };
    public static final ViewEvent.Type MouseEvents[] = { MousePress, MouseDrag, MouseRelease,
        MouseEnter, MouseMove, MouseExit };
    public static final ViewEvent.Type DragEvents[] = { DragEnter, DragExit, DragOver, DragDrop };

    /**
     * Returns the name for the view.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name for the view.
     */
    public void setName(String aName)
    {
        if (SnapUtils.equals(aName, _name)) return;
        firePropChange(Name_Prop, _name, _name=StringUtils.min(aName));
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
        // Set value and fire prop change
        if (aValue==_x) return;
        repaintInParent(null);
        firePropChange(X_Prop, _x, _x=aValue);
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
        // Set value and fire prop change
        if (aValue==_y) return;
        repaintInParent(null);
        firePropChange(Y_Prop, _y, _y=aValue);
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
        // Set value, fire prop change and register for relayout
        if (aValue==_width) return;
        repaintInParent(null);
        firePropChange(Width_Prop, _width, _width=aValue);
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
        // Set value, fire prop change and register for relayout
        if (aValue==_height) return;
        repaintInParent(null);
        firePropChange(Height_Prop, _height, _height=aValue);
        relayout();
    }

    /**
     * Returns the mid x.
     */
    public double getMidX()  { return getX() + getWidth()/2; }

    /**
     * Returns the mid y.
     */
    public double getMidY()  { return getY() + getHeight()/2; }

    /**
     * Returns the max x.
     */
    public double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the max x.
     */
    public double getMaxY()  { return getY() + getHeight(); }

    /**
     * Returns the view x/y.
     */
    public Point getXY()  { return new Point(getX(),getY()); }

    /**
     * Sets the view x/y.
     */
    public void setXY(double aX, double aY)
    {
        setX(aX); setY(aY);
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
        setWidth(aW); setHeight(aH);
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
        setX(aX); setY(aY);
        setWidth(aW); setHeight(aH);
    }

    /**
     * Returns the bounds inside view.
     */
    public Rect getBoundsLocal()
    {
        return new Rect(0,0, getWidth(), getHeight());
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
        if (isLocalToParentSimple()) { setXY(getX() + aX, getY() + aY); return; }
        Point p0 = localToParent(0,0);
        Point p1 = localToParent(aX,aY);
        double p2x = Math.round(getX() + p1.x - p0.x);
        double p2y = Math.round(getY() + p1.y - p0.y);
        setXY(p2x, p2y);
    }

    /**
     * Sets the view size such that it maintains it's location in parent.
     */
    public void setSizeLocal(double aW, double aH)
    {
        if (isLocalToParentSimple()) { setSize(aW, aH); return; }
        Point p0 = localToParent(0,0);
        setSize(aW,aH);
        Point p1 = localToParent(0,0);
        double p2x = Math.round(getX() - (p1.x - p0.x));
        double p2y = Math.round(getY() - (p1.y - p0.y));
        setXY(Math.round(p2x), p2y);
    }

    /**
     * Returns the bounds in parent coords.
     */
    public Rect getBoundsParent()
    {
        return getBoundsShapeParent().getBounds();
    }

    /**
     * Returns the bounds shape in view coords.
     */
    public Shape getBoundsShape()
    {
        return getBoundsLocal();
    }

    /**
     * Returns the bounds shape in parent coords.
     */
    public Shape getBoundsShapeParent()
    {
        return localToParent(getBoundsShape());
    }

    /**
     * Returns the translation of this view from X.
     */
    public double getTransX()  { return _tx; }

    /**
     * Sets the translation of this view from X.
     */
    public void setTransX(double aValue)
    {
        if (aValue==_tx) return;
        repaintInParent(null);
        firePropChange(TransX_Prop, _tx, _tx=aValue);
    }

    /**
     * Returns the translation of this view from Y.
     */
    public double getTransY()  { return _ty; }

    /**
     * Sets the translation of this view from Y.
     */
    public void setTransY(double aValue)
    {
        if (aValue==_ty) return;
        repaintInParent(null);
        firePropChange(TransY_Prop, _ty, _ty=aValue);
    }

    /**
     * Returns the rotation of the view in degrees.
     */
    public double getRotate()  { return _rot; }

    /**
     * Turn to given angle.
     */
    public void setRotate(double theDegrees)
    {
        if (theDegrees==_rot) return;
        repaintInParent(null);
        firePropChange(Rotate_Prop, _rot, _rot=theDegrees);
    }

    /**
     * Returns the scale of this view from X.
     */
    public double getScaleX()  { return _sx; }

    /**
     * Sets the scale of this view from X.
     */
    public void setScaleX(double aValue)
    {
        if (aValue==_sx) return;
        repaintInParent(null);
        firePropChange(ScaleX_Prop, _sx, _sx=aValue);
    }

    /**
     * Returns the scale of this view from Y.
     */
    public double getScaleY()  { return _sy; }

    /**
     * Sets the scale of this view from Y.
     */
    public void setScaleY(double aValue)
    {
        if (aValue==_sy) return;
        repaintInParent(null);
        firePropChange(ScaleY_Prop, _sy, _sy=aValue);
    }

    /**
     * Returns the scale of this view.
     */
    public double getScale()  { return _sx; }

    /**
     * Sets the scale of this view from Y.
     */
    public void setScale(double aValue)  { setScaleX(aValue); setScaleY(aValue); }

    /**
     * Returns fill paint.
     */
    public Paint getFill()  { return _fill; }

    /**
     * Sets fill paint.
     */
    public void setFill(Paint aPaint)
    {
        if (SnapUtils.equals(aPaint,getFill())) return;
        firePropChange(Fill_Prop, _fill, _fill=aPaint);
        repaint();
    }

    /**
     * Returns the fill as color.
     */
    public Color getFillColor()
    {
        Paint fill = getFill(); if (fill==null || fill instanceof Color) return (Color)fill;
        if (fill instanceof GradientPaint) return ((GradientPaint)fill).getStopColor(0);
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
        if (SnapUtils.equals(aBorder,getBorder())) return;
        firePropChange(Border_Prop, _border, _border=aBorder);
        relayout(); relayoutParent(); repaint();
    }

    /**
     * Convenience to set border to given color and line width.
     */
    public void setBorder(Color aColor, double aWidth)
    {
        setBorder(aColor!=null ? Border.createLineBorder(aColor, aWidth) : null);
    }

    /**
     * Returns effect.
     */
    public Effect getEffect()
    {
        return _effect !=null ? _effect._eff : null;
    }

    /**
     * Sets paint.
     */
    public void setEffect(Effect anEff)
    {
        // If already set, just return
        Effect old = getEffect(); if (SnapUtils.equals(anEff, getEffect())) return;

        // Set new ViewEffect, fire prop change and repaint
        repaintInParent(null);
        _effect = anEff!=null ? new ViewEffect(this, anEff) : null;
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
        if (aValue==_opacity) return;
        firePropChange(Opacity_Prop, _opacity, _opacity=aValue);
        repaint();
    }

    /**
     * Returns the combined opacity of this view and it's parents.
     */
    public double getOpacityAll()
    {
        double opacity = getOpacity(); ParentView par = getParent();
        return par!=null ? opacity*par.getOpacityAll() : opacity;
    }

    /**
     * Returns whether font has been explicitly set for this view.
     */
    public boolean isFontSet()  { return _font!=null; }

    /**
     * Returns the font for the view (defaults to parent font).
     */
    public Font getFont()  { return _font!=null ? _font : getDefaultFont(); }

    /**
     * Sets the font for the view.
     */
    public void setFont(Font aFont)
    {
        // Special case: If both fonts are null, assume parent updated
        if (aFont==null && _font==null) {
            relayout(); relayoutParent(); repaint(); return; }

        // Do normal version
        if (SnapUtils.equals(aFont, _font)) return;
        firePropChange(Font_Prop, _font, _font=aFont);
        relayout(); relayoutParent(); repaint();
    }

    /**
     * Returns the cursor.
     */
    public Cursor getCursor()  { return _cursor; }

    /**
     * Sets the cursor.
     */
    public void setCursor(Cursor aCursor)
    {
        if (aCursor==null) aCursor = Cursor.DEFAULT; if (aCursor==_cursor) return;
        firePropChange(Cursor_Prop, _cursor, _cursor=aCursor);
        WindowView win = getWindow();
        if (win!=null) win.resetActiveCursor();
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
        if (aValue==_disabled) return;
        firePropChange(Disabled_Prop, _disabled, _disabled=aValue);
        repaint();
    }

    /**
     * Whether view is enabled.
     */
    public boolean isEnabled()  { return !_disabled; }

    /**
     * Sets whether view is enabled.
     */
    public void setEnabled(boolean aValue)  { setDisabled(!aValue); }

    /**
     * Returns whether view should clip to bounds.
     */
    public boolean isClipToBounds()  { return _clip==ClipToBoundsRect; }

    /**
     * Sets whether view should clip to bounds.
     */
    public void setClipToBounds(boolean aValue)
    {
        if (aValue==isClipToBounds()) return;
        setClip(aValue ? ClipToBoundsRect : null);
    }

    // The shared rect to represent "ClipToBounds"
    private static Rect ClipToBoundsRect = new Rect();

    /**
     * Returns the clip shape.
     */
    public Shape getClip()
    {
        if (_clip==ClipToBoundsRect)
            return getBoundsShape();
        return _clip;
    }

    /**
     * Sets the clip shape.
     */
    public void setClip(Shape aShape)
    {
        if (SnapUtils.equals(aShape,_clip)) return;
        firePropChange(Clip_Prop, _clip, _clip = aShape);
    }

    /**
     * Returns the clip bounds.
     */
    public Rect getClipBounds()
    {
        Shape clip = getClip();
        return clip!=null ? clip.getBounds() : null;
    }

    /**
     * Returns the clip of this view due to all parents.
     */
    public Shape getClipAll()
    {
        Shape vshp = getParent()!=null ? getParent().getClipAll() : null;
        if (vshp!=null)
            vshp = parentToLocal(vshp);
        if (getClip()!=null)
            vshp = vshp!=null ? Shape.intersect(vshp, getClip()) : getClip();
        return vshp;
    }

    /**
     * Returns the clip bounds due to all parents.
     */
    public Rect getClipAllBounds()
    {
        Shape clip = getClipAll();
        return clip!=null ? clip.getBounds() : null;
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
    public boolean isLocalToParentSimple()  { return _rot==0 && _sx==1 && _sy==1; }

    /**
     * Returns the transform.
     */
    public Transform getLocalToParent()
    {
        double viewX = getX();
        double viewY = getY();
        if (isLocalToParentSimple())
            return new Transform(viewX + _tx, viewY +_ty);

        // Get location, size, point of rotation, rotation, scale, skew
        double x = viewX + getTransX();
        double y = viewY + getTransY();
        double w = getWidth();
        double h = getHeight();
        double prx = w/2, pry = h/2;
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
        for (View view=getParent(); view != aPar; ) {

            // If simple, just add translation
            if (view.isLocalToParentSimple())
                xfm.preTranslate(view._x + view._tx,view._y + view._ty);

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
            return new Point(aX + _x + _tx,aY + _y + _ty);

        // Otherwise do full transform
        Transform localToParent = getLocalToParent();
        return localToParent.transform(aX, aY);
    }

    /**
     * Converts a point from local to given parent.
     */
    public Point localToParent(double aX, double aY, View aPar)
    {
        Point point = new Point(aX, aY);
        for (View view=this; view!=aPar && view!=null; view=view.getParent()) {
            if (view.isLocalToParentSimple())
                point.offset(view._x + view._tx,view._y + view._ty);
            else point = view.localToParent(point.x, point.y);
        }
        return point;
    }

    /**
     * Converts a shape from local to parent.
     */
    public Shape localToParent(Shape aShape)
    {
        return aShape.copyFor(getLocalToParent());
    }

    /**
     * Converts a point from local to given parent.
     */
    public Shape localToParent(Shape aShape, View aPar)
    {
        return aShape.copyFor(getLocalToParent(aPar));
    }

    /**
     * Returns the transform from parent to local coords.
     */
    public Transform getParentToLocal()
    {
        if (isLocalToParentSimple())
            return new Transform(-_x-_tx, -_y-_ty);
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
            return new Point(aX - _x - _tx,aY - _y - _ty);

        // Otherwise do full transform
        Transform parentToLocal = getParentToLocal();
        return parentToLocal.transform(aX, aY);
    }

    /**
     * Converts a point from given parent to local.
     */
    public Point parentToLocal(double aX, double aY, View aPar)
    {
        Transform parentToLocal = getParentToLocal(aPar);
        return parentToLocal.transform(aX,aY);
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
        if (win==null) {
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
    public boolean contains(Point aPnt)  { return contains(aPnt.x, aPnt.y); }

    /**
     * Returns whether view contains point.
     */
    public boolean contains(double aX, double aY)
    {
        return 0<=aX && aX<=getWidth() && 0<=aY && aY<=getHeight();
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
        firePropChange(Parent_Prop, _parent, _parent=aPar);

        // Propagate Showing to children
        setShowing(aPar!=null && aPar.isShowing());

        // If inherrit font, propagate to children
        if (!isFontSet()) setFont(null);
    }

    /**
     * Returns the first parent with given class by iterating up parent hierarchy.
     */
    public <T extends View> T getParent(Class<T> aClass)
    {
        for (ParentView s=getParent(); s!=null; s=s.getParent())
            if (aClass.isInstance(s)) return (T)s;
        return null; // Return null since parent of class wasn't found
    }

    /**
     * Returns the number of ancestors of this view.
     */
    public int getParentCount()
    {
        int pc = 0; for (View n=getParent(); n!=null; n=n.getParent()) pc++;
        return pc;
    }

    /**
     * Returns whether given view is an ancestor of this view.
     */
    public boolean isAncestor(View aView)
    {
        for (View n=getParent();n!=null;n=n.getParent())
            if (n==aView) return true;
        return false;
    }

    /**
     * Returns the index of this view in parent.
     */
    public int indexInParent()
    {
        ParentView par = getParent();
        return par!=null ? par.indexOfChild(this) : -1;
    }

    /**
     * Returns the ViewHost if this view is guest view of parent ViewHost.
     */
    public ViewHost getHost()
    {
        return isGuest() ? (ViewHost)getParent() : null;
    }

    /**
     * Returns whether view is a "guest" child of a ViewHost.
     */
    public boolean isGuest()  { return indexInHost()>=0; }

    /**
     * Returns the index of this view in ViewHost parent (or -1 if parent isn't host).
     */
    public int indexInHost()  { return ViewHost.indexInHost(this); }

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
        if (aValue==_visible) return;
        firePropChange(Visible_Prop, _visible, _visible=aValue);

        // Update Showing
        setShowing(_visible && _parent!=null && _parent.isShowing());

        // Repaint in parent
        repaintInParent(null);

        // Trigger Parent relayout
        ParentView par = getParent();
        if (par!=null) {
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
        if (aValue==_showing) return;
        if (aValue) repaint();
        firePropChange(Showing_Prop, _showing, _showing=aValue);

        // If focused, turn off
        if (!aValue && isFocused())
            setFocused(false);

        // If Anim set, play/suspend
        ViewAnim anim = getAnim(-1);
        if (aValue && anim!=null  && anim.isSuspended())
            anim.play();
        else if (!aValue && anim!=null && anim.isPlaying())
            anim.suspend();
    }

    /**
     * Returns whether view can be hit by mouse.
     */
    public boolean isPickable()  { return _pickable; }

    /**
     * Sets whether view can be hit by mouse.
     */
    public void setPickable(boolean aValue)  { _pickable = aValue; }

    /**
     * Returns whether view can be hit by mouse and visible.
     */
    public boolean isPickableVisible()  { return isPickable() && isVisible(); }

    /**
     * Returns whether view should be painted.
     */
    public boolean isPaintable()  { return _paintable; }

    /**
     * Sets whether view should be painted.
     */
    public void setPaintable(boolean aValue)  { _paintable = aValue; }

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
        if (par!=null) {
            par._children._managed = null;
            relayoutParent();
        }
    }

    /**
     * Returns whether view should be included when a parent does layout.
     */
    public boolean isManagedVisible()  { return isManaged() && isVisible(); }

    /**
     * Returns whether current mouse location is over this view.
     */
    public boolean isMouseOver()
    {
        WindowView win = getWindow(); if (win==null) return false;
        EventDispatcher disp = win.getDispatcher();
        return disp.isMouseOver(this);
    }

    /**
     * Returns whether current mouse location is over this view.
     */
    public boolean isMouseDown()
    {
        WindowView win = getWindow(); if (win==null) return false;
        EventDispatcher disp = win.getDispatcher();
        return disp.isMouseDown(this);
    }

    /**
     * Returns the root view.
     */
    public RootView getRootView()  { return _parent!=null ? _parent.getRootView() : null; }

    /**
     * Returns the window.
     */
    public WindowView getWindow()  { return _parent!=null ? _parent.getWindow() : null; }

    /**
     * Returns the ViewUpdater.
     */
    public ViewUpdater getUpdater()  { return _parent!=null ? _parent.getUpdater() : null; }

    /**
     * Returns the position this view would prefer to take when inside a pane.
     */
    public Pos getLean()  { return Pos.get(_leanX, _leanY); }

    /**
     * Sets the lean this view would prefer to take when inside a pane.
     */
    public void setLean(Pos aPos)
    {
        setLeanX(aPos!=null ? aPos.getHPos() : null);
        setLeanY(aPos!=null ? aPos.getVPos() : null);
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
        if (aPos==_leanX) return;
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
        if (aPos==_leanY) return;
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
        if (aValue==_growWidth) return;
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
        if (aValue==_growHeight) return;
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
        if (aValue==_vertical) return;
        firePropChange(Vertical_Prop, _vertical, _vertical = aValue);
        relayoutParent();
    }

    /**
     * Returns the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
     */
    public String getAutosizing()
    {
        HPos lx = getLeanX();
        VPos ly = getLeanY();
        boolean left = lx==null || lx==HPos.LEFT, right = lx==null || lx==HPos.RIGHT;
        boolean top = ly==null || ly==VPos.TOP, btm = ly==null || ly==VPos.BOTTOM;
        char c1 = left ? '-' : '~', c2 = isGrowWidth() ? '~' : '-', c3 = right ? '-' : '~';
        char c4 = top ? '-' : '~', c5 = isGrowHeight() ? '~' : '-', c6 = btm ? '-' : '~';
        return "" + c1 + c2 + c3 + ',' + c4 + c5 + c6;
    }

    /**
     * Sets the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
     */
    public void setAutosizing(String aVal)
    {
        String val = aVal!=null ? aVal : "--~,--~"; if (val.length()<7) return;
        char c1 = val.charAt(0), c2 = val.charAt(1), c3 = val.charAt(2);
        char c4 = val.charAt(4), c5 = val.charAt(5), c6 = val.charAt(6);
        setGrowWidth(c2=='~'); setGrowHeight(c5=='~');
        setLeanX(c1=='~' && c3=='~' ? HPos.CENTER : c1=='~' ? HPos.RIGHT : c3=='~' ? HPos.LEFT : null);
        setLeanY(c4=='~' && c6=='~' ? VPos.CENTER : c4=='~' ? VPos.BOTTOM : c6=='~' ? VPos.TOP : null);
    }

    /**
     * Returns the autosizing default.
     */
    public String getAutosizingDefault()  { return "--~,--~"; }

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
    public Binding getBinding(int anIndex)  { return _bindings.get(anIndex); }

    /**
     * Adds the individual binding to the shape's bindings list.
     */
    public void addBinding(Binding aBinding)
    {
        if (_bindings==Collections.EMPTY_LIST) _bindings = new ArrayList();
        removeBinding(aBinding.getPropertyName());     // Remove current binding for property (if it exists)
        _bindings.add(aBinding);
        aBinding.setView(this);
    }

    /**
     * Removes the binding at the given index from view bindings list.
     */
    public Binding removeBinding(int anIndex)  { return _bindings.remove(anIndex); }

    /**
     * Removes the given binding from view bindings list.
     */
    public boolean removeBinding(Binding aBinding)
    {
        int index = ListUtils.indexOfId(getBindings(), aBinding);
        if (index>=0) removeBinding(index);
        return index>=0;
    }

    /**
     * Returns the individual binding with the given property name.
     */
    public Binding getBinding(String aPropName)
    {
        // Iterate over bindings and if we find one for given property name, return it
        for (Binding b : _bindings)
            if (b.getPropertyName().equals(aPropName))
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
        for (int i=0, iMax=getBindingCount(); i<iMax; i++) { Binding binding = getBinding(i);
            if (binding.getPropertyName().equals(aPropName)) {
                removeBinding(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a binding for given name and key.
     */
    public void addBinding(String aPropName, String aKey)
    {
        String pname = aPropName.equals("Value") ? getValuePropName() : aPropName;
        addBinding(new Binding(pname, aKey));
    }

    /**
     * Returns the property map.
     */
    public Map getProps()  { return _props; }

    /**
     * Returns a named client property.
     */
    public Object getProp(String aName)  { return _props.get(aName); }

    /**
     * Puts a named client property.
     */
    public Object setProp(String aName, Object aValue)
    {
        if (_props==Collections.EMPTY_MAP) _props = new HashMap();
        Object val = _props.put(aName, aValue); //firePropChange(aName, val, aValue);
        return val;
    }

    /**
     * Returns whether view minimum width is set.
     */
    public boolean isMinWidthSet()  { return _minWidth>=0; }

    /**
     * Returns the view minimum width.
     */
    public double getMinWidth()  { return _minWidth>=0 ? _minWidth : getMinWidthImpl(); }

    /**
     * Sets the view minimum width.
     */
    public void setMinWidth(double aWidth)
    {
        if (aWidth==_minWidth) return;
        firePropChange(MinWidth_Prop, _minWidth, _minWidth = aWidth);
        relayoutParent();
    }

    /**
     * Returns whether view minimum height is set.
     */
    public boolean isMinHeightSet()  { return _minHeight>=0; }

    /**
     * Returns the view minimum height.
     */
    public double getMinHeight()  { return _minHeight>=0 ? _minHeight : getMinHeightImpl(); }

    /**
     * Sets the view minimum height.
     */
    public void setMinHeight(double aHeight)
    {
        if (aHeight==_minHeight) return;
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
    public Size getMinSize()  { return new Size(getMinWidth(), getMinHeight()); }

    /**
     * Sets the view minimum size.
     */
    public void setMinSize(Size aSize)  { setMinSize(aSize.getWidth(), aSize.getHeight()); }

    /**
     * Sets the view minimum size.
     */
    public void setMinSize(double aWidth, double aHeight)  { setMinWidth(aWidth); setMinHeight(aHeight); }

    /**
     * Returns whether view maximum width is set.
     */
    public boolean isMaxWidthSet()  { return _maxWidth>=0; }

    /**
     * Returns the view maximum width.
     */
    public double getMaxWidth()  { return _maxWidth>=0 ? _maxWidth : Float.MAX_VALUE; }

    /**
     * Sets the view maximum width.
     */
    public void setMaxWidth(double aWidth)
    {
        if (aWidth==_maxWidth) return;
        firePropChange(MaxWidth_Prop, _maxWidth, _maxWidth = aWidth);
        relayoutParent();
    }

    /**
     * Returns whether view maximum height is set.
     */
    public boolean isMaxHeightSet()  { return _maxHeight>=0; }

    /**
     * Returns the view maximum height.
     */
    public double getMaxHeight()  { return _maxHeight>=0 ? _maxHeight : Float.MAX_VALUE; }

    /**
     * Sets the view maximum height.
     */
    public void setMaxHeight(double aHeight)
    {
        if (aHeight==_maxHeight) return;
        firePropChange(MaxHeight_Prop, _maxHeight, _maxHeight = aHeight);
        relayoutParent();
    }

    /**
     * Returns the view maximum size.
     */
    public Size getMaxSize()  { return new Size(getMaxWidth(), getMaxHeight()); }

    /**
     * Sets the view maximum size.
     */
    public void setMaxSize(Size aSize)  { setMaxSize(aSize.getWidth(), aSize.getHeight()); }

    /**
     * Sets the view maximum size.
     */
    public void setMaxSize(double aWidth, double aHeight)  { setMaxWidth(aWidth); setMaxHeight(aHeight); }

    /**
     * Returns whether preferred width is set.
     */
    public boolean isPrefWidthSet()  { return _prefWidth>=0; }

    /**
     * Returns the view preferred width.
     */
    public double getPrefWidth()  { return getPrefWidth(-1); }

    /**
     * Returns the view preferred width.
     */
    public double getPrefWidth(double aH)
    {
        return _prefWidth>=0 ? _prefWidth : getPrefWidthImpl(aH);
    }

    /**
     * Sets the view preferred width.
     */
    public void setPrefWidth(double aWidth)
    {
        if (aWidth==_prefWidth) return;
        firePropChange(PrefWidth_Prop, _prefWidth, _prefWidth = aWidth);
        relayoutParent();
    }

    /**
     * Returns whether preferred width is set.
     */
    public boolean isPrefHeightSet()  { return _prefHeight>=0; }

    /**
     * Returns the view preferred height.
     */
    public double getPrefHeight()  { return getPrefHeight(-1); }

    /**
     * Returns the view preferred height.
     */
    public double getPrefHeight(double aW)
    {
        return _prefHeight>=0 ? _prefHeight : getPrefHeightImpl(aW);
    }

    /**
     * Sets the view preferred height.
     */
    public void setPrefHeight(double aHeight)
    {
        if (aHeight==_prefHeight) return;
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
    public Size getPrefSize()  { return new Size(getPrefWidth(), getPrefHeight()); }

    /**
     * Sets the view preferred size.
     */
    public void setPrefSize(Size aSize)  { setPrefSize(aSize.getWidth(), aSize.getHeight()); }

    /**
     * Sets the view preferred size.
     */
    public void setPrefSize(double aWidth, double aHeight)  { setPrefWidth(aWidth); setPrefHeight(aHeight); }

    /**
     * Returns the best width for view - accounting for pref/min/max.
     */
    public double getBestWidth(double aH)
    {
        // If cached case, return cached value
        if (MathUtils.equals(aH, _bestWidthParam) && _bestWidth>=0)
            return _bestWidth;

        // Otherwise, return uncached value
        _bestWidthParam = aH;
        return _bestWidth = MathUtils.clamp(getPrefWidth(aH), getMinWidth(), getMaxWidth());
    }

    /**
     * Returns the best height for view - accounting for pref/min/max.
     */
    public double getBestHeight(double aW)
    {
        // If common case, return cached value (set if needed)
        if (MathUtils.equals(aW, _bestHeightParam) && _bestHeight>=0)
            return _bestHeight;

        // Otherwise, return uncached value
        _bestHeightParam = aW;
        return _bestHeight = MathUtils.clamp(getPrefHeight(aW), getMinHeight(), getMaxHeight());
    }

    /**
     * Returns the best size.
     */
    public Size getBestSize()
    {
        double bw, bh;
        if (isHorizontal()) {
            bw = getBestWidth(-1);
            bh = getBestHeight(bw);
        }
        else {
            bh = getBestHeight(-1);
            bw = getBestWidth(bh);
        }
        return new Size(bw, bh);
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
        if (aPos==_align) return;
        firePropChange(Align_Prop, _align, _align=aPos);
        relayout();
    }

    /**
     * Sets the horizontal alignment.
     */
    public void setAlign(HPos aPos)  { setAlign(Pos.get(aPos, getAlign().getVPos())); }

    /**
     * Sets the vertical alignment.
     */
    public void setAlign(VPos aPos)  { setAlign(Pos.get(getAlign().getHPos(), aPos)); }

    /**
     * Returns the spacing insets requested between parent/neighbors and the border of this view.
     */
    public Insets getMargin()  { return _margin; }

    /**
     * Sets the spacing insets requested between parent/neighbors and the border of this view.
     */
    public void setMargin(double aTp, double aRt, double aBtm, double aLt)  { setMargin(new Insets(aTp,aRt,aBtm,aLt)); }

    /**
     * Sets the spacing insets requested between parent/neighbors and the border of this view.
     */
    public void setMargin(Insets theIns)
    {
        // If value already set, just return
        if (theIns==null) theIns = getDefaultMargin();
        if (SnapUtils.equals(theIns, _margin)) return;

        // Set value, fire prop change, relayout parent
        firePropChange(Padding_Prop, _margin, _margin = theIns);
        relayoutParent();
    }

    /**
     * Returns the spacing insets between the border of this view and it's content.
     */
    public Insets getPadding()  { return _padding; }

    /**
     * Sets the spacing insets between the border of this view and it's content.
     */
    public void setPadding(double aTp, double aRt, double aBtm, double aLt)  { setPadding(new Insets(aTp,aRt,aBtm,aLt)); }

    /**
     * Sets the spacing insets between the border of this view and it's content.
     */
    public void setPadding(Insets theIns)
    {
        if (theIns==null) theIns = getDefaultPadding();
        if (SnapUtils.equals(theIns,_padding)) return;
        firePropChange(Padding_Prop, _padding, _padding = theIns);
        relayout(); relayoutParent();
    }

    /**
     * Returns the spacing for views that support it (Label, Button, ColView, RowView etc.).
     */
    public double getSpacing()  { return 0; }

    /**
     * Sets the spacing for views that support it (Label, Button, ColView, RowView etc.).
     */
    public void setSpacing(double aValue)  { }

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
        return par!=null ? par.getFont() : Font.Arial11;
    }

    /**
     * Returns the margin default.
     */
    public Insets getDefaultMargin()  { return _emptyIns; }

    /**
     * Returns the padding default.
     */
    public Insets getDefaultPadding()  { return _emptyIns; }

    /**
     * Returns the padding default.
     */
    public boolean getDefaultVertical()  { return false; }

    /**
     * Returns the insets due to border and/or padding.
     */
    public Insets getInsetsAll()
    {
        Insets ins = getPadding();
        Border border = getBorder();
        if (border!=null) ins = Insets.add(ins, border.getInsets());
        return ins;
    }

    /**
     * Main paint method.
     */
    protected void paintAll(Painter aPntr)
    {
        // Set opacity
        double opacity = getOpacityAll(), opacityOld = 0;
        if (opacity!=1) {
            opacityOld = aPntr.getOpacity();
            aPntr.setOpacity(opacity);
        }

        // If focused, render focused
        if (isFocused() && isFocusPainted())
            ViewEffect.getFocusViewEffect(this).paintAll(aPntr);

        // If view has effect, get/create effect painter to speed up successive paints
        else if (_effect !=null)
            _effect.paintAll(aPntr);

        // Otherwise, do normal draw
        else {
            paintBack(aPntr);
            paintFront(aPntr);
        }

        // Restore opacity
        if (opacity!=1)
            aPntr.setOpacity(opacityOld);

        // Clear RepaintRect
        _repaintRect = null;
    }

    /**
     * Paints background.
     */
    protected void paintBack(Painter aPntr)
    {
        Paint fill = getFill();
        Border border = getBorder(); if (fill==null && border==null) return;
        Shape shape = getBoundsShape();
        if (fill!=null) {
            aPntr.setPaint(fill); aPntr.fill(shape);
        }
        if (border!=null)
            border.paint(aPntr, shape);
    }

    /**
     * Paints foreground.
     */
    protected void paintFront(Painter aPntr)
    {
        // If View with unrealized RealClassName, paint it
        if (getClass()==View.class) {
            String cname = getRealClassName();
            if (cname==null) cname = "Custom View";
            else cname = cname.substring(cname.lastIndexOf('.')+1);
            if (getFill()==null) {
                aPntr.setPaint(Color.LIGHTGRAY);
                aPntr.fill(getBoundsLocal());
            }
            if (getBorder()==null) {
                aPntr.setPaint(Color.GRAY); aPntr.setStroke(Stroke.Stroke2);
                aPntr.draw(getBoundsLocal().getInsetRect(1));
                aPntr.setStroke(Stroke.Stroke1);
            }
            StringBox sbox = StringBox.getForStringAndAttributes(cname, Font.Arial14.getBold(), Color.WHITE);
            sbox.setCenteredXY(getWidth()/2, getHeight()/2);
            sbox.paint(aPntr);
        }
    }

    /**
     * Returns the tool tip text.
     */
    public String getToolTip()  { return _ttip; }

    /**
     * Sets the tool tip text.
     */
    public void setToolTip(String aString)
    {
        if (SnapUtils.equals(aString,_ttip)) return;
        firePropChange(ToolTip_Prop, _ttip, _ttip=aString);
    }

    /**
     * Returns wether tooltip is enabled.
     */
    public boolean isToolTipEnabled()  { return _ttipEnbld; } boolean _ttipEnbld;

    /**
     * Returns wether tooltip is enabled.
     */
    public void setToolTipEnabled(boolean aValue)  { firePropChange("ToolTipEnabled", _ttipEnbld, _ttipEnbld=aValue); }

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
        if (SnapUtils.equals(aName,getRealClassName()) || getClass().getName().equals(aName)) return;
        firePropChange("RealClassString", _realClassName, _realClassName = aName);
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
        ParentView par = getParent(); if (par==null) return;
        par.relayout(); par.relayoutParent();
    }

    /**
     * Called to register view for repaint.
     */
    public void repaint()  { repaint(0, 0, getWidth(), getHeight()); }

    /**
     * Called to register view for repaint.
     */
    public void repaint(Rect aRect)  { repaint(aRect.x,aRect.y,aRect.width,aRect.height); }

    /**
     * Called to register view for repaint.
     */
    public void repaint(double aX, double aY, double aW, double aH)
    {
        // If RepaintRect already set, just union with given bounds and return
        if (_repaintRect!=null) {
            _repaintRect.union(aX, aY, aW, aH);
            return;
        }

        // Get ViewUpdater (if not available, just return)
        ViewUpdater updater = getUpdater(); if (updater==null) return;

        // Create repaint rect, register for repaintLater and call Parent.setNeedsRepaintDeep()
        _repaintRect = new Rect(aX, aY, aW, aH);
        updater.repaintLater(this);
        if (_parent!=null) _parent.setNeedsRepaintDeep(true);
    }

    /**
     * Called to repaint in parent for cases where transform might change.
     */
    protected void repaintInParent(Rect aRect)
    {
        // Get parent (just return if not set)
        ParentView par = getParent(); if (par==null) return;

        // Do normal repaint
        if (aRect==null) repaint(0, 0, getWidth(), getHeight());
        else repaint(aRect);

        // Get expanded repaint rect and rect in parent, and have parent repaint
        Rect rectExp = getRepaintRect(); if (rectExp==null) return;
        Rect parRect = localToParent(rectExp).getBounds();
        parRect.snap(); parRect.inset(-1); // Shouldn't need this unless someone paints out of bounds (lookin at U, Button)
        par.repaint(parRect);
    }

    /**
     * Returns the rect of view that has been registered for repaint, expanded for focus/effects.
     */
    public Rect getRepaintRect()
    {
        Rect rect = _repaintRect; if (rect == null) return null;
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
        if (isFocused() && isFocusPainted())
            rect = ViewEffect.getFocusEffect().getBounds(rect);

        // If effect, combine effect bounds
        else if (getEffect()!=null)
            rect = getEffect().getBounds(rect);

        // Return rect
        return rect;
    }

    /**
     * Returns whether needs repaint.
     */
    public boolean isNeedsRepaint()  { return _repaintRect!=null; }

    /**
     * Returns whether this view is the RootView.FocusedView.
     */
    public boolean isFocused()  { return _focused; }

    /**
     * Sets whether this view is the RootView.FocusedView.
     */
    protected void setFocused(boolean aValue)
    {
        if (aValue==_focused) return;
        firePropChange(Focused_Prop, _focused, _focused=aValue);
        if (isFocusPainted()) {
            if (aValue) repaint();
            else repaint(ViewEffect.getFocusEffect().getBounds(getBoundsLocal()));
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
        if (aValue==_focusable) return;
        firePropChange(Focusable_Prop, _focusable, _focusable=aValue);
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
        if (aValue==_focusWhenPrsd) return;
        firePropChange(FocusWhenPressed_Prop, _focusWhenPrsd, _focusWhenPrsd=aValue);
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
        if (getWidth()*getHeight()>90000) return false;
        return _focusPainted;
    }

    /**
     * Sets whether focus ring should be painted when view is focused.
     */
    public void setFocusPainted(boolean aValue)  { _focusPainted = aValue; }

    /**
     * Tells view to request focus.
     */
    public void requestFocus()
    {
        if (isFocused()) return;
        WindowView win = getWindow();
        if (win!=null)
            win.requestFocus(this);
    }

    /**
     * Returns the next focus View.
     */
    public View getFocusNext()
    {
        return getParent()!=null ? getParent().getFocusNext(this) : null;
    }

    /**
     * Returns the next focus View.
     */
    public View getFocusPrev()
    {
        return getParent()!=null ? getParent().getFocusPrev(this) : null;
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
        if (_owner!=null) return;
        _owner = anOwner;
        if (_evtAdptr!=null && _evtAdptr.isEnabled(Action))
            anOwner.enableEvents(this, Action);
    }

    /**
     * Returns the owner of given class.
     */
    public <T> T getOwner(Class <T> aClass)
    {
        if (getOwner()!=null && aClass.isAssignableFrom(getOwner().getClass()))
            return (T)getOwner();
        return getParent()!=null ? getParent().getOwner(aClass) : null;
    }

    /**
     * Returns the view environment.
     */
    public ViewEnv getEnv()  { return ViewEnv.getEnv(); }

    /**
     * Returns the value for given prop name.
     */
    public Object getPropValue(String aPropName)
    {
        // Map property name
        String pname = aPropName.equals("Value") ? getValuePropName() : aPropName;

        // Handle properties
        switch(pname) {
            case View.X_Prop: return getX();
            case View.Y_Prop: return getY();
            case View.Width_Prop: return getWidth();
            case View.Height_Prop: return getHeight();
            case View.Rotate_Prop: return getRotate();
            case View.ScaleX_Prop: return getScaleX();
            case View.ScaleY_Prop: return getScaleY();
            case View.TransX_Prop: return getTransX();
            case View.TransY_Prop: return getTransY();
            case View.PrefWidth_Prop: return getPrefWidth();
            case View.PrefHeight_Prop: return getPrefHeight();
            case View.Fill_Prop: return getFill();
            case View.Opacity_Prop: return getOpacity();
            case View.Text_Prop: return getText();
            case "Enabled": return isEnabled();
            case Selectable.Items_Prop: return ((Selectable)this).getItems();
            case Selectable.SelItem_Prop: return ((Selectable)this).getSelItem();
            case Selectable.SelIndex_Prop: return ((Selectable)this).getSelIndex();
            default: break;
        }

        // Use key chain evaluator to get value
        return KeyChain.getValue(this, pname);
    }

    /**
     * Sets the value for given prop name.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        // Map property name
        String pname = aPropName.equals("Value") ? getValuePropName() : aPropName;

        // Handle properties
        switch(pname) {
            case View.X_Prop: setX(SnapUtils.doubleValue(aValue)); break;
            case View.Y_Prop: setY(SnapUtils.doubleValue(aValue)); break;
            case View.Width_Prop: setWidth(SnapUtils.doubleValue(aValue)); break;
            case View.Height_Prop: setHeight(SnapUtils.doubleValue(aValue)); break;
            case View.Rotate_Prop: setRotate(SnapUtils.doubleValue(aValue)); break;
            case View.ScaleX_Prop: setScaleX(SnapUtils.doubleValue(aValue)); break;
            case View.ScaleY_Prop: setScaleY(SnapUtils.doubleValue(aValue)); break;
            case View.TransX_Prop: setTransX(SnapUtils.doubleValue(aValue)); break;
            case View.TransY_Prop: setTransY(SnapUtils.doubleValue(aValue)); break;
            case View.PrefWidth_Prop: setPrefWidth(SnapUtils.doubleValue(aValue)); break;
            case View.PrefHeight_Prop: setPrefHeight(SnapUtils.doubleValue(aValue)); break;
            case View.Fill_Prop: setFill(aValue instanceof Paint ? (Paint)aValue : null); break;
            case View.Opacity_Prop: setOpacity(SnapUtils.doubleValue(aValue)); break;
            case View.Text_Prop: setText(SnapUtils.stringValue(aValue)); break;
            case "Enabled": setDisabled(!SnapUtils.boolValue(aValue)); break;
            case Selectable.Items_Prop: { Selectable sview = (Selectable)this;
                if (aValue instanceof List) sview.setItems((List)aValue);
                else if (aValue!=null && aValue.getClass().isArray()) sview.setItems((Object[])aValue);
                else sview.setItems(Collections.emptyList());
                break;
            }
            case Selectable.SelItem_Prop: ((Selectable)this).setSelItem(aValue); break;
            case Selectable.SelIndex_Prop: { Selectable sview = (Selectable)this;
                int index = aValue==null ? -1 : SnapUtils.intValue(aValue);
                sview.setSelIndex(index);
                break;
            }
            default: KeyChain.setValueSafe(this, pname, aValue);
        }
    }

    /**
     * Returns a mapped property name name.
     */
    protected String getValuePropName()  { return "Value"; }

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
        return _evtAdptr!=null ? _evtAdptr : (_evtAdptr=new EventAdapter());
    }

    /**
     * Sets an array of enabled events.
     */
    protected void enableEvents(ViewEvent.Type ... theTypes)
    {
        getEventAdapter().enableEvents(this, theTypes);
    }

    /**
     * Sets an array of enabled events.
     */
    protected void disableEvents(ViewEvent.Type ... theTypes)
    {
        getEventAdapter().disableEvents(this, theTypes);
    }

    /**
     * Adds an event filter.
     */
    public void addEventFilter(EventListener aLsnr, ViewEvent.Type ... theTypes)
    {
        getEventAdapter().addFilter(aLsnr, theTypes);
    }

    /**
     * Removes an event filter.
     */
    public void removeEventFilter(EventListener aLsnr, ViewEvent.Type ... theTypes)
    {
        getEventAdapter().removeFilter(aLsnr, theTypes);
    }

    /**
     * Adds an event handler.
     */
    public void addEventHandler(EventListener aLsnr, ViewEvent.Type ... theTypes)
    {
        getEventAdapter().addHandler(aLsnr, theTypes);
    }

    /**
     * Removes an event handler.
     */
    public void removeEventHandler(EventListener aLsnr, ViewEvent.Type ... theTypes)
    {
        getEventAdapter().removeHandler(aLsnr, theTypes);
    }

    /**
     * Fires the action event.
     * This should only be called in response to user input events (mouse, key) when a complete change has been made
     * to the primary value of a control view. Perhaps this method should even take an event to wrap in Action event.
     */
    protected void fireActionEvent(ViewEvent anEvent)
    {
        ViewEvent event = ViewEvent.createEvent(this, null, null, null);
        if (anEvent!=null) event.setParentEvent(anEvent);
        fireEvent(event);
    }

    /**
     * Sends an event to this view.
     */
    public void fireEvent(ViewEvent anEvent)  { processEventAll(anEvent); }

    /**
     * Sends an event to this view.
     */
    protected void processEventAll(ViewEvent anEvent)
    {
        // Forward to Filters
        processEventFilters(anEvent);

        // If event consumed, just return
        if (anEvent.isConsumed()) return;

        // Forward to Handlers from last to first
        processEventHandlers(anEvent);
    }

    /**
     * Process ViewEvent for View EventFilters.
     */
    protected void processEventFilters(ViewEvent anEvent)
    {
        // Forward to Filters, short-circuit if event is consumed
        EventListener filters[] = getEventAdapter()._filters;
        for (int i=0; i<filters.length; i++) { EventListener lsnr = filters[i];
            if (getEventAdapter()._types.get(lsnr).contains(anEvent.getType())) {
                lsnr.fireEvent(anEvent);
                if (anEvent.isConsumed()) break;
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

        // If event consumed, just return
        //if (anEvent.isConsumed()) return;

        // Forward to Handlers, short-circuit if event is consumed
        EventListener handlers[] = getEventAdapter()._handlers;
        for (int i=0; i<handlers.length; i++) { EventListener lsnr = handlers[i];
            if (getEventAdapter()._types.get(lsnr).contains(anEvent.getType()))
                lsnr.fireEvent(anEvent);
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
        if (aTime<0) return _anim;
        if (_anim==null) _anim = new ViewAnim(this, 0, 0);
        return aTime>0 ? _anim.getAnim(aTime) : _anim;
    }

    /**
     * Returns a cleared anim at given time.
     */
    public ViewAnim getAnimCleared(int aTime)  { return getAnim(0).clear().getAnim(aTime); }

    /**
     * Play animations deep.
     */
    public void playAnimDeep()  { ViewAnim.playDeep(this); }

    /**
     * Stop animations deep.
     */
    public void stopAnimDeep()  { ViewAnim.stopDeep(this); }

    /**
     * Returns the anim time.
     */
    public int getAnimTimeDeep()  { return ViewAnim.getTimeDeep(this); }

    /**
     * Sets the anim time deep.
     */
    public void setAnimTimeDeep(int aValue)  { ViewAnim.setTimeDeep(this, aValue); }

    /**
     * Returns the physics objects that provides information for physics simulations.
     */
    public ViewPhysics getPhysics()  { return _physics; }

    /**
     * Returns the physics objects that provides information for physics simulations.
     */
    public ViewPhysics getPhysics(boolean doCreate)
    {
        if (_physics!=null || !doCreate) return _physics;
        return _physics = new ViewPhysics();
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get class name for element
        String cname;
        for (Class c=getClass();;c=c.getSuperclass()) {
            if (c==ParentView.class) continue;
            if (c.getName().startsWith("snap.view")) {
                cname = c.getSimpleName(); break; }
        }

        // Get new element with class name
        XMLElement e = new XMLElement(cname);

        // Archive name
        if (getName()!=null && getName().length()>0) e.add(Name_Prop, getName());

        // Archive X, Y, Width, Height
        View par = getParent();
        if (this instanceof SpringView || par instanceof SpringView || par instanceof PageView) {
            if (getX()!=0) e.add("x", getX());
            if (getY()!=0) e.add("y", getY());
            if (getWidth()!=0) e.add("width", getWidth());
            if (getHeight()!=0) e.add("height", getHeight());
        }

        // Archive MinWidth, MinHeight, PrefWidth, PrefHeight
        if (isMinWidthSet()) e.add(MinWidth_Prop, getMinWidth());
        if (isMinHeightSet()) e.add(MinHeight_Prop, getMinHeight());
        if (isPrefWidthSet()) e.add(PrefWidth_Prop, getPrefWidth());
        if (isPrefHeightSet()) e.add(PrefHeight_Prop, getPrefHeight());

        // Archive Rotate, ScaleX, ScaleY
        if (getRotate()!=0) e.add(Rotate_Prop, getRotate());
        if (getScaleX()!=1) e.add(ScaleX_Prop, getScaleX());
        if (getScaleY()!=1) e.add(ScaleY_Prop, getScaleY());

        // Archive Vertical
        if (isVertical()!=getDefaultVertical()) e.add(Vertical_Prop, isVertical());

        // Archive border, Fill, Effect
        Paint fill = getFill(); Border brdr = getBorder(); Effect eff = getEffect();
        if (brdr!=null && !SnapUtils.equals(brdr,getDefaultBorder())) e.add(anArchiver.toXML(brdr, this));
        if (fill!=null && !SnapUtils.equals(fill,getDefaultFill())) e.add(anArchiver.toXML(fill, this));
        if (eff!=null) e.add(anArchiver.toXML(eff, this));

        // Archive font
        if (!SnapUtils.equals(getFont(),getDefaultFont())) e.add(getFont().toXML(anArchiver));

        // Archive Disabled, Visible, Opacity
        if (isDisabled()) e.add(Disabled_Prop, true);
        if (!isVisible()) e.add(Visible_Prop, false);
        if (getOpacity()<1) e.add(Opacity_Prop, getOpacity());

        // Archive Alignment, Margin, Padding
        if (getAlign()!=getDefaultAlign()) e.add(Align_Prop, getAlign());
        if (!getMargin().equals(getDefaultMargin())) e.add(Margin_Prop, getMargin().getString());
        if (!getPadding().equals(getDefaultPadding())) e.add(Padding_Prop, getPadding().getString());

        // Archive GrowWidth, GrowHeight, LeanX, LeanY
        if (isGrowWidth()) e.add(GrowWidth_Prop, true);
        if (isGrowHeight()) e.add(GrowHeight_Prop, true);
        if (getLeanX()!=null) e.add(LeanX_Prop, getLeanX());
        if (getLeanY()!=null) e.add(LeanY_Prop, getLeanY());

        // Archive animation
        if (getAnim(-1)!=null && !getAnim(0).isEmpty()) {
            ViewAnim anim = getAnim(0);
            e.add(anim.toXML(anArchiver));
        }

        // Archive bindings
        for (Binding b : getBindings())
            e.add(b.toXML(anArchiver));

        // Archive ToolTip
        if (getToolTip()!=null) e.add(ToolTip_Prop, getToolTip());

        // Archive RealClassName
        cname = getRealClassName();
        if (cname!=null && cname.length()>0) e.add("Class", cname);

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
            double x = anElement.getAttributeFloatValue("x"), y = anElement.getAttributeFloatValue("y");
            double w = anElement.getAttributeFloatValue("width"), h = anElement.getAttributeFloatValue("height");
            setBounds(x, y, w, h);
        }

        // Unarchive MinWidth, MinHeight, PrefWidth, PrefHeight
        if (anElement.hasAttribute(MinWidth_Prop)) setMinWidth(anElement.getAttributeFloatValue(MinWidth_Prop));
        if (anElement.hasAttribute(MinHeight_Prop)) setMinHeight(anElement.getAttributeFloatValue(MinHeight_Prop));
        if (anElement.hasAttribute(PrefWidth_Prop)) setPrefWidth(anElement.getAttributeFloatValue(PrefWidth_Prop));
        if (anElement.hasAttribute(PrefHeight_Prop)) setPrefHeight(anElement.getAttributeFloatValue(PrefHeight_Prop));

        // Unarchive Roll, ScaleX, ScaleY
        if (anElement.hasAttribute("roll")) setRotate(anElement.getAttributeFloatValue("roll"));
        if (anElement.hasAttribute(Rotate_Prop)) setRotate(anElement.getAttributeFloatValue(Rotate_Prop));
        if (anElement.hasAttribute(ScaleX_Prop)) setScaleX(anElement.getAttributeFloatValue(ScaleX_Prop));
        if (anElement.hasAttribute(ScaleY_Prop)) setScaleY(anElement.getAttributeFloatValue(ScaleY_Prop));

        // Unarchive Vertical
        if (anElement.hasAttribute(Vertical_Prop)) setVertical(anElement.getAttributeBoolValue(Vertical_Prop));

        // Unarchive Border
        int bind = anArchiver.indexOf(anElement, Border.class);
        if (bind>=0) {
            Border border = (Border)anArchiver.fromXML(anElement.get(bind), this);
            setBorder(border);
        }

        // Unarchive Fill
        int pind = anArchiver.indexOf(anElement, Paint.class);
        if (pind>=0) {
            Paint fill = (Paint)anArchiver.fromXML(anElement.get(pind), this);
            setFill(fill);
        }

        // Unarchive Effect
        int eind = anArchiver.indexOf(anElement, Effect.class);
        if (eind>=0) {
            Effect eff = (Effect)anArchiver.fromXML(anElement.get(eind), this);
            setEffect(eff);
        }

        // Unarchive Fill, Border (Legacy)
        XMLElement sxml = anElement.getElement("stroke");
        if (sxml!=null) { String cstr = sxml.getAttributeValue("color");
            Color sc = cstr!=null ? new Color(cstr):Color.BLACK;
            double sw = sxml.getAttributeFloatValue("width", 1);
            setBorder(sc, sw);
        }
        XMLElement fxml = anElement.getElement("fill");
        if (fxml!=null) {
            Paint fill = (Paint)anArchiver.fromXML(fxml, this);
            setFill(fill);
        }
        XMLElement bxml = anElement.getElement("border");
        if (bxml!=null) {
            Border border = Border.fromXMLBorder(anArchiver, bxml);
            setBorder(border);
        }

        // Unarchive font
        XMLElement fontXML = anElement.getElement(Font_Prop);
        if (fontXML!=null) setFont((Font)anArchiver.fromXML(fontXML, this));

        // Unarchive Disabled, Visible, Opacity
        if (anElement.hasAttribute(Disabled_Prop)) setDisabled(anElement.getAttributeBoolValue(Disabled_Prop));
        if (anElement.hasAttribute(Visible_Prop)) setVisible(anElement.getAttributeBoolValue(Visible_Prop));
        if (anElement.hasAttribute(Opacity_Prop)) setOpacity(anElement.getAttributeFloatValue(Opacity_Prop));

        // Unarchive Alignment, Margin, Padding
        else if (anElement.hasAttribute(Align_Prop)) setAlign(Pos.get(anElement.getAttributeValue(Align_Prop)));
        if (anElement.hasAttribute(Margin_Prop)) { Insets ins = Insets.get(anElement.getAttributeValue(Margin_Prop));
            setMargin(ins); }
        if (anElement.hasAttribute(Padding_Prop)) { Insets ins = Insets.get(anElement.getAttributeValue(Padding_Prop));
            setPadding(ins); }

        // Unarchive GrowWidth, GrowHeight, LeanX, LeanY
        if (anElement.hasAttribute(GrowWidth_Prop)) setGrowWidth(anElement.getAttributeBoolValue(GrowWidth_Prop));
        if (anElement.hasAttribute(GrowHeight_Prop)) setGrowHeight(anElement.getAttributeBoolValue(GrowHeight_Prop));
        if (anElement.hasAttribute(LeanX_Prop)) setLeanX(HPos.get(anElement.getAttributeValue(LeanX_Prop)));
        if (anElement.hasAttribute(LeanY_Prop)) setLeanY(VPos.get(anElement.getAttributeValue(LeanY_Prop)));

        // Unarchive Autosizing
        if (anElement.hasAttribute("asize")) setAutosizing(anElement.getAttributeValue("asize"));

        // Unarchive animation
        XMLElement animXML = anElement.getElement("Anim");
        if (animXML==null) animXML = anElement.getElement("KeyFrame")!=null ? anElement : null;
        if (animXML!=null)
            getAnim(0).fromXML(anArchiver, animXML);

        // Unarchive bindings
        for (int i=anElement.indexOf("binding");i>=0;i=anElement.indexOf("binding",i+1)) {
            XMLElement bx = anElement.get(i);
            addBinding(new Binding().fromXML(anArchiver, bx));
        }

        // Unarchive ToolTip
        if (anElement.hasAttribute("ttip")) setToolTip(anElement.getAttributeValue("ttip"));
        if (anElement.hasAttribute(ToolTip_Prop)) setToolTip(anElement.getAttributeValue(ToolTip_Prop));

        // Unarchive class property for subclass substitution, if available
        if (anElement.hasAttribute("Class"))
            setRealClassName(anElement.getAttributeValue("Class"));

        // Return this shape
        return this;
    }

    /**
     * Standard clone implementation.
     */
    public View copyUsingArchiver()
    {
        return new ViewArchiver().copy(this);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        StringBuffer sb = StringUtils.toString(this);
        if (getName()!=null && getName().length()>0) StringUtils.toStringAdd(sb, "Name", getName());
        if (getText()!=null && getText().length()>0) StringUtils.toStringAdd(sb, "Text", getText());
        StringUtils.toStringAdd(sb, "Bounds", getBounds());
        return sb.toString();
    }
}