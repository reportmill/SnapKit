package snap.webapi;

/**
 * This class is a wrapper for Web API UIEvent (https://developer.mozilla.org/en-US/docs/Web/API/UIEvent).
 */
public class UIEvent extends Event {

    /**
     * Constructor.
     */
    public UIEvent(Object eventJS)
    {
        super(eventJS);
    }

    public boolean isShiftKey()  { return getMemberBoolean("shiftKey"); }

    public boolean isAltKey()  { return getMemberBoolean("altKey"); }

    public boolean isCtrlKey()  { return getMemberBoolean("ctrlKey"); }

    public boolean isMetaKey()  { return getMemberBoolean("metaKey"); }
}
