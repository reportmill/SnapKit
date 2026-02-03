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
    private RootView _rootView;

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
    private ViewTimer  _timer = new ViewTimer(this::updateLater, 25);
    
    // The ViewUpdater.Lister that is notified on certain update actions
    private ViewUpdater.Listener  _lsnr;
    
    // Whether currently painting
    private boolean  _painting;

    // Whether painting in debug mode
    protected static boolean _paintDebug = false;
    private static boolean _clearFlash;
    private static boolean _paintFrameRateText = false;
    private long _frameStartTime;
    private static int _frameCount;
    protected static long[]  _frames = null; //new long[20];

    /**
     * Creates a ViewUpdater.
     */
    public ViewUpdater(WindowView aWin)
    {
        _win = aWin;
        _rootView = _win.getRootView();
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
    protected final void updateLater()
    {
        if (_updateRun == null)
            ViewUtils.runLater(_updateRun = _updateRunShared);
    }

    /**
     * Main update method: Updates these view things:
     *   - View animation
     *   - ViewOwner resetUI calls
     *   - View layout
     *   - View painting.
     */
    protected synchronized void updateViews()
    {
        _updateRun = null;

        // Send RunBefore calls
        while (!_runBefores.isEmpty()) {
            Runnable[] runs = _runBefores.toArray(new Runnable[0]);
            _runBefores.clear();
            for (Runnable run : runs)
                run.run();
        }

        // If timer is running, send Anim calls
        if (_timer.isRunning()) {

            // Get anims array and current timer time
            ViewAnim[] anims = _viewAnims.toArray(new ViewAnim[0]);
            int time = _timer.getTime();

            // Iterate over anims and update time
            for (ViewAnim anim : anims) {

                // If view isn't showing, just finish (should get suspended instead)
                View view = anim.getView();
                if (!view.isShowing())
                    anim.finish();

                // Update anim time
                else anim.setTime(time - anim._startTime);
            }
        }

        // Send reset later calls
        while (!_resetLaters.isEmpty()) {

            // Get resetLater owners
            ViewOwner[] owners = _resetLaters.toArray(new ViewOwner[0]);
            _resetLaters.clear();
            for (ViewOwner owner : owners)
                owner.invokeResetUI();
        }

        // Layout all views that need it
        _rootView.layoutDeep();

        // Get composite repaint rect from all repaint views
        Rect rect = getRepaintRect();
        if (rect == null)
            return;

        // Do repaint (in exception handler so we can reset things on failure)
        try {
            _painting = true;
            if (_win != null && _win._helper != null)
                _win._helper.requestPaint(rect);
        }

        // Clear RepaintViews, reset runnable, update PaintCount and set Painting false
        finally {
            for (View v : _repaintViews)
                v._repaintRect = null;
            _repaintViews.clear();
            _painting = false;

            // If ClearFlash, register for proper repaint to clear highlight
            if (_clearFlash)
                ViewUtils.runDelayed(() -> _rootView.repaint(rect), 10);
        }
    }

    /**
     * Paint views.
     */
    public synchronized void paintViews(Painter aPntr, Rect aRect)
    {
        // Save painter state
        aPntr.save();

        // Exception handler so we make sure we always restore
        try {

            // If calculating frame rate, call startTime
            if (_frames != null && !_paintFrameRateText)
                _frameStartTime = System.currentTimeMillis();

            // Clip to rect, clear background
            aPntr.clip(aRect);
            if (_rootView.getFill() == null)
                aPntr.clearRect(aRect.x, aRect.y, aRect.width, aRect.height);

            // Paint views
            if (_paintFrameRateText)
                paintFrameRate(aPntr);
            else if (_paintDebug)
                paintDebug(aPntr, aRect);
            else _rootView.paintAll(aPntr);

            // If paint was called outside of paintLater (maybe Window.show() or resize), repaint all
            if (!_painting && !_paintFrameRateText) {
                for (View v : _repaintViews)
                    v._repaintRect = null;
                _repaintViews.clear();
                _rootView.repaint();
            }
        }

        // Restore painter state
        finally {
            aPntr.restore();

            // Handle PaintFrameRate
            if (_paintFrameRateText)
                _paintFrameRateText = false;
            else if (_frames != null) {

                // Add current frame to set
                long frameRenderTime = Math.max(System.currentTimeMillis() - _frameStartTime, 1);
                _frames[_frameCount % _frames.length] = frameRenderTime;
                _frameCount++;

                // Register to paint new average frame rate
                _paintFrameRateText = true;
                ViewUtils.runLater(() -> _win._helper.requestPaint(getFrameRateTextRect()));
            }
        }
    }

    /**
     * Do debug paint: Paints a yellow flash and registers for a later paint to clear flash.
     */
    protected void paintDebug(Painter aPntr, Shape aShape)
    {
        // If ClearFlash, pause for a moment, paint and return
        if (_clearFlash) {
            _rootView.paintAll(aPntr);
            _clearFlash = false;
            return;
        }

        // Fill paint bounds with yellow
        aPntr.setColor(Color.YELLOW);
        aPntr.fill(aShape);

        // Set ClearFlash to trigger repaint to clear highlight
        _clearFlash = true;
    }

    /**
     * Returns the current repaint rect combined rects of RepaintViews.RepaintRects.
     */
    public Rect getRepaintRect()
    {
        // Get array of RepaintViews (just return if none)
        int count = _repaintViews.size(); if (count == 0)  return null;
        View[] views = _repaintViews.toArray(new View[count]);

        // Iterate over RepaintViews to calculate composite repaint rect from all views
        Rect totalRect = null;
        for (View view : views) {

            // If view no longer in hierarchy or has no Repaint rect, just continue
            if (view.getRootView() != _rootView || !view.isVisible())
                continue;

            // Get view repaint rect - just continue if not set
            Rect viewPaintRect = view.getRepaintRect();
            if (viewPaintRect == null)
                continue;

            // Get view paint rect cliped to any ancestor view clips
            Rect viewPaintRectClipped = viewPaintRect;
            Rect clipBoundsAll = view.getClipBoundsAll();
            if (clipBoundsAll != null) {
                viewPaintRectClipped = viewPaintRectClipped.getIntersectRect(clipBoundsAll);
                viewPaintRectClipped.snap();
            }

            // If view paint rect is empty just continue
            if (viewPaintRectClipped.isEmpty())
                continue;

            // Transform to root coords
            if (view != _rootView)
                viewPaintRectClipped = view.localToParent(viewPaintRectClipped, _rootView).getBounds();

            // Combine
            if (totalRect == null)
                totalRect = viewPaintRectClipped;
            else totalRect.union(viewPaintRectClipped);
        }

        // Round rect and constrain to root bounds
        if (totalRect == null)
            return null;
        totalRect.snap();
        if (totalRect.x < 0)
            totalRect.x = 0;
        if (totalRect.y < 0)
            totalRect.y = 0;
        if (totalRect.width > _rootView.getWidth())
            totalRect.width = _rootView.getWidth();
        if (totalRect.height > _rootView.getHeight())
            totalRect.height = _rootView.getHeight();

        // Give listener a chance to modify rect
        if (_lsnr != null)
            totalRect = _lsnr.updaterWillPaint(_rootView, totalRect);

        // Return rect
        return totalRect;
    }

    /**
     * Adds given ViewAnim to set of anims that are running.
     */
    public void startAnim(ViewAnim anAnim)
    {
        // Add anim to ViewAnims and start timer (if first anim)
        _viewAnims.add(anAnim);
        if (_viewAnims.size() == 1)
            _timer.start();

        // Record Anim.StartTime, so we can always set View.Anim.Time relative to start
        anAnim._startTime = _timer.getTime() - anAnim.getTime();
    }

    /**
     * Removes given ViewAnim to set of anims that are running.
     */
    public void stopAnim(ViewAnim anAnim)
    {
        if (!_viewAnims.remove(anAnim))
            return;
        if (_viewAnims.isEmpty())
            _timer.stop();
    }

    /**
     * Returns whether view is animating or inside animating view.
     */
    public boolean isViewAnimating(View aView)
    {
        for (ViewAnim viewAnim : _viewAnims) {
            View view = viewAnim.getView();
            if (aView == view || aView.isAncestor(view))
                return true;
        }
        return false;
    }

    /**
     * Adds a ViewUpdater listener.
     */
    public void addListener(ViewUpdater.Listener aLsnr)
    {
        if (_lsnr != null)
            System.err.println("ViewUpdater.addListener: Multiple listeners not yet supported");
        _lsnr = aLsnr;
    }

    /**
     * Removes a ViewUpdater listener.
     */
    public void removeListener(ViewUpdater.Listener aLsnr)
    {
        if (_lsnr == aLsnr)
            _lsnr = null;
    }

    /**
     * Returns whether painting is debug.
     */
    public static boolean isPaintDebug()  { return _paintDebug; }

    /**
     * Set whether painting is debug.
     */
    public static void setPaintDebug(boolean aValue)
    {
        _paintDebug = aValue;
    }

    /**
     * Returns whether to show frame rate.
     */
    public static boolean isShowFrameRate()  { return _frames != null; }

    /**
     * Set whether to show frame rate.
     */
    public static void setShowFrameRate(boolean aValue)
    {
        _frames = aValue ? new long[10] : null;
    }

    /**
     * Paints frame rate label.
     */
    private void paintFrameRate(Painter aPntr)
    {
        // Calculate average frame rate
        long totalFramesTime = 0; for (long frame : _frames) totalFramesTime += frame;
        double averageFrameTime = totalFramesTime / (double) _frames.length;
        int averageFrameRate = (int) Math.round(1000 / averageFrameTime);

        // Paint fps in lower right corner
        Rect rect = getFrameRateTextRect();
        aPntr.setPaint(Color.PINK);
        aPntr.fill(rect);
        aPntr.setPaint(Color.BLACK);
        aPntr.setFont(Font.Arial11);
        aPntr.drawString(averageFrameRate + " fps", rect.x + 8, rect.y + 12);
    }

    /**
     * Returns the rect to paint frame rate label.
     */
    private Rect getFrameRateTextRect()
    {
        double rectX = _rootView.getWidth() - 50;
        double rectY = _rootView.getHeight() - 20;
        return new Rect(rectX, rectY, 48, 18);
    }

    /**
     * An interface to listen to ViewUpdater events.
     */
    public interface Listener {

        /** Called before paint request. */
        Rect updaterWillPaint(RootView aRV, Rect aRect);
    }
}