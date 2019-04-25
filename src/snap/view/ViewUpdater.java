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

    // The set of views that have requested repaint
    Set <View>               _repaintViews = new HashSet();
    
    // PaintLater runnable
    Runnable                 _updateRun, _updateRunShared = () -> updateViews();
    
    // A set of ViewOwners that want to be reset on next UI update call
    Set <ViewOwner>          _resetLaters = Collections.synchronizedSet(new HashSet());
    
    // A set of Views with active animations
    Set <View>               _animViews = Collections.synchronizedSet(new HashSet());
    
    // The timer for animated views
    ViewTimer                _timer = new ViewTimer(25, t -> updateLater());
    
    // The ViewUpdater.Lister that is notified on certain update actions
    ViewUpdater.Listener     _lsnr;
    
    // Whether currently painting
    boolean                  _painting;

    // Whether painting in debug mode
    static boolean           _debug = false; static int _pc; static long _frames[] = null;//new long[20];
    Rect                     _debugRepaintRect;

/**
 * Creates a ViewUpdater.
 */
public ViewUpdater(WindowView aWin)
{
    _win = aWin;
    _rview = _win.getRootView();
}

/**
 * Adds a given ViewOwner to set of owners that need reset on next UI update call.
 */
public void resetLater(ViewOwner anOwnr)
{
    _resetLaters.add(anOwnr);
    updateLater();
}

/**
 * Registers to do layout for views.
 */
public synchronized void relayoutLater()
{
    // If Painting, complain (nothing should change during paint)
    if(_painting)
        System.err.println("ViewUpdater.relayoutLater: Illegal view changes during paint.");
    updateLater();
}

/**
 * Called to register a view for repaint.
 */
public synchronized void repaintLater(View aView)
{
    // If Painting, complaint (nothing should change during paint)
    if(_painting)
        System.err.println("ViewUpdater.repaintLater: Illegal repaint call during paint.");

    // Register for updateLater and add view to RepaintViews
    updateLater();
    _repaintViews.add(aView);
}

/**
 * Register call to update via runLater.
 */
protected final void updateLater()  { if(_updateRun==null) ViewUtils.runLater(_updateRun = _updateRunShared); }

/**
 * Main update method: Updates these view things:
 *   - View animation
 *   - ViewOwner resetUI calls
 *   - View layout
 *   - View painting.
 */
protected synchronized void updateViews()
{
    // If timer is running, send Anim calls
    if(_timer.isRunning()) {
        View aviews[] = _animViews.toArray(new View[0]); int time = _timer.getTime();
        for(View av : aviews) { ViewAnim anim = av.getAnim(-1);
            if(!av.isShowing()) anim.finish();
            else if(anim==null) stopAnim(av);
            else anim.setTime(time - anim._startTime);
        }
    }

    // Send reset later calls
    while(_resetLaters.size()>0) {
        ViewOwner owners[] = _resetLaters.toArray(new ViewOwner[_resetLaters.size()]); _resetLaters.clear();
        for(ViewOwner no : owners) no.processResetUI();
    }
    
    // Layout all views that need it
    _rview.layoutDeep();

    // Get composite repaint rect from all repaint views
    Rect rect = getRepaintRect(); if(rect==null) { _updateRun = null; return; }
        
    // Do repaint (in exception handler so we can reset things on failure)
    try {
        _painting = true;
        if(_win!=null && _win._helper!=null)
            _win._helper.requestPaint(rect);
    }
    
    // Clear RepaintViews, reset runnable, update PaintCount and set Painting false
    finally { _repaintViews.clear(); _updateRun = null; _pc++; _painting = false; }
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
    
    // If paint was called outside of paintLater (maybe Window.show() or resize), repaint all
    if(!_painting) { //System.out.println("ViewUpdater: Repaint not from paintLater");
        _repaintViews.clear();
        _rview.repaint();
    }
}

/**
 * Do debug paint.
 */
protected void paintDebug(View aView, Painter aPntr, Shape aShape)
{
    // If odd paint call, sleep for a moment to give debug paint a moment to register then do normal paint
    if(_debugRepaintRect!=null) {
        try { Thread.sleep(30); } catch(Exception e) { }
        _rview.paintAll(aPntr); _debugRepaintRect = null; return;
    }
    
    // Fill paint bounds with yellow
    aPntr.setColor(Color.YELLOW); aPntr.fill(aShape);
    
    // Schedule repaint to do real paint
    Rect rect = _debugRepaintRect = aShape.getBounds();
    ViewUtils.runLater(() -> _rview.repaint(rect));
}

/**
 * Returns the current repaint rect combined rects of RepaintViews.RepaintRects.
 */
public Rect getRepaintRect()
{
    // Get array of RepaintViews (just return if none)
    int count = _repaintViews.size(); if(count==0)  return null;
    View views[] = _repaintViews.toArray(new View[count]);

    // Iterate over RepaintViews to calculate composite repaint rect from all views
    Rect rect = null;
    for(View view : views) {
    
        // If view no longer in hierarchy or has no Repaint rect, just continue
        if(view.getRootView()!=_rview) continue;
        Rect r = view.getRepaintRect(); if(r==null) continue;
        view._repaintRect = null;
    
        // Constrain to ancestor clips
        r = view.getClippedRect(r); if(r.isEmpty()) continue;
        
        // Transform to root coords
        if(view!=_rview)
            r = view.localToParent(r, _rview).getBounds();
        
        // Combine
        if(rect==null) rect = r;
        else rect.union(r);
    }
    
    // Round rect and constrain to root bounds
    if(rect==null) return null;
    rect.snap(); if(rect.x<0) rect.x = 0; if(rect.y<0) rect.y = 0;
    if(rect.width>_rview.getWidth()) rect.width = _rview.getWidth();
    if(rect.height>_rview.getHeight()) rect.height = _rview.getHeight();
        
    // Give listener a chance to modify rect
    if(_lsnr!=null) rect = _lsnr.updaterWillPaint(_rview, rect);
    return rect;
}

/**
 * Adds a view to set of Views that are being animated.
 */
public void startAnim(View aView)
{
    // Add view to AnimViews and start timer (if first AnimView)
    _animViews.add(aView);
    if(_animViews.size()==1) _timer.start();
    
    // Record Anim.StartTime, so we can always set View.Anim.Time relative to start
    ViewAnim anim = aView.getAnim(0);
    anim._startTime = _timer.getTime() - anim.getTime();
}

/**
 * Removes a view to set of Views that are being animated.
 */
public void stopAnim(View aView)
{
    if(!_animViews.remove(aView)) return;
    if(_animViews.size()==0) _timer.stop();
    ViewAnim anim = aView.getAnim(0);
}

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
 * An interface to listen to ViewUpdater events.
 */
public static interface Listener {
    
    /** Called before paint request. */
    public Rect updaterWillPaint(RootView aRV, Rect aRect);
}
    
}