package snap.webapi;

/**
 * This class is a wrapper for Web API WheelEvent (https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent).
 */
public class WheelEvent extends MouseEvent {

    /**
     * Constructor.
     */
    public WheelEvent(Object eventJS)
    {
        super(eventJS);
    }

    public int getDeltaX()  { return getMemberInt("deltaX"); }

    public int getDeltaY()  { return getMemberInt("deltaY"); }
}
