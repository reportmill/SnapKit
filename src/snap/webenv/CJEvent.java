package snap.webenv;
import snap.geom.Point;
import snap.view.*;
import snap.view.Clipboard;
import snap.webapi.*;

/**
 * A ViewEvent implementation for CheerpJ.
 */
public class CJEvent extends ViewEvent {

    // Whether platform is windows
    private static boolean _isWindows = Navigator.isWindows();

    /**
     * Returns the event point from browser mouse event.
     */
    @Override
    protected Point getPointImpl()
    {
        // Handle MouseEvent
        MouseEvent mouseEvent = getMouseEvent();
        if (mouseEvent != null)
            return getPointForMouseEvent(mouseEvent);

        // Handle TouchEvent
        TouchEvent touchEvent = getTouchEvent();
        if (touchEvent != null)
            return getPointForTouchEvent(touchEvent);

        // Handle unknown event type (Currently called by ViewEvent.copyForView())
        //System.out.println("CJEvent.getPointImpl: Unsupported event type: " + event.getType());
        return Point.ZERO;
    }

    /**
     * Returns the event point from browser MouseEvent.
     */
    private Point getPointForMouseEvent(MouseEvent mouseEvent)
    {
        // Get event X/Y and convert to view
        View view = getView();
        WindowView window = view.getWindow(); // Can be null for MouseExit sent to removed view
        boolean winMaximized = window != null && window.isMaximized();
        double viewX = winMaximized ? mouseEvent.getClientX() : mouseEvent.getPageX(); viewX = Math.round(viewX);
        double viewY = winMaximized ? mouseEvent.getClientY() : mouseEvent.getPageY(); viewY = Math.round(viewY);
        Point point = view.parentToLocal(viewX, viewY, null);
        return point;
    }

    /**
     * Returns the event point from browser TouchEvent.
     */
    private Point getPointForTouchEvent(TouchEvent touchEvent)
    {
        // Get event X/Y and convert to view
        View view = getView();
        boolean winMaximized = view.getWindow().isMaximized();
        double viewX = winMaximized ? touchEvent.getClientX() : touchEvent.getPageX(); viewX = Math.round(viewX);
        double viewY = winMaximized ? touchEvent.getClientY() : touchEvent.getPageY(); viewY = Math.round(viewY);
        Point point = view.parentToLocal(viewX,viewY, null);
        return point;
    }

    /**
     * Returns the scroll amount for a wheel event.
     */
    public double getScrollX()
    {
        MouseEvent mouseEvent = getMouseEvent();
        WheelEvent wheelEvent = (WheelEvent) mouseEvent;
        return wheelEvent.getDeltaX();
    }

    /**
     * Returns the scroll amount for a wheel event.
     */
    public double getScrollY()
    {
        MouseEvent mouseEvent = getMouseEvent();
        WheelEvent wheelEvent = (WheelEvent) mouseEvent;
        return wheelEvent.getDeltaY();
    }

    /**
     * Returns the event keycode.
     */
    @Override
    public int getKeyCode()
    {
        KeyboardEvent keyboardEvent = getKeyEvent();
        int keyCode = keyboardEvent.getKeyCode();

        // Remap some codes
        switch (keyCode) {
            case 13: return KeyCode.ENTER;
            case 91: return KeyCode.COMMAND;
            case 46: return KeyCode.DELETE;
            default: return keyCode;
        }
    }

    /** Returns the event key char. */
    @Override
    public char getKeyChar()
    {
        // If KeyString is valid, return first char
        String keyString = getKeyString();
        if (keyString.length() == 1)
            return keyString.charAt(0);

        // Handle some known values
        switch (keyString) {
            case "Backspace": return 8;
            case "Delete": return 127;
        }

        // Return 0
        return KeyCode.CHAR_UNDEFINED;
    }

    /**
     * Returns the event key char.
     */
    @Override
    public String getKeyString()
    {
        KeyboardEvent keyboardEvent = getKeyEvent();
        return keyboardEvent.getKey();
    }

    /**
     * Returns whether shift key is down.
     */
    public boolean isShiftDown()
    {
        UIEvent uiEvent = getUIEvent();
        if (uiEvent != null)
            return uiEvent.isShiftKey();
        return false;
    }

    /**
     * Returns whether control key is down.
     */
    public boolean isControlDown()
    {
        UIEvent uiEvent = getUIEvent();
        if (uiEvent != null)
            return uiEvent.isCtrlKey();
        return false;
    }

    /**
     * Returns whether alt key is down.
     */
    public boolean isAltDown()
    {
        UIEvent uiEvent = getUIEvent();
        if (uiEvent != null)
            return uiEvent.isAltKey();
        return false;
    }

    /**
     * Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows).
     */
    public boolean isMetaDown()
    {
        UIEvent uiEvent = getUIEvent();
        if (uiEvent != null)
            return uiEvent.isMetaKey();
        return false;
    }

    /**
     * Returns whether shortcut key is pressed.
     */
    public boolean isShortcutDown()
    {
        KeyboardEvent keyEvent = getKeyEvent();
        if (keyEvent != null) {
            if (_isWindows)
                return keyEvent.isCtrlKey();
            return keyEvent.isMetaKey();
        }

        UIEvent uiEvent = getUIEvent();
        if (uiEvent != null) {
            if (_isWindows)
                return isControlDown();
            return isMetaDown();
        }
        return false;
    }

    /**
     * Returns whether popup trigger is down.
     */
    public boolean isPopupTrigger()
    {
        MouseEvent mouseEvent = getMouseEvent();
        return mouseEvent != null && mouseEvent.getButton() == MouseEvent.RIGHT_BUTTON;
    }

    /**
     * Returns the UIEvent (or null, if not available).
     */
    private UIEvent getUIEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent()) {
            Object eventObj = viewEvent.getEvent();
            if (eventObj instanceof UIEvent)
                return (UIEvent) eventObj;
        }

        return null;
    }

    /**
     * Returns the KeyboardEvent (or null, if not available).
     */
    private KeyboardEvent getKeyEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent()) {
            Object eventObj = viewEvent.getEvent();
            if (eventObj instanceof KeyboardEvent)
                return (KeyboardEvent) eventObj;
        }

        return null;
    }

    /**
     * Returns the MouseEvent (or null, if not available).
     */
    private MouseEvent getMouseEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent()) {
            Object eventObj = viewEvent.getEvent();
            if (eventObj instanceof MouseEvent)
                return (MouseEvent) eventObj;
        }

        return null;
    }

    /**
     * Returns the JSO TouchEvent (or null, if not available).
     */
    private TouchEvent getTouchEvent()
    {
        for (ViewEvent ve = this; ve != null; ve = ve.getParentEvent()) {
            Object eventObj = ve.getEvent();
            if (eventObj instanceof TouchEvent)
                return (TouchEvent) getEvent();
        }

        return null;
    }

    /**
     * Returns the event type.
     */
    protected Type getTypeImpl()
    {
        Event event = (Event) getEvent();
        String type = event.getType();
        switch(type) {
            case "dragstart": return Type.DragGesture;
            case "dragend": return Type.DragSourceEnd;
            case "dragenter": return Type.DragEnter;
            case "dragexit": return Type.DragExit;
            case "dragover": return Type.DragOver;
            case "drop": return Type.DragDrop;
            default: return null;
        }
    }

    /**
     * Returns the drag Clipboard for this event.
     */
    public Clipboard getClipboard()
    {
        return CJDragboard.getDrag(this);
    }

    /**
     * Called to indicate that drop is accepted.
     */
    public void acceptDrag()
    {
        CJDragboard.getDrag(this).acceptDrag();
    }

    /**
     * Called to indicate that drop is complete.
     */
    public void dropComplete()
    {
        CJDragboard.getDrag(this).dropComplete();
    }
}