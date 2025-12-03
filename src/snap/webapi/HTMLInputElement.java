package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLInputElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLInputElement).
 */
public class HTMLInputElement extends HTMLElement implements CanvasImageSource {

    /**
     * Constructor.
     */
    public HTMLInputElement(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Returns the input type.
     */
    public String getType()  { return getMemberString("type"); }

    /**
     * Sets the input type.
     */
    public void setType(String aType)  { setMemberString("type", aType); }

    /**
     * Sets a string that represents the element's accept attribute, containing comma-separated list of file types that can be selected.
     */
    public void setAccept(String fileTypes)  { setMemberString("accept", fileTypes); }

    /**
     * Sets a string that represents the element's accept attribute, containing comma-separated list of file types that can be selected.
     */
    public void setAcceptTypes(String[] fileTypes)
    {
        String acceptTypesStr = String.join(",", fileTypes);
        setAccept(acceptTypesStr);
    }
}
