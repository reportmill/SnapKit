package snap.view;
import java.util.*;

import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.*;

/**
 * A class to update view painting, layout and animation and ViewOwner resets.
 */
public class ViewUpdater {
    
    // The Window
    private WindowView  _win;
    
    // The RootView
    private RootView  _rview;

    // A set of Runnables to be called at beginning of update
    private Set <Runnable>  _runBefores = Collections.synchronizedSet(new HashSet<>());

    // A set of ViewAnims with active animations
    private Set <ViewAnim>  _viewAnims = Collections.synchronizedSet(new HashSet<>());

    // A set of ViewOwners that want to be reset on next UI update call
    private Set <ViewOwner>  _resetLaters = Collections.synchronizedSet(new HashSet<>());

    // The set of views that have requested repaint
    private Set <View>  _repaintViews = new HashSet<>();

    // PaintLater runnable
    private Runnable  _updateRun;

    // PaintLater runnable (reusable)
    private Runnable  _updateRunShared = () -> updateViews();

    // The timer for animated views
    private ViewTimer  _timer = new ViewTimer(25, t -> updateLater());
    
    // The ViewUpdater.Lister that is notified on certain update actions
    private ViewUpdater.Listener  _lsnr;
    
    // Whether currently painting
    private boolean  _painting;

    // Whether painting in debug mode
    public static boolean  _debug = false;
    private static int  _pc;
    protected static long  _frames[] = null; //new long[20];
    private Rect  _debugRepaintRect;

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
    public void runBeforeUpdate(Runnable aRun)
    {
        _runBefores.add(aRun);
        updateLater();
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
        if (_painting)
            System.err.println("ViewUpdater.relayoutLater: Illegal view changes during paint.");
        updateLater();
    }

    /**
     * Called to register a view for repaint.
     */
    public synchronized void repaintLater(View aView)
    {
        // If Painting, complaint (nothing should change during paint)
        if (_painting)
            System.err.println("ViewUpdater.repaintLater: Illegal repaint call during paint.");

        // Register for updateLater and add view to RepaintViews
        updateLater();
        _repaintViews.add(aView);
    }

    /**
     * Register call to update via runLater.
     */
    protected final void updateLater()  { if (_updateRun==null) ViewUtils.runLater(_updateRun = _updateRunShared); }

    /**
     * Main update method: Updates these view things:
     *   - View animation
     *   - ViewOwner resetUI calls
     *   - View layout
     *   - View painting.
     */
    protected synchronized void updateViews()
    {
        // Send RunBefore calls
        while (_runBefores.size()>0) {
            Runnable runs[] = _runBefores.toArray(new Runnable[0]);
            _runBefores.clear();
            for (Runnable run : runs)
                run.run();
        }

        // If timer is running, send Anim calls
        if (_timer.isRunning()) {

            // Get anims array and current timer time
            ViewAnim anims[] = _viewAnims.toArray(new ViewAnim[0]);
            int time = _timer.getTime();

            // Iterate over anims and update time
            for (ViewAnim anim : anims) {

                // If view isn't showing, just finish (should get suspended instead)
                View view = anim.getView();
                if (!view.isShowing()) anim.finish();

                // Update anim time
                else anim.setTime(time - anim._startTime);
            }
        }

        // Send reset later calls
        while (_resetLaters.size()>0) {
            ViewOwner owners[] = _resetLaters.toArray(new ViewOwner[0]);
            _resetLaters.clear();
            for (ViewOwner owner : owners)
                owner.processResetUI();
        }

        // Layout all views that need it
        _rview.layoutDeep();

        // Get composite repaint rect from all repaint views
        Rect rect = getRepaintRect(); if (rect==null) { _updateRun = null; return; }

        // Do repaint (in exception handler so we can reset things on failure)
        try {
            _painting = true;
            if (_win!=null && _win._helper!=null)
                _win._helper.requestPaint(rect);
        }

        // Clear RepaintViews, reset runnable, update PaintCount and set Painting false
        finally {
            for (View v : _repaintViews) v._repaintRect = null;
            _repaintViews.clear();
            _updateRun = null; _pc++; _painting = false;
        }
    }

    /**
     * Paint views.
     */
    public synchronized void paintViews(Painter aPntr, Rect aRect)
    {
        // Save painter state, clip to rect, clear background
        aPntr.save(); if (_frames!=null) startTime();
        aPntr.clip(aRect);
        if (_rview.getFill()==null) aPntr.clearRect(aRect.x,aRect.y,aRect.width,aRect.height);

        // Paint views
        if (_debug) paintDebug(_rview, aPntr, aRect);
        else _rview.paintAll(aPntr);

        // Restore painter state and update frame counts
        aPntr.restore(); if (_frames!=null) { stopTime(); if (_pc%20==0) printTime(); }

        // If paint was called outside of paintLater (maybe Window.show() or resize), repaint all
        if (!_painting) { //System.out.println("ViewUpdater: Repaint not from paintLater");
            for (View v : _repaintViews) v._repaintRect = null; _repaintViews.clear();
            _rview.repaint();
        }
    }

    /**
     * Do debug paint.
     */
    protected void paintDebug(View aView, Painter aPntr, Shape aShape)
    {
        // If odd paint call, sleep for a moment to give debug paint a moment to register then do normal paint
        if (_debugRepaintRect!=null) {
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
        int count = _repaintViews.size(); if (count==0)  return null;
        View views[] = _repaintViews.toArray(new View[count]);

        // Iterate over RepaintViews to calculate composite repaint rect from all views
        Rect rect = null;
        for (View view : views) {

            // If view no longer in hierarchy or has no Repaint rect, just continue
            if (view.getRootView()!=_rview) continue;
            Rect r = view.getRepaintRect(); if (r==null) continue;

            // Constrain to ancestor clips
            r = view.getClippedRect(r); if (r.isEmpty()) continue;

            // Transform to root coords
            if (view!=_rview)
                r = view.localToParent(r, _rview).getBounds();

            // Combine
            if (rect==null) rect = r;
            else rect.union(r);
        }

        // Round rect and constrain to root bounds
        if (rect==null) return null;
        rect.snap(); if (rect.x<0) rect.x = 0; if (rect.y<0) rect.y = 0;
        if (rect.width>_rview.getWidth()) rect.width = _rview.getWidth();
        if (rect.height>_rview.getHeight()) rect.height = _rview.getHeight();

        // Give listener a chance to modify rect
        if (_lsnr!=null) rect = _lsnr.updaterWillPaint(_rview, rect);
        return rect;
    }

    /**
     * Adds given ViewAnim to set of anims that are running.
     */
    public void startAnim(ViewAnim anAnim)
    {
        // Add anim to ViewAnims and start timer (if first anim)
        _viewAnims.add(anAnim);
        if (_viewAnims.size()==1) _timer.start();

        // Record Anim.StartTime, so we can always set View.Anim.Time relative to start
        anAnim._startTime = _timer.getTime() - anAnim.getTime();
    }

    /**
     * Removes given ViewAnim to set of anims that are running.
     */
    public void stopAnim(ViewAnim anAnim)
    {
        if (!_viewAnims.remove(anAnim)) return;
        if (_viewAnims.size()==0) _timer.stop();
    }

    /**
     * Adds a ViewUpdater listener.
     */
    public void addListener(ViewUpdater.Listener aLsnr)
    {
        if (_lsnr!=null) System.err.println("ViewUpdater.addListener: Multiple listeners not yet supported");
        _lsnr = aLsnr;
    }

    /**
     * Removes a ViewUpdater listener.
     */
    public void removeListener(ViewUpdater.Listener aLsnr)  { if (_lsnr==aLsnr) _lsnr = null; }

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
        long time = 0; for (long frame : _frames) time += frame;
        double avg = time/(double)_frames.length;
        System.out.println("FrameRate: " + (int)(1000/avg));
    }

    /**
     * Returns whether painting is debug.
     */
    public static boolean isDebug()  { return _debug; }

    /**
     * Set whether painting is debug.
     */
    public static void setDebug(boolean aValue)  { _debug = aValue; }

    /**
     * An interface to listen to ViewUpdater events.
     */
    public interface Listener {

        /** Called before paint request. */
        Rect updaterWillPaint(RootView aRV, Rect aRect);
    }
}