package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLAnchorElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLAnchorElement).
 */
public class HTMLAnchorElement extends HTMLElement {

    /**
     * Constructor.
     */
    public HTMLAnchorElement(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Sets the anchor HREF link address.
     */
    public void setHref(String linkAddress)  { setMemberString("href", linkAddress); }

    /**
     * Sets the anchor HREF link address.
     */
    public void setDownload(String filename)  { setMemberString("download", filename); }
}
