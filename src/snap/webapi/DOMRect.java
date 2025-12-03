package snap.webapi;

/**
 * This class is a wrapper for Web API DOMRect (https://developer.mozilla.org/en-US/docs/Web/API/DOMRect).
 */
public class DOMRect extends JSProxy {

    /**
     * Constructor.
     */
    public DOMRect(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Return canvas X.
     */
    public int getX()  { return getMemberInt("x"); }

    /**
     * Set canvas X.
     */
    public void setX(int aValue)  { setMemberInt("x", aValue); }

    /**
     * Return canvas Y.
     */
    public int getY()  { return getMemberInt("y"); }

    /**
     * Set canvas Y.
     */
    public void setY(int aValue)  { setMemberInt("y", aValue); }

    /**
     * Return canvas width.
     */
    public int getWidth()  { return getMemberInt("width"); }

    /**
     * Set canvas height.
     */
    public void setWidth(int aValue)  { setMemberInt("width", aValue); }

    /**
     * Return canvas height.
     */
    public int getHeight()  { return getMemberInt("height"); }

    /**
     * Set canvas height.
     */
    public void setHeight(int aValue)  { setMemberInt("height", aValue);}
}
