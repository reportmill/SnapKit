/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.Point;
import snap.util.*;

/**
 * Represents node UI events sent to a Views for input events.
 */
public abstract class ViewEvent implements Cloneable {
    
    // The view associated with this event
    private View  _view;
    
    // The platform specific event, if available
    private Object  _event;

    // The name of the event node
    private String  _name = "";
    
    // The UI event type
    protected Type  _type;
    
    // When the event was sent
    private long  _when;
    
    // The mouse location
    private double  _mx = Float.MIN_VALUE;
    private double  _my = Float.MIN_VALUE;

    // The mouse location (point)
    private Point  _point;
    
    // The click count
    private int  _clickCount = 1;
    
    // The event that precipitated this event (usually null)
    private ViewEvent  _parent;
    
    // Whether event was consumed
    private boolean  _consumed;

    // The KeyCombo for event (if Key event)
    private KeyCombo  _keyCombo;

    // Constants for double/triple click constraints
    private static int  CLICK_TIME = 500;
    private static double  CLICK_DIST = 1;

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
    public void setName(String aName)
    {
        _name = aName != null ? aName : "";
    }

    /**
     * Returns the event type.
     */
    public Type getType()
    {
        if (_type != null) return _type;
        return _type = getTypeImpl();
    }

    /**
     * Returns the event type.
     */
    protected abstract Type getTypeImpl();

    /**
     * Sets the event type.
     */
    public void setType(Type aType)  { _type = aType; }

    /**
     * Sets the event that precipitated this event (optional).
     */
    public ViewEvent getParentEvent()  { return _parent; }

    /**
     * Sets the event that precipitated this event (optional).
     */
    protected void setParentEvent(ViewEvent anEvent)  { _parent = anEvent; }

    /**
     * Sets the event that precipitated this event (optional).
     */
    public ViewEvent getRootEvent()
    {
        ViewEvent root = _parent;
        while (root != null && root._parent != null)
            root = root._parent;
        return root;
    }

    /**
     * Returns the time the event was sent.
     */
    public long getWhen()
    {
        if (_when > 0) return _when;
        return _when = System.currentTimeMillis();
    }

    /**
     * Returns whether event is action event.
     */
    public boolean isActionEvent()  { return getType() == Type.Action; }

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
    public boolean isMouseClick()
    {
        return isMouseRelease() && isClickCandidate();
    }

    /**
     * Returns whether event is mouse clicked.
     */
    public boolean isEventWithinTimeAndDist(int timeMillis, double aDist)
    {
        // Get last MouseDown (just return if null)
        ViewEvent last = ViewUtils.getMouseDown(); if (last == null) return false;

        // If event not within given time of last mouse, return false
        if (timeMillis > 0) {
            long time = getWhen() - last.getWhen();
            if (time > timeMillis)
                return false;
        }

        // If event is in another window, return false
        if (getView().getWindow() != last.getView().getWindow())
            return false;

        // If event not within 1 point of last mouse, return false
        Point lastPoint = getView() == last.getView() ? last.getPoint() : last.getPoint(getView());
        double dist = lastPoint.getDistance(getX(), getY());
        if (dist > aDist)
            return false;

        // Return true since passed tests
        return true;
    }

    /**
     * Returns whether event is mouse event in mouse click range (within half second of down
     */
    public boolean isClickCandidate()
    {
        return isEventWithinTimeAndDist(CLICK_TIME, CLICK_DIST);
    }

    /**
     * Returns whether event is mouse entered.
     */
    public boolean isMouseEnter()  { return getType() == Type.MouseEnter; }

    /**
     * Returns whether event is mouse moved.
     */
    public boolean isMouseMove()  { return getType() == Type.MouseMove; }

    /**
     * Returns whether event is mouse exited.
     */
    public boolean isMouseExit()  { return getType() == Type.MouseExit; }

    /**
     * Returns whether event is key event.
     */
    public boolean isKeyEvent()
    {
        Type type = getType();
        return type == Type.KeyPress || type == Type.KeyType || type == Type.KeyRelease;
    }

    /**
     * Returns whether event is key pressed.
     */
    public boolean isKeyPress()  { return getType() == Type.KeyPress; }

    /**
     * Returns whether event is key released.
     */
    public boolean isKeyRelease()  { return getType() == Type.KeyRelease; }

    /**
     * Returns whether event is key typed.
     */
    public boolean isKeyType()  { return getType() == Type.KeyType; }

    /**
     * Returns whether event is scroll.
     */
    public boolean isScroll()  { return getType() == Type.Scroll; }

    /**
     * Returns whether event is any drag event.
     */
    public boolean isDragEvent()
    {
        Type type = getType();
        return type == Type.DragEnter || type == Type.DragOver || type == Type.DragExit || type == Type.DragDrop;
    }

    /**
     * Returns whether event is drag enter.
     */
    public boolean isDragEnter()  { return getType() == Type.DragEnter; }

    /**
     * Returns whether event is drag over.
     */
    public boolean isDragOver()  { return getType() == Type.DragOver; }

    /**
     * Returns whether event is drag exit.
     */
    public boolean isDragExit()  { return getType() == Type.DragExit; }

    /**
     * Returns whether event is drop event.
     */
    public boolean isDragDrop()  { return getType() == Type.DragDrop; }

    /**
     * Returns whether event is drop event.
     */
    public boolean isDragDropEvent()  { return getType() == Type.DragDrop; }

    /**
     * Returns whether event is drag source event.
     */
    public boolean isDragSourceEvent()
    {
        Type type = getType();
        return type == Type.DragGesture || type == Type.DragSourceEnter || type == Type.DragSourceOver ||
            type == Type.DragSourceExit || type == Type.DragSourceEnd;
    }

    /**
     * Returns whether event is DragGesture event.
     */
    public boolean isDragGesture()  { return getType() == Type.DragGesture; }

    /**
     * Returns whether event is DragSourceEnter event.
     */
    public boolean isDragSourceEnter()  { return getType() == Type.DragSourceEnter; }

    /**
     * Returns whether event is DragSourceExit event.
     */
    public boolean isDragSourceExit()  { return getType() == Type.DragSourceExit; }

    /**
     * Returns whether event is DragSourceEnd event.
     */
    public boolean isDragSourceEnd()  { return getType() == Type.DragSourceEnd; }

    /**
     * Returns whether event is WinActivated.
     */
    public boolean isWinActivate()  { return getType() == Type.WinActivate; }

    /**
     * Returns whether event is WinDeactivated.
     */
    public boolean isWinDeativate()  { return getType() == Type.WinDeactivate; }

    /**
     * Returns whether event is WinOpened.
     */
    public boolean isWinOpen()  { return getType() == Type.WinOpen; }

    /**
     * Returns whether event is WinClosing.
     */
    public boolean isWinClose()  { return getType() == Type.WinClose; }

    /**
     * Returns the value encapsulated by the event widget.
     */
    public Object getValue()
    {
        // Handle DragDropEvent: Return String value
        if (isDragDrop())
            return getClipboard().getString();

        // Otherwise, return node value
        View view = getView();
        return view != null ? view.getPropValue("Value") : null;
    }

    /**
     * Sets the value encapsulated by the event widget.
     */
    public void setValue(Object aValue)  { getView().setPropValue("Value", aValue); }

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
    public int getSelIndex()  { return getView(Selectable.class).getSelIndex(); }

    /**
     * Returns the selected item for encapsulated widget.
     */
    public Object getSelItem()  { return getView(Selectable.class).getSelItem(); }

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
        if (_keyCombo != null) return _keyCombo;
        return _keyCombo = new KeyCombo(getKeyCode(), isAltDown(), isMetaDown(), isControlDown(), isShiftDown());
    }

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
    public void setClickCount(int aValue)  { _clickCount = aValue; }

    /**
     * Returns the mouse event x.
     */
    public double getX()
    {
        if (_mx != Float.MIN_VALUE) return _mx;
        return _mx = getPoint().x;
    }

    /**
     * Returns the mouse event y.
     */
    public double getY()
    {
        if (_my != Float.MIN_VALUE) return _my;
        return _my = getPoint().y;
    }

    /**
     * Sets the x/y.
     */
    protected void setXY(double aX, double aY)
    {
        _mx = aX; _my = aY;
        _point = new Point(_mx, _my);
    }

    /**
     * Returns the event location.
     */
    public Point getPoint()
    {
        if (_point != null) return _point;
        return _point = getPointImpl();
    }

    /**
     * Returns the event location.
     */
    protected Point getPointImpl()
    {
        return new Point(getX(), getY());
    }

    /**
     * Returns the event location in coords of given view.
     */
    public Point getPoint(View aView)
    {
        Point pt = getPoint(); View view0 = getView();
        View ancestor = ViewUtils.getCommonAncetor(view0,aView);
        if (ancestor != view0)
            pt = view0.localToParent(pt.x, pt.y, ancestor);
        if (ancestor != aView)
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
    public char getKeyChar()
    {
        String s = getKeyString();
        return s != null && s.length() > 0 ? s.charAt(0) : (char) 0;
    }

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
    public void consume()
    {
        _consumed = true;
        if (_parent != null)
            _parent.consume();
    }

    /**
     * Returns a ViewEvent at new point.
     */
    public ViewEvent copyForView(View aView)
    {
        View thisView = getView(); double x = getX(), y = getY(); if (aView == thisView) return this;
        View par = ViewUtils.getCommonAncetor(thisView, aView);
        Point point = par == thisView ? aView.parentToLocal(x,y,par) : thisView.localToParent(x,y,par);
        return copyForViewPoint(aView, point.x, point.y, -1);
    }

    /**
     * Returns a ViewEvent at new point.
     */
    public ViewEvent copyForPoint(double aX, double aY)
    {
        return copyForViewPoint(getView(), aX, aY, -1);
    }

    /**
     * Returns a ViewEvent at new click count.
     */
    public ViewEvent copyForClickCount(int aClickCount)
    {
        return copyForViewPoint(getView(), getX(), getY(), aClickCount);
    }

    /**
     * Returns a ViewEvent at new point.
     */
    public ViewEvent copyForViewPoint(View aView, double aX, double aY, int aClickCount)
    {
        ViewEvent copy = clone(); //getEnv().createEvent(aView, getEvent(), getType(), name);
        copy.setView(aView);
        String name = getName();
        if (name == null || name.length() == 0 || name.equals(getView().getName()))
            name = aView.getName();
        copy.setName(name);
        copy.setXY(aX, aY);
        if (aClickCount > 0)
            copy.setClickCount(aClickCount);
        return copy;
    }

    /**
     * Copy event.
     */
    public ViewEvent clone()
    {
        ViewEvent copy = null; try { copy = (ViewEvent)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
        copy.setParentEvent(this);
        return copy;
    }

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
        if (anObj instanceof String)
            return anObj.equals(getName());
        if (anObj instanceof View)
            return getView() == anObj;
        return super.equals(anObj);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getClass().getSimpleName() + " { ";
        str += "Type:" + getType();
        if (getName() != null && getName().length() > 0)
            str += ", Name:" + getName();
        if (isMouseEvent())
            str += ", X:" + getX() + ", Y:" + getY() + ", ClickCount:" + getClickCount();
        if (isKeyEvent())
            str += ", KeyChar:" + getKeyString() + ", KeyCode:" + getKeyCode();
        if (getView() != null)
            str += ", View:" + getView().getClass().getSimpleName();
        str += ", Consumed:" + isConsumed() + " }";
        return str;
    }

    /**
     * Creates an Event.
     */
    public static ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName)
    {
        ViewEvent event = ViewEnv.getEnv().createEvent(aView, anEvent, aType, aName);
        return event;
    }

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
    void complain(String s)
    {
        String msg = getClass().getSimpleName() + ": Not implemented:" + s;
        if (!_cmpln.contains(msg))
            System.err.println(msg);
        _cmpln.add(msg);
    }
    static Set _cmpln = new HashSet();
}