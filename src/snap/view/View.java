/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A custom class.
 */
public class View implements XMLArchiver.Archivable {

    // The name of this view
    String          _name;
    
    // The view location
    double          _x, _y;
    
    // The view size
    double          _width, _height;
    
    // The view translation from x and y
    double          _tx, _ty;
    
    // The view rotation
    double          _rot;
    
    // The view scale from x and y
    double          _sx = 1, _sy = 1;
    
    // The alignment of content in this view
    Pos             _align = getDefaultAlign();
    
    // The padding for content in this view
    Insets          _padding = getDefaultPadding();
    
    // The horizontal position this view would prefer to take when inside a pane
    HPos            _leanX;
    
    // The vertical position this view would prefer to take when inside a pane
    VPos            _leanY;
    
    // Whether this view would like to grow horizontally/vertically if possible when inside a pane
    boolean         _growWidth, _growHeight;
    
    // Whether this view has a vertical orientation.
    boolean         _vertical;
    
    // The view minimum width and height
    double          _minWidth = -1, _minHeight = -1;
    
    // The view maximum width and height
    double          _maxWidth = -1, _maxHeight = -1;
    
    // The view preferred width and height
    double          _prefWidth = -1, _prefHeight = -1;
    
    // Whether view is disabled
    boolean         _disabled;
    
    // Whether view is currently the RootView.FocusedView
    boolean         _focused;
    
    // Whether view can receive focus
    boolean         _focusable;
    
    // Whether view should request focus when pressed
    boolean         _focusWhenPrsd;

    // Whether view focus should change when traveral key is pressed (Tab)
    boolean         _focusKeysEnbld = true;
 
    // Whether view is visible
    boolean         _visible = true;
    
    // Whether view is visible and has parent that is showing
    boolean         _showing;
 
    // Whether view can be hit by mouse
    boolean         _pickable = true;
    
    // Whether view should be included in layout
    boolean         _managed = true;
    
    // The view fill
    Paint           _fill;
    
    // The view border
    Border          _border;
    
    // The view effect
    Effect          _effect;
    
    // The opacity
    double          _opacity = 1;
    
    // The view font
    Font            _font;
    
    // The view cursor
    Cursor          _cursor = Cursor.DEFAULT;
    
    // The tooltip
    String          _ttip;
    
    // The clip (if set)
    Shape           _clip;
    
    // Bindings for this view
    List <Binding>  _bindings = Collections.EMPTY_LIST;
    
    // Client properties
    Map             _props = Collections.EMPTY_MAP;
    
    // The parent of this view
    ParentView      _parent;
    
    // The real class name, if shape component is really a custom subclass
    String          _realClassName;
    
    // PropertyChangeSupport
    PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // The event adapter
    EventAdapter    _evtAdptr;
    
    // Provides animation for View
    ViewAnim        _anim;
    
    // The view owner of this view
    ViewOwner       _owner;
    
    // The helper for this view when dealing with native
    ViewHelper      _helper;
    
    // The view environment
    static final ViewEnv  _env = ViewEnv.getEnv();

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
    public static final String Padding_Prop = "Padding";
    public static final String Parent_Prop = "Parent";
    public static final String Showing_Prop = "Showing";
    public static final String Text_Prop = "Text";
    public static final String ToolTip_Prop = "ToolTip";
    public static final String ItemKey_Prop = "ItemKey";
    
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
    if(SnapUtils.equals(aName, _name)) return;
    firePropChange(Name_Prop, _name, _name=StringUtils.min(aName));
}

/**
 * Returns the X location of the view.
 */
public double getX()  { return _x; } //_width<0? _x + _width : _x; }

/**
 * Sets the X location of the view.
 */
public void setX(double aValue)
{
    if(aValue==_x) return;
    repaintInParent(null);
    firePropChange(X_Prop, _x, _x=aValue);
    repaintInParent(null);
}

/**
 * Returns the Y location of the view.
 */
public double getY()  { return _y; } //_height<0? _y + _height : _y; }

/**
 * Sets the Y location of the view.
 */
public void setY(double aValue)
{
    if(aValue==_y) return;
    repaintInParent(null);
    firePropChange(Y_Prop, _y, _y=aValue);
    repaintInParent(null);
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
    double old = _width; if(aValue==old) return;
    firePropChange(Width_Prop, old, _width=aValue);
    relayout();
    repaintInParent(new Rect(0,0,Math.max(old,aValue),getHeight()));
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
    double old = _height; if(aValue==old) return;
    firePropChange(Height_Prop, old, _height=aValue);
    relayout();
    repaintInParent(new Rect(0,0,getWidth(),Math.max(old,aValue)));
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
public void setXY(double aX, double aY)  { setX(aX); setY(aY); }

/**
 * Returns the view size.
 */
public Size getSize()  { return new Size(getWidth(), getHeight()); }

/**
 * Sets the size.
 */
public void setSize(Size aSize)  { setSize(aSize.getWidth(), aSize.getHeight()); }

/**
 * Sets the size.
 */
public void setSize(double aW, double aH)  { setWidth(aW); setHeight(aH); }

/**
 * Returns the bounds.
 */
public Rect getBounds()  { return new Rect(getX(),getY(),getWidth(),getHeight()); }

/**
 * Sets the bounds.
 */
public void setBounds(Rect aRect)  { setBounds(aRect.x, aRect.y, aRect.width, aRect.height); }

/**
 * Sets the bounds.
 */
public void setBounds(double aX, double aY, double aW, double aH)  { setX(aX); setY(aY); setWidth(aW); setHeight(aH); }

/**
 * Returns the bounds inside view.
 */
public Rect getBoundsInside()  { return new Rect(0,0,getWidth(),getHeight()); }

/**
 * Sets the bounds view such that it maintains it's location in parent.
 */
public void setBoundsLocal(double aX, double aY, double aW, double aH)
{
    setXYLocal(aX, aY);
    setSizeLocal(aW, aH);
}

/**
 * Sets the size view such that it maintains it's location in parent.
 */
public void setXYLocal(double aX, double aY)
{
    if(isLocalToParentSimple()) { setXY(getX() + aX, getY() + aY); return; }
    Point p0 = localToParent(0,0);
    Point p1 = localToParent(aX,aY);
    double p2x = p1.x - p0.x, p2y = p1.y - p0.y;
    setXY(Math.round(getX() + p2x), Math.round(getY() + p2y));
}

/**
 * Sets the size view such that it maintains it's location in parent.
 */
public void setSizeLocal(double aW, double aH)
{
    if(isLocalToParentSimple()) { setSize(aW, aH); return; }
    Point p0 = localToParent(0,0);
    setSize(aW,aH);
    Point p1 = localToParent(0,0);
    double p2x = p1.x - p0.x, p2y = p1.y - p0.y;
    setXY(Math.round(getX() - p2x), Math.round(getY() - p2y));
}

/**
 * Returns the bounds inside view.
 */
public Shape getBoundsShape()  { return getBoundsInside(); }

/**
 * Returns the translation of this view from X.
 */
public double getTransX()  { return _tx; }

/**
 * Sets the translation of this view from X.
 */
public void setTransX(double aValue)
{
    if(aValue==_tx) return;
    repaintInParent(null);
    firePropChange(TransX_Prop, _tx, _tx=aValue);
    repaintInParent(null);
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
    if(aValue==_ty) return;
    repaintInParent(null);
    firePropChange(TransY_Prop, _ty, _ty=aValue);
    repaintInParent(null);
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
    if(theDegrees==_rot) return;
    repaintInParent(null);
    firePropChange(Rotate_Prop, _rot, _rot=theDegrees);
    repaintInParent(null);
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
    if(aValue==_sx) return;
    repaintInParent(null);
    firePropChange(ScaleX_Prop, _sx, _sx=aValue);
    repaintInParent(null);
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
    if(aValue==_sy) return;
    repaintInParent(null);
    firePropChange(ScaleY_Prop, _sy, _sy=aValue);
    repaintInParent(null);
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
    if(SnapUtils.equals(aPaint,getFill())) return;
    firePropChange(Fill_Prop, _fill, _fill=aPaint);
    repaint();
}

/**
 * Returns the fill as color.
 */
public Color getFillColor()
{
    Paint fill = getFill(); if(fill==null || fill instanceof Color) return (Color)fill;
    if(fill instanceof GradientPaint) return ((GradientPaint)fill).getStopColor(0); 
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
    if(SnapUtils.equals(aBorder,getBorder())) return;
    firePropChange(Border_Prop, _border, _border=aBorder);
    relayout(); relayoutParent(); repaint();
}

/**
 * Convenience to set border to given color and line width.
 */
public void setBorder(Color aColor, double aWidth)
{
    setBorder(aColor!=null? Border.createLineBorder(aColor, aWidth) : null);
}

/**
 * Returns effect.
 */
public Effect getEffect()  { return _effect; }

/**
 * Sets paint.
 */
public void setEffect(Effect anEff)
{
    if(SnapUtils.equals(anEff,_effect)) return;
    firePropChange(Effect_Prop, _effect, _effect=anEff);
    repaint();
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
    if(aValue==_opacity) return;
    firePropChange(Opacity_Prop, _opacity, _opacity=aValue);
    repaint();
}

/**
 * Returns the combined opacity of this view and it's parents.
 */
public double getOpacityAll()
{
    double opacity = getOpacity(); ParentView par = getParent();
    return par!=null? opacity*par.getOpacityAll() : opacity;
}

/**
 * Returns the font for the view (defaults to parent font).
 */
public Font getFont()  { return _font!=null? _font : getDefaultFont(); }

/**
 * Sets the font for the view.
 */
public void setFont(Font aFont)
{
    if(aFont==_font) return;
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
    if(aCursor==null) aCursor = Cursor.DEFAULT; if(aCursor==_cursor) return;
    firePropChange(Cursor_Prop, _cursor, _cursor=aCursor);
    RootView rview = getRootView(); if(rview!=null) rview.setCurrentCursor(_cursor);
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
    if(aValue==_disabled) return;
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
 * Returns the clip shape.
 */
public Shape getClip()  { return _clip; }

/**
 * Sets the clip shape.
 */
public void setClip(Shape aShape)
{
    if(SnapUtils.equals(aShape,_clip)) return;
    firePropChange(Clip_Prop, _clip, _clip = aShape);
}

/**
 * Returns the clip bounds.
 */
public Rect getClipBounds()  { Shape clip = getClip(); return clip!=null? clip.getBounds() : null; }

/**
 * Returns the clip of this view due to all parents.
 */
public Shape getClipAll()
{
    Shape vshp = getParent()!=null? getParent().getClipAll() : null;
    if(vshp!=null) vshp = parentToLocal(vshp);
    if(getClip()!=null) vshp = vshp!=null? Shape.intersect(vshp, getClip()) : getClip();
    return vshp;
}

/**
 * Returns the clip bounds due to all parents.
 */
public Rect getClipBoundsAll()  { Shape clip = getClipAll(); return clip!=null? clip.getBounds() : null; }

/**
 * Returns the visible bounds for a view based on first ancestor clip (or null if no clipping found).
 */
public Rect getVisRect()
{
    if(!isVisible()) return new Rect(0,0,0,0);
    Rect cbnds = getClipBoundsAll();
    Rect bnds = getBoundsInside(); if(cbnds==null) return bnds;
    double x = Math.floor(Math.max(cbnds.getX(),bnds.getX()));
    double y = Math.floor(Math.max(cbnds.getY(),bnds.getY()));
    double w = Math.ceil(Math.min(cbnds.getMaxX(),bnds.getMaxX())) - x;
    double h = Math.ceil(Math.min(cbnds.getMaxY(),bnds.getMaxY())) - y;
    if(w<=0 || h<=0) { x = bnds.getMinX(); y = bnds.getMinY(); w = 0; h = 0; }
    return new Rect(x,y,w,h);
}

/**
 * Sets the visible rect.
 */
public void setVisRect(Rect aRect)  { scrollToVisible(aRect); }

/**
 * Called to scroll the given shape in this view coords to visible.
 */
public void scrollToVisible(Shape aShape)  { if(getParent()!=null) getParent().scrollToVisible(localToParent(aShape)); }

/**
 * Returns whether transform to parent is simple (contains no rotate, scale, skew).
 */
public boolean isLocalToParentSimple()  { return _rot==0 && _sx==1 && _sy==1; }

/**
 * Returns the transform.
 */
public Transform getLocalToParent()
{
    if(isLocalToParentSimple()) return Transform.getTrans(getX()+_tx, getY()+_ty);
    
    // Get location, size, point of rotation, rotation, scale, skew
    double x = getX() + getTransX(), y = getY() + getTransY(), w = getWidth(), h = getHeight(), prx = w/2, pry = h/2;
    double rot = getRotate(), sx = getScaleX(), sy = getScaleY(); //skx = getSkewX(), sky = getSkewY();
    
    // Transform about point of rotation and return
    Transform t = Transform.getTrans(-prx, -pry); //if(skx!=0 || sky!=0) t.skew(skx, sky);
    if(sx!=1 || sy!=1) t.scale(sx, sy); if(rot!=0) t.rotate(rot);
    t.translate(prx + x, pry + y); return t;
}

/**
 * Returns the transform.
 */
public Transform getLocalToParent(View aPar)
{
    Transform tfm = getLocalToParent();
    for(View n=getParent();n!=aPar&&n!=null;n=n.getParent()) {
        if(n.isLocalToParentSimple()) tfm.translate(n._x+n._tx,n._y+n._ty);
        else tfm.multiply(n.getLocalToParent());
    }
    return tfm;
}

/**
 * Converts a point from local to parent.
 */
public Point localToParent(double aX, double aY)
{
    if(isLocalToParentSimple()) return new Point(aX+_x+_tx,aY+_y+_ty);
    return getLocalToParent().transform(aX, aY);
}

/**
 * Converts a point from local to given parent.
 */
public Point localToParent(View aPar, double aX, double aY)
{
    Point point = new Point(aX,aY);
    for(View n=this;n!=aPar&&n!=null;n=n.getParent()) {
        if(n.isLocalToParentSimple()) point.offset(n._x+n._tx,n._y+n._ty);
        else point = n.localToParent(point.x,point.y); }
    return point;
}

/**
 * Converts a shape from local to parent.
 */
public Shape localToParent(Shape aShape)  { return aShape.copyFor(getLocalToParent()); }

/**
 * Converts a point from local to given parent.
 */
public Shape localToParent(View aPar, Shape aShape)  { return aShape.copyFor(getLocalToParent(aPar)); }

/**
 * Returns the transform from parent to local coords.
 */
public Transform getParentToLocal()
{
    if(isLocalToParentSimple()) return Transform.getTrans(-_x-_tx, -_y-_ty);
    Transform tfm = getLocalToParent(); tfm.invert(); return tfm;
}

/**
 * Returns the transform from parent to local coords.
 */
public Transform getParentToLocal(View aPar)  { Transform tfm = getLocalToParent(aPar); tfm.invert(); return tfm; }

/**
 * Converts a point from parent to local.
 */
public Point parentToLocal(double aX, double aY)
{
    if(isLocalToParentSimple()) return new Point(aX-_x-_tx,aY-_y-_ty);
    return getParentToLocal().transform(aX, aY);
}

/**
 * Converts a point from given parent to local.
 */
public Point parentToLocal(View aPar, double aX, double aY)  { return getParentToLocal(aPar).transform(aX,aY); }

/**
 * Converts a shape from parent to local.
 */
public Shape parentToLocal(Shape aShape)  { return aShape.copyFor(getParentToLocal()); }

/**
 * Converts a shape from parent to local.
 */
public Shape parentToLocal(View aView, Shape aShape)  { return aShape.copyFor(getParentToLocal(aView)); }

/**
 * Returns the transform from local to screen.
 */
public Transform getLocalToScreen()
{
    WindowView win = getWindow();
    Transform xfm = getLocalToParent(win);
    if(win!=null)
        xfm.multiply(Transform.getTrans(win.getX(),win.getY()));
    return xfm;
}

/**
 * Converts a point from local to parent.
 */
public Point localToScreen(double aX, double aY)
{
    WindowView win = getWindow();
    Point pnt = localToParent(win, aX,aY);
    if(win!=null) { pnt.x += win.getX(); pnt.y += win.getY(); }
    return pnt;
}

/**
 * Returns the transform from screen to local.
 */
public Transform getScreenToLocal()  { Transform t = getLocalToScreen(); t.invert(); return t; }

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
    //if(!(0<=aX && aX<=getWidth() && 0<=aY && aY<=getHeight())) return false;
    //if(isPickBounds()) return getBoundsInside().contains(aX, aY);
    //return getBoundsShape().contains(aX, aY);
}

/**
 * Returns whether view contains shape.
 */
public boolean contains(Shape aShape)
{
    //if(isPickBounds()) return getBoundsInside().contains(aShape,1);
    return getBoundsShape().contains(aShape);
}

/**
 * Returns whether view intersects shape.
 */
public boolean intersects(Shape aShape)
{
    //if(isPickBounds()) return getBoundsInside().intersects(aShape,1);
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
    firePropChange(Parent_Prop, _parent, _parent=aPar);
    setShowing(aPar!=null && aPar.isShowing());
}

/**
 * Returns the first parent with given class by iterating up parent hierarchy.
 */
public <T extends View> T getParent(Class<T> aClass)
{
    for(ParentView s=getParent(); s!=null; s=s.getParent()) if(aClass.isInstance(s)) return (T)s;
    return null; // Return null since parent of class wasn't found
}

/**
 * Returns the number of ancestors of this view.
 */
public int getParentCount()  { int pc = 0; for(View n=getParent();n!=null;n=n.getParent()) pc++; return pc; }

/**
 * Returns whether given view is an ancestor of this view.
 */
public boolean isAncestor(View aView)
{
    for(View n=getParent();n!=null;n=n.getParent()) if(n==aView) return true; return false;
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
    if(aValue==_visible) return;
    firePropChange(Visible_Prop, _visible, _visible=aValue);
    setShowing(_visible && _parent!=null && _parent.isShowing());
    repaintInParent(null);
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
    if(aValue==_showing) return;
    if(aValue) repaint();
    firePropChange(Showing_Prop, _showing, _showing=aValue);
    
    // If focused, turn off
    if(!aValue && isFocused())
        setFocused(false);
    
    // If Anim set, play/suspend
    ViewAnim anim = getAnim(-1);
    if(aValue && anim!=null  && anim.isSuspended()) anim.play();
    else if(!aValue && anim!=null && anim.isPlaying()) anim.suspend();
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
 * Returns whether view should be handled when a parent does layout.
 */
public boolean isManaged()  { return _managed; }

/**
 * Sets whether view should be laid out.
 */
public void setManaged(boolean aValue)
{
    _managed = aValue;
    ParentView par = getParent(); if(par!=null) { par._managed = null; relayoutParent(); }
}

/**
 * Returns the root view.
 */
public RootView getRootView()  { return _parent!=null? _parent.getRootView() : null; }

/**
 * Returns the window.
 */
public WindowView getWindow()  { ParentView par = getParent(); return par!=null? par.getWindow() : null; }

/**
 * Returns the position this view would prefer to take when inside a pane.
 */
public Pos getLean()  { return Pos.get(_leanX, _leanY); } 

/**
 * Sets the lean this view would prefer to take when inside a pane.
 */
public void setLean(Pos aPos)
{
    setLeanX(aPos!=null? aPos.getHPos() : null);
    setLeanY(aPos!=null? aPos.getVPos() : null);
}

/**
 * Returns the horizontal position this view would prefer to take when inside a pane.
 */
public HPos getLeanX()  { return _leanX; } 

/**
 * Sets the horizontal lean this view would prefer to take when inside a pane.
 */
public void setLeanX(HPos aPos)  { _leanX = aPos; }

/**
 * Returns the vertical position this view would prefer to take when inside a pane.
 */
public VPos getLeanY()  { return _leanY; } 

/**
 * Sets the vertical position this view would prefer to take when inside a pane.
 */
public void setLeanY(VPos aPos)  { _leanY = aPos; }

/**
 * Returns whether this view would like to grow horizontally if possible when inside a pane.
 */
public boolean isGrowWidth()  { return _growWidth; }

/**
 * Sets whether this view would like to grow horizontally if possible when inside a pane.
 */
public void setGrowWidth(boolean aValue)  { _growWidth = aValue; }

/**
 * Returns whether this view would like to grow vertically if possible when inside a pane.
 */
public boolean isGrowHeight()  { return _growHeight; }

/**
 * Sets whether this view would like to grow vertically if possible when inside a pane.
 */
public void setGrowHeight(boolean aValue)  { _growHeight = aValue; }

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
public void setVertical(boolean aValue)  { _vertical = aValue; }

/**
 * Returns the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
 */
public String getAutosizing()  //{ return _asize!=null && _asize.length()>6? _asize : getAutosizingDefault(); }
{
    HPos lx = getLeanX(); boolean left = lx==null || lx==HPos.LEFT, right = lx!=null && lx==HPos.RIGHT;
    VPos ly = getLeanY(); boolean top = ly==null || ly==VPos.TOP, btm = ly!=null && ly==VPos.BOTTOM;
    char c1 = left? '-' : '~', c2 = isGrowWidth()? '~' : '-', c3 = right? '-' : '~';
    char c4 = top? '-' : '~', c5 = isGrowHeight()? '~' : '-', c6 = btm? '-' : '~';
    return "" + c1 + c2 + c3 + ',' + c4 + c5 + c6;
}

/**
 * Sets the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
 */
public void setAutosizing(String aVal)
{
    String val = aVal!=null? aVal : "--~,--~"; if(val.length()<7) return;
    char c1 = val.charAt(0), c2 = val.charAt(1), c3 = val.charAt(2);
    char c4 = val.charAt(4), c5 = val.charAt(5), c6 = val.charAt(6);
    setGrowWidth(c2=='~'); setGrowHeight(c5=='~');
    setLeanX(c1=='~' && c3=='~'? HPos.CENTER : c1=='~'? HPos.RIGHT : null);
    setLeanY(c4=='~' && c6=='~'? VPos.CENTER : c4=='~'? VPos.BOTTOM : null);
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
    if(_bindings==Collections.EMPTY_LIST) _bindings = new ArrayList();
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
    if(index>=0) removeBinding(index);
    return index>=0;
}

/**
 * Returns the individual binding with the given property name.
 */
public Binding getBinding(String aPropName)
{
    // Iterate over bindings and if we find one for given property name, return it
    for(Binding b : _bindings)
        if(b.getPropertyName().equals(aPropName))
            return b;
    
    // If property name is mapped, try again
    String mappedName = aPropName.equals("Value")? getValuePropName() : aPropName;
    if(!aPropName.equals(mappedName))
        return getBinding(mappedName);
    
    // Return null since binding with property name not found
    return null;
}

/**
 * Removes the binding with given property name.
 */
public boolean removeBinding(String aPropName)
{
    for(int i=0, iMax=getBindingCount(); i<iMax; i++) { Binding binding = getBinding(i);
        if(binding.getPropertyName().equals(aPropName)) {
            removeBinding(i); return true; }}
    return false;
}

/**
 * Adds a binding for given name and key.
 */
public void addBinding(String aPropName, String aKey)
{
    String pname = aPropName.equals("Value")? getValuePropName() : aPropName;
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
    if(_props==Collections.EMPTY_MAP) _props = new HashMap();
    Object val = _props.put(aName, aValue);
    firePropChange(aName, val, aValue);
    return val;
}

/**
 * Returns whether view minimum width is set.
 */
public boolean isMinWidthSet()  { return _minWidth>=0; }

/**
 * Returns the view minimum width.
 */
public double getMinWidth()  { return _minWidth>=0? _minWidth : getMinWidthImpl(); }

/**
 * Sets the view minimum width.
 */
public void setMinWidth(double aWidth)
{
    if(aWidth==_minWidth) return;
    firePropChange(MinWidth_Prop, _minWidth, _minWidth = aWidth);
}

/**
 * Returns whether view minimum height is set.
 */
public boolean isMinHeightSet()  { return _minHeight>=0; }

/**
 * Returns the view minimum height.
 */
public double getMinHeight()  { return _minHeight>=0? _minHeight : getMinHeightImpl(); }

/**
 * Sets the view minimum height.
 */
public void setMinHeight(double aHeight)
{
    if(aHeight==_minHeight) return;
    firePropChange(MinHeight_Prop, _minHeight, _minHeight = aHeight);
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
public double getMaxWidth()  { return _maxWidth>=0? _maxWidth : Float.MAX_VALUE; }

/**
 * Sets the view maximum width.
 */
public void setMaxWidth(double aWidth)
{
    if(aWidth==_maxWidth) return;
    firePropChange(MaxWidth_Prop, _maxWidth, _maxWidth = aWidth);
}

/**
 * Returns whether view maximum height is set.
 */
public boolean isMaxHeightSet()  { return _maxHeight>=0; }

/**
 * Returns the view maximum height.
 */
public double getMaxHeight()  { return _maxHeight>=0? _maxHeight : Float.MAX_VALUE; }

/**
 * Sets the view maximum height.
 */
public void setMaxHeight(double aHeight)
{
    if(aHeight==_maxHeight) return;
    firePropChange(MaxHeight_Prop, _maxHeight, _maxHeight = aHeight);
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
public double getPrefWidth(double aH)  { return _prefWidth>=0? _prefWidth : getPrefWidthImpl(aH); }

/**
 * Sets the view preferred width.
 */
public void setPrefWidth(double aWidth)
{
    if(aWidth==_prefWidth) return;
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
public double getPrefHeight(double aW)  { return _prefHeight>=0? _prefHeight : getPrefHeightImpl(aW); }

/**
 * Sets the view preferred height.
 */
public void setPrefHeight(double aHeight)
{
    if(aHeight==_prefHeight) return;
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
public double getBestWidth(double aH)  { return MathUtils.clamp(getPrefWidth(), getMinWidth(), getMaxWidth()); }

/**
 * Returns the best height for view - accounting for pref/min/max.
 */
public double getBestHeight(double aW)  { return MathUtils.clamp(getPrefHeight(), getMinHeight(), getMaxHeight()); }

/**
 * Returns the best size.
 */
public Size getBestSize()
{
    double bw, bh;
    if(isHorizontal()) { bw = getBestWidth(-1); bh = getBestHeight(bw); }
    else { bh = getBestHeight(-1); bw = getBestWidth(bh); }
    return new Size(bw,bh);
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
    if(aPos==_align) return;
    firePropChange(Align_Prop, _align, _align=aPos);
    relayout();
}

/**
 * Returns the padding.
 */
public Insets getPadding()  { return _padding; }

/**
 * Sets padding.
 */
public void setPadding(double aTp, double aRt, double aBtm, double aLt)  { setPadding(new Insets(aTp,aRt,aBtm,aLt)); }

/**
 * Sets the padding.
 */
public void setPadding(Insets theIns)
{
    if(theIns==null) theIns = getDefaultPadding();
    if(SnapUtils.equals(theIns,_padding)) return;
    firePropChange(Padding_Prop, _padding, _padding = theIns);
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
public Font getDefaultFont()  { View p = getParent(); return p!=null? p.getFont() : Font.Arial11; }

/**
 * Returns the padding default.
 */
public Insets getDefaultPadding()  { return _emptyIns; }

/**
 * Returns the insets due to border and/or padding.
 */
public Insets getInsetsAll()
{
    Insets ins = getPadding();
    Border border = getBorder(); if(border!=null) ins = Insets.add(ins, border.getInsets());
    return ins;
}

/**
 * Main paint method.
 */
protected void paintAll(Painter aPntr)
{
    // Set opacity
    double opacity = getOpacityAll(), opacityOld = 0;
    if(opacity!=1) {
        opacityOld = aPntr.getOpacity(); aPntr.setOpacity(opacity); }

    // If view has effect, get/create effect painter to speed up successive paints
    if(getEffect()!=null) { Effect eff = getEffect();
        PainterDVR pdvr = new PainterDVR();
        paintBack(pdvr); paintFront(pdvr);
        if(_pdvr1==null || !pdvr.equals(_pdvr1)) {
            _pdvr1 = pdvr; _pdvr2 = new PainterDVR();
            eff.applyEffect(pdvr, _pdvr2, getBoundsInside());
        }
        _pdvr2.exec(aPntr);
    }
    
    // Otherwise, do normal draw
    else { paintBack(aPntr); paintFront(aPntr); }
    
    // Restore opacity
    if(opacity!=1)
        aPntr.setOpacity(opacityOld);
}

// DVR painters for caching effect drawing
PainterDVR _pdvr1, _pdvr2;

/**
 * Paints background.
 */
protected void paintBack(Painter aPntr)
{
    Paint fill = getFill();
    Border border = getBorder(); if(fill==null && border==null) return;
    Shape shape = getBoundsShape();
    if(fill!=null) {
        aPntr.setPaint(fill); aPntr.fill(shape); }
    if(border!=null)
        border.paint(aPntr, shape);
}

/**
 * Paints foreground.
 */
protected void paintFront(Painter aPntr)  { }

/**
 * Returns the tool tip text.
 */
public String getToolTip()  { return _ttip; }

/**
 * Sets the tool tip text.
 */
public void setToolTip(String aString)
{
    if(SnapUtils.equals(aString,_ttip)) return;
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
 * Returns the ItemKey.
 */
public String getItemKey()  { return (String)getProp("ItemKey"); }

/**
 * Sets the ItemKey.
 */
public void setItemKey(String aKey)
{
    String old = getItemKey(); if(SnapUtils.equals(aKey,old)) return;
    setProp("ItemKey", aKey);
    firePropChange(ItemKey_Prop, old, aKey);
}

/**
 * Returns the substitution class name.
 */
public String getRealClassName()  { return _realClassName; }

/**
 * Sets the substitution class string.
 */
public void setRealClassName(String aName)
{
    if(SnapUtils.equals(aName,getRealClassName()) || getClass().getName().equals(aName)) return;
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
    ParentView par = getParent(); if(par==null) return;
    par.relayout(); par.relayoutParent();
}

/**
 * Called to register view for repaint.
 */
public void repaint()  { repaint(0,0,getWidth(),getHeight()); }

/**
 * Called to register view for repaint.
 */
public void repaint(Rect aRect)  { repaint(aRect.x,aRect.y,aRect.width,aRect.height); }

/**
 * Called to register view for repaint.
 */
public void repaint(double aX, double aY, double aW, double aH)
{
    RootView rview = getRootView(); if(rview==null) return;
    rview.repaint(this, aX, aY, aW, aH);
}

/**
 * Called to repaint in parent for cases where transform might change.
 */
protected void repaintInParent(Rect aRect)
{
    ParentView par = getParent(); if(par==null) return;
    Rect rect = localToParent(aRect!=null? aRect : getBoundsInside()).getBounds();
    par.repaint(rect);
}

/**
 * Returns whether this view is the RootView.FocusedView.
 */
public boolean isFocused()  { return _focused; }

/**
 * Sets whether this view is the RootView.FocusedView.
 */
protected void setFocused(boolean aValue)
{
    if(aValue==_focused) return;
    firePropChange(Focused_Prop, _focused, _focused=aValue);
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
    if(aValue==_focusable) return;
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
    if(aValue==_focusWhenPrsd) return;
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
 * Tells view to request focus.
 */
public void requestFocus()
{
    if(isFocused()) return;
    RootView rview = getRootView(); if(rview!=null) rview.requestFocus(this);
    repaint();
}

/**
 * Returns the next focus View.
 */
public View getFocusNext()  { return getParent()!=null? getParent().getFocusNext(this) : null; }

/**
 * Returns the next focus View.
 */
public View getFocusPrev()  { return getParent()!=null? getParent().getFocusPrev(this) : null; }

/**
 * Returns the view owner.
 */
public ViewOwner getOwner()  { return _owner; }

/**
 * Sets the owner.
 */
public void setOwner(ViewOwner anOwner)
{
    if(_owner!=null) return;
    _owner = anOwner;
    if(_evtAdptr!=null && _evtAdptr.isEnabled(Action)) anOwner.enableEvents(this, Action);
}

/**
 * Returns the owner of given class.
 */
public <T> T getOwner(Class <T> aClass)
{
    if(getOwner()!=null && aClass.isAssignableFrom(getOwner().getClass())) return (T)getOwner();
    return getParent()!=null? getParent().getOwner(aClass) : null;
}

/**
 * Returns the view helper.
 */
public ViewHelper getHelper()
{
    if(_helper!=null) return _helper;
    _helper = _env.createHelper(this);
    _helper.setView(this);
    return _helper;
}

/**
 * Returns the native version of view.
 */
public Object getNative()  { return getHelper().get(); }

/**
 * Returns the native version of view.
 */
public <T> T getNative(Class <T> aClass)  { return ClassUtils.getInstance(getNative(), aClass); }

/**
 * Returns the view environment.
 */
public ViewEnv getEnv()  { return _env; }

/**
 * Returns the value for given key.
 */
public Object getValue(String aPropName)
{
    // Map property name
    String pname = aPropName.equals("Value")? getValuePropName() : aPropName;

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
        case "Enabled": return isEnabled();
        case "Items": return ((Selectable)this).getItems();
        case "SelectedItem": return ((Selectable)this).getSelectedItem();
        case "SelectedIndex": return ((Selectable)this).getSelectedIndex();
        case Scroller.ScrollV_Prop: return ((Scroller)this).getScrollV();
        default: break;
    }
    
    // Use key chain evaluator to get value
    return GFXEnv.getEnv().getKeyChainValue(this, pname);
}

/**
 * Sets the value for given key.
 */
public void setValue(String aPropName, Object aValue)
{
    // Map property name
    String pname = aPropName.equals("Value")? getValuePropName() : aPropName;
    
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
        case "Enabled": setDisabled(!SnapUtils.boolValue(aValue)); break;
        case "Items": { Selectable sview = (Selectable)this;
            if(aValue instanceof List) sview.setItems((List)aValue);
            else if(aValue!=null && aValue.getClass().isArray()) sview.setItems((Object[])aValue);
            else sview.setItems(Collections.emptyList());
            break;
        }
        case "SelectedItem": ((Selectable)this).setSelectedItem(aValue); break;
        case "SelectedIndex": { Selectable sview = (Selectable)this;
            int index = aValue==null? -1 : SnapUtils.intValue(aValue);
            sview.setSelectedIndex(index);
            break;
        }
        case Scroller.ScrollV_Prop: ((Scroller)this).setScrollV(SnapUtils.doubleValue(aValue)); break;
        default: GFXEnv.getEnv().setKeyChainValue(this, pname, aValue);
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
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aPCL)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aPCL);
}

/**
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aPCL, String aProp)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aPCL, aProp);
}

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aPCL, String aProp)
{
    _pcs.removePropChangeListener(aPCL, aProp);
}

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal)
{
    if(!_pcs.hasListeners(aProp)) return;
    firePropChange(new PropChange(this, aProp, oldVal, newVal));
}

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
{
    if(!_pcs.hasListeners(aProp)) return;
    firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
}

/**
 * Fires a given property change.
 */
protected void firePropChange(PropChange aPC)  { _pcs.firePropChange(aPC); }

/**
 * Returns the event adapter for view.
 */
public EventAdapter getEventAdapter() { return _evtAdptr!=null? _evtAdptr : (_evtAdptr=new EventAdapter()); }

/**
 * Sets an array of enabled events.
 */
protected void enableEvents(ViewEvent.Type ... theTypes)  { getEventAdapter().enableEvents(this, theTypes); }

/**
 * Sets an array of enabled events.
 */
protected void disableEvents(ViewEvent.Type ... theTypes)  { getEventAdapter().disableEvents(this, theTypes); }

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
 */
public void fireActionEvent()
{
    ViewEvent event = getEnv().createEvent(this, null, null, null);
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
            
    // Forward to Handlers from last to first
    processEventHandlers(anEvent);
}

/**
 * Process ViewEvent for View EventFilters.
 */
protected void processEventFilters(ViewEvent anEvent)
{
    // Forward to Filters from last to first, short-circuit if event is consume
    EventListener filters[] = getEventAdapter()._filters;
    for(int i=filters.length-1; i>=0; i--) { EventListener lsnr = filters[i];
        if(getEventAdapter()._types.get(lsnr).contains(anEvent.getType())) {
            lsnr.fireEvent(anEvent); if(anEvent.isConsumed()) break; }}
}

/**
 * Process ViewEvent for View EventHandlers.
 */
protected void processEventHandlers(ViewEvent anEvent)
{
    // If event not consumed, send to view
    if(!anEvent.isConsumed())
        processEvent(anEvent);

    // Forward to Handlers from last to first
    EventListener handlers[] = getEventAdapter()._handlers;
    for(int i=handlers.length-1; i>=0; i--) { EventListener lsnr = handlers[i];
        if(getEventAdapter()._types.get(lsnr).contains(anEvent.getType()))
            lsnr.fireEvent(anEvent); }
}

/**
 * Process ViewEvent.
 */
protected void processEvent(ViewEvent anEvent)  { }

/**
 * Returns the anim for the given time.
 */
public ViewAnim getAnim(int aTime)
{
    if(aTime<0) return _anim;
    if(_anim==null) _anim = new ViewAnim(this, 0, 0);
    return aTime>0? _anim.getAnim(aTime) : _anim;
}

/**
 * Returns a cleared anim at given time.
 */
public ViewAnim getAnimCleared(int aTime)  { return getAnim(0).clear().getAnim(aTime); }

/**
 * Play animations deep.
 */
public void playAnimDeep()  { ViewAnim anim = getAnim(-1); if(anim!=null) anim.play(); }

/**
 * Stop animations deep.
 */
public void stopAnimDeep()  { ViewAnim anim = getAnim(-1); if(anim!=null) anim.stop(); }

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get class name for element
    String cname = null;
    for(Class c=getClass();;c=c.getSuperclass()) { if(c==ParentView.class) continue;
        if(c.getName().startsWith("snap.view")) { cname = c.getSimpleName(); break; }}

    // Get new element with class name
    XMLElement e = new XMLElement(cname);
    
    // Archive name
    if(getName()!=null && getName().length()>0) e.add(Name_Prop, getName());
    
    // Archive X, Y, Width, Height
    if(this instanceof SpringView || getParent() instanceof SpringView) {
        if(getX()!=0) e.add("x", getX()); if(getY()!=0) e.add("y", getY());
        if(getWidth()!=0) e.add("width", getWidth()); if(getHeight()!=0) e.add("height", getHeight());
    }
    
    // Archive MinWidth, MinHeight, PrefWidth, PrefHeight
    if(isMinWidthSet()) e.add(MinWidth_Prop, getMinWidth());
    if(isMinHeightSet()) e.add(MinHeight_Prop, getMinHeight());
    if(isPrefWidthSet()) e.add(PrefWidth_Prop, getPrefWidth());
    if(isPrefHeightSet()) e.add(PrefHeight_Prop, getPrefHeight());
    
    // Archive Rotate, ScaleX, ScaleY
    if(getRotate()!=0) e.add(Rotate_Prop, getRotate());
    if(getScaleX()!=1) e.add(ScaleX_Prop, getScaleX());
    if(getScaleY()!=1) e.add(ScaleY_Prop, getScaleY());

    // Archive Vertical
    if(isVertical()) e.add(Vertical_Prop, true);
    
    // Archive border, Fill, Effect
    if(!SnapUtils.equals(getBorder(),getDefaultBorder())) e.add(anArchiver.toXML(getBorder(), this));
    if(!SnapUtils.equals(getFill(),getDefaultFill())) e.add(anArchiver.toXML(getFill(), this));
    if(getEffect()!=null) e.add(anArchiver.toXML(getEffect(), this));
    
    // Archive font
    if(!SnapUtils.equals(getFont(),getDefaultFont())) e.add(getFont().toXML(anArchiver));
    
    // Archive Disabled, Visible, Opacity
    if(isDisabled()) e.add(Disabled_Prop, true);
    if(!isVisible()) e.add(Visible_Prop, false);
    if(getOpacity()<1) e.add(Opacity_Prop, getOpacity());
    
    // Archive Alignment, Padding
    if(getAlign()!=getDefaultAlign()) e.add(Align_Prop, getAlign());
    if(!getPadding().equals(getDefaultPadding())) e.add(Padding_Prop, getPadding().getString());
        
    // Archive GrowWidth, GrowHeight, LeanX, LeanY
    if(isGrowWidth()) e.add(GrowWidth_Prop, true);
    if(isGrowHeight()) e.add(GrowHeight_Prop, true);
    if(getLeanX()!=null) e.add(LeanX_Prop, getLeanX());
    if(getLeanY()!=null) e.add(LeanY_Prop, getLeanY());
        
    // Archive bindings
    for(Binding b : getBindings())
        e.add(b.toXML(anArchiver));

    // Archive ToolTip, ItemKey
    if(getToolTip()!=null) e.add(ToolTip_Prop, getToolTip());
    if(getItemKey()!=null) e.add(ItemKey_Prop, getItemKey());
    
    // Archive RealClassName
    cname = getRealClassName(); if(cname!=null && cname.length()>0) e.add("Class", cname);
    
    // Return the element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive class property for subclass substitution, if available
    if(anElement.hasAttribute("Class"))
        setRealClassName(anElement.getAttributeValue("Class"));

    // Unarchive Name
    if(anElement.hasAttribute(Name_Prop))
        setName(anElement.getAttributeValue(Name_Prop));
    
    // Unarchive X, Y, Width, Height
    if(anElement.hasAttribute("x") || anElement.hasAttribute("y") ||
        anElement.hasAttribute("width") || anElement.hasAttribute("height")) {
        double x = anElement.getAttributeFloatValue("x"), y = anElement.getAttributeFloatValue("y");
        double w = anElement.getAttributeFloatValue("width"), h = anElement.getAttributeFloatValue("height");
        setBounds(x, y, w, h);
    }
    
    // Unarchive MinWidth, MinHeight, PrefWidth, PrefHeight
    if(anElement.hasAttribute(MinWidth_Prop)) setMinWidth(anElement.getAttributeFloatValue(MinWidth_Prop));
    if(anElement.hasAttribute(MinHeight_Prop)) setMinHeight(anElement.getAttributeFloatValue(MinHeight_Prop));
    if(anElement.hasAttribute(PrefWidth_Prop)) setPrefWidth(anElement.getAttributeFloatValue(PrefWidth_Prop));
    if(anElement.hasAttribute(PrefHeight_Prop)) setPrefHeight(anElement.getAttributeFloatValue(PrefHeight_Prop));
    
    // Unarchive Roll, ScaleX, ScaleY
    if(anElement.hasAttribute("roll")) setRotate(anElement.getAttributeFloatValue("roll"));
    if(anElement.hasAttribute(Rotate_Prop)) setRotate(anElement.getAttributeFloatValue(Rotate_Prop));
    if(anElement.hasAttribute(ScaleX_Prop)) setScaleX(anElement.getAttributeFloatValue(ScaleX_Prop));
    if(anElement.hasAttribute(ScaleY_Prop)) setScaleY(anElement.getAttributeFloatValue(ScaleY_Prop));

    // Unarchive Vertical
    if(anElement.hasAttribute(Vertical_Prop)) setVertical(anElement.getAttributeBoolValue(Vertical_Prop));
        
    // Unarchive Border
    int bind = anArchiver.indexOf(anElement, Border.class);
    if(bind>=0) { Border border = (Border)anArchiver.fromXML(anElement.get(bind), this);
        setBorder(border); }
    
    // Unarchive Fill 
    int pind = anArchiver.indexOf(anElement, Paint.class);
    if(pind>=0) { Paint fill = (Paint)anArchiver.fromXML(anElement.get(pind), this);
        setFill(fill); }
    
    // Unarchive Effect
    int eind = anArchiver.indexOf(anElement, Effect.class);
    if(eind>=0) { Effect eff = (Effect)anArchiver.fromXML(anElement.get(eind), this);
        setEffect(eff); }

    // Unarchive Fill, Border (Legacy)
    XMLElement sxml = anElement.getElement("stroke");
    if(sxml!=null) { String cstr = sxml.getAttributeValue("color"); Color sc = cstr!=null? new Color(cstr):Color.BLACK;
        double sw = sxml.getAttributeFloatValue("width", 1); setBorder(sc, sw); }
    XMLElement fxml = anElement.getElement("fill");
    if(fxml!=null) { Paint fill = (Paint)anArchiver.fromXML(fxml, this); setFill(fill); }
    XMLElement bxml = anElement.getElement("border");
    if(bxml!=null) { Border border = Border.fromXMLBorder(anArchiver, bxml); setBorder(border); }
    
    // Unarchive font
    XMLElement fontXML = anElement.getElement(Font_Prop);
    if(fontXML!=null) setFont((Font)anArchiver.fromXML(fontXML, this));
    
    // Unarchive Disabled, Visible, Opacity
    if(anElement.hasAttribute(Disabled_Prop)) setDisabled(anElement.getAttributeBoolValue(Disabled_Prop));
    if(anElement.hasAttribute(Visible_Prop)) setVisible(anElement.getAttributeBoolValue(Visible_Prop));
    if(anElement.hasAttribute(Opacity_Prop)) setOpacity(anElement.getAttributeFloatValue(Opacity_Prop));
    
    // Unarchive Alignment, Padding
    else if(anElement.hasAttribute(Align_Prop)) setAlign(Pos.get(anElement.getAttributeValue(Align_Prop)));
    if(anElement.hasAttribute(Padding_Prop)) {
        Insets ins = Insets.get(anElement.getAttributeValue(Padding_Prop));
        setPadding(ins);
    }
    
    // Unarchive GrowWidth, GrowHeight, LeanX, LeanY
    if(anElement.hasAttribute(GrowWidth_Prop)) setGrowWidth(anElement.getAttributeBoolValue(GrowWidth_Prop));
    if(anElement.hasAttribute(GrowHeight_Prop)) setGrowHeight(anElement.getAttributeBoolValue(GrowHeight_Prop));
    if(anElement.hasAttribute(LeanX_Prop)) setLeanX(HPos.get(anElement.getAttributeValue(LeanX_Prop)));
    if(anElement.hasAttribute(LeanY_Prop)) setLeanY(VPos.get(anElement.getAttributeValue(LeanY_Prop)));
        
    // Unarchive Autosizing
    if(anElement.hasAttribute("asize")) setAutosizing(anElement.getAttributeValue("asize"));
    
    // Unarchive animation
    ViewAnim anim = null;
    for(int i=anElement.indexOf("KeyFrame");i>=0;i=anElement.indexOf("KeyFrame",i+1)) {
        XMLElement kframe = anElement.get(i); int time = kframe.getAttributeIntValue("time");
        anim = anim!=null? anim.getAnim(time) : getAnim(0).getAnim(time);
        for(int j=kframe.indexOf("KeyValue");j>=0;j=kframe.indexOf("KeyValue",j+1)) { XMLElement kval = kframe.get(j);
            String key = kval.getAttributeValue("key"); double val = kval.getAttributeFloatValue("value");
            anim.setValue(key, val);
        }
        if(kframe.getAttributeBoolValue("Loops", false)) anim.setLoops();
        if(kframe.hasAttribute("LoopCount")) anim.setLoopCount(kframe.getAttributeIntValue("LoopCount"));
    }
    
    // Unarchive bindings
    for(int i=anElement.indexOf("binding");i>=0;i=anElement.indexOf("binding",i+1)) { XMLElement bx=anElement.get(i);
        addBinding(new Binding().fromXML(anArchiver, bx)); }

    // Unarchive ToolTip, ItemKey
    if(anElement.hasAttribute("ttip")) setToolTip(anElement.getAttributeValue("ttip"));
    if(anElement.hasAttribute(ToolTip_Prop)) setToolTip(anElement.getAttributeValue(ToolTip_Prop));
    if(anElement.hasAttribute(ItemKey_Prop)) setItemKey(anElement.getAttributeValue(ItemKey_Prop));
    
    // Unarchive class property for subclass substitution, if available
    if(anElement.hasAttribute("Class"))
        setRealClassName(anElement.getAttributeValue("Class"));

    // Return this shape
    return this;
}

/**
 * Standard clone implementation.
 */
public View clone()  { return new ViewArchiver().copy(this); }

/**
 * Standard toString implementation.
 */
public String toString()
{
    StringBuffer sb = StringUtils.toString(this);
    if(getName()!=null && getName().length()>0) StringUtils.toStringAdd(sb, "Name", getName());
    if(getText()!=null && getText().length()>0) StringUtils.toStringAdd(sb, "Text", getText());
    StringUtils.toStringAdd(sb, "Bounds", getBounds());
    return sb.toString();
}

/**
 * An interface for views that are selectable.
 */
public interface Selectable <T> {
    
    /** Returns the items for a given name or UI view. */
    default List <T> getItems() { return null; }

    /** Sets the items for a given name or UI view. */
    default void setItems(List <T> theItems)  { }

    /** Sets the items for a given name or UI view. */
    default void setItems(T ... theItems)  { }

    /** Returns the selected index for given name or UI view. */
    int getSelectedIndex();
    
    /** Sets the selected index for given name or UI view. */
    void setSelectedIndex(int aValue);
    
    /** Returns the selected item for given name or UI view. */
    default T getSelectedItem()  { int i = getSelectedIndex(); return i>=0? getItems().get(i) : null; }
    
    /** Sets the selected item for given name or UI view. */
    default void setSelectedItem(T anItem)  { int i = getItems().indexOf(anItem); setSelectedIndex(i); }

    // Constants for properties
    public static final String Items_Prop = "Items";
    public static final String SelectedItem_Prop = "SelectedItem";
    public static final String SelectedIndex_Prop = "SelectedIndex";
}

}