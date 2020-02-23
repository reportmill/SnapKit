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
    private static final int SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

/** Returns the input event. */
private InputEvent getInputEvent()  { return getEvent(InputEvent.class); }

/** Returns whether alt key is down. */
public boolean isAltDown()
{
    if(getInputEvent()!=null) return getInputEvent().isAltDown();
    return ViewUtils.isAltDown();
}

/** Returns whether control key is down. */
public boolean isControlDown()
{
    if(getInputEvent()!=null) return getInputEvent().isControlDown();
    return ViewUtils.isControlDown();
}

/** Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows). */
public boolean isMetaDown()
{
    if(getInputEvent()!=null) return getInputEvent().isMetaDown();
    return ViewUtils.isMetaDown();
}

/** Returns whether shift key is down. */
public boolean isShiftDown()
{
    if(getInputEvent()!=null) return getInputEvent().isShiftDown();
    return ViewUtils.isShiftDown();
}

/** Returns whether shortcut key is pressed. */
public boolean isShortcutDown()
{
    if(getInputEvent()!=null) return (getInputEvent().getModifiers() & SHORTCUT_KEY_MASK)>0;
    return ViewUtils.isShortcutDown();
}

/** Returns whether popup trigger is down. */
public boolean isPopupTrigger()  { return isMouseEvent() && getEvent(MouseEvent.class).isPopupTrigger(); }

/**
 * Returns the location for a mouse event or drop event.
 */
private Point getLocation()
{
    Object event = getEvent();
    if(event instanceof DragGestureEvent)
        event = ((DragGestureEvent)event).getTriggerEvent();
    if(event instanceof MouseEvent) { MouseEvent me = (MouseEvent)event;
        return me.getPoint(); }
    if(event instanceof DropTargetDragEvent) { DropTargetDragEvent de = (DropTargetDragEvent)event;
        return de.getLocation(); }
    if(event instanceof DropTargetDropEvent) { DropTargetDropEvent de = (DropTargetDropEvent)event;
        return de.getLocation(); }
    return new Point();
}

/**
 * Returns the location for a mouse event or drop event.
 */
protected snap.geom.Point getPointImpl()
{
    Point pnt = getLocation();
    return new snap.geom.Point(pnt.getX(), pnt.getY());
}

/**
 * Returns the scroll amount X.
 */
public double getScrollY()
{
    MouseWheelEvent me = getEvent(MouseWheelEvent.class); if(me==null || me.isShiftDown()) return 0;
    return me.getUnitsToScroll();
}

/**
 * Returns the scroll amount X.
 */
public double getScrollX()
{
    MouseWheelEvent me = getEvent(MouseWheelEvent.class); if(me==null || !me.isShiftDown()) return 0;
    return me.getUnitsToScroll();
}

/**
 * Returns the key event.
 */
private KeyEvent getKeyEvent()  { return getEvent(KeyEvent.class); }

/** Returns the event keycode. */
public int getKeyCode()  { return getKeyEvent()!=null? getKeyEvent().getKeyCode() : 0; }

/** Returns the event key char. */
public char getKeyChar()  { return getKeyEvent()!=null? getKeyEvent().getKeyChar() : 0; }

/** Called to indicate that drop is accepted. */
public void acceptDrag()
{
    DropTargetDragEvent dragEv = getEvent(DropTargetDragEvent.class);
    if(dragEv!=null) dragEv.acceptDrag(DnDConstants.ACTION_COPY);
    DropTargetDropEvent dropEv = getEvent(DropTargetDropEvent.class);
    if(dropEv!=null) dropEv.acceptDrop(DnDConstants.ACTION_COPY);
}

/** Called to indicate that drop is complete. */
public void dropComplete()  { getEvent(DropTargetDropEvent.class).dropComplete(true); }

/** Returns the drag Clipboard for this event. */
public Clipboard getClipboard()  { return SwingClipboard.getDrag(this); }

/**
 * Consume event.
 */
public void consume()
{
    super.consume();
    if(getInputEvent()!=null) getInputEvent().consume();
}

/**
 * Computes the event type from EventObject.
 */
protected Type getTypeImpl()
{
    Object event = getEvent();
    int id = event instanceof AWTEvent? ((AWTEvent)event).getID() : 0;
    switch(id) {
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
        case WindowEvent.WINDOW_ACTIVATED: return Type.WinActivate;
        case WindowEvent.WINDOW_CLOSING: return Type.WinClose;
        case WindowEvent.WINDOW_DEACTIVATED: return Type.WinDeactivate;
        case WindowEvent.WINDOW_OPENED: return Type.WinOpen;
    }
    if(event instanceof DropTargetDropEvent) return Type.DragDrop;
    if(event instanceof DragGestureEvent) return Type.DragGesture;
    return null;
}

}