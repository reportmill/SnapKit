package snap.webenv;
import snap.util.ListUtils;
import snap.util.SnapEnv;
import snap.view.*;
import snap.webapi.*;
import snap.webapi.EventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A class to work with the browser web page.
 */
public class CJScreen {

    // The Window hit by last MouseDown
    private WindowView  _mousePressWin;

    // The Window hit by last MouseMove (if mouse still down)
    private WindowView  _mouseDownWin;

    // Time of last mouse release
    private long  _lastReleaseTime;

    // Last number of clicks
    private int  _clicks;

    // The list of open windows
    private List <WindowView>  _windows = new ArrayList<>();

    // The current main window
    private WindowView  _win;

    // The shared screen object
    private static CJScreen _screen;

    // The element used as screen
    protected static HTMLElement _screenDiv;

    // The input element used to get text input
    private HTMLElement _focusEnabler;

    /**
     * Constructor.
     */
    private CJScreen()
    {
        // Get Doc and body
        HTMLDocument doc = HTMLDocument.getDocument();
        HTMLElement html = doc.getDocumentElement();
        HTMLElement body = doc.getBody();

        // Configure html and body elements
        html.getStyle().setProperty("margin", "0");
        html.getStyle().setProperty("height", "100%");
        body.getStyle().setProperty("margin", "0");
        body.getStyle().setProperty("height", "100%");

        // Create and configure ScreenDiv
        _screenDiv = doc.createElement("div");
        _screenDiv.setId("ScreenDiv");
        _screenDiv.getStyle().setProperty("margin", "0");
        _screenDiv.getStyle().setProperty("position", "fixed");
        _screenDiv.getStyle().setProperty("width", "100%");
        _screenDiv.getStyle().setProperty("height", "100%");
        _screenDiv.setMemberInt("tabIndex", -1); // iOS
        _screenDiv.getStyle().setProperty("cursor", "unset"); // iOS
        if (_screenDiv != body)
            body.appendChild(_screenDiv);
        _screenDiv.focus();

        // Add element with tabindex to allow keyboard focus
        _focusEnabler = doc.createElement("input");
        _focusEnabler.setId("FocusEnabler");
        _focusEnabler.getStyle().setProperty("position", "absolute");
        _focusEnabler.getStyle().setProperty("opacity", "0");
        _focusEnabler.getStyle().setProperty("padding", "0px");
        _focusEnabler.getStyle().setProperty("border", "0px");
        _focusEnabler.setMemberInt("tabIndex", 0);
        _screenDiv.appendChild(_focusEnabler);
        _focusEnabler.focus();

        // Add Mouse listeners
        EventListener<?> lsnr = e -> handleEvent(e);
        _screenDiv.addEventListener("mousedown", lsnr);
        _screenDiv.addEventListener("mousemove", lsnr);
        _screenDiv.addEventListener("mouseup", lsnr);
        _screenDiv.addEventListener("wheel", lsnr);

        // Add Key Listeners
        _screenDiv.addEventListener("keydown", lsnr);
        _screenDiv.addEventListener("keyup", lsnr);

        // Add pointerdown: Used to keep getting events when mousedown goes outside window
        _screenDiv.addEventListener("pointerdown", lsnr);

        // Add Touch Listeners
        _screenDiv.addEventListener("touchstart", lsnr);
        _screenDiv.addEventListener("touchmove", lsnr);
        _screenDiv.addEventListener("touchend", lsnr);

        // Add focus/blur listeners
        _focusEnabler.addEventListener("focus", this::handleDocumentGainedFocus);
        _focusEnabler.addEventListener("blur", this::handleDocumentLostFocus);

        // Disable click, contextmenu events
        EventListener<?> stopLsnr = e -> { };
        _screenDiv.addEventListener("click", stopLsnr);
        _screenDiv.addEventListener("contextmenu", stopLsnr);

        // Disable selection events on iOS
        _screenDiv.addEventListener("select", stopLsnr);
        _screenDiv.addEventListener("selectstart", stopLsnr);
        _screenDiv.addEventListener("selectend", stopLsnr);

        // Stop weird Safari iOS element selection
        html.getStyle().setProperty("-webkit-user-select", "none");
    }

    /**
     * Handles an event.
     */
    void handleEvent(Event e)
    {
        // Vars
        Runnable run = null;

        // Handle event types
        switch(e.getType()) {

            // Handle MouseDown
            case "mousedown":
                run = () -> mouseDown((MouseEvent) e);
                _mousePressWin = _mouseDownWin = getWindow((MouseEvent) e);
                if (_mousePressWin == null) return;
                break;

            // Handle MouseMove
            case "mousemove":
                if (_mouseDownWin != null)
                    run = () -> mouseDrag((MouseEvent) e);
                else run = () -> mouseMove((MouseEvent) e);
                break;

            // Handle MouseUp
            case "mouseup":
                run = () -> mouseUp((MouseEvent) e);
                if (_mousePressWin == null) return; //stopProp = prevDefault = true;
                break;

            // Handle Wheel
            case "wheel":
                if (getWindow((WheelEvent) e) == null) return;
                run = () -> mouseWheel((WheelEvent) e);
                break;

            // Handle KeyDown
            case "keydown":
                if (_mousePressWin == null) return;
                run = () -> keyDown((KeyboardEvent) e);
                break;

            // Handle KeyUp
            case "keyup":
                if (_mousePressWin == null) return;
                run = () -> keyUp((KeyboardEvent) e);
                break;

            // Handle TouchStart
            case "touchstart":
                run = () -> touchStart((TouchEvent) e);
                _mousePressWin = _mouseDownWin = getWindow((TouchEvent) e);
                if (_mousePressWin == null) return;
                break;

            // Handle TouchMove
            case "touchmove":
                if (_mousePressWin == null) return;
                run = () -> touchMove((TouchEvent) e);
                break;

            // Handle TouchEnd
            case "touchend":
                if (_mousePressWin == null) return;
                run = () -> touchEnd((TouchEvent) e);
                break;

            // Handle pointerDown
            case "pointerdown":
                setPointerCapture(e);
                break;

            // Unknown
            default: System.err.println("CJScreen.handleEvent: Not handled: " + e.getType()); return;
        }

        // Run event
        if (run != null)
            run.run();
    }

    /**
     * This is used to keep getting events even when mousedown goes outside window.
     */
    public void setPointerCapture(Event pointerEvent)
    {
        HTMLElement screenDiv = CJScreen.getScreenDiv();
        int id = pointerEvent.getMemberInt("pointerId");
        screenDiv.setPointerCapture(id);
    }

    /**
     * Returns the list of visible windows.
     */
    public List <WindowView> getWindows()  { return _windows; }

    /**
     * Called when a window is ordered onscreen.
     */
    public void addWindowToScreen(WindowView aWin)
    {
        // If first window, see if 'snap_loader' needs to be removed
        if (_windows.isEmpty())
            removeSnapLoader();

        // Add to list
        _windows.add(aWin);

        // Set Window showing
        ViewUtils.setShowing(aWin, true);

        // Make window main window
        if (aWin.isFocusable()) {
            _win = _mousePressWin = aWin;
            _windows.forEach(win -> ViewUtils.setFocused(win, false));
            ViewUtils.setFocused(aWin, true);
        }
    }

    /**
     * Called when a window is hidden.
     */
    public void removeWindowFromScreen(WindowView aWin)
    {
        // Set Window not showing or focused
        ViewUtils.setShowing(aWin, false);
        ViewUtils.setFocused(aWin, false);

        // Remove window from list
        _windows.remove(aWin);

        // Make next window in list main window
        _win = ListUtils.findLastMatch(_windows, win -> win.isFocusable());
        if (_win != null)
            ViewUtils.setFocused(_win, true);
    }

    /**
     * Called when body gets mouseMove.
     */
    public void mouseMove(MouseEvent anEvent)
    {
        // Get window for MouseEvent
        WindowView win = getWindow(anEvent);
        if (win == null) win = _win;
        if (win == null) return;

        // Dispatch MouseMove event
        ViewEvent event = createEvent(win, anEvent, View.MouseMove, null);
        event.setClickCount(_clicks);
        win.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets MouseDown.
     */
    public void mouseDown(MouseEvent anEvent)
    {
        // Restore focus if need be
        _focusEnabler.focus();

        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime < 400 ? (_clicks + 1) : 1;
        _lastReleaseTime = time;

        // Get MouseDownWin for event
        WindowView mouseDownWin = _mouseDownWin = getWindow(anEvent);
        if (mouseDownWin == null)
            return;

        // Dispatch MousePress event
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MousePress, null);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEventToWindow(event);

        // If any draggable views under mouse press, preemptively dispatch drag gesture event to configure things in CJDom.js
        if (!SnapEnv.isJxBrowser)
            preemptiveDispatchDragGestureForMouseEvent(anEvent, mouseDownWin);
    }

    /**
     * Checks for any views under mouse press that handle DragGesture and sends event so they can configure things in CJDom.js.
     */
    private void preemptiveDispatchDragGestureForMouseEvent(MouseEvent mouseDownEvent, WindowView mouseDownWin)
    {
        // If MouseDownWin changed, just return. Modal window started new event loop - maybe should eat next drag/up events.
        // There really should be more code to prevent mouse loop straddling event queues
        if (mouseDownWin != _mouseDownWin)
            return;

        // Get MousePressView
        EventDispatcher eventDispatcher = mouseDownWin.getDispatcher();
        View mousePressView = eventDispatcher.getMousePressView();

        // If MousePressView wants DragGesture, go ahead and send event (start drag will just set cjdom._dragGestureDataTransfer)
        for (View mousePressV = mousePressView; mousePressV != null; mousePressV = mousePressV.getParent()) {
            if (mousePressV.getEventAdapter().isEnabled(ViewEvent.Type.DragGesture)) {
                ViewEvent dragGestureEvent = createEvent(mouseDownWin, mouseDownEvent, ViewEvent.Type.DragGesture, null);
                mouseDownWin.dispatchEventToWindow(dragGestureEvent);
                break;
            }
        }
    }

    /**
     * Called when body gets mouseMove with MouseDown.
     */
    public void mouseDrag(MouseEvent anEvent)
    {
        if (_mouseDownWin == null) return;

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MouseDrag, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets mouseUp.
     */
    public void mouseUp(MouseEvent anEvent)
    {
        if (_mouseDownWin == null) return;
        WindowView mouseDownWin = _mouseDownWin;
        _mouseDownWin = null;

        // Create and dispatch MouseRelease event
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MouseRelease, null);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEventToWindow(event);
    }

    /* Only Y Axis Scrolling has been implemented */
    public void mouseWheel(WheelEvent anEvent)
    {
        // Get window for WheelEvent and dispatch WheelEvent event
        WindowView win = getWindow(anEvent); if (win == null) return;
        ViewEvent event = createEvent(win, anEvent, View.Scroll, null);
        win.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets keyDown.
     */
    public void keyDown(KeyboardEvent keyboardEvent)
    {
        ViewEvent keyPressEvent = createEvent(_win, keyboardEvent, View.KeyPress, null);
        _win.dispatchEventToWindow(keyPressEvent);

        // If event is typeable, send as KeyType too
        if (isTypeableKeyboardEvent(keyboardEvent)) {
            ViewEvent keyTypeEvent = createEvent(_win, keyboardEvent, View.KeyType, null);
            _win.dispatchEventToWindow(keyTypeEvent);
        }
    }

    /**
     * Called when body gets keyUp.
     */
    public void keyUp(KeyboardEvent keyboardEvent)
    {
        ViewEvent event = createEvent(_win, keyboardEvent, View.KeyRelease, null);
        _win.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets TouchStart.
     */
    public void touchStart(TouchEvent anEvent)
    {
        // Restore focus if need be
        _focusEnabler.focus();

        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime < 400 ? (_clicks + 1) : 1; _lastReleaseTime = time;

        // Get MouseDownWin for event
        _mouseDownWin = getWindow(anEvent);
        if (_mouseDownWin == null)
            return;

        // Create and dispatch MousePress event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MousePress, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets touchMove.
     */
    public void touchMove(TouchEvent anEvent)
    {
        if (_mouseDownWin == null) return;

        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MouseDrag, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets touchEnd.
     */
    public void touchEnd(TouchEvent anEvent)
    {
        if (_mouseDownWin == null) return;

        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        WindowView mouseDownWin = _mouseDownWin;
        _mouseDownWin = null;

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MouseRelease, null);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets cut/copy/paste.
     */
    /*public void cutCopyPaste(ClipboardEvent anEvent)
    {
        String type = anEvent.getType();
        CJClipboard cb = (CJClipboard)Clipboard.get();
        DataTransfer dtrans = anEvent.getClipboardData();

        // Handle cut/copy: Load DataTransfer from Clipboard.ClipboardDatas
        if (type.equals("cut") || type.equals("copy")) {
            dtrans.clearData(null);
            for (ClipboardData cdata : cb.getClipboardDatas().values())
                if (cdata.isString())
                    dtrans.setData(cdata.getMIMEType(), cdata.getString());
        }

        // Handle paste: Update Clipboard.ClipboardDatas from DataTransfer
        else if (type.equals("paste")) {
            cb.clearData();
            for (String typ : dtrans.getTypes())
                cb.addData(typ,dtrans.getData(typ));
        }

        // Needed to push changes to system clipboard
        anEvent.preventDefault();
    }*/

    /**
     * Called when browser document gets focus.
     */
    protected void handleDocumentGainedFocus(Event anEvent)
    {
        WindowView lastFocusableWin = ListUtils.findLastMatch(_windows, win -> win.isFocusable());
        if (lastFocusableWin != null)
            ViewUtils.setFocused(lastFocusableWin, true);
    }

    /**
     * Called when browser document loses focus.
     */
    protected void handleDocumentLostFocus(Event anEvent)
    {
        // This bogus - but needed for now
        if (SnapEnv.isJxBrowser) return;

        for (WindowView win : _windows)
            ViewUtils.setFocused(win, false);
    }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(MouseEvent anEvent)
    {
        int x = anEvent.getPageX();
        int y = anEvent.getPageY();
        return getWindow(x, y);
    }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(TouchEvent anEvent)
    {
        int x = anEvent.getPageX();
        int y = anEvent.getPageY();
        return getWindow(x, y);
    }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(int aX, int aY)
    {
        for (int i = _windows.size() - 1; i >= 0; i--) {
            WindowView win = _windows.get(i);
            if (win.isMaximized() || win.contains(aX - win.getX(), aY - win.getY()))
                return win;
        }
        return null;
    }

    /**
     * Creates an Event.
     */
    ViewEvent createEvent(WindowView aWin, Object anEvent, ViewEvent.Type aType, String aName)
    {
        View rootView = aWin.getRootView();
        ViewEvent event = ViewEvent.createEvent(rootView, anEvent, aType, aName);
        return event;
    }

    /**
     * Look for 'snap_loader' element and remove if found.
     */
    private void removeSnapLoader()
    {
        HTMLDocument doc = HTMLDocument.getDocument();
        HTMLElement snapLoader = doc.getElementById("snap_loader");
        if (snapLoader != null)
            CJUtils.removeElementWithFadeAnim(snapLoader, 500);
    }

    /**
     * Returns the shared screen.
     */
    public static CJScreen getScreen()
    {
        if (_screen != null) return _screen;
        return _screen = new CJScreen();
    }

    /**
     * Returns the screen div.
     */
    public static HTMLElement getScreenDiv()
    {
        if (_screenDiv != null) return _screenDiv;
        getScreen();
        return _screenDiv;
    }

    /**
     * Returns whether given keyboard event can be typed.
     */
    private static boolean isTypeableKeyboardEvent(KeyboardEvent keyboardEvent)
    {
        // If control/command modifier is down, just return
        if (keyboardEvent.isCtrlKey() || keyboardEvent.isMetaKey())
            return false;

        // If key name is special/modifier key name, just return
        String keyName = keyboardEvent.getKey();
        if (keyName == null || keyName.isEmpty() || IGNORE_KEY_NAMES.contains(keyName))
            return false;

        return true;
    }

    // Key names to ignore
    private static Set<String> IGNORE_KEY_NAMES = Set.of("Control", "Alt", "Meta", "Shift", "ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight",
            "Enter", "Backspace", "Escape");
}