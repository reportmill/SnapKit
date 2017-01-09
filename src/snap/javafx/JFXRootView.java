package snap.javafx;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import snap.view.*;

/**
 * A JFX Node subclass to embed RootView.
 */
public class JFXRootView extends Pane {
    
    // The node
    View                  _view;
    
    // The RootView node
    RootView              _rpane;
    
    // The last mouse moved event, if tooltip set
    MouseEvent            _lastMouseMoved;
    
/**
 * Returns the view.
 */
public View getView()  { return _view; }

/**
 * Sets the view.
 */
public void setView(View aView)
{
    // Set view
    _view = aView;
    
    // RootView only beyond this point
    _rpane = aView instanceof RootView? (RootView)aView : null; if(_rpane==null) return;
    
    // Make root node focusable
    setFocusTraversable(true);
    
    // Add EventHandler for KeyEvents, MouseEvents, ScrollEvents
    addEventHandler(KeyEvent.ANY, e -> handleKeyEvent(e));
    addEventHandler(MouseEvent.ANY, e -> handleMouseEvent(e));
    addEventHandler(ScrollEvent.ANY, e -> handleScrollEvent(e));

    // Handle DragSource events
    setOnDragDetected(e -> handleDragGesture(e));
    setOnDragDone(e -> handleDragDone(e));
    
    // Handle drag events
    EventHandler <DragEvent> hdlr = e ->handleDragEvent(e); //addEventHandler(DragEvent.ANY, e -> handleDragEvent(e));
    setOnDragEntered(hdlr); setOnDragOver(hdlr); setOnDragExited(hdlr); setOnDragDropped(hdlr);
    
    // Enable ToolTips
    enableToolTips();
}

/**
 * Handles KeyEvents.
 */
public void handleKeyEvent(KeyEvent anEvent)
{
    ViewEvent nevent = JFXViewEnv.get().createEvent(_view, anEvent, null, null);
    _rpane.dispatchEvent(nevent);
}

/**
 * Handles MouseEvents.
 */
public void handleMouseEvent(MouseEvent anEvent)
{
    EventType etype = anEvent.getEventType();
    if(etype==MouseEvent.MOUSE_ENTERED || etype==MouseEvent.MOUSE_EXITED) return;
    if(etype==MouseEvent.MOUSE_ENTERED_TARGET || etype==MouseEvent.MOUSE_EXITED_TARGET) return;
    ViewEvent nevent = JFXViewEnv.get().createEvent(_rpane, anEvent, null, null);
    _rpane.dispatchEvent(nevent);
}

/**
 * Handles ScrollEvents.
 */
public void handleScrollEvent(ScrollEvent anEvent)
{
    EventType etype = anEvent.getEventType(); if(etype!=ScrollEvent.SCROLL) return;
    ViewEvent nevent = JFXViewEnv.get().createEvent(_rpane, anEvent, null, null);
    _rpane.dispatchEvent(nevent);
}

/**
 * Handles DragGesture.
 */
public void handleDragGesture(MouseEvent anEvent)
{
    ViewEvent nevent = JFXViewEnv.get().createEvent(_rpane, anEvent, ViewEvent.Type.DragGesture, null);
    _rpane.dispatchDragSourceEvent(nevent);
}

/**
 * Handle DragDone.
 */
public void handleDragDone(DragEvent anEvent)
{
    JFXClipboard.get().getDragboard();
    ViewEvent nevent = JFXViewEnv.get().createEvent(_rpane, anEvent, ViewEvent.Type.DragSourceEnd, null);
    _rpane.dispatchDragSourceEvent(nevent);
}

/**
 * Handles DragEvent.
 */
public void handleDragEvent(DragEvent anEvent)
{
    ViewEvent nevent = JFXViewEnv.get().createEvent(_rpane, anEvent, null, null);
    _rpane.dispatchDragTargetEvent(nevent);
}

/**
 * Repaints pane.
 */
public void repaint(double aX, double aY, double aW, double aH)
{
    Scene scene = getScene(); if(scene==null || _rpane==null) return;
    _rpane.repaint(_view, aX, aY, aW, aH);
}

/**
 * Returns the preferred width.
 */
protected double computePrefWidth(double aH) { return _view!=null? _view.getPrefWidth() : super.computePrefWidth(aH); }

/**
 * Returns the preferred height.
 */
protected double computePrefHeight(double aW) { return _view!=null? _view.getPrefHeight():super.computePrefHeight(aW); }

/**
 * Sets whether tooltips are enabled.
 */
public void enableToolTips()
{
    Tooltip ttip =new Tooltip(); Tooltip.install(this, ttip);
    setOnMouseMoved(e -> _lastMouseMoved = e);
    ttip.setOnShowing(e -> {
        ViewEvent event = JFXViewEnv.get().createEvent(_view,_lastMouseMoved, null, null);
        String ttstr = _view.getToolTip(event);
        if(ttstr!=null) ttip.setText(ttstr);
        else Platform.runLater(() ->ttip.hide());
    });
}

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + " for " + _view; }

}