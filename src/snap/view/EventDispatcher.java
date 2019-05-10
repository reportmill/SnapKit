/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.util.ArrayUtils;
import static snap.view.ViewEvent.Type.*;

/**
 * A class to help RootView dispatch events to Views.
 */
public class EventDispatcher {
    
    // The Window
    WindowView             _win;

     // The last mouse press point
     double                _mpx, _mpy;

    // The list of shapes that are currently under the mouse (in "mouse over" state)
    List <View>            _mouseOvers = new ArrayList();
    
    // The top most view under the mouse
    View                   _mouseOverView;
    
    // The view that received the last mouse press
    View                   _mousePressView;
    
    // The view that initiated the current drag/drop state
    View                   _dragSourceView;
    
    // The top most view under the current drag
    View                   _dragOverView;
     
    // A popup window, if one was added to root view during last event
    PopupWindow            _popup;
    
    // A buffer to hold debug keys (after "snp" sequence is typed)
    StringBuilder          _debugKeys = new StringBuilder();

/**
 * Creates a new EventDispatcher for given RootView.
 */
public EventDispatcher(WindowView aWin)  { _win = aWin; }

/**
 * Returns the popup window, if one was added to root view during last event.
 */
public PopupWindow getPopup()  { return _popup; }

/**
 * Sets the popup window, if one added to this root view during last event.
 */
protected void setPopup(PopupWindow aPopup)
{
    if(_popup!=null) _popup.hide();
    _popup = aPopup;
}

/**
 * Dispatch event.
 */
public void dispatchEvent(ViewEvent anEvent)
{
    // If popup window, forward to it
    if(_popup!=null) {
        if(anEvent.isMousePress()) {
            _popup.hide(); _popup = null; }
        else if(anEvent.isKeyPress() && anEvent.isEscapeKey())
            _popup.hide();
        if(_popup!=null && !_popup.isShowing())
            _popup = null;
    }
    
    // Dispatch Mouse events
    if(anEvent.isMouseEvent() || anEvent.isScroll())
        dispatchMouseEvent(anEvent);
        
    // Dispatch Key events
    else if(anEvent.isKeyEvent())
        dispatchKeyEvent(anEvent);
        
    // Dispatch DragTartget events
    else if(anEvent.isDragEvent())
        dispatchDragTargetEvent(anEvent);
        
    // Dispatch DragSource events
    else if(anEvent.isDragSourceEvent())
        dispatchDragSourceEvent(anEvent);
        
    // All other events just go to the view
    else anEvent.getView().fireEvent(anEvent);
}

/**
 * Dispatch Mouse event.
 */
public void dispatchMouseEvent(ViewEvent anEvent)
{
    // Update ViewEnv.MouseDown
    if(anEvent.isMousePress()) ViewUtils.setMouseDown(anEvent);
    else if(anEvent.isMouseDrag()) {
        if(ViewUtils._lastMouseDown==null) return;
        ViewUtils._mouseDrag = true; anEvent.setClickCount(getClickCount());
    }
    else if(anEvent.isMouseRelease()) {
        if(ViewUtils._lastMouseDown==null) { ViewUtils.runLater(() -> dispatchMouseEvent(anEvent)); return; }
        ViewUtils._mouseDown = ViewUtils._mouseDrag = false;
        anEvent.setClickCount(getClickCount());
    }

    // Get target view (at mouse point, or mouse press, or mouse press point)
    View rview = _win.getRootView();
    View targ = ViewUtils.getDeepestViewAt(rview, anEvent.getX(), anEvent.getY());
    if(anEvent.isMouseExit()) targ = null;
    if(anEvent.isMouseDrag() || anEvent.isMouseRelease()) {
        targ = _mousePressView;
        if(targ.getRootView()!=rview)
            targ = _mousePressView = ViewUtils.getDeepestViewAt(rview, _mpx, _mpy);
    }
    
    // Get target parents
    View pars[] = getParents(targ);
    
    // Update MouseOvers: Remove views no longer under mouse and dispatch MouseExit events
    for(int i=_mouseOvers.size()-1;i>=0;i--) { View view = _mouseOvers.get(i);
         if(!ArrayUtils.containsId(pars,view)) {
             _mouseOvers.remove(i); _mouseOverView = i>0? _mouseOvers.get(i-1) : null;
            if(!view.getEventAdapter().isEnabled(MouseExit)) continue;
             ViewEvent e2 = ViewEvent.createEvent(view, anEvent.getEvent(), MouseExit, null);
             view.fireEvent(e2);
         }
         else break;
    }
    
    // Update MouseOvers: Add views now under mouse and dispatch MouseEnter events
    for(int i=_mouseOvers.size();i<pars.length;i++) { View view = pars[i];
        _mouseOvers.add(view); _mouseOverView = view;
        if(!view.getEventAdapter().isEnabled(MouseEnter)) continue;
         ViewEvent e2 = ViewEvent.createEvent(view, anEvent.getEvent(), MouseEnter, null);
         view.fireEvent(e2);
    }
    
    // Update CurrentCursor
    _win.resetActiveCursor();
    
    // Handle MousePress: Update MousePressView and mouse pressed point
    if(anEvent.isMousePress()) {
        _mousePressView = targ; _mpx = anEvent.getX(); _mpy = anEvent.getY();
        for(View v=targ;v!=null;v=v.getParent())
            if(v.isFocusWhenPressed() && !v.isFocused() && ViewUtils._lastMouseDown!=null) {
                v.requestFocus(); ViewUtils._lastMouseDown = null;
                ViewUtils.runLater(() -> dispatchMouseEvent(anEvent)); return;
            }
    }
    
    // Iterate down and see if any should filter
    for(View view : pars)
        if(view.getEventAdapter().isEnabled(anEvent.getType())) {
            ViewEvent e2 = anEvent.copyForView(view);
            view.processEventFilters(e2);
            if(e2.isConsumed()) return;
        }
        
    // Iterate back up and see if any parents should handle
    for(int i=pars.length-1;i>=0;i--) { View view = pars[i];
        if(view.getEventAdapter().isEnabled(anEvent.getType())) {
            ViewEvent e2 = anEvent.copyForView(view);
            view.processEventHandlers(e2);
            if(e2.isConsumed()) break;
        }
    }
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
    
    // Track keys whenever "snp" + anything typed in to enable certain debug options
    if(anEvent.isKeyType() && (anEvent.getKeyChar()=='d' || _debugKeys.length()>0))
        trackDebugKeys(anEvent);

    // Get current focused view and array of parents
    View focusedView = _win.getFocusedView();
    if(focusedView==null) focusedView = _win.getContent(); // This is bogus
    View pars[] = getParents(focusedView);
    
    // Iterate down and see if any should filter
    for(View view : pars)
        if(view.getEventAdapter().isEnabled(anEvent.getType())) {
            ViewEvent e2 = anEvent.copyForView(view);
            view.processEventFilters(e2);
            if(e2.isConsumed()) return;
        }

    // If key pressed and tab and FocusedView.FocusKeysEnabled, switch focus
    if(anEvent.isKeyPress() && anEvent.isTabKey() && focusedView!=null && focusedView.isFocusKeysEnabled()) {
        View next = anEvent.isShiftDown()? focusedView.getFocusPrev() : focusedView.getFocusNext();
        if(next!=null) { _win.requestFocus(next); return; }
    }
    
    // Iterate back up and see if any parents should handle
    for(int i=pars.length-1;i>=0;i--) { View view = pars[i];
        if(view.getEventAdapter().isEnabled(anEvent.getType())) {
            ViewEvent event = anEvent.copyForView(view);
            view.processEventHandlers(event);
            if(event.isConsumed()) return;
        }
    }
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
                view.fireEvent(event); if(event.isConsumed()) break;
            }
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
    View rview = _win.getRootView();
    View targ = ViewUtils.getDeepestViewAt(rview, anEvent.getX(), anEvent.getY());
    View pars[] = getParents(targ);
    
    // Remove old DragOver views and dispatch appropriate MouseExited events
    for(View view=_dragOverView;view!=null;view=view.getParent()) {
         if(!ArrayUtils.containsId(pars,view)) {
             _dragOverView = view.getParent();
             if(!view.getEventAdapter().isEnabled(DragExit)) continue;
             ViewEvent e2 = anEvent.copyForView(view); e2._type = DragExit;
             view.fireEvent(e2); if(e2.isConsumed()) anEvent.consume();
         }
         else break;
    }
    
    // Add new DragOver views and dispatch appropriate MouseEntered events
    int start = getParentCount(_dragOverView!=null? _dragOverView : rview);
    for(int i=start;i<pars.length;i++) { View view = pars[i]; _dragOverView = view;
        if(!view.getEventAdapter().isEnabled(DragEnter)) continue;
        ViewEvent e2 = anEvent.copyForView(view); e2._type = DragEnter;
        view.fireEvent(e2); if(e2.isConsumed()) anEvent.consume();
    }
    
    // Handle DragOver, DragDrop
    if(anEvent.isDragOver() || anEvent.isDragDrop()) {
        for(View view=_dragOverView;view!=null;view=view.getParent()) {
            if(view.getEventAdapter().isEnabled(anEvent.getType())) {
                ViewEvent e2 = anEvent.copyForView(view);
                view.fireEvent(e2); if(e2.isConsumed()) anEvent.consume(); break;
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
    for(View v : _mouseOvers) if(v==aView) return true;
    return false;
}

/**
 * Returns whether given view is currently in mouse press.
 */
public boolean isMouseDown(View aView)  { return _mousePressView==aView && ViewUtils.isMouseDown(); }

/** Returns the number of parents of given view including RootView. */
private int getParentCount(View aView)
{
    if(aView==null) return 0; View rview = _win.getRootView();
    int pc = 1; for(View v=aView;v!=rview;v=v.getParent()) pc++;
    return pc;
}

/** Returns array of parents of given view up to and including RootView. */
private View[] getParents(View aView)
{
    int pc = getParentCount(aView); View pars[] = new View[pc]; if(pc==0) return pars;
    View rview = _win.getRootView();
    for(View v=aView;v!=rview;v=v.getParent()) pars[--pc] = v; pars[0] = rview;
    return pars;
}

// A method to 
void trackDebugKeys(ViewEvent anEvent)
{
    // Add char and clear if too long
    _debugKeys.append(anEvent.getKeyChar());
    if(_debugKeys.length()>5) _debugKeys.setLength(0);
    
    // If last sequence was "ddd1" toggle draw debug
    String str = _debugKeys.toString();
    if(str.equals("ddd1")) { boolean val = !ViewUpdater._debug;
        ViewUpdater._debug = val; beep(val); _debugKeys.setLength(0); }
        
    // If last sequence was "ddd2" toggle draw debug
    if(str.equals("ddd2")) { boolean val = ViewUpdater._frames==null;
        ViewUpdater._frames = val? new long[20] : null; beep(val); _debugKeys.setLength(0); }
}

// A beep method to beep once for true or twice for false.
void beep(boolean aFlag)
{
    ViewUtils.beep();
    if(!aFlag) ViewUtils.runDelayed(() -> ViewUtils.beep(), 300, true);
}

// Returns the current click count
private static int getClickCount()  { return ViewUtils.getMouseDown().getClickCount(); }

}