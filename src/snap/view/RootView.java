/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;

/**
 * The top level View in a window.
 * Responsible for repaint, relayout, resetUI and animation.
 * Also repsonsible for managing focus view and dispatching events.
 */
public class RootView extends ParentView {
    
    // The MenuBar
    MenuBar                  _mbar;
    
    // The content
    View                     _content;
    
    // The window used to show this root view on screen
    WindowView               _win;
    
    // The focused view
    View                     _focusedView = this, _focusedViewLast;
    
    // The RootView.Lister that is notified on certain root view actions
    RootView.Listener        _lsnr;
    
    // The EventDispatcher
    EventDispatcher          _eventDispatcher = new EventDispatcher(this);
    
    // A map of dirty info
    Map <View,Rect>          _dirtyRects = new HashMap();
    
    // PaintLater runnable
    Runnable                 _plater, _platerShared = () -> paintLater();
    
    // A set of ViewOwners that want to be reset on next UI update call
    Set <ViewOwner>          _resetLaters = Collections.synchronizedSet(new HashSet());
    
    // A set of Views with active animations
    Set <View>               _animViews = Collections.synchronizedSet(new HashSet());
    
    // The timer for animated views
    ViewTimer                _timer = new ViewTimer(25, t -> activatePaintLater());
    
    // Whether currently painting
    boolean                  _painting;

    // Whether painting in debug mode
    static boolean           _debug = false; static int _pc; static long _frames[] = null;//new long[20];
    
    // Constants for properties
    public static final String MenuBar_Prop = "MenuBar";
    public static final String Content_Prop = "Content";
    public static final String CurrentCursor_Prop = "CurrentCursor";

/**
 * Creates a RootView.
 */
public RootView()
{
    enableEvents(KeyEvents); setFocusable(true); setFocusPainted(false);
    setFill(ViewUtils.getBackFill());
}

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
    if(_content!=null) removeChild(_content); _content = aView;
    if(_content!=null) { addChild(_content); _content.setGrowHeight(true); }
    firePropChange(Content_Prop, old, _content);
}

/**
 * Returns the Window to manage this ViewOwner's window.
 */
public boolean isWindowSet()  { return _win!=null; }

/**
 * Returns the window for this root view.
 */
public WindowView getWindow()
{
    if(_win!=null) return _win;
    WindowView win = new WindowView();
    win.setRootView(this);
    return _win = win;
}

/**
 * Returns the popup window, if one was added to root view during last event.
 */
public PopupWindow getPopup()  { return _eventDispatcher.getPopup(); }

/**
 * Sets the popup window, if one added to this root view during last event.
 */
protected void setPopup(PopupWindow aPopup)  { _eventDispatcher.setPopup(aPopup); }

/**
 * Adds a RootView listener.
 */
public void addRootViewListener(RootView.Listener aLsnr)
{
    if(_lsnr!=null) System.err.println("RootView.addRootViewListener: Multiple listeners not yet supported");
    _lsnr = aLsnr;
}

/**
 * Removes a RootView listener.
 */
public void removeRootViewListener(RootView.Listener aLsnr)  { if(_lsnr==aLsnr) _lsnr = null; }

/**
 * Returns the root view.
 */
public RootView getRootView()  { return this; }

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
    for(int i=_eventDispatcher._mouseOvers.size()-1;i>=0;i--) { View view = _eventDispatcher._mouseOvers.get(i);
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
        dispatchEvent(event);
    }
}

/**
 * Returns the view that currently receives KeyEvents.
 */
public View getFocusedView()
{
    if(_focusedView!=null && !_focusedView.isFocused())
        _focusedView = null;
    return _focusedView;
}

/**
 * Returns the previous focus view.
 */
public View getFocusedViewLast()  { return _focusedViewLast; }

/**
 * Tries to makes the given view the view that receives KeyEvents.
 */
protected void requestFocus(View aView)
{
    // Make sure this happens on Event thread
    if(!getEnv().isEventThread()) { getEnv().runLater(() -> requestFocus(aView)); return; }
    
    // If already set, just return
    if(aView==_focusedView) return;
    
    // If existing FocusedView, clear View.Focused
    if(_focusedView!=null)
        _focusedView.setFocused(false);
    
    // Update FocusViewLast, FocusView
    _focusedViewLast = _focusedView; _focusedView = aView;
    
    // If new FocusedView, set View.Focused
    if(_focusedView!=null)
        _focusedView.setFocused(true);
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
 * Adds a given ViewOwner to set of owners that need reset on next UI update call.
 */
public void resetLater(ViewOwner anOwnr)
{
    _resetLaters.add(anOwnr);
    activatePaintLater();
}

/**
 * Override to register for layout.
 */
protected synchronized void setNeedsLayoutDeep(boolean aVal)
{
    // If Painting, complaint (nothing should change during paint)
    if(_painting)
        System.err.println("RootView.setNeedsLayoutDeep: Illegal view changes during paint.");

    // If already set, just return
    if(aVal==isNeedsLayoutDeep()) return;
    
    // Do normal version and activate paint later
    super.setNeedsLayoutDeep(aVal);
    activatePaintLater();
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return ColView.getPrefWidth(this, null, -1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return ColView.getPrefHeight(this, null, 0, -1); }

/**
 * Layout children.
 */
protected void layoutImpl()  { ColView.layout(this, null, null, true, 0); }

/**
 * Override to actually paint in this RootView.
 */
protected void repaintInParent(Rect aRect)  { repaint(aRect!=null? aRect : getBoundsLocal()); }

/**
 * Called to register a repaint.
 */
public synchronized void repaint(View aView, double aX, double aY, double aW, double aH)
{
    // If Painting, complaint (nothing should change during paint)
    if(_painting)
        System.err.println("RootView.repaint: Illegal repaint call during paint.");

    // Register for paintLater
    activatePaintLater();
    
    // Set or combine dirty rect
    Rect drect = _dirtyRects.get(aView);
    if(drect==null) _dirtyRects.put(aView,new Rect(aX,aY,aW,aH));
    else {
        double x = drect.x, y = drect.y, w = drect.width, h = drect.height;
        drect.x = Math.min(x,aX); drect.width = Math.max(x+w, aX+aW) - drect.x;
        drect.y = Math.min(y,aY); drect.height = Math.max(y+h, aY+aH) - drect.y;
    }
}

/**
 * Called to request a paint after current event.
 */
public synchronized void paintLater()
{
    // If timer is running, send Anim calls
    if(_timer.isRunning()) {
        View aviews[] = _animViews.toArray(new View[0]); int time = _timer.getTime();
        for(View av : aviews) { ViewAnim anim = av.getAnim(-1);
            if(!av.isShowing()) { System.err.println("View shouldn't be animated: " + ViewUtils.getId(av));
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
    for(View view : views) { Rect r = _dirtyRects.get(view);
    
        // Constrain to ancestor clips
        r = view.getClippedRect(r); if(r.isEmpty()) continue;
        
        // Transform to root coords
        r = view.localToParent(r, this).getBounds();
        
        // Combine
        if(rect==null) rect = r;
        else rect.union(r);
    }
    
    // Round rect and constrain to root bounds
    if(rect==null) { _plater = null; return; }
    rect.snap(); if(rect.x<0) rect.x = 0; if(rect.y<0) rect.y = 0;
    if(rect.width>getWidth()) rect.width = getWidth(); if(rect.height>getHeight()) rect.height = getHeight();
        
    // Notify listener
    if(_lsnr!=null) rect = _lsnr.rootViewWillPaint(this, rect);
    
    // Do repaint (in exception handler so we can reset things on failure)
    try {
        _painting = true;
        getHelper().requestPaint(rect);
    }
    
    // Clear dirty rects, reset runnable, update PaintCount and set Painting false
    finally { _dirtyRects.clear(); _plater = null; _pc++; _painting = false; }
}

/**
 * Paint views.
 */
public synchronized void paintViews(Painter aPntr, Rect aRect)
{
    // Save painter state, clip to rect, clear background
    aPntr.save(); if(_frames!=null) startTime();
    aPntr.clip(aRect);
    if(getFill()==null) aPntr.clearRect(aRect.x,aRect.y,aRect.width,aRect.height);
    
    // Paint views
    if(_debug) paintDebug(this, aPntr, aRect);
    else paintAll(aPntr);
    
    // Restore painter state and update frame counts
    aPntr.restore(); if(_frames!=null) { stopTime(); if(_pc%20==0) printTime(); }
}

/**
 * Do debug paint.
 */
protected void paintDebug(View aView, Painter aPntr, Shape aShape)
{
    // If odd paint call, sleep for a moment to give debug paint a moment to register then do normal paint
    if(_pc%2==1) {
        try { Thread.sleep(30); } catch(Exception e) { }
        paintAll(aPntr); return;
    }
    
    // Fill paint bounds with yellow
    aPntr.setColor(Color.YELLOW); aPntr.fill(aShape);
    
    // Schedule repaint to do real paint
    ViewUtils.runLater(() -> getChild(0).repaint(aShape.getBounds()));
}

/**
 * Activate PaintLater.
 */
private final void activatePaintLater()  { if(_plater==null) getEnv().runLater(_plater = _platerShared); }

/**
 * Returns the EventDispatcher.
 */
public EventDispatcher getDispatcher()  { return _eventDispatcher; }

/**
 * Dispatch event.
 */
public void dispatchEvent(ViewEvent anEvent)  { _eventDispatcher.dispatchEvent(anEvent); }

/** Timing method: Returns animation start time. */
private void startTime()  { _time = System.currentTimeMillis(); } long _time;

/** Timing method: Returns animation stop time. */
private void stopTime()
{
    //System.arraycopy(_frames,1,_frames,0,_frames.length-1);
    //_frames[_frames.length-1] = System.currentTimeMillis() - _time;
    long time = System.currentTimeMillis(), dt = time - _time; _time = time;
    _frames[_pc%_frames.length] = dt;
}

/** Prints the time. */
private void printTime()
{
    long time = 0; for(int i=0;i<_frames.length;i++) time += _frames[i]; double avg = time/(double)_frames.length;
    System.out.println("FrameRate: " + (int)(1000/avg));
}

/**
 * An interface to listen to RootView events.
 */
public static interface Listener {
    
    /** Called before paint request. */
    public Rect rootViewWillPaint(RootView aRV, Rect aRect);
}
    
}