/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.ArrayUtils;
import snap.viewx.DevPane;

import static snap.view.ViewEvent.Type.*;

/**
 * A class to help RootView dispatch events to Views.
 */
public class EventDispatcher {
    
    // The Window
    private WindowView  _win;

    // The last mouse press point
    private double  _mpx, _mpy;

    // The list of shapes that are currently under the mouse (in "mouse over" state)
    protected List <View>  _mouseOvers = new ArrayList<>();
    
    // The top most view under the mouse
    protected View  _mouseOverView;
    
    // The view that received the last mouse press
    private View  _mousePressView;
    
    // The view that initiated the current drag/drop state
    private View  _dragSourceView;
    
    // The top most view under the current drag
    private View  _dragOverView;
     
    // A popup window, if one was added to root view during last event
    private PopupWindow  _popup;
    
    // A counter to track if user is requesting debug panel (hit control key 3 times)
    private long  _debugTrigger;

    // Whether mouse is currently down
    private static boolean  _mouseDown;

    // Whether mouse is currently in drag loop
    private static boolean  _mouseDrag;

    // The last MouseDown event
    private static ViewEvent  _lastMouseDown;

    // Constant to allow getTarget() to indicate short-circuit
    private static final View TRY_AGAIN_LATER_VIEW = new Separator();

    /**
     * Creates a new EventDispatcher for given Window.
     */
    public EventDispatcher(WindowView aWin)  { _win = aWin; }

    /**
     * Returns whether mouse is down.
     */
    public static boolean isMouseDown()  { return _mouseDown; }

    /**
     * Returns whether mouse is being dragged.
     */
    public static boolean isMouseDrag()  { return _mouseDrag; }

    /**
     * Returns the last MouseDown event.
     */
    public static ViewEvent getLastMouseDown()  { return _lastMouseDown; }

    /**
     * Sets the last MouseDown event.
     */
    private static void setLastMouseDown(ViewEvent anEvent)
    {
        // If mouse down is within time/dist range of last mouse down, bump click count
        if (_lastMouseDown!=null && anEvent.isClickCandidate())
            anEvent.setClickCount(_lastMouseDown.getClickCount()+1);

        // Set new event
        _lastMouseDown = anEvent;
        _mouseDown = true;
    }

    /**
     * Returns the popup window, if one was added to root view during last event.
     */
    public PopupWindow getPopup()  { return _popup; }

    /**
     * Sets the popup window, if one added to this root view during last event.
     */
    protected void setPopup(PopupWindow aPopup)
    {
        if (_popup!=null) _popup.hide();
        _popup = aPopup;
    }

    /**
     * Dispatch event.
     */
    public void dispatchEvent(ViewEvent anEvent)
    {
        // If popup window, forward to it
        if (_popup!=null) {
            if (anEvent.isMousePress()) {
                _popup.hide(); _popup = null; }
            else if (anEvent.isKeyPress() && anEvent.isEscapeKey())
                _popup.hide();
            if (_popup!=null && !_popup.isShowing())
                _popup = null;
        }

        // Dispatch Mouse events
        if (anEvent.isMouseEvent() || anEvent.isScroll())
            dispatchMouseEvent(anEvent);

        // Dispatch Key events
        else if (anEvent.isKeyEvent())
            dispatchKeyEvent(anEvent);

        // Dispatch DragTartget events
        else if (anEvent.isDragEvent())
            dispatchDragTargetEvent(anEvent);

        // Dispatch DragSource events
        else if (anEvent.isDragSourceEvent())
            dispatchDragSourceEvent(anEvent);

        // All other events just go to the view
        else anEvent.getView().fireEvent(anEvent);
    }

    /**
     * Dispatch Mouse event.
     */
    public void dispatchMouseEvent(ViewEvent anEvent)
    {
        // Update LastMouseDown
        if (anEvent.isMousePress())
            setLastMouseDown(anEvent);

        // Update MouseDrag
        else if (anEvent.isMouseDrag()) {
            if (_lastMouseDown==null) return;
            _mouseDrag = true;
            anEvent.setClickCount(getClickCount());
        }

        // Update MouseDown, MouseDrag
        else if (anEvent.isMouseRelease()) {
            if (_lastMouseDown==null) return; // Was: return ViewUtils.runLater(() -> dispatchMouseEvent(anEvent))
            _mouseDown = _mouseDrag = false;
            anEvent.setClickCount(getClickCount());
        }

        // Get DeepView for event and update MouseOvers for DeepView+Event
        View deepView = ViewUtils.getDeepestViewAt(_win.getRootView(), anEvent.getX(), anEvent.getY());
        View deepParents[] = getParents(deepView);
        updateMouseOvers(deepParents, anEvent);

        // Get target view (if bogus, just return)
        View targView = getTargetView(anEvent, deepView);
        if (targView==TRY_AGAIN_LATER_VIEW)
            return;

        // Get target parents
        View targParents[] = targView!=deepView ? getParents(targView) : deepParents;

        // Forward event to View EventFilters (iterate from root to target)
        for (View view : targParents) {
            if (view.getEventAdapter().isEnabled(anEvent.getType())) {
                ViewEvent e2 = anEvent.copyForView(view);
                view.processEventFilters(e2);
                if (e2.isConsumed())
                    return;
            }
        }

        // Forward event to View EventHandlers: (iterate from target to root)
        for (int i=targParents.length-1; i>=0; i--) { View view = targParents[i];
            if (view.getEventAdapter().isEnabled(anEvent.getType())) {
                ViewEvent e2 = anEvent.copyForView(view);
                view.processEventHandlers(e2);
                if (e2.isConsumed())
                    break;
            }
        }
    }

    /**
     * Returns the target view for deepest view and event.
     */
    private View getTargetView(ViewEvent anEvent, View deepView)
    {
        // Handle MousePress special: Update MousePressView, MousePressed-X/Y, and handle FocusWhenPressed
        if (anEvent.isMousePress()) {

            // Update MousePressed-X/Y
            _mpx = anEvent.getX();
            _mpy = anEvent.getY();

            // Don't know where this came from - should not be possible
            if (_lastMouseDown==null) {
                System.err.println("EventDispatcher.dispatchMouse: Impossible condition LastMouse==null");
                return TRY_AGAIN_LATER_VIEW;
            }

            // Update Focus if any targ parent not focused but wants focus
            for (View view=deepView; view!=null; view=view.getParent()) {

                // Update MousePressView
                _mousePressView = view;

                // If View focused, break
                if (view.isFocused())
                    break;

                // If View can be focused, focus and return
                if (view.isFocusWhenPressed()) {
                    view.requestFocus();
                    _lastMouseDown = null;
                    ViewUtils.runLater(() -> dispatchMouseEvent(anEvent));
                    return TRY_AGAIN_LATER_VIEW;
                }

                // If view accepts event, break
                EventAdapter eventAdapter = view.getEventAdapter();
                if (eventAdapter.isEnabled(MousePress) || eventAdapter.isEnabled(MouseRelease))
                    break;
            }

            // Return MousePressView (make sure it's at least the RootView)
            if (_mousePressView==null || _mousePressView instanceof WindowView)
                _mousePressView = _win.getRootView();

            // If MousePressView not focused, see if any parents want focus
            for (View view=_mousePressView; view!=null; view=view.getParent()) {
                if (view.isFocused())
                    break;
                if (view.isFocusWhenPressed()) {
                    view.requestFocus();
                    break;
                }
            }

            // Return MousePressView
            return _mousePressView;
        }

        // Handle other event types
        switch (anEvent.getType()) {

            // Handle Move, Enter, Scroll: Get deepest view
            case MouseMove: case MouseEnter: case Scroll:
                return deepView;

            // Handle Drag, Release: Get MousePressView
            case MouseDrag:
            case MouseRelease: {
                View targView = _mousePressView, rootView = _win.getRootView();
                if (targView.getRootView() != rootView)
                    targView = _mousePressView = ViewUtils.getDeepestViewAt(rootView, _mpx, _mpy);
                return targView;
            }

            // Handle Exit
            case MouseExit: return null;

            // Can't happen
            default: System.err.println("EventDispatcher.getTarget: Unknown type: " + anEvent.getType()); return null;
        }
    }

    /**
     * Update MouseOvers for new deepView/parents.
     */
    private void updateMouseOvers(View deepParents[], ViewEvent anEvent)
    {
        // Update MouseOvers: Remove views no longer under mouse and dispatch MouseExit events
        for (int i=_mouseOvers.size()-1; i>=0; i--) { View view = _mouseOvers.get(i);

            // Remove views no longer under mouse
            if (!ArrayUtils.containsId(deepParents, view)) {
                _mouseOvers.remove(i);
                _mouseOverView = i>0 ? _mouseOvers.get(i-1) : null;
                if (!view.getEventAdapter().isEnabled(MouseExit)) continue;

                // Dispatch MouseExit event
                ViewEvent e2 = ViewEvent.createEvent(view, anEvent.getEvent(), MouseExit, null);
                view.fireEvent(e2);
            }
            else break;
        }

        // Update MouseOvers: Add views now under mouse and dispatch MouseEnter events
        for (int i=_mouseOvers.size(); i<deepParents.length; i++) { View view = deepParents[i];
            _mouseOvers.add(view);
            _mouseOverView = view;
            if (!view.getEventAdapter().isEnabled(MouseEnter)) continue;

            // Dispatch MouseEnter event
            ViewEvent e2 = ViewEvent.createEvent(view, anEvent.getEvent(), MouseEnter, null);
            view.fireEvent(e2);
        }

        // Update CurrentCursor
        _win.resetActiveCursor();
    }

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

        // If CONTROL key is hit 3 times in a row, trigger DevPane
        if (anEvent.isKeyPress()) {
            if (anEvent.getKeyCode()==KeyCode.CONTROL)
                trackShowDevPane(anEvent);
            else _debugTrigger = 0;
        }

        // Get current focused view and array of parents
        View focusedView = _win.getFocusedView();
        if (focusedView==null) focusedView = _win.getContent(); // This is bogus
        View pars[] = getParents(focusedView);

        // Iterate down and see if any should filter
        for (View view : pars)
            if (view.getEventAdapter().isEnabled(anEvent.getType())) {
                ViewEvent e2 = anEvent.copyForView(view);
                view.processEventFilters(e2);
                if (e2.isConsumed()) return;
            }

        // If key pressed and tab and FocusedView.FocusKeysEnabled, switch focus
        if (anEvent.isKeyPress() && anEvent.isTabKey() && focusedView!=null && focusedView.isFocusKeysEnabled()) {
            View next = anEvent.isShiftDown() ? focusedView.getFocusPrev() : focusedView.getFocusNext();
            if (next!=null) { _win.requestFocus(next); return; }
        }

        // Iterate back up and see if any parents should handle
        for (int i=pars.length-1;i>=0;i--) { View view = pars[i];
            if (view.getEventAdapter().isEnabled(anEvent.getType())) {
                ViewEvent event = anEvent.copyForView(view);
                view.processEventHandlers(event);
                if (event.isConsumed())
                    return;
            }
        }
    }

    /**
     * Dispatch drag gesture event.
     */
    public void dispatchDragSourceEvent(ViewEvent anEvent)
    {
        // Handle DragGesture
        if (anEvent.isDragGesture()) {
            for (View view=_mousePressView;view!=null;view=view.getParent())
                if (view.getEventAdapter().isEnabled(DragGesture)) { _dragSourceView = view;
                    ViewEvent event = anEvent.copyForView(view);
                    view.fireEvent(event);
                    if (event.isConsumed()) break;
                }
        }

        // Handle DragSource
        else if (_dragSourceView!=null) {
            if (_dragSourceView.getEventAdapter().isEnabled(anEvent.getType())) {
                ViewEvent event = anEvent.copyForView(_dragSourceView);
                _dragSourceView.fireEvent(event);
            }
            if (anEvent.isDragSourceEnd())
                _dragSourceView = null;
        }
    }

    /**
     * Dispatch drag target event.
     */
    public void dispatchDragTargetEvent(ViewEvent anEvent)
    {
        // Get target view and parents
        View rview = _win.getRootView();
        View targ = ViewUtils.getDeepestViewAt(rview, anEvent.getX(), anEvent.getY());
        View pars[] = getParents(targ);

        // Remove old DragOver views and dispatch appropriate MouseExited events
        for (View view=_dragOverView;view!=null;view=view.getParent()) {
            if (!ArrayUtils.containsId(pars,view)) {
                _dragOverView = view.getParent();
                if (!view.getEventAdapter().isEnabled(DragExit)) continue;
                ViewEvent e2 = anEvent.copyForView(view);
                e2._type = DragExit;
                view.fireEvent(e2);
                if (e2.isConsumed()) anEvent.consume();
            }
            else break;
        }

        // Add new DragOver views and dispatch appropriate MouseEntered events
        int start = getParentCount(_dragOverView!=null ? _dragOverView : rview);
        for (int i=start;i<pars.length;i++) { View view = pars[i];
            _dragOverView = view;
            if (!view.getEventAdapter().isEnabled(DragEnter)) continue;
            ViewEvent e2 = anEvent.copyForView(view);
            e2._type = DragEnter;
            view.fireEvent(e2);
            if (e2.isConsumed()) anEvent.consume();
        }

        // Handle DragOver, DragDrop
        if (anEvent.isDragOver() || anEvent.isDragDrop()) {
            for (View view=_dragOverView; view!=null; view=view.getParent()) {
                if (view.getEventAdapter().isEnabled(anEvent.getType())) {
                    ViewEvent e2 = anEvent.copyForView(view);
                    view.fireEvent(e2);
                    if (e2.isConsumed()) anEvent.consume();
                    break;
                }
            }
        }
    }

    /**
     * Dispatch MouseMove event to trigger exit.
     */
    public void dispatchMouseMoveOutsideWindow()
    {
        ViewEvent event = ViewEvent.createEvent(_win, null, MouseMove, null);
        event = event.copyForViewPoint(_win, _win.getWidth()+100, 0, 0);
        dispatchEvent(event);
    }

    /**
     * Returns whether given view is current under the mouse.
     */
    public boolean isMouseOver(View aView)
    {
        for (View v : _mouseOvers) if (v==aView) return true;
        return false;
    }

    /**
     * Returns whether given view is currently in mouse press.
     */
    public boolean isMouseDown(View aView)  { return _mousePressView==aView && ViewUtils.isMouseDown(); }

    /** Returns the number of parents of given view including RootView. */
    private int getParentCount(View aView)
    {
        if (aView==null) return 0;
        View rview = _win.getRootView();
        int pc = 1; for (View v=aView; v!=rview; v=v.getParent()) pc++;
        return pc;
    }

    /** Returns array of parents of given view up to and including RootView. */
    private View[] getParents(View aView)
    {
        int pc = getParentCount(aView);
        View pars[] = new View[pc]; if (pc==0) return pars;
        View rview = _win.getRootView();
        for (View v=aView; v!=rview; v=v.getParent()) pars[--pc] = v; pars[0] = rview;
        return pars;
    }

    /**
     * Tracks whether user is requesting DevPane.
     */
    private void trackShowDevPane(ViewEvent anEvent)
    {
        final int SHOW_DEV_PANE_CONTROL_KEY_DOUBLE_CLICK_MIN_TIME = 500;
        long time = System.currentTimeMillis();
        if (time - _debugTrigger < SHOW_DEV_PANE_CONTROL_KEY_DOUBLE_CLICK_MIN_TIME) {
            DevPane.setDevPaneShowing(anEvent.getView(), !DevPane.isDevPaneShowing(anEvent.getView()));
            _debugTrigger = 0;
        }
        else _debugTrigger = time;
    }

    // Returns the current click count
    private static int getClickCount()  { return ViewUtils.getMouseDown().getClickCount(); }
}