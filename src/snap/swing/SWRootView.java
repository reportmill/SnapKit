package snap.swing;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import snap.gfx.Painter;
import snap.gfx.Rect;
import snap.view.*;

/**
 * A JComponent subclass to embed RootView.
 */
public class SWRootView extends JComponent implements DragGestureListener {
    
    // The RootView
    RootView              _rview;
    
    // The last request paint rect
    Rect                  _paintRect;
    
    // The DragSource
    DragSource            _dragSource;

/**
 * Returns the RootView.
 */
public View getRootView()  { return _rview; }

/**
 * Sets the RootView.
 */
public void setRootView(RootView aRootView)
{
    // RootView only beyond this point
    _rview = aRootView;
    
    // Suppress Tab key handling
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, java.util.Collections.EMPTY_SET);
    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, java.util.Collections.EMPTY_SET);
    
    // Enable KeyEvents
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    setFocusable(true);
        
    // RootView should handle mouse events
    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    ToolTipManager.sharedInstance().registerComponent(this);

    // Enable DragGestureRecognizer for RootView
    _dragSource = DragSource.getDefaultDragSource();
    _dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    
    // Create DropTargetListener
    DropTargetListener dtl = new DropTargetAdapter() {
        public void dragEnter(DropTargetDragEvent anEvent) { sendDropTargetEvent(anEvent, ViewEvent.Type.DragEnter); }
        public void dragOver(DropTargetDragEvent anEvent) { sendDropTargetEvent(anEvent, ViewEvent.Type.DragOver); }
        public void dragExit(DropTargetEvent anEvent) { sendDropTargetEvent(anEvent, ViewEvent.Type.DragExit); }
        public void drop(DropTargetDropEvent anEvent) { sendDropTargetEvent(anEvent, ViewEvent.Type.DragDrop); }
    };
    
    // Enable DropTarget for RootView
    new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, dtl);
}

/**
 * Override to sync with view.
 */
public void setBounds(int aX, int aY, int aW, int aH)
{
    super.setBounds(aX, aY, aW, aH);
    _rview.setBounds(aX,aY,aW,aH);
}

/**
 * Repaint from snap.
 */
public void repaint(Rect aRect)
{
    _paintRect = aRect;
    int x = (int)aRect.x, y = (int)aRect.y, w = (int)aRect.width, h = (int)aRect.height;
    paintImmediately(x, y, w, h); //super.repaint(0,x,y,w,h);
}

/**
 * Override to suppress normal repaints.
 */
public void repaint(long aTM, int aX, int aY, int aW, int aH)  { }

/**
 * Override to wrap in Painter and forward.
 */
protected void paintComponent(Graphics aGr)
{
    Painter pntr = new J2DPainter(aGr); pntr.clipRect(0,0,getWidth(),getHeight());
    if(_paintRect==null) _paintRect = new Rect(0,0,getWidth(),getHeight());
    _rview.paintViews(pntr, _paintRect);
    _paintRect = null;
}

/**
 * Override to suppress child paint.
 */
protected void paintChildren(Graphics aGr)  { }

/**
 * Override to forward MouseEvents to RootView.
 */
protected void processEvent(AWTEvent anEvent)
{
    // Do normal version (just return if consumed)
    super.processEvent(anEvent); if(anEvent instanceof InputEvent && ((InputEvent)anEvent).isConsumed()) return;
    
    // Handle MouseEvents
    if(anEvent instanceof MouseEvent) { MouseEvent me = (MouseEvent)anEvent; int id = me.getID();
        if(id==MouseEvent.MOUSE_CLICKED) return;
        ViewEvent event = SwingViewEnv.get().createEvent(_rview, me, null, null);
        _rview.dispatchEvent(event);
        if(!isFocusOwner()) requestFocusInWindow(true); // Bogus!
        me.consume(); // This stops scroll events from closing popup window
    }
    
    // Handle KeyEvents
    else if(anEvent instanceof KeyEvent) { KeyEvent ke = (KeyEvent)anEvent; int id = ke.getID();
        ViewEvent event = SwingViewEnv.get().createEvent(_rview, anEvent, null, null);
        _rview.dispatchEvent(event);
    }
}

/**
 * Returns the minimum size.
 */
public Dimension getMinimumSize()
{
    if(_rview==null || isMinimumSizeSet()) return super.getMinimumSize();
    double mw = _rview.getMinWidth(), mh = _rview.getMinHeight();
    return new Dimension((int)Math.round(mw), (int)Math.round(mh));
}

/**
 * Returns the preferred size.
 */
public Dimension getPreferredSize()
{
    if(_rview==null || isPreferredSizeSet()) return super.getPreferredSize();
    double pw = _rview.getPrefWidth(), ph = _rview.getPrefHeight();
    return new Dimension((int)Math.round(pw), (int)Math.round(ph));
}

/** 
 * Returns a tool tip string by asking deepest shape's tool.
 */
public String getToolTipText(MouseEvent anEvent)
{
    if(_rview==null) return null;
    ViewEvent event = SwingViewEnv.get().createEvent(_rview, anEvent, null, null);
    return _rview.getToolTip(event);
}

/**
 * Sends an event to RootView.
 */
public void dragGestureRecognized(DragGestureEvent anEvent)
{
    ViewEvent event = SwingViewEnv.get().createEvent(_rview, anEvent, ViewEvent.Type.DragGesture, null);
    _rview.dispatchDragSourceEvent(event);
}

/**
 * Sends an event to RootView.
 */
public void sendDropTargetEvent(DropTargetEvent anEvent, ViewEvent.Type aType)
{
    ViewEvent event = SwingViewEnv.get().createEvent(_rview, anEvent, aType, null);
    _rview.dispatchDragTargetEvent(event);
}

}