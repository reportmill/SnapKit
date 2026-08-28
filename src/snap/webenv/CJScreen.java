package snap.webenv;
import snap.util.ListUtils;
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

    // The Window hit by last MouseMove (if mouse still down)
    private WindowView _mouseDownWin;

    // Time of last mouse release
    private long _lastReleaseTime;

    // Last number of clicks
    private int _clicks;

    // The open windows
    private List<WindowView> _openWindows = new ArrayList<>();

    // The current active window
    private WindowView _activeWindow;

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
        EventListener<?> lsnr = this::handleEvent;
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
     * Returns the list of visible windows.
     */
    public List <WindowView> getWindows()  { return _openWindows; }

    /**
     * Called when a window is ordered onscreen.
     */
    public void addWindowToScreen(WindowView aWin)
    {
        // If first window, see if 'snap_loader' needs to be removed
        if (_openWindows.isEmpty())
            removeSnapLoader();

        // Add to list
        _openWindows.add(aWin);

        // Set Window showing
        ViewUtils.setShowing(aWin, true);

        // Activate window
        if (aWin.isFocusable())
            activateWindow(aWin);
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
        _openWindows.remove(aWin);

        // Activate top focusable window
        WindowView topFocusableWindow = ListUtils.findLastMatch(_openWindows, WindowView::isFocusable);
        activateWindow(topFocusableWindow);
    }

    /**
     * Activates the given window.
     */
    private void activateWindow(WindowView aWin)
    {
        _activeWindow = aWin;
        _openWindows.forEach(win -> ViewUtils.setFocused(win, false));
        if (_activeWindow != null)
            ViewUtils.setFocused(aWin, true);
    }

    /**
     * Handles an event.
     */
    private void handleEvent(Event e)
    {
        switch (e.getType()) {
            case "mousedown" -> mouseDown((MouseEvent) e);
            case "mousemove" -> mouseMove((MouseEvent) e);
            case "mouseup" -> mouseUp((MouseEvent) e);
            case "wheel" -> mouseWheel((WheelEvent) e);
            case "keydown" -> keyDown((KeyboardEvent) e);
            case "keyup" -> keyUp((KeyboardEvent) e);
            case "touchstart" -> touchStart((TouchEvent) e);
            case "touchmove" -> touchMove((TouchEvent) e);
            case "touchend" -> touchEnd((TouchEvent) e);
            case "pointerdown" -> setPointerCapture(e);
            default -> System.err.println("Screen.handleEvent: Not handled: " + e.getType());
        }
    }

    /**
     * Called when body gets MouseDown.
     */
    private void mouseDown(MouseEvent anEvent)
    {
        // Get MouseDownWin for event
        _mouseDownWin = getWindow(anEvent);
        if (_mouseDownWin == null)
            return;

        // Restore focus if need be
        _focusEnabler.focus();

        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime < 400 ? (_clicks + 1) : 1;
        _lastReleaseTime = time;

        // If not active window, either return if active is modal or make active window
        if (_mouseDownWin != _activeWindow) {
             if (_activeWindow != null && _activeWindow.isModal()) {
                 _mouseDownWin = null;
                 return;
             }
             if (_mouseDownWin.isFocusable())
                 activateWindow(_mouseDownWin);
        }

        // Dispatch MousePress event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MousePress);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);

        // If modal window activated, just return
        if (_mouseDownWin != _activeWindow) {
            _mouseDownWin = null;
            return;
        }

        // If any draggable views under mouse press, preemptively dispatch drag gesture event to configure things in CJDom.js
        preemptiveDispatchDragGestureForMouseEvent(anEvent);
    }

    /**
     * Checks for any views under mouse press that handle DragGesture and sends event so they can configure things in CJDom.js.
     */
    private void preemptiveDispatchDragGestureForMouseEvent(MouseEvent mouseDownEvent)
    {
        // Get MousePressView
        EventDispatcher eventDispatcher = _mouseDownWin.getDispatcher();
        View mousePressView = eventDispatcher.getMousePressView();

        // If MousePressView wants DragGesture, go ahead and send event (start drag will just set cjdom._dragGestureDataTransfer)
        for (View mousePressV = mousePressView; mousePressV != null; mousePressV = mousePressV.getParent()) {
            if (mousePressV.getEventAdapter().isTypeEnabled(EventType.DragGesture)) {
                ViewEvent dragGestureEvent = createEvent(_mouseDownWin, mouseDownEvent, EventType.DragGesture);
                _mouseDownWin.dispatchEventToWindow(dragGestureEvent);
                break;
            }
        }
    }

    /**
     * Called when body gets mouseMove.
     */
    private void mouseMove(MouseEvent anEvent)
    {
        if (_mouseDownWin != null) {
            mouseDrag(anEvent);
            return;
        }

        // Get window for MouseEvent
        WindowView win = getWindow(anEvent);
        if (win == null) win = _activeWindow;
        if (win == null) return;

        // Dispatch MouseMove event
        ViewEvent event = createEvent(win, anEvent, View.MouseMove);
        event.setClickCount(_clicks);
        win.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets mouseMove with MouseDown.
     */
    private void mouseDrag(MouseEvent anEvent)
    {
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MouseDrag);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets mouseUp.
     */
    private void mouseUp(MouseEvent anEvent)
    {
        if (_mouseDownWin == null) return;
        WindowView mouseDownWin = _mouseDownWin;
        _mouseDownWin = null;

        // Create and dispatch MouseRelease event
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MouseRelease);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEventToWindow(event);
    }

    /* Only Y Axis Scrolling has been implemented */
    private void mouseWheel(WheelEvent anEvent)
    {
        WindowView win = getWindow(anEvent); if (win == null) return;
        ViewEvent event = createEvent(win, anEvent, View.Scroll);
        win.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets keyDown.
     */
    private void keyDown(KeyboardEvent keyboardEvent)
    {
        if (_activeWindow == null) return;
        ViewEvent keyPressEvent = createEvent(_activeWindow, keyboardEvent, View.KeyPress);
        _activeWindow.dispatchEventToWindow(keyPressEvent);

        // If event is typeable, send as KeyType too
        if (isTypeableKeyboardEvent(keyboardEvent)) {
            ViewEvent keyTypeEvent = createEvent(_activeWindow, keyboardEvent, View.KeyType);
            _activeWindow.dispatchEventToWindow(keyTypeEvent);
        }
    }

    /**
     * Called when body gets keyUp.
     */
    private void keyUp(KeyboardEvent keyboardEvent)
    {
        if (_activeWindow == null) return;
        ViewEvent event = createEvent(_activeWindow, keyboardEvent, View.KeyRelease);
        _activeWindow.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets TouchStart.
     */
    private void touchStart(TouchEvent anEvent)
    {
        // Get MouseDownWin for event
        _mouseDownWin = getWindow(anEvent);
        if (_mouseDownWin == null)
            return;

        // Restore focus if need be
        _focusEnabler.focus();

        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime < 400 ? (_clicks + 1) : 1; _lastReleaseTime = time;

        // If not active window, either return if active is modal or make active window
        if (_mouseDownWin != _activeWindow) {
            if (_activeWindow != null && _activeWindow.isModal()) {
                _mouseDownWin = null;
                return;
            }
            if (_mouseDownWin.isFocusable())
                activateWindow(_mouseDownWin);
        }

        // Create and dispatch MousePress event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MousePress);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);

        // If modal window activated, just return
        if (_mouseDownWin != _activeWindow)
            _mouseDownWin = null;
    }

    /**
     * Called when body gets touchMove.
     */
    private void touchMove(TouchEvent anEvent)
    {
        if (_mouseDownWin == null) return;

        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MouseDrag);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets touchEnd.
     */
    private void touchEnd(TouchEvent anEvent)
    {
        if (_mouseDownWin == null) return;

        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        WindowView mouseDownWin = _mouseDownWin;
        _mouseDownWin = null;

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MouseRelease);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * This is used to keep getting events even when mousedown goes outside window.
     */
    private void setPointerCapture(Event pointerEvent)
    {
        HTMLElement screenDiv = CJScreen.getScreenDiv();
        int id = pointerEvent.getMemberInt("pointerId");
        screenDiv.setPointerCapture(id);
    }

    /**
     * Called when browser document gets focus.
     */
    private void handleDocumentGainedFocus(Event anEvent)
    {
        // If no active window, activate top focusable window
        if (_activeWindow == null) {
            WindowView topFocusableWindow = ListUtils.findLastMatch(_openWindows, WindowView::isFocusable);
            if (topFocusableWindow != null)
                activateWindow(topFocusableWindow);
        }

        // Otherwise, focus active window
        else if (_activeWindow.isFocusable())
            ViewUtils.setFocused(_activeWindow, true);
    }

    /**
     * Called when browser document loses focus.
     */
    private void handleDocumentLostFocus(Event anEvent)
    {
        _openWindows.forEach(win -> ViewUtils.setFocused(win, false));
    }

    /**
     * Returns the WindowView for an event.
     */
    private WindowView getWindow(MouseEvent anEvent)
    {
        int x = anEvent.getPageX();
        int y = anEvent.getPageY();
        return getWindowForXY(x, y);
    }

    /**
     * Returns the WindowView for an event.
     */
    private WindowView getWindow(TouchEvent anEvent)
    {
        int x = anEvent.getPageX();
        int y = anEvent.getPageY();
        return getWindowForXY(x, y);
    }

    /**
     * Returns the WindowView for an event.
     */
    private WindowView getWindowForXY(int aX, int aY)
    {
        return ListUtils.findLastMatch(_openWindows, win -> win.isMaximized() || win.contains(aX - win.getX(), aY - win.getY()));
    }

    /**
     * Creates an Event.
     */
    private ViewEvent createEvent(WindowView aWin, Object anEvent, EventType aType)
    {
        View rootView = aWin.getRootView();
        return ViewEvent.createEvent(rootView, anEvent, aType, null);
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