package snap.webapi;

/**
 * This class is a wrapper for Web API HTMLBodyElement (https://developer.mozilla.org/en-US/docs/Web/API/HTMLBodyElement).
 */
public class HTMLBodyElement extends HTMLElement {

    /**
     * Constructor.
     */
    public HTMLBodyElement(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Returns the body element.
     */
    public static HTMLBodyElement getBody()  { return HTMLDocument.getDocument().getBody(); }
}
