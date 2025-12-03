package snap.webapi;

/**
 * This class is a wrapper for Web API KeyboardEvent (https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent).
 */
public class KeyboardEvent extends UIEvent {

    // Constants for types
    public static final String MOUSEDOWN = "mousedown";
    public static final String MOUSEUP = "mouseup";

    // Constants for buttons
    public static final short LEFT_BUTTON = 0;
    public static final short MIDDLE_BUTTON = 1;
    public static final short RIGHT_BUTTON = 2;

    /**
     * Constructor.
     */
    public KeyboardEvent(Object eventJS)
    {
        super(eventJS);
    }

    public int getKeyCode()  { return getMemberInt("keyCode"); }

    public String getKey()  { return getMemberString("key"); }
}
