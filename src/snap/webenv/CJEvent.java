package snap.webenv;
import snap.geom.Point;
import snap.util.SnapEnv;
import snap.view.*;
import snap.view.Clipboard;
import snap.webapi.*;

/**
 * A ViewEvent implementation for CheerpJ.
 */
public class CJEvent extends ViewEvent {

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
        return mouseEvent instanceof WheelEvent wheelEvent ? wheelEvent.getDeltaX() : 0;
    }

    /**
     * Returns the scroll amount for a wheel event.
     */
    public double getScrollY()
    {
        MouseEvent mouseEvent = getMouseEvent();
        return mouseEvent instanceof WheelEvent wheelEvent ? wheelEvent.getDeltaY() : 0;
    }

    /**
     * Returns the event keycode.
     */
    @Override
    public int getKeyCode()
    {
        KeyboardEvent keyboardEvent = getKeyEvent(); if (keyboardEvent == null) return 0;
        int keyCode = keyboardEvent.getKeyCode();

        // Remap some codes
        return switch (keyCode) {
            case 13 -> KeyCode.ENTER;
            case 91 -> KeyCode.META;
            case 46 -> KeyCode.DELETE;
            default -> keyCode;
        };
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
        return switch (keyString) {
            case "Backspace" -> '\b';
            case "Delete" -> 127;
            case "Tab" -> '\t';
            default -> KeyCode.CHAR_UNDEFINED;
        };
    }

    /**
     * Returns the event key char.
     */
    @Override
    public String getKeyString()
    {
        KeyboardEvent keyboardEvent = getKeyEvent(); if (keyboardEvent == null) return "";
        String keyString = keyboardEvent.getKey();
        if (keyString.equals("Tab"))
            return "\t";
        return keyString;
    }

    /**
     * Returns whether shift key is down.
     */
    public boolean isShiftDown()
    {
        UIEvent uiEvent = getUIEvent();
        return uiEvent != null && uiEvent.isShiftKey();
    }

    /**
     * Returns whether control key is down.
     */
    public boolean isControlDown()
    {
        UIEvent uiEvent = getUIEvent();
        return uiEvent != null && uiEvent.isCtrlKey();
    }

    /**
     * Returns whether alt key is down.
     */
    public boolean isAltDown()
    {
        UIEvent uiEvent = getUIEvent();
        return uiEvent != null && uiEvent.isAltKey();
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
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent())
            if (viewEvent.getEvent() instanceof UIEvent uiEvent)
                return uiEvent;
        return null;
    }

    /**
     * Returns the KeyboardEvent (or null, if not available).
     */
    private KeyboardEvent getKeyEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent())
            if (viewEvent.getEvent() instanceof KeyboardEvent keyboardEvent)
                return keyboardEvent;
        return null;
    }

    /**
     * Returns the MouseEvent (or null, if not available).
     */
    private MouseEvent getMouseEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent())
            if (viewEvent.getEvent() instanceof MouseEvent mouseEvent)
                return mouseEvent;
        return null;
    }

    /**
     * Returns the JSO TouchEvent (or null, if not available).
     */
    private TouchEvent getTouchEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent())
            if (viewEvent.getEvent() instanceof TouchEvent touchEvent)
                return touchEvent;
        return null;
    }

    /**
     * Returns the event type.
     */
    protected EventType getTypeImpl()
    {
        Event event = (Event) getEvent();
        String type = event.getType();
        return switch (type) {
            case "dragstart" -> EventType.DragGesture;
            case "dragend" -> EventType.DragSourceEnd;
            case "dragenter" -> EventType.DragEnter;
            case "dragexit" -> EventType.DragExit;
            case "dragover" -> EventType.DragOver;
            case "drop" -> EventType.DragDrop;
            default -> null;
        };
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