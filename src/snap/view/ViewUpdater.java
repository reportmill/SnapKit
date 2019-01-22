package snap.view;
import java.util.*;
import snap.gfx.*;

/**
 * A class to update view painting, layout and animation and ViewOwner resets.
 */
public class ViewUpdater {
    
    // The Window
    WindowView               _win;
    
    // The RootView
    RootView                 _rview;

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
    
    // The ViewUpdater.Lister that is notified on certain update actions
    ViewUpdater.Listener     _lsnr;
    
    // Whether currently painting
    boolean                  _painting;

    // Whether painting in debug mode
    static boolean           _debug = false; static int _pc; static long _frames[] = null;//new long[20];

/**
 * Creates a ViewUpdater.
 */
public ViewUpdater(RootView aRV)
{
    _rview = aRV;
    _win = aRV.getWindow();
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
 * Registers to do layout for views.
 */
public synchronized void relayoutViews()
{
    // If Painting, complain (nothing should change during paint)
    if(_painting)
        System.err.println("RootView.setNeedsLayoutDeep: Illegal view changes during paint.");
    activatePaintLater();
}

/**
 * Adds a view to set of Views that are being animated.
 */
public void startAnim(View aView)
{
    _animViews.add(aView);                     //System.out.println("Add Anim " + name(aView));
    if(_animViews.size()==1) _timer.start();
    ViewAnim anim = aView.getAnim(0); anim._updater = this;
    if(!anim.isSuspended() || anim.getStartTime()<0) anim.setStartTime(_timer.getTime());
}

/**
 * Removes a view to set of Views that are being animated.
 */
public void stopAnim(View aView)
{
    if(!_animViews.remove(aView)) return;
    if(_animViews.size()==0) _timer.stop();   //System.out.println("Remove Anim " + name(aView));
    ViewAnim anim = aView.getAnim(0); anim._updater = null;
}

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
            if(!av.isShowing()) anim.finish();
            else if(anim==null || anim.isSuspended()) stopAnim(av);
            else anim.setTime(time);
        }
    }

    // Send reset later calls
    while(_resetLaters.size()>0) {
        ViewOwner owners[] = _resetLaters.toArray(new ViewOwner[_resetLaters.size()]); _resetLaters.clear();
        for(ViewOwner no : owners) no.processResetUI();
    }
    
    // Layout all views that need it
    _rview.layoutDeep();

    // Calculate composite repaint rect from all dirty views/rects
    Rect rect = null; if(_dirtyRects.size()==0)  { _plater = null; return; }
    View views[] = _dirtyRects.keySet().toArray(new View[_dirtyRects.size()]);
    for(View view : views) { Rect r = _dirtyRects.get(view);
    
        // Constrain to ancestor clips
        r = view.getClippedRect(r); if(r.isEmpty()) continue;
        
        // Transform to root coords
        r = view.localToParent(r, _rview).getBounds();
        
        // Combine
        if(rect==null) rect = r;
        else rect.union(r);
    }
    
    // Round rect and constrain to root bounds
    if(rect==null) { _plater = null; return; }
    rect.snap(); if(rect.x<0) rect.x = 0; if(rect.y<0) rect.y = 0;
    if(rect.width>_rview.getWidth()) rect.width = _rview.getWidth();
    if(rect.height>_rview.getHeight()) rect.height = _rview.getHeight();
        
    // Notify listener
    if(_lsnr!=null) rect = _lsnr.rootViewWillPaint(_rview, rect);
    
    // Do repaint (in exception handler so we can reset things on failure)
    try {
        _painting = true;
        if(_win!=null && _win._helper!=null)
            _win._helper.requestPaint(rect);
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
    if(_rview.getFill()==null) aPntr.clearRect(aRect.x,aRect.y,aRect.width,aRect.height);
    
    // Paint views
    if(_debug) paintDebug(_rview, aPntr, aRect);
    else _rview.paintAll(aPntr);
    
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
        _rview.paintAll(aPntr); return;
    }
    
    // Fill paint bounds with yellow
    aPntr.setColor(Color.YELLOW); aPntr.fill(aShape);
    
    // Schedule repaint to do real paint
    ViewUtils.runLater(() -> _rview.getChild(0).repaint(aShape.getBounds()));
}

/**
 * Activate PaintLater.
 */
private final void activatePaintLater()  { if(_plater==null) ViewUtils.runLater(_plater = _platerShared); }

/**
 * Adds a ViewUpdater listener.
 */
public void addListener(ViewUpdater.Listener aLsnr)
{
    if(_lsnr!=null) System.err.println("ViewUpdater.addListener: Multiple listeners not yet supported");
    _lsnr = aLsnr;
}

/**
 * Removes a ViewUpdater listener.
 */
public void removeListener(ViewUpdater.Listener aLsnr)  { if(_lsnr==aLsnr) _lsnr = null; }

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