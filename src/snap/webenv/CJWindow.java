package snap.webenv;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.view.*;
import snap.webapi.*;
import snap.webapi.EventListener;

/**
 * A class to represent the WindowView in the browser page.
 */
public class CJWindow {

    // The Window View
    protected WindowView _win;

    // The RootView
    protected RootView _rootView;

    // A div element to hold window canvas
    protected HTMLElement _windowDiv;

    // The HTMLCanvas to paint/show window content
    protected HTMLCanvasElement _canvas;

    // The Painter for window content
    private Painter _painter;

    // The parent element holding window element when showing
    protected HTMLElement _parent;

    // A listener for browser window resize
    protected EventListener<?> _resizeLsnr = null;

    // The body overflow value
    private String _bodyOverflow;

    // The last top window
    protected static int _topWin;

    // The paint scale
    public static int PIXEL_SCALE = Window.getDevicePixelRatio() == 2 ? 2 : 1;

    /**
     * Constructor.
     */
    public CJWindow(WindowView snapWindow)
    {
        // Set Window and RootView
        _win = snapWindow;
        _rootView = _win.getRootView();
        ViewUtils.setNative(_win, this);

        // Start listening to snap window prop changes
        _win.addPropChangeListener(this::handleSnapWindowPropChange);

        // Create/configure WindowDiv, the HTMLElement to hold window and canvas
        HTMLDocument doc = HTMLDocument.getDocument();
        _windowDiv = doc.createElement("div");
        _windowDiv.setId("WindowDiv");
        _windowDiv.getStyle().setProperty("box-sizing", "border-box");
        //_windowDiv.getStyle().setProperty("background", "#F4F4F4CC");

        // Create canvas and configure to totally fill window element (minus padding insets)
        _canvas = (HTMLCanvasElement) HTMLDocument.getDocument().createElement("canvas");
        _canvas.getStyle().setProperty("width", "100%");
        _canvas.getStyle().setProperty("height", "100%");
        _canvas.getStyle().setProperty("box-sizing", "border-box");

        // Add RootView listener to propagate size changes to canvas
        _rootView.addPropChangeListener(pc -> handleRootViewSizeChange(), View.Width_Prop, View.Height_Prop);
        handleRootViewSizeChange();

        // Have to do this so TouchEvent.preventDefault doesn't complain and iOS doesn't scroll doc
        _canvas.getStyle().setProperty("touch-action", "none");
        _canvas.setAttribute("touch-action", "none");
        //_canvas.addEventListener("touchstart", e -> e.preventDefault());
        //_canvas.addEventListener("touchmove", e -> e.preventDefault());
        //_canvas.addEventListener("touchend", e -> e.preventDefault());
        //_canvas.addEventListener("wheel", e -> e.preventDefault());

        // Create painter
        _painter = new CJPainter2(_canvas, PIXEL_SCALE);

        // Register for drop events
        _canvas.setAttribute("draggable", "true");
        EventListener<DragEvent> dragLsnr = e -> handleDragEvent(e);
        _canvas.addEventListener("dragenter", dragLsnr);
        _canvas.addEventListener("dragover", dragLsnr);
        _canvas.addEventListener("dragexit", dragLsnr);
        _canvas.addEventListener("drop", dragLsnr);

        // Register for drag start event
        _canvas.addEventListener("dragstart", e -> handleDragGesture((DragEvent) e));
        _canvas.addEventListener("dragend", e -> handleDragEnd((DragEvent) e));

        // Add canvas to WindowDiv
        _windowDiv.appendChild(_canvas);
    }

    /**
     * Initializes window.
     */
    public void initWindow()
    {
        if (_rootView.getFill() == null)
            _rootView.setFill(ViewUtils.getBackFill());
        if (_rootView.getBorder() == null && _win.getType() != WindowView.Type.PLAIN)
            _rootView.setBorder(Color.GRAY, 1);
    }

    /**
     * Returns the canvas for the window.
     */
    public HTMLCanvasElement getCanvas()  { return _canvas; }

    /**
     * Returns the parent DOM element of this window (WindowDiv).
     */
    public HTMLElement getParent()  { return _parent; }

    /**
     * Sets the parent DOM element of this window (WindowDiv).
     */
    protected void setParent(HTMLElement aNode)
    {
        // If already set, just return
        if (aNode == _parent) return;

        // Set new value
        HTMLElement parent = _parent;
        _parent = aNode;

        // If null, just remove from old parent and return
        if (aNode == null) {
            parent.removeChild(_windowDiv);
            return;
        }

        // Add WindowDiv to given node
        aNode.appendChild(_windowDiv);

        // If screenDiv, configure special
        if (aNode == CJScreen.getScreenDiv()) {

            // Configure WindowDiv for body
            _windowDiv.getStyle().setProperty("position", _win.isMaximized() ? "fixed" : "absolute");
            _windowDiv.getStyle().setProperty("z-index", String.valueOf(_topWin++));

            // If not maximized, clear background and add drop shadow
            if (!_win.isMaximized()) {
                _windowDiv.getStyle().setProperty("background", null);
                _windowDiv.getStyle().setProperty("box-shadow", "1px 1px 8px grey");
            }
        }

        // If arbitrary element
        else {
            _windowDiv.getStyle().setProperty("position", "static");
            _windowDiv.getStyle().setProperty("width", "100%");
            _windowDiv.getStyle().setProperty("height", "100%");
        }
    }

    /**
     * Returns the parent DOM element of this window.
     */
    private HTMLElement getParentForWin()
    {
        // If window is maximized, parent should always be body
        if (_win.isMaximized())
            return CJScreen.getScreenDiv();

        // If window has named element, return that
        String parentName = _win.getName();
        if (parentName != null) {
            HTMLDocument doc = HTMLDocument.getDocument();
            HTMLElement parent = doc.getElementById(parentName);
            if (parent != null)
                return parent;
        }

        // Default to ScreenDiv
        return CJScreen.getScreenDiv();
    }

    /**
     * Returns whether window is child of screen.
     */
    private boolean isChildOfBody()
    {
        return getParent() == CJScreen.getScreenDiv();
    }

    /**
     * Resets the parent DOM element and Window/WindowDiv bounds.
     */
    protected void resetParentAndBounds()
    {
        // Get proper parent node and set
        HTMLElement parent = getParentForWin();
        setParent(parent);

        // If window is in screen, set WindowDiv bounds from Window
        if (parent == CJScreen.getScreenDiv()) {
            if (_win.isMaximized())
                _win.setBounds(CJ.getViewportBounds());
            handleSnapWindowBoundsChange(null);
        }

        // If window in DOM container element
        else handleBrowserWindowSizeChange();
    }

    /**
     * Shows window.
     */
    public void show()
    {
        if (_win.isModal())
            showModal();
        else showImpl();
    }

    /**
     * Shows window.
     */
    public void showImpl()
    {
        // Make sure WindowDiv is in proper parent node with proper bounds
        resetParentAndBounds();

        // Add to Screen.Windows
        CJScreen screen = CJScreen.getScreen();
        screen.addWindowToScreen(_win);

        // Start listening to browser window resizes
        if (_resizeLsnr == null)
            _resizeLsnr = e -> handleBrowserWindowSizeChange();
        Window.get().addEventListener("resize", _resizeLsnr);
    }

    /**
     * Shows modal window.
     */
    protected void showModal()
    {
        // Do normal show
        showImpl();

        // Register listener to activate current thread on window not showing
        PropChangeListener hideLsnr = pc -> modalWindowShowingChanged();
        _win.addPropChangeListener(hideLsnr, View.Showing_Prop);

        // Start new app thread, since this thread is now tied up until window closes
        WebEnv.get().startNewEventThreadAndWait();

        // Remove listener
        _win.removePropChangeListener(hideLsnr);
    }

    /**
     * Called when modal window sets showing to false.
     */
    private void modalWindowShowingChanged()
    {
        WebEnv.get().stopEventThreadAndNotify();
    }

    /**
     * Hides window.
     */
    public void hide()
    {
        // Remove WindowDiv from parent
        setParent(null);

        // Remove Window from screen
        CJScreen screen = CJScreen.getScreen();
        screen.removeWindowFromScreen(_win);

        // Stop listening to browser window resizes
        Window.get().removeEventListener("resize", _resizeLsnr);
        _resizeLsnr = null;

        // Send WinClose event
        sendWinEvent(ViewEvent.Type.WinClose);
    }

    /**
     * Window/Popup method: Order window to front.
     */
    public void toFront()
    {
        _windowDiv.getStyle().setProperty("z-index", String.valueOf(_topWin++));
    }

    /**
     * Called to register for repaint.
     */
    public void paintViews(Rect aRect)
    {
        _painter.setTransform(1,0,0,1,0,0); // I don't know why I need this!
        ViewUpdater updater = _rootView.getUpdater();
        updater.paintViews(_painter, aRect);
        _painter.flush();
    }

    /**
     * Called when browser window resizes.
     */
    private void handleBrowserWindowSizeChange()
    {
        // If Window is child of body, just return
        if (isChildOfBody()) {
            if (_win.isMaximized())
                _win.setBounds(CJ.getViewportBounds());
            return;
        }

        // Reset window location
        HTMLElement parent = getParent();
        Point off = CJ.getOffsetAll(parent);
        _win.setXY(off.x, off.y);

        // Reset window size
        int parW = parent.getClientWidth();
        int parH = parent.getClientHeight();
        _win.setSize(parW, parH);
        _win.repaint();
    }

    /**
     * Called when root view size changes.
     */
    private void handleRootViewSizeChange()
    {
        int rootW = (int) Math.ceil(_rootView.getWidth());
        int rootH = (int) Math.ceil(_rootView.getHeight());
        _canvas.setWidth(rootW * PIXEL_SCALE);
        _canvas.setHeight(rootH * PIXEL_SCALE);
    }

    /**
     * Called to handle a drag event.
     * Not called on app thread, because drop data must be processed when event is issued.
     * TVEnv.runOnAppThread(() -> handleDragEvent(anEvent));
     */
    public void handleDragEvent(DragEvent anEvent)
    {
        anEvent.preventDefault();
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(event);
    }

    /** Called to handle a drag event. */
    public void handleDragGesture(DragEvent anEvent)
    {
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(event);
    }

    /** Called to handle dragend event. */
    public void handleDragEnd(DragEvent anEvent)
    {
        ViewEvent nevent = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(nevent);
    }

    /**
     * Called when Snap WindowView has prop change.
     */
    private void handleSnapWindowPropChange(PropChange aPC)
    {
        switch (aPC.getPropName()) {

            // Handle bounds change
            case View.X_Prop: case View.Y_Prop:
            case View.Width_Prop: case View.Height_Prop:
                handleSnapWindowBoundsChange(aPC);
                break;

            // Handle WindowView.Maximized_Prop
            case WindowView.Maximized_Prop: handleSnapWindowMaximizedChange(); break;

            // Handle WindowView.ActiveCursor_Prop
            case WindowView.ActiveCursor_Prop: handleSnapWindowActiveCursorChange(); break;
        }
    }

    /**
     * Called when WindowView has bounds change to sync to WindowDiv.
     */
    private void handleSnapWindowBoundsChange(PropChange aPC)
    {
        // If Window not child of body, just return (parent node changes go to win, not win to parent)
        if (!isChildOfBody()) return;

        // Get bounds x, y, width, height and PropChange name
        int x = (int) Math.round(_win.getX());
        int y = (int) Math.round(_win.getY());
        int w = (int) Math.round(_win.getWidth());
        int h = (int) Math.round(_win.getHeight());
        String propName = aPC != null ? aPC.getPropName() : null;

        // Handle changes
        if (propName == null || propName == View.X_Prop)
            _windowDiv.getStyle().setProperty("left", x + "px");
        if (propName == null || propName == View.Y_Prop)
            _windowDiv.getStyle().setProperty("top", y + "px");
        if (propName == null || propName == View.Width_Prop)
            _windowDiv.getStyle().setProperty("width", w + "px");
        if (propName == null || propName == View.Height_Prop)
            _windowDiv.getStyle().setProperty("height", h + "px");
    }

    /**
     * Called when WindowView.Maximized is changed.
     */
    private void handleSnapWindowMaximizedChange()
    {
        // Get body and canvas
        HTMLBodyElement body = HTMLBodyElement.getBody();
        HTMLCanvasElement canvas = getCanvas();

        // Handle Maximized on
        if (_win.isMaximized()) {

            // Set body overflow to hidden (to get rid of scrollbars)
            _bodyOverflow = body.getStyle().getPropertyValue("overflow");
            body.getStyle().setProperty("overflow", "hidden");

            // Set Window/WindowDiv padding
            _win.setPadding(5, 5, 5, 5);
            _windowDiv.getStyle().setProperty("padding", "5px");

            // Add a shadow to canvas
            canvas.getStyle().setProperty("box-shadow", "1px 1px 8px grey");
        }

        // Handle Maximized off
        else {

            // Restore body overflow
            body.getStyle().setProperty("overflow", _bodyOverflow);

            // Clear Window/WindowDiv padding
            _win.setPadding(0, 0, 0, 0);
            _windowDiv.getStyle().setProperty("padding", null);

            // Remove shadow from canvas
            canvas.getStyle().setProperty("box-shadow", null);
        }

        // Reset parent and Window/WindowDiv bounds
        resetParentAndBounds();
    }

    /**
     * Sets the cursor.
     */
    private void handleSnapWindowActiveCursorChange()
    {
        Cursor aCursor = _win.getActiveCursor();
        String cstr = "default";
        if (aCursor == Cursor.DEFAULT) cstr = "default";
        if (aCursor == Cursor.CROSSHAIR) cstr = "crosshair";
        if (aCursor == Cursor.HAND) cstr = "pointer";
        if (aCursor == Cursor.MOVE) cstr = "move";
        if (aCursor == Cursor.TEXT) cstr = "text";
        if (aCursor == Cursor.NONE) cstr = "none";
        if (aCursor == Cursor.N_RESIZE) cstr = "n-resize";
        if (aCursor == Cursor.S_RESIZE) cstr = "s-resize";
        if (aCursor == Cursor.E_RESIZE) cstr = "e-resize";
        if (aCursor == Cursor.W_RESIZE) cstr = "w-resize";
        if (aCursor == Cursor.NE_RESIZE) cstr = "ne-resize";
        if (aCursor == Cursor.NW_RESIZE) cstr = "nw-resize";
        if (aCursor == Cursor.SE_RESIZE) cstr = "se-resize";
        if (aCursor == Cursor.SW_RESIZE) cstr = "sw-resize";
        getCanvas().getStyle().setProperty("cursor", cstr);
    }

    /**
     * Sends the given event.
     */
    private void sendWinEvent(ViewEvent.Type aType)
    {
        // If no listener for event type, just return
        if (!_win.getEventAdapter().isEnabled(aType))
            return;

        // Create ViewEvent and dispatch
        ViewEvent event = ViewEvent.createEvent(_win, null, aType, null);
        _win.dispatchEventToWindow(event);
    }
}