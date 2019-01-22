/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.Point;
import snap.util.*;

/**
 * Represents node UI events sent to a Views for input events.
 */
public abstract class ViewEvent {
    
    // The node that this event is associated with
    View                _view;
    
    // The platform specific event, if available
    Object              _event;

    // The name of the event node
    String              _name = "";
    
    // The UI event type
    Type                _type;
    
    // When the event was sent
    long                _when;
    
    // The click count
    int                 _clickCount = 1;
    
    // Whether event was consumed
    boolean             _consumed;
    
    // Whether event triggers a UI reset
    boolean             _triggersReset = true;
    
    // Constants for double/triple click constraints
    static long         CLICK_TIME = 550;
    static double       CLICK_DIST = 1;

/**
 * Returns the node associated with this event.
 */
public View getView()  { return _view; }

/**
 * Returns the event node as given class.
 */
public <T> T getView(Class <T> aClass)  { return ClassUtils.getInstance(getView(), aClass); }

/**
 * Returns the node associated with this event.
 */
public void setView(View aView)  { _view = aView; }

/**
 * Returns the platform specific event, if available.
 */
public Object getEvent()  { return _event; }

/**
 * Sets the platform specific event, if available.
 */
public void setEvent(Object anEvent)  { _event = anEvent; }

/**
 * Returns the platform specific event as given class.
 */
public <T> T getEvent(Class<T> aClass)  { return ClassUtils.getInstance(getEvent(), aClass); }

/**
 * Returns the name of the event (or the name of the Event Target).
 */
public String getName()  { return _name; }

/**
 * Returns the name of the event (or the name of the Event Target).
 */
public void setName(String aName)  { _name = aName!=null? aName : ""; }

/**
 * Returns the event type.
 */
public Type getType()  { return _type!=null? _type : (_type=getTypeImpl()); }

/**
 * Returns the event type.
 */
protected abstract Type getTypeImpl();

/**
 * Sets the event type.
 */
public void setType(Type aType)  { _type = aType; }

/**
 * Returns the time the event was sent.
 */
public long getWhen()  { return _when>0? _when : (_when=System.currentTimeMillis()); }

/**
 * Returns whether event is action event.
 */
public boolean isActionEvent()  { return getType()==Type.Action; }

/**
 * Returns whether event is mouse event.
 */
public boolean isMouseEvent()
{
    Type t = getType();
    return t==Type.MouseEnter || t==Type.MouseMove || t==Type.MouseExit || t==Type.MousePress ||
        t==Type.MouseDrag || t==Type.MouseRelease;
}

/**
 * Returns whether event is mouse pressed.
 */
public boolean isMousePress()  { return getType()==Type.MousePress; }

/**
 * Returns whether event is mouse dragged.
 */
public boolean isMouseDrag()  { return getType()==Type.MouseDrag; }

/**
 * Returns whether event is mouse released.
 */
public boolean isMouseRelease()  { return getType()==Type.MouseRelease; }

/**
 * Returns whether event is mouse clicked.
 */
public boolean isMouseClick()  { return isMouseRelease() && isClickCandidate(); }

/**
 * Returns whether event is mouse event in mouse click range (within half second of down
 */
public boolean isClickCandidate()
{
    // If event not within half second of last mouse, return false
    ViewEvent last = ViewUtils.getMouseDown(); if(last==null) return false;
    if(getWhen() - last.getWhen() >= CLICK_TIME) return false;
    
    // If event is in another window, return false
    if(getView().getWindow()!=last.getView().getWindow()) return false;
    
    // If event not within 1 point of last mouse, return false
    Point pnt = getView()==last.getView()? last.getPoint() : last.getPoint(getView());
    if(Math.abs(getX()-pnt.getX())>CLICK_DIST || Math.abs(getY()-pnt.getY())>CLICK_DIST) return false;
    
    // Return true since passed tests
    return true;
}

/**
 * Returns whether event is mouse entered.
 */
public boolean isMouseEnter()  { return getType()==Type.MouseEnter; }

/**
 * Returns whether event is mouse moved.
 */
public boolean isMouseMove()  { return getType()==Type.MouseMove; }

/**
 * Returns whether event is mouse exited.
 */
public boolean isMouseExit()  { return getType()==Type.MouseExit; }

/**
 * Returns whether event is key event.
 */
public boolean isKeyEvent() { Type t = getType(); return t==Type.KeyPress || t==Type.KeyType || t==Type.KeyRelease;}

/**
 * Returns whether event is key pressed.
 */
public boolean isKeyPress()  { return getType()==Type.KeyPress; }

/**
 * Returns whether event is key released.
 */
public boolean isKeyRelease()  { return getType()==Type.KeyRelease; }

/**
 * Returns whether event is key typed.
 */
public boolean isKeyType()  { return getType()==Type.KeyType; }

/**
 * Returns whether event is scroll.
 */
public boolean isScroll()  { return getType()==Type.Scroll; }

/**
 * Returns whether event is any drag event.
 */
public boolean isDragEvent()
{
    Type t = getType(); return t==Type.DragEnter || t==Type.DragOver || t==Type.DragExit || t==Type.DragDrop;
}

/**
 * Returns whether event is drag enter.
 */
public boolean isDragEnter()  { return getType()==Type.DragEnter; }

/**
 * Returns whether event is drag over.
 */
public boolean isDragOver()  { return getType()==Type.DragOver; }

/**
 * Returns whether event is drag exit.
 */
public boolean isDragExit()  { return getType()==Type.DragExit; }

/**
 * Returns whether event is drop event.
 */
public boolean isDragDrop()  { return getType()==Type.DragDrop; }

/**
 * Returns whether event is drop event.
 */
public boolean isDragDropEvent()  { return getType()==Type.DragDrop; }

/**
 * Returns whether event is drag source event.
 */
public boolean isDragSourceEvent()
{
    Type t = getType(); return t==Type.DragGesture || t==Type.DragSourceEnter || t==Type.DragSourceOver ||
        t==Type.DragSourceExit || t==Type.DragSourceEnd;
}

/**
 * Returns whether event is DragGesture event.
 */
public boolean isDragGesture()  { return getType()==Type.DragGesture; }

/**
 * Returns whether event is DragSourceEnter event.
 */
public boolean isDragSourceEnter()  { return getType()==Type.DragSourceEnter; }

/**
 * Returns whether event is DragSourceExit event.
 */
public boolean isDragSourceExit()  { return getType()==Type.DragSourceExit; }

/**
 * Returns whether event is DragSourceEnd event.
 */
public boolean isDragSourceEnd()  { return getType()==Type.DragSourceEnd; }

/**
 * Returns whether event is WinActivated.
 */
public boolean isWinActivate()  { return getType()==Type.WinActivate; }

/**
 * Returns whether event is WinDeactivated.
 */
public boolean isWinDeativate()  { return getType()==Type.WinDeactivate; }

/**
 * Returns whether event is WinOpened.
 */
public boolean isWinOpen()  { return getType()==Type.WinOpen; }

/**
 * Returns whether event is WinClosing.
 */
public boolean isWinClose()  { return getType()==Type.WinClose; }

/**
 * Returns the value encapsulated by the event widget.
 */
public Object getValue()
{
    // Handle DragDropEvent: Return String value
    if(isDragDropEvent())
        return getClipboard().getString();
    
    // Otherwise, return node value
    View view = getView(); return view!=null? view.getValue("Value") : null;
}

/**
 * Sets the value encapsulated by the event widget.
 */
public void setValue(Object aValue)  { getView().setValue("Value", aValue); }

/**
 * Returns the String value encapsulated by the event widget.
 */
public String getStringValue()  { return SnapUtils.stringValue(getValue()); }

/**
 * Returns the Boolean value encapsulated by the event widget.
 */
public boolean getBoolValue()  { return SnapUtils.booleanValue(getValue()); }

/**
 * Returns the Boolean value encapsulated by the event widget.
 */
public Boolean getBooleanValue()  { return SnapUtils.booleanValue(getValue()); }

/**
 * Returns the Integer value encapsulated by the event widget.
 */
public Integer getIntValue()  { return SnapUtils.getInteger(getValue()); }

/**
 * Returns the Float value encapsulated by the event widget.
 */
public Float getFloatValue()  { return SnapUtils.getFloat(getValue()); }

/**
 * Returns text for encapsulated widget.
 */
public String getText()  { return getView().getText(); }

/**
 * Returns the selected index for encapsulated widget.
 */
public int getSelIndex()  { return getView(View.Selectable.class).getSelIndex(); }

/**
 * Returns the selected item for encapsulated widget.
 */
public Object getSelItem()  { return getView(View.Selectable.class).getSelItem(); }

/** Returns whether shift key is down. */
public boolean isShiftDown()  { return false; }

/** Returns whether control key is down. */
public boolean isControlDown()  { return false; }

/** Returns whether alt key is down. */
public boolean isAltDown()  { return false; }

/** Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows). */
public boolean isMetaDown()  { return false; }

/** Returns whether shortcut key is pressed. */
public boolean isShortcutDown()  { return false; }

/** Returns the key combo. */
public KeyCombo getKeyCombo()
{
    if(_kcombo!=null) return _kcombo;
    return _kcombo = new KeyCombo(getKeyCode(), isAltDown(), isMetaDown(), isControlDown(), isShiftDown());
} KeyCombo _kcombo;

/**
 * Returns whether popup trigger is down.
 */
public boolean isPopupTrigger()  { return false; }

/**
 * Returns the click count for a mouse event.
 */
public int getClickCount()  { return _clickCount; }

/**
 * Sets the click count for a mouse event.
 */
protected void setClickCount(int aValue)  { _clickCount = aValue; }

/**
 * Returns the mouse event x.
 */
public double getX()  { return 0; }

/**
 * Returns the mouse event y.
 */
public double getY()  { return 0; }

/**
 * Returns the event location.
 */
public Point getPoint()  { return new Point(getX(),getY()); }

/**
 * Returns the event location in coords of given view.
 */
public Point getPoint(View aView)
{
    Point pt = getPoint(); View view0 = getView();
    View ancestor = ViewUtils.getCommonAncetor(view0,aView);
    if(ancestor!=view0)
        pt = view0.localToParent(pt.x, pt.y, ancestor);
    if(ancestor!=aView)
        pt = aView.parentToLocal(pt.x, pt.y, ancestor);
    return pt;
}

/** Returns the scroll amount X. */
public double getScrollX()  { complain("getScrollAmountX"); return 0; }

/** Returns the scroll amount Y. */
public double getScrollY()  { complain("getScrollAmountY"); return 0; }

/** Returns the event keycode. */
public int getKeyCode()  { complain("getKeyCode"); return 0; }

/** Returns the event key char. */
public char getKeyChar()  { String s = getKeyString(); return s!=null && s.length()>0? s.charAt(0) : (char)0; }

/** Returns the event key char. */
public String getKeyString()  { return String.valueOf(getKeyChar()); }

/** Returns whether key is left arrow. */
public boolean isLeftArrow()  { return getKeyCode()==KeyCode.LEFT; }

/** Returns whether key is right arrow. */
public boolean isRightArrow()  { return getKeyCode()==KeyCode.RIGHT; }

/** Returns whether key is up arrow. */
public boolean isUpArrow()  { return getKeyCode()==KeyCode.UP; }

/** Returns whether key is down arrow. */
public boolean isDownArrow()  { return getKeyCode()==KeyCode.DOWN; }

/** Returns whether key is delete key. */
public boolean isBackSpaceKey()  { return getKeyCode()==KeyCode.BACK_SPACE || getKeyChar()=='\b'; }

/** Returns whether key is delete key. */
public boolean isDeleteKey()  { return getKeyCode()==KeyCode.DELETE; }

/** Returns whether key is enter key. */
public boolean isEnterKey()  { return getKeyCode()==KeyCode.ENTER || getKeyChar()=='\n'; }

/** Returns whether key is tab key. */
public boolean isTabKey()  { return getKeyCode()==KeyCode.TAB || getKeyChar()=='\t'; }

/** Returns whether key is escape key. */
public boolean isEscapeKey()  { return getKeyCode()==KeyCode.ESCAPE; }

/** Returns whether key is escape key. */
public boolean isSpaceKey()  { return getKeyCode()==KeyCode.SPACE || getKeyChar()==' '; }

/** Returns whether key is ISO control character. */
public boolean isControlChar()  { char c = getKeyChar(); return Character.isISOControl(c); }

/** Returns the Drag Clipboard for this event, if appropriate. */
public Clipboard getClipboard()  { complain("getDragboard"); return null; }

/** Called to indicate that drop is accepted. */
public void acceptDrag()  { complain("acceptDrag"); }

/** Called to indicate that drop is complete. */
public void dropComplete()  { complain("dropComplete"); }

/** Returns whether event was consumed. */
public boolean isConsumed()  { return _consumed; }

/** Consume event. */
public void consume()  { _consumed = true; }

/**
 * Returns whether this event triggers a UI reset.
 */
public boolean getTriggersReset()  { return _triggersReset; }

/**
 * Sets whether this event triggers a UI reset.
 */
public void setTriggersReset(boolean aValue)  { _triggersReset = aValue; }

/**
 * Returns a ViewEvent at new point.
 */
public ViewEvent copyForView(View aView)
{
    View thisView = getView(); double x = getX(), y = getY();
    View par = ViewUtils.getCommonAncetor(thisView, aView);
    snap.gfx.Point point = par==thisView? aView.parentToLocal(x,y,par) : thisView.localToParent(x,y,par);
    return copyForViewPoint(aView, point.x, point.y, -1);
}

/**
 * Returns a ViewEvent at new point.
 */
public ViewEvent copyForPoint(double aX, double aY)  { return copyForViewPoint(getView(), aX, aY, -1); }

/**
 * Returns a ViewEvent at new click count.
 */
public ViewEvent copyForClickCount(int aClickCount)
{
    return copyForViewPoint(getView(), getX(),getY(), aClickCount);
}

/**
 * Returns a ViewEvent at new point.
 */
public ViewEvent copyForViewPoint(View aView, double aX, double aY, int aClickCount)  { return null; }

/**
 * Returns whether event represents component with given name.
 */
public boolean is(String aName)  { return getName().equals(aName); }

/**
 * Returns whether widget is equal to given name.
 */
public boolean equals(String aName)  { return getName().equals(aName); }

/**
 * Returns whether widget is equal to given object.
 */
public boolean equals(Object anObj)
{
    if(anObj instanceof String) return anObj.equals(getName());
    if(anObj instanceof View) return getView()==anObj;
    return super.equals(anObj);
}

/**
 * Standard toString implementation.
 */
public String toString()  { return getClass().getSimpleName() + " " + getName() + " " + getType(); }

/**
 * Types for events.
 */
public enum Type {
    
    /** Action event. */
    Action,

    /** Key events. */
    KeyPress, KeyRelease, KeyType,
    
    /** Mouse events.*/
    MousePress, MouseDrag, MouseRelease, MouseEnter, MouseMove, MouseExit,
        
    /** Scroll event. */
    Scroll,
    
    /** Drag events. */
    DragEnter, DragOver, DragExit, DragDrop,
    
    /** DragSource events. */
    DragGesture, DragSourceEnter, DragSourceOver, DragSourceExit, DragSourceEnd,
        
    /** Window events. */
    WinActivate, WinDeactivate, WinOpen, WinClose
}

/** Prints "not implemented" for string (method name). */
public void complain(String s)  { String msg = getClass().getSimpleName() + ": Not implemented:" + s;
    if(!_cmpln.contains(msg)) System.err.println(msg); _cmpln.add(msg); } static Set _cmpln = new HashSet();

}