package snap.webapi;

/**
 * This class is a wrapper for Web API MouseEvent (https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent).
 */
public class MouseEvent extends UIEvent {

    // Constants for types
    public static final String CLICK = "click";
    public static final String MOUSEDOWN = "mousedown";
    public static final String MOUSEUP = "mouseup";
    public static final String MOUSEOVER = "mouseover";
    public static final String MOUSEMOVE = "mousemove";
    public static final String MOUSEOUT = "mouseout";

    // Constants for buttons
    public static final short LEFT_BUTTON = 0;
    public static final short MIDDLE_BUTTON = 1;
    public static final short RIGHT_BUTTON = 2;

    /**
     * Constructor.
     */
    public MouseEvent(Object eventJS)
    {
        super(eventJS);
    }

    /**
     * MouseEvent method: getClientX().
     */
    public int getClientX()  { return getMemberInt("clientX"); }

    /**
     * MouseEvent method: getClientY().
     */
    public int getClientY()  { return getMemberInt("clientY"); }

    /**
     * MouseEvent method: getPageX().
     */
    public int getPageX()  { return getMemberInt("pageX"); }

    /**
     * MouseEvent method: getPageY().
     */
    public int getPageY()  { return getMemberInt("pageY"); }

    /**
     * MouseEvent method: getScreenX().
     */
    public int getScreenX()  { return getMemberInt("screenX"); }

    /**
     * MouseEvent method: getScreenY().
     */
    public int getScreenY()  { return getMemberInt("screenY"); }

    public int getButton()  { return getMemberInt("button"); }

    public int getButtons()  { return getMemberInt("buttons"); }

    //int getOffsetX();

    //int getOffsetY();

    //EventTarget getRelatedTarget();

    //double getMovementX();

    //double getMovementY();
}
