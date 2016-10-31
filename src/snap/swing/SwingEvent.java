package snap.swing;
import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.*;
import java.awt.event.*;
import snap.view.*;

/**
 * A custom class.
 */
public class SwingEvent extends ViewEvent {
    
    // The mouse location
    double            _mx = Float.MIN_VALUE, _my = Float.MIN_VALUE;
    
    // The click count
    int               _ccount = -1;
    
    // Shortcut key mask
    private static final int SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

/**
 * Returns the input event.
 */
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
 * Returns the click count for a mouse event.
 */
public int getClickCount()
{
    if(_ccount>=0) return _ccount;
    MouseEvent me = getEvent(MouseEvent.class);
    return _ccount = me!=null? me.getClickCount() : 0;
}

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

/** Returns the mouse event x. */
public double getX()  { return _mx!=Float.MIN_VALUE? _mx : (_mx=getLocation().getX()); }

/** Returns the mouse event y. */
public double getY()  { return _my!=Float.MIN_VALUE? _my : (_my=getLocation().getY()); }

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

/** Returns a Dragboard for this event. */
public Dragboard getDragboard()  { return new SwingDragboard(getView(), this); }

/**
 * Returns a ViewEvent at new point.
 */
public ViewEvent copyForViewPoint(View aView, double aX, double aY, int aClickCount)
{
    String name = getName(); if(name!=null && (name.length()==0 || name.equals(getView().getName()))) name = null;
    SwingEvent copy = (SwingEvent)SwingViewEnv.get().createEvent(aView, getEvent(), getType(), name);
    copy._mx = aX; copy._my = aY; if(aClickCount>0) copy._ccount = aClickCount;
    return copy;
}

/**
 * Consume event.
 */
public void consume()
{
    if(getInputEvent()!=null) getInputEvent().consume();
    super.consume();
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
        case MouseEvent.MOUSE_PRESSED: return Type.MousePressed;
        case MouseEvent.MOUSE_DRAGGED: return Type.MouseDragged;
        case MouseEvent.MOUSE_RELEASED: return Type.MouseReleased;
        case MouseEvent.MOUSE_ENTERED: return Type.MouseEntered;
        case MouseEvent.MOUSE_MOVED: return Type.MouseMoved;
        case MouseEvent.MOUSE_EXITED: return Type.MouseExited;
        case MouseEvent.MOUSE_WHEEL: return Type.Scroll;
        case KeyEvent.KEY_PRESSED: return Type.KeyPressed;
        case KeyEvent.KEY_RELEASED: return Type.KeyReleased;
        case KeyEvent.KEY_TYPED: return Type.KeyTyped;
        case WindowEvent.WINDOW_ACTIVATED: return Type.WinActivated;
        case WindowEvent.WINDOW_CLOSING: return Type.WinClosing;
        case WindowEvent.WINDOW_DEACTIVATED: return Type.WinDeactivated;
        case WindowEvent.WINDOW_OPENED: return Type.WinOpened;
    }
    if(event instanceof DropTargetDropEvent) return Type.DragDrop;
    if(event instanceof DragGestureEvent) return Type.DragGesture;
    return null;
}

}