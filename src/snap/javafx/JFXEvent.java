package snap.javafx;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.*;
import javafx.stage.WindowEvent;
import snap.view.*;
import snap.view.KeyCode;
import snap.view.Dragboard;

/**
 * A ViewEvent subclass for JavaFX.
 */
public class JFXEvent extends ViewEvent {

    // The mouse location
    double            _mx = Float.MIN_VALUE, _my = Float.MIN_VALUE;
    
    // The click count
    int               _ccount = -1;

/**
 * Returns the input event.
 */
public InputEvent getInputEvent()  { return getEvent(InputEvent.class); }

/**
 * Returns whether alt key is down.
 */
public boolean isAltDown()
{
    if(isMouseEvent()) return getMouseEvent().isAltDown();
    if(isKeyEvent()) return getKeyEvent().isAltDown();
    return ViewUtils.isAltDown();
}

/**
 * Returns whether control key is down.
 */
public boolean isControlDown()
{
    if(isMouseEvent()) return getMouseEvent().isControlDown();
    if(isKeyEvent()) return getKeyEvent().isControlDown();
    return ViewUtils.isControlDown();
}

/**
 * Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows).
 */
public boolean isMetaDown()
{
    if(isMouseEvent()) return getMouseEvent().isMetaDown();
    if(isKeyEvent()) return getKeyEvent().isMetaDown();
    return ViewUtils.isMetaDown();
}

/**
 * Returns whether shift key is down.
 */
public boolean isShiftDown()
{
    if(isMouseEvent()) return getMouseEvent().isShiftDown();
    if(isKeyEvent()) return getKeyEvent().isShiftDown();
    return ViewUtils.isShiftDown();
}

/** Returns whether menu shortcut key is pressed. */
public boolean isShortcutDown()
{
    if(isMouseEvent()) return getMouseEvent().isShortcutDown();
    if(isKeyEvent()) return getKeyEvent().isShortcutDown();
    return ViewUtils.isShortcutDown();
}

/** Returns whether popup trigger is down. */
public boolean isPopupTrigger()  { return isMouseEvent() && getMouseEvent().isPopupTrigger(); }

/** Returns the mouse event. */
public MouseEvent getMouseEvent()  { return getEvent(MouseEvent.class); }

/** Returns the mouse event x. */
public double getX()
{
    if(_mx!=Float.MIN_VALUE) return _mx;
    if(getMouseEvent()!=null) return _mx = getMouseEvent().getX();
    if(getDragEvent()!=null) return _mx = getDragEvent().getX();
    if(isScroll()) return _mx = getEvent(ScrollEvent.class).getX();
    return _mx = 0;
}

/** Returns the mouse event y. */
public double getY()
{
    if(_my!=Float.MIN_VALUE) return _my;
    if(getMouseEvent()!=null) return _my = getMouseEvent().getY();
    if(getDragEvent()!=null) return _my = getDragEvent().getY();
    if(isScroll()) return _my = getEvent(ScrollEvent.class).getY();
    return _my = 0;
}

/**
 * Returns the scroll amount X.
 */
public double getScrollX()
{
    ScrollEvent se = getEvent(ScrollEvent.class);
    return -se.getDeltaX()/se.getMultiplierX();
}

/**
 * Returns the scroll amount Y.
 */
public double getScrollY()
{
    ScrollEvent se = getEvent(ScrollEvent.class);
    return -se.getDeltaY()/se.getMultiplierY();
}

/** Returns the mouse event click count. */
public int getClickCount()
{
    if(_ccount>=0) return _ccount;
    return _ccount = isMouseEvent()? getMouseEvent().getClickCount() : 0;
}

/** Returns the key event. */
public KeyEvent getKeyEvent()  { return getEvent(KeyEvent.class); }

/** Returns the event keycode. */
public int getKeyCode()  { return getKeyEvent()!=null? JFX.get(getKeyEvent().getCode()) : KeyCode.CHAR_UNDEFINED; }

/** Returns the event key string. */
public String getKeyString()  { return getKeyEvent()!=null? getKeyEvent().getCharacter() : null; }

/** Returns the drag event. */
public DragEvent getDragEvent()  { return getEvent(DragEvent.class); }

/** Returns a Dragboard. */
public Dragboard getDragboard()  { return new JFXDragboard(getView(), this); }

/** Called to indicate that drop is accepted. */
public void acceptDrag()  { getDragEvent().acceptTransferModes(TransferMode.ANY); }

/** Called to indicate that drop is complete. */
public void dropComplete()  { getDragEvent().setDropCompleted(true); consume(); }

/**
 * Returns a ViewEvent at new point.
 */
public ViewEvent copyForViewPoint(snap.view.View aView, double aX, double aY, int aClickCount)
{
    String name = getName(); if(name!=null && (name.length()==0 || name.equals(getView().getName()))) name = null;
    JFXEvent copy = (JFXEvent)JFXViewEnv.get().createEvent(aView, getEvent(), getType(), name);
    copy._mx = aX; copy._my = aY; if(aClickCount>0) copy._ccount = aClickCount;
    return copy;
}

/**
 * Override to consume JFXEvent.
 */
public void consume()
{
    if(getEvent(Event.class)!=null)
        getEvent(Event.class).consume();
    super.consume();
}

/**
 * Returns ViewEvent.Type for this ViewEvent.Event.
 */
protected Type getTypeImpl()
{
    Object event = getEvent();
    if(event instanceof Event) return getType((Event)event);
    return null;
}

/**
 * Returns ViewEvent.Type for given JavaFX Event.
 */
protected static Type getType(Event anEvent)
{
    EventType etype = anEvent.getEventType();
    if(etype==ActionEvent.ACTION) return Type.Action;
    if(etype==MouseEvent.MOUSE_PRESSED) return Type.MousePressed;
    if(etype==MouseEvent.MOUSE_DRAGGED) return Type.MouseDragged;
    if(etype==MouseEvent.MOUSE_RELEASED) return Type.MouseReleased;
    if(etype==MouseEvent.MOUSE_ENTERED) return Type.MouseEntered;
    if(etype==MouseEvent.MOUSE_MOVED) return Type.MouseMoved;
    if(etype==MouseEvent.MOUSE_EXITED) return Type.MouseExited;
    if(etype==MouseEvent.DRAG_DETECTED) return Type.DragGesture;
    if(etype==ScrollEvent.SCROLL) return Type.Scroll;
    if(etype==KeyEvent.KEY_PRESSED) return Type.KeyPressed;
    if(etype==KeyEvent.KEY_RELEASED) return Type.KeyReleased;
    if(etype==KeyEvent.KEY_TYPED) return Type.KeyTyped;
    if(etype==DragEvent.DRAG_ENTERED) return Type.DragEnter;
    if(etype==DragEvent.DRAG_OVER) return Type.DragOver;
    if(etype==DragEvent.DRAG_EXITED) return Type.DragExit;
    if(etype==DragEvent.DRAG_DROPPED) return Type.DragDrop;
    if(etype==WindowEvent.WINDOW_SHOWN) return Type.WinOpened;
    if(etype==WindowEvent.WINDOW_CLOSE_REQUEST) return Type.WinClosing;
    return null;  // Return null since event object not recognized
}

}