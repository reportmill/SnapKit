package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLImageElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement).
 */
public class HTMLCanvasElement extends HTMLElement implements CanvasImageSource {

    /**
     * Constructor.
     */
    public HTMLCanvasElement(Object jsObj)
    {
        super(jsObj);
    }

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

    /**
     * HTMLCanvasElement: getContext().
     */
    public Object getContext(String contextType)
    {
        Object contextJS = call("getContext", contextType);
        return WebEnv.get().getRenderingContext(contextType, contextJS);
    }

    /**
     * Returns a DOMRect object providing information about the size of an element and its position relative to the viewport
     */
    public DOMRect getBoundingClientRect()
    {
        Object contextJS = call("getBoundingClientRect");
        return new DOMRect(contextJS);
    }

    /**
     * HTMLCanvasElement: toDataURL()
     */
    public String toDataURL(String mimeType)  { return (String) call("toDataURL", mimeType); }
}
