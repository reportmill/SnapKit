package snap.swing;
import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.*;
import java.awt.event.*;
import snap.view.*;

/**
 * An ViewEvent implementation for Swing.
 */
public class SwingEvent extends ViewEvent {

    // Shortcut key mask
    private static final int SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    /**
     * Constructor.
     */
    public SwingEvent()
    {
        super();
    }

    /**
     * Returns whether alt key is down.
     */
    @Override
    public boolean isAltDown()
    {
        if (getInputEvent() != null)
            return getInputEvent().isAltDown();
        return super.isAltDown();
    }

    /**
     * Returns whether control key is down.
     */
    @Override
    public boolean isControlDown()
    {
        if (getInputEvent() != null)
            return getInputEvent().isControlDown();
        return super.isControlDown();
    }

    /**
     * Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows).
     */
    @Override
    public boolean isMetaDown()
    {
        if (getInputEvent() != null)
            return getInputEvent().isMetaDown();
        return super.isMetaDown();
    }

    /**
     * Returns whether shift key is down.
     */
    @Override
    public boolean isShiftDown()
    {
        if (getInputEvent() != null)
            return getInputEvent().isShiftDown();
        return super.isShiftDown();
    }

    /**
     * Returns whether shortcut key is pressed.
     */
    @Override
    public boolean isShortcutDown()
    {
        if (getInputEvent() != null)
            return (getInputEvent().getModifiersEx() & SHORTCUT_KEY_MASK) > 0;
        return super.isShortcutDown();
    }

    /**
     * Returns whether popup trigger is down.
     */
    @Override
    public boolean isPopupTrigger()
    {
        return isMouseEvent() && getEvent(MouseEvent.class).isPopupTrigger();
    }

    /**
     * Returns the location for a mouse event or drop event.
     */
    @Override
    protected snap.geom.Point getPointImpl()
    {
        Point pnt = getLocation();
        return new snap.geom.Point(pnt.getX(), pnt.getY());
    }

    /**
     * Returns the scroll amount X.
     */
    @Override
    public double getScrollY()
    {
        MouseWheelEvent me = getEvent(MouseWheelEvent.class);
        if (me == null || me.isShiftDown()) return 0;
        return me.getUnitsToScroll();
    }

    /**
     * Returns the scroll amount X.
     */
    @Override
    public double getScrollX()
    {
        MouseWheelEvent me = getEvent(MouseWheelEvent.class);
        if (me == null || !me.isShiftDown()) return 0;
        return me.getUnitsToScroll();
    }

    /**
     * Returns the event keycode.
     */
    @Override
    public int getKeyCode()
    {
        KeyEvent keyEvent = getKeyEvent();
        return keyEvent != null ? keyEvent.getKeyCode() : 0;
    }

    /**
     * Returns the event key char.
     */
    @Override
    public char getKeyChar()
    {
        KeyEvent keyEvent = getKeyEvent();
        return keyEvent != null ? keyEvent.getKeyChar() : 0;
    }

    /**
     * Called to indicate that drop is accepted.
     */
    @Override
    public void acceptDrag()
    {
        Object dragEvent = getEvent();
        if (dragEvent instanceof DropTargetDragEvent dropTargetDragEvent)
            dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY);
        else if (dragEvent instanceof DropTargetDropEvent dropTargetDropEvent)
            dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
    }

    /**
     * Called to indicate that drop is complete.
     */
    @Override
    public void dropComplete()
    {
        getEvent(DropTargetDropEvent.class).dropComplete(true);
    }

    /**
     * Returns the drag Clipboard for this event.
     */
    @Override
    public Clipboard getClipboard()
    {
        return SwingClipboard.getDrag(this);
    }

    /**
     * Consume event.
     */
    @Override
    public void consume()
    {
        super.consume();
        if (getInputEvent() != null)
            getInputEvent().consume();
    }

    /**
     * Computes the event type from EventObject.
     */
    @Override
    protected Type getTypeImpl()
    {
        Object event = getEvent();
        int id = event instanceof AWTEvent ? ((AWTEvent) event).getID() : 0;
        switch (id) {
            case ActionEvent.ACTION_PERFORMED: return Type.Action;
            case MouseEvent.MOUSE_PRESSED: return Type.MousePress;
            case MouseEvent.MOUSE_DRAGGED: return Type.MouseDrag;
            case MouseEvent.MOUSE_RELEASED: return Type.MouseRelease;
            case MouseEvent.MOUSE_ENTERED: return Type.MouseEnter;
            case MouseEvent.MOUSE_MOVED: return Type.MouseMove;
            case MouseEvent.MOUSE_EXITED: return Type.MouseExit;
            case MouseEvent.MOUSE_WHEEL: return Type.Scroll;
            case KeyEvent.KEY_PRESSED: return Type.KeyPress;
            case KeyEvent.KEY_RELEASED: return Type.KeyRelease;
            case KeyEvent.KEY_TYPED: return Type.KeyType;
        }
        if (event instanceof DropTargetDropEvent)
            return Type.DragDrop;
        if (event instanceof DragGestureEvent)
            return Type.DragGesture;
        return null;
    }

    /**
     * Returns the input event.
     */
    private InputEvent getInputEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent())
            if (viewEvent.getEvent() instanceof InputEvent inputEvent)
                return inputEvent;
        return null;
    }

    /**
     * Returns the key event.
     */
    private KeyEvent getKeyEvent()
    {
        for (ViewEvent viewEvent = this; viewEvent != null; viewEvent = viewEvent.getParentEvent())
            if (viewEvent.getEvent() instanceof KeyEvent keyEvent)
                return keyEvent;
        return null;
    }

    /**
     * Returns the location for a mouse event or drop event.
     */
    private Point getLocation()
    {
        Object event = getEvent();
        if (event instanceof DragGestureEvent dragGestureEvent)
            event = dragGestureEvent.getTriggerEvent();
        if (event instanceof MouseEvent mouseEvent)
            return mouseEvent.getPoint();
        if (event instanceof DropTargetDragEvent dropTargetDragEvent)
            return dropTargetDragEvent.getLocation();
        if (event instanceof DropTargetDropEvent dropTargetDropEvent)
            return dropTargetDropEvent.getLocation();
        return new Point();
    }
}