package snap.swing;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import snap.gfx.Painter;
import snap.geom.Rect;
import snap.view.*;

/**
 * A JComponent subclass to embed RootView.
 */
public class SWRootView extends JComponent implements DragGestureListener {

    // The Window
    private WindowView _win;

    // The RootView
    private RootView _rootView;

    // The DragSource
    private DragSource _dragSource;

    // Last mouse location (to suppress faux MouseDrags due to HiDPI)
    private int _lx, _ly;

    /**
     * Constructor.
     */
    public SWRootView(WindowView aWin, RootView aRootView)
    {
        // RootView only beyond this point
        _win = aWin;
        _rootView = aRootView;

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
            public void dragEnter(DropTargetDragEvent anEvent)
            {
                sendDropTargetEvent(anEvent, ViewEvent.Type.DragEnter);
            }

            public void dragOver(DropTargetDragEvent anEvent)
            {
                sendDropTargetEvent(anEvent, ViewEvent.Type.DragOver);
            }

            public void dragExit(DropTargetEvent anEvent)
            {
                sendDropTargetEvent(anEvent, ViewEvent.Type.DragExit);
            }

            public void drop(DropTargetDropEvent anEvent)
            {
                sendDropTargetEvent(anEvent, ViewEvent.Type.DragDrop);
            }
        };

        // Enable DropTarget for RootView
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, dtl);

        // Add component listener to check for rare case of Snap RootView manually installed in JComponent hierarchy
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e)  { swingShowingChanged(); }
            public void componentHidden(ComponentEvent e)  { swingShowingChanged(); }
        });
    }

    /**
     * Override to sync with RootView.
     */
    public void setBounds(int aX, int aY, int aW, int aH)
    {
        // Do normal set bounds
        super.setBounds(aX, aY, aW, aH);

        // Correct X/Y for RootView in Window
        Window winNtv = SwingUtils.getParent(this, Window.class);
        for (Component c = getParent(); c != winNtv; c = c.getParent()) {
            aX += c.getX();
            aY += c.getY();
        }

        // Set new bounds
        _rootView.setBounds(aX, aY, aW, aH);
    }

    /**
     * Repaint from snap.
     */
    public void repaint(Rect aRect)
    {
        int x = (int) aRect.x, y = (int) aRect.y, w = (int) aRect.width, h = (int) aRect.height;
        paintImmediately(x, y, w, h); //super.repaint(0,x,y,w,h);
    }

    /**
     * Override to suppress normal repaints.
     */
    public void repaint(long aTM, int aX, int aY, int aW, int aH)
    {
    }

    /**
     * Override to wrap in Painter and forward.
     */
    protected void paintComponent(Graphics aGr)
    {
        Painter pntr = new J2DPainter(aGr);
        java.awt.Rectangle crect = aGr.getClipBounds();
        _win.getUpdater().paintViews(pntr, new Rect(crect.x, crect.y, crect.width, crect.height));
        pntr.flush();
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
        super.processEvent(anEvent);
        if (anEvent instanceof InputEvent && ((InputEvent) anEvent).isConsumed())
            return;

        // Handle MouseEvents
        if (anEvent instanceof MouseEvent mouseEvent) {
            int id = mouseEvent.getID();

            // MousePress: Store location for potential MouseDrag suppression
            if (id == MouseEvent.MOUSE_PRESSED) {
                _lx = mouseEvent.getX();
                _ly = mouseEvent.getY();
            }

            // MouseDrag: If matches last location, skip (these can show up on HiDPI Mac and can cause problems)
            else if (id == MouseEvent.MOUSE_DRAGGED) {
                if (mouseEvent.getX() == _lx && mouseEvent.getY() == _ly) {
                    mouseEvent.consume();
                    return;
                }
                _lx = mouseEvent.getX();
                _ly = mouseEvent.getY();
            }

            // Handle MouseClick: Just skip
            if (id == MouseEvent.MOUSE_CLICKED) return;

            // Create new event and dispatch
            ViewEvent event = ViewEvent.createEvent(_rootView, mouseEvent, null, null);
            _win.dispatchEventToWindow(event);

            // Bogus! (not sure why this is here)
            if (!isFocusOwner()) requestFocusInWindow(true);

            // This stops scroll events from closing popup window
            mouseEvent.consume();
        }

        // Handle KeyEvents
        else if (anEvent instanceof KeyEvent) {
            ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
            _win.dispatchEventToWindow(event);
        }
    }

    /**
     * Override to get from RootView.
     */
    public Dimension getMinimumSize()
    {
        if (_rootView == null || isMinimumSizeSet()) return super.getMinimumSize();
        double mw = _rootView.getMinWidth(), mh = _rootView.getMinHeight();
        return new Dimension((int) Math.round(mw), (int) Math.round(mh));
    }

    /**
     * Override to get from RootView.
     */
    public Dimension getPreferredSize()
    {
        if (_rootView == null || isPreferredSizeSet()) return super.getPreferredSize();
        double pw = _rootView.getPrefWidth(), ph = _rootView.getPrefHeight();
        return new Dimension((int) Math.round(pw), (int) Math.round(ph));
    }

    /**
     * Override to get from RootView.
     */
    public String getToolTipText(MouseEvent anEvent)
    {
        if (_rootView == null) return null;
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        return _win.getToolTip(event);
    }

    /**
     * Sends DragGestureEvent to RootView.
     */
    public void dragGestureRecognized(DragGestureEvent anEvent)
    {
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, ViewEvent.Type.DragGesture, null);
        _win.dispatchEventToWindow(event);
    }

    /**
     * Sends DropTargetEvent to RootView.
     */
    public void sendDropTargetEvent(DropTargetEvent anEvent, ViewEvent.Type aType)
    {
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, aType, null);
        _win.dispatchEventToWindow(event);
    }

    /**
     * Called when Showing attribute changes for this SWRootView.
     * Only needed for rare case of Snap RootView manually installed in JComponent hierarchy.
     */
    void swingShowingChanged()
    {
        // Get native window and just return if it is Snap Window native
        Window winNtv = SwingUtils.getParent(this, Window.class);
        if (winNtv == _win.getNative())
            return;

        // Update Snap RootView Showing
        ViewUtils.setShowing(_rootView, isShowing());

        // If not showing and Window.Popup.Showing, hide Popup.
        if (!isShowing() && _win.getPopup() != null)
            _win.getPopup().hide();
        if (!isShowing())
            _win.getDispatcher().dispatchMouseMoveOutsideWindow();
    }
}