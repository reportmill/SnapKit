package snap.webapi;

/**
 * This class is a wrapper for Web API Touch (https://developer.mozilla.org/en-US/docs/Web/API/Touch).
 */
public class Touch extends Event {

    /**
     * Constructor.
     */
    public Touch(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Touch method: getClientX().
     */
    public int getClientX()  { return getMemberInt("clientX"); }

    /**
     * Touch method: getClientY().
     */
    public int getClientY()  { return getMemberInt("clientY"); }

    /**
     * Touch method: getPageX().
     */
    public int getPageX()  { return getMemberInt("pageX"); }

    /**
     * Touch method: getPageY().
     */
    public int getPageY()  { return getMemberInt("pageY"); }

    /**
     * Touch method: getScreenX().
     */
    public int getScreenX()  { return getMemberInt("screenX"); }

    /**
     * Touch method: getScreenY().
     */
    public int getScreenY()  { return getMemberInt("screenY"); }

    //int getOffsetX();

    //int getOffsetY();

    //int getPageX();

    //int getPageY();

    //boolean getCtrlKey();

    //boolean getShiftKey();

    //boolean getAltKey();

    //boolean getMetaKey();
}