/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.ArrayUtils;

/**
 * The top level View in a window.
 */
public class RootView extends ParentView {
    
    // The window this root view is part of
    WindowView               _win;
    
    // The MenuBar
    MenuBar                  _mbar;
    
    // The content
    View                     _content;
    
    // The focused view
    View                     _focusedView = this;
    
    // A popup window, if one was added to root view during last event
    PopupWindow              _popup;

    // The layout
    ViewLayout.BorderLayout  _layout = new ViewLayout.BorderLayout(this);
    
    // A map of dirty info
    Map <View,Rect>          _dirtyRects = new HashMap();
    
    // PaintLater runnable
    Runnable                 _plater, _platerShared = () -> paintLater();
    
    // A set of ViewOwners that want to be reset on next UI update call
    Set <ViewOwner>          _resetLaters = Collections.synchronizedSet(new HashSet());
    
    // A set of Views with active animations
    Set <View>               _animViews = new HashSet();
    
    // The timer for animated views
    ViewTimer                _timer = new ViewTimer(25, t -> activatePaintLater());

    // Whether painting in debug mode
    boolean                  _debug = false; int _pc; long _frames[] = null; //new long[20];
    
    // Constants for properties
    public static final String MenuBar_Prop = "MenuBar";
    public static final String Content_Prop = "Content";
    public static final String CurrentCursor_Prop = "CurrentCursor";

/**
 * Creates a RootView.
 */
public RootView()  { enableEvents(KeyEvents); setFocusable(true); }

/**
 * Returns the window for this root view.
 */
public WindowView getWindow()  { return _win; }

/**
 * Returns the menubar.
 */
public MenuBar getMenuBar()  { return _mbar; }

/**
 * Sets the menubar.
 */
public void setMenuBar(MenuBar aMBar)
{
    if(aMBar==_mbar) return;
    _layout.setTop(aMBar);
    View old = _mbar; if(_mbar!=null) removeChild(_mbar);
    _mbar = aMBar; if(_mbar!=null) addChild(_mbar,0);
    firePropChange(MenuBar_Prop, old, _mbar);
}

/**
 * Returns the content.
 */
public View getContent()  { return _content; }

/**
 * Sets the content.
 */
public void setContent(View aView)
{
    View old = _content; if(aView==old) return;
    _layout.setCenter(aView);
    if(_content!=null) removeChild(_content); _content = aView;
    if(_content!=null) addChild(_content);
    firePropChange(Content_Prop, old, _content);
}

/**
 * Returns the popup window, if one was added to root view during last event.
 */
public PopupWindow getPopup()  { return _popup; }

/**
 * Sets the popup window, if one added to this root view during last event.
 */
protected void setPopup(PopupWindow aPopup)  { _popup = aPopup; }

/**
 * Returns the root view.
 */
public RootView getRootView()  { return this; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(-1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(-1); }

/**
 * Returns the current cursor.
 */
public Cursor getCurrentCursor()  { return _ccursor; } Cursor _ccursor = Cursor.DEFAULT;

/**
 * Sets the current cursor.
 */
public void setCurrentCursor(Cursor aCursor)
{
    if(aCursor==_ccursor) return;
    firePropChange(CurrentCursor_Prop, _ccursor, _ccursor=aCursor);
}

/** 
 * Override to try to get tool tip from mouse over stack.
 */
public String getToolTip(ViewEvent anEvent)
{
    for(int i=_mouseOvers.size()-1;i>=0;i--) { View view = _mouseOvers.get(i);
        String text = view.isToolTipEnabled()? view.getToolTip(anEvent.copyForView(view)) : view.getToolTip();
        if(text!=null) return text;
    }
    return null;
}

/**
 * Override to handle when RootView is ordered out.
 */
protected void setShowing(boolean aVal)
{
    if(aVal==isShowing()) return; super.setShowing(aVal);
    
    // If no longer showing, dispatch mouse move outside bounds to trigger any mouse exit code
    if(!aVal) {
        ViewEvent event = getEnv().createEvent(this, null, MouseMove, null);
        event = event.copyForViewPoint(this, getWidth()+100, 0, 0);
        dispatchMouseEvent(event);
    }
}

/**
 * Returns the view that currently receives KeyEvents.
 */
public View getFocusedView()  { return _focusedView; }

/**
 * Tries to makes the given view the view that receives KeyEvents.
 */
protected void requestFocus(View aView)
{
    // Make sure this happens on Event thread
    if(!getEnv().isEventThread()) { getEnv().runLater(() -> requestFocus(aView)); return; }
    
    // If existing FocusedView, clear View.Focused
    if(_focusedView!=null)
        _focusedView.setFocused(false);
    
    // If new FocusedView, set View.Focused
    _focusedView = aView;
    if(_focusedView!=null)
        _focusedView.setFocused(true);
}

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

/**
 * Override to register for layout.
 */
protected void setNeedsLayoutDeep(boolean aVal)
{
    if(aVal==isNeedsLayoutDeep()) return; super.setNeedsLayoutDeep(aVal);
    activatePaintLater();
}

/**
 * Activate PaintLater.
 */
private final void activatePaintLater()  { if(_plater==null) getEnv().runLater(_plater = _platerShared); }

/**
 * Override to actually paint in this RootView.
 */
protected void repaintInParent(Rect aRect)  { repaint(aRect!=null? aRect : getBoundsInside()); }

/**
 * Adds a given ViewOwner to set of owners that need reset on next UI update call.
 */
public void resetLater(ViewOwner anOwnr)
{
    _resetLaters.add(anOwnr);
    activatePaintLater();
}

/**
 * Adds a view to set of Views that are being animated.
 */
public void playAnim(View aView)
{
    _animViews.add(aView);                     //System.out.println("Add Anim " + name(aView));
    if(_animViews.size()==1) _timer.start();
    ViewAnim anim = aView.getAnim(0); anim._rview = this;
    if(!anim.isSuspended() || anim.getStartTime()<0) anim.setStartTime(_timer.getTime());
}

/**
 * Removes a view to set of Views that are being animated.
 */
public void stopAnim(View aView)
{
    if(!_animViews.remove(aView)) return;
    if(_animViews.size()==0) _timer.stop();   //System.out.println("Remove Anim " + name(aView));
    ViewAnim anim = aView.getAnim(0); anim._rview = null;
}

/**
 * Called to register a repaint.
 */
public synchronized void repaint(View aView, double aX, double aY, double aW, double aH)
{
    // Register for paintLater
    if(_plater==null) getEnv().runLater(_plater = _platerShared);
    
    // Set or combine dirty rect
    Rect drect = _dirtyRects.get(aView);
    if(drect==null) _dirtyRects.put(aView,new Rect(aX,aY,aW,aH));
    else {
        double x = drect.x, y = drect.y, w = drect.width, h = drect.height;
        drect.x = Math.min(x,aX); drect.width = Math.max(x+w, aX+aW) - drect.x;
        drect.y = Math.min(y,aY); drect.height = Math.max(y+h, aY+aH) - drect.y;
    }
}

String name(View v)
{ return (v.getName()!=null? v.getName() : v.getClass().getSimpleName()) + " " + System.identityHashCode(v); }

/**
 * Called to request a paint after current event.
 */
public synchronized void paintLater()
{
    // If timer is running, send Anim calls
    if(_timer.isRunning()) {
        View aviews[] = _animViews.toArray(new View[0]); int time = _timer.getTime();
        for(View av : aviews) { ViewAnim anim = av.getAnim(-1);
            if(!av.isShowing()) { System.err.println("View shouldn't be animated: "+name(av));
                System.err.flush(); stopAnim(av); continue; }
            if(anim==null || anim.isSuspended()) stopAnim(av);
            else anim.setTime(time);
        }
        //System.out.println("SetTime " + time);
    }

    // Send reset later calls
    while(_resetLaters.size()>0) {
        ViewOwner owners[] = _resetLaters.toArray(new ViewOwner[_resetLaters.size()]); _resetLaters.clear();
        for(ViewOwner no : owners) no.processResetUI();
    }
    
    // Layout all views that need it
    layoutDeep();

    // Calculate composite repaint rect from all dirty views/rects
    Rect rect = null; if(_dirtyRects.size()==0)  { _plater = null; return; }
    View views[] = _dirtyRects.keySet().toArray(new View[_dirtyRects.size()]);
    for(View n : views) { Rect r = _dirtyRects.get(n);
        Transform tfm = n!=this? n.getLocalToParent(this) : Transform.IDENTITY;
        Rect vr = n.getVisRect(); vr = vr.copyFor(tfm).getBounds();
        r = r.copyFor(tfm).getBounds();
        r = r.getIntersectRect(vr);
        if(rect==null) rect = r;
        else rect.union(r);
    }
    
    // Round rect and request real repaint
    rect.snap();
    getHelper().requestPaint(rect);
    
    // Clear dirty rects, reset runnable and return
    _dirtyRects.clear(); _plater = null; _pc++;
    if(_debug && _pc%2==0) repaint(this, rect.x, rect.y, rect.width, rect.height);
}

/**
 * Paint views.
 */
public void paintViews(Painter aPntr, Rect aRect)
{
    aPntr.save(); if(_frames!=null) startTime();
    aPntr.clip(aRect);
    if(getFill()==null) aPntr.clearRect(aRect.x,aRect.y,aRect.width,aRect.height);
    if(_debug && _pc%2==0) paintDebug(this, aPntr, aRect);
    else paintAll(aPntr);
    aPntr.restore();
    if(_frames!=null) { stopTime(); if(_pc%20==0) printTime(); }
}

/**
 * Dispatch event.
 */
public void dispatchEvent(ViewEvent anEvent)
{
    // If popup window, forward to it
    if(_popup!=null) {
        if(anEvent.isMouseDrag() || anEvent.isMouseRelease()) {
            _popup.processTriggerEvent(anEvent);
            if(anEvent.isMouseRelease()) _popup = null; return;
        }
        else if(anEvent.isKeyPress() && anEvent.isEscapeKey())
            _popup.hide();
        if(!_popup.isShowing())
            _popup = null;
    }
    
    if(anEvent.isMouseEvent() || anEvent.isScroll()) dispatchMouseEvent(anEvent);
    else if(anEvent.isKeyEvent()) dispatchKeyEvent(anEvent);
    else anEvent.getView().fireEvent(anEvent);
}

/**
 * Dispatch Mouse event.
 */
public void dispatchMouseEvent(ViewEvent anEvent)
{
    // Update ViewEnv.MouseDown
    if(anEvent.isMousePress()) ViewUtils._mouseDown = true;
    else if(anEvent.isMouseRelease()) ViewUtils._mouseDown = false;

    // Get target view (at mouse point, or mouse press, or mouse press point)
    View targ = ViewUtils.getDeepestViewAt(this, anEvent.getX(), anEvent.getY());
    if(anEvent.isMouseExit()) targ = null;
    if(anEvent.isMouseDrag() || anEvent.isMouseRelease()) {
        targ = _mousePressView;
        if(targ.getRootView()!=this)
            targ = _mousePressView = ViewUtils.getDeepestViewAt(this, _mpx, _mpy);
    }
    
    // Get target parents
    View pars[] = getParents(targ);
    
    // Update MouseOvers
    if(anEvent.isMouseMove() || anEvent.isMouseRelease() || anEvent.isMouseEnter() || anEvent.isMouseExit()) {
    
        // Remove old MouseOver views and dispatch appropriate MouseExited events
        for(int i=_mouseOvers.size()-1;i>=0;i--) { View view = _mouseOvers.get(i);
             if(!ArrayUtils.containsId(pars,view)) {
                 _mouseOvers.remove(i); _mouseOverView = i>0? _mouseOvers.get(i-1) : null;
                if(!view.getEventAdapter().isEnabled(MouseExit)) continue;
                 ViewEvent e2 = getEnv().createEvent(view, anEvent.getEvent(), MouseExit, null);
                 view.fireEvent(e2);
             }
             else break;
        }
        
        // Add new MouseOver views and dispatch appropriate MouseEntered events
        for(int i=_mouseOvers.size();i<pars.length;i++) { View view = pars[i];
            _mouseOvers.add(view); _mouseOverView = view;
            if(!view.getEventAdapter().isEnabled(MouseEnter)) continue;
             ViewEvent e2 = getEnv().createEvent(view, anEvent.getEvent(), MouseEnter, null);
             view.fireEvent(e2);
        }
        
        // Update CurrentCursor
        if(_mouseOverView!=null && _mouseOverView.getCursor()!=getCurrentCursor())
            setCurrentCursor(_mouseOverView.getCursor());
    }
    
    // Handle MousePress: Update MousePressView and mouse pressed point
    else if(anEvent.isMousePress()) {
        _mousePressView = targ; _mpx = anEvent.getX(); _mpy = anEvent.getY();
        for(View n=targ;n!=null;n=n.getParent())
            if(n.isFocusWhenPressed() && (getFocusedView()==null || !getFocusedView().isAncestor(n))) {
                n.requestFocus(); break; }
    }
    
    // Iterate down and see if any should filter
    /*for(View view : pars)
        if(view.getEventAdapter().isFiltered(anEvent.getType())) {
            ViewEvent e2 = anEvent.copyForView(view);
            view.processEvent(e2);
            if(e2.isConsumed()) { anEvent.consume(); return; }  }*/
        
    // Iterate back up and see if any parents should handle
    for(int i=pars.length-1;i>=0;i--) { View view = pars[i];
        if(view.getEventAdapter().isEnabled(anEvent.getType())) {
            ViewEvent e2 = anEvent.copyForView(view);
            view.fireEvent(e2);
            if(e2.isConsumed()) { anEvent.consume(); break; }
        }
    }
    
    // If popup window is now present, forward trigger event to it
    if(_popup!=null)
        _popup.processTriggerEvent(anEvent);
}

List <View> _mouseOvers = new ArrayList();
View _mouseOverView, _mousePressView, _dragSourceView, _dragOverView;
double _mpx, _mpy;

/**
 * Dispatch key event.
 */
public void dispatchKeyEvent(ViewEvent anEvent)
{
    // Update modifiers
    ViewUtils._altDown = anEvent.isAltDown();
    ViewUtils._cntrDown = anEvent.isControlDown();
    ViewUtils._metaDown = anEvent.isMetaDown();
    ViewUtils._shiftDown = anEvent.isShiftDown();
    ViewUtils._shortcutDown = anEvent.isShortcutDown();

    if(_focusedView.getRootView()!=this) requestFocus(this); // Bogus
    View targ = _focusedView;
    View pars[] = getParents(targ);
    
    // Iterate back up and see if any parents should handle
    for(int i=pars.length-1;i>=0;i--) { View view = pars[i];
        if(view.getEventAdapter().isEnabled(anEvent.getType())) {
            ViewEvent event = anEvent.copyForView(view);
            view.fireEvent(event);
            if(event.isConsumed()) { anEvent.consume(); return; }
        }
    }
    
    // Send to MenuBar
    if(anEvent.isKeyPress() && anEvent.isShortcutDown() && getMenuBar()!=null)
        getMenuBar().processEvent(anEvent);
}

/**
 * Dispatch drag gesture event.
 */
public void dispatchDragSourceEvent(ViewEvent anEvent)
{
    // Handle DragGesture
    if(anEvent.isDragGesture()) {
        for(View view=_mousePressView;view!=null;view=view.getParent())
            if(view.getEventAdapter().isEnabled(DragGesture)) { _dragSourceView = view;
                ViewEvent event = anEvent.copyForView(view);
                view.fireEvent(event); break; }
    }
    
    // Handle DragSource
    else if(_dragSourceView!=null) {
        if(_dragSourceView.getEventAdapter().isEnabled(anEvent.getType())) {
            ViewEvent event = anEvent.copyForView(_dragSourceView);
            _dragSourceView.fireEvent(event);
        }
        if(anEvent.isDragSourceEnd())
            _dragSourceView = null;
    }
}

/**
 * Dispatch drag target event.
 */
public void dispatchDragTargetEvent(ViewEvent anEvent)
{
    // Get target view and parents
    View targ = ViewUtils.getDeepestViewAt(this, anEvent.getX(), anEvent.getY());
    View pars[] = getParents(targ);
    
    // Remove old DragOver views and dispatch appropriate MouseExited events
    for(View view=_dragOverView;view!=null;view=view.getParent()) {
         if(!ArrayUtils.containsId(pars,view)) {
             _dragOverView = view.getParent();
             if(!view.getEventAdapter().isEnabled(DragExit)) continue;
             ViewEvent e2 = anEvent.copyForView(view); e2._type = DragExit;
             view.fireEvent(e2);
         }
         else break;
    }
    
    // Add new DragOver views and dispatch appropriate MouseEntered events
    int start = getParentCount(_dragOverView!=null? _dragOverView : this);
    for(int i=start;i<pars.length;i++) { View view = pars[i]; _dragOverView = view;
        if(!view.getEventAdapter().isEnabled(DragEnter)) continue;
        ViewEvent e2 = anEvent.copyForView(view); e2._type = DragEnter;
        view.fireEvent(e2);
    }
    
    // Handle DragOver, DragDrop
    if(anEvent.isDragOver() || anEvent.isDragDrop()) {
        for(View view=_dragOverView;view!=null;view=view.getParent()) {
            if(view.getEventAdapter().isEnabled(anEvent.getType())) {
                ViewEvent e2 = anEvent.copyForView(view);
                view.fireEvent(e2); break;
            }
        }
    }
}

/**
 * Do debug paint.
 */
protected void paintDebug(View aView, Painter aPntr, Shape aShape)
{
    aPntr.setColor(Color.YELLOW); aPntr.fill(aShape);
    getEnv().runLater(() -> { try { Thread.sleep(50); } catch(Exception e) { }});
    //TimerTask task = new TimerTask() { public void run()  { RootView.this.repaint(aShape.getBounds()); }};
    //new Timer().schedule(task, 100);
}

/**
 * Timing methods.
 */
private void startTime()  { _time = System.currentTimeMillis(); } long _time;
private void stopTime() {
    System.arraycopy(_frames,1,_frames,0,_frames.length-1);
    _frames[_frames.length-1] = System.currentTimeMillis() - _time; }
private void printTime() {
    int time = 0; for(int i=0;i<_frames.length;i++) time += _frames[i]; double avg = time/(double)_frames.length;
    System.out.println("FrameRate: " + (int)(1000/avg)); }
    
// Returns the number of parents of given view including this RootView.
private int getParentCount(View aView)
{
    if(aView==null) return 0;
    int pc = 1; for(View n=aView;n!=this;n=n.getParent()) pc++; return pc;
}

// Returns array of parents of given view up to and including this RootView.
private View[] getParents(View aView)
{
    int pc = getParentCount(aView); View pars[] = new View[pc]; if(pc==0) return pars;
    for(View n=aView;n!=this;n=n.getParent()) pars[--pc] = n; pars[0] = this;
    return pars;
}

}