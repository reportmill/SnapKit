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
    Pos             _align = getAlignDefault();
    
    // The padding for content in this view
    Insets          _padding = getPaddingDefault();
    
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
    
    // Whether view is enabled
    boolean         _enabled = true;
    
    // Whether view is currently the RootView.FocusedView
    boolean         _focused;
    
    // Whether view can receive focus
    boolean         _focusable;
    
    // Whether view should request focus when pressed
    boolean         _focusWhenPrsd;
    
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
    
    // The view font
    Font            _font;
    
    // The view cursor
    Cursor          _cursor = Cursor.DEFAULT;
    
    // The tooltip
    String          _ttip;
    
    // The clip (if set)
    Shape           _clip;
    
    // Autosizing
    String          _asize;
    
    // Bindings for this view
    List <Binding>  _bindings = Collections.EMPTY_LIST;
    
    // Client properties
    Map             _props = new HashMap();
    
    // The parent of this view
    ParentView      _parent;
    
    // The real class name, if shape component is really a custom subclass
    String          _realClassName;
    
    // PropertyChangeSupport
    PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // The event adapter
    EventAdapter    _evtAdptr;
    
    // The animator for this view
    Animator        _animator;
    
    // The view owner of this view
    ViewOwner       _owner;
    
    // The helper for this view when dealing with native
    ViewHelper      _helper;
    
    // The view environment
    ViewEnv         _env = ViewEnv.getEnv();

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
    public static final String MaxWidth_Prop = "MinWidth";
    public static final String MaxHeight_Prop = "MaxHeight";
    public static final String PrefWidth_Prop = "PrefWidth";
    public static final String PrefHeight_Prop = "PrefHeight";
    public static final String Enabled_Prop = "Enabled";
    public static final String Focused_Prop = "Focused";
    public static final String Focusable_Prop = "Focusable";
    public static final String FocusWhenPressed_Prop = "FocusWhenPressed";
    public static final String Visible_Prop = "Visible";
    public static final String Autosizing_Prop = "Autosizing";
    public static final String Clip_Prop = "Clip";
    public static final String Cursor_Prop = "Cursor";
    public static final String Effect_Prop = "Effect";
    public static final String Fill_Prop = "Fill";
    public static final String Font_Prop = "Font";
    public static final String Align_Prop = "Align";
    public static final String Padding_Prop = "Padding";
    public static final String Parent_Prop = "Parent";
    public static final String Border_Prop = "Border";
    public static final String Showing_Prop = "Showing";
    public static final String Text_Prop = "Text";
    public static final String ToolTipText_Prop = "ToolTipText";
    public static final String ItemKey_Prop = "ItemKey";
    
    // Convenience for common events
    public static final ViewEvent.Type Action = ViewEvent.Type.Action;
    public static final ViewEvent.Type KeyPressed = ViewEvent.Type.KeyPressed;
    public static final ViewEvent.Type KeyReleased = ViewEvent.Type.KeyReleased;
    public static final ViewEvent.Type KeyTyped = ViewEvent.Type.KeyTyped;
    public static final ViewEvent.Type MousePressed = ViewEvent.Type.MousePressed;
    public static final ViewEvent.Type MouseDragged = ViewEvent.Type.MouseDragged;
    public static final ViewEvent.Type MouseReleased = ViewEvent.Type.MouseReleased;
    public static final ViewEvent.Type MouseClicked = ViewEvent.Type.MouseClicked;
    public static final ViewEvent.Type MouseEntered = ViewEvent.Type.MouseEntered;
    public static final ViewEvent.Type MouseMoved = ViewEvent.Type.MouseMoved;
    public static final ViewEvent.Type MouseExited = ViewEvent.Type.MouseExited;
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
    public static final ViewEvent.Type WinActivated = ViewEvent.Type.WinActivated;
    public static final ViewEvent.Type WinDeactivated = ViewEvent.Type.WinDeactivated;
    public static final ViewEvent.Type WinOpened = ViewEvent.Type.WinOpened;
    public static final ViewEvent.Type WinClosing = ViewEvent.Type.WinClosing;
    public ViewEvent.Type KeyEvents[] = { KeyPressed, KeyReleased, KeyTyped };
    public ViewEvent.Type MouseEvents[] = { MousePressed, MouseDragged, MouseReleased,
        MouseClicked, MouseEntered, MouseMoved, MouseExited };
    public ViewEvent.Type DragEvents[] = { DragEnter, DragExit, DragOver, DragDrop };

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
    setClipAll(null);
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
    setClipAll(null);
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
public Size getSize()  { return Size.get(getWidth(), getHeight()); }

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
 * Returns fill.
 */
public Paint getFill()  { return _fill; }

/**
 * Sets paint.
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
 * Returns the default border.
 */
public Border getBorderDefault()  { return null; }

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
 * Returns whether font has been set.
 */
public boolean isFontSet()  { return _font!=null; }

/**
 * Returns the font for the view (defaults to parent font).
 */
public Font getFont()
{
    if(_font!=null) return _font;
    View par = getParent(); if(par==null) return Font.Arial12;
    return par.getFont();
}

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
    RootView rpane = getRootView(); if(rpane!=null) rpane.setCurrentCursor(_cursor);
}

/**
 * Whether view is disabled.
 */
public boolean isDisabled()  { return !_enabled; }

/**
 * Whether view is enabled.
 */
public boolean isEnabled()  { return _enabled; }

/**
 * Sets whether view is enabled.
 */
public void setEnabled(boolean aValue)
{
    if(aValue==_enabled) return;
    firePropChange(Enabled_Prop, _enabled, _enabled=aValue);
    repaint();
}

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
    Shape old = _clip; _clip = aShape; setClipAll(null);
    firePropChange(Clip_Prop, old, _clip);
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
    if(_clipAll!=null) return _clipAll;
    Shape vshp = getParent()!=null? getParent().getClipAll() : null;
    if(vshp!=null) vshp = parentToLocal(vshp);
    if(getClip()!=null) vshp = vshp!=null? Shape.intersect(vshp, getClip()) : getClip();
    return _clipAll = vshp;
} Shape _clipAll;

/**
 * Sets the clip of this view due to all parents.
 */
protected void setClipAll(Shape aShape)
{
    if(SnapUtils.equals(aShape, _clipAll)) return;
    _clipAll = aShape;
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
    Rect cbnds = getClipBoundsAll(); //return clip!=null? clip : getBoundsInside(); - old version
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
public Shape localToParent(Shape aShape)  { return getLocalToParent().createTransformedShape(aShape); }

/**
 * Converts a point from local to given parent.
 */
public Shape localToParent(View aPar, Shape aShape)  { return getLocalToParent(aPar).createTransformedShape(aShape); }

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
public Shape parentToLocal(Shape aShape)  { return getParentToLocal().createTransformedShape(aShape); }

/**
 * Converts a shape from parent to local.
 */
public Shape parentToLocal(View aView, Shape aShape)  { return getParentToLocal(aView).createTransformedShape(aShape); }

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
    
    // Propogate to children
    if(this instanceof ParentView) { ParentView pview = (ParentView)this;
        for(View child : pview.getChildren()) child.setShowing(_showing && child.isVisible()); }
        
    // If animator set, pause/play
    if(getAnimator(false)!=null) { Animator anim = getAnimator(false);
        if(aValue && anim.isPaused()) anim.play();
        else if(!aValue && anim.isRunning()) anim.pause();
    }
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
public void setManaged(boolean aValue)  { _managed = aValue; }

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
public boolean isHorizontal()  { return !_vertical; }

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
public String getAutosizing()  { return _asize!=null && _asize.length()>6? _asize : getAutosizingDefault(); }

/**
 * Sets the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
 */
public void setAutosizing(String aValue)
{
    if(SnapUtils.equals(aValue, _asize)) return;
    firePropChange(Autosizing_Prop, _asize, _asize=aValue);
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
public Size getMinSize()  { return Size.get(getMinWidth(), getMinHeight()); }

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
public double getMaxWidth()  { return _maxWidth>=0? _maxWidth : Double.MAX_VALUE; }

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
public double getMaxHeight()  { return _maxHeight>=0? _maxHeight : Double.MAX_VALUE; }

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
public Size getMaxSize()  { return Size.get(getMaxWidth(), getMaxHeight()); }

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
public Size getPrefSize()  { return Size.get(getPrefWidth(), getPrefHeight()); }

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
public double getBestWidth(double aH)  { return Math.max(getMinWidth(), getPrefWidth()); }

/**
 * Returns the best height for view - accounting for pref/min/max.
 */
public double getBestHeight(double aW)  { return Math.max(getMinHeight(), getPrefHeight()); }

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
}

/**
 * Returns the default alignment.
 */    
public Pos getAlignDefault()  { return Pos.TOP_LEFT; }

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
    if(theIns==null) theIns = getPaddingDefault();
    if(SnapUtils.equals(theIns,_padding)) return;
    firePropChange(Padding_Prop, _padding, _padding = theIns);
}

/**
 * Returns the padding default.
 */
public Insets getPaddingDefault()  { return _emptyIns; }

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
public String getToolTipText()  { return _ttip; }

/**
 * Sets the tool tip text.
 */
public void setToolTipText(String aString)
{
    if(SnapUtils.equals(aString,_ttip)) return;
    firePropChange(ToolTipText_Prop, _ttip, _ttip=aString);
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
 * Returns a tool tip string by asking deepest shape's tool.
 */
public String getToolTipText(ViewEvent anEvent)  { return null; }

/**
 * Returns whether focus keys are enabled.
 */
public boolean isFocusKeysEnabled()  { return _focusKeysEnbld; } boolean _focusKeysEnbld = true;

/**
 * Sets whether focus keys are enabled.
 */
public void setFocusKeysEnabled(boolean aValue)  { _focusKeysEnbld = aValue; }

/** Scroller method. */
public boolean isScrollFitWidth() { return getScrollPrefWidth()<getParent().getWidth(); }

/** Scroller method. */
public boolean isScrollFitHeight() { return getScrollPrefHeight()<getParent().getHeight(); }

/** Scroller method. */
public double getScrollPrefWidth()  { return getPrefWidth(); }

/** Scroller method. */
public double getScrollPrefHeight()  { return getPrefHeight(); }

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
    RootView rpane = getRootView(); if(rpane==null) return;
    rpane.repaint(this, aX, aY, aW, aH);
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
 * Tells view to request focus.
 */
public void requestFocus()
{
    if(isFocused()) return;
    RootView rpane = getRootView(); if(rpane!=null) rpane.requestFocus(this);
    repaint();
}

/**
 * Returns the view owner.
 */
public ViewOwner getOwner()  { return _owner; }

/**
 * Sets the owner.
 */
public void setOwner(ViewOwner anOwner)  { _owner = anOwner; }

/**
 * Returns the owner of given class.
 */
public <T> T getOwner(Class <T> aClass)
{
    if(getOwner()!=null && aClass.isAssignableFrom(getOwner().getClass())) return (T)getOwner();
    return getParent()!=null? getParent().getOwner(aClass) : null;
}

/**
 * Initialize view for owner.
 */
protected void initUI(ViewOwner anOwner)
{
    if(_evtAdptr!=null && _evtAdptr.isEnabled(Action)) anOwner.enableEvents(this,Action);
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
 * Returns a value.
 */
public Object getValue(String aPropName)
{
    // Map property name
    String pname = aPropName.equals("Value")? getValuePropName() : aPropName;

    // Handle Enabled
    if(pname.equals("Enabled"))
        return isEnabled();
    
    // Handle Items
    else if(pname.equals("Items")) { Selectable sview = (Selectable)this;
        return sview.getItems(); }
    
    // Handle SelectedItem
    else if(pname.equals("SelectedItem")) { Selectable sview = (Selectable)this;
        return sview.getSelectedItem(); }
    
    // Handle SelectedIndex
    else if(pname.equals("SelectedIndex")) { Selectable sview = (Selectable)this;
        return sview.getSelectedIndex(); }
    
    // Use key chain evaluator to get value
    return KeyChain.getValue(this, pname);
}

/**
 * Sets a value.
 */
public void setValue(String aPropName, Object aValue)
{
    // Map property name
    String pname = aPropName.equals("Value")? getValuePropName() : aPropName;
    
    // Handle Enabled
    if(pname.equals("Enabled"))
        setEnabled(SnapUtils.boolValue(aValue));
    
    // Handle Items
    else if(pname.equals("Items")) { Selectable sview = (Selectable)this;
        if(aValue instanceof List) sview.setItems((List)aValue);
        else if(aValue!=null && aValue.getClass().isArray()) sview.setItems((Object[])aValue);
        else sview.setItems(Collections.emptyList());
    }
        
    // Handle SelectedItem
    else if(pname.equals("SelectedItem")) { Selectable sview = (Selectable)this;
        sview.setSelectedItem(aValue); }
    
    // Handle SelectedIndex
    else if(pname.equals("SelectedIndex")) { Selectable sview = (Selectable)this;
        int index = aValue==null? -1 : SnapUtils.intValue(aValue);
        sview.setSelectedIndex(index);
    }
    
    // Set value with key
    else KeyChain.setValueSafe(this, pname, aValue);
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
 * Sends an event to this view.
 */
public void fireEvent(ViewEvent anEvent)
{
    // Forward event to listeners from last to first, short-circuit if event is consume
    EventListener handlers[] = getEventAdapter()._handlers;
    for(int i=handlers.length-1; i>=0; i--) { EventListener lsnr = handlers[i];
        if(getEventAdapter()._types.get(lsnr).contains(anEvent.getType())) {
            lsnr.fireEvent(anEvent); if(anEvent.isConsumed()) break; }}
            
    // If event not consumed, send to view
    if(!anEvent.isConsumed())
        processEvent(anEvent);
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
 * Process ViewEvent.
 */
protected void processEvent(ViewEvent anEvent)  { }

/**
 * Returns the animator for this view.
 */
public Animator getAnimator()  { return _animator; }

/**
 * Returns the animator for this view.
 */
public Animator getAnimator(boolean doCreate)
{
    return _animator!=null || !doCreate? _animator : (_animator=new Animator(this));
}

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element called shape
    XMLElement e = new XMLElement("shape");
    
    // Archive name
    if(getName()!=null && getName().length()>0) e.add("name", getName());
    
    // Archive X, Y, Width, Height
    View par = getParent();
    if(par instanceof SpringView) {
        if(getX()!=0) e.add("x", getX());
        if(getY()!=0) e.add("y", getY());
        if(getWidth()!=0) e.add("width", getWidth());
        if(getHeight()!=0) e.add("height", getHeight());
    }
    
    // Archive Roll, ScaleX, ScaleY, SkewX, SkewY
    if(getRotate()!=0) e.add("roll", getRotate());
    if(getScaleX()!=1) e.add("scalex", getScaleX());
    if(getScaleY()!=1) e.add("scaley", getScaleY());
    //if(getSkewX()!=0) e.add("skewx", getSkewX());
    //if(getSkewY()!=0) e.add("skewy", getSkewY());

    // Archive Stroke, Fill, Effect
    //if(getStroke()!=null) e.add(anArchiver.toXML(getStroke(), this));
    //if(getFill()!=null) e.add(anArchiver.toXML(getFill(), this));
    //if(getEffect()!=null) e.add(anArchiver.toXML(getEffect(), this));
    
    // Archive font
    if(isFontSet()) e.add(getFont().toXML(anArchiver));
    
    // Archive Opacity, Visible
    //if(getOpacity()<1) e.add("opacity", getOpacity());
    if(!isVisible()) e.add("visible", false);
    
    // Archive URL, Hover
    //if(getURL()!=null && getURL().length()>0) e.add("url", getURL());
    //if(getHover()!=null && getHover().length()>0) e.add("hover", getHover());
    
    // Archive MinWidth, MinHeight, PrefWidth, PrefHeight
    if(isMinWidthSet()) e.add("MinWidth", getMinWidth());
    if(isMinHeightSet()) e.add("MinHeight", getMinHeight());
    if(isPrefWidthSet()) e.add("PrefWidth", getPrefWidth());
    if(isPrefHeightSet()) e.add("PrefHeight", getPrefHeight());
    
    // Archive Autosizing
    String asize = getAutosizing(); if(!asize.equals(getAutosizingDefault())) e.add("asize", asize);
    
    // Archive Alignment
    if(getAlign()!=getAlignDefault()) e.add("align", getAlign());
    
    // Archive Vertical
    if(isVertical()) e.add("Vertical", true);
    
    // Archive Padding
    if(!getPadding().equals(getPaddingDefault())) e.add("Padding", getPadding().getString());
        
    // Archive GrowWidth/GrowHeight
    if(isGrowWidth()) e.add("GrowWidth", true);
    if(isGrowHeight()) e.add("GrowHeight", true);
    
    // Archive Locked
    //if(isLocked()) e.add("locked", true);
    
    // Archive shape timeline
    //if(getTimeline()!=null) getTimeline().toXML(anArchiver, e);

    // Archive bindings
    for(Binding b : getBindings())
        e.add(b.toXML(anArchiver));

    // Archive border
    if(!SnapUtils.equals(getBorder(),getBorderDefault())) e.add(anArchiver.toXML(getBorder(), this));
    
    // Archive ToolTipText
    if(getToolTipText()!=null) e.add("ttip", getToolTipText());
    
    // Archive "enabled" flag, (defaults to true)
    if(!isEnabled()) e.add("enabled", false);
    
    // Archive ItemKey
    if(getItemKey()!=null) e.add("ItemKey", getItemKey());
    
    // Archive RealClassName
    String cname = getRealClassName(); if(cname!=null && cname.length()>0) e.add("class", cname);
    
    // Return the element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive class property for subclass substitution, if available
    if(anElement.hasAttribute("class"))
        setRealClassName(anElement.getAttributeValue("class"));

    // Unarchive name
    if(anElement.hasAttribute("name"))
        setName(anElement.getAttributeValue("name"));
    
    // Unarchive X, Y, Width, Height
    double x = anElement.getAttributeFloatValue("x", 0);
    double y = anElement.getAttributeFloatValue("y", 0);
    double w = anElement.getAttributeFloatValue("width", 0);
    double h = anElement.getAttributeFloatValue("height", 0);
    setBounds(x, y, w, h);
    
    // Unarchive Roll, ScaleX, ScaleY, SkewX, SkewY
    setRotate(anElement.getAttributeFloatValue("roll"));
    setScaleX(anElement.getAttributeFloatValue("scalex", 1));
    setScaleY(anElement.getAttributeFloatValue("scaley", 1));
    //setSkewX(anElement.getAttributeFloatValue("skewx", 0));
    //setSkewY(anElement.getAttributeFloatValue("skewy", 0));

    // Unarchive Stroke
    XMLElement sxml = anElement.getElement("stroke");
    if(sxml!=null) {
        String cstr = sxml.getAttributeValue("color");
        Color sc = cstr!=null? new Color(cstr) : Color.BLACK;
        double sw = sxml.getAttributeFloatValue("width", 1);
        Border bdr = Border.createLineBorder(sc, sw);
        setBorder(bdr);
    }
    
    // Unarchive Fill 
    XMLElement fxml = anElement.getElement("fill");
    if(fxml!=null) {
        Paint fill = (Paint)anArchiver.fromXML(fxml, this);
        setFill(fill);
    }
    
    // Unarchive Effect
    for(int i=anArchiver.indexOf(anElement, Effect.class); i>=0; i=-1) {
        Effect fill = (Effect)anArchiver.fromXML(anElement.get(i), this);
        setEffect(fill);
    }
    
    // Unarchive font
    XMLElement fontXML = anElement.getElement("font");
    if(fontXML!=null) setFont((Font)anArchiver.fromXML(fontXML, this));
    
    // Unarchive Opacity, Visible
    //setOpacity(anElement.getAttributeFloatValue("opacity", 1));
    if(anElement.hasAttribute("visible")) setVisible(anElement.getAttributeBoolValue("visible"));
    
    // Unarchive URL, Hover
    //setURL(anElement.getAttributeValue("url"));
    //setHover(anElement.getAttributeValue("hover"));
    
    // Unarchive MinWidth, MinHeight, PrefWidth, PrefHeight
    if(anElement.hasAttribute("MinWidth")) setMinWidth(anElement.getAttributeFloatValue("MinWidth"));
    if(anElement.hasAttribute("MinHeight")) setMinHeight(anElement.getAttributeFloatValue("MinHeight"));
    if(anElement.hasAttribute("PrefWidth")) setPrefWidth(anElement.getAttributeFloatValue("PrefWidth"));
    if(anElement.hasAttribute("PrefHeight")) setPrefHeight(anElement.getAttributeFloatValue("PrefHeight"));
    
    // Unarchive Autosizing
    String asize = anElement.getAttributeValue("asize");
    if(asize==null) asize = anElement.getAttributeValue("LayoutInfo");
    if(asize!=null) setAutosizing(asize);
    
    // Unarchive Alignment
    if(anElement.hasAttribute("align"))
        setAlign(Pos.get(anElement.getAttributeValue("align")));
    else if(anElement.hasAttribute("Align"))
        setAlign(Pos.get(anElement.getAttributeValue("Align")));
        
    // Unarchive Vertical
    if(anElement.hasAttribute("Vertical"))
        setVertical(anElement.getAttributeBoolValue("Vertical"));
        
    // Unarchive Padding
    if(anElement.hasAttribute("Padding")) {
        Insets insets = Insets.get(anElement.getAttributeValue("Padding"));
        setPadding(insets);
    }
    
    // Unarchive GrowWidth/GrowHeight
    if(anElement.hasAttribute("GrowWidth")) setGrowWidth(anElement.getAttributeBoolValue("GrowWidth"));
    if(anElement.hasAttribute("GrowHeight")) setGrowHeight(anElement.getAttributeBoolValue("GrowHeight"));
    
    // Unarchive LeanX, LeanY
    if(anElement.hasAttribute("LeanX")) setLeanX(HPos.get(anElement.getAttributeValue("LeanX")));
    if(anElement.hasAttribute("LeanY")) setLeanY(VPos.get(anElement.getAttributeValue("LeanY")));
        
    // Unarchive Locked
    //setLocked(anElement.getAttributeBoolValue("locked"));
    
    // Unarchive animation
    for(int i=anElement.indexOf("KeyFrame");i>=0;i=anElement.indexOf("KeyFrame",i+1)) {
        XMLElement kframe = anElement.get(i); int time = kframe.getAttributeIntValue("time");
        for(int j=kframe.indexOf("KeyValue");j>=0;j=kframe.indexOf("KeyValue",j+1)) { XMLElement kval = kframe.get(j);
            String key = kval.getAttributeValue("key"); double val = kval.getAttributeFloatValue("value");
            getAnimator(true).addKeyFrame(key, val, time);
        }
    }
    
    // Unarchive bindings
    for(int i=anElement.indexOf("binding");i>=0;i=anElement.indexOf("binding",i+1)) { XMLElement bxml=anElement.get(i);
        addBinding(new Binding().fromXML(anArchiver, bxml)); }

    // Unarchive property keys (legacy)
    /*for(int i=anElement.indexOf("property-key"); i>=0; i=anElement.indexOf("property-key", i+1)) {
        XMLElement prop = anElement.get(i); String name = prop.getAttributeValue("name");
        if(name.equals("FontColor")) name = "TextColor"; if(name.equals("IsVisible")) name = "Visible";
        String key = prop.getAttributeValue("key"); addBinding(new Binding(name, key)); } */

    // Unarchive Border
    XMLElement bxml = anElement.getElement("border");
    if(bxml!=null) { Border border = Border.fromXMLBorder(anArchiver, bxml);
        setBorder(border); }
    
    // Unarchive ToolTipText
    if(anElement.hasAttribute("ttip")) setToolTipText(anElement.getAttributeValue("ttip"));
    
    // Unarchive Enabled
    setEnabled(anElement.getAttributeBoolValue("enabled", true));
    
    // Unarchive ItemKey
    if(anElement.hasAttribute("ItemKey")) setItemKey(anElement.getAttributeValue("ItemKey"));
    
    // Unarchive class property for subclass substitution, if available
    if(anElement.hasAttribute("class"))
        setRealClassName(anElement.getAttributeValue("class"));

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
    StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append(" {");
    if(getName()!=null && getName().length()>0) sb.append(" Name=").append(getName()).append(',');
    sb.append(" Bounds=").append(getBounds()).append(',');
    return sb.append(" }").toString();
}

/**
 * An interface for views that are selectable.
 */
public interface Selectable <T> {
    
    /** Returns the view helper. */
    public ViewHelper getHelper();

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
}

}
