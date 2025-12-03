package snap.webapi;

/**
 * This class is a wrapper for Web API Document (https://developer.mozilla.org/en-US/docs/Web/API/Document).
 */
public class Document extends Node implements EventTarget {

    // The document HTML element
    private HTMLHtmlElement _html;

    // The Body element
    private HTMLBodyElement _body;

    /**
     * Constructor.
     */
    public Document(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Returns Document.documentElement for this document.
     */
    public HTMLHtmlElement getDocumentElement()
    {
        if (_html != null) return _html;
        Object htmlElementJS = getMember("documentElement");
        return _html = new HTMLHtmlElement(htmlElementJS);
    }

    /**
     * Returns the HTMLBodyElement for this document.
     */
    public HTMLBodyElement getBody()
    {
        if (_body != null) return _body;
        Object bodyJS = getMember("body");
        return _body = new HTMLBodyElement(bodyJS);
    }

    /**
     * Returns the HTMLHeadElement for this document.
     */
    public HTMLHeadElement getHead()
    {
        Object headJS = getMember("head");
        return new HTMLHeadElement(headJS);
    }

    /**
     * Returns an object reference to the identified element.
     */
    public HTMLElement getElementById(String idStr)
    {
        Object elementJS = call("getElementById", idStr);
        if (elementJS == null)
            return null;
        String tagName = WebEnv.get().getMemberString(elementJS, "nodeName");
        return HTMLElement.getElementForName(tagName, elementJS);
    }

    /**
     * Creates the HTML element specified by tagName.
     */
    public HTMLElement createElement(String tagName)
    {
        Object elementJS = call("createElement", tagName);
        return HTMLElement.getElementForName(tagName, elementJS);
    }
}
